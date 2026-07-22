# Code Review: task-04-functional-mockup-intake-screen

Date: 2026-07-16
Scope: `task-04-functional-mockup-intake-screen.md` and the implementation it introduces for `/assessments/new` (`page.tsx`, `BriefFormSection`, `BriefForm`, `useIntakeAssessmentPage`, `briefSchema`, `DynamicForm` child-slot change, and tests).

## Re-review - 2026-07-16

### Findings

No findings. Both P2 findings from the original review are fixed.

### Validation Notes

- The `@NotBlank` mismatch is fixed: all five `briefSchema` fields now use `z.string().trim().min(1, ...)`, and `BriefForm.test.tsx` covers a whitespace-only `topic` value blocking submit.
- The submitting edit race is fixed: `BriefForm` passes `disabled={isSubmitting}` into `DynamicForm`; `DynamicForm` propagates `disabled` to `Input`, `Textarea`, `Select`, and `Checkbox`; both `BriefForm.test.tsx` and `DynamicForm.test.tsx` assert disabled controls.
- The original `fieldErrors` -> `DynamicForm.externalErrors` flow remains intact after the fix.

### Verification

- `npm run test -- BriefForm` — passed, 7 tests.
- `npm run test -- NewAssessmentPage` — passed, 2 tests.
- `npm run test -- DynamicForm` — passed, 6 tests.
- `npm run test -- --runInBand` — still fails only on the known unrelated suite issues: `RegisterPage.test.tsx` queries English `/full name/i`, and `SignOutButton.test.tsx` queries English `/sign out/i` while the UI is Spanish (`64 passed`, `5 failed`).
- `npm run build` — compiles successfully, then still fails during static prerender with pre-existing Firebase `auth/invalid-api-key`.
- `npx tsc --noEmit` — failed after the interrupted build because `tsconfig.json` includes `.next/types/**/*.ts` paths that are missing after the failed prerender cleanup; this is a build-artifact state issue, not a new task-04 type error.

## Findings

### P2 - `briefSchema` accepts whitespace-only values even though the backend contract is `@NotBlank`

- Files:
  - `src/features/assessment-creation/schemas/briefSchema.ts:4`
  - `src/features/assessment-creation/schemas/briefSchema.ts:5`
  - `src/features/assessment-creation/schemas/briefSchema.ts:6`
  - `src/features/assessment-creation/schemas/briefSchema.ts:7`
  - `src/features/assessment-creation/schemas/briefSchema.ts:8`

The task says `briefSchema` is the "real, final validation schema" that `task-06` will reuse, and the fields are meant to match `CreateAssessmentBriefRequest`'s `@NotBlank` contract. `z.string().min(1, ...)` only rejects the empty string; `"   "` passes client validation. That means the mockup currently accepts values the real API will reject, so `task-06` inherits a schema that does not actually mirror the backend and users can get server-side 422s for input the UI presented as valid.

Recommendation: make each required string trim-aware, for example `z.string().trim().min(1, ...)`, or use a shared helper such as `requiredNotBlank(message)`. Add a test that fills one field with spaces and verifies submission is blocked.

### P2 - The submitting state leaves form fields editable

- Files:
  - `src/features/assessment-creation/components/BriefForm.tsx:24`
  - `src/features/assessment-creation/components/BriefForm.tsx:35`
  - `src/components/ds/DynamicForm.tsx:21`
  - `src/components/ds/DynamicForm.tsx:92`
  - `src/components/ds/DynamicForm.tsx:100`
  - `src/components/ds/DynamicForm.tsx:108`
  - `src/components/ds/DynamicForm.tsx:117`

The wireframe's submitting state requires the button and fields to be disabled to prevent double-submit/edit races. The current implementation only passes `isSubmitting` to the submit `Button`; `DynamicForm` has no disabled/read-only prop and renders all controls editable while the fake submit timer is in flight. A teacher can submit `topic = "trigger-field-error"`, edit the topic before the timer resolves, and then receive an inline topic error for a value that is no longer in the input. The same race becomes more expensive in `task-06` when the timer is replaced with the real two-step network mutation.

Recommendation: add a `disabled?: boolean` (or equivalent form state prop) to `DynamicForm`, pass `isSubmitting` from `BriefForm`, and propagate it to `Input`, `Textarea`, `Select`, and `Checkbox`. Extend the page/form test to assert fields are disabled while submitting.

## Validation Notes

- The corrected task-03 contract is implemented: Page calls `useIntakeAssessmentPage()`, passes `view` to `BriefFormSection`, and the `fieldErrors` path reaches `DynamicForm.externalErrors`.
- `DynamicForm`'s child slot is narrow and backward-compatible for rendering the submit button/server-error banner inside the same `<form>`.
- `BriefForm.test.tsx`, `NewAssessmentPage.test.tsx`, and `DynamicForm.test.tsx` pass individually.

## Verification

- `git diff --check bdfcb8577a5fc64cca9dce5a7365644dc0d2425f...HEAD` — passed.
- `npm run test -- BriefForm` — passed, 5 tests.
- `npm run test -- NewAssessmentPage` — passed, 2 tests.
- `npm run test -- DynamicForm` — passed, 5 tests.
- `npm run test -- --runInBand` — failed with the known unrelated suite issues: `RegisterPage.test.tsx` still queries English `/full name/i`, and `SignOutButton.test.tsx` still queries English `/sign out/i` while the UI is Spanish.
- `npx tsc --noEmit` — failed on the pre-existing missing Jest globals/types across test files (`describe`, `it`, `jest`, `expect`).
- `npm run build` — compiled successfully, then failed during static prerender with pre-existing Firebase `auth/invalid-api-key` on `/assessments`.
