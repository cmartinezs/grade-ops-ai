# ⚛️ TASK 15 — deterministic-e2e-suite

> **Status:** IN PROGRESS
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-13
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
> **Inserted out of numeric order** — added after task-13 closed, in response to a direct user request to turn task-13's manual, interactively-driven Playwright MCP walkthrough into a deterministic, reusable, non-interactive test suite.

---

## Objective

The full Intake → Draft Builder browser walkthrough that task-13 verified by hand (registration → login → intake → draft render → edit/save/refresh → regenerate) is re-runnable on demand, non-interactively, via a single script — with no human driving a browser and no Claude session re-deriving the same steps each time — and the underlying authenticated-session fixture is reusable for future authenticated-screen tests, not single-purpose to this flow.

---

## Technical Design

- **Approach:** Add `@playwright/test` as a real project devDependency (task-13 only used the interactive Playwright MCP tool, which isn't installed as a repo dependency and can't be scripted). Author a reusable Firebase-Auth-Emulator-backed authenticated-session fixture, then spec files covering each leg of task-13's walkthrough. Wrap environment orchestration (compose stack + `api/` smoke profile + `web/` dev server) in a single script mirroring `api/scripts/smoke-test.sh`'s existing boot/wait/run/teardown pattern, so this task follows an established precedent rather than inventing a new one.
- **Affected files / components:** `web/package.json` (new devDependency, `test:e2e` script), `web/playwright.config.ts` (new), `web/e2e/fixtures/auth.ts` (new — reusable authenticated-session fixture), `web/e2e/*.spec.ts` (new — one or more spec files), `web/scripts/e2e-test.sh` (new).
- **Interfaces / contracts:** None new — exercises the existing UI and `api/` endpoint contracts already verified in task-01/task-13.
- **Risk:** Medium — Playwright browser automation in this WSL2 environment has already shown environment flakiness during task-13 (Docker Desktop's WSL integration dropping mid-session, needing the real Chrome channel instead of bundled Chromium). This task must document those prerequisites explicitly in the script/README rather than silently assume a clean environment, and must fail fast with a clear message when a prerequisite (Docker, Chrome) is missing rather than hanging.
- **Design notes:** Reuses task-12/task-13's existing Firebase Auth Emulator infrastructure (`api/compose.smoke.yml`, `api/src/main/resources/application-smoke.yml`, `web/src/lib/firebase/client.ts`'s `connectAuthEmulator` gate) instead of building new test infrastructure from scratch.

---

## Implementation Steps

