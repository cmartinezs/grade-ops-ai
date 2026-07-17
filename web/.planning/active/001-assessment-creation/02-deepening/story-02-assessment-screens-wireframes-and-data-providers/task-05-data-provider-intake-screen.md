# ⚛️ TASK 05 — data-provider-intake-screen

> **Status:** IN PROGRESS
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

DTOs for the brief-intake/draft-generation endpoints and a `submitAssessmentBrief()` function in `lib/api` that orchestrates the two sequential real calls (`POST /assessments` then `POST /assessments/{id}/draft`) behind one interface — independent of the mockup UI, ready for `task-06` to call.

---

## Technical Design

- **Approach:** This is a **mutation orchestration**, not a Page Data Loader/Screen Data Facade — the guide's Loader concept (`06-estado-datos-y-api.md` §7) is for rendering data on page load; this is a two-step write flow, covered by §10 Mutaciones instead. It still must not live inline in the Page/hook as two separate `await` calls — it's centralized in one `lib/api` function so the hook only makes one call and only handles one error surface.
- **Affected files / components:** New: `src/lib/logging/logger.ts`. Modified: `src/types/assessment.ts`, `src/lib/api/assessments.ts`, `src/lib/api/__tests__/assessments.test.ts`.

  Per-file detail:
  - `src/types/assessment.ts` — add `CreateAssessmentBriefRequestDto`, `CreateAssessmentBriefResponseDto`.
  - `src/lib/api/assessments.ts` — add `createAssessmentBrief()`, `generateAssessmentDraft()`, and the orchestrating `submitAssessmentBrief()`.
  - `src/lib/api/__tests__/assessments.test.ts` — extend existing test file.
  - `src/lib/logging/logger.ts` — new, shared Pino instance per the logging decision recorded in `.planning/LOGGING.md`.
- **Interfaces / contracts:**
  ```ts
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
    brief: CreateAssessmentBriefRequestDto
  ): Promise<{ assessmentId: string }>
  ```
  `submitAssessmentBrief` calls `createAssessmentBrief(brief)` then `generateAssessmentDraft(assessmentId)` sequentially (the second call depends on the first's result — this cannot be `Promise.all`'d) and returns the confirmed `assessmentId`.
