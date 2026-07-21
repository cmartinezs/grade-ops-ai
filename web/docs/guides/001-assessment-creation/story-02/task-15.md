# Deterministic E2e Suite

**Source:** task-15 | **Area:** unknown | **Date:** 2026-07-21

## What it does
The full Intake → Draft Builder browser walkthrough that task-13 verified by hand (registration → login → intake → draft render → edit/save/refresh → regenerate) is re-runnable on demand, non-interactively, via a single script — with no human driving a browser and no Claude session re-deriving the same steps each time — and the underlying authenticated-session fixture is reusable for future authenticated-screen tests, not single-purpose to this flow.

---

## How to use it
- Add `@playwright/test` as a devDependency; document the Chrome/Chromium prerequisite (this environment required the real `google-chrome-stable` channel, not bundled Chromium — see task-13's notes) as a comment in `playwright.config.ts` or the e2e script.
- Author `web/e2e/fixtures/auth.ts`: a Playwright fixture that programmatically creates and signs in a teacher via the Firebase Auth Emulator's REST API (`accounts:signUp` → admin-bypass `accounts:update` to force `emailVerified:true` → `accounts:signInWithPassword`), then registers the teacher against the real `api/` backend and seeds the browser's auth state — exposing an `authenticatedPage` fixture other specs (and future authenticated-screen tests) can import directly, without re-deriving this flow.
- Author spec files covering task-13's walkthrough legs:
- Registration UI (real form submit, not the fixture's programmatic path) — one spec exercising the actual `/register` page end to end, to keep UI regression coverage independent of the fixture shortcut.
- Login UI end to end.
- Intake submission: real brief persisted before any agent call; confirm the expected agent-down error banner when `agents/` is unreachable (matches task-06's designed error path).

## Example
Use `Add` through the public interface introduced by this task.
