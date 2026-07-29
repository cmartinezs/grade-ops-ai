package cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceMapper;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRecord;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
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
 * Validates {@link IdempotencyRecordPersistenceAdapter} against a real Flyway schema (V1-V16),
 * including that the {@code UNIQUE NULLS NOT DISTINCT} constraint (A1's correction to
 * LOCAL-CONTRACTS.md's plain {@code UNIQUE}) actually rejects a duplicate key within either the
 * TEACHER or ASSESSMENT scope at the database level — not merely asserted in application logic.
 * Requires Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class IdempotencyRecordPersistenceAdapterTest {

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

    @Autowired IdempotencyRecordJpaRepository jpaRepository;
    @Autowired AssessmentJpaRepository assessmentJpaRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    IdempotencyRecordPersistenceAdapter adapter;
    AssessmentPersistenceAdapter assessmentAdapter;

    @BeforeEach
    void setUp() {
        adapter = new IdempotencyRecordPersistenceAdapter(jpaRepository, new IdempotencyRecordPersistenceMapper());
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
    }

    private UUID seedAssessment() {
        Assessment assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        entityManager.flush();
        return assessment.getId().value();
    }

    @Test
    void shouldRoundTripTeacherScopedRecordThroughSaveAndFind() {
        IdempotencyScope scope = IdempotencyScope.teacher("uid-1");
        IdempotencyRecord record = IdempotencyRecord.create(
                scope, "CREATE_ASSESSMENT_BRIEF", "key-1", "hash-1", "ref-1", 201);

        adapter.save(record);
        entityManager.flush();
        entityManager.clear();

        Optional<IdempotencyRecord> found = adapter.find(scope, "CREATE_ASSESSMENT_BRIEF", "key-1");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(record.getId());
        assertThat(found.get().getScope()).isEqualTo(scope);
        assertThat(found.get().getRequestPayloadHash()).isEqualTo("hash-1");
        assertThat(found.get().getResultReference()).isEqualTo("ref-1");
        assertThat(found.get().getResponseStatus()).isEqualTo(201);
    }

    @Test
    void shouldRoundTripAssessmentScopedRecordThroughSaveAndFind() {
        UUID assessmentId = seedAssessment();
        IdempotencyScope scope = IdempotencyScope.assessment(assessmentId);
        IdempotencyRecord record = IdempotencyRecord.create(
                scope, "CREATE_INITIAL_REVISION", "key-2", "hash-2", "ref-2", 201);

        adapter.save(record);
        entityManager.flush();
        entityManager.clear();

        Optional<IdempotencyRecord> found = adapter.find(scope, "CREATE_INITIAL_REVISION", "key-2");

        assertThat(found).isPresent();
        assertThat(found.get().getScope()).isEqualTo(scope);
    }

    @Test
    void shouldReturnEmptyWhenNoRecordMatches() {
        assertThat(adapter.find(IdempotencyScope.teacher("uid-1"), "CREATE_ASSESSMENT_BRIEF", "missing-key"))
                .isEmpty();
    }

    @Test
    void shouldRejectDuplicateKeyWithinTeacherScopeAtTheDatabaseLevel() {
        IdempotencyScope scope = IdempotencyScope.teacher("uid-1");
        adapter.save(IdempotencyRecord.create(scope, "CREATE_ASSESSMENT_BRIEF", "dup-key", "hash-a", "ref-a", 201));
        jpaRepository.flush();

        assertThatThrownBy(() -> {
            adapter.save(IdempotencyRecord.create(scope, "CREATE_ASSESSMENT_BRIEF", "dup-key", "hash-b", "ref-b", 201));
            jpaRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectDuplicateKeyWithinAssessmentScopeAtTheDatabaseLevelDespiteNullTeacherUid() {
        UUID assessmentId = seedAssessment();
        IdempotencyScope scope = IdempotencyScope.assessment(assessmentId);
        adapter.save(IdempotencyRecord.create(scope, "CREATE_INITIAL_REVISION", "dup-key-2", "hash-a", "ref-a", 201));
        jpaRepository.flush();

        assertThatThrownBy(() -> {
            adapter.save(IdempotencyRecord.create(scope, "CREATE_INITIAL_REVISION", "dup-key-2", "hash-b", "ref-b", 201));
            jpaRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowSameKeyAcrossDifferentAssessmentScopesDespiteBothHavingNullTeacherUid() {
        IdempotencyScope scopeA = IdempotencyScope.assessment(seedAssessment());
        IdempotencyScope scopeB = IdempotencyScope.assessment(seedAssessment());

        adapter.save(IdempotencyRecord.create(scopeA, "CREATE_INITIAL_REVISION", "shared-key", "hash-a", "ref-a", 201));
        jpaRepository.flush();
        adapter.save(IdempotencyRecord.create(scopeB, "CREATE_INITIAL_REVISION", "shared-key", "hash-b", "ref-b", 201));
        jpaRepository.flush();

        assertThat(adapter.find(scopeA, "CREATE_INITIAL_REVISION", "shared-key")).isPresent();
        assertThat(adapter.find(scopeB, "CREATE_INITIAL_REVISION", "shared-key")).isPresent();
    }
}
