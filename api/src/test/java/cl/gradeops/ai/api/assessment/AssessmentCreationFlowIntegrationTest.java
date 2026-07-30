package cl.gradeops.ai.api.assessment;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.command.GetCurrentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.ListDraftVersionsCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.application.usecase.CreateAssessmentBriefHandler;
import cl.gradeops.ai.api.assessment.application.usecase.GetCurrentDraftHandler;
import cl.gradeops.ai.api.assessment.application.usecase.ListAssessmentsHandler;
import cl.gradeops.ai.api.assessment.application.usecase.ListDraftVersionsHandler;
import cl.gradeops.ai.api.assessment.application.usecase.RegenerateAssessmentDraftHandler;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
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
 * live Postgres (Flyway-migrated through V16). Complements — does not duplicate — this
 * packet's own per-handler focused tests: those exercise one handler in isolation, this class
 * chains several together the way a real teacher session would (brief → regenerate → edit →
 * list → retrieve current → retrieve version history) and asserts consistency at every step.
 *
 * <p>Session A2 (Task 07B) pivoted {@code GenerateAssessmentDraftHandler} onto the durable
 * {@code AssessmentRevision} model — it no longer produces an {@code AssessmentDraft} row, so
 * it can no longer feed this chain's {@code RegenerateAssessmentDraftHandler} step (still
 * {@code AssessmentDraft}-based this session; regenerate's own migration onto revisions is
 * Task 09, a later session in the same packet). "V1" is therefore seeded directly as a
 * pre-existing {@code AssessmentDraft} below, simulating an assessment already generated before
 * this cut, exactly the legacy shape Task 07B's own risk section requires the initial generation
 * path to tolerate. {@code GenerateAssessmentDraftHandler}'s own behavior is covered separately
 * by {@code GenerateAssessmentDraftHandlerIntegrationTest}. Only {@link AssessmentAgentClient} is
 * stubbed; every persistence adapter and handler exercised here runs for real. Requires Docker.
 *
 * <p>Session A3 (Task 08) removed {@code UpdateAssessmentDraftHandler} — human edits now create
 * an immutable {@code AssessmentRevision} via {@code CreateHumanRevisionHandler}, which requires
 * an {@code Assessment.currentRevisionId} to exist (not applicable to this file's still-legacy,
 * {@code AssessmentDraft}-seeded chain). This file's own "edit preserves id/version" coverage
 * was therefore deleted outright, not adapted — TEST-PLAN.md's regression guard #2. A
 * revision-based whole-story equivalent (generate → regenerate → human-edit, all via {@code
 * AssessmentRevision}) is added by Task 09/10 once regenerate and the read handlers also move
 * onto the revision model.
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
    AssessmentDraftPersistenceAdapter draftAdapter;

    CreateAssessmentBriefHandler createBriefHandler;
    RegenerateAssessmentDraftHandler regenerateHandler;
    GetCurrentDraftHandler getCurrentDraftHandler;
    ListDraftVersionsHandler listDraftVersionsHandler;
    ListAssessmentsHandler listAssessmentsHandler;

    static final String TEACHER_UID = "uid-1";

    @BeforeEach
    void setUp() {
        AssessmentPersistenceAdapter assessmentAdapter =
                new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefJpaRepository, new AssessmentBriefPersistenceMapper());
        draftAdapter = new AssessmentDraftPersistenceAdapter(draftJpaRepository, new AssessmentDraftPersistenceMapper());
        AgentExecutionLogPersistenceAdapter logAdapter =
                new AgentExecutionLogPersistenceAdapter(logJpaRepository, new AgentExecutionLogPersistenceMapper());
        assessmentAgentClient = mock(AssessmentAgentClient.class);
        OwnershipVerifier ownershipVerifier = new OwnershipVerifier();

        createBriefHandler = new CreateAssessmentBriefHandler(assessmentAdapter, briefAdapter);
        regenerateHandler = new RegenerateAssessmentDraftHandler(assessmentAdapter, briefAdapter, draftAdapter,
                logAdapter, ownershipVerifier, assessmentAgentClient, transactionManager);
        getCurrentDraftHandler = new GetCurrentDraftHandler(assessmentAdapter, draftAdapter, ownershipVerifier);
        listDraftVersionsHandler = new ListDraftVersionsHandler(assessmentAdapter, draftAdapter, ownershipVerifier);
        listAssessmentsHandler = new ListAssessmentsHandler(assessmentAdapter);

        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                TEACHER_UID, "Test", "Teacher", TEACHER_UID + "@test.com");
    }

    /**
     * Seeds "V1" directly as a pre-existing {@code AssessmentDraft} — simulating an assessment
     * generated before this cut — since {@code GenerateAssessmentDraftHandler} no longer
     * produces a draft row for {@code RegenerateAssessmentDraftHandler} to build on.
     */
    private GenerateAssessmentDraftResult seedV1Draft(UUID assessmentId) {
        AssessmentDraft draft = AssessmentDraft.generate(new AssessmentId(assessmentId), "V1", "Context V1",
                "Instructions V1", List.of("obj-V1"), List.of("del-V1"), List.of("con-V1"), null);
        draftAdapter.save(draft);
        entityManager.flush();
        entityManager.clear();
        return new GenerateAssessmentDraftResult(draft.getId(), draft.getTitle(), draft.getContext(),
                draft.getInstructions(), draft.getObjectives(), draft.getDeliverables(), draft.getConstraints(),
                draft.getVersionNumber());
    }

    private static AssessmentAgentResponse response(String title) {
        Instant startedAt = Instant.now().minusSeconds(2);
        Instant finishedAt = Instant.now();
        return new AssessmentAgentResponse(
                new AssessmentAgentResponse.Result(title, "Context " + title, "Instructions " + title,
                        List.of("obj-" + title), List.of("del-" + title), List.of("con-" + title)),
                new AssessmentAgentResponse.Log(UUID.randomUUID(), "assessment", "gemini", "gemini-2.0-flash", "v1",
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

        GenerateAssessmentDraftResult generated = seedV1Draft(assessmentId);
        assertThat(generated.versionNumber()).isEqualTo(1);
        assertThat(generated.title()).isEqualTo("V1");

        when(assessmentAgentClient.generate(any(), any())).thenReturn(response("V2"));
        GenerateAssessmentDraftResult regenerated = regenerateHandler.execute(
                new RegenerateAssessmentDraftCommand(assessmentId, TEACHER_UID, "make it harder"));
        entityManager.flush();
        entityManager.clear();
        assertThat(regenerated.versionNumber()).isEqualTo(2);
        assertThat(regenerated.title()).isEqualTo("V2");

        // Dashboard listing reflects the assessment with the latest (regenerated) title.
        List<AssessmentSummaryResult> summaries = listAssessmentsHandler.execute(TEACHER_UID);
        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).id()).isEqualTo(assessmentId.toString());
        assertThat(summaries.get(0).title()).isEqualTo("V2");

        // Retrieve current draft matches the regenerated state.
        GenerateAssessmentDraftResult current = getCurrentDraftHandler.execute(
                new GetCurrentDraftCommand(assessmentId, TEACHER_UID));
        assertThat(current.draftId()).isEqualTo(regenerated.draftId());
        assertThat(current.versionNumber()).isEqualTo(2);
        assertThat(current.title()).isEqualTo("V2");

        // Version history lists both versions, newest first.
        List<GenerateAssessmentDraftResult> versions = listDraftVersionsHandler.execute(
                new ListDraftVersionsCommand(assessmentId, TEACHER_UID));
        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).versionNumber()).isEqualTo(2);
        assertThat(versions.get(0).title()).isEqualTo("V2");
        assertThat(versions.get(1).versionNumber()).isEqualTo(1);
        assertThat(versions.get(1).title()).isEqualTo("V1");
        assertThat(versions.get(1).draftId()).isEqualTo(generated.draftId());
    }

    @Test
    void twoRegenerationsInARowProduceThreeDistinctRetrievableVersions() {
        UUID assessmentId = createBrief();

        GenerateAssessmentDraftResult v1 = seedV1Draft(assessmentId);

        when(assessmentAgentClient.generate(any(), any())).thenReturn(response("V2"));
        GenerateAssessmentDraftResult v2 = regenerateHandler.execute(
                new RegenerateAssessmentDraftCommand(assessmentId, TEACHER_UID, "harder"));
        entityManager.flush();
        entityManager.clear();

        when(assessmentAgentClient.generate(any(), any())).thenReturn(response("V3"));
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

        // Each of the two regenerate executions produced its own distinct AgentExecutionLog
        // (v1 was seeded directly, without going through an agent call).
        List<AgentExecutionLogJpaEntity> logs = logJpaRepository.findAll().stream()
                .filter(l -> l.getAssessmentId().equals(assessmentId))
                .toList();
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(AgentExecutionLogJpaEntity::getDraftId)
                .containsExactlyInAnyOrder(v2.draftId(), v3.draftId());
    }

}
