<a id="top"></a>

# Authoring Operation Contract

- Status: Accepted
- Date: 2026-07-28
- Decision owner: Technical
- Informed by: [Research 01](../../design-system/research/research-01-current-domain-data-model-alignment-audit.md), [Research 02](../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md)
- Depends on: [Assessment Authoring Model](2026-07-28-assessment-authoring-model.md)
- Related decisions: [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md), [Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md), [API-Agent Orchestration And REST Maturity](2026-07-20-api-agent-orchestration.md), [UI Design/Data Semantics](2026-07-21-ui-design-data-semantics.md)

## Context

Research 02 CRITICAL-02 documents a real, reproducible defect: `web/` creates an assessment with `POST /assessments`, then calls `POST /assessments/{id}/draft` to generate the first revision. If the second call fails — `agents/` unavailable, timeout, malformed output — the assessment and brief remain committed, but there is no UI path back to that assessment; the intake page only knows how to submit a new form, which creates a second `Assessment`. This is Web acting as the coordinator of a distributed operation it cannot make atomic, exactly the failure mode the task brief warns against.

This decision defines the functional contract for six operations — create intent, generate initial revision, retry failed generation, resume an existing assessment, regenerate, and create a human revision — so that no operation requires Web to paper over partial completion.

## Problem

Which of these are separate HTTP calls versus one compound command, what a client gets back when generation fails, how a client resumes without creating a duplicate `Assessment`, and which HTTP status/error codes are stable enough for Web to branch on.

## Decision

### Create and generate remain two calls, not one compound command

```text
POST /api/v1/assessments                          — create intent (Assessment + AssessmentBrief)
POST /api/v1/assessments/{id}/draft                — generate initial revision
```

They are **not** merged into a single endpoint. Merging would bundle a trivial, fast DB write with a slow (5-60s), failure-prone LLM call into one HTTP request, which conflicts with `2026-07-21-ui-design-data-semantics.md`'s requirement to declare sync/async completion model explicitly per action — a merged endpoint would need to declare a completion model for a request that is itself two different kinds of work. Keeping them separate preserves the option to make *only* the generation call asynchronous later (§ Open Consequences) without touching intent creation at all.

What changes is that both calls become idempotent and addressable, and the second call becomes safely retryable against the *same* assessment instead of dead-ending.

### 1 — Create assessment intent

`POST /api/v1/assessments`, unchanged path/method, now requires an `Idempotency-Key` header.

- Scope: `(teacherUid, "CREATE_ASSESSMENT_BRIEF", idempotencyKey)` — see [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md).
- Same key + same payload → replays the original `201` response body (does not create a second `Assessment`).
- Same key + different payload → `409 Conflict`, typed `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH`.
- Missing header: for this cut, rejected with `400` — the header is mandatory, not best-effort, because this exact gap (double-submit creates two assessments) is the reproduced defect.

### 2 — Generate initial revision

`POST /api/v1/assessments/{id}/draft`, unchanged path/method, requires `Idempotency-Key`.

