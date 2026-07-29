package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.AlreadyGeneratedException;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttemptStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
                agentAttemptAdapter, revisionAdapter, assessmentAgentClient, jsonMapper, transactionManager);

        handler = new GenerateAssessmentDraftHandler(assessmentAdapter, briefAdapter, revisionAdapter,
                new OwnershipVerifier(), idempotencyGuard, coordinator);

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

        GenerateAssessmentDraftResult result = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "key-1"));

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
    void shouldPersistDurableEvidenceEvenWhenTheAgentCallFails() {
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.UNREACHABLE, "unreachable", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        assertThatThrownBy(() -> handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "key-1")))
                .isSameAs(agentEx);

        entityManager.flush();
        entityManager.clear();

        // Phase 0's evidence survives even though the call to agents/ itself failed — this is
        // the durability-before-dispatch property the previous shared coordinator never had.
        AiOperationJpaEntity operation = aiOperationJpaRepository.findAll().stream()
                .filter(op -> op.getAssessmentId().equals(assessment.getId().value())).findFirst().orElseThrow();
        assertThat(operation.getStatus()).isEqualTo(AiOperationStatus.FAILED_RETRYABLE.name());

        AgentAttemptJpaEntity attempt = agentAttemptJpaRepository
                .findAllByAiOperationIdOrderByAttemptNumberDesc(operation.getId()).get(0);
        assertThat(attempt.getStatus()).isEqualTo(AgentAttemptStatus.FAILED.name());
        assertThat(attempt.getFailureCode()).isEqualTo("AGENT_UNAVAILABLE");

        Assessment reloaded = assessmentAdapter.findById(assessment.getId()).orElseThrow();
        assertThat(reloaded.getCurrentRevisionId()).isNull();
    }

    @Test
    void shouldReplayIdempotentRequestWithoutCallingTheAgentASecondTime() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        GenerateAssessmentDraftResult first = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "same-key"));
        entityManager.flush();
        entityManager.clear();

        GenerateAssessmentDraftResult replayed = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "same-key"));

        assertThat(replayed.draftId()).isEqualTo(first.draftId());
        assertThat(replayed.title()).isEqualTo(first.title());
        verify(assessmentAgentClient, times(1)).generate(any(), anyString());
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

        GenerateAssessmentDraftResult result = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "key-1"));

        assertThat(result).isNotNull();
        assertThat(result.versionNumber()).isEqualTo(1);
    }
}
