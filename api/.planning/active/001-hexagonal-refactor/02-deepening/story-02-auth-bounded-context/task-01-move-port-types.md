# ⚛️ TASK 01 — Move TeacherIdentity + AuthPort from port/ to auth packages; delete port/

> **Status:** TODO
> **Workflow:** REFACTOR
> **Depends On:** —
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Relocate `TeacherIdentity` from `port/` to `auth.domain.model.TeacherIdentity` and `AuthPort` from `port/` to `auth.application.port.out.AuthPort`. Update all 8 importing files so the build stays green. Delete the `port/` directory (which should contain only these two files after Story 01 removed `StoragePort`).

---

## Technical Design

- **Approach:** Both types move together in one task because they are referenced by the same files (`FirebaseTokenFilter`, `FirebaseAuthAdapter`). Touching those files twice (once per type) would risk intermediate compile failures. `TeacherIdentity` is a value object and belongs in `auth.domain.model` because it models an identity claim returned from Firebase token verification — an auth domain concept. `AuthPort` is an output port of the auth application layer. Both package changes are pure rename; no logic changes.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/domain/model/TeacherIdentity.java` ← NEW (moved; package changed)
  - `src/main/java/cl/gradeops/ai/api/auth/application/port/out/AuthPort.java` ← NEW (moved; package + `TeacherIdentity` import updated)
  - `src/main/java/cl/gradeops/ai/api/port/TeacherIdentity.java` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/port/AuthPort.java` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/security/FirebaseTokenFilter.java` ← UPDATE both imports
  - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/security/EmailVerifiedFilter.java` ← UPDATE `TeacherIdentity` import
  - `src/main/java/cl/gradeops/ai/api/auth/AuthService.java` ← UPDATE both imports (old class, deleted in task-12)
  - `src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java` ← UPDATE `AuthPort` import (old class, deleted in task-12)
  - `src/main/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapter.java` ← UPDATE both imports (moves in task-08)
  - `src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java` ← UPDATE `AuthPort` import
  - `src/test/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapterTest.java` ← UPDATE `TeacherIdentity` import
  - `src/test/java/cl/gradeops/ai/api/security/EmailVerifiedFilterTest.java` ← UPDATE `TeacherIdentity` import
- **Interfaces / contracts:** `TeacherIdentity(String uid, String email, boolean emailVerified, String name, String signInProvider)` — record, unchanged. `AuthPort` — interface, unchanged methods; only `TeacherIdentity` import updated internally.
- **Design notes:** After this task `port/` must be empty and deleted. The `shared/infrastructure/config/security/` files were moved from `security/` in Story 01 but retained old `port.*` imports as an acknowledged intermediate state — this task resolves that debt.

---

## Implementation Steps

1. Create directories `src/main/java/cl/gradeops/ai/api/auth/domain/model/` and `src/main/java/cl/gradeops/ai/api/auth/application/port/out/`.
2. Create `auth/domain/model/TeacherIdentity.java` — copy body from `port/TeacherIdentity.java`, change package to `cl.gradeops.ai.api.auth.domain.model`.
3. Create `auth/application/port/out/AuthPort.java` — copy body from `port/AuthPort.java`, change package to `cl.gradeops.ai.api.auth.application.port.out`, update `TeacherIdentity` return type import to `cl.gradeops.ai.api.auth.domain.model.TeacherIdentity`.
4. Delete `src/main/java/cl/gradeops/ai/api/port/TeacherIdentity.java`.
5. Delete `src/main/java/cl/gradeops/ai/api/port/AuthPort.java`.
6. In `shared/infrastructure/config/security/FirebaseTokenFilter.java`:
   - Replace `import cl.gradeops.ai.api.port.AuthPort;` → `import cl.gradeops.ai.api.auth.application.port.out.AuthPort;`
   - Replace `import cl.gradeops.ai.api.port.TeacherIdentity;` → `import cl.gradeops.ai.api.auth.domain.model.TeacherIdentity;`
7. In `shared/infrastructure/config/security/EmailVerifiedFilter.java`:
   - Replace `import cl.gradeops.ai.api.port.TeacherIdentity;` → `import cl.gradeops.ai.api.auth.domain.model.TeacherIdentity;`
8. In `auth/AuthService.java`:
   - Replace both `port.*` imports with new locations.
9. In `auth/PasswordResetService.java`:
   - Replace `import cl.gradeops.ai.api.port.AuthPort;` → new location.
10. In `adapter/auth/FirebaseAuthAdapter.java`:
    - Replace both `port.*` imports with new locations.
11. In `test/auth/PasswordResetServiceTest.java`:
    - Replace `import cl.gradeops.ai.api.port.AuthPort;` → new location.
12. In `test/adapter/auth/FirebaseAuthAdapterTest.java`:
    - Replace `import cl.gradeops.ai.api.port.TeacherIdentity;` → new location.
13. In `test/security/EmailVerifiedFilterTest.java`:
    - Replace `import cl.gradeops.ai.api.port.TeacherIdentity;` → new location.
14. Run `./mvnw compile -q` to confirm build is green.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `port/` directory is empty | `find src/main/java/cl/gradeops/ai/api/port -name "*.java"` returns 0 results |
| 2 | No remaining imports of `cl.gradeops.ai.api.port.*` | `grep -rn "import cl.gradeops.ai.api.port\." src/` returns 0 results |
| 3 | New types in correct packages | `find src/main/java -name "TeacherIdentity.java" -o -name "AuthPort.java"` shows only new paths |
| 4 | Build stays green | `./mvnw compile -q` exits 0 |

---

## Done Criteria

- [ ] `auth/domain/model/TeacherIdentity.java` exists with package `cl.gradeops.ai.api.auth.domain.model`
- [ ] `auth/application/port/out/AuthPort.java` exists with package `cl.gradeops.ai.api.auth.application.port.out`
- [ ] `port/TeacherIdentity.java` and `port/AuthPort.java` deleted; `port/` directory gone
- [ ] `grep -rn "import cl.gradeops.ai.api.port\." src/` returns 0 results
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-02-auth-bounded-context.md)
