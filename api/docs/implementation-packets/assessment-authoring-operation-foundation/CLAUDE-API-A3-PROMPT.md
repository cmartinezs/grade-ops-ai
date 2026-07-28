<a id="top"></a>

# CLAUDE-API-A3-PROMPT — Authoring Mutations and Public API

**Status:** Ready. **Parent:** [README](README.md) · **Session:** A3 of 4 (API) · **Prev session:** [CLAUDE-API-A2-PROMPT.md](CLAUDE-API-A2-PROMPT.md) · **Next session:** [CLAUDE-API-A4-PROMPT.md](CLAUDE-API-A4-PROMPT.md)

Give this file, verbatim, as the task prompt to a **fresh** session that opens **only `api/`**. It is complete and self-contained.

## Repository and workspace identity

```text
Repository: cmartinezs/grade-ops-ai
Workspace: api/ (Spring Boot 4 + Java 21 + PostgreSQL, Maven)
Branch: feat/assessment-authoring-operation-foundation-api  (continuing, not new)
Session: A3 — Authoring Mutations and Public API (third of four sequential, recoverable API sessions)
```

## Who you are and what you are building

You are Session A3. Sessions A1 (schema/inert domain) and A2 (idempotency guard + durable coordinator pivot) are both complete — initial generation is now durable and idempotent. This session extends the same discipline to the remaining mutations (human edit, regenerate) and builds the public-facing endpoint surface Web needs: retry, generation status, revision creation, revision history. **By the end of this session, the API contract Web depends on is complete and ready to consume** — this is the handoff point that unblocks Session C (Web)'s final, non-mocked verification.

**This is documentation-implementing work.** You write real Java, real tests, no new migrations.

## Prerequisites gate

```bash
git fetch origin
git switch feat/assessment-authoring-operation-foundation-api
git pull --ff-only
git log --oneline -10
./mvnw -f api/pom.xml clean test
```

Confirm the branch's last commit is `docs(api): record session A2 handoff` (or later), [API-A2-HANDOFF.md](API-A2-HANDOFF.md) exists with no unresolved critical blocker, and the baseline you just ran is genuinely green — do not trust A2's recorded test count without re-running it yourself. If any of this fails, stop and report it as a blocker rather than re-deriving A2's work.

## Authority — read in this order before writing any code

1. [Authoring Operation Contract](../../../../docs/99-decisions/2026-07-28-authoring-operation-contract.md)
2. [Idempotency and Concurrency Strategy](../../../../docs/99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md)
3. [03 — Operation and API Contracts](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/03-operation-and-api-contracts.md) of the plan
4. This packet's [TASKS.md](TASKS.md) (Tasks 08, 09, 10 specifically), [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md), [API-A2-HANDOFF.md](API-A2-HANDOFF.md)

