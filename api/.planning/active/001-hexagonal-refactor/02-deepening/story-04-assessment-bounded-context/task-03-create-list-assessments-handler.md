# ⚛️ TASK 03 — Create ListAssessmentsHandler + ListAssessmentsHandlerTest

> **Status:** TODO
> **Workflow:** IMPLEMENT
> **Depends On:** task-02
> [← story file](../story-04-assessment-bounded-context.md)

---

## Objective

Create `ListAssessmentsHandler` — a thin handler that delegates to `AssessmentRepositoryPort`. Write `ListAssessmentsHandlerTest` with 2 unit test cases. No `@Service` annotation — declared as `@Bean` in `AssessmentConfig` (task-05).

---

## Technical Design

- **Approach:** The handler is a direct extraction of `AssessmentService.listForTeacher()`. It is trivially thin: one port dependency, one `@Transactional(readOnly = true)` method, no branching logic. The only reason for the `@Transactional` is consistency with the hexagonal rule (handlers own transactions) and preparedness for Epic 02 when real DB calls are introduced. NO `@Service`, NO `@Component` — declared as `@Bean` in `AssessmentConfig` (task-05).
- **Affected files:**
  - `src/main/java/cl/gradeops/ai/api/assessment/application/usecase/ListAssessmentsHandler.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/assessment/application/usecase/ListAssessmentsHandlerTest.java` ← NEW

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/assessment/application/usecase/`.
2. Create `ListAssessmentsHandler.java`:
   ```java
   package cl.gradeops.ai.api.assessment.application.usecase;

   import cl.gradeops.ai.api.assessment.application.port.in.ListAssessmentsUseCase;
   import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
   import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
   import lombok.RequiredArgsConstructor;
   import org.springframework.transaction.annotation.Transactional;
   import java.util.List;

   // NO @Service — declared as @Bean in AssessmentConfig (task-05)
   @RequiredArgsConstructor
   public class ListAssessmentsHandler implements ListAssessmentsUseCase {

       private final AssessmentRepositoryPort assessmentRepository;

       @Override
       @Transactional(readOnly = true)
       public List<AssessmentSummaryResult> execute(String teacherUid) {
           return assessmentRepository.findAllByTeacherId(teacherUid);
       }
   }
   ```
3. Create test directory `src/test/java/cl/gradeops/ai/api/assessment/application/usecase/`.
4. Create `ListAssessmentsHandlerTest.java`:
   - `@ExtendWith(MockitoExtension.class)`
   - Mock `AssessmentRepositoryPort`
   - Construct handler: `new ListAssessmentsHandler(mockAssessmentRepository)`
   - **Test 1 — empty list**: `findAllByTeacherId("uid-1")` returns `List.of()` → `execute("uid-1")` returns empty list; `findAllByTeacherId` called with `"uid-1"`.
   - **Test 2 — non-empty list**: `findAllByTeacherId("uid-2")` returns `List.of(result1, result2)` → `execute("uid-2")` returns the same list; verify delegation only (handler adds no logic).
5. Run `./mvnw test -Dtest=ListAssessmentsHandlerTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Empty repo → empty list returned | `assertThat(result).isEmpty()` |
| 2 | Non-empty repo → same list returned (delegation only) | `assertThat(result).hasSize(2).containsExactly(result1, result2)` |
| 3 | Handler does not filter, sort, or transform the list | No mapping in implementation; assertion above confirms pass-through |
| 4 | `@Service` annotation absent | `grep -n "@Service" src/main/java/.../assessment/application/usecase/ListAssessmentsHandler.java` → 0 results |

---

## Done Criteria

- [ ] `ListAssessmentsHandler` exists in `assessment/application/usecase/`
- [ ] Handler is `@RequiredArgsConstructor` with NO `@Service` — declared as `@Bean` in `AssessmentConfig`
- [ ] Handler injects `AssessmentRepositoryPort` only (single dependency)
- [ ] `@Transactional(readOnly = true)` on `execute()`
- [ ] Both test cases pass
- [ ] `./mvnw test -Dtest=ListAssessmentsHandlerTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-04-assessment-bounded-context.md)
