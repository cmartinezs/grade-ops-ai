package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.GetGenerationStatusCommand;
import cl.gradeops.ai.api.assessment.application.result.GetGenerationStatusResult;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttemptStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
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
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises {@link GetGenerationStatusHandler}'s {@code INDETERMINATE} classification against a
 * live Postgres, with a genuinely persisted, still-{@code DISPATCHED} {@code AgentAttempt} whose
 * {@code dispatchedAt} is manually set far enough in the past (no scheduler/background job exists
 * to do this — LOCAL-CONTRACTS.md § Orphaned in-flight detection is computed at read time only).
 * Requires Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class GetGenerationStatusHandlerIntegrationTest {

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
    @Autowired AiOperationJpaRepository aiOperationJpaRepository;
    @Autowired AgentAttemptJpaRepository agentAttemptJpaRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentBriefPersistenceAdapter briefAdapter;
    AiOperationPersistenceAdapter aiOperationAdapter;
    AgentAttemptPersistenceAdapter agentAttemptAdapter;
    GetGenerationStatusHandler handler;

    Assessment assessment;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefJpaRepository, new AssessmentBriefPersistenceMapper());
        aiOperationAdapter = new AiOperationPersistenceAdapter(aiOperationJpaRepository, new AiOperationPersistenceMapper());
        agentAttemptAdapter = new AgentAttemptPersistenceAdapter(agentAttemptJpaRepository, new AgentAttemptPersistenceMapper());
        OwnershipVerifier ownershipVerifier = new OwnershipVerifier();
        handler = new GetGenerationStatusHandler(assessmentAdapter, aiOperationAdapter, agentAttemptAdapter, ownershipVerifier);

        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java"));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldReportIndeterminateForAGenuinelyPersistedOrphanedDispatchPastTheReadTimeoutThreshold() {
        AiOperation op = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                "uid-1", "gen-key-1", null).markInProgress();
        aiOperationAdapter.save(op);
        Instant longAgo = Instant.now().minus(Duration.ofMinutes(10));
        AgentAttempt orphaned = AgentAttempt.restore(java.util.UUID.randomUUID(), op.getId(), 1, "assessment",
                null, null, "v1", "corr-1", longAgo, null, AgentAttemptStatus.DISPATCHED,
                null, null, null, null, null, null);
        agentAttemptAdapter.save(orphaned);
        entityManager.flush();
        entityManager.clear();

        GetGenerationStatusResult result = handler.execute(
                new GetGenerationStatusCommand(assessment.getId().value(), "uid-1"));

        assertThat(result.status()).isEqualTo("INDETERMINATE");
        assertThat(result.retryable()).isTrue();
        assertThat(result.failureCode()).isNull();
        assertThat(result.currentRevisionId()).isNull();
    }

    @Test
    void shouldReportInProgressNotIndeterminateForARecentlyDispatchedAttempt() {
        AiOperation op = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                "uid-1", "gen-key-2", null).markInProgress();
        aiOperationAdapter.save(op);
        AgentAttempt recent = AgentAttempt.dispatch(op.getId(), 1, "assessment", "v1", "corr-1");
        agentAttemptAdapter.save(recent);
        entityManager.flush();
        entityManager.clear();

        GetGenerationStatusResult result = handler.execute(
                new GetGenerationStatusCommand(assessment.getId().value(), "uid-1"));

        assertThat(result.status()).isEqualTo("IN_PROGRESS");
        assertThat(result.retryable()).isFalse();
    }
}
