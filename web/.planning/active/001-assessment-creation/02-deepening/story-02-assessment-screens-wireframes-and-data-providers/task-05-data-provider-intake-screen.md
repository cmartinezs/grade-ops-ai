# ⚛️ TASK 05 — data-provider-intake-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

DTOs for the brief-intake/draft-generation endpoints and a `submitAssessmentBrief()` function in `lib/api` that orchestrates the two sequential real calls (`POST /assessments` then `POST /assessments/{id}/draft`) behind one interface — independent of the mockup UI, ready for `task-06` to call. This task also records the mapping between semantic form values and the current transport DTO, including catalog/enum/numeric gaps, API I/O gaps, sync/async behavior and i18n behavior.

---

## Technical Design

- **Approach:** This is a **mutation orchestration**, not a Page Data Loader/Screen Data Facade — the guide's Loader concept (`06-estado-datos-y-api.md` §7) is for rendering data on page load; this is a two-step write flow, covered by §10 Mutaciones instead. It still must not live inline in the Page/hook as two separate `await` calls — it's centralized in one `lib/api` function so the hook only makes one call and only handles one error surface.
- **Affected files / components:**
  - `src/types/assessment.ts` (add `AssessmentBriefFormValue`, `CreateAssessmentBriefRequestDto`, `CreateAssessmentBriefResponseDto`)
  - `src/lib/api/assessments.ts` (add `createAssessmentBrief()`, `generateAssessmentDraft()`, and the orchestrating `submitAssessmentBrief()`)
  - `src/lib/api/__tests__/assessments.test.ts` (extend existing test file)
- **Interfaces / contracts:**
  ```ts
  export interface AssessmentBriefFormValue {
    learningGoal: string;
    topic: string; // controlled topic/tag/custom value per task-02 field matrix
    level: string; // selected enum/difficulty value until API exposes stronger type
    durationMinutes: number;
    language: string; // selected catalog/enum value until API exposes stronger type
    outputLocale: string; // BCP47 generated-content locale, separate from programming language
  }
  export interface CreateAssessmentBriefRequestDto {
    learningGoal: string;
    topic: string;
    level: string;
    duration: string;
    language: string;
  }
  export interface CreateAssessmentBriefResponseDto {
    assessmentId: string;
  }
  export async function submitAssessmentBrief(
    brief: AssessmentBriefFormValue
  ): Promise<{ assessmentId: string }>
  ```
  `submitAssessmentBrief` maps the semantic form value to `CreateAssessmentBriefRequestDto`, calls `createAssessmentBrief(dto)` then `generateAssessmentDraft(assessmentId)` sequentially (the second call depends on the first's result — this cannot be `Promise.all`'d) and returns the confirmed `assessmentId`.
- **Mapping rule:** If the API still requires `duration: string`, the client maps `durationMinutes` to the API-required string at the boundary only. Do not let UI form state model duration as arbitrary text. Record any need for future `durationMinutes`, `AssessmentLevel`, `ProgrammingLanguage`, `Subject` or `Topic` API changes as explicit residuals.
- **i18n rule:** `outputLocale`/`contentLocale` and the effective UI locale are transport concerns alongside the DTO, not labels-only UI state. Send the confirmed locale mechanism (`Accept-Language`, explicit request field or both) only if `api/` supports it; if the API lacks locale support, record a blocking API/agents residual rather than silently relying on browser language or the programming `language` field.
- **API I/O rule:** This task may only implement client functions for contracts confirmed in task-01. If catalogs/defaults/capabilities, operation status or mutation endpoints are missing, record the API gap and do not hide it with permanent local fixtures.
- **Sync/async rule:** If task-01 confirms sync legacy generation, `submitAssessmentBrief` returns after `generateAssessmentDraft` succeeds/fails. If task-01 confirms async generation, `submitAssessmentBrief` must return the operation/result shape agreed with `api/`, and a separate poll/SSE/WebSocket consumer must be planned before task-06.
- **Risk:** Medium — if `generateAssessmentDraft` fails after `createAssessmentBrief` already succeeded, the assessment brief exists but has no draft yet. `submitAssessmentBrief` must surface this as a distinguishable error (not silently retry) so `task-06`'s UI can tell the teacher the brief was saved but generation failed, rather than implying nothing happened.
- **Design notes:** Reuse the existing `apiClient` from `src/lib/api/client.ts` (already handles Firebase auth token + 401 handling) — do not create a second HTTP client. Field names must exactly match `task-01`'s confirmed `CreateAssessmentBriefRequest`/`Response` shapes, no renaming.

---

## API / Agent / Web Contract Gate

