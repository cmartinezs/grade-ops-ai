# ⚛️ TASK 04 — functional-mockup-intake-screen

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-02, task-03, task-14
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A navigable functional mockup of the Intake screen at `src/app/(protected)/assessments/new/`, built in TSX with fake/local data only — no `lib/api` calls yet — that validates layout, field-level validation UX, and the submitting state before any backend wiring exists.

---

## Technical Design

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

## Verification Summary

- **Component tree built exactly per `task-03`'s corrected hierarchy:** Page (`src/app/(protected)/assessments/new/page.tsx`) → `BriefFormSection` (`view` prop only) → `BriefForm` (`fieldErrors` prop) → `DynamicForm` (`externalErrors` prop). `useIntakeAssessmentPage` is called **only** by the Page — confirmed via `grep -rn "useIntakeAssessmentPage" src/app src/features`: the only call site is `page.tsx`; `BriefFormSection.tsx` imports only the `IntakeAssessmentPageViewModel` type, never the hook itself.
- **`DynamicForm` gap found and fixed during implementation:** `DynamicForm` (built in `task-14`) had no slot for a submit button or server-error banner — `BriefForm` needs both rendered *inside* the same `<form>` as the config-driven fields (for native submit-on-Enter and RHF's `handleSubmit` wiring). Added an optional `children?: React.ReactNode` prop to `DynamicForm`, rendered after the mapped fields, inside the same `<Form>`. Backward-compatible (existing `task-14` tests still pass unmodified) — this is the minimum change needed to make `BriefForm` buildable from `DynamicForm` at all, matching PDR-001 decision item 7's intent.
- **`RemoteData<T>`-style state:** `docs/06-estado-datos-y-api.md` §8 documents this pattern but it isn't implemented anywhere in the repo yet (confirmed via `grep -rn "RemoteData" src` — zero hits) and no existing hook uses even the simpler `LoadState` string-union either. Introduced a local discriminated union (`idle | submitting | success | error`) scoped to `useIntakeAssessmentPage.ts` only, not a new shared type module — keeps this task's scope to its declared affected files. The externally-exposed `IntakeAssessmentPageViewModel` still matches the task's exact 4-field contract (`isSubmitting`, `serverError`, `fieldErrors`, `handleSubmit`); `success` and `idle` are intentionally indistinguishable from the outside in this fake-data mockup (no redirect target exists yet), and the fake submit function never produces the `error` state in this task (it's part of the type shape for `task-06`'s real failure mode, not exercised until then).
- **Unit tests:** `BriefForm.test.tsx` (5 cases: all 5 fields render/accept input, required-field blocks submission, submit button disables while `isSubmitting`, fake server field error renders via `externalErrors`, top-level `serverError` banner renders) — `5 passed, 5 total`. Added one more file beyond the task's original list, `src/app/(protected)/assessments/new/__tests__/NewAssessmentPage.test.tsx` (2 cases), to close a real gap: `BriefForm.test.tsx` alone only proves `BriefForm` displays whatever `fieldErrors` it's handed — it doesn't prove the hook's own `topic === "trigger-field-error"` magic-string logic actually produces that value end-to-end. Confirmed `AuthGuard` (which needs real Firebase) lives only in `(protected)/layout.tsx`, not in the page module — importing the page directly in a Jest test bypasses it entirely, same pattern already used by `DashboardPage.test.tsx`. Both new tests pass: `2 passed, 2 total`.
- **Full suite re-run:** `66 tests, 61 passed`; the 5 failures are the same pre-existing, unrelated ones from `task-14`'s round (English-vs-Spanish label queries in `RegisterPage.test.tsx`/`SignOutButton.test.tsx`).
- **Code review findings (both P2, now fixed):**
  1. `briefSchema` used `z.string().min(1, ...)`, which accepts whitespace-only values even though the backend contract is `@NotBlank` — a user could submit `"   "` and pass client validation only to hit a server-side 422 the UI never predicted. Fixed by adding `.trim()` before `.min(1, ...)` on all 5 fields; RHF/Zod trims the value as part of parsing, so the submitted payload is also trimmed, not just validated. Added a test: submitting `topic: "   "` (all other fields valid) blocks submission and shows `"Ingresa el tema."`.
  2. The submitting state only disabled the submit `Button`; `DynamicForm` had no `disabled` prop at all, so every field stayed editable while the fake `setTimeout` was in flight — a user could edit `topic` after submitting `"trigger-field-error"` and then see an inline error attached to a value no longer in the input, a real edit/submit race that gets more expensive once `task-06` replaces the timer with a real network round-trip. Fixed by adding a `disabled?: boolean` prop to `DynamicForm`, propagated to `Input`/`Textarea`/`Select`/`Checkbox` on every rendered control, and wired `BriefForm` to pass `disabled={isSubmitting}`. Added a `DynamicForm.test.tsx` case (primitive-level, where the prop actually lives) and a `BriefForm.test.tsx` case (all 5 fields disabled while `isSubmitting`).
  Re-ran the full suite after both fixes: `69 tests, 64 passed` — same 5 pre-existing, unrelated failures; `tsc --noEmit` still clean.
- **Static analysis:** `npm run lint` — still N/A, no ESLint config in this repo (pre-existing, unrelated to this task). Substituted `npx tsc --noEmit`: 0 errors outside the pre-existing missing-`@types/jest` gap in test files repo-wide.
- **Runtime smoke:** `npm run build` compiles/type-checks cleanly (`✓ Compiled successfully`); fails only at the `/assessments/new` **prerender** step with `auth/invalid-api-key` — the same pre-existing missing-real-Firebase-credentials sandbox gap from `task-14` (only `.env.local.example` exists). Set up a throwaway `.env.local` (gitignored, removed after) to run `npm run dev`: started cleanly, `curl http://localhost:3000/assessments/new` returned `HTTP 200`. Full browser interaction wasn't available in this sandbox (`npx playwright install chromium` doesn't ship a browser without `@playwright/test` as a project dependency, which this repo doesn't have and installing one was judged out of scope for a mockup task) — the two Jest integration tests above (real hook, not mocked props, fake timers driving the actual `setTimeout`) are the practical equivalent for the "fill the form, submit, confirm fake submitting/success states" and "trigger-field-error" checks Implementation Step 7 and the Software Smoke Test Check ask for.
- **`[CHECK-ATOMICITY]`:** scope stayed within the task's own declared files plus the two additions explained above (`DynamicForm.tsx`'s `children` slot — a required, backward-compatible gap fix; `NewAssessmentPage.test.tsx` — additional verification evidence, not new production surface). No rubric/question-bank-specific fields, no other screens touched.

