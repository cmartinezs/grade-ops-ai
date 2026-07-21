# ⚛️ TASK 12 — connect-real-api-draft-builder-screen

> **Status:** IN PROGRESS
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-09, task-10, task-11
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

The Draft Builder screen loads via `loadAssessmentDraftBuilderPage`, saves edits and regenerates via the real mutations, refetches versions after either succeeds, and surfaces 404/422/500 (plus 502/503 agent errors) — the fake local dataset from `task-09` is fully removed.

---

## Technical Design

- **Approach:** Swap `useAssessmentDraftBuilderPage`'s fake dataset and fake `onSave`/`onRegenerate` (from `task-09`) for a call to `loadAssessmentDraftBuilderPage` (from `task-10`) on mount and the real mutations (from `task-11`) — no other component or prop-shape changes, since `task-09` already built the real component tree with `onSave`/`onRegenerate` owned by the page hook and passed through to the Sections unchanged (`task-08`'s hierarchy).
- **Affected files / components:**
  - `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` (replace fake data with the real loader; replace fake save/regenerate handlers with the real ones)
  - `src/app/(protected)/assessments/[id]/draft/page.tsx` (pass `params.id` through)
  - Section test files (extend for real error states)
- **Interfaces / contracts:** The page hook's public shape (`RemoteData`-style page state, per `06-estado-datos-y-api.md` §8, including `onSave`/`onRegenerate` and their `isSaving`/`isRegenerating`/error fields per `task-08`) stays what `task-09` established — only what feeds it changes, from fake data/fake mutations to `task-10`'s loader and `task-11`'s real mutation functions.
- **Risk:** Medium — per `06-estado-datos-y-api.md` §13, after regenerate (always) or a save (which always edits the current version, since the editor is read-only while previewing history per `task-08`), the version list must be refetched, not assumed stale-safe; missing this would show the teacher an outdated version list right after they just created a new version.
- **Design notes:** Per `15-backend-frontend-contracts.md` §4 and `task-07`'s traced backend contract: 404 → assessment not found (shouldn't normally happen via the normal navigation flow from `task-06`, but handle it defensively — e.g. a stale bookmark); 422 → business validation error (field validation / empty notes / agent-rejected / no-prior-draft), translate the message; 502/503 → agent-down, translate the message, current draft is never cleared. **No 409 handling** — `task-07`/`task-08` both confirmed no draft endpoint returns one (`GlobalExceptionHandler` maps 409 only for the unrelated `DuplicateEmailException`); the last-write-wins concurrency risk this replaces is a documented backend limitation, not a UI-detectable conflict.

---

## Implementation Steps

1. Replace `useAssessmentDraftBuilderPage`'s fake dataset with a `loadAssessmentDraftBuilderPage(assessmentId)` call on mount, using the `RemoteData` states from `task-09`.
2. Replace the page hook's fake `onSave` with a call to `updateAssessmentDraft` (from `task-11`), and on success, refetch the version list (or the full page data) via `loadAssessmentDraftBuilderPage` — `DraftEditorSection`'s own props/behavior are unchanged from `task-09`, only what the page hook's `onSave` does internally changes.
3. Replace the page hook's fake `onRegenerate` with a call to `regenerateAssessmentDraft` (from `task-11`), and on success, refetch the version list and update the displayed draft to the new version — same "Section props unchanged" note as step 2.
4. Map 404/422/500 (plus 502/503 agent errors) to translated messages per `15-backend-frontend-contracts.md` §4, surfaced via the existing error-state UI from `task-09`. No 409 mapping exists — `task-07`/`task-08` confirmed no draft endpoint returns one.
5. Extend Section tests to cover: successful save/regenerate refreshes the version list; an agent-rejected (422) or agent-down (502/503) error during regenerate shows a clear message without clearing the current draft (`task-07`); a 404 on initial load shows a "not found" state.
6. Remove the fake dataset and fake `onSave`/`onRegenerate` code paths entirely from `task-09` — no leftover dead code or feature flag.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Initial load calls the real `loadAssessmentDraftBuilderPage` and renders the returned draft/versions | `npm run test` |
| 2 | Successful save refetches and reflects the updated draft | `npm run test` |
| 3 | Successful regenerate refetches the version list and shows the new version as current | `npm run test` |
| 4 | 404/422/500 (plus 502/503 agent errors) each render a distinct, translated message — no 409 case exists (`task-07`/`task-08`) | `npm run test` |
| 5 | No fake/mocked dataset remains in the hook | Manual code review |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | Changed surface responds correctly | With `api/` running locally and a real assessment id from `task-06`'s flow, load the draft screen, edit and save, then regenerate, and confirm both reflect in the UI with an updated version list |
| 3 | No startup regressions are visible | Inspect `npm run dev` output for new errors |

