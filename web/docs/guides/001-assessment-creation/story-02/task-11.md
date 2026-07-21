# Mutations Draft Builder Screen

**Source:** task-11 | **Area:** unknown | **Date:** 2026-07-21

## What it does
`updateAssessmentDraft()` (partial PATCH) and `regenerateAssessmentDraft()` (POST with adjustment notes) in `lib/api`, each with proper submitting/success/error semantics per `06-estado-datos-y-api.md` §10, and each triggering a version-list refetch after success since both can produce a new version.

---

## How to use it
- Add `UpdateAssessmentDraftRequestDto` to `src/types/assessment.ts` — all fields optional, matching `task-01`'s confirmed `UpdateAssessmentDraftRequest` partial-update semantics exactly (only include keys actually being changed).
- Add `updateAssessmentDraft(assessmentId, changes)` to `src/lib/api/assessments.ts`, `PATCH`-ing `/api/v1/assessments/${assessmentId}/draft` with only the provided keys.
- Add `regenerateAssessmentDraft(assessmentId, adjustmentNotes)` `POST`-ing `/api/v1/assessments/${assessmentId}/draft/regenerate` with `{ adjustmentNotes }`.
- Write tests: `updateAssessmentDraft` sends only the changed keys (not a full object with empty-string defaults); `regenerateAssessmentDraft` sends the notes and parses the new draft; both surface 422 field/notes/agent-rejected errors and 502/503 agent-down distinctly from a generic 500, per `06-estado-datos-y-api.md` §9 and `task-07`'s traced error surface — neither surfaces a 409, since none exists for these endpoints.
- --

## Example
Use `Add` through the public interface introduced by this task.
