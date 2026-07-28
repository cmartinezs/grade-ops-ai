<a id="top"></a>

# Durable AI Operation Model

- Status: Accepted
- Date: 2026-07-28
- Decision owner: Technical
- Informed by: [Research 01](../../design-system/research/research-01-current-domain-data-model-alignment-audit.md), [Research 02](../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md)
- Depends on: [Assessment Authoring Model](2026-07-28-assessment-authoring-model.md), [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md)
- Related decisions: [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md), [Agent Runtime Separation](2026-06-10-agent-runtime-separation.md), [Policy-Based Provider And Model Routing](2026-07-27-policy-based-model-routing.md)

## Context

`AgentExecutionLog` today is written *after* the external call to `agents/` returns (success or `AgentClientException`), inside `DraftGenerationCoordinator.callAgentAndPersist`. Research 02 CRITICAL-03 states the resulting failure mode precisely: if `agents/` completes the generation (provider was called, tokens were spent) but the API's own persistence transaction then fails — connection drop, DB outage — no run is ever recorded. A retry re-spends the cost with no record that the first attempt happened at all. Research 01/02 both also note that `provider`/`model` are frequently persisted as `null` (handlers send `null` to let `agents/` pick a default, and the coordinator persists exactly what was sent, not what was resolved), that agents'-side detailed failure reasons (`INVALID_COMMAND`, `MALFORMED_OUTPUT`) collapse into one generic API-side code (`AGENT_REJECTED`), and that the correlation ID created per call is never persisted.

## Problem

How to represent an AI operation so that durable evidence exists **before** the external dispatch (not after), so retries are traceable to one logical intent, so the actually-resolved provider/model/correlation-id are captured, and so typed failures survive the API↔agents boundary — without introducing a queue, scheduler, or distributed saga this cut does not need.

## Decision

### Non-negotiable principle

> Durable evidence of the attempt must exist before the external dispatch.

This is enforced structurally, not by convention: the row is inserted and committed in its own transaction *before* the HTTP call to `agents/` is made, not after.

### Two tables, not three

The task brief describes a three-level shape (`AiOperation` → `AgentRun` → `AgentAttempt`) and explicitly says the names/table count are not mandatory, only the responsibilities are. This cut uses **two** tables, folding `AgentRun`'s fields into `AgentAttempt`, because in this cut's actual implemented behavior every run has exactly one attempt (no automatic transport-level sub-retry is being built now) — a third table would model a distinction with no current behavioral payoff. The responsibilities are still fully resolved:

```text
AiOperation   — the durable functional intent. Survives across every user-initiated retry.
AgentAttempt  — one concrete dispatch: resolved provider/model/prompt version, correlation id, timing, outcome, cost.
```

If/when transport-level sub-retry becomes a real requirement (e.g., automatically retrying one `AgentAttempt` on a 5xx before surfacing failure), splitting `AgentAttempt` into `AgentRun` + `AgentAttempt` is an additive, backward-compatible migration — not a redesign. This ADR records that reversal path explicitly so a future author does not have to rediscover it.

### `AiOperation`

```text
id
assessmentId
operationType          (CREATE_INITIAL_REVISION | REGENERATE_REVISION)
requestedBy             (teacherUid)
idempotencyKey
expectedRevisionId      (null for CREATE_INITIAL_REVISION)
status                  (PENDING | IN_PROGRESS | SUCCEEDED | FAILED_RETRYABLE | FAILED_TERMINAL)
resultRevisionId        (nullable, set on SUCCEEDED)
createdAt, updatedAt
```

`UNIQUE (assessmentId, operationType) WHERE status IN ('PENDING','IN_PROGRESS')` — a DB-level backstop preventing two concurrent in-flight operations of the same type for the same assessment, complementing the application-level pre-check in the [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md).

### `AgentAttempt`

```text
id
aiOperationId
attemptNumber            (1, 2, 3… per operation — increments on user-initiated retry)
agentName
resolvedProvider         (the provider agents/ actually used — never null on a completed attempt)
resolvedModel
promptVersion
correlationId             (persisted — today it exists only in HTTP headers/logs, per Research 02 §11 "Correlation")
dispatchedAt
completedAt
status                    (DISPATCHED | COMPLETED | FAILED)
providerRequestId         (nullable)
failureCode               (nullable, typed — see below)
estimatedInputTokens, estimatedOutputTokens
costEstimate              (NUMERIC, not DOUBLE — Research 01 §11 flags `Double` for money as a primitive-obsession smell; `agent_execution_logs.cost_estimate` is `DOUBLE PRECISION` today)
structuredResult           (JSONB — the full structured output, retained even for attempts that never became a revision, e.g. `STALE_ON_COMPLETION`)
```

### Sequencing