If anything here contradicts a document above it, the higher document wins — report the discrepancy (see [Handling discoveries](#handling-discoveries)). Do not modify approved contracts, taxonomies, field names, endpoint shapes, or the task list itself.

## Git — continuing on the same branch

```bash
git fetch origin
git switch feat/assessment-authoring-operation-foundation-api
git pull --ff-only
```

Do **not** create a new branch. Do not force-push, rebase destructively, or amend a commit Session A1/A2 already pushed.

## Scope — exactly these three tasks, in order

### Task 08 — Application: human edit creates a revision

- **Objective:** Replace `UpdateAssessmentDraftHandler`'s in-place edit with `CreateHumanRevisionHandler`, CAS-protected.
- **Files:** Delete `UpdateAssessmentDraftCommand`/`UpdateAssessmentDraftUseCase`/`UpdateAssessmentDraftHandler`; add `CreateHumanRevisionCommand`/`CreateHumanRevisionUseCase`/`CreateHumanRevisionHandler`.
- **Dependencies:** Task 04 (Session A1, already done)
- **Steps:** Load `Assessment`, verify ownership, compare `expectedRevisionId` to `currentRevisionId` (mismatch → `STALE_REVISION`), create `AssessmentRevision.createFromHumanEdit(...)`, CAS-update `Assessment.currentRevisionId`/`lockVersion` in one transaction.
- **Tests to write first:** (a) the test proving the *old* "edit preserves version/id" assertion is now false — confirm it fails against old code / passes against new; (b) `STALE_REVISION` conflict test; (c) concurrency test (two concurrent edits, exactly one succeeds).
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=CreateHumanRevisionHandlerIntegrationTest`, then full suite
- **Acceptance criteria:** Criteria #1, #2, #3, #5 from [10](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md).
- **Risks:** Medium — must delete, not deprecate-and-ignore, the old handler and its now-contradictory test.
- **Commit boundary:** `feat(api): human edits create immutable revisions instead of mutating in place`

### Task 09 — Application: regenerate requires expected revision

- **Objective:** Update `RegenerateAssessmentDraftHandler` to require `expectedRevisionId`, persist `adjustmentNotes` as `reason`, and route through Session A2's `AiOperationCoordinator`.
- **Files:** `RegenerateAssessmentDraftCommand.java`, `RegenerateAssessmentDraftHandler.java`, `RegenerateAssessmentDraftRequest.java`
- **Dependencies:** Task 04 (Session A1), Task 07B (Session A2, already done)
- **Steps:** Add `expectedRevisionId` to command/request. Pre-dispatch CAS check before calling the coordinator.
- **Tests to write first:** `STALE_REVISION` pre-dispatch test (assert **zero** invocations reach the agent client mock when stale); `reason` persistence test; concurrency test (two regenerations, one succeeds).
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=RegenerateAssessmentDraftHandlerIntegrationTest`, then full suite
- **Acceptance criteria:** Criteria #6, #7, #8 from [10](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md).
- **Risks:** Medium — the pre-dispatch check must genuinely happen before Phase 1 (agent call), verified by asserting zero invocations on the mocked agent-client port, not merely the final response code.
- **Commit boundary:** `feat(api): regenerate requires expectedRevisionId and persists adjustment reason`

### Task 10 — Application + API: retry and generation-status

- **Objective:** New `RetryGenerationHandler` + `GetGenerationStatusHandler`, wired to new controller endpoints.
- **Files:** New use cases; `AssessmentController.java` gains `POST .../draft/retry`, `GET .../generation-status`; `PATCH .../draft` **removed**; `POST .../revisions` added (wraps Task 08's handler).
- **Dependencies:** Task 05 (Session A1), Task 07B (Session A2), Tasks 08, 09 (this session, above)
- **Steps:** Per [LOCAL-CONTRACTS.md § API ↔ Web public contract](LOCAL-CONTRACTS.md#api--web-public-contract) exactly — status codes, typed error bodies, the `INDETERMINATE` classification (`now() - dispatchedAt > 5 × agentclient.read-timeout`).
- **Tests to write first:** API acceptance tests per endpoint (happy path + every typed error), written against the target contract before wiring the controller.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AssessmentControllerTest`, then full suite
- **Acceptance criteria:** Every row of the contract table in [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) exercised by at least one passing test; `PATCH .../draft` returns `404`/`405`, verified explicitly.
- **Risks:** Medium — largest single controller-surface change; test each endpoint's contract row individually, not only end-to-end.
- **Commit boundary:** `feat(api): add retry/generation-status/revisions endpoints, remove in-place draft PATCH`

**Coordination note (unchanged from the original packet):** Do not deploy/merge this task's endpoint removal (`PATCH .../draft`) to `develop` in isolation ahead of Web's replacement call — Session D coordinates the actual cutover timing. Your job here is to have the branch ready and correct.

## Out of scope for this session

Legacy backfill and cleanup (Tasks 12–13, Session A4). Do not drop or alter `assessment_drafts`/`agent_execution_logs`. Do not modify `agents/` or `web/`.

## Non-negotiable principles (reproduced from the ADRs — do not weaken these)

- **`expectedRevisionId` is checked before dispatch**, not only at persist time — for both Task 08 (human edit) and Task 09 (regenerate). The residual pre-check-to-commit race is accepted, not eliminated.
- **No silent last-write-wins.** Two concurrent edits/regenerations from the same `expectedRevisionId` produce exactly one success, one deterministic `409`.
- **All `409` errors are structured error bodies** (`{ code, message }`), never a bare status code — Web branches on `code`.
- **`PATCH .../draft` is retired, not deprecated-and-kept** — this cut has one internal consumer (`web/`), updated in the same overall cut (by Session C).

## TDD discipline

Follow RED → GREEN → REFACTOR for every task above, per its "Tests to write first."

## Testing

Full matrix: [TEST-PLAN.md](TEST-PLAN.md). This session closes out regression guard #2 (the old "edit preserves id/version" test deleted, replaced by its opposite) and guard #4 (`uq_ai_operations_in_flight` rejects a second concurrent row at the database level — exercised via Task 10's `OPERATION_IN_PROGRESS` path). Guard #1 (Research 02 §5.6 full reproduction including retry) becomes fully provable once Task 10 lands.

## Security

- Never print or persist secrets — in code, fixtures, logs, or your handoff.
- `expectedRevisionId`/`STALE_REVISION`/`ALREADY_GENERATED` checks run under the same ownership verification as today — a client cannot probe another teacher's assessment by guessing a revision id.

## Commits

Three commits: Task 08, Task 09, Task 10, at the exact boundaries above. Before each: `git diff`, `git diff --cached`, `git status`.

## Handling discoveries

If a task reveals a gap the plan didn't anticipate, stop, do not silently adapt, record it in [API-A3-HANDOFF.md](API-A3-HANDOFF.md)'s "Blockers" section.

## Verification before you consider this session done

```bash
./mvnw -f api/pom.xml clean test
```

Full suite green, no regression against the running baseline. Every row of [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md)'s contract table has at least one passing test. `PATCH /api/v1/assessments/{id}/draft` returns `404`/`405`, verified by an explicit test, not just "the route isn't defined."

## Push and handoff

```bash
git push origin feat/assessment-authoring-operation-foundation-api
```

Fill in [API-A3-HANDOFF.md](API-A3-HANDOFF.md) completely — **this handoff is also the contract artifact Session C (Web) needs**, so include a concrete example request/response for every endpoint, not just a reference back to [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md). Commit it: `docs(api): record session A3 handoff`. Push again. Do not open a PR, do not merge.

## Handoff gate — what Session A4 checks before starting

Session A4 will not proceed if: this handoff is incomplete, HEAD doesn't match, the recorded test run wasn't actually green, a critical blocker is flagged, or the public contract isn't demonstrably complete (every endpoint in [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) has a passing test).

## Final report to whoever invoked this session

Branch and HEAD; the three tasks completed with commit hashes; full test count before/after; confirmation `PATCH .../draft` is removed and tested as removed; the contract artifact provided for Web; any blocker recorded; confirmation `API-A3-HANDOFF.md` was pushed; the exact next command (`CLAUDE-API-A4-PROMPT.md`, given to a fresh session).

---

[← README](README.md) · [↑ Volver al inicio](#top)
