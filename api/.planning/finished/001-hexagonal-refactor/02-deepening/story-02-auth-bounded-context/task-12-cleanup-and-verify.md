# ⚛️ TASK 12 — Delete old auth flat-package files and verify full test suite

> **Status:** DONE
> **Workflow:** CLEANUP
> **Depends On:** task-05, task-06, task-07, task-08, task-09, task-10, task-11
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Delete all remaining old flat-package source files in `auth/` that have been superseded by the hexagonal structure created in tasks 01–11. Delete the superseded test files. Run `./mvnw test` and confirm it exits 0. Story 02 is DONE.

---

## Technical Design

- **Approach:** By this task every class in the old `auth/` flat package has a replacement in the new hexagonal layout. Each deletion must be verified against the tasks that introduced the replacement before proceeding. The rule: delete only what was replaced, in the order listed below. After all deletions, run the full test suite to confirm no dangling references.
- **Files to delete (source):**
  - `src/main/java/cl/gradeops/ai/api/auth/AuthService.java` — replaced by `RegisterHandler`, `SignOutHandler`, `RevokeRefreshTokensHandler` (task-05)
  - `src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java` — replaced by `IssuePasswordResetCodeHandler` (task-06) + `SendPasswordResetEmailOrchestrator`, `ResetPasswordOrchestrator` (task-07)
  - `src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeEntity.java` — replaced by `PasswordResetCodeJpaEntity` (task-09)
  - `src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeRepository.java` — replaced by `PasswordResetCodeJpaRepository` (task-09)
  - `src/main/java/cl/gradeops/ai/api/auth/RegisterResult.java` — replaced by `auth.application.result.RegisterResult` (task-03)
  - `src/main/java/cl/gradeops/ai/api/auth/AuthController.java` — replaced by `auth.infrastructure.adapter.in.web.AuthController` (task-11)
  - `src/main/java/cl/gradeops/ai/api/auth/PasswordResetController.java` — replaced by `auth.infrastructure.adapter.in.web.AuthController` (task-11)
- **Files to delete (tests):**
  - `src/test/java/cl/gradeops/ai/api/auth/PasswordResetCodeEntityTest.java` — superseded by `PasswordResetCodeTest` (task-02)
  - `src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java` — superseded by orchestrator tests (task-07)
  - `src/test/java/cl/gradeops/ai/api/auth/AuthControllerTest.java` — migrated in task-11 (if not already deleted there)
  - `src/test/java/cl/gradeops/ai/api/auth/PasswordResetControllerTest.java` — migrated in task-11 (if not already deleted there)
- **Compile check before deleting:** Run `./mvnw compile -q` before any deletion. If it fails, stop and investigate — a previous task was incomplete.
- **Design notes:**
  - After deletions, `src/main/java/cl/gradeops/ai/api/auth/` may still contain the `domain/`, `application/`, and `infrastructure/` subdirectories — do NOT delete those.
  - The `port/` package (old `AuthPort`, `TeacherIdentity`) was already deleted in task-01.
  - The `adapter/auth/` directory was already deleted in task-08.
  - `TeacherRepository` (in `domain/teacher/`) is NOT deleted here — it is still referenced by `RegisterHandler` and the orchestrators (temporary dependency until Story 03).

---

## Implementation Steps

1. Create `src/main/java/cl/gradeops/ai/api/auth/infrastructure/config/AuthConfig.java` (actual implementation — `TeacherRepositoryPort` used directly, pulled forward from Story 03):
   ```java
   @Configuration
   @RequiredArgsConstructor
   class AuthConfig {
       private final GradeOpsEmailProperties emailProperties;

       @Bean FirebaseAuthAdapter firebaseAuthAdapter(FirebaseAuth firebaseAuth) { ... }
       @Bean TeacherJpaRepositoryAdapter teacherJpaRepositoryAdapter(TeacherRepository repo) { ... }
       @Bean PasswordResetCodePersistenceMapper passwordResetCodePersistenceMapper() { ... }
       @Bean PasswordResetCodePersistenceAdapter passwordResetCodePersistenceAdapter(...) { ... }
       @Bean ThymeleafEmailNotificationAdapter thymeleafEmailNotificationAdapter(...) { ... }
       @Bean RevokeRefreshTokensHandler revokeRefreshTokensHandler(AuthPort authPort) { ... }
       @Bean SignOutHandler signOutHandler(RevokeRefreshTokensUseCase ...) { ... }
       @Bean RegisterHandler registerHandler(AuthPort authPort, TeacherRepositoryPort repo) { ... }
       @Bean IssuePasswordResetCodeHandler issuePasswordResetCodeHandler(PasswordResetCodeRepositoryPort ...) { ... }
       @Bean SendPasswordResetEmailOrchestrator sendPasswordResetEmailOrchestrator(TeacherRepositoryPort ...) { ... }
       @Bean ResetPasswordOrchestrator resetPasswordOrchestrator(PasswordResetCodeRepositoryPort ..., TeacherRepositoryPort ...) { ... }
   }
   ```
   11 `@Bean` methods total (includes `TeacherJpaRepositoryAdapter`). No `TeacherRepository` references outside `teacherJpaRepositoryAdapter` bean method.

