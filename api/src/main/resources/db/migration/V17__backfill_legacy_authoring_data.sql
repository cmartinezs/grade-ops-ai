-- Task 12 (API A4): one-time, idempotent-guarded backfill of the legacy assessment_drafts /
-- agent_execution_logs tables into the assessment_revisions / ai_operations / agent_attempts
-- model introduced by V13-V16. Every migrated row is labeled honestly and conservatively per
-- docs/implementation-plans/assessment-authoring-operation-foundation/06-database-migration.md:
-- no assessment_drafts row can be labeled AI_GENERATED with confidence (no edit-timestamp
-- column exists to rule out an in-place PATCH), so every migrated revision is LEGACY_UNKNOWN,
-- provenance_complete = false, actor_id = NULL, reason = NULL, unconditionally. Every migrated
-- agent_execution_logs row becomes exactly one ai_operations + one agent_attempts row (no
-- multi-attempt retry chain is ever inferred). assessment_drafts / agent_execution_logs are
-- read-only from here on but are NOT dropped in this cut (kept one release for audit).
--
-- Migration-integrity correction (API A4, post-handoff): assessment_drafts.previous_version_id
-- only guarantees "references some assessment_drafts row" - the legacy schema never enforced
-- that it points at the same assessment, nor at exactly version_number - 1 (Research 01 §6.2).
-- previous_revision_id and ai_operations.expected_revision_id are therefore never copied from
-- that legacy column - they are reconstructed structurally from (assessment_id, version_number),
-- and the whole script fails atomically (no partial rows in any table) if a version chain has a
-- gap that cannot be normalized this way. See the gap-validation and expected_revision_id steps
-- below, and API-A4-HANDOFF.md's "Revision-chain normalization" section.
--
-- Every statement below is guarded (WHERE NOT EXISTS / IS NULL) so this script is safe to
-- execute a second time by hand, independent of Flyway's own history bookkeeping.

-- 1. Add the audit-only provenance_complete column. Existing (non-legacy) revisions default to
--    TRUE; every row this script inserts below explicitly overrides it to FALSE. Not exposed on
--    any HTTP contract in this cut (see API-A4-HANDOFF.md).
ALTER TABLE assessment_revisions
    ADD COLUMN IF NOT EXISTS provenance_complete BOOLEAN NOT NULL DEFAULT TRUE;

-- 2. Validate every legacy version chain is structurally complete before any data is migrated: a
--    draft with version_number > 1 must have a same-assessment predecessor at version_number - 1,
--    or previous_revision_id/expected_revision_id cannot be honestly reconstructed for it (see
--    the note at the top of this file - assessment_drafts.previous_version_id is not authoritative
--    for this). Flyway runs each migration transactionally against PostgreSQL, so raising here
--    aborts the whole script atomically - no ai_operations/agent_attempts/assessment_revisions
--    rows are left behind for this or any other assessment.
DO $$
DECLARE
    gap RECORD;
BEGIN
    SELECT current_draft.assessment_id, current_draft.version_number
    INTO gap
    FROM assessment_drafts current_draft
    WHERE current_draft.version_number > 1
      AND NOT EXISTS (
          SELECT 1
          FROM assessment_drafts predecessor
          WHERE predecessor.assessment_id = current_draft.assessment_id
            AND predecessor.version_number = current_draft.version_number - 1
      )
    LIMIT 1;

    IF FOUND THEN
        RAISE EXCEPTION
            'Legacy assessment_drafts contains a non-normalizable version chain: missing same-assessment predecessor N-1 for assessment_id=%, version_number=%',
            gap.assessment_id, gap.version_number;
    END IF;
END
$$;