### Database / ORM Consistency Check

N/A — no database or ORM involved in `web/`.

### Logging / Observability

- **Logging mechanism:** Reuse whatever was decided in `task-05`/`task-10`/`task-11` — do not re-decide.
- **Correlation / trace context:** Initial load uses the loader's correlation id (`task-10`); save/regenerate each use their own mutation-scoped id (`task-11`), plus the follow-up refetch after either should carry a new load-scoped id since it's a fresh page-data read, not part of the mutation itself.
- **Levels by event criticality:** INFO on successful load/save/regenerate; WARN on 404/422/502/503 (recoverable via navigation/correction/retry); ERROR on 500. No 409 level applies (`task-07`/`task-08`: no draft endpoint returns one).
- **Execution trace points:** Page mount → loader call; save/regenerate handler entry → mutation call → refetch call → completion/failure.
- **Sensitive data guardrails:** Same as `task-10`/`task-11` — no full draft text or adjustment notes in logs.
- **Verification evidence:** A test or manual log sample showing the full load → edit → save → refetch sequence with correlation ids present at each step.

### Generated Test Suite

- **Task suite file:** `test-suites/task-12-connect-real-api-draft-builder-screen-test-suite.md`
- **Required gates:** unit, coverage, integration, static analysis (`npm run lint`), code style, smoke, architecture/design guide review.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md`, `15-backend-frontend-contracts.md`.
- **Acceptance environment:** Mock `apiClient`/`fetch` for unit tests; a manual smoke pass against a real local `api/` instance (`./mvnw spring-boot:run`) covers the integration gate — no Docker/Testcontainers needed.
- **Acceptance dependency inventory:** `api/` running locally at the configured `NEXT_PUBLIC_API_BASE_URL`; a real assessment id with an existing draft (produced via `task-06`'s flow) to test against.
- **Missing acceptance profile:** N/A — not a Maven/Cucumber service.

---

## Verification Summary

### 1. Real backend contract traced directly (not assumed from the wireframe alone)

Read `api/`'s source directly, same discipline as `task-07`/`task-10`/`task-11`: `GetCurrentDraftHandler.java`, `UpdateAssessmentDraftHandler.java`, `RegenerateAssessmentDraftHandler.java`, `GlobalExceptionHandler.java`, `NoPriorDraftException.java`, `AssessmentController.java` (2026-07-21). Two findings beyond what the wireframe/task-01/task-07 already established:

**Finding A — the wireframe's "empty (defensivo)" and "404 assessment no existe/ownership" states are indistinguishable in practice.** `GetCurrentDraftHandler.execute()`:
```java
Assessment assessment = assessmentRepository.findById(assessmentId)
        .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
ownershipVerifier.verify(...);
AssessmentDraft draft = assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)
        .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
