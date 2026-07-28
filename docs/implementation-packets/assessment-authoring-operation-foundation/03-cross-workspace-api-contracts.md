<a id="top"></a>

# 03 — Cross-Workspace API Contracts

**Parent:** [README](README.md) · **Status:** Ready — frozen · **Prev:** [02 — Shared Domain Contract](02-shared-domain-contract.md) · **Next:** [04 — Task Distribution →](04-task-distribution.md)

## API ↔ Agents internal contract

Source of truth: [Durable AI Operation Model](../../99-decisions/2026-07-28-durable-ai-operation-model.md), [04 — AI Operation Lifecycle](../../implementation-plans/assessment-authoring-operation-foundation/04-ai-operation-lifecycle.md). This is the internal, OIDC-protected contract — not exposed to `web/`.

### Request (API → Agents)

At minimum: `operationId` (the `AiOperation.id`), `attemptId` (the `AgentAttempt.id`, already created durably before this call), `correlationId`, `assessmentId`, `expectedRevisionId` (nullable — absent for `CREATE_INITIAL_REVISION`), `agentType`, `input` (brief/content fields), resolved policy context. No secret value is ever included in this payload — it is persisted (as `structuredResult`'s sibling request context is not persisted verbatim; only the fields already in `AgentAttempt`/`AiOperation` are).

**Owner of each field:** API originates and owns `operationId`, `attemptId`, `correlationId`, `assessmentId`, `expectedRevisionId` — Agents never invents or overrides these. Agents owns interpretation of `agentType`/`input`/policy context to select provider and model — API never dictates provider/model directly (per existing [Policy-Based Provider And Model Routing](../../99-decisions/2026-07-27-policy-based-model-routing.md), unchanged by this cut).

### Response — success (Agents → API)

At minimum: `attemptId` (echoed, must match the request), `correlationId` (echoed), `provider`, `model`, `providerRequestId`, `structuredOutput`, `inputTokens`, `outputTokens`, `estimatedCost`, `latency`, `completedAt`.

**Implementation note (not a new decision — already flagged by [04 — AI Operation Lifecycle](../../implementation-plans/assessment-authoring-operation-foundation/04-ai-operation-lifecycle.md#where-providermodel-get-resolved)):** the current `AssessmentAgentResponse.Log`/`AgentExecutionLogPayload` shape already carries `model` but must be verified to also carry `provider` explicitly. If it does not, adding it is a small additive field — not a breaking rename — and is the Agents workspace's Task 07A (see [04 — Task Distribution](04-task-distribution.md#task-07-split)).

### Response — failure (Agents → API)

At minimum: `attemptId`, `correlationId`, `failureCode`, `retryable`, `sanitizedMessage`, `provider` (when resolution happened before the failure), `model` (same condition), `providerRequestId` (when available), `latency`, `failedAt`.

**Failure code values Agents may return here:** `INVALID_COMMAND`, `MALFORMED_OUTPUT` (from `AssessmentAgentException.Reason` — see [Canonical failure-code taxonomy](#canonical-failure-code-taxonomy) below). Agents does not invent new failure code strings outside this taxonomy without a corresponding ADR amendment.

**Ownership boundary:** Agents does not persist `AiOperation`/`AgentAttempt`/`AssessmentRevision` — it returns this structured response and API performs every write. `AssessmentAgentOrchestrator` is unchanged by this cut except for the possible additive `provider` field.

## API ↔ Web public contract

Source of truth: [Authoring Operation Contract](../../99-decisions/2026-07-28-authoring-operation-contract.md), [03 — Operation and API Contracts](../../implementation-plans/assessment-authoring-operation-foundation/03-operation-and-api-contracts.md). Full request/response/status-code detail lives in that plan document — this table is the cross-workspace summary Web and API must agree on before either starts.

| Operation | Endpoint | Idempotency-Key | `expectedRevisionId` | Success | Key conflict responses |
|---|---|---|---|---|---|
| Create assessment | `POST /api/v1/assessments` | Required | n/a | `201` | `400` missing header, `409 IDEMPOTENCY_KEY_PAYLOAD_MISMATCH` |
| Generate initial revision | `POST /api/v1/assessments/{id}/draft` | Required | n/a | `201` (sync success) or `202` (durable operation created, failure/in-progress) | `409 ALREADY_GENERATED` |
| Retry operation | `POST /api/v1/assessments/{id}/draft/retry` | None (naturally idempotent by operation lookup) | n/a | `202` | `409 NO_ACTIVE_OPERATION_TO_RETRY`, `409 OPERATION_IN_PROGRESS` |
| Get generation status | `GET /api/v1/assessments/{id}/generation-status` | n/a (read) | n/a | `200` | n/a |
| Regenerate | `POST /api/v1/assessments/{id}/draft/regenerate` | Required | Required | `201` | `409 STALE_REVISION`, `409 ALREADY_GENERATED` does not apply here |
| Create human revision | `POST /api/v1/assessments/{id}/revisions` | None required | Required | `201` | `409 STALE_REVISION` |
| Get current revision / history | `GET /api/v1/assessments/{id}/draft`, `GET .../draft/versions` (URL segment may change — see plan §03) | n/a | n/a | `200` | n/a |

`PATCH /api/v1/assessments/{id}/draft` is **removed**, not deprecated-and-kept. Web must not call it after Task 11 lands.

## Canonical status taxonomy

The only status values that exist in this cut are the ones below. **A generic four-value or seven-value status sketch (`PENDING`/`DISPATCHING`/`RUNNING`/`SUCCEEDED`/`FAILED_RETRYABLE`/`FAILED_FINAL`/`INDETERMINATE`) was considered while drafting this coordination packet and rejected as the surface taxonomy — it does not match the ADR's actual field names and would have required Web to translate between two vocabularies for no reason. This correction is recorded explicitly in [07 — Integration and Final Verification § Alignment check table](07-integration-and-final-verification.md#alignment-check-table).**

| Layer | Values | Owner |
|---|---|---|
| `AiOperation.status` | `PENDING`, `IN_PROGRESS`, `SUCCEEDED`, `FAILED_RETRYABLE`, `FAILED_TERMINAL` | API |
| `AgentAttempt.status` | `DISPATCHED`, `COMPLETED`, `FAILED` | API |
| `GET .../generation-status` response `status` (read model, computed) | `NOT_STARTED`, `IN_PROGRESS`, `FAILED_RETRYABLE`, `INDETERMINATE` (plus implicit `SUCCEEDED`, signaled by `currentRevisionId` being non-null rather than a separate status string) | API |

`FAILED_TERMINAL` (not `FAILED_FINAL`), `IN_PROGRESS`/`DISPATCHED` (not `RUNNING`/`DISPATCHING`) are the only spellings any workspace may use. Web must render exactly these values, never invent a client-side synonym.

## Canonical failure-code taxonomy

Same correction applies here: a generic taxonomy (`TIMEOUT`, `PROVIDER_UNAVAILABLE`, `RATE_LIMITED`, `INVALID_PROVIDER_RESPONSE`, `STRUCTURED_OUTPUT_VALIDATION_FAILED`, `PERSISTENCE_FAILURE`, `CONCURRENCY_CONFLICT`, `IDEMPOTENCY_CONFLICT`, `INTERNAL_ERROR`) was considered and rejected in favor of the ADR's actual, already-implemented taxonomy, which is more specific and does not need to be invented from scratch. The table below is authoritative.

| Code | Produced by | Public to Web | Retryable | Notes |
|---|---|---|---|---|
| `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH` | API | Yes | No — new key required | Creation/generation/regeneration key reuse with a different payload |
| `ALREADY_GENERATED` | API | Yes | N/A — use current revision | Initial generation requested twice |
| `STALE_REVISION` | API | Yes | Yes — reload and resubmit | `expectedRevisionId` no longer current |
| `NO_ACTIVE_OPERATION_TO_RETRY` | API | Yes | No | Retry called with nothing to retry |
| `OPERATION_IN_PROGRESS` | API | Yes | Yes — poll, then retry | An attempt may still be running |
| `AGENT_UNAVAILABLE` | API (transport, `AgentClientException.Reason.UNREACHABLE`) | Yes | Yes | Transient transport failure reaching `agents/` |
| `AGENT_ERROR` | API (transport, `AgentClientException.Reason.AGENT_ERROR`) | Yes | Yes | `agents/` returned 5xx |
| `AGENT_REJECTED` | API (transport, `AgentClientException.Reason.AGENT_REJECTED`) | Yes | No — fix input | `agents/` returned 4xx and the detailed reason below was not separately available |
| `INVALID_COMMAND` | AGENTS (`AssessmentAgentException.Reason.INVALID_COMMAND`), persisted verbatim by API | Yes | No | Agents-side validation failure |
| `MALFORMED_OUTPUT` | AGENTS (`AssessmentAgentException.Reason.MALFORMED_OUTPUT`), persisted verbatim by API | Yes | Yes | LLM structured-output shape failure |
| `STALE_ON_COMPLETION` | API (CAS failure at persist time) | Yes | No — the operation is terminal; a fresh operation must be started | Late AI success arrives after `expectedRevisionId` moved |

No workspace may use `PROVIDER_TIMEOUT`, `TIMEOUT_ERROR`, `TIMEOUT`, or any other synonym for the above without an explicit, documented translation table added to this file — none exists today because none is needed.

---

← [02 — Shared Domain Contract](02-shared-domain-contract.md) | [↑ inicio](#top) | [Siguiente: Task Distribution →](04-task-distribution.md)
