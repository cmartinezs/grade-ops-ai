# DB/ORM Consistency Validation — assessments, assessment_briefs, assessment_drafts

**Source:** task-04-db-orm-consistency | **Area:** AP | **Date:** 2026-07-13

## What it does

A dedicated, no-new-production-code validation pass confirming the three migrations introduced across task-01/02/03 (`V9` assessments, `V10` assessment_briefs, `V11` assessment_drafts) and their JPA entities are consistent together, and that the foreign-key relationships between all three tables hold end to end — something no single task's own tests exercised on their own.

## How to use it

- `V9`/`V10`/`V11` apply cleanly together, in order, with no checksum conflicts — verify with `./mvnw flyway:info` against a fresh local Postgres.
- The full FK chain (`assessments` ← `assessment_briefs`, `assessments` ← `assessment_drafts` ← `assessment_drafts.previous_version_id`) is covered by `AssessmentPersistenceFkChainIntegrationTest`:
  - inserting one `Assessment`, one `AssessmentBrief`, and two `AssessmentDraft` versions succeeds with no orphan rows;
  - a brief or draft referencing a non-existent assessment is rejected by the FK constraint;
  - deleting an `Assessment` cascades to its brief and all draft versions (`ON DELETE CASCADE`).
- `HexagonalArchitectureTest` continues to pass with all three new bounded-context slices in place — no ArchUnit rule violations from the new classes.
- The existing dashboard endpoint (`GET /api/v1/assessments`) still returns `200 []` with the full combined schema present — no regression.

## Example

```bash
docker compose up -d postgres
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/gradeops -Dflyway.user=gradeops -Dflyway.password=gradeops
./mvnw flyway:info -Dflyway.url=jdbc:postgresql://localhost:5432/gradeops -Dflyway.user=gradeops -Dflyway.password=gradeops
./mvnw test -Dtest=AssessmentPersistenceFkChainIntegrationTest,HexagonalArchitectureTest
```
