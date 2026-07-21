# ⚛️ TASK 06 — connect-real-api-intake-screen

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-04, task-05
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

The Intake screen calls the real `api/` via `submitAssessmentBrief`, redirects to the Draft Builder screen with the real `assessmentId`, and surfaces the real error surface (validation 422, agent-rejected 422, agent-down 502/503, not-found 404, unexpected 500 — per `task-02`'s traced evidence, not the generic 422/500) to the teacher — the fake `setTimeout` submit from `task-04` is fully removed.

---

## Technical Design

- **Approach:** Swap `useIntakeAssessmentPage`'s fake submit for a call to `submitAssessmentBrief` (from `task-05`); no other component changes, since `task-04` already built the real component tree — this is the "conectar API real" step of the guide's flow (`02-ux-wireframes-y-maquetas.md` §2 step 8), not a rebuild.
- **Affected files / components:** Modified: `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts`, `src/app/(protected)/assessments/new/page.tsx`, `src/features/assessment-creation/components/__tests__/BriefForm.test.tsx`.

  Per-file detail:
  - `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` — replace fake submit with `submitAssessmentBrief`.
  - `src/app/(protected)/assessments/new/page.tsx` — add redirect on success.
  - `src/features/assessment-creation/components/__tests__/BriefForm.test.tsx` — extend for error states.
- **Interfaces / contracts:** The hook's public return shape (`RemoteData`-style submit state, including `fieldErrors`) stays exactly the same as `task-04` — only what feeds it changes, from a fake timer/simulated `topic: "trigger-field-error"` case to `submitAssessmentBrief`'s real response. A real `List<FieldErrorResponse>` 422 populates `fieldErrors` the same way the fake case did in `task-04`; it flows through the same `BriefForm.fieldErrors` → `DynamicForm.externalErrors` path already built there, with no new prop or component added here.
- **Risk:** Medium — per `15-backend-frontend-contracts.md` §4/§5, backend validation is authoritative. `task-02`'s traced evidence (`GlobalExceptionHandler.java`, `DraftGenerationCoordinator.java`, `AgentClientException.java`) found two distinct 422 body shapes plus 502/503 that the original 2-state (422/500) design missed entirely — treating everything as "422 = business error, 500 = unexpected" would misparse `List<FieldErrorResponse>` responses and show the wrong message for an `agents/` outage.
- **Design notes:** Real error surface, from `task-02`'s § Verification Summary (verified against `api/` source, not assumed from `06-estado-datos-y-api.md` §9's generic list):
  - `POST /assessments` — 422 with body `List<FieldErrorResponse>` (Bean Validation; shouldn't normally trigger since client-side Zod mirrors the same `@NotBlank` fields, but the parser must handle it distinctly from the shape below); 400 `ApiErrorResponse{error:"MALFORMED_REQUEST"}`; 500 `ApiErrorResponse{error:"INTERNAL_ERROR"}`.
  - `POST /assessments/{id}/draft` — 404 `ApiErrorResponse{error:"NOT_FOUND"}` (assessment/brief not found or ownership mismatch — shouldn't occur in the normal flow since the id comes from step 1's own response, handle as generic retry if seen); 422 `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_REJECTED"}`; 502/503 `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_ERROR"|"UNREACHABLE"}` (translate as "servicio no disponible," distinct from the validation message — the brief is already persisted, nothing is lost); 500 `ApiErrorResponse{error:"INTERNAL_ERROR"}`.
  - Do not show raw HTTP status codes, `error` codes, or English backend strings to the teacher in any case.

---

## API / Agent / Web Contract Gate

> Added to `develop` post-divergence (commit `e2703d5`, 2026-07-20); reconciled into this already-DONE task during story-02 closeout (2026-07-21) with real evidence, not left as the generic prescriptive text.

| Gate | Required check | Task answer |
|---|---|---|
| API as orchestrator | Screen submits to `submitAssessmentBrief`; it never calls `agents/` or handles provider/model/prompt choices | **Confirmed.** `useIntakeAssessmentPage.ts` calls only `submitAssessmentBrief` (`task-05`); confirmed via the real-stack smoke run in § Verification Summary (real `POST /api/v1/assessments` → real `POST /assessments/{id}/draft`, never a direct `agents/` call from the browser). |
| Richardson REST maturity | The route honors current API status/error semantics and does not assume unsupported `202 Location`/operation polling | Confirmed: the redirect fires only after `submitAssessmentBrief` resolves with a real `assessmentId` (synchronous 201/201 response), no `202`/`Location`/polling logic exists anywhere in the hook. |
| AI operation model | Generation may become operation-backed in R01; current web route must not fake operation state | Confirmed: the hook's `SubmitState` union (`idle/submitting/success/error`) has no operation-id/polling state — the real-stack run's happy path returned the full generated draft synchronously, matching this. |
| Idempotency | Avoid automatic invisible retries of generation when API has no `Idempotency-Key` support | Confirmed: `handleSubmit` makes exactly one `submitAssessmentBrief` call per teacher-initiated form submit; no retry loop, timeout-based resubmit, or `useEffect` re-triggers it. A failed submit requires the teacher to click "Crear evaluación" again. |
| Contract testing | Tests cover navigation with real `assessmentId` plus 422/500 mapping | Done: `NewAssessmentPage.test.tsx`'s 6 cases (success+navigation, field-error 422, `AGENT_REJECTED`, `AGENT_ERROR`/`UNREACHABLE` 502/503, generic 500) plus the real-stack smoke run in § Verification Summary that found and fixed the English-string-leak bug this mocked suite alone couldn't catch. |
| Web route functionality | `/assessments/new` exposes submit loading, success redirect, safe validation/server errors and brief-created/generation-failed warning | Confirmed — all 5 states (`isSubmitting`, success+redirect, field errors, agent-rejected banner, service-unavailable banner) implemented and tested; none shows a raw status code or English string (see § Verification Summary's "Distinguishable error messages" note). |

---

## Implementation Steps

1. Replace `useIntakeAssessmentPage`'s fake submit with a call to `submitAssessmentBrief(brief)`.
2. On success, use `useRouter().push()` to navigate to `/assessments/${assessmentId}/draft` (the Draft Builder screen route from `task-08`).
3. On failure, branch on the response shape/status per `task-02`'s traced evidence, not a flat 422/500 switch: `List<FieldErrorResponse>` (422 from `POST /assessments`) → map to `fieldErrors` (`Partial<Record<keyof BriefFormValues, string>>`) and return it from the hook exactly as `task-04`'s fake `trigger-field-error` case did — it flows through the already-built `BriefForm.fieldErrors` → `DynamicForm.externalErrors` path, no new UI mechanism; `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_REJECTED"}` → business-rejection message via `serverError` (banner); `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_ERROR"|"UNREACHABLE"}` (502/503) → service-unavailable message via `serverError`, distinct wording from the rejection case; anything else (400/404/500) → generic retry message via `serverError`. All translated to Spanish per `15-backend-frontend-contracts.md` §4, surfaced via the existing error-state UI from `task-04`.
4. Extend `BriefForm.test.tsx`/add a hook test covering: successful submit navigates with the real `assessmentId`; the `List<FieldErrorResponse>` 422 shows per-field messages; the `AGENT_REJECTED` 422 shows the business message; a 502/503 shows the service-unavailable message; a 500 shows the generic retry message.
5. Remove the `setTimeout` fake-submit code path entirely — no leftover dead code or feature flag.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Successful submit navigates to `/assessments/{assessmentId}/draft` with the real id from the API response | `npm run test` |
| 2 | A `List<FieldErrorResponse>` 422 (from `POST /assessments`) renders per-field translated messages, not a generic one | `npm run test` |
| 3 | An `AGENT_REJECTED` 422 (from `POST /assessments/{id}/draft`) renders a business-rejection message, distinct from the field-validation case | `npm run test` |
| 4 | An `AGENT_ERROR`/`UNREACHABLE` 502/503 renders a service-unavailable message, distinct from both 422 cases | `npm run test` |
| 5 | A 500 response renders a generic retry message | `npm run test` |
| 6 | No fake/mocked submit code remains in the hook | Manual code review |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | Changed surface responds correctly | With `api/` running locally (or `NEXT_PUBLIC_API_BASE_URL` pointed at a reachable instance), submit the form and confirm real navigation to the draft screen with a real id |
| 3 | No startup regressions are visible | Inspect `npm run dev` output for new errors |

### Database / ORM Consistency Check

N/A — no database or ORM involved in `web/`.

### Logging / Observability

- **Logging mechanism:** Already decided and recorded in `task-05` — Pino, structured JSON, confirmed in `.planning/LOGGING.md` (status: confirmed). This task reuses it; no further human sign-off needed.
- **Correlation / trace context:** `submitAssessmentBrief` (from `task-05`) already creates a client-generated correlation id and logs both outbound calls under it via a Pino child logger — this task does not need to propagate a header (no backend-side correlation header contract exists; `task-05`'s own Logging section explicitly scopes that out). This task's submit handler should just let `submitAssessmentBrief`'s existing logging run; no new correlation mechanism to build here.
- **Levels by event criticality:** INFO on successful navigation; WARN on 422 (either shape) and 502/503 (all recoverable, teacher can retry); ERROR on 500.
- **Execution trace points:** Submit handler entry, `submitAssessmentBrief` call, success/failure branch, navigation.
- **Sensitive data guardrails:** Do not log the brief's free-text fields; log status codes and the resulting `assessmentId` only.
- **Verification evidence:** Confirm (via `task-05`'s existing tests/log sample, or a quick manual check) that `submitAssessmentBrief`'s correlation id still appears on both outbound calls for one submit — no new header-based test needed.

### Generated Test Suite

- **Task suite file:** `test-suites/task-06-connect-real-api-intake-screen-test-suite.md`
- **Required gates:** unit, coverage, integration, static analysis (`npm run lint`), code style, smoke, architecture/design guide review.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md`, `15-backend-frontend-contracts.md`.
- **Acceptance environment:** Mock `apiClient`/`fetch` for unit tests; a manual smoke pass against a real local `api/` instance covers the integration gate (no Docker/Testcontainers needed — `api/` runs standalone per its own `./mvnw spring-boot:run`).
- **Acceptance dependency inventory:** `api/` running locally at the URL configured in `NEXT_PUBLIC_API_BASE_URL`; no other external dependency.
- **Missing acceptance profile:** N/A — not a Maven/Cucumber service.

---

## Verification Summary

- **`page.tsx` was not modified — deliberate, not an oversight:** the task's Affected files list named `page.tsx` for "add redirect on success," but `task-04`'s and this task's own Interfaces/contracts text both say the `IntakeAssessmentPageViewModel` "stays exactly the same" (`isSubmitting`, `serverError`, `fieldErrors`, `handleSubmit` — no 5th field for a success/assessmentId signal). Since the Page has no way to observe "submission succeeded, here's the id" without growing that contract, the only design that respects the frozen 4-field ViewModel is for the hook itself to call `useRouter().push()` internally on success. Implemented it there; `page.tsx` needed zero code changes. Confirmed via `git status --porcelain` showing `page.tsx` untouched.
- **Test file target — `NewAssessmentPage.test.tsx` instead of `BriefForm.test.tsx`:** the task listed `BriefForm.test.tsx` for the new error-branching tests, but `BriefForm.tsx` itself isn't modified in this task (it doesn't own the error logic — the hook does), so its existing 7 tests from `task-04` still fully cover its rendering-given-props behavior. The actual new logic under test — mapping `CreateAssessmentBriefError`/`GenerateAssessmentDraftError` to `fieldErrors`/`serverError`/navigation — lives in the hook, and `task-04` already established `NewAssessmentPage.test.tsx` as the place that exercises the real hook end-to-end (not mocked props). Extended that file instead: replaced the 2 now-obsolete fake-timer tests with 6 new ones mocking `submitAssessmentBrief` (success+navigation, submitting-disables-button, field-error 422, `AGENT_REJECTED`, `AGENT_ERROR`/`UNREACHABLE` 502/503, generic 500) — `6 passed, 6 total`.
- **No fake/mocked submit code remains:** `grep -rn "setTimeout\|trigger-field-error\|fake" src/features/assessment-creation/hooks src/features/assessment-creation/components "src/app/(protected)/assessments/new/page.tsx"` returns nothing.
- **Distinguishable error messages, all Spanish, no raw codes/English strings:** field-level errors use the backend's own (already-Spanish) `FieldErrorResponse.message` verbatim; `AGENT_REJECTED` → a business-rejection message; `AGENT_ERROR`/`UNREACHABLE` → a distinct service-unavailable message noting the brief is already saved; everything else (400/404/500) → one generic retry message. Verified each renders distinctly and none leaks a status code or English string.
- **Full suite re-run:** `80 tests, 75 passed`; the 5 failures are the same pre-existing, unrelated ones from prior tasks' rounds (English-vs-Spanish label queries).
- **Static analysis:** `npm run lint` — still N/A, no ESLint config in this repo (pre-existing, unrelated). Substituted `npx tsc --noEmit`: 0 errors outside the pre-existing missing-`@types/jest` gap.
- **Integration/acceptance gate — Docker became available mid-task; ran the real smoke test and it found a real bug.** Initially documented as infeasible (no Docker daemon). Once Docker was available, found a full local stack already running (from the sibling `story-04-e2e-integration-verification` planning's own e2e work, in the `../gradeops-e2e-verification` worktree — real Postgres, real `api/`, real `agents/` with a real Groq key, real Firebase project). Wrote a throwaway script (deleted after use, never committed) that provisions a real test teacher, signs in via the actual `firebase/auth` client, and calls this task's own real `submitAssessmentBrief`/`createAssessmentBrief` exports (not a reimplementation) against that stack:
  - **Happy path:** real `POST /api/v1/assessments` → 201 → real `assessmentId`; real `POST /assessments/{id}/draft` → 201 → real Groq-generated draft. Confirms task-05's provider code and this task's redirect wiring work against the actual backend contract, not just mocks.
  - **Real bug found — English backend strings were about to leak to the teacher:** the real 422 body for a blank required field is `[{"field":"topic","message":"must not be blank"}]` — Hibernate Validator's default English text (`CreateAssessmentBriefRequest`'s `@NotBlank` has no custom message). The original implementation mapped `fieldError.message` straight into `fieldErrors`, which would have shown "must not be blank" to the teacher, directly violating this task's own "no English backend strings, ever" rule — an assumption from `task-05`'s synthetic unit tests (which used my own invented Spanish mock text) that the real backend never contradicted until this real call. **Fixed:** field errors now map through a fixed `FIELD_ERROR_MESSAGES` dictionary reusing `briefSchema`'s own Zod copy per field, ignoring the backend's message text entirely (every one of these 5 fields has the exact same single constraint, so no generic translation layer is needed). Updated the corresponding test to assert on the *translated* text and assert the raw English string is absent.
  - **502/503 path confirmed for real too:** stopped the `agents` container, retried — got `GenerateAssessmentDraftError` with `status: 503`, `body: {"error":"AGENT_CALL_FAILED","message":"UNREACHABLE"}`, and `assessmentId` preserved (brief already saved) — an exact match to the shape already assumed in the hook's `SERVICE_UNAVAILABLE_MESSAGE` branch and the unit tests. Restarted `agents` afterward to restore the stack to how it was found.
  - Real Pino logs from the happy-path run confirm one shared `correlationId` across `submitAssessmentBrief started` → `createAssessmentBrief succeeded` (201, real latency) → `generateAssessmentDraft succeeded` (201, real latency) → `submitAssessmentBrief completed`; the 503 run logged the failure at WARN (Pino level 40), matching `.planning/LOGGING.md`'s criticality mapping.
  - Cleaned up: deleted all 3 throwaway scripts and the throwaway `.env.local` (gitignored, never committed) once verification was complete; restored the `agents` container.
- **Runtime smoke (what *is* verifiable in this sandbox):** `npm run build` compiles/type-checks cleanly (`✓ Compiled successfully`), fails only at the same pre-existing missing-Firebase-credentials prerender gap (this time surfacing on `/forgot-password`, same root cause as prior tasks). `npm run dev` started cleanly, `curl http://localhost:3000/assessments/new` returned `HTTP 200` with no console errors in the dev log.
- **Logging:** reused `task-05`'s already-confirmed Pino decision, no new human sign-off needed (correctly, per this task's own Logging section) — `submitAssessmentBrief`'s existing correlation-id logging runs unchanged through this task's new call sites; no new logging code was added here since the hook doesn't log directly (it only branches on the already-logged error).
- **`[CHECK-ATOMICITY]`:** scope stayed within the task's real intent — `useIntakeAssessmentPage.ts` (fake submit replaced) and `NewAssessmentPage.test.tsx` (real test target, per the reasoning above) — `page.tsx` and `BriefForm.test.tsx` were correctly left untouched, not silently skipped.

---

## Done Criteria

- [x] Submitting the real form creates a brief, generates a draft, and navigates to the real draft screen with the real `assessmentId`. Verified twice: via `NewAssessmentPage.test.tsx`'s navigation test (mocked `submitAssessmentBrief`), and for real against the local `api/`/`agents/`/Postgres stack once Docker became available mid-task — real 201/201, real `assessmentId`, real Groq-generated draft (see Verification Summary).
- [x] API / Agent / Web Contract Gate is completed; no implicit retry or fake operation state is introduced — reconciled 2026-07-21 (story-02 closeout); see § API / Agent / Web Contract Gate.
- [x] Both 422 shapes (`List<FieldErrorResponse>` and `ApiErrorResponse{AGENT_REJECTED}`), 502/503 (`agents/` down), and 500 responses each show a distinct, translated, teacher-facing message — not raw codes/English strings, and not collapsed into one generic "422/500" bucket. 4 dedicated tests, one per shape.
- [x] No fake/mocked submit code remains. Confirmed via grep (see Verification Summary).
- [x] All tests pass; `npm run lint` passes. `80 tests, 75 passed` (same 5 pre-existing unrelated failures); `npm run lint` N/A (no ESLint config, pre-existing) — substituted `tsc --noEmit`, clean.
- [x] Logging mechanism decision is recorded in `.planning/LOGGING.md` with human sign-off before this task is marked done. Already recorded in `task-05`; reused as-is, correctly per this task's own Logging section.
- [x] Software smoke test check above passes (build/startup/connectivity confirmed against a real local `api/`); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR. Build/startup confirmed; connectivity against a real local `api/` confirmed once Docker became available mid-task — real submit, real navigation, real error shapes (422 field validation, 503 agent-unreachable), all against the actual running stack, not simulated. PR #77 (`tasks/story-02-assessment-screens-wireframes-and-data-providers/task-06-connect-real-api-intake-screen` → `story-02-assessment-screens-wireframes-and-data-providers`) opened, reviewed with no findings, corrected post-approval with a real-stack-discovered bug fix (English-string leak), re-reviewed with no remaining findings (noting the deeper backend/agent localization question is an `api/`/`agents/` follow-up, out of `web/`'s scope), and merged 2026-07-16 (merge commit `68e0a6c`).
- [x] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section. Unchanged, reused from `task-05`.
- [x] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence — `test-suites/task-06-connect-real-api-intake-screen-test-suite.md` generated and gaps filled, including the honest integration/acceptance gap.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — see Verification Summary's atomicity note.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
