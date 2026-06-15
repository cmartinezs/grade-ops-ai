# ⚛️ TASK 02 — PATCH /internal/teachers/{uid}/flags Endpoint

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← scope file](../scope-03-pilot-account-flag.md)

---

## Objective

Implement `PATCH /internal/teachers/{uid}/flags` in `api/` allowing the operator to set or update pilot flag, related-party flag, offer details, and evidence link for a teacher account, with an audit trail.

---

## Technical Design

- **Approach:** `InternalTeacherController` already exists (scope-06 task-03). Add a `PATCH /internal/teachers/{uid}/flags` handler that updates the pilot flag columns and records `flag_set_by` and `flag_set_at`. Use `@Transactional`. Request body uses partial update semantics (only provided fields are set). Secured by the same `InternalAuthFilter` (`X-Internal-Key` header) from scope-06 task-03. Teachers are queryable by `plan_type` and `related_party` via a repository method.
- **Affected files / components:**
  - `api/src/main/java/com/gradeops/api/internal/teacher/InternalTeacherController.java` (update — add `patchFlags` handler)
  - `api/src/main/java/com/gradeops/api/internal/teacher/PilotFlagRequest.java` (new — `planType`, `relatedParty`, `offerDetails`, `evidenceLink`, `setBy`)
  - `api/src/main/java/com/gradeops/api/internal/teacher/PilotFlagService.java` (new — `updateFlags(uid, request)`)
  - `api/src/main/java/com/gradeops/api/domain/teacher/TeacherEntity.java` (update — add flag fields)
  - `api/src/main/java/com/gradeops/api/domain/teacher/TeacherRepository.java` (update — add `findByRelatedParty(boolean)` and `findByPlanType(String)`)
- **Interfaces / contracts:** `PATCH /internal/teachers/{uid}/flags` body: `{"planType": "pilot", "relatedParty": true, "offerDetails": "...", "evidenceLink": "...", "setBy": "admin@gradeops.ai"}`. Response 200 with updated teacher summary. 404 if UID not found.
- **Design notes:** `flag_set_at` is set server-side to `NOW()` — not provided by the client. Partial updates: fields not included in the request body are left unchanged (use `Optional` params in the request DTO or a `Map<String, Object>` patch approach). The `setBy` field identifies the operator for the audit trail.

---

## Implementation Steps

1. Update `TeacherEntity.java` to include the 6 new flag fields (mapped to V2 migration columns).
2. Create `PilotFlagRequest.java` record with optional fields.
3. Create `PilotFlagService.java` with `updateFlags(String uid, PilotFlagRequest req)`: loads the teacher by UID (404 if not found), applies non-null fields from the request, sets `flagSetAt = Instant.now()`, saves.
4. Update `TeacherRepository.java` with `findByPlanType(String type)` and `findByRelatedParty(boolean value)` queries.
5. Add `patchFlags(@PathVariable String uid, @RequestBody PilotFlagRequest req)` handler to `InternalTeacherController.java`.
6. Write `PilotFlagControllerTest.java`.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Valid PATCH sets flags | HTTP 200; `plan_type`, `related_party` updated in DB | `PilotFlagControllerTest` |
| 2 | UID not found | HTTP 404 | `PilotFlagControllerTest` |
| 3 | Missing `X-Internal-Key` | HTTP 403 | `PilotFlagControllerTest` |
| 4 | Query by `plan_type=pilot` | Returns only pilot accounts | `TeacherRepositoryTest` |

---

## Done Criteria

- [x] `PATCH /internal/teachers/{uid}/flags` returns 200 with updated data.
- [x] Unknown UID returns 404.
- [x] `flag_set_at` is set server-side on each update.
- [x] `TeacherRepository` supports query-by-plan-type and query-by-related-party.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-03-pilot-account-flag.md)
