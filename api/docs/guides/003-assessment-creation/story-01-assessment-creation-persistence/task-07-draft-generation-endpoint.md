# Draft generation endpoint (+ AgentExecutionLog)

**Source:** task-07-draft-generation-endpoint | **Area:** AP | **Date:** 2026-07-13

## What it does

`POST` endpoint that builds an `AssessmentCommand` from the persisted brief, calls `agents/` via `agentclient`, and persists the returned draft (version 1) together with its `AgentExecutionLog` — the first-class evidence record of the agent call (US-011). `AgentExecutionLog` is a first-class entity per `CLAUDE.md`, kept as its own table so it's reusable by future agents beyond Assessment.

## How to use it

`POST /api/v1/assessments/{id}/draft` — no request body (the brief is already persisted from task-06). Requires an authenticated teacher who owns the assessment; a mismatched or non-existent assessment id returns `404`.

On success, returns `201 Created` with the generated draft:

```json
{
  "draftId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "title": "Java Loops Quiz",
  "context": "Second-semester students",
  "instructions": "Solve the following...",
  "objectives": ["Evaluate loop control flow"],
  "deliverables": ["Working program"],
  "constraints": ["No external libraries"],
  "versionNumber": 1
}
```

On failure, no draft row is created and the response reflects why `agents/` couldn't be reached or rejected the request:

| `AgentClientException.Reason` | HTTP status |
|---|---|
| `UNREACHABLE` (agents/ unreachable — connection/timeout) | `503 Service Unavailable` |
| `AGENT_REJECTED` (agents/ rejected the command, 4xx) | `422 Unprocessable Content` |
| `AGENT_ERROR` (agents/ failed processing, 5xx) | `502 Bad Gateway` |

Every attempt — success or failure — persists an `AgentExecutionLog` row (`status="COMPLETED"`/`"FAILED"`, `errorCode` set on failure). The call to `agents/` always happens with no open database transaction; persistence (log + draft, cross-referenced) happens afterward in a single transaction.

## Example

```java
GenerateAssessmentDraftResult result = generateAssessmentDraftUseCase.execute(
    new GenerateAssessmentDraftCommand(assessmentId, teacher.uid()));
// result.draftId(), result.title(), result.versionNumber() == 1
```
