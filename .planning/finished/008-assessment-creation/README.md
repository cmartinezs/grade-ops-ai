# Planning: 008-assessment-creation

> [← planning/README.md](../../README.md)

Enable the first step of the open-assessment pipeline: turning a teacher's learning intent into a structured, AI-generated assessment draft that is editable and fully logged.

---

## Overview

- **Planning ID:** 008-assessment-creation
- **Current status:** Completed
- **Intent:** Enable the first step of the open-assessment pipeline: turning a teacher's learning intent into a structured, AI-generated assessment draft that is editable and fully logged.
- **Owner:** human
- **Started:** 2026-07-09
- **Completed:** 2026-07-22

---

## Key Links

- [Initial context](00-initial.md)
- [Expansion plan](01-expansion.md)
- [R01 release-to-planning bridge](R01-RELEASE-BRIDGE.md)
- [Story details](02-deepening/)
- [Traceability](TRACEABILITY.md)
- [Retrospective raw notes](RETROSPECTIVE-RAW.md)
- [Baseline certification (2026-07-22)](BASELINE-CERTIFICATION-2026-07-22.md)
- [Security & configuration baseline (2026-07-22)](SECURITY-BASELINE-2026-07-22.md)

---

## Current State

- [x] Initial intent is complete.
- [x] Expansion is dimensioned (5 stories: 01 agents-assessment-agent-coordination, 02 api-assessment-creation-coordination, 03 web-assessment-creation-coordination, 04 e2e-integration-verification, 05 automated-cross-service-test-suite).
- [x] Stories are DONE or intentionally SKIPPED (01, 02, 03, 04 DONE; 05 SKIPPED).
- [x] Traceability is complete.
- [x] Retrospective is complete.

---

## Retrospective

### Outcomes

- The full open-assessment "assessment creation" slice (US-010 Brief Intake, US-011 Draft Generation, US-012 Draft Regeneration) shipped end-to-end across `agents/`, `api/` and `web/`, each via its own child planning: `agents/.planning/active/001-assessment-creation` (DONE), `api/.planning/finished/003-assessment-creation` (DONE, archived), `web/.planning/active/001-assessment-creation` (both stories resolved — 01 SKIPPED/absorbed, 02 DONE).
- Real, non-mocked cross-service reachability was proven directly by Story 04: `agents` added to root `compose.yml`, a real docker-compose brief→generate→retrieve flow verified against Postgres, and a real Render `beta` post-deploy smoke check (`scripts/smoke-e2e-local.sh`, `scripts/smoke-e2e-render-beta.sh`, `scripts/lib/e2e-smoke-flow.sh`).
- The monorepo parent/child coordination pattern (root planning + one child workspace per service, plus a root-owned story for cross-cutting concerns no single child owns) was proven out across three services.
- Story 05 (durable automated cross-service test suite) never advanced past dimensioning — closed `SKIPPED`, deferred as Master Plan input.

### Deviations

- Stories 01 and 02 (`agents`/`api`) were originally scoped as full implementation stories directly in this root planning — corrected 2026-07-09 into child plannings + coordination stories once the monorepo parent/child rule was caught. Story 03 (`web`) received the same correction later, 2026-07-14, once `web/` got its own `.planning/` workspace.
- Story 04 (`e2e-integration-verification`) was added post-initial-expansion, 2026-07-14, after discovering both children's test suites mock the `api`↔`agents` HTTP call — a genuine coverage gap neither child owned.
- Story 05 was added 2026-07-15 via `/plan-enrich-epic` but never atomized; closed `SKIPPED` 2026-07-22 during this pre-master-plan cleanup pass rather than executed.
- `agents/`'s `AssessmentCommand` contract changed mid-flight (2026-07-10, gained a `previousDraft` field) after `api/`'s dependent tasks were already atomized against the original shape — caught and propagated across both child plannings and this parent before implementation.
- `web/`'s docker build had a pre-existing, unrelated `@tailwindcss/oxide` native-binding bug on `node:18-alpine`, discovered during Story 04 task-01 (2026-07-15) and flagged but not fixed there (out of scope for that task). **Already fixed 2026-07-21** (commit `2c73156`, PR #91, base image bumped to `node:24-alpine`) — before this pre-master-plan cleanup pass even started on 2026-07-22. This session initially missed that and had to correct a stale claim in the master-plan entry decision — see that document's own note and the Lessons entry below.
- This cleanup pass (2026-07-22) additionally closed Stories 01, 02 and 03 to `DONE` together: reconciling `web/`'s real child-planning state showed all three coordination checkpoints were already satisfied by existing evidence (Story 04's reachability proof + web/story-02's real API wiring) — see `01-expansion.md`'s "Coordination closure update".

