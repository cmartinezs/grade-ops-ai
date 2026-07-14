# Retrieval endpoints + dashboard wiring

**Source:** task-10-retrieval-and-dashboard-wiring | **Area:** AP | **Date:** 2026-07-14

## What it does

Exposes current-draft and version-history retrieval endpoints, and completes the `Assessment` stub replacement started in task-01: `GET /api/v1/assessments` (the teacher dashboard) now returns real assessments with a real title, instead of the empty list it returned since task-01. This is the last task in story-01 — assessment creation, generation, regeneration, editing, and retrieval are now all wired end to end.

## How to use it

`GET /api/v1/assessments/{id}/draft` — returns the current version. `404` if no draft has ever been generated for that assessment, or if the assessment belongs to another teacher.

`GET /api/v1/assessments/{id}/draft/versions` — returns every version, newest-first (same ordering as `AssessmentDraftRepositoryPort.findAllByAssessmentId`). An empty list, not `404`, if no draft exists yet — a version-history collection with zero items is a normal state. `404` on cross-teacher access.

`GET /api/v1/assessments` (existing endpoint, now fully wired) — returns every assessment owned by the authenticated teacher, one row per assessment:

```json
[
  {
    "id": "...",
    "title": "Java Loops Quiz v2",
    "status": "DRAFT",
    "submissionCount": 0,
    "pendingApprovals": 0,
    "reportLink": null
  }
]
```

`title` comes from the assessment's current draft (highest `versionNumber`); if no draft has been generated yet, it falls back to the brief's `topic`. `submissionCount`/`pendingApprovals`/`reportLink` stay at their placeholder values (`0`/`0`/`null`) — those belong to later epics (grading, feedback) and are out of scope here. The underlying query is a single native SQL statement per call (`LEFT JOIN LATERAL`), regardless of how many assessments the teacher has — no N+1.

## Example

```java
GenerateAssessmentDraftResult current = getCurrentDraftUseCase.execute(
    new GetCurrentDraftCommand(assessmentId, teacher.uid()));

List<GenerateAssessmentDraftResult> allVersions = listDraftVersionsUseCase.execute(
    new ListDraftVersionsCommand(assessmentId, teacher.uid()));
// allVersions.get(0) is the newest version
```
