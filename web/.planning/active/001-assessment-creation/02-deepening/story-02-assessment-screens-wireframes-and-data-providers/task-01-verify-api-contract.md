# ⚛️ TASK 01 — verify-api-contract

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A confirmed, written record of the exact request/response shape for every `api/` endpoint this story's two screens will call, read directly from `api/`'s controller and DTO source — plus data-semantics, API I/O, sync/async and i18n checks so downstream UI tasks do not invent data, statuses, locale behavior or completion behavior locally.

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
- **Data semantics:** Also read `docs/04-architecture/data-model.md`, `docs/06-ux/teacher-workspace-ux.md` and `docs/master-plan/analysis/ui-design-data-strategy.md`. Record whether each intake field is free text, enum, numeric, catalog/master-data or controlled custom, and whether `api/` currently exposes the necessary source of truth.
- **API I/O:** Record every datum each screen needs to read or write: catalogs/defaults/capabilities, create brief mutation, generate/regenerate command, current draft, version list, operation status and error shapes. If a required API does not exist, record it as a gap for `api/` planning, not as a `web/` mock.
- **Sync/async:** For each mutation, record whether current API behavior is sync or async. If generation/regeneration is or becomes async, document the required completion mechanism before downstream tasks proceed.
- **i18n:** Record the locale contract across `web`/`api`/`agents`: source-code fields and DTO names stay in English, user-facing values/messages/catalog labels may be localized, `web` must send or resolve the effective UI/content locale, GenAI generation must receive an explicit `outputLocale`/`contentLocale` when supported, and logs/telemetry/codes remain in English.
- **Risk:** Low if `api/` hasn't changed since this planning's Context section was written (2026-07-15); re-verify by re-reading the files above rather than trusting the cached shapes below, since `api/` is a sibling repo that can change independently of `web/`.
- **Design notes:** There is **no endpoint to restore a past draft version as current** — do not let any downstream task assume one exists. `UpdateAssessmentDraftRequest` fields are all optional/partial (`null` means "don't change this field"); this is a deliberate partial-update contract per its own Javadoc, not an oversight.

---

## API / Agent / Web Contract Gate

| Gate | Required check | Task answer |
|---|---|---|
| API as orchestrator | Confirm `web/` calls only `api/` functional endpoints and no `agents/` URL/provider/prompt contract leaks into the screen plan | Re-read the controller/client contract and record any leak as a blocker before downstream tasks start |
| Richardson REST maturity | Record resource URI, method, expected status behavior, validation errors and unsupported transitions for all six endpoints | Add the maturity notes to the Verification result; call out that the current implemented draft-generation endpoints return `200` sync responses and do not yet expose `AiOperation`/`Location` |
| AI operation model | Identify whether each endpoint is sync-only today or operation-backed | Current task must mark generation/regeneration as sync legacy in the verified contract and note the R01 API-orchestration follow-up |
| Idempotency | Confirm whether mutating GenAI endpoints require/admit `Idempotency-Key` | Current task must record the absence/presence explicitly; if absent, downstream web code must not fake idempotency silently |
| Contract testing | Verify DTOs directly from `api/` source and identify the contract-test gap | Architecture-review evidence only in this task; downstream implementation tasks must add tests against these shapes |
| Web route functionality | Ensure endpoint contract supports `/assessments/new` and `/assessments/{id}/draft` states without invented actions | Explicitly preserve no-restore behavior and safe error states |
| API I/O completeness | Identify all read/write data, catalogs/defaults/capabilities, mutations, operation states and errors needed by the screens | Record missing API support as blocker/residual for downstream tasks |
| Sync/async completion | Decide whether create/generate/regenerate are sync or async in the current contract | If async, require operation polling/SSE/WebSocket/webhook/push contract before UI wiring |
| i18n contract | Confirm locale negotiation, safe localized error shape, localized catalog labels and GenAI output locale support | Record `Accept-Language`/profile/tenant fallback behavior, `outputLocale`/`contentLocale` support or API gaps, and keep logs/telemetry/error codes in English |

## UI Design/Data Semantics Gate

| Field | Required check | Task answer |
|---|---|---|
| `learningGoal` | Confirm whether domain treats it as long free text and identify max length/sanitization evidence | Record `Textarea`/long-text expectation or API gap |
| `topic` | Detect whether this is free text, curriculum tag or master data (`Subject`/`Topic`/learning outcome) | Record source of truth or residual; downstream UI must not assume unrestricted `Input` |
| `level` | Confirm enum/difficulty values or absence of enum endpoint | Record controlled values/gap; downstream UI uses `Select`/`Radio` |
| `duration` | Confirm numeric minutes/preset semantics vs current DTO string | Record numeric/unit gap; downstream UI uses numeric/preset control |
| `language` | Confirm `ProgrammingLanguage`/pseudocode values or catalog source | Record controlled values/gap; downstream UI uses selector/catalog |

## Sync/Async Contract Gate

| Action | Required check | Task answer |
|---|---|---|
| Create brief | Confirm sync response, timeout/error behavior and returned `assessmentId` | Record current behavior and gaps |
| Generate draft | Confirm sync response vs operation-backed async | If async, record completion model and operation/status endpoint |
| Load draft | Confirm data source for current draft and versions | Record read endpoints and stale/missing states |
| Regenerate draft | Confirm sync response vs operation-backed async and idempotency | If async, record completion model, retry/cancel and error states |

## i18n Contract Gate