| Gate | Required check | Task answer |
|---|---|---|
| API as orchestrator | `submitAssessmentBrief` calls `api/` only; it does not know `agents/`, provider/model, prompts or internal generation URLs beyond public API routes | Keep orchestration in `src/lib/api/assessments.ts`; no agent/provider fields in DTOs |
| Richardson REST maturity | `POST /api/v1/assessments` creates a resource; `POST /api/v1/assessments/{id}/draft` starts generation; status/error semantics are mapped explicitly | Preserve exact current API behavior from `task-01`; do not invent `201`/`202`/`Location` if the API does not return them yet, but record the R01 orchestration gap |
| AI operation model | Draft generation is sync legacy until R01 adds operation-backed contract | Return current `assessmentId` for routing; do not fake an `operationId` in web |
| Idempotency | Mutating GenAI generation should use `Idempotency-Key` when API supports it | Check `task-01`; if absent, record as API gap and do not implement client-only retry that can double-generate |
| Contract testing | DTO tests must assert exact request shape, paths and error branch after brief-created/generation-failed | Extend `assessments.test.ts` with both steps and the partial-failure case |
| Web route functionality | Intake submit must support submitting, success redirect, brief-created/generation-failed warning, auth/validation/server errors | Expose distinguishable error state for `task-06`; no raw backend error shown to teacher |
| API I/O completeness | Client functions cover only API-backed read/write data; missing catalogs/defaults/capabilities/operation status are gaps | Record missing API support as residual or child API scope |
| Sync/async completion | Data provider exposes the chosen sync result or async operation/completion contract | No local timer, hidden polling or fake operation ID |
| i18n contract | Data provider sends supported locale headers/fields, keeps DTO field names English, maps safe localized errors, and does not infer content locale from programming language | Add tests/residuals for `outputLocale`/`contentLocale`, `Accept-Language`/effective locale and English-only technical codes |

## UI Design/Data Semantics Gate

| Gate | Required check | Task answer |
|---|---|---|
| Form-to-DTO mapping | Semantic form values are converted to the current API DTO only at the API boundary | Add mapper/test; no arbitrary text duration in form state |
| Catalog gaps | Missing API/catalog for `topic`, `level`, `duration` presets or `language` is recorded | Do not silently replace with free-text `Input` |
| Master data ownership | If catalog/master data is required, identify whether it belongs to `api/`/DB and whether infra/migration task is needed | Record residual or child planning input |
| Validation parity | Zod/client validation mirrors API/domain restrictions without replacing server-side validation | Add tests for invalid enum/catalog/numeric values |

## i18n Contract Gate

| Gate | Required check | Task answer |
|---|---|---|
| Locale propagation | Confirm how `effectiveLocale` is passed to `api/` (`Accept-Language`, explicit field, profile preference or tenant default) | Implement only confirmed support; otherwise record API gap before task-06 |
| Generated content locale | Confirm whether draft generation accepts `outputLocale`/`contentLocale` | Add to request/command only when API supports it; never reuse programming `language` as a proxy |
| Safe localized errors | Confirm error responses expose code + params or safe localized message | Map to teacher-facing localized text without exposing raw backend strings |
| Technical language | Confirm logs, correlation ids, error codes and test identifiers remain English | Locale appears only as request metadata/attributes |

## Sync/Async Contract Gate

| Action | Required check | Task answer |
|---|---|---|
| `submitAssessmentBrief` sync | If sync, distinguish brief-created/generation-failed and return `assessmentId` only after generation trigger succeeds | Unit tests cover both steps |
| `submitAssessmentBrief` async | If async, return operation metadata/link and expose/plan completion consumer | No fake completion in `lib/api` |
| Completion mechanism | Polling/SSE/WebSocket/webhook/push mechanism is named if async | Required before task-06 |
| Idempotency | GenAI mutating command carries `Idempotency-Key` when API supports it | No invisible retry without idempotency |

---

## Implementation Steps

