# ⚛️ TASK 06 — connect-real-api-intake-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-04, task-05
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

The Intake screen is reachable from the dashboard "Nueva evaluacion" action, calls the real `api/` via `submitAssessmentBrief`, redirects to the Draft Builder screen with the real `assessmentId` or follows the agreed async completion contract, and surfaces localized safe 422/500 errors to the teacher — the fake submit/completion adapter from `task-04` is fully removed. The connected form must preserve the DS controls, data semantics, API I/O contract and i18n contract from the mockup instead of falling back to arbitrary text fields, permanent fixtures, hardcoded copy or local timers.

---

## Technical Design

- **Approach:** Swap `useIntakeAssessmentPage`'s fake submit for a call to `submitAssessmentBrief` (from `task-05`); no other component changes, since `task-04` already built the real component tree — this is the "conectar API real" step of the guide's flow (`02-ux-wireframes-y-maquetas.md` §2 step 8), not a rebuild.
- **Affected files / components:**
  - `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` (replace fake submit)
  - `src/app/(protected)/assessments/new/page.tsx` (add redirect on success)
  - `src/features/assessment-creation/components/__tests__/BriefForm.test.tsx` (extend for error states)
- **Interfaces / contracts:** The hook's public return shape (`RemoteData`-style submit state) stays the same as `task-04` if the flow remains sync. If task-05 chooses async, the hook must expose the operation state agreed with `api/`. The hook submits `AssessmentBriefFormValue`; `lib/api` owns conversion to the current DTO, locale propagation and operation contract.
- **Risk:** Medium — per `15-backend-frontend-contracts.md` §4/§5, backend validation is authoritative; a 422 here means a business-rule rejection the client-side Zod schema didn't (and shouldn't try to) catch. The UI must translate it into the teacher's effective locale, not show the raw error code.
- **Design notes:** Per `06-estado-datos-y-api.md` §9: 422 → business validation error (localized message), 500 → unexpected error (generic localized retry message). Do not show raw HTTP status codes or English backend strings to the teacher. Code identifiers, logs and telemetry remain English.

---

## API / Agent / Web Contract Gate

| Gate | Required check | Task answer |
|---|---|---|
| API as orchestrator | Screen submits to `submitAssessmentBrief`; it never calls `agents/` or handles provider/model/prompt choices | Keep route-level code focused on form state, submit state and navigation |
| Richardson REST maturity | The route honors current API status/error semantics and does not assume unsupported `202 Location`/operation polling | If API still returns sync result, redirect only after confirmed `assessmentId`; if API later returns operation, update by explicit contract |
| AI operation model | Generation may become operation-backed in R01; current web route must not fake operation state | Support loading and partial-failure warning; defer polling UI until API exposes it |
| Idempotency | Avoid automatic invisible retries of generation when API has no `Idempotency-Key` support | Retry must be explicit and teacher-visible |
| Contract testing | Tests cover navigation with real `assessmentId` plus 422/500 mapping | Extend hook/component tests |
| Web route functionality | `/dashboard` "Nueva evaluacion" reaches `/assessments/new`; `/assessments/new` exposes submit loading, success redirect, safe validation/server errors and brief-created/generation-failed warning | No raw backend strings, URL-only route or fake submit path remain |
| API I/O completeness | Connected UI uses `api/` for all required reads/writes/catalogs/states or records blocker/residual | No permanent fixtures for production data |
| Sync/async completion | Connected UI follows chosen sync response or async completion mechanism | No local timer, invented operation ID or spinner-only completion |
| i18n contract | Connected UI uses the resolved effective locale, passes supported locale metadata to `submitAssessmentBrief`, renders localized copy/catalog/error text and keeps `outputLocale` separate from programming `language` | Tests cover locale propagation, localized errors and absence of raw backend strings |

## UI Design/Data Semantics Gate

| Gate | Required check | Task answer |
|---|---|---|
| Preserve DS controls | Real API wiring must not replace `Textarea`/select/radio/numeric/catalog controls with generic `Input` | Compare against task-04 component tree |
| Semantic submit | Hook submits `AssessmentBriefFormValue`; DTO conversion happens in `lib/api` only | Review hook and API function boundary |
| Server validation | 422 mapping handles domain validation for enum/catalog/numeric/source-of-truth errors | Add tests for invalid/restricted values |
| Catalog fallback | Missing or failed catalog/source-of-truth state is user-visible or residualized | No silent all-text fallback |

## i18n Contract Gate

| Gate | Required check | Task answer |
|---|---|---|
| Effective locale | Resolve from UI selection/profile/browser/tenant fallback before submit | Hook passes locale metadata only through the task-05 boundary |
| UI text | Labels, helper text, buttons and validation messages use i18n resources | No final hardcoded user-facing literals in components |
| API errors | 422/500 and partial-generation failures render localized safe messages | No raw English backend strings or technical codes shown to the teacher |
| Generated draft locale | Submit uses `outputLocale`/`contentLocale` if API supports it | Programming `language` remains a separate selected catalog value |

## Sync/Async Contract Gate

| Gate | Required check | Task answer |
|---|---|---|
| Sync path | If generation is sync, redirect only after `api/` confirms `assessmentId`/draft trigger success | Tests cover success and partial failure |
| Async path | If generation is async, show operation state and wait by polling/SSE/WebSocket/webhook/push as agreed | Tests cover queued/running/succeeded/failed/timeout |
| Retry/cancel | Retry/cancel affordances appear only if API exposes them | No fake capability |
| Timeout | Timeout/error messages come from API contract or documented client policy | No indefinite spinner |

---

## Implementation Steps