### Follow-ups

**All items below are input to the Master Plan, not open debt of this planning:**

- Post-closeout R01 revalidation of i18n, UI data semantics, API I/O, sync/async completion and UI-action reachability for the `web/` slice, under the Master Plan gates added after `web/story-02` closed (tracked as Story 03's Residual #1, carried forward from `web/`'s `R-POST-01`).
- Story 05's original scope — a durable, re-runnable cross-service Playwright suite (HTTP + browser, wired into CI) — deferred to the Master Plan; conceptually already covered by `docs/master-plan/analysis/testing-strategy.md`.
- The global `.planning/SMOKE-TESTS.md` template remains unfilled (`[fill in]` placeholders throughout). **Explicit decision:** baseline certification for this planning's own scope was done manually and is evidenced directly in Story 04's task files (real docker-compose run, real Render `beta` smoke check) — formalizing a reusable, stack-wide smoke plan is deferred as Master Plan input, not this planning's open debt.
- ~~`web/`'s broken docker build (`@tailwindcss/oxide` on `node:18-alpine`) remains unfixed.~~ **Already fixed 2026-07-21**, before this cleanup pass — see Deviations above.
- `agents/.planning/active/001-assessment-creation`'s Residual #1 wording mismatch (Gemini vs. Groq success-path claims) remains unfixed in that child's own file — flagged in this planning's Story 01 Inconsistencies Found #3, belongs to whoever next touches that child planning.
- ~~Baseline certification (2026-07-22) found `.github/workflows/agents.yml`'s test/build-image steps and `CLAUDE.md`'s documented `agents/` commands failed to compile without `-Pdemo`/`-Pbeta`.~~ **Fixed same day** — see [`BASELINE-CERTIFICATION-2026-07-22.md`](BASELINE-CERTIFICATION-2026-07-22.md) for the root cause and the fix (`-Pdemo` added to both CI steps and to `CLAUDE.md`, re-verified passing).

### Lessons

- Always check `<child>/.planning/` for every affected directory before `/plan-expand` on a monorepo-root planning — don't assume a sub-repo lacks its own workspace just because it wasn't mentioned in the initial idea doc.
- A sync checkpoint marked DONE is a snapshot, not a guarantee — if a child planning's contract or status changes afterward, push the change back through the coordination story, not just inside the child that happened to catch it.
- Three files independently duplicate each story's status (`story-NN-*.md`, `01-expansion.md`'s Story Summary table, `active/README.md`) — a status change in one doesn't propagate automatically; check all three together.
- When a coordination story's Done Criteria says "reachable in the target environment," verify against actual test code, not just each child's own DONE status — a cross-cutting integration point can remain unverified even when every child is individually complete.
- WSL2 + Docker Desktop's integration can silently disconnect between sessions/worktrees — run a fast `docker ps` sanity check at the start of any task known to need Docker/compose, before writing any implementation.
- A retrospective note is a snapshot from when it was written, not a live status — before citing an old "flagged but not fixed" note (or any claim of an unresolved defect) as current fact, check `git log` for that exact path first. This cleanup pass initially proposed a CONDITIONAL GO in the master-plan entry decision based on Story 04's 2026-07-15 note about `web/`'s broken docker build, without checking whether it had since been fixed — it had, on 2026-07-21, before this pass even started.

---

> [← planning/README.md](../../README.md)