2. Run `./mvnw compile -q` to confirm the codebase compiles cleanly before making any deletions.
3. Verify each replacement file exists before deleting the original:
   - `grep -r "class RegisterHandler" src/main/java` → expect 1 match.
   - `grep -r "class SendPasswordResetEmailOrchestrator" src/main/java` → expect 1 match.
   - `grep -r "class PasswordResetCodeJpaEntity" src/main/java` → expect 1 match.
   - `grep -r "class AuthController" src/main/java/cl/gradeops/ai/api/auth/infrastructure` → expect 1 match.
4. Delete source files:
   ```
   src/main/java/cl/gradeops/ai/api/auth/AuthService.java
   src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java
   src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeEntity.java
   src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeRepository.java
   src/main/java/cl/gradeops/ai/api/auth/RegisterResult.java
   src/main/java/cl/gradeops/ai/api/auth/AuthController.java
   src/main/java/cl/gradeops/ai/api/auth/PasswordResetController.java
   ```
5. Delete test files (skip any already deleted in task-11):
   ```
   src/test/java/cl/gradeops/ai/api/auth/PasswordResetCodeEntityTest.java
   src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java
   src/test/java/cl/gradeops/ai/api/auth/AuthControllerTest.java        (if still exists)
   src/test/java/cl/gradeops/ai/api/auth/PasswordResetControllerTest.java (if still exists)
   ```
6. Run `./mvnw compile -q` again after all deletions to catch dangling references.
7. Fix any compile errors (likely a stale import pointing to a deleted class) before running tests.
8. Run `./mvnw test` (full suite) and confirm exit code 0.
9. Verify the `auth/` flat-package directory is clean:
   ```
   find src/main/java/cl/gradeops/ai/api/auth -maxdepth 1 -name "*.java" | wc -l
   ```
   Result should be 0 (all top-level `.java` files removed; only subdirectories remain).

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `AuthConfig` exists and declares all auth beans | Read file: 10+ `@Bean` methods present |
| 2 | No top-level `.java` files remain in `auth/` | `find src/main/java/cl/gradeops/ai/api/auth -maxdepth 1 -name "*.java"` returns 0 |
| 3 | Compile succeeds after deletions | `./mvnw compile -q` exits 0 |
| 4 | Full test suite passes | `./mvnw test` exits 0 |
| 5 | Old test files are absent | `find src/test/java/cl/gradeops/ai/api/auth -maxdepth 1 -name "*.java"` returns 0 |
| 6 | `TeacherRepository` is NOT deleted (still needed in Story 03) | `find src/main/java -name "TeacherRepository.java"` returns 1 result |

---

## Done Criteria

- [x] `auth/infrastructure/config/AuthConfig.java` created with `@Bean` for all 11 auth beans (adapters including `TeacherJpaRepositoryAdapter`, handlers, orchestrators, mapper)
- [x] All 7 listed source files in `auth/` flat package are deleted
- [x] All 4 listed superseded test files are deleted (or were already deleted in task-11)
- [x] `src/main/java/cl/gradeops/ai/api/auth/` contains only subdirectories (`domain/`, `application/`, `infrastructure/`)
- [x] `./mvnw compile -q` exits 0 after deletions
- [x] `./mvnw test` exits 0 — 72/72 pass
- [x] `TeacherRepository.java` still exists (wrapped by `TeacherJpaRepositoryAdapter` — application layer no longer imports it)
- [x] Story 02 status is `DONE` in `story-02-auth-bounded-context.md`

---

> [← story file](../story-02-auth-bounded-context.md)
