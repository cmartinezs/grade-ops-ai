package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import cl.gradeops.ai.api.assessment.domain.model.RevisionOrigin;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @Autowired AssessmentRevisionJpaRepository revisionRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    AssessmentPersistenceAdapter adapter;
    AssessmentBriefPersistenceAdapter briefAdapter;
    AssessmentRevisionPersistenceAdapter revisionAdapter;

    @BeforeEach
    void setUp() {
        adapter = new AssessmentPersistenceAdapter(repository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefRepository, new AssessmentBriefPersistenceMapper());
        revisionAdapter = new AssessmentRevisionPersistenceAdapter(revisionRepository, new AssessmentRevisionPersistenceMapper());
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
    void shouldUseCurrentRevisionTitleWhenRevisionExists() {
        Assessment assessment = Assessment.create("uid-1");
        adapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "Brief Topic", "basic", "90min", "Java"));
        AssessmentRevision v1 = AssessmentRevision.restore(UUID.randomUUID(), assessment.getId(), 1, null,
                RevisionOrigin.AI_GENERATED, "uid-1", null, null,
                "Draft Title v1", "ctx", "instr", List.of(), List.of(), List.of(), Instant.now());
        revisionAdapter.save(v1);
        AssessmentRevision v2 = AssessmentRevision.restore(UUID.randomUUID(), assessment.getId(), 2, v1.getId(),
                RevisionOrigin.AI_GENERATED, "uid-1", null, null,
                "Draft Title v2", "ctx2", "instr2", List.of(), List.of(), List.of(), Instant.now());
        revisionAdapter.save(v2);
        adapter.save(assessment.withCurrentRevision(v2.getId()));

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
