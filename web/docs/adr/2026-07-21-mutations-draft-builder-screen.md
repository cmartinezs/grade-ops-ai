# ADR: `updateAssessmentDraft()` (partial PATCH) and `regenerateAssessmentDraft()` (POST with adjustment notes) in `lib/api`, each with proper submitting/success/error semantics per `06-estado-datos-y-api.md` §10, and each triggering a version-list refetch after success since both can produce a new version.

**Date:** 2026-07-21
**Status:** Accepted
**Planning:** 001-assessment-creation / story-02 / task-11

## Context
- **Approach:** Two separate mutation functions, not one generic "updateDraft" — `PATCH` (edit) and regenerate (`POST .../regenerate`) hit different endpoints with different request shapes (`task-01`) and different semantics (editing the current version in place vs. producing a new version via AI). Both must trigger a refetch of the version list afterward per `06-estado-datos-y-api.md` §13 (sync with backend after a critical mutation) — regenerate always creates a new version, and a save always edits the current version (per `task-08`'s hierarchy, `PATCH` is only reachable while viewing the current version — the editor is read-only while previewing history, so there is no "PATCH while viewing a past version" case to design for).
- **No 409 handling (correction — this task previously assumed one):** `task-07` traced the real backend and found neither `UpdateAssessmentDraftHandler` nor `RegenerateAssessmentDraftHandler` implements optimistic locking, and `GlobalExceptionHandler` maps 409 only for `DuplicateEmailException` (an auth-domain exception unrelated to drafts) — no draft endpoint can ever return 409. Both mutation functions here surface the real error surface instead: 422 (field validation / empty notes / agent-rejected / no-prior-draft), 502/503 (agent down), and 500. The last-write-wins concurrency risk this replaces is a documented, unresolved backend limitation (`task-07`), not something a 409 branch in this task could ever catch.
- **Affected files / components:**
  - `src/lib/api/assessments.ts` (add `updateAssessmentDraft()`, `regenerateAssessmentDraft()`)
  - `src/lib/api/__tests__/assessments.test.ts` (extend)
- **Interfaces / contracts:**
  ```ts
  export interface UpdateAssessmentDraftRequestDto {
    title?: string;
    context?: string;
    instructions?: string;
    objectives?: string[];
    deliverables?: string[];
    constraints?: string[];
  }
  export async function updateAssessmentDraft(
    assessmentId: string,
    changes: UpdateAssessmentDraftRequestDto
  ): Promise<AssessmentDraftDto>
  export async function regenerateAssessmentDraft(
    assessmentId: string,
    adjustmentNotes: string
  ): Promise<AssessmentDraftDto>
  ```
  Both return the same `AssessmentDraftDto` shape from `task-10`.
- **Risk:** Medium — `UpdateAssessmentDraftRequestDto`'s fields are all optional per `task-01`'s confirmed contract (`null`/absent means "don't change this field"); a caller that sends an empty string instead of omitting a field would unintentionally blank it out server-side. `updateAssessmentDraft` must only include keys the caller actually changed, not all fields with empty-string defaults.
- **Design notes:** Neither function refetches the version list itself — per `06-estado-datos-y-api.md` §13's own framing, that's a page-level concern; `task-12`'s hook is responsible for calling `loadAssessmentDraftBuilderPage` again (or at least the versions half) after either mutation succeeds. This task only guarantees the two functions themselves are correct.

---

## Decision
Both mutation functions here surface the real error surface instead: 422 (field validation / empty notes / agent-rejected / no-prior-draft), 502/503 (agent down), and 500.

- **Risk:** Medium — `UpdateAssessmentDraftRequestDto`'s fields are all optional per `task-01`'s confirmed contract (`null`/absent means "don't change this field"); a caller that sends an empty string instead of omitting a field would unintentionally blank it out server-side.

## Consequences
** Neither function refetches the version list itself — per `06-estado-datos-y-api.md` §13's own framing, that's a page-level concern; `task-12`'s hook is responsible for calling `loadAssessmentDraftBuilderPage` again (or at least the versions half) after either mutation succeeds. This task only guarantees the two functions themselves are correct.

## Alternatives Considered
Both mutation functions here surface the real error surface instead: 422 (field validation / empty notes / agent-rejected / no-prior-draft), 502/503 (agent down), and 500.

- **Risk:** Medium — `UpdateAssessmentDraftRequestDto`'s fields are all optional per `task-01`'s confirmed contract (`null`/absent means "don't change this field"); a caller that sends an empty string instead of omitting a field would unintentionally blank it out server-side.
