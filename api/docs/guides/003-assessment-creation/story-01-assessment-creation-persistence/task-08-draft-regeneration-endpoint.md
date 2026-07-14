# Draft regeneration endpoint

**Source:** task-08-draft-regeneration-endpoint | **Area:** AP | **Date:** 2026-07-13

## What it does

`POST` endpoint that regenerates the draft with teacher-provided adjustment notes, creating a new non-destructive version (US-012). Shares the "call agents/, persist log + draft" flow with task-07's generation endpoint via a new internal `DraftGenerationCoordinator`, so a bug in that ordering-sensitive logic can't be introduced independently by each endpoint.

## How to use it

`POST /api/v1/assessments/{id}/draft/regenerate` — requires an authenticated teacher who owns the assessment; regeneration requires a prior draft to already exist.

Request body:

```json
{ "adjustmentNotes": "Make it harder, add error handling requirements" }
```

`adjustmentNotes` is required (`@NotBlank`). Calling this before any draft has ever been generated for the assessment returns `422 UNPROCESSABLE_CONTENT`. A mismatched or non-existent assessment id returns `404`.

On success, returns `201 Created` with the new version — same shape as task-07's generation response:

```json
{
  "draftId": "...",
  "title": "Java Loops Quiz v2",
  "context": "...",
  "instructions": "...",
  "objectives": ["..."],
  "deliverables": ["..."],
  "constraints": ["..."],
  "versionNumber": 2
}
```

The previous version's row is never modified (append-only, per task-03's design) — the new draft links back to it via `previousVersionId`, and every regeneration produces its own distinct `AgentExecutionLog`. `AgentClientException` failures map to the same `503`/`422`/`502` status codes as task-07's generation endpoint.

## Example

```java
GenerateAssessmentDraftResult result = regenerateAssessmentDraftUseCase.execute(
    new RegenerateAssessmentDraftCommand(assessmentId, teacher.uid(), "make it harder"));
// result.versionNumber() == currentVersion + 1
```
