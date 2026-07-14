# Brief intake endpoint

**Source:** task-06-brief-intake-endpoint | **Area:** AP | **Date:** 2026-07-13

## What it does

`POST` endpoint that creates an `Assessment` (status `DRAFT`) and its `AssessmentBrief` in one transaction, before any agent call — satisfying US-010's DoD that a failed agent call never loses the teacher's input. Draft generation (the actual AI call) is a separate, subsequent request (task-07).

## How to use it

`POST /api/v1/assessments` — requires an authenticated teacher (`Authorization: Bearer <token>`).

Request body:

```json
{
  "learningGoal": "Evaluate loop control flow",
  "topic": "Java loops",
  "level": "basic",
  "duration": "90min",
  "language": "Java"
}
```

All 5 fields are required (`@NotBlank`). A blank field returns `422 UNPROCESSABLE_CONTENT` (the project-wide convention for Bean Validation failures — see `shared/infrastructure/adapter/in/web/GlobalExceptionHandler.java`'s `MethodArgumentNotValidException` handler; not `400`). An unauthenticated request returns `401`.

On success, returns `201 Created`:

```json
{
  "assessmentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

Both the `Assessment` (status `DRAFT`, owned by the authenticated teacher) and the linked `AssessmentBrief` are persisted in a single `@Transactional` handler (`CreateAssessmentBriefHandler`) — a partial write (assessment created, brief failed) is not possible. No call to `agents/` happens in this request.

## Example

```java
CreateAssessmentBriefCommand command = new CreateAssessmentBriefCommand(
    teacher.uid(), "Evaluate loop control flow", "Java loops", "basic", "90min", "Java");
CreateAssessmentBriefResult result = createAssessmentBriefUseCase.execute(command);
// result.assessmentId() — UUID string of the newly created Assessment
```
