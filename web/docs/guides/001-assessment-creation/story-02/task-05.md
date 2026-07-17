# Data Provider Intake Screen

**Source:** task-05 | **Area:** unknown | **Date:** 2026-07-17

## What it does
DTOs for the brief-intake/draft-generation endpoints and a `submitAssessmentBrief()` function in `lib/api` that orchestrates the two sequential real calls (`POST /assessments` then `POST /assessments/{id}/draft`) behind one interface — independent of the mockup UI, ready for `task-06` to call.

---

## How to use it
- Add `CreateAssessmentBriefRequestDto`/`CreateAssessmentBriefResponseDto` to `src/types/assessment.ts`, matching `task-01`'s confirmed shapes exactly.
- Add `createAssessmentBrief(brief: CreateAssessmentBriefRequestDto)` to `src/lib/api/assessments.ts`, `POST`-ing to `/api/v1/assessments` via `apiClient`, throwing on non-2xx per the existing `getAssessments()` pattern in the same file.
- Add `generateAssessmentDraft(assessmentId: string)` `POST`-ing to `/api/v1/assessments/${assessmentId}/draft` via `apiClient`.
- Add `submitAssessmentBrief(brief)` calling both in sequence, distinguishing which step failed in the thrown error.
- Extend `src/lib/api/__tests__/assessments.test.ts` with tests for all three functions, including the case where step 2 fails after step 1 succeeds.
- --

## Example
Use `Add` through the public interface introduced by this task.