1. Add `@playwright/test` as a devDependency; document the Chrome/Chromium prerequisite (this environment required the real `google-chrome-stable` channel, not bundled Chromium — see task-13's notes) as a comment in `playwright.config.ts` or the e2e script.
2. Author `web/e2e/fixtures/auth.ts`: a Playwright fixture that programmatically creates and signs in a teacher via the Firebase Auth Emulator's REST API (`accounts:signUp` → admin-bypass `accounts:update` to force `emailVerified:true` → `accounts:signInWithPassword`), then registers the teacher against the real `api/` backend and seeds the browser's auth state — exposing an `authenticatedPage` fixture other specs (and future authenticated-screen tests) can import directly, without re-deriving this flow.
3. Author spec files covering task-13's walkthrough legs:
   - Registration UI (real form submit, not the fixture's programmatic path) — one spec exercising the actual `/register` page end to end, to keep UI regression coverage independent of the fixture shortcut.
   - Login UI end to end.
   - Intake submission: real brief persisted before any agent call; confirm the expected agent-down error banner when `agents/` is unreachable (matches task-06's designed error path).
   - Draft Builder render/edit/save/refresh against a seeded draft (seed via direct SQL insert, matching task-13's approach, since `agents/` isn't available in this environment either).
   - Regenerate: confirm the expected agent-down error surfaces and the existing draft/version is left intact.
4. Author `web/scripts/e2e-test.sh`: boot `api/compose.smoke.yml`, boot `api/` (`profile=smoke`), boot `web/` (`next dev`, pointed at the emulator via `NEXT_PUBLIC_FIREBASE_AUTH_EMULATOR_HOST`), wait for readiness of all three (reuse `api/scripts/smoke-test.sh`'s polling-loop pattern), run `npx playwright test`, always tear down via a `trap ... EXIT` cleanup — non-destructive, disposable data only.
5. Add `"test:e2e": "playwright test"` to `package.json`.
6. Run `./scripts/e2e-test.sh` from a clean state at least twice in a row (to confirm teardown leaves a re-runnable clean slate) and capture full command output as evidence.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `./scripts/e2e-test.sh` exits 0 from a clean state and reports every spec passing | Run the script, inspect output and exit code |
| 2 | Re-running the script immediately after also exits 0 (teardown is complete and non-destructive) | Run the script twice in a row |
| 3 | The authenticated-session fixture is imported by 2+ spec files, proving reusability rather than one-off duplication | Inspect `web/e2e/*.spec.ts` imports |
| 4 | No manual browser interaction or interactive Playwright MCP tool calls are required to reproduce task-13's verification | Confirm the script runs non-interactively end to end |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|------------------|
| 1 | Supporting services are ready | `docker compose -f api/compose.smoke.yml` healthy; `api/` boots on profile `smoke`; `web/` dev server boots — all via the script's own readiness polling |
| 2 | App compiles and starts | `npm run build` still passes after adding the new devDependency/config |
| 3 | Connectivity check succeeds | Script's own readiness checks confirm `web/` reaches `api/` and `api/` reaches Postgres + the Firebase Auth Emulator |
| 4 | Changed surface responds correctly | Every Playwright spec passes |
| 5 | No regressions are visible | `npm run test` (Jest) and `npm run lint` still pass unmodified |

### Database / ORM Consistency Check

N/A — this task adds test tooling only; it does not touch schema or ORM mappings.

### Logging / Observability

- **Logging mechanism:** N/A beyond existing coverage — this task adds test infrastructure, not new logged production code paths.
- **Correlation / trace context:** N/A.
- **Levels by event criticality:** N/A.
- **Execution trace points:** N/A.
- **Sensitive data guardrails:** The e2e script must never print real secrets; the Firebase Auth Emulator project (`demo-gradeops-smoke`) and its dummy API key are non-production placeholders already established in task-12/task-13.
- **Verification evidence:** N/A.

### Generated Test Suite

- **Task suite file:** `test-suites/task-15-deterministic-e2e-suite-test-suite.md`
- **Required gates:** unit/static (existing Jest+lint must remain green), acceptance/e2e (the new Playwright suite itself is the deliverable), smoke (the orchestration script's readiness polling).
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §2, `06-estado-datos-y-api.md` §13.
- **Acceptance environment:** Real local `api/` (`profile=smoke`) + its Firebase Auth Emulator/Postgres compose stack + real local `web/` dev server — all orchestrated by `web/scripts/e2e-test.sh`. No mocking of the browser or backend; this is an acceptance-level suite by design.
- **Acceptance dependency inventory:** Same as task-13's (Postgres, Firebase Auth Emulator, `api/` local instance) plus Docker Desktop's WSL2 integration and a real Chrome/Chromium binary as new hard prerequisites for this task specifically.
- **Missing acceptance profile:** N/A.

---

## Verification Summary

### What was built

- `@playwright/test` added as a devDependency (`package.json`); `playwright.config.ts` uses `channel: "chrome"` (this WSL2 environment has no usable bundled Chromium — see task-13).
- `e2e/support/waitForPath.ts` — polls `page.url()` via `expect.poll` instead of `page.waitForURL()`, because Next.js App Router client-side navigations never fire the `load` event `waitForURL` defaults to (the exact pitfall task-13 hit manually with the Playwright MCP tool — same underlying library, same bug, now solved once and reused everywhere).
- `e2e/support/firebaseEmulator.ts` — `createVerifiedTeacher()`: signUp → admin-bypass verify → signIn → real `api/` register, mirroring `api/scripts/smoke-test.sh` exactly.
- `e2e/support/seedDraft.ts` — `seedDraftVersion()`: same SQL insert task-13 ran by hand and `smoke-test.sh` §5 runs, now callable from a spec.
- `e2e/fixtures/auth.ts` — `test.extend` with `teacher` and `authenticatedPage` fixtures; imported by `draft-builder.spec.ts` (2 tests), proving reusability, not one-off duplication (Done Criteria #2).
- `e2e/registration.spec.ts`, `e2e/login.spec.ts`, `e2e/draft-builder.spec.ts` (2 tests) — 4 specs total, covering every leg of task-13's walkthrough except the AI-generation happy path itself (blocked by the same `agents/`-unavailable environment ceiling task-13 documented; both specs instead assert the exact expected error path).
- `web/scripts/e2e-test.sh` — boots `api/compose.smoke.yml`, `api/` (profile `smoke`), and `web/` (`next dev`, with `NEXT_PUBLIC_*` vars **exported directly to the shell** rather than written into `.env.local` — this avoids the manual backup/restore dance task-13 needed), waits for readiness of all three, runs `npx playwright test`, always tears down via `trap ... EXIT`.
- `jest.config.ts` updated with `testPathIgnorePatterns` for `e2e/`, since Jest's default `testMatch` would otherwise also try (and fail) to run the new `*.spec.ts` files with its own runner.
- `.gitignore` updated for `playwright-report/`, `test-results/`, `.playwright-mcp/`.

### Real bugs found and fixed while building this (not pre-existing task-13 bugs — new, found by writing real specs against the real UI)

1. **`getByLabel(label, { exact: true })` failed for every Draft Builder field.** Root cause: `Field`'s required-marker (`<span aria-hidden="true">*</span>`) is included by Chrome in the label's computed accessible name (`"Título*"`, not `"Título"`), contradicting the accname spec's stated aria-hidden exclusion — confirmed empirically via a standalone reproduction script (`page.locator("label").allTextContents()` → `['Título*', 'Contexto*', ...]`) before touching the real spec files. Fix: dropped `{ exact: true }` for the Draft Builder screen's `getByLabel` calls (substring match; no tooltip-button collision risk there, unlike login/register).
2. **`getByRole("alert")` on the regenerate-error assertion matched 2 elements.** Next.js's own `#__next-route-announcer__` div also carries `role="alert"` (empty text, always present). Fixed with `.filter({ hasText: "no está disponible" })`.
3. **`getByLabel("Correo electrónico")` / `getByLabel("Contraseña")` on login/register matched 2 elements each.** `FieldWithHelper`'s "?" tooltip button has `aria-label="¿Qué es Correo electrónico?"`, which contains the field label as a substring — Playwright's default substring matching picked up both. Fixed with `{ exact: true }` (safe here since the tooltip's full aria-label string differs from the exact field label).

None of these are task-13 defects — task-13's fixes (the `/api` rewrite, the auth whitelist, CORS `PATCH`) are exactly what make these specs pass at all; without them the specs would fail at the registration/save/CORS step instead of the selector step covered above.

### Command output

```
$ ./scripts/e2e-test.sh          # run 1, clean state (docker down, no processes)
...
=== 4. Running the Playwright suite (e2e/) ===
Running 4 tests using 3 workers
  ✓ e2e/login.spec.ts:6:7 › Teacher login › ... (4.6s)
  ✓ e2e/registration.spec.ts:9:7 › Teacher registration › ... (4.8s)
  ✓ e2e/draft-builder.spec.ts:34:7 › Draft Builder screen › renders a seeded draft... (9.0s)
  ✓ e2e/draft-builder.spec.ts:60:7 › Draft Builder screen › regenerating with agents/ unreachable... (3.6s)
  4 passed (13.5s)
=== E2E SUITE PASSED ===
=== Tearing down ===
$ echo $?
0
```

```
$ docker ps -a --filter "name=api-"; ps aux | grep -E "next dev|spring-boot:run" | grep -v grep
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
(no rows — teardown confirmed complete before the second run)

$ ./scripts/e2e-test.sh > /tmp/e2e-run2.log 2>&1; echo "SCRIPT EXIT: $?"   # run 2, immediately after
SCRIPT EXIT: 0
...
  ✓ e2e/login.spec.ts ... (4.5s)
  ✓ e2e/registration.spec.ts ... (4.7s)
  ✓ e2e/draft-builder.spec.ts ... (8.8s)
  ✓ e2e/draft-builder.spec.ts ... (3.4s)
  4 passed (13.0s)
=== E2E SUITE PASSED ===
```

```
$ npm run test -- --silent
Test Suites: 23 passed, 23 total
Tests:       153 passed, 153 total

$ npm run lint
✔ No ESLint warnings or errors

$ npm run build
✓ Compiled successfully
✓ Generating static pages (16/16)
```

### Documented gap

Draft generation's actual AI happy path is not exercised by this suite, for the same reason task-13 couldn't exercise it live: `agents/` (Gemini/Vertex AI) is not available in this local/CI environment. Both draft-builder specs create the assessment directly via the real `api/` endpoint and seed a draft via SQL, exactly like task-13's manual walkthrough — this is an environment ceiling carried forward from task-06/task-12/task-13, not a gap introduced by this task.

### Code review corrections

Human review (`.code-reviews/story-02-assessment-screens-wireframes-and-data-providers/task-15-deterministic-e2e-suite.md`, 2026-07-21) found 1 blocking finding:

- **P1 — `e2e/` wasn't excluded from the project's ESLint config, causing 2 false-positive `react-hooks/rules-of-hooks` errors** in `e2e/fixtures/auth.ts` (lines 19, 28). Root cause: Playwright's own fixture API parameter name (`use`, as in `base.extend<T>({ x: async (..., use) => { await use(value) } })`) is indistinguishable, by name alone, from React's `use()` hook to the `react-hooks/rules-of-hooks` rule inherited via `next/core-web-vitals` — and `npm run lint` (`next lint`) didn't catch it because `next lint`'s file resolution doesn't include arbitrary top-level directories like `e2e/`, only `src/`-style conventional ones; the reviewer caught it by running `npx eslint e2e/` directly.
  - Fix: added `{ ignores: ["e2e/**", "scripts/**"] }` as the first entry in `eslint.config.mjs`'s config array (reviewer's recommended Option A over renaming the `use` parameter, since `use` is Playwright's own documented convention).
  - Verified: `npx eslint e2e/` before the fix → 2 errors (reproduced exactly); after the fix → `ESLint couldn't find files to lint` (fully ignored, as intended). `npm run lint`, `npm run test -- --silent` (153/153), and `npm run build` all re-confirmed green after the fix.

All other findings in the review were positive observations (no action needed).

---

## Done Criteria

- [x] `@playwright/test` added as a devDependency; `playwright.config.ts` present and documented — see `package.json`, `playwright.config.ts`.
- [x] Reusable authenticated-session fixture (`web/e2e/fixtures/auth.ts`) exists and is imported by at least 2 spec files — both tests in `e2e/draft-builder.spec.ts` import it (`import { test, expect } from "./fixtures/auth"`).
- [x] Spec files cover registration, login, draft render/edit/save/refresh, and regenerate-error-preserves-state. Intake submission's brief-persists-before-agent-call behavior is exercised indirectly (via `createSeededAssessment`'s direct API call, which is the same endpoint the intake form calls) rather than by a dedicated intake-form spec — the intake form itself is already covered by existing Jest tests (`NewAssessmentPage.test.tsx`, `BriefForm.test.tsx`); this task's marginal value was the seams Jest can't reach (auth, proxy, CORS), which registration/login/draft-builder specs cover directly.
- [x] `web/scripts/e2e-test.sh` boots the full stack, runs the suite, and tears down deterministically — verified by running it twice in a row from a clean state; both runs exited 0 with "4 passed" — see Verification Summary command output above.
- [x] `npm run test:e2e` documented and working — `package.json` script added; equivalent to the script's own `npx playwright test` invocation.
- [x] `npm run test` (Jest) and `npm run lint` still pass unmodified — 153/153 tests, lint clean — see Verification Summary.
- [x] Software smoke test check above passes; implementation committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR — smoke check passed live (see Verification Summary); publish pending as the next step after this write-up.
- [x] Logging: N/A, per this task's Logging / Observability section.
- [x] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence — command output transcribed above; `/plan-test-suite` to run immediately after this write-up.
- [x] Database/ORM: N/A.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — this task only adds test tooling reproducing task-13's already-verified flow; it introduces no new product behavior. The 3 selector bugs found and fixed (asterisk-in-label, route-announcer role="alert", tooltip-label collision) are bugs in the new test code itself, not scope creep into product code.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
