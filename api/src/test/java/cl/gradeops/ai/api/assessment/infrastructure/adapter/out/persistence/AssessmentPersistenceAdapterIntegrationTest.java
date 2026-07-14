package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates AssessmentPersistenceAdapter against a real Flyway schema (V1-V12).
 * Requires Docker. Each test runs in a rolled-back transaction — no teardown needed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class AssessmentPersistenceAdapterIntegrationTest {

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

    @Autowired AssessmentJpaRepository repository;
    @Autowired AssessmentBriefJpaRepository briefRepository;
    @Autowired AssessmentDraftJpaRepository draftRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    AssessmentPersistenceAdapter adapter;
    AssessmentBriefPersistenceAdapter briefAdapter;
    AssessmentDraftPersistenceAdapter draftAdapter;

    @BeforeEach
    void setUp() {
        adapter = new AssessmentPersistenceAdapter(repository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefRepository, new AssessmentBriefPersistenceMapper());
        draftAdapter = new AssessmentDraftPersistenceAdapter(draftRepository, new AssessmentDraftPersistenceMapper());
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-2", "Other", "Teacher", "uid-2@test.com");
    }

    @Test
    void shouldRoundTripAssessmentThroughSaveAndFindById() {
        Assessment assessment = Assessment.create("uid-1");

        adapter.save(assessment);
        Optional<Assessment> found = adapter.findById(assessment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(assessment.getId());
        assertThat(found.get().getTeacherUid()).isEqualTo("uid-1");
        assertThat(found.get().getStatus()).isEqualTo(AssessmentStatus.DRAFT);
        assertThat(found.get().getCreatedAt()).isEqualTo(assessment.getCreatedAt());
    }

    @Test
    void shouldReturnEmptyListWhenTeacherHasNoAssessments() {
        List<?> result = adapter.findAllByTeacherId("uid-1");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldUseCurrentDraftTitleWhenDraftExists() {
        Assessment assessment = Assessment.create("uid-1");
        adapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "Brief Topic", "basic", "90min", "Java"));
        AssessmentDraft v1 = AssessmentDraft.generate(assessment.getId(), "Draft Title v1", "ctx", "instr",
                List.of(), List.of(), List.of(), null);
        draftAdapter.save(v1);
        AssessmentDraft v2 = AssessmentDraft.regenerate(v1, "Draft Title v2", "ctx2", "instr2",
                List.of(), List.of(), List.of(), null);
        draftAdapter.save(v2);

        List<AssessmentSummaryResult> result = adapter.findAllByTeacherId("uid-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(assessment.getId().value().toString());
        assertThat(result.get(0).title()).isEqualTo("Draft Title v2");
        assertThat(result.get(0).status()).isEqualTo(AssessmentStatus.DRAFT);
    }

    @Test
    void shouldFallBackToBriefTopicWhenNoDraftExistsYet() {
        Assessment assessment = Assessment.create("uid-1");
        adapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "Brief Topic", "basic", "90min", "Java"));

        List<AssessmentSummaryResult> result = adapter.findAllByTeacherId("uid-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Brief Topic");
    }

    @Test
    void shouldOnlyReturnAssessmentsOwnedByTheRequestingTeacher() {
        Assessment mine = Assessment.create("uid-1");
        adapter.save(mine);
        briefAdapter.save(AssessmentBrief.create(mine.getId(), "goal", "Mine", "basic", "90min", "Java"));
        Assessment theirs = Assessment.create("uid-2");
        adapter.save(theirs);
        briefAdapter.save(AssessmentBrief.create(theirs.getId(), "goal", "Theirs", "basic", "90min", "Java"));

        List<AssessmentSummaryResult> result = adapter.findAllByTeacherId("uid-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Mine");
    }
}
