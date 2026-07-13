package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Task-04 (validate-db-orm-consistency): exercises the full FK chain across all three
 * tables introduced in this story together — assessments <- assessment_briefs and
 * assessments <- assessment_drafts <- assessment_drafts.previous_version_id — which none
 * of task-01/02/03's own per-table integration tests cover in a single scenario.
 * Requires Docker. Each test runs in a rolled-back transaction — no teardown needed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class AssessmentPersistenceFkChainIntegrationTest {

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
    @Autowired AssessmentBriefJpaRepository briefRepository;
    @Autowired AssessmentDraftJpaRepository draftRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentBriefPersistenceAdapter briefAdapter;
    AssessmentDraftPersistenceAdapter draftAdapter;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentRepository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefRepository, new AssessmentBriefPersistenceMapper());
        draftAdapter = new AssessmentDraftPersistenceAdapter(draftRepository, new AssessmentDraftPersistenceMapper());
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
    }

    @Test
    void shouldPersistFullFkChainAssessmentBriefAndTwoDraftVersionsWithNoOrphans() {
        Assessment assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);

        AssessmentBrief brief = AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java");
        briefAdapter.save(brief);

        AssessmentDraft v1 = AssessmentDraft.generate(assessment.getId(), "t1", "c1", "i1",
                List.of("obj1"), List.of("del1"), List.of("con1"), null);
        draftAdapter.save(v1);
        AssessmentDraft v2 = AssessmentDraft.regenerate(v1, "t2", "c2", "i2",
                List.of("obj2"), List.of("del2"), List.of("con2"), null);
        draftAdapter.save(v2);

        entityManager.flush();
        entityManager.clear();

        assertThat(assessmentAdapter.findById(assessment.getId())).isPresent();
        assertThat(briefAdapter.findByAssessmentId(assessment.getId())).isPresent();
        List<AssessmentDraft> drafts = draftAdapter.findAllByAssessmentId(assessment.getId());
        assertThat(drafts).hasSize(2);
        assertThat(drafts.get(0).getVersionNumber()).isEqualTo(2);
        assertThat(drafts.get(0).getPreviousVersionId()).isEqualTo(v1.getId());
        assertThat(drafts.get(1).getVersionNumber()).isEqualTo(1);
        assertThat(drafts.get(1).getPreviousVersionId()).isNull();
    }

    @Test
    void shouldRejectBriefReferencingNonExistentAssessment() {
        AssessmentBrief orphanBrief = AssessmentBrief.create(
                new AssessmentId(UUID.randomUUID()),
                "goal", "topic", "basic", "90min", "Java");

        assertThatThrownBy(() -> {
            briefAdapter.save(orphanBrief);
            briefRepository.flush();
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectDraftReferencingNonExistentAssessment() {
        AssessmentDraft orphanDraft = AssessmentDraft.generate(
                new AssessmentId(UUID.randomUUID()),
                "t", "c", "i", List.of(), List.of(), List.of(), null);

        assertThatThrownBy(() -> {
            draftAdapter.save(orphanDraft);
            draftRepository.flush();
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void shouldCascadeDeleteBriefAndDraftsWhenAssessmentIsDeleted() {
        Assessment assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java"));
        AssessmentDraft v1 = AssessmentDraft.generate(assessment.getId(), "t1", "c1", "i1", List.of(), List.of(), List.of(), null);
        draftAdapter.save(v1);

        entityManager.flush();

        jdbcTemplate.update("DELETE FROM assessments WHERE id = ?", assessment.getId().value());
        entityManager.clear();

        assertThat(briefAdapter.findByAssessmentId(assessment.getId())).isEmpty();
        assertThat(draftAdapter.findAllByAssessmentId(assessment.getId())).isEmpty();
    }
}