1. Replace `useIntakeAssessmentPage`'s fake submit/completion adapter with a call to `submitAssessmentBrief(brief)` and the agreed completion consumer if async.
2. On sync success, use `useRouter().push()` to navigate to `/assessments/${assessmentId}/draft` (the Draft Builder screen route from `task-08`). On async success, navigate only when the operation reports the draft/result is ready, or show the operation state per contract.
3. On failure, map the thrown error to a localized teacher-facing message per `15-backend-frontend-contracts.md` §4 (422 → business validation message; 500 → generic retry message), surfaced via the existing error-state UI from `task-04`.
4. Extend `BriefForm.test.tsx`/add a hook test covering: successful submit navigates with the real `assessmentId`; a 422 response shows a translated message; a 500 response shows a generic retry message.
5. Add or extend a dashboard action test proving "Nueva evaluacion" navigates/links to `/assessments/new`.
6. Add or extend tests proving the real connected form keeps semantic controls and rejects invalid enum/catalog/numeric values before submit.
7. Add tests proving effective locale propagation, localized copy/catalog/error rendering and `outputLocale`/`contentLocale` separation from programming `language`.
8. Add tests for sync/async behavior selected in task-05. If async, cover queued/running/succeeded/failed/timeout and completion mechanism.
9. Remove the `setTimeout` fake-submit/completion code path entirely — no leftover dead code or feature flag.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Successful submit navigates to `/assessments/{assessmentId}/draft` with the real id from the API response | `npm run test` |
| 2 | A 422 response renders a translated, non-technical message | `npm run test` |
| 3 | A 500 response renders a generic retry message | `npm run test` |
| 4 | No fake/mocked submit code remains in the hook | Manual code review |
| 5 | Dashboard "Nueva evaluacion" reaches `/assessments/new` | `npm run test` plus manual smoke from `/dashboard` |
| 6 | Connected intake uses semantic DS controls and sends explicit DTO mapping through `submitAssessmentBrief` | `npm run test` plus manual code review |
| 7 | Connected intake uses real API I/O and chosen sync/async completion model | `npm run test` plus manual smoke |
| 8 | Connected intake propagates locale, renders localized copy/errors/catalog labels and keeps generated-content locale separate from programming language | `npm run test` plus manual smoke |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | Changed surface responds correctly | Start at `/dashboard`, click "Nueva evaluacion", then with `api/` running locally (or `NEXT_PUBLIC_API_BASE_URL` pointed at a reachable instance), submit the form and confirm real navigation to the draft screen with a real id |
| 3 | No startup regressions are visible | Inspect `npm run dev` output for new errors |

### Database / ORM Consistency Check

N/A — no database or ORM involved in `web/`.

### Logging / Observability

- **Logging mechanism:** Resolves the deferral from `task-05` — this task introduces the first real outbound network call from a user action. If no decision was recorded in `.planning/LOGGING.md` by the time this task starts, the agent must propose Pino (structured JSON, fits this Node.js/TypeScript stack) and get explicit human sign-off recorded in `LOGGING.md` before implementing.
- **Correlation / trace context:** Propagate a client-generated correlation id header through `submitAssessmentBrief`'s two calls (from `task-05`) so both are traceable as one logical submit in any server-side logs that echo it back.
- **Levels by event criticality:** INFO on successful navigation; WARN on 422 (recoverable, teacher can retry); ERROR on 500.
- **Execution trace points:** Submit handler entry, `submitAssessmentBrief` call, success/failure branch, navigation.
- **Sensitive data guardrails:** Do not log the brief's free-text fields; log status codes and the resulting `assessmentId` only.
- **Verification evidence:** A test or manual log sample showing the correlation id present on both outbound calls for one submit.

### Generated Test Suite

- **Task suite file:** `test-suites/task-06-connect-real-api-intake-screen-test-suite.md`
- **Required gates:** unit, coverage, integration, static analysis (`npm run lint`), code style, smoke, architecture/design guide review.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md`, `15-backend-frontend-contracts.md`.
- **Acceptance environment:** Mock `apiClient`/`fetch` for unit tests; a manual smoke pass against a real local `api/` instance covers the integration gate (no Docker/Testcontainers needed — `api/` runs standalone per its own `./mvnw spring-boot:run`).
- **Acceptance dependency inventory:** `api/` running locally at the URL configured in `NEXT_PUBLIC_API_BASE_URL`; no other external dependency.
- **Missing acceptance profile:** N/A — not a Maven/Cucumber service.

---

## Done Criteria

- [ ] Submitting the real form creates a brief, generates a draft, and navigates to the real draft screen with the real `assessmentId`.
- [ ] The real form is reachable from `/dashboard` via "Nueva evaluacion"; direct URL access is not the only happy path.
- [ ] The connected form preserves DS/data semantics; `level`, `duration`, `language` and `topic` do not become unrestricted text inputs.
- [ ] Tests cover valid and invalid controlled values, including API/domain 422 mapping for restricted/catalog/numeric errors.
- [ ] All required screen I/O comes from `api/` or is explicitly blocked/residualized; no production path depends on local fixtures.
- [ ] Sync/async contract is honored. Async completion/progress/failure is implemented only through the agreed mechanism.
- [ ] i18n contract is honored: effective locale is propagated, UI copy/errors/catalog labels are localized, and `outputLocale`/`contentLocale` is separate from programming `language`.
- [ ] API / Agent / Web Contract Gate is completed; no implicit retry or fake operation state is introduced.
- [ ] 422 and 500 responses show translated, teacher-facing messages, not raw codes/English strings.
- [ ] No fake/mocked submit code remains.
- [ ] All tests pass; `npm run lint` passes.
- [ ] Logging mechanism decision is recorded in `.planning/LOGGING.md` with human sign-off before this task is marked done.
- [ ] Software smoke test check above passes (build/startup/connectivity confirmed against a real local `api/`); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section.
- [ ] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
