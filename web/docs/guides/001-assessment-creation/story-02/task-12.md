# Connect Real Api Draft Builder Screen

**Source:** task-12 | **Area:** unknown | **Date:** 2026-07-21

## What it does
The Draft Builder screen loads via `loadAssessmentDraftBuilderPage`, saves edits and regenerates via the real mutations, refetches versions after either succeeds, and surfaces 404/422/500 (plus 502/503 agent errors) — the fake local dataset from `task-09` is fully removed.

---

## How to use it
- Replace `useAssessmentDraftBuilderPage`'s fake dataset with a `loadAssessmentDraftBuilderPage(assessmentId)` call on mount, using the `RemoteData` states from `task-09`.
- Replace the page hook's fake `onSave` with a call to `updateAssessmentDraft` (from `task-11`), and on success, refetch the version list (or the full page data) via `loadAssessmentDraftBuilderPage` — `DraftEditorSection`'s own props/behavior are unchanged from `task-09`, only what the page hook's `onSave` does internally changes.
- Replace the page hook's fake `onRegenerate` with a call to `regenerateAssessmentDraft` (from `task-11`), and on success, refetch the version list and update the displayed draft to the new version — same "Section props unchanged" note as step 2.
- Map 404/422/500 (plus 502/503 agent errors) to translated messages per `15-backend-frontend-contracts.md` §4, surfaced via the existing error-state UI from `task-09`. No 409 mapping exists — `task-07`/`task-08` confirmed no draft endpoint returns one.
- Extend Section tests to cover: successful save/regenerate refreshes the version list; an agent-rejected (422) or agent-down (502/503) error during regenerate shows a clear message without clearing the current draft (`task-07`); a 404 on initial load shows a "not found" state.
- Remove the fake dataset and fake `onSave`/`onRegenerate` code paths entirely from `task-09` — no leftover dead code or feature flag.

## Example
Use `Replace` through the public interface introduced by this task.
