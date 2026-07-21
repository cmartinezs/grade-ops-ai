# ADR: A navigable functional mockup of the Intake screen at `src/app/(protected)/assessments/new/`, built in TSX with fake/local data only — no `lib/api` calls yet — that validates layout, field-level validation UX, and the submitting state before any backend wiring exists.

**Date:** 2026-07-16
**Status:** Accepted
**Planning:** 001-assessment-creation / story-02 / task-04

## Context
- **Approach:** Build the real component tree from `task-03`'s hierarchy now, with `BriefForm`'s `onSubmit` handler simulated (a `setTimeout`-based fake submit in the Page hook) rather than calling `api/`. Building the real components (not a throwaway prototype) means `task-06` only has to swap the fake submit for `submitAssessmentBrief` — it doesn't rebuild the UI *or the component contract*. This means the full prop/data flow from `task-03`'s corrected hierarchy must exist now, not be added in `task-06`: Page passes a `view: IntakeAssessmentPageViewModel` prop to `BriefFormSection`, which forwards `isSubmitting`/`serverError`/`fieldErrors`/`handleSubmit` to `BriefForm`, which passes `fieldErrors` to `DynamicForm`'s `externalErrors`. The fake submit simulates a server-side field-error response for one input value (see Implementation Step 4) so this path is exercised by a real test now, not left implicit until `task-06`. This matches the guide's flow: wireframe → hierarchy → **mockup with fake data** → connect real API.
- **Affected files / components:** New: `src/app/(protected)/assessments/new/page.tsx`, `src/features/assessment-creation/components/BriefFormSection.tsx`, `src/features/assessment-creation/components/BriefForm.tsx`, `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts`, `src/features/assessment-creation/schemas/briefSchema.ts`, `src/features/assessment-creation/components/__tests__/BriefForm.test.tsx`.

  Per-file detail:
  - `src/app/(protected)/assessments/new/page.tsx` — Page, calls the hook and passes `view` to the Section.
  - `src/features/assessment-creation/components/BriefFormSection.tsx` — Section, forwards `view` fields to `BriefForm`.
  - `src/features/assessment-creation/components/BriefForm.tsx` — Component, thin `DynamicForm` configuration.
  - `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` — Page hook, fake submit for now.
  - `src/features/assessment-creation/schemas/briefSchema.ts` — Zod schema, the real final shape.
  - `src/features/assessment-creation/components/__tests__/BriefForm.test.tsx` — tests.
- **Interfaces / contracts:** `briefSchema` (Zod) is the real, final validation schema — it's the same schema `task-06` will reuse, not a placeholder. Fields: `learningGoal`, `topic`, `level`, `duration`, `language`, all `z.string().min(1, ...)` per `CreateAssessmentBriefRequest`'s `@NotBlank` constraints (confirmed in `task-01`). `IntakeAssessmentPageViewModel { isSubmitting, serverError, fieldErrors, handleSubmit }` and `BriefFormProps { onSubmit, isSubmitting, serverError, fieldErrors }` are the real, final shapes `task-06` will reuse as-is — `task-06` only replaces the fake submit function's body with `submitAssessmentBrief`, it does not add `fieldErrors`/`externalErrors` wiring that doesn't already exist here.
- **Risk:** Medium — if the fake-data states here don't match what `task-06` needs to swap in (e.g. success/error state shapes), `task-06` has to rework the hook instead of just swapping one function call. Mitigated by using the same `RemoteData<T>`-style state shape (`docs/06-estado-datos-y-api.md` §8) the real version will use.
- **Design notes:** Every form uses React Hook Form + Zod (`zodResolver`) — never native HTML validation, per this project's established convention (already noted in the story's Context). Build `BriefForm` from the DS form primitives `task-14` produces (`Field`/`Input`/`Textarea`/`Button`), per `pdr-001-design-system-form-primitives.md` — since this form's 5 fields are linear with no conditional logic, use `DynamicForm` rather than hand-composing each field individually (PDR-001 decision item 7). Do not reintroduce `FieldWithHelper` or inline label/error markup here; that predates the PDR's centralization of label/required/error/hint in `Field`.

---

## Decision
success/error state shapes), `task-06` has to rework the hook instead of just swapping one function call.

## Consequences
** Every form uses React Hook Form + Zod (`zodResolver`) — never native HTML validation, per this project's established convention (already noted in the story's Context). Build `BriefForm` from the DS form primitives `task-14` produces (`Field`/`Input`/`Textarea`/`Button`), per `pdr-001-design-system-form-primitives.md` — since this form's 5 fields are linear with no conditional logic, use `DynamicForm` rather than hand-composing each field individually (PDR-001 decision item 7). Do not reintroduce `FieldWithHelper` or inline label/error markup here; that predates the PDR's centralization of label/required/error/hint in `Field`.

## Alternatives Considered
success/error state shapes), `task-06` has to rework the hook instead of just swapping one function call.
