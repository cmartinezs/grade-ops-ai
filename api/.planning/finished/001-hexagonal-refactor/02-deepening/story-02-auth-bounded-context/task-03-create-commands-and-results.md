# ⚛️ TASK 03 — Create auth.application.command and auth.application.result records

> **Status:** DONE
> **Workflow:** IMPLEMENT
> **Depends On:** —
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Create 4 command records and 2 result records in `auth.application.command` and `auth.application.result`. These are the input/output data contracts for all auth use cases and must exist before the port interfaces (task-04) can be compiled.

---

## Technical Design

- **Approach:** Commands are `record` types with `@Builder` (Lombok) and null-validation in the compact constructor, per guidelines. Results are also `record` types with `@Builder`. `RegisterResult` already exists as a package-private record in `auth/RegisterResult.java` — it is recreated as a public record here and deleted in task-12. Commands use Bean Validation annotations only on fields, not for business validation (that belongs in domain).
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/application/command/RegisterCommand.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/application/command/IssuePasswordResetCodeCommand.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/application/command/SendPasswordResetEmailCommand.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/application/command/ResetPasswordCommand.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/application/result/RegisterResult.java` ← NEW (public version of old package-private `auth/RegisterResult.java`)
  - `src/main/java/cl/gradeops/ai/api/auth/application/result/IssuePasswordResetCodeResult.java` ← NEW
- **Interfaces / contracts:**
  - `RegisterCommand(String idToken, String firstName, String lastName)` — `idToken` must be non-blank (compact constructor).
  - `IssuePasswordResetCodeCommand(String teacherUid, int ttlMinutes)` — both fields required.
  - `SendPasswordResetEmailCommand(String email)` — `email` must be non-blank.
  - `ResetPasswordCommand(String rawCode, String email, String password, String passwordRepeat)` — all non-blank; password ≥ 6 chars validated by handler/orchestrator, not command.
  - `RegisterResult(String uid, boolean created)` — same shape as old package-private record, now public.
  - `IssuePasswordResetCodeResult(String rawCode, java.time.Instant expiresAt)`.
- **Design notes:** Lombok `@Builder` is the only Lombok annotation used. No `@Data`. Spring/validation annotations (`@NotBlank`, `@Email`) are NOT used on command fields — commands are application-layer data carriers, not HTTP request objects. Null checks in compact constructors use `Objects.requireNonNull`.

---

## Implementation Steps

1. Create directories `src/main/java/cl/gradeops/ai/api/auth/application/command/` and `src/main/java/cl/gradeops/ai/api/auth/application/result/`.
2. Create `RegisterCommand.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.command;

   import lombok.Builder;
   import java.util.Objects;

   @Builder
   public record RegisterCommand(String idToken, String firstName, String lastName) {
       public RegisterCommand {
           Objects.requireNonNull(idToken, "idToken must not be null");
       }
   }
   ```
3. Create `IssuePasswordResetCodeCommand.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.command;

   import lombok.Builder;
   import java.util.Objects;

   @Builder
   public record IssuePasswordResetCodeCommand(String teacherUid, int ttlMinutes) {
       public IssuePasswordResetCodeCommand {
           Objects.requireNonNull(teacherUid, "teacherUid must not be null");
       }
   }
   ```
4. Create `SendPasswordResetEmailCommand.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.command;

   import lombok.Builder;
   import java.util.Objects;

   @Builder
   public record SendPasswordResetEmailCommand(String email) {
       public SendPasswordResetEmailCommand {
           Objects.requireNonNull(email, "email must not be null");
       }
   }
   ```
5. Create `ResetPasswordCommand.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.command;

   import lombok.Builder;
   import java.util.Objects;

   @Builder
   public record ResetPasswordCommand(String rawCode, String email,
                                       String password, String passwordRepeat) {
       public ResetPasswordCommand {
           Objects.requireNonNull(rawCode, "rawCode must not be null");
           Objects.requireNonNull(email, "email must not be null");
           Objects.requireNonNull(password, "password must not be null");
           Objects.requireNonNull(passwordRepeat, "passwordRepeat must not be null");
       }
   }
   ```
6. Create `RegisterResult.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.result;

   import lombok.Builder;

   @Builder
   public record RegisterResult(String uid, boolean created) {}
   ```
7. Create `IssuePasswordResetCodeResult.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.result;

   import lombok.Builder;
   import java.time.Instant;

   @Builder
   public record IssuePasswordResetCodeResult(String rawCode, Instant expiresAt) {}
   ```
8. Run `./mvnw compile -q` to confirm all 6 files compile.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `RegisterCommand` rejects null `idToken` | `assertThatThrownBy(() -> new RegisterCommand(null, "a", "b")).isInstanceOf(NullPointerException.class)` |
| 2 | All 6 records compile with `@Builder` | `./mvnw compile -q` exits 0 |
| 3 | No Spring/JPA/validation annotations on command fields | `grep -r "@NotBlank\|@Email\|@Entity" src/main/java/cl/gradeops/ai/api/auth/application/` returns 0 |

---

## Done Criteria

- [ ] `auth/application/command/` contains `RegisterCommand`, `IssuePasswordResetCodeCommand`, `SendPasswordResetEmailCommand`, `ResetPasswordCommand`
- [ ] `auth/application/result/` contains `RegisterResult` (public), `IssuePasswordResetCodeResult`
- [ ] All 6 records have `@Builder` and use `Objects.requireNonNull` in compact constructors (except boolean/primitive fields)
- [ ] No `@Data`, no Spring/JPA/Bean Validation annotations on record fields
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-02-auth-bounded-context.md)
