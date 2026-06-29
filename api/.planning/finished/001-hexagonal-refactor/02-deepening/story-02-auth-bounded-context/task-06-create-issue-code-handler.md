# ⚛️ TASK 06 — Create IssuePasswordResetCodeHandler + unit test

> **Status:** DONE
> **Workflow:** IMPLEMENT
> **Depends On:** task-02, task-03, task-04
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Create `IssuePasswordResetCodeHandler` — the atomic use case that deletes any existing reset code for a teacher, creates a new `PasswordResetCode` aggregate via `issue()`, persists it, and returns the raw code and expiry. Write `IssuePasswordResetCodeHandlerTest`.

---

## Technical Design

- **Approach:** This handler encapsulates the "delete existing + issue new + save" sequence that previously lived inline in `PasswordResetService.sendResetEmail()`. Making it an atomic, reusable use case allows both `SendPasswordResetEmailOrchestrator` (task-07) and future provisioning flows (Story 03 `ProvisionTeacherHandler`) to invoke it without duplicating the logic. It injects `PasswordResetCodeRepositoryPort` — the domain port, not a JPA repository directly. The TTL comes from the command, not from config properties — the caller (orchestrator or handler) is responsible for resolving TTL from `GradeOpsEmailProperties`.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/application/usecase/IssuePasswordResetCodeHandler.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/application/usecase/IssuePasswordResetCodeHandlerTest.java` ← NEW
- **Interfaces / contracts:** `IssuePasswordResetCodeHandler` implements `IssuePasswordResetCodeUseCase`. Method: `execute(IssuePasswordResetCodeCommand) → IssuePasswordResetCodeResult`. Result contains `rawCode` (UUID string) and `expiresAt`.
- **Design notes:** `rawCode` is generated internally as a `RawCode` value object (`RawCode.generate()` wraps `UUID.randomUUID()`) — not passed in the command, since generation is the handler's responsibility. `expiresAt = Instant.now().plus(command.ttlMinutes(), ChronoUnit.MINUTES)`. `deleteByTeacherUid` is called before `save` to enforce the one-active-code-per-teacher invariant. The `@Transactional` annotation ensures delete + save are atomic. **Post-review addition:** `IssuePasswordResetCodeCommand` includes a `SignInProvider provider` field; the handler guards against non-`EMAIL_PASSWORD` providers and throws `IllegalArgumentException` before touching the repository.

---

## Implementation Steps

1. Create `IssuePasswordResetCodeHandler.java` in `auth/application/usecase/`:
   ```java
   package cl.gradeops.ai.api.auth.application.usecase;

   import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
   import cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase;
   import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
   import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;
   import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
   import lombok.RequiredArgsConstructor;
   import org.springframework.transaction.annotation.Transactional;

   import java.time.Instant;
   import java.time.temporal.ChronoUnit;
   import java.util.UUID;

   // NO @Service — declared as @Bean in AuthConfig (task-12)
   @RequiredArgsConstructor
   public class IssuePasswordResetCodeHandler implements IssuePasswordResetCodeUseCase {

       private final PasswordResetCodeRepositoryPort codeRepository;

       @Override
       @Transactional
       public IssuePasswordResetCodeResult execute(IssuePasswordResetCodeCommand command) {
           codeRepository.deleteByTeacherUid(command.teacherUid());

           String rawCode = UUID.randomUUID().toString();
           Instant expiresAt = Instant.now().plus(command.ttlMinutes(), ChronoUnit.MINUTES);
           PasswordResetCode code = PasswordResetCode.issue(command.teacherUid(), rawCode, expiresAt);
           codeRepository.save(code);

           return IssuePasswordResetCodeResult.builder()
                   .rawCode(rawCode)
                   .expiresAt(expiresAt)
                   .build();
       }
   }
   ```
2. Create `IssuePasswordResetCodeHandlerTest.java` in `src/test/java/cl/gradeops/ai/api/auth/application/usecase/`:
   - Mock `PasswordResetCodeRepositoryPort`.
   - Test: happy path — `deleteByTeacherUid` called, `save` called with `PasswordResetCode` whose `teacherUid` matches, result `rawCode` is a valid UUID string, `expiresAt` is in the future.
   - Test: `deleteByTeacherUid` is always called before `save` (verify call order with `InOrder`).
   - Test: returned `rawCode` is the same value persisted in the `PasswordResetCode` passed to `save` (capture with `ArgumentCaptor`).
3. Run `./mvnw test -Dtest=IssuePasswordResetCodeHandlerTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `deleteByTeacherUid` called before `save` | `InOrder` verification in Mockito |
| 2 | Saved `PasswordResetCode` has correct `teacherUid` and non-null `rawCode` | `ArgumentCaptor<PasswordResetCode>` + `assertThat(saved.getTeacherUid()).isEqualTo(command.teacherUid())` |
| 3 | Returned `rawCode` matches the value in the saved aggregate | `assertThat(result.rawCode()).isEqualTo(saved.getRawCode())` |
| 4 | `expiresAt` is approximately `now + ttlMinutes` | `assertThat(result.expiresAt()).isAfter(Instant.now())` |

---

## Done Criteria

- [x] `IssuePasswordResetCodeHandler` exists, implements `IssuePasswordResetCodeUseCase`, is `@RequiredArgsConstructor` with NO `@Service` — declared as `@Bean` in `AuthConfig`
- [x] Handler injects `PasswordResetCodeRepositoryPort` only (no JPA repo, no `GradeOpsEmailProperties`)
- [x] `IssuePasswordResetCodeCommand` includes `SignInProvider provider` field; handler throws `IllegalArgumentException` for non-`EMAIL_PASSWORD` before any repository interaction
- [x] `rawCode` is generated internally as `RawCode` value object (`RawCode.generate()`), not received from command
- [x] `deleteByTeacherUid` is always called before `save`
- [x] `IssuePasswordResetCodeHandlerTest` passes all test cases including `shouldThrowWhenProviderIsNotEmailPassword`; methods follow `should...When...` convention
- [x] `./mvnw test -Dtest=IssuePasswordResetCodeHandlerTest -q` exits 0

---

> [← story file](../story-02-auth-bounded-context.md)
