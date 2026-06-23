# ⚛️ TASK 02 — Run full test suite + verify all 4 ArchUnit constraints

> **Status:** TODO
> **Workflow:** TEST
> **Depends On:** (none — can run concurrently with task-01)
> [← story file](../story-05-final-cleanup.md)

---

## Objective

Run `./mvnw test` and confirm 0 failures across all migrated + new unit tests. Verify that `HexagonalArchitectureTest` reports 0 violations for all 4 architectural constraint rules.

---

## Technical Design

- **Approach:** The full test suite at this point includes: all original tests (migrated to their new package paths by stories 01–04) plus all new unit tests written during those stories. `HexagonalArchitectureTest` was introduced in Story 01 and has been guarding each story from regressions. This task confirms the final green state of the entire suite.
- **Expected test classes to be present and passing:**

| Test class | Created/migrated by |
|-----------|-------------------|
| `shared.domain.model.AggregateRootTest` (if written) | Story 01 |
| `auth.domain.model.PasswordResetCodeTest` | Story 01 or 02 |
| `auth.infrastructure.adapter.out.firebase.FirebaseAuthAdapterTest` | Story 02 (migrated) |
| `auth.application.usecase.RegisterHandlerTest` | Story 02 |
| `auth.application.usecase.SignOutHandlerTest` | Story 02 |
| `auth.application.usecase.RevokeRefreshTokensHandlerTest` | Story 02 |
| `auth.application.usecase.IssuePasswordResetCodeHandlerTest` | Story 02 |
| `auth.application.orchestrator.SendPasswordResetEmailOrchestratorTest` | Story 02 |
| `auth.application.orchestrator.ResetPasswordOrchestratorTest` | Story 02 |
| `auth.infrastructure.adapter.out.persistence.PasswordResetCodePersistenceAdapterTest` | Story 02 |
| `auth.infrastructure.adapter.in.web.AuthControllerTest` | Story 02 (migrated) |
| `teacher.domain.model.TeacherTest` | Story 03 |
| `teacher.application.usecase.ProvisionTeacherHandlerTest` | Story 03 |
| `teacher.application.usecase.UpdatePilotFlagsHandlerTest` | Story 03 |
| `teacher.infrastructure.adapter.out.persistence.TeacherPersistenceAdapterTest` | Story 03 |
| `teacher.infrastructure.adapter.in.web.InternalTeacherControllerTest` | Story 03 (migrated) |
| `assessment.application.usecase.ListAssessmentsHandlerTest` | Story 04 |
| `assessment.infrastructure.adapter.in.web.AssessmentControllerTest` | Story 04 (migrated) |
| `shared.infrastructure.config.security.EmailVerifiedFilterTest` | Story 01 (migrated) |
| `shared.infrastructure.config.security.FirebaseTokenFilterTest` | Story 01 (migrated) |
| `shared.infrastructure.config.security.OwnershipVerifierTest` | Story 01 (migrated) |
| `shared.infrastructure.adapter.out.email.JavaMailEmailServiceTest` | Story 01 (migrated) |
| `HexagonalArchitectureTest` | Story 01 |

- **4 ArchUnit rules to verify green:**
  1. Domain classes do not depend on Spring or JPA
  2. Application classes do not depend on infrastructure classes
  3. Web adapter classes (`adapter.in.web`) do not implement/use `Repository`
  4. Web adapter classes do not access persistence adapter classes (`adapter.out.persistence`)

---

## Implementation Steps

1. Run the full suite:
   ```bash
   ./mvnw test
   ```
2. Check the summary line: `Tests run: N, Failures: 0, Errors: 0, Skipped: 0`.
3. If any test fails:
   - Identify which story introduced the failing class.
   - Fix the root cause (broken import, missing bean, wrong package) in the relevant source file.
   - Re-run `./mvnw test` until green.
4. Grep output for `HexagonalArchitectureTest`:
   ```bash
   ./mvnw test -Dtest=HexagonalArchitectureTest -q 2>&1 | tail -20
   ```
   Confirm: `Tests run: 4, Failures: 0`.
5. If any ArchUnit rule fires, the violation message names the offending class and rule. Fix the dependency violation in the identified class.

---

## Unit Tests

| # | Verification | Expected result |
|---|-------------|----------------|
| 1 | `./mvnw test` exit code | 0 |
| 2 | Summary line | `Failures: 0, Errors: 0` |
| 3 | `HexagonalArchitectureTest` | `Tests run: 4, Failures: 0` |
| 4 | No test at an old package path | `find src/test -path "*/api/common/*" -o -path "*/api/internal/*" ...` → 0 results |

---

## Done Criteria

- [ ] `./mvnw test` exits 0
- [ ] 0 test failures; 0 test errors
- [ ] `HexagonalArchitectureTest` runs 4 tests, all green
- [ ] No test class still lives at a legacy package path (task-01 verified the source; this verifies the test side)
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-05-final-cleanup.md)
