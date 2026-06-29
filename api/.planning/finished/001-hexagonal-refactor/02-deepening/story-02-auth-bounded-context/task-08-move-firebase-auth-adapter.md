# ⚛️ TASK 08 — Move FirebaseAuthAdapter to auth.infrastructure.adapter.out.firebase

> **Status:** DONE
> **Workflow:** REFACTOR
> **Depends On:** task-01
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Relocate `FirebaseAuthAdapter` from `adapter.auth` to `auth.infrastructure.adapter.out.firebase`. Update its imports to reference `AuthPort` and `TeacherIdentity` from their new locations (task-01). Migrate `FirebaseAuthAdapterTest` to the new package. Delete `adapter/auth/` directory.

---

## Technical Design

- **Approach:** Pure move — no logic changes. The adapter already has all the right behaviour. Its imports need updating to point to `auth.application.port.out.AuthPort` and `auth.domain.model.TeacherIdentity` (both moved in task-01). Remove the `@Component` annotation from the old source — `FirebaseAuthAdapter` is declared as `@Bean` in `AuthConfig` (created in task-12), not via component scan. `FirebaseAuthAdapterTest` uses `@InjectMocks FirebaseAuthAdapter` — its package declaration must be updated; the test body is unchanged.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/firebase/FirebaseAuthAdapter.java` ← NEW (moved + imports updated)
  - `src/main/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapter.java` ← DELETE
  - `src/test/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/firebase/FirebaseAuthAdapterTest.java` ← NEW (moved + package updated)
  - `src/test/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapterTest.java` ← DELETE
- **Interfaces / contracts:** `FirebaseAuthAdapter` still implements `auth.application.port.out.AuthPort`. Public API unchanged.
- **Design notes:** After this task `adapter/` directory at the root level has no remaining packages (storage was removed in Story 01, auth is removed here). Confirm with `find src/main/java/cl/gradeops/ai/api/adapter -type f` returns 0 results.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/firebase/`.
2. Create `FirebaseAuthAdapter.java` in the new location:
   - Copy body from `adapter/auth/FirebaseAuthAdapter.java`.
   - Change package to `cl.gradeops.ai.api.auth.infrastructure.adapter.out.firebase`.
   - Update import: `cl.gradeops.ai.api.port.AuthPort` → `cl.gradeops.ai.api.auth.application.port.out.AuthPort`.
   - Update import: `cl.gradeops.ai.api.port.TeacherIdentity` → `cl.gradeops.ai.api.auth.domain.model.TeacherIdentity`.
   - Remove `@Component` annotation and its import — declared as `@Bean` in `AuthConfig` instead.
3. Delete `src/main/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapter.java`.
4. Create test directory `src/test/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/firebase/`.
5. Create `FirebaseAuthAdapterTest.java` in the new test location:
   - Copy body from `test/adapter/auth/FirebaseAuthAdapterTest.java`.
   - Change package to `cl.gradeops.ai.api.auth.infrastructure.adapter.out.firebase`.
   - Update import: `cl.gradeops.ai.api.port.TeacherIdentity` → `cl.gradeops.ai.api.auth.domain.model.TeacherIdentity`.
   - All test bodies and assertions unchanged.
6. Delete `src/test/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapterTest.java`.
7. Run `./mvnw test -Dtest=FirebaseAuthAdapterTest -q` to confirm migrated test passes.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `adapter/auth/` is fully deleted | `find src/main/java/cl/gradeops/ai/api/adapter -type f` returns 0 results |
| 2 | New `FirebaseAuthAdapter` is in the correct package | First line of new file has correct package declaration |
| 3 | All 7 existing `FirebaseAuthAdapterTest` cases pass at new location | `./mvnw test -Dtest=FirebaseAuthAdapterTest -q` exits 0 |
| 4 | `FirebaseAuthAdapter` implements `auth.application.port.out.AuthPort` (not old `port.AuthPort`) | Read the file — `implements AuthPort` with correct import |

---

## Done Criteria

- [ ] `auth/infrastructure/adapter/out/firebase/FirebaseAuthAdapter.java` exists with correct package and updated imports; NO `@Component` annotation
- [ ] `auth/infrastructure/adapter/out/firebase/FirebaseAuthAdapterTest.java` exists with correct package
- [ ] `adapter/auth/FirebaseAuthAdapter.java` deleted; `adapter/` root-level directory is fully empty
- [ ] `./mvnw test -Dtest=FirebaseAuthAdapterTest -q` exits 0 (all 7 test cases pass)
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-02-auth-bounded-context.md)
