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
-- Every statement below is guarded (WHERE NOT EXISTS / IS NULL) so this script is safe to
-- execute a second time by hand, independent of Flyway's own history bookkeeping.

-- 1. Add the audit-only provenance_complete column. Existing (non-legacy) revisions default to
--    TRUE; every row this script inserts below explicitly overrides it to FALSE. Not exposed on
--    any HTTP contract in this cut (see API-A4-HANDOFF.md).
ALTER TABLE assessment_revisions
    ADD COLUMN IF NOT EXISTS provenance_complete BOOLEAN NOT NULL DEFAULT TRUE;

-- 2. Synthesize one ai_operations row per agent_execution_logs row, with result_revision_id left
--    NULL for now (assessment_revisions doesn't have matching rows yet - step 4 backfills it).
--    id is deterministically derived from the source log's id (stable, collision-safe, and
--    distinct from agent_attempts.id which reuses the log's own id directly in step 3).
--    requested_by is the assessment's real teacher_uid (a true structural fact, not fabricated
--    actor data - the legacy schema never recorded a per-operation actor).
--    "Produced a draft" is determined by assessment_drafts.agent_execution_log_id referencing
--    this log - the single-write field set at draft-creation time - not by
--    agent_execution_logs.draft_id, which is a second, later cross-reference write
--    (AgentExecutionLog.withDraftId) that could be missing even for a log that did produce a
--    draft. At most one draft is considered per log (DISTINCT ON), defensively, in case legacy
--    data ever has more than one (not expected under the real write path).
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

-- 3. Synthesize exactly one agent_attempts row per agent_execution_logs row (attempt_number = 1
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

-- 4. Migrate every assessment_drafts row into assessment_revisions, id-preserving (compatible
--    with FKs since assessment_revisions has its own PK space). previous_revision_id is left
--    NULL in this pass and backfilled in step 5, once every row exists, to avoid any dependency
--    on INSERT...SELECT row ordering for this self-referencing chain. source_agent_attempt_id
--    reuses the draft's own agent_execution_log_id directly (agent_attempts.id = log.id, from
--    step 3), real historical linkage, not fabricated.
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

-- 5. Backfill previous_revision_id now that every migrated revision exists (avoids the
--    self-referencing-FK ordering hazard step 4's comment describes).
UPDATE assessment_revisions r
SET previous_revision_id = d.previous_version_id
FROM assessment_drafts d
WHERE r.id = d.id
  AND d.previous_version_id IS NOT NULL
  AND r.previous_revision_id IS NULL
  AND r.origin = 'LEGACY_UNKNOWN';

-- 6. Backfill ai_operations.result_revision_id for SUCCEEDED legacy operations, now that
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

-- 7. Point assessments.current_revision_id at the highest-version migrated legacy revision, for
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
