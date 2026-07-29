package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link RegenerateAssessmentDraftHandler} with real repositories against a live
 * Postgres (Flyway-migrated through V16): seeds "v1" directly as a pre-existing {@code
 * AssessmentDraft} (see {@link RegenerateAssessmentDraftHandler}'s javadoc — {@code
 * GenerateAssessmentDraftHandler} no longer produces one after Task 07B's pivot), then
 * regenerates to v2, and verifies v1's row is byte-for-byte unchanged, both versions are
 * retrievable, and each version has its own distinct {@code AgentExecutionLog}. Requires Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class RegenerateAssessmentDraftHandlerIntegrationTest {

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

    AssessmentPersistenceAdapter assessmentAdapter;
    AssessmentBriefPersistenceAdapter briefAdapter;
    AssessmentDraftPersistenceAdapter draftAdapter;
    AssessmentAgentClient assessmentAgentClient;
    RegenerateAssessmentDraftHandler regenerateHandler;

    Assessment assessment;

    @BeforeEach
    void setUp() {
        assessmentAdapter = new AssessmentPersistenceAdapter(assessmentJpaRepository, new AssessmentPersistenceMapper());
        briefAdapter = new AssessmentBriefPersistenceAdapter(briefJpaRepository, new AssessmentBriefPersistenceMapper());
        draftAdapter = new AssessmentDraftPersistenceAdapter(draftJpaRepository, new AssessmentDraftPersistenceMapper());
        AgentExecutionLogPersistenceAdapter logAdapter =
                new AgentExecutionLogPersistenceAdapter(logJpaRepository, new AgentExecutionLogPersistenceMapper());
        assessmentAgentClient = mock(AssessmentAgentClient.class);
        OwnershipVerifier ownershipVerifier = new OwnershipVerifier();

        regenerateHandler = new RegenerateAssessmentDraftHandler(assessmentAdapter, briefAdapter, draftAdapter,
                logAdapter, ownershipVerifier, assessmentAgentClient, transactionManager);

        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
        assessment = Assessment.create("uid-1");
        assessmentAdapter.save(assessment);
        briefAdapter.save(AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java"));
        entityManager.flush();
        entityManager.clear();
    }

    private static AssessmentAgentResponse response(String title) {
        Instant startedAt = Instant.now().minusSeconds(2);
        Instant finishedAt = Instant.now();
        return new AssessmentAgentResponse(
                new AssessmentAgentResponse.Result(title, "Context", "Instructions",
                        List.of("obj"), List.of("del"), List.of("con")),
                new AssessmentAgentResponse.Log(UUID.randomUUID(), "assessment", "gemini", "gemini-2.0-flash", "v1",
                        "in-hash-" + title, "out-hash-" + title, 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt));
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
    void shouldCreateV2WithoutAlteringV1AndEachVersionHasItsOwnLog() {
        GenerateAssessmentDraftResult v1Result = seedV1Draft();
        AssessmentDraftJpaEntity v1Before = draftJpaRepository.findById(v1Result.draftId()).orElseThrow();

        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(response("Title v2"));
        GenerateAssessmentDraftResult v2Result = regenerateHandler.execute(
                new RegenerateAssessmentDraftCommand(assessment.getId().value(), "uid-1", "make it harder"));

        entityManager.flush();
        entityManager.clear();

        // v1's row is byte-for-byte unchanged.
        AssessmentDraftJpaEntity v1After = draftJpaRepository.findById(v1Result.draftId()).orElseThrow();
        assertThat(v1After.getTitle()).isEqualTo(v1Before.getTitle());
        assertThat(v1After.getVersionNumber()).isEqualTo(v1Before.getVersionNumber());
        assertThat(v1After.getPreviousVersionId()).isEqualTo(v1Before.getPreviousVersionId());
        assertThat(v1After.getAgentExecutionLogId()).isEqualTo(v1Before.getAgentExecutionLogId());
        assertThat(v1After.getCreatedAt()).isEqualTo(v1Before.getCreatedAt());

        // v2 is a new, distinct row linked back to v1.
        assertThat(v2Result.versionNumber()).isEqualTo(2);
        assertThat(v2Result.title()).isEqualTo("Title v2");
        AssessmentDraftJpaEntity v2Entity = draftJpaRepository.findById(v2Result.draftId()).orElseThrow();
        assertThat(v2Entity.getPreviousVersionId()).isEqualTo(v1Result.draftId());

        // Both versions are retrievable.
        List<AssessmentDraftJpaEntity> allDrafts = draftJpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(
                assessment.getId().value());
        assertThat(allDrafts).hasSize(2);
        assertThat(allDrafts).extracting(AssessmentDraftJpaEntity::getVersionNumber).containsExactly(2, 1);

        // v2 has its own AgentExecutionLog (v1 was seeded directly, without an agent call).
        assertThat(v2Entity.getAgentExecutionLogId()).isNotNull();
        List<AgentExecutionLogJpaEntity> logs = logJpaRepository.findAll().stream()
                .filter(l -> l.getAssessmentId().equals(assessment.getId().value()))
                .toList();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getDraftId()).isEqualTo(v2Result.draftId());
    }
}