| Gate | Required check | Task answer |
|---|---|---|
| Locale source | Confirm whether `web` sends `Accept-Language`, an explicit locale field, profile preference or tenant default | Record the precedence and fallback path; if absent, create an API/web residual before implementation |
| Contract language | Confirm DTO field names/codes remain English while user-facing values/messages are localized | Downstream DTOs must not translate source-code field names |
| Catalog labels | Confirm how `topic`, `level`, `duration` presets and `language` labels are localized while stable values remain code-safe | Missing catalog-label API support is an API/master-data gap |
| GenAI output | Confirm how draft generation/regeneration receives `outputLocale`/`contentLocale`, separate from the programming `language` field | If unsupported, record an R01 API/agents gap; web must not infer generated-content language from programming language |
| Observability | Confirm logs, metrics, traces, event names, error codes and warning codes stay in English | Locale may be recorded only as low-cardinality attributes such as `requested_locale`/`effective_locale` |

---

## Implementation Steps

1. Read `AssessmentController.java` in full; list every `@GetMapping`/`@PostMapping`/`@PatchMapping` method, its path, and its request/response types.
2. Read each request/response record referenced above; transcribe field name, Java type, and any validation annotation (`@NotBlank`, `@Size`).
3. Read the domain/UX sources named in the Data semantics note and classify each intake field.
4. Build an API I/O matrix for Intake and Draft Builder: each read/write datum, endpoint/API owner, missing endpoint/catalog/mutation/operation status, and residual path.
5. Record sync/async behavior for create brief, generate draft, update draft and regenerate draft. If any is async, record completion/progress mechanism and UI states.
6. Record the i18n matrix: locale source/precedence, localized user-facing values/messages, catalog label ownership, `outputLocale`/`contentLocale` support, and observability language boundaries.
7. Cross-check the transcribed shapes against this story's own Context section (`../story-02-assessment-screens-wireframes-and-data-providers.md` § Context) and this task's Verification table below; flag and correct any mismatch found.
8. Record the confirmed shapes in the Verification table below (already pre-filled from the 2026-07-15 reading — re-verify, don't skip), plus the field-semantics matrix and any API/catalog/operation/i18n gap.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `POST /api/v1/assessments` accepts `{learningGoal, topic, level, duration, language}` (all `@NotBlank` strings at the transport DTO boundary only) and returns `{assessmentId: string}` | Re-read `CreateAssessmentBriefRequest.java` + `CreateAssessmentBriefResponse.java`; confirm field names/types match |
| 2 | `POST /api/v1/assessments/{id}/draft` takes no body and returns `{draftId: UUID, title, context, instructions, objectives: string[], deliverables: string[], constraints: string[], versionNumber: int}` | Re-read `AssessmentController.generateDraft()` + `GenerateAssessmentDraftResponse.java` |
| 3 | `POST /api/v1/assessments/{id}/draft/regenerate` accepts `{adjustmentNotes: string}` (`@NotBlank`) and returns the same `GenerateAssessmentDraftResponse` shape as #2 | Re-read `RegenerateAssessmentDraftRequest.java` + `AssessmentController.regenerateDraft()` |
| 4 | `PATCH /api/v1/assessments/{id}/draft` accepts `{title?, context?, instructions?, objectives?: string[], deliverables?: string[], constraints?: string[]}` — every field optional, `null` means unchanged — and returns the same `GenerateAssessmentDraftResponse` shape | Re-read `UpdateAssessmentDraftRequest.java` (note its own Javadoc on `null` semantics) + `AssessmentController.updateDraft()` |
| 5 | `GET /api/v1/assessments/{id}/draft` returns the current draft in the same `GenerateAssessmentDraftResponse` shape | Re-read `AssessmentController.getCurrentDraft()` |
| 6 | `GET /api/v1/assessments/{id}/draft/versions` returns `GenerateAssessmentDraftResponse[]`, one entry per version, newest included | Re-read `AssessmentController.listDraftVersions()` |
| 7 | No endpoint exists to restore/rollback a past draft version as current | Confirm `AssessmentController.java`'s full method list contains no such mapping |
| 8 | Intake field semantics are recorded and any `topic`/`level`/`duration`/`language` source-of-truth gap is explicit | Cross-check `docs/04-architecture/data-model.md`, `docs/06-ux/teacher-workspace-ux.md` and `ui-design-data-strategy.md` |
| 9 | API I/O matrix records all screen reads/writes and missing `api/` support | Manual review of task output |
| 10 | Sync/async behavior and completion model are recorded for create/generate/update/regenerate | Manual review against controller and API orchestration strategy |
| 11 | i18n matrix records locale negotiation, localized catalog/error/content behavior, `outputLocale`/`contentLocale` support, and English-only observability boundary | Manual review against `docs/master-plan/analysis/i18n-strategy.md` |

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

- [ ] All 10 verification rows above are re-confirmed directly against current `api/` source and domain/API orchestration docs (not assumed from this story's Context section alone).
- [ ] Any mismatch found between the Context section and the actual `api/` source is corrected in both this task file and the story's Context section before `task-05`/`task-10`/`task-11` start.
- [ ] API / Agent / Web Contract Gate is completed, including Richardson REST notes and current idempotency/operation-model gaps.
- [ ] UI Design/Data Semantics Gate is completed with a field matrix for `learningGoal`, `topic`, `level`, `duration`, `language`, including master-data/catalog/API residuals where needed.
- [ ] API I/O matrix is completed for Intake and Draft Builder; no downstream task relies on permanent local fixtures for required data.
- [ ] Sync/async contract is recorded; any async flow has completion/progress mechanism, state model, timeout and retry/cancel expectations.
- [ ] i18n Contract Gate is completed: locale precedence, localized labels/messages/catalog values, `outputLocale`/`contentLocale`, programming `language` separation and English-only observability are recorded.
- [ ] Software smoke/build/startup/connectivity checks: N/A, no runtime surface (see Software Smoke Test Check); for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [ ] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
