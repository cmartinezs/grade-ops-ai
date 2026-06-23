# ⚛️ TASK 05 — Create ProvisionTeacherHandler (saga) + ProvisionTeacherHandlerTest

> **Status:** TODO
> **Workflow:** IMPLEMENT
> **Depends On:** task-02, task-03, task-04
> [← story file](../story-03-teacher-bounded-context.md)

---

## Objective

Create `ProvisionTeacherHandler` — a saga-pattern handler that creates a Firebase user, provisions a `Teacher` aggregate, and issues a password reset code. Compensates by deleting the Firebase user if DB save or code issuance fails. Write `ProvisionTeacherHandlerTest` covering 4 scenarios.

---

## Technical Design

- **Approach:** The saga inlines compensation logic in a try/catch because the flow is short (< 40 lines) and the only non-transactional side-effect is the Firebase user creation. `@Transactional` on `execute()` ensures DB operations roll back on exception; the catch block handles the Firebase compensation (deleteUser) before re-throwing, which is exactly when the Spring proxy will roll back. No orchestrator class needed — the handler is self-contained. `resetCodeTtlMinutes` is a plain `int` constructor field supplied by `TeacherConfig` (created in task-09) from `GradeOpsEmailProperties` — the handler has no config imports.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/teacher/application/usecase/ProvisionTeacherHandler.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/teacher/application/usecase/ProvisionTeacherHandlerTest.java` ← NEW
- **Interfaces / contracts:** `ProvisionTeacherHandler` implements `ProvisionTeacherUseCase`.
- **Design notes:**
  - Pre-check: `teacherRepository.existsByEmail(command.email())` → throws `DuplicateEmailException` immediately (before any Firebase call). This avoids creating a Firebase user that would need compensation.
  - Saga body: `createUser` → `Teacher.provision` → `teacherRepository.save` → `issuePasswordResetCodeUseCase.execute`.
  - Compensation: if any step after `createUser` throws, the catch block calls `authPort.deleteUser(firebaseUid)` (best-effort, swallows its own exception) before re-throwing the original.
  - `IssuePasswordResetCodeCommand` requires `teacherUid` and `ttlMinutes`.
  - Returns `ProvisionTeacherResult(firebaseUid, rawCode)` — the controller builds the invite URL.
  - NO `@Service`, NO `@Component` — declared as `@Bean` in `TeacherConfig` (task-09).

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/teacher/application/usecase/`.
2. Create `ProvisionTeacherHandler.java`:
   ```java
   package cl.gradeops.ai.api.teacher.application.usecase;

   import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
   import cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase;
   import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
   import cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException;
   import cl.gradeops.ai.api.teacher.application.command.ProvisionTeacherCommand;
   import cl.gradeops.ai.api.teacher.application.port.in.ProvisionTeacherUseCase;
   import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
   import cl.gradeops.ai.api.teacher.application.result.ProvisionTeacherResult;
   import cl.gradeops.ai.api.teacher.domain.model.AuthProvider;
   import cl.gradeops.ai.api.teacher.domain.model.Teacher;
   import lombok.RequiredArgsConstructor;
   import org.springframework.transaction.annotation.Transactional;

   // NO @Service — declared as @Bean in TeacherConfig (task-09)
   @RequiredArgsConstructor
   public class ProvisionTeacherHandler implements ProvisionTeacherUseCase {

       private final AuthPort authPort;
       private final TeacherRepositoryPort teacherRepository;
       private final IssuePasswordResetCodeUseCase issuePasswordResetCodeUseCase;
       private final int resetCodeTtlMinutes; // supplied by TeacherConfig

       @Override
       @Transactional
       public ProvisionTeacherResult execute(ProvisionTeacherCommand command) {
           if (teacherRepository.existsByEmail(command.email())) {
               throw new DuplicateEmailException(command.email());
           }

           String firebaseUid = null;
           try {
               String displayName = command.firstName() + " " + command.lastName();
               firebaseUid = authPort.createUser(command.email(), displayName);

               Teacher teacher = Teacher.provision(
                   firebaseUid, command.firstName(), command.lastName(),
                   command.email(), AuthProvider.EMAIL_PASSWORD
               );
               teacherRepository.save(teacher);

               var codeResult = issuePasswordResetCodeUseCase.execute(
                   IssuePasswordResetCodeCommand.builder()
                       .teacherUid(firebaseUid)
                       .ttlMinutes(resetCodeTtlMinutes)
                       .build()
               );

               return ProvisionTeacherResult.builder()
                   .firebaseUid(firebaseUid)
                   .rawCode(codeResult.rawCode())
                   .build();

           } catch (Exception ex) {
               if (firebaseUid != null) {
                   try { authPort.deleteUser(firebaseUid); } catch (Exception ignored) {}
               }
               if (ex instanceof RuntimeException rte) throw rte;
               throw new RuntimeException(ex);
           }
       }
   }
   ```
3. Create test directory `src/test/java/cl/gradeops/ai/api/teacher/application/usecase/`.
4. Create `ProvisionTeacherHandlerTest.java`:
   - `@ExtendWith(MockitoExtension.class)`.
   - Mock `AuthPort`, `TeacherRepositoryPort`, `IssuePasswordResetCodeUseCase`.
   - Construct handler: `new ProvisionTeacherHandler(mockAuthPort, mockTeacherRepo, mockIssueUseCase, 30)`.
   - **Test 1 — duplicate email pre-check**: `mockTeacherRepo.existsByEmail("dup@x.com")` returns `true` → `DuplicateEmailException` thrown; verify `authPort.createUser` is NOT called.
   - **Test 2 — happy path**: `existsByEmail` returns `false`; `createUser` returns `"uid-123"`; `save` succeeds; `issuePasswordResetCodeUseCase.execute` returns result with `rawCode = "code-abc"` → result has `firebaseUid = "uid-123"` and `rawCode = "code-abc"`.
   - **Test 3 — DB save fails (compensation)**: `existsByEmail` false; `createUser` returns `"uid-123"`; `save` throws `RuntimeException("db down")` → `deleteUser("uid-123")` called; exception propagates.
   - **Test 4 — Firebase createUser fails**: `createUser` throws `DuplicateEmailException` (e.g., Firebase already has the email) → `deleteUser` NOT called (uid is still null); `DuplicateEmailException` propagates.
5. Run `./mvnw test -Dtest=ProvisionTeacherHandlerTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Duplicate email → `DuplicateEmailException`; no Firebase call | `verifyNoInteractions(authPort)` |
| 2 | Happy path → correct `firebaseUid` and `rawCode` in result | Assert result fields |
| 3 | DB save fails → Firebase user compensated | `verify(authPort).deleteUser("uid-123")` |
| 4 | `createUser` fails → no compensation (uid null) | `verify(authPort, never()).deleteUser(any())` |

---

## Done Criteria

- [ ] `ProvisionTeacherHandler` exists in `teacher/application/usecase/`
- [ ] Handler is `@RequiredArgsConstructor` with NO `@Service` — declared as `@Bean` in `TeacherConfig`
- [ ] Handler receives `int resetCodeTtlMinutes` as constructor field (no config imports)
- [ ] Pre-check for duplicate email runs before any Firebase call
- [ ] Compensation (`deleteUser`) is called when any post-`createUser` step fails
- [ ] Handler is `@Transactional` on `execute()`
- [ ] All 4 test cases pass
- [ ] `./mvnw test -Dtest=ProvisionTeacherHandlerTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-03-teacher-bounded-context.md)
