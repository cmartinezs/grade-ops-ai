# Draft edit endpoint

**Source:** task-09-draft-edit-endpoint | **Area:** AP | **Date:** 2026-07-14

## What it does

`PATCH` endpoint letting the teacher edit the current draft's fields directly (US-013) — no agent call, no new version, no new `AgentExecutionLog`. Unlike task-08's regeneration (which always appends a new version), this updates the current version's row in place.

## How to use it

`PATCH /api/v1/assessments/{id}/draft` — requires an authenticated teacher who owns the assessment; a prior draft must already exist (same 422 rule as task-08's regeneration).

Request body: any subset of the 6 draft fields. `null`/absent fields keep their current value — only fields present in the request are changed.

```json
{ "title": "New title" }
```

An explicitly-sent blank string (`""`) or a blank element inside `objectives`/`deliverables`/`constraints` is rejected with `422` before the handler runs. A `null` field, or an absent field, is always valid and means "don't change this field."

On success, returns `200 OK` with the updated draft — same shape as task-07/08's response, but the `draftId` and `versionNumber` are always unchanged from before the edit:

```json
{
  "draftId": "...",
  "title": "New title",
  "context": "...",
  "instructions": "...",
  "objectives": ["..."],
  "deliverables": ["..."],
  "constraints": ["..."],
  "versionNumber": 1
}
```

## Example

```java
GenerateAssessmentDraftResult result = updateAssessmentDraftUseCase.execute(
    new UpdateAssessmentDraftCommand(assessmentId, teacher.uid(), "New title", null, null, null, null, null));
// result.draftId() and result.versionNumber() are identical to before the call
```
