# ⚛️ TASK 01 — GET /assessments API Endpoint

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-02-assessment-dashboard.md)

---

## Objective

Implement `GET /assessments` in `api/` returning an aggregated list of assessments for the authenticated teacher — each with status, submission count, and pending-approval count — in a single query.

---

## Technical Design

- **Approach:** `AssessmentController` → `AssessmentService` → `AssessmentRepository`. Since assessments don't exist yet (Epic 02 creates them), this endpoint returns an empty list for now. Implement the DTO and endpoint structure so it's usable immediately and extensible. Use a JPQL or native query to aggregate counts in one round-trip to avoid N+1. The `ApprovalEvent` table doesn't exist yet either — use `COALESCE(0)` placeholders and document the join as a TODO once Epics 03/05/06 add the table.
- **Affected files / components:**
  - `api/src/main/java/com/gradeops/api/assessment/AssessmentController.java` (new)
  - `api/src/main/java/com/gradeops/api/assessment/AssessmentService.java` (new)
  - `api/src/main/java/com/gradeops/api/assessment/AssessmentRepository.java` (new)
  - `api/src/main/java/com/gradeops/api/assessment/AssessmentSummaryDto.java` (new — `id`, `title`, `status`, `submissionCount`, `pendingApprovals`, `reportLink`)
  - `api/src/main/resources/db/migration/` — no new migration needed (assessment table created in Epic 02)
- **Interfaces / contracts:** `GET /assessments` (authenticated). Response: `[{"id": "...", "title": "...", "status": "DRAFT", "submissionCount": 0, "pendingApprovals": 0, "reportLink": null}]`. Returns `[]` if no assessments exist.
- **Design notes:** Teacher scoping is enforced by `OwnershipVerifier` (scope-07) — `AssessmentRepository` always filters by `teacher_firebase_uid`. Status values come from the `AssessmentStatus` enum owned by `api/` (not invented in the frontend). The response type is the canonical DTO that `web/` mirrors.

---

## Implementation Steps

1. Create `AssessmentSummaryDto.java` record with fields: `id`, `title`, `status`, `submissionCount`, `pendingApprovals`, `reportLink`.
2. Create `AssessmentRepository.java` with a method `findSummariesByTeacher(String firebaseUid)` returning `List<AssessmentSummaryDto>` (JPQL projection or empty-list stub if the `assessment` table doesn't exist yet — use a `@Query` that tolerates the missing table in tests).
3. Create `AssessmentService.java` delegating to the repository.
4. Create `AssessmentController.java`: `@GetMapping("/assessments")`, reads `AuthenticatedTeacher` from `SecurityContextHolder`, calls service, returns 200 with the list.
5. Write `AssessmentControllerTest.java`.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Authenticated teacher, no assessments | HTTP 200 `[]` | `AssessmentControllerTest` |
| 2 | Unauthenticated request | HTTP 401 | `AssessmentControllerTest` |
| 3 | Teacher with assessments (mock data) | HTTP 200 with correct summary fields | `AssessmentControllerTest` |

---

## Done Criteria

- [x] `GET /assessments` returns 200 `[]` for a teacher with no assessments.
- [x] Unauthenticated request returns 401.
- [x] Response is scoped to the authenticated teacher (no cross-teacher leakage).
- [x] `AssessmentSummaryDto` fields match the contract in Technical Notes.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-02-assessment-dashboard.md)
