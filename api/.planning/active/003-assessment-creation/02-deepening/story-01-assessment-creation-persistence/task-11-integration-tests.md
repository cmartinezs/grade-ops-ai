# ⚛️ TASK 11 — End-to-end integration tests

> **Status:** IN PROGRESS
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-06, task-07, task-08, task-09, task-10
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

Comprehensive integration test suite covering the full assessment-creation flow end-to-end: persist-before-agent-call ordering, versioning (no overwrite), `AgentExecutionLog` correctness, and edit-does-not-create-a-new-version — the story's own Done Criteria, verified together rather than only piecemeal per-task.

---

## Technical Design

- **Approach:** each task (06–10) already has its own focused tests; this task adds the cross-cutting scenarios that only make sense once the full flow exists together (e.g. "brief → generate → regenerate → edit → list → retrieve", asserting consistency at every step). Use Testcontainers-Postgres (already a project dependency) for a real-DB integration test class, with a stubbed/mocked `AssessmentAgentClient` (no real Gemini/agents call in CI).
- **Affected files / components:** `AssessmentCreationFlowIntegrationTest.java` (new, under `src/test/java/cl/gradeops/ai/api/assessment/`).
- **Interfaces / contracts:** none new — test code only.
- **Risk:** Low — verification-only task.
- **Design notes:** this test class complements, not replaces, each task's own unit/integration tests — do not duplicate task-06 through task-10's individual test scenarios here, only the cross-task flow assertions.

---

## Implementation Steps

1. Create `AssessmentCreationFlowIntegrationTest.java` with a Testcontainers-Postgres base (matching the project's existing Testcontainers test pattern).
2. Test: full happy path — brief intake → draft generation → regeneration → edit → list (dashboard) → retrieve current → retrieve version history — assert every step's state is consistent with the previous one.
3. Test: agent-call failure during generation leaves the brief intact and no draft/log-success row (only a failure log), and the assessment remains queryable.
4. Test: two regenerations in a row produce 3 distinct draft versions (1 initial + 2 regenerations), all retrievable, none overwritten.
5. Test: an edit after a regeneration updates only the latest version, not any earlier one.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Full happy-path flow passes | `./mvnw test -Dtest=AssessmentCreationFlowIntegrationTest` |
| 2 | Agent failure scenario leaves consistent state | Same test class, failure scenario |
| 3 | Multi-regeneration scenario preserves all versions | Same test class |
| 4 | `./mvnw test` (full suite, all tasks) passes | Full suite green |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | Testcontainers manages its own Postgres instance automatically |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors |
| 3 | Connectivity or schema validation succeeds | All migrations (`V9`–`V12`) apply cleanly in the Testcontainers environment |
| 4 | Changed surface responds correctly | Full flow test (step 2 above) passes |
| 5 | No startup or migration regressions are visible | Full `./mvnw test` run is clean |

### Database / ORM Consistency Check

N/A — no schema change in this task; exercises the full schema from prior tasks.

---

## Done Criteria

- [x] `AssessmentCreationFlowIntegrationTest` covers: happy path, agent-failure resilience, multi-regeneration version integrity, edit-scoped-to-latest-version.
- [x] Full `./mvnw test` suite passes (all 11 tasks combined) — 288 tests, 0 failures, 0 errors.
- [x] Every Done Criterion listed in `story-01-assessment-creation-persistence.md` is verifiable by an existing automated test or documented manual check — see mapping below.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

### Story Done Criteria → verification mapping

| Story Done Criterion | Verified by |
|---|---|
| Brief persisted before agent invoked, separate step, agent failure never loses input | task-06 unit tests; this task's `agentFailureDuringGenerationLeavesBriefIntactWithOnlyAFailureLogAndNoDraft` |
| Persisted brief and draft retrievable after a page refresh | task-10 retrieval tests; this task's flow test (`entityManager.flush()/clear()` between every step forces a real re-read) |
| Draft generation calls `agents/` exclusively through `agentclient`; no other module imports Spring AI | `HexagonalArchitectureTest` (ArchUnit, part of `./mvnw test`) |
| Draft generation persists full structured result + full 13-field `AgentExecutionLog` | task-07's `AgentExecutionLogTest`, `GenerateAssessmentDraftHandlerIntegrationTest` |
| Regeneration creates a new version without deleting/overwriting the previous; previous remains retrievable | task-08 tests; this task's `twoRegenerationsInARowProduceThreeDistinctRetrievableVersions` |
| Each regeneration produces its own distinct `AgentExecutionLog` row | task-08's `RegenerateAssessmentDraftHandlerIntegrationTest`; this task's multi-regeneration test |
| Draft editing persists without a new agent call or new `AgentExecutionLog` | task-09's `UpdateAssessmentDraftHandlerIntegrationTest`; this task's `editAfterRegenerationUpdatesOnlyTheLatestVersion` |
| Gemini API key never exposed to the frontend | True by construction — no Gemini/Vertex credential exists anywhere in `api/`'s codebase or config; `api/` only calls the internal `agents/` endpoint via `agentclient`. Documented manual check, not a runtime-testable assertion. |
| `./mvnw test` passes | Confirmed — 288/288, 0 failures |
| `TRACEABILITY.md` updated with new terms from this story | Already current through task-10 (`.planning/active/003-assessment-creation/TRACEABILITY.md`); this task introduces no new domain term (test-only), so no new row was needed |

---

> [← story file](../story-01-assessment-creation-persistence.md)
