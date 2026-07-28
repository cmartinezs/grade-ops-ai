<a id="top"></a>

# 06 — Database Migration

**Parent:** [README](README.md) · **Status:** Planned · **Prev:** [05 — Web Migration](05-web-migration.md) · **Next:** [07 — Testing Strategy →](07-testing-strategy.md)

Schema reference: [02 — Domain and Data Changes](02-domain-and-data-changes.md). This document covers ordering and the legacy data backfill specifically — the honest migration strategy required by the task brief §8, grounded in what Research 01 §13 and §6.3 actually established about the current schema's guarantees.

## Flyway file ordering (additive, V13 onward — current schema ends at V12)

```text
V13__add_agent_attempts_and_ai_operations.sql     -- agent_attempts, ai_operations (no FK to revisions yet)
V14__add_assessment_revisions.sql                 -- assessment_revisions (FKs agent_attempts)
V15__add_assessment_current_revision.sql          -- assessments.current_revision_id, lock_version
V16__add_idempotency_records.sql                  -- idempotency_records
V17__backfill_legacy_authoring_data.sql           -- one-time data migration, see below
```

`agent_attempts`/`ai_operations` are created before `assessment_revisions` because `assessment_revisions.source_agent_attempt_id` references `agent_attempts(id)`. `assessments.current_revision_id` is added only after `assessment_revisions` exists, so the FK is valid from the moment the column is created (starts `NULL` for every existing row — no immediate backfill required at V15, since V17 sets it explicitly per assessment).

Each `V13`–`V16` migration is schema-only (`CREATE TABLE`/`ALTER TABLE ADD COLUMN`), reversible by a straightforward `DROP`, and safe to run against a database with live traffic on the old tables, since nothing reads or writes the new tables until the application code from [08 — Implementation Sequence](08-implementation-sequence.md) tasks 1–9 is deployed.

## What can be migrated with certainty, what must be inferred, what must be marked unknown

Research 01 §13 already states the honest baseline: **`assessment_drafts` rows have no column recording whether a `PATCH` ever touched them.** `UpdateAssessmentDraftHandler` overwrites the row in place, preserving `id`, `version_number`, `created_at`, and `agent_execution_log_id` — so a row that was AI-generated and later hand-edited is indistinguishable, by data alone, from one that was never edited.

| Category | Can determine from data? | Migration label |
|---|---|---|
| A draft row that is **not** the current version for its assessment (i.e., a later regenerated version exists) | Yes — its `agent_execution_log_id` was set at creation and it can never have been the target of a `PATCH`, because `PATCH` only ever targets `findCurrentByAssessmentId` (the highest `version_number`) at the time it ran, and once a newer version exists, the older one could not have been "current" for any subsequent edit call *targeting this row* — **caveat below** | `AI_GENERATED`, `provenanceComplete = true` |
| A draft row that **is** the current version for its assessment, and its assessment has no way to prove a `PATCH` was ever issued against it | No — cannot distinguish "never edited" from "edited, and no other signal survives" | `LEGACY_UNKNOWN`, `provenanceComplete = false` |
| Any row's `reason`/edit rationale | No — `UpdateAssessmentDraftRequest` never persisted one, and the current schema has no audit table capturing it | Left `NULL`; never fabricated |

**Caveat on the "not current" rule above:** a row that is not the *final* current version could still have been edited at some point *while it was* current, before being superseded by a later regeneration. The application code does not prevent `PATCH` on a non-final row via direct API access (Research 01 §11, "Historical version is read-only only in UI"), so this rule is a reasonable, disclosed approximation, not a certainty. The migration script therefore does **not** claim `provenanceComplete = true` with full confidence for non-final rows either — it uses a slightly weaker, honestly-labeled distinction:

```text
origin = AI_GENERATED, provenanceComplete = true   → only for rows where the assessment has ONLY ever had exactly one AssessmentDraft row ever (version_number = 1, no regeneration, no other rows for that assessment) AND updated_at (if such a column existed) — since it does not, this case reduces further to: only assessments with a single draft row and zero evidence either way remain LEGACY_UNKNOWN too.
```

