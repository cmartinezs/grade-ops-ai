# Data Provider Draft Builder Screen

**Source:** task-10 | **Area:** unknown | **Date:** 2026-07-21

## What it does
`AssessmentDraftDto` matching `GenerateAssessmentDraftResponse`, and a `loadAssessmentDraftBuilderPage(assessmentId)` Screen Data Facade that fetches the current draft and its version list in parallel and returns one page-level view model — independent of the mockup UI, ready for `task-12` to call.

---

## How to use it
- Add `AssessmentDraftDto` to `src/types/assessment.ts`, matching `task-01`'s confirmed `GenerateAssessmentDraftResponse` shape exactly.
- Add `getAssessmentDraft(assessmentId: string)` to `src/lib/api/assessments.ts`, `GET`-ing `/api/v1/assessments/${assessmentId}/draft`.
- Add `getAssessmentDraftVersions(assessmentId: string)` `GET`-ing `/api/v1/assessments/${assessmentId}/draft/versions`, returning `AssessmentDraftDto[]`.
- Create `src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts` combining both via `Promise.all` and the `task-09` mapper.
- Write tests for `getAssessmentDraft`, `getAssessmentDraftVersions`, and the loader (including a case where one of the two parallel calls fails).
- --

## Example
Use `Add` through the public interface introduced by this task.
