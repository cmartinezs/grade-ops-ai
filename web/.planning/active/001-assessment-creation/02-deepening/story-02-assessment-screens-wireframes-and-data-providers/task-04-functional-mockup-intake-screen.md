# ⚛️ TASK 04 — functional-mockup-intake-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-02, task-03, task-14
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A navigable functional mockup of the Intake screen at `src/app/(protected)/assessments/new/`, built in TSX with fake/local data only — no `lib/api` calls yet — that validates layout, field-level validation UX, and the submitting state before any backend wiring exists.

---

## Technical Design

- **Approach:** Build the real component tree from `task-03`'s hierarchy now, with `BriefForm`'s `onSubmit` handler simulated (a `setTimeout`-based fake submit in the Page hook) rather than calling `api/`. Building the real components (not a throwaway prototype) means `task-06` only has to swap the fake submit for `submitAssessmentBrief` — it doesn't rebuild the UI *or the component contract*. This means the full prop/data flow from `task-03`'s corrected hierarchy must exist now, not be added in `task-06`: Page passes a `view: IntakeAssessmentPageViewModel` prop to `BriefFormSection`, which forwards `isSubmitting`/`serverError`/`fieldErrors`/`handleSubmit` to `BriefForm`, which passes `fieldErrors` to `DynamicForm`'s `externalErrors`. The fake submit simulates a server-side field-error response for one input value (see Implementation Step 4) so this path is exercised by a real test now, not left implicit until `task-06`. This matches the guide's flow: wireframe → hierarchy → **mockup with fake data** → connect real API.
- **Affected files / components:**
  - `src/app/(protected)/assessments/new/page.tsx`
  - `src/features/assessment-creation/components/BriefFormSection.tsx`
  - `src/features/assessment-creation/components/BriefForm.tsx`
  - `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` (fake submit for now)
  - `src/features/assessment-creation/schemas/briefSchema.ts`
  - `src/features/assessment-creation/components/__tests__/BriefForm.test.tsx`
- **Interfaces / contracts:** `briefSchema` (Zod) is the real, final validation schema — it's the same schema `task-06` will reuse, not a placeholder. Fields: `learningGoal`, `topic`, `level`, `duration`, `language`, all `z.string().min(1, ...)` per `CreateAssessmentBriefRequest`'s `@NotBlank` constraints (confirmed in `task-01`). `IntakeAssessmentPageViewModel { isSubmitting, serverError, fieldErrors, handleSubmit }` and `BriefFormProps { onSubmit, isSubmitting, serverError, fieldErrors }` are the real, final shapes `task-06` will reuse as-is — `task-06` only replaces the fake submit function's body with `submitAssessmentBrief`, it does not add `fieldErrors`/`externalErrors` wiring that doesn't already exist here.
- **Risk:** Medium — if the fake-data states here don't match what `task-06` needs to swap in (e.g. success/error state shapes), `task-06` has to rework the hook instead of just swapping one function call. Mitigated by using the same `RemoteData<T>`-style state shape (`docs/06-estado-datos-y-api.md` §8) the real version will use.
- **Design notes:** Every form uses React Hook Form + Zod (`zodResolver`) — never native HTML validation, per this project's established convention (already noted in the story's Context). Build `BriefForm` from the DS form primitives `task-14` produces (`Field`/`Input`/`Textarea`/`Button`), per `pdr-001-design-system-form-primitives.md` — since this form's 5 fields are linear with no conditional logic, use `DynamicForm` rather than hand-composing each field individually (PDR-001 decision item 7). Do not reintroduce `FieldWithHelper` or inline label/error markup here; that predates the PDR's centralization of label/required/error/hint in `Field`.

---

## Implementation Steps

