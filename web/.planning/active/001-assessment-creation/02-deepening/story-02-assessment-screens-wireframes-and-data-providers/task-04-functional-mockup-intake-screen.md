# ⚛️ TASK 04 — functional-mockup-intake-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-02, task-03
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A navigable functional mockup of the Intake screen at `src/app/(protected)/assessments/new/`, built in TSX with fake/local data only — no `lib/api` calls yet — that validates layout, DS controls, field-level validation UX, API I/O-shaped states, sync/async UX, field data semantics, i18n behavior, and the dashboard "Nueva evaluacion" action that reaches the screen before any backend wiring exists.

---

## Technical Design

- **Approach:** Build the real component tree from `task-03`'s hierarchy now, with `BriefForm`'s `onSubmit` handler simulated (a `setTimeout`-based fake submit in the Page hook) rather than calling `api/`. Building the real components (not a throwaway prototype) means `task-06` only has to swap the fake submit for `submitAssessmentBrief` — it doesn't rebuild the UI. This matches the guide's flow: wireframe → hierarchy → **mockup with fake data** → connect real API.
- **Affected files / components:**
  - `src/app/(protected)/assessments/new/page.tsx`
  - existing dashboard page/action component that renders "Nueva evaluacion"
  - `src/features/assessment-creation/components/BriefFormSection.tsx`
  - `src/features/assessment-creation/components/BriefForm.tsx`
  - `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` (fake submit for now)
  - `src/features/assessment-creation/schemas/briefSchema.ts`
  - `src/features/assessment-creation/components/__tests__/BriefForm.test.tsx`
- **Interfaces / contracts:** `briefSchema` (Zod) is the real, final validation schema — it's the same schema `task-06` will reuse, not a placeholder. Fields must match `CreateAssessmentBriefRequest`, but form values may use richer local semantics before conversion: `learningGoal` long text, `topic` controlled topic/tag/custom value, `level` enum value, `duration` numeric/preset value, `language` enum/catalog value. Fake/local data must match the API I/O matrix from task-02: catalogs/defaults/capabilities, submit result, server error and async operation state if applicable. If the outgoing DTO still requires strings, the conversion must be explicit and tested. Fake/local i18n data must also match task-02: translated UI copy, localized catalog labels, localized safe error messages and explicit `outputLocale`/`contentLocale` state separate from programming `language`.
- **Risk:** Medium — if the fake-data states here don't match what `task-06` needs to swap in (e.g. success/error state shapes), `task-06` has to rework the hook instead of just swapping one function call. Mitigated by using the same `RemoteData<T>`-style state shape (`docs/06-estado-datos-y-api.md` §8) the real version will use.
- **Design notes:** Every form uses React Hook Form + Zod (`zodResolver`) — never native HTML validation, per this project's established convention (already noted in the story's Context). Reuse existing DS components (`Button`, `Input`, `Textarea`, `Select`, `Radio`, `Tag`, `FieldWithHelper` from `src/components/ds` as available) rather than inventing new form primitives. Do not render every field as `Input`; the mockup is the point where incorrect control semantics must be caught. Do not leave user-facing strings as final hardcoded literals; use the selected i18n mechanism or a replaceable test dictionary that task-06 can keep.

---

## Implementation Steps

