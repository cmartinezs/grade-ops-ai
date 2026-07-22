# ⚛️ TASK 11 — mutations-draft-builder-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-10
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

`updateAssessmentDraft()` (partial PATCH) and `regenerateAssessmentDraft()` (POST with adjustment notes) in `lib/api`, each with proper submitting/success/error semantics per `06-estado-datos-y-api.md` §10. The task must confirm whether each mutation is sync or async, and for async regeneration must consume the agreed completion contract instead of inventing operation state in `web/`. Regeneration must also carry the agreed `outputLocale`/`contentLocale` behavior so AI output language is explicit.

---

## Technical Design

- **Approach:** Two separate mutation functions, not one generic "updateDraft" — `PATCH` (edit) and regenerate (`POST .../regenerate`) hit different endpoints with different request shapes (`task-01`) and different semantics (editing the current version in place vs. producing a new version via AI). Both must trigger a refetch of the version list afterward per `06-estado-datos-y-api.md` §13 (sync with backend after a critical mutation) — regenerate always creates a new version; a PATCH edit while viewing a past version, if allowed by the UI, would need the same refresh. If `api/` exposes regenerate as async, this task returns/handles the API operation contract and leaves completion consumption to the page hook in `task-12`; if `api/` has no such endpoint/status model, record the API gap instead of faking it. Regeneration captures `outputLocale`/`contentLocale` at request time so later UI changes do not silently change an in-flight AI request.
- **Affected files / components:**
  - `src/lib/api/assessments.ts` (add `updateAssessmentDraft()`, `regenerateAssessmentDraft()`)
  - `src/lib/api/__tests__/assessments.test.ts` (extend)
- **Interfaces / contracts:**
  ```ts
  export interface UpdateAssessmentDraftRequestDto {
    title?: string;
    context?: string;
    instructions?: string;
    objectives?: string[];
    deliverables?: string[];
    constraints?: string[];
  }
  export async function updateAssessmentDraft(
    assessmentId: string,
    changes: UpdateAssessmentDraftRequestDto
  ): Promise<AssessmentDraftDto>
  export async function regenerateAssessmentDraft(
    assessmentId: string,
    adjustmentNotes: string,
    options?: { outputLocale?: string; contentLocale?: string }
  ): Promise<AssessmentDraftDto>
  ```
  Both return the same `AssessmentDraftDto` shape from `task-10` only if the API contract remains sync. If regenerate is async, update the function shape to the explicit API operation/result contract confirmed in `task-01` or record a blocking residual/API task.
- **Risk:** Medium — `UpdateAssessmentDraftRequestDto`'s fields are all optional per `task-01`'s confirmed contract (`null`/absent means "don't change this field"); a caller that sends an empty string instead of omitting a field would unintentionally blank it out server-side. `updateAssessmentDraft` must only include keys the caller actually changed, not all fields with empty-string defaults.
- **Design notes:** Neither function refetches the version list itself — per `06-estado-datos-y-api.md` §13's own framing, that's a page-level concern; `task-12`'s hook is responsible for calling `loadAssessmentDraftBuilderPage` again (or at least the versions half) after either mutation succeeds. This task only guarantees the two functions themselves are correct.

---

## API / Agent / Web Contract Gate

| Gate | Required check | Task answer |
|---|---|---|
| API as orchestrator | Mutations call `api/` only; regenerate does not call or configure `agents/` from `web` | Keep provider/model/prompt absent from web DTOs and function signatures |
| Richardson REST maturity | `PATCH /draft` is partial update; `POST /draft/regenerate` is command-style generation under the assessment resource | Preserve exact current contract from `task-01`; record that operation-backed `202 Location` is an R01 API follow-up, not a web invention |
| AI operation model | Regeneration is GenAI-backed and must be declared sync or async from the API contract | Do not fabricate `operationId`; async requires API operation/status contract and completion mechanism |
| Idempotency | Regeneration should use `Idempotency-Key` when API supports it; PATCH should avoid duplicate unintended writes via changed-key-only payloads | Check `task-01`; if absent, document API gap and keep UI retry manual/visible |
| Contract testing | Tests assert exact PATCH body, regenerate body, paths and conflict behavior | Extend `assessments.test.ts` |
| Web route functionality | Save/regenerate support submitting, success, conflict, validation and server-error states | `task-12` consumes these errors as distinct UI states |
| API I/O completeness | Every write datum and response/status comes from `api/` | Missing mutation, response field, idempotency or operation status becomes API scope/residual |
| Sync/async completion | Save/regenerate expose the chosen sync result or async operation contract | No local timer, hidden polling or fake completion in `lib/api` |
| i18n contract | Regenerate sends supported `outputLocale`/`contentLocale`; update preserves existing content locale unless API explicitly changes it | No inference from programming language; errors are localized-safe and technical codes remain English |

