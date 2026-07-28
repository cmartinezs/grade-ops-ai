<a id="top"></a>

# LOCAL-CONTRACTS — Agents: Assessment Authoring Operation Foundation

**Status:** Ready — frozen. **Parent:** [README](README.md) · **Prev:** [TASKS](TASKS.md) · **Next:** [TEST-PLAN →](TEST-PLAN.md)

## What `agents/` owns in this cut

Provider resolution, model resolution, the external LLM call, structured-output validation, consumption metadata (tokens, cost estimate, latency), provider request id, correlation-id echo, normalization of technical failures into `AssessmentAgentException.Reason`. Full responsibility boundary: [root packet § `agents/` owns](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/01-workspace-responsibility-matrix.md#agents-owns).

## What `agents/` does not own

Business idempotency, current-revision selection, authoritative persistence of authoring state, publication, approval, academic grades, autonomous creation of a current revision. `agents/` returns a structured proposal; `api/` decides whether it becomes a revision. No code in this packet's scope may create, update, or delete `AiOperation`, `AgentAttempt`, or `AssessmentRevision` rows — those are `api/`'s aggregates, in `api/`'s database, written only by `api/`.

## The one contract change this packet makes

`AgentExecutionLogPayload` (`agents/src/main/java/cl/gradeops/ai/agents/assessment/application/result/AgentExecutionLogPayload.java`) gains a `provider` field alongside its existing `model` field, populated from `AssessmentAgentOrchestrator`'s already-resolved `selected.name()`. See [TASKS.md § Task 07A](TASKS.md#task-07a--add-resolved-provider-to-the-generation-response) for exact line numbers and the full existing field list.

**This is additive.** Every other field on `AgentExecutionLogPayload`/`AssessmentExecutionResponse` is unchanged. `api/`'s Task 07B updates its own mirrored DTO (`AssessmentAgentResponse.Log` in `api/src/main/java/cl/gradeops/ai/api/agentclient/AssessmentAgentResponse.java`) to deserialize the new field — that file is not this packet's to edit, but the field name you choose here must match exactly what API's session expects, per the [Task 07 split](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/04-task-distribution.md#task-07-split).

## What already works and needs no change (do not "fix" these)

- **Correlation id:** `api/`'s `AssessmentAgentClient` mints a correlation id and sends it as the `X-Correlation-Id` header on every outbound call. `agents/`'s `CorrelationIdFilter` (`agents/src/main/java/cl/gradeops/ai/agents/shared/infrastructure/adapter/in/web/CorrelationIdFilter.java`) already reads it, generates one only if absent/blank, and echoes it back as a response header plus MDC. Since `api/` originates the value, it does not need it echoed in the response *body* to persist it on `AgentAttempt.correlationId` — it already has the value it sent. No response-body change is needed for correlation id.
- **Failure taxonomy:** `AssessmentAgentException.Reason.{INVALID_COMMAND, MALFORMED_OUTPUT}` already exist and are already meaningfully distinct. The defect this whole cut fixes (`api/`'s `GlobalExceptionHandler` collapsing every agents-side 4xx into one generic `AGENT_REJECTED`) is entirely on `api/`'s side — `agents/` already returns the detailed reason. Do not add new failure codes here.
- **Provider/model resolution:** `AssessmentGenerationPortSelector.resolve(command.provider())` (unchanged by the [Policy-Based Provider And Model Routing ADR](../../../../docs/99-decisions/2026-07-27-policy-based-model-routing.md)) already works correctly. This packet only exposes its existing result in one additional response field — it does not change how or when resolution happens.

## Canonical failure-code taxonomy (for reference — `agents/` produces two of these, never invents new ones)

| Code | Produced by |
|---|---|
| `INVALID_COMMAND` | `AssessmentAgentException.Reason.INVALID_COMMAND` |
| `MALFORMED_OUTPUT` | `AssessmentAgentException.Reason.MALFORMED_OUTPUT` |

Every other code in the [root packet's canonical taxonomy](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/03-cross-workspace-api-contracts.md#canonical-failure-code-taxonomy) is produced or assigned by `api/`, not `agents/`.

---

← [TASKS](TASKS.md) | [↑ inicio](#top) | [Siguiente: TEST-PLAN →](TEST-PLAN.md)
