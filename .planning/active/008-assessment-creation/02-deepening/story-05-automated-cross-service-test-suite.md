# 🔍 DEEPENING: Story 05 — automated-cross-service-test-suite

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Replace one-off "bring up docker and poke at it" verification with a durable, re-runnable automated test suite that exercises real service boundaries: direct HTTP calls against `agents/`, direct HTTP calls against `api/`, and real browser automation against `web/`. Seeded with the first real case (the brief→generate flow this story's predecessor, story-04, proved manually); grows as new features land in future stories.

**Why this exists:** story-04 proved api↔agents connectivity once, by hand, for this one gap. That proof isn't re-runnable and doesn't cover `web/` at all. Without a standing suite, every future feature re-litigates "does this still actually work end-to-end" from scratch instead of adding one more case to a growing regression net. Formalizing the tooling now (one Playwright project covering HTTP + browser) gives every future story in this workspace a cheap, consistent place to add its own coverage — the process convention for *requiring* that addition on every story is deliberately left for a future plugin version, per the user's direction; this story only ships the tooling.

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| `web/`'s docker build is currently broken (pre-existing, unrelated `@tailwindcss/oxide` native-binding bug flagged in story-04's retrospective) | M | H | Task 3's browser test targets `web`'s dev server directly if the docker build isn't fixed by the time this story executes; not blocked on that unrelated fix |
| `web/`'s assessment-creation UI (tracked in `web/.planning/active/001-assessment-creation`, story-03 here) may not exist yet when this story executes | M | M | Task 3 seeds the pattern against whatever UI flow already exists (auth), not assessment creation specifically — future stories add assessment-creation coverage once that UI ships |
| CI wiring (task 4) needs docker-in-CI, which can be slower/flakier than unit tests | L | M | Scope the CI job to only the paths that touch `agents/`, `api/`, `web/`, or `compose.yml`; keep it a separate, allowed-to-be-slower check, not blocking on every PR |

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Scaffold root-level Playwright e2e project](story-05-automated-cross-service-test-suite/task-01-scaffold-playwright-project.md) | GENERATE-DOCUMENT | TODO | Root-level `e2e/` project (Playwright Test) configured to run against the docker-compose stack — one tool for both direct HTTP calls and real browser automation |
| 2 | [Direct HTTP tests against agents/ and api/](story-05-automated-cross-service-test-suite/task-02-direct-http-tests-agents-api.md) | GENERATE-DOCUMENT | TODO | First direct-HTTP regression case (brief→generate flow) hitting `agents/` and `api/`'s real endpoints, bypassing any UI, run against the compose stack |
| 3 | [Browser-driven web UI test](story-05-automated-cross-service-test-suite/task-03-browser-driven-web-ui-test.md) | GENERATE-DOCUMENT | TODO | Playwright browser test exercising the real `web/` UI end-to-end (existing auth flow) as the seed pattern future stories extend for new features |
| 4 | [Wire e2e suite into CI](story-05-automated-cross-service-test-suite/task-04-wire-e2e-suite-into-ci.md) | GENERATE-DOCUMENT | TODO | GitHub Actions workflow running the e2e suite automatically on PRs touching `agents/`, `api/`, `web/`, or `compose.yml` |

---

## Test Suite

- [ ] Story-level test suite is generated or refreshed with `/plan-test-suite`.

---

## Done Criteria

- [ ] Root-level e2e test project runs locally against the docker-compose stack with a single command
- [ ] At least one direct HTTP test each for agents/ and api/ passes against real running services, not mocks
- [ ] At least one Playwright browser test drives the real web UI end-to-end and passes
- [ ] CI runs the suite automatically on relevant PRs
- [ ] TRACEABILITY.md updated with new terms from this story

---

## Dependencies

01, 02, 04

---

## Area

IN
