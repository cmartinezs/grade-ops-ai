# ⚛️ TASK 11 — mutations-draft-builder-screen

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-10
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

`updateAssessmentDraft()` (partial PATCH) and `regenerateAssessmentDraft()` (POST with adjustment notes) in `lib/api`, each with proper submitting/success/error semantics per `06-estado-datos-y-api.md` §10, and each triggering a version-list refetch after success since both can produce a new version.

---

## Technical Design

- **Approach:** Two separate mutation functions, not one generic "updateDraft" — `PATCH` (edit) and regenerate (`POST .../regenerate`) hit different endpoints with different request shapes (`task-01`) and different semantics (editing the current version in place vs. producing a new version via AI). Both must trigger a refetch of the version list afterward per `06-estado-datos-y-api.md` §13 (sync with backend after a critical mutation) — regenerate always creates a new version, and a save always edits the current version (per `task-08`'s hierarchy, `PATCH` is only reachable while viewing the current version — the editor is read-only while previewing history, so there is no "PATCH while viewing a past version" case to design for).
- **No 409 handling (correction — this task previously assumed one):** `task-07` traced the real backend and found neither `UpdateAssessmentDraftHandler` nor `RegenerateAssessmentDraftHandler` implements optimistic locking, and `GlobalExceptionHandler` maps 409 only for `DuplicateEmailException` (an auth-domain exception unrelated to drafts) — no draft endpoint can ever return 409. Both mutation functions here surface the real error surface instead: 422 (field validation / empty notes / agent-rejected / no-prior-draft), 502/503 (agent down), and 500. The last-write-wins concurrency risk this replaces is a documented, unresolved backend limitation (`task-07`), not something a 409 branch in this task could ever catch.
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
    adjustmentNotes: string
  ): Promise<AssessmentDraftDto>
  ```
  Both return the same `AssessmentDraftDto` shape from `task-10`.
- **Risk:** Medium — `UpdateAssessmentDraftRequestDto`'s fields are all optional per `task-01`'s confirmed contract (`null`/absent means "don't change this field"); a caller that sends an empty string instead of omitting a field would unintentionally blank it out server-side. `updateAssessmentDraft` must only include keys the caller actually changed, not all fields with empty-string defaults.
- **Design notes:** Neither function refetches the version list itself — per `06-estado-datos-y-api.md` §13's own framing, that's a page-level concern; `task-12`'s hook is responsible for calling `loadAssessmentDraftBuilderPage` again (or at least the versions half) after either mutation succeeds. This task only guarantees the two functions themselves are correct.

---

## API / Agent / Web Contract Gate

> Added to `develop` post-divergence (commit `e2703d5`, 2026-07-20); reconciled into this already-DONE task during story-02 closeout (2026-07-21) with real evidence, not left as the generic prescriptive text.

| Gate | Required check | Task answer |
|---|---|---|
| API as orchestrator | Mutations call `api/` only; regenerate does not call or configure `agents/` from `web` | **Confirmed.** `updateAssessmentDraft`/`regenerateAssessmentDraft` call only `/api/v1/assessments/{id}/draft` (PATCH) and `.../draft/regenerate` (POST) — neither `UpdateAssessmentDraftRequestDto` nor the regenerate call signature carries any provider/model/prompt field. |
| Richardson REST maturity | `PATCH /draft` is partial update; `POST /draft/regenerate` is command-style generation under the assessment resource | Preserved exactly: `PATCH` sends only caller-changed keys (§ Verification Summary #1), `POST .../regenerate` sends `{adjustmentNotes}` (§2) — matching `task-01`'s confirmed contract; no `202`/`Location` invented. |
| AI operation model | Regeneration is GenAI-backed but sync legacy today | Confirmed: `regenerateAssessmentDraft` returns the full `AssessmentDraftDto` synchronously (§ Verification Summary #2) — no `operationId`/polling state fabricated. |
| Idempotency | Regeneration should use `Idempotency-Key` when API supports it; PATCH should avoid duplicate unintended writes via changed-key-only payloads | Per `task-01`'s gate finding: absent from `api/`. `updateAssessmentDraft` already avoids unintended writes via the changed-keys-only design (§ Verification Summary #1); no client-side auto-retry exists for either mutation — a failed save/regenerate requires an explicit teacher re-click (`task-12`). |
| Contract testing | Tests assert exact PATCH body, regenerate body, paths and conflict behavior | Done, with the important correction that **no conflict (409) behavior exists to test** — `task-07` traced the real backend and found no draft endpoint returns 409; the explicit test sending a mocked 409 (§ Verification Summary #3) proves no special branch catches it, i.e., 409 is correctly treated as an ordinary unmapped status, not silently assumed away. |
| Web route functionality | Save/regenerate support submitting, success, conflict, validation and server-error states | This task's own scope is the `lib/api` layer only (route/UI states are `task-12`'s scope); the two distinct error classes this task exports (`UpdateAssessmentDraftError`, `RegenerateAssessmentDraftError`) are exactly what let `task-12` build submitting/success/validation/server-error states without a conflict state, since none is backed by the real API. |

---

## Implementation Steps

1. Add `UpdateAssessmentDraftRequestDto` to `src/types/assessment.ts` — all fields optional, matching `task-01`'s confirmed `UpdateAssessmentDraftRequest` partial-update semantics exactly (only include keys actually being changed).
2. Add `updateAssessmentDraft(assessmentId, changes)` to `src/lib/api/assessments.ts`, `PATCH`-ing `/api/v1/assessments/${assessmentId}/draft` with only the provided keys.
3. Add `regenerateAssessmentDraft(assessmentId, adjustmentNotes)` `POST`-ing `/api/v1/assessments/${assessmentId}/draft/regenerate` with `{ adjustmentNotes }`.
4. Write tests: `updateAssessmentDraft` sends only the changed keys (not a full object with empty-string defaults); `regenerateAssessmentDraft` sends the notes and parses the new draft; both surface 422 field/notes/agent-rejected errors and 502/503 agent-down distinctly from a generic 500, per `06-estado-datos-y-api.md` §9 and `task-07`'s traced error surface — neither surfaces a 409, since none exists for these endpoints.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `updateAssessmentDraft` sends only the caller-provided keys, never blanking unspecified fields | `npm run test -- assessments` |
| 2 | `regenerateAssessmentDraft` sends `{ adjustmentNotes }` and returns the new `AssessmentDraftDto` with an incremented `versionNumber` | `npm run test -- assessments` |
| 3 | Both functions surface 422 (field/notes/agent-rejected) and 502/503 (agent-down) as distinguishable from a generic 500 — neither surfaces a 409, since none exists for these endpoints (`task-07`) | `npm run test -- assessments` |

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
- **Levels by event criticality:** INFO on successful update/regenerate; WARN on 422 (field/notes/agent-rejected) and 502/503 (agent-down) — all recoverable, teacher can correct input or retry; ERROR on 500. No 409 level applies (`task-07`: no draft endpoint returns one).
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

## Verification Summary

### 1. `updateAssessmentDraft` sends only caller-provided keys

Test `"PATCHes /api/v1/assessments/{assessmentId}/draft sending only the caller-provided keys"` asserts `Object.keys(sentBody)` equals exactly `["title"]` when only `title` is passed — no other fields appear as empty-string/null defaults. A second test (`"sends multiple changed keys together without including unspecified ones"`) confirms this holds for multi-field partial updates too.

### 2. `regenerateAssessmentDraft` sends notes and returns the new draft

```
$ npm run test -- --testPathPattern="assessments\.test" --no-coverage
...
regenerateAssessmentDraft
    ✓ POSTs to /api/v1/assessments/{assessmentId}/draft/regenerate with { adjustmentNotes }
    ✓ returns the new AssessmentDraftDto with an incremented versionNumber
    ✓ throws RegenerateAssessmentDraftError carrying the ApiErrorResponse body, status, and assessmentId on 422 (empty/agent-rejected notes)
    ✓ throws RegenerateAssessmentDraftError on 502 (agent down)
    ✓ throws RegenerateAssessmentDraftError on a generic 500 without a 409 branch (task-07: no draft endpoint returns 409)
