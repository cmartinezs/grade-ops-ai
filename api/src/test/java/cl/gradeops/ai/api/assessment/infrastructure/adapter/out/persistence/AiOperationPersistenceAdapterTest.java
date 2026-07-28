package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validates AiOperationPersistenceAdapter against a real Flyway schema (V1-V16), including
 * that uq_ai_operations_in_flight actually rejects a second concurrent in-flight row at the
 * database level — defense-in-depth, not merely asserted in application logic.
 * Requires Docker. Each test runs in a rolled-back transaction — no teardown needed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class AiOperationPersistenceAdapterTest {

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
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AiOperationPersistenceAdapter aiOperationAdapter;
    Assessment assessment;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentRepository, new AssessmentPersistenceMapper());
        aiOperationAdapter = new AiOperationPersistenceAdapter(aiOperationRepository, new AiOperationPersistenceMapper());
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        entityManager.flush();
    }

    @Test
    void shouldRoundTripPendingOperationThroughSaveAndFindById() {
        AiOperation op = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                "uid-1", "key-1", null);

        aiOperationAdapter.save(op);
        entityManager.flush();
        entityManager.clear();

        Optional<AiOperation> found = aiOperationAdapter.findById(op.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(op.getId());
        assertThat(found.get().getAssessmentId()).isEqualTo(assessment.getId());
        assertThat(found.get().getOperationType()).isEqualTo(AiOperationType.CREATE_INITIAL_REVISION);
        assertThat(found.get().getRequestedBy()).isEqualTo("uid-1");
        assertThat(found.get().getIdempotencyKey()).isEqualTo("key-1");
        assertThat(found.get().getExpectedRevisionId()).isNull();
        assertThat(found.get().getStatus()).isEqualTo(AiOperationStatus.PENDING);
        assertThat(found.get().getResultRevisionId()).isNull();
    }

    @Test
    void shouldRoundTripStatusTransitionsThroughSaveAndFindById() {
        AiOperation op = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                "uid-1", "key-1", null);
        aiOperationAdapter.save(op);

        AiOperation inProgress = op.markInProgress();
        aiOperationAdapter.save(inProgress);
        entityManager.flush();
        entityManager.clear();

        Optional<AiOperation> found = aiOperationAdapter.findById(op.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(AiOperationStatus.IN_PROGRESS);
    }

    @Test
    void shouldReturnEmptyWhenOperationDoesNotExist() {
        assertThat(aiOperationAdapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldRejectSecondInFlightOperationForSameAssessmentAndTypeAtTheDatabaseLevel() {
        AiOperation first = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                "uid-1", "key-1", null);
        aiOperationAdapter.save(first);
        entityManager.flush();

        AiOperation second = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                "uid-1", "key-2", null);

        assertThatThrownBy(() -> {
            aiOperationAdapter.save(second);
            aiOperationRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
