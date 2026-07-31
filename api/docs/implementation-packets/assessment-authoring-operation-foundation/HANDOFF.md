<a id="top"></a>

# API-HANDOFF — Assessment Authoring Operation Foundation (Final, Consolidated)

**Status:** Complete. Filled in by **Session A4** as the last commit on `feat/assessment-authoring-operation-foundation-api`, alongside [API-A4-HANDOFF.md](API-A4-HANDOFF.md). Committed as `docs(api): record final consolidated handoff for sessions A1-A4`. **Parent:** [README](README.md) · **Prev:** [TEST-PLAN](TEST-PLAN.md)

**This is the API packet's single authoritative handoff — the one Session D reads.** API execution is split into four sequential, recoverable sessions on one branch ([A1](CLAUDE-API-A1-PROMPT.md), [A2](CLAUDE-API-A2-PROMPT.md), [A3](CLAUDE-API-A3-PROMPT.md), [A4](CLAUDE-API-A4-PROMPT.md)), each producing its own intermediate handoff ([API-A1-HANDOFF.md](API-A1-HANDOFF.md), [API-A2-HANDOFF.md](API-A2-HANDOFF.md), [API-A3-HANDOFF.md](API-A3-HANDOFF.md), [API-A4-HANDOFF.md](API-A4-HANDOFF.md)) as a per-session recovery/gate record. This file does not replace those — it **summarizes** them into one cross-session view so Session D does not have to read four separate documents and reconstruct the sequence itself.

