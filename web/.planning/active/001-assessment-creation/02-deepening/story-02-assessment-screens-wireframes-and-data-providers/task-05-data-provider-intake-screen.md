# ⚛️ TASK 05 — data-provider-intake-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

DTOs for the brief-intake/draft-generation endpoints and a `submitAssessmentBrief()` function in `lib/api` that orchestrates the two sequential real calls (`POST /assessments` then `POST /assessments/{id}/draft`) behind one interface — independent of the mockup UI, ready for `task-06` to call.

---

## Technical Design

- **Approach:** This is a **mutation orchestration**, not a Page Data Loader/Screen Data Facade — the guide's Loader concept (`06-estado-datos-y-api.md` §7) is for rendering data on page load; this is a two-step write flow, covered by §10 Mutaciones instead. It still must not live inline in the Page/hook as two separate `await` calls — it's centralized in one `lib/api` function so the hook only makes one call and only handles one error surface.
- **Affected files / components:**
  - `src/types/assessment.ts` (add `CreateAssessmentBriefRequestDto`, `CreateAssessmentBriefResponseDto`)
  - `src/lib/api/assessments.ts` (add `createAssessmentBrief()`, `generateAssessmentDraft()`, and the orchestrating `submitAssessmentBrief()`)
  - `src/lib/api/__tests__/assessments.test.ts` (extend existing test file)
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
- [ ] `submitAssessmentBrief` orchestrates both calls sequentially and distinguishes which step failed.
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
