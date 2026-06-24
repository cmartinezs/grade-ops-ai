# ⚛️ TASK 07 — Create SendPasswordResetEmailOrchestrator + ResetPasswordOrchestrator + unit tests

> **Status:** DONE
> **Workflow:** IMPLEMENT
> **Depends On:** task-01, task-03, task-04
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Create `SendPasswordResetEmailOrchestrator` and `ResetPasswordOrchestrator` — the two orchestrators that coordinate 3–4 collaborators each, implement the corresponding port.in use case interfaces, and are `@Component @RequiredArgsConstructor` (NOT `@Transactional`). Write unit tests for both.

---

## Technical Design

- **Approach:** Orchestrators coordinate atomic use cases (port.in interfaces) and output ports — they never contain business logic themselves. Both orchestrators temporarily inject `domain.teacher.TeacherRepository` (old JPA interface) to access teacher data because `TeacherRepositoryPort` does not yet exist — Story 03 will replace these injections. The reset link URL is the adapter's responsibility: orchestrators pass the raw code to `EmailNotificationPort` and the adapter builds the URL. TTL for code generation: `SendPasswordResetEmailOrchestrator` receives `ttlMinutes` as a constructor parameter (a plain `int`) — the value is supplied via `AuthConfig` which reads it from `GradeOpsEmailProperties`. This way the orchestrator has no Spring or config import at all. Neither orchestrator carries any Spring stereotype annotation (`@Component`, `@Service`); both are declared as `@Bean` in `AuthConfig` (created in task-12).
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/application/orchestrator/SendPasswordResetEmailOrchestrator.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/application/orchestrator/ResetPasswordOrchestrator.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/application/orchestrator/SendPasswordResetEmailOrchestratorTest.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/application/orchestrator/ResetPasswordOrchestratorTest.java` ← NEW
- **Interfaces / contracts:**
  - `SendPasswordResetEmailOrchestrator` implements `SendPasswordResetEmailUseCase`; receives `ttlMinutes` as a plain `int` constructor parameter (no config imports).
  - `ResetPasswordOrchestrator` implements `ResetPasswordUseCase`.
- **Design notes on ResetPasswordOrchestrator:**
  - The orchestrator checks `passwordRepeat` match before any port call (early validation).
  - Calls `PasswordResetCodeRepositoryPort.findByRawCode(command.rawCode())`.
  - Throws `InvalidResetCodeException` if not found, expired, or used.
  - Then loads teacher via `TeacherRepository.findById(code.getTeacherUid())` (temporary — Story 03 updates this).
  - Validates `teacher.getEmail().equalsIgnoreCase(command.email())`.
  - Calls `AuthPort.updatePassword`, then `RevokeRefreshTokensUseCase.execute`, then `code.markUsed()` + `PasswordResetCodeRepositoryPort.save(code)`.
  - NOT `@Transactional` — orchestrators don't own transactions. The `save(code)` call in the repository adapter IS transactional via its own `@Transactional`.
- **Design notes on SendPasswordResetEmailOrchestrator:**
  - Loads teacher by email via `TeacherRepository.findByEmail(command.email())`. Returns silently if not found.
  - Returns silently if teacher is not `"EMAIL_PASSWORD"` provider.
  - Calls `IssuePasswordResetCodeUseCase.execute(...)` with `ttlMinutes` from `GradeOpsEmailProperties`.
  - Calls `EmailNotificationPort.sendPasswordResetEmail(email, firstName, result.rawCode())`.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/auth/application/orchestrator/`.
2. Create `SendPasswordResetEmailOrchestrator.java`:
   - NO `@Component` — declared as `@Bean` in `AuthConfig`.
   - `@RequiredArgsConstructor`, implements `SendPasswordResetEmailUseCase`.
   - Fields: `TeacherRepository teacherRepository` (old JPA repo), `IssuePasswordResetCodeUseCase issuePasswordResetCodeUseCase`, `EmailNotificationPort emailNotificationPort`, `int ttlMinutes`.
   - `ttlMinutes` is a plain `int` constructor field; `AuthConfig` supplies the value from `emailProperties.getResetPassword().getTtlMinutes()`.
   - No import of `GradeOpsEmailProperties` inside the orchestrator class.
