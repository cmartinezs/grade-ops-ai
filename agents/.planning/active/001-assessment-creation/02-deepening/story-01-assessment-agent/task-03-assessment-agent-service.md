# ⚛️ TASK 03 — AssessmentAgentService (fixed pipeline)

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02
> [← story file](../story-01-assessment-agent.md)

---

## Objective

Implement `AssessmentAgentService`, the fixed pipeline (validate command → load data → build envelope → call Gemini → validate structured output → log execution → return result) that turns an `AssessmentCommand` into an `AssessmentResult` plus execution-log metadata.

---

## Technical Design

- **Approach:** single service class orchestrating the whole pipeline using Spring AI's `ChatClient` (built from the auto-configured `ChatModel` bean supplied by `spring-ai-starter-model-google-genai`, active in the `beta`/`demo` Maven profiles) and the `.st` template from task-02, rendered via ST4. Structured output is obtained via Spring AI's entity-mapping (`.call().entity(AssessmentResult.class)`), then explicitly re-validated for required-field completeness before returning — Spring AI's entity mapping can still yield a partially-populated object if the model omits fields. Schema validation and execution-log capture are implemented **inside this same service**, not as separate tasks, because they are non-optional steps of one pipeline: validation only makes sense wired directly into the call, and the log payload only makes sense captured around the actual call — splitting them would create two tasks that cannot be independently verified.
- **Affected files / components:** `AssessmentAgentService.java`, `AssessmentAgentException.java` (thrown on invalid command or malformed output), `AgentExecutionLogPayload.java`, `AssessmentExecutionOutcome.java`.
- **Interfaces / contracts:** `AssessmentAgentService.generate(AssessmentCommand command) -> AssessmentExecutionOutcome`, where `AssessmentExecutionOutcome(AssessmentResult result, AgentExecutionLogPayload log)` bundles both — this is what task-04's endpoint returns to `api/`. `AgentExecutionLogPayload(String model, Double costEstimate, String status, Instant startedAt, Instant finishedAt)`.
- **Risk:** M — Gemini structured output malformed/incomplete (story-level risk R-01); mitigated by explicit post-call field-completeness validation that throws `AssessmentAgentException` rather than returning a partial `AssessmentResult`.
- **Design notes:** cost estimate is not returned directly by the Gemini API — compute it from token usage metadata (`ChatResponse.getMetadata().getUsage()`) times a per-model rate constant; if usage metadata is unavailable, record a `null` cost and reflect that in the log status rather than failing the whole call.

---

## Implementation Steps

1. Create `AgentExecutionLogPayload.java` (record: `String model`, `Double costEstimate`, `String status`, `Instant startedAt`, `Instant finishedAt`).
2. Create `AssessmentExecutionOutcome.java` (record: `AssessmentResult result`, `AgentExecutionLogPayload log`).
3. Create `AssessmentAgentException.java` (runtime exception carrying a reason code: `INVALID_COMMAND`, `MALFORMED_OUTPUT`).
4. Create `AssessmentAgentService.java`:
   - Constructor-inject `ChatClient.Builder` and build a `ChatClient` from it — no field injection.
   - `validate(AssessmentCommand)` — reject blank `learningGoal`/`topic`/`level`/`duration`/`language`, and reject a mismatched `adjustmentNotes`/`previousDraftId` pairing (one present without the other); throw `AssessmentAgentException(INVALID_COMMAND)` for either case. (Moved here from task-01: `AssessmentCommand` itself does not validate — see its Javadoc.)
   - Load and cache the parsed `assessment-generation.st` template once (constructor or `@PostConstruct`), not per call.
   - `buildEnvelope(AssessmentCommand)` — render the cached template with the command's attributes.
   - `callGemini(String renderedPrompt)` — `chatClient.prompt(renderedPrompt).call()`, capture the `ChatResponse`, then map to `AssessmentResult` via `.entity(AssessmentResult.class)`.
   - `validateOutput(AssessmentResult)` — reject if any required field is blank/empty; throw `AssessmentAgentException(MALFORMED_OUTPUT)` otherwise.
   - `buildLog(ChatResponse, status, startedAt, finishedAt)` — compute `AgentExecutionLogPayload`, reading the model name from `ChatResponse` metadata when available, falling back to the injected `${spring.ai.google.genai.chat.options.model}` property.
   - `generate(AssessmentCommand)` — orchestrates all of the above in the fixed pipeline order, returns `AssessmentExecutionOutcome`.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Valid command produces a fully-populated `AssessmentResult` | Unit test (task-05) with mocked `ChatClient` returning a complete valid JSON payload |
| 2 | Invalid command is rejected before calling Gemini | Unit test (task-05) asserting no `ChatClient` call happens for a blank `learningGoal` |
| 3 | Malformed/incomplete model output is rejected, not returned | Unit test (task-05) with mocked `ChatClient` returning JSON missing a required field |
| 4 | Regeneration path renders the `adjustmentNotes` branch of the template | Unit test (task-05) asserting the rendered prompt differs when `adjustmentNotes` is present |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | None required beyond Gemini itself — no other external service |
| 2 | App compiles and starts | `./mvnw -Pbeta spring-boot:run` (requires `GOOGLE_AI_API_KEY`, `AI_MODEL_NAME` env vars) starts without errors |
| 3 | Connectivity or schema validation succeeds | N/A — no DB; confirm the `ChatModel`/`ChatClient` beans are present in the Spring context under the `beta` profile |
| 4 | Changed surface responds correctly | Manually invoke `AssessmentAgentService.generate(...)` once against a real `GOOGLE_AI_API_KEY` (this is the first real Gemini call in the project) and confirm a valid `AssessmentResult` comes back — capture the response in the task's PR description as evidence |
| 5 | No startup or migration regressions are visible | `./mvnw -Pbeta test` passes fully |

### Database / ORM Consistency Check

N/A — no database or ORM involved.

---

## Done Criteria

- [ ] `AssessmentAgentService.generate(AssessmentCommand)` returns a fully-populated `AssessmentResult` for a valid command, verified once against a real Gemini call.
- [ ] Invalid `AssessmentCommand` (blank required field) is rejected before any Gemini call.
- [ ] Malformed/incomplete Gemini output is rejected with `AssessmentAgentException(MALFORMED_OUTPUT)`, never returned as a partial result.
- [ ] `AgentExecutionLogPayload` is populated (model, cost estimate or explicit null, status, timestamps) for every call, success or failure.
- [ ] Regeneration (`adjustmentNotes` present) renders a visibly different prompt than initial generation.
- [ ] `./mvnw -Pbeta test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)