-- 3. Synthesize one ai_operations row per agent_execution_logs row, with result_revision_id and
--    expected_revision_id left NULL for now (assessment_revisions doesn't have matching rows yet
--    - steps 6/8 backfill them once it does). id is deterministically derived from the source
--    log's id (stable, collision-safe, and distinct from agent_attempts.id which reuses the
--    log's own id directly in step 4). requested_by is the assessment's real teacher_uid (a true
--    structural fact, not fabricated actor data - the legacy schema never recorded a
--    per-operation actor). "Produced a draft" is determined by
--    assessment_drafts.agent_execution_log_id referencing this log - the single-write field set
--    at draft-creation time - not by agent_execution_logs.draft_id, which is a second, later
--    cross-reference write (AgentExecutionLog.withDraftId) that could be missing even for a log
--    that did produce a draft. At most one draft is considered per log (DISTINCT ON), defensively,
--    in case legacy data ever has more than one (not expected under the real write path).
WITH log_draft AS (
    SELECT DISTINCT ON (d.agent_execution_log_id)
        d.agent_execution_log_id AS log_id, d.id AS draft_id, d.version_number
    FROM assessment_drafts d
    WHERE d.agent_execution_log_id IS NOT NULL
    ORDER BY d.agent_execution_log_id, d.version_number ASC
)
INSERT INTO ai_operations (
    id, assessment_id, operation_type, requested_by, idempotency_key,
    expected_revision_id, status, result_revision_id, created_at, updated_at
)
SELECT
    md5('legacy-ai-operation:' || l.id::text)::uuid,
    l.assessment_id,
    CASE WHEN ld.version_number IS NOT NULL AND ld.version_number > 1
         THEN 'REGENERATE_REVISION' ELSE 'CREATE_INITIAL_REVISION' END,
    a.teacher_uid,
    'legacy-backfill:' || l.id::text,
    NULL,
    CASE WHEN l.status = 'COMPLETED' AND ld.draft_id IS NOT NULL
         THEN 'SUCCEEDED' ELSE 'FAILED_TERMINAL' END,
    NULL,
    l.started_at,
    l.finished_at
FROM agent_execution_logs l
JOIN assessments a ON a.id = l.assessment_id
LEFT JOIN log_draft ld ON ld.log_id = l.id
WHERE NOT EXISTS (
    SELECT 1 FROM ai_operations op WHERE op.id = md5('legacy-ai-operation:' || l.id::text)::uuid
);

-- 4. Synthesize exactly one agent_attempts row per agent_execution_logs row (attempt_number = 1
--    always - no historical row is inferred to have been part of a retry chain). id reuses the
--    log's own id directly (a genuine 1:1 correspondence: the attempt IS the execution log).
--    agent_name is NOT NULL on this table but nullable on the legacy one - COALESCE to a
--    clearly-labeled placeholder for the rare row missing it (documented discovery, see
--    API-A4-HANDOFF.md; the app's own AgentAttempt.restore() requires a non-blank agentName).
WITH log_draft AS (
    SELECT DISTINCT ON (d.agent_execution_log_id)
        d.agent_execution_log_id AS log_id, d.id AS draft_id
    FROM assessment_drafts d
    WHERE d.agent_execution_log_id IS NOT NULL
    ORDER BY d.agent_execution_log_id, d.version_number ASC
)
INSERT INTO agent_attempts (
    id, ai_operation_id, attempt_number, agent_name, resolved_provider, resolved_model,
    prompt_version, correlation_id, dispatched_at, completed_at, status, provider_request_id,
    failure_code, estimated_input_tokens, estimated_output_tokens, cost_estimate, structured_result
)
SELECT
    l.id,
    md5('legacy-ai-operation:' || l.id::text)::uuid,
    1,
    COALESCE(NULLIF(TRIM(l.agent_name), ''), 'legacy-unknown'),
    l.provider,
    l.model,
    l.prompt_version,
    l.agent_execution_id::text,
    l.started_at,
    l.finished_at,
    CASE WHEN l.status = 'COMPLETED' AND ld.draft_id IS NOT NULL THEN 'COMPLETED' ELSE 'FAILED' END,
    NULL,
    l.error_code,
    l.estimated_input_tokens,
    l.estimated_output_tokens,
    l.cost_estimate::numeric(12,6),
    NULL
FROM agent_execution_logs l
LEFT JOIN log_draft ld ON ld.log_id = l.id
WHERE NOT EXISTS (
    SELECT 1 FROM agent_attempts aa WHERE aa.id = l.id
);

-- 5. Migrate every assessment_drafts row into assessment_revisions, id-preserving (compatible
--    with FKs since assessment_revisions has its own PK space). previous_revision_id is left
--    NULL in this pass and backfilled in step 6, once every row exists, using the structural
--    (assessment_id, version_number) chain rather than the legacy previous_version_id column
--    (see the note at the top of this file). source_agent_attempt_id reuses the draft's own
--    agent_execution_log_id directly (agent_attempts.id = log.id, from step 4), real historical
--    linkage, not fabricated.
INSERT INTO assessment_revisions (
    id, assessment_id, version_number, previous_revision_id, origin, actor_id, reason,
    source_agent_attempt_id, title, context, instructions, objectives, deliverables, constraints,
    created_at, provenance_complete
)
SELECT
    d.id,
    d.assessment_id,
    d.version_number,
    NULL,
    'LEGACY_UNKNOWN',
    NULL,
    NULL,
    d.agent_execution_log_id,
    d.title, d.context, d.instructions, d.objectives, d.deliverables, d.constraints,
    d.created_at,
    FALSE
FROM assessment_drafts d
WHERE NOT EXISTS (
    SELECT 1 FROM assessment_revisions r WHERE r.id = d.id
);

-- 6. Backfill previous_revision_id now that every migrated revision exists, using the structural
--    (assessment_id, version_number) chain - never assessment_drafts.previous_version_id, which
--    only proves "points at some assessment_drafts row", not "same assessment" or "exactly one
--    version back" (see the note at the top of this file, and the gap-validation block in step 2,
--    which guarantees this self-join always finds a predecessor for version_number > 1).
--    version_number = 1 always stays NULL. Never derived via MAX(version_number) - the self-join
--    is keyed on the exact predecessor version, so it is safe even if intermediate versions were
--    migrated out of order.
UPDATE assessment_revisions current_revision
SET previous_revision_id = predecessor.id
FROM assessment_revisions predecessor
WHERE current_revision.assessment_id = predecessor.assessment_id
  AND predecessor.version_number = current_revision.version_number - 1
  AND current_revision.version_number > 1
  AND current_revision.origin = 'LEGACY_UNKNOWN'
  AND current_revision.previous_revision_id IS NULL;

