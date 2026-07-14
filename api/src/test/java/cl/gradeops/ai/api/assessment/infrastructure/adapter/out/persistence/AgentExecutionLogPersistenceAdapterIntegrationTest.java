package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AgentExecutionLog;
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

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates AgentExecutionLogPersistenceAdapter against a real Flyway schema (V1-V12), including
 * the {@code cost_estimate} column: a first attempt declared it {@code NUMERIC}, which passed a
 * plain save/read round trip here but failed Hibernate's {@code ddl-auto=validate} schema check
 * at real app startup (Hibernate maps {@code Double} to {@code float(53)} by default) — this test
 * intentionally uses {@code ddl-auto=validate}, not {@code none}, so that class of mismatch fails
 * automatically instead of only surfacing via a manual smoke test. Requires Docker. Each test runs
 * in a rolled-back transaction — no teardown needed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class AgentExecutionLogPersistenceAdapterIntegrationTest {

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
    @Autowired AgentExecutionLogJpaRepository logRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AgentExecutionLogPersistenceAdapter logAdapter;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentRepository, new AssessmentPersistenceMapper());
        logAdapter = new AgentExecutionLogPersistenceAdapter(logRepository, new AgentExecutionLogPersistenceMapper());
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
    }

    @Test
    void shouldRoundTripAgentExecutionLogIncludingCostEstimateThroughSaveAndFind() {
        Assessment assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);

        Instant startedAt = Instant.now().minusSeconds(2);
        Instant finishedAt = Instant.now();
        AgentExecutionLog log = AgentExecutionLog.create(assessment.getId(), UUID.randomUUID(), "assessment",
                "groq", "model-1", "v1", "in-hash", "out-hash", 100, 200, 0.0123, "COMPLETED", null,
                startedAt, finishedAt);
        logAdapter.save(log);

        entityManager.flush();
        entityManager.clear();

        var found = logRepository.findById(log.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAssessmentId()).isEqualTo(assessment.getId().value());
        assertThat(found.get().getDraftId()).isNull();
        assertThat(found.get().getProvider()).isEqualTo("groq");
        assertThat(found.get().getCostEstimate()).isEqualTo(0.0123);
        assertThat(found.get().getStatus()).isEqualTo("COMPLETED");
        assertThat(found.get().getErrorCode()).isNull();
    }

    @Test
    void shouldRejectLogReferencingNonExistentAssessment() {
        AgentExecutionLog orphan = AgentExecutionLog.create(
                new cl.gradeops.ai.api.assessment.domain.model.AssessmentId(UUID.randomUUID()),
                null, "assessment", null, null, null, null, null, null, null, null,
                "FAILED", "UNREACHABLE", Instant.now(), Instant.now());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            logAdapter.save(orphan);
            logRepository.flush();
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
