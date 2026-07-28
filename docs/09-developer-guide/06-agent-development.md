# Agent Development Guide

This guide covers how to implement AI agents in the `agents/` repository. It assumes familiarity with Spring Boot and the agent execution pattern described in [`docs/04-architecture/system-architecture.md`](../04-architecture/system-architecture.md).

> **Status:** `agents/` has a real Assessment Agent vertical slice. It exposes `POST /internal/agents/assessment`, uses versioned prompt resources, supports Gemini and Groq provider adapters, validates structured output, and returns an execution payload to `api/` for persistence. The generic runtime (`AgentDefinition`, registry, tool loop, `AgentRun`/`AgentStep`, async/cancel/resume) is still planned and must be introduced only when a functional release creates a real consumer.

---

## The agent runtime

The `agents/` repo is a separate Spring Boot 4 + Java 21 application from `api/`. It is called by the `api/` service via internal HTTP. It is never exposed to the public internet.

In production (Cloud Run), the `api/` service calls `agents/` using service-to-service OIDC authentication — Cloud Run injects an identity token automatically, and the agents service verifies it. In local development, no auth token is required (plain HTTP between localhost processes).

The base Java package for all agent code is `cl.gradeops.ai.agents`.

The API remains the authority for domain state, persistence, billing, publication and approvals. The agents service receives commands, calls allowed providers/tools, validates output, and returns `{Agent}Result` plus execution metadata.

---

## Current runtime pattern

Every implemented agent in GradeOps AI follows the same execution sequence. The steps should be enforced by code structure, not by convention alone:

1. **Validate command** — check that all required inputs are present and internally consistent. Throw a descriptive exception early if the command is invalid.
2. **Load or receive data** — use domain data passed by the API. If a future agent needs lookup tools, expose those as explicit allowed tools rather than direct persistence access.
3. **Build envelope** — assemble the full prompt context as a structured object. Avoid PII in the envelope; reference IDs rather than names or emails where possible.
4. **Resolve provider/model** — use the provider/model policy (`gemini` and `groq` today) instead of hardcoding a single model.
5. **Call provider** — use Spring AI `ChatClient` through an adapter/port.
6. **Validate structured output** — parse and validate required fields, numeric bounds, schema shape and domain-safe constraints.
7. **Return result and execution payload** — return `{Agent}Result` plus provider/model/tokens/cost/status/error metadata. `api/` persists the final `AgentExecutionLog`.

For R04 and later Closed-assessment authoring, this wrapper can grow into a controlled tool loop: model proposes `AgentAction`, runtime checks policy/budget, tool executor returns observation, model continues, and the runtime eventually returns `Finish` or `Block`.

---

## Naming conventions

| Artifact | Pattern | Example |
|----------|---------|---------|
| Command DTO | `{AgentName}Command` | `RubricCommand` |
| Result DTO | `{AgentName}Result` | `RubricResult` |
| Service class | `{AgentName}AgentService` | `RubricAgentService` |
| Internal REST controller | `{AgentName}AgentController` | `RubricAgentController` |
| Prompt template file | `{agent-name}.st` or `{agent-name}-{operation}.st` | `assessment-generation.st` |
| Java package | `cl.gradeops.ai.agents.{agentname}` | `cl.gradeops.ai.agents.rubric` |

---

## Prompt templates

Prompts are StringTemplate (`.st`) files located in:

```
agents/src/main/resources/prompts/
```

**Never inline prompt text in Java code.** Prompts are versioned content, not code. Keeping them in resource files makes them reviewable, diffable, and changeable without recompilation.

Template variables use `$variableName$` syntax. Example template fragment for the Assessment Agent:

```
You are an expert programming educator assistant for GradeOps AI.

Generate a structured assessment draft for the following context:

Learning goal: $learningGoal$
Topic: $topic$
Target level: $level$
Programming language: $language$
Estimated duration: $duration$
Adjustment notes: $adjustmentNotes$
Previous draft: $previousDraft$

$formatInstructions$
```

Template variables to use by agent:

| Agent | Key template variables |
|-------|----------------------|
| Assessment | `$learningGoal$`, `$topic$`, `$level$`, `$language$`, `$duration$`, `$adjustmentNotes$`, `$previousDraft$` |
| Rubric | `$assessmentTitle$`, `$learningObjectives$`, `$expectedEvidence$`, `$programmingLanguage$`, `$targetLevel$`, `$preferredScoringScale$` |
| Grading | `$submissionContent$`, `$rubricCriteria$`, `$assessmentInstructions$`, `$programmingLanguage$` |
| Feedback | `$criteriaResults$`, `$evidenceSummaries$`, `$studentIdentifier$` |

Load templates via Spring's `ResourceLoader`:

```java
@Value("classpath:prompts/assessment-generation.st")
private Resource assessmentPromptTemplate;
```

---

## Current Assessment Agent baseline

The current Assessment Agent implementation is the baseline for new agents:

