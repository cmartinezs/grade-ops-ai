package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.RetryGenerationCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoActiveOperationToRetryException;
import cl.gradeops.ai.api.assessment.application.exception.OperationInProgressException;
import cl.gradeops.ai.api.assessment.application.exception.StaleRevisionException;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftOutcome;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.application.result.RetryGenerationResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AiOperationJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AiOperationPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AiOperationPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionPersistenceMapper;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link RetryGenerationHandler} with real repositories against a live Postgres
 * (Flyway-migrated through V16): generate v1 fails transiently (a real {@link
 * AiOperationCoordinator} Phase 0/1/2 run, not a mock of the coordinator), leaving a durable
 * {@code FAILED_RETRYABLE} {@code AiOperation} with one {@code FAILED} {@code AgentAttempt} —
 * then retry dispatches a second {@code AgentAttempt} under the SAME operation and succeeds.
 * Requires Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class RetryGenerationHandlerIntegrationTest {

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
    @Autowired AiOperationJpaRepository aiOperationJpaRepository;
    @Autowired AgentAttemptJpaRepository agentAttemptJpaRepository;
    @Autowired IdempotencyRecordJpaRepository idempotencyRecordJpaRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentBriefPersistenceAdapter briefAdapter;
    AssessmentRevisionPersistenceAdapter revisionAdapter;
    AssessmentAgentClient assessmentAgentClient;
    GenerateAssessmentDraftHandler generateHandler;
    RetryGenerationHandler retryHandler;

    Assessment assessment;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefJpaRepository, new AssessmentBriefPersistenceMapper());
        revisionAdapter = new AssessmentRevisionPersistenceAdapter(revisionJpaRepository, new AssessmentRevisionPersistenceMapper());
        AiOperationPersistenceAdapter aiOperationAdapter =
                new AiOperationPersistenceAdapter(aiOperationJpaRepository, new AiOperationPersistenceMapper());
        AgentAttemptPersistenceAdapter agentAttemptAdapter =
                new AgentAttemptPersistenceAdapter(agentAttemptJpaRepository, new AgentAttemptPersistenceMapper());
        IdempotencyRecordPersistenceAdapter idempotencyAdapter =
                new IdempotencyRecordPersistenceAdapter(idempotencyRecordJpaRepository, new IdempotencyRecordPersistenceMapper());
        IdempotencyGuard idempotencyGuard = new IdempotencyGuard(idempotencyAdapter);
        assessmentAgentClient = mock(AssessmentAgentClient.class);
        JsonMapper jsonMapper = JsonMapper.builder().build();
        OwnershipVerifier ownershipVerifier = new OwnershipVerifier();

        AiOperationCoordinator coordinator = new AiOperationCoordinator(assessmentAdapter, aiOperationAdapter,
                agentAttemptAdapter, revisionAdapter, assessmentAgentClient, jsonMapper, transactionManager);

        generateHandler = new GenerateAssessmentDraftHandler(assessmentAdapter, briefAdapter, revisionAdapter,
                aiOperationAdapter, agentAttemptAdapter, ownershipVerifier, idempotencyGuard, coordinator);
        retryHandler = new RetryGenerationHandler(assessmentAdapter, briefAdapter, aiOperationAdapter,
                agentAttemptAdapter, ownershipVerifier, coordinator);

        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java"));
        entityManager.flush();
        entityManager.clear();
    }

    private static AssessmentAgentResponse response(String title) {
        Instant startedAt = Instant.now().minusSeconds(2);
        Instant finishedAt = Instant.now();
        return new AssessmentAgentResponse(
                new AssessmentAgentResponse.Result(title, "Context " + title, "Instructions " + title,
                        List.of("obj-" + title), List.of("del-" + title), List.of("con-" + title)),
                new AssessmentAgentResponse.Log(UUID.randomUUID(), "assessment", "gemini", "gemini-2.0-flash", "v1",
                        "in-hash-" + title, "out-hash-" + title, 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt));
    }

    /** Leaves a durable FAILED_RETRYABLE AiOperation with one FAILED AgentAttempt — no revision created. */
    private void generateV1FailingTransiently() {
        when(assessmentAgentClient.generate(any(), anyString()))
                .thenThrow(new AgentClientException(AgentClientException.Reason.UNREACHABLE, "connection refused", null));

        GenerateAssessmentDraftOutcome outcome = generateHandler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "gen-key-1"));
        assertThat(outcome).isInstanceOf(GenerateAssessmentDraftOutcome.OperationAccepted.class);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldThrowNoActiveOperationToRetryWhenGenerationWasNeverAttempted() {
        assertThatThrownBy(() -> retryHandler.execute(new RetryGenerationCommand(assessment.getId().value(), "uid-1")))
                .isInstanceOf(NoActiveOperationToRetryException.class);
    }

    @Test
    void shouldDispatchASecondAttemptUnderTheSameOperationAndSucceedOnRetry() {
        generateV1FailingTransiently();
        assertThat(aiOperationJpaRepository.findAll()).hasSize(1);
        UUID operationId = aiOperationJpaRepository.findAll().get(0).getId();
        assertThat(agentAttemptJpaRepository.findAll()).hasSize(1);

        doReturn(response("V1-retry")).when(assessmentAgentClient).generate(any(), anyString());

        RetryGenerationResult result = retryHandler.execute(new RetryGenerationCommand(assessment.getId().value(), "uid-1"));
        entityManager.flush();
        entityManager.clear();

        assertThat(result.status()).isEqualTo("SUCCEEDED");
        assertThat(result.id()).isEqualTo(operationId);

        // Same AiOperation row — no new operation was created.
        assertThat(aiOperationJpaRepository.findAll()).hasSize(1);
        assertThat(aiOperationJpaRepository.findById(operationId).orElseThrow().getStatus()).isEqualTo("SUCCEEDED");

        // A second, distinct AgentAttempt was dispatched under the same operation.
        List<cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptJpaEntity> attempts =
                agentAttemptJpaRepository.findAllByAiOperationIdOrderByAttemptNumberDesc(operationId);
        assertThat(attempts).hasSize(2);
        assertThat(attempts.get(0).getAttemptNumber()).isEqualTo(2);
        assertThat(attempts.get(0).getStatus()).isEqualTo("COMPLETED");
        assertThat(attempts.get(1).getAttemptNumber()).isEqualTo(1);
        assertThat(attempts.get(1).getStatus()).isEqualTo("FAILED");

        Assessment reloaded = assessmentAdapter.findById(assessment.getId()).orElseThrow();
        assertThat(reloaded.getCurrentRevisionId()).isEqualTo(result.resultRevisionId());
    }

    @Test
    void shouldAllowExactlyOneOfTwoConcurrentRetriesOfTheSameFailedOperation() throws Exception {
        String teacherUid = "uid-concurrent-retry-" + UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                teacherUid, "Test", "Teacher", teacherUid + "@test.com");
        Assessment concurrentAssessment = Assessment.create(teacherUid);
        assessmentAdapter.save(concurrentAssessment);
        briefAdapter.save(AssessmentBrief.create(concurrentAssessment.getId(), "goal", "topic", "basic", "90min", "Java"));
        entityManager.flush();
        entityManager.clear();

        AiOperationPersistenceAdapter aiOperationAdapter =
                new AiOperationPersistenceAdapter(aiOperationJpaRepository, new AiOperationPersistenceMapper());
        AgentAttemptPersistenceAdapter agentAttemptAdapter =
                new AgentAttemptPersistenceAdapter(agentAttemptJpaRepository, new AgentAttemptPersistenceMapper());
        cl.gradeops.ai.api.assessment.domain.model.AiOperation failedOp =
                cl.gradeops.ai.api.assessment.domain.model.AiOperation.create(concurrentAssessment.getId(),
                        cl.gradeops.ai.api.assessment.domain.model.AiOperationType.CREATE_INITIAL_REVISION,
                        teacherUid, "gen-key-concurrent", null)
                        .markInProgress().markFailedRetryable();
        aiOperationAdapter.save(failedOp);
        cl.gradeops.ai.api.assessment.domain.model.AgentAttempt failedAttempt =
                cl.gradeops.ai.api.assessment.domain.model.AgentAttempt.dispatch(failedOp.getId(), 1, "assessment", "v1", "corr-1")
                        .markFailed("AGENT_UNAVAILABLE", null, null, null);
        agentAttemptAdapter.save(failedAttempt);
        entityManager.flush();
        entityManager.clear();

        when(assessmentAgentClient.generate(any(), anyString())).thenAnswer(inv -> response("V1-concurrent-retry"));

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
                    return retryHandler.execute(new RetryGenerationCommand(concurrentAssessment.getId().value(), teacherUid));
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

            long successCount = results.stream().filter(RetryGenerationResult.class::isInstance).count();
            // A3 Contract Correction § Correction 4: the loser's Phase 0 insert collides on
            // agent_attempts' UNIQUE(ai_operation_id, attempt_number) — an operation-already-
            // in-flight condition, not a stale expectedRevisionId (retry never sends one) — so it
            // must surface as 409 OPERATION_IN_PROGRESS, never STALE_REVISION.
            long conflictCount = results.stream().filter(OperationInProgressException.class::isInstance).count();
            assertThat(results.stream().filter(StaleRevisionException.class::isInstance).count())
                    .withFailMessage("STALE_REVISION must never appear as a retry conflict outcome")
                    .isEqualTo(0);

            assertThat(successCount)
                    .withFailMessage(() -> "results were: " + results.stream()
                            .map(r -> r.getClass().getName() + (r instanceof Throwable t ? (": " + t.getMessage()) : ""))
                            .toList())
                    .isEqualTo(1);
            assertThat(conflictCount).isEqualTo(1);

            // Exactly one new AgentAttempt (attemptNumber=2) was dispatched — the loser's insert
            // collided on agent_attempts' UNIQUE(ai_operation_id, attempt_number) and rolled back,
            // never leaving a partial row (Idempotency and Concurrency Strategy ADR).
            List<cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptJpaEntity> attempts =
                    agentAttemptJpaRepository.findAllByAiOperationIdOrderByAttemptNumberDesc(failedOp.getId());
            assertThat(attempts).hasSize(2);
        } finally {
            executor.shutdown();
            jdbcTemplate.update("DELETE FROM assessments WHERE id = ?", concurrentAssessment.getId().value());
            jdbcTemplate.update("DELETE FROM assessment_briefs WHERE assessment_id = ?", concurrentAssessment.getId().value());
            jdbcTemplate.update("DELETE FROM teacher WHERE firebase_uid = ?", teacherUid);
            jdbcTemplate.update("DELETE FROM assessment_briefs WHERE assessment_id = ?", assessment.getId().value());
            jdbcTemplate.update("DELETE FROM assessments WHERE id = ?", assessment.getId().value());
            jdbcTemplate.update("DELETE FROM teacher WHERE firebase_uid = ?", "uid-1");
            TestTransaction.start();
        }
    }
}
