# ⚛️ TASK 04 — Create auth application port interfaces (6 port.in + 2 port.out)

> **Status:** DONE
> **Workflow:** IMPLEMENT
> **Depends On:** task-03
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Create the 6 inbound use-case port interfaces in `auth.application.port.in` and 2 new outbound port interfaces (`PasswordResetCodeRepositoryPort`, `EmailNotificationPort`) in `auth.application.port.out`. These are the stable contracts that handlers (task-05, 06), orchestrators (task-07), and controllers (task-11) depend on.

---

## Technical Design

- **Approach:** All port interfaces are minimal: one method each (or two for closely related operations). They reference command/result types from task-03. `AuthPort` already exists in `auth.application.port.out` from task-01. `PasswordResetCodeRepositoryPort` models persistence operations on `PasswordResetCode` aggregates (not JPA entities). `EmailNotificationPort` receives the raw reset code — the adapter (task-10) is responsible for building the reset URL.
- **Affected files / components — port.in:**
  - `src/main/java/cl/gradeops/ai/api/auth/application/port/in/RegisterUseCase.java`
  - `src/main/java/cl/gradeops/ai/api/auth/application/port/in/SignOutUseCase.java`
  - `src/main/java/cl/gradeops/ai/api/auth/application/port/in/RevokeRefreshTokensUseCase.java`
  - `src/main/java/cl/gradeops/ai/api/auth/application/port/in/IssuePasswordResetCodeUseCase.java`
  - `src/main/java/cl/gradeops/ai/api/auth/application/port/in/SendPasswordResetEmailUseCase.java`
  - `src/main/java/cl/gradeops/ai/api/auth/application/port/in/ResetPasswordUseCase.java`
- **Affected files / components — port.out (new):**
  - `src/main/java/cl/gradeops/ai/api/auth/application/port/out/PasswordResetCodeRepositoryPort.java`
  - `src/main/java/cl/gradeops/ai/api/auth/application/port/out/EmailNotificationPort.java`
- **Interfaces / contracts:**
  - `RegisterUseCase.execute(RegisterCommand) → RegisterResult`
  - `SignOutUseCase.execute(String uid) → void`
  - `RevokeRefreshTokensUseCase.execute(String uid) → void`
  - `IssuePasswordResetCodeUseCase.execute(IssuePasswordResetCodeCommand) → IssuePasswordResetCodeResult`
  - `SendPasswordResetEmailUseCase.execute(SendPasswordResetEmailCommand) → void`
  - `ResetPasswordUseCase.execute(ResetPasswordCommand) → void`
  - `PasswordResetCodeRepositoryPort.save(PasswordResetCode) → void` + `findByRawCode(String) → Optional<PasswordResetCode>` + `deleteByTeacherUid(String) → void`
  - `EmailNotificationPort.sendPasswordResetEmail(String toEmail, String firstName, String rawCode) → void`
- **Design notes:** Port interfaces have no annotations. They import only types from `auth.application.command`, `auth.application.result`, `auth.domain.model` (PasswordResetCode), and `java.*`. `PasswordResetCodeRepositoryPort` returns domain aggregates (`PasswordResetCode`), not JPA entities.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/auth/application/port/in/` (port.out already exists from task-01).
2. Create `RegisterUseCase.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.port.in;
   import cl.gradeops.ai.api.auth.application.command.RegisterCommand;
   import cl.gradeops.ai.api.auth.application.result.RegisterResult;
   public interface RegisterUseCase {
       RegisterResult execute(RegisterCommand command);
   }
   ```
3. Create `SignOutUseCase.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.port.in;
   public interface SignOutUseCase {
       void execute(String uid);
   }
   ```
4. Create `RevokeRefreshTokensUseCase.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.port.in;
   public interface RevokeRefreshTokensUseCase {
       void execute(String uid);
   }
   ```
5. Create `IssuePasswordResetCodeUseCase.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.port.in;
   import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
   import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;
   public interface IssuePasswordResetCodeUseCase {
       IssuePasswordResetCodeResult execute(IssuePasswordResetCodeCommand command);
   }
   ```
6. Create `SendPasswordResetEmailUseCase.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.port.in;
   import cl.gradeops.ai.api.auth.application.command.SendPasswordResetEmailCommand;
   public interface SendPasswordResetEmailUseCase {
       void execute(SendPasswordResetEmailCommand command);
   }
   ```
7. Create `ResetPasswordUseCase.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.port.in;
   import cl.gradeops.ai.api.auth.application.command.ResetPasswordCommand;
   public interface ResetPasswordUseCase {
       void execute(ResetPasswordCommand command);
   }
   ```
8. Create `PasswordResetCodeRepositoryPort.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.port.out;
   import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
   import java.util.Optional;
   public interface PasswordResetCodeRepositoryPort {
       void save(PasswordResetCode code);
       Optional<PasswordResetCode> findByRawCode(String rawCode);
       void deleteByTeacherUid(String teacherUid);
   }
   ```
9. Create `EmailNotificationPort.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.port.out;
   public interface EmailNotificationPort {
       void sendPasswordResetEmail(String toEmail, String firstName, String rawCode);
   }
   ```
10. Run `./mvnw compile -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | All 8 interface files compile | `./mvnw compile -q` exits 0 |
| 2 | `PasswordResetCodeRepositoryPort` returns `PasswordResetCode` domain type (not entity) | Read the file: `Optional<PasswordResetCode>` as return type |
| 3 | No annotations on port interfaces | `grep -r "@Service\|@Component\|@Repository" src/main/java/cl/gradeops/ai/api/auth/application/port/` returns 0 |

---

## Done Criteria

- [ ] `auth/application/port/in/` contains all 6 use case interfaces
- [ ] `auth/application/port/out/` contains `AuthPort` (from task-01) + `PasswordResetCodeRepositoryPort` + `EmailNotificationPort`
- [ ] `PasswordResetCodeRepositoryPort.findByRawCode()` returns `Optional<PasswordResetCode>` (domain type)
- [ ] `EmailNotificationPort.sendPasswordResetEmail(String, String, String)` takes raw code, not reset URL
- [ ] No Spring/JPA/Lombok annotations on any port interface
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-02-auth-bounded-context.md)