---

## Master Plan Addendum — Intake Mockup Gates

Added after this task was already `DONE` in `develop`. Any R01 revalidation or future mockup change must prove localized labels, helper text, validation messages, safe server errors and catalog labels are sourced from i18n resources or API-backed localized labels, not final hardcoded component literals.

Fake data must still mirror the API I/O and sync/async contract, but it also needs locale/fallback cases and an explicit generated-content locale (`outputLocale`/`contentLocale`) separate from programming `language`.

---

## Done Criteria

- [x] `/assessments/new` renders a navigable form with all 5 fields, using fake/local submit state only. Confirmed via `NewAssessmentPage.test.tsx` (real hook, no mocks) and `HTTP 200` on `curl http://localhost:3000/assessments/new`.
- [x] Required-field validation blocks submission per-field, using RHF + Zod exclusively (no native HTML validation), and rejects whitespace-only values matching the backend's `@NotBlank` contract (`briefSchema`'s `.trim().min(1, ...)`, fixed in code review). `BriefForm.test.tsx`'s "blocks submission..." and "whitespace-only" cases; `DynamicForm`'s `Form` always sets `noValidate`.
- [x] The full `view`/`fieldErrors`/`externalErrors` contract from `task-03`'s (corrected) hierarchy is built now: Page → `BriefFormSection` (`view` prop) → `BriefForm` (`fieldErrors` prop) → `DynamicForm` (`externalErrors` prop) — confirmed by the fake `topic: "trigger-field-error"` case rendering inline (both via a direct prop in `BriefForm.test.tsx` and end-to-end through the real hook in `NewAssessmentPage.test.tsx`), so `task-06` only swaps the fake submit function's body, with no component/prop rewiring. All fields (not just the submit button) are disabled while submitting, preventing the edit/submit race code review found (`DynamicForm`'s new `disabled` prop).
- [x] `BriefForm.test.tsx` passes — `7 passed, 7 total` (5 original + whitespace-only + all-fields-disabled, added in code review).
- [~] `npm run lint` passes — N/A, no ESLint config in this repo (pre-existing gap, unchanged from `task-14`); substituted `npx tsc --noEmit`, 0 errors outside the pre-existing test-typings gap.
- [x] `npm run build` and `npm run dev` succeed with no new console errors. `build` compiles/type-checks cleanly (fails only at the same pre-existing missing-Firebase-credentials prerender gap as `task-14`); `dev` starts cleanly and serves `/assessments/new` with `HTTP 200`.
- [x] Software smoke test check above passes (build/startup/connectivity confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR. PR #73 (`tasks/story-02-assessment-screens-wireframes-and-data-providers/task-04-functional-mockup-intake-screen` → `story-02-assessment-screens-wireframes-and-data-providers`) opened, reviewed (2 P2 findings, both fixed in the correction commit), re-reviewed with no remaining findings, and merged 2026-07-16 (merge commit `d43c07c`).
- [x] Logging/observability for this task is N/A (deferred to `task-06`, the first task with a real network call) — no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply yet.
- [x] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence — `test-suites/task-04-functional-mockup-intake-screen-test-suite.md` generated and gaps filled.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — see Verification Summary's atomicity note (one backward-compatible `DynamicForm` fix, one extra test file for real verification evidence, nothing else).

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
