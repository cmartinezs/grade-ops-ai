# ⚛️ TASK 04 — functional-mockup-intake-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-02, task-03
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A navigable functional mockup of the Intake screen at `src/app/(protected)/assessments/new/`, built in TSX with fake/local data only — no `lib/api` calls yet — that validates layout, field-level validation UX, and the submitting state before any backend wiring exists.

---

## Technical Design

- **Approach:** Build the real component tree from `task-03`'s hierarchy now, with `BriefForm`'s `onSubmit` handler simulated (a `setTimeout`-based fake submit in the Page hook) rather than calling `api/`. Building the real components (not a throwaway prototype) means `task-06` only has to swap the fake submit for `submitAssessmentBrief` — it doesn't rebuild the UI. This matches the guide's flow: wireframe → hierarchy → **mockup with fake data** → connect real API.
- **Affected files / components:**
  - `src/app/(protected)/assessments/new/page.tsx`
  - `src/features/assessment-creation/components/BriefFormSection.tsx`
  - `src/features/assessment-creation/components/BriefForm.tsx`
  - `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` (fake submit for now)
  - `src/features/assessment-creation/schemas/briefSchema.ts`
  - `src/features/assessment-creation/components/__tests__/BriefForm.test.tsx`
- **Interfaces / contracts:** `briefSchema` (Zod) is the real, final validation schema — it's the same schema `task-06` will reuse, not a placeholder. Fields: `learningGoal`, `topic`, `level`, `duration`, `language`, all `z.string().min(1, ...)` per `CreateAssessmentBriefRequest`'s `@NotBlank` constraints (confirmed in `task-01`).
- **Risk:** Medium — if the fake-data states here don't match what `task-06` needs to swap in (e.g. success/error state shapes), `task-06` has to rework the hook instead of just swapping one function call. Mitigated by using the same `RemoteData<T>`-style state shape (`docs/06-estado-datos-y-api.md` §8) the real version will use.
- **Design notes:** Every form uses React Hook Form + Zod (`zodResolver`) — never native HTML validation, per this project's established convention (already noted in the story's Context). Reuse existing DS components (`Button`, `Input`, `FieldWithHelper` from `src/components/ds`) rather than inventing new form primitives.

---

## Implementation Steps

1. Create `src/features/assessment-creation/schemas/briefSchema.ts` with the Zod schema for `learningGoal`, `topic`, `level`, `duration`, `language` (all required strings), matching `CreateAssessmentBriefRequest`.
2. Create `src/features/assessment-creation/components/BriefForm.tsx` using `useForm({ resolver: zodResolver(briefSchema) })`, rendering fields with `FieldWithHelper`/`Input` from `src/components/ds`, and a submit `Button` disabled while `isSubmitting`.
3. Create `src/features/assessment-creation/components/BriefFormSection.tsx` wrapping `BriefForm` with a semantic `<section aria-labelledby="brief-form-title">` per `03-jerarquia-de-componentes.md` §4.
4. Create `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` holding a `RemoteData`-shaped submit state (`idle | submitting | success | error`) with a **fake** submit function (`setTimeout(() => ..., 800)`) standing in for the real API call.
5. Create `src/app/(protected)/assessments/new/page.tsx` calling `useShellConfig({ title: "Nueva evaluación", subtitle: "Describe el objetivo de aprendizaje" })` and composing `BriefFormSection`.
6. Write `BriefForm.test.tsx` covering: required-field validation blocks submission (per story-01's own Done Criteria), and the submit button disables while submitting.
7. Manually exercise the fake-data edge cases from `02-ux-wireframes-y-maquetas.md` §6 relevant here: a validation error per field, and the submitting-state visual treatment — not just one symmetric happy path.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Submitting with any required field empty blocks submission and shows an inline error for that field | `npm run test -- BriefForm` |
| 2 | Submit button is disabled while the (fake) submit is in flight | `npm run test -- BriefForm` |
| 3 | All 5 fields render and accept input | `npm run test -- BriefForm` |

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
