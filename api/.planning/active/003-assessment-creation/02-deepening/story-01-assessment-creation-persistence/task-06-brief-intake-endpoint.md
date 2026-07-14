# ⚛️ TASK 06 — Brief intake endpoint

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

`POST` endpoint that creates an `Assessment` (status `DRAFT`) and its `AssessmentBrief` in one transaction, before any agent call — satisfying US-010's DoD that a failed agent call never loses the teacher's input.

---

## Technical Design

- **Approach:** a single use case (`CreateAssessmentBriefHandler`) creates both the `Assessment` and `AssessmentBrief` rows in one `@Transactional` method — no agent call happens in this request at all (draft generation is a separate, subsequent request, task-07). This directly satisfies the epic DoD: persistence and agent invocation are two separate requests/transactions, never combined.
- **Affected files / components:**
  - `assessment/application/command/CreateAssessmentBriefCommand.java` (new)
  - `assessment/application/port/in/CreateAssessmentBriefUseCase.java` (new)
  - `assessment/application/usecase/CreateAssessmentBriefHandler.java` (new)
  - `assessment/application/result/CreateAssessmentBriefResult.java` (new — returns `assessmentId`)
  - `assessment/infrastructure/adapter/in/web/AssessmentController.java` (**modify** — add `POST /api/v1/assessments`)
  - `assessment/infrastructure/adapter/in/web/request/CreateAssessmentBriefRequest.java` (new — `@Valid` with `@NotBlank` on all 5 fields)
  - `assessment/infrastructure/adapter/in/web/response/CreateAssessmentBriefResponse.java` (new)
  - `assessment/infrastructure/config/AssessmentConfig.java` (**modify** — wire the new handler bean)
- **Interfaces / contracts:** `POST /api/v1/assessments` — body `{learningGoal, topic, level, duration, language}`, response `{assessmentId}` (201 Created). Requires an authenticated teacher (`AuthenticatedTeacher` from `SecurityContextHolder`, matching `AssessmentController`'s existing pattern).
- **Risk:** Low — routine create-two-rows-in-one-transaction flow; main risk is a partial write (assessment created, brief fails) — mitigated by `@Transactional` wrapping both saves.
- **Design notes:** validation (`@NotBlank` etc.) happens at the request-DTO level via `spring-boot-starter-validation` (already a project dependency) — reject before the handler runs, not inside the handler. **Corrected 2026-07-13 (reality check before implementation):** the shared `GlobalExceptionHandler` (`shared/infrastructure/adapter/in/web/GlobalExceptionHandler.java`) already maps every `MethodArgumentNotValidException` — exactly what a failed `@Valid` produces — to `422 UNPROCESSABLE_CONTENT`, applied uniformly to every validated endpoint in the app (not 400). This task follows that existing convention rather than adding a one-off 400 handler for just this endpoint, which would make validation-error status codes inconsistent across the API. All "400" references below (Verification #2, Done Criteria) mean 422 in practice.

---

## Implementation Steps

1. Create `CreateAssessmentBriefCommand.java` (mirrors the request fields).
2. Create `CreateAssessmentBriefUseCase.java` (port in).
3. Create `CreateAssessmentBriefHandler.java` — `@Transactional`: build `Assessment` (status `DRAFT`, `teacherUid` from the command), save via `AssessmentRepositoryPort`; build `AssessmentBrief` FK'd to the new assessment id, save via `AssessmentBriefRepositoryPort`; return `CreateAssessmentBriefResult(assessmentId)`.
4. Create `CreateAssessmentBriefResult.java`.
5. Create `CreateAssessmentBriefRequest.java` with Bean Validation annotations.
6. Create `CreateAssessmentBriefResponse.java`.
7. Add `POST /api/v1/assessments` to `AssessmentController.java`, reading `AuthenticatedTeacher` the same way `listAssessments()` does.
8. Wire `CreateAssessmentBriefHandler` as a `@Bean` in `AssessmentConfig.java`.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Valid request creates both rows and returns 201 + assessmentId | `MockMvc`/`@WebMvcTest` (or full `@SpringBootTest` with Testcontainers) posting a valid brief |
| 2 | Invalid request (blank field) returns 400 | Test posting with a blank `learningGoal` |
| 3 | Unauthenticated request is rejected | Test without a valid teacher session, assert 401 |
| 4 | `./mvnw test` passes | Full suite green |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up db` |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors |
| 3 | Connectivity or schema validation succeeds | No new migration in this task — uses `V9`/`V10` from task-01/02 |
| 4 | Changed surface responds correctly | `curl -X POST localhost:8080/api/v1/assessments -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"learningGoal":"...", "topic":"...", "level":"...", "duration":"...", "language":"..."}'` returns 201 with an `assessmentId` |
| 5 | No startup or migration regressions are visible | App logs clean |

### Database / ORM Consistency Check

N/A — no schema change in this task (reuses `V9`/`V10`).

---

## Done Criteria

- [x] `POST /api/v1/assessments` creates an `Assessment` (status `DRAFT`) and `AssessmentBrief` in one transaction.
- [x] No agent call happens in this request.
- [x] Invalid/blank fields are rejected with 422 (project-wide `MethodArgumentNotValidException` convention — see corrected Design notes above) before persistence is attempted.
- [x] Unauthenticated requests are rejected with 401.
- [x] `./mvnw test` passes.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