- Precondition check, in order: assessment exists and is owned by the caller → brief exists → **no current revision exists yet**. If a current revision already exists, this call does **not** silently regenerate or error with a raw unique-constraint violation (today's behavior once the DB constraint is hit after the LLM has already been called) — it returns `409 Conflict`, typed `ALREADY_GENERATED`, carrying the existing revision. This closes Research 02's "a second call vuelve a intentar `versionNumber = 1`" gap.
- On failure (agent unreachable, rejected, malformed output), the response is `202 Accepted` with a body describing the `AiOperation` (id, status `FAILED_RETRYABLE` or `FAILED_TERMINAL`, typed failure code) rather than a bare `5xx`/`502`. The assessment remains addressable — see Resume, below. (Returning `202` for a call that fully executed synchronously is intentional: it signals "an operation record was durably created for this request," which is the contract Web needs, independent of whether the underlying call was sync or async — see [Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md).)
- On success: `201 Created` with the new `AssessmentRevision` (including `origin`, `actorId`, `previousRevisionId = null`).

### 3 — Retry a failed generation

`POST /api/v1/assessments/{id}/draft/retry`, new endpoint, **no idempotency key required** and no request body.

Retry is deliberately not "call endpoint 2 again with a new key." It looks up the existing `AiOperation` for `(assessmentId, operationType)`:

- If none exists, or the latest is not in a retryable state → `409 Conflict`, typed `NO_ACTIVE_OPERATION_TO_RETRY`.
- If the latest is `FAILED_RETRYABLE` → dispatches a new `AgentAttempt` under the **same** `AiOperation` (not a new one — "retries quedan asociados a la misma operación lógica").
- If the latest is `PENDING`/`IN_PROGRESS` and not past the indeterminate threshold ([Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md)) → `409 Conflict`, typed `OPERATION_IN_PROGRESS` (refuses to double-dispatch while a real attempt may still be running).
- If the latest is `IN_PROGRESS` past the indeterminate threshold → allowed, flagged as a stale-retry in the resulting attempt's metadata (documented residual risk, not hidden — see Durable AI Operation Model, "orphaned in-flight" handling).

### 4 — Resume an existing assessment

Not a distinct endpoint — a Web capability built on a new read model:

`GET /api/v1/assessments/{id}/generation-status` → `{ operationType, status, failureCode?, retryable, currentRevisionId? }`.

On load, Web asks this endpoint whenever `currentRevisionId` is absent from the assessment summary. `status = FAILED_RETRYABLE` renders a Retry action calling endpoint 3. `status = IN_PROGRESS` renders a pending state. This is what makes "reload the page after a failed generation" a recoverable path instead of a dead end (Research 02 §5.6, "punto muerto demostrado").

A generic, provider-agnostic `/ai-operations/{id}` resource is deferred — this cut only needs assessment-scoped visibility for one operation type; generalizing is additive once a second agent needs the same visibility pattern.

### 5 — Regenerate

`POST /api/v1/assessments/{id}/draft/regenerate`, unchanged path/method, requires `Idempotency-Key` **and** `expectedRevisionId` in the body (replacing the implicit "whatever is current" assumption).

- `expectedRevisionId` must equal `Assessment.currentRevisionId`. Mismatch → `409 Conflict`, typed `STALE_REVISION`, before any call to `agents/` is made (see [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md) for why this check must happen pre-dispatch).
- `adjustmentNotes` is persisted onto the resulting revision's `reason` column — today it is sent to `agents/` but never stored (Research 02 §11, "Regenerate draft" gaps).
- On success: `201 Created` with the new revision (`origin = AI_GENERATED`, `previousRevisionId = expectedRevisionId`).

### 6 — Create a human revision

`POST /api/v1/assessments/{id}/revisions`, **new endpoint**, replacing `PATCH /api/v1/assessments/{id}/draft`.

- Body: edited content fields + `expectedRevisionId` (+ optional `reason`).
- CAS against `Assessment.currentRevisionId`, same as regenerate. Mismatch → `409 Conflict`, `STALE_REVISION`.
- On success: `201 Created`, `origin = HUMAN_EDITED`, no call to `agents/`, no `AiOperation` created.
- `PATCH /api/v1/assessments/{id}/draft` is retired in the same cut, not kept as a parallel compatibility path. `web/` is this API's only consumer today (`docs/99-decisions/2026-07-20-api-agent-orchestration.md`), so there is no external contract to protect with a dual-path compromise; the break is documented here and executed atomically with the Web migration task in the implementation plan, rather than left open-ended.

Creating a new resource (a revision) via `POST` to a sub-collection is also the more Richardson-mature choice than `PATCH`-ing the assessment's "draft" as if it were one resource being partially updated — consistent with `2026-07-20-api-agent-orchestration.md`'s REST maturity gate.

### Typed, stable error codes

| Code | HTTP | Retryable by client | Meaning |
|---|---|---|---|
| `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH` | 409 | No — new key required | Same key, different request body |
| `ALREADY_GENERATED` | 409 | N/A — use current revision | Initial generation requested twice |
| `STALE_REVISION` | 409 | Yes — reload and resubmit | `expectedRevisionId` no longer current |
| `NO_ACTIVE_OPERATION_TO_RETRY` | 409 | No | Retry called with nothing to retry |
| `OPERATION_IN_PROGRESS` | 409 | Yes — poll, then retry | An attempt may still be running |
| `AGENT_UNAVAILABLE` | 502/503 (unchanged) | Yes | Transient transport failure |
| `AGENT_REJECTED` | 422 (unchanged) | No — fix input | Agents-side validation/output failure |

This directly answers "qué errores son recuperables" — every code above states whether the client should retry, resubmit with fresh state, or stop.

### Sync now, async-ready later

The generation call stays **synchronous with durable state** in this cut: the HTTP request blocks until `agents/` responds, exactly as today, but the `AiOperation`/`AgentAttempt` durable records ([Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md)) are written regardless of that synchronous wrapper. Moving to `202 Accepted` + polling later changes only the controller's response shape, not the persistence model — this is the concrete meaning of "durabilidad e idempotencia no implican obligatoriamente asincronía distribuida."

## Decision Drivers

- Research 02 CRITICAL-02, CRITICAL-03: non-atomic Web orchestration; AI execution not durable before dispatch.
- Research 02 §5.6: the demonstrated dead end (failed generation with no recoverable UI path).
- `2026-07-20-api-agent-orchestration.md`: API is the sole intermediary; Richardson REST maturity is a stated gate for new endpoints.
- `2026-07-21-ui-design-data-semantics.md`: every action must declare sync/async and its completion model explicitly.
- No Kafka, queue, or distributed saga: the task explicitly requires demonstrating durability/idempotency without unjustified distributed infrastructure.

## Alternatives Considered

| Option | Description | Verdict |
|---|---|---|
| A — Single compound endpoint | Merge create + generate into one call | Rejected: forces one completion-model declaration onto two different kinds of work; removes the option to async only the slow half later |
| B — Saga/process manager | Coordinate create+generate via an explicit saga component | Rejected: no distributed transaction exists here (one local Postgres, one internal HTTP call to `agents/`) — a saga adds orchestration machinery to solve a problem a durable operation record + idempotency key already solves |
| C — Two endpoints, idempotent + resumable | As decided above | **Adopted** |
| D (for human edit) — Keep `PATCH`, add `expectedRevisionId` to it | Preserve the URL, change semantics under the hood | Rejected: `PATCH` implies mutating the existing resource; the resulting behavior (a new row, old row untouched) is a `POST` of a new revision, not a patch — keeping the verb would misrepresent the operation to any future client reading only the HTTP contract |

## Consequences

- Web's intake flow keeps the same two-call shape but stops being able to silently duplicate an `Assessment` on retry, and gains an explicit resume path — detailed in the implementation plan's [Web migration](../implementation-plans/assessment-authoring-operation-foundation/05-web-migration.md).
- A new read endpoint (`generation-status`) and a new action endpoint (`retry`) are added; `PATCH .../draft` is removed and replaced by `POST .../revisions`.
- Every mutating authoring call now requires either an `Idempotency-Key`, an `expectedRevisionId`, or both — this is a breaking contract change for the two existing generation/regeneration calls, documented rather than hidden behind optional-header behavior.

## Migration Impact

- `Idempotency-Key` becomes mandatory on create/generate/regenerate; Web must be updated to send it in the same cut (no server-side default/synthesized-key fallback — a fallback would defeat the guarantee).
- `expectedRevisionId` becomes mandatory on regenerate/create-human-revision; Web must track the currently-displayed revision id to supply it.

## Compatibility Impact

- Breaking, scoped, and intentional: `PATCH /api/v1/assessments/{id}/draft` is removed. No external consumer exists (`web/` is updated in the same cut). No versioned public API contract is broken because none is published yet.
- `GET /api/v1/assessments/{id}/draft` and `GET .../draft/versions` keep their shapes but gain the new provenance fields (`origin`, `actorId`, `reason`, `previousRevisionId`) additively.

## Security Impact

- `Idempotency-Key` scope includes `teacherUid` for the create call specifically so one teacher cannot replay or collide with another teacher's key (addresses §9 of the task brief: "imposibilidad de reutilizar idempotency keys entre usuarios").
- `expectedRevisionId`/`ALREADY_GENERATED`/`STALE_REVISION` checks run under the same ownership verification as today (`OwnershipVerifier`) — a client cannot probe or affect another teacher's assessment by guessing a revision id, since the 404-for-unauthorized-or-missing pattern (Research 01 §11) is preserved ahead of the CAS check.

## Testing Impact

- API acceptance tests per operation: happy path, idempotent replay, payload-mismatch conflict, stale-revision conflict, already-generated conflict, retry-with-nothing-to-retry conflict.
- A dedicated test reproduces the exact Research 02 §5.6 scenario (brief created, generation fails, reload, retry succeeds without a second `Assessment`) as a regression guard.
- Web acceptance test for the resume flow (fail → reload → see retry action → retry → success).

## Open Consequences (Explicitly Deferred)

- Moving generation to `202 Accepted` + polling/webhook is compatible with this contract but not implemented now — the response shape would change, the persistence model would not.
- A general-purpose `/ai-operations/{id}` resource, once a second agent needs the same status-polling shape.
- Rate limiting / abuse protection on retry is not addressed here (pilot-scale, single-tenant-per-teacher risk is low); flagged for the release that introduces multi-teacher/institutional load.

## Related Decisions

- [Assessment Authoring Model](2026-07-28-assessment-authoring-model.md)
- [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md)
- [Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md)
- [API-Agent Orchestration And REST Maturity](2026-07-20-api-agent-orchestration.md)
- [UI Design/Data Semantics](2026-07-21-ui-design-data-semantics.md)

## References

- [Research 02 — Workflow & Lifecycle Alignment Audit](../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md), §11 "Assessment Authoring", §16 "Failure and Recovery Analysis", §26 "First Implementation Boundary Recommendation"
- `api/src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/AssessmentController.java`
- `web/src/lib/api/assessments.ts`, `web/src/hooks/useIntakeAssessmentPage.ts` (see Research 02 E-020..E-023)

---

← [Assessment Authoring Model](2026-07-28-assessment-authoring-model.md) | [↑ inicio](#top) | [Siguiente: Idempotency and Concurrency Strategy →](2026-07-28-idempotency-and-concurrency-strategy.md)
