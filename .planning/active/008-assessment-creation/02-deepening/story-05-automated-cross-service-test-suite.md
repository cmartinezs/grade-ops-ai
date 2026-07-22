# 🔍 DEEPENING: Story 05 — automated-cross-service-test-suite

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Replace one-off "bring up docker and poke at it" verification with a durable, re-runnable automated test suite that exercises real service boundaries: direct HTTP calls against `agents/`, direct HTTP calls against `api/`, and real browser automation against `web/`. Seeded with the first real case (the brief→generate flow this story's predecessor, story-04, proved manually); grows as new features land in future stories.

**Why this exists:** story-04 proved api↔agents connectivity once, by hand, for this one gap. That proof isn't re-runnable and doesn't cover `web/` at all. Without a standing suite, every future feature re-litigates "does this still actually work end-to-end" from scratch instead of adding one more case to a growing regression net. Formalizing the tooling now (one Playwright project covering HTTP + browser) gives every future story in this workspace a cheap, consistent place to add its own coverage — the process convention for *requiring* that addition on every story is deliberately left for a future plugin version, per the user's direction; this story only ships the tooling.

Browser tests must exercise features from visible UI actions, not by starting the happy path at an internal URL. For assessment creation specifically, once the `web/.planning/active/001-assessment-creation` UI exists, the browser test must start at `/dashboard`, click the visible "Nueva evaluacion" action, assert navigation to `/assessments/new`, and only then continue the intake/draft flow. Direct URL navigation can remain as a deep-link/guard check, but not as the sole happy path.

Browser tests that cover forms must also exercise UI Design/Data Semantics. For assessment creation, the happy path must interact with the actual DS controls for `learningGoal`, `topic`, `level`, `duration` and `language`, and at least one negative path must prove invalid/restricted values are blocked or mapped to safe validation feedback. Do not fill every field as arbitrary text through DOM shortcuts if the UI exposes selects, radios, numeric controls, tags or catalogs.

Browser and HTTP tests must also prove API I/O and sync/async contracts. For every screen datum in the assessed flow, tests should observe the corresponding `api/` read/write path or documented API fixture boundary. If generation/regeneration is async, tests must wait through the agreed completion mechanism (operation polling, SSE, WebSocket, webhook/push or equivalent), not arbitrary sleeps or local timers.

Browser, HTTP and direct service tests must cover i18n where the flow exposes user-facing text or generated content. The assessment creation extension must verify effective locale/fallback, translated UI copy or translation keys, safe errors/catalog labels, `outputLocale` propagation to generation/regeneration, and that logs/telemetry/event codes remain in English.

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| `web/`'s docker build is currently broken (pre-existing, unrelated `@tailwindcss/oxide` native-binding bug flagged in story-04's retrospective) | M | H | Task 3's browser test targets `web`'s dev server directly if the docker build isn't fixed by the time this story executes; not blocked on that unrelated fix |
| `web/`'s assessment-creation UI (tracked in `web/.planning/active/001-assessment-creation`, story-03 here) may not exist yet when this story executes | M | M | Task 3 seeds the pattern against whatever UI flow already exists (auth), not assessment creation specifically — future stories add assessment-creation coverage once that UI ships |
| Browser e2e proves a route loads by direct URL but misses a dead dashboard button or menu action | H | H | Browser task pattern requires visible UI action reachability; assessment creation coverage starts from `/dashboard` "Nueva evaluacion" before visiting `/assessments/new` |
| Browser e2e fills forms through arbitrary text and misses DS/control/data-semantics regressions | H | M | Browser task pattern requires interacting with visible controls and covering valid/invalid enum/catalog/numeric values for forms |
| Browser/e2e passes against local fixtures or timers while `api/` lacks required screen data/status contracts | H | M | Test pattern requires API I/O evidence and waits for async completion through the agreed API-backed mechanism |
| Browser/e2e proves labels but misses GenAI output locale or localized API errors/catalogs | H | M | Test pattern requires i18n evidence across Web-API-Agents and checks logs/telemetry remain English |
| CI wiring (task 4) needs docker-in-CI, which can be slower/flakier than unit tests | L | M | Scope the CI job to only the paths that touch `agents/`, `api/`, `web/`, or `compose.yml`; keep it a separate, allowed-to-be-slower check, not blocking on every PR |

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Scaffold root-level Playwright e2e project](story-05-automated-cross-service-test-suite/task-01-scaffold-playwright-project.md) | GENERATE-DOCUMENT | TODO | Root-level `e2e/` project (Playwright Test) configured to run against the docker-compose stack — one tool for both direct HTTP calls and real browser automation |
| 2 | [Direct HTTP tests against agents/ and api/](story-05-automated-cross-service-test-suite/task-02-direct-http-tests-agents-api.md) | GENERATE-DOCUMENT | TODO | First direct-HTTP regression case (brief→generate flow) hitting `agents/` and `api/`'s real endpoints, bypassing any UI, run against the compose stack |
| 3 | [Browser-driven web UI test](story-05-automated-cross-service-test-suite/task-03-browser-driven-web-ui-test.md) | GENERATE-DOCUMENT | TODO | Playwright browser test exercising the real `web/` UI end-to-end from visible UI actions, semantic DS controls, API I/O evidence, sync/async completion and i18n; assessment creation extension must start dashboard -> "Nueva evaluacion" -> `/assessments/new` |
| 4 | [Wire e2e suite into CI](story-05-automated-cross-service-test-suite/task-04-wire-e2e-suite-into-ci.md) | GENERATE-DOCUMENT | TODO | GitHub Actions workflow running the e2e suite automatically on PRs touching `agents/`, `api/`, `web/`, or `compose.yml` |

---

## Test Suite

- [ ] Story-level test suite is generated or refreshed with `/plan-test-suite`.

---

## Done Criteria

- [ ] Root-level e2e test project runs locally against the docker-compose stack with a single command
- [ ] At least one direct HTTP test each for agents/ and api/ passes against real running services, not mocks
- [ ] At least one Playwright browser test drives the real web UI end-to-end and passes
- [ ] Browser test pattern documents UI action reachability: new features are tested from buttons/links/menus, not only direct URLs
- [ ] Browser test pattern documents UI Design/Data Semantics: form tests use visible DS controls and cover valid/invalid restricted values instead of arbitrary text-only filling
- [ ] Browser and HTTP test pattern documents API I/O + sync/async: screen data is backed by `api/`, and async completion waits through the agreed mechanism rather than sleeps/timers
- [ ] Browser, HTTP and service test pattern documents i18n: locale/fallback, translation keys or labels, safe errors/catalog labels, `outputLocale`, and logs/telemetry in English
- [ ] CI runs the suite automatically on relevant PRs
- [ ] TRACEABILITY.md updated with new terms from this story

---

## Dependencies

01, 02, 04

---

## Area

IN
