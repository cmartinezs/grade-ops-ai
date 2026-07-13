# agentclient module

**Source:** task-05-agentclient | **Area:** AP | **Date:** 2026-07-13

## What it does

The `agentclient` module (`cl.gradeops.ai.api.agentclient`) is the only part of `api/` allowed to call `agents/`. It sends an `AssessmentCommand`-shaped request to the internal agent endpoint and maps the response back to a structured `AssessmentAgentResponse` (the generated draft plus execution-log metadata), or to a typed `AgentClientException` when the call fails.

## How to use it

Inject `AssessmentAgentClient` (wired via `AgentClientConfig`, which exposes both the underlying `RestClient` and the client bean) and call:

```java
AssessmentAgentResponse response = assessmentAgentClient.generate(command);
```

- `AssessmentCommand` — mirrors `agents/`'s contract: `learningGoal`, `topic`, `level`, `duration`, `language` (required for initial generation); `adjustmentNotes`, `previousDraftId`, `previousDraft` (regeneration only, all three present together or all absent); `provider`, `model` (optional per-request overrides, `null` uses `agents/`'s configured defaults).
- `AssessmentAgentResponse` — `result` (title, context, instructions, objectives, deliverables, constraints) and `log` (the full 13-field execution-evidence payload: `agentExecutionId`, `agentName`, `model`, `promptVersion`, `inputHash`, `outputHash`, `estimatedInputTokens`, `estimatedOutputTokens`, `costEstimate`, `status`, `errorCode`, `startedAt`, `finishedAt`).
- On failure, `generate()` throws `AgentClientException` with a `reason()`: `UNREACHABLE` (no response — connection refused, DNS failure, timeout), `AGENT_REJECTED` (4xx from `agents/`), or `AGENT_ERROR` (5xx from `agents/`). Callers should catch this rather than a raw `RestClientException`.
- Every call generates a fresh `X-Correlation-Id` header and sends the configured `X-Internal-Key` shared-secret header automatically — callers don't need to set either.
- Configure the target with `app.agents.base-url` (env `AGENTS_BASE_URL`, defaults to `http://localhost:8081` for local dev) and reuse the existing `app.internal.secret` (env `INTERNAL_API_SECRET`).

## Example

```java
AssessmentCommand command = new AssessmentCommand(
    "Evaluate loop control flow", "Java loops", "basic", "90min", "Java",
    null, null, null, null, null); // initial generation, default provider/model

try {
    AssessmentAgentResponse response = assessmentAgentClient.generate(command);
    // response.result().title(), response.log().status(), ...
} catch (AgentClientException ex) {
    switch (ex.reason()) {
        case UNREACHABLE -> // agents/ unreachable — surface a retry-later error
        case AGENT_REJECTED -> // invalid command — surface a validation error
        case AGENT_ERROR -> // agents/ failed processing — surface a generic failure
    }
}
```
