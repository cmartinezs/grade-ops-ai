# ADR: `AssessmentDraftDto` matching `GenerateAssessmentDraftResponse`, and a `loadAssessmentDraftBuilderPage(assessmentId)` Screen Data Facade that fetches the current draft and its version list in parallel and returns one page-level view model — independent of the mockup UI, ready for `task-12` to call.

**Date:** 2026-07-21
**Status:** Accepted
**Planning:** 001-assessment-creation / story-02 / task-10

## Context
- **Approach:** This screen loads 2 remote sources on render (current draft + version list) — per `06-estado-datos-y-api.md` §7, that mandates a Screen Data Facade, not two separate `getX()` calls from the Page/hook. `loadAssessmentDraftBuilderPage` is that facade: it calls both via `Promise.all` (independent, not sequentially dependent) and returns the already-composed view model from `task-09`'s mapper.
- **Affected files / components:**
  - `src/types/assessment.ts` (add `AssessmentDraftDto`)
  - `src/lib/api/assessments.ts` (add `getAssessmentDraft()`, `getAssessmentDraftVersions()`)
  - `src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts` (new — the Screen Data Facade)
  - `src/lib/api/__tests__/assessments.test.ts` (extend), new test file for the loader
- **Interfaces / contracts:**
  ```ts
  export interface AssessmentDraftDto {
    draftId: string;
    title: string;
    context: string;
    instructions: string;
    objectives: string[];
    deliverables: string[];
    constraints: string[];
    versionNumber: number;
  }
  export async function loadAssessmentDraftBuilderPage(
    assessmentId: string
  ): Promise<AssessmentDraftBuilderPageViewModel>
  ```
  Internally: `const [draft, versions] = await Promise.all([getAssessmentDraft(assessmentId), getAssessmentDraftVersions(assessmentId)])`, then `toAssessmentDraftBuilderPageViewModel({ draft, versions })` (mapper from `task-09`).
- **Risk:** Low — both calls are independent GETs with no ordering dependency, a straightforward `Promise.all` case; the main risk is accidentally calling either `getX()` directly from a component instead of through this facade, which `task-12`'s review must catch.
- **Design notes:** `draftId` is a UUID string on the wire (per `task-01`) — keep it as `string` in the DTO, don't parse to a branded type unless a real need arises elsewhere.

---

## Decision
- **Risk:** Low — both calls are independent GETs with no ordering dependency, a straightforward `Promise.all` case; the main risk is accidentally calling either `getX()` directly from a component instead of through this facade, which `task-12`'s review must catch.

## Consequences
** `draftId` is a UUID string on the wire (per `task-01`) — keep it as `string` in the DTO, don't parse to a branded type unless a real need arises elsewhere.

## Alternatives Considered
- **Risk:** Low — both calls are independent GETs with no ordering dependency, a straightforward `Promise.all` case; the main risk is accidentally calling either `getX()` directly from a component instead of through this facade, which `task-12`'s review must catch.
