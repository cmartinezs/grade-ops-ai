# ⚛️ TASK 07 — Draft generation endpoint (+ AgentExecutionLog)

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-03, task-05, task-06
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

`POST` endpoint that builds an `AssessmentCommand` from the persisted brief, calls `agents/` via `agentclient`, and persists the returned draft (version 1) together with its `AgentExecutionLog` — US-011.

---

## Technical Design

- **Approach:** this task also introduces the `AgentExecutionLog` entity/table, since it is first used here (a first-class evidence entity per `CLAUDE.md`, not folded into `AssessmentDraft`'s own columns beyond the FK — keeping it a separate table makes it reusable by future agents beyond Assessment). One use case (`GenerateAssessmentDraftHandler`): load the brief, call `agentclient`, persist the log row, persist the draft row (version 1) referencing the log — all in one `@Transactional` method *except* the outbound HTTP call itself, which must happen **outside** any open transaction (an external network call must never hold a DB transaction open).
- **Affected files / components:**
  - `src/main/resources/db/migration/V12__add_agent_execution_logs.sql` (new)
  - `assessment/domain/model/AgentExecutionLog.java` (new, pure domain)
  - `assessment/infrastructure/adapter/out/persistence/AgentExecutionLogJpaEntity.java`, `...JpaRepository.java`, `...PersistenceAdapter.java`, `...PersistenceMapper.java` (new)
  - `assessment/application/port/out/AgentExecutionLogRepositoryPort.java` (new)
  - `assessment/application/command/GenerateAssessmentDraftCommand.java`, `application/port/in/GenerateAssessmentDraftUseCase.java`, `application/usecase/GenerateAssessmentDraftHandler.java`, `application/result/GenerateAssessmentDraftResult.java` (new)
  - `assessment/infrastructure/adapter/in/web/AssessmentController.java` (**modify** — add `POST /api/v1/assessments/{id}/draft`)
  - request/response DTOs for the new endpoint
  - **Added 2026-07-13 (found during implementation):** `shared/application/security/OwnershipVerifier.java` (**moved** from `shared/infrastructure/config/security/`) — this endpoint operates on a specific assessment by path `{id}`, so the authenticated teacher must be verified as its owner (404, not 403, on mismatch — don't leak existence) before generating a draft; otherwise any authenticated teacher could trigger a (billed) generation on another teacher's assessment. A scaffolded, unused `OwnershipVerifier` bean already existed for exactly this purpose but lived under `..infrastructure..`, which `GenerateAssessmentDraftHandler` (application layer) cannot import without violating `HexagonalArchitectureTest`'s `application_has_no_infrastructure_imports` rule — moved to `shared/application/security/` (a pure POJO utility, no Spring/JPA deps, so the move is purely a package fix) and `SecurityConfig.java`'s bean declaration updated to match. `AgentExecutionLogPersistenceAdapterIntegrationTest.java` (new, Testcontainers) was also added beyond the originally listed files, matching task-01/02/03's convention of one round-trip integration test per persistence adapter.
- **Interfaces / contracts:** `POST /api/v1/assessments/{id}/draft` (no request body — brief is already persisted) → 201 with the generated draft (title, context, instructions, objectives, deliverables, constraints, version=1). `AgentExecutionLog` fields (updated 2026-07-12 to match `agents/`'s actual `AgentExecutionLogPayload`, verified against source — see this story's Inconsistency #3): `id`, `assessmentId`, `draftId` (nullable until the draft row exists — see design notes), `agentExecutionId` (UUID from `agents/`, for cross-service correlation), `agentName`, `provider` (the requested provider — `agents/` doesn't echo this back, so persist the value `api/` sent, see Residual #1), `model`, `promptVersion`, `inputHash`, `outputHash`, `estimatedInputTokens`, `estimatedOutputTokens`, `costEstimate`, `status` (`COMPLETED`/`FAILED`), `errorCode` (`INVALID_COMMAND`/`MALFORMED_OUTPUT`, nullable — **separate column from `status`, not the same field**), `startedAt`, `finishedAt`.
- **Risk:** M — the "call outside transaction, persist after" ordering is easy to get backwards; getting it wrong either holds a DB connection open for the duration of a slow Gemini call (connection-pool exhaustion risk) or persists a log for a call that hasn't actually completed. Mitigated by an explicit implementation-step ordering below and a dedicated test asserting no transaction is open during the `agentclient` call.
- **Design notes:** insertion order within the (post-call) transaction: save the draft row first (without `agent_execution_log_id` if a chicken-and-egg FK issue arises — alternative: save the log first without `draft_id`, then the draft with the log's id, then back-fill the log's `draft_id` in a second update within the same transaction). Decide and document the exact order during implementation; either is acceptable as long as both rows end up consistently cross-referenced and the whole sequence is atomic. **Implemented 2026-07-13:** chose the second ordering — save the log first (`draft_id = null`), save the draft referencing the log's id, then re-save the log with `draft_id` back-filled (`AgentExecutionLogRepositoryPort.save` is a natural JPA upsert-by-id, unlike `AssessmentDraftRepositoryPort.save`, which is insert-only per task-03). `V12` also adds the FK constraint on `assessment_drafts.agent_execution_log_id` (a bare column with no FK since `V11`, since `agent_execution_logs` didn't exist yet) — completing that deferred reference now that the table exists, within the already-listed `V12` file.
- **Corrected 2026-07-13 (reality check before implementation) — transaction boundary:** "one `@Transactional` method" for the persist step cannot literally be a second method on `GenerateAssessmentDraftHandler` itself called from `execute()` — Spring's proxy-based AOP does not intercept self-invocation, so a `@Transactional`-annotated sibling method called via `this.` would silently run with no transaction at all, defeating the entire point of this task's Risk callout. Implemented instead with a programmatic `TransactionTemplate` (backed by the auto-configured `PlatformTransactionManager`): the agent call runs with zero transaction-manager involvement, and the persist step opens a real transaction via `TransactionTemplate.execute(...)`/`executeWithoutResult(...)`, regardless of self-invocation. No extra Spring bean or file was needed beyond what's already listed.
- **Corrected 2026-07-13 (reality check before implementation) — error code granularity:** `agentclient` (task-05, already merged) does not parse `agents/`'s JSON error body (`AgentErrorResponse.errorCode`, e.g. `INVALID_COMMAND`/`MALFORMED_OUTPUT`) — it only exposes its own three-value `AgentClientException.Reason` (`UNREACHABLE`/`AGENT_REJECTED`/`AGENT_ERROR`). The failed `AgentExecutionLog.errorCode` persisted here is `ex.reason().name()` — the only reason code actually available to `api/` — not `agents/`'s more granular reason. Extending `agentclient` to surface the granular code is out of scope for this task (would require reopening task-05's already-reviewed contract).
- **Bug found via the DB/ORM Consistency Check smoke test, fixed 2026-07-13:** `V12` originally declared `cost_estimate NUMERIC`. `AgentExecutionLogJpaEntity.costEstimate` is `Double`, which Hibernate maps to `float(53)` (`Types#FLOAT`/`DOUBLE`) by default — a real `spring-boot:run` startup with `ddl-auto=validate` failed with `SchemaManagementException: wrong column type encountered in column [cost_estimate] ... found [numeric], but expecting [float(53)]`. None of the automated tests caught this beforehand: the unit-level adapter test mocks the JPA repository (never touches Hibernate schema validation), and the sibling `@DataJpaTest` integration tests in this story all run with `ddl-auto=none`, which never validates declared column types against entity metadata — a `Double` inserts into a `NUMERIC` column fine at the JDBC level regardless. Fixed by changing `V12` to `cost_estimate DOUBLE PRECISION`, matching Hibernate's default mapping. Added `AgentExecutionLogPersistenceAdapterIntegrationTest` with `ddl-auto=validate` (deliberately not `none`, unlike its siblings) specifically so this class of mismatch fails automatically going forward instead of only surfacing via a manual smoke test.

---

## Implementation Steps

1. Create `V12__add_agent_execution_logs.sql`: `agent_execution_logs` table — `id UUID PRIMARY KEY`, `assessment_id UUID NOT NULL REFERENCES assessments(id)`, `draft_id UUID REFERENCES assessment_drafts(id)`, `agent_execution_id UUID`, `agent_name VARCHAR`, `provider VARCHAR`, `model VARCHAR`, `prompt_version VARCHAR`, `input_hash VARCHAR`, `output_hash VARCHAR`, `estimated_input_tokens INTEGER`, `estimated_output_tokens INTEGER`, `cost_estimate NUMERIC`, `status VARCHAR NOT NULL`, `error_code VARCHAR`, `started_at TIMESTAMPTZ NOT NULL`, `finished_at TIMESTAMPTZ NOT NULL`. (Column set expanded 2026-07-12 to match `agents/`'s actual `AgentExecutionLogPayload` — see this story's Inconsistency #3; `status` and `error_code` are separate columns.)
2. Create `AgentExecutionLog.java` (domain) and its full persistence stack (entity, repo, adapter, mapper, port) mirroring task-01's pattern.
3. Create `GenerateAssessmentDraftHandler.java`:
   - Load the `AssessmentBrief` for the given assessment id (404 if not found).
   - Build `agentclient.AssessmentCommand` from the brief (no `adjustmentNotes`/`previousDraftId` — this is initial generation).
   - Call `AssessmentAgentClient.generate(...)` **outside** any `@Transactional` boundary.
   - On success, open a transaction: persist the `AgentExecutionLog` with the full field set from step 1 (including `provider` from the outgoing command, since `agents/` doesn't echo it back), persist the `AssessmentDraft` (version 1, `previousVersionId = null`), cross-reference both per the design-notes decision.
   - On `AgentClientException`, persist a failed `AgentExecutionLog` — `status = "FAILED"`, `error_code` set to the failure's reason code (`AgentClientException`'s own reason, or `AssessmentAgentException.Reason` name if surfaced) — without a draft row, and propagate a clean error to the controller (422/502 depending on the failure type).
4. Add `POST /api/v1/assessments/{id}/draft` to `AssessmentController.java`.
5. Create request/response DTOs.
6. Wire the new handler and repositories in `AssessmentConfig.java`.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Successful generation persists both draft (v1) and a success `AgentExecutionLog` | Integration test with a stubbed `agentclient` (mock `AssessmentAgentClient`) returning a valid response |
| 2 | Failed agent call persists a failure log, no draft row, and returns a clean error | Integration test with a stubbed `agentclient` throwing `AgentClientException` |
| 3 | No open DB transaction spans the `agentclient` call | Test/manual verification: assert the call happens before `@Transactional` entry (code inspection + a test that fails if the call is moved inside the transactional method, e.g. via a Mockito verify ordering check) |
| 4 | `./mvnw test` passes | Full suite green |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up db`; for a full end-to-end check, `agents/` running locally too (`./mvnw -Pbeta spring-boot:run` in the `agents/` worktree) |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors |
| 3 | Connectivity or schema validation succeeds | `V12__add_agent_execution_logs.sql` applies cleanly |
| 4 | Changed surface responds correctly | With both services running locally: `curl -X POST localhost:8080/api/v1/assessments/{id}/draft -H "Authorization: Bearer <token>"` returns 201 with a real generated draft — first true end-to-end check of the whole `web-would-call → api → agents → Gemini` chain |
| 5 | No startup or migration regressions are visible | App logs clean |

### Database / ORM Consistency Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Static database-to-ORM consistency is valid | `agent_execution_logs` columns match `AgentExecutionLogJpaEntity` fields; FK to `assessments`/`assessment_drafts` correct |
| 2 | Local runtime environment starts | `docker compose up db` then `./mvnw spring-boot:run -Dspring.profiles.active=local` |
| 3 | Persistence smoke check passes | Full generate-and-persist flow (step 4 above) leaves consistent, cross-referenced draft + log rows |

---

## Done Criteria

- [x] `POST /api/v1/assessments/{id}/draft` generates and persists a version-1 draft plus its `AgentExecutionLog` — with the full field set (`agentExecutionId`, `agentName`, `provider`, `model`, `promptVersion`, `inputHash`, `outputHash`, `estimatedInputTokens`, `estimatedOutputTokens`, `costEstimate`, `status`, `errorCode`, `startedAt`, `finishedAt`), not just model/cost/status.
- [x] The `agentclient` call happens outside any open DB transaction.
- [x] A failed agent call persists a failure log (no draft row) with `status="FAILED"` and `errorCode` set to the specific reason — not just a generic status string — and returns a clean, non-500 error.
- [x] `./mvnw test` passes.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
