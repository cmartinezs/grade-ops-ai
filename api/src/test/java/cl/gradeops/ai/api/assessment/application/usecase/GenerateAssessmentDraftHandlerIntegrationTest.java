package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.AlreadyGeneratedException;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftOutcome;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttemptStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptJpaEntity;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AiOperationJpaEntity;
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
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyCompletionContext;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
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
import org.springframework.dao.DataIntegrityViolationException;
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
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link GenerateAssessmentDraftHandler} + {@link AiOperationCoordinator} with real
 * repositories against a live Postgres (Flyway-migrated through V16), unlike {@link
 * AiOperationCoordinatorTest} (mocked ports). Only {@link AssessmentAgentClient} is stubbed —
 * everything downstream runs for real, including the Phase 0 (durable evidence before dispatch)
 * / Phase 1 (HTTP, outside any transaction) / Phase 2 (CAS + revision) sequence and Task 06's
 * idempotency replay. Requires Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class GenerateAssessmentDraftHandlerIntegrationTest {

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
    AssessmentAgentClient assessmentAgentClient;
    GenerateAssessmentDraftHandler handler;

    Assessment assessment;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefJpaRepository, new AssessmentBriefPersistenceMapper());
        AssessmentRevisionPersistenceAdapter revisionAdapter =
                new AssessmentRevisionPersistenceAdapter(revisionJpaRepository, new AssessmentRevisionPersistenceMapper());
        AiOperationPersistenceAdapter aiOperationAdapter =
                new AiOperationPersistenceAdapter(aiOperationJpaRepository, new AiOperationPersistenceMapper());
        AgentAttemptPersistenceAdapter agentAttemptAdapter =
                new AgentAttemptPersistenceAdapter(agentAttemptJpaRepository, new AgentAttemptPersistenceMapper());
        IdempotencyRecordPersistenceAdapter idempotencyAdapter =
                new IdempotencyRecordPersistenceAdapter(idempotencyRecordJpaRepository, new IdempotencyRecordPersistenceMapper());
        IdempotencyGuard idempotencyGuard = new IdempotencyGuard(idempotencyAdapter);
        assessmentAgentClient = mock(AssessmentAgentClient.class);
        JsonMapper jsonMapper = JsonMapper.builder().build();

        AiOperationCoordinator coordinator = new AiOperationCoordinator(assessmentAdapter, aiOperationAdapter,
                agentAttemptAdapter, revisionAdapter, assessmentAgentClient, idempotencyGuard, jsonMapper, transactionManager);

        handler = new GenerateAssessmentDraftHandler(assessmentAdapter, briefAdapter, revisionAdapter,
                aiOperationAdapter, agentAttemptAdapter, new OwnershipVerifier(), idempotencyGuard, coordinator);

        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java"));
        entityManager.flush();
        entityManager.clear();
    }

    private static AssessmentAgentResponse successResponse() {
        Instant startedAt = Instant.now().minusSeconds(2);
        Instant finishedAt = Instant.now();
        return new AssessmentAgentResponse(
                new AssessmentAgentResponse.Result("Title", "Context", "Instructions",
                        List.of("obj"), List.of("del"), List.of("con")),
                new AssessmentAgentResponse.Log(UUID.randomUUID(), "assessment", "gemini", "gemini-2.0-flash", "v1",
                        "in-hash", "out-hash", 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt));
    }

    @Test
    void shouldCreateRevisionAndCasUpdateAssessmentCurrentRevisionOnSuccess() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        GenerateAssessmentDraftOutcome outcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "key-1"));
        GenerateAssessmentDraftResult result = ((GenerateAssessmentDraftOutcome.RevisionCreated) outcome).revision();

        entityManager.flush();
        entityManager.clear();

        assertThat(result.title()).isEqualTo("Title");
        assertThat(result.versionNumber()).isEqualTo(1);

        Assessment reloaded = assessmentAdapter.findById(assessment.getId()).orElseThrow();
        assertThat(reloaded.getCurrentRevisionId()).isEqualTo(result.draftId());

        AiOperationJpaEntity operation = aiOperationJpaRepository.findAll().stream()
                .filter(op -> op.getAssessmentId().equals(assessment.getId().value())).findFirst().orElseThrow();
        assertThat(operation.getStatus()).isEqualTo(AiOperationStatus.SUCCEEDED.name());
        assertThat(operation.getResultRevisionId()).isEqualTo(result.draftId());

        AgentAttemptJpaEntity attempt = agentAttemptJpaRepository
                .findAllByAiOperationIdOrderByAttemptNumberDesc(operation.getId()).get(0);
        assertThat(attempt.getStatus()).isEqualTo(AgentAttemptStatus.COMPLETED.name());
        assertThat(attempt.getResolvedProvider()).isEqualTo("gemini");
        assertThat(attempt.getResolvedModel()).isEqualTo("gemini-2.0-flash");
        assertThat(attempt.getStructuredResult()).contains("Title");
    }

    @Test
    void shouldPersistDurableEvidenceAndReturnOperationAcceptedWhenTheAgentCallFails() {
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.UNREACHABLE, "unreachable", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        GenerateAssessmentDraftOutcome outcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "key-1"));

        assertThat(outcome).isInstanceOf(GenerateAssessmentDraftOutcome.OperationAccepted.class);
        var operation = ((GenerateAssessmentDraftOutcome.OperationAccepted) outcome).operation();
        assertThat(operation.status()).isEqualTo("FAILED_RETRYABLE");
        assertThat(operation.failureCode()).isEqualTo("AGENT_UNAVAILABLE");
        assertThat(operation.retryable()).isTrue();
        assertThat(operation.resultRevisionId()).isNull();

        entityManager.flush();
        entityManager.clear();

        // Phase 0's evidence survives even though the call to agents/ itself failed, and it was
        // durably recorded BEFORE this method returned a 202 — this is the durability-before-
        // dispatch property the previous shared coordinator never had.
        AiOperationJpaEntity persistedOperation = aiOperationJpaRepository.findAll().stream()
                .filter(op -> op.getAssessmentId().equals(assessment.getId().value())).findFirst().orElseThrow();
        assertThat(persistedOperation.getId()).isEqualTo(operation.id());
        assertThat(persistedOperation.getStatus()).isEqualTo(AiOperationStatus.FAILED_RETRYABLE.name());

        AgentAttemptJpaEntity attempt = agentAttemptJpaRepository
                .findAllByAiOperationIdOrderByAttemptNumberDesc(persistedOperation.getId()).get(0);
        assertThat(attempt.getStatus()).isEqualTo(AgentAttemptStatus.FAILED.name());
        assertThat(attempt.getFailureCode()).isEqualTo("AGENT_UNAVAILABLE");

        Assessment reloaded = assessmentAdapter.findById(assessment.getId()).orElseThrow();
        assertThat(reloaded.getCurrentRevisionId()).isNull();
    }

    @Test
    void shouldReplayIdempotentRequestWithoutCallingTheAgentASecondTime() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        GenerateAssessmentDraftOutcome firstOutcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "same-key"));
        GenerateAssessmentDraftResult first = ((GenerateAssessmentDraftOutcome.RevisionCreated) firstOutcome).revision();
        entityManager.flush();
        entityManager.clear();

        GenerateAssessmentDraftOutcome replayedOutcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "same-key"));
        GenerateAssessmentDraftResult replayed = ((GenerateAssessmentDraftOutcome.RevisionCreated) replayedOutcome).revision();

        assertThat(replayed.draftId()).isEqualTo(first.draftId());
        assertThat(replayed.title()).isEqualTo(first.title());
        verify(assessmentAgentClient, times(1)).generate(any(), anyString());
    }

    @Test
    void shouldReplayA202WithoutDispatchingASecondAgentAttempt() {
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.UNREACHABLE, "unreachable", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        GenerateAssessmentDraftOutcome firstOutcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "same-fail-key"));
        var firstOperation = ((GenerateAssessmentDraftOutcome.OperationAccepted) firstOutcome).operation();
        entityManager.flush();
        entityManager.clear();
        clearInvocations(assessmentAgentClient);

        GenerateAssessmentDraftOutcome replayedOutcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "same-fail-key"));
        var replayedOperation = ((GenerateAssessmentDraftOutcome.OperationAccepted) replayedOutcome).operation();

        assertThat(replayedOperation.id()).isEqualTo(firstOperation.id());
        verifyNoInteractions(assessmentAgentClient);
        assertThat(agentAttemptJpaRepository.findAllByAiOperationIdOrderByAttemptNumberDesc(firstOperation.id()))
                .hasSize(1);
    }

    @Test
    void shouldRejectSecondGenerationOnceACurrentRevisionAlreadyExists() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        handler.execute(new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "key-1"));
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "key-2")))
                .isInstanceOf(AlreadyGeneratedException.class);
    }

    @Test
    void shouldSupportALegacyAssessmentWithNullCurrentRevisionGoingThroughInitialGeneration() {
        // Assessment.create(...) already produces exactly the pre-cut legacy shape
        // (currentRevisionId = null, lockVersion = 0) — this is the scenario Task 07B's own risk
        // section requires: such an assessment must generate successfully, not be mistaken for
        // ALREADY_GENERATED or crash.
        assertThat(assessment.getCurrentRevisionId()).isNull();
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        GenerateAssessmentDraftOutcome outcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "key-1"));
        GenerateAssessmentDraftResult result = ((GenerateAssessmentDraftOutcome.RevisionCreated) outcome).revision();

        assertThat(result).isNotNull();
        assertThat(result.versionNumber()).isEqualTo(1);
    }

    @Test
    void shouldRecoverAndBackfillTheMissingRecordForAPreFixOrphanedSucceededOperation() {
        // A3 Final Idempotency Correction § 5: simulates data written by the PREVIOUS (non-atomic)
        // implementation — a durable AiOperation/AgentAttempt/AssessmentRevision that succeeded,
        // with NO IdempotencyRecord ever written for it (the exact crash-window gap this
        // correction closes). A fresh request with the SAME key must replay it, backfill the
        // missing record, and never call agents/ again.
        AiOperationPersistenceAdapter aiOperationAdapter =
                new AiOperationPersistenceAdapter(aiOperationJpaRepository, new AiOperationPersistenceMapper());
        AgentAttemptPersistenceAdapter agentAttemptAdapter =
                new AgentAttemptPersistenceAdapter(agentAttemptJpaRepository, new AgentAttemptPersistenceMapper());
        AssessmentRevisionPersistenceAdapter revisionAdapter =
                new AssessmentRevisionPersistenceAdapter(revisionJpaRepository, new AssessmentRevisionPersistenceMapper());

        AiOperation orphanedOperation = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                "uid-1", "orphaned-key", null).markInProgress();
        aiOperationAdapter.save(orphanedOperation);
        cl.gradeops.ai.api.assessment.domain.model.AgentAttempt orphanedAttempt =
                cl.gradeops.ai.api.assessment.domain.model.AgentAttempt.dispatch(
                                orphanedOperation.getId(), 1, "assessment", "v1", "corr-orphan")
                        .markCompleted("gemini", "gemini-2.0-flash", null, 100, 200, null, "{}");
        agentAttemptAdapter.save(orphanedAttempt);
        AssessmentRevision orphanedRevision = AssessmentRevision.generateFromAi(assessment.getId(),
                "Orphaned", "Context", "Instructions", List.of("obj"), List.of("del"), List.of("con"),
                "uid-1", orphanedAttempt.getId());
        revisionAdapter.save(orphanedRevision);
        assessmentAdapter.save(assessment.withCurrentRevision(orphanedRevision.getId()));
        aiOperationAdapter.save(orphanedOperation.markSucceeded(orphanedRevision.getId()));
        entityManager.flush();
        entityManager.clear();

        GenerateAssessmentDraftOutcome outcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "orphaned-key"));

        assertThat(outcome).isInstanceOf(GenerateAssessmentDraftOutcome.RevisionCreated.class);
        var result = ((GenerateAssessmentDraftOutcome.RevisionCreated) outcome).revision();
        assertThat(result.draftId()).isEqualTo(orphanedRevision.getId());
        verifyNoInteractions(assessmentAgentClient);

        assertThat(idempotencyRecordJpaRepository.findAll()).anySatisfy(record ->
                assertThat(record.getResultReference()).isEqualTo(orphanedRevision.getId().toString()));
    }

    @Test
    void shouldAllowExactlyOneDispatchWhenTwoConcurrentInitialGenerationRequestsShareTheSameIdempotencyKey() throws Exception {
        String teacherUid = "uid-concurrent-gen-samekey-" + UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                teacherUid, "Test", "Teacher", teacherUid + "@test.com");
        Assessment concurrentAssessment = Assessment.create(teacherUid);
        assessmentAdapter.save(concurrentAssessment);
        briefAdapter.save(AssessmentBrief.create(concurrentAssessment.getId(), "goal", "topic", "basic", "90min", "Java"));
        entityManager.flush();
        entityManager.clear();

        when(assessmentAgentClient.generate(any(), anyString())).thenAnswer(inv -> successResponse());

        TestTransaction.flagForCommit();
        TestTransaction.end();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        try {
            Callable<GenerateAssessmentDraftOutcome> attempt = () -> {
                ready.countDown();
                go.await();
                return handler.execute(new GenerateAssessmentDraftCommand(
                        concurrentAssessment.getId().value(), teacherUid, "same-concurrent-gen-key"));
            };

            List<Future<GenerateAssessmentDraftOutcome>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(attempt));
            }
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            go.countDown();

            List<GenerateAssessmentDraftOutcome> results = new ArrayList<>();
            for (Future<GenerateAssessmentDraftOutcome> f : futures) {
                results.add(f.get(15, TimeUnit.SECONDS));
            }

            // Neither request ever throws — a same-key race is never a real conflict, only the
            // dispatch itself is exclusive (A3 Final Idempotency Correction § 6).
            assertThat(results).hasSize(2).doesNotContainNull();
            verify(assessmentAgentClient, times(1)).generate(any(), anyString());
            assertThat(aiOperationJpaRepository.findAll().stream()
                    .filter(op -> op.getAssessmentId().equals(concurrentAssessment.getId().value())).count())
                    .isEqualTo(1);
        } finally {
            executor.shutdown();
            jdbcTemplate.update("DELETE FROM idempotency_records WHERE assessment_id = ?", concurrentAssessment.getId().value());
            jdbcTemplate.update("DELETE FROM assessments WHERE id = ?", concurrentAssessment.getId().value());
            jdbcTemplate.update("DELETE FROM assessment_briefs WHERE assessment_id = ?", concurrentAssessment.getId().value());
            jdbcTemplate.update("DELETE FROM teacher WHERE firebase_uid = ?", teacherUid);
            jdbcTemplate.update("DELETE FROM assessment_briefs WHERE assessment_id = ?", assessment.getId().value());
            jdbcTemplate.update("DELETE FROM assessments WHERE id = ?", assessment.getId().value());
            jdbcTemplate.update("DELETE FROM teacher WHERE firebase_uid = ?", "uid-1");
            TestTransaction.start();
        }
    }

    @Test
    void shouldRollBackTheEntireSuccessfulOutcomeWhenTheIdempotencyRecordWriteFailsInTheSamePhase2Transaction() throws Exception {
        // A3 Final Idempotency Correction § 7 "Atomicidad de éxito", proven against a REAL
        // Postgres transaction (not mocks). Drives the coordinator directly (bypassing the
        // handler's own idempotencyGuard.check(), which would otherwise short-circuit to a
        // replay) so a conflicting IdempotencyRecord can be pre-seeded under the exact tuple the
        // coordinator's own Phase 2 write will try to insert. The resulting real UNIQUE-constraint
        // violation must roll back the revision/attempt-completion/operation-success/CAS write
        // issued in the same transactional callback — not leave a half-applied success.
        AiOperationPersistenceAdapter aiOperationAdapter =
                new AiOperationPersistenceAdapter(aiOperationJpaRepository, new AiOperationPersistenceMapper());
        AgentAttemptPersistenceAdapter agentAttemptAdapter =
                new AgentAttemptPersistenceAdapter(agentAttemptJpaRepository, new AgentAttemptPersistenceMapper());
        AssessmentRevisionPersistenceAdapter revisionAdapter =
                new AssessmentRevisionPersistenceAdapter(revisionJpaRepository, new AssessmentRevisionPersistenceMapper());
        IdempotencyRecordPersistenceAdapter idempotencyAdapter = new IdempotencyRecordPersistenceAdapter(
                idempotencyRecordJpaRepository, new IdempotencyRecordPersistenceMapper());
        IdempotencyGuard localIdempotencyGuard = new IdempotencyGuard(idempotencyAdapter);
        AiOperationCoordinator coordinator = new AiOperationCoordinator(assessmentAdapter, aiOperationAdapter,
                agentAttemptAdapter, revisionAdapter, assessmentAgentClient, localIdempotencyGuard,
                JsonMapper.builder().build(), transactionManager);
        IdempotencyScope scope = IdempotencyScope.assessment(assessment.getId().value());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        try {
            // Real, physically-committed pre-seed of a conflicting record under the exact tuple
            // the coordinator will try to insert at Phase 2 completion.
            localIdempotencyGuard.record(scope, "CREATE_INITIAL_REVISION", "conflict-key", "irrelevant-hash",
                    UUID.randomUUID().toString(), 201);

            when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());
            IdempotencyCompletionContext context = new IdempotencyCompletionContext(
                    scope, "CREATE_INITIAL_REVISION", "conflict-key", "irrelevant-hash");

            assertThatThrownBy(() -> coordinator.createInitialRevision(assessment,
                    new AssessmentCommand("goal", "topic", "basic", "90min", "Java", null, null, null, null, null),
                    "uid-1", "conflict-key", context))
                    .isInstanceOf(DataIntegrityViolationException.class);

            assertThat(revisionJpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessment.getId().value()))
                    .isEmpty();
            Assessment reloaded = assessmentAdapter.findById(assessment.getId()).orElseThrow();
            assertThat(reloaded.getCurrentRevisionId()).isNull();
        } finally {
            jdbcTemplate.update("DELETE FROM idempotency_records WHERE assessment_id = ?", assessment.getId().value());
            jdbcTemplate.update("DELETE FROM ai_operations WHERE assessment_id = ?", assessment.getId().value());
            jdbcTemplate.update("DELETE FROM assessment_briefs WHERE assessment_id = ?", assessment.getId().value());
            jdbcTemplate.update("DELETE FROM assessments WHERE id = ?", assessment.getId().value());
            jdbcTemplate.update("DELETE FROM teacher WHERE firebase_uid = ?", "uid-1");
            TestTransaction.start();
        }
    }
}
