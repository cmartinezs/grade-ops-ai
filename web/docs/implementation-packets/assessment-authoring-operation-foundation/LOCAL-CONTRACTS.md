<a id="top"></a>

# LOCAL-CONTRACTS — Web: Assessment Authoring Operation Foundation

**Status:** Ready — frozen. **Parent:** [README](README.md) · **Prev:** [TASKS](TASKS.md) · **Next:** [TEST-PLAN →](TEST-PLAN.md)

## API ↔ Web public contract

| Operation | Endpoint | Idempotency-Key | `expectedRevisionId` | Success | Conflict responses |
|---|---|---|---|---|---|
| Create assessment | `POST /api/v1/assessments` | Required | n/a | `201` | `400` missing header, `409 IDEMPOTENCY_KEY_PAYLOAD_MISMATCH` |
| Generate initial revision | `POST /api/v1/assessments/{id}/draft` | Required | n/a | `201` or `202` (durable operation created, failure/in-progress) | `409 ALREADY_GENERATED` |
| Retry operation | `POST /api/v1/assessments/{id}/draft/retry` | None | n/a | `202` | `409 NO_ACTIVE_OPERATION_TO_RETRY`, `409 OPERATION_IN_PROGRESS` |
| Get generation status | `GET /api/v1/assessments/{id}/generation-status` | n/a | n/a | `200`, `{ operationType, status, failureCode?, retryable, currentRevisionId? }` | n/a |
| Regenerate | `POST /api/v1/assessments/{id}/draft/regenerate` | Required | Required | `201` | `409 STALE_REVISION` |
| Create human revision | `POST /api/v1/assessments/{id}/revisions` | None | Required | `201` | `409 STALE_REVISION` |
| Get current/history | `GET /api/v1/assessments/{id}/draft`, `GET .../draft/versions` | n/a | n/a | `200`, additive `origin`/`actorId`/`reason`/`previousRevisionId` fields | n/a |

`PATCH /api/v1/assessments/{id}/draft` is **removed**. Do not call it after this task lands — `web/` is the API's only consumer, there is no compatibility window.

All `409` errors are structured error bodies (`{ code, message }`), not bare status codes — always branch on `code`, never on status alone (`409` is ambiguous by itself).

## Canonical status taxonomy

| Layer | Values | Where Web sees it |
|---|---|---|
| `GET .../generation-status` response `status` | `NOT_STARTED`, `IN_PROGRESS`, `FAILED_RETRYABLE`, `INDETERMINATE` (plus implicit success signaled by `currentRevisionId` being non-null) | Directly, this is the read model Web polls |

Use exactly these spellings. Do not invent a client-side synonym (`FAILED_FINAL`, `RUNNING`, etc. are not real values in this cut).

## Canonical failure-code taxonomy

| Code | Meaning | Web's response |
|---|---|---|
| `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH` | Same key, different request body | Do not silently retry with a new key — surface/log the conflict |
| `ALREADY_GENERATED` | Initial generation requested twice | Fetch and display the existing current revision instead |
| `STALE_REVISION` | `expectedRevisionId` no longer current | Show conflict explicitly, offer reload, discard in-flight edit |
| `NO_ACTIVE_OPERATION_TO_RETRY` | Retry called with nothing to retry | Should not be reachable from the UI if state tracking is correct — treat as an unexpected-state error if it occurs |
| `OPERATION_IN_PROGRESS` | An attempt may still be running | Show pending state, do not offer retry |
| `AGENT_UNAVAILABLE`, `AGENT_ERROR` | Transient transport/provider failure | Retryable — show "Retry" action |
| `AGENT_REJECTED`, `INVALID_COMMAND` | Non-retryable failure | Show error, no retry action, suggest editing input |
| `MALFORMED_OUTPUT` | LLM output shape failure | Retryable — show "Retry" action |
| `STALE_ON_COMPLETION` | Late AI success arrived after `expectedRevisionId` moved | Terminal for that operation — the user must start a fresh operation, not retry this one |

## New/changed UI states

| State | Trigger | Rendered as |
|---|---|---|
| `idle` | initial | intake form (unchanged) |
| `submitting` | form submit | unchanged |
| `assessment created, generation pending` | create succeeded, generate call in flight | unchanged |
| `assessment created, generation failed (retryable)` | generate call returned `202` with `AiOperation.status = FAILED_RETRYABLE` | **New** — explicit "Generation failed — Retry" affordance, assessment id retained in the URL/route so a reload does not lose it |
| `assessment created, generation in progress` (post-reload) | `GET generation-status` returns `IN_PROGRESS` | **New** — pending state, no retry button (avoids double-dispatch) |
| `resume after reload` | user navigates back to an assessment with `currentRevisionId = null` | **New** — calls `GET generation-status` on load instead of assuming "not found" |
| `stale revision conflict` | regenerate/edit returns `409 STALE_REVISION` | **New** — explicit conflict message + reload-and-retry action, not a generic error toast |
| `ready` | `currentRevisionId` set | unchanged draft builder view, now reading `origin`/`actorId`/`reason` from the response to render provenance instead of the current transient, non-authoritative local label |

## Recovery after refresh

```text
if assessment.currentRevisionId is null:
    call GET .../generation-status
    NOT_STARTED       → show "Generate" action (first-time path, unchanged)
    IN_PROGRESS        → show pending state, poll or show manual refresh
    FAILED_RETRYABLE   → show "Retry" action → POST .../draft/retry
    INDETERMINATE       → show "Status unknown — retry with caution"
else:
    render current revision as today
```

## Conflict handling

Regenerate and human-edit calls send `expectedRevisionId` — the id of the revision currently displayed. A `409 STALE_REVISION` means the current revision changed since the page loaded (another tab, a concurrent regenerate). Response: show the conflict explicitly, offer "reload to see the latest version," discard the user's in-flight edit rather than silently resubmitting over it.

## Idempotency key lifecycle

- **Created:** once per user-initiated submit action (form submit, retry click, regenerate click) — a UUID generated client-side.
- **Sent:** as the `Idempotency-Key` header on that specific request.
- **Reused:** only for automatic retries of the *same* logical submit (e.g., a network-level retry of a request that never got a response) — never regenerated for what is really the same user action.
- **Discarded:** once a terminal response (success or a definitive error) is received for that key. Ephemeral React state may hold it during the in-flight window; it does not need to survive a page reload, because what makes reload-then-retry safe is the *assessment id* plus server-side operation state (`generation-status`), not the client remembering the original key.
- **Never** the sole record of a durable operation — the server's `AiOperation`/`generation-status` is authoritative; Web's local state is a cache of it, not a substitute for it.

## What Web must not do

Coordinate authoritative business rules; invent states not in this document; assume success from the absence of an error; treat ephemeral React state as the sole record of a durable operation; create a second `Assessment` on retry; hide a conflict from the user; mutate a displayed revision locally without a round trip to the server.

---

← [TASKS](TASKS.md) | [↑ inicio](#top) | [Siguiente: TEST-PLAN →](TEST-PLAN.md)
