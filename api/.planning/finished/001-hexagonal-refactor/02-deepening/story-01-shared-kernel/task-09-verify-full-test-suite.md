# ⚛️ TASK 09 — Verify full test suite

> **Status:** DONE
> **Workflow:** TEST
> **Depends On:** task-03, task-04, task-05, task-06, task-07, task-08
> [← story file](../story-01-shared-kernel.md)

---

## Objective

Run `./mvnw test` and confirm all tests pass — the 14 existing tests (migrated to new package imports across tasks 03–07) plus the new `HexagonalArchitectureTest` (task-08). Zero failures, zero compilation errors.

---

## Technical Design

- **Approach:** This is a quality-gate task, not a code-writing task. It exists to make the "story is complete" signal explicit and binary. If any test fails, the fix belongs in the task that moved the relevant class (not here), and this task is retried after that fix.
- **Affected files / components:** No files are created or modified. If failures are found, trace them to the responsible task and fix there.
- **Interfaces / contracts:** None.
- **Design notes:**
  - The ArchUnit test (`HexagonalArchitectureTest`) will be included in the full `./mvnw test` run by default.
  - Tests that loaded `FirebaseTestConfig` to avoid a live Firebase call may fail if `FirebaseConfig` is not found by Spring test context — this would indicate `FirebaseTestConfig` was not updated in task-06. Fix in task-06.
  - `PasswordResetServiceTest` mocks `JavaMailEmailService` — if the mock type is wrong the test fails. Fix in task-07.
  - Security tests (`EmailVerifiedFilterTest`, `FirebaseTokenFilterTest`, `OwnershipVerifierTest`) use new class locations — if imports were missed in task-05 the test compile fails. Fix in task-05.

---

## Implementation Steps

1. Run the full Maven test suite: `./mvnw test`
2. If failures: identify which test and which task-NN introduced the broken state. Fix in that task, commit the fix, then re-run this verification.
3. Confirm test count includes `HexagonalArchitectureTest` (look for it in Surefire output).
4. Confirm no `BUILD FAILURE` on compilation (`[ERROR] COMPILATION ERROR`).

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | All tests pass | `./mvnw test` output ends with `BUILD SUCCESS` and `Tests run: N, Failures: 0, Errors: 0, Skipped: 0` |
| 2 | HexagonalArchitectureTest is included | Surefire output includes `HexagonalArchitectureTest` in the test list |
| 3 | All 4 ArchUnit rules pass | No `ArchAssertionError` in test output |
| 4 | No orphan class references to deleted packages | No `ClassNotFoundException` or `NoSuchBeanDefinitionException` in test output |

---

## Done Criteria

- [ ] `./mvnw test` exits 0
- [ ] Surefire report: 0 failures, 0 errors
- [ ] `HexagonalArchitectureTest` appears in the test output with all 4 tests passing
- [ ] No `import cl.gradeops.ai.api.common.*`, `import cl.gradeops.ai.api.config.*`, `import cl.gradeops.ai.api.email.*`, or `import cl.gradeops.ai.api.security.*` remain in any `src/` file (confirmed by the grep checks from tasks 03–07)
- [ ] Story 01 status can be set to DONE
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-01-shared-kernel.md)
