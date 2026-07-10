# ⚛️ TASK 05 — Assessment draft generation unit tests

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-03
> [← story file](../story-01-assessment-agent.md)

---

## Objective

Unit test the three collaborators task-03 introduced — `GenerateAssessmentDraftHandler`, `AssessmentAgentOrchestrator`, `GeminiAssessmentGenerationAdapter` — covering valid generation, regeneration with adjustment notes, malformed-output rejection, and the audit payload's completeness on both success and failure.

---

## Technical Design

- **Approach:** three separate test classes, one per collaborator, matching `10-testing-calidad-y-automatizacion.md`'s pyramid (application/usecase tests use fakes/mocks for their one collaborator each; the adapter test validates the Spring AI integration mapping). No `@SpringBootTest` for any of them — `AssessmentAgentOrchestrator` and `GenerateAssessmentDraftHandler` have no Spring wiring dependency worth bootstrapping a context for; `GeminiAssessmentGenerationAdapter`'s test mocks `ChatClient`'s builder chain directly. All three use `@ExtendWith(MockitoExtension.class)` (`10-testing-calidad-y-automatizacion.md` — never `MockitoAnnotations.openMocks(this)`), strict Given-When-Then structure with comments, `should...When...` test names, and the exhaustive-assertions strategy (not-null → attributes not-null → expected values → state) for every test whose method returns an object.
- **Affected files / components:**
  - `application/usecase/GenerateAssessmentDraftHandlerTest.java`
  - `application/orchestrator/AssessmentAgentOrchestratorTest.java`
  - `infrastructure/adapter/out/gemini/GeminiAssessmentGenerationAdapterTest.java`
  - all under `src/test/java/cl/gradeops/ai/agents/assessment/...`, mirroring the main-source packages exactly.
- **Interfaces / contracts:** none new — test code only.
- **Risk:** Low — routine test-writing; main risk is over-mocking hiding real integration issues, mitigated by task-03's smoke check 4 already exercising one real Gemini call by hand, and by `GeminiAssessmentGenerationAdapterTest` existing as its own adapter-tier test rather than being folded into the orchestrator test.
- **Design notes:** `AssessmentAgentOrchestratorTest` mocks `AssessmentGenerationPort` (not `ChatClient`) — the orchestrator no longer touches Spring AI types after task-03's port/adapter split, so its test doesn't either. `GenerateAssessmentDraftHandlerTest` mocks `AssessmentAgentOrchestrator` and only asserts the delegation happens — it must not duplicate the orchestrator's own test cases.

---

## Implementation Steps

1. Create `GenerateAssessmentDraftHandlerTest.java`:
   - Mock `AssessmentAgentOrchestrator`. Test: `execute(command)` delegates to `orchestrator.generate(command)` and returns its result unchanged (`verify` the call, assert the returned reference).
2. Create `AssessmentAgentOrchestratorTest.java`:
   - Mock `AssessmentGenerationPort`.
   - Test: valid `AssessmentCommand` → mocked port returns a complete `AssessmentGenerationResponse` → `generate()` returns a fully-populated `AssessmentExecutionOutcome` (exhaustive assertions on both `result` and every `AgentExecutionLogPayload` field, `status="COMPLETED"`, `errorCode` null).
   - Test: `AssessmentCommand` with `adjustmentNotes`/`previousDraftId`/`previousDraft` all set → assert the rendered prompt passed to the mocked port differs from the no-adjustment case and contains the `previousDraft` content (`ArgumentCaptor` on the prompt string).
   - Test: mocked port returns a response whose `AssessmentResult` is missing a required field → `generate()` throws `AssessmentAgentException(MALFORMED_OUTPUT)`; assert the exception's attached `AgentExecutionLogPayload` has `status="FAILED"` and `errorCode="MALFORMED_OUTPUT"`.
   - Test: `AssessmentCommand` with a blank required field → `generate()` throws `AssessmentAgentException(INVALID_COMMAND)` and the mocked port is never invoked (`verifyNoInteractions`); assert the exception's log payload is present and `FAILED`.
   - Test: `AssessmentCommand` with an incomplete regeneration triple (e.g. `adjustmentNotes` set but `previousDraftId`/`previousDraft` absent, or any other partial combination) → same `INVALID_COMMAND` assertions as above.
3. Create `GeminiAssessmentGenerationAdapterTest.java`:
   - Mock the `ChatClient.Builder`/`ChatClient`/`ChatClientRequestSpec` chain per Spring AI's standard test pattern.
   - Test: a rendered prompt produces a mapped `AssessmentResult` plus `modelName`/token counts read from `ChatResponse` metadata.
   - Test: when usage metadata is unavailable on the `ChatResponse`, token fields come back `null` rather than throwing.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | All handler delegation cases pass | `./mvnw -Pbeta test -Dtest=GenerateAssessmentDraftHandlerTest` |
| 2 | All orchestrator pipeline cases pass | `./mvnw -Pbeta test -Dtest=AssessmentAgentOrchestratorTest` |
| 3 | All adapter mapping cases pass | `./mvnw -Pbeta test -Dtest=GeminiAssessmentGenerationAdapterTest` |
| 4 | No real network/Gemini call is made | Test run succeeds with no `GOOGLE_AI_API_KEY` set in the test environment |

### Software Smoke Test Check

N/A — test-only task, no new runtime surface. The existing `./mvnw -Pbeta test` full run is the smoke check for this task.

### Database / ORM Consistency Check

N/A — no database or ORM involved.

---

## Done Criteria

- [ ] `GenerateAssessmentDraftHandlerTest` covers delegation.
- [ ] `AssessmentAgentOrchestratorTest` covers: valid generation, regeneration prompt difference, malformed-output rejection (with failure log payload), invalid-command rejection without a port call (both the blank-field and mismatched-pairing cases), with a failure log payload on each rejection.
- [ ] `GeminiAssessmentGenerationAdapterTest` covers: successful mapping with metadata, and missing-metadata fallback to `null` token fields.
- [ ] Every test follows Given-When-Then with comments, `@ExtendWith(MockitoExtension.class)`, `should...When...` naming, and exhaustive assertions on any returned object (`10-testing-calidad-y-automatizacion.md`).
- [ ] `./mvnw -Pbeta test` passes with no real Gemini API key required.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)