```text
Phase 0 (transaction A, before dispatch)
  validate request
  pre-check expectedRevisionId (see Idempotency and Concurrency Strategy)
  create/reuse AiOperation → status = PENDING or IN_PROGRESS
  create AgentAttempt → status = DISPATCHED
  commit

Phase 1 (no transaction)
  call agents/ over HTTP

Phase 2 (transaction B, after response)
  on success:
    update AgentAttempt → COMPLETED, with resolvedProvider/resolvedModel/tokens/cost/correlationId/structuredResult
    CAS-update Assessment.currentRevisionId + lockVersion
    create AssessmentRevision (sourceAgentAttemptId = this attempt)
    update AiOperation → SUCCEEDED, resultRevisionId = new revision
    commit
  on CAS failure (expectedRevisionId no longer current):
    update AgentAttempt → FAILED, failureCode = STALE_ON_COMPLETION (structuredResult still retained)
    update AiOperation → FAILED_TERMINAL
    commit
  on agent/transport failure:
    update AgentAttempt → FAILED, failureCode = <mapped from AgentClientException.Reason / AssessmentAgentException.Reason>
    update AiOperation → FAILED_RETRYABLE (transient) or FAILED_TERMINAL (validation-class failure)
    commit
```

Phase 0 is what closes CRITICAL-03: even if Phase 2 never runs at all (process crash, DB outage right as the provider responds), a durable `AiOperation`/`AgentAttempt` pair already exists in `DISPATCHED`/`IN_PROGRESS` state — the worst case becomes an orphaned in-flight record, never a silent hole with no evidence.

### Orphaned in-flight records (honest residual, not hidden)