- **Risk:** Medium — if `generateAssessmentDraft` fails after `createAssessmentBrief` already succeeded, the assessment brief exists but has no draft yet. `submitAssessmentBrief` must surface this as a distinguishable error (not silently retry) so `task-06`'s UI can tell the teacher the brief was saved but generation failed, rather than implying nothing happened.
- **Design notes:** Reuse the existing `apiClient` from `src/lib/api/client.ts` (already handles Firebase auth token + 401 handling) — do not create a second HTTP client. Field names must exactly match `task-01`'s confirmed `CreateAssessmentBriefRequest`/`Response` shapes, no renaming. Per `task-02`'s traced error evidence, the two calls have different error-body shapes on failure: `createAssessmentBrief`'s 422 is `List<FieldErrorResponse>` (Bean Validation), while `generateAssessmentDraft`'s errors (404/422/502/503/500) are all `ApiErrorResponse{error, message}`. `createAssessmentBrief`/`generateAssessmentDraft` must propagate the parsed body as-is (don't collapse into a single generic `Error`) so `task-06` can branch on shape/status, not guess.

---

## Implementation Steps

1. Add `CreateAssessmentBriefRequestDto`/`CreateAssessmentBriefResponseDto` to `src/types/assessment.ts`, matching `task-01`'s confirmed shapes exactly.
2. Add `createAssessmentBrief(brief: CreateAssessmentBriefRequestDto)` to `src/lib/api/assessments.ts`, `POST`-ing to `/api/v1/assessments` via `apiClient`, throwing on non-2xx per the existing `getAssessments()` pattern in the same file.
3. Add `generateAssessmentDraft(assessmentId: string)` `POST`-ing to `/api/v1/assessments/${assessmentId}/draft` via `apiClient`.
4. Add `submitAssessmentBrief(brief)` calling both in sequence, distinguishing which step failed in the thrown error.
5. Extend `src/lib/api/__tests__/assessments.test.ts` with tests for all three functions, including the case where step 2 fails after step 1 succeeds.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `createAssessmentBrief` sends the exact `CreateAssessmentBriefRequestDto` shape and parses `{assessmentId}` | `npm run test -- assessments` |
| 2 | `generateAssessmentDraft` posts to the correct path with the returned `assessmentId` | `npm run test -- assessments` |
| 3 | `submitAssessmentBrief` surfaces a distinguishable error when the draft-generation step fails after brief creation succeeds | `npm run test -- assessments` |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | No startup regressions are visible | Inspect `npm run dev` output for new errors |

### Database / ORM Consistency Check

N/A — no database or ORM involved in `web/`.

### Logging / Observability

- **Logging mechanism:** Confirmed 2026-07-16 by human decision (recorded in `.planning/LOGGING.md`): Pino, structured JSON logs, via a new shared `src/lib/logging/logger.ts` instance.
- **Correlation / trace context:** A client-generated correlation id (timestamp + random suffix, not a cryptographic UUID — `crypto.randomUUID()` throws under this repo's jsdom test environment despite working in real Node/browsers, see Verification Summary) is created once at the start of `submitAssessmentBrief` and passed as a bound field to a Pino child logger, so both `createAssessmentBrief` and `generateAssessmentDraft` log under the same `correlationId` and a failure between the two steps is traceable as one logical operation. Not propagated as an HTTP header to the backend in this task — no documented backend-side correlation header contract exists yet; `task-06`'s Logging section has been corrected to not require one either.
- **Levels by event criticality:** INFO for successful brief creation and draft generation; WARN if draft generation fails after brief creation succeeded (recoverable — brief still exists); ERROR for brief creation failure.
- **Execution trace points:** Entry into `submitAssessmentBrief`, each of the two outbound calls (dependency name, status, latency), and completion/failure.
- **Sensitive data guardrails:** Do not log the full brief payload (learning goal text may contain course-identifying context); log the `assessmentId` and status only.
- **Verification evidence:** Manual log sample (real Pino, run against `node` directly from `web/`) confirms all 4 log lines share one `correlationId`, structured JSON, no payload logged:
  ```json
  {"level":30,"time":1784241875335,"pid":7286,"hostname":"tatooine","correlationId":"mro3jm2v-axud12oj","msg":"submitAssessmentBrief started"}
  {"level":30,"time":1784241875336,"pid":7286,"hostname":"tatooine","correlationId":"mro3jm2v-axud12oj","dependency":"api/assessments","status":201,"latencyMs":42,"assessmentId":"assess-1","msg":"createAssessmentBrief succeeded"}
  {"level":30,"time":1784241875337,"pid":7286,"hostname":"tatooine","correlationId":"mro3jm2v-axud12oj","dependency":"api/assessments/draft","status":200,"latencyMs":15,"assessmentId":"assess-1","msg":"generateAssessmentDraft succeeded"}
  {"level":30,"time":1784241875337,"pid":7286,"hostname":"tatooine","correlationId":"mro3jm2v-axud12oj","assessmentId":"assess-1","msg":"submitAssessmentBrief completed"}
  ```

### Generated Test Suite

- **Task suite file:** `test-suites/task-05-data-provider-intake-screen-test-suite.md`
- **Required gates:** unit, coverage, static analysis (`npm run lint`), code style, architecture/design guide review.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md`, `15-backend-frontend-contracts.md`.
- **Acceptance environment:** N/A — unit-level mocking of `apiClient`/`fetch` is sufficient; no live `api/` instance needed for this task's tests.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Verification Summary

- **Logging decision resolved before implementation, per this task's own hard stop:** presented the human with the choice (Pino/JSON, Winston/JSON, or defer to `task-06`); Pino was chosen. Recorded in `.planning/LOGGING.md`'s "Current Mechanism" section (status: confirmed) before writing any of `assessments.ts`'s new functions. Added `pino` (`^10.3.1`) as a dependency.
- **DTOs match `task-01`'s confirmed shapes exactly:** `CreateAssessmentBriefRequestDto`/`ResponseDto` per the task's own Interfaces/contracts text. Also added `FieldErrorResponse { field, message }` and `ApiErrorResponse { error, message: string | null }` to `src/types/assessment.ts` — not explicitly named in the task's Affected files list, but required to type the two distinct 422/error-body shapes `task-02` traced; verified field-for-field against the actual backend records (`api/src/main/java/.../FieldErrorResponse.java`, `.../ApiErrorResponse.java`), not assumed.
- **Distinguishable step failure:** implemented via two distinct error classes — `CreateAssessmentBriefError` (carries the raw `FieldErrorResponse[] | ApiErrorResponse` body) and `GenerateAssessmentDraftError` (carries the `ApiErrorResponse` body **and** the `assessmentId`, so a caller catching it already knows the brief exists). `task-06` can `instanceof`-branch on which failed, per the task's Risk note.
- **Correlation id fix during implementation:** `crypto.randomUUID()` (the task's own suggested approach) throws in this repo's jsdom test environment even though it's supported in real Node/browsers — confirmed via `node -e "console.log(typeof globalThis.crypto?.randomUUID)"` returning `"function"` outside jsdom. Since a correlation id only needs to be unique for log tracing, not cryptographically strong, replaced it with a small dependency-free `createCorrelationId()` (timestamp + random suffix). Recorded the reasoning in `.planning/LOGGING.md` too, so the next task reusing this pattern doesn't reintroduce the same jsdom failure.
- **Code review finding (P2, now fixed) — stale handoff docs after the correlation-id change:** `.planning/LOGGING.md` was updated to describe the timestamp+random approach during implementation, but this task's own Logging section (line 80) and `task-06`'s Logging section were never propagated to match — `task-05` still said "UUID v4" and `task-06` still told the next implementer to "propagate a client-generated correlation id header... from `task-05`," even though neither `task-05` nor `.planning/LOGGING.md` ever implements or calls for an HTTP header (deliberately out of scope, no backend contract exists). This is the same "fix in one place, forget to propagate" mistake this planning already learned from once during `task-03`'s review. Fixed both: `task-05`'s Correlation/trace context line now matches the implementation exactly; `task-06`'s Logging section now says the mechanism is already decided (no re-asking for sign-off) and that `submitAssessmentBrief`'s existing per-call correlation logging already covers `task-06`'s needs — no header to build, no new correlation mechanism.
- **Logging levels match `.planning/LOGGING.md`'s criticality mapping:** INFO for successful `createAssessmentBrief`/`generateAssessmentDraft`/`submitAssessmentBrief`; ERROR for `createAssessmentBrief` failure; WARN for `generateAssessmentDraft` failure (recoverable — the brief already exists by the time this step runs). Sensitive-data guardrail respected: never logs the brief payload, only `assessmentId`/`status`/`latencyMs`/`dependency`.
- **Manual log sample:** see the Logging / Observability section above — real Pino output, one shared `correlationId` across all 4 lines, structured JSON, correct levels, no payload.
- **Unit tests:** `assessments.test.ts` (7 cases: `createAssessmentBrief` sends exact shape + parses response, throws `CreateAssessmentBriefError` on 422; `generateAssessmentDraft` posts to the correct path, throws `GenerateAssessmentDraftError` carrying `assessmentId`; `submitAssessmentBrief` orchestrates both calls in order, surfaces `GenerateAssessmentDraftError` distinguishably when step 2 fails after step 1 succeeds, surfaces `CreateAssessmentBriefError` — and only 1 call is made — when step 1 itself fails) — `7 passed, 7 total`.
- **Full suite re-run:** `76 tests, 71 passed`; the 5 failures are the same pre-existing, unrelated ones from `task-14`/`task-04`'s rounds (English-vs-Spanish label queries).
- **Static analysis:** `npm run lint` — still N/A, no ESLint config in this repo (pre-existing, unrelated). Substituted `npx tsc --noEmit`: 0 errors outside the pre-existing missing-`@types/jest` gap.
- **Runtime smoke:** `npm run build` compiles/type-checks cleanly (`✓ Compiled successfully`) — importantly this also confirms `pino` bundles cleanly into the client-side webpack build, since `assessments.ts` is imported from `"use client"` pages. Fails only at prerender with the same pre-existing missing-Firebase-credentials sandbox gap (this time surfacing on `/register` — same root cause, different page, since prerender order varies). `npm run dev` started cleanly (`✓ Ready in 1490ms`), `curl http://localhost:3000/dashboard` returned `HTTP 200` with no console errors in the dev log.
- **`[CHECK-ATOMICITY]`:** scope stayed within the task's declared files plus `FieldErrorResponse`/`ApiErrorResponse` (required to type the already-traced 422/error shapes, added to the same `src/types/assessment.ts` file already in scope) and the `pino` dependency addition (required by the human-approved logging decision this task itself gates on). No UI changes, no other endpoints touched.

---

## Done Criteria

- [x] `CreateAssessmentBriefRequestDto`/`ResponseDto` match `task-01`'s confirmed shapes exactly.
- [x] `submitAssessmentBrief` orchestrates both calls sequentially and distinguishes which step failed. Two distinct error classes (`CreateAssessmentBriefError`, `GenerateAssessmentDraftError`), the latter carrying `assessmentId`.
- [x] All new/extended tests in `assessments.test.ts` pass — `7 passed, 7 total`.
- [~] `npm run lint` passes — N/A, no ESLint config in this repo (pre-existing gap, unchanged from prior tasks); substituted `npx tsc --noEmit`, 0 errors outside the pre-existing test-typings gap.
- [x] Logging mechanism decision is recorded in `.planning/LOGGING.md` before this task is marked done, or explicitly deferred to `task-06` with the human's sign-off recorded here. Recorded: Pino, structured JSON, human-confirmed 2026-07-16, before any implementation.
- [x] Software smoke test check above passes (build/startup confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR. (PR publish step next in the task workflow.)
- [x] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section. See manual log sample above.
- [x] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence — `test-suites/task-05-data-provider-intake-screen-test-suite.md` generated and gaps filled.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — see Verification Summary's atomicity note.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
