<a id="top"></a>

# 03 — Operation and API Contracts

**Parent:** [README](README.md) · **Status:** Planned · **Prev:** [02 — Domain and Data Changes](02-domain-and-data-changes.md) · **Next:** [04 — AI Operation Lifecycle →](04-ai-operation-lifecycle.md)

Decision authority: [Authoring Operation Contract](../../99-decisions/2026-07-28-authoring-operation-contract.md). This document is the endpoint-by-endpoint implementation reference.

## `POST /api/v1/assessments` — create assessment intent

| | Current | Target |
|---|---|---|
| Request | `CreateAssessmentBriefRequest` body only | Same body **+ required `Idempotency-Key` header** |
| Response | `201`, `CreateAssessmentBriefResponse{assessmentId}` | Unchanged on first call; **replayed verbatim** on same-key repeat |
| Status codes | `201`, `401`, `422`, `500` | + `400` (missing header), `409` (`IDEMPOTENCY_KEY_PAYLOAD_MISMATCH`) |
| Idempotency | none | `(teacherUid, CREATE_ASSESSMENT_BRIEF, key)` — see [Idempotency and Concurrency Strategy](../../99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md) |
| Concurrency | n/a | n/a — no shared mutable state to race on creation |
| Failure behavior | rolls back both inserts | unchanged, plus: no idempotency record is written on failure (so a genuinely failed attempt can be retried with the same key) |
| Compatibility | — | **Breaking**: header becomes mandatory. Single consumer (`web/`), updated in the same cut. |

## `POST /api/v1/assessments/{id}/draft` — generate initial revision

| | Current | Target |
|---|---|---|
| Request | none (path only) | `Idempotency-Key` header required |
| Response (success) | `201`, `GenerateAssessmentDraftResponse` | `201`, revision response shape incl. `origin`, `actorId`, `previousRevisionId: null` |
| Response (already generated) | re-attempts version 1, fails on DB unique constraint after paying for an LLM call | `409`, `ALREADY_GENERATED`, body includes existing current revision — **no agent call made** |
| Response (agent/transport failure) | `502`/`503`/`422` per `AgentClientException.Reason`, no addressable state | `202`, body describing the `AiOperation` (id, status, failure code, retryable) |
| Idempotency | none | `(assessmentId, CREATE_INITIAL_REVISION, key)` |
| Concurrency | none — two concurrent calls both attempt version 1 | `uq_ai_operations_in_flight` blocks a second concurrent dispatch for the same assessment+type |
| Failure behavior | generic `FAILED` `AgentExecutionLog`, agents-side detail discarded | typed `failureCode` preserved verbatim from agents/ when the failure originates there (see [04](04-ai-operation-lifecycle.md)) |
| Compatibility | — | **Breaking**: header mandatory; response for an already-generated assessment changes from a wasted-LLM-call error to an immediate `409`. |

## `POST /api/v1/assessments/{id}/draft/retry` — new endpoint

| | Target |
|---|---|
| Request | none (path only), no idempotency key |
| Response (success) | `202`, updated `AiOperation` |
| Response (nothing to retry) | `409`, `NO_ACTIVE_OPERATION_TO_RETRY` |
| Response (already in progress, within threshold) | `409`, `OPERATION_IN_PROGRESS` |
| Idempotency | not key-based — naturally idempotent by looking up the existing `AiOperation` for `(assessmentId, operationType)` |
| Concurrency | same `uq_ai_operations_in_flight` guard |
| Failure behavior | new `AgentAttempt` under the same `AiOperation`; `attemptNumber` incremented |

## `GET /api/v1/assessments/{id}/generation-status` — new endpoint

| | Target |
|---|---|
| Request | none |
| Response | `200`, `{ operationType, status, failureCode?, retryable, currentRevisionId? }` |
| Response (no operation ever run, no revision) | `200`, `{ operationType: "CREATE_INITIAL_REVISION", status: "NOT_STARTED", retryable: false }` |
| Failure behavior | n/a — read-only |

Powers the Web resume flow — see [05 — Web Migration](05-web-migration.md).

## `POST /api/v1/assessments/{id}/draft/regenerate` — regenerate

| | Current | Target |
|---|---|---|
| Request | `RegenerateAssessmentDraftRequest{adjustmentNotes}` | Same **+ `expectedRevisionId`**, `Idempotency-Key` header |
| Response (success) | `201` | `201`, `origin=AI_GENERATED`, `reason=adjustmentNotes` (now persisted) |
| Response (stale) | not detected — silently regenerates from whatever is current | `409`, `STALE_REVISION`, **no agent call made** |
| Idempotency | none | `(assessmentId, REGENERATE_REVISION, key)` |
| Concurrency | none — two concurrent regenerations both call the LLM and race on the unique `(assessment_id, version_number)` constraint | `expectedRevisionId` pre-check + `Assessment.lockVersion` CAS — exactly one succeeds; the loser never reaches the DB unique-constraint race because it fails the pre-check or the CAS first |
| Compatibility | — | **Breaking**: `expectedRevisionId` becomes a required body field. |

## `POST /api/v1/assessments/{id}/revisions` — new endpoint, replaces human edit

| | Current (`PATCH .../draft`) | Target |
|---|---|---|
| Request | `UpdateAssessmentDraftRequest` (all fields optional/partial) | Full content fields **+ required `expectedRevisionId`**, optional `reason` |
| Response | `200`, same row, same version | `201`, **new** `AssessmentRevision`, `origin=HUMAN_EDITED` |
| Response (stale) | not detected — last-write-wins | `409`, `STALE_REVISION` |
| Idempotency | none (not needed — every call is a distinct edit intent, not a retry-prone operation in the same sense as AI dispatch) | none required |
| Concurrency | none — two concurrent edits silently overwrite | `expectedRevisionId` + `lockVersion` CAS |
| Compatibility | — | **Breaking, endpoint retired**: `PATCH /api/v1/assessments/{id}/draft` is removed, not deprecated-and-kept. See [Authoring Operation Contract § 6](../../99-decisions/2026-07-28-authoring-operation-contract.md). |

## `GET /api/v1/assessments/{id}/draft` and `GET /api/v1/assessments/{id}/draft/versions`

| | Current | Target |
|---|---|---|
| Response | draft fields only | **additive**: `origin`, `actorId`, `reason`, `previousRevisionId` added to the existing response shape |
| Compatibility | — | Non-breaking — existing fields unchanged, new fields added |

Whether these two read paths keep the `.../draft` URL segment or move to `.../revisions`/`.../revisions/current` is an implementation-time naming choice, not a decision this plan locks — either is compatible with the contract above as long as it is applied consistently across both read endpoints in the same task.

## Typed error codes (shared across the above)

See [Authoring Operation Contract § Typed, stable error codes](../../99-decisions/2026-07-28-authoring-operation-contract.md) for the authoritative table (`IDEMPOTENCY_KEY_PAYLOAD_MISMATCH`, `ALREADY_GENERATED`, `STALE_REVISION`, `NO_ACTIVE_OPERATION_TO_RETRY`, `OPERATION_IN_PROGRESS`, `AGENT_UNAVAILABLE`, `AGENT_REJECTED`). All are surfaced through `GlobalExceptionHandler` as a structured error body (`{ code, message }`), not a bare HTTP status — Web branches on `code`, never on status alone, for the `409` cases (which are ambiguous by status code alone).

---

← [02 — Domain and Data Changes](02-domain-and-data-changes.md) | [↑ README](README.md) | [Siguiente: AI Operation Lifecycle →](04-ai-operation-lifecycle.md)
