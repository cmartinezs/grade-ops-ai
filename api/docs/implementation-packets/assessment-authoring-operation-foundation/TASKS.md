<a id="top"></a>

# TASKS — API: Assessment Authoring Operation Foundation

**Status:** Ready. **Parent:** [README](README.md) · **Prev:** [CLAUDE-IMPLEMENTATION-PROMPT](CLAUDE-IMPLEMENTATION-PROMPT.md) · **Next:** [LOCAL-CONTRACTS →](LOCAL-CONTRACTS.md)

Twelve tasks, executed in this order. Reproduced from [08 — Implementation Sequence](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/08-implementation-sequence.md), scoped to `api/`'s ownership per [04 — Task Distribution](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/04-task-distribution.md). If this file and the plan's own 08-implementation-sequence.md ever disagree, the plan wins.

---

### Task 01 — Schema: durable AI operation tables

- **Objective:** Add `ai_operations` and `agent_attempts` tables, additive, unused by any application code yet.
- **Files:** `api/src/main/resources/db/migration/V13__add_agent_attempts_and_ai_operations.sql`
- **Dependencies:** none
- **Steps:** Write the migration per [LOCAL-CONTRACTS.md § Persistence changes](LOCAL-CONTRACTS.md#persistence-changes). Include the partial unique index `uq_ai_operations_in_flight`.
- **Tests to write first:** none (schema-only). After: a Testcontainers test asserting the migration applies cleanly to an empty DB and the partial unique index rejects a second `PENDING` row for the same `(assessment_id, operation_type)`.
- **Verification command:** `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** Flyway applies V13 cleanly alongside V1–V12; the partial unique index behaves as specified.
- **Risks:** Low. Rollback: `DROP TABLE agent_attempts, ai_operations;`.
- **Commit boundary:** `feat(api): add durable ai_operations/agent_attempts schema`

### Task 02 — Schema: assessment revisions and current-revision pointer

- **Objective:** Add `assessment_revisions` table and `assessments.current_revision_id`/`lock_version` columns.
- **Files:** `api/src/main/resources/db/migration/V14__add_assessment_revisions.sql`, `V15__add_assessment_current_revision.sql`
- **Dependencies:** Task 01 (FK to `agent_attempts`)
- **Steps:** Per [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md). `lock_version` defaults `0 NOT NULL`.
- **Tests to write first:** none; after: migration test extended for V14/V15, plus a direct-SQL test proving `UNIQUE (assessment_id, version_number)` and the self-referential FK exist.
- **Verification command:** `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** V14/V15 apply cleanly after V13; existing `assessments` rows get `lock_version = 0`, `current_revision_id = NULL`.
- **Risks:** Low. Rollback: `ALTER TABLE assessments DROP COLUMN current_revision_id, DROP COLUMN lock_version; DROP TABLE assessment_revisions;`.
- **Commit boundary:** `feat(api): add assessment_revisions schema and current-revision pointer`

### Task 03 — Schema: idempotency records

- **Objective:** Add `idempotency_records` table.
- **Files:** `api/src/main/resources/db/migration/V16__add_idempotency_records.sql`
- **Dependencies:** Task 02 (FK to `assessments`)
- **Steps:** Per [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md).
- **Tests to write first:** none; after: unique-constraint direct-SQL test.
- **Verification command:** `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** V16 applies cleanly; unique constraint rejects a duplicate `(scope_type, teacher_uid, assessment_id, operation_type, idempotency_key)`.
- **Risks:** Low. Rollback: `DROP TABLE idempotency_records;`.
- **Commit boundary:** `feat(api): add idempotency_records schema`

### Task 04 — Domain: `AssessmentRevision` aggregate + persistence

- **Objective:** Introduce `AssessmentRevision` domain class, JPA entity, repository port + adapter — parallel to `AssessmentDraft`, not yet wired into any use case.
- **Files:** `.../assessment/domain/model/AssessmentRevision.java`, `RevisionOrigin.java`; `.../application/port/out/AssessmentRevisionRepositoryPort.java`; `.../infrastructure/adapter/out/persistence/AssessmentRevisionJpaEntity.java`, `AssessmentRevisionPersistenceAdapter.java`
- **Dependencies:** Task 02
- **Steps:** Factories `generateFromAi`/`regenerateFromAi`/`createFromHumanEdit`/`restore`, no `applyEdit`. Full field list in [LOCAL-CONTRACTS.md § `AssessmentRevision`](LOCAL-CONTRACTS.md#assessmentrevision).
- **Tests to write first:** Unit tests for chain-integrity invariants (version 1 has no predecessor; N>1 requires same-assessment predecessor at N-1) and immutability, before the class exists — TDD.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AssessmentRevisionTest`, then full `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** All invariants in [LOCAL-CONTRACTS.md § Invariants](LOCAL-CONTRACTS.md#invariants) hold under unit test; a Testcontainers round-trip test (`AssessmentRevisionPersistenceAdapterTest`) proves save/load fidelity.
- **Risks:** Low — purely additive, unreachable from any endpoint yet.
- **Commit boundary:** `feat(api): add AssessmentRevision aggregate and persistence`

### Task 05 — Domain: `AiOperation`/`AgentAttempt` aggregates + persistence

- **Objective:** Introduce both new aggregates, ports, and adapters — not yet wired into any use case.
- **Files:** `.../domain/model/AiOperation.java`, `AiOperationType.java`, `AiOperationStatus.java`, `AgentAttempt.java`, `AgentAttemptStatus.java`; ports + adapters mirroring Task 04's pattern.
- **Dependencies:** Task 01
- **Steps:** Per [LOCAL-CONTRACTS.md § `AiOperation`/`AgentAttempt`](LOCAL-CONTRACTS.md#aioperation). Include state-transition guards (e.g., cannot transition `SUCCEEDED` → anything).
- **Tests to write first:** Unit tests for valid/invalid state transitions on both aggregates, written first.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AiOperationTest,AgentAttemptTest`, then full suite
- **Acceptance criteria:** Invalid transitions throw `DomainInvariantViolationException`; Testcontainers round-trip tests pass; `uq_ai_operations_in_flight` exercised by an adapter-level test attempting two in-flight rows.
- **Risks:** Low.
- **Commit boundary:** `feat(api): add AiOperation and AgentAttempt aggregates and persistence`

### Task 06 — Application: idempotency guard service

- **Objective:** A reusable, generic idempotency-check/record service usable by any command handler.
- **Files:** `.../application/security/IdempotencyGuard.java` (or `.../shared/application/idempotency/`), `.../port/out/IdempotencyRepositoryPort.java`, adapter.
- **Dependencies:** Task 03
- **Steps:** `check(scope, operationType, key, payloadHash) → Optional<PriorResult>`; `record(scope, ..., resultReference, status)`. Hash over validated command fields — see [LOCAL-CONTRACTS.md § Idempotency](LOCAL-CONTRACTS.md#idempotency).
- **Tests to write first:** Unit tests for the check/record contract before implementation — no record → proceed; same hash → replay; different hash → conflict.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=IdempotencyGuardTest`, then full suite
- **Acceptance criteria:** Matches the three-way contract exactly; not yet wired into any endpoint.
- **Risks:** Low.
- **Commit boundary:** `feat(api): add reusable idempotency guard service`

### Task 07B — Application: rewrite the generation coordinator (the pivot task)

**Depends on Task 07A landing in `agents/` first** (see [root packet § Task 07 split](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/04-task-distribution.md#task-07-split)) — you need to know the exact field name/shape for the resolved provider before writing deserialization code. If Session B (Agents) has not finished when you reach this task, read `agents/src/main/java/.../assessment/application/dto/` **read-only** to verify the current response shape yourself rather than blocking indefinitely; record which path you took in `HANDOFF.md`.

- **Objective:** Replace `DraftGenerationCoordinator` with the three-phase, `AiOperation`/`AgentAttempt`/`AssessmentRevision`-aware coordinator. Wire `GenerateAssessmentDraftHandler` to it. **This is the task where the write path actually changes** — every prior task was additive and inert.
- **Files:** New `AiOperationCoordinator.java` replacing `DraftGenerationCoordinator.java`; `GenerateAssessmentDraftHandler.java` updated; `GlobalExceptionHandler` updated for the typed error codes in [LOCAL-CONTRACTS.md § Failure codes](LOCAL-CONTRACTS.md#canonical-failure-code-taxonomy); `AssessmentAgentResponse.Log`/`AgentExecutionLogPayload` deserialization updated for the (possibly new) `provider` field.
- **Dependencies:** Tasks 04, 05, 06
- **Steps:** Implement the Phase 0 / dispatch / Phase 2 sequence exactly per [LOCAL-CONTRACTS.md § Sequencing](LOCAL-CONTRACTS.md#sequencing). `GenerateAssessmentDraftHandler` now: checks idempotency (Task 06) → checks `ALREADY_GENERATED` precondition → delegates to the new coordinator → maps typed failures to the response shape in [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md).
- **Tests to write first:** (a) the failure-recovery test proving an `AgentAttempt` survives a simulated Phase-2 crash after Phase-0 commit — must fail against the *old* coordinator, pass against the new one; (b) `ALREADY_GENERATED` precondition test; (c) idempotency replay test for this endpoint.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AiOperationCoordinatorTest,GenerateAssessmentDraftHandlerIntegrationTest`, then full suite — must not regress the 289-test baseline.
- **Acceptance criteria:** Criteria #9, #10, #11 from [10 — Acceptance Criteria](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md); old `DraftGenerationCoordinatorTest`/`DraftGenerationCoordinator.java` deleted, not left dangling.
- **Risks:** **Highest-risk task in this packet.** An assessment created before this deploy has no `currentRevisionId`; the `ALREADY_GENERATED` check must treat that as "not generated yet," not crash. Mitigation: an explicit test with an `Assessment` restored via the pre-migration shape going through the new generate path successfully.
- **Commit boundary:** `feat(api): switch initial generation to durable AiOperation/AgentAttempt coordinator` (implementation + its own tests in one commit).

### Task 08 — Application: human edit creates a revision

- **Objective:** Replace `UpdateAssessmentDraftHandler`'s in-place edit with `CreateHumanRevisionHandler`, CAS-protected.
- **Files:** Delete `UpdateAssessmentDraftCommand`/`UpdateAssessmentDraftUseCase`/`UpdateAssessmentDraftHandler`; add `CreateHumanRevisionCommand`/`CreateHumanRevisionUseCase`/`CreateHumanRevisionHandler`.
- **Dependencies:** Task 04
- **Steps:** Load `Assessment`, verify ownership, compare `expectedRevisionId` to `currentRevisionId` (mismatch → `STALE_REVISION`), create `AssessmentRevision.createFromHumanEdit(...)`, CAS-update `Assessment.currentRevisionId`/`lockVersion` in one transaction.
- **Tests to write first:** (a) the test proving the *old* "edit preserves version/id" assertion is now false — confirm it fails against old code / passes against new; (b) `STALE_REVISION` conflict test; (c) concurrency test (two concurrent edits, exactly one succeeds).
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=CreateHumanRevisionHandlerIntegrationTest`, then full suite
- **Acceptance criteria:** Criteria #1, #2, #3, #5 from [10](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md).
- **Risks:** Medium — must delete, not deprecate-and-ignore, the old handler and its now-contradictory test.
- **Commit boundary:** `feat(api): human edits create immutable revisions instead of mutating in place`

### Task 09 — Application: regenerate requires expected revision

- **Objective:** Update `RegenerateAssessmentDraftHandler` to require `expectedRevisionId`, persist `adjustmentNotes` as `reason`, and route through the Task 07B coordinator.
- **Files:** `RegenerateAssessmentDraftCommand.java`, `RegenerateAssessmentDraftHandler.java`, `RegenerateAssessmentDraftRequest.java`
- **Dependencies:** Tasks 04, 07B
- **Steps:** Add `expectedRevisionId` to command/request. Pre-dispatch CAS check before calling the coordinator.
- **Tests to write first:** `STALE_REVISION` pre-dispatch test (assert **zero** invocations reach the agent client mock when stale); `reason` persistence test; concurrency test (two regenerations, one succeeds).
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=RegenerateAssessmentDraftHandlerIntegrationTest`, then full suite
- **Acceptance criteria:** Criteria #6, #7, #8 from [10](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md).
- **Risks:** Medium — the pre-dispatch check must genuinely happen before Phase 1 (agent call), verified by asserting zero invocations on the mocked agent-client port, not merely the final response code.
- **Commit boundary:** `feat(api): regenerate requires expectedRevisionId and persists adjustment reason`

### Task 10 — Application + API: retry and generation-status

- **Objective:** New `RetryGenerationHandler` + `GetGenerationStatusHandler`, wired to new controller endpoints.
- **Files:** New use cases; `AssessmentController.java` gains `POST .../draft/retry`, `GET .../generation-status`; `PATCH .../draft` **removed**; `POST .../revisions` added (wraps Task 08's handler).
- **Dependencies:** Tasks 05, 07B, 08, 09
- **Steps:** Per [LOCAL-CONTRACTS.md § API ↔ Web public contract](LOCAL-CONTRACTS.md#api--web-public-contract) exactly — status codes, typed error bodies, the `INDETERMINATE` classification.
- **Tests to write first:** API acceptance tests per endpoint (happy path + every typed error), written against the target contract before wiring the controller.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AssessmentControllerTest`, then full suite
- **Acceptance criteria:** Every row of the contract table in [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) exercised by at least one passing test; `PATCH .../draft` returns `404`/`405`, verified explicitly.
- **Risks:** Medium — largest single controller-surface change; test each endpoint's contract row individually, not only end-to-end.
- **Commit boundary:** `feat(api): add retry/generation-status/revisions endpoints, remove in-place draft PATCH`

**Coordination note:** Do not deploy/merge this task's endpoint removal (`PATCH .../draft`) to `develop` in isolation ahead of Session C's replacement call — Session D coordinates the actual cutover timing. Your job here is to have the branch ready and correct; the deploy-ordering call belongs to [09 — Session Handoff Protocol](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/09-session-handoff-protocol.md).

### Task 12 — Database: legacy data backfill

- **Objective:** Execute the honest legacy migration.
- **Files:** `api/src/main/resources/db/migration/V17__backfill_legacy_authoring_data.sql`
- **Dependencies:** Tasks 01–10 complete
- **Steps:** Every `assessment_drafts` row → `LEGACY_UNKNOWN`, `provenanceComplete = false`, `actorId = NULL`, unconditionally. Every `agent_execution_logs` row → one `ai_operations` + one `agent_attempts` row with fields copied faithfully. Full reasoning in [LOCAL-CONTRACTS.md § Legacy migration](LOCAL-CONTRACTS.md#legacy-migration).
- **Tests to write first:** Migration test with fixture data (single-version assessment, multi-version regenerated assessment, failed-generation-only assessment), asserting the exact labels above and zero fabricated fields, written before the migration script.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=LegacyAuthoringBackfillMigrationTest`, then full suite
- **Acceptance criteria:** Row counts match; no `AI_GENERATED`/`HUMAN_EDITED` label appears anywhere in migrated data; `provenanceComplete = false` on every migrated revision.
- **Risks:** Medium — main risk is scale on a large legacy table, not correctness (labeling rule is deliberately simple). Script must be safe to run twice (`WHERE NOT EXISTS` guard).
- **Commit boundary:** `feat(api): backfill legacy assessment_drafts/agent_execution_logs into revision/operation model`

### Task 13 — Cleanup: delete dead legacy code paths

- **Objective:** Remove `AssessmentDraft`, `AgentExecutionLog` domain classes, their adapters, and any `DraftGenerationCoordinator` remnants.
- **Files:** grep for remaining references across `api/src/main/java` and `api/src/test/java`; remove.
- **Dependencies:** Tasks 07B, 08, 12
- **Steps:** Confirm zero remaining compile-time references. Note the deprecated tables in `docs/09-developer-guide/05-database-guide.md` (documentation only — the tables themselves stay).
- **Tests to write first:** none new — the existing suite passing with zero references to deleted classes is this task's test.
- **Verification command:** `./mvnw -f api/pom.xml clean test`
- **Acceptance criteria:** Clean compile, full suite green, `grep -r "AssessmentDraft\|AgentExecutionLog" api/src` returns no matches outside migration SQL comments/history.
- **Risks:** Low.
- **Commit boundary:** `refactor(api): remove superseded AssessmentDraft/AgentExecutionLog code paths`

---

**Not owned by this packet:** Task 11 (Web) — [`web/docs/implementation-packets/assessment-authoring-operation-foundation/`](../../../../web/docs/implementation-packets/assessment-authoring-operation-foundation/README.md). Task 14 (full regression + docs sync) — [FINAL-INTEGRATION-PROMPT.md](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/FINAL-INTEGRATION-PROMPT.md).

---

← [CLAUDE-IMPLEMENTATION-PROMPT](CLAUDE-IMPLEMENTATION-PROMPT.md) | [↑ inicio](#top) | [Siguiente: LOCAL-CONTRACTS →](LOCAL-CONTRACTS.md)
