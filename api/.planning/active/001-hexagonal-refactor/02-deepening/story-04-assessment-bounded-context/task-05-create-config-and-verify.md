# ⚛️ TASK 05 — Create StubAssessmentPersistenceAdapter + AssessmentConfig + final verification

> **Status:** DONE
> **Workflow:** CLEANUP+VERIFY
> **Depends On:** task-03, task-04
> [← story file](../story-04-assessment-bounded-context.md)

---

## Objective

Create `StubAssessmentPersistenceAdapter` (implements `AssessmentRepositoryPort`, returns `List.of()`), create `AssessmentConfig` (declares the 2 application beans), and run `./mvnw test` to confirm the full suite is green including `HexagonalArchitectureTest`.

---

## Technical Design

- **Approach:** `StubAssessmentPersistenceAdapter` is the infrastructure placeholder for the assessment repository — there is no assessment database table yet (Epic 02 will introduce it). It implements `AssessmentRepositoryPort` and returns an empty list. NO `@Repository` annotation — declared as `@Bean` in `AssessmentConfig`. `AssessmentConfig` is a package-private `@Configuration` class in `assessment/infrastructure/config/` that wires 2 beans: the stub adapter and the handler. No `@RequiredArgsConstructor` is needed on `AssessmentConfig` itself (no field dependencies).
- **Affected files (new):**
  - `src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/out/persistence/StubAssessmentPersistenceAdapter.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/assessment/infrastructure/config/AssessmentConfig.java` ← NEW

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/out/persistence/`.
2. Create `StubAssessmentPersistenceAdapter.java`:
   ```java
   package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

   import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
   import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
   import java.util.List;

   // Stub — returns empty list until assessment table is created in Epic 02
   // NO @Repository — declared as @Bean in AssessmentConfig
   public class StubAssessmentPersistenceAdapter implements AssessmentRepositoryPort {

       @Override
       public List<AssessmentSummaryResult> findAllByTeacherId(String teacherUid) {
           return List.of();
       }
   }
   ```
3. Create directory `src/main/java/cl/gradeops/ai/api/assessment/infrastructure/config/`.
4. Create `AssessmentConfig.java`:
   ```java
   package cl.gradeops.ai.api.assessment.infrastructure.config;

   import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
   import cl.gradeops.ai.api.assessment.application.usecase.ListAssessmentsHandler;
   import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.StubAssessmentPersistenceAdapter;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;

   @Configuration
   class AssessmentConfig {

       @Bean
       StubAssessmentPersistenceAdapter stubAssessmentPersistenceAdapter() {
           return new StubAssessmentPersistenceAdapter();
       }

       @Bean
       ListAssessmentsHandler listAssessmentsHandler(AssessmentRepositoryPort assessmentRepository) {
           return new ListAssessmentsHandler(assessmentRepository);
       }
   }
   ```
5. Run `grep -r "assessment\.AssessmentStatus\|assessment\.AssessmentService\|assessment\.AssessmentSummaryDto\|assessment\.AssessmentController" src/ --include="*.java"` — must return 0 results.
6. Run `./mvnw compile -q` — must succeed.
7. Run `./mvnw test` — must pass with 0 failures.
8. Confirm `HexagonalArchitectureTest` passes (included in step 7).

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | No remaining references to old flat `assessment.*` types | `grep -r "assessment\.Assessment" src/ --include="*.java"` filtered for old names → 0 results |
| 2 | `AssessmentConfig` compiles; 2 beans declared | `./mvnw compile -q` exits 0 |
| 3 | Full test suite passes | `./mvnw test` exits 0; 0 failures |
| 4 | `HexagonalArchitectureTest` passes | Included in `./mvnw test` run |
| 5 | `StubAssessmentPersistenceAdapter` has NO `@Repository` | `grep -n "@Repository" src/main/.../StubAssessmentPersistenceAdapter.java` → 0 results |

---

## Done Criteria

- [ ] `StubAssessmentPersistenceAdapter` in `assessment/infrastructure/adapter/out/persistence/`; implements `AssessmentRepositoryPort`; returns `List.of()`; NO `@Repository`
- [ ] `AssessmentConfig` in `assessment/infrastructure/config/`; declares `StubAssessmentPersistenceAdapter` and `ListAssessmentsHandler` as `@Bean`
- [ ] `grep` for old flat-package types returns 0 results
- [ ] `./mvnw test` exits 0; 0 failures
- [ ] `HexagonalArchitectureTest` passes
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-04-assessment-bounded-context.md)
