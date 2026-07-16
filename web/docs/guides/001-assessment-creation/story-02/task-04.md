# Functional Mockup Intake Screen

**Source:** task-04 | **Area:** unknown | **Date:** 2026-07-16

## What it does
A navigable functional mockup of the Intake screen at `src/app/(protected)/assessments/new/`, built in TSX with fake/local data only — no `lib/api` calls yet — that validates layout, field-level validation UX, and the submitting state before any backend wiring exists.

---

## How to use it
- Create `src/features/assessment-creation/schemas/briefSchema.ts` with the Zod schema for `learningGoal`, `topic`, `level`, `duration`, `language` (all required strings), matching `CreateAssessmentBriefRequest`.
- Create `src/features/assessment-creation/components/BriefForm.tsx` accepting `{ onSubmit, isSubmitting, serverError, fieldErrors }`, using `DynamicForm` (from `src/components/ds`, built in `task-14`) configured with the 5 fields (`learningGoal` as `textarea`, the other 4 as `input`), `briefSchema` as its Zod resolver, and `fieldErrors` passed straight to `DynamicForm`'s `externalErrors` prop, plus a submit `Button` disabled while `isSubmitting` and the server-error banner rendered from `serverError`.
- Create `src/features/assessment-creation/components/BriefFormSection.tsx` accepting a single `view: IntakeAssessmentPageViewModel` prop and forwarding `view.isSubmitting`, `view.serverError`, `view.fieldErrors`, and `view.handleSubmit` (as `onSubmit`) to `BriefForm`, wrapped in a semantic `<section aria-labelledby="brief-form-title">` per `03-jerarquia-de-componentes.md` §4. Do not have `BriefFormSection` call `useIntakeAssessmentPage()` itself — the Page owns that call.
- Create `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` returning `{ isSubmitting, serverError, fieldErrors, handleSubmit }` (`fieldErrors: Partial<Record<keyof BriefFormValues, string>> | null`), holding a `RemoteData`-shaped submit state (`idle | submitting | success | error`) with a **fake** submit function (`setTimeout(() => ..., 800)`) standing in for the real API call. The fake submit must simulate a server-side field-error response for one recognizable input (e.g. `topic === "trigger-field-error"`) that resolves to a fake `{ topic: "Ya existe una evaluación con este tema" }`-shaped `fieldErrors` result instead of success, so the `fieldErrors` → `DynamicForm.externalErrors` path is exercised now with fake data, not left untested until `task-06`'s real 422 response.
- Create `src/app/(protected)/assessments/new/page.tsx` calling `useShellConfig({ title: "Nueva evaluación", subtitle: "Describe el objetivo de aprendizaje" })`, calling `useIntakeAssessmentPage()`, and passing its result into `BriefFormSection` as the `view` prop.
- Write `BriefForm.test.tsx` covering: required-field validation blocks submission (per story-01's own Done Criteria), the submit button disables while submitting, and — submitting `topic: "trigger-field-error"` renders the fake field error inline via `DynamicForm`'s `externalErrors` path.

## Example
Use `Create` through the public interface introduced by this task.