---

## Implementation Steps

1. Confirm the API I/O matrix for writes: editable draft fields, adjustment notes, response draft/status, error shape, idempotency support and sync/async behavior.
2. Add `UpdateAssessmentDraftRequestDto` to `src/types/assessment.ts` — all fields optional, matching `task-01`'s confirmed `UpdateAssessmentDraftRequest` partial-update semantics exactly (only include keys actually being changed).
3. Add `updateAssessmentDraft(assessmentId, changes)` to `src/lib/api/assessments.ts`, `PATCH`-ing `/api/v1/assessments/${assessmentId}/draft` with only the provided keys.
4. Add `regenerateAssessmentDraft(assessmentId, adjustmentNotes, options)` `POST`-ing `/api/v1/assessments/${assessmentId}/draft/regenerate` with `{ adjustmentNotes }` plus supported locale fields/headers if sync, or returning the explicit API operation/result shape if async.
5. If async, document and expose the completion mechanism (`GET /operations/{id}` polling, SSE, WebSocket, webhook/push or another agreed contract) for `task-12`; if missing in `api/`, create/track the API gap before closing this task.
6. Write tests: `updateAssessmentDraft` sends only the changed keys (not a full object with empty-string defaults); `regenerateAssessmentDraft` sends the notes plus supported locale metadata and parses the new draft or operation result; both surface 409 (conflict) distinctly from other errors per `06-estado-datos-y-api.md` §9.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `updateAssessmentDraft` sends only the caller-provided keys, never blanking unspecified fields | `npm run test -- assessments` |
| 2 | `regenerateAssessmentDraft` sends `{ adjustmentNotes }` and returns the new `AssessmentDraftDto` with an incremented `versionNumber` | `npm run test -- assessments` |
| 3 | Both functions surface a 409 response as a distinguishable conflict error | `npm run test -- assessments` |
| 4 | Mutation functions expose only API-backed write/result/operation data and chosen sync/async behavior | `npm run test -- assessments` plus manual review against task-01 |
| 5 | Regeneration locale metadata is sent only through the confirmed API contract and stays separate from programming language | `npm run test -- assessments` plus manual review |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | No startup regressions are visible | Inspect `npm run dev` output for new errors |

### Database / ORM Consistency Check

N/A — no database or ORM involved in `web/`.

### Logging / Observability

- **Logging mechanism:** Same project-level decision as `task-05`/`task-10` — reuse whatever was resolved there; do not re-decide.
- **Correlation / trace context:** Each mutation call gets its own correlation id (they're independent user actions, not part of one page-load operation like `task-10`'s loader).
- **Levels by event criticality:** INFO on successful update/regenerate; WARN on 409 conflict (recoverable — teacher can refresh and retry); ERROR on 500.
- **Execution trace points:** Mutation entry, outbound call (dependency, status, latency), completion/failure.
- **Sensitive data guardrails:** Do not log full draft text or adjustment notes content; log `assessmentId`, `versionNumber`, and status only.
- **Verification evidence:** A test or manual log sample showing each mutation logged with its own correlation id and outcome.

### Generated Test Suite

- **Task suite file:** `test-suites/task-11-mutations-draft-builder-screen-test-suite.md`
- **Required gates:** unit, coverage, static analysis (`npm run lint`), code style, architecture/design guide review.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md` §9-10 (errors, mutations), `15-backend-frontend-contracts.md`.
- **Acceptance environment:** N/A — unit-level mocking of `apiClient`/`fetch` is sufficient.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Done Criteria

- [ ] `updateAssessmentDraft` only sends caller-provided keys, matching the partial-update contract.
- [ ] `regenerateAssessmentDraft` sends adjustment notes and returns the new draft.
- [ ] API I/O gaps for write payloads, response fields, idempotency or operation status are recorded as API scope/residual before task-12.
- [ ] Sync/async contract is implemented or residualized; async completion is not simulated inside `lib/api`.
- [ ] i18n mutation contract is implemented or residualized: regeneration captures supported `outputLocale`/`contentLocale`, update preserves content locale semantics, and safe localized errors are mapped.
- [ ] API / Agent / Web Contract Gate is completed; regenerate does not leak agent/provider/prompt concerns into `web`.
- [ ] Both distinguish 409 conflicts from other errors.
- [ ] All new/extended tests pass; `npm run lint` passes.
- [ ] Software smoke test check above passes (build/startup confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section.
- [ ] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