| Concern | Current artifact |
|---|---|
| Internal endpoint | `agents/src/main/java/.../assessment/infrastructure/adapter/in/web/AssessmentController.java` |
| Command | `assessment/application/command/AssessmentCommand.java` |
| Orchestrator | `assessment/application/orchestrator/AssessmentAgentOrchestrator.java` |
| Provider port | `assessment/application/port/out/AssessmentGenerationPort.java` |
| Provider selector | `assessment/application/port/out/AssessmentGenerationPortSelector.java` |
| Gemini adapter | `assessment/infrastructure/adapter/out/gemini/GeminiAssessmentGenerationAdapter.java` |
| Groq adapter | `assessment/infrastructure/adapter/out/groq/GroqAssessmentGenerationAdapter.java` |
| Prompt | `agents/src/main/resources/prompts/assessment-generation.st` |
| Execution payload | `assessment/application/result/AgentExecutionLogPayload.java` |

New agents should copy the same boundary style: command/result records in application, provider/tool ports at the boundary, infrastructure adapters behind those ports, and explicit configuration wiring. Do not introduce persistence repositories inside `agents/`.

## Adding the next agent

When adding a new agent, start from the current Assessment Agent shape rather than from a standalone service skeleton:

1. Create `{Agent}Command`, `{Agent}Result` and any nested result records in the agent application package.
2. Add an application orchestrator that validates the capability-oriented command and workflow budget, builds the envelope, resolves an allowlisted route through the Model Router, calls a provider/tool port, validates output, and builds an execution payload.
3. Add provider/tool ports under `application/port/out`.
4. Add infrastructure adapters behind those ports.
5. Add an internal controller only if the API needs a synchronous endpoint for that agent.
6. Return result + execution payload to `api`; do not persist domain entities in `agents`.
7. Add tests for command/budget validation, routing precedence, forbidden override, fallback limits, output validation, failure payloads, and prompt rendering.

Extract shared abstractions only when the second or third agent creates real duplication.

## Spring AI provider configuration

Current `beta` and `demo` profiles configure both Google GenAI / Gemini and OpenAI-compatible Groq. The generic `ChatClient` auto-configuration is excluded because two `ChatModel` beans are present; `AssessmentConfig` builds provider-specific `ChatClient` instances from the named model beans.

```yaml
spring:
  ai:
    google:
      genai:
        project-id: ${GOOGLE_CLOUD_PROJECT}
        location: ${VERTEX_AI_LOCATION:us-central1}
        chat:
          options:
            model: ${GRADEOPS_GEMINI_MODEL}
    openai:
      api-key: ${GRADEOPS_GROQ_API_KEY}
      base-url: ${GRADEOPS_GROQ_BASE_URL:https://api.groq.com/openai/v1}
      chat:
        options:
          model: ${GRADEOPS_GROQ_MODEL}
    autoconfigure:
      exclude:
        - org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration
```

The current default provider is configured separately:

```yaml
app:
  agents:
    llm:
      default-provider: groq
```

Provider defaults are compatibility preferences, not the normal routing contract. See [`Policy-Based Provider And Model Routing`](../99-decisions/2026-07-27-policy-based-model-routing.md).

Provider-specific clients are wired explicitly:

```java
@Bean(name = "gemini")
GeminiAssessmentGenerationAdapter geminiAssessmentGenerationAdapter(
        @Qualifier("googleGenAiChatModel") ChatModel chatModel) {
    return new GeminiAssessmentGenerationAdapter(ChatClient.builder(chatModel).build());
}

@Bean(name = "groq")
GroqAssessmentGenerationAdapter groqAssessmentGenerationAdapter(
        @Qualifier("openAiChatModel") ChatModel chatModel) {
    return new GroqAssessmentGenerationAdapter(ChatClient.builder(chatModel).build());
}
```

---

## Structured output validation

Spring AI structured output can be used inside provider adapters, but the agent contract must not depend on raw LLM text. Parse the provider response into the `{Agent}Result` record and validate it before returning to `api/`.

For the current Assessment Agent, validation happens in `AssessmentAgentOrchestrator.validateOutput(...)` after the selected provider returns `AssessmentGenerationResponse`.

The Java record must use types that map cleanly to JSON: `String`, `Integer`, `Boolean`, `List<T>`, and nested records. Avoid using `Optional` in DTO contracts; use nullable fields only where the contract permits absence.

---

## Future generic runtime capabilities

Add these only when a release needs them:

| Capability | First expected need | Rule |
|---|---|---|
| `AgentDefinition` and registry | R02 | Introduce when Rubric/Grading/Feedback become real second consumers. |
| Shared model gateway | R02 | Extract from Assessment Agent only after duplication appears. |
| Tool registry/executor | R04 | Required for Closed authoring tools and coverage/bank lookups. |
| Policy engine and budget manager | R04 | Enforce allowed tools, max steps, tokens, cost, time and retries. |
| `AgentRun`/`AgentStep` persistence | R05 | Add only when async/volume/latency makes synchronous payloads insufficient. |
| Sandbox | R02 or later | Only if grading actually executes untrusted student code. |

