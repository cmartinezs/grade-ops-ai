# ⚛️ TASK 04 — Move AssessmentController + AssessmentSummaryResponse + update test + delete old flat package

> **Status:** TODO
> **Workflow:** REFACTOR+IMPLEMENT
> **Depends On:** task-02, task-03
> [← story file](../story-04-assessment-bounded-context.md)

---

## Objective

Create `AssessmentController` in `assessment.infrastructure.adapter.in.web` (injecting `ListAssessmentsUseCase` by interface), create `AssessmentSummaryResponse` in `response/`, update `AssessmentControllerTest` to mock `ListAssessmentsUseCase` instead of `AssessmentService`, and delete all remaining old flat `assessment.*` classes (`AssessmentController`, `AssessmentService`, `AssessmentSummaryDto`, old test).

---

## Technical Design

- **Approach:** The new `AssessmentController` is `@RestController @RequiredArgsConstructor` — no `@Value` needed (no config properties used). It reads `AuthenticatedTeacher` from `SecurityContextHolder` (same as old controller), calls `listAssessmentsUseCase.execute(teacher.uid())`, and maps each `AssessmentSummaryResult` to `AssessmentSummaryResponse`. The mapping is structural (same 6 fields); no business logic.
  `AssessmentSummaryResponse` mirrors `AssessmentSummaryResult` but lives in the web layer. The old `AssessmentSummaryDto` is deleted.
  The existing `AssessmentControllerTest` is updated in-place (moved to new package path): `@SpringBootTest` is kept because the controller uses Firebase authentication and `@WebMvcTest` would require additional security config imports. The only change is replacing `@MockitoBean AssessmentService` with `@MockitoBean ListAssessmentsUseCase` and updating the stub return types from `AssessmentSummaryDto` to `AssessmentSummaryResult`.
- **Affected files (new):**
  - `src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/AssessmentController.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/response/AssessmentSummaryResponse.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/AssessmentControllerTest.java` ← NEW (`@SpringBootTest`, updated mock)
