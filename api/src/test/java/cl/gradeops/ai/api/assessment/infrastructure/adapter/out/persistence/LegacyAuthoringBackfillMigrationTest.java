package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task 12: proves the V17 legacy backfill migrates every {@code assessment_drafts}/
 * {@code agent_execution_logs} row into {@code assessment_revisions}/{@code ai_operations}/
 * {@code agent_attempts} honestly (LEGACY_UNKNOWN, provenanceComplete=false, actorId=null,
 * unconditionally) with zero fabricated fields, and that the script is safe to execute twice.
 * Fixtures are seeded via raw SQL matching V11/V12's real legacy schema, never via the legacy
 * Java domain classes Task 13 removes. Requires Docker.
 *
 * <p>Context startup applies every migration, V13-V17 included, in the normal order — V17 runs
 * once here against empty legacy tables, a harmless no-op (exactly what a real deploy against a
 * legacy-free database would also do). Each test then seeds its own legacy fixtures and drives
 * V17's actual backfill logic itself via {@link #applyBackfill()}, which re-executes V17's own
 * SQL file directly on the test's own {@link JdbcTemplate} connection — this deliberately
 * bypasses both Flyway's history bookkeeping (already "applied") and the cross-connection
 * visibility problem a separate Flyway-managed connection would have with this test's
 * transactionally-scoped fixture rows. This also happens to be exactly the kind of raw,
 * bookkeeping-independent re-run the idempotency requirement (safe to execute twice) needs
 * proving against.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class LegacyAuthoringBackfillMigrationTest {

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

    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                "uid-1", "Test", "Teacher", "uid-1@test.com");
    }

    /** Re-executes V17's own SQL file directly, on this test's own connection/transaction. */
    void applyBackfill() {
        String sql;
        try {
            sql = new ClassPathResource("db/migration/V17__backfill_legacy_authoring_data.sql")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        // Strip "--" line comments first - a comment may itself contain a literal ";", which
        // would otherwise be mistaken for a statement terminator by the naive split below.
        StringBuilder withoutComments = new StringBuilder();
        for (String line : sql.split("\n")) {
            int commentStart = line.indexOf("--");
            withoutComments.append(commentStart >= 0 ? line.substring(0, commentStart) : line).append('\n');
        }
        for (String statement : withoutComments.toString().split(";")) {
            String trimmed = statement.strip();
            if (!trimmed.isEmpty()) {
                jdbcTemplate.execute(trimmed);
            }
        }
    }

    UUID insertAssessment(String teacherUid) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO assessments (id, teacher_uid) VALUES (?, ?)", id, teacherUid);
        return id;
    }

    UUID insertLegacyLog(UUID assessmentId, UUID draftId, String status, String errorCode,
                          Instant started, Instant finished) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO agent_execution_logs (id, assessment_id, draft_id, agent_execution_id, agent_name, " +
                "provider, model, prompt_version, input_hash, output_hash, estimated_input_tokens, " +
                "estimated_output_tokens, cost_estimate, status, error_code, started_at, finished_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, assessmentId, draftId, UUID.randomUUID(), "assessment-agent",
                "gemini", "gemini-2.0-flash", "v1", "hash-in", "hash-out", 100, 200,
                0.05, status, errorCode, Timestamp.from(started), Timestamp.from(finished));
        return id;
    }

    UUID insertLegacyDraft(UUID assessmentId, int versionNumber, UUID previousVersionId, UUID agentExecutionLogId) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO assessment_drafts (id, assessment_id, version_number, previous_version_id, title, " +
                "context, instructions, objectives, deliverables, constraints, agent_execution_log_id, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?)",
                id, assessmentId, versionNumber, previousVersionId, "title-v" + versionNumber,
                "context-v" + versionNumber, "instructions-v" + versionNumber,
                "[\"obj\"]", "[\"del\"]", "[\"con\"]", agentExecutionLogId,
                Timestamp.from(Instant.now().minusSeconds(3600L * (4 - versionNumber))));
        return id;
    }

    void linkLogToDraft(UUID logId, UUID draftId) {
        jdbcTemplate.update("UPDATE agent_execution_logs SET draft_id = ? WHERE id = ?", draftId, logId);
    }

    UUID agentAttemptOperationId(UUID attemptId) {
        return jdbcTemplate.queryForObject(
                "SELECT ai_operation_id FROM agent_attempts WHERE id = ?", UUID.class, attemptId);
    }

    // Case 1: a single-version assessment with one completed log.
    @Test
    void shouldMigrateASingleVersionAssessmentWithACompletedLog() {
        UUID assessmentId = insertAssessment("uid-1");
        Instant started = Instant.now().minusSeconds(120);
        Instant finished = Instant.now().minusSeconds(60);
        UUID logId = insertLegacyLog(assessmentId, null, "COMPLETED", null, started, finished);
        UUID draftId = insertLegacyDraft(assessmentId, 1, null, logId);
        linkLogToDraft(logId, draftId);

        applyBackfill();

        var revision = jdbcTemplate.queryForMap(
                "SELECT * FROM assessment_revisions WHERE id = ?", draftId);
        assertThat(revision.get("origin")).isEqualTo("LEGACY_UNKNOWN");
        assertThat(revision.get("provenance_complete")).isEqualTo(false);
        assertThat(revision.get("actor_id")).isNull();
        assertThat(revision.get("reason")).isNull();
        assertThat(revision.get("version_number")).isEqualTo(1);
        assertThat(revision.get("previous_revision_id")).isNull();
        assertThat(revision.get("source_agent_attempt_id")).isEqualTo(logId);
        assertThat(revision.get("title")).isEqualTo("title-v1");

        var attempt = jdbcTemplate.queryForMap("SELECT * FROM agent_attempts WHERE id = ?", logId);
        assertThat(attempt.get("attempt_number")).isEqualTo(1);
        assertThat(attempt.get("status")).isEqualTo("COMPLETED");
        assertThat(attempt.get("resolved_provider")).isEqualTo("gemini");
        assertThat(attempt.get("resolved_model")).isEqualTo("gemini-2.0-flash");
        assertThat(((Timestamp) attempt.get("dispatched_at")).toInstant().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
                .isEqualTo(started.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        assertThat(((Timestamp) attempt.get("completed_at")).toInstant().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
                .isEqualTo(finished.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));

        UUID operationId = agentAttemptOperationId(logId);
        var operation = jdbcTemplate.queryForMap("SELECT * FROM ai_operations WHERE id = ?", operationId);
        assertThat(operation.get("operation_type")).isEqualTo("CREATE_INITIAL_REVISION");
        assertThat(operation.get("status")).isEqualTo("SUCCEEDED");
        assertThat(operation.get("requested_by")).isEqualTo("uid-1");
        assertThat(operation.get("result_revision_id")).isEqualTo(draftId);
        assertThat(operation.get("expected_revision_id")).isNull();

        UUID currentRevisionId = jdbcTemplate.queryForObject(
                "SELECT current_revision_id FROM assessments WHERE id = ?", UUID.class, assessmentId);
        assertThat(currentRevisionId).isEqualTo(draftId);
    }

    // Case 2: a multi-version, regenerated assessment — the version chain must be preserved.
    @Test
    void shouldMigrateAMultiVersionRegeneratedAssessmentPreservingTheVersionChain() {
        UUID assessmentId = insertAssessment("uid-1");
        Instant t0 = Instant.now().minusSeconds(600);

        UUID log1 = insertLegacyLog(assessmentId, null, "COMPLETED", null, t0, t0.plusSeconds(30));
        UUID draft1 = insertLegacyDraft(assessmentId, 1, null, log1);
        linkLogToDraft(log1, draft1);

        UUID log2 = insertLegacyLog(assessmentId, null, "COMPLETED", null, t0.plusSeconds(120), t0.plusSeconds(150));
        UUID draft2 = insertLegacyDraft(assessmentId, 2, draft1, log2);
        linkLogToDraft(log2, draft2);

        UUID log3 = insertLegacyLog(assessmentId, null, "COMPLETED", null, t0.plusSeconds(240), t0.plusSeconds(270));
        UUID draft3 = insertLegacyDraft(assessmentId, 3, draft2, log3);
        linkLogToDraft(log3, draft3);

        applyBackfill();

        var r1 = jdbcTemplate.queryForMap("SELECT * FROM assessment_revisions WHERE id = ?", draft1);
        var r2 = jdbcTemplate.queryForMap("SELECT * FROM assessment_revisions WHERE id = ?", draft2);
        var r3 = jdbcTemplate.queryForMap("SELECT * FROM assessment_revisions WHERE id = ?", draft3);
        assertThat(r1.get("previous_revision_id")).isNull();
        assertThat(r2.get("previous_revision_id")).isEqualTo(draft1);
        assertThat(r3.get("previous_revision_id")).isEqualTo(draft2);
        for (var r : List.of(r1, r2, r3)) {
            assertThat(r.get("origin")).isEqualTo("LEGACY_UNKNOWN");
            assertThat(r.get("provenance_complete")).isEqualTo(false);
            assertThat(r.get("actor_id")).isNull();
        }

        UUID op1 = agentAttemptOperationId(log1);
        UUID op2 = agentAttemptOperationId(log2);
        UUID op3 = agentAttemptOperationId(log3);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT operation_type FROM ai_operations WHERE id = ?", String.class, op1))
                .isEqualTo("CREATE_INITIAL_REVISION");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT operation_type FROM ai_operations WHERE id = ?", String.class, op2))
                .isEqualTo("REGENERATE_REVISION");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT operation_type FROM ai_operations WHERE id = ?", String.class, op3))
                .isEqualTo("REGENERATE_REVISION");

        UUID currentRevisionId = jdbcTemplate.queryForObject(
                "SELECT current_revision_id FROM assessments WHERE id = ?", UUID.class, assessmentId);
        assertThat(currentRevisionId).isEqualTo(draft3);
    }

    // Case 3: a draft that might have been edited in place (current version, no edit-timestamp
    // column to prove otherwise) must still migrate as LEGACY_UNKNOWN, never AI_GENERATED.
    @Test
    void shouldNeverLabelAPossiblyEditedLegacyDraftAsAiGeneratedOrHumanEdited() {
        UUID assessmentId = insertAssessment("uid-1");
        Instant started = Instant.now().minusSeconds(120);
        UUID logId = insertLegacyLog(assessmentId, null, "COMPLETED", null, started, started.plusSeconds(30));
        UUID draftId = insertLegacyDraft(assessmentId, 1, null, logId);
        linkLogToDraft(logId, draftId);

        applyBackfill();

        String origin = jdbcTemplate.queryForObject(
                "SELECT origin FROM assessment_revisions WHERE id = ?", String.class, draftId);
        assertThat(origin).isNotEqualTo("AI_GENERATED");
        assertThat(origin).isNotEqualTo("HUMAN_EDITED");
        assertThat(origin).isEqualTo("LEGACY_UNKNOWN");
    }

    // Case 4: a failed generation that never produced a draft.
    @Test
    void shouldMigrateAFailedGenerationLogThatNeverProducedADraftAsFailedTerminal() {
        UUID assessmentId = insertAssessment("uid-1");
        Instant started = Instant.now().minusSeconds(60);
        UUID logId = insertLegacyLog(assessmentId, null, "FAILED", "AGENT_UNAVAILABLE", started, started.plusSeconds(10));

        applyBackfill();

        var attempt = jdbcTemplate.queryForMap("SELECT * FROM agent_attempts WHERE id = ?", logId);
        assertThat(attempt.get("status")).isEqualTo("FAILED");
        assertThat(attempt.get("failure_code")).isEqualTo("AGENT_UNAVAILABLE");

        UUID operationId = agentAttemptOperationId(logId);
        var operation = jdbcTemplate.queryForMap("SELECT * FROM ai_operations WHERE id = ?", operationId);
        assertThat(operation.get("status")).isEqualTo("FAILED_TERMINAL");
        assertThat(operation.get("operation_type")).isEqualTo("CREATE_INITIAL_REVISION");
        assertThat(operation.get("result_revision_id")).isNull();

        Integer revisionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM assessment_revisions WHERE assessment_id = ?", Integer.class, assessmentId);
        assertThat(revisionCount).isZero();

        UUID currentRevisionId = jdbcTemplate.queryForObject(
                "SELECT current_revision_id FROM assessments WHERE id = ?", UUID.class, assessmentId);
        assertThat(currentRevisionId).isNull();
    }

    // Case 5: a draft with no associated log.
    @Test
    void shouldMigrateADraftWithNoAssociatedLogLeavingSourceAgentAttemptIdNull() {
        UUID assessmentId = insertAssessment("uid-1");
        UUID draftId = insertLegacyDraft(assessmentId, 1, null, null);

        applyBackfill();

        var revision = jdbcTemplate.queryForMap("SELECT * FROM assessment_revisions WHERE id = ?", draftId);
        assertThat(revision.get("source_agent_attempt_id")).isNull();
        assertThat(revision.get("origin")).isEqualTo("LEGACY_UNKNOWN");
        assertThat(revision.get("provenance_complete")).isEqualTo(false);

        Integer operationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ai_operations WHERE assessment_id = ?", Integer.class, assessmentId);
        assertThat(operationCount).isZero();

        UUID currentRevisionId = jdbcTemplate.queryForObject(
                "SELECT current_revision_id FROM assessments WHERE id = ?", UUID.class, assessmentId);
        assertThat(currentRevisionId).isEqualTo(draftId);
    }

    // Case 6: an orphaned log — claims COMPLETED but no assessment_drafts row actually references
    // it (the cross-reference was never made, or never existed). The conservative "did this log
    // actually produce a persisted draft" check — not the log's own status column alone — must
    // govern the outcome: FAILED_TERMINAL, not SUCCEEDED.
    @Test
    void shouldTreatAnOrphanedCompletedLogWithNoReferencingDraftAsFailedTerminal() {
        UUID assessmentId = insertAssessment("uid-1");
        Instant started = Instant.now().minusSeconds(60);
        UUID logId = insertLegacyLog(assessmentId, null, "COMPLETED", null, started, started.plusSeconds(10));
        // No assessment_drafts row references this log at all.

        applyBackfill();

        UUID operationId = agentAttemptOperationId(logId);
        var operation = jdbcTemplate.queryForMap("SELECT * FROM ai_operations WHERE id = ?", operationId);
        assertThat(operation.get("status")).isEqualTo("FAILED_TERMINAL");
        assertThat(operation.get("result_revision_id")).isNull();

        String attemptStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM agent_attempts WHERE id = ?", String.class, logId);
        assertThat(attemptStatus).isEqualTo("FAILED");
    }

    // Discovery: assessment_drafts.agent_execution_log_id (set once, at draft-creation time) is
    // the authoritative "did this log produce a draft" signal - not agent_execution_logs.draft_id
    // (a second, later cross-reference write, AgentExecutionLog.withDraftId, that could be
    // missing even for a log that genuinely did produce a draft, e.g. a crash between the two
    // writes). This must still migrate as SUCCEEDED even though the log's own draft_id was never
    // cross-referenced back.
    @Test
    void shouldDetectAProducedDraftViaTheDraftsOwnLogReferenceEvenWhenTheLogWasNeverCrossReferencedBack() {
        UUID assessmentId = insertAssessment("uid-1");
        Instant started = Instant.now().minusSeconds(120);
        UUID logId = insertLegacyLog(assessmentId, null, "COMPLETED", null, started, started.plusSeconds(30));
        UUID draftId = insertLegacyDraft(assessmentId, 1, null, logId);
        // Deliberately no linkLogToDraft(logId, draftId) call: agent_execution_logs.draft_id
        // stays NULL, simulating the withDraftId cross-reference never having completed.

        applyBackfill();

        UUID operationId = agentAttemptOperationId(logId);
        String operationStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM ai_operations WHERE id = ?", String.class, operationId);
        String attemptStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM agent_attempts WHERE id = ?", String.class, logId);
        assertThat(operationStatus).isEqualTo("SUCCEEDED");
        assertThat(attemptStatus).isEqualTo("COMPLETED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT result_revision_id FROM ai_operations WHERE id = ?", UUID.class, operationId))
                .isEqualTo(draftId);
    }

    // Case 7: an assessment with no legacy data at all must be left untouched.
    @Test
    void shouldLeaveAnAssessmentWithNoLegacyDataUntouched() {
        UUID assessmentId = insertAssessment("uid-1");

        applyBackfill();

        Integer revisionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM assessment_revisions WHERE assessment_id = ?", Integer.class, assessmentId);
        Integer operationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ai_operations WHERE assessment_id = ?", Integer.class, assessmentId);
        assertThat(revisionCount).isZero();
        assertThat(operationCount).isZero();

        UUID currentRevisionId = jdbcTemplate.queryForObject(
                "SELECT current_revision_id FROM assessments WHERE id = ?", UUID.class, assessmentId);
        assertThat(currentRevisionId).isNull();
    }

    // Case 8: the script must be safe to execute a second time, independent of Flyway's own
    // history bookkeeping (re-executes the raw SQL file directly, not via Flyway.migrate()).
    @Test
    void shouldProduceNoAdditionalOrChangedRowsWhenTheBackfillSqlIsExecutedASecondTimeDirectly() {
        UUID assessmentId = insertAssessment("uid-1");
        Instant t0 = Instant.now().minusSeconds(600);
        UUID log1 = insertLegacyLog(assessmentId, null, "COMPLETED", null, t0, t0.plusSeconds(30));
        UUID draft1 = insertLegacyDraft(assessmentId, 1, null, log1);
        linkLogToDraft(log1, draft1);
        UUID log2 = insertLegacyLog(assessmentId, null, "COMPLETED", null, t0.plusSeconds(120), t0.plusSeconds(150));
        UUID draft2 = insertLegacyDraft(assessmentId, 2, draft1, log2);
        linkLogToDraft(log2, draft2);

        applyBackfill();

        Integer revisionCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM assessment_revisions WHERE assessment_id = ?", Integer.class, assessmentId);
        Integer operationCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ai_operations WHERE assessment_id = ?", Integer.class, assessmentId);
        Integer attemptCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM agent_attempts WHERE ai_operation_id IN " +
                "(SELECT id FROM ai_operations WHERE assessment_id = ?)", Integer.class, assessmentId);
        UUID currentRevisionIdBefore = jdbcTemplate.queryForObject(
                "SELECT current_revision_id FROM assessments WHERE id = ?", UUID.class, assessmentId);
        var revisionBefore = jdbcTemplate.queryForMap(
                "SELECT origin, provenance_complete FROM assessment_revisions WHERE id = ?", draft1);

        applyBackfill();

        Integer revisionCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM assessment_revisions WHERE assessment_id = ?", Integer.class, assessmentId);
        Integer operationCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ai_operations WHERE assessment_id = ?", Integer.class, assessmentId);
        Integer attemptCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM agent_attempts WHERE ai_operation_id IN " +
                "(SELECT id FROM ai_operations WHERE assessment_id = ?)", Integer.class, assessmentId);
        UUID currentRevisionIdAfter = jdbcTemplate.queryForObject(
                "SELECT current_revision_id FROM assessments WHERE id = ?", UUID.class, assessmentId);
        var revisionAfter = jdbcTemplate.queryForMap(
                "SELECT origin, provenance_complete FROM assessment_revisions WHERE id = ?", draft1);

        assertThat(revisionCountAfter).isEqualTo(revisionCountBefore);
        assertThat(operationCountAfter).isEqualTo(operationCountBefore);
        assertThat(attemptCountAfter).isEqualTo(attemptCountBefore);
        assertThat(currentRevisionIdAfter).isEqualTo(currentRevisionIdBefore);
        assertThat(revisionAfter).isEqualTo(revisionBefore);
    }

    // The provenance_complete column itself: exists, NOT NULL, defaults TRUE for
    // normally-created revisions, explicitly FALSE for every legacy-migrated one.
    @Test
    void shouldAddProvenanceCompleteColumnDefaultingTrueForNonLegacyRevisionsAndFalseForMigratedOnes() {
        UUID normalAssessmentId = insertAssessment("uid-1");
        UUID normalRevisionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO assessment_revisions (id, assessment_id, version_number, origin, actor_id, title, " +
                "context, instructions, objectives, deliverables, constraints) VALUES " +
                "(?, ?, 1, 'AI_GENERATED', 'uid-1', 'title', 'context', 'instructions', '[]'::jsonb, '[]'::jsonb, '[]'::jsonb)",
                normalRevisionId, normalAssessmentId);

        UUID legacyAssessmentId = insertAssessment("uid-1");
        Instant started = Instant.now().minusSeconds(60);
        UUID logId = insertLegacyLog(legacyAssessmentId, null, "COMPLETED", null, started, started.plusSeconds(10));
        UUID legacyDraftId = insertLegacyDraft(legacyAssessmentId, 1, null, logId);
        linkLogToDraft(logId, legacyDraftId);

        applyBackfill();

        Boolean columnIsNotNull = jdbcTemplate.queryForObject(
                "SELECT is_nullable = 'NO' FROM information_schema.columns " +
                "WHERE table_name = 'assessment_revisions' AND column_name = 'provenance_complete'",
                Boolean.class);
        assertThat(columnIsNotNull).isTrue();

        Boolean normalDefault = jdbcTemplate.queryForObject(
                "SELECT provenance_complete FROM assessment_revisions WHERE id = ?", Boolean.class, normalRevisionId);
        Boolean legacyValue = jdbcTemplate.queryForObject(
                "SELECT provenance_complete FROM assessment_revisions WHERE id = ?", Boolean.class, legacyDraftId);
        assertThat(normalDefault).isTrue();
        assertThat(legacyValue).isFalse();
    }
}
