# ⚛️ TASK 03 — AssessmentDraft entity + versioning

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

Create the versioned `AssessmentDraft` domain model, JPA entity, migration, and repository — one row per generation/regeneration (US-011, US-012), never overwritten.

---

## Technical Design

- **Approach:** mirror task-01/task-02's structure. `AssessmentDraft` is versioned via a self-referencing `previous_version_id` FK plus a `version_number` integer, both FK'd to the same `Assessment` (`assessment_id`). Every regeneration inserts a new row; nothing is ever updated or deleted (append-only), satisfying the epic DoD that previous versions remain retrievable.
- **Affected files / components:**
  - `assessment/domain/model/AssessmentDraft.java` (new, pure domain)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentDraftJpaEntity.java` (new)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentDraftJpaRepository.java` (new)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentDraftPersistenceAdapter.java` (new)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentDraftPersistenceMapper.java` (new)
  - `assessment/application/port/out/AssessmentDraftRepositoryPort.java` (new — `save(AssessmentDraft)`, `findCurrentByAssessmentId(AssessmentId) -> Optional<AssessmentDraft>`, `findAllByAssessmentId(AssessmentId) -> List<AssessmentDraft>` ordered by `version_number`)
  - `src/main/resources/db/migration/V11__add_assessment_drafts.sql` (new)
- **Interfaces / contracts:** `AssessmentDraft` fields: `id` (`UUID`), `assessmentId` (`AssessmentId`, FK), `versionNumber` (`int`), `previousVersionId` (`UUID`, nullable — null for version 1), `title`, `context`, `instructions` (`String`), `objectives`, `deliverables`, `constraints` (each `List<String>`, stored as JSON columns — see design notes), `agentExecutionLogId` (`UUID`, FK — populated once task-07/task-08 wire it), `createdAt` (`Instant`). Fields (`title`/`context`/`instructions`/`objectives`/`deliverables`/`constraints`) must match `AssessmentResult`'s shape in `agents/`'s contract exactly.
- **Risk:** M — same cross-repo field-drift risk as task-02, plus versioning-logic risk (a bug here could let a regeneration overwrite a previous version, which is the story's own R-02 risk); mitigated by making every write an `INSERT`, never an `UPDATE`, and by task-11's dedicated versioning integration test.
- **Design notes:** `objectives`/`deliverables`/`constraints` are `List<String>` — store as a JSON/JSONB column (Postgres `jsonb`, mapped via a converter, or Hibernate's native `@JdbcTypeCode(SqlTypes.JSON)` if available in the project's Hibernate version) rather than a separate child table, since these lists have no independent lifecycle outside their draft version. `findCurrentByAssessmentId` returns the row with the highest `version_number` for that assessment.

---

## Implementation Steps

1. Create `V11__add_assessment_drafts.sql`: `assessment_drafts` table — `id UUID PRIMARY KEY`, `assessment_id UUID NOT NULL REFERENCES assessments(id)`, `version_number INT NOT NULL`, `previous_version_id UUID REFERENCES assessment_drafts(id)`, `title VARCHAR NOT NULL`, `context TEXT NOT NULL`, `instructions TEXT NOT NULL`, `objectives JSONB NOT NULL`, `deliverables JSONB NOT NULL`, `constraints JSONB NOT NULL`, `agent_execution_log_id UUID`, `created_at TIMESTAMPTZ NOT NULL`, `UNIQUE(assessment_id, version_number)`.
2. Create `AssessmentDraft.java` (pure domain).
3. Create `AssessmentDraftJpaEntity.java` with a JSON converter/type for the three list columns (confirm the exact Hibernate JSON mapping mechanism available in the project's Spring Boot/Hibernate version before finalizing — do not assume a specific annotation without checking).
4. Create `AssessmentDraftJpaRepository.java` (`extends JpaRepository<AssessmentDraftJpaEntity, UUID>`, add `List<AssessmentDraftJpaEntity> findAllByAssessmentIdOrderByVersionNumberDesc(UUID assessmentId)`).
5. Create `AssessmentDraftPersistenceMapper.java`.
6. Create `AssessmentDraftPersistenceAdapter.java` implementing `AssessmentDraftRepositoryPort` (`findCurrentByAssessmentId` = first element of the descending-ordered query).
7. Create `AssessmentDraftRepositoryPort.java`.
8. Cross-check field names against `agents/`'s `AssessmentResult` (same cross-check as task-02).

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `AssessmentDraftPersistenceAdapter` saves and retrieves correctly, including JSON list fields | New `AssessmentDraftPersistenceAdapterTest` — save a draft with populated `objectives`/`deliverables`/`constraints`, retrieve, assert round-trip equality including list contents and order |
| 2 | `findCurrentByAssessmentId` returns the highest version | Test: save versions 1, 2, 3 for the same assessment, assert `findCurrentByAssessmentId` returns version 3 |
| 3 | `findAllByAssessmentId` never loses a version | Test: save 3 versions, assert all 3 are retrievable and none was overwritten |
| 4 | `./mvnw test` passes | Full suite green |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up db` |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors |
| 3 | Connectivity or schema validation succeeds | `V11__add_assessment_drafts.sql` applies cleanly on startup |
| 4 | Changed surface responds correctly | N/A — no new endpoint yet (task-07/08); covered by the persistence tests |
| 5 | No startup or migration regressions are visible | App logs show no Flyway/Hibernate errors, including JSON column mapping |

### Database / ORM Consistency Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Static database-to-ORM consistency is valid | `assessment_drafts` columns match `AssessmentDraftJpaEntity` fields exactly, including JSON column mapping for the three list fields and the `(assessment_id, version_number)` unique constraint |
| 2 | Local runtime environment starts | `docker compose up db` then `./mvnw spring-boot:run -Dspring.profiles.active=local` |
| 3 | Persistence smoke check passes | Insert 2 versions for one assessment via the adapter (test), confirm both are retrievable and version 2 does not overwrite version 1's row |

---

## Done Criteria

- [ ] `V11__add_assessment_drafts.sql` applies cleanly with `(assessment_id, version_number)` unique and a self-referencing `previous_version_id`.
- [ ] `AssessmentDraft` domain + persistence classes exist, including correct JSON list-field mapping.
- [ ] Multiple versions for the same assessment coexist without overwrite; `findCurrentByAssessmentId` returns the latest.
- [ ] Field names verified against `agents/`'s `AssessmentResult`.
- [ ] `./mvnw test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