- **Affected files (deleted):**
  - `src/main/java/cl/gradeops/ai/api/assessment/AssessmentController.java`
  - `src/main/java/cl/gradeops/ai/api/assessment/AssessmentService.java`
  - `src/main/java/cl/gradeops/ai/api/assessment/AssessmentSummaryDto.java`
  - `src/test/java/cl/gradeops/ai/api/assessment/AssessmentControllerTest.java`

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/response/`.
2. Create `AssessmentSummaryResponse.java`:
   ```java
   package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response;

   import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;

   public record AssessmentSummaryResponse(
       String id,
       String title,
       AssessmentStatus status,
       int submissionCount,
       int pendingApprovals,
       String reportLink
   ) {}
   ```
3. Create `AssessmentController.java`:
   ```java
   package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web;

   import cl.gradeops.ai.api.assessment.application.port.in.ListAssessmentsUseCase;
   import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response.AssessmentSummaryResponse;
   import cl.gradeops.ai.api.security.AuthenticatedTeacher;
   import lombok.RequiredArgsConstructor;
   import org.springframework.security.core.context.SecurityContextHolder;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RequestMapping;
   import org.springframework.web.bind.annotation.RestController;
   import java.util.List;

   @RestController
   @RequestMapping("/api/v1")
   @RequiredArgsConstructor
   public class AssessmentController {

       private final ListAssessmentsUseCase listAssessmentsUseCase;

       @GetMapping("/assessments")
       public List<AssessmentSummaryResponse> listAssessments() {
           AuthenticatedTeacher teacher = (AuthenticatedTeacher)
               SecurityContextHolder.getContext().getAuthentication().getPrincipal();
           return listAssessmentsUseCase.execute(teacher.uid()).stream()
               .map(r -> new AssessmentSummaryResponse(
                   r.id(), r.title(), r.status(),
                   r.submissionCount(), r.pendingApprovals(), r.reportLink()))
               .toList();
       }
   }
   ```
   Note: `AuthenticatedTeacher` import path is still `cl.gradeops.ai.api.security.AuthenticatedTeacher` — this will be updated in Story 05 when `security.*` is migrated to `shared.*`.
4. Create test directory `src/test/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/`.
5. Create `AssessmentControllerTest.java` at the new path — copy the existing test and apply these changes:
   - Update package declaration: `package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web;`
   - Add import: `import cl.gradeops.ai.api.assessment.application.port.in.ListAssessmentsUseCase;`
   - Add import: `import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;`
   - Add import: `import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;`
   - Replace: `@MockitoBean AssessmentService assessmentService;` → `@MockitoBean ListAssessmentsUseCase listAssessmentsUseCase;`
   - In **Test 1** (empty list): `when(listAssessmentsUseCase.execute("uid-teacher-1")).thenReturn(List.of());`
   - In **Test 3** (non-empty list): stub returns `List<AssessmentSummaryResult>`:
     ```java
     AssessmentSummaryResult r1 = AssessmentSummaryResult.builder()
         .id("assess-1").title("Java Basics").status(AssessmentStatus.OPEN)
         .submissionCount(10).pendingApprovals(3).reportLink(null).build();
     AssessmentSummaryResult r2 = AssessmentSummaryResult.builder()
         .id("assess-2").title("Data Structures").status(AssessmentStatus.GRADING)
         .submissionCount(25).pendingApprovals(0)
         .reportLink("https://reports.example.com/assess-2").build();
     when(listAssessmentsUseCase.execute("uid-teacher-2")).thenReturn(List.of(r1, r2));
     ```
   - All `andExpect` assertions remain identical — field names and values are unchanged.
   - Remove all `AssessmentSummaryDto` usages (replaced by `AssessmentSummaryResult` in stubs).
6. Delete old flat files:
   - `src/main/java/cl/gradeops/ai/api/assessment/AssessmentController.java`
   - `src/main/java/cl/gradeops/ai/api/assessment/AssessmentService.java`
   - `src/main/java/cl/gradeops/ai/api/assessment/AssessmentSummaryDto.java`
   - `src/test/java/cl/gradeops/ai/api/assessment/AssessmentControllerTest.java`
7. Run `./mvnw test -Dtest=AssessmentControllerTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Authenticated teacher, empty list → 200 `[]` | Same assertion as old test |
| 2 | Unauthenticated request → 401 | Same assertion as old test |
| 3 | Authenticated teacher, 2 results → 200 with correct JSON fields | Same `jsonPath` assertions as old test |
| 4 | `AssessmentSummaryDto` no longer exists | `find src -name "AssessmentSummaryDto.java"` → 0 results |
| 5 | `AssessmentService` no longer exists | `find src -name "AssessmentService.java"` → 0 results |
| 6 | No flat `assessment.*` controllers or services | `find src -path "*/assessment/Assessment*.java"` → 0 results |

---

## Done Criteria

- [ ] `AssessmentController` in `assessment/infrastructure/adapter/in/web/`; annotated `@RestController @RequiredArgsConstructor`
- [ ] Controller injects `ListAssessmentsUseCase` (by interface) — no direct service or repository references
- [ ] `listAssessments()` maps `AssessmentSummaryResult` → `AssessmentSummaryResponse`; same HTTP behavior
- [ ] `AssessmentSummaryResponse` in `response/`; mirrors the 6 fields of the old `AssessmentSummaryDto`
- [ ] `AssessmentControllerTest` at new package path; `@MockitoBean ListAssessmentsUseCase` (not `AssessmentService`)
- [ ] All 3 test cases pass with identical assertions
- [ ] Old flat files deleted: `AssessmentController`, `AssessmentService`, `AssessmentSummaryDto`, old `AssessmentControllerTest`
- [ ] `./mvnw test -Dtest=AssessmentControllerTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-04-assessment-bounded-context.md)
