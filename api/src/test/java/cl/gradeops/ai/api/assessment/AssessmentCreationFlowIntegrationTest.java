package cl.gradeops.ai.api.assessment;

import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.GetCurrentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.ListDraftVersionsCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.application.usecase.AiOperationCoordinator;
import cl.gradeops.ai.api.assessment.application.usecase.CreateAssessmentBriefHandler;
import cl.gradeops.ai.api.assessment.application.usecase.GenerateAssessmentDraftHandler;
import cl.gradeops.ai.api.assessment.application.usecase.GetCurrentDraftHandler;
import cl.gradeops.ai.api.assessment.application.usecase.ListAssessmentsHandler;
import cl.gradeops.ai.api.assessment.application.usecase.ListDraftVersionsHandler;
import cl.gradeops.ai.api.assessment.application.usecase.RegenerateAssessmentDraftHandler;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AiOperationJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AiOperationPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AiOperationPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionJpaEntity;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionPersistenceMapper;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence.IdempotencyRecordJpaRepository;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence.IdempotencyRecordPersistenceAdapter;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence.IdempotencyRecordPersistenceMapper;
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
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Cross-cutting, whole-story integration coverage for the assessment-creation flow, against a
 * live Postgres (Flyway-migrated through V16): brief → generate → regenerate → regenerate again,
 * asserting consistency at every step. Complements — does not duplicate — this packet's own
 * per-handler focused tests: those exercise one handler in isolation, this class chains several
 * together the way a real teacher session would.
 *
 * <p>Session A3 (Task 09) rewrote this file onto the durable {@code AssessmentRevision} model —
 * both {@link GenerateAssessmentDraftHandler} (Session A2) and {@link
 * RegenerateAssessmentDraftHandler} (this session) now produce {@code AssessmentRevision} rows,
 * so there is no more legacy-{@code AssessmentDraft}-seeding step. Session A3 (Task 10) migrated
 * {@link ListAssessmentsHandler}/{@link GetCurrentDraftHandler}/{@link ListDraftVersionsHandler}
 * off the legacy {@code AssessmentDraft} table onto {@code AssessmentRevision}/{@code
 * Assessment.currentRevisionId} — this file now exercises all three against the same
 * revision-based assessment the write path produces, closing the gap Task 09 left open.
 *
 * <p>Session A3 (Task 08) removed {@code UpdateAssessmentDraftHandler} — human edits now create
 * an immutable {@code AssessmentRevision} via {@code CreateHumanRevisionHandler} instead (see
 * {@code CreateHumanRevisionHandlerIntegrationTest}). This file's own former "edit preserves
 * id/version" coverage was deleted outright, not adapted — TEST-PLAN.md's regression guard #2.
 *
 * <p>Only {@link AssessmentAgentClient} is stubbed; every persistence adapter and handler
 * exercised here runs for real. Requires Docker.
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
    @Autowired AssessmentRevisionJpaRepository revisionJpaRepository;
    @Autowired AiOperationJpaRepository aiOperationJpaRepository;
    @Autowired AgentAttemptJpaRepository agentAttemptJpaRepository;
    @Autowired IdempotencyRecordJpaRepository idempotencyRecordJpaRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;

    AssessmentAgentClient assessmentAgentClient;
    AssessmentBriefPersistenceAdapter briefAdapter;
    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentRevisionPersistenceAdapter revisionAdapter;

    CreateAssessmentBriefHandler createBriefHandler;
    GenerateAssessmentDraftHandler generateHandler;
    RegenerateAssessmentDraftHandler regenerateHandler;
    GetCurrentDraftHandler getCurrentDraftHandler;
    ListDraftVersionsHandler listDraftVersionsHandler;
    ListAssessmentsHandler listAssessmentsHandler;

    static final String TEACHER_UID = "uid-1";

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefJpaRepository, new AssessmentBriefPersistenceMapper());
        revisionAdapter = new AssessmentRevisionPersistenceAdapter(revisionJpaRepository, new AssessmentRevisionPersistenceMapper());
        AiOperationPersistenceAdapter aiOperationAdapter =
                new AiOperationPersistenceAdapter(aiOperationJpaRepository, new AiOperationPersistenceMapper());
        AgentAttemptPersistenceAdapter agentAttemptAdapter =
                new AgentAttemptPersistenceAdapter(agentAttemptJpaRepository, new AgentAttemptPersistenceMapper());
        IdempotencyRecordPersistenceAdapter idempotencyAdapter =
                new IdempotencyRecordPersistenceAdapter(idempotencyRecordJpaRepository, new IdempotencyRecordPersistenceMapper());
        IdempotencyGuard idempotencyGuard = new IdempotencyGuard(idempotencyAdapter);
        assessmentAgentClient = mock(AssessmentAgentClient.class);
        JsonMapper jsonMapper = JsonMapper.builder().build();
        OwnershipVerifier ownershipVerifier = new OwnershipVerifier();

        AiOperationCoordinator coordinator = new AiOperationCoordinator(assessmentAdapter, aiOperationAdapter,
                agentAttemptAdapter, revisionAdapter, assessmentAgentClient, jsonMapper, transactionManager);

        createBriefHandler = new CreateAssessmentBriefHandler(assessmentAdapter, briefAdapter, idempotencyGuard, transactionManager);
        generateHandler = new GenerateAssessmentDraftHandler(assessmentAdapter, briefAdapter, revisionAdapter,
                ownershipVerifier, idempotencyGuard, coordinator);
        regenerateHandler = new RegenerateAssessmentDraftHandler(assessmentAdapter, briefAdapter, revisionAdapter,
                ownershipVerifier, idempotencyGuard, coordinator);
        getCurrentDraftHandler = new GetCurrentDraftHandler(assessmentAdapter, revisionAdapter, ownershipVerifier);
        listDraftVersionsHandler = new ListDraftVersionsHandler(assessmentAdapter, revisionAdapter, ownershipVerifier);
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
                new AssessmentAgentResponse.Log(UUID.randomUUID(), "assessment", "gemini", "gemini-2.0-flash", "v1",
                        "in-hash-" + title, "out-hash-" + title, 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt));
    }

    private UUID createBrief() {
        CreateAssessmentBriefResult result = createBriefHandler.execute(new CreateAssessmentBriefCommand(
                TEACHER_UID, "Evaluate loops", "Java loops", "basic", "90min", "Java", "brief-key"));
        entityManager.flush();
        entityManager.clear();
        return UUID.fromString(result.assessmentId());
    }

    @Test
    void fullHappyPathKeepsEveryStepConsistentWithThePrevious() {
        UUID assessmentId = createBrief();

        // The brief is retrievable before any agent call — persist-before-agent-call ordering.
        AssessmentBrief persistedBrief = briefAdapter.findByAssessmentId(new cl.gradeops.ai.api.assessment.domain.model.AssessmentId(assessmentId)).orElseThrow();
        assertThat(persistedBrief.getTopic()).isEqualTo("Java loops");

        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(response("V1"));
        GenerateAssessmentDraftResult generated = generateHandler.execute(
                new GenerateAssessmentDraftCommand(assessmentId, TEACHER_UID, "gen-key"));
        entityManager.flush();
        entityManager.clear();
        assertThat(generated.versionNumber()).isEqualTo(1);
        assertThat(generated.title()).isEqualTo("V1");
        assertThat(generated.origin()).isEqualTo("AI_GENERATED");

        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(response("V2"));
        GenerateAssessmentDraftResult regenerated = regenerateHandler.execute(new RegenerateAssessmentDraftCommand(
                assessmentId, TEACHER_UID, "make it harder", generated.draftId(), "regen-key"));
        entityManager.flush();
        entityManager.clear();
        assertThat(regenerated.versionNumber()).isEqualTo(2);
        assertThat(regenerated.title()).isEqualTo("V2");
        assertThat(regenerated.previousRevisionId()).isEqualTo(generated.draftId());

        // currentRevisionId points at the regenerated version.
        Assessment reloaded = assessmentAdapter.findById(new cl.gradeops.ai.api.assessment.domain.model.AssessmentId(assessmentId)).orElseThrow();
        assertThat(reloaded.getCurrentRevisionId()).isEqualTo(regenerated.draftId());

        // v1 is untouched.
        AssessmentRevisionJpaEntity v1Entity = revisionJpaRepository.findById(generated.draftId()).orElseThrow();
        assertThat(v1Entity.getTitle()).isEqualTo("V1");
        assertThat(v1Entity.getVersionNumber()).isEqualTo(1);

        // GetCurrentDraftHandler reads through Assessment.currentRevisionId, not MAX(version_number).
        GenerateAssessmentDraftResult currentDraft = getCurrentDraftHandler.execute(
                new GetCurrentDraftCommand(assessmentId, TEACHER_UID));
        assertThat(currentDraft.draftId()).isEqualTo(regenerated.draftId());
        assertThat(currentDraft.title()).isEqualTo("V2");
        assertThat(currentDraft.versionNumber()).isEqualTo(2);

        // ListDraftVersionsHandler returns both revisions, newest first.
        List<GenerateAssessmentDraftResult> versions = listDraftVersionsHandler.execute(
                new ListDraftVersionsCommand(assessmentId, TEACHER_UID));
        assertThat(versions).extracting(GenerateAssessmentDraftResult::versionNumber).containsExactly(2, 1);
        assertThat(versions).extracting(GenerateAssessmentDraftResult::title).containsExactly("V2", "V1");

        // ListAssessmentsHandler's dashboard summary title tracks the current revision, not the brief topic.
        List<AssessmentSummaryResult> summaries = listAssessmentsHandler.execute(TEACHER_UID);
        assertThat(summaries).extracting(AssessmentSummaryResult::id).contains(assessmentId.toString());
        AssessmentSummaryResult summary = summaries.stream()
                .filter(s -> s.id().equals(assessmentId.toString())).findFirst().orElseThrow();
        assertThat(summary.title()).isEqualTo("V2");
    }

    @Test
    void twoRegenerationsInARowProduceThreeDistinctRetrievableVersions() {
        UUID assessmentId = createBrief();

        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(response("V1"));
        GenerateAssessmentDraftResult v1 = generateHandler.execute(
                new GenerateAssessmentDraftCommand(assessmentId, TEACHER_UID, "gen-key"));
        entityManager.flush();
        entityManager.clear();

        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(response("V2"));
        GenerateAssessmentDraftResult v2 = regenerateHandler.execute(new RegenerateAssessmentDraftCommand(
                assessmentId, TEACHER_UID, "harder", v1.draftId(), "regen-key-1"));
        entityManager.flush();
        entityManager.clear();

        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(response("V3"));
        GenerateAssessmentDraftResult v3 = regenerateHandler.execute(new RegenerateAssessmentDraftCommand(
                assessmentId, TEACHER_UID, "harder still", v2.draftId(), "regen-key-2"));
        entityManager.flush();
        entityManager.clear();

        assertThat(v1.versionNumber()).isEqualTo(1);
        assertThat(v2.versionNumber()).isEqualTo(2);
        assertThat(v3.versionNumber()).isEqualTo(3);
        assertThat(List.of(v1.draftId(), v2.draftId(), v3.draftId())).doesNotHaveDuplicates();

        List<cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision> versions =
                revisionAdapter.findAllByAssessmentId(new cl.gradeops.ai.api.assessment.domain.model.AssessmentId(assessmentId));
        assertThat(versions).hasSize(3);
        assertThat(versions).extracting(cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision::getVersionNumber)
                .containsExactly(3, 2, 1);
        assertThat(versions).extracting(cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision::getTitle)
                .containsExactly("V3", "V2", "V1");

        // None of the earlier versions were overwritten.
        AssessmentRevisionJpaEntity v1Entity = revisionJpaRepository.findById(v1.draftId()).orElseThrow();
        AssessmentRevisionJpaEntity v2Entity = revisionJpaRepository.findById(v2.draftId()).orElseThrow();
        assertThat(v1Entity.getTitle()).isEqualTo("V1");
        assertThat(v2Entity.getTitle()).isEqualTo("V2");
        assertThat(v2Entity.getPreviousRevisionId()).isEqualTo(v1.draftId());
        AssessmentRevisionJpaEntity v3Entity = revisionJpaRepository.findById(v3.draftId()).orElseThrow();
        assertThat(v3Entity.getPreviousRevisionId()).isEqualTo(v2.draftId());

        // Each of the three revisions has its own distinct source AgentAttempt.
        assertThat(v1Entity.getSourceAgentAttemptId()).isNotNull();
        assertThat(v2Entity.getSourceAgentAttemptId()).isNotNull();
        assertThat(v3Entity.getSourceAgentAttemptId()).isNotNull();
        assertThat(List.of(v1Entity.getSourceAgentAttemptId(), v2Entity.getSourceAgentAttemptId(),
                v3Entity.getSourceAgentAttemptId())).doesNotHaveDuplicates();
    }
}
