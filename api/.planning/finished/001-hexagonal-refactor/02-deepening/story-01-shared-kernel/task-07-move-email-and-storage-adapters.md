# ⚛️ TASK 07 — Move email adapter + storage components to shared.infrastructure.adapter.out

> **Status:** DONE
> **Workflow:** REFACTOR
> **Depends On:** task-06
> [← story file](../story-01-shared-kernel.md)

---

## Objective

Rename `EmailService` → `JavaMailEmailService` and relocate it to `shared.infrastructure.adapter.out.email`. Move `StoragePort`, `GcsStorageAdapter`, and `R2StorageAdapter` to `shared.infrastructure.adapter.out.storage`. Update all importing files. Delete `email/`, `port/StoragePort`, and `adapter/storage/`. After this task, `port/` retains only `AuthPort` and `TeacherIdentity` (those move in Story 02).

---

## Technical Design

- **Approach:** `EmailService` is renamed to `JavaMailEmailService` because in Story 02 a new `EmailNotificationPort` interface will be introduced and `JavaMailEmailService` will implement it. The rename now avoids a collision between the current concrete class name and the future port name. The class body is identical — only package declaration and class name change. `StoragePort` moves from `port/` (a flat "ports dumping ground") to `shared.infrastructure.adapter.out.storage/` because storage is an infrastructure concern of the `shared` bounded context. `GcsStorageAdapter` and `R2StorageAdapter` implement `StoragePort`, so they move to the same package.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/out/email/JavaMailEmailService.java` ← NEW (renamed + moved)
  - `src/main/java/cl/gradeops/ai/api/email/EmailService.java` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/out/storage/StoragePort.java` ← NEW (moved)
  - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/out/storage/GcsStorageAdapter.java` ← NEW (moved)
  - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/out/storage/R2StorageAdapter.java` ← NEW (moved)
  - `src/main/java/cl/gradeops/ai/api/port/StoragePort.java` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/adapter/storage/GcsStorageAdapter.java` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/adapter/storage/R2StorageAdapter.java` ← DELETE
  - `src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java` ← UPDATE (field type `EmailService` → `JavaMailEmailService`, constructor param, import)
  - `src/test/java/cl/gradeops/ai/api/email/EmailServiceTest.java` ← UPDATE (class reference `EmailService` → `JavaMailEmailService`, import)
  - `src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java` ← UPDATE import (`EmailService` → `JavaMailEmailService`)
- **Interfaces / contracts:**
  - `JavaMailEmailService.sendPasswordReset(String toEmail, String firstName, String resetLink)` — same public method, unchanged signature. In Story 02 this class will gain `implements EmailNotificationPort`.
  - `StoragePort` interface — unchanged (same methods: `store`, `retrieve`, `delete`, `signedUrl`).
- **Design notes:** `adapter/auth/FirebaseAuthAdapter.java` is NOT moved in this task — it moves in Story 02 when the `auth` bounded context is built. The `adapter/` directory will therefore have only `adapter/auth/` remaining after this task. `PasswordResetService` (old, still alive during Story 01) injects `EmailService` by concrete type — this must be updated to `JavaMailEmailService`. This is a temporary fix; in Story 02 `PasswordResetService` is deleted entirely and replaced by orchestrators that inject `EmailNotificationPort`.

---

## Implementation Steps

