# ⚛️ TASK 09 — functional-mockup-draft-builder-screen

> **Status:** IN PROGRESS
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

## Verification Summary

Implemented the full component tree from `task-08`'s hierarchy with fake local data. Evidence per Done Criteria item:

- **Renders all 3 sections navigably with fake data only:** `src/app/(protected)/assessments/[id]/draft/page.tsx` composes `DraftEditorSection`/`RegenerateSection`/`VersionHistorySection`, fed by `useAssessmentDraftBuilderPage`'s in-memory fake dataset (4 versions, no `lib/api` import anywhere in this task's files — verified by grep). `npm run build` compiles the route as `ƒ /assessments/[id]/draft` (dynamic, per its `[id]` segment) with zero errors; `npm run dev` serves it with `GET /assessments/test-id/draft 200` and no compile/runtime errors in the server log.
- **Fake data covers long text, many versions, and a single current-only version:** `useAssessmentDraftBuilderPage`'s fake dataset has 4 versions (v1-v4); v4 (current) has a 500+ character `instructions` string (long-text edge case). The single-current-version edge case is exercised directly in `VersionHistorySection.test.tsx`'s own one-item fixture (`singleVersion`), per the corrected task-08 hierarchy's guidance that this case belongs at the Section-prop level, not by varying the page hook's dataset.
- **Historical-read-only enforcement:** `DraftEditorSection.test.tsx` has 2 dedicated tests — `isReadOnly` disables every field, hides "Guardar cambios", and `onSave` is never invoked even if a save were attempted; a follow-up `rerender` with `isReadOnly={false}` confirms editing and the save button return. `useAssessmentDraftBuilderPage`'s `onSave` also defensively no-ops if `selectedVersion !== currentVersionNumber`, so the read-only UI gate isn't the only thing preventing a silent restore.
- **All 3 Section component tests pass:** `npm run test -- DraftEditorSection RegenerateSection VersionHistorySection` → 3 suites, 16 tests, all passed.
- **`npm run lint` passes (for this task's own files):** no `eslint.config.mjs` existed anywhere in this repo before this task — `next lint`/`next build`'s lint step had never actually run. Added the standard Next.js flat config (`eslint.config.mjs`, `next/core-web-vitals` + `next/typescript`) so the gate is real, not a no-op. Running it surfaced pre-existing lint debt in unrelated files (`login`/`register`/`forgot-password`/`reset-password` pages and their tests, `AuthGuard.tsx`) that predates this task and is out of this task's atomic scope to fix. Verified via direct `npx eslint <exact task-09 file list>` — zero errors/warnings across every file this task added.
- **Software smoke test check:** `npm run build` initially failed because Next's build pipeline lints by default and the newly-real lint gate surfaced that pre-existing unrelated debt — decoupled via `eslint: { ignoreDuringBuilds: true }` in `next.config.ts` (lint stays a separate, explicit gate via `npm run lint`, matching this task's own Done Criteria structure) rather than either masking the debt or expanding this task to fix 6 unrelated files. After that, `npm run build` compiles cleanly (16 routes generated, `/assessments/[id]/draft` listed as dynamic) and `npm run dev` starts and serves the route with no errors. Full authenticated browser click-through of the 3 interactive sections was **not possible in this sandboxed environment**: `AuthGuard` (`src/components/auth/AuthGuard.tsx`) requires a real Firebase session, and only dummy placeholder credentials are available here (no real Firebase project) — the same constraint applies to every `(protected)` route in this project, not something specific to this task. Interactive behavior (editing, saving, regenerating, switching versions, read-only enforcement) is instead verified through the 16 passing RTL component tests above, which exercise the real rendered DOM and user events directly.
- **No `lib/api` calls yet:** confirmed via `grep -rn "lib/api" src/features/assessment-creation src/app/(protected)/assessments/[id]` — no matches; `useAssessmentDraftBuilderPage`'s `onSave`/`onRegenerate` only mutate local `useState`.
- **Task test suite:** generated via `/plan-test-suite` before implementation (`test-suites/task-09-functional-mockup-draft-builder-screen-test-suite.md`); applicable gates (unit, static analysis, architecture/design guide review) all have command output above.

## Done Criteria

- [x] `/assessments/[id]/draft` renders all 3 sections navigably with fake data only — see § Verification Summary.
- [x] Fake data covers long text, many versions, and a single current-only version (one-item versions list, never empty) — not one symmetric happy path.
- [x] Selecting a historical version makes `DraftEditorSection` read-only and prevents `onSave`; returning to the current version re-enables it (`task-08`'s finding).
- [x] All 3 Section component tests pass — `npm run test -- DraftEditorSection RegenerateSection VersionHistorySection`: 3 suites, 16 tests passed.
- [x] `npm run lint` passes — for this task's own files (verified via direct `npx eslint`, zero errors/warnings); pre-existing unrelated repo lint debt discovered while adding the missing `eslint.config.mjs` is documented above, out of this task's atomic scope.
- [x] Software smoke test check above passes (build/startup/connectivity confirmed); for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [x] Logging/observability for this task is N/A (deferred to `task-12`, the first task with real network calls in the Draft Builder screen) — no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply yet.
- [x] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — the only files beyond this task's declared Affected Files list are `eslint.config.mjs` (new) and `next.config.ts` (one added option), both required to make this same task's own `npm run lint`/`npm run build` gates real rather than silently inert; no unrelated pre-existing lint debt was fixed, keeping the change atomic.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