1. Create `src/features/assessment-creation/schemas/briefSchema.ts` with the Zod schema for `learningGoal`, `topic`, `level`, `duration`, `language` (all required strings), matching `CreateAssessmentBriefRequest`.
2. Create `src/features/assessment-creation/components/BriefForm.tsx` accepting `{ onSubmit, isSubmitting, serverError, fieldErrors }`, using `DynamicForm` (from `src/components/ds`, built in `task-14`) configured with the 5 fields (`learningGoal` as `textarea`, the other 4 as `input`), `briefSchema` as its Zod resolver, and `fieldErrors` passed straight to `DynamicForm`'s `externalErrors` prop, plus a submit `Button` disabled while `isSubmitting` and the server-error banner rendered from `serverError`.
3. Create `src/features/assessment-creation/components/BriefFormSection.tsx` accepting a single `view: IntakeAssessmentPageViewModel` prop and forwarding `view.isSubmitting`, `view.serverError`, `view.fieldErrors`, and `view.handleSubmit` (as `onSubmit`) to `BriefForm`, wrapped in a semantic `<section aria-labelledby="brief-form-title">` per `03-jerarquia-de-componentes.md` §4. Do not have `BriefFormSection` call `useIntakeAssessmentPage()` itself — the Page owns that call.
4. Create `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` returning `{ isSubmitting, serverError, fieldErrors, handleSubmit }` (`fieldErrors: Partial<Record<keyof BriefFormValues, string>> | null`), holding a `RemoteData`-shaped submit state (`idle | submitting | success | error`) with a **fake** submit function (`setTimeout(() => ..., 800)`) standing in for the real API call. The fake submit must simulate a server-side field-error response for one recognizable input (e.g. `topic === "trigger-field-error"`) that resolves to a fake `{ topic: "Ya existe una evaluación con este tema" }`-shaped `fieldErrors` result instead of success, so the `fieldErrors` → `DynamicForm.externalErrors` path is exercised now with fake data, not left untested until `task-06`'s real 422 response.
5. Create `src/app/(protected)/assessments/new/page.tsx` calling `useShellConfig({ title: "Nueva evaluación", subtitle: "Describe el objetivo de aprendizaje" })`, calling `useIntakeAssessmentPage()`, and passing its result into `BriefFormSection` as the `view` prop.
6. Write `BriefForm.test.tsx` covering: required-field validation blocks submission (per story-01's own Done Criteria), the submit button disables while submitting, and — submitting `topic: "trigger-field-error"` renders the fake field error inline via `DynamicForm`'s `externalErrors` path.
7. Manually exercise the fake-data edge cases from `02-ux-wireframes-y-maquetas.md` §6 relevant here: a validation error per field, the submitting-state visual treatment, and the simulated server-side field-error case from Step 4 — not just one symmetric happy path.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Submitting with any required field empty blocks submission and shows an inline error for that field | `npm run test -- BriefForm` |
| 2 | Submit button is disabled while the (fake) submit is in flight | `npm run test -- BriefForm` |
| 3 | All 5 fields render and accept input | `npm run test -- BriefForm` |
| 4 | Submitting `topic: "trigger-field-error"` renders the fake server-side field error inline via `DynamicForm`'s `externalErrors` prop — confirms the `fieldErrors` path works with fake data, not just in theory | `npm run test -- BriefForm` |
| 5 | `BriefFormSection` receives `view` as its only prop and forwards `isSubmitting`/`serverError`/`fieldErrors`/`handleSubmit` to `BriefForm` unchanged — no component in this tree calls `useIntakeAssessmentPage()` except the Page | Manual code review of `page.tsx`/`BriefFormSection.tsx` |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | Changed surface responds correctly | Navigate to `/assessments/new`, fill the form, submit, confirm the fake submitting/success states render |
| 3 | No startup regressions are visible | Inspect `npm run dev` console/terminal output for new errors or warnings |

### Database / ORM Consistency Check

N/A — no database or ORM involved.

### Logging / Observability

- **Logging mechanism:** Not yet confirmed for `web/` (`.planning/LOGGING.md` § Current Mechanism status: "not confirmed"). Suggested for this Node.js/TypeScript stack: Pino with structured JSON logs, since this is a mockup with no real API calls yet, defer the actual logging-mechanism decision to `task-06` (the task that introduces the real network call) rather than deciding it here.
- **Correlation / trace context:** N/A at this stage — no network call exists yet in this task.
- **Levels by event criticality:** N/A at this stage.
- **Execution trace points:** N/A at this stage — revisit in `task-06`.
- **Sensitive data guardrails:** The brief's `learningGoal`/`topic` fields are not sensitive/personal data; no special guardrail needed for this task.
- **Verification evidence:** N/A — no logging surface exists in a fake-data mockup.

### Generated Test Suite

- **Task suite file:** `test-suites/task-04-functional-mockup-intake-screen-test-suite.md`
- **Required gates:** unit, static analysis (`npm run lint`), code style, architecture/design guide review.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md`, `04-hooks-y-logica-de-ui.md`, `07-formularios-validacion-y-feedback.md`, `09-nomenclatura-typescript-react.md` (TSX mandatory).
- **Acceptance environment:** N/A — no backend integration in this task.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Done Criteria

- [ ] `/assessments/new` renders a navigable form with all 5 fields, using fake/local submit state only.
- [ ] Required-field validation blocks submission per-field, using RHF + Zod exclusively (no native HTML validation).
- [ ] The full `view`/`fieldErrors`/`externalErrors` contract from `task-03`'s (corrected) hierarchy is built now: Page → `BriefFormSection` (`view` prop) → `BriefForm` (`fieldErrors` prop) → `DynamicForm` (`externalErrors` prop) — confirmed by the fake `topic: "trigger-field-error"` case rendering inline, so `task-06` only swaps the fake submit function's body, with no component/prop rewiring.
- [ ] `BriefForm.test.tsx` passes.
- [ ] `npm run lint` passes.
- [ ] `npm run build` and `npm run dev` succeed with no new console errors.
- [ ] Software smoke test check above passes (build/startup/connectivity confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability for this task is N/A (deferred to `task-06`, the first task with a real network call) — no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply yet.
- [ ] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