Format required by [09 — Session Handoff Protocol](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/09-session-handoff-protocol.md#-handoffmd-format). Every section must be present; write "None." where genuinely empty — do not delete a section.

```markdown
# API Handoff — Assessment Authoring Operation Foundation (Sessions A1-A4)

## Branch
feat/assessment-authoring-operation-foundation-api, pushed to origin at 3225d37e9e85f0c101adbbf1cf84f4566dad0308

## Session summary
A1 — Schema and Inert Domain             — HEAD 2d6d408c99253fbf230214a02d48cf0955932834 — PASS (370/370)
A2 — Idempotency and Durable Coordinator — HEAD 9c5bc0ad28e622d4c82294939d1ebb878f1693e9 — PASS (405/405)
A3 — Authoring Mutations and Public API  — HEAD be9cf6214791940d5e3445061e79107d8d78b35a — PASS (495/495)
   (includes: original A3 scope ending ad265c3, PASS 456/456; the A3 Contract Correction ending
    69c7f43, PASS 477/477; and the A3 Final Idempotency Correction ending be9cf62, PASS 495/495 —
    three sequential, recoverable corrections on the same branch, all folded into "A3" here since
    none started a new numbered session)
A4 — Backfill, Cleanup and Final Handoff — HEAD 3225d37e9e85f0c101adbbf1cf84f4566dad0308 — PASS (457/457)
Confirmed: no gap in the commit sequence across sessions — each session's starting commit matches
 the prior session's recorded final HEAD (A2 started at A1's f85c489; A3 started at A2's 14fb8f0;
 A4 started at A3's be9cf62, verified directly via `git rev-parse HEAD` at this session's preflight).

## Commits (full list, all four sessions, in order)
1352d3a feat(api): add durable ai_operations/agent_attempts schema                              (A1, Task 01)
a9e316b feat(api): add assessment_revisions schema and current-revision pointer                  (A1, Task 02)
490bed3 feat(api): add idempotency_records schema                                                (A1, Task 03)
1b4db20 feat(api): add AssessmentRevision aggregate and persistence                              (A1, Task 04)
2d6d408 feat(api): add AiOperation and AgentAttempt aggregates and persistence                   (A1, Task 05)
f85c489 docs(api): record session A1 handoff                                                     (A1)
faea8c1 feat(api): add reusable idempotency guard service                                        (A2, Task 06)
9c5bc0a feat(api): switch initial generation to durable AiOperation/AgentAttempt coordinator      (A2, Task 07B/07C)
14fb8f0 docs(api): record session A2 handoff                                                     (A2)
71c3ded feat(api): human edits create immutable revisions instead of mutating in place            (A3, Task 08)
248770c feat(api): regenerate requires expectedRevisionId and persists adjustment reason          (A3, Task 09)
ad265c3 feat(api): add retry/generation-status/revisions endpoints, remove in-place draft PATCH   (A3, Task 10)
3f820e3 docs(api): record session A3 handoff                                                     (A3)
010d35f fix(api): enforce idempotent assessment intent creation                                  (A3 Contract Correction)
13cb613 fix(api): align generation and retry responses with durable operation contract            (A3 Contract Correction)
69c7f43 docs(api): amend generation status contract and correct A3 handoff                        (A3 Contract Correction)
4fadbdc fix(api): persist AI idempotency outcomes atomically                                      (A3 Final Idempotency Correction)
be9cf62 docs(api): finalize A3 idempotency guarantees and handoff metadata                        (A3 Final Idempotency Correction)
e3be4cc feat(api): backfill legacy assessment_drafts/agent_execution_logs into revision/operation model (A4, Task 12)
3225d37 refactor(api): remove superseded AssessmentDraft/AgentExecutionLog code paths             (A4, Task 13)
<pending> docs(api): record final consolidated handoff for sessions A1-A4                         (A4)

## Tasks completed
01 — Done (A1, 1352d3a). V13: ai_operations/agent_attempts + uq_ai_operations_in_flight.
02 — Done (A1, a9e316b). V14/V15: assessment_revisions + assessments.current_revision_id/lock_version.
03 — Done (A1, 490bed3). V16: idempotency_records (NULLS NOT DISTINCT correction applied).
04 — Done (A1, 1b4db20). AssessmentRevision aggregate + persistence, TDD.
05 — Done (A1, 2d6d408). AiOperation/AgentAttempt aggregates + persistence, TDD.
06 — Done (A2, faea8c1). IdempotencyGuard service (check/record), TEACHER/ASSESSMENT scopes.
07B — Done (A2, 9c5bc0a). AiOperationCoordinator (three-phase) replaces DraftGenerationCoordinator
 for initial generation; folds in 07C (Agents contract test against the real fixture).
08 — Done (A3, 71c3ded). CreateHumanRevisionHandler replaces in-place applyEdit.
09 — Done (A3, 248770c). Regenerate requires expectedRevisionId, persists reason, routes through
 the Task 07B coordinator.
10 — Done (A3, ad265c3). Retry/generation-status/revisions endpoints; PATCH .../draft removed.
12 — Done (A4, e3be4cc). V17 backfill + provenance_complete column, TDD.
13 — Done (A4, 3225d37). Eight dead legacy classes + AssessmentConfig wiring + seven exclusive
 test classes removed; database guide updated.

## Migrations
V13, V14, V15, V16, V17 — all applied cleanly, in order, alongside V1-V12 untouched (confirmed by
 `AssessmentAuthoringSchemaMigrationTest` for V13-V16 and `LegacyAuthoringBackfillMigrationTest`
 for V17, both passing at final HEAD). V17 additionally adds `assessment_revisions.provenance_complete`.

## Endpoints (final state)
| Endpoint | Status | Session introduced/changed |
|---|---|---|
| `POST /api/v1/assessments` | `201`; `Idempotency-Key` required and enforced | A1-A2 baseline; idempotency enforcement added in the A3 Contract Correction |
| `POST /assessments/{id}/draft` (generate) | `201` or `202` (durable dispatch failure) | A2 (coordinator), 202 behavior added in the A3 Contract Correction |
| `POST /assessments/{id}/draft/regenerate` | `201` only, never `202`; requires `expectedRevisionId` | A3 (Task 09) |
| `POST /assessments/{id}/draft/retry` | `202` always | A3 (Task 10) |
| `GET /assessments/{id}/generation-status` | `200`; six-value status taxonomy | A3 (Task 10), taxonomy formalized in the A3 Contract Correction |
| `POST /assessments/{id}/revisions` (human edit) | `201`; requires `expectedRevisionId` | A3 (Task 08, wired to the controller in Task 10) |
| `GET /assessments/{id}/draft`, `GET .../draft/versions` | `200` | A3 (Task 10), migrated off the legacy table |
| `PATCH /assessments/{id}/draft` | **Removed** — `405` | A3 (Task 10) |
Full request/response shapes, typed error codes, and MockMvc-verified JSON examples:
 [API-A3-HANDOFF.md](API-A3-HANDOFF.md).

## Contract changes
1. Initial-generate's `202` (durable-failure) path and create-assessment's `Idempotency-Key`
   enforcement were both originally under-implemented at A3's first completion, then corrected in
   place by the A3 Contract Correction (2026-07-30) — documented, not silent, in
   [API-A3-HANDOFF.md](API-A3-HANDOFF.md).
2. `idempotency_records`'s unique constraint uses `UNIQUE NULLS NOT DISTINCT (...)`, not the plain
   `UNIQUE (...)` LOCAL-CONTRACTS.md's SQL block shows — required because `teacher_uid`/
   `assessment_id` are each null for the other scope (A1, [API-A1-HANDOFF.md](API-A1-HANDOFF.md)).
3. `ai_operations`'s two revision-id FKs are added in V14, not V13, since `assessment_revisions`
   doesn't exist until V14 (A1).
4. Concurrent-retry collisions map to `OPERATION_IN_PROGRESS`, not `STALE_REVISION` (A3 Final
   Idempotency Correction) — an existing failure code reused, not a new one introduced.
No other field, table, taxonomy, or contract value differs from LOCAL-CONTRACTS.md across any
 of the four sessions.

## Test results
`./mvnw -f api/pom.xml clean test` → **PASS**, final count **457/457**, 0 failures/errors/skipped.
Progression across the whole packet: **289** (pre-work baseline) → **370** (A1) → **405** (A2) →
 **456** (A3 original) → **477** (A3 Contract Correction) → **495** (A3 Final Idempotency
 Correction) → **505** (A4 Task 12, +10 migration tests) → **457** (A4 Task 13 final — a net
 decrease of 48 is expected and correct here: Task 13 deletes dead legacy code and its seven
 exclusive test classes, adding no new tests of its own, per its own "no tests to write first"
 acceptance criterion).

## Legacy migration outcome
Every `assessment_drafts` row → one `assessment_revisions` row: `origin = LEGACY_UNKNOWN`,
 `provenance_complete = false`, `actor_id = NULL`, `reason = NULL`, unconditionally — the legacy
 schema has no edit-timestamp column, so no row can be honestly labeled `AI_GENERATED` with
 confidence, even one that was never the target of a `PATCH`. Every `agent_execution_logs` row →
 one `ai_operations` + one `agent_attempts` row, single-attempt, `operationType` inferred from the
 linked draft's `version_number`, status derived from whether the log actually produced a
 persisted draft (via the draft's own `agent_execution_log_id`, the authoritative single-write
 field — not the log's own, second-write `draft_id` cross-reference). Verified by
 `LegacyAuthoringBackfillMigrationTest` (10/10 PASS) against fixture data covering all 8 mandated
 scenarios plus 2 discoveries (see [API-A4-HANDOFF.md](API-A4-HANDOFF.md) for full detail and the
 exact schema-column mapping used). No real legacy production/staging data existed in this branch's
 database at any point this cut — row-count confirmation against a real target dataset is
 Session D/deployment's responsibility, flagged under "Integration instructions" below.

## Elements removed
- `AssessmentDraft`, `AgentExecutionLog` (domain, ports, JPA entities, adapters, mappers — 8
  files) — removed A4, Task 13, commit 3225d37, once V17 (A4, Task 12, e3be4cc) had a proven,
  tested path to migrate every row they modeled.
- `DraftGenerationCoordinator` — removed A2 (Task 07B, commit 9c5bc0a), replaced by
  `AiOperationCoordinator`.
- `UpdateAssessmentDraftHandler`/`UpdateAssessmentDraftUseCase`/`UpdateAssessmentDraftCommand` —
  removed A3 (Task 08, commit 71c3ded), replaced by `CreateHumanRevisionHandler`.
- `PATCH /assessments/{id}/draft` — removed A3 (Task 10, commit ad265c3); confirmed `405`, not
  `404` (path still mapped for other methods).
- `applyEdit()` — never existed on `AssessmentRevision` (A1); `AssessmentDraft.applyEdit` itself
  was removed along with the rest of `AssessmentDraft` in Task 13.

## Assumptions
- Task 07B's Agents dependency (A2): the real fixture path was taken —
  `agents/docs/implementation-packets/assessment-authoring-operation-foundation/HANDOFF.md` at
  Agents HEAD `5542d7c` was already available and used directly, no fallback read-only inspection
  needed. See [API-A2-HANDOFF.md](API-A2-HANDOFF.md) for the fixture SHA-256 verification.
- A4's V17: with no real legacy data present in this branch's database, "safe to run twice" and
  "row counts match" were proven against fixture data engineered to cover every mandated scenario,
  not against a production-scale dataset — an explicit, disclosed assumption, not a silent gap
  (see [API-A4-HANDOFF.md](API-A4-HANDOFF.md) "Integration instructions").
- A4's `agent_name` COALESCE fallback (`'legacy-unknown'`) for the rare legacy row missing it: a
  conservative placeholder, disclosed as a discovery rather than treated as an error, since the
  legacy column is nullable but the new schema/domain require a non-blank value.

## Known residual risks
- The narrow pre-check-to-commit race for two truly concurrent `CREATE_INITIAL_REVISION` requests
  is narrowed (by `uq_ai_operations_in_flight` and `Assessment.lockVersion` CAS), not eliminated —
  accepted per the Idempotency and Concurrency Strategy ADR, not load-tested (A2).
- `INDETERMINATE` orphaned in-flight attempts (no scheduler/background reconciliation) are computed
  at read time by `generation-status` only — an accepted, documented design, not a bug (A3).
- `OPERATION_IN_PROGRESS` is not exercised against a real, genuinely-still-running dispatch end to
  end — the equivalent real-DB race is covered by the concurrent-retry test instead (A3).
- No cleanup job exists for expired `idempotency_records` rows (24h retention is a data column,
  not enforced deletion) — explicitly deferred past this cut (A1).
- V17's correctness against a real, non-trivial legacy dataset (row counts, performance) is
  unverified — only fixture data was available this cut (A4).

## Blockers
None.

## Integration instructions
- **Deploy ordering:** Task 10's `PATCH .../draft` removal must not reach `develop` ahead of
  Web's replacement call being ready — Session D coordinates the actual cutover timing (per
  [09 — Session Handoff Protocol](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/09-session-handoff-protocol.md)),
  this branch is ready-and-correct but does not itself decide deploy timing.
- **Legacy backfill on the real target environment:** re-verify V17's row counts against the
  actual `assessment_drafts`/`agent_execution_logs` tables once deployed somewhere those contain
  real data — this was only proven against fixtures in development (see
  [API-A4-HANDOFF.md](API-A4-HANDOFF.md)).
- **AssessmentStatus.java:** confirmed zero diff (`git diff origin/develop...HEAD --
  'api/src/**/AssessmentStatus.java'` is empty) — re-verify after merge, since a merge conflict
  resolution could in principle reintroduce a change.
- **Web adjustments Session D must apply** (from [API-A3-HANDOFF.md](API-A3-HANDOFF.md)'s Web C
  alignment section): add a `FAILED_TERMINAL` rendering path to `GenerationStatusValue` (non-
  retryable, distinct from `FAILED_RETRYABLE`); confirm `Idempotency-Key` is sent on
  `POST /api/v1/assessments`; keep Web's existing `202`-handling branch for initial generation (it
  is not dead code after all, per the A3 Contract Correction).
- **Agents contract HEAD:** `feat/assessment-authoring-operation-foundation-agents` at
  `5542d7c2b8b8c72142840087625b4fdd7dcb0bc2`, fixture SHA-256
  `9f961eac2228508d01fc50fe177f2701ecd2fa34f234fdb38ab28ea1c62fb696` (A2).
```

---

← [TEST-PLAN](TEST-PLAN.md) | [↑ inicio](#top) | [↑ README](README.md)
