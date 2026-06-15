# ⚛️ TASK 01 — OwnershipVerifier Utility in api/

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-07-cross-teacher-access-denial.md)

---

## Objective

Implement `OwnershipVerifier` in `api/` — a reusable service that checks whether the authenticated teacher owns a given resource and throws `ResourceNotFoundException` (mapped to HTTP 404) if not.

---

## Technical Design

- **Approach:** `OwnershipVerifier` is a `@Component` with a generic `verify(String ownerUid, String authenticatedUid, String resourceId)` method. If `ownerUid.equals(authenticatedUid)`, it passes. Otherwise, it throws `ResourceNotFoundException(resourceId)`. `ResourceNotFoundException` is a custom `RuntimeException` mapped to HTTP 404 via `@ResponseStatus(HttpStatus.NOT_FOUND)` or a `@RestControllerAdvice`. The 404 response body is identical to a resource-not-found response — no `403` variant exists.
- **Affected files / components:**
  - `api/src/main/java/com/gradeops/api/security/OwnershipVerifier.java` (new)
  - `api/src/main/java/com/gradeops/api/common/ResourceNotFoundException.java` (new)
  - `api/src/main/java/com/gradeops/api/common/GlobalExceptionHandler.java` (new or update — maps `ResourceNotFoundException` to 404)
- **Interfaces / contracts:** `ownershipVerifier.verify(resource.getTeacherFirebaseUid(), authenticatedTeacher.uid(), resourceId)`. Called by any service that handles teacher-scoped resources. Returns void on success, throws on violation.
- **Design notes:** The response body for 404 (cross-teacher or truly not found) must be identical: `{"error": "NOT_FOUND", "resource": "<id>"}`. This prevents existence disclosure. Every teacher-scoped entity in Epics 02+ must call `OwnershipVerifier` before returning data.

---

## Implementation Steps

1. Create `api/src/main/java/com/gradeops/api/common/ResourceNotFoundException.java`:
   - `public class ResourceNotFoundException extends RuntimeException { ... }` with `resourceId` field.
2. Create `api/src/main/java/com/gradeops/api/security/OwnershipVerifier.java`:
   - `@Component` with `verify(String ownerUid, String authenticatedUid, String resourceId)`.
   - Throws `ResourceNotFoundException(resourceId)` when UIDs don't match.
3. Create or update `api/src/main/java/com/gradeops/api/common/GlobalExceptionHandler.java`:
   - `@RestControllerAdvice` with `@ExceptionHandler(ResourceNotFoundException.class)` → return `ResponseEntity` with 404 and `{"error": "NOT_FOUND", "resource": "<id>"}`.
4. Write `OwnershipVerifierTest.java`.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Owner UID matches authenticated UID | No exception thrown | `OwnershipVerifierTest` |
| 2 | Owner UID differs from authenticated UID | `ResourceNotFoundException` thrown | `OwnershipVerifierTest` |
| 3 | `ResourceNotFoundException` reaches controller | HTTP 404 `{"error": "NOT_FOUND", ...}` | `GlobalExceptionHandlerTest` |

---

## Done Criteria

- [x] `OwnershipVerifier.java` exists as a `@Component`.
- [x] Mismatched UIDs throw `ResourceNotFoundException`, not an access-denied exception.
- [x] `GlobalExceptionHandler` maps it to HTTP 404 with a generic not-found body.
- [x] Response body for denied access is identical to a genuinely not-found resource.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-07-cross-teacher-access-denial.md)
