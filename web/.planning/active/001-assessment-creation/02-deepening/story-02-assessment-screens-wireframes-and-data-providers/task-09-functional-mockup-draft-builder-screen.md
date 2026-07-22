# ⚛️ TASK 09 — functional-mockup-draft-builder-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-07, task-08
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A navigable functional mockup of the Draft Builder screen at `src/app/(protected)/assessments/[id]/draft/`, built in TSX with fake/local data only — no `lib/api` calls yet — that validates the editor, regenerate action, version history UX, API-shaped data, i18n behavior and sync/async states against realistic edge cases before any backend wiring exists.

---

## Technical Design

- **Approach:** Build the real component tree from `task-08`'s hierarchy now, with `useAssessmentDraftBuilderPage` returning fake local data (a hardcoded draft + version list) instead of calling the loader from `task-10`. Fake data must mirror the API I/O matrix from `task-07`, including operation-shaped state if regenerate is async. This lets `task-12` swap in the real Screen Data Facade without touching the Sections' UI.
- **Affected files / components:**
  - `src/app/(protected)/assessments/[id]/draft/page.tsx`
  - `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` (fake data for now)
  - `src/features/assessment-creation/components/{DraftEditorSection,RegenerateSection,VersionHistorySection}.tsx` + their hooks
  - `src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts` (real mapper, fed fake DTOs)
  - `src/features/assessment-creation/components/__tests__/{DraftEditorSection,RegenerateSection,VersionHistorySection}.test.tsx`
- **Interfaces / contracts:** `AssessmentDraftViewModel`/`AssessmentDraftVersionViewModel` are the real, final view model shapes `task-10`'s loader will produce — not placeholders. The mapper (`toAssessmentDraftBuilderPageViewModel`) is written for real now, fed fake DTOs shaped like `task-01`'s confirmed `GenerateAssessmentDraftResponse` and the API I/O matrix from `task-07`. View models must include any locale/content-language metadata confirmed by task-07 (`outputLocale`/`contentLocale`, fallback state, version locale label). If regenerate is async, the mockup exposes operation-shaped states (`queued`, `running`, `succeeded`, `failed`, `timeout`) without treating timers as production behavior.
- **Risk:** Medium — per `02-ux-wireframes-y-maquetas.md` §6, fake data must cover long AI-generated text, many versions, and zero prior versions, not one symmetric happy path; under-covering this here means UX problems surface only after `task-12`'s real wiring, which is more expensive to fix.
- **Design notes:** `RemoteData<T>`-style states per `06-estado-datos-y-api.md` §8, not loose booleans, for the page-level loading/ready/error state. User-facing screen copy, section labels, errors and version labels must come from the i18n mechanism or replaceable test dictionaries; code identifiers and logs remain English.

---

## Implementation Steps

1. Create `toAssessmentDraftBuilderPageViewModel.ts` mapping a `GenerateAssessmentDraftResponse`-shaped draft + `GenerateAssessmentDraftResponse[]`-shaped versions into `{ draft: AssessmentDraftViewModel, versions: AssessmentDraftVersionViewModel[] }`.
2. Create `DraftEditorSection.tsx` + `useDraftEditorSection.ts`: editable fields for title/context/instructions/objectives/deliverables/constraints, using RHF + Zod, an `onSave` callback (fake for now).
3. Create `RegenerateSection.tsx` + `useRegenerateSection.ts`: adjustment-notes textarea + regenerate button, its own submitting/error state, `onRegenerate` callback (fake for now).
4. Create `VersionHistorySection.tsx` + `useVersionHistorySection.ts`: read-only list of past versions, `onViewVersion` callback that swaps which version's fields are displayed in `DraftEditorSection` locally (no API call).
5. Create `useAssessmentDraftBuilderPage.ts` with a fake dataset: at least one draft with long objectives/instructions text (edge case), a version list with 4+ entries (many-versions edge case), and separately test the zero-prior-versions case (empty version list, only the current draft).
6. Include fake locale/content-language cases: draft in `es-CL`, draft in `en`, a fallback-locale label, and a regeneration request whose `outputLocale` is captured at request time.
7. Model fake save/regenerate states according to the sync/async contract from `task-07`: sync submit/success/error or async operation states if applicable. Keep the fake adapter isolated for full removal in `task-12`.
8. Create `src/app/(protected)/assessments/[id]/draft/page.tsx` reading `params.id`, calling `useShellConfig` with localized title/subtitle values, composing the 3 Sections.
9. Write component tests for all 3 Sections covering: editing and calling `onSave`, regenerating and calling `onRegenerate`, switching versions via `onViewVersion`, confirming the editor reflects the selected version, localized labels/errors render, and content-locale metadata/fallback labels are visible when required.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Editing a field and saving calls `onSave` with the updated values | `npm run test -- DraftEditorSection` |
| 2 | Entering adjustment notes and regenerating calls `onRegenerate` with the notes | `npm run test -- RegenerateSection` |
| 3 | Selecting a past version updates the editor's displayed fields to that version's content | `npm run test -- VersionHistorySection` |
| 4 | Long text (500+ characters) in objectives/instructions renders without layout breakage | Manual visual check with the long-text fake dataset |
| 5 | Zero-prior-versions state renders without a broken/empty version list UI | `npm run test` with an empty versions fixture |
| 6 | Fake data and fake operation states mirror the API I/O + sync/async contract and are isolated for removal in task-12 | `npm run test` plus manual code review |
| 7 | Localized section copy/errors/version labels and content-locale/fallback metadata render from the i18n mechanism | `npm run test` plus manual visual check |
| 8 | Regeneration fake state captures `outputLocale`/`contentLocale` at request time and keeps it separate from technical identifiers | `npm run test` plus manual code review |

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
- [ ] Fake data covers long text, many versions, and zero prior versions — not one symmetric happy path.
- [ ] Fake/local data mirrors the API I/O contract and sync/async states from task-07; no fake adapter is treated as a production source of truth.
- [ ] Fake data covers at least two content locales plus fallback display; localized UI/errors/version labels come from the i18n mechanism or replaceable test dictionary.
- [ ] Regeneration state captures `outputLocale`/`contentLocale` separately from technical/code identifiers.
- [ ] All 3 Section component tests pass.
- [ ] `npm run lint` passes.
- [ ] Software smoke test check above passes (build/startup/connectivity confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability for this task is N/A (deferred to `task-12`, the first task with real network calls in the Draft Builder screen) — no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply yet.
- [ ] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
