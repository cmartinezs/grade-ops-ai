# ADR: Retrieval endpoints + dashboard wiring — single-query dashboard join

**Date:** 2026-07-14
**Status:** Accepted
**Planning:** 003-assessment-creation / story-01-assessment-creation-persistence / task-10-retrieval-and-dashboard-wiring

## Context

`AssessmentPersistenceAdapter.findAllByTeacherId` was left as an empty-list-preserving stub since task-01, deliberately, to avoid returning partial rows (`title = null`) before this task's join logic existed. Completing it means producing a `title` per assessment from its current draft, falling back to the brief's `topic` if no draft has been generated yet — for every assessment a teacher owns, in one dashboard call. The task's own Risk section flagged the obvious naive implementation (loop over assessments, call `findCurrentByAssessmentId` per row) as an N+1 query risk.

## Decision

Added `AssessmentJpaRepository.findSummariesByTeacherUid`, a native Postgres query (`@Query(nativeQuery = true)`) using `LEFT JOIN LATERAL` to pull each assessment's highest-`version_number` draft title in the same query as the assessment and brief rows, `COALESCE`d with the brief's `topic`:

```sql
SELECT a.id AS id, a.status AS status, COALESCE(d.title, b.topic) AS title
FROM assessments a
JOIN assessment_briefs b ON b.assessment_id = a.id
LEFT JOIN LATERAL (
    SELECT ad.title FROM assessment_drafts ad
    WHERE ad.assessment_id = a.id
    ORDER BY ad.version_number DESC
    LIMIT 1
) d ON true
WHERE a.teacher_uid = :teacherUid
ORDER BY a.created_at DESC
```

Returns an `AssessmentSummaryProjection` (Spring Data interface projection), mapped to `AssessmentSummaryResult` in the adapter. One round trip per `findAllByTeacherId` call, independent of how many assessments or draft versions exist — verified in `AssessmentPersistenceAdapterIntegrationTest` (title-from-draft, title-falls-back-to-topic, teacher isolation).

`GET .../draft` returns `404` (`ResourceNotFoundException`) when no draft exists — a plain "nothing to return" — deliberately not task-08/09's `NoPriorDraftException` (`422`, a write-operation precondition that doesn't apply to a read). `GET .../draft/versions` returns an empty list, not `404`, in the same situation: a version-history collection with zero items is a normal, valid state for a GET-list endpoint, not an error.

## Consequences

`AssessmentPersistenceAdapter.findAllByTeacherId`'s pre-task-10 contract (documented in its own code comment as "intentionally empty-list-preserving until task-10") is now fully replaced — the obsolete unit test asserting the stub behavior was replaced with tests asserting the real projection mapping. `submissionCount`/`pendingApprovals`/`reportLink` remain `0`/`0`/`null`, as specified — those belong to later epics (grading, feedback), not this story. This is the first native SQL query in the `assessment` bounded context (every prior repository used Spring Data derived-query methods); acceptable since the project is Postgres-only end to end (Testcontainers, JSONB columns, `gen_random_uuid()` defaults already assume it).

## Alternatives Considered

Looping `findCurrentByAssessmentId` per assessment in the adapter (N+1 queries) was the naive approach the task's own Risk section explicitly warned against — rejected outright, not implemented even as a starting point.

A JPQL query with a correlated subquery was considered instead of native SQL, but JPQL has no direct equivalent to Postgres's `LATERAL` join for "the single most recent row per group" without resorting to a window function wrapped in a subquery — messier than the native query for no portability benefit, given the project has no non-Postgres target.
