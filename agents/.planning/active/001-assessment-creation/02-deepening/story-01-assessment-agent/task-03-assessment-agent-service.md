# ⚛️ TASK 03 — Assessment draft generation (orchestrator, port, adapter)

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02
> [← story file](../story-01-assessment-agent.md)

---

## Objective

Implement the fixed pipeline (validate command → load data → build envelope → call Gemini → validate structured output → log execution → return result) that turns an `AssessmentCommand` into an `AssessmentExecutionOutcome`, following the project's Nivel-2 use-case pattern (`03-use-cases-orquestadores-y-pasos.md`): an explicit `UseCase` port, a thin `Handler`, an `Orchestrator` that runs the pipeline, and the Gemini call isolated behind a dedicated port/adapter (`01-arquitectura-hexagonal-y-paquetes.md`'s own `agents/` example: `...agents.grading.infrastructure.adapter.out.gemini`).

---

## Technical Design

- **Approach:** the pipeline is modeled as a Nivel-2 orchestrated use case, not a single monolithic service:
  - `GenerateAssessmentDraftUseCase` (port in) — the contract `AssessmentController` (task-04) depends on.
  - `GenerateAssessmentDraftHandler` (application.usecase) — thin, delegates to the orchestrator. No business logic.
  - `AssessmentAgentOrchestrator` (application.orchestrator) — runs the fixed pipeline: `validate` → render prompt (ST4, from task-02) → call `AssessmentGenerationPort` → `validateOutput` → build `AgentExecutionLogPayload` → assemble `AssessmentExecutionOutcome`.
  - `AssessmentGenerationPort` (application.port.out) — abstracts "generate a structured assessment from a rendered prompt"; the orchestrator never touches Spring AI's `ChatClient` directly.
  - `GeminiAssessmentGenerationAdapter` (infrastructure.adapter.out.gemini) — the only class holding `ChatClient`; implements the port, does `.call().entity(AssessmentResult.class)`, reads `ChatResponse` usage metadata for token counts.
  - Prompt rendering (ST4) is **not** behind a port — it's pure local templating with no external/swappable dependency, so isolating it would be ceremonial (`00-principios-rectores.md` #6, KISS). It stays inside the orchestrator, loaded/cached once (constructor or `@PostConstruct`).
