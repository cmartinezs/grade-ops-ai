# ⚛️ TASK 01 — Assessment aggregate root (domain + persistence)

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

Create the real `Assessment` aggregate root (domain model, JPA entity, Flyway migration, repository) and replace `StubAssessmentPersistenceAdapter` with a real implementation — the stub's own comment says *"Epic 02 will replace return type with domain Assessment objects"*, and this is that replacement.

---

## Technical Design

- **Approach:** mirror the existing `teacher` bounded context's hexagonal layout exactly (`domain/model` pure, `application/{port/out}`, `infrastructure/adapter/out/persistence` with Entity + JPA repo + adapter + mapper). `Assessment` becomes the aggregate that `AssessmentBrief` (task-02) and `AssessmentDraft` (task-03) attach to via foreign key. `AssessmentStatus` (already exists: `DRAFT`, `OPEN`, `GRADING`, `CLOSED`) stays `DRAFT` for the entire scope of this story — transitioning to `OPEN` is a future epic's concern (teacher approval/publish), out of scope here.
- **Affected files / components:**
  - `assessment/domain/model/Assessment.java` (new, pure domain record/class — no Spring/JPA imports)
  - `assessment/domain/model/AssessmentId.java` (new value object wrapping `UUID`, mirrors `TeacherId`)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentJpaEntity.java` (new)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentJpaRepository.java` (new, Spring Data JPA)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentPersistenceAdapter.java` (new — implements `AssessmentRepositoryPort`, replaces `StubAssessmentPersistenceAdapter`)
  - `assessment/infrastructure/adapter/out/persistence/AssessmentPersistenceMapper.java` (new)
  - `assessment/application/port/out/AssessmentRepositoryPort.java` (**modify** — currently returns `List<AssessmentSummaryResult>` from a stub-only method; extend with `save(Assessment)`, `findById(AssessmentId)`, keep `findAllByTeacherId` but source it from real data)
  - `assessment/infrastructure/config/AssessmentConfig.java` (**modify** — remove the `StubAssessmentPersistenceAdapter` `@Bean`, wire the real `AssessmentPersistenceAdapter` instead)
  - `assessment/infrastructure/adapter/out/persistence/StubAssessmentPersistenceAdapter.java` (**delete** once the real adapter is wired)
  - `src/main/resources/db/migration/V9__add_assessments.sql` (new — confirm V8 is still the latest before naming this)
- **Interfaces / contracts:** `Assessment` domain fields: `id` (`AssessmentId`), `teacherUid` (`String`), `status` (`AssessmentStatus`), `createdAt` (`Instant`). No `title` column on `Assessment` itself — the dashboard's display title is derived from the current draft (or the brief's topic before any draft exists); storing it redundantly on `Assessment` risks drift between the two. This changes `AssessmentSummaryResult`'s title source but not its shape (handled in task-10, not here).
- **Risk:** M — this is the first real persistence for a bounded context whose port/stub was already relied upon by an existing dashboard endpoint (`GET /api/v1/assessments`); replacing the stub incorrectly could silently break that endpoint. Mitigated by keeping `findAllByTeacherId`'s signature unchanged in this task (still returns `List<AssessmentSummaryResult>`, just backed by an empty real table instead of a stub — behavior is identical, `List.of()`, until task-10 wires brief/draft data into the summary).
- **Design notes:** do not implement `findAllByTeacherId`'s real query logic (joining draft/brief data) in this task — that's task-10's job, once `AssessmentBrief`/`AssessmentDraft` exist. This task only needs the query to keep returning an empty list (real query against an empty table), preserving current dashboard behavior.

---

## Implementation Steps

1. Check `src/main/resources/db/migration/` for the latest `V<N>__` migration (was `V8` at story-atomization time) and create `V9__add_assessments.sql`: `assessments` table with `id UUID PRIMARY KEY`, `teacher_uid VARCHAR NOT NULL`, `status VARCHAR NOT NULL DEFAULT 'DRAFT'`, `created_at TIMESTAMPTZ NOT NULL`.
2. Create `AssessmentId.java` (value object wrapping `UUID`, mirrors `TeacherId`'s pattern).
3. Create `Assessment.java` (pure domain class/record: `id`, `teacherUid`, `status`, `createdAt`; no Spring/JPA imports).
4. Create `AssessmentJpaEntity.java` (`@Entity @Table(name = "assessments")`, `@Id @GeneratedValue(strategy = GenerationType.UUID)`, `@Getter @Setter @NoArgsConstructor` per project convention — no `@Data`).
5. Create `AssessmentJpaRepository.java` (`extends JpaRepository<AssessmentJpaEntity, UUID>`, add `List<AssessmentJpaEntity> findAllByTeacherUid(String teacherUid)`).
6. Create `AssessmentPersistenceMapper.java` (`toDomain(AssessmentJpaEntity)`, `toEntity(Assessment)`).
7. Create `AssessmentPersistenceAdapter.java` implementing the extended `AssessmentRepositoryPort` (`save`, `findById`), and re-implementing `findAllByTeacherId` to query the real (currently empty) table and map to `List.of()`-equivalent `AssessmentSummaryResult` (temporary mapping — full summary logic lands in task-10).
8. Modify `AssessmentRepositoryPort.java` — add `save(Assessment)`, `findById(AssessmentId) -> Optional<Assessment>`; remove the "Stub port" comment.
9. Modify `AssessmentConfig.java` — remove the `stubAssessmentPersistenceAdapter()` `@Bean`, register `AssessmentPersistenceAdapter` (via `@Component`/constructor injection, following `TeacherPersistenceAdapter`'s registration pattern) instead.
10. Delete `StubAssessmentPersistenceAdapter.java`.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `Assessment` domain class has no Spring/JPA imports | Manual review; mirrors existing `HexagonalArchitectureTest` intent even though that specific rule is currently scoped to `..shared.domain..` |
| 2 | `AssessmentPersistenceAdapter` correctly saves and retrieves an `Assessment` | New `AssessmentPersistenceAdapterTest` (`@DataJpaTest` or Testcontainers, matching existing project convention) — save then `findById`, assert round-trip equality |
| 3 | Existing dashboard endpoint still returns 200 + empty list | `AssessmentControllerTest` (or existing test if any) still passes — `GET /api/v1/assessments` returns `[]` for a teacher with no assessments, same as before this task |
| 4 | `./mvnw test` passes | Full suite green |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up db` (or full `compose.yml`) — Postgres healthy |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors |
| 3 | Connectivity or schema validation succeeds | `V9__add_assessments.sql` applies cleanly against the local DB on startup (Flyway runs automatically) |
| 4 | Changed surface responds correctly | `curl localhost:8080/api/v1/assessments` (with a valid teacher session) returns `200 []` |
| 5 | No startup or migration regressions are visible | App logs show no Flyway or Hibernate validation errors |

### Database / ORM Consistency Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Static database-to-ORM consistency is valid | `assessments` table columns (`id`, `teacher_uid`, `status`, `created_at`) match `AssessmentJpaEntity` fields exactly in name, type, and nullability |
| 2 | Local runtime environment starts | `docker compose up db` then `./mvnw spring-boot:run -Dspring.profiles.active=local` |
| 3 | Persistence smoke check passes | Manually insert one row via the adapter (or a throwaway integration test), confirm it round-trips through `AssessmentPersistenceMapper` unchanged |

---

## Done Criteria

- [ ] `V9__add_assessments.sql` applies cleanly.
- [ ] `Assessment`, `AssessmentId` domain classes exist with no Spring/JPA imports.
- [ ] `AssessmentJpaEntity`, `AssessmentJpaRepository`, `AssessmentPersistenceAdapter`, `AssessmentPersistenceMapper` exist and round-trip correctly.
- [ ] `StubAssessmentPersistenceAdapter` deleted; `AssessmentConfig` wires the real adapter.
- [ ] `GET /api/v1/assessments` still returns `200 []` for a teacher with no assessments (no regression).
- [ ] `./mvnw test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
