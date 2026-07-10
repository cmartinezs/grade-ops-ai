# ⚛️ TASK 02 — AssessmentBrief entity + persistence

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

Create the `AssessmentBrief` domain model, JPA entity, migration, and repository — the teacher's intake fields (US-010), persisted before any agent call.

---

## Technical Design

- **Approach:** mirror task-01's structure exactly (domain model, JPA entity, JPA repository, persistence adapter, mapper), FK'd to `Assessment` (`assessment_id`). One `AssessmentBrief` per `Assessment` (1:1) — a regeneration (US-012) reuses the same brief plus optional `adjustmentNotes`, it does not create a new brief.
- **Affected files / components:**
  - `assessment/domain/model/AssessmentBrief.java` (new, pure domain)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentBriefJpaEntity.java` (new)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentBriefJpaRepository.java` (new)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentBriefPersistenceAdapter.java` (new)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentBriefPersistenceMapper.java` (new)
  - `assessment/application/port/out/AssessmentBriefRepositoryPort.java` (new — `save(AssessmentBrief)`, `findByAssessmentId(AssessmentId) -> Optional<AssessmentBrief>`)
  - `src/main/resources/db/migration/V10__add_assessment_briefs.sql` (new)
- **Interfaces / contracts:** `AssessmentBrief` fields: `id` (`UUID`), `assessmentId` (`AssessmentId`, FK), `learningGoal`, `topic`, `level`, `duration`, `language` (all `String`, `NOT NULL`), `createdAt` (`Instant`). Field names must match `AssessmentCommand`'s corresponding fields in the sibling `agents/` child planning exactly (`agents/.planning/001-assessment-creation`, task-01) — this is the cross-repo contract boundary.
- **Risk:** M — field-name drift against `agents/`'s `AssessmentCommand` would silently break the draft-generation call (task-07); mitigated by cross-checking both task files' field lists before finalizing, and by task-07's integration test.
- **Design notes:** `assessment_id` has a `UNIQUE` constraint (1:1 with `Assessment`) — a second `POST` brief-intake for the same assessment should be rejected or treated as an update, not silently create a duplicate row (decide and document in task-06's endpoint logic, not here).

---

## Implementation Steps

1. Create `V10__add_assessment_briefs.sql`: `assessment_briefs` table — `id UUID PRIMARY KEY`, `assessment_id UUID NOT NULL UNIQUE REFERENCES assessments(id)`, `learning_goal VARCHAR NOT NULL`, `topic VARCHAR NOT NULL`, `level VARCHAR NOT NULL`, `duration VARCHAR NOT NULL`, `language VARCHAR NOT NULL`, `created_at TIMESTAMPTZ NOT NULL`.
2. Create `AssessmentBrief.java` (pure domain).
3. Create `AssessmentBriefJpaEntity.java` (`@Entity @Table(name = "assessment_briefs")`, UUID id, `@ManyToOne`/plain UUID FK column to `assessment_id` — prefer a plain UUID column over a JPA relationship to keep the entity simple, matching the project's light-JPA style seen in `PasswordResetCodeJpaEntity`).
4. Create `AssessmentBriefJpaRepository.java` (`extends JpaRepository<AssessmentBriefJpaEntity, UUID>`, add `Optional<AssessmentBriefJpaEntity> findByAssessmentId(UUID assessmentId)`).
5. Create `AssessmentBriefPersistenceMapper.java`.
6. Create `AssessmentBriefPersistenceAdapter.java` implementing `AssessmentBriefRepositoryPort`.
7. Create `AssessmentBriefRepositoryPort.java`.
8. Cross-check field names (`learningGoal`, `topic`, `level`, `duration`, `language`) against `agents/.planning/001-assessment-creation/02-deepening/story-01-assessment-agent/task-01-contracts.md`'s `AssessmentCommand` field list; record any mismatch as a residual in this story's `TRACEABILITY.md`.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `AssessmentBriefPersistenceAdapter` saves and retrieves correctly | New `AssessmentBriefPersistenceAdapterTest` — save then `findByAssessmentId`, assert round-trip equality |
| 2 | Field names match `AssessmentCommand` | Manual diff against the `agents/` task-01 file (present in this worktree if `agents/` is checked out alongside; otherwise diff against the documented field list in `agents/.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent.md`) |
| 3 | `./mvnw test` passes | Full suite green |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up db` |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors |
| 3 | Connectivity or schema validation succeeds | `V10__add_assessment_briefs.sql` applies cleanly on startup |
| 4 | Changed surface responds correctly | N/A — no new endpoint yet (task-06); covered by the persistence test |
| 5 | No startup or migration regressions are visible | App logs show no Flyway/Hibernate errors |

### Database / ORM Consistency Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Static database-to-ORM consistency is valid | `assessment_briefs` columns match `AssessmentBriefJpaEntity` fields exactly; FK/unique constraint on `assessment_id` matches the 1:1 domain rule |
| 2 | Local runtime environment starts | `docker compose up db` then `./mvnw spring-boot:run -Dspring.profiles.active=local` |
| 3 | Persistence smoke check passes | Insert a brief for an existing assessment id via the adapter (test), confirm round-trip |

---

## Done Criteria

- [ ] `V10__add_assessment_briefs.sql` applies cleanly with a `UNIQUE` FK to `assessments(id)`.
- [ ] `AssessmentBrief` domain + persistence classes exist and round-trip correctly.
- [ ] Field names verified against `agents/`'s `AssessmentCommand`; any mismatch recorded as a residual.
- [ ] `./mvnw test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
