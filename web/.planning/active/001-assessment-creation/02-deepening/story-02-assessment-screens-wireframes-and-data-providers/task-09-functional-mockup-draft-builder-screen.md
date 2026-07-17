# ⚛️ TASK 09 — functional-mockup-draft-builder-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-07, task-08, task-14
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A navigable functional mockup of the Draft Builder screen at `src/app/(protected)/assessments/[id]/draft/`, built in TSX with fake/local data only — no `lib/api` calls yet — that validates the editor, regenerate action, and version history UX against realistic edge cases before any backend wiring exists.

---

## Technical Design

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

## Implementation Steps

1. Create `toAssessmentDraftBuilderPageViewModel.ts` mapping a `GenerateAssessmentDraftResponse`-shaped draft + `GenerateAssessmentDraftResponse[]`-shaped versions into `{ draft: AssessmentDraftViewModel, versions: AssessmentDraftVersionViewModel[] }`.
2. Create `DraftEditorSection.tsx` + `useDraftEditorSection.ts`: editable fields for title/context/instructions/objectives/deliverables/constraints, using RHF + Zod for local form state/validation only; `isSaving`/`fieldErrors`/`serverError`/`onSave` are props passed through from `useAssessmentDraftBuilderPage` (per `task-08`'s hierarchy — the Section hook does not own submitting/error state or call the mutation itself, even in this fake-data phase). Must accept and honor an `isReadOnly` prop: when `true` (previewing a non-current version), every field is disabled and the "Guardar cambios" button is hidden/disabled — `onSave` must not be callable in this state. This is not optional polish; without it, previewing a past version and saving becomes a silent restore, which `task-01`/`task-07`/`task-08` all confirm has no real endpoint.
3. Create `RegenerateSection.tsx` + `useRegenerateSection.ts`: adjustment-notes textarea + regenerate button, owning only local textarea state/required-field validation; `isRegenerating`/`fieldError`/`agentError`/`onRegenerate` are props passed through from `useAssessmentDraftBuilderPage`, same ownership split as Section 1.
4. Create `VersionHistorySection.tsx` + `useVersionHistorySection.ts`: read-only list of past versions, `onViewVersion` callback that swaps which version's fields are displayed in `DraftEditorSection` locally (no API call) **and sets `isReadOnly=true` on `DraftEditorSection` whenever the selected version is not the current one** (`task-08`'s finding) — selecting the current version again restores normal editing.
5. Create `useAssessmentDraftBuilderPage.ts` with a fake dataset: at least one draft with long objectives/instructions text (edge case), a version list with 4+ entries (many-versions edge case), and separately test the single-current-version case (a one-item version list containing only the current draft — never an empty array, since `GET .../draft/versions` always includes the current version per `task-01`/`task-08`). Its `onSave`/`onRegenerate` are fake mutations for now (update the in-memory fake dataset directly, no network call) — `task-12` swaps these for the real `task-11` mutations plus a real `task-10` refetch without touching the Sections' props.
6. Create `src/app/(protected)/assessments/[id]/draft/page.tsx` reading `params.id`, calling `useShellConfig`, composing the 3 Sections.
7. Write component tests for all 3 Sections covering: editing and calling `onSave`, regenerating and calling `onRegenerate`, switching versions via `onViewVersion` and confirming the editor reflects the selected version, **selecting a historical version disables editing and prevents `onSave` from being called, and returning to the current version re-enables both**.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Editing a field and saving calls `onSave` with the updated values | `npm run test -- DraftEditorSection` |
| 2 | Entering adjustment notes and regenerating calls `onRegenerate` with the notes | `npm run test -- RegenerateSection` |
| 3 | Selecting a past version updates the editor's displayed fields to that version's content, disables all editor fields, and hides/disables "Guardar cambios" (`onSave` is never invoked while a historical version is selected) | `npm run test -- VersionHistorySection` and `npm run test -- DraftEditorSection` |
| 4 | Long text (500+ characters) in objectives/instructions renders without layout breakage | Manual visual check with the long-text fake dataset |
| 5 | Single-current-version state (one-item versions list, no prior history) renders without a broken UI | `npm run test` with a one-item versions fixture |
| 6 | Returning to the current version from a historical selection re-enables editing and "Guardar cambios" | `npm run test -- VersionHistorySection` |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | Changed surface responds correctly | Navigate to `/assessments/any-id/draft`, confirm the fake draft/versions render and all 3 sections are interactive |
| 3 | No startup regressions are visible | Inspect `npm run dev` output for new errors |

### Database / ORM Consistency Check

N/A — no database or ORM involved.

### Logging / Observability

N/A at this stage — no real network calls exist yet in this task; revisit in `task-12`, matching the same deferral pattern used in `task-04`/`task-06` for the Intake screen.

### Generated Test Suite

- **Task suite file:** `test-suites/task-09-functional-mockup-draft-builder-screen-test-suite.md`
- **Required gates:** unit, static analysis (`npm run lint`), code style, architecture/design guide review.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md`, `04-hooks-y-logica-de-ui.md`, `06-estado-datos-y-api.md` §6 (view models), `02-ux-wireframes-y-maquetas.md` §6 (fake data edge cases).
- **Acceptance environment:** N/A — no backend integration in this task.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Done Criteria

- [ ] `/assessments/[id]/draft` renders all 3 sections navigably with fake data only.
- [ ] Fake data covers long text, many versions, and a single current-only version (one-item versions list, never empty) — not one symmetric happy path.
- [ ] Selecting a historical version makes `DraftEditorSection` read-only and prevents `onSave`; returning to the current version re-enables it (`task-08`'s finding).
- [ ] All 3 Section component tests pass.
- [ ] `npm run lint` passes.
- [ ] Software smoke test check above passes (build/startup/connectivity confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability for this task is N/A (deferred to `task-12`, the first task with real network calls in the Draft Builder screen) — no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply yet.
- [ ] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
