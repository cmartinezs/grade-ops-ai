package cl.gradeops.ai.api.assessment;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.GetCurrentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.ListDraftVersionsCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.UpdateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.application.usecase.CreateAssessmentBriefHandler;
import cl.gradeops.ai.api.assessment.application.usecase.DraftGenerationCoordinator;
import cl.gradeops.ai.api.assessment.application.usecase.GenerateAssessmentDraftHandler;
import cl.gradeops.ai.api.assessment.application.usecase.GetCurrentDraftHandler;
import cl.gradeops.ai.api.assessment.application.usecase.ListAssessmentsHandler;
import cl.gradeops.ai.api.assessment.application.usecase.ListDraftVersionsHandler;
import cl.gradeops.ai.api.assessment.application.usecase.RegenerateAssessmentDraftHandler;
import cl.gradeops.ai.api.assessment.application.usecase.UpdateAssessmentDraftHandler;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogJpaEntity;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogPersistenceMapper;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Cross-cutting, whole-story integration coverage for the assessment-creation flow, against a
 * live Postgres (Flyway-migrated through V12). Complements — does not duplicate — task-06
 * through task-10's own focused tests: those exercise one endpoint's handler in isolation, this
 * class chains all of them together the way a real teacher session would (brief → generate →
 * regenerate → edit → list → retrieve current → retrieve version history) and asserts
 * consistency at every step. Only {@link AssessmentAgentClient} is stubbed; every persistence
 * adapter, coordinator, and handler runs for real. Requires Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class AssessmentCreationFlowIntegrationTest {

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
    @Autowired PlatformTransactionManager transactionManager;

    AssessmentAgentClient assessmentAgentClient;
    AssessmentBriefPersistenceAdapter briefAdapter;

    CreateAssessmentBriefHandler createBriefHandler;
    GenerateAssessmentDraftHandler generateHandler;
    RegenerateAssessmentDraftHandler regenerateHandler;
    UpdateAssessmentDraftHandler updateHandler;
    GetCurrentDraftHandler getCurrentDraftHandler;
    ListDraftVersionsHandler listDraftVersionsHandler;
    ListAssessmentsHandler listAssessmentsHandler;

    static final String TEACHER_UID = "uid-1";

    @BeforeEach
    void setUp() {
        AssessmentPersistenceAdapter assessmentAdapter =
                new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefJpaRepository, new AssessmentBriefPersistenceMapper());
        AssessmentDraftPersistenceAdapter draftAdapter =
                new AssessmentDraftPersistenceAdapter(draftJpaRepository, new AssessmentDraftPersistenceMapper());
        AgentExecutionLogPersistenceAdapter logAdapter =
                new AgentExecutionLogPersistenceAdapter(logJpaRepository, new AgentExecutionLogPersistenceMapper());
        assessmentAgentClient = mock(AssessmentAgentClient.class);
        DraftGenerationCoordinator coordinator = new DraftGenerationCoordinator(
                draftAdapter, logAdapter, assessmentAgentClient, transactionManager);
        OwnershipVerifier ownershipVerifier = new OwnershipVerifier();

        createBriefHandler = new CreateAssessmentBriefHandler(assessmentAdapter, briefAdapter);
        generateHandler = new GenerateAssessmentDraftHandler(assessmentAdapter, briefAdapter, ownershipVerifier, coordinator);
        regenerateHandler = new RegenerateAssessmentDraftHandler(
                assessmentAdapter, briefAdapter, draftAdapter, ownershipVerifier, coordinator);
        updateHandler = new UpdateAssessmentDraftHandler(assessmentAdapter, draftAdapter, ownershipVerifier);
        getCurrentDraftHandler = new GetCurrentDraftHandler(assessmentAdapter, draftAdapter, ownershipVerifier);
        listDraftVersionsHandler = new ListDraftVersionsHandler(assessmentAdapter, draftAdapter, ownershipVerifier);
        listAssessmentsHandler = new ListAssessmentsHandler(assessmentAdapter);

        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                TEACHER_UID, "Test", "Teacher", TEACHER_UID + "@test.com");
    }

    private static AssessmentAgentResponse response(String title) {
        Instant startedAt = Instant.now().minusSeconds(2);
        Instant finishedAt = Instant.now();
        return new AssessmentAgentResponse(
                new AssessmentAgentResponse.Result(title, "Context " + title, "Instructions " + title,
                        List.of("obj-" + title), List.of("del-" + title), List.of("con-" + title)),
                new AssessmentAgentResponse.Log(UUID.randomUUID(), "assessment", "gemini-2.0-flash", "v1",
                        "in-hash-" + title, "out-hash-" + title, 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt));
    }

    private UUID createBrief() {
        CreateAssessmentBriefResult result = createBriefHandler.execute(new CreateAssessmentBriefCommand(
                TEACHER_UID, "Evaluate loops", "Java loops", "basic", "90min", "Java"));
        entityManager.flush();
        entityManager.clear();
        return UUID.fromString(result.assessmentId());
    }

    @Test
    void fullHappyPathKeepsEveryStepConsistentWithThePrevious() {
        UUID assessmentId = createBrief();

        // The brief is retrievable before any agent call — persist-before-agent-call ordering.
        AssessmentBrief persistedBrief = briefAdapter.findByAssessmentId(new AssessmentId(assessmentId)).orElseThrow();
        assertThat(persistedBrief.getTopic()).isEqualTo("Java loops");

        when(assessmentAgentClient.generate(any())).thenReturn(response("V1"));
        GenerateAssessmentDraftResult generated = generateHandler.execute(
                new GenerateAssessmentDraftCommand(assessmentId, TEACHER_UID));
        entityManager.flush();
        entityManager.clear();
        assertThat(generated.versionNumber()).isEqualTo(1);
        assertThat(generated.title()).isEqualTo("V1");

        when(assessmentAgentClient.generate(any())).thenReturn(response("V2"));
        GenerateAssessmentDraftResult regenerated = regenerateHandler.execute(
                new RegenerateAssessmentDraftCommand(assessmentId, TEACHER_UID, "make it harder"));
        entityManager.flush();
        entityManager.clear();
        assertThat(regenerated.versionNumber()).isEqualTo(2);
        assertThat(regenerated.title()).isEqualTo("V2");

        GenerateAssessmentDraftResult edited = updateHandler.execute(new UpdateAssessmentDraftCommand(
                assessmentId, TEACHER_UID, "V2 edited", null, null, null, null, null));
        entityManager.flush();
        entityManager.clear();
        // Editing does not create a new version: same draft id and version number as v2.
        assertThat(edited.draftId()).isEqualTo(regenerated.draftId());
        assertThat(edited.versionNumber()).isEqualTo(2);
        assertThat(edited.title()).isEqualTo("V2 edited");

        // Dashboard listing reflects the assessment with the latest (edited) title.
        List<AssessmentSummaryResult> summaries = listAssessmentsHandler.execute(TEACHER_UID);
        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).id()).isEqualTo(assessmentId.toString());
        assertThat(summaries.get(0).title()).isEqualTo("V2 edited");

        // Retrieve current draft matches the edited state.
        GenerateAssessmentDraftResult current = getCurrentDraftHandler.execute(
                new GetCurrentDraftCommand(assessmentId, TEACHER_UID));
        assertThat(current.draftId()).isEqualTo(edited.draftId());
        assertThat(current.versionNumber()).isEqualTo(2);
        assertThat(current.title()).isEqualTo("V2 edited");

        // Version history lists both versions, newest first, v1 untouched by the edit.
        List<GenerateAssessmentDraftResult> versions = listDraftVersionsHandler.execute(
                new ListDraftVersionsCommand(assessmentId, TEACHER_UID));
        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).versionNumber()).isEqualTo(2);
        assertThat(versions.get(0).title()).isEqualTo("V2 edited");
        assertThat(versions.get(1).versionNumber()).isEqualTo(1);
        assertThat(versions.get(1).title()).isEqualTo("V1");
        assertThat(versions.get(1).draftId()).isEqualTo(generated.draftId());
    }

    @Test
    void agentFailureDuringGenerationLeavesBriefIntactWithOnlyAFailureLogAndNoDraft() {
        UUID assessmentId = createBrief();

        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.AGENT_REJECTED, "rejected", new RuntimeException());
        when(assessmentAgentClient.generate(any())).thenThrow(agentEx);

        assertThatThrownBy(() -> generateHandler.execute(new GenerateAssessmentDraftCommand(assessmentId, TEACHER_UID)))
                .isSameAs(agentEx);

        entityManager.flush();
        entityManager.clear();

        // The brief survives the agent failure untouched.
        AssessmentBrief persistedBrief = briefAdapter.findByAssessmentId(new AssessmentId(assessmentId)).orElseThrow();
        assertThat(persistedBrief.getTopic()).isEqualTo("Java loops");

        // No draft was created.
        assertThat(draftJpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessmentId)).isEmpty();

        // Exactly one failure log was recorded for this assessment.
        List<AgentExecutionLogJpaEntity> logs = logJpaRepository.findAll().stream()
                .filter(l -> l.getAssessmentId().equals(assessmentId))
                .toList();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getStatus()).isEqualTo("FAILED");
        assertThat(logs.get(0).getErrorCode()).isEqualTo("AGENT_REJECTED");
        assertThat(logs.get(0).getDraftId()).isNull();

        // The assessment remains queryable via the dashboard listing, falling back to the
        // brief's topic since no draft title exists yet.
        List<AssessmentSummaryResult> summaries = listAssessmentsHandler.execute(TEACHER_UID);
        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).id()).isEqualTo(assessmentId.toString());
        assertThat(summaries.get(0).title()).isEqualTo("Java loops");
    }

    @Test
    void twoRegenerationsInARowProduceThreeDistinctRetrievableVersions() {
        UUID assessmentId = createBrief();

        when(assessmentAgentClient.generate(any())).thenReturn(response("V1"));
        GenerateAssessmentDraftResult v1 = generateHandler.execute(
                new GenerateAssessmentDraftCommand(assessmentId, TEACHER_UID));
        entityManager.flush();
        entityManager.clear();

        when(assessmentAgentClient.generate(any())).thenReturn(response("V2"));
        GenerateAssessmentDraftResult v2 = regenerateHandler.execute(
                new RegenerateAssessmentDraftCommand(assessmentId, TEACHER_UID, "harder"));
        entityManager.flush();
        entityManager.clear();

        when(assessmentAgentClient.generate(any())).thenReturn(response("V3"));
        GenerateAssessmentDraftResult v3 = regenerateHandler.execute(
                new RegenerateAssessmentDraftCommand(assessmentId, TEACHER_UID, "harder still"));
        entityManager.flush();
        entityManager.clear();

        assertThat(v1.versionNumber()).isEqualTo(1);
        assertThat(v2.versionNumber()).isEqualTo(2);
        assertThat(v3.versionNumber()).isEqualTo(3);
        assertThat(List.of(v1.draftId(), v2.draftId(), v3.draftId())).doesNotHaveDuplicates();

        List<GenerateAssessmentDraftResult> versions = listDraftVersionsHandler.execute(
                new ListDraftVersionsCommand(assessmentId, TEACHER_UID));
        assertThat(versions).hasSize(3);
        assertThat(versions).extracting(GenerateAssessmentDraftResult::versionNumber)
                .containsExactly(3, 2, 1);
        assertThat(versions).extracting(GenerateAssessmentDraftResult::title)
                .containsExactly("V3", "V2", "V1");

        // None of the earlier versions were overwritten.
        AssessmentDraftJpaEntity v1Entity = draftJpaRepository.findById(v1.draftId()).orElseThrow();
        AssessmentDraftJpaEntity v2Entity = draftJpaRepository.findById(v2.draftId()).orElseThrow();
        assertThat(v1Entity.getTitle()).isEqualTo("V1");
        assertThat(v2Entity.getTitle()).isEqualTo("V2");
        assertThat(v2Entity.getPreviousVersionId()).isEqualTo(v1.draftId());
        AssessmentDraftJpaEntity v3Entity = draftJpaRepository.findById(v3.draftId()).orElseThrow();
        assertThat(v3Entity.getPreviousVersionId()).isEqualTo(v2.draftId());

        // Each of the three executions produced its own distinct AgentExecutionLog.
        List<AgentExecutionLogJpaEntity> logs = logJpaRepository.findAll().stream()
                .filter(l -> l.getAssessmentId().equals(assessmentId))
                .toList();
        assertThat(logs).hasSize(3);
        assertThat(logs).extracting(AgentExecutionLogJpaEntity::getDraftId)
                .containsExactlyInAnyOrder(v1.draftId(), v2.draftId(), v3.draftId());
    }

    @Test
    void editAfterRegenerationUpdatesOnlyTheLatestVersion() {
        UUID assessmentId = createBrief();

        when(assessmentAgentClient.generate(any())).thenReturn(response("V1"));
        GenerateAssessmentDraftResult v1 = generateHandler.execute(
                new GenerateAssessmentDraftCommand(assessmentId, TEACHER_UID));
        entityManager.flush();
        entityManager.clear();

        when(assessmentAgentClient.generate(any())).thenReturn(response("V2"));
        GenerateAssessmentDraftResult v2 = regenerateHandler.execute(
                new RegenerateAssessmentDraftCommand(assessmentId, TEACHER_UID, "harder"));
        entityManager.flush();
        entityManager.clear();

        GenerateAssessmentDraftResult edited = updateHandler.execute(new UpdateAssessmentDraftCommand(
                assessmentId, TEACHER_UID, "V2 edited by teacher", null, null, null, null, null));
        entityManager.flush();
        entityManager.clear();

        // The edit landed on v2's row in place — same id, same version number.
        assertThat(edited.draftId()).isEqualTo(v2.draftId());
        assertThat(edited.versionNumber()).isEqualTo(2);
        assertThat(edited.title()).isEqualTo("V2 edited by teacher");

        // v1 is completely untouched by the edit.
        AssessmentDraftJpaEntity v1Entity = draftJpaRepository.findById(v1.draftId()).orElseThrow();
        assertThat(v1Entity.getTitle()).isEqualTo("V1");
        assertThat(v1Entity.getVersionNumber()).isEqualTo(1);

        // Still exactly two versions — the edit did not create a third.
        List<GenerateAssessmentDraftResult> versions = listDraftVersionsHandler.execute(
                new ListDraftVersionsCommand(assessmentId, TEACHER_UID));
        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).title()).isEqualTo("V2 edited by teacher");
        assertThat(versions.get(1).title()).isEqualTo("V1");
    }
}