1. Add `AssessmentBriefFormValue` plus `CreateAssessmentBriefRequestDto`/`CreateAssessmentBriefResponseDto` to `src/types/assessment.ts`, matching `task-01`'s confirmed API shapes exactly at the DTO boundary.
2. Add a mapper from `AssessmentBriefFormValue` to `CreateAssessmentBriefRequestDto`, converting numeric/preset/catalog values explicitly and documenting any gap where the current API forces a string representation.
3. Add `createAssessmentBrief(brief: CreateAssessmentBriefRequestDto)` to `src/lib/api/assessments.ts`, `POST`-ing to `/api/v1/assessments` via `apiClient`, throwing on non-2xx per the existing `getAssessments()` pattern in the same file.
4. Add `generateAssessmentDraft(assessmentId: string)` `POST`-ing to `/api/v1/assessments/${assessmentId}/draft` via `apiClient`.
5. Add locale propagation to the boundary according to task-01: supported header/field for effective locale and supported `outputLocale`/`contentLocale` for generation; if unsupported, record the API/agents residual and keep the web type explicit for later wiring.
6. Add `submitAssessmentBrief(brief: AssessmentBriefFormValue)` mapping once at the boundary, then calling both API functions in sequence and distinguishing which step failed in the thrown error.
7. If task-01 found missing API support for catalogs/defaults/capabilities, locale negotiation, generated-content locale or operation status, record it in this task output and block or residualize task-06 accordingly.
8. Extend `src/lib/api/__tests__/assessments.test.ts` with tests for all three functions, the mapper, invalid semantic values, locale propagation/fallback, the case where step 2 fails after step 1 succeeds, and async operation handling if applicable.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `createAssessmentBrief` sends the exact `CreateAssessmentBriefRequestDto` shape and parses `{assessmentId}` | `npm run test -- assessments` |
| 2 | `generateAssessmentDraft` posts to the correct path with the returned `assessmentId` | `npm run test -- assessments` |
| 3 | `submitAssessmentBrief` surfaces a distinguishable error when the draft-generation step fails after brief creation succeeds | `npm run test -- assessments` |
| 4 | Semantic form values serialize explicitly to the current DTO, including duration numeric/preset handling | `npm run test -- assessments` |
| 5 | Missing catalog/enum/source-of-truth gaps are recorded as residuals or follow-up API scope | Manual review of task output |
| 6 | Sync/async behavior matches task-01; async returns operation/completion contract instead of fake completion | `npm run test -- assessments` plus manual review |
| 7 | Locale propagation and `outputLocale`/`contentLocale` behavior match task-01, with missing support recorded as API/agents residual | `npm run test -- assessments` plus manual review |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | No startup regressions are visible | Inspect `npm run dev` output for new errors |

### Database / ORM Consistency Check

N/A — no database or ORM involved in `web/`.

### Logging / Observability

- **Logging mechanism:** Not yet confirmed for `web/` (`.planning/LOGGING.md` status: "not confirmed"). Suggested: Pino with structured JSON logs for this Node.js/TypeScript stack. **This task must not proceed to implementation without a human decision recorded in `LOGGING.md`**, since it introduces the first real outbound network calls in this story.
- **Correlation / trace context:** Once a mechanism is chosen, propagate a client-generated correlation id as a header on both `createAssessmentBrief` and `generateAssessmentDraft` calls so a failure between the two steps is traceable as one logical operation.
- **Levels by event criticality:** INFO for successful brief creation and draft generation; WARN if draft generation fails after brief creation succeeded (recoverable — brief still exists); ERROR for brief creation failure.
- **Execution trace points:** Entry into `submitAssessmentBrief`, each of the two outbound calls (dependency name, status, latency), and completion/failure.
- **Sensitive data guardrails:** Do not log the full brief payload (learning goal text may contain course-identifying context); log the `assessmentId` and status only.
- **Verification evidence:** Once the logging mechanism is chosen, a test or manual log sample showing both calls logged with a shared correlation id.

### Generated Test Suite

- **Task suite file:** `test-suites/task-05-data-provider-intake-screen-test-suite.md`
- **Required gates:** unit, coverage, static analysis (`npm run lint`), code style, architecture/design guide review.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md`, `15-backend-frontend-contracts.md`.
- **Acceptance environment:** N/A — unit-level mocking of `apiClient`/`fetch` is sufficient; no live `api/` instance needed for this task's tests.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Done Criteria

- [ ] `CreateAssessmentBriefRequestDto`/`ResponseDto` match `task-01`'s confirmed shapes exactly.
- [ ] Semantic form values and current API DTO are separated; conversion is explicit and tested.
- [ ] Catalog/enum/numeric/master-data gaps for `topic`, `level`, `duration` and `language` are recorded instead of hidden in free-text fields.
- [ ] API I/O gaps for catalogs/defaults/capabilities/mutations/operation status are recorded as API scope or residual before task-06.
- [ ] Sync/async contract is implemented or residualized; async completion is not simulated inside `lib/api`.
- [ ] i18n contract is implemented or residualized: effective locale propagation, `outputLocale`/`contentLocale`, safe localized errors and English-only technical codes are covered by tests/manual evidence.
- [ ] `submitAssessmentBrief` orchestrates both calls sequentially and distinguishes which step failed.
- [ ] API / Agent / Web Contract Gate is completed; no agent/provider/prompt fields leak into `web` DTOs.
- [ ] All new/extended tests in `assessments.test.ts` pass.
- [ ] `npm run lint` passes.
- [ ] Logging mechanism decision is recorded in `.planning/LOGGING.md` before this task is marked done, or explicitly deferred to `task-06` with the human's sign-off recorded here.
- [ ] Software smoke test check above passes (build/startup confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section.
- [ ] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
