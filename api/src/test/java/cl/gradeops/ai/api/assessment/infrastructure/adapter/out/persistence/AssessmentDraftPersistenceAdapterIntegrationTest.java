package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates AssessmentDraftPersistenceAdapter against a real Flyway schema (V1-V11),
 * including JSON list-column mapping. Requires Docker. Each test runs in a rolled-back
 * transaction — no teardown needed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class AssessmentDraftPersistenceAdapterIntegrationTest {

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
    @Autowired AssessmentDraftJpaRepository draftRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentDraftPersistenceAdapter draftAdapter;
    Assessment assessment;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentRepository, new AssessmentPersistenceMapper());
        draftAdapter = new AssessmentDraftPersistenceAdapter(draftRepository, new AssessmentDraftPersistenceMapper());
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
    }

    @Test
    void shouldRoundTripDraftIncludingJsonListFieldsThroughSaveAndFindCurrent() {
        AssessmentDraft draft = AssessmentDraft.generate(assessment.getId(), "title", "context", "instructions",
                List.of("obj1", "obj2"), List.of("del1"), List.of("con1", "con2", "con3"), null);
        draftAdapter.save(draft);

        entityManager.flush();
        entityManager.clear();

        Optional<AssessmentDraft> found = draftAdapter.findCurrentByAssessmentId(assessment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(draft.getId());
        assertThat(found.get().getVersionNumber()).isEqualTo(1);
        assertThat(found.get().getPreviousVersionId()).isNull();
        assertThat(found.get().getTitle()).isEqualTo("title");
        assertThat(found.get().getContext()).isEqualTo("context");
        assertThat(found.get().getInstructions()).isEqualTo("instructions");
        assertThat(found.get().getObjectives()).containsExactly("obj1", "obj2");
        assertThat(found.get().getDeliverables()).containsExactly("del1");
        assertThat(found.get().getConstraints()).containsExactly("con1", "con2", "con3");
        assertThat(found.get().getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(draft.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    void shouldReturnHighestVersionWhenMultipleVersionsExist() {
        AssessmentDraft v1 = AssessmentDraft.generate(assessment.getId(), "t1", "c1", "i1", List.of(), List.of(), List.of(), null);
        draftAdapter.save(v1);
        AssessmentDraft v2 = AssessmentDraft.regenerate(v1, "t2", "c2", "i2", List.of(), List.of(), List.of(), null);
        draftAdapter.save(v2);
        AssessmentDraft v3 = AssessmentDraft.regenerate(v2, "t3", "c3", "i3", List.of(), List.of(), List.of(), null);
        draftAdapter.save(v3);

        entityManager.flush();
        entityManager.clear();

        Optional<AssessmentDraft> current = draftAdapter.findCurrentByAssessmentId(assessment.getId());

        assertThat(current).isPresent();
        assertThat(current.get().getId()).isEqualTo(v3.getId());
        assertThat(current.get().getVersionNumber()).isEqualTo(3);
        assertThat(current.get().getPreviousVersionId()).isEqualTo(v2.getId());
    }

    @Test
    void shouldRetainAllVersionsWithoutOverwriteWhenFindingAll() {
        AssessmentDraft v1 = AssessmentDraft.generate(assessment.getId(), "t1", "c1", "i1", List.of(), List.of(), List.of(), null);
        draftAdapter.save(v1);
        AssessmentDraft v2 = AssessmentDraft.regenerate(v1, "t2", "c2", "i2", List.of(), List.of(), List.of(), null);
        draftAdapter.save(v2);
        AssessmentDraft v3 = AssessmentDraft.regenerate(v2, "t3", "c3", "i3", List.of(), List.of(), List.of(), null);
        draftAdapter.save(v3);

        entityManager.flush();
        entityManager.clear();

        List<AssessmentDraft> all = draftAdapter.findAllByAssessmentId(assessment.getId());

        assertThat(all).hasSize(3);
        assertThat(all).extracting(AssessmentDraft::getVersionNumber).containsExactly(3, 2, 1);
        assertThat(all).extracting(AssessmentDraft::getTitle).containsExactly("t3", "t2", "t1");
    }
}
