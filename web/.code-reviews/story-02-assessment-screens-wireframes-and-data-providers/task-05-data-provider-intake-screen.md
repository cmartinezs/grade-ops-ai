# Code Review: task-05-data-provider-intake-screen

Date: 2026-07-16
Scope: `task-05-data-provider-intake-screen.md` and the implementation it introduces for the intake mutation data provider (`src/lib/api/assessments.ts`, `src/types/assessment.ts`, `src/lib/logging/logger.ts`, `src/lib/api/__tests__/assessments.test.ts`, logging policy, package changes, and the generated task suite).

## Findings

### P2 - Correlation-id handoff is inconsistent before `task-06` consumes this provider

- Files:
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-05-data-provider-intake-screen.md:80`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-05-data-provider-intake-screen.md:108`
  - `.planning/LOGGING.md:14`
  - `src/lib/api/assessments.ts:77`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-06-connect-real-api-intake-screen.md:68`

`task-05`'s Logging section still says `submitAssessmentBrief` creates a UUID v4 correlation id, but the implemented code and `.planning/LOGGING.md` deliberately use a timestamp + random suffix because `crypto.randomUUID()` failed under jsdom. More importantly, `.planning/LOGGING.md` and `task-05` explicitly say the correlation id is not propagated as an HTTP header because no backend-side header contract exists yet, while `task-06` still tells the next implementer to propagate a correlation id header "through `submitAssessmentBrief`'s two calls (from `task-05`)."

That leaves the `task-06` handoff internally contradictory: either `task-05` should expose/propagate a header now, or `task-06` should be updated to verify the current child-logger correlation behavior instead of requiring a nonexistent header. As written, a correct `task-06` implementation against the current provider will appear to miss its own logging requirement.

Recommendation: align the planning docs before closing `task-05`. The narrow fix is to update `task-05` line 80 to match the implemented timestamp+random correlation id, and update `task-06`'s Logging / Observability section to remove the header requirement unless a concrete backend header name/contract is added.

## Validation Notes

- The mutation orchestration itself matches the task: `submitAssessmentBrief` calls `createAssessmentBrief` first, then `generateAssessmentDraft` with the returned `assessmentId`, and returns `{ assessmentId }`.
- Failure modes are distinguishable for `task-06`: `CreateAssessmentBriefError` carries the raw `FieldErrorResponse[] | ApiErrorResponse` body, while `GenerateAssessmentDraftError` carries `ApiErrorResponse` plus `assessmentId`.
- `AssessmentDraftDto` is not treated as missing in this review because this story explicitly defers that DTO to `task-10`; `task-05` only needs the intake submit result for navigation.
- Pino bundles through the Next build compile step; the remaining build failure is the pre-existing Firebase credential/prerender problem.

## Verification

- `git diff --check 4a37f4e8b47a87b0dfa145616f10eb4330534610...HEAD` — passed.
- `npm run test -- assessments` — passed; this runs `assessments.test.ts` plus the route test matched by the pattern (`9 passed`, `2 suites`).
- `npm run test -- --runInBand` — still fails only on known unrelated Spanish/English selector issues in `RegisterPage.test.tsx` and `SignOutButton.test.tsx` (`71 passed`, `5 failed`).
- `npm run build` — compiled successfully, then failed during static prerender with the pre-existing Firebase `auth/invalid-api-key` issue.
