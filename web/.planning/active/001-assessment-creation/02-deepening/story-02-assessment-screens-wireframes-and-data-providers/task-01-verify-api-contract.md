# ⚛️ TASK 01 — verify-api-contract

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A confirmed, written record of the exact request/response shape for every `api/` endpoint this story's two screens will call, read directly from `api/`'s controller and DTO source — not from `docs/04-architecture/api-design.md` alone, which was already found stale relative to `api/`'s real implementation during `api/003-assessment-creation`'s own execution.

---

## Technical Design

- **Approach:** Read `AssessmentController.java` and every request/response record it references directly from source, transcribe the exact field names/types/HTTP methods/paths into this task's Verification section, and flag any endpoint the story's Context section assumes that does not actually exist. This is a read-only verification task — it produces no application code, only a verified reference other tasks depend on.
- **Affected files / components:** None created or modified in `web/`. Read-only against:
  - `api/src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/AssessmentController.java`
  - `api/src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/request/CreateAssessmentBriefRequest.java`
  - `api/src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/request/RegenerateAssessmentDraftRequest.java`
  - `api/src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/request/UpdateAssessmentDraftRequest.java`
  - `api/src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/response/CreateAssessmentBriefResponse.java`
  - `api/src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/response/GenerateAssessmentDraftResponse.java`
- **Interfaces / contracts:** This task's own output (the verified shapes below) is the interface every downstream data-provider task (`task-05`, `task-10`, `task-11`) is written against. Downstream tasks must not invent fields beyond what is recorded here.
- **Risk:** Low if `api/` hasn't changed since this planning's Context section was written (2026-07-15); re-verify by re-reading the files above rather than trusting the cached shapes below, since `api/` is a sibling repo that can change independently of `web/`.
- **Design notes:** There is **no endpoint to restore a past draft version as current** — do not let any downstream task assume one exists. `UpdateAssessmentDraftRequest` fields are all optional/partial (`null` means "don't change this field"); this is a deliberate partial-update contract per its own Javadoc, not an oversight.

---

## Implementation Steps

1. Read `AssessmentController.java` in full; list every `@GetMapping`/`@PostMapping`/`@PatchMapping` method, its path, and its request/response types.
2. Read each request/response record referenced above; transcribe field name, Java type, and any validation annotation (`@NotBlank`, `@Size`).
3. Cross-check the transcribed shapes against this story's own Context section (`../story-02-assessment-screens-wireframes-and-data-providers.md` § Context) and this task's Verification table below; flag and correct any mismatch found.
4. Record the confirmed shapes in the Verification table below (already pre-filled from the 2026-07-15 reading — re-verify, don't skip).

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `POST /api/v1/assessments` accepts `{learningGoal, topic, level, duration, language}` (all `@NotBlank` strings) and returns `{assessmentId: string}` | Re-read `CreateAssessmentBriefRequest.java` + `CreateAssessmentBriefResponse.java`; confirm field names/types match |
| 2 | `POST /api/v1/assessments/{id}/draft` takes no body and returns `{draftId: UUID, title, context, instructions, objectives: string[], deliverables: string[], constraints: string[], versionNumber: int}` | Re-read `AssessmentController.generateDraft()` + `GenerateAssessmentDraftResponse.java` |
| 3 | `POST /api/v1/assessments/{id}/draft/regenerate` accepts `{adjustmentNotes: string}` (`@NotBlank`) and returns the same `GenerateAssessmentDraftResponse` shape as #2 | Re-read `RegenerateAssessmentDraftRequest.java` + `AssessmentController.regenerateDraft()` |
| 4 | `PATCH /api/v1/assessments/{id}/draft` accepts `{title?, context?, instructions?, objectives?: string[], deliverables?: string[], constraints?: string[]}` — every field optional, `null` means unchanged — and returns the same `GenerateAssessmentDraftResponse` shape | Re-read `UpdateAssessmentDraftRequest.java` (note its own Javadoc on `null` semantics) + `AssessmentController.updateDraft()` |
| 5 | `GET /api/v1/assessments/{id}/draft` returns the current draft in the same `GenerateAssessmentDraftResponse` shape | Re-read `AssessmentController.getCurrentDraft()` |
| 6 | `GET /api/v1/assessments/{id}/draft/versions` returns `GenerateAssessmentDraftResponse[]`, one entry per version, newest included | Re-read `AssessmentController.listDraftVersions()` |
| 7 | No endpoint exists to restore/rollback a past draft version as current | Confirm `AssessmentController.java`'s full method list contains no such mapping |

### Software Smoke Test Check

N/A — this task makes no runtime changes to `web/`; it produces a verified reference document only. No app build/start is required to validate it.

### Database / ORM Consistency Check

N/A — no database or ORM artifact is touched by this task.

### Logging / Observability

N/A — this task produces no executable code.

### Generated Test Suite

- **Task suite file:** `test-suites/task-01-verify-api-contract-test-suite.md`
- **Required gates:** architecture/design guide review only (confirm the transcribed shapes match `api/` source verbatim). Unit, coverage, integration, acceptance/e2e, static analysis, code style, smoke, security, and mutation gates do not apply — no code is produced.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/15-backend-frontend-contracts.md` (contract-mirroring rules), `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md` §5 (DTO rules).
- **Acceptance environment:** N/A.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Done Criteria

- [ ] All 7 verification rows above are re-confirmed directly against current `api/` source (not assumed from this story's Context section alone).
- [ ] Any mismatch found between the Context section and the actual `api/` source is corrected in both this task file and the story's Context section before `task-05`/`task-10`/`task-11` start.
- [ ] Software smoke/build/startup/connectivity checks: N/A, no runtime surface (see Software Smoke Test Check); for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [ ] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