1. Create directories:
   - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/out/email/`
   - `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/out/storage/`
2. Create `JavaMailEmailService.java` in the email directory:
   - Copy body from `email/EmailService.java`
   - Change `package` to `cl.gradeops.ai.api.shared.infrastructure.adapter.out.email`
   - Change `class EmailService` → `class JavaMailEmailService`
   - Change import `cl.gradeops.ai.api.config.GradeOpsEmailProperties` → `cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsEmailProperties` (already moved in task-06)
3. Delete `src/main/java/cl/gradeops/ai/api/email/EmailService.java`.
4. Create `StoragePort.java` in the storage directory:
   - Copy from `port/StoragePort.java`, change package only.
5. Create `GcsStorageAdapter.java` in the storage directory:
   - Copy from `adapter/storage/GcsStorageAdapter.java`, change package to new storage package.
   - Update import: `cl.gradeops.ai.api.port.StoragePort` → `cl.gradeops.ai.api.shared.infrastructure.adapter.out.storage.StoragePort`
6. Create `R2StorageAdapter.java` in the storage directory:
   - Copy from `adapter/storage/R2StorageAdapter.java`, change package.
   - Update import: `cl.gradeops.ai.api.port.StoragePort` → `cl.gradeops.ai.api.shared.infrastructure.adapter.out.storage.StoragePort`
7. Delete `src/main/java/cl/gradeops/ai/api/port/StoragePort.java`.
8. Delete `src/main/java/cl/gradeops/ai/api/adapter/storage/GcsStorageAdapter.java`.
9. Delete `src/main/java/cl/gradeops/ai/api/adapter/storage/R2StorageAdapter.java`.
10. In `src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java`:
    - Change import: `cl.gradeops.ai.api.email.EmailService` → `cl.gradeops.ai.api.shared.infrastructure.adapter.out.email.JavaMailEmailService`
    - Change field type and constructor param from `EmailService` to `JavaMailEmailService`.
11. In `src/test/java/cl/gradeops/ai/api/email/EmailServiceTest.java`:
    - Change import: `cl.gradeops.ai.api.email.EmailService` → `cl.gradeops.ai.api.shared.infrastructure.adapter.out.email.JavaMailEmailService` (note: test file path does not change — test package can differ from main package)
    - Change all references to `EmailService` → `JavaMailEmailService`
12. In `src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java`:
    - Change import: `cl.gradeops.ai.api.email.EmailService` → `cl.gradeops.ai.api.shared.infrastructure.adapter.out.email.JavaMailEmailService`
    - Change mock type declaration from `EmailService` to `JavaMailEmailService`.
13. Run `./mvnw compile -q` to confirm clean build.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | No remaining references to `EmailService` class | `grep -rn "import cl.gradeops.ai.api.email\." src/` returns 0 |
| 2 | No remaining references to old `port.StoragePort` | `grep -rn "import cl.gradeops.ai.api.port\.StoragePort" src/` returns 0 |
| 3 | No remaining references to old `adapter.storage` | `grep -rn "import cl.gradeops.ai.api.adapter\.storage\." src/` returns 0 |
| 4 | `adapter/` has only `auth/` remaining | `find src/main/java/cl/gradeops/ai/api/adapter -name "*.java"` shows only `FirebaseAuthAdapter.java` |
| 5 | `port/` has only `AuthPort` and `TeacherIdentity` | `find src/main/java/cl/gradeops/ai/api/port -name "*.java"` shows exactly 2 files |
| 6 | Clean build | `./mvnw compile -q` exits 0 |

---

## Done Criteria

- [ ] `shared/infrastructure/adapter/out/email/JavaMailEmailService.java` exists with `sendPasswordReset()` method
- [ ] `shared/infrastructure/adapter/out/storage/StoragePort.java`, `GcsStorageAdapter.java`, `R2StorageAdapter.java` all exist in the storage package
- [ ] `email/EmailService.java`, `port/StoragePort.java`, `adapter/storage/GcsStorageAdapter.java`, `adapter/storage/R2StorageAdapter.java` all deleted
- [ ] `adapter/` directory contains only `adapter/auth/FirebaseAuthAdapter.java`
- [ ] `port/` directory contains only `AuthPort.java` and `TeacherIdentity.java`
- [ ] `PasswordResetService.java` injects `JavaMailEmailService` (field type updated)
- [ ] `EmailServiceTest.java` and `PasswordResetServiceTest.java` reference `JavaMailEmailService`
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-01-shared-kernel.md)
