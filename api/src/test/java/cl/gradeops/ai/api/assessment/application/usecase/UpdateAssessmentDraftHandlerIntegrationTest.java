package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.UpdateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoPriorDraftException;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftJpaEntity;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceMapper;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises {@link UpdateAssessmentDraftHandler} with real repositories against a live
 * Postgres (Flyway-migrated through V16): seeds v1 directly as a pre-existing {@code
 * AssessmentDraft} (this handler never calls {@code agents/}, so no coordinator/agent client is
 * needed here), edits a field, and verifies the row id and version number are unchanged, only
 * the edited field(s) differ, and no new {@code AgentExecutionLog} row was created. Requires
 * Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class UpdateAssessmentDraftHandlerIntegrationTest {

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

    @Autowired AssessmentJpaRepository assessmentJpaRepository;
    @Autowired AssessmentBriefJpaRepository briefJpaRepository;
    @Autowired AssessmentDraftJpaRepository draftJpaRepository;
    @Autowired AgentExecutionLogJpaRepository logJpaRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentDraftPersistenceAdapter draftAdapter;
    UpdateAssessmentDraftHandler updateHandler;

    Assessment assessment;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        AssessmentBriefPersistenceAdapter briefAdapter =
                new AssessmentBriefPersistenceAdapter(briefJpaRepository, new AssessmentBriefPersistenceMapper());
        draftAdapter = new AssessmentDraftPersistenceAdapter(draftJpaRepository, new AssessmentDraftPersistenceMapper());
        OwnershipVerifier ownershipVerifier = new OwnershipVerifier();

        updateHandler = new UpdateAssessmentDraftHandler(assessmentAdapter, draftAdapter, ownershipVerifier);

        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java"));
        entityManager.flush();
        entityManager.clear();
    }

    private GenerateAssessmentDraftResult seedV1Draft() {
        AssessmentDraft draft = AssessmentDraft.generate(assessment.getId(), "Title v1", "Context", "Instructions",
                List.of("obj"), List.of("del"), List.of("con"), null);
        draftAdapter.save(draft);
        entityManager.flush();
        entityManager.clear();
        return new GenerateAssessmentDraftResult(draft.getId(), draft.getTitle(), draft.getContext(),
                draft.getInstructions(), draft.getObjectives(), draft.getDeliverables(), draft.getConstraints(),
                draft.getVersionNumber());
    }

    @Test
    void shouldUpdateRowInPlaceWithoutNewVersionOrNewLog() {
        GenerateAssessmentDraftResult v1Result = seedV1Draft();
        long logCountBefore = logJpaRepository.count();

        GenerateAssessmentDraftResult editResult = updateHandler.execute(new UpdateAssessmentDraftCommand(
                assessment.getId().value(), "uid-1", "Edited title", null, null, null, null, null));

        entityManager.flush();
        entityManager.clear();

        // Same row id and version number — an in-place update, not a new version.
        assertThat(editResult.draftId()).isEqualTo(v1Result.draftId());
        assertThat(editResult.versionNumber()).isEqualTo(1);
        assertThat(editResult.title()).isEqualTo("Edited title");
        // Unedited fields are preserved.
        assertThat(editResult.context()).isEqualTo("Context");
        assertThat(editResult.instructions()).isEqualTo("Instructions");

        List<AssessmentDraftJpaEntity> allDrafts = draftJpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(
                assessment.getId().value());
        assertThat(allDrafts).hasSize(1);
        assertThat(allDrafts.get(0).getId()).isEqualTo(v1Result.draftId());
        assertThat(allDrafts.get(0).getTitle()).isEqualTo("Edited title");

        // No new AgentExecutionLog was created by the edit.
        assertThat(logJpaRepository.count()).isEqualTo(logCountBefore);
    }

    @Test
    void shouldRejectEditWhenNoPriorDraftExists() {
        assertThatThrownBy(() -> updateHandler.execute(new UpdateAssessmentDraftCommand(
                assessment.getId().value(), "uid-1", "Edited title", null, null, null, null, null)))
                .isInstanceOf(NoPriorDraftException.class);
    }
}
