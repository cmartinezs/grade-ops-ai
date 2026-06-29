# ⚛️ TASK 03 — Create teacher application port interfaces (port.in + TeacherRepositoryPort)

> **Status:** TODO
> **Workflow:** IMPLEMENT
> **Depends On:** task-01, task-02
> [← story file](../story-03-teacher-bounded-context.md)

---

## Objective

Create 2 inbound use-case interfaces (`ProvisionTeacherUseCase`, `UpdatePilotFlagsUseCase`) in `teacher.application.port.in` and the `TeacherRepositoryPort` outbound interface in `teacher.application.port.out`. These are the stable contracts consumed by handlers (tasks 05, 06) and adapters (task 07).

---

## Technical Design

- **Approach:** Minimal port interfaces — one method each, typed on command/result records from task-02 and domain types from task-01. `TeacherRepositoryPort` exposes only the 4 operations needed by the two handlers; `findByPlanType`/`findByRelatedParty` from the old `TeacherRepository` are NOT in the port (currently unused by any use case). No annotations on port interfaces.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/teacher/application/port/in/ProvisionTeacherUseCase.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/application/port/in/UpdatePilotFlagsUseCase.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/application/port/out/TeacherRepositoryPort.java` ← NEW
- **Interfaces / contracts:**
  - `ProvisionTeacherUseCase.execute(ProvisionTeacherCommand) → ProvisionTeacherResult`
  - `UpdatePilotFlagsUseCase.execute(UpdatePilotFlagsCommand) → UpdatePilotFlagsResult`
  - `TeacherRepositoryPort`:
    - `void save(Teacher teacher)`
    - `Optional<Teacher> findById(String firebaseUid)`
    - `Optional<Teacher> findByEmail(String email)`
    - `boolean existsByEmail(String email)`
- **Design notes:** Port interfaces import only types from `teacher.application.command`, `teacher.application.result`, `teacher.domain.model`, and `java.*`. No Spring, no JPA, no Lombok annotations.

---

## Implementation Steps

1. Create directories:
   - `src/main/java/cl/gradeops/ai/api/teacher/application/port/in/`
   - `src/main/java/cl/gradeops/ai/api/teacher/application/port/out/`
2. Create `ProvisionTeacherUseCase.java`:
   ```java
   package cl.gradeops.ai.api.teacher.application.port.in;

   import cl.gradeops.ai.api.teacher.application.command.ProvisionTeacherCommand;
   import cl.gradeops.ai.api.teacher.application.result.ProvisionTeacherResult;

   public interface ProvisionTeacherUseCase {
       ProvisionTeacherResult execute(ProvisionTeacherCommand command);
   }
   ```
3. Create `UpdatePilotFlagsUseCase.java`:
   ```java
   package cl.gradeops.ai.api.teacher.application.port.in;

   import cl.gradeops.ai.api.teacher.application.command.UpdatePilotFlagsCommand;
   import cl.gradeops.ai.api.teacher.application.result.UpdatePilotFlagsResult;

   public interface UpdatePilotFlagsUseCase {
       UpdatePilotFlagsResult execute(UpdatePilotFlagsCommand command);
   }
   ```
4. Create `TeacherRepositoryPort.java`:
   ```java
   package cl.gradeops.ai.api.teacher.application.port.out;

   import cl.gradeops.ai.api.teacher.domain.model.Teacher;
   import java.util.Optional;

   public interface TeacherRepositoryPort {
       void save(Teacher teacher);
       Optional<Teacher> findById(String firebaseUid);
       Optional<Teacher> findByEmail(String email);
       boolean existsByEmail(String email);
   }
   ```
5. Run `./mvnw compile -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | All 3 interface files compile | `./mvnw compile -q` exits 0 |
| 2 | `TeacherRepositoryPort` returns `Teacher` domain type (not entity) | Read the file: `Optional<Teacher>` as return type |
| 3 | No Spring/JPA/Lombok annotations on any port interface | `grep -r "@Service\|@Component\|@Repository\|@Entity" src/main/java/cl/gradeops/ai/api/teacher/application/port/` returns 0 |

---

## Done Criteria

- [ ] `ProvisionTeacherUseCase` and `UpdatePilotFlagsUseCase` exist in `teacher/application/port/in/`
- [ ] `TeacherRepositoryPort` exists in `teacher/application/port/out/` with 4 methods
- [ ] `TeacherRepositoryPort.findById()` and `findByEmail()` return `Optional<Teacher>` (domain type, not JPA entity)
- [ ] No Spring, JPA, or Lombok annotations on any port interface
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-03-teacher-bounded-context.md)
