package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.CreateHumanRevisionCommand;
import cl.gradeops.ai.api.assessment.application.exception.StaleRevisionException;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.assessment.domain.model.RevisionOrigin;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionPersistenceMapper;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises {@link CreateHumanRevisionHandler} with real repositories against a live Postgres
 * (Flyway-migrated through V16): generates v1 directly as an {@code AssessmentRevision} (the
 * durable model Task 07B's {@code GenerateAssessmentDraftHandler} now produces), edits it, and
 * verifies the new revision chains correctly, the old one is untouched, and two genuinely
 * concurrent edits from the same {@code expectedRevisionId} produce exactly one success and one
 * {@code STALE_REVISION} conflict. Requires Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class CreateHumanRevisionHandlerIntegrationTest {

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
    @Autowired AssessmentRevisionJpaRepository revisionJpaRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentRevisionPersistenceAdapter revisionAdapter;
    CreateHumanRevisionHandler handler;

    Assessment assessment;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        AssessmentBriefPersistenceAdapter briefAdapter =
                new AssessmentBriefPersistenceAdapter(briefJpaRepository, new AssessmentBriefPersistenceMapper());
        revisionAdapter = new AssessmentRevisionPersistenceAdapter(revisionJpaRepository, new AssessmentRevisionPersistenceMapper());
        OwnershipVerifier ownershipVerifier = new OwnershipVerifier();

        handler = new CreateHumanRevisionHandler(assessmentAdapter, revisionAdapter, ownershipVerifier, transactionManager);

        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java"));
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * {@code sourceAgentAttemptId} is left {@code null} via {@code restore(...)} — the FK column
     * genuinely references {@code agent_attempts(id)}, and this test seeds v1 directly without
     * ever dispatching a real {@code AgentAttempt}, so a fabricated random id would violate the
     * constraint (a real one is exercised end-to-end by {@code AiOperationCoordinatorTest}/
     * {@code GenerateAssessmentDraftHandlerIntegrationTest} instead).
     */
    private GenerateAssessmentDraftResult seedV1Revision() {
        AssessmentRevision v1 = AssessmentRevision.restore(UUID.randomUUID(), assessment.getId(), 1, null,
                RevisionOrigin.AI_GENERATED, "uid-1", null, null, "Title v1", "Context v1", "Instructions v1",
                List.of("obj1"), List.of("del1"), List.of("con1"), java.time.Instant.now());
        revisionAdapter.save(v1);
        assessmentAdapter.save(assessment.withCurrentRevision(v1.getId()));
        entityManager.flush();
        entityManager.clear();
        return GenerateAssessmentDraftResult.fromRevision(v1);
    }

    @Test
    void shouldCreateHumanRevisionAndUpdateCurrentRevisionOnSuccess() {
        GenerateAssessmentDraftResult v1 = seedV1Revision();

        GenerateAssessmentDraftResult edited = handler.execute(new CreateHumanRevisionCommand(
                assessment.getId().value(), "uid-1", v1.draftId(), "Edited title", "Edited context",
                "Edited instructions", List.of("obj2"), List.of("del2"), List.of("con2"), "fixed a typo"));

        entityManager.flush();
        entityManager.clear();

        assertThat(edited.draftId()).isNotEqualTo(v1.draftId());
        assertThat(edited.versionNumber()).isEqualTo(2);
        assertThat(edited.previousRevisionId()).isEqualTo(v1.draftId());
        assertThat(edited.origin()).isEqualTo(RevisionOrigin.HUMAN_EDITED.name());
        assertThat(edited.actorId()).isEqualTo("uid-1");
        assertThat(edited.reason()).isEqualTo("fixed a typo");

        Assessment reloaded = assessmentAdapter.findById(assessment.getId()).orElseThrow();
        assertThat(reloaded.getCurrentRevisionId()).isEqualTo(edited.draftId());
    }

    @Test
    void shouldLeaveThePreviousRevisionRowByteForByteUnchanged() {
        GenerateAssessmentDraftResult v1 = seedV1Revision();

        handler.execute(new CreateHumanRevisionCommand(assessment.getId().value(), "uid-1", v1.draftId(),
                "Edited title", "Edited context", "Edited instructions",
                List.of("obj2"), List.of("del2"), List.of("con2"), null));

        entityManager.flush();
        entityManager.clear();

        AssessmentRevision v1Reloaded = revisionAdapter.findById(v1.draftId()).orElseThrow();
        assertThat(v1Reloaded.getTitle()).isEqualTo("Title v1");
        assertThat(v1Reloaded.getContext()).isEqualTo("Context v1");
        assertThat(v1Reloaded.getOrigin()).isEqualTo(RevisionOrigin.AI_GENERATED);
        assertThat(v1Reloaded.getVersionNumber()).isEqualTo(1);
    }

    @Test
    void shouldRejectWithStaleRevisionWhenExpectedRevisionIdIsNoLongerCurrent() {
        seedV1Revision();
        UUID wrongExpected = UUID.randomUUID();

        assertThatThrownBy(() -> handler.execute(new CreateHumanRevisionCommand(
                assessment.getId().value(), "uid-1", wrongExpected, "t", "c", "i",
                List.of(), List.of(), List.of(), null)))
                .isInstanceOf(StaleRevisionException.class);

        assertThat(revisionJpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessment.getId().value()))
                .hasSize(1);
    }

    @Test
    void shouldRejectWhenAssessmentHasNoCurrentRevisionYet() {
        assertThatThrownBy(() -> handler.execute(new CreateHumanRevisionCommand(
                assessment.getId().value(), "uid-1", UUID.randomUUID(), "t", "c", "i",
                List.of(), List.of(), List.of(), null)))
                .isInstanceOf(StaleRevisionException.class);
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessment() {
        GenerateAssessmentDraftResult v1 = seedV1Revision();

        assertThatThrownBy(() -> handler.execute(new CreateHumanRevisionCommand(
                assessment.getId().value(), "someone-else", v1.draftId(), "t", "c", "i",
                List.of(), List.of(), List.of(), null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldAllowExactlyOneOfTwoConcurrentHumanEditsFromTheSameExpectedRevision() throws Exception {
        // Isolated teacher/assessment (not the class-level "uid-1" fixture): this test commits
        // its seed data for real (see below) so two independent worker-thread transactions can
        // see it, and that commit cannot be scoped to just the revision row — it commits
        // everything accumulated in the current test's transaction, including @BeforeEach's
        // "uid-1" teacher insert. Reusing "uid-1" here would permanently leak that row past this
        // test (③DataJpaTest's per-test rollback no longer applies once committed), breaking
        // every later test's own @BeforeEach insert with a duplicate-key violation.
        String teacherUid = "uid-concurrent-" + UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                teacherUid, "Test", "Teacher", teacherUid + "@test.com");
        Assessment concurrentAssessment = Assessment.create(teacherUid);
        assessmentAdapter.save(concurrentAssessment);
        AssessmentRevision v1 = AssessmentRevision.restore(UUID.randomUUID(), concurrentAssessment.getId(), 1, null,
                RevisionOrigin.AI_GENERATED, teacherUid, null, null, "Title v1", "Context v1", "Instructions v1",
                List.of("obj1"), List.of("del1"), List.of("con1"), java.time.Instant.now());
        revisionAdapter.save(v1);
        assessmentAdapter.save(concurrentAssessment.withCurrentRevision(v1.getId()));
        entityManager.flush();
        entityManager.clear();

        // Commit the seed data so the two worker threads below (each its own DB transaction) can
        // actually see it — @DataJpaTest's implicit per-test transaction would otherwise hide
        // uncommitted state from any transaction other than the main test thread's own.
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
                try {
                    return handler.execute(new CreateHumanRevisionCommand(concurrentAssessment.getId().value(), teacherUid,
                            v1.getId(), "Edited concurrently", "Context", "Instructions",
                            List.of("obj"), List.of("del"), List.of("con"), null));
                } catch (RuntimeException ex) {
                    return ex;
                }
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

            long successCount = results.stream().filter(GenerateAssessmentDraftResult.class::isInstance).count();
            long conflictCount = results.stream().filter(StaleRevisionException.class::isInstance).count();

            assertThat(successCount)
                    .withFailMessage(() -> "results were: " + results.stream()
                            .map(r -> r.getClass().getName() + (r instanceof Throwable t ? (": " + t.getMessage()) : ""))
                            .toList())
                    .isEqualTo(1);
            assertThat(conflictCount).isEqualTo(1);
            assertThat(revisionJpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(concurrentAssessment.getId().value()))
                    .hasSize(2);
        } finally {
            executor.shutdown();
            // Clean up this test's own committed rows (while still outside any transaction, so
            // the deletes themselves commit) so it does not permanently grow the database across
            // repeated local runs — other tests never commit, so need no equivalent cleanup; this
            // test is the sole exception, precisely because it must commit to prove a real
            // cross-transaction race. Must run before TestTransaction.start() re-establishes the
            // per-test transaction the framework rolls back at teardown, or these deletes would
            // be rolled back too, silently undoing the cleanup. Deleting the assessment row first
            // (not assessment_revisions first) is required: assessments.current_revision_id
            // references assessment_revisions with no cascade in that direction, so deleting a
            // referenced revision first violates the FK; deleting assessments cascades to
            // assessment_revisions (ON DELETE CASCADE from assessments), which a row merely
            // containing a since-deleted FK value does not block.
            jdbcTemplate.update("DELETE FROM assessments WHERE id = ?", concurrentAssessment.getId().value());
            jdbcTemplate.update("DELETE FROM teacher WHERE firebase_uid = ?", teacherUid);
            // @BeforeEach's own "uid-1"/assessment/brief were also committed by the same
            // TestTransaction.end() call above (they were inserted in the same, now-committed
            // transaction) — clean those up too, or the next test's own @BeforeEach insert of
            // "uid-1" fails with a duplicate-key violation.
            jdbcTemplate.update("DELETE FROM assessment_briefs WHERE assessment_id = ?", assessment.getId().value());
            jdbcTemplate.update("DELETE FROM assessments WHERE id = ?", assessment.getId().value());
            jdbcTemplate.update("DELETE FROM teacher WHERE firebase_uid = ?", "uid-1");
            TestTransaction.start();
        }
    }
}