3. Create `ResetPasswordOrchestrator.java`:
   - NO `@Component` — declared as `@Bean` in `AuthConfig`.
   - `@RequiredArgsConstructor`, implements `ResetPasswordUseCase`.
   - Fields: `PasswordResetCodeRepositoryPort codeRepository`, `AuthPort authPort`, `RevokeRefreshTokensUseCase revokeRefreshTokensUseCase`, `TeacherRepository teacherRepository` (old JPA repo).
   - Throws `InvalidResetCodeException` (from `auth.domain.exception`) for invalid/expired/used codes.
   - Throws `ResponseStatusException(422, "RESET_CODE_EMAIL_MISMATCH")` for email mismatch (same HTTP semantics as old service).
4. Create test directory `src/test/java/cl/gradeops/ai/api/auth/application/orchestrator/`.
5. Create `SendPasswordResetEmailOrchestratorTest.java`:
   - Construct orchestrator directly: `new SendPasswordResetEmailOrchestrator(mockTeacherRepo, mockIssueUseCase, mockEmailPort, 30)` — no Spring context.
   - Mock `TeacherRepository`, `IssuePasswordResetCodeUseCase`, `EmailNotificationPort`.
   - Test: unknown email → no interactions with issue or email port.
   - Test: GOOGLE provider → no interactions with issue or email port.
   - Test: EMAIL_PASSWORD teacher → `issuePasswordResetCodeUseCase.execute()` and `emailNotificationPort.sendPasswordResetEmail()` called with correct args.
6. Create `ResetPasswordOrchestratorTest.java`:
   - Mock all 4 dependencies.
   - Test: code not found → `InvalidResetCodeException`.
   - Test: expired code → `InvalidResetCodeException`.
   - Test: used code → `InvalidResetCodeException`.
   - Test: email mismatch → `ResponseStatusException` 422.
   - Test: happy path → `authPort.updatePassword`, `revokeRefreshTokensUseCase.execute`, `code.markUsed()`, `codeRepository.save(code)` all called.
7. Run `./mvnw test -Dtest=SendPasswordResetEmailOrchestratorTest,ResetPasswordOrchestratorTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Unknown email → silent return, no downstream calls | `verifyNoInteractions(issuePasswordResetCodeUseCase, emailNotificationPort)` |
| 2 | GOOGLE teacher → silent return | Same verification |
| 3 | EMAIL_PASSWORD teacher → `IssuePasswordResetCodeUseCase` + `EmailNotificationPort` called | Verify in order; capture args |
| 4 | Expired code → `InvalidResetCodeException` | `assertThatThrownBy(...).isInstanceOf(InvalidResetCodeException.class)` |
| 5 | Happy path reset → password updated + tokens revoked + code marked used | Verify 4 calls in order |

---

## Done Criteria

- [ ] `SendPasswordResetEmailOrchestrator` and `ResetPasswordOrchestrator` exist in `auth/application/orchestrator/`
- [ ] Both are `@RequiredArgsConstructor` with NO `@Component`/`@Service` — declared as `@Bean` in `AuthConfig`
- [ ] Neither orchestrator is `@Transactional` and neither imports Spring or config classes
- [ ] `SendPasswordResetEmailOrchestrator` receives `int ttlMinutes` as a constructor field (no `GradeOpsEmailProperties` import)
- [ ] Neither orchestrator contains business logic — only coordination of collaborator calls
- [ ] `ResetPasswordOrchestrator` throws `InvalidResetCodeException` for invalid/expired/used codes
- [ ] `SendPasswordResetEmailOrchestrator` does NOT build the reset URL — passes `rawCode` to `EmailNotificationPort`
- [ ] Both test classes pass all cases
- [ ] `./mvnw test -Dtest=SendPasswordResetEmailOrchestratorTest,ResetPasswordOrchestratorTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | ~~`SendPasswordResetEmailOrchestrator` imports `GradeOpsEmailProperties` (infra config) from application layer~~ — resolved: `ttlMinutes` is now a plain `int` constructor field; `AuthConfig` reads the property and passes it at wire-up time | — | CLOSED |

---

> [← story file](../story-02-auth-bounded-context.md)
