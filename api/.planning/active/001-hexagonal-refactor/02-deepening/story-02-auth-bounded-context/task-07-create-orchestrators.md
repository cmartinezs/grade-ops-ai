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

- **Approach:** Orchestrators coordinate atomic use cases (port.in interfaces) and output ports — they never contain business logic themselves. Both orchestrators inject `TeacherRepositoryPort` (pulled forward from Story 03 as part of a post-review fix — see story Inconsistency #4). The reset link URL is the adapter's responsibility: orchestrators pass the raw code to `EmailNotificationPort` and the adapter builds the URL. TTL for code generation: `SendPasswordResetEmailOrchestrator` receives `ttlMinutes` as a constructor parameter (a plain `int`) — the value is supplied via `AuthConfig` which reads it from `GradeOpsEmailProperties`. This way the orchestrator has no Spring or config import at all. Neither orchestrator carries any Spring stereotype annotation (`@Component`, `@Service`); both are declared as `@Bean` in `AuthConfig`.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/application/orchestrator/SendPasswordResetEmailOrchestrator.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/application/orchestrator/ResetPasswordOrchestrator.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/application/orchestrator/SendPasswordResetEmailOrchestratorTest.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/application/orchestrator/ResetPasswordOrchestratorTest.java` ← NEW
- **Interfaces / contracts:**
  - `SendPasswordResetEmailOrchestrator` implements `SendPasswordResetEmailUseCase`; receives `ttlMinutes` as a plain `int` constructor parameter (no config imports).
  - `ResetPasswordOrchestrator` implements `ResetPasswordUseCase`.
- **Design notes on ResetPasswordOrchestrator:**
  - IS `@Transactional` — owns the password-update + code-save unit of work (post-review correction; original design said NOT transactional).
  - The orchestrator checks `passwordRepeat` match first, throwing `PasswordMismatchException` (domain exception — not `ResponseStatusException`).
  - Calls `PasswordResetCodeRepositoryPort.findByRawCode(command.rawCode())`.
  - Throws `InvalidResetCodeException` if not found, expired, or used.
  - Then loads teacher via `TeacherRepositoryPort.findById(code.getTeacherUid())`.
  - Email mismatch throws `ResetCodeEmailMismatchException` (domain exception).
  - Calls `AuthPort.updatePassword`, then `RevokeRefreshTokensUseCase.execute`, then `code.markUsed()` + `PasswordResetCodeRepositoryPort.save(code)`.
  - Calls `code.pullDomainEvents()` after save (to drain the event list and avoid silent event loss).
- **Design notes on SendPasswordResetEmailOrchestrator:**
  - Loads teacher by email via `TeacherRepositoryPort.findByEmail(command.email())`. Returns silently if not found.
  - Parses provider via `SignInProvider.valueOf(teacher.getProvider())`; returns silently for unknown or non-`EMAIL_PASSWORD` providers.
  - Passes `provider` field in `IssuePasswordResetCodeCommand` (required by handler's provider guard).
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

- [x] `SendPasswordResetEmailOrchestrator` and `ResetPasswordOrchestrator` exist in `auth/application/orchestrator/`
- [x] Both are `@RequiredArgsConstructor` with NO `@Component`/`@Service` — declared as `@Bean` in `AuthConfig`
- [x] `SendPasswordResetEmailOrchestrator` is NOT `@Transactional` and imports no Spring/config classes; `ResetPasswordOrchestrator` IS `@Transactional` (owns password-update + code-save unit of work)
- [x] `SendPasswordResetEmailOrchestrator` receives `int ttlMinutes` as a constructor field (no `GradeOpsEmailProperties` import)
- [x] Both orchestrators inject `TeacherRepositoryPort` (not `domain.teacher.TeacherRepository`)
- [x] `ResetPasswordOrchestrator` throws domain exceptions: `PasswordMismatchException` for password mismatch, `InvalidResetCodeException` for invalid/expired/used codes, `ResetCodeEmailMismatchException` for email mismatch
- [x] `ResetPasswordOrchestrator` calls `code.pullDomainEvents()` after `codeRepository.save(code)` to drain domain events
- [x] `SendPasswordResetEmailOrchestrator` passes `provider` field in `IssuePasswordResetCodeCommand`
- [x] `SendPasswordResetEmailOrchestrator` does NOT build the reset URL — passes `rawCode` to `EmailNotificationPort`
- [x] Both test classes pass all cases; methods follow `should...When...` convention
- [x] `./mvnw test -Dtest=SendPasswordResetEmailOrchestratorTest,ResetPasswordOrchestratorTest -q` exits 0

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | ~~`SendPasswordResetEmailOrchestrator` imports `GradeOpsEmailProperties` (infra config) from application layer~~ — resolved: `ttlMinutes` is now a plain `int` constructor field; `AuthConfig` reads the property and passes it at wire-up time | — | CLOSED |

---

> [← story file](../story-02-auth-bounded-context.md)
