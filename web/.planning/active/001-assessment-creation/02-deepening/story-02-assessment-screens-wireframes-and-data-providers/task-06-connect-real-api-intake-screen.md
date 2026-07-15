# ⚛️ TASK 06 — connect-real-api-intake-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-04, task-05
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

The Intake screen calls the real `api/` via `submitAssessmentBrief`, redirects to the Draft Builder screen with the real `assessmentId`, and surfaces the real error surface (validation 422, agent-rejected 422, agent-down 502/503, not-found 404, unexpected 500 — per `task-02`'s traced evidence, not the generic 422/500) to the teacher — the fake `setTimeout` submit from `task-04` is fully removed.

---

## Technical Design

- **Approach:** Swap `useIntakeAssessmentPage`'s fake submit for a call to `submitAssessmentBrief` (from `task-05`); no other component changes, since `task-04` already built the real component tree — this is the "conectar API real" step of the guide's flow (`02-ux-wireframes-y-maquetas.md` §2 step 8), not a rebuild.
- **Affected files / components:**
  - `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` (replace fake submit)
  - `src/app/(protected)/assessments/new/page.tsx` (add redirect on success)
  - `src/features/assessment-creation/components/__tests__/BriefForm.test.tsx` (extend for error states)
- **Interfaces / contracts:** The hook's public return shape (`RemoteData`-style submit state) stays the same as `task-04` — only what feeds it changes, from a fake timer to `submitAssessmentBrief`.
- **Risk:** Medium — per `15-backend-frontend-contracts.md` §4/§5, backend validation is authoritative. `task-02`'s traced evidence (`GlobalExceptionHandler.java`, `DraftGenerationCoordinator.java`, `AgentClientException.java`) found two distinct 422 body shapes plus 502/503 that the original 2-state (422/500) design missed entirely — treating everything as "422 = business error, 500 = unexpected" would misparse `List<FieldErrorResponse>` responses and show the wrong message for an `agents/` outage.
- **Design notes:** Real error surface, from `task-02`'s § Verification Summary (verified against `api/` source, not assumed from `06-estado-datos-y-api.md` §9's generic list):
  - `POST /assessments` — 422 with body `List<FieldErrorResponse>` (Bean Validation; shouldn't normally trigger since client-side Zod mirrors the same `@NotBlank` fields, but the parser must handle it distinctly from the shape below); 400 `ApiErrorResponse{error:"MALFORMED_REQUEST"}`; 500 `ApiErrorResponse{error:"INTERNAL_ERROR"}`.
  - `POST /assessments/{id}/draft` — 404 `ApiErrorResponse{error:"NOT_FOUND"}` (assessment/brief not found or ownership mismatch — shouldn't occur in the normal flow since the id comes from step 1's own response, handle as generic retry if seen); 422 `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_REJECTED"}`; 502/503 `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_ERROR"|"UNREACHABLE"}` (translate as "servicio no disponible," distinct from the validation message — the brief is already persisted, nothing is lost); 500 `ApiErrorResponse{error:"INTERNAL_ERROR"}`.
  - Do not show raw HTTP status codes, `error` codes, or English backend strings to the teacher in any case.

---

## Implementation Steps

1. Replace `useIntakeAssessmentPage`'s fake submit with a call to `submitAssessmentBrief(brief)`.
2. On success, use `useRouter().push()` to navigate to `/assessments/${assessmentId}/draft` (the Draft Builder screen route from `task-08`).
3. On failure, branch on the response shape/status per `task-02`'s traced evidence, not a flat 422/500 switch: `List<FieldErrorResponse>` (422 from `POST /assessments`) → per-field messages; `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_REJECTED"}` → business-rejection message; `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_ERROR"|"UNREACHABLE"}` (502/503) → service-unavailable message, distinct wording from the rejection case; anything else (400/404/500) → generic retry message. All translated to Spanish per `15-backend-frontend-contracts.md` §4, surfaced via the existing error-state UI from `task-04`.
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

- **Logging mechanism:** Resolves the deferral from `task-05` — this task introduces the first real outbound network call from a user action. If no decision was recorded in `.planning/LOGGING.md` by the time this task starts, the agent must propose Pino (structured JSON, fits this Node.js/TypeScript stack) and get explicit human sign-off recorded in `LOGGING.md` before implementing.
- **Correlation / trace context:** Propagate a client-generated correlation id header through `submitAssessmentBrief`'s two calls (from `task-05`) so both are traceable as one logical submit in any server-side logs that echo it back.
- **Levels by event criticality:** INFO on successful navigation; WARN on 422 (either shape) and 502/503 (all recoverable, teacher can retry); ERROR on 500.
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
- [ ] Both 422 shapes (`List<FieldErrorResponse>` and `ApiErrorResponse{AGENT_REJECTED}`), 502/503 (`agents/` down), and 500 responses each show a distinct, translated, teacher-facing message — not raw codes/English strings, and not collapsed into one generic "422/500" bucket.
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