An `AgentAttempt` that stays `DISPATCHED` past a generous timeout (a documented multiple of the configured HTTP read timeout, e.g., 5× — see `AgentClientConfig`) is classified `INDETERMINATE` by the read path (`GET generation-status`), computed on the fly from `now() - dispatchedAt > threshold` — no scheduler or background job is introduced to do this. `INDETERMINATE` is surfaced to the user distinctly from `FAILED_RETRYABLE`: retry is still allowed (per the [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md)'s retry rules), but the possibility that the original call actually succeeded server-side is not hidden from the operator. This is a deliberate, honest trade-off: building a reconciliation job that reaches out to the provider to check attempt status is out of scope for this cut (no provider exposes that lookup uniformly, and it would be new infrastructure this cut does not need).

### Retry relationship

A user-initiated retry (via the dedicated retry endpoint) creates a **new `AgentAttempt`** (`attemptNumber` incremented) under the **same `AiOperation`** — never a new `AiOperation`, and never a new `Assessment`. This directly satisfies "Retrying no crea otro Assessment" and "Los retries quedan asociados a la misma operación lógica."

### Typed failure codes

`AgentClientException.Reason` (API-side: `UNREACHABLE`, `AGENT_REJECTED`, `AGENT_ERROR`) and `AssessmentAgentException.Reason` (agents-side: `INVALID_COMMAND`, `MALFORMED_OUTPUT`) both already exist and are already meaningfully distinct — the defect is that the API's `GlobalExceptionHandler` collapses every agents-side 4xx into the single generic `AGENT_REJECTED` before persisting it (Research 02 §11 "Provider provenance" / CRITICAL-05). This decision requires the agents-side detailed reason to be persisted onto `AgentAttempt.failureCode` verbatim (not remapped to a coarser API-side code) whenever agents/ returns one; the coarser `AgentClientException.Reason` values remain for failures that never reach agents/ at all (network/transport). No new failure taxonomy is invented — the fix is to stop discarding the one that already exists.

### What is available for credits, audit, and observability

Every `AgentAttempt` carries token counts, cost estimate, latency (`completedAt - dispatchedAt`), resolved provider/model, correlation id, and typed outcome — sufficient for cost attribution and reliability reporting without a separate ledger. This is explicitly **not** a credits/billing ledger (out of scope per the task brief); it is the minimal durable usage/cost evidence a future ledger would consume.

### Technical logs vs. business auditability

Pino (Web)/SLF4J (API, agents) technical logs remain unstructured, ops-facing, and are not the audit source of truth — they answer "what happened for debugging," not "who requested what, when, against which assessment/revision, at what cost." `AiOperation`/`AgentAttempt` are the durable, queryable, business-auditable record; this is the same separation `AgentExecutionLog` already established today (Research 02 §17 "Logs técnicos versus auditoría") — this decision generalizes and completes it, it does not invent it.

### Relationship to `AgentExecutionLog`

`agent_execution_logs` is absorbed by `ai_operation`/`agent_attempt` going forward. `AssessmentRevision.sourceAgentAttemptId` replaces `AssessmentDraft.agentExecutionLogId`. The old table is migrated and retained read-only for one release, then retired — detailed in the implementation plan's database migration task, mirroring the same approach applied to `assessment_drafts` in the [Assessment Authoring Model](2026-07-28-assessment-authoring-model.md).

## Decision Drivers

- Research 02 CRITICAL-03, CRITICAL-05: durability-before-dispatch, and lost provider/failure provenance.
- Research 01 §11 "AI-related Records", "Required evolution": evolve the log into a general run/execution record with operation, requester, configuration snapshot, and outcome.
- Task brief §4.4 non-negotiable principle, stated verbatim above.
- Existing, reusable pattern: `DraftGenerationCoordinator`'s external-call-outside-transaction discipline is correct and is kept — this decision only moves *when* the pre-dispatch record is written, not the transaction-boundary pattern itself.

## Alternatives Considered

| Option | Description | Verdict |
|---|---|---|
| A — Keep `AgentExecutionLog`, add columns | Patch the existing table with correlation id, resolved provider, typed failure code | Rejected: does not fix the core defect — the row is still written *after* dispatch, so a crash between provider-success and API-persistence still loses all evidence |
| B — Three tables (`AiOperation`/`AgentRun`/`AgentAttempt`) exactly as sketched in the task brief | Full three-level model | Rejected for *this cut*: no current behavior needs the Run/Attempt split (1:1 today); adds a join with no payoff now. Documented as a compatible future split, not discarded |
| C — Two tables (`AiOperation`/`AgentAttempt`), pre-dispatch durability | As decided above | **Adopted** |
| D — Async queue + worker consuming operations | Decouple dispatch from the HTTP request via a message broker | Rejected: no distributed infrastructure is justified yet; the durability requirement is met by pre-dispatch persistence in the same process, not by moving dispatch to a separate consumer |

## Consequences

- New tables `ai_operation`, `agent_attempt`; `agent_execution_logs` becomes legacy/read-only after migration.
- `DraftGenerationCoordinator` is restructured into the three-phase sequence above (still no `agents/` call inside a DB transaction — that discipline is preserved, just bracketed by an additional pre-dispatch commit).
- `agents/`'s orchestrator (`AssessmentAgentOrchestrator`) is unchanged in this cut — it already returns a structured result and a detailed failure reason; the fix is entirely on the API side (stop discarding what agents/ already provides).

## Migration Impact

- Additive: two new tables. `agent_execution_logs` rows are migrated into `agent_attempt` (one attempt per historical log row; `attemptNumber = 1` for all, since no historical row records more than one attempt); a corresponding `ai_operation` is synthesized per migrated row with `status` derived from the log's terminal outcome (`COMPLETED` → `SUCCEEDED`, `FAILED` → `FAILED_TERMINAL`) and `operationType` inferred from whether the migrated `assessment_drafts` row it links to was `versionNumber = 1` (`CREATE_INITIAL_REVISION`) or greater (`REGENERATE_REVISION`). This is a reasonable, honest inference — not a fabrication — because both facts (version number, which log produced which draft) are already faithfully recorded today.

## Compatibility Impact

- Internal only — `agents/`'s public contract (`AssessmentCommand`/`AssessmentAgentResponse`) is unchanged by this decision.

## Security Impact

- `AgentAttempt.structuredResult` may contain teacher-authored curriculum text; it is internal (API database), never exposed via a public endpoint in this cut, and subject to the same access control as the assessment it belongs to.
- Internal-endpoint protection (`X-Internal-Key` between `api/` and `agents/`) is unchanged.

## Testing Impact

- Unit: `AiOperation`/`AgentAttempt` state transitions (valid vs. invalid), failure-code mapping from both exception hierarchies.
- Integration: a test asserting that if Phase 2 is simulated to throw after a successful Phase 0 commit, an `AgentAttempt` row still exists in `DISPATCHED` state (proving the durability-before-dispatch principle, not just asserting it in prose).
- Migration test: `agent_execution_logs` → `ai_operation`/`agent_attempt` backfill preserves row counts and produces no `null` `resolvedProvider`/`resolvedModel` where the source log had a value.

## Open Consequences (Explicitly Deferred)

- Splitting `AgentAttempt` into `AgentRun` + `AgentAttempt` when transport-level sub-retry becomes a real requirement.
- A reconciliation job for `INDETERMINATE` orphaned attempts (currently computed at read time only).
- A general-purpose credits/cost ledger consuming `AgentAttempt` evidence — explicitly out of scope per the task brief.

## Related Decisions

- [Assessment Authoring Model](2026-07-28-assessment-authoring-model.md)
- [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md)
- [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md)
- [Agent Runtime Separation](2026-06-10-agent-runtime-separation.md)
- [Policy-Based Provider And Model Routing](2026-07-27-policy-based-model-routing.md)

## References

- [Research 01 — Current Domain & Data Model Alignment Audit](../../design-system/research/research-01-current-domain-data-model-alignment-audit.md), §11 "AI-related Records"
- [Research 02 — Workflow & Lifecycle Alignment Audit](../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md), §11 "AI Execution Lifecycle", §22 CRITICAL-03/CRITICAL-05
- `api/src/main/java/cl/gradeops/ai/api/assessment/application/usecase/DraftGenerationCoordinator.java`
- `agents/src/main/java/cl/gradeops/ai/agents/assessment/application/orchestrator/AssessmentAgentOrchestrator.java`

---

← [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md) | [↑ inicio](#top) | [README](README.md)