Do not add RAG, vector memory, multi-agent orchestration or new providers without a concrete release consumer and decision record.

---

## Output validation rules

After parsing structured output, apply these checks before returning the execution payload:

| Check | Action on failure |
|-------|------------------|
| Required fields are non-null | Throw agent-specific exception with `OUTPUT_VALIDATION_FAILED` or equivalent reason |
| Numeric bounds (scores 0-100, duration > 0) | Throw agent-specific validation exception |
| List fields have at least the minimum expected items | Throw validation exception |
| JSON/schema match | Provider adapter or converter throws; orchestrator translates to normalized error |

When validation fails:

- Build a failed execution payload with redacted error metadata.
- Do not include raw prompt or raw student submission content.
- Throw the exception so the `api/` caller receives an error response and can retry or surface it to the teacher.
- Let `api/` persist the failed `AgentExecutionLog`.

---

## AgentExecutionLog payload — what to return

Return execution evidence on every agent run, including failures. `api/` persists the final row and attaches tenant/assessment/resource context.

| Field | Value to provide |
|-------|----------------|
| `agentExecutionId` | Generated UUID for correlation |
| `agentName` | Agent name, e.g., `assessment` |
| `provider` | Provider name, e.g., `gemini` or `groq`; target field even if the current payload still needs to add it |
| `model` | Provider-specific model string |
| `promptVersion` | Prompt template version/header |
| `inputHash` | Hash of rendered prompt or compact input envelope; never raw prompt |
| `outputHash` | Hash of raw provider response when available |
| `estimatedInputTokens` | Token count if available |
| `estimatedOutputTokens` | Token count if available |
| `costEstimate` | Provider-aware best-effort estimate; `null` when unknown |
| `status` | `COMPLETED`, `FAILED`, `BLOCKED` or future normalized state |
| `errorCode` | Normalized reason for failure/block |
| `startedAt` | Start timestamp |
| `finishedAt` | Finish timestamp |

The current `AgentExecutionLogPayload` does not yet include `provider`; that is a required alignment item from the 2026-07-20 provider/model policy.

---

## Security rules for agents

- **Never log raw submission content.** Log the `submissionId` reference or a hash. If a brief content summary is needed for debugging, keep it short and redact student identifiers.
- **Never log teacher PII** beyond what is necessary for correlation; prefer IDs supplied by `api/`.
- **Always return token cost and latency when available.** These fields are required for billing evidence and unit economics tracking.
- **No secrets in prompts.** Never include API keys, database credentials, or internal system URLs in prompt templates.
- **Provider secrets are server-side only.** Gemini and Groq keys belong in server-side config/Secret Manager, never in frontend code.
- **Agents service is internal.** In `demo`, API-to-agents should use Cloud Run service-to-service auth; in local/beta, use the documented internal/shared-secret mechanism until OIDC is available.
- **Tool use must be policy-gated.** When tool loops arrive, every tool must declare input schema, allowed agent(s), max use, timeout, side-effect level and audit fields.

---

## Local development with providers

Use the profile appropriate to the environment. Current provider env vars:

```bash
GRADEOPS_GEMINI_MODEL=...
GRADEOPS_GROQ_API_KEY=...
GRADEOPS_GROQ_BASE_URL=https://api.groq.com/openai/v1
GRADEOPS_GROQ_MODEL=llama-3.3-70b-versatile
```

`app.agents.llm.default-provider` is the current compatibility fallback and a future routing preference. Normal commands request capabilities and provide an authorized budget; they do not choose an exact provider/model. Exact overrides are restricted to internal development, benchmarking, incident replay, controlled experiments, or contractually constrained tenants and must be permission-gated and audited.

To start the agents service locally:

```bash
cd agents/
./mvnw spring-boot:run -Dspring.profiles.active=beta
```

Do not commit `.env`, local secrets or provider API keys.

---

## Thirteen agents reference

The complete agent set across both assessment modes:

**Open assessment (8 agents):**
1. `assessment` — generates assessment draft from learning goal
2. `rubric` — creates and validates scoring rubric
3. `grading` — analyzes submissions against approved rubric
4. `feedback` — drafts student-facing feedback
5. `learning_gap` — detects cohort gaps from graded submissions
6. `recovery` — suggests remedial activities per gap
7. `teacher_report` — summarizes the assessment cycle
8. `ops_evidence` — records usage, cost, and business evidence

**Closed assessment (5 agents):**
9. `question_generation` — generates TF/SC/MC questions with alternatives and answer key
10. `distractor_quality` — evaluates and flags weak or biased distractors
11. `ambiguity_review` — detects double-valid answers and interpretation problems
12. `assessment_assembly` — composes a balanced assessment from approved bank questions
13. `item_analytics` — analyzes post-assessment item performance

For detailed input/output contracts and quality rules for each agent, see the corresponding files in [`docs/03-ai-agents/`](../03-ai-agents/).

---

<!-- nav -->

← [05-database-guide.md](05-database-guide.md) | [↑ Top](#agent-development-guide) | [Web Development →](07-web-development.md)