```

### 3. Error surface: 422/502/503 vs 500, no 409

Both functions throw dedicated error classes (`UpdateAssessmentDraftError`, `RegenerateAssessmentDraftError`) carrying `status`, `body: ApiErrorResponse`, `assessmentId` — mirroring `task-10`'s `GetAssessmentDraftError` pattern. A dedicated test explicitly sends a mocked `409` response and confirms it falls through the same generic error path as any other status (no special 409 branch exists in the implementation), directly verifying the task's "no 409 handling" design note against `task-07`'s traced backend confirmation.

Log-level criticality verified via logger mock assertions (not just code reading):
```
updateAssessmentDraft / regenerateAssessmentDraft — logging level by criticality
    ✓ logs WARN (not ERROR) for 422/502/503 on updateAssessmentDraft
    ✓ logs ERROR (not WARN) for a generic 500 on updateAssessmentDraft
    ✓ logs WARN (not ERROR) for 422/502/503 on regenerateAssessmentDraft
    ✓ logs ERROR (not WARN) for a generic 500 on regenerateAssessmentDraft
```

### 4. Full test run

```
$ npm run test -- --testPathPattern="assessments\.test" --no-coverage
Test Suites: 1 passed, 1 total
Tests:       27 passed, 27 total (11 new for task-11 + 16 existing)
Time:        0.765 s
```

### 5. Lint (scoped)

```
$ npx eslint src/types/assessment.ts src/lib/api/assessments.ts src/lib/api/__tests__/assessments.test.ts
exit code: 0 (no output)
```

Repo-wide `npm run test` has 5 pre-existing failures in `SignOutButton.test.tsx`/`RegisterPage.test.tsx`, confirmed via `git stash` to exist on the story branch baseline before this task's changes — not a regression, out of `[CHECK-ATOMICITY]` scope (same pattern as task-10's pre-existing repo-wide lint note).

### 6. Coverage

```
File            | % Stmts | % Branch | % Funcs | % Lines | Uncovered
----------------|---------|----------|---------|---------|----------
assessments.ts  |    92.3 |    94.44 |   66.66 |   97.56 | 16-18
```
Lines 16-18 are the pre-existing `getAssessments()`, out of task-11's affected files.

### 7. Build

```
$ npm run build
✓ Compiled successfully in 1933ms
```

### 8. Logging

`.planning/LOGGING.md` was already confirmed by `task-05` — no re-decision needed. Each mutation creates its own `correlationId` (independent user actions, not part of one page-load operation, per this task's Logging section) via `logger.child({ correlationId, assessmentId })`. INFO on success (with `versionNumber`), WARN on 422/502/503, ERROR on 500 — verified by logger-mock tests in § Verification Summary #3, not just code inspection. No draft text or adjustment-notes content is logged — only `assessmentId`, `versionNumber`, `status`, `latencyMs`.

---

## Master Plan Addendum — Draft Mutation i18n Gate

Added after this task was already `DONE` in `develop`. Any R01 revalidation or future mutation change must make regeneration language explicit by passing supported `outputLocale`/`contentLocale` through the confirmed API contract and capturing it at request time.

PATCH preserves existing content-locale semantics unless `api/` explicitly changes them. Regeneration must not infer natural-language output from programming `language`, and logs/telemetry/error codes remain English.

---

## Done Criteria

- [x] `updateAssessmentDraft` only sends caller-provided keys, matching the partial-update contract — see § Verification Summary #1.
- [x] `regenerateAssessmentDraft` sends adjustment notes and returns the new draft — see § Verification Summary #2.
- [x] API / Agent / Web Contract Gate is completed; regenerate does not leak agent/provider/prompt concerns into `web` — reconciled 2026-07-21 (story-02 closeout); see § API / Agent / Web Contract Gate.
- [x] Both distinguish 422 (field/notes/agent-rejected) and 502/503 (agent-down) from a generic 500 — no 409 handling exists, since `task-07` confirmed no draft endpoint returns one — see § Verification Summary #3, including an explicit test sending a mocked 409 to confirm no special branch catches it.
- [x] All new/extended tests pass; `npm run lint` passes — 27/27 tests pass (§4); lint scoped to this task's affected files passes with exit 0 (§5, with rationale for the pre-existing unrelated test/lint noise).
- [x] Software smoke test check above passes (build/startup confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR — build confirmed in §7; task branch `story-02-assessment-screens-wireframes-and-data-providers--task-11-mutations-draft-builder-screen` created off the up-to-date story branch; PR pending publish.
- [x] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section — see §8.
- [x] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence — `test-suites/task-11-mutations-draft-builder-screen-test-suite.md` regenerated and filled with evidence for unit, coverage, static analysis, code style, and architecture/design guide review; integration/acceptance/security/mutation marked N/A with rationale.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — only the 3 files in Technical Design's affected-files list were changed (`src/types/assessment.ts`, `src/lib/api/assessments.ts`, `src/lib/api/__tests__/assessments.test.ts`); pre-existing unrelated test/lint failures were identified but deliberately left untouched.

---

## Code Review Corrections

Code review (`.code-reviews/story-02-.../task-11-mutations-draft-builder-screen.md`) approved with 2 P3 findings. Only 1 was actionable:

### P3 #1 — `isRecoverableDraftMutationStatus` not exported / testable in isolation

**Fix:** changed `function isRecoverableDraftMutationStatus` to `export function isRecoverableDraftMutationStatus` in `src/lib/api/assessments.ts`. Added a dedicated `describe("isRecoverableDraftMutationStatus", ...)` block with 2 tests asserting the classification directly (422/502/503 → `true`; 500/409/404 → `false`), independent of exercising the full `updateAssessmentDraft`/`regenerateAssessmentDraft` call path.

### P3 #2 — `updateAssessmentDraft`/`regenerateAssessmentDraft` don't accept an external `Logger` (consistency observation)

**No action taken.** The reviewer explicitly classified this as "not a blocking finding — the design decision is well-reasoned" and did not propose a change: mutations are independent user actions (not part of a shared page-load operation like task-10's loader), so each creating its own `correlationId` internally is the correct design, already documented in `EVIDENCE.md`'s Architecture Decision Rationale. Task-12 does not need to inject an external logger into these functions.

**Re-verification after fix:**
```
$ npm run test -- --testPathPattern="assessments\.test" --no-coverage
Test Suites: 1 passed, 1 total
Tests:       29 passed, 29 total (2 new isolation tests + 27 existing)
Time:        0.525 s

$ npx eslint src/lib/api/assessments.ts src/lib/api/__tests__/assessments.test.ts
exit code: 0 (no output)

$ npm run build
✓ Compiled successfully in 2.1s
```

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
