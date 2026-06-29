# ⚛️ TASK 06 — Create UpdatePilotFlagsHandler + UpdatePilotFlagsHandlerTest

> **Status:** TODO
> **Workflow:** IMPLEMENT
> **Depends On:** task-02, task-03
> [← story file](../story-03-teacher-bounded-context.md)

---

## Objective

Create `UpdatePilotFlagsHandler` — a simple atomic handler that loads a `Teacher` by UID, delegates flag mutation to `teacher.updatePilotFlags()`, saves, and returns a result. Write `UpdatePilotFlagsHandlerTest`.

---

## Technical Design

- **Approach:** Direct extraction from `PilotFlagService.updateFlags()`. Key difference: mutation now goes through `Teacher.updatePilotFlags()` (domain method) instead of direct JPA entity setters. The handler loads via `TeacherRepositoryPort.findById()` and throws `TeacherNotFoundException` if not found — the `GlobalExceptionHandler` maps that to 404 through the `ResourceNotFoundException` hierarchy. `@Transactional` on `execute()`. NO `@Service`, NO `@Component` — declared as `@Bean` in `TeacherConfig` (task-09).
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/teacher/application/usecase/UpdatePilotFlagsHandler.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/teacher/application/usecase/UpdatePilotFlagsHandlerTest.java` ← NEW
- **Interfaces / contracts:** `UpdatePilotFlagsHandler` implements `UpdatePilotFlagsUseCase`.

---

## Implementation Steps

1. Create `UpdatePilotFlagsHandler.java` in `teacher/application/usecase/`:
   ```java
   package cl.gradeops.ai.api.teacher.application.usecase;

   import cl.gradeops.ai.api.teacher.application.command.UpdatePilotFlagsCommand;
   import cl.gradeops.ai.api.teacher.application.port.in.UpdatePilotFlagsUseCase;
   import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
   import cl.gradeops.ai.api.teacher.application.result.UpdatePilotFlagsResult;
   import cl.gradeops.ai.api.teacher.domain.exception.TeacherNotFoundException;
   import cl.gradeops.ai.api.teacher.domain.model.Teacher;
   import lombok.RequiredArgsConstructor;
   import org.springframework.transaction.annotation.Transactional;

   // NO @Service — declared as @Bean in TeacherConfig (task-09)
   @RequiredArgsConstructor
   public class UpdatePilotFlagsHandler implements UpdatePilotFlagsUseCase {

       private final TeacherRepositoryPort teacherRepository;

       @Override
       @Transactional
       public UpdatePilotFlagsResult execute(UpdatePilotFlagsCommand command) {
           Teacher teacher = teacherRepository.findById(command.uid())
               .orElseThrow(() -> new TeacherNotFoundException(command.uid()));

           teacher.updatePilotFlags(
               command.planType(), command.relatedParty(),
               command.offerDetails(), command.evidenceLink(), command.setBy()
           );
           teacherRepository.save(teacher);

           return UpdatePilotFlagsResult.builder()
               .firebaseUid(teacher.getFirebaseUid())
               .planType(teacher.getPlanType())
               .relatedParty(teacher.isRelatedParty())
               .flagSetAt(teacher.getFlagSetAt() != null ? teacher.getFlagSetAt().toString() : null)
               .build();
       }
   }
   ```
2. Create `UpdatePilotFlagsHandlerTest.java`:
   - `@ExtendWith(MockitoExtension.class)`.
   - Mock `TeacherRepositoryPort`.
   - Construct handler: `new UpdatePilotFlagsHandler(mockTeacherRepo)`.
   - **Test 1 — teacher not found**: `findById("unknown")` returns `Optional.empty()` → `TeacherNotFoundException` thrown; `save` not called.
   - **Test 2 — happy path**: `findById("uid-1")` returns teacher stub via `Teacher.restore(...)`; command sets `planType = "pilot"`, `relatedParty = true`; verify `save` called; assert result `planType = "pilot"`, `relatedParty = true`, `flagSetAt` non-null.
   - **Test 3 — null fields not overwritten**: command has `planType = "pilot"`, `relatedParty = null`; teacher starts with `relatedParty = true`; after `updatePilotFlags`, `isRelatedParty()` still `true`.
3. Run `./mvnw test -Dtest=UpdatePilotFlagsHandlerTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Unknown UID → `TeacherNotFoundException` | `assertThatThrownBy(...).isInstanceOf(TeacherNotFoundException.class)` |
| 2 | Happy path → `save` called; result has updated flags | `verify(teacherRepo).save(any(Teacher.class))`; assert result fields |
| 3 | Null command field → existing value preserved on aggregate | Pass null `relatedParty`; assert aggregate still has original value |

---

## Done Criteria

- [ ] `UpdatePilotFlagsHandler` exists in `teacher/application/usecase/`
- [ ] Handler is `@RequiredArgsConstructor` with NO `@Service`
- [ ] Handler injects `TeacherRepositoryPort` only (single dependency)
- [ ] Handler calls `teacher.updatePilotFlags()` — does not use direct field setters
- [ ] Handler throws `TeacherNotFoundException` for missing UID
- [ ] All 3 test cases pass
- [ ] `./mvnw test -Dtest=UpdatePilotFlagsHandlerTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-03-teacher-bounded-context.md)
