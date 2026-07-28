package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the Assessment Authoring Operation Foundation migrations (V13+) apply cleanly
 * on top of V1-V12 and that the new constraints behave as specified in LOCAL-CONTRACTS.md.
 * Extended task-by-task (V13 here, V14/V15/V16 added by later tasks in this same session).
 * Requires Docker. Each test runs in a rolled-back transaction — no teardown needed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class AssessmentAuthoringSchemaMigrationTest {

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

    @Autowired JdbcTemplate jdbcTemplate;

    UUID assessmentId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        assessmentId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO assessments (id, teacher_uid) VALUES (?, ?)",
                assessmentId, "uid-1");
    }

    @Test
    void shouldApplyV13MigrationCleanlyAlongsideV1ThroughV12() {
        Integer aiOperationsTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'ai_operations'", Integer.class);
        Integer agentAttemptsTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'agent_attempts'", Integer.class);

        assertThat(aiOperationsTableCount).isEqualTo(1);
        assertThat(agentAttemptsTableCount).isEqualTo(1);
    }

    @Test
    void shouldRejectSecondPendingAiOperationForSameAssessmentAndOperationType() {
        insertAiOperation(UUID.randomUUID(), assessmentId, "CREATE_INITIAL_REVISION", "PENDING");

        assertThatThrownBy(() -> insertAiOperation(UUID.randomUUID(), assessmentId, "CREATE_INITIAL_REVISION", "PENDING"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowASecondAiOperationOnceTheFirstIsTerminal() {
        insertAiOperation(UUID.randomUUID(), assessmentId, "CREATE_INITIAL_REVISION", "SUCCEEDED");

        insertAiOperation(UUID.randomUUID(), assessmentId, "CREATE_INITIAL_REVISION", "PENDING");
    }

    @Test
    void shouldAllowConcurrentInFlightOperationsOfDifferentTypesForTheSameAssessment() {
        insertAiOperation(UUID.randomUUID(), assessmentId, "CREATE_INITIAL_REVISION", "PENDING");

        insertAiOperation(UUID.randomUUID(), assessmentId, "REGENERATE_REVISION", "IN_PROGRESS");
    }

    void insertAiOperation(UUID id, UUID assessmentId, String operationType, String status) {
        jdbcTemplate.update(
                "INSERT INTO ai_operations (id, assessment_id, operation_type, requested_by, idempotency_key, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                id, assessmentId, operationType, "uid-1", "key-" + id, status);
    }

    @Test
    void shouldApplyV14AndV15MigrationsCleanlyAfterV13() {
        Integer assessmentRevisionsTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'assessment_revisions'", Integer.class);
        Integer currentRevisionColumnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'assessments' AND column_name = 'current_revision_id'",
                Integer.class);
        Integer lockVersionColumnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'assessments' AND column_name = 'lock_version'",
                Integer.class);

        assertThat(assessmentRevisionsTableCount).isEqualTo(1);
        assertThat(currentRevisionColumnCount).isEqualTo(1);
        assertThat(lockVersionColumnCount).isEqualTo(1);
    }

    @Test
    void shouldSetLockVersionZeroAndCurrentRevisionIdNullForExistingAssessments() {
        Integer lockVersion = jdbcTemplate.queryForObject(
                "SELECT lock_version FROM assessments WHERE id = ?", Integer.class, assessmentId);
        UUID currentRevisionId = jdbcTemplate.queryForObject(
                "SELECT current_revision_id FROM assessments WHERE id = ?", UUID.class, assessmentId);

        assertThat(lockVersion).isZero();
        assertThat(currentRevisionId).isNull();
    }

    @Test
    void shouldRejectDuplicateVersionNumberForSameAssessment() {
        insertAssessmentRevision(UUID.randomUUID(), assessmentId, 1, null);

        assertThatThrownBy(() -> insertAssessmentRevision(UUID.randomUUID(), assessmentId, 1, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldEnforceSelfReferentialForeignKeyOnPreviousRevisionId() {
        assertThatThrownBy(() -> insertAssessmentRevision(UUID.randomUUID(), assessmentId, 2, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowPreviousRevisionIdReferencingARealPriorRevision() {
        UUID v1Id = UUID.randomUUID();
        insertAssessmentRevision(v1Id, assessmentId, 1, null);

        insertAssessmentRevision(UUID.randomUUID(), assessmentId, 2, v1Id);
    }

    void insertAssessmentRevision(UUID id, UUID assessmentId, int versionNumber, UUID previousRevisionId) {
        jdbcTemplate.update(
                "INSERT INTO assessment_revisions " +
                "(id, assessment_id, version_number, previous_revision_id, origin, actor_id, title, context, instructions, objectives, deliverables, constraints) " +
                "VALUES (?, ?, ?, ?, 'AI_GENERATED', 'uid-1', 'title', 'context', 'instructions', '[]'::jsonb, '[]'::jsonb, '[]'::jsonb)",
                id, assessmentId, versionNumber, previousRevisionId);
    }
}
