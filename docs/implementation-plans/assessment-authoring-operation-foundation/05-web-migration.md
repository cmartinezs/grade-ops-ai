<a id="top"></a>

# 05 — Web Migration

**Parent:** [README](README.md) · **Status:** Planned · **Prev:** [04 — AI Operation Lifecycle](04-ai-operation-lifecycle.md) · **Next:** [06 — Database Migration →](06-database-migration.md)

Current Web orchestration evidence: `web/src/hooks/useIntakeAssessmentPage.ts`, `web/src/hooks/useAssessmentDraftBuilderPage.ts`, `web/src/lib/api/assessments.ts` (Research 02 E-020..E-023).

## What Web stops orchestrating

Today, `submitAssessmentBrief` (in `web/src/lib/api/assessments.ts`) owns the *only* recovery logic for a failed generation: none. It calls create, then generate, and if generate fails, the assessment id is not retained anywhere the UI can act on — the intake page's only affordance is "submit the form again," which creates a second `Assessment` (Research 02 §5.6). After this cut, Web still calls the same two endpoints in the same order, but:

- it no longer needs to invent recovery behavior — the server is addressable (`GET generation-status`) and resumable (`POST .../draft/retry`) regardless of what the client does after a failure;
- it must generate and send an `Idempotency-Key` per user-initiated submit, so a double form submission (double-click, refresh-and-resubmit) cannot create a second `Assessment` even without any recovery UI at all.

## New/changed UI states

| State | Trigger | Rendered as |
|---|---|---|
| `idle` | initial | intake form (unchanged) |
| `submitting` | form submit | unchanged |
| `assessment created, generation pending` | create succeeded, generate call in flight | unchanged — same as today's brief `submitting`→`success` window |
| `assessment created, generation failed (retryable)` | generate call returned `202` with `AiOperation.status = FAILED_RETRYABLE` | **new** — explicit "Generation failed — Retry" affordance, assessment id retained in the URL/route so a page reload does not lose it |
| `assessment created, generation in progress` (post-reload) | `GET generation-status` returns `IN_PROGRESS` | **new** — pending state, no retry button shown (avoids double-dispatch) |
| `resume after reload` | user navigates back to an assessment with `currentRevisionId = null` | **new** — calls `GET generation-status` on load instead of assuming "not found" |
| `stale revision conflict` | regenerate/edit returns `409 STALE_REVISION` | **new** — explicit conflict message + reload-and-retry action, not a generic error toast |
| `ready` | `currentRevisionId` set | unchanged draft builder view, now reading `origin`/`actorId`/`reason` from the response to render provenance (e.g., "AI-generated" vs. "Edited by you") instead of the current transient, non-authoritative local label (`ST-031`/`ST-032` in Research 02 — `generado-por-ia`/`version-actual` reset on reload today) |

## Recovery after refresh

This is the concrete fix for Research 02's demonstrated dead end. On loading an assessment page:

```text
if assessment.currentRevisionId is null:
    call GET .../generation-status
    NOT_STARTED       → show "Generate" action (first-time path, unchanged)
    IN_PROGRESS        → show pending state, poll or show manual refresh
    FAILED_RETRYABLE   → show "Retry" action → POST .../draft/retry
    INDETERMINATE       → show "Status unknown — retry with caution" (see 04-ai-operation-lifecycle.md)
else:
    render current revision as today
```

## Conflict handling

Regenerate and human-edit calls now send `expectedRevisionId` (the id of the revision currently displayed). A `409 STALE_REVISION` response means someone/something else changed the current revision since the page loaded (another tab, a concurrent regenerate). Web's response: show the conflict explicitly, offer "reload to see the latest version," and discard the user's in-flight edit rather than silently resubmitting over it — this is the client-side half of closing "last-write-wins silencioso."

## Backward compatibility

`web/` is this API's only consumer (`2026-07-20-api-agent-orchestration.md`). There is no compatibility window to maintain for `PATCH .../draft` — it is removed, and Web's edit call switches to `POST .../revisions` in the same implementation cut (task 11 in [08 — Implementation Sequence](08-implementation-sequence.md)), not on a deferred schedule.

## Out of scope for this plan

Building the actual React components/hooks for the new states above is implementation work covered by task 11; this document specifies the contract Web must satisfy, not the component tree. No visual design work is authorized by this plan — if new UI patterns are needed beyond wiring existing components to new states, that goes through the design-system workflow (`design-system/workflow/`), not this implementation plan.

---

← [04 — AI Operation Lifecycle](04-ai-operation-lifecycle.md) | [↑ README](README.md) | [Siguiente: Database Migration →](06-database-migration.md)