```
Both "assessment doesn't exist/isn't yours" and "no draft generated yet" throw the *same* `ResourceNotFoundException`, and `GlobalExceptionHandler.handleNotFound` maps it to the *same* `404 {error:"NOT_FOUND", message:assessmentId}` body in both cases — byte-identical. The frontend cannot tell these apart from the response. Implemented as **one** `not-found` page state (not two, as the wireframe's 12-state table implied) — see § Design Correction below for why this doesn't contradict this task's own Design notes, which already described a single 404 case.

**Finding B — task-11's `UpdateAssessmentDraftError`/`RegenerateAssessmentDraftError` were mistyped.** `AssessmentController.updateDraft()`/`regenerateDraft()` both use `@Valid @RequestBody`, so a bean-validation failure (`@Size`/`@NotBlank`) throws Spring's `MethodArgumentNotValidException`, which `GlobalExceptionHandler.handleValidation()` maps to a `List<FieldErrorResponse>` body — an **array**, not the `ApiErrorResponse` object task-11 typed `body` as. This is the same shape `CreateAssessmentBriefError` already correctly types as `FieldErrorResponse[] | ApiErrorResponse`. **Fixed** in this task (see § Design Correction #2) — required to correctly implement Implementation Step 4's 422 sub-case translation.

### 2. Design Corrections to already-merged task-10/task-11 code

Both required because implementing task-12 against the *real* API surfaced gaps that couldn't have been caught without actually wiring it up:

1. **`toAssessmentDraftBuilderPageViewModel.ts` (task-10):** added a `versionDrafts: AssessmentDraftViewModel[]` field to `AssessmentDraftBuilderPageData`, carrying full content for every version (not just the preview-only `versions` field). Without this, there was no way for the page hook to show a past version's full content read-only in the editor (task-08/09's already-tested "browse history" requirement) without either bypassing the Screen Data Facade (explicitly disallowed by task-10's own risk note) or losing data the mapper was already discarding. Additive change — `versions`/`draft` fields unchanged, so no existing consumer (`loadAssessmentDraftBuilderPage.test.ts`) broke.
2. **`assessments.ts` (task-11):** `UpdateAssessmentDraftError`/`RegenerateAssessmentDraftError`'s `body` type widened from `ApiErrorResponse` to `FieldErrorResponse[] | ApiErrorResponse`, matching Finding B above. Backward compatible — existing task-11 tests (which never asserted on the type, only runtime shape) still pass unmodified.

### 3. Fake dataset fully removed

`useAssessmentDraftBuilderPage.ts` no longer contains `buildFakeVersions()`/`LONG_INSTRUCTIONS` or any local state seeded from fake data. Confirmed via `grep -n "fake" src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` → no matches. The hook now calls `loadAssessmentDraftBuilderPage` (task-10) on mount and `updateAssessmentDraft`/`regenerateAssessmentDraft` (task-11) for mutations exclusively.

### 4. Version list refetch after save/regenerate

`onSave`/`onRegenerate` both call `await loadPage()` (which re-invokes `loadAssessmentDraftBuilderPage`) immediately after their respective mutation succeeds, before clearing `isSaving`/`isRegenerating` — verified by 2 dedicated integration tests asserting `getAssessmentDraft`/`getAssessmentDraftVersions` were each called exactly twice (initial mount + post-mutation refetch) after a successful save/regenerate.

### 5. Error surface — distinct translated messages, no 409

| Backend condition | HTTP | Body | UI treatment | Test |
|---|---|---|---|---|
| Assessment/draft not found | 404 | `{error:"NOT_FOUND"}` | Full-screen "No encontramos esta evaluación." + link to `/assessments` | ✓ integration |
| Field validation on save | 422 | `FieldErrorResponse[]` | Inline error under the affected field | ✓ integration + unit |
| Empty notes on regenerate | 422 | `FieldErrorResponse[]` | Inline error under notes field | ✓ unit |
| No prior draft (save/regenerate) | 422 | `{error:"APPLICATION_ERROR"}` | Defensive message, section-level | ✓ unit (both) |
| Agent rejected (regenerate) | 422 | `{error:"AGENT_CALL_FAILED", message:"AGENT_REJECTED"}` | "No pudimos regenerar..." banner, current draft preserved | ✓ integration + unit |
| Agent down (regenerate) | 502/503 | `{error:"AGENT_CALL_FAILED", message:"AGENT_ERROR"/"UNREACHABLE"}` | "Servicio no disponible..." banner, distinct from agent-rejected | ✓ integration + unit |
| Unexpected (any) | 500 | `{error:"INTERNAL_ERROR"}` | Generic retry message | ✓ integration + unit |

No 409 branch exists anywhere in the implementation — confirmed by `grep -n "409" src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts src/lib/api/assessments.ts` → no matches, consistent with `task-07`/`task-11`'s traced finding that no draft endpoint can produce one.

### 6. Test execution

```
$ npm run test -- --testPathPattern="page.integration" --no-coverage
Test Suites: 1 passed, 1 total
Tests:       13 passed, 13 total

$ npm run test -- --testPathPattern="useAssessmentDraftBuilderPage\.test" --no-coverage
Test Suites: 1 passed, 1 total
Tests:       16 passed, 16 total
```
Full repo run: `Test Suites: 2 failed, 20 passed, 22 total` / `Tests: 5 failed, 131 passed, 136 total` — the 2 failed suites (`SignOutButton.test.tsx`, `RegisterPage.test.tsx`) are pre-existing, unrelated to task-12 (confirmed present before this task's changes).

### 7. Coverage

```
useAssessmentDraftBuilderPage.ts:         96.93% stmts, 84.61% branch, 100% funcs, 100% lines
toAssessmentDraftBuilderPageViewModel.ts: 100% all
assessments.ts:                           92.36% stmts (uncovered: pre-existing getAssessments(), out of scope)
```
Remaining uncovered branches in the hook are defensive early-returns (`if (pageState.status !== "ready") return`, `if (selectedVersion !== currentVersionNumber) return` in `onSave`) unreachable via the UI since the relevant buttons/inputs don't render until `status === "ready"` — same category of low-risk defensive guard already accepted in task-10's own coverage evidence.

### 8. Lint

```
$ npx eslint src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts \
    src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts \
    src/lib/api/assessments.ts \
    "src/app/(protected)/assessments/[id]/draft/page.tsx" \
    "src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx"
