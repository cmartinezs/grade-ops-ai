# ⚛️ TASK 09 — Draft edit endpoint

> **Status:** TODO
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

- [ ] `PATCH /api/v1/assessments/{id}/draft` updates the current draft's row in place — no new version, no new log.
- [ ] Edit without a prior draft returns a clean 409/422.
- [ ] `./mvnw test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
