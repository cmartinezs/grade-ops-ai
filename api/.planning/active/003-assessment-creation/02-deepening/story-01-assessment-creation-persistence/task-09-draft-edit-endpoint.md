# ⚛️ TASK 09 — Draft edit endpoint

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-07
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

`PATCH`/`PUT` endpoint letting the teacher edit the current draft's fields directly — no agent call, no new version, no new `AgentExecutionLog`.

---

## Technical Design

- **Approach:** unlike task-08 (which always creates a new version), editing **updates the current version's row in place** — this is a deliberate, distinct persistence pattern from the append-only regeneration flow, since a manual teacher edit is not an AI execution and does not need version history in this epic's scope. `UpdateAssessmentDraftHandler` loads the current draft, applies the changed fields, saves (JPA dirty-checking `UPDATE`, not `INSERT`).
- **Affected files / components:**
  - `assessment/application/command/UpdateAssessmentDraftCommand.java`, `application/port/in/UpdateAssessmentDraftUseCase.java`, `application/usecase/UpdateAssessmentDraftHandler.java` (new)
  - `assessment/infrastructure/adapter/in/web/AssessmentController.java` (**modify** — add `PATCH /api/v1/assessments/{id}/draft`)
  - request/response DTOs (all 6 draft fields optional/partial in the request — only provided fields are updated)
- **Interfaces / contracts:** `PATCH /api/v1/assessments/{id}/draft` — body: any subset of `{title, context, instructions, objectives, deliverables, constraints}`, response: the updated current draft.
- **Risk:** Low — routine partial-update flow; main risk is accidentally triggering a new version row instead of an in-place update — mitigated by an explicit test asserting the version number and row id are unchanged after an edit.
- **Design notes:** if no current draft exists, return 409/422 (same as task-08 — editing requires a prior generation).
- **Implemented 2026-07-14 — clarifying the "in-place update" mechanism:** `AssessmentDraft` has no setters (all domain fields are set only via the private constructor + static factories, matching every other aggregate in this story). Rather than adding mutation methods, added `AssessmentDraft.applyEdit(...)` — returns a **new** domain object that reuses the current draft's `id`/`versionNumber`/`previousVersionId`/`agentExecutionLogId`/`createdAt`, with any non-null parameter overriding the corresponding field (`null` keeps the current value, giving the partial-update semantics the request DTO needs). Calling `assessmentDraftRepository.save(...)` with this object produces a genuine SQL `UPDATE`, not an `INSERT` — Spring Data JPA's `save()` upserts by id, and the id already exists in the DB — the exact same mechanism task-07 already relies on to back-fill `AgentExecutionLog.draftId`. This reconciles with task-03's own inline doc, which describes `AssessmentDraftRepositoryPort.save` as "insert-only" — that statement describes this story's design *intent* (every other call site always passes a brand-new id), not a technical limitation of the port method itself; task-09 is the first call site that intentionally reuses an existing id to get an update. No new port method or domain mutation was needed. Reused `NoPriorDraftException` (from task-08) rather than adding a new exception type, and reused `GenerateAssessmentDraftResponse`/`GenerateAssessmentDraftResult` (already exactly the right shape) rather than duplicating them for this endpoint.
- **Review fix 2026-07-14 — NPE risk in `AssessmentController`:** all 5 endpoints (including the 4 pre-existing ones from tasks 06–08) repeated a raw `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` cast, flagged by static analysis as a possible `NullPointerException`. Deduplicated into a single `currentTeacher()` helper using `Optional.ofNullable(...).map(...).filter(...).map(...).orElseThrow(...)`. The `orElseThrow` branch throws a new `MissingAuthenticationException` (`shared/infrastructure/exception`, extends `InfrastructureException` → existing generic 500 mapping) rather than a raw `IllegalStateException`, per this project's own convention of never throwing native Java exceptions from application/infrastructure code.
- **Review fix 2026-07-14 — missing Bean Validation on `UpdateAssessmentDraftRequest`:** the DTO had no `jakarta.validation` annotations and the controller had no `@Valid`, unlike every other request DTO in this controller. Added `@Size(min = 1)` on `title`/`context`/`instructions` (Bean Validation's own convention: `null` is always valid regardless of the constraint, so an absent field still means "don't change this field" — only an explicitly-sent `""` is rejected) and `List<@NotBlank String>` on `objectives`/`deliverables`/`constraints` (rejects a blank element within a provided list, while `null` — meaning "don't change this list" — stays valid). Added `@Valid` to the controller parameter, matching task-06's established "reject at the DTO boundary, before the handler runs" convention.

---

## Implementation Steps

1. Create `UpdateAssessmentDraftHandler.java`: load the current draft (404/409 if none), apply only the fields present in the command, save via `AssessmentDraftRepositoryPort.save(...)` on the **existing** entity (update, not insert).
2. Add `PATCH /api/v1/assessments/{id}/draft` to `AssessmentController.java`.
3. Create request/response DTOs with all fields optional.
4. Wire the new handler in `AssessmentConfig.java`.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Edit updates the current draft in place, same row id and version number | Integration test: generate v1, edit a field, assert the row id and `versionNumber` are unchanged, only the edited field(s) differ |
| 2 | Edit does not create a new `AgentExecutionLog` | Test: assert the log count is unchanged after an edit |
| 3 | Edit without a prior draft is rejected | Test on an assessment with no draft yet, assert 409/422 |
| 4 | `./mvnw test` passes | Full suite green |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up db` |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors |
| 3 | Connectivity or schema validation succeeds | No new migration in this task |
| 4 | Changed surface responds correctly | Generate a draft, then `curl -X PATCH .../draft -d '{"title":"New title"}'`, confirm the response reflects the change and the version number is unchanged |
| 5 | No startup or migration regressions are visible | App logs clean |

### Database / ORM Consistency Check

N/A — no schema change in this task.

---

## Done Criteria

- [x] `PATCH /api/v1/assessments/{id}/draft` updates the current draft's row in place — no new version, no new log.
- [x] Edit without a prior draft returns a clean 409/422.
- [x] `./mvnw test` passes.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