1. Create `src/features/assessment-creation/schemas/briefSchema.ts` with the Zod schema for `learningGoal`, `topic`, `level`, `duration`, `language`, matching `CreateAssessmentBriefRequest` at the submit boundary while preserving field semantics in form state/validation.
2. Create `src/features/assessment-creation/components/BriefForm.tsx` using `useForm({ resolver: zodResolver(briefSchema) })`, rendering fields with the DS controls selected in task-02/task-03: `Textarea` for `learningGoal`, select/radio for `level`, numeric/preset control for `duration`, selector/catalog for `language`, and topic selector/tag/custom-controlled control according to the recorded source of truth/residual.
3. Create `src/features/assessment-creation/components/BriefFormSection.tsx` wrapping `BriefForm` with a semantic `<section aria-labelledby="brief-form-title">` per `03-jerarquia-de-componentes.md` §4.
4. Create `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` holding a `RemoteData`-shaped submit state (`idle | submitting | success | error`) or operation-shaped async state (`idle | submitting | queued | running | success | error | timeout`) according to task-02, with a clearly isolated **fake** submit/completion adapter standing in for the real API call.
5. Add replaceable i18n resources or test dictionaries for the mockup: labels, helper text, buttons, validation/server messages and catalog labels. Code identifiers stay English; user-facing copy is read through this mechanism.
6. Add explicit locale state in the page/hook (`effectiveLocale` and, if generation content is in scope, `outputLocale`/`contentLocale`) and keep it separate from the programming `language` field.
7. Create `src/app/(protected)/assessments/new/page.tsx` using localized shell title/subtitle values and composing `BriefFormSection`.
8. Wire the existing `/dashboard` "Nueva evaluacion" action as a visible navigation affordance to `/assessments/new` in the mockup stage; do not leave the route reachable only by typing the URL.
9. Write tests covering: required-field validation blocks submission (per story-01's own Done Criteria), controlled fields expose only allowed fake/catalog values, invalid enum/catalog/numeric values are rejected, localized labels/messages/catalog labels render from the i18n mechanism, the submit button disables while submitting, and the dashboard action points/navigates to `/assessments/new`.
10. Manually exercise the fake-data edge cases from `02-ux-wireframes-y-maquetas.md` §6 relevant here: a validation error per field, missing/empty catalog state if applicable, server validation failure, async queued/running/failure if applicable, locale fallback, and the submitting-state visual treatment — not just one symmetric happy path.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Submitting with any required field empty blocks submission and shows an inline error for that field | `npm run test -- BriefForm` |
| 2 | Submit button is disabled while the (fake) submit is in flight | `npm run test -- BriefForm` |
| 3 | All 5 fields render with correct DS controls and accept valid values only | `npm run test -- BriefForm` |
| 4 | Dashboard "Nueva evaluacion" action navigates or links to `/assessments/new` | `npm run test` covering the dashboard action |
| 5 | Invalid enum/catalog/numeric values are rejected before submit and valid values serialize to the DTO boundary explicitly | `npm run test -- BriefForm` |
| 6 | Fake submit/operation states mirror the API I/O + sync/async contract and are isolated for removal in task-06 | `npm run test -- BriefForm` plus manual code review |
| 7 | Labels, helper text, buttons, validation/server errors and catalog labels render through the i18n mechanism or replaceable test dictionary | `npm run test -- BriefForm` |
| 8 | `outputLocale`/`contentLocale` is modeled separately from programming `language` in fake submit state | `npm run test -- BriefForm` plus manual code review |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | Changed surface responds correctly | Start at `/dashboard`, click "Nueva evaluacion", confirm navigation to `/assessments/new`, fill the form, submit, confirm the fake submitting/success states render |
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
- [ ] `/dashboard` "Nueva evaluacion" reaches `/assessments/new`; the mockup is not URL-only.
- [ ] The mockup uses DS controls matching field semantics; it is not an all-`Input` form.
- [ ] Fake/catalog data covers allowed `level`, `duration`, `language` and `topic` values plus invalid/missing-value states.
- [ ] Fake/local data mirrors the API I/O contract and sync/async states from task-02; no fake adapter is treated as production source of truth.
- [ ] User-facing copy, validation messages, server-error messages and catalog labels come from the i18n mechanism or replaceable test dictionary, not final hardcoded component strings.
- [ ] Locale state and `outputLocale`/`contentLocale` are separated from the programming `language` field.
- [ ] If async is part of the chosen contract, queued/running/success/failure/timeout states render before real API wiring.
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