- **Affected files / components:**
  - `application/port/in/GenerateAssessmentDraftUseCase.java`
  - `application/usecase/GenerateAssessmentDraftHandler.java`
  - `application/orchestrator/AssessmentAgentOrchestrator.java`
  - `application/port/out/AssessmentGenerationPort.java`
  - `application/port/out/AssessmentGenerationResponse.java` (port's return shape — see below)
  - `application/exception/AssessmentAgentException.java`
  - `application/result/AgentExecutionLogPayload.java`
  - `application/result/AssessmentExecutionOutcome.java`
  - `infrastructure/adapter/out/gemini/GeminiAssessmentGenerationAdapter.java`
  - `infrastructure/config/AssessmentConfig.java` (registers the four feature beans below via `@Bean` — no `@Service`/`@Component`, per `01-arquitectura-hexagonal-y-paquetes.md`'s "única anotación de estereotipo permitida: `@RestController`")
  - all under `cl.gradeops.ai.agents.assessment.*`
- **Interfaces / contracts:**
  - `GenerateAssessmentDraftUseCase.execute(AssessmentCommand) -> AssessmentExecutionOutcome`.
  - `AssessmentGenerationPort.generate(String renderedPrompt) -> AssessmentGenerationResponse`.
  - `AssessmentGenerationResponse(AssessmentResult result, String modelName, Integer estimatedInputTokens, Integer estimatedOutputTokens)` — the adapter's raw output; the orchestrator turns this into the final `AssessmentExecutionOutcome`.
  - `AssessmentExecutionOutcome(AssessmentResult result, AgentExecutionLogPayload log)`.
  - `AgentExecutionLogPayload` — expanded per `11-seguridad-observabilidad-y-auditoria.md`'s "Auditoría de agentes AI" minimum list, scoped to what `agents/` actually knows (no `tenantId`/`teacherId`/resource — `AssessmentCommand` doesn't carry tenant context; `api/` enriches those when it persists the final `AgentExecutionLog` row):
    ```java
    public record AgentExecutionLogPayload(
        UUID agentExecutionId,
        String agentName,          // constant "assessment"
        String model,
        String promptVersion,      // from assessment-generation.st's header comment (task-02)
        String inputHash,          // SHA-256 hex of the rendered prompt — never log the raw prompt
        String outputHash,         // SHA-256 hex of the raw Gemini JSON response; null if generation failed
        Integer estimatedInputTokens,
        Integer estimatedOutputTokens,
        Double costEstimate,
        String status,             // "COMPLETED" | "FAILED" — synchronous call, no "STARTED" state to report
        String errorCode,          // AssessmentAgentException.Reason name when status=FAILED, else null
        Instant startedAt,
        Instant finishedAt) {
    }
    ```
  - `AssessmentAgentException` carries its own partial `AgentExecutionLogPayload` (status `FAILED`, `outputHash`/tokens/cost null as appropriate) so task-04's exception handler can still return execution evidence to `api/` even when generation fails — evidence must never be a side-effect of the happy path only (`00-principios-rectores.md` #8):
    ```java
    public class AssessmentAgentException extends RuntimeException {
        public enum Reason { INVALID_COMMAND, MALFORMED_OUTPUT }
        // constructor(Reason reason, String message, AgentExecutionLogPayload log)
        // reason(), log() accessors
    }
    ```
- **Risk:** M — Gemini structured output malformed/incomplete (story-level risk R-01); mitigated by explicit post-call field-completeness validation that throws `AssessmentAgentException` rather than returning a partial `AssessmentResult`. Secondary risk: hashing/token-metadata extraction adds surface area — mitigated by making `inputHash` always computable (local operation) and treating `estimatedInputTokens`/`estimatedOutputTokens`/`costEstimate` as best-effort `null`-able fields when `ChatResponse` metadata is unavailable, matching the original design note.
- **Design notes:**
  - Lombok: `AssessmentAgentOrchestrator`, `GenerateAssessmentDraftHandler`, `GeminiAssessmentGenerationAdapter` use `@RequiredArgsConstructor` (constructor injection, `private final` fields, no `@Autowired`). `AgentExecutionLogPayload`, `AssessmentExecutionOutcome`, `AssessmentGenerationResponse` use `@Builder` per `07-lombok.md`.
  - No Spring/JPA/Jackson types appear in `application.*` — the adapter is the only place `ChatClient`/`ChatResponse` are imported.
  - Hashing: `java.security.MessageDigest` (`SHA-256`), no new dependency.
  - `status`/`errorCode` as `String` (not enum) matches `11-seguridad-observabilidad-y-auditoria.md`'s SQL sketch (`status varchar(40)`, `error_code varchar(120)`) — this payload is transported as JSON to `api/`, which is free to parse it into its own enum on persistence.

---

## Implementation Steps

1. Create `application/port/out/AssessmentGenerationPort.java` and `AssessmentGenerationResponse.java`.
2. Create `infrastructure/adapter/out/gemini/GeminiAssessmentGenerationAdapter.java` implementing the port: constructor-inject `ChatClient.Builder`, build the `ChatClient`; `generate(String renderedPrompt)` calls Gemini, maps to `AssessmentResult`, reads token usage from `ChatResponse` metadata.
3. Create `application/exception/AssessmentAgentException.java` with the `Reason` enum and the `log` accessor described above.
4. Create `application/result/AgentExecutionLogPayload.java` and `AssessmentExecutionOutcome.java` (both `@Builder`).
5. Create `application/orchestrator/AssessmentAgentOrchestrator.java`:
   - `validate(AssessmentCommand)` — reject blank `learningGoal`/`topic`/`level`/`duration`/`language`, and reject an inconsistent regeneration triple: `adjustmentNotes`, `previousDraftId`, and `previousDraft` must be all present or all absent — any partial combination is `INVALID_COMMAND` (this is stricter than the original two-field pairing check from task-01, extended when `previousDraft` was added to the command post-hoc — see task-01-contracts.md and `RETROSPECTIVE-RAW.md`); throw `AssessmentAgentException(INVALID_COMMAND, ...)`.
   - Load and cache the parsed `assessment-generation.st` template once (constructor or `@PostConstruct`); read its header-comment version string for `promptVersion`.
   - `buildEnvelope(AssessmentCommand)` — render the cached template, mapping every command field to its matching template attribute 1:1: `learningGoal`, `topic`, `level`, `duration`, `language` always; `adjustmentNotes` and `previousDraft` only when present (matches the template's `<if(adjustmentNotes)>` branch — `previousDraftId` is not a template attribute, it never reaches the prompt, it stays in `AgentExecutionLogPayload` for correlation only).
   - Call `assessmentGenerationPort.generate(renderedPrompt)`.
   - `validateOutput(AssessmentResult)` — reject if any required field is blank/empty; throw `AssessmentAgentException(MALFORMED_OUTPUT, ...)` (with a `FAILED` log payload attached) otherwise.
   - Build the `COMPLETED` `AgentExecutionLogPayload` (hash prompt/output, carry tokens/cost/model from the port response, generate a fresh `agentExecutionId`, record `startedAt`/`finishedAt`).
   - Assemble and return `AssessmentExecutionOutcome`.
6. Create `application/port/in/GenerateAssessmentDraftUseCase.java` (interface: `AssessmentExecutionOutcome execute(AssessmentCommand command)`).
7. Create `application/usecase/GenerateAssessmentDraftHandler.java` implementing the use case, delegating to the orchestrator — no logic beyond the delegation.
8. Create `infrastructure/config/AssessmentConfig.java` (`@Configuration`) registering `GeminiAssessmentGenerationAdapter`, `AssessmentAgentOrchestrator`, `GenerateAssessmentDraftHandler` as `@Bean`s.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Valid command produces a fully-populated `AssessmentExecutionOutcome` | Unit test (task-05) with a mocked `AssessmentGenerationPort` returning a complete valid response |
| 2 | Invalid command is rejected before calling the port | Unit test (task-05) asserting no port call happens for a blank `learningGoal` |
| 3 | Malformed/incomplete model output is rejected, not returned | Unit test (task-05) with a mocked port returning a response missing a required `AssessmentResult` field |
| 4 | Regeneration path renders the `adjustmentNotes` branch of the template | Unit test (task-05) asserting the rendered prompt differs when `adjustmentNotes` is present |
| 5 | `AgentExecutionLogPayload` is fully populated on both success and failure | Unit tests (task-05) asserting every field is non-null on the success path (except `errorCode`) and that `status`/`errorCode` are set on the failure path |
| 6 | `GeminiAssessmentGenerationAdapter` correctly maps a real `ChatResponse` | Adapter test (task-05) with a mocked `ChatClient` chain |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | None required beyond Gemini itself — no other external service |
| 2 | App compiles and starts | `./mvnw -Pbeta spring-boot:run` (requires `GOOGLE_AI_API_KEY`, `AI_MODEL_NAME` env vars) starts without errors; `AssessmentConfig` beans present in context |
| 3 | Connectivity or schema validation succeeds | N/A — no DB; confirm `ChatModel`/`ChatClient` beans and the four `AssessmentConfig` beans are present under the `beta` profile |
| 4 | Changed surface responds correctly | Manually invoke `GenerateAssessmentDraftHandler.execute(...)` once against a real `GOOGLE_AI_API_KEY` (first real Gemini call in the project) and confirm a valid `AssessmentExecutionOutcome` comes back — capture the response in the task's PR description as evidence |
| 5 | No startup or migration regressions are visible | `./mvnw -Pbeta test` passes fully |

### Database / ORM Consistency Check

N/A — no database or ORM involved.

---

## Done Criteria

- [ ] `GenerateAssessmentDraftHandler.execute(AssessmentCommand)` returns a fully-populated `AssessmentExecutionOutcome` for a valid command, verified once against a real Gemini call.
- [ ] Invalid `AssessmentCommand` (blank required field, or an incomplete `adjustmentNotes`/`previousDraftId`/`previousDraft` regeneration triple) is rejected before any Gemini call.
- [ ] Malformed/incomplete Gemini output is rejected with `AssessmentAgentException(MALFORMED_OUTPUT)`, never returned as a partial result.
- [ ] `AgentExecutionLogPayload` carries every field listed in the Interfaces/contracts section above, for both the success and failure paths.
- [ ] Regeneration (`adjustmentNotes` present) renders a visibly different prompt than initial generation.
- [ ] `ChatClient`/`ChatResponse` types appear only in `GeminiAssessmentGenerationAdapter` — nowhere else in `application.*`.
- [ ] No Spring stereotype annotation (`@Service`/`@Component`) on the orchestrator, handler, or adapter; all three are wired via `@Bean` in `AssessmentConfig`.
- [ ] `./mvnw -Pbeta test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)
