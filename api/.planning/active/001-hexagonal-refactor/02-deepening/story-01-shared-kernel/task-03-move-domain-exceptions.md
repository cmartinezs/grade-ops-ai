# ⚛️ TASK 03 — Move domain exceptions to shared.domain.exception

> **Status:** DONE
> **Workflow:** REFACTOR
> **Depends On:** task-02
> [← story file](../story-01-shared-kernel.md)

---

## Objective

Relocate `ResourceNotFoundException` and `DuplicateEmailException` from `common/` to `shared.domain.exception`, updating them to extend `DomainException`. Update all four importing files. Delete the two old `common/` exception files. `common/` still contains `GlobalExceptionHandler` and `ApiError` after this task — those move in task-04.

---

## Technical Design

- **Approach:** The two exceptions are structural moves — their public API (constructors, accessor methods) does not change. The only new element is that they now extend `DomainException` instead of `RuntimeException` directly, which is backwards-compatible for callers. `DomainException` extends `RuntimeException`, so all existing catch clauses remain valid.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/shared/domain/exception/ResourceNotFoundException.java` ← NEW (moved + reparented)
  - `src/main/java/cl/gradeops/ai/api/shared/domain/exception/DuplicateEmailException.java` ← NEW (moved + reparented)
  - `src/main/java/cl/gradeops/ai/api/common/ResourceNotFoundException.java` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/common/DuplicateEmailException.java` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/security/OwnershipVerifier.java` ← UPDATE import
  - `src/main/java/cl/gradeops/ai/api/internal/teacher/PilotFlagService.java` ← UPDATE import
  - `src/main/java/cl/gradeops/ai/api/internal/teacher/ProvisionTeacherService.java` ← UPDATE import (DuplicateEmailException)
  - `src/test/java/cl/gradeops/ai/api/security/OwnershipVerifierTest.java` ← UPDATE import (ResourceNotFoundException)
- **Interfaces / contracts:** `ResourceNotFoundException(String resourceId)` and `getResourceId()` are unchanged. `DuplicateEmailException(String email)` and `getEmail()` are unchanged.
- **Design notes:** `GlobalExceptionHandler` (still in `common/` at this stage) currently uses same-package access for `DuplicateEmailException` and `ResourceNotFoundException` — after this task it will need explicit imports, but that update happens in task-04 when `GlobalExceptionHandler` itself moves. During the intermediate state between task-03 and task-04 the build will still compile because `GlobalExceptionHandler` still lives in `common/` and can be updated to use explicit imports pointing to the new location.
  - Verify with `grep -rn "common\.ResourceNotFoundException\|common\.DuplicateEmailException" src/` after completion — must return 0 results.

---

## Implementation Steps

1. Create `src/main/java/cl/gradeops/ai/api/shared/domain/exception/ResourceNotFoundException.java`:
   ```java
   package cl.gradeops.ai.api.shared.domain.exception;

   public class ResourceNotFoundException extends DomainException {

       private final String resourceId;

       public ResourceNotFoundException(String resourceId) {
           super("Resource not found: " + resourceId);
           this.resourceId = resourceId;
       }

       public String getResourceId() { return resourceId; }
   }
   ```
2. Create `src/main/java/cl/gradeops/ai/api/shared/domain/exception/DuplicateEmailException.java`:
   ```java
   package cl.gradeops.ai.api.shared.domain.exception;

   public class DuplicateEmailException extends DomainException {

       private final String email;

       public DuplicateEmailException(String email) {
           super("Teacher with email already exists: " + email);
           this.email = email;
       }

       public String getEmail() { return email; }
   }
   ```
3. Delete `src/main/java/cl/gradeops/ai/api/common/ResourceNotFoundException.java`.
4. Delete `src/main/java/cl/gradeops/ai/api/common/DuplicateEmailException.java`.
5. In `src/main/java/cl/gradeops/ai/api/common/GlobalExceptionHandler.java`: add explicit imports for the two exceptions now that they are no longer in the same package:
   ```java
   import cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException;
   import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
   ```
6. In `src/main/java/cl/gradeops/ai/api/security/OwnershipVerifier.java`: change import:
   - Remove: `import cl.gradeops.ai.api.common.ResourceNotFoundException;`
   - Add: `import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;`
7. In `src/main/java/cl/gradeops/ai/api/internal/teacher/PilotFlagService.java`: change import:
   - Remove: `import cl.gradeops.ai.api.common.ResourceNotFoundException;`
   - Add: `import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;`
8. In `src/main/java/cl/gradeops/ai/api/internal/teacher/ProvisionTeacherService.java`: change import:
   - Remove: `import cl.gradeops.ai.api.common.DuplicateEmailException;`
   - Add: `import cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException;`
9. In `src/test/java/cl/gradeops/ai/api/security/OwnershipVerifierTest.java`: change import:
   - Remove: `import cl.gradeops.ai.api.common.ResourceNotFoundException;`
   - Add: `import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;`
10. Run `./mvnw compile -q` to confirm clean build.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | No remaining references to old exception locations | `grep -rn "common\.ResourceNotFoundException\|common\.DuplicateEmailException" src/` returns 0 lines |
| 2 | New exceptions compile with correct parent | `./mvnw compile -q` exits 0 |
| 3 | `OwnershipVerifierTest` still compiles | `./mvnw test-compile -q` exits 0 |
| 4 | Both new classes extend `DomainException` | `grep "extends DomainException" src/main/java/cl/gradeops/ai/api/shared/domain/exception/*.java` shows 2 matches |

---

## Done Criteria

- [ ] `src/main/java/cl/gradeops/ai/api/shared/domain/exception/ResourceNotFoundException.java` exists, extends `DomainException`, has `getResourceId()`
- [ ] `src/main/java/cl/gradeops/ai/api/shared/domain/exception/DuplicateEmailException.java` exists, extends `DomainException`, has `getEmail()`
- [ ] `src/main/java/cl/gradeops/ai/api/common/ResourceNotFoundException.java` deleted
- [ ] `src/main/java/cl/gradeops/ai/api/common/DuplicateEmailException.java` deleted
- [ ] `grep -rn "common\.ResourceNotFoundException\|common\.DuplicateEmailException" src/` returns 0 results
- [ ] `./mvnw compile -q` exits 0 (build compiles cleanly in intermediate state)
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-01-shared-kernel.md)
