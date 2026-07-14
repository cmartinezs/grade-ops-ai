package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validates AssessmentBriefPersistenceAdapter against a real Flyway schema (V1-V10).
 * Requires Docker. Each test runs in a rolled-back transaction — no teardown needed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class AssessmentBriefPersistenceAdapterIntegrationTest {

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
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentBriefPersistenceAdapter briefAdapter;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentRepository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefRepository, new AssessmentBriefPersistenceMapper());
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
    }

    @Test
    void shouldRoundTripBriefThroughSaveAndFindByAssessmentId() {
        Assessment assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);

        AssessmentBrief brief = AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java");
        briefAdapter.save(brief);

        // Force a genuine round trip through Postgres instead of returning the same
        // managed instance from Hibernate's first-level cache — otherwise this assertion
        // would compare createdAt against itself and never actually exercise persistence.
        entityManager.flush();
        entityManager.clear();

        Optional<AssessmentBrief> found = briefAdapter.findByAssessmentId(assessment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(brief.getId());
        assertThat(found.get().getAssessmentId()).isEqualTo(assessment.getId());
        assertThat(found.get().getLearningGoal()).isEqualTo("goal");
        assertThat(found.get().getTopic()).isEqualTo("topic");
        assertThat(found.get().getLevel()).isEqualTo("basic");
        assertThat(found.get().getDuration()).isEqualTo("90min");
        assertThat(found.get().getLanguage()).isEqualTo("Java");
        // PostgreSQL TIMESTAMPTZ has microsecond precision, and pgjdbc rounds rather than
        // truncates when storing — comparing at MICROS is flaky by +/-1us depending on the
        // discarded nanosecond remainder. Truncate both sides to MILLIS, matching
        // PasswordResetCodeJpaRepositoryIntegrationTest's established convention while
        // staying clear of that rounding boundary.
        assertThat(found.get().getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(brief.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    void shouldReturnEmptyWhenNoBriefExistsForAssessment() {
        Assessment assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);

        assertThat(briefAdapter.findByAssessmentId(assessment.getId())).isEmpty();
    }

    @Test
    void shouldRejectSecondBriefForSameAssessmentDueToUniqueConstraint() {
        Assessment assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java"));

        AssessmentBrief second = AssessmentBrief.create(assessment.getId(), "goal2", "topic2", "advanced", "60min", "Python");

        // save() alone only queues the insert in the persistence context; the unique
        // constraint isn't enforced by Postgres until the statement actually flushes.
        assertThatThrownBy(() -> {
            briefAdapter.save(second);
            briefRepository.flush();
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
