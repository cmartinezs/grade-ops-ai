# Code Review: task-06-connect-real-api-intake-screen

Date: 2026-07-17
Scope: `task-06-connect-real-api-intake-screen.md` and the implementation that connects the Intake screen to `submitAssessmentBrief` (`src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts`, `src/app/(protected)/assessments/new/__tests__/NewAssessmentPage.test.tsx`, and the generated task suite).

## Findings

No findings.

## Re-review - 2026-07-17

### Findings

No findings. The previously noted documentation mismatch is not a `web/` defect: normalizing backend validation language belongs in `api/`, and keeping agent output aligned with the teacher's language likely belongs in the `api/` → `agents/` contract. This task's web-side obligation is to avoid leaking raw backend English strings to the teacher, which the current hook and test cover.

### Validation Notes

- The new runtime behavior in `useIntakeAssessmentPage` matches the task's "no English backend strings" rule: `CreateAssessmentBriefError` field-error messages are mapped by field name, not displayed verbatim.
- The updated `NewAssessmentPage` test covers the regression shape with backend text `"must not be blank"` and asserts the Spanish `"Ingresa el tema."` copy is shown instead.
- `FIELD_ERROR_MESSAGES` currently matches the five `briefSchema` required-field messages.
- Backend/API localization remains a separate follow-up outside `web/`, especially if the selected teacher language also needs to shape generated agent content.

### Verification

- `git fetch origin` — passed; local task branch is aligned with `origin/tasks/story-02-assessment-screens-wireframes-and-data-providers/task-06-connect-real-api-intake-screen` at `113da3f`.
- `npm run test -- NewAssessmentPage` — passed (`6 passed`, `1 suite`).
- `git diff --check origin/story-02-assessment-screens-wireframes-and-data-providers...HEAD` — passed.
- `npm run test -- --runInBand` — still fails only on the pre-existing unrelated English-selector tests in `RegisterPage.test.tsx` and `SignOutButton.test.tsx` (`75 passed`, `5 failed`).

## Validation Notes

- `useIntakeAssessmentPage` now calls `submitAssessmentBrief(values)` directly, keeps the form component passive, and routes success to `/assessments/{assessmentId}/draft`.
- The fake `setTimeout` / `trigger-field-error` submit path is removed from the hook.
- The error branching distinguishes the two task-required 422 shapes: `CreateAssessmentBriefError` with `FieldErrorResponse[]` becomes field errors, while `GenerateAssessmentDraftError` with `AGENT_REJECTED` becomes a business-rejection banner.
- `AGENT_ERROR` and `UNREACHABLE` both map to the separate service-unavailable message, and the fallback path remains a generic Spanish retry message.
- The focused page test exercises the real hook/UI boundary while mocking only `submitAssessmentBrief`, which is the right seam for this task.
- The redirect target route is not implemented in this task branch; that matches the current story breakdown, where `src/app/(protected)/assessments/[id]/draft/page.tsx` is introduced later by `task-09` and verified end-to-end by `task-13`.

## Verification

- `npm run test -- NewAssessmentPage` — passed (`6 passed`, `1 suite`).
- `git diff --check origin/story-02-assessment-screens-wireframes-and-data-providers...HEAD` — passed.
- `npm run test -- --runInBand` — still fails only on the pre-existing unrelated English-selector tests in `RegisterPage.test.tsx` and `SignOutButton.test.tsx` (`75 passed`, `5 failed`).
- `npm run lint` — not usable as a non-interactive gate in this repo yet; `next lint` prompts to configure ESLint.
- `./node_modules/.bin/tsc --noEmit` — blocked by the pre-existing missing Jest globals / missing `@types/jest` test-type gap.
- `npm run build` — compiles successfully, then fails during prerender with the pre-existing Firebase `auth/invalid-api-key` configuration issue.
