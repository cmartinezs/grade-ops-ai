# ⚛️ TASK 05 — AssessmentAgentService unit tests

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-03
> [← story file](../story-01-assessment-agent.md)

---

## Objective

Unit test `AssessmentAgentService` with a mocked `ChatClient`, covering valid generation, regeneration with adjustment notes, and malformed-output rejection.

---

## Technical Design

- **Approach:** mock `ChatClient` (or its underlying builder chain) with Mockito, avoiding any real Gemini call — matches the existing `test` Spring profile, which already excludes the real Google GenAI autoconfiguration (`src/test/resources/application-test.yml`). Prefer a plain unit test (no `@SpringBootTest`) since `AssessmentAgentService` has no other Spring wiring dependency worth bootstrapping the full context for — construct it directly with a mocked `ChatClient.Builder`.
- **Affected files / components:** `AssessmentAgentServiceTest.java` (new, under `src/test/java/cl/gradeops/ai/agents/assessment/`).
- **Interfaces / contracts:** none new — test code only.
- **Risk:** Low — routine test-writing; main risk is over-mocking hiding real integration issues, mitigated by task-03's smoke check 4 already exercising one real Gemini call by hand.
- **Design notes:** none beyond the mocking approach above.

---

## Implementation Steps

1. Create `AssessmentAgentServiceTest.java`.
2. Test: valid `AssessmentCommand` → mocked `ChatClient` returns a complete valid JSON → `generate()` returns a fully-populated `AssessmentExecutionOutcome`.
3. Test: `AssessmentCommand` with `adjustmentNotes` set → assert the rendered prompt passed to the mocked `ChatClient` differs from the no-`adjustmentNotes` case (via `ArgumentCaptor` on the prompt string).
4. Test: mocked `ChatClient` returns JSON missing a required `AssessmentResult` field → `generate()` throws `AssessmentAgentException(MALFORMED_OUTPUT)`.
5. Test: `AssessmentCommand` with a blank required field → `generate()` throws `AssessmentAgentException(INVALID_COMMAND)` and the mocked `ChatClient` is never invoked (`verifyNoInteractions`).

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | All 4 test cases above exist and pass | `./mvnw -Pbeta test -Dtest=AssessmentAgentServiceTest` |
| 2 | No real network/Gemini call is made | Test run succeeds with no `GOOGLE_AI_API_KEY` set in the test environment |

### Software Smoke Test Check

N/A — test-only task, no new runtime surface. The existing `./mvnw -Pbeta test` full run is the smoke check for this task.

### Database / ORM Consistency Check

N/A — no database or ORM involved.

---

## Done Criteria

- [ ] `AssessmentAgentServiceTest` covers: valid generation, regeneration prompt difference, malformed-output rejection, invalid-command rejection without a Gemini call.
- [ ] `./mvnw -Pbeta test` passes with no real Gemini API key required.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)
