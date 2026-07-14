# ⚛️ TASK 04 — validate-db-orm-consistency (V9+V10+V11)

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02, task-03
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

Explicit static + runtime DB/ORM consistency validation covering all three new migrations (`V9` assessments, `V10` assessment_briefs, `V11` assessment_drafts) and their JPA entities together, since they were introduced in the same story and share foreign-key relationships.

---

## Technical Design

- **Approach:** a dedicated validation pass, not folded into task-01/02/03 individually, because the FK relationships between all three tables (`assessments` ← `assessment_briefs`, `assessments` ← `assessment_drafts` ← `assessment_drafts.previous_version_id`) can only be fully verified once all three exist together.
- **Affected files / components:** no new production code — this task produces verification evidence (test results, manual query output) attached to the task's PR description. May add a small integration test class if none of task-01/02/03's own tests already exercise the full FK chain end-to-end.
- **Interfaces / contracts:** none new.
- **Risk:** Low — verification-only task.
- **Design notes:** run this after task-01/02/03 are all merged into the story branch, not in parallel with them.

---

## Implementation Steps

1. Run `./mvnw flyway:info` (or equivalent) against a fresh local Postgres to confirm `V9`, `V10`, `V11` are all applied in order with no checksum conflicts.
2. Manually (or via a throwaway integration test) insert one `Assessment`, one `AssessmentBrief` FK'd to it, and two `AssessmentDraft` versions (the second referencing the first via `previous_version_id`) — confirm all FK constraints hold and no orphan rows are possible.
3. Diff each JPA entity's fields against its table's columns one more time, side by side (name, type, nullability, defaults) — catch anything task-01/02/03's individual reviews missed.
4. Confirm `HexagonalArchitectureTest` (existing ArchUnit suite) still passes with the new classes in place — no rule violations introduced by the three new bounded-context slices.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | All three migrations apply cleanly together, in order | `./mvnw flyway:info` shows `V9`, `V10`, `V11` as `Success` with no pending/conflicted state |
| 2 | Full FK chain (assessment → brief, assessment → draft → previous draft) holds | Integration test or manual insert sequence per step 2 |
| 3 | ArchUnit suite passes with the new classes | `./mvnw test -Dtest=HexagonalArchitectureTest` |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up db` |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors, all 3 migrations apply |
| 3 | Connectivity or schema validation succeeds | No Flyway checksum or Hibernate validation errors in startup logs |
| 4 | Changed surface responds correctly | `GET /api/v1/assessments` still returns `200 []` (no regression from the combined schema) |
| 5 | No startup or migration regressions are visible | Startup logs clean end to end |

### Database / ORM Consistency Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Static database-to-ORM consistency is valid across all 3 tables/entities | Side-by-side diff per implementation step 3 |
| 2 | Local runtime environment starts | `docker compose up db` then `./mvnw spring-boot:run -Dspring.profiles.active=local` |
| 3 | Persistence smoke check passes | Full FK-chain insert sequence (step 2) succeeds with no constraint violations |

---

## Done Criteria

- [x] `V9`, `V10`, `V11` all apply cleanly together against a fresh database.
- [x] Full FK chain (assessment → brief, assessment → draft v1 → draft v2) verified with no orphan or constraint-violation risk.
- [x] `HexagonalArchitectureTest` passes with all new classes.
- [x] `./mvnw test` passes.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
