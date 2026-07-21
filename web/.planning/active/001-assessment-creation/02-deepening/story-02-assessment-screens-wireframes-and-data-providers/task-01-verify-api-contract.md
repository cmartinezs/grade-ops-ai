# ⚛️ TASK 01 — verify-api-contract

> **Status:** DONE
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

## API / Agent / Web Contract Gate

> Added to `develop` post-divergence (commit `e2703d5`, 2026-07-20); reconciled into this already-DONE task during story-02 closeout (2026-07-21) with real evidence, not left as the generic prescriptive text.

| Gate | Required check | Task answer |
|---|---|---|
| API as orchestrator | Confirm `web/` calls only `api/` functional endpoints and no `agents/` URL/provider/prompt contract leaks into the screen plan | **Confirmed.** The 7-method exhaustive controller listing in § Verification Summary contains no `agents/` URL, provider name, model name, or prompt reference anywhere — every endpoint is a plain `assessments`/`draft` resource route. No downstream task (05/06/10/11/12) DTO carries an agent/provider/prompt field either (cross-checked against each task's own Verification Summary). |
| Richardson REST maturity | Record resource URI, method, expected status behavior, validation errors and unsupported transitions for all six endpoints | Level 2 (resources + HTTP verbs), not Level 3/HATEOAS: all 6 endpoints are plain resource routes (`/assessments`, `/assessments/{id}/draft`, `/assessments/{id}/draft/versions`, `/assessments/{id}/draft/regenerate`) using standard GET/POST/PATCH. Draft generation/regeneration return the full `GenerateAssessmentDraftResponse` body synchronously (`200`/`201`) — no `202 Accepted` + `Location` header, no `AiOperation` polling handle anywhere in the transcribed response records. |
| AI operation model | Identify whether each endpoint is sync-only today or operation-backed | Sync legacy, confirmed by the verbatim response records above (`GenerateAssessmentDraftResponse` returns the full generated draft directly, not an operation id/status). The operation-backed contract (R01 follow-up) does not exist in `api/` as of this planning; no downstream task fakes one. |
| Idempotency | Confirm whether mutating GenAI endpoints require/admit `Idempotency-Key` | **Absent.** None of the 4 request records transcribed above (`CreateAssessmentBriefRequest`, `RegenerateAssessmentDraftRequest`, `UpdateAssessmentDraftRequest`, plus the no-body draft-generation call) declare an `Idempotency-Key` header or equivalent field. Confirmed no downstream task fakes one: `task-06`/`task-12` never auto-retry a failed generate/regenerate call — every retry in the real UI is an explicit teacher-initiated resubmit. |
| Contract testing | Verify DTOs directly from `api/` source and identify the contract-test gap | Closed by downstream tasks, not left open: `task-05`'s `CreateAssessmentBriefRequestDto`/`ResponseDto` and `task-10`'s `AssessmentDraftDto` were each verified field-for-field against these exact records (see those tasks' own Verification Summary sections) and covered by `assessments.test.ts`'s unit tests. |
| Web route functionality | Ensure endpoint contract supports `/assessments/new` and `/assessments/{id}/draft` states without invented actions | Confirmed via `task-07`/`task-08`'s wireframe/hierarchy docs: no restore/rollback affordance exists anywhere in `web/` — `VersionHistorySectionProps` only exposes `onViewVersion` (read-only), matching this task's own row-7 exhaustive-listing finding that no such endpoint exists. |

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

## Verification Summary

Re-read directly from `api/` at commit `0a23627aa2d737f233e0a8a2be864f7187725139` (2026-07-15). Raw evidence below — not a paraphrase.

**Controller mappings** (`AssessmentController.java`, full method list — 7 total, confirming row 7 by exhaustion):

```
@GetMapping("/assessments")                          -> listAssessments()      -> List<AssessmentSummaryResponse>
@PostMapping("/assessments")                         -> createAssessmentBrief() -> CreateAssessmentBriefResponse
@PostMapping("/assessments/{id}/draft")               -> generateDraft()        -> GenerateAssessmentDraftResponse
@PostMapping("/assessments/{id}/draft/regenerate")    -> regenerateDraft()      -> GenerateAssessmentDraftResponse
@PatchMapping("/assessments/{id}/draft")              -> updateDraft()          -> GenerateAssessmentDraftResponse
@GetMapping("/assessments/{id}/draft")                -> getCurrentDraft()      -> GenerateAssessmentDraftResponse
@GetMapping("/assessments/{id}/draft/versions")       -> listDraftVersions()    -> List<GenerateAssessmentDraftResponse>
```

No 8th mapping exists — confirms Verification row 7 (no restore/rollback endpoint) by exhaustive listing, not by absence-of-evidence.

**Request records** (verbatim):

```java
public record CreateAssessmentBriefRequest(
    @NotBlank String learningGoal,
    @NotBlank String topic,
    @NotBlank String level,
    @NotBlank String duration,
    @NotBlank String language
) {}

public record RegenerateAssessmentDraftRequest(@NotBlank String adjustmentNotes) {}

public record UpdateAssessmentDraftRequest(
    @Size(min = 1, message = "must not be blank if provided") String title,
    @Size(min = 1, message = "must not be blank if provided") String context,
    @Size(min = 1, message = "must not be blank if provided") String instructions,
    List<@NotBlank String> objectives,
    List<@NotBlank String> deliverables,
    List<@NotBlank String> constraints
) {}
```

**Response records** (verbatim):

```java
public record CreateAssessmentBriefResponse(String assessmentId) {}

public record GenerateAssessmentDraftResponse(
    UUID draftId,
    String title,
    String context,
    String instructions,
    List<String> objectives,
    List<String> deliverables,
    List<String> constraints,
    int versionNumber
) {}
```

**Diff against this task's Verification table (rows 1-7):** none — every field name, type, and annotation above matches what rows 1-7 already asserted. No correction was required in this task file or in the story's Context section.

---

## Done Criteria

- [x] All 7 verification rows above are re-confirmed directly against current `api/` source (not assumed from this story's Context section alone) — see § Verification Summary for the raw controller mappings and record definitions transcribed on 2026-07-15 at `api/` commit `0a23627aa2d737f233e0a8a2be864f7187725139`.
- [x] Any mismatch found between the Context section and the actual `api/` source is corrected in both this task file and the story's Context section before `task-05`/`task-10`/`task-11` start — see § Verification Summary's "Diff against this task's Verification table" line: none found, nothing to correct.
- [x] API / Agent / Web Contract Gate is completed, including Richardson REST notes and current idempotency/operation-model gaps — reconciled 2026-07-21 (story-02 closeout) after this gate was added to `develop` post-divergence (commit `e2703d5`); see § API / Agent / Web Contract Gate for the evidence-filled table.
- [x] Software smoke/build/startup/connectivity checks: N/A, no runtime surface (see Software Smoke Test Check); for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR. PR #66 (`tasks/story-02-.../task-01-verify-api-contract` → `story-02-assessment-screens-wireframes-and-data-providers`) opened, reviewed, approved, and merged 2026-07-15 (merge commit `911557b`).
- [x] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [x] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — already validated during `/plan-atomize` (2026-07-15), no scope creep since.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