Given the schema genuinely does not carry an `updated_at`/edit-timestamp column on `assessment_drafts` (confirmed: `V11__add_assessment_drafts.sql` has no such column), the honest conclusion is stronger and simpler than the table above first suggests: **no `assessment_drafts` row can be labeled `AI_GENERATED` with full confidence purely from existing data, because none of them carry any signal that would positively rule out an in-place edit.** The migration therefore uses:

```text
origin = LEGACY_UNKNOWN for every migrated assessment_drafts row, unconditionally
provenanceComplete = false for every migrated row, unconditionally
actorId = NULL (no actor was ever recorded for any draft row, AI or human, under the legacy schema)
reason = NULL
sourceAgentAttemptId = the migrated agent_attempts.id corresponding to this row's agent_execution_log_id, when one exists (draft rows can have a non-null agent_execution_log_id even if later edited — the link itself is real data, only the "is this still what that log produced" claim is uncertain)
```

This is more conservative than the first table above, and is the version actually implemented — it is kept here specifically because it is a good example of an assumption ("non-final rows are safe to call `AI_GENERATED`") that does not survive a second look at what the schema actually proves, and future readers should see that reasoning, not just the conclusion.

`agent_execution_logs` rows migrate with full confidence for the fields they do carry (`provider`, `model`, `status`, timestamps, hashes, tokens, cost) — nothing about *those* rows is ambiguous; the ambiguity is specific to whether a linked draft's *content* still matches what the log produced.

## `ai_operations` synthesis for legacy rows

Each migrated `agent_execution_logs` row gets exactly one synthesized `ai_operations` row (`status = SUCCEEDED` if the log's `status = 'COMPLETED'` and it produced a draft, `FAILED_TERMINAL` if `status = 'FAILED'`) and exactly one `agent_attempts` row (`attempt_number = 1`) — no historical row is inferred to have been part of a multi-attempt retry chain, because the legacy schema has no way to prove that either; every legacy operation is treated as its own independent, single-attempt operation. `operationType` is inferred from the linked draft's `version_number` (`1` → `CREATE_INITIAL_REVISION`, `>1` → `REGENERATE_REVISION`) — this inference is safe because `version_number` is a real, enforced, non-ambiguous column (`UNIQUE (assessment_id, version_number)` since V11).

## What cannot be reconstructed, full stop

- Any human edit that happened before the *final* draft row for an assessment (superseded by a later regeneration) — if an edit happened and was then overwritten by a regeneration, there is no trace of it anywhere; migration does not attempt to detect or represent it.
- `adjustmentNotes` for any historical regeneration — never persisted by the legacy code, not recoverable.
- Any approval, review, or publication event — none exist in the legacy schema; none are fabricated. `provenanceComplete = false` is not upgraded to imply anything about review status.

## Legacy compatibility window

`assessment_drafts` and `agent_execution_logs` are **not** dropped in this cut. They remain, read-only (no application code writes to them after cutover), for one release, as a fallback audit trail in case the backfill needs manual review. A follow-up task (tracked, not scheduled here) drops them once the backfilled data has been spot-checked in the target environment.

## Rollback

Every migration V13–V17 is additive-only; rolling back means running the equivalent `DROP TABLE`/`DROP COLUMN` statements in reverse order, which is safe precisely because nothing in the legacy read/write path (`assessment_drafts`, `agent_execution_logs`, `AssessmentDraft`, `AgentExecutionLog` domain classes) is touched until the application-code tasks in [08 — Implementation Sequence](08-implementation-sequence.md) that consume the new tables are deployed. See [09 — Risks and Rollback](09-risks-and-rollback.md) for the full per-task rollback matrix.

---

← [05 — Web Migration](05-web-migration.md) | [↑ README](README.md) | [Siguiente: Testing Strategy →](07-testing-strategy.md)
