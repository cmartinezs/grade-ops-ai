package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.assessment.domain.model.RevisionOrigin;
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

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates AssessmentRevisionPersistenceAdapter against a real Flyway schema (V1-V16),
 * including JSON list-column mapping and provenance fields. This aggregate is not yet wired
 * into any use case — this test exercises it directly, mirroring AssessmentDraftPersistenceAdapterIntegrationTest.
 * Requires Docker. Each test runs in a rolled-back transaction — no teardown needed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class AssessmentRevisionPersistenceAdapterTest {

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
    @Autowired AssessmentRevisionJpaRepository revisionRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentRevisionPersistenceAdapter revisionAdapter;
    Assessment assessment;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentRepository, new AssessmentPersistenceMapper());
        revisionAdapter = new AssessmentRevisionPersistenceAdapter(revisionRepository, new AssessmentRevisionPersistenceMapper());
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        entityManager.flush();
    }

    /** Directly inserts a minimal ai_operations/agent_attempts pair to satisfy the FK on source_agent_attempt_id. */
    UUID insertAgentAttempt() {
        UUID aiOperationId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO ai_operations (id, assessment_id, operation_type, requested_by, idempotency_key, status) " +
                "VALUES (?, ?, 'CREATE_INITIAL_REVISION', 'uid-1', ?, 'SUCCEEDED')",
                aiOperationId, assessment.getId().value(), "key-" + aiOperationId);
        UUID attemptId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO agent_attempts (id, ai_operation_id, attempt_number, agent_name, dispatched_at, status) " +
                "VALUES (?, ?, 1, 'assessment-agent', now(), 'DISPATCHED')",
                attemptId, aiOperationId);
        return attemptId;
    }

    @Test
    void shouldRoundTripAiGeneratedRevisionIncludingJsonListFieldsAndProvenance() {
        UUID sourceAttemptId = insertAgentAttempt();
        AssessmentRevision revision = AssessmentRevision.generateFromAi(assessment.getId(), "title", "context", "instructions",
                List.of("obj1", "obj2"), List.of("del1"), List.of("con1", "con2", "con3"), "uid-1", sourceAttemptId);

        revisionAdapter.save(revision);
        entityManager.flush();
        entityManager.clear();

        Optional<AssessmentRevision> found = revisionAdapter.findById(revision.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(revision.getId());
        assertThat(found.get().getAssessmentId()).isEqualTo(assessment.getId());
        assertThat(found.get().getVersionNumber()).isEqualTo(1);
        assertThat(found.get().getPreviousRevisionId()).isNull();
        assertThat(found.get().getOrigin()).isEqualTo(RevisionOrigin.AI_GENERATED);
        assertThat(found.get().getActorId()).isEqualTo("uid-1");
        assertThat(found.get().getReason()).isNull();
        assertThat(found.get().getSourceAgentAttemptId()).isEqualTo(sourceAttemptId);
        assertThat(found.get().getTitle()).isEqualTo("title");
        assertThat(found.get().getContext()).isEqualTo("context");
        assertThat(found.get().getInstructions()).isEqualTo("instructions");
        assertThat(found.get().getObjectives()).containsExactly("obj1", "obj2");
        assertThat(found.get().getDeliverables()).containsExactly("del1");
        assertThat(found.get().getConstraints()).containsExactly("con1", "con2", "con3");
        assertThat(found.get().getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(revision.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    void shouldRoundTripHumanEditedRevisionWithNullSourceAgentAttemptIdAndPreserveThePriorAiGeneratedRevision() {
        AssessmentRevision v1 = AssessmentRevision.generateFromAi(assessment.getId(), "t1", "c1", "i1",
                List.of(), List.of(), List.of(), "uid-1", insertAgentAttempt());
        revisionAdapter.save(v1);

        AssessmentRevision v2 = AssessmentRevision.createFromHumanEdit(v1, "edited", "c2", "i2",
                List.of("o2"), List.of("d2"), List.of("k2"), "uid-1", "fixed a typo");
        revisionAdapter.save(v2);

        entityManager.flush();
        entityManager.clear();

        Optional<AssessmentRevision> foundEdit = revisionAdapter.findById(v2.getId());
        Optional<AssessmentRevision> foundOriginal = revisionAdapter.findById(v1.getId());

        assertThat(foundEdit).isPresent();
        assertThat(foundEdit.get().getOrigin()).isEqualTo(RevisionOrigin.HUMAN_EDITED);
        assertThat(foundEdit.get().getSourceAgentAttemptId()).isNull();
        assertThat(foundEdit.get().getReason()).isEqualTo("fixed a typo");
        assertThat(foundEdit.get().getPreviousRevisionId()).isEqualTo(v1.getId());
        assertThat(foundOriginal).isPresent();
        assertThat(foundOriginal.get().getOrigin()).isEqualTo(RevisionOrigin.AI_GENERATED);
        assertThat(foundOriginal.get().getTitle()).isEqualTo("t1");
    }

    @Test
    void shouldRetainFullChainWithoutOverwriteWhenFindingAllByAssessmentId() {
        AssessmentRevision v1 = AssessmentRevision.generateFromAi(assessment.getId(), "t1", "c1", "i1",
                List.of(), List.of(), List.of(), "uid-1", insertAgentAttempt());
        revisionAdapter.save(v1);

        AssessmentRevision v2 = AssessmentRevision.regenerateFromAi(v1, "t2", "c2", "i2",
                List.of(), List.of(), List.of(), "uid-1", "adjust", insertAgentAttempt());
        revisionAdapter.save(v2);

        AssessmentRevision v3 = AssessmentRevision.createFromHumanEdit(v2, "t3", "c3", "i3",
                List.of(), List.of(), List.of(), "uid-2", "manual fix");
        revisionAdapter.save(v3);

        entityManager.flush();
        entityManager.clear();

        List<AssessmentRevision> all = revisionAdapter.findAllByAssessmentId(assessment.getId());

        assertThat(all).hasSize(3);
        assertThat(all).extracting(AssessmentRevision::getVersionNumber).containsExactly(3, 2, 1);
        assertThat(all.get(0).getOrigin()).isEqualTo(RevisionOrigin.HUMAN_EDITED);
        assertThat(all.get(1).getOrigin()).isEqualTo(RevisionOrigin.AI_GENERATED);
        assertThat(all.get(2).getOrigin()).isEqualTo(RevisionOrigin.AI_GENERATED);
        assertThat(all.get(0).getPreviousRevisionId()).isEqualTo(v2.getId());
        assertThat(all.get(1).getPreviousRevisionId()).isEqualTo(v1.getId());
        assertThat(all.get(2).getPreviousRevisionId()).isNull();
    }

    @Test
    void shouldReturnEmptyWhenRevisionDoesNotExist() {
        assertThat(revisionAdapter.findById(UUID.randomUUID())).isEmpty();
    }
}
