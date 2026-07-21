# ADR: The full Intake → Draft Builder browser walkthrough that task-13 verified by hand (registration → login → intake → draft render → edit/save/refresh → regenerate) is re-runnable on demand, non-interactively, via a single script — with no human driving a browser and no Claude session re-deriving the same steps each time — and the underlying authenticated-session fixture is reusable for future authenticated-screen tests, not single-purpose to this flow.

**Date:** 2026-07-21
**Status:** Accepted
**Planning:** 001-assessment-creation / story-02 / task-15

## Context
- **Approach:** Add `@playwright/test` as a real project devDependency (task-13 only used the interactive Playwright MCP tool, which isn't installed as a repo dependency and can't be scripted). Author a reusable Firebase-Auth-Emulator-backed authenticated-session fixture, then spec files covering each leg of task-13's walkthrough. Wrap environment orchestration (compose stack + `api/` smoke profile + `web/` dev server) in a single script mirroring `api/scripts/smoke-test.sh`'s existing boot/wait/run/teardown pattern, so this task follows an established precedent rather than inventing a new one.
- **Affected files / components:** `web/package.json` (new devDependency, `test:e2e` script), `web/playwright.config.ts` (new), `web/e2e/fixtures/auth.ts` (new — reusable authenticated-session fixture), `web/e2e/*.spec.ts` (new — one or more spec files), `web/scripts/e2e-test.sh` (new).
- **Interfaces / contracts:** None new — exercises the existing UI and `api/` endpoint contracts already verified in task-01/task-13.
- **Risk:** Medium — Playwright browser automation in this WSL2 environment has already shown environment flakiness during task-13 (Docker Desktop's WSL integration dropping mid-session, needing the real Chrome channel instead of bundled Chromium). This task must document those prerequisites explicitly in the script/README rather than silently assume a clean environment, and must fail fast with a clear message when a prerequisite (Docker, Chrome) is missing rather than hanging.
- **Design notes:** Reuses task-12/task-13's existing Firebase Auth Emulator infrastructure (`api/compose.smoke.yml`, `api/src/main/resources/application-smoke.yml`, `web/src/lib/firebase/client.ts`'s `connectAuthEmulator` gate) instead of building new test infrastructure from scratch.

---

## Decision
- **Risk:** Medium — Playwright browser automation in this WSL2 environment has already shown environment flakiness during task-13 (Docker Desktop's WSL integration dropping mid-session, needing the real Chrome channel instead of bundled Chromium).

- **Design notes:** Reuses task-12/task-13's existing Firebase Auth Emulator infrastructure (`api/compose.smoke.yml`, `api/src/main/resources/application-smoke.yml`, `web/src/lib/firebase/client.ts`'s `connectAuthEmulator` gate) instead of building new test infrastructure from scratch.

## Consequences
** Reuses task-12/task-13's existing Firebase Auth Emulator infrastructure (`api/compose.smoke.yml`, `api/src/main/resources/application-smoke.yml`, `web/src/lib/firebase/client.ts`'s `connectAuthEmulator` gate) instead of building new test infrastructure from scratch.

## Alternatives Considered
- **Risk:** Medium — Playwright browser automation in this WSL2 environment has already shown environment flakiness during task-13 (Docker Desktop's WSL integration dropping mid-session, needing the real Chrome channel instead of bundled Chromium).

- **Design notes:** Reuses task-12/task-13's existing Firebase Auth Emulator infrastructure (`api/compose.smoke.yml`, `api/src/main/resources/application-smoke.yml`, `web/src/lib/firebase/client.ts`'s `connectAuthEmulator` gate) instead of building new test infrastructure from scratch.
