# ⚛️ TASK 10 — Retrieval endpoints + dashboard wiring

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-03
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

Expose current-draft and version-history retrieval endpoints, and complete the `Assessment` stub replacement started in task-01 by making `findAllByTeacherId` return real dashboard data instead of an empty list.

---

## Technical Design

- **Approach:** two GET endpoints (current draft, version history) plus finishing `AssessmentPersistenceAdapter.findAllByTeacherId` (left as an empty-list-preserving stub-equivalent in task-01) to join `Assessment` with its current draft's title, producing real `AssessmentSummaryResult`s for the dashboard. This is the point where the existing `GET /api/v1/assessments` endpoint (`ListAssessmentsUseCase`/`ListAssessmentsHandler`, unchanged since task-01) starts returning real teacher assessments instead of `[]`.
- **Affected files / components:**
  - `assessment/application/port/in/GetCurrentDraftUseCase.java`, `application/usecase/GetCurrentDraftHandler.java` (new)
  - `assessment/application/port/in/ListDraftVersionsUseCase.java`, `application/usecase/ListDraftVersionsHandler.java` (new)
  - `assessment/infrastructure/adapter/in/web/AssessmentController.java` (**modify** — add `GET /api/v1/assessments/{id}/draft`, `GET /api/v1/assessments/{id}/draft/versions`)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentPersistenceAdapter.java` (**modify** — `findAllByTeacherId` now joins the current draft's title per assessment; `submissionCount`/`pendingApprovals`/`reportLink` remain `0`/`0`/`null` — those belong to later epics (grading, feedback) and are explicitly out of scope here)
  - response DTOs for both new endpoints
- **Interfaces / contracts:** `GET /api/v1/assessments/{id}/draft` → current version (404 if none yet). `GET /api/v1/assessments/{id}/draft/versions` → all versions ordered newest-first. `GET /api/v1/assessments` (existing) → now returns real rows with `title` sourced from the current draft (or `null`/the brief's `topic` if no draft exists yet — decide and document the exact fallback during implementation).
- **Risk:** Low — read-only endpoints; main risk is an N+1 query when joining assessments with their current drafts for the list endpoint — mitigated by a single query (e.g. a repository method that joins/subqueries rather than looping `findCurrentByAssessmentId` per assessment).
- **Design notes:** all three endpoints must enforce that the assessment belongs to the authenticated teacher (`teacherUid` match), returning 404 (not 403, to avoid existence disclosure — matches the project's existing `OwnershipVerifier` convention from prior plannings) for a cross-teacher access attempt.

---

## Implementation Steps

1. Create `GetCurrentDraftHandler.java` — loads the assessment (ownership-checked), returns the current draft or 404.
2. Create `ListDraftVersionsHandler.java` — loads the assessment (ownership-checked), returns all versions newest-first.
3. Modify `AssessmentPersistenceAdapter.findAllByTeacherId` to join with the current draft per assessment in a single query (avoid N+1); populate `title` from the current draft (or the brief's `topic` as fallback), leave `submissionCount`/`pendingApprovals`/`reportLink` at their existing placeholder defaults.
4. Add `GET /api/v1/assessments/{id}/draft` and `GET /api/v1/assessments/{id}/draft/versions` to `AssessmentController.java`, both ownership-checked (404 on cross-teacher access, matching the project's existing `OwnershipVerifier` pattern from prior epics if reusable, or an inline equivalent check if not).
5. Create response DTOs for both new endpoints.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `GET .../draft` returns the current version, 404 if none | Integration test both cases |
| 2 | `GET .../draft/versions` returns all versions, newest-first, none lost | Integration test with 3 versions |
| 3 | `GET /api/v1/assessments` (dashboard) now returns real rows with a title | Integration test: create an assessment + generate a draft, assert it appears in the list with the draft's title |
| 4 | Cross-teacher access returns 404, not 403 or data leakage | Test: teacher A requests teacher B's assessment draft, assert 404 |
| 5 | `./mvnw test` passes | Full suite green |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up db` |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors |
| 3 | Connectivity or schema validation succeeds | No new migration in this task |
| 4 | Changed surface responds correctly | Full flow: create brief → generate → `GET /api/v1/assessments` shows it with a title → `GET .../draft` returns it → `GET .../draft/versions` lists it |
| 5 | No startup or migration regressions are visible | App logs clean |

### Database / ORM Consistency Check

N/A — no schema change in this task (query-only changes).

---

## Done Criteria

- [ ] `GET /api/v1/assessments/{id}/draft` and `.../draft/versions` work and are ownership-checked.
- [ ] `GET /api/v1/assessments` dashboard endpoint returns real data (title populated) instead of `[]`, with no N+1 query.
- [ ] Cross-teacher access returns 404 on all three endpoints.
- [ ] `./mvnw test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
