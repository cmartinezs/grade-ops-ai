package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttemptStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates AgentAttemptPersistenceAdapter against a real Flyway schema (V1-V16), including
 * cost_estimate (NUMERIC, not Double) and structured_result (JSONB) round-tripping.
 * Requires Docker. Each test runs in a rolled-back transaction — no teardown needed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class AgentAttemptPersistenceAdapterTest {

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

    @Autowired AssessmentJpaRepository assessmentRepository;
    @Autowired AiOperationJpaRepository aiOperationRepository;
    @Autowired AgentAttemptJpaRepository agentAttemptRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AiOperationPersistenceAdapter aiOperationAdapter;
    AgentAttemptPersistenceAdapter agentAttemptAdapter;
    UUID aiOperationId;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentRepository, new AssessmentPersistenceMapper());
        aiOperationAdapter = new AiOperationPersistenceAdapter(aiOperationRepository, new AiOperationPersistenceMapper());
        agentAttemptAdapter = new AgentAttemptPersistenceAdapter(agentAttemptRepository, new AgentAttemptPersistenceMapper());
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        Assessment assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        entityManager.flush();

        AiOperation op = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION, "uid-1", "key-1", null);
        aiOperationAdapter.save(op);
        entityManager.flush();
        aiOperationId = op.getId();
    }

    @Test
    void shouldRoundTripDispatchedAttemptThroughSaveAndFindById() {
        AgentAttempt attempt = AgentAttempt.dispatch(aiOperationId, 1, "assessment-agent", "v1", "corr-1");

        agentAttemptAdapter.save(attempt);
        entityManager.flush();
        entityManager.clear();

        Optional<AgentAttempt> found = agentAttemptAdapter.findById(attempt.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAiOperationId()).isEqualTo(aiOperationId);
        assertThat(found.get().getAttemptNumber()).isEqualTo(1);
        assertThat(found.get().getAgentName()).isEqualTo("assessment-agent");
        assertThat(found.get().getPromptVersion()).isEqualTo("v1");
        assertThat(found.get().getCorrelationId()).isEqualTo("corr-1");
        assertThat(found.get().getStatus()).isEqualTo(AgentAttemptStatus.DISPATCHED);
        assertThat(found.get().getResolvedProvider()).isNull();
        assertThat(found.get().getResolvedModel()).isNull();
        assertThat(found.get().getCompletedAt()).isNull();
    }

    @Test
    void shouldRoundTripCompletedAttemptIncludingCostEstimateAndStructuredResult() {
        AgentAttempt attempt = AgentAttempt.dispatch(aiOperationId, 1, "assessment-agent", "v1", "corr-1")
                .markCompleted("groq", "llama-3", "req-1", 100, 200, new BigDecimal("0.012345"), "{\"title\":\"t\"}");

        agentAttemptAdapter.save(attempt);
        entityManager.flush();
        entityManager.clear();

        Optional<AgentAttempt> found = agentAttemptAdapter.findById(attempt.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(AgentAttemptStatus.COMPLETED);
        assertThat(found.get().getResolvedProvider()).isEqualTo("groq");
        assertThat(found.get().getResolvedModel()).isEqualTo("llama-3");
        assertThat(found.get().getProviderRequestId()).isEqualTo("req-1");
        assertThat(found.get().getEstimatedInputTokens()).isEqualTo(100);
        assertThat(found.get().getEstimatedOutputTokens()).isEqualTo(200);
        assertThat(found.get().getCostEstimate()).isEqualByComparingTo("0.012345");
        // Hibernate's JSON JdbcType round-trips through parse+re-serialize (normalizing
        // whitespace), not a raw byte-for-byte passthrough — compare ignoring whitespace.
        assertThat(found.get().getStructuredResult()).isEqualToIgnoringWhitespace("{\"title\":\"t\"}");
        assertThat(found.get().getCompletedAt()).isNotNull();
    }

    @Test
    void shouldRoundTripFailedAttemptRetainingStructuredResult() {
        AgentAttempt attempt = AgentAttempt.dispatch(aiOperationId, 1, "assessment-agent", "v1", "corr-1")
                .markFailed("STALE_ON_COMPLETION", "gemini", "gemini-2.0-flash", "{\"partial\":true}");

        agentAttemptAdapter.save(attempt);
        entityManager.flush();
        entityManager.clear();

        Optional<AgentAttempt> found = agentAttemptAdapter.findById(attempt.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(AgentAttemptStatus.FAILED);
        assertThat(found.get().getFailureCode()).isEqualTo("STALE_ON_COMPLETION");
        assertThat(found.get().getStructuredResult()).isEqualToIgnoringWhitespace("{\"partial\":true}");
    }

    @Test
    void shouldRetainAllAttemptsOrderedByAttemptNumberDescWhenFindingAllByAiOperationId() {
        AgentAttempt first = AgentAttempt.dispatch(aiOperationId, 1, "assessment-agent", "v1", "corr-1")
                .markFailed("AGENT_ERROR", null, null, null);
        agentAttemptAdapter.save(first);
        AgentAttempt second = AgentAttempt.dispatch(aiOperationId, 2, "assessment-agent", "v1", "corr-2");
        agentAttemptAdapter.save(second);

        entityManager.flush();
        entityManager.clear();

        List<AgentAttempt> all = agentAttemptAdapter.findAllByAiOperationId(aiOperationId);

        assertThat(all).hasSize(2);
        assertThat(all).extracting(AgentAttempt::getAttemptNumber).containsExactly(2, 1);
        assertThat(all.get(1).getStatus()).isEqualTo(AgentAttemptStatus.FAILED);
        assertThat(all.get(0).getStatus()).isEqualTo(AgentAttemptStatus.DISPATCHED);
    }

    @Test
    void shouldReturnEmptyWhenAttemptDoesNotExist() {
        assertThat(agentAttemptAdapter.findById(UUID.randomUUID())).isEmpty();
    }
}