-- 7. Backfill ai_operations.result_revision_id for SUCCEEDED legacy operations, now that
--    assessment_revisions rows exist (avoids the ai_operations/assessment_revisions FK cycle).
WITH log_draft AS (
    SELECT DISTINCT ON (d.agent_execution_log_id)
        d.agent_execution_log_id AS log_id, d.id AS draft_id
    FROM assessment_drafts d
    WHERE d.agent_execution_log_id IS NOT NULL
    ORDER BY d.agent_execution_log_id, d.version_number ASC
)
UPDATE ai_operations op
SET result_revision_id = ld.draft_id
FROM agent_execution_logs l
JOIN log_draft ld ON ld.log_id = l.id
WHERE op.id = md5('legacy-ai-operation:' || l.id::text)::uuid
  AND op.status = 'SUCCEEDED'
  AND op.result_revision_id IS NULL;

-- 8. Backfill ai_operations.expected_revision_id for REGENERATE_REVISION legacy operations, now
--    that assessment_revisions rows (and their normalized previous_revision_id chain from step 6)
--    exist. expected_revision_id is the id of the assessment_revisions row for the same
--    assessment at version_number - 1 of the draft this operation's log produced - resolved
--    structurally via the same (assessment_id, version_number) join as step 6, never via the
--    legacy previous_version_id column. CREATE_INITIAL_REVISION operations are untouched by this
--    UPDATE (the WHERE clause only ever matches operations already classified
--    REGENERATE_REVISION back in step 3) and keep expected_revision_id = NULL.
WITH log_draft AS (
    SELECT DISTINCT ON (d.agent_execution_log_id)
        d.agent_execution_log_id AS log_id, d.assessment_id, d.version_number
    FROM assessment_drafts d
    WHERE d.agent_execution_log_id IS NOT NULL
    ORDER BY d.agent_execution_log_id, d.version_number ASC
)
UPDATE ai_operations op
SET expected_revision_id = predecessor.id
FROM agent_execution_logs l
JOIN log_draft ld ON ld.log_id = l.id
JOIN assessment_revisions predecessor
    ON predecessor.assessment_id = ld.assessment_id
   AND predecessor.version_number = ld.version_number - 1
