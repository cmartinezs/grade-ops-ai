<a id="top"></a>

# 08 — Implementation Sequence

**Parent:** [README](README.md) · **Status:** Planned · **Prev:** [07 — Testing Strategy](07-testing-strategy.md) · **Next:** [09 — Risks and Rollback →](09-risks-and-rollback.md)

Fourteen tasks. Ordered so the repository compiles after every task and passes its full test suite after every task except where explicitly marked "compile-only checkpoint" (schema-only tasks with nothing yet to exercise them). Verticality note: tasks 1–5 build new, unused-by-anyone infrastructure (safe, additive, zero behavior change); task 6 is the pivot where the write path actually switches over; tasks 7–11 extend the switch to every operation and to the API/Web surface; tasks 12–14 handle legacy data, cleanup, and final regression.

---

### Task 01 — Schema: durable AI operation tables

- **Objective:** Add `ai_operations` and `agent_attempts` tables, additive, unused by any application code yet.
- **Files:** `api/src/main/resources/db/migration/V13__add_agent_attempts_and_ai_operations.sql`
- **Dependencies:** none
- **Steps:** Write the migration per [02 — Domain and Data Changes](02-domain-and-data-changes.md#persistence-changes). Include the partial unique index `uq_ai_operations_in_flight`.
- **Tests to write first:** none (schema-only) — write the migration test after: a Testcontainers test asserting the migration applies cleanly to an empty DB and the partial unique index exists and behaves as expected (insert two `PENDING` rows for the same `(assessment_id, operation_type)`, second insert fails).
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=*MigrationTest` (or the full suite — this is fast and additive, no reason to skip the full run: `./mvnw -f api/pom.xml test`)
- **Acceptance criteria:** Flyway applies V13 cleanly from empty DB alongside V1–V12; the partial unique index rejects a second concurrent in-flight row in a direct SQL test.
- **Migration considerations:** none — additive, no existing data touched.
- **Risks:** low. Rollback: `DROP TABLE agent_attempts, ai_operations;`.
- **Commit boundary:** `feat(api): add durable ai_operations/agent_attempts schema`

### Task 02 — Schema: assessment revisions and current-revision pointer

- **Objective:** Add `assessment_revisions` table and `assessments.current_revision_id`/`lock_version` columns.
- **Files:** `api/src/main/resources/db/migration/V14__add_assessment_revisions.sql`, `V15__add_assessment_current_revision.sql`
- **Dependencies:** Task 01 (FK to `agent_attempts`)
- **Steps:** Per [02](02-domain-and-data-changes.md#persistence-changes). `lock_version` defaults `0 NOT NULL`.
- **Tests to write first:** none (schema-only); after: migration test extended to cover V14/V15, plus a direct-SQL test proving the `UNIQUE (assessment_id, version_number)` and self-referential FK exist.
- **Verification command:** `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** V14/V15 apply cleanly after V13; existing `assessments` rows get `lock_version = 0`, `current_revision_id = NULL`.
- **Migration considerations:** none yet — no backfill in this task.
- **Risks:** low. Rollback: `ALTER TABLE assessments DROP COLUMN current_revision_id, DROP COLUMN lock_version; DROP TABLE assessment_revisions;`.
- **Commit boundary:** `feat(api): add assessment_revisions schema and current-revision pointer`

### Task 03 — Schema: idempotency records

- **Objective:** Add `idempotency_records` table.
- **Files:** `api/src/main/resources/db/migration/V16__add_idempotency_records.sql`
- **Dependencies:** Task 02 (FK to `assessments`)
- **Steps:** Per [02](02-domain-and-data-changes.md#persistence-changes).
- **Tests to write first:** none; after: unique-constraint direct-SQL test.
- **Verification command:** `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** V16 applies cleanly; unique constraint rejects a duplicate `(scope_type, teacher_uid, assessment_id, operation_type, idempotency_key)`.
- **Migration considerations:** none.
- **Risks:** low. Rollback: `DROP TABLE idempotency_records;`.
- **Commit boundary:** `feat(api): add idempotency_records schema`

### Task 04 — Domain: `AssessmentRevision` aggregate + persistence

- **Objective:** Introduce `AssessmentRevision` domain class, JPA entity, repository port + adapter — parallel to `AssessmentDraft`, not yet wired into any use case.
- **Files:** `api/src/main/java/.../assessment/domain/model/AssessmentRevision.java`, `RevisionOrigin.java`; `.../application/port/out/AssessmentRevisionRepositoryPort.java`; `.../infrastructure/adapter/out/persistence/AssessmentRevisionJpaEntity.java`, `AssessmentRevisionPersistenceAdapter.java`
- **Dependencies:** Task 02
- **Steps:** Implement per [02 — Domain and Data Changes](02-domain-and-data-changes.md#assessmentrevision--new-aggregate-replaces-assessmentdraft-as-the-write-path) — factories `generateFromAi`/`regenerateFromAi`/`createFromHumanEdit`/`restore`, no `applyEdit`.
- **Tests to write first:** Unit tests for chain-integrity invariants (version 1 has no predecessor; N>1 requires same-assessment predecessor at N-1) and immutability, written before the class — TDD.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AssessmentRevisionTest`, then full `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** all invariants from [02](02-domain-and-data-changes.md#invariants-enforced-in-application-code--proven-by-integration-tests-per-assessment-authoring-model) hold under unit test; a Testcontainers round-trip test (`AssessmentRevisionPersistenceAdapterTest`) proves save/load fidelity.
- **Migration considerations:** none — this class is not yet used by any handler.
- **Risks:** low — purely additive code, unreachable from any endpoint yet.
- **Commit boundary:** `feat(api): add AssessmentRevision aggregate and persistence`

### Task 05 — Domain: `AiOperation`/`AgentAttempt` aggregates + persistence

- **Objective:** Introduce both new aggregates, ports, and adapters — not yet wired into any use case.
- **Files:** `.../domain/model/AiOperation.java`, `AiOperationType.java`, `AiOperationStatus.java`, `AgentAttempt.java`, `AgentAttemptStatus.java`; ports + adapters mirroring Task 04's pattern.
- **Dependencies:** Task 01
- **Steps:** Per [02](02-domain-and-data-changes.md#aioperation--new-aggregate) and [04 — AI Operation Lifecycle](04-ai-operation-lifecycle.md). Include the state-transition guards (e.g., cannot transition `SUCCEEDED` → anything).
- **Tests to write first:** Unit tests for valid/invalid state transitions on both aggregates, written first.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AiOperationTest,AgentAttemptTest`, then full suite
- **Acceptance criteria:** invalid transitions throw `DomainInvariantViolationException`; Testcontainers round-trip tests pass; the `uq_ai_operations_in_flight` index is exercised by an adapter-level test attempting to save two in-flight rows.
- **Migration considerations:** none.
- **Risks:** low.
- **Commit boundary:** `feat(api): add AiOperation and AgentAttempt aggregates and persistence`

### Task 06 — Application: idempotency guard service

- **Objective:** A reusable, generic idempotency-check/record service usable by any command handler.
- **Files:** `.../application/security/IdempotencyGuard.java` (or `.../shared/application/idempotency/`), `.../port/out/IdempotencyRepositoryPort.java`, adapter.
- **Dependencies:** Task 03
- **Steps:** `check(scope, operationType, key, payloadHash) → Optional<PriorResult>`; `record(scope, ..., resultReference, status)`. Hash function over validated command fields, per [Idempotency and Concurrency Strategy](../../99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md).
- **Tests to write first:** Unit tests for the check/record contract (no record → proceed; same hash → replay; different hash → conflict) before implementation.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=IdempotencyGuardTest`, then full suite
- **Acceptance criteria:** matches the three-way contract exactly (first-use, replay, mismatch); not yet wired into any endpoint.
- **Migration considerations:** none.
- **Risks:** low.
- **Commit boundary:** `feat(api): add reusable idempotency guard service`

### Task 07 — Application: rewrite the generation coordinator (the pivot task)

- **Objective:** Replace `DraftGenerationCoordinator` with the three-phase, `AiOperation`/`AgentAttempt`/`AssessmentRevision`-aware coordinator from [04 — AI Operation Lifecycle](04-ai-operation-lifecycle.md). Wire `GenerateAssessmentDraftHandler` to it. **This is the task where the write path actually changes** — every prior task was additive and inert.
- **Files:** New `AiOperationCoordinator.java` replacing `DraftGenerationCoordinator.java`; `GenerateAssessmentDraftHandler.java` updated; `GlobalExceptionHandler` updated for the new typed error codes ([03](03-operation-and-api-contracts.md#typed-error-codes-shared-across-the-above)); `AssessmentAgentResponse.Log`/`AgentExecutionLogPayload` gain a `provider` field if not already present (verify against current code first — see [04](04-ai-operation-lifecycle.md#where-providermodel-get-resolved)).
- **Dependencies:** Tasks 04, 05, 06
- **Steps:** Implement the Phase 0 / dispatch / Phase 2 sequence exactly as specified in [04](04-ai-operation-lifecycle.md#when-the-agentattempt-is-recorded). `GenerateAssessmentDraftHandler` now: checks idempotency (Task 06) → checks `ALREADY_GENERATED` precondition → delegates to the new coordinator → maps typed failures to the response shape from [03](03-operation-and-api-contracts.md#post-apiv1assessmentsiddraft--generate-initial-revision).
- **Tests to write first:** (a) the failure-recovery test proving an `AgentAttempt` survives a simulated Phase-2 crash after Phase-0 commit, written before the phase split exists — it should fail against the *old* coordinator and pass against the new one, proving the fix; (b) `ALREADY_GENERATED` precondition test; (c) idempotency replay test for this specific endpoint.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AiOperationCoordinatorTest,GenerateAssessmentDraftHandlerIntegrationTest`, then full suite (this task must not regress the 289-test baseline from Research 03)
- **Acceptance criteria:** acceptance criteria #9, #10, #11 from [10 — Acceptance Criteria](10-acceptance-criteria.md); old `DraftGenerationCoordinatorTest`/`DraftGenerationCoordinator.java` deleted, not left dangling.
- **Migration considerations:** none yet — legacy tables untouched, this only affects newly-created assessments' first generation.
- **Risks:** **highest-risk task in this plan** — it is the one place where old and new models must coexist correctly (an assessment created before this deploy has no `currentRevisionId`; the `ALREADY_GENERATED` check must correctly treat that as "not generated yet," not crash). Mitigation: explicit test with an `Assessment` restored via the pre-migration shape (`currentRevisionId = null`, no `assessment_revisions` rows) going through the new generate path successfully.
- **Commit boundary:** `feat(api): switch initial generation to durable AiOperation/AgentAttempt coordinator` (implementation + its own tests in one commit; do not split the coordinator from its tests across commits)

### Task 08 — Application: human edit creates a revision

- **Objective:** Replace `UpdateAssessmentDraftHandler`'s in-place edit with `CreateHumanRevisionHandler`, CAS-protected.
- **Files:** Delete `UpdateAssessmentDraftCommand`/`UpdateAssessmentDraftUseCase`/`UpdateAssessmentDraftHandler`; add `CreateHumanRevisionCommand`/`CreateHumanRevisionUseCase`/`CreateHumanRevisionHandler`.
- **Dependencies:** Task 04
- **Steps:** Load `Assessment`, verify ownership, compare `expectedRevisionId` to `currentRevisionId` (mismatch → `STALE_REVISION`), create `AssessmentRevision.createFromHumanEdit(...)`, CAS-update `Assessment.currentRevisionId`/`lockVersion` in one transaction.
- **Tests to write first:** (a) the test proving the *old* "edit preserves version/id" assertion is now false — write it as "edit creates a new revision with a new id and incremented version" and confirm it fails against old code / passes against new; (b) `STALE_REVISION` conflict test; (c) concurrency test (two concurrent edits, exactly one succeeds).
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=CreateHumanRevisionHandlerIntegrationTest`, then full suite
- **Acceptance criteria:** criteria #1, #2, #3, #5 from [10](10-acceptance-criteria.md).
- **Migration considerations:** none.
- **Risks:** medium — must delete, not deprecate-and-ignore, the old handler and its now-contradictory test (see [07 — Testing Strategy](07-testing-strategy.md#specific-regression-guards-required-not-optional) item 2).
- **Commit boundary:** `feat(api): human edits create immutable revisions instead of mutating in place`

### Task 09 — Application: regenerate requires expected revision

- **Objective:** Update `RegenerateAssessmentDraftHandler` to require `expectedRevisionId`, persist `adjustmentNotes` as `reason`, and route through the Task 07 coordinator.
- **Files:** `RegenerateAssessmentDraftCommand.java`, `RegenerateAssessmentDraftHandler.java`, `RegenerateAssessmentDraftRequest.java`
- **Dependencies:** Tasks 04, 07
- **Steps:** Add `expectedRevisionId` to command/request. Pre-dispatch CAS check before calling the coordinator (avoids the wasted-LLM-call race per [Idempotency and Concurrency Strategy](../../99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md)).
- **Tests to write first:** `STALE_REVISION` pre-dispatch test (asserting **no** call reaches the agent client mock when stale); `reason` persistence test; concurrency test (two regenerations, one succeeds).
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=RegenerateAssessmentDraftHandlerIntegrationTest`, then full suite
- **Acceptance criteria:** criteria #6, #7, #8 from [10](10-acceptance-criteria.md).
- **Migration considerations:** none.
- **Risks:** medium — the pre-dispatch check must genuinely happen before the coordinator's Phase 1 (agent call), verified by asserting zero invocations on the mocked agent client port in the stale-conflict test, not merely asserting the final response code.
- **Commit boundary:** `feat(api): regenerate requires expectedRevisionId and persists adjustment reason`

### Task 10 — Application + API: retry and generation-status

- **Objective:** New `RetryGenerationHandler` + `GetGenerationStatusHandler`, wired to new controller endpoints.
- **Files:** New use cases; `AssessmentController.java` gains `POST .../draft/retry`, `GET .../generation-status`; `PATCH .../draft` **removed**; `POST .../revisions` added (wraps Task 08's handler).
- **Dependencies:** Tasks 05, 07, 08, 09
- **Steps:** Implement per [03 — Operation and API Contracts](03-operation-and-api-contracts.md) exactly — status codes, typed error bodies, the `INDETERMINATE` classification logic from [04](04-ai-operation-lifecycle.md#orphaned-in-flight-detection-no-scheduler).
- **Tests to write first:** API acceptance tests per endpoint (happy path + every typed error in the [03](03-operation-and-api-contracts.md#typed-error-codes-shared-across-the-above) table for that endpoint), written against the target contract before wiring the controller.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AssessmentControllerTest` (or equivalent `@SpringBootTest` acceptance test class), then full suite
- **Acceptance criteria:** every row of the [03](03-operation-and-api-contracts.md) contract table is exercised by at least one passing test; `PATCH .../draft` returns `404`/`405` (route no longer exists), verified explicitly.
- **Migration considerations:** none.
- **Risks:** medium — this is the largest single controller surface change; mitigate by testing each endpoint's contract table row individually rather than only end-to-end.
- **Commit boundary:** `feat(api): add retry/generation-status/revisions endpoints, remove in-place draft PATCH`

### Task 11 — Web: migrate to the new contract

- **Objective:** Update `web/` per [05 — Web Migration](05-web-migration.md) — idempotency key generation, `expectedRevisionId` tracking, resume/retry UI, conflict handling.
- **Files:** `web/src/lib/api/assessments.ts`, `web/src/hooks/useIntakeAssessmentPage.ts`, `web/src/hooks/useAssessmentDraftBuilderPage.ts`, associated components.
- **Dependencies:** Task 10 (API contract must exist first)
- **Steps:** Per [05](05-web-migration.md). Generate a UUID `Idempotency-Key` client-side per submit action; track `expectedRevisionId` from the last-fetched revision; add the new UI states.
- **Tests to write first:** Jest/RTL tests for each new state from [05](05-web-migration.md#newchanged-ui-states), written against the target behavior first.
- **Verification command:** `npm run lint && npm run test && npm run build` (in `web/`)
- **Acceptance criteria:** criterion #15 from [10](10-acceptance-criteria.md) (Web can resume a failed generation after refresh) demonstrated by a passing test that reproduces Research 02 §5.6.
- **Migration considerations:** none (Web has no persisted state to migrate).
- **Risks:** low-medium — mostly mechanical once the API contract (Task 10) is stable; risk is scope creep into visual redesign, explicitly out of bounds per [05](05-web-migration.md#out-of-scope-for-this-plan).
- **Commit boundary:** `feat(web): migrate authoring flow to idempotent, resumable, revision-aware contract`

### Task 12 — Database: legacy data backfill

- **Objective:** Execute the honest legacy migration from [06 — Database Migration](06-database-migration.md).
- **Files:** `api/src/main/resources/db/migration/V17__backfill_legacy_authoring_data.sql`
- **Dependencies:** Tasks 01–10 complete and deployed-compatible (schema and application code both ready; running the backfill before the app code exists is unnecessary risk, running it after means the target shape is proven correct first)
- **Steps:** Implement exactly the labeling rules from [06](06-database-migration.md#ai_operations-synthesis-for-legacy-rows) — every `assessment_drafts` row → `LEGACY_UNKNOWN`, `provenanceComplete = false`, `actorId = NULL`; every `agent_execution_logs` row → one `ai_operations` + one `agent_attempts` row with fields copied faithfully.
- **Tests to write first:** Migration test with fixture data mirroring realistic legacy rows (a single-version assessment, a multi-version regenerated assessment, a failed-generation-only assessment with no draft), asserting the exact labels above and zero fabricated fields, written before the migration script.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=LegacyAuthoringBackfillMigrationTest`, then full suite
- **Acceptance criteria:** row counts match (every legacy draft/log row produces exactly one migrated row); no `AI_GENERATED`/`HUMAN_EDITED` label appears anywhere in migrated data; `provenanceComplete = false` on every migrated revision.
- **Migration considerations:** this is the migration considerations — see [06](06-database-migration.md) in full. One-time, not repeatable without a reset (idempotent guard: the script should be safe to run twice without duplicating rows, e.g. `WHERE NOT EXISTS`, even though Flyway normally prevents re-running a migration — defense in depth for manual recovery scenarios).
- **Risks:** medium — the main risk is scale (large legacy table) rather than correctness, which is why the labeling rule was deliberately kept simple (unconditional `LEGACY_UNKNOWN`) rather than a more complex heuristic that would be harder to verify at migration time.
- **Commit boundary:** `feat(api): backfill legacy assessment_drafts/agent_execution_logs into revision/operation model`

### Task 13 — Cleanup: delete dead legacy code paths

- **Objective:** Remove `AssessmentDraft`, `AgentExecutionLog` domain classes, their adapters, and `DraftGenerationCoordinator` remnants — anything Task 07/08 didn't already delete.
- **Files:** grep for remaining references to the deleted classes across `api/src/main/java` and `api/src/test/java`; remove.
- **Dependencies:** Tasks 07, 08, 12
- **Steps:** Confirm zero remaining compile-time references (the compiler enforces this — if it compiles after deletion, nothing was missed). Mark `assessment_drafts`/`agent_execution_logs` tables as deprecated in [09-developer-guide/05-database-guide.md] (documentation only, table itself stays per [06](06-database-migration.md#legacy-compatibility-window)).
- **Tests to write first:** none new — this task's test is "the existing suite still passes with zero references to deleted classes."
- **Verification command:** `./mvnw -f api/pom.xml clean test`
- **Acceptance criteria:** clean compile, full suite green, `grep -r "AssessmentDraft\|AgentExecutionLog" api/src` returns no matches outside migration SQL comments/history.
- **Migration considerations:** none — code-only.
- **Risks:** low.
- **Commit boundary:** `refactor(api): remove superseded AssessmentDraft/AgentExecutionLog code paths`

### Task 14 — Full regression and documentation sync

- **Objective:** Run the complete baseline (per [Research 03](../../../design-system/research/research-03-baseline-technical-verification.md)'s command set) against the finished cut; update `docs/09-developer-guide/03-api-reference.md` and `05-database-guide.md` to reflect the new contract/schema.
- **Files:** `docs/09-developer-guide/03-api-reference.md`, `docs/09-developer-guide/05-database-guide.md`
- **Dependencies:** all prior tasks
- **Steps:** Re-run every command from Research 03 §2; update docs to describe the *new* endpoints/tables as current, and the retired `PATCH .../draft` as removed (not silently deleted from the docs with no trace — note it was retired and why, one line, linking to the ADR).
- **Tests to write first:** none — this is the verification task.
- **Verification command:** the full Research 03 command set: `./mvnw -f api/pom.xml test`, `./mvnw -f agents/pom.xml -Pdemo test`, `npm run lint && npm run test && npm run build` (in `web/`)
- **Acceptance criteria:** all 25 criteria in [10 — Acceptance Criteria](10-acceptance-criteria.md) checked off with a linked test or evidence.
- **Migration considerations:** none.
- **Risks:** low — this is verification, not new implementation.
- **Commit boundary:** `docs(developer-guide): sync API reference and database guide with authoring operation foundation`

---

← [07 — Testing Strategy](07-testing-strategy.md) | [↑ README](README.md) | [Siguiente: Risks and Rollback →](09-risks-and-rollback.md)
