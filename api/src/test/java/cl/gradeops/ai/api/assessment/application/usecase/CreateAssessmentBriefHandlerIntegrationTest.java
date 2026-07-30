package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceMapper;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyKeyPayloadMismatchException;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence.IdempotencyRecordJpaRepository;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence.IdempotencyRecordPersistenceAdapter;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence.IdempotencyRecordPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises {@link CreateAssessmentBriefHandler} with real repositories against a live Postgres
 * (A3 Contract Correction § Correction 1) — the atomic Assessment/AssessmentBrief/IdempotencyRecord
 * write, the concurrent-duplicate-request race resolved via {@code idempotency_records}' own
 * unique constraint, and the "no record survives a genuinely failed creation" guarantee. Requires
 * Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class CreateAssessmentBriefHandlerIntegrationTest {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired AssessmentJpaRepository assessmentJpaRepository;
    @Autowired AssessmentBriefJpaRepository briefJpaRepository;
    @Autowired IdempotencyRecordJpaRepository idempotencyRecordJpaRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentBriefPersistenceAdapter briefAdapter;
    CreateAssessmentBriefHandler handler;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefJpaRepository, new AssessmentBriefPersistenceMapper());
        IdempotencyRecordPersistenceAdapter idempotencyAdapter = new IdempotencyRecordPersistenceAdapter(
                idempotencyRecordJpaRepository, new IdempotencyRecordPersistenceMapper());
        IdempotencyGuard idempotencyGuard = new IdempotencyGuard(idempotencyAdapter);
        handler = new CreateAssessmentBriefHandler(assessmentAdapter, briefAdapter, idempotencyGuard, transactionManager);

        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
    }

    private static CreateAssessmentBriefCommand command(String teacherUid, String key) {
        return new CreateAssessmentBriefCommand(teacherUid, "Evaluate loops", "Java loops", "basic", "90min", "Java", key);
    }

    @Test
    void shouldCreateExactlyOneAssessmentAndOneIdempotencyRecordOnFirstCall() {
        CreateAssessmentBriefResult result = handler.execute(command("uid-1", "key-1"));
        entityManager.flush();
        entityManager.clear();

        assertThat(assessmentJpaRepository.findAll()).hasSize(1);
        assertThat(briefJpaRepository.findAll()).hasSize(1);
        assertThat(result.assessmentId()).isEqualTo(assessmentJpaRepository.findAll().get(0).getId().toString());
    }

    @Test
    void shouldReplaySameAssessmentIdWithoutCreatingASecondAssessmentOnRepeatedCall() {
        CreateAssessmentBriefResult first = handler.execute(command("uid-1", "same-key"));
        entityManager.flush();
        entityManager.clear();

        CreateAssessmentBriefResult replayed = handler.execute(command("uid-1", "same-key"));

        assertThat(replayed.assessmentId()).isEqualTo(first.assessmentId());
        assertThat(assessmentJpaRepository.findAll()).hasSize(1);
        assertThat(briefJpaRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldRejectSameKeyWithDifferentPayloadAsIdempotencyMismatch() {
        handler.execute(command("uid-1", "mismatch-key"));
        entityManager.flush();
        entityManager.clear();

        CreateAssessmentBriefCommand different = new CreateAssessmentBriefCommand(
                "uid-1", "Different goal", "Java loops", "basic", "90min", "Java", "mismatch-key");

        assertThatThrownBy(() -> handler.execute(different))
                .isInstanceOf(IdempotencyKeyPayloadMismatchException.class);
        assertThat(assessmentJpaRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldNotCollideAcrossTwoDifferentTeachersUsingTheSameKey() {
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-2", "Other", "Teacher", "uid-2@test.com");

        CreateAssessmentBriefResult first = handler.execute(command("uid-1", "shared-key"));
        entityManager.flush();
        entityManager.clear();
        CreateAssessmentBriefResult second = handler.execute(command("uid-2", "shared-key"));

        assertThat(second.assessmentId()).isNotEqualTo(first.assessmentId());
        assertThat(assessmentJpaRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldLeaveNoIdempotencyRecordWhenTheCreationTransactionFails() {
        // "unknown-teacher" is never inserted into `teacher` — the Assessment insert violates
        // the teacher_uid FK, so the whole transaction (Assessment + Brief + IdempotencyRecord)
        // rolls back. The genuinely-failed key must remain retryable, not permanently blocked by
        // a half-written idempotency record.
        //
        // @DataJpaTest wraps this method in an ambient transaction that the handler's own
        // TransactionTemplate would otherwise merely PARTICIPATE in (deferring the real
        // INSERT/commit indefinitely instead of physically committing). Ending the ambient
        // transaction first makes the handler's transaction genuinely independent — exactly as
        // it is in production — so the FK violation is a real, synchronous Postgres error and
        // (since a failed transaction can never be committed) TestTransaction.flagForCommit()
        // here is a no-op for this specific call, not a real permanent commit. Plain JdbcTemplate
        // queries (not JPA reads) verify the outcome, since JPA repository access requires an
        // active test-managed transaction that intentionally isn't open here.
        TestTransaction.flagForCommit();
        TestTransaction.end();
        try {
            assertThatThrownBy(() -> handler.execute(command("unknown-teacher", "retryable-key")))
                    .isInstanceOf(DataIntegrityViolationException.class);

            assertThat(jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM idempotency_records WHERE idempotency_key = ?",
                    Integer.class, "retryable-key")).isEqualTo(0);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM assessments WHERE teacher_uid = ?",
                    Integer.class, "unknown-teacher")).isEqualTo(0);

            jdbcTemplate.update(
                    "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                    "unknown-teacher", "Now", "Exists", "now-exists@test.com");

            CreateAssessmentBriefResult result = handler.execute(command("unknown-teacher", "retryable-key"));

            assertThat(result.assessmentId()).isNotNull();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM assessments WHERE teacher_uid = ?",
                    Integer.class, "unknown-teacher")).isEqualTo(1);
        } finally {
            jdbcTemplate.update("DELETE FROM idempotency_records WHERE teacher_uid IN ('uid-1','unknown-teacher')");
            jdbcTemplate.update("DELETE FROM assessment_briefs WHERE assessment_id IN "
                    + "(SELECT id FROM assessments WHERE teacher_uid IN ('uid-1','unknown-teacher'))");
            jdbcTemplate.update("DELETE FROM assessments WHERE teacher_uid IN ('uid-1','unknown-teacher')");
            jdbcTemplate.update("DELETE FROM teacher WHERE firebase_uid IN ('uid-1','unknown-teacher')");
            TestTransaction.start();
        }
    }

    @Test
    void shouldResultInExactlyOneAssessmentWhenTwoConcurrentRequestsShareTheSameKeyAndPayload() throws Exception {
        String teacherUid = "uid-concurrent-create-" + java.util.UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                teacherUid, "Test", "Teacher", teacherUid + "@test.com");
        entityManager.flush();
        entityManager.clear();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        try {
            Callable<Object> attempt = () -> {
                ready.countDown();
                go.await();
                return handler.execute(command(teacherUid, "concurrent-key"));
            };

            List<Future<Object>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(attempt));
            }
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            go.countDown();

            List<Object> results = new ArrayList<>();
            for (Future<Object> f : futures) {
                results.add(f.get(15, TimeUnit.SECONDS));
            }

            List<String> assessmentIds = results.stream()
                    .map(CreateAssessmentBriefResult.class::cast)
                    .map(CreateAssessmentBriefResult::assessmentId)
                    .distinct()
                    .toList();

            assertThat(assessmentIds)
                    .withFailMessage(() -> "both requests must observe the same logical result, got: " + assessmentIds)
                    .hasSize(1);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM assessments WHERE teacher_uid = ?", Integer.class, teacherUid))
                    .isEqualTo(1);
        } finally {
            executor.shutdown();
            // TestTransaction.flagForCommit() above also permanently committed @BeforeEach's
            // "uid-1" insert (same transaction) — it must be cleaned up here too, or every later
            // test's own @BeforeEach insert fails with a duplicate-key violation (see the
            // identical reasoning in RetryGenerationHandlerIntegrationTest's concurrent test).
            jdbcTemplate.update("DELETE FROM idempotency_records WHERE teacher_uid IN (?, 'uid-1')", teacherUid);
            jdbcTemplate.update("DELETE FROM assessment_briefs WHERE assessment_id IN "
                    + "(SELECT id FROM assessments WHERE teacher_uid IN (?, 'uid-1'))", teacherUid);
            jdbcTemplate.update("DELETE FROM assessments WHERE teacher_uid IN (?, 'uid-1')", teacherUid);
            jdbcTemplate.update("DELETE FROM teacher WHERE firebase_uid IN (?, 'uid-1')", teacherUid);
            TestTransaction.start();
        }
    }
}
