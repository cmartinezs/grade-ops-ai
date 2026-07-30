package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.StaleOnCompletionException;
import cl.gradeops.ai.api.assessment.application.exception.StaleRevisionException;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
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
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link RegenerateAssessmentDraftHandler} with real repositories against a live
 * Postgres (Flyway-migrated through V16): generates v1 through {@link
 * GenerateAssessmentDraftHandler} — the same durable {@code AssessmentRevision}-producing flow
 * Session A2 built — then regenerates through this handler, proving Session A2's own
 * "cannot regenerate a revision-based assessment" gap (API-A2-HANDOFF.md § Known residual risks)
 * is closed: this is the create → generate → regenerate → v1 unchanged → v2 current chain
 * required by Task 09. Requires Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class RegenerateAssessmentDraftHandlerIntegrationTest {

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
    RegenerateAssessmentDraftHandler regenerateHandler;

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
                ownershipVerifier, idempotencyGuard, coordinator);
        regenerateHandler = new RegenerateAssessmentDraftHandler(assessmentAdapter, briefAdapter, revisionAdapter,
                ownershipVerifier, idempotencyGuard, coordinator);

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

    private GenerateAssessmentDraftResult generateV1() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(response("V1"));
        GenerateAssessmentDraftResult v1 = generateHandler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "gen-key-1"));
        entityManager.flush();
        entityManager.clear();
        return v1;
    }

    @Test
    void shouldCreateV2ChainedToV1CasUpdateCurrentRevisionAndLeaveV1Unchanged() {
        GenerateAssessmentDraftResult v1 = generateV1();

        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(response("V2"));
        GenerateAssessmentDraftResult v2 = regenerateHandler.execute(new RegenerateAssessmentDraftCommand(
                assessment.getId().value(), "uid-1", "make it harder", v1.draftId(), "regen-key-1"));
        entityManager.flush();
        entityManager.clear();

        assertThat(v2.versionNumber()).isEqualTo(2);
        assertThat(v2.title()).isEqualTo("V2");
        assertThat(v2.previousRevisionId()).isEqualTo(v1.draftId());
        assertThat(v2.origin()).isEqualTo("AI_GENERATED");
        assertThat(v2.reason()).isEqualTo("make it harder");

        // v1's row is byte-for-byte unchanged.
        AssessmentRevision v1Reloaded = revisionAdapter.findById(v1.draftId()).orElseThrow();
        assertThat(v1Reloaded.getTitle()).isEqualTo("V1");
        assertThat(v1Reloaded.getVersionNumber()).isEqualTo(1);

        // currentRevisionId now points to v2 (CAS-updated).
        Assessment reloaded = assessmentAdapter.findById(assessment.getId()).orElseThrow();
        assertThat(reloaded.getCurrentRevisionId()).isEqualTo(v2.draftId());

        // Version chain is valid and both versions retrievable.
        List<AssessmentRevision> all = revisionAdapter.findAllByAssessmentId(assessment.getId());
        assertThat(all).hasSize(2);
        assertThat(all).extracting(AssessmentRevision::getVersionNumber).containsExactly(2, 1);
    }

    @Test
    void shouldRejectWithStaleRevisionAndMakeZeroAgentCallsWhenExpectedRevisionIdIsStale() {
        generateV1();
        // generateV1() itself legitimately calls the mock once (to create v1) and creates one
        // CREATE_INITIAL_REVISION AiOperation — reset/snapshot both before asserting that the
        // stale regenerate call makes zero *additional* agent calls or operations.
        clearInvocations(assessmentAgentClient);
        long aiOperationCountBeforeRegenerate = aiOperationJpaRepository.count();
        UUID wrongExpected = UUID.randomUUID();

        assertThatThrownBy(() -> regenerateHandler.execute(new RegenerateAssessmentDraftCommand(
                assessment.getId().value(), "uid-1", "make it harder", wrongExpected, "regen-key-2")))
                .isInstanceOf(StaleRevisionException.class);

        verifyNoInteractions(assessmentAgentClient);
        assertThat(revisionJpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessment.getId().value()))
                .hasSize(1);
        assertThat(aiOperationJpaRepository.count()).isEqualTo(aiOperationCountBeforeRegenerate);
    }

    @Test
    void shouldReplayIdempotentRegenerationWithoutCallingTheAgentASecondTime() {
        GenerateAssessmentDraftResult v1 = generateV1();

        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(response("V2"));
        GenerateAssessmentDraftResult first = regenerateHandler.execute(new RegenerateAssessmentDraftCommand(
                assessment.getId().value(), "uid-1", "make it harder", v1.draftId(), "same-regen-key"));
        entityManager.flush();
        entityManager.clear();
        // generateV1() + the regenerate above already account for two prior invocations of the
        // mock — reset here so the assertion below cleanly proves the replay adds zero more.
        clearInvocations(assessmentAgentClient);

        GenerateAssessmentDraftResult replayed = regenerateHandler.execute(new RegenerateAssessmentDraftCommand(
                assessment.getId().value(), "uid-1", "make it harder", v1.draftId(), "same-regen-key"));

        assertThat(replayed.draftId()).isEqualTo(first.draftId());
        assertThat(replayed.title()).isEqualTo(first.title());
        verifyNoInteractions(assessmentAgentClient);
    }

    @Test
    void shouldAllowExactlyOneOfTwoConcurrentRegenerationsFromTheSameExpectedRevision() throws Exception {
        // Isolated teacher/assessment/v1 (not the class-level "uid-1" fixture): this test commits
        // its seed data for real (see below) so two independent worker-thread transactions can
        // see it, and that commit cannot be scoped to just the revision row — it commits
        // everything accumulated in the current test's transaction, including @BeforeEach's
        // "uid-1" teacher insert. Reusing "uid-1" here would permanently leak that row past this
        // test, breaking every later test's own @BeforeEach insert with a duplicate-key violation
        // (see CreateHumanRevisionHandlerIntegrationTest's identical concurrency test for the
        // same reasoning).
        String teacherUid = "uid-concurrent-regen-" + UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                teacherUid, "Test", "Teacher", teacherUid + "@test.com");
        Assessment concurrentAssessment = Assessment.create(teacherUid);
        assessmentAdapter.save(concurrentAssessment);
        briefAdapter.save(AssessmentBrief.create(concurrentAssessment.getId(), "goal", "topic", "basic", "90min", "Java"));
        AssessmentRevision v1 = AssessmentRevision.restore(UUID.randomUUID(), concurrentAssessment.getId(), 1, null,
                cl.gradeops.ai.api.assessment.domain.model.RevisionOrigin.AI_GENERATED, teacherUid, null, null,
                "Title v1", "Context v1", "Instructions v1", List.of("obj1"), List.of("del1"), List.of("con1"),
                Instant.now());
        revisionAdapter.save(v1);
        assessmentAdapter.save(concurrentAssessment.withCurrentRevision(v1.getId()));
        entityManager.flush();
        entityManager.clear();

        when(assessmentAgentClient.generate(any(), anyString())).thenAnswer(inv -> response("V2-concurrent"));

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
                    return regenerateHandler.execute(new RegenerateAssessmentDraftCommand(
                            concurrentAssessment.getId().value(), teacherUid, "concurrent edit", v1.getId(),
                            "concurrent-key-" + Thread.currentThread().threadId()));
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
            // Depending on scheduling, the loser is caught either at the handler's pre-dispatch
            // check (StaleRevisionException, if it reads after the winner already committed) or
            // at the coordinator's Phase 2 CAS (StaleOnCompletionException, if both threads pass
            // the pre-check before either commits) — both are correct, expected outcomes of the
            // same race; see Idempotency and Concurrency Strategy ADR § "residual race."
            long conflictCount = results.stream()
                    .filter(r -> r instanceof StaleRevisionException || r instanceof StaleOnCompletionException)
                    .count();

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
            // repeated local runs — must run before TestTransaction.start() re-establishes the
            // per-test transaction the framework rolls back at teardown, or these deletes would
            // be rolled back too, silently undoing the cleanup. Deleting the assessment row first
            // (not assessment_revisions first) is required: assessments.current_revision_id
            // references assessment_revisions with no cascade in that direction, so deleting a
            // referenced revision first would violate the FK; deleting assessments cascades to
            // assessment_revisions/ai_operations/agent_attempts (all ON DELETE CASCADE from
            // assessments), which a row merely containing a since-deleted FK value does not block.
            // idempotency_records.assessment_id is a plain (non-cascading) reference too, and
            // regenerate — unlike human edit — does write one, so it must be cleared explicitly.
            jdbcTemplate.update("DELETE FROM idempotency_records WHERE assessment_id = ?", concurrentAssessment.getId().value());
            jdbcTemplate.update("DELETE FROM assessments WHERE id = ?", concurrentAssessment.getId().value());
            jdbcTemplate.update("DELETE FROM assessment_briefs WHERE assessment_id = ?", concurrentAssessment.getId().value());
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
