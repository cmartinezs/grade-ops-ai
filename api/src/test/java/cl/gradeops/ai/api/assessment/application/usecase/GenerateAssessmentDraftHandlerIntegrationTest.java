package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogJpaEntity;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftJpaEntity;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceMapper;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link GenerateAssessmentDraftHandler} with real repositories against a live
 * Postgres (Flyway-migrated through V12), unlike {@link GenerateAssessmentDraftHandlerTest}
 * (mocked repositories) and {@code AgentExecutionLogPersistenceAdapterIntegrationTest} (log
 * round-trip only, never creates a draft referencing the log). Only {@link AssessmentAgentClient}
 * is stubbed — everything downstream of it, including the {@code TransactionTemplate}-managed
 * log→draft→log-backfill cross-reference, runs for real. Requires Docker.
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
    @Autowired AssessmentDraftJpaRepository draftJpaRepository;
    @Autowired AgentExecutionLogJpaRepository logJpaRepository;
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
        AssessmentDraftPersistenceAdapter draftAdapter =
                new AssessmentDraftPersistenceAdapter(draftJpaRepository, new AssessmentDraftPersistenceMapper());
        AgentExecutionLogPersistenceAdapter logAdapter =
                new AgentExecutionLogPersistenceAdapter(logJpaRepository, new AgentExecutionLogPersistenceMapper());
        assessmentAgentClient = mock(AssessmentAgentClient.class);
        DraftGenerationCoordinator coordinator = new DraftGenerationCoordinator(
                draftAdapter, logAdapter, assessmentAgentClient, transactionManager);

        handler = new GenerateAssessmentDraftHandler(assessmentAdapter, briefAdapter, new OwnershipVerifier(), coordinator);

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
                new AssessmentAgentResponse.Log(UUID.randomUUID(), "assessment", "gemini-2.0-flash", "v1",
                        "in-hash", "out-hash", 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt));
    }

    @Test
    void shouldPersistDraftAndLogCrossReferencedThroughRealTransactionOnSuccess() {
        when(assessmentAgentClient.generate(any())).thenReturn(successResponse());

        GenerateAssessmentDraftResult result = handler.execute(
                new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1"));

        entityManager.flush();
        entityManager.clear();

        List<AssessmentDraftJpaEntity> drafts = draftJpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(
                assessment.getId().value());
        assertThat(drafts).hasSize(1);
        AssessmentDraftJpaEntity draftEntity = drafts.get(0);
        assertThat(draftEntity.getId()).isEqualTo(result.draftId());
        assertThat(draftEntity.getVersionNumber()).isEqualTo(1);
        assertThat(draftEntity.getAgentExecutionLogId()).isNotNull();

        Optional<AgentExecutionLogJpaEntity> logEntity = logJpaRepository.findById(draftEntity.getAgentExecutionLogId());
        assertThat(logEntity).isPresent();
        assertThat(logEntity.get().getStatus()).isEqualTo("COMPLETED");
        assertThat(logEntity.get().getErrorCode()).isNull();
        assertThat(logEntity.get().getAssessmentId()).isEqualTo(assessment.getId().value());

        // The cross-reference is bidirectional: draft -> log via agent_execution_log_id,
        // and log -> draft via the back-filled draft_id.
        assertThat(logEntity.get().getDraftId()).isEqualTo(draftEntity.getId());
    }

    @Test
    void shouldPersistOnlyFailureLogWithNoDraftRowOnAgentFailure() {
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.AGENT_REJECTED, "rejected", new RuntimeException());
        when(assessmentAgentClient.generate(any())).thenThrow(agentEx);

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1")))
                .isSameAs(agentEx);

        entityManager.flush();
        entityManager.clear();

        assertThat(draftJpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessment.getId().value()))
                .isEmpty();

        List<AgentExecutionLogJpaEntity> logs = logJpaRepository.findAll().stream()
                .filter(l -> l.getAssessmentId().equals(assessment.getId().value()))
                .toList();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getStatus()).isEqualTo("FAILED");
        assertThat(logs.get(0).getErrorCode()).isEqualTo("AGENT_REJECTED");
        assertThat(logs.get(0).getDraftId()).isNull();
    }
}
