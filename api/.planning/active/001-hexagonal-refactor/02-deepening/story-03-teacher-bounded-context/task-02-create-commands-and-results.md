# ⚛️ TASK 02 — Create teacher commands and results

> **Status:** TODO
> **Workflow:** IMPLEMENT
> **Depends On:** (none)
> [← story file](../story-03-teacher-bounded-context.md)

---

## Objective

Create 4 records in `teacher.application`: `ProvisionTeacherCommand`, `UpdatePilotFlagsCommand` (in `command/`) and `ProvisionTeacherResult`, `UpdatePilotFlagsResult` (in `result/`), all with `@Builder` and null-guard compact constructors where required.

---

## Technical Design

- **Approach:** Pure data records. No domain imports, no Spring imports. `ProvisionTeacherCommand` validates that the three required fields are non-null. `UpdatePilotFlagsCommand` only validates `uid` (all flag fields are optional). `ProvisionTeacherResult` carries `rawCode` (UUID string) — the caller (controller) builds the invite URL. `UpdatePilotFlagsResult` mirrors the shape of the old `PilotFlagResponse` to keep the HTTP response unchanged.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/teacher/application/command/ProvisionTeacherCommand.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/application/command/UpdatePilotFlagsCommand.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/application/result/ProvisionTeacherResult.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/application/result/UpdatePilotFlagsResult.java` ← NEW
- **Interfaces / contracts:** No external dependencies. All 4 files must compile with `./mvnw compile -q`.

---

## Implementation Steps

1. Create directories:
   - `src/main/java/cl/gradeops/ai/api/teacher/application/command/`
   - `src/main/java/cl/gradeops/ai/api/teacher/application/result/`
2. Create `ProvisionTeacherCommand.java`:
   ```java
   package cl.gradeops.ai.api.teacher.application.command;

   import lombok.Builder;
   import java.util.Objects;

   @Builder
   public record ProvisionTeacherCommand(String firstName, String lastName, String email) {
       public ProvisionTeacherCommand {
           Objects.requireNonNull(firstName, "firstName must not be null");
           Objects.requireNonNull(lastName,  "lastName must not be null");
           Objects.requireNonNull(email,     "email must not be null");
       }
   }
   ```
3. Create `UpdatePilotFlagsCommand.java`:
   ```java
   package cl.gradeops.ai.api.teacher.application.command;

   import lombok.Builder;
   import java.util.Objects;

   @Builder
   public record UpdatePilotFlagsCommand(
       String uid,
       String planType,
       Boolean relatedParty,
       String offerDetails,
       String evidenceLink,
       String setBy
   ) {
       public UpdatePilotFlagsCommand {
           Objects.requireNonNull(uid, "uid must not be null");
       }
   }
   ```
4. Create `ProvisionTeacherResult.java`:
   ```java
   package cl.gradeops.ai.api.teacher.application.result;

   import lombok.Builder;

   @Builder
   public record ProvisionTeacherResult(String firebaseUid, String rawCode) {}
   ```
5. Create `UpdatePilotFlagsResult.java`:
   ```java
   package cl.gradeops.ai.api.teacher.application.result;

   import lombok.Builder;

   @Builder
   public record UpdatePilotFlagsResult(
       String firebaseUid,
       String planType,
       boolean relatedParty,
       String flagSetAt
   ) {}
   ```
6. Run `./mvnw compile -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | All 4 records compile | `./mvnw compile -q` exits 0 |
| 2 | `ProvisionTeacherCommand` rejects null `email` | `assertThatThrownBy(() -> new ProvisionTeacherCommand("F", "L", null)).isInstanceOf(NullPointerException.class)` |
| 3 | `UpdatePilotFlagsCommand` accepts null flag fields | Build with only `uid` set — no exception |
| 4 | `@Builder` works on all 4 types | Use `.builder().build()` syntax in test without errors |

---

## Done Criteria

- [ ] `ProvisionTeacherCommand`, `UpdatePilotFlagsCommand` exist in `teacher/application/command/`
- [ ] `ProvisionTeacherResult`, `UpdatePilotFlagsResult` exist in `teacher/application/result/`
- [ ] All 4 records have `@Builder`
- [ ] `ProvisionTeacherCommand` enforces non-null on `firstName`, `lastName`, `email`
- [ ] `UpdatePilotFlagsCommand` enforces non-null on `uid` only
- [ ] `ProvisionTeacherResult` carries `rawCode` (not a full URL)
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-03-teacher-bounded-context.md)
