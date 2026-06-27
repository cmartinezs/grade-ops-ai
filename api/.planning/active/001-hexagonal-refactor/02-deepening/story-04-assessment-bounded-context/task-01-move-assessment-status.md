# ⚛️ TASK 01 — Move AssessmentStatus to assessment.domain.model

> **Status:** DONE
> **Workflow:** REFACTOR
> **Depends On:** (none)
> [← story file](../story-04-assessment-bounded-context.md)

---

## Objective

Move the `AssessmentStatus` enum from the flat `cl.gradeops.ai.api.assessment` package to `cl.gradeops.ai.api.assessment.domain.model`. Update the two files that import it (`AssessmentSummaryDto` and `AssessmentControllerTest`). Delete the old `AssessmentStatus.java`.

---

## Technical Design

- **Approach:** Simple file relocation + import update. `AssessmentStatus` is a pure enum with no Spring/JPA annotations — it already belongs in domain. The two consumers (`AssessmentSummaryDto` and `AssessmentControllerTest`) reference it by import; both will have a temporary update here (they are themselves replaced in tasks 02 and 04 respectively, but must compile cleanly after this task).
- **Affected files:**
  - `src/main/java/cl/gradeops/ai/api/assessment/domain/model/AssessmentStatus.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/assessment/AssessmentSummaryDto.java` ← MODIFIED (import update only)
  - `src/test/java/cl/gradeops/ai/api/assessment/AssessmentControllerTest.java` ← MODIFIED (import update only)
  - `src/main/java/cl/gradeops/ai/api/assessment/AssessmentStatus.java` ← DELETED
- **Design notes:** `AssessmentController.java` does NOT import `AssessmentStatus` directly (it uses `AssessmentSummaryDto` which contains it); no change needed there.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/assessment/domain/model/`.
2. Create `AssessmentStatus.java` at new location:
   ```java
   package cl.gradeops.ai.api.assessment.domain.model;

   public enum AssessmentStatus {
       DRAFT,
       OPEN,
       GRADING,
       CLOSED
   }
   ```
3. Update import in `AssessmentSummaryDto.java` (add import, same package → explicit):
   - Before: no explicit import (same package)
   - After: `import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;`
4. Update import in `AssessmentControllerTest.java`:
   - Before: no explicit import (same package)
   - After: `import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;`
5. Delete `src/main/java/cl/gradeops/ai/api/assessment/AssessmentStatus.java`.
6. Run `./mvnw compile -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | New `AssessmentStatus.java` compiles in `assessment.domain.model` | `./mvnw compile -q` exits 0 |
| 2 | Old `AssessmentStatus.java` gone | `find src -path "*/assessment/AssessmentStatus.java"` → 0 results |
| 3 | All 4 enum values present | Read new file: `DRAFT, OPEN, GRADING, CLOSED` |
| 4 | No Spring/JPA annotations on enum | `grep -n "@" src/main/java/.../assessment/domain/model/AssessmentStatus.java` → 0 results |

---

## Done Criteria

- [ ] `AssessmentStatus` exists in `assessment/domain/model/`; no Spring, JPA, or Lombok annotations
- [ ] Old `assessment/AssessmentStatus.java` deleted
- [ ] `AssessmentSummaryDto` imports from new path
- [ ] `AssessmentControllerTest` imports from new path
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-04-assessment-bounded-context.md)
