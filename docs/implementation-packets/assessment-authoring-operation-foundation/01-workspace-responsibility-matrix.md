<a id="top"></a>

# 01 — Workspace Responsibility Matrix

**Parent:** [README](README.md) · **Status:** Ready · **Prev:** [00 — Coordination Overview](00-coordination-overview.md) · **Next:** [02 — Shared Domain Contract →](02-shared-domain-contract.md)

## Authoritative responsibility boundaries

### `api/` owns

Domain model of authoring; `Assessment` identity; `AssessmentBrief`; immutable revisions and provenance; current-revision pointer; optimistic concurrency; functional idempotency; durable AI operations (`AiOperation` persistence, `AgentAttempt` persistence); the public HTTP contract and its typed errors; authorization; the link between an authoring operation and an AI operation; retry/resume authority; removal of legacy endpoints; the integration with `agents/` as a client.

`api/` does not delegate authoritative business rules to `web/` or `agents/`. A rule enforced only in a React hook or only inside `agents/`'s orchestrator, with no server-side equivalent in `api/`, does not satisfy this plan's acceptance criteria.

### `agents/` owns

Provider resolution; model resolution; tool/prompt configuration; agent execution; the external LLM call; structured-output validation; consumption metadata (tokens, cost estimate, latency); provider request id; correlation-id propagation (echoing what `api/` sends, not minting its own); normalization of technical failures into the existing `AssessmentAgentException.Reason` taxonomy; the internal contract response shape consumed by `api/`.

`agents/` does not own: business idempotency, current-revision selection, authoritative persistence of authoring state, publication, approval, grades, or autonomous creation of a current revision. `agents/` returns a structured proposal; `api/` decides whether that proposal becomes a revision.

**Scope note specific to this cut:** Research 01/02 and the [Durable AI Operation Model ADR](../../99-decisions/2026-07-28-durable-ai-operation-model.md) all independently conclude that `agents/`'s existing behavior already satisfies almost everything in the paragraph above — `AssessmentAgentOrchestrator` already resolves provider/model correctly, already returns a detailed failure reason, and correlation propagation already works at the transport layer. The defect this plan fixes is that `api/` discards what `agents/` already provides. Do not read the responsibility list above as a mandate to rewrite working `agents/` code; see [Task 07A in the Agents local packet](../../../agents/docs/implementation-packets/assessment-authoring-operation-foundation/TASKS.md) for the one narrow, real, additive change.

### `web/` owns

The creation experience; initial generation trigger; visualization of durable state (pending/failed/succeeded/indeterminate); retry action; resume-after-refresh; conflict handling (`STALE_REVISION`); client-side idempotency-key generation and lifecycle; the `expectedRevisionId` the client sends; revision-history display; human-revision creation through the new contract; removal of the legacy edit flow; safe rendering of error messages.

`web/` does not: coordinate authoritative business rules; invent states not in [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md#canonical-status-taxonomy); assume success from the absence of an error; treat ephemeral React state as the sole record of a durable operation; create a second `Assessment` on retry; hide a conflict from the user; mutate a displayed revision locally without a round trip.

### `infra/`

No infrastructure changes are required or authorized by this cut — see [08 — Risk and Decision Ledger § Infrastructure](08-risk-and-decision-ledger.md#infrastructure) for the explicit `NOT_REQUIRED` determination and its reasoning. No infra packet is created.

## Task ownership matrix

Source: [08 — Implementation Sequence](../../implementation-plans/assessment-authoring-operation-foundation/08-implementation-sequence.md). Every task has exactly one primary owner; cross-workspace tasks are split into lettered subtasks with one owner each, detailed in [04 — Task Distribution](04-task-distribution.md).

| Task | Title | Primary workspace | Secondary workspace | Prerequisites | Shared contract touched | Integration dependency |
|---|---|---|---|---|---|---|
| 01 | Schema: `ai_operations`/`agent_attempts` | API | — | none | [02](02-shared-domain-contract.md) `AiOperation`/`AgentAttempt` | none |
| 02 | Schema: `assessment_revisions` + pointer | API | — | Task 01 | [02](02-shared-domain-contract.md) `AssessmentRevision` | none |
| 03 | Schema: `idempotency_records` | API | — | Task 02 | [02](02-shared-domain-contract.md) `IdempotencyKey` | none |
| 04 | Domain: `AssessmentRevision` aggregate | API | — | Task 02 | same | none |
| 05 | Domain: `AiOperation`/`AgentAttempt` aggregates | API | — | Task 01 | same | none |
| 06 | Application: idempotency guard | API | — | Task 03 | [03](03-cross-workspace-api-contracts.md) idempotency semantics | none |
| 07 | Rewrite generation coordinator (pivot) | API | AGENTS | Tasks 04, 05, 06 | [03](03-cross-workspace-api-contracts.md) API↔Agents contract | **Yes** — 07B needs 07A's field decided first (see [04](04-task-distribution.md#task-07-split)) |
| 08 | Human edit → revision | API | — | Task 04 | — | none |
| 09 | Regenerate requires `expectedRevisionId` | API | — | Tasks 04, 07 | — | none |
| 10 | API surface: retry/status/revisions endpoints | API | — | Tasks 05, 07, 08, 09 | [03](03-cross-workspace-api-contracts.md) public contract | Gates Task 11's final wiring |
| 11 | Web migration | WEB | — | Task 10 (final integration; may start against mocks earlier — see [05 — Execution Order](05-execution-order.md)) | [03](03-cross-workspace-api-contracts.md) public contract | **Yes** — needs API's real endpoints for final verification |
| 12 | Legacy data backfill (V17) | API | — | Tasks 01–10 deployed-compatible | — | none |
| 13 | Cleanup dead legacy code | API | — | Tasks 07, 08, 12 | — | none |
| 14 | Full regression + docs sync | INTEGRATION | API, AGENTS, WEB | all prior tasks | all | **Yes** — this task *is* the integration dependency |

No task appears twice with a contradictory owner. No task is missing an owner.

## Reading this matrix correctly

- "Primary workspace" is who writes and commits the code/tests for that task.
- "Secondary workspace" only appears when a task cannot be completed without a change landing in a second workspace first (Task 07, Task 14). It never means shared authoritative ownership of the same rule — see [Alignment check: ownership duplication](07-integration-and-final-verification.md#alignment-check-table) for the explicit verification that no responsibility is claimed by two workspaces.
- "Integration dependency" flags tasks whose *final* verification needs another workspace's real (non-mocked) artifact, as distinct from tasks whose *development* can start using a mock — this distinction is expanded in [05 — Execution Order](05-execution-order.md#session-start-vs-task-completion-gates).

---

← [00 — Coordination Overview](00-coordination-overview.md) | [↑ inicio](#top) | [Siguiente: Shared Domain Contract →](02-shared-domain-contract.md)