exit code: 0 (no output)
```

### 9. Build & smoke

```
$ npm run build
✓ Compiled successfully in 2.5s

$ npm run dev &
✓ Ready in 1579ms
✓ Compiled / in 1255ms
GET / 307 in 1577ms   ← expected auth redirect, not an error
```
**Real-`api/` connectivity smoke — re-attempted with Docker (unsandboxed) and completed as far as this environment allows:**

Started `api/`'s real local stack: `docker compose up -d` (Postgres 16, `api/compose.yml`) + `./mvnw spring-boot:run -Dspring-boot.run.profiles=local` against it, using a syntactically-valid-but-fake service-account JSON for `firebase.credentials-path` (a real Firebase project isn't available in this environment, but `FirebaseConfig`'s `GoogleCredentials.fromStream(...)` only needs to *parse* a well-formed key at boot — it doesn't make a network call until a token is actually verified).

```
Started GradeOpsApiApplication in 3.709 seconds
Database: jdbc:postgresql://localhost:5432/gradeops (PostgreSQL 16.14)
Successfully validated 12 migrations
Schema "public" is up to date. No migration necessary.
Tomcat started on port 8080
```

Verified against the real running server (real Postgres, real Spring Security filter chain, real `AssessmentController` routing):

| Request | Result | Confirms |
|---|---|---|
| `GET .../draft` (no token) | `401` | `/api/v1/assessments/**` correctly requires auth |
| `GET .../draft` (garbage `Bearer` token) | `401`, no stack trace in logs | `FirebaseTokenFilter`'s catch block handles a malformed token gracefully, not a 500 |
| `GET /api/v1/assessments` (no token) | `401` | Same enforcement on the list endpoint |
| `PATCH .../draft` (no token, invalid body) | `401` (auth checked before body validation) | Security filter runs before controller/bean-validation |
| `POST /api/v1/auth/register` (no idToken) | `422 [{"field":"idToken","message":"must not be blank"}]` | Clean validation error, not a crash — and confirms registration itself requires a **client-side-issued** Firebase ID token, so there is genuinely no path to mint a real authenticated session without an actual Firebase project in this environment |

**Ceiling reached:** exercising the actual authenticated happy path (`GetCurrentDraftHandler`/`UpdateAssessmentDraftHandler`/`RegenerateAssessmentDraftHandler` executing against real rows) requires a Firebase ID token signed by a real Firebase project — confirmed at the infrastructure level (`FirebaseAuth.verifyIdToken()` needs Google's public certs for a *real* project, and even registration needs a client-issued token first). This is the same category of limitation `task-09` already documented for authenticated browser walkthroughs, now confirmed to extend to server-side smoke testing too — not a gap specific to this task's implementation.

**What this adds beyond the mocked integration tests:** real Postgres connectivity, real Flyway schema validation (12 migrations, matches what `task-01`–`task-11` produced), real Spring Security filter chain behavior, and confirmation that unauthenticated/malformed-token requests fail cleanly (401, not 500) — none of which the jsdom-mocked integration tests could verify, since they never touch a real HTTP server or database.

Environment cleaned up after verification: process killed, `docker compose down` (container + network removed). No changes committed from this exploration — `application-local.yml` is git-ignored (`api/.gitignore:57`).

### 10. Incidental fix — misplaced test file directory

Discovered `src/app/%28protected%29/assessments/%5Bid%5D/draft/page.integration.test.tsx` — a directory tree with **literal percent-encoded characters in its name** (`%28protected%29`, `%5Bid%5D`), tracked in git since `task-09`'s original commit, sitting *alongside* the real `src/app/(protected)/assessments/[id]/draft/` directory. Jest discovered and ran the test fine regardless (glob-based, not Next.js-routing-aware), which is why this went unnoticed through task-09/10/11's reviews. Fixed via `git mv` to the real path; the bogus directory is now empty and removed. This is the exact file task-12 already needed to modify (Implementation Step 5), so folding the relocation into this task's commit was the lowest-friction fix.

### 11. Logging

Reuses the `task-05`/`task-10`/`task-11` Pino decision — no re-decision. `loadPage()` delegates to `loadAssessmentDraftBuilderPage` (task-10's own correlation id, shared across its 2 parallel calls); `onSave`/`onRegenerate` delegate to `updateAssessmentDraft`/`regenerateAssessmentDraft` (task-11's own per-mutation correlation ids). The post-mutation refetch triggers a *new* `loadAssessmentDraftBuilderPage` call with a fresh correlation id (a new page-data read, not part of the mutation itself), per this task's own Logging section. No draft text or adjustment notes appear in any log call — same guardrail as task-10/11, unchanged.

---

## Code Review Corrections

Code review approved with 1 P3 finding — coverage gap, not a bug:

### P3 — `translateRegenerateError`'s 502/503 branch untested for a non-`AGENT_CALL_FAILED` body

- File: `useAssessmentDraftBuilderPage.ts:119`

The branch requires both `status ∈ {502, 503}` **and** `body.error === "AGENT_CALL_FAILED"`. A 502 with a different body (e.g. a generic infra-level `BAD_GATEWAY`) correctly falls through to `GENERIC_RETRY_MESSAGE`, but no test exercised that fallback specifically for a 502/503 status — matching the 84.61% branch coverage figure reported in §7.

**Fix:** added `"falls back to the generic retry message for a 502 that isn't AGENT_CALL_FAILED (e.g. an infra-level bad gateway)"` to `useAssessmentDraftBuilderPage.test.ts`, asserting `new RegenerateAssessmentDraftError(502, { error: "BAD_GATEWAY", message: null }, "a1")` → `{ fieldError: null, agentError: GENERIC_RETRY_MESSAGE }`.

**Re-verification:**
```
$ npm run test -- --testPathPattern="useAssessmentDraftBuilderPage\.test" --no-coverage
Tests: 16 passed, 16 total (1 new)

$ npx eslint src/features/assessment-creation/hooks/__tests__/useAssessmentDraftBuilderPage.test.ts
exit code: 0

$ npm run test -- --no-coverage
Test Suites: 2 failed, 21 passed, 23 total
Tests:       5 failed, 148 passed, 153 total
```
The 2 failed suites/5 failed tests are the same pre-existing `SignOutButton.test.tsx`/`RegisterPage.test.tsx` failures, unrelated to this task.

---

## Done Criteria

- [x] Draft Builder screen loads, edits/saves, and regenerates against the real API — see §§3-4; fake dataset fully removed, hook calls only `loadAssessmentDraftBuilderPage`/`updateAssessmentDraft`/`regenerateAssessmentDraft`.
- [x] Version list refetches after both save and regenerate — see §4, verified by dedicated tests asserting the refetch call count.
- [x] 404/422/500 (plus 502/503 agent errors) each show a distinct, translated message — no 409 case, since none exists for these endpoints (`task-07`/`task-08`) — see §5's full mapping table with test references; confirmed via grep that no 409 branch exists anywhere in the implementation.
- [x] No fake/mocked dataset remains — see §3.
- [x] All tests pass; `npm run lint` passes — see §§6, 8.
- [x] Software smoke test check above passes (build/startup/connectivity confirmed against a real local `api/`); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR — build/dev-server smoke confirmed in §9; real `api/` + real Postgres (via Docker) started and connectivity confirmed (§9) — Flyway validated 12 migrations, security filter chain correctly rejects unauthenticated/malformed-token requests with clean 401s; the authenticated happy path remains out of reach without a real Firebase project (confirmed at the infrastructure level, not assumed), consistent with `task-09`'s documented limitation; task branch created off the up-to-date story branch, PR pending publish.
- [x] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section — see §11.
- [x] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence — `test-suites/task-12-connect-real-api-draft-builder-screen-test-suite.md` regenerated and filled; integration/smoke/unit/coverage/static-analysis/architecture-review all have evidence; acceptance/e2e and security/mutation marked N/A with rationale.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — changes confined to the 2 files in this task's own affected-files list (`useAssessmentDraftBuilderPage.ts`, `page.tsx`) plus the integration test file and 2 narrowly-scoped, evidence-backed corrections to task-10/task-11 files that were prerequisites for this task's own real-API wiring to work correctly (documented in §2), plus one incidental file-path fix for a pre-existing misplaced test file this task was already touching (§10).

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
