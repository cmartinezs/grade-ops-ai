<a id="top"></a>

# CLAUDE-API-A2-PROMPT — Idempotency and Durable Coordinator

**Status:** Ready. **Parent:** [README](README.md) · **Session:** A2 of 4 (API) · **Prev session:** [CLAUDE-API-A1-PROMPT.md](CLAUDE-API-A1-PROMPT.md) · **Next session:** [CLAUDE-API-A3-PROMPT.md](CLAUDE-API-A3-PROMPT.md)

Give this file, verbatim, as the task prompt to a **fresh** session that opens **only `api/`**. It is complete and self-contained, but it has real prerequisites — read [Prerequisites gate](#prerequisites-gate-check-this-before-anything-else) before doing anything.

## Repository and workspace identity

```text
Repository: cmartinezs/grade-ops-ai
Workspace: api/ (Spring Boot 4 + Java 21 + PostgreSQL, Maven)
Branch: feat/assessment-authoring-operation-foundation-api  (continuing, not new)
Session: A2 — Idempotency and Durable Coordinator (second of four sequential, recoverable API sessions)
```

## Who you are and what you are building

You are Session A2. Session A1 introduced inert schema and domain aggregates that nothing calls yet. **This session is where the write path actually changes for the first time** — you introduce a reusable idempotency guard and then replace `DraftGenerationCoordinator` with the durable, three-phase `AiOperationCoordinator`. This is the single highest-risk task in the entire API packet: it is the pivot where old and new models must coexist correctly, and it is the fix for the literal defect that motivated this whole cut (a crash between "AI call succeeds" and "API persists the result" loses all evidence the call ever happened).

**This is documentation-implementing work.** You write real Java, real tests, no new migrations (Session A1 already added everything this session needs).

## Prerequisites gate — check this before anything else

This session has two hard prerequisites. Do not begin Task 06/07B until both are confirmed.

### 1. Session A1 complete

```bash
git fetch origin
git switch feat/assessment-authoring-operation-foundation-api
git pull --ff-only
git log --oneline -10
```

Confirm the branch's last commit is `docs(api): record session A1 handoff` (or later) and that [API-A1-HANDOFF.md](API-A1-HANDOFF.md) exists at the repo root of this branch with no unresolved critical blocker. Then run the baseline yourself — do not trust the handoff's test-count claim without re-running it:

```bash
./mvnw -f api/pom.xml clean test
```

If A1's handoff is missing, incomplete, or this baseline does not pass, **stop** — do not proceed by re-deriving A1's work yourself. Report this as a blocker.

### 2. Agents Task 07A complete

You need to know the exact field name/shape `agents/` uses for the resolved provider before writing deserialization code. The **approved path** is:

```text
Read agents/docs/implementation-packets/assessment-authoring-operation-foundation/HANDOFF.md
(committed by the Agents session as AGENTS-HANDOFF.md's content — same template-vs-committed-name
pattern as this packet's own HANDOFF.md)
→ extract its response fixture (a real JSON example of the new response shape)
→ write API's Task 07C contract test against that fixture
→ implement Task 07B's deserialization to match
```

Check whether Agents' handoff exists:

```bash
git log --all --oneline --grep="record assessment authoring foundation handoff" -- agents/
```

or, if the Agents branch has been merged into the integration branch already, look for `agents/docs/implementation-packets/assessment-authoring-operation-foundation/HANDOFF.md` with its template placeholders filled in.

**If Agents' handoff is not available when you reach this point, read-only inspection is a documented contingency, not the default path.** Only if you cannot find a completed Agents handoff after checking the above: read `agents/src/main/java/cl/gradeops/ai/agents/assessment/application/result/AgentExecutionLogPayload.java` and `AssessmentAgentOrchestrator.java` **read-only** (you may read `agents/`, you may not write to it) to verify the current field shape yourself. If you use this fallback, **record it explicitly** in [API-A2-HANDOFF.md](API-A2-HANDOFF.md)'s "Dependency consumed from Agents" section, including which path you took and why the primary path wasn't available.

**Verified baseline fact (from when this packet was drafted, not a promise about the current state):** as of the coordination session that split this packet, `AgentExecutionLogPayload.java:42-55` did **not** carry a `provider` field, only `model` — confirmed by reading `agents/` source directly. `AssessmentAgentOrchestrator.java:76` already resolves it locally (`selected.name()`) but never assigned it to the response. This is exactly what Agents' Task 07A adds. Do not assume it's still missing without checking — Agents' session may have already completed it.

## Authority — read in this order before writing any code

1. [Idempotency and Concurrency Strategy](../../../../docs/99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md)
2. [Durable AI Operation Model](../../../../docs/99-decisions/2026-07-28-durable-ai-operation-model.md)
3. [Authoring Operation Contract](../../../../docs/99-decisions/2026-07-28-authoring-operation-contract.md)
4. [04 — AI Operation Lifecycle](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/04-ai-operation-lifecycle.md) of the plan
5. This packet's [TASKS.md](TASKS.md) (Tasks 06, 07B specifically), [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md), and [API-A1-HANDOFF.md](API-A1-HANDOFF.md)

If anything here contradicts a document above it, the higher document wins — report the discrepancy (see [Handling discoveries](#handling-discoveries)). Do not modify approved contracts, taxonomies, field names, or the task list itself.

## Git — continuing on the same branch

```bash
git fetch origin
git switch feat/assessment-authoring-operation-foundation-api
git pull --ff-only
./mvnw -f api/pom.xml clean test
```

Do **not** create a new branch. Do not force-push, rebase destructively, or amend a commit Session A1 already pushed.

## Scope — exactly these two tasks, in order

### Task 06 — Application: idempotency guard service

- **Objective:** A reusable, generic idempotency-check/record service usable by any command handler.
- **Files:** `.../application/security/IdempotencyGuard.java` (or `.../shared/application/idempotency/`), `.../port/out/IdempotencyRepositoryPort.java`, adapter.
- **Dependencies:** Task 03 (Session A1, already done)
- **Steps:** `check(scope, operationType, key, payloadHash) → Optional<PriorResult>`; `record(scope, ..., resultReference, status)`. Hash over validated command fields — see [LOCAL-CONTRACTS.md § Idempotency](LOCAL-CONTRACTS.md#idempotency).
- **Tests to write first:** Unit tests for the check/record contract before implementation — no record → proceed; same hash → replay; different hash → conflict.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=IdempotencyGuardTest`, then full suite
- **Acceptance criteria:** Matches the three-way contract exactly; not yet wired into any endpoint.
- **Risks:** Low.
- **Commit boundary:** `feat(api): add reusable idempotency guard service`

### Task 07B — Application: rewrite the generation coordinator (the pivot task)

- **Objective:** Replace `DraftGenerationCoordinator` with the three-phase, `AiOperation`/`AgentAttempt`/`AssessmentRevision`-aware coordinator. Wire `GenerateAssessmentDraftHandler` to it. **This is the task where the write path actually changes** — every prior task (A1's five, and this session's Task 06) was additive and inert.
- **Files:** New `AiOperationCoordinator.java` replacing `DraftGenerationCoordinator.java`; `GenerateAssessmentDraftHandler.java` updated; `GlobalExceptionHandler` updated for the typed error codes in [LOCAL-CONTRACTS.md § Failure codes](LOCAL-CONTRACTS.md#canonical-failure-code-taxonomy); `AssessmentAgentResponse.Log`/`AgentExecutionLogPayload` deserialization updated for the `provider` field per the [Prerequisites gate](#prerequisites-gate-check-this-before-anything-else) above.
- **Dependencies:** Tasks 04, 05 (Session A1), Task 06 (this session), Agents Task 07A (external)
- **Steps:** Implement the Phase 0 / dispatch / Phase 2 sequence exactly per [LOCAL-CONTRACTS.md § Sequencing](LOCAL-CONTRACTS.md#sequencing):
  ```text
  Phase 0 (transaction A, before dispatch): validate, idempotency check, pre-dispatch staleness
    check, create/reuse AiOperation (PENDING/IN_PROGRESS), insert AgentAttempt (DISPATCHED), commit.
  Phase 1 (no transaction): call agents/ over HTTP.
  Phase 2 (transaction B, after response): success+CAS valid → AgentAttempt COMPLETED,
    AssessmentRevision inserted, Assessment.currentRevisionId/lockVersion CAS-updated,
    AiOperation SUCCEEDED. success+CAS invalid → AgentAttempt FAILED (STALE_ON_COMPLETION),
    AiOperation FAILED_TERMINAL. agent/transport failure → AgentAttempt FAILED (mapped code),
    AiOperation FAILED_RETRYABLE or FAILED_TERMINAL.
  ```
  `GenerateAssessmentDraftHandler` now: checks idempotency (Task 06) → checks `ALREADY_GENERATED` precondition → delegates to the new coordinator → maps typed failures to the response shape in [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md).
- **Tests to write first:**
  (a) **The 07C contract test** — proves `api/`'s `agentclient` module correctly deserializes the `provider` field from Agents' actual fixture (per the [Prerequisites gate](#prerequisites-gate-check-this-before-anything-else)), not a hand-rolled JSON string. Write this before the deserialization code.
  (b) the failure-recovery test proving an `AgentAttempt` survives a simulated Phase-2 crash after Phase-0 commit — must fail against the *old* coordinator, pass against the new one;
  (c) `ALREADY_GENERATED` precondition test;
  (d) idempotency replay test for this endpoint.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AiOperationCoordinatorTest,GenerateAssessmentDraftHandlerIntegrationTest`, then full suite — must not regress the 289-test baseline.
- **Acceptance criteria:** Criteria #9, #10, #11, #13 (provider/model/correlation persisted — this is where #13 is jointly closed with Agents' half) from [10 — Acceptance Criteria](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md); old `DraftGenerationCoordinatorTest`/`DraftGenerationCoordinator.java` deleted, not left dangling.
- **Risks:** **Highest-risk task in the entire API packet.** An assessment created before this deploy has no `currentRevisionId`; the `ALREADY_GENERATED` check must treat that as "not generated yet," not crash. Mitigation: an explicit test with an `Assessment` restored via the pre-migration shape going through the new generate path successfully.
- **Commit boundary:** `feat(api): switch initial generation to durable AiOperation/AgentAttempt coordinator` (implementation + its own tests, including the 07C contract test, in one commit — do not split the coordinator from its tests).

## Out of scope for this session

Human-edit/regenerate/retry/status endpoints (Tasks 08–10, Session A3), legacy backfill and cleanup (Tasks 12–13, Session A4). Do not touch `Assessment.status`, do not add new endpoints, do not modify `agents/` or `web/`.

## Non-negotiable principles (reproduced from the ADRs — do not weaken these)

- **Durable evidence before dispatch.** The `AiOperation`/`AgentAttempt` row is committed in its own transaction *before* the HTTP call to `agents/`, not after — this is the entire point of this session.
- **`expectedRevisionId` is not used by this session's tasks directly** (Task 07B has no `expectedRevisionId` — `CREATE_INITIAL_REVISION` has none by definition) — that check belongs to Task 09 (Session A3, regenerate). Do not add it prematurely here.
- **Idempotency and optimistic concurrency are two different mechanisms** — implement idempotency (Task 06) and rely on `lockVersion` CAS (already on `Assessment` from Session A1) for the coordinator's Phase 2, do not substitute one for the other.
- **AI output is a proposal.** No code path lets an `AgentAttempt` result become a revision without going through the same `AssessmentRevision` creation path Session A3's human edit will also use.

## TDD discipline

Follow RED → GREEN → REFACTOR. The failure-recovery test for Task 07B is explicitly required to fail against the *old* `DraftGenerationCoordinator` and pass against the new one — write it, confirm it fails first if the old coordinator is still present, then implement.

## Testing

Full matrix: [TEST-PLAN.md](TEST-PLAN.md). This session's regression guards specifically: guard #1 (Research 02 §5.6 end-to-end reproduction) becomes provable once this session lands, though the full guard (including retry) isn't complete until Session A3's Task 10. Guard #3 (`STALE_ON_COMPLETION` persists `structuredResult`) is fully this session's responsibility.

## Security

- Never print or persist `GRADEOPS_GROQ_API_KEY`, `INTERNAL_API_SECRET`, or other secrets — in code, fixtures, logs, or your handoff.
- `actorId` on every revision created by this coordinator must come from the authenticated security context, never a client-supplied value.
- Ownership checks (`OwnershipVerifier`) run before the idempotency/CAS checks.

## Commits

Two commits: one for Task 06, one for Task 07B (implementation + all its tests together, per the commit boundary above). Before each: `git diff`, `git diff --cached`, `git status`.

## Handling discoveries

If the Agents contract genuinely doesn't match what [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) specifies, or Task 07B reveals a gap the plan didn't anticipate: stop, do not silently adapt, record it in [API-A2-HANDOFF.md](API-A2-HANDOFF.md)'s "Blockers" section. Do not invent scope to work around it.

## Residual risks you are not expected to close

- The narrow pre-check-to-commit race that can waste one LLM call under adversarial concurrency — not fully relevant yet (no `expectedRevisionId` in this session's tasks), but the CAS-at-persist-time half (`STALE_ON_COMPLETION`) is this session's responsibility and is accepted, not eliminated.
- `INDETERMINATE` orphaned `AgentAttempt` classification is computed at read time only (Session A3's Task 10 exposes this via `generation-status`) — no reconciliation job, do not add one.

## Verification before you consider this session done

```bash
./mvnw -f api/pom.xml test -Dtest=AiOperationCoordinatorTest,GenerateAssessmentDraftHandlerIntegrationTest
./mvnw -f api/pom.xml clean test
```

Full suite green, must not regress the 289-test baseline. `DraftGenerationCoordinator.java` and `DraftGenerationCoordinatorTest.java` no longer exist. `grep -rn "DraftGenerationCoordinator" api/src` returns nothing.

## Push and handoff

```bash
git push origin feat/assessment-authoring-operation-foundation-api
```

Fill in [API-A2-HANDOFF.md](API-A2-HANDOFF.md) completely and commit it as the last commit on this branch: `docs(api): record session A2 handoff`. Push again. Do not open a PR, do not merge.

## Handoff gate — what Session A3 checks before starting

Session A3 will not proceed if: this handoff is incomplete, HEAD doesn't match, the recorded test run wasn't actually green, a critical blocker is flagged, or the Agents dependency wasn't genuinely resolved (either via a real `AGENTS-HANDOFF.md`/fixture, or an explicitly documented read-only-verification fallback — not silently skipped).

## Final report to whoever invoked this session

Branch and HEAD; which path was taken for the Agents dependency (handoff-consumed or read-only-verification-fallback) and why; both tasks completed with commit hashes; full test count before/after; confirmation `DraftGenerationCoordinator` is fully removed; any blocker recorded; confirmation `API-A2-HANDOFF.md` was pushed; the exact next command (`CLAUDE-API-A3-PROMPT.md`, given to a fresh session).

---

[← README](README.md) · [↑ Volver al inicio](#top)
