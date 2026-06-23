# ⚛️ TASK 09 — Create TeacherConfig, delete domain/teacher/, run full test suite

> **Status:** TODO
> **Workflow:** CLEANUP+VERIFY
> **Depends On:** task-05, task-06, task-07, task-08
> [← story file](../story-03-teacher-bounded-context.md)

---

## Objective

Create `TeacherConfig.java` in `teacher/infrastructure/config/` with 4 `@Bean` declarations that wire the teacher bounded context. Delete `domain/teacher/TeacherEntity.java`, `domain/teacher/TeacherRepository.java`, and `TeacherRepositoryTest.java` (queries no longer exposed through the port). Run `./mvnw test` and confirm all tests pass, including `HexagonalArchitectureTest`.

---

## Technical Design

- **Approach:** `TeacherConfig` is the final wiring step for the teacher context. It declares `@Bean` methods for `TeacherPersistenceMapper`, `TeacherPersistenceAdapter`, `ProvisionTeacherHandler`, and `UpdatePilotFlagsHandler`. `resetCodeTtlMinutes` is read here from `GradeOpsEmailProperties` and passed as a plain `int` to `ProvisionTeacherHandler`. `AuthConfig` already wires auth context beans (tasks 02 task-12). After this task, `domain/teacher/` must be empty and deleted, and no code anywhere imports `cl.gradeops.ai.api.domain.teacher.*`.
- **TeacherConfig `@Bean` declarations:**
  ```java
  @Configuration
  @RequiredArgsConstructor
  class TeacherConfig {

      private final GradeOpsEmailProperties emailProperties;

      @Bean
      TeacherPersistenceMapper teacherPersistenceMapper() {
          return new TeacherPersistenceMapper();
      }

      @Bean
      TeacherPersistenceAdapter teacherPersistenceAdapter(
              TeacherJpaRepository jpaRepository,
              TeacherPersistenceMapper mapper) {
          return new TeacherPersistenceAdapter(jpaRepository, mapper);
      }

      @Bean
      ProvisionTeacherHandler provisionTeacherHandler(
              AuthPort authPort,
              TeacherRepositoryPort teacherRepository,
              IssuePasswordResetCodeUseCase issuePasswordResetCodeUseCase) {
          int ttlMinutes = emailProperties.getResetPassword().getTtlMinutes();
          return new ProvisionTeacherHandler(authPort, teacherRepository, issuePasswordResetCodeUseCase, ttlMinutes);
      }

      @Bean
      UpdatePilotFlagsHandler updatePilotFlagsHandler(TeacherRepositoryPort teacherRepository) {
          return new UpdatePilotFlagsHandler(teacherRepository);
      }
  }
  ```
- **Files to delete:**
  - `src/main/java/cl/gradeops/ai/api/domain/teacher/TeacherEntity.java`
  - `src/main/java/cl/gradeops/ai/api/domain/teacher/TeacherRepository.java`
  - `src/test/java/cl/gradeops/ai/api/domain/teacher/TeacherRepositoryTest.java`
  - If `domain/teacher/` directory is now empty after the deletions, delete the directory too.
- **Design notes:**
  - `GradeOpsEmailProperties` is imported from `shared.infrastructure.config` (or wherever it lives today) — the same class already used by `AuthConfig`. No new config class needed.
  - `TeacherJpaRepository` is package-private inside `teacher.infrastructure.adapter.out.persistence` — Spring Data auto-detects it because it extends `JpaRepository`; `TeacherConfig` can inject it as a `@Bean` parameter because Spring creates the proxy regardless of visibility.
  - `TeacherRepositoryTest` tested `findByPlanType` and `findByRelatedParty` on the old `TeacherRepository`. Both queries were removed from the port because no current use case calls them. The test file is simply deleted.

---

## Implementation Steps

1. Create `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/config/TeacherConfig.java` with the `@Configuration @RequiredArgsConstructor class TeacherConfig` body shown above. Include all necessary imports.
2. Delete `src/main/java/cl/gradeops/ai/api/domain/teacher/TeacherEntity.java`.
3. Delete `src/main/java/cl/gradeops/ai/api/domain/teacher/TeacherRepository.java`.
4. Delete `src/test/java/cl/gradeops/ai/api/domain/teacher/TeacherRepositoryTest.java`.
5. Check whether `src/main/java/cl/gradeops/ai/api/domain/teacher/` is now empty; if so, delete the directory.
6. Run `grep -r "domain\.teacher" src/ --include="*.java"` — must return 0 results.
7. Run `./mvnw compile -q` — must succeed.
8. Run `./mvnw test` — must pass with 0 failures.
9. Verify `HexagonalArchitectureTest` passes (it will run as part of step 8).

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | No remaining references to `domain.teacher` package | `grep -r "domain\.teacher" src/ --include="*.java"` → 0 results |
| 2 | `TeacherConfig` compiles; all 4 beans declared | `./mvnw compile -q` exits 0 |
| 3 | Full test suite passes | `./mvnw test` exits 0; 0 failures |
| 4 | `HexagonalArchitectureTest` passes | Included in `./mvnw test` run |
| 5 | Old JPA entity/repo files gone | `find src -name "TeacherEntity.java" -o -name "TeacherRepositoryTest.java"` → 0 results |

---

## Done Criteria

- [ ] `TeacherConfig` exists in `teacher/infrastructure/config/`; declares 4 `@Bean` methods; annotated `@Configuration @RequiredArgsConstructor`
- [ ] `resetCodeTtlMinutes` read from `emailProperties.getResetPassword().getTtlMinutes()` in `TeacherConfig` — not in `ProvisionTeacherHandler`
- [ ] `domain/teacher/TeacherEntity.java` deleted
- [ ] `domain/teacher/TeacherRepository.java` deleted
- [ ] `TeacherRepositoryTest.java` deleted
- [ ] `grep -r "domain\.teacher" src/ --include="*.java"` returns 0 results
- [ ] `./mvnw test` exits 0; 0 failures
- [ ] `HexagonalArchitectureTest` passes
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-03-teacher-bounded-context.md)
