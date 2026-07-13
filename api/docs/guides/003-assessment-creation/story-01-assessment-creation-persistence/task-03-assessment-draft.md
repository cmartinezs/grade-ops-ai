# AssessmentDraft

**Source:** task-03-assessment-draft | **Area:** AP | **Date:** 2026-07-13

## What it does

Persists the versioned output of the Assessment Agent — one immutable row per generation or regeneration (US-011, US-012). Every regeneration inserts a new row linked to the previous one via `previousVersionId`; no row is ever updated or deleted, so every prior version remains retrievable.

## How to use it

Domain layer (`cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft`):

- `AssessmentDraft.generate(AssessmentId assessmentId, String title, String context, String instructions, List<String> objectives, List<String> deliverables, List<String> constraints, UUID agentExecutionLogId)` — creates version 1 (`previousVersionId = null`).
- `AssessmentDraft.regenerate(AssessmentDraft previousVersion, String title, String context, String instructions, List<String> objectives, List<String> deliverables, List<String> constraints, UUID agentExecutionLogId)` — creates the next version, deriving `versionNumber` and `previousVersionId` from `previousVersion` so an inconsistent version/link pair can't be constructed by a caller.
- `AssessmentDraft.restore(...)` — reconstructs a draft from persistence; validates every field, including the version-1-has-no-previous / version-N-has-a-previous consistency rule.

Persistence layer, via `AssessmentDraftRepositoryPort` (`cl.gradeops.ai.api.assessment.application.port.out`):

- `void save(AssessmentDraft draft)` — always inserts a new row; there is no update path.
- `Optional<AssessmentDraft> findCurrentByAssessmentId(AssessmentId assessmentId)` — the highest `versionNumber` for that assessment.
- `List<AssessmentDraft> findAllByAssessmentId(AssessmentId assessmentId)` — every version, newest first.

Schema (`V11__add_assessment_drafts.sql`): `assessment_drafts(id UUID PK, assessment_id UUID FK -> assessments(id), version_number INT, previous_version_id UUID FK -> assessment_drafts(id) NULLABLE, title VARCHAR, context TEXT, instructions TEXT, objectives/deliverables/constraints JSONB, agent_execution_log_id UUID NULLABLE, created_at TIMESTAMPTZ, UNIQUE(assessment_id, version_number))`.

`objectives`/`deliverables`/`constraints` map `List<String>` to Postgres `jsonb` via Hibernate's native `@JdbcTypeCode(SqlTypes.JSON)` — no custom converter needed. Field names (`title`, `context`, `instructions`, `objectives`, `deliverables`, `constraints`) intentionally mirror `agents/`'s `AssessmentResult` contract exactly.

`agentExecutionLogId` is nullable at this stage — task-07/task-08 populate it once `AgentExecutionLog` exists.

## Example

```java
AssessmentDraft v1 = AssessmentDraft.generate(
    assessment.getId(), "Java Loops Quiz", "Second-semester students", "Solve the following...",
    List.of("Evaluate loop control flow"), List.of("Working program"), List.of("No external libraries"),
    null);
assessmentDraftRepositoryPort.save(v1);

// later, on regeneration:
AssessmentDraft v2 = AssessmentDraft.regenerate(v1, "Java Loops Quiz v2", "...", "...",
    List.of("..."), List.of("..."), List.of("..."), null);
assessmentDraftRepositoryPort.save(v2);

assessmentDraftRepositoryPort.findCurrentByAssessmentId(assessment.getId()); // returns v2
assessmentDraftRepositoryPort.findAllByAssessmentId(assessment.getId());    // returns [v2, v1]
```
