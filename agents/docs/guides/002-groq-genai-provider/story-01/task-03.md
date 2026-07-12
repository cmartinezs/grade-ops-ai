# Provider/model selection (Strategy pattern), default Groq

**Source:** task-03-provider-selection | **Area:** unknown | **Date:** 2026-07-12

## What it does

A request to `POST /internal/agents/assessment` can specify which provider (and optionally model) to use; when omitted, it defaults to Groq. Both `GeminiAssessmentGenerationAdapter` and `GroqAssessmentGenerationAdapter` are registered as named beans and selected at runtime through a Strategy-style resolver — adding a third provider later means adding one more named `@Bean`, no resolver changes. `AgentExecutionLogPayload.costEstimate` is computed with the resolved provider's own per-1K-token rate, not the single Gemini-only constant that existed before this task.

## How to use it

- Send `provider` in the request body to pick a provider explicitly (`"gemini"` or `"groq"`); omit it to use the configured default (`groq`). An unrecognized value is rejected with `422` (`AssessmentAgentException(INVALID_COMMAND)`) before any model call is attempted.
- Send `model` to override the resolved provider's configured default model for that one call — it is forwarded as a per-call `ChatOptions` override to the underlying `ChatClient`, not merely accepted and ignored. It is not validated against a list of models the provider actually supports: an unrecognized value surfaces as whatever error that provider returns.
- `AssessmentGenerationPortSelector` (`application.port.out`) is the resolver: constructed with a `Map<String, AssessmentGenerationPort>` (Spring populates this from every bean name registered in `AssessmentConfig` — `"gemini"`, `"groq"`) and the configured default provider (`app.agents.llm.default-provider` in `application.yml`). `resolve(String)` returns a `SelectedProvider(name, port)` record; `supports(String)` lets the orchestrator reject an unrecognized provider before generation is attempted.
- `AgentExecutionLogPayload.costEstimate` reads `app.agents.llm.cost-per-1k-tokens.<provider>` for the resolved provider's own rate (`gemini: 0.000075`, `groq: 0.0` for its free tier).

## Example

```bash
# Default provider (Groq), default model
curl -s http://localhost:8081/internal/agents/assessment \
  -H "X-Internal-Key: ${INTERNAL_API_SECRET}" \
  -H "Content-Type: application/json" \
  -d '{"learningGoal": "...", "topic": "...", "level": "introductory", "duration": "60 minutes", "language": "Java"}'

# Explicit provider and model override
curl -s http://localhost:8081/internal/agents/assessment \
  -H "X-Internal-Key: ${INTERNAL_API_SECRET}" \
  -H "Content-Type: application/json" \
  -d '{"learningGoal": "...", "topic": "...", "level": "introductory", "duration": "60 minutes", "language": "Java", "provider": "groq", "model": "llama-3.1-8b-instant"}'
```
