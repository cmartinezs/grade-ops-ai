# ADR: A navigable functional mockup of the Draft Builder screen at `src/app/(protected)/assessments/[id]/draft/`, built in TSX with fake/local data only — no `lib/api` calls yet — that validates the editor, regenerate action, and version history UX against realistic edge cases before any backend wiring exists.

**Date:** 2026-07-21
**Status:** Accepted
**Planning:** 001-assessment-creation / story-02 / task-09

## Context
- **Approach:** Build the real component tree from `task-08`'s hierarchy now, with `useAssessmentDraftBuilderPage` returning fake local data (a hardcoded draft + version list) instead of calling the loader from `task-10`. This lets `task-12` swap in the real Screen Data Facade without touching the Sections' UI.
- **Affected files / components:**
  - `src/app/(protected)/assessments/[id]/draft/page.tsx`
  - `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` (fake data for now)
  - `src/features/assessment-creation/components/{DraftEditorSection,RegenerateSection,VersionHistorySection}.tsx` + their hooks
  - `src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts` (real mapper, fed fake DTOs)
  - `src/features/assessment-creation/components/__tests__/{DraftEditorSection,RegenerateSection,VersionHistorySection}.test.tsx`
- **Interfaces / contracts:** `AssessmentDraftViewModel`/`AssessmentDraftVersionViewModel` are the real, final view model shapes `task-10`'s loader will produce — not placeholders. The mapper (`toAssessmentDraftBuilderPageViewModel`) is written for real now, fed fake DTOs shaped like `task-01`'s confirmed `GenerateAssessmentDraftResponse`.
- **Risk:** Medium — per `02-ux-wireframes-y-maquetas.md` §6, fake data must cover long AI-generated text, many versions, and a single current-only version (never an empty versions array — `GET .../draft/versions` always includes the current version, `task-01`/`task-08`), not one symmetric happy path; under-covering this here means UX problems surface only after `task-12`'s real wiring, which is more expensive to fix.
- **Design notes:** `RemoteData<T>`-style states per `06-estado-datos-y-api.md` §8, not loose booleans, for the page-level loading/ready/error state. Build the 3 Sections' fields from the DS primitives `task-14` produces (`Field`/`Input`/`Textarea`), per `pdr-001-design-system-form-primitives.md` — but compose them directly rather than through `DynamicForm`, since each Section has per-field custom behavior (draft-editor multi-field save, regenerate's own submitting state, version-switching) that a declarative field list doesn't fit (PDR-001 decision item 5).

---

## Decision
- **Approach:** Build the real component tree from `task-08`'s hierarchy now, with `useAssessmentDraftBuilderPage` returning fake local data (a hardcoded draft + version list) instead of calling the loader from `task-10`.

## Consequences
** `RemoteData<T>`-style states per `06-estado-datos-y-api.md` §8, not loose booleans, for the page-level loading/ready/error state. Build the 3 Sections' fields from the DS primitives `task-14` produces (`Field`/`Input`/`Textarea`), per `pdr-001-design-system-form-primitives.md` — but compose them directly rather than through `DynamicForm`, since each Section has per-field custom behavior (draft-editor multi-field save, regenerate's own submitting state, version-switching) that a declarative field list doesn't fit (PDR-001 decision item 5).

## Alternatives Considered
- **Approach:** Build the real component tree from `task-08`'s hierarchy now, with `useAssessmentDraftBuilderPage` returning fake local data (a hardcoded draft + version list) instead of calling the loader from `task-10`.
