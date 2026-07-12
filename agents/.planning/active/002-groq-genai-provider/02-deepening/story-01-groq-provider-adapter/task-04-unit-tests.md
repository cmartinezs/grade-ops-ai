# ⚛️ TASK 04 — Unit tests: Groq adapter and provider selection

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02, task-03
> [← story file](../story-01-groq-provider-adapter.md)

---

## Objective

`GroqAssessmentGenerationAdapter` and `AssessmentGenerationPortSelector` have unit test coverage matching `001`'s established conventions (exhaustive assertions, no `@Mock`/manual-`mock()` mixing), and `AssessmentAgentOrchestratorTest`/`AssessmentCommandTest` are updated for the new constructor signature and `provider`/`model` fields.

**Source:** mirrors `001-assessment-creation`'s task-05 precedent — pipeline-level collaborator tests are written once the collaborators and their wiring are stable, not embedded in each implementation task.

---

## Technical Design

- **Approach:** Follow `GeminiAssessmentGenerationAdapterTest`'s structure exactly for `GroqAssessmentGenerationAdapterTest` (same `@Mock` fields for `ChatClient`/`ChatClientRequestSpec`/`CallResponseSpec`/`Usage`, same two cases: usage present, usage unavailable via `EmptyUsage`). `AssessmentGenerationPortSelectorTest` covers: resolves named provider, falls back to default when `provider` is `null`, throws a clear exception for an unrecognized provider — **including an explicit test for a blank string (`""`), which is rejected as an unrecognized provider, not treated as equivalent to `null`.** `AssessmentAgentOrchestratorTest` gets one new failure-path test (unrecognized provider → `INVALID_COMMAND`) using the existing shared failure-log assertion helper pattern from `001`.
  - **Design correction (2026-07-12, post code-review):** this bullet originally read "falls back to default when `provider` is null/blank," written at atomization time before task-03 existed. Task-03's actual, already-reviewed-and-merged `AssessmentGenerationPortSelector` only special-cases a literal `null` — a blank string is looked up as a regular (unregistered) provider name and rejected. Since task-04 is test-only scope and task-03 is already `DONE`, this bullet is corrected to match the real implementation rather than changing already-approved selector behavior under this task's cover. Whether client-submitted blank strings (a common "not specified" shape from web/DTO layers) should be normalized to `null` before reaching `AssessmentCommand` is tracked as R-02 in `TRACEABILITY.md`, not solved here.
- **Affected files / components:** new `GroqAssessmentGenerationAdapterTest.java`, new `AssessmentGenerationPortSelectorTest.java`, updated `AssessmentAgentOrchestratorTest.java`, updated `AssessmentCommandTest.java` (assert `provider`/`model` fields, including the null-defaults-to-Groq case if that logic lives in the orchestrator rather than the record itself).
- **Interfaces / contracts:** None — test-only changes.
- **Risk:** Low — routine test-writing following an already-established, reviewed pattern in this codebase.
- **Design notes:** Exhaustive-assertions strategy applies to every test, including failure-path ones (not-null result → not-null attributes → expected values → state) — this was a repeated correction point in `001`, do not repeat it here.

---

## Implementation Steps

1. Write `GroqAssessmentGenerationAdapterTest.java`, mirroring `GeminiAssessmentGenerationAdapterTest.java`'s two test cases.
2. Write `AssessmentGenerationPortSelectorTest.java` covering named resolution, default fallback, and unrecognized-provider rejection.
3. Update `AssessmentAgentOrchestratorTest.java`: adjust constructor calls for the new selector dependency, add the unrecognized-provider failure-path test with exhaustive log-payload assertions.
4. Update `AssessmentCommandTest.java` for the new `provider`/`model` fields.
5. Run `./mvnw test` and confirm the full suite passes.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | All new/updated test classes pass | `./mvnw test` |
| 2 | Failure-path assertions are exhaustive, not just status/errorCode | Manual review of test bodies against `AgentExecutionLogPayload`'s full field set |
| 3 | No `@Mock`/manual-`mock()` mixing in any test class | Manual review |

### Software Smoke Test Check

N/A — test-only task, no runtime behavior change; task-03 already covered live smoke verification.

### Database / ORM Consistency Check

N/A — no database or ORM artifacts involved.

---

## Done Criteria

- [x] `GroqAssessmentGenerationAdapterTest.java` and `AssessmentGenerationPortSelectorTest.java` created and passing.
- [x] `AssessmentAgentOrchestratorTest.java` and `AssessmentCommandTest.java` updated and passing.
- [x] `./mvnw test` passes in full.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] TRACEABILITY.md updated with new terms from this story.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-groq-provider-adapter.md)
