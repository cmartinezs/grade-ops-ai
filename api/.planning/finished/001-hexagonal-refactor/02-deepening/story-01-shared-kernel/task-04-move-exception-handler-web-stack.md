# ⚛️ TASK 04 — Move exception-handler web stack to shared.infrastructure.adapter.in.web

> **Status:** DONE
> **Workflow:** REFACTOR
> **Depends On:** task-03
> [← story file](../story-01-shared-kernel.md)

---

## Objective

Move `GlobalExceptionHandler`, rename `ApiError` → `ApiErrorResponse`, introduce `FieldErrorResponse`, and move `InvalidTokenException` (from `auth/`) all into `cl.gradeops.ai.api.shared.infrastructure.adapter.in.web`. Delete the now-empty `common/` package and `auth/InvalidTokenException.java`.

---

## Technical Design

- **Approach:** `GlobalExceptionHandler` is an `@RestControllerAdvice` that Spring scans by component scan — moving its package requires no Spring XML or `@Import` wiring. After the move it will import exceptions from `shared.domain.exception` (already in new location from task-03). `ApiError` is renamed to `ApiErrorResponse` per the naming guideline (guideline 09: response DTOs end in `Response`). `FieldErrorResponse` is a new record that replaces the inline string-joining in `handleValidation()` — it models a structured per-field error `{ field, message }` to improve API ergonomics. `InvalidTokenException` is moved here because it is a web-boundary concern (it maps to HTTP 401 in the exception handler), not an auth domain exception.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/in/web/GlobalExceptionHandler.java` ← NEW (moved)
  - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/in/web/ApiErrorResponse.java` ← NEW (renamed from `ApiError`)
  - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/in/web/FieldErrorResponse.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/in/web/InvalidTokenException.java` ← NEW (moved from `auth/`)
  - `src/main/java/cl/gradeops/ai/api/common/GlobalExceptionHandler.java` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/common/ApiError.java` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/auth/InvalidTokenException.java` ← DELETE
  - `src/test/java/cl/gradeops/ai/api/security/OwnershipVerifierTest.java` ← UPDATE import (`GlobalExceptionHandler` reference)
- **Interfaces / contracts:**
  - `ApiErrorResponse(String error, String message)` — same record shape as old `ApiError`, factory methods `of(String)` and `of(String, String)` preserved.
  - `FieldErrorResponse(String field, String message)` — new record; `GlobalExceptionHandler.handleValidation()` now returns `ResponseEntity<List<FieldErrorResponse>>` instead of `ResponseEntity<ApiError>` with a joined string.
  - `InvalidTokenException(String message)` — unchanged constructor.
- **Design notes:** The `handleValidation()` method signature change (from `ApiError` with concatenated string to `List<FieldErrorResponse>`) is a **behavioural change** in the validation error response body. The web contract spec must be updated to reflect the new JSON shape. Verify the web team's `AuthControllerTest` and `AssessmentControllerTest` do not assert on the old validation error body format — if they do, update those assertions.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/in/web/`.
2. Create `ApiErrorResponse.java` (record, same shape as old `ApiError`):
   ```java
   package cl.gradeops.ai.api.shared.infrastructure.adapter.in.web;

   public record ApiErrorResponse(String error, String message) {
       public static ApiErrorResponse of(String error) { return new ApiErrorResponse(error, null); }
       public static ApiErrorResponse of(String error, String message) { return new ApiErrorResponse(error, message); }
   }
   ```
3. Create `FieldErrorResponse.java`:
   ```java
   package cl.gradeops.ai.api.shared.infrastructure.adapter.in.web;

   public record FieldErrorResponse(String field, String message) {}
   ```
4. Create `InvalidTokenException.java` (copied from `auth/`, package updated):
   ```java
   package cl.gradeops.ai.api.shared.infrastructure.adapter.in.web;

   public class InvalidTokenException extends RuntimeException {
       public InvalidTokenException(String message) { super(message); }
   }
   ```
5. Create `GlobalExceptionHandler.java` with updated package, updated imports, and updated `handleValidation()`:
   - Package: `cl.gradeops.ai.api.shared.infrastructure.adapter.in.web`
   - Imports: `shared.domain.exception.DuplicateEmailException`, `shared.domain.exception.ResourceNotFoundException`, `InvalidTokenException` (same package — no import needed), `ApiErrorResponse` (same package)
   - `handleValidation()` now returns `ResponseEntity<List<FieldErrorResponse>>`:
     ```java
     @ExceptionHandler(MethodArgumentNotValidException.class)
     public ResponseEntity<List<FieldErrorResponse>> handleValidation(MethodArgumentNotValidException ex) {
         List<FieldErrorResponse> errors = ex.getBindingResult().getFieldErrors().stream()
                 .map(e -> new FieldErrorResponse(e.getField(), e.getDefaultMessage()))
                 .toList();
         return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
     }
     ```
   - All other handler methods: change `ApiError` → `ApiErrorResponse`, keep same logic.
6. Delete `src/main/java/cl/gradeops/ai/api/common/GlobalExceptionHandler.java`.
7. Delete `src/main/java/cl/gradeops/ai/api/common/ApiError.java`.
8. Delete `src/main/java/cl/gradeops/ai/api/auth/InvalidTokenException.java`.
9. In `src/test/java/cl/gradeops/ai/api/security/OwnershipVerifierTest.java`: update import from `cl.gradeops.ai.api.common.GlobalExceptionHandler` to `cl.gradeops.ai.api.shared.infrastructure.adapter.in.web.GlobalExceptionHandler`.
10. Search for any remaining `import cl.gradeops.ai.api.auth.InvalidTokenException` in `src/` and update to new location. Run: `grep -rn "auth\.InvalidTokenException" src/`
11. Run `./mvnw compile -q` to confirm clean build.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `common/` package has no `.java` files | `find src/main/java/cl/gradeops/ai/api/common -name "*.java"` returns 0 results |
| 2 | `auth/InvalidTokenException.java` deleted | `find src/main/java/cl/gradeops/ai/api/auth -name "InvalidTokenException.java"` returns 0 results |
| 3 | No remaining imports of old `ApiError` class | `grep -rn "import cl.gradeops.ai.api.common.ApiError" src/` returns 0 results |
| 4 | `GlobalExceptionHandler` in new location is picked up by Spring | `./mvnw compile -q` exits 0; `@RestControllerAdvice` is component-scanned from `cl.gradeops.ai.api` root |

---

## Done Criteria

- [ ] `shared/infrastructure/adapter/in/web/GlobalExceptionHandler.java` exists with `@RestControllerAdvice`
- [ ] `shared/infrastructure/adapter/in/web/ApiErrorResponse.java` exists with `of(String)` and `of(String, String)` factory methods
- [ ] `shared/infrastructure/adapter/in/web/FieldErrorResponse.java` exists as a record with `field` and `message` components
- [ ] `shared/infrastructure/adapter/in/web/InvalidTokenException.java` exists
- [ ] `GlobalExceptionHandler.handleValidation()` returns `List<FieldErrorResponse>`, not `ApiError`/`ApiErrorResponse` with joined string
- [ ] `common/` directory is fully empty (no `.java` files remain)
- [ ] `auth/InvalidTokenException.java` deleted
- [ ] `grep -rn "import cl.gradeops.ai.api.common\." src/` returns 0 results
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-01-shared-kernel.md)
