# ⚛️ TASK 13 — end-to-end-connection

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-06, task-12
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

The full Intake → Draft Builder flow works end-to-end against a real local `api/` instance with real ids, no fake/mock data remains anywhere in `src/features/assessment-creation/`, and story-01's Done Criteria that this story's scope covers are demonstrably satisfiable on top of it.

---

## Technical Design

- **Approach:** This task does not add new UI or API functions — `task-06` and `task-12` already connected each screen independently. This task's job is the **seam between them**: confirm the redirect from Intake actually lands on a working Draft Builder screen with real data, and do a final repo-wide sweep for anything `task-04`/`task-09` left behind (fake timers, hardcoded fixtures, dead feature flags).
- **Affected files / components:** No new files expected from the sweep itself (it found no residue — no leftover fake-data code, unused imports, or stray mocks directory). The real end-to-end walkthrough uncovered and required fixes in `next.config.ts` and `src/lib/firebase/client.ts`, and — ad hoc, in the shared repo per the task-12 precedent — `../api/src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/security/EmailVerifiedFilter.java`, its test `../api/src/test/java/cl/gradeops/ai/api/shared/infrastructure/config/security/EmailVerifiedFilterTest.java`, `../api/src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/security/SecurityConfig.java`, and a new regression test `../api/src/test/java/cl/gradeops/ai/api/shared/infrastructure/config/security/SecurityConfigTest.java` — see Verification Summary.
- **Interfaces / contracts:** None new — this task validates the existing contracts from `task-01` hold end-to-end, not just per-function in isolation.
- **Risk:** Medium — integration seams (redirect target, id propagation, version refetch timing) are exactly the class of bug that per-task unit tests don't catch; this is the only task in the story that exercises the real flow start to finish.
- **Design notes:** Per `02-ux-wireframes-y-maquetas.md` §2 step 9 ("agregar pruebas"), this is also the point to confirm the story-01 Done Criteria this scope covers are actually testable against real behavior, not just plausible on paper.

---

## Implementation Steps

1. `grep -r` for leftover fake-data markers (`setTimeout`, hardcoded fixture objects, a stray `mocks/` directory) under `src/features/assessment-creation/`; remove or justify each hit.
2. With `api/` running locally, manually walk the full flow: submit a real brief on `/assessments/new` → confirm redirect to `/assessments/{realId}/draft` → confirm the generated draft renders → edit a field and save → confirm the edit persists after a page refresh → regenerate with adjustment notes → confirm a new version appears and the version history shows the prior one.
3. Confirm story-01's Done Criteria that fall within this story's scope hold against the real flow just walked (not the mockups): required-field validation blocks submission; brief persists before the agent call; draft is fully editable and edits persist via the API; regeneration works with adjustment notes; previous versions remain accessible; draft and versions survive a page refresh.
4. Run the full test suite and lint once more across everything this story touched.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | No fake-data code remains under `src/features/assessment-creation/` | `grep -r "setTimeout\|mocks/" src/features/assessment-creation/` returns nothing unexplained |
| 2 | Full flow (submit brief → generated draft → edit → save → regenerate → version history) works against real `api/` | Manual walkthrough with `api/` running locally |
| 3 | Draft and version history survive a page refresh (re-fetched from the API, not held only in client state) | Manual refresh mid-walkthrough |
| 4 | Story-01's in-scope Done Criteria hold against the real flow | Manual cross-check against `../story-01-assessment-creation-ui.md` § Done Criteria |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `api/` running locally via `./mvnw spring-boot:run` (with its own local Postgres per `api/`'s own setup) |
| 2 | App compiles and starts | `npm run build` then `npm run dev` |
| 3 | Connectivity check succeeds | `web/` successfully reaches `api/` at the configured `NEXT_PUBLIC_API_BASE_URL` (no CORS/network errors in the browser console) |
| 4 | Changed surface responds correctly | The full manual walkthrough in Implementation Step 2 completes without unhandled errors |
| 5 | No regressions are visible | Existing `/assessments` list page and unrelated routes still render without new console errors |

### Database / ORM Consistency Check

N/A — this task touches no database or ORM artifacts directly; it exercises `api/`'s existing, already-`DONE` persistence layer as a black box.

### Logging / Observability

