# ⚛️ TASK 02 — Create AssessmentSummaryResult + port interfaces

> **Status:** TODO
> **Workflow:** IMPLEMENT
> **Depends On:** task-01
> [← story file](../story-04-assessment-bounded-context.md)

---

## Objective

Create `AssessmentSummaryResult` (application result record), `ListAssessmentsUseCase` (inbound port interface), and `AssessmentRepositoryPort` (stub outbound port interface). These three files are the stable contracts needed by the handler (task-03) and the controller (task-04).

---

## Technical Design

- **Approach:** All three files are pure interface/record declarations — no implementation logic, no Spring annotations. `AssessmentSummaryResult` mirrors the 6 fields of the old `AssessmentSummaryDto` but lives in the application layer and references `AssessmentStatus` from the domain. `AssessmentRepositoryPort` returns `List<AssessmentSummaryResult>` directly — this is a pragmatic choice for the stub phase (no `Assessment` aggregate exists yet; Epic 02 will replace this with proper domain objects). Both port interfaces have no Spring, JPA, or Lombok annotations.
- **Affected files:**
  - `src/main/java/cl/gradeops/ai/api/assessment/application/result/AssessmentSummaryResult.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/assessment/application/port/in/ListAssessmentsUseCase.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/assessment/application/port/out/AssessmentRepositoryPort.java` ← NEW

---

## Implementation Steps

1. Create directories:
   - `src/main/java/cl/gradeops/ai/api/assessment/application/result/`
   - `src/main/java/cl/gradeops/ai/api/assessment/application/port/in/`
   - `src/main/java/cl/gradeops/ai/api/assessment/application/port/out/`
2. Create `AssessmentSummaryResult.java`:
   ```java
   package cl.gradeops.ai.api.assessment.application.result;

   import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
   import lombok.Builder;

   @Builder
   public record AssessmentSummaryResult(
       String id,
       String title,
       AssessmentStatus status,
       int submissionCount,
       int pendingApprovals,
       String reportLink
   ) {}
   ```
3. Create `ListAssessmentsUseCase.java`:
   ```java
   package cl.gradeops.ai.api.assessment.application.port.in;

   import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
   import java.util.List;

   public interface ListAssessmentsUseCase {
       List<AssessmentSummaryResult> execute(String teacherUid);
   }
   ```
4. Create `AssessmentRepositoryPort.java`:
   ```java
   package cl.gradeops.ai.api.assessment.application.port.out;

   import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
   import java.util.List;

   // Stub port — Epic 02 will replace return type with domain Assessment objects
   public interface AssessmentRepositoryPort {
       List<AssessmentSummaryResult> findAllByTeacherId(String teacherUid);
   }
   ```
5. Run `./mvnw compile -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | All 3 files compile | `./mvnw compile -q` exits 0 |
| 2 | `AssessmentSummaryResult` has 6 fields matching old `AssessmentSummaryDto` | Read file: `id, title, status, submissionCount, pendingApprovals, reportLink` |
| 3 | No Spring/JPA annotations on any port interface | `grep -r "@Service\|@Component\|@Repository\|@Entity" src/main/java/.../assessment/application/port/` → 0 results |
| 4 | `AssessmentRepositoryPort` returns domain-layer `AssessmentStatus` through `AssessmentSummaryResult` | Transitive via result record |

---

## Done Criteria

- [ ] `AssessmentSummaryResult` in `assessment/application/result/`; `@Builder` record with 6 fields; uses `AssessmentStatus` from domain
- [ ] `ListAssessmentsUseCase` in `assessment/application/port/in/`; single method `execute(String teacherUid)`
- [ ] `AssessmentRepositoryPort` in `assessment/application/port/out/`; single method `findAllByTeacherId(String teacherUid)`
- [ ] No Spring, JPA, or Lombok annotations (other than `@Builder`) on any port interface
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-04-assessment-bounded-context.md)
