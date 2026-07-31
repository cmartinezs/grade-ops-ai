<a id="top"></a>

# API-A4-HANDOFF — Session A4: Backfill, Cleanup and Final API Handoff

**Status:** Complete. **Parent:** [README](README.md)

This is Session A4's **own** handoff (its two tasks, specifically) — distinct from [HANDOFF.md](HANDOFF.md), which this same session also produces as the **consolidated** record of all four API sessions together. Fill in both; do not conflate them.

```markdown
# API Session A4 Handoff — Backfill, Cleanup and Final API Handoff

## Session
A4 — Backfill, Cleanup and Final API Handoff, plus a same-branch Migration Integrity Correction
 (see below) — no new session number, per the correction prompt's explicit instruction not to
 start Session D and not to create a V18.

## Branch
feat/assessment-authoring-operation-foundation-api

## HEAD
Implementation HEAD (correction): 6719afd. This document's own commit (the second correction
 commit) is reported externally in the final report, not inside this file — see
 [HANDOFF.md](HANDOFF.md)'s metadata-strategy note for why.

## Starting commit
be9cf6214791940d5e3445061e79107d8d78b35a (A3's final recorded HEAD, after the A3 Contract
 Correction and A3 Final Idempotency Correction — verified identical at this session's preflight).
The Migration Integrity Correction below started from d2d0e8bea1ed0370e2ccf0bb10528b41131a6ad0 —
 this session's own original Task 12/13 handoff commit, i.e. a continuation of the same session,
 not a new one.

## Tasks completed
12 — Done (commit e3be4cc, corrected by 6719afd). V17 backfill migration + provenance_complete
 column + LegacyAuthoringBackfillMigrationTest (RED confirmed before V17 existed; the migration
 test grew from 10 to 16 methods in the correction below).
13 — Done (commit 3225d37). Eight dead legacy classes + AssessmentConfig wiring + seven
 exclusive test classes removed; database guide updated.

## Commits
e3be4cc feat(api): backfill legacy assessment_drafts/agent_execution_logs into revision/operation model
3225d37 refactor(api): remove superseded AssessmentDraft/AgentExecutionLog code paths
6719afd fix(api): normalize legacy revision chains during V17 backfill (Migration Integrity Correction)

## Migration
V17__backfill_legacy_authoring_data.sql

This session's own fixture-driven migration test (`LegacyAuthoringBackfillMigrationTest`, 10
 scenarios) is the only exercise of V17 in this branch — no legacy production/staging data
 existed to migrate at commit time, so no real-world row counts are reported here; row-count
 confirmation against the actual target-environment `assessment_drafts`/`agent_execution_logs`
 tables is Session D/deployment's responsibility (flagged below under "Integration instructions").

Confirmed via the migration test, against real fixture data covering all 8 mandated scenarios
 (single-version, multi-version chain, possibly-edited-in-place draft, failed-generation-with-no-
 draft, draft-with-no-log, orphaned completed log, assessment-with-no-legacy-data, double
 execution) plus 2 additional edge cases (authoritative draft→log direction when the log's own
 cross-reference was never written back; `provenance_complete` column defaulting):
- Every migrated revision has `origin = LEGACY_UNKNOWN`, `provenance_complete = false`,
  `actor_id = NULL`, `reason = NULL`, unconditionally — **yes**, asserted explicitly in every
  scenario, including the "possibly edited in place" case which exists specifically to prove the
  conservative rule holds even for a row that *could* have been AI_GENERATED-with-confidence
  under a more generous (rejected) rule.
- Zero `AI_GENERATED`/`HUMAN_EDITED` labels anywhere in migrated data — **yes**, asserted via
  `assertThat(origin).isNotEqualTo("AI_GENERATED")`/`.isNotEqualTo("HUMAN_EDITED")` in addition to
  the positive `LEGACY_UNKNOWN` assertion.
- Script is safe to run twice — **yes**, verified two ways: (a) the full V17 script (including the
  `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`) runs once automatically at context startup against
  empty legacy tables as a harmless no-op (the same thing a real deploy against a legacy-free
  database does); (b) `shouldProduceNoAdditionalOrChangedRowsWhenTheBackfillSqlIsExecutedASecondTimeDirectly`
  re-executes the raw SQL file a second time on the *same* connection as the seeded fixtures
  (bypassing Flyway's own history bookkeeping entirely, per the session prompt's explicit
  instruction not to rely solely on Flyway preventing re-runs), asserting identical row counts,
  an unchanged `current_revision_id`, and unchanged `origin`/`provenance_complete` values.

### Real schema mapping used (not invented — read from V11/V12 before writing V17)
- `assessment_drafts.id` → `assessment_revisions.id` (reused directly — same PK space, no FK
  collision risk, makes the self-referential `previous_revision_id` chain trivial to preserve).
- `assessment_drafts.agent_execution_log_id` → `assessment_revisions.source_agent_attempt_id`
  (also `agent_attempts.id`, reused directly from `agent_execution_logs.id`).
- `agent_execution_logs.id` → `agent_attempts.id` (reused directly).
- A new, deterministic id (`md5('legacy-ai-operation:' || log.id)::uuid`) synthesizes
  `ai_operations.id` — stable across re-runs, collision-safe, never a random UUID.
- "Did this log produce a draft" is determined by `assessment_drafts.agent_execution_log_id`
  referencing the log (the single, first-write field set at draft-creation time), **not** by
  `agent_execution_logs.draft_id` (a second, later cross-reference write —
  `AgentExecutionLog.withDraftId` — that could be missing even for a log that genuinely did
  produce a draft, e.g. a crash between the two writes). See discovery below.
- `ai_operations.requested_by` ← the assessment's real `teacher_uid` (a true structural fact
  looked up via join, not a fabricated actor — the legacy schema never recorded a per-operation
  actor at all).

## Migration Integrity Correction (same session, commit 6719afd)

Discovered after the original A4 handoff was recorded: `assessment_drafts.previous_version_id`
 only guarantees "references some `assessment_drafts` row" — the legacy schema's self-FK never
 enforced that the target belongs to the same assessment, nor that it is exactly
 `version_number - 1`. The original V17 copied this column directly into
 `assessment_revisions.previous_revision_id`, which could therefore have produced a cross-
 assessment link, a skipped-version link, or (for a null legacy value) an unlinked chain — none of
 which the revision-chain integrity invariants in [Assessment Authoring
 Model](../../../../docs/99-decisions/2026-07-28-assessment-authoring-model.md) would tolerate
 from application-created data.

Fixed by reconstructing `previous_revision_id` and the new `ai_operations.expected_revision_id`
 backfill **structurally**, from `(assessment_id, version_number)`, never from the legacy
 `previous_version_id` column:
- A new pre-insert `DO $$` block (step 2 of V17) validates every `assessment_drafts` row with
  `version_number > 1` has a same-assessment predecessor at `version_number - 1`; if not, it
  `RAISE EXCEPTION`s, which aborts the whole migration atomically (Flyway runs each migration in
  one transaction against PostgreSQL) — no partial `ai_operations`/`agent_attempts`/
  `assessment_revisions` rows are left behind for that assessment or any other.
- Step 6 (`previous_revision_id` backfill) is now a self-join on `assessment_revisions` keyed by
  `(assessment_id, version_number - 1)`, not a copy of `assessment_drafts.previous_version_id`.
  Never uses `MAX(version_number)`.
- A new step 8 backfills `ai_operations.expected_revision_id` for every `REGENERATE_REVISION`
  legacy operation, resolved the same structural way (`CREATE_INITIAL_REVISION` operations are
  untouched and keep it `NULL`). This column existed on `ai_operations` since V13 but the original
  V17 never populated it for legacy rows.
- A new step 10 (`DO $$` block, no permanent trigger/procedure) asserts the invariants above hold
  for every migrated row before the migration completes, as a defensive check against a future
  edit to this file reintroducing the same class of bug.
- The test helper (`LegacyAuthoringBackfillMigrationTest.applyBackfill()`) was rewritten to stop
  hand-splitting the SQL file on `;` (which cannot correctly handle a `DO $$ ... $$` block's
  internal semicolons) and instead calls `ScriptUtils.executeSqlScript(...)` with
  `ScriptUtils.EOF_STATEMENT_SEPARATOR`, which hands the entire, unmodified V17 file to PostgreSQL
  as a single multi-statement simple-query batch — statement boundaries and `$$` dollar-quoting
  are then parsed server-side by PostgreSQL itself (the same mechanism `psql -f script.sql` uses),
  not by any client-side parser, hand-rolled or Spring-provided. Confirmed against Spring's own
  `ScriptUtils` source (7.0.8) that its char-by-char `splitSqlScript` does **not** understand `$$`
  quoting on its own; `EOF_STATEMENT_SEPARATOR` mode sidesteps that entirely by not splitting
  client-side at all.
- Six new test methods (Cases A–F) added to `LegacyAuthoringBackfillMigrationTest`, confirmed RED
  against the original V17 (all six failed with either a wrong/null `previous_revision_id`/
  `expected_revision_id`, or "expected an exception, none thrown" for the gap case), then GREEN
  after the fix — 16/16 in that test class, 463/463 for the full suite (up from 457/457).

No `V18` was created — this corrects `V17` in place, per the correction prompt's explicit
 instruction, since `V17` had not yet been deployed to any shared/integrated environment. If a
 developer's **local, persistent** database already applied the pre-correction `V17` and Flyway's
 checksum validation now rejects the edited file, that local database must be recreated or Flyway-
 repaired **on that local environment only** — never `flyway repair` against a shared environment,
 and no `V18` workaround.

## Legacy code removed
Eight classes: `AssessmentDraft`, `AgentExecutionLog` (domain); `AssessmentDraftRepositoryPort`,
 `AgentExecutionLogRepositoryPort` (application ports); `AssessmentDraftJpaEntity`,
 `AssessmentDraftJpaRepository`, `AssessmentDraftPersistenceAdapter`,
 `AssessmentDraftPersistenceMapper`, `AgentExecutionLogJpaEntity`,
 `AgentExecutionLogJpaRepository`, `AgentExecutionLogPersistenceAdapter`,
 `AgentExecutionLogPersistenceMapper` (infrastructure — 8 of these, 4 domain/port, 12 total
 production files). `AssessmentConfig.java`'s 4 now-orphaned `@Bean` methods (mapper + adapter for
 each) and their imports removed. Seven exclusive test classes also removed:
 `AgentExecutionLogTest`, `AssessmentDraftTest`, `AgentExecutionLogPersistenceAdapterIntegrationTest`,
 `AgentExecutionLogPersistenceAdapterTest`, `AssessmentDraftPersistenceAdapterTest`,
 `AssessmentDraftPersistenceAdapterIntegrationTest`, and `AssessmentPersistenceFkChainIntegrationTest`
 (the last one exercised the `assessment_drafts` self-referencing FK chain specifically — its
 equivalent coverage for `assessment_revisions` already exists via
 `AssessmentRevisionPersistenceAdapterTest`, so no coverage was lost).

Verification: `grep -r "AssessmentDraft\|AgentExecutionLog" api/src` (the literal mandated
 command) returns 310 matches — but this literal grep also matches the substring inside
 legitimate, unrelated current class/method names that happen to contain "Draft" or share no
 real relationship to the deleted classes (`GenerateAssessmentDraftHandler`,
 `RegenerateAssessmentDraftHandler`, `AgentExecutionLogPayload` in `AssessmentAgentResponse`
 doc-comments referring to the *agents/* module's own DTO, etc. — none of these ever imported or
 instantiated the deleted classes). A word-boundary-precise grep for the exact eight deleted
 class names (`\b(AssessmentDraft|AssessmentDraftJpaEntity|...|AgentExecutionLogRepositoryPort)\b`)
 returns exactly 11 matches, **all** Javadoc/comment prose (`{@code AssessmentDraft}`,
 `AgentExecutionLog.withDraftId` inside V17's own SQL comment and the migration test's comment)
 explaining what was replaced — zero imports, zero instantiations, zero type declarations, zero
 method calls. `grep -rniE "DROP TABLE[[:space:]]+(assessment_drafts|agent_execution_logs)"
 api/src/main/resources/db/migration` returns zero matches.

## Legacy tables status
`assessment_drafts`, `agent_execution_logs` — **NOT dropped**, read-only retention for one release
 per the ADR. Confirmed no application code reads or writes them: the word-boundary grep above
 shows zero real class references remaining anywhere in `api/src/main/java`, and `AssessmentConfig`
 no longer registers persistence beans for either table.

## AssessmentStatus check
`git diff origin/develop...HEAD -- 'api/src/**/AssessmentStatus.java'` → **empty**, confirmed.

## Discovery: draft→log cross-reference direction (not a blocker, documented per §"Handling discoveries")
`agent_execution_logs.draft_id` and `assessment_drafts.agent_execution_log_id` are two separate,
 independently-written columns pointing at the same relationship. The domain code
 (`AgentExecutionLog.create`/`withDraftId`) writes the log first with `draftId = null`, then the
 draft with `agentExecutionLogId` set, then updates the log's `draft_id` as a second, later write.
 A crash between the second and third writes would leave `agent_execution_logs.draft_id` NULL for
 a log that *did* produce a draft. V17 uses `assessment_drafts.agent_execution_log_id` (the single,
 first-write field) as the authoritative "did this log produce a draft" signal, not
 `agent_execution_logs.draft_id` — proven correct by a dedicated test
 (`shouldDetectAProducedDraftViaTheDraftsOwnLogReferenceEvenWhenTheLogWasNeverCrossReferencedBack`).

## Discovery: agent_name nullability mismatch (not a blocker)
`agent_execution_logs.agent_name` is nullable in the legacy schema; `agent_attempts.agent_name` is
 `NOT NULL` in the new schema, and the domain's `AgentAttempt.restore()` further requires it
 non-blank. V17 applies `COALESCE(NULLIF(TRIM(agent_name), ''), 'legacy-unknown')` — a clearly-
 labeled, conservative placeholder, not a fabricated real agent name — for the rare legacy row
 missing it, rather than leaving the migration unable to proceed for that row.

## Tests
`LegacyAuthoringBackfillMigrationTest` — **16/16 PASS** (grew from 10 to 16 in the Migration
 Integrity Correction). Fixture data covers: (1) single-version assessment with a completed log;
 (2) multi-version regenerated assessment (3-revision chain); (3) a draft that might have been
 edited in place, asserting the conservative label holds; (4) a failed generation with no draft
 produced; (5) a draft with no associated log; (6) an orphaned log claiming COMPLETED with no
 draft referencing it back; (7) an assessment with no legacy data; (8) double execution of the raw
 SQL; the two discoveries above; the `provenance_complete` column's own default-true/explicit-
 false behavior; and — added by the correction — Case A (null legacy link repaired structurally),
 Case B (cross-assessment legacy link ignored), Case C (skipped-version legacy link corrected),
 Case D (irrecoverable gap fails atomically, verified via `SAVEPOINT`/`ROLLBACK TO SAVEPOINT`
 around the assertion so the rest of the test can still query post-failure state), Case E
 (`expected_revision_id` set for `REGENERATE_REVISION`, null for `CREATE_INITIAL_REVISION`), Case F
 (chain normalization and `expected_revision_id` both stable across a second execution).

## Baseline
Before this session (A3 Final Idempotency Correction's recorded final count): 495 tests.
After Task 12 (isolated checkpoint): 505/505 (495 + 10 new migration tests).
After Task 13 (original A4 completion): 457/457 (505 − 48: the seven deleted test classes' own
 test methods — an expected decrease, since Task 13 removes dead code and its dedicated tests
 rather than adding new ones, per its own "no tests to write first" acceptance criterion).
After the Migration Integrity Correction (final, for the whole API packet): **463/463**, 0
 failures, 0 errors, 0 skipped (457 + 6 new revision-chain-normalization tests).
`./mvnw -f api/pom.xml clean test` → **PASS**, confirmed via a full, non-cached (`clean test`) run
 against real Testcontainers Postgres, at implementation HEAD 6719afd.

## Blockers
None.

## Integration instructions for Session D
See the consolidated HANDOFF.md for the full cross-session summary — this section covers only
 what's specific to Task 12/13:
- No real legacy production/staging data existed in this development branch's database at any
  point — V17 was proven correct against fixture data only. Before/when V17 is actually deployed
  against a database carrying real `assessment_drafts`/`agent_execution_logs` rows, re-verify row
  counts (`SELECT COUNT(*) FROM assessment_drafts` vs. `SELECT COUNT(*) FROM assessment_revisions
  WHERE origin = 'LEGACY_UNKNOWN'`, similarly for logs → operations/attempts) match exactly, since
  this was never observed against a non-trivial dataset.
- The legacy compatibility window (`assessment_drafts`/`agent_execution_logs` retained, read-only,
  for one release) starts counting from whenever V17 is actually deployed, not from this commit.
- No performance characteristics were observed or need reporting — V17 was only ever exercised
  against small fixture-sized data in this session.
- **Web must add `LEGACY_UNKNOWN` to its `AssessmentRevisionOrigin` type union.** Web currently
  only models `"AI_GENERATED" | "HUMAN_EDITED"`, but `GET /api/v1/assessments/{id}/draft` and
  `GET .../draft/versions` can return a legacy-migrated revision with `origin = "LEGACY_UNKNOWN"`,
  `actorId = null`, `reason = null` once V17 has run. Session D must: widen the type union; add a
  neutral rendering path for unknown provenance (never presented as AI-generated or human-edited);
  add mapper/component tests for it; keep the literal `"LEGACY_UNKNOWN"` string canonical (do not
  rename or alias it on the Web side).
```

---

← [README](README.md) | [↑ inicio](#top)
