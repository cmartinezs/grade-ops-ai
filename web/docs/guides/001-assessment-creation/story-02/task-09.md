# Functional Mockup Draft Builder Screen

**Source:** task-09 | **Area:** unknown | **Date:** 2026-07-21

## What it does
A navigable functional mockup of the Draft Builder screen at `src/app/(protected)/assessments/[id]/draft/`, built in TSX with fake/local data only — no `lib/api` calls yet — that validates the editor, regenerate action, and version history UX against realistic edge cases before any backend wiring exists.

---

## How to use it
- Create `toAssessmentDraftBuilderPageViewModel.ts` mapping a `GenerateAssessmentDraftResponse`-shaped draft + `GenerateAssessmentDraftResponse[]`-shaped versions into `{ draft: AssessmentDraftViewModel, versions: AssessmentDraftVersionViewModel[] }`.
- Create `DraftEditorSection.tsx` + `useDraftEditorSection.ts`: editable fields for title/context/instructions/objectives/deliverables/constraints, using RHF + Zod for local form state/validation only; `isSaving`/`fieldErrors`/`serverError`/`onSave` are props passed through from `useAssessmentDraftBuilderPage` (per `task-08`'s hierarchy — the Section hook does not own submitting/error state or call the mutation itself, even in this fake-data phase). Must accept and honor an `isReadOnly` prop: when `true` (previewing a non-current version), every field is disabled and the "Guardar cambios" button is hidden/disabled — `onSave` must not be callable in this state. This is not optional polish; without it, previewing a past version and saving becomes a silent restore, which `task-01`/`task-07`/`task-08` all confirm has no real endpoint.
- Create `RegenerateSection.tsx` + `useRegenerateSection.ts`: adjustment-notes textarea + regenerate button, owning only local textarea state/required-field validation; `isRegenerating`/`fieldError`/`agentError`/`onRegenerate` are props passed through from `useAssessmentDraftBuilderPage`, same ownership split as Section 1.
- Create `VersionHistorySection.tsx` + `useVersionHistorySection.ts`: read-only list of past versions, `onViewVersion` callback that swaps which version's fields are displayed in `DraftEditorSection` locally (no API call) **and sets `isReadOnly=true` on `DraftEditorSection` whenever the selected version is not the current one** (`task-08`'s finding) — selecting the current version again restores normal editing.
- Create `useAssessmentDraftBuilderPage.ts` with a fake dataset: at least one draft with long objectives/instructions text (edge case), a version list with 4+ entries (many-versions edge case), and separately test the single-current-version case (a one-item version list containing only the current draft — never an empty array, since `GET .../draft/versions` always includes the current version per `task-01`/`task-08`). Its `onSave`/`onRegenerate` are fake mutations for now (update the in-memory fake dataset directly, no network call) — `task-12` swaps these for the real `task-11` mutations plus a real `task-10` refetch without touching the Sections' props.
- Create `src/app/(protected)/assessments/[id]/draft/page.tsx` reading `params.id`, calling `useShellConfig`, composing the 3 Sections.

## Example
Use `Create` through the public interface introduced by this task.
