# ⚛️ TASK 05 — Move security classes to shared.infrastructure.config.security

> **Status:** DONE
> **Workflow:** REFACTOR
> **Depends On:** task-03
> [← story file](../story-01-shared-kernel.md)

---

## Objective

Move all 6 classes from `cl.gradeops.ai.api.security` to `cl.gradeops.ai.api.shared.infrastructure.config.security`, update all importing files (main and test), and delete the now-empty `security/` package.

---

## Technical Design

- **Approach:** Pure package-rename move. No business logic changes. Spring picks up these classes via root component scan (`@SpringBootApplication` on `GradeOpsApiApplication`); no `@ComponentScan` annotation needs changing. `FirebaseTokenFilter` and `EmailVerifiedFilter` still import `port.AuthPort` and `port.TeacherIdentity` — those remain in `port/` during Story 01 and move in Story 02. That cross-story dependency is expected and intentional.
- **Affected files / components:**
  - 6 files MOVED into `src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/security/`:
    - `SecurityConfig.java`
    - `FirebaseTokenFilter.java`
    - `EmailVerifiedFilter.java`
    - `InternalAuthFilter.java`
    - `OwnershipVerifier.java`
    - `AuthenticatedTeacher.java`
  - 6 original files in `security/` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/auth/AuthController.java` ← UPDATE import (`AuthenticatedTeacher`)
  - `src/test/java/cl/gradeops/ai/api/security/EmailVerifiedFilterTest.java` ← UPDATE package imports
  - `src/test/java/cl/gradeops/ai/api/security/FirebaseTokenFilterTest.java` ← UPDATE package imports
  - `src/test/java/cl/gradeops/ai/api/security/OwnershipVerifierTest.java` ← UPDATE package import (`OwnershipVerifier`)
- **Interfaces / contracts:** `AuthenticatedTeacher(String uid, String email)` record — public API unchanged. `OwnershipVerifier.verify(String ownerUid, String authenticatedUid, String resourceId)` — public API unchanged.
- **Design notes:** `OwnershipVerifier` imports `shared.domain.exception.ResourceNotFoundException` (already updated in task-03). No circular dependency is introduced. `AuthController` imports `AuthenticatedTeacher` from the old `security/` package — this import must be updated here. Do not update `AuthController`'s service wiring (that is Story 02 work).

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/security/`.
2. For each of the 6 classes, create a new file in the new package directory with only the `package` declaration changed:
   - `SecurityConfig.java`: `package cl.gradeops.ai.api.shared.infrastructure.config.security;`
   - `FirebaseTokenFilter.java`: same package change (imports for `port.AuthPort` and `port.TeacherIdentity` remain unchanged — they stay in `port/` for now)
   - `EmailVerifiedFilter.java`: same package change (`port.TeacherIdentity` import unchanged)
   - `InternalAuthFilter.java`: same package change
   - `OwnershipVerifier.java`: same package change (already imports from `shared.domain.exception.ResourceNotFoundException` after task-03)
   - `AuthenticatedTeacher.java`: same package change
3. Delete all 6 original files under `src/main/java/cl/gradeops/ai/api/security/`.
4. In `src/main/java/cl/gradeops/ai/api/auth/AuthController.java`: change import:
   - Remove: `import cl.gradeops.ai.api.security.AuthenticatedTeacher;`
   - Add: `import cl.gradeops.ai.api.shared.infrastructure.config.security.AuthenticatedTeacher;`
5. Update security test files (all 3 are in `src/test/java/cl/gradeops/ai/api/security/`):
   - `EmailVerifiedFilterTest.java`: update imports for `EmailVerifiedFilter` and any other moved class.
   - `FirebaseTokenFilterTest.java`: update imports for `FirebaseTokenFilter`.
   - `OwnershipVerifierTest.java`: update imports for `OwnershipVerifier` (also update `GlobalExceptionHandler` import from task-04 if not done already).
6. Run `./mvnw compile -q` to confirm clean build.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | No `.java` files remain in `security/` | `find src/main/java/cl/gradeops/ai/api/security -name "*.java"` returns 0 |
| 2 | No imports of old `cl.gradeops.ai.api.security.*` outside the new package | `grep -rn "import cl.gradeops.ai.api.security\." src/` returns 0 |
| 3 | `SecurityConfig` is in new location and Spring can wire it | `./mvnw compile -q` exits 0 |
| 4 | Security test files compile | `./mvnw test-compile -q` exits 0 |

---

## Done Criteria

- [ ] `src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/security/` contains all 6 classes
- [ ] `src/main/java/cl/gradeops/ai/api/security/` directory has no `.java` files
- [ ] `grep -rn "import cl.gradeops.ai.api.security\." src/` returns 0 results
- [ ] `AuthController.java` imports `AuthenticatedTeacher` from new location
- [ ] All 3 security test files compile with updated imports
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-01-shared-kernel.md)
