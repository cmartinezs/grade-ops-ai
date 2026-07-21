# ADR: The Draft Builder screen loads via `loadAssessmentDraftBuilderPage`, saves edits and regenerates via the real mutations, refetches versions after either succeeds, and surfaces 404/422/500 (plus 502/503 agent errors) — the fake local dataset from `task-09` is fully removed.

**Date:** 2026-07-21
**Status:** Accepted
**Planning:** 001-assessment-creation / story-02 / task-12

## Context
- **Approach:** Swap `useAssessmentDraftBuilderPage`'s fake dataset and fake `onSave`/`onRegenerate` (from `task-09`) for a call to `loadAssessmentDraftBuilderPage` (from `task-10`) on mount and the real mutations (from `task-11`) — no other component or prop-shape changes, since `task-09` already built the real component tree with `onSave`/`onRegenerate` owned by the page hook and passed through to the Sections unchanged (`task-08`'s hierarchy).
- **Affected files / components:**
  - `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` (replace fake data with the real loader; replace fake save/regenerate handlers with the real ones)
  - `src/app/(protected)/assessments/[id]/draft/page.tsx` (pass `params.id` through)
  - Section test files (extend for real error states)
- **Interfaces / contracts:** The page hook's public shape (`RemoteData`-style page state, per `06-estado-datos-y-api.md` §8, including `onSave`/`onRegenerate` and their `isSaving`/`isRegenerating`/error fields per `task-08`) stays what `task-09` established — only what feeds it changes, from fake data/fake mutations to `task-10`'s loader and `task-11`'s real mutation functions.
- **Risk:** Medium — per `06-estado-datos-y-api.md` §13, after regenerate (always) or a save (which always edits the current version, since the editor is read-only while previewing history per `task-08`), the version list must be refetched, not assumed stale-safe; missing this would show the teacher an outdated version list right after they just created a new version.
- **Design notes:** Per `15-backend-frontend-contracts.md` §4 and `task-07`'s traced backend contract: 404 → assessment not found (shouldn't normally happen via the normal navigation flow from `task-06`, but handle it defensively — e.g. a stale bookmark); 422 → business validation error (field validation / empty notes / agent-rejected / no-prior-draft), translate the message; 502/503 → agent-down, translate the message, current draft is never cleared. **No 409 handling** — `task-07`/`task-08` both confirmed no draft endpoint returns one (`GlobalExceptionHandler` maps 409 only for the unrelated `DuplicateEmailException`); the last-write-wins concurrency risk this replaces is a documented backend limitation, not a UI-detectable conflict.

---

## Decision
a stale bookmark); 422 → business validation error (field validation / empty notes / agent-rejected / no-prior-draft), translate the message; 502/503 → agent-down, translate the message, current draft is never cleared.

## Consequences
** Per `15-backend-frontend-contracts.md` §4 and `task-07`'s traced backend contract: 404 → assessment not found (shouldn't normally happen via the normal navigation flow from `task-06`, but handle it defensively — e.g. a stale bookmark); 422 → business validation error (field validation / empty notes / agent-rejected / no-prior-draft), translate the message; 502/503 → agent-down, translate the message, current draft is never cleared. **No 409 handling** — `task-07`/`task-08` both confirmed no draft endpoint returns one (`GlobalExceptionHandler` maps 409 only for the unrelated `DuplicateEmailException`); the last-write-wins concurrency risk this replaces is a documented backend limitation, not a UI-detectable conflict.

## Alternatives Considered
a stale bookmark); 422 → business validation error (field validation / empty notes / agent-rejected / no-prior-draft), translate the message; 502/503 → agent-down, translate the message, current draft is never cleared.
