<a id="top"></a>

# 04 — AI Operation Lifecycle

**Parent:** [README](README.md) · **Status:** Planned · **Prev:** [03 — Operation and API Contracts](03-operation-and-api-contracts.md) · **Next:** [05 — Web Migration →](05-web-migration.md)

Decision authority: [Durable AI Operation Model](../../99-decisions/2026-07-28-durable-ai-operation-model.md). This document translates the three-phase sequence into concrete implementation steps against the existing code.

## Where provider/model get resolved

Unchanged: `agents/`'s `AssessmentAgentOrchestrator.generate()` resolves the provider via `AssessmentGenerationPortSelector.resolve(command.provider())` and returns the actually-used provider/model in its response (`AssessmentExecutionOutcome`). The defect being fixed is entirely on the `api/` side, which today persists `agentCommand.provider()` (frequently `null`, since handlers pass `null` to let `agents/` choose) instead of the value `agents/` actually resolved and returned. This cut changes `api/` to persist `response.log().model()` and the newly-required `response.log().provider()` fields onto `AgentAttempt.resolvedModel`/`resolvedProvider` — **not** what the client requested.

This requires one small, additive change to the `agents/`↔`api/` contract: `AssessmentAgentResponse.Log` must include the resolved provider name (today it carries `model` but not `provider` explicitly — verify against `AgentExecutionLogPayload` at implementation time; if `provider` is not already present, add it as a new field, additive, not a breaking rename).

## When the `AgentAttempt` is recorded

```text
1. Use case receives the command (generate / regenerate / retry).
2. Load Assessment. Verify ownership.
3. Idempotency check (see Idempotency and Concurrency Strategy) — replay or reject before anything else.
4. Pre-dispatch staleness check: expectedRevisionId vs. Assessment.currentRevisionId (skip for CREATE_INITIAL_REVISION, which has none).
   → mismatch: 409 STALE_REVISION, no AiOperation touched, no agent call.
5. TRANSACTION A (commits before step 6):
   - find-or-create AiOperation (PENDING → IN_PROGRESS)
   - insert AgentAttempt (status = DISPATCHED, dispatchedAt = now())
   - commit
6. Call agents/ over HTTP — NO open transaction during this call (unchanged discipline from DraftGenerationCoordinator).
7. TRANSACTION B:
   - success + CAS still valid → AgentAttempt COMPLETED, AssessmentRevision inserted, Assessment.currentRevisionId/lockVersion CAS-updated, AiOperation SUCCEEDED
   - success + CAS now invalid (someone else's operation won in the meantime) → AgentAttempt FAILED (failureCode = STALE_ON_COMPLETION), structuredResult still persisted, AiOperation FAILED_TERMINAL
   - agent/transport failure → AgentAttempt FAILED (failureCode = mapped from AssessmentAgentException.Reason or AgentClientException.Reason), AiOperation FAILED_RETRYABLE or FAILED_TERMINAL
   - commit
```

Step 5 is the concrete implementation of "durable evidence before dispatch." If the process crashes between step 5's commit and step 7's commit, the `AgentAttempt` row exists in `DISPATCHED` state — this is the intended, accepted "orphaned in-flight" outcome, not a bug (see below).

## Failure code mapping

| Source | Value | Maps to `AgentAttempt.failureCode` | `AiOperation.status` |
|---|---|---|---|
| `AssessmentAgentException.Reason.INVALID_COMMAND` (agents/) | validation | `INVALID_COMMAND` (verbatim) | `FAILED_TERMINAL` |
| `AssessmentAgentException.Reason.MALFORMED_OUTPUT` (agents/) | LLM output shape | `MALFORMED_OUTPUT` (verbatim) | `FAILED_RETRYABLE` |
| `AgentClientException.Reason.UNREACHABLE` (api/) | transport | `AGENT_UNAVAILABLE` | `FAILED_RETRYABLE` |
| `AgentClientException.Reason.AGENT_REJECTED` (api/) | agents/ 4xx, detail not separately available at this layer | `AGENT_REJECTED` | `FAILED_TERMINAL` |
| `AgentClientException.Reason.AGENT_ERROR` (api/) | agents/ 5xx | `AGENT_ERROR` | `FAILED_RETRYABLE` |
| CAS failure at persist time | — | `STALE_ON_COMPLETION` | `FAILED_TERMINAL` |

`GlobalExceptionHandler`'s current behavior of collapsing every agents/-side 4xx into `AGENT_REJECTED` is why `INVALID_COMMAND`/`MALFORMED_OUTPUT` are only reachable when the agents/ response body is inspected directly, not solely through the exception's `Reason` — implementation must read the response payload (already returned by `agents/`, already discarded today) rather than only the HTTP status class.

## Orphaned in-flight detection (no scheduler)

`GET .../generation-status` (03) computes, at read time:

```text
if attempt.status == DISPATCHED and now() - attempt.dispatchedAt > (5 × agentclient.read-timeout):
    → surface as INDETERMINATE, retryable = true, with a distinct message from FAILED_RETRYABLE
else:
    → surface attempt.status as-is
```

No background job, no scheduler, no queue. `agentclient.read-timeout` is the existing configured value in `AgentClientConfig` (60s per Research 02 §14) — the threshold is derived from it, not a new independent constant to keep in sync.

## Retry vs. new operation

| Action | Creates |
|---|---|
| User clicks "Retry" on a `FAILED_RETRYABLE` operation | new `AgentAttempt` (`attemptNumber + 1`) under the **same** `AiOperation` |
| User submits the intake form again after a fully `SUCCEEDED` generation exists | rejected upstream — `ALREADY_GENERATED` (03) before any `AiOperation` logic runs |
| User submits the intake form again with the same `Idempotency-Key` before the first attempt resolved | idempotency replay (returns the same in-progress/terminal state), no new `AiOperation` |

## What is intentionally not built in this cut

- No automatic transport-level retry inside a single `AgentAttempt` (the `AgentRun`/`AgentAttempt` split stays deferred — see [Durable AI Operation Model § Two tables, not three](../../99-decisions/2026-07-28-durable-ai-operation-model.md)).
- No reconciliation job that proactively resolves `INDETERMINATE` attempts — computed at read time only.
- No async/polling response shape (`202` here means "an operation record exists," not "check back later via long-polling infrastructure") — this cut's dispatch is still synchronous end-to-end from Web's perspective.

---

← [03 — Operation and API Contracts](03-operation-and-api-contracts.md) | [↑ README](README.md) | [Siguiente: Web Migration →](05-web-migration.md)