WHERE op.id = md5('legacy-ai-operation:' || l.id::text)::uuid
  AND op.operation_type = 'REGENERATE_REVISION'
  AND op.expected_revision_id IS NULL;

-- 9. Point assessments.current_revision_id at the highest-version migrated legacy revision, for
--    every assessment that has legacy drafts and no current-revision pointer set yet. Guarded by
--    "IS NULL" so this never overwrites a pointer the new (post-A1) application code already set,
--    and is naturally idempotent on re-run (already non-NULL after the first run).
WITH latest_legacy_revision AS (
    SELECT DISTINCT ON (assessment_id) assessment_id, id
    FROM assessment_revisions
    WHERE origin = 'LEGACY_UNKNOWN'
    ORDER BY assessment_id, version_number DESC
)
UPDATE assessments a
SET current_revision_id = llr.id
FROM latest_legacy_revision llr
WHERE a.id = llr.assessment_id
  AND a.current_revision_id IS NULL;

-- 10. Post-migration integrity assertions - defensive, not permanent (no triggers or stored
--     procedures are created here; this DO block runs once, as part of this same migration, and
--     leaves nothing behind). Raises immediately if any invariant this script is supposed to
--     guarantee somehow does not hold, rather than silently deploying a corrupted revision chain.
--     Every check below is scoped to migration-produced rows only (origin = 'LEGACY_UNKNOWN' for
--     revisions, idempotency_key LIKE 'legacy-backfill:%' for operations) so it never second-
--     guesses application-created AI_GENERATED/HUMAN_EDITED data.
DO $$
DECLARE
    bad_row RECORD;
BEGIN
    SELECT id INTO bad_row FROM assessment_revisions
    WHERE origin = 'LEGACY_UNKNOWN' AND version_number = 1 AND previous_revision_id IS NOT NULL
    LIMIT 1;
    IF FOUND THEN
        RAISE EXCEPTION 'Integrity check failed: legacy revision % has version_number = 1 but a non-null previous_revision_id', bad_row.id;
    END IF;

    SELECT id INTO bad_row FROM assessment_revisions
    WHERE origin = 'LEGACY_UNKNOWN' AND version_number > 1 AND previous_revision_id IS NULL
    LIMIT 1;
    IF FOUND THEN
        RAISE EXCEPTION 'Integrity check failed: legacy revision % has version_number > 1 but a null previous_revision_id', bad_row.id;
    END IF;

    SELECT r.id INTO bad_row
    FROM assessment_revisions r
    JOIN assessment_revisions p ON p.id = r.previous_revision_id
    WHERE r.origin = 'LEGACY_UNKNOWN' AND p.assessment_id != r.assessment_id
    LIMIT 1;
    IF FOUND THEN
        RAISE EXCEPTION 'Integrity check failed: legacy revision % has previous_revision_id from a different assessment', bad_row.id;
    END IF;

    SELECT r.id INTO bad_row
    FROM assessment_revisions r
    JOIN assessment_revisions p ON p.id = r.previous_revision_id
    WHERE r.origin = 'LEGACY_UNKNOWN' AND p.version_number != r.version_number - 1
    LIMIT 1;
    IF FOUND THEN
        RAISE EXCEPTION 'Integrity check failed: legacy revision % has a predecessor that is not exactly version_number - 1', bad_row.id;
    END IF;

    SELECT id INTO bad_row FROM ai_operations
    WHERE idempotency_key LIKE 'legacy-backfill:%' AND operation_type = 'REGENERATE_REVISION' AND expected_revision_id IS NULL
    LIMIT 1;
    IF FOUND THEN
        RAISE EXCEPTION 'Integrity check failed: legacy REGENERATE_REVISION operation % has a null expected_revision_id', bad_row.id;
    END IF;

    SELECT id INTO bad_row FROM ai_operations
    WHERE idempotency_key LIKE 'legacy-backfill:%' AND operation_type = 'CREATE_INITIAL_REVISION' AND expected_revision_id IS NOT NULL
    LIMIT 1;
    IF FOUND THEN
        RAISE EXCEPTION 'Integrity check failed: legacy CREATE_INITIAL_REVISION operation % has a non-null expected_revision_id', bad_row.id;
    END IF;
END
$$;
