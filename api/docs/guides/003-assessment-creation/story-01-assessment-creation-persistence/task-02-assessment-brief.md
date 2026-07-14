# AssessmentBrief

**Source:** task-02-assessment-brief | **Area:** AP | **Date:** 2026-07-13

## What it does

Persists the teacher's intake fields for an assessment — learning goal, topic, level, duration, and language — before any AI agent call. One `AssessmentBrief` exists per `Assessment` (1:1, enforced by a `UNIQUE` foreign key on `assessment_id`). A draft regeneration reuses the same brief; it never creates a second one.

## How to use it

Domain layer (`cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief`):

- `AssessmentBrief.create(AssessmentId assessmentId, String learningGoal, String topic, String level, String duration, String language)` — creates a new brief with a generated `id` and `createdAt = now()`. Throws `DomainInvariantViolationException` if `assessmentId` is null or any `String` field is null/blank.
- `AssessmentBrief.restore(UUID id, AssessmentId assessmentId, String learningGoal, String topic, String level, String duration, String language, Instant createdAt)` — reconstructs a brief from persistence. Validates every field, including `id` and `createdAt`.

Persistence layer, via `AssessmentBriefRepositoryPort` (`cl.gradeops.ai.api.assessment.application.port.out`):

- `void save(AssessmentBrief brief)` — inserts or updates the brief row.
- `Optional<AssessmentBrief> findByAssessmentId(AssessmentId assessmentId)` — looks up the brief for a given assessment; empty if none exists yet.

Schema (`V10__add_assessment_briefs.sql`): `assessment_briefs(id UUID PK, assessment_id UUID UNIQUE FK -> assessments(id) ON DELETE CASCADE, learning_goal, topic, level, duration, language VARCHAR NOT NULL, created_at TIMESTAMPTZ NOT NULL)`.

Field names (`learningGoal`, `topic`, `level`, `duration`, `language`) intentionally mirror the `agents/` service's `AssessmentCommand` contract exactly — this is the cross-repo boundary the future draft-generation call (task-07) depends on.

## Example

```java
Assessment assessment = Assessment.create(teacherUid);
assessmentRepositoryPort.save(assessment);

AssessmentBrief brief = AssessmentBrief.create(
    assessment.getId(), "Evaluate loops and functions", "Java basics",
    "basic", "90min", "Java");
assessmentBriefRepositoryPort.save(brief);

Optional<AssessmentBrief> found = assessmentBriefRepositoryPort.findByAssessmentId(assessment.getId());
```