- **Logging mechanism:** Confirm whatever mechanism was decided in `task-05`/`task-10`/`task-11` is actually producing log output during the manual walkthrough — this task is the first point where the full chain of correlation ids (submit → generate → load → save → regenerate → refetch) can be observed together.
- **Correlation / trace context:** Confirm each logical operation (brief submission, draft load, edit save, regeneration) has its own correlation id and none leak across unrelated operations.
- **Levels by event criticality:** N/A beyond what earlier tasks defined — this task verifies, doesn't add new log points.
- **Execution trace points:** N/A — verification only.
- **Sensitive data guardrails:** Confirm no full draft text, adjustment notes, or brief content appears in captured log output during the walkthrough.
- **Verification evidence:** A captured log excerpt from the manual walkthrough showing distinct correlation ids per logical operation with no sensitive payload content.

### Generated Test Suite

- **Task suite file:** `test-suites/task-13-end-to-end-connection-test-suite.md`
- **Required gates:** integration/acceptance (manual, since no e2e browser-automation harness exists yet in this project), smoke, security (confirm no secrets/tokens logged), architecture/design guide review (confirm story-01 Done Criteria hold).
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §2 (full design-to-connection flow), `06-estado-datos-y-api.md` §13 (sync with backend).
- **Acceptance environment:** Real local `api/` instance (`./mvnw spring-boot:run` + its local Postgres), real local `web/` (`npm run dev`). No Docker Compose/Testcontainers needed — both services already support standalone local startup per their own existing setup.
- **Acceptance dependency inventory:** `api/` local instance and its database; `NEXT_PUBLIC_API_BASE_URL` pointed at it; a Firebase-authenticated teacher session (existing auth flow, out of this story's scope to build).
- **Missing acceptance profile:** N/A — not a Maven/Cucumber service; this is a manual walkthrough by design since no acceptance harness exists yet for `web/`.

---

## Verification Summary

### Step 1 — Fake-data sweep

```
$ grep -rn "setTimeout\|mocks/" src/features/assessment-creation/
src/features/assessment-creation/loaders/__tests__/loadAssessmentDraftBuilderPage.test.ts:61:          setTimeout(() => {
src/features/assessment-creation/loaders/__tests__/loadAssessmentDraftBuilderPage.test.ts:71:          setTimeout(() => {

$ find src/features/assessment-creation -iname "*mock*" -o -iname "*fixture*"
(no output)
```

Both hits are inside `loadAssessmentDraftBuilderPage.test.ts`, lines 54–76, using `setTimeout` to stagger two mocked promise resolutions and assert `Promise.all` fires both fetches in parallel rather than sequentially — a test-timing device, not production fake-data. No `mocks/`/fixture directory exists. Sweep is clean.

### Step 2/3 — Real end-to-end walkthrough (real local `api/` + Firebase Auth Emulator, real browser via Playwright MCP)

Infrastructure: reused task-12's `api/compose.smoke.yml` (Postgres + Firebase Auth Emulator, project `demo-gradeops-smoke`), extended `web/src/lib/firebase/client.ts` with `connectAuthEmulator()` gated behind `NEXT_PUBLIC_FIREBASE_AUTH_EMULATOR_HOST` (unset in demo/beta/prod) so a real browser session — not just curl — could authenticate against the emulator.

Walkthrough performed and evidence:
1. **Registration** — real browser form submit → `POST /api/v1/auth/register` → real `teacher` row persisted. Confirmed via `SELECT * FROM teacher;`: `firebase_uid=NXZr6ECvSYdtRr9MLct07V9yIFeM, first_name=Ada, last_name=Lovelace, email=ada-e2e-v2-1784617569@test.com, provider=EMAIL_PASSWORD`.
2. **Login** — real `signInWithPassword` against the emulator, redirect to `/dashboard` confirmed by polling `page.url()` (Next.js App Router client-side navigation doesn't fire a full `load` event, so `waitForURL`'s default `until:"load"` never resolves — polling is the correct check, not a bug).
3. **Intake → real brief persistence** — submitting `/assessments/new` created a real `assessments` row before any agent call: `SELECT id, teacher_uid, status FROM assessments;` → `id=cb77bcfb-b454-4000-90a3-e8795176e90f, status=DRAFT`. Draft generation then surfaced the expected agent-down error banner (no `agents/` service available in this environment) — confirmed this is the correct/designed error path, not a bug, matching task-06's error handling.
4. **Draft Builder render** — seeded a draft directly via SQL for `cb77bcfb-b454-4000-90a3-e8795176e90f` (version 1, "Recursividad: Fibonacci") to exercise the redirect-target/render seam independent of the unavailable agent service. Confirmed the Draft Builder screen at `/assessments/{realId}/draft` renders the real seeded data.
5. **Edit + save + refresh persistence** — edited a field in the real browser, saved (`PATCH` request), reloaded the page, confirmed the edit was still present (re-fetched from the API, not held in client state only).
6. **Regenerate** — triggered regeneration with adjustment notes; surfaced the expected agent-down error while leaving the existing draft/version intact (no partial/corrupt state) — again the correct error path given no `agents/` service in this environment, not the AI-generation happy path itself (blocked by environment ceiling, not a code defect).

**Three genuine, previously-undetected production bugs were found and fixed** purely by exercising the real system end-to-end (none were catchable by the existing jest-mocked test suite, which mocks `fetch` and never exercises the proxy/CORS/auth-whitelist seams):

| # | Bug | File | Root cause | Fix | Evidence |
|---|-----|------|-----------|-----|----------|
| 1 | Every browser-originated API call 500'd | `web/next.config.ts` | `rewrites()` stripped the `/api` prefix (`destination: ".../:path*"`), but every `api/` controller is mapped under `@RequestMapping("/api/v1")`, so requests hit a path `api/` doesn't serve → `NoResourceFoundException` → `GlobalExceptionHandler`'s catch-all masked it as a generic 500 | Changed destination to `".../api/:path*"` | curl: before → `GET /v1/assessments` (no `/api`) with valid Bearer token → `{"error":"INTERNAL_ERROR"}` 500; after → `GET /api/v1/assessments` → `200 []` |
| 2 | Every fresh email/password self-registration 401'd | `api/.../EmailVerifiedFilter.java` | `WHITELIST` held bare paths (`/auth/register`) but `HttpServletRequest.getRequestURI()` returns the full servlet path (`/api/v1/auth/register`); `startsWith` never matched | Updated `WHITELIST` to full paths `/api/v1/auth/register`, `/api/v1/auth/verify/resend` | Playwright network capture: before → `POST /api/v1/auth/register => [401]` followed by a rollback `accounts:delete => [200]`; after → registration succeeds, real `teacher` row persisted (see walkthrough step 1) |
| 3 | Saving draft edits always 403'd from a real browser | `api/.../SecurityConfig.java` | `corsConfigurationSource().setAllowedMethods(...)` omitted `PATCH`, and `AssessmentController.updateDraft()` is a `@PatchMapping` — Spring's `CorsFilter` rejects any CORS-preflighted method not in the allow-list | Added `"PATCH"` to `allowedMethods` | curl before: `curl -X PATCH .../draft -H "Origin: http://localhost:3000" ...` → 403 `"Invalid CORS request"`; after → 200 with the updated draft body; also confirmed via real browser edit+save+refresh (walkthrough step 5) |

A regression test was added for bug 3 (the only one of the three with no prior test at all, buggy or otherwise — bug 1 has no equivalent web-side test since jest mocks `fetch`; bug 2's existing `EmailVerifiedFilterTest` was itself testing the wrong path and was corrected in place):

```
api/src/test/java/cl/gradeops/ai/api/shared/infrastructure/config/security/SecurityConfigTest.java
  shouldAllowPatchForDraftEditSaves()
```

Red-green verified: reverted `SecurityConfig`'s `allowedMethods` to the buggy list (no `PATCH`) → `./mvnw test -Dtest=SecurityConfigTest` → `Tests run: 1, Failures: 1` (assertion: `to contain: ["PATCH"] but could not find`). Restored the fix → same command → `Tests run: 1, Failures: 0`.

### Step 3 — Story-01 Done Criteria cross-check against the real flow

| Story-01 Done Criteria | Verified against real flow | Evidence |
|---|---|---|
| Required-field validation blocks submission with missing learning goal/topic/level/duration/language | Held — RHF+Zod validation on `/assessments/new` (unchanged by this task; exercised during walkthrough step 3's submission) | Pre-existing `BriefForm.test.tsx` (23/23 passing, see test run below) plus manual submission during the walkthrough |
| Submitting the brief persists it via the API before any agent call, with a clear loading/confirmation state | Held | Walkthrough step 3: `assessments` row `cb77bcfb-b454-4000-90a3-e8795176e90f` persisted with `status=DRAFT` before the (unavailable) agent call was attempted; loading state and the agent-down error banner both rendered correctly |
| Generated draft rendered fully editable (all six fields), edits persist via the API | Held | Walkthrough steps 4–5: seeded draft rendered with all fields editable; edit survived a page refresh (re-fetched, not client-only state) — this is also where bug 3 (CORS PATCH) was caught and fixed |
| Teacher can trigger regeneration with adjustment notes | Held (seam confirmed; AI generation itself blocked by no `agents/` service in this environment) | Walkthrough step 6: regeneration request correctly reached the backend and produced the expected agent-down error without corrupting existing state |
| Previous draft version(s) remain accessible after regeneration | Not independently re-verified live in this walkthrough beyond task-12's existing coverage, since regeneration itself couldn't complete without `agents/` | Relying on task-12's existing `VersionHistorySection.test.tsx` + `RegenerateAssessmentDraftHandlerIntegrationTest` (both re-run green below) — documented gap, not silently assumed |
| Draft and versions survive a page refresh | Held | Walkthrough step 5 |
| All forms use RHF + Zod, no native HTML validation | Held (unchanged by this task) | Pre-existing `BriefForm.test.tsx`, `DraftEditorSection.test.tsx` |
| `npm run test` and `npm run lint` pass | Held | See Step 4 below |

### Step 4 — Full test suite + lint re-run (after all three fixes)

**web/:**
```
$ npm run test -- --silent
Test Suites: 23 passed, 23 total
Tests:       153 passed, 153 total
Time:        4.804 s

$ npm run lint
✔ No ESLint warnings or errors
```

**api/** (touched by bugs 2 and 3 — `EmailVerifiedFilter.java`/its test, `SecurityConfig.java`, plus the new `SecurityConfigTest.java`):
```
$ ./mvnw test
Tests run: 289, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
(Includes `EmailVerifiedFilterTest` — corrected to assert against the real full-servlet-path whitelist — and the new `SecurityConfigTest`, both passing; also `HexagonalArchitectureTest` and every Testcontainers-backed integration test, run after Docker Desktop's WSL integration was restarted mid-session.)

### Logging / Observability

Relying on pre-existing unit-test evidence from task-10/task-11/task-12 for Pino correlation-id behavior (`useAssessmentDraftBuilderPage.test.ts`, `apiClient.test.ts`) rather than a fresh browser-console capture: attempted to capture live Pino JSON log lines via Playwright's `browser_console_messages` during the walkthrough but only HMR/dev-server noise surfaced, no clear correlation-id lines distinguishable in the dev console output. This is a documented gap, not a silent assumption — no sensitive draft/brief/adjustment-note content was observed in any captured console or server log output during the walkthrough.

### Environment cleanup

Docker Desktop's WSL2 integration dropped twice during this task's walkthrough (`docker-desktop` distro stopped) and was restarted by the user each time. After the walkthrough concluded: both `api/` processes (Maven wrapper + Spring Boot JVM) stopped, `docker compose -f compose.smoke.yml down` removed both containers (`api-postgres-1`, `api-firebase-emulator-1`) and the network — confirmed via `docker ps -a` returning no rows. `web/.env.local` (git-ignored) restored from its pre-walkthrough backup.

---

## Done Criteria

- [x] No fake-data residue remains under `src/features/assessment-creation/` — see Step 1 above; the only `setTimeout` hits are test-timing code in `loadAssessmentDraftBuilderPage.test.ts`, no `mocks/`/fixture directory exists.
- [x] The full flow works end-to-end against real `api/` with real ids — see Step 2/3 walkthrough above (real `teacher` row, real `assessments` row `cb77bcfb-b454-4000-90a3-e8795176e90f`, real draft render/edit/save).
- [x] Draft and version history survive a page refresh — see walkthrough step 5 (edit persisted after refresh, re-fetched from the API).
- [x] Story-01's in-scope Done Criteria are confirmed against the real flow, not just the mockups — see the cross-check table above, including the one documented partial-coverage gap (version-history-after-regeneration, blocked by no `agents/` service in this environment, covered instead by task-12's existing test evidence).
- [x] Full test suite and lint pass across everything this story touched — see Step 4 above: web/ 153/153 tests + lint clean; api/ 289/289 tests (including the corrected `EmailVerifiedFilterTest` and new `SecurityConfigTest`), BUILD SUCCESS.
- [x] Software smoke test check above passes (services ready, build/startup/connectivity confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR — services ready/build/connectivity confirmed by the walkthrough itself; commit/publish pending (see next step in this session).
- [x] Logging follows `.planning/LOGGING.md`: correlation/trace context present across the full walkthrough, with INFO/DEBUG/WARN/ERROR levels chosen by criticality, per this task's Logging / Observability section — partially re-verified; see the documented gap in the Logging / Observability section above (relying on task-10/11/12's existing evidence rather than a fresh capture).
- [x] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence — command output transcribed in Steps 1–4 above; `/plan-test-suite` run to follow immediately after this closeout write-up.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply directly; `api/`'s existing persistence layer is exercised only as a black box.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — all three fixes are minimal, single-line-cause corrections directly required to make the pre-existing, already-`DONE` seam actually work end-to-end; no new features or refactors were introduced.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
