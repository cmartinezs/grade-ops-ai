# 📋 Planning 003 — assessment-creation

> [← planning/README.md](../README.md)

**Status:** DONE — story-01 completada (11/11 tareas), PR #44 mergeado a `develop` 2026-07-14.
**Período:** iniciado 2026-07-09.
**Área:** `api/` — persistencia de brief/draft e integración con `agents/` para la creación de assessments.
**Related planning:** child planning de la planning raíz del monorepo `008-assessment-creation` (`.planning/active/008-assessment-creation/` en la raíz del repo). Ver también el planning hermano `agents/.planning/001-assessment-creation`.

---

## Intent

Persistir el brief del profesor antes de invocar al Assessment Agent, integrar con `agents/` vía `agentclient` para generar un draft estructurado, y soportar regeneración del draft como una nueva versión no destructiva — la mitad de `api/` del pipeline de creación de assessments (US-010, US-011, US-012).

---

## Stories

| # | Story | Status |
|---|-------|--------|
| 01 | [assessment-creation-persistence](02-deepening/story-01-assessment-creation-persistence.md) | DONE |

---

## Retrospective

Generated from `RETROSPECTIVE-RAW.md` and planning context on 2026-07-14.

### Executive Summary

This planning delivered the `api/` half of the assessment-creation pipeline (US-010/011/012) in a single story, `story-01-assessment-creation-persistence`, atomized into 11 tasks — all `DONE`. It covers the `Assessment`/`AssessmentBrief`/`AssessmentDraft`/`AgentExecutionLog` aggregates, the `agentclient` module (the sole caller of `agents/`), six REST endpoints spanning the full brief-to-draft teacher lifecycle, and a final cross-task integration suite (task-11) tying it all together. Execution ran concurrently with two sibling child plannings (`agents/`'s `001-assessment-creation` and `002-groq-genai-provider`), whose contract changes were tracked and synced mid-story rather than causing rework. All 10 story-level Done Criteria are verified by automated tests; the full suite (288 tests) passes with zero failures. PR #44 merged into `develop` on 2026-07-14.

### Outcomes

- `Assessment`/`AssessmentBrief`/`AssessmentDraft`/`AgentExecutionLog` aggregates and their persistence adapters, backed by Flyway migrations `V9`–`V12`.
- `agentclient` module as the only caller of `agents/` — enforced by `HexagonalArchitectureTest` (ArchUnit), no Spring AI leakage elsewhere in `api/`.
- Six REST endpoints under `/api/v1/assessments`: brief intake, draft generation, regeneration, in-place edit, current-draft retrieval, version-history retrieval, plus the real (non-stub) dashboard listing.
- `AssessmentCreationFlowIntegrationTest` (task-11) — the only test in the story that exercises the full brief → generate → regenerate → edit → list → retrieve flow together, catching cross-step regressions no single task's own tests could.
- 288 automated tests passing at story close, zero failures; every one of the 11 task PRs individually human-reviewed and approved (`.code-reviews/story-01-assessment-creation-persistence/`).
- `TRACEABILITY.md` and per-task inline docs (`docs/guides/003-assessment-creation/...`, plus 6 ADRs) kept current task-by-task rather than backfilled at the end.

### Deviations And Edge Cases

- Scope grew from an originally-estimated 8 candidate tasks to 11 atomic tasks: an existing `Assessment` stub needed replacing rather than duplicating (added task-01), and dashboard wiring needed its own dedicated task (task-10), neither anticipated in the original breakdown.
- `agents/`'s `AssessmentCommand` contract changed twice mid-story from concurrent sibling-planning work — a `previousDraft` field (2026-07-10) and `provider`/`model` fields (2026-07-12) — both tracked as "Inconsistencies Found" on the story and synced into task-05/06/07/08 before those tasks were implemented, avoiding rework after the fact.
- The originally-planned `AgentExecutionLog` schema was under-scoped (5 of the 13 fields `agents/` actually returns, with `status`/`errorCode` conflated) — caught by verifying against `agents/`'s actual source code rather than its docs, before task-07 was implemented.
- Four tasks (task-01, task-02, task-03, task-07) received human-review-requested corrections after being marked `DONE` — a null-validation/contract-ambiguity bug, a missing defensive-copy on a mutable list aggregate, a session-cache false-positive in a round-trip test, and a missing real-repository integration test for a two-table cross-reference. None were style nitpicks; each caught a latent bug or a real test-coverage gap, and all were resolved same-session via follow-up commits to the existing task PR.
- Task-11 hit a live blocker mid-session: Docker was unreachable from the WSL2 dev shell (broken Docker Desktop WSL integration) even though Docker Desktop was running on the host. This was flagged explicitly to the user rather than worked around; the user fixed the host-level integration and verification completed normally afterward.
- Task-11 also surfaced two workspace-wide tooling gaps: the test-suite generator script (`.planning/scripts/generate-test-suite.sh`) and the `RECORD-EDGE-CASE` workflow file are both absent from this workspace's `.planning/` scaffold (pre-dates that plugin version) — substituted with sibling integration tests as the de facto test plan and this raw-notes log in place of `RECORD-EDGE-CASE`, consistent with how tasks 01–10 had already (implicitly) handled the same gap.

### Decisions And Tradeoffs

- **D-01/D-02** — reused the existing `assessment` bounded-context stub instead of inventing a parallel concept; `Assessment` carries no redundant `title` column, deriving it from the current draft (or brief topic as fallback) instead.
- **D-03** — `agentclient` authenticates with a shared-secret header, not a fetched Cloud Run OIDC identity token, accepted as adequate defense-in-depth for the `demo` environment where Cloud Run's IAM invoker binding is the primary protection. Tracked as residual R-01 for a post-MVP revisit.
- **D-04** — `AssessmentDraft`'s `objectives`/`deliverables`/`constraints` are JSONB columns, not child tables, since these lists have no independent lifecycle outside their draft version.
- **D-05** — draft editing (task-09) updates the current version's row in place; only regeneration (task-08) is append-only, since a manual teacher edit isn't an AI execution and doesn't need version history in this epic's scope.
- The hexagonal three-table split (`Assessment`/`AssessmentBrief`/`AssessmentDraft`) versus the single monolithic `Assessment` table documented in `docs/04-architecture/data-model.md` was a deliberate, evidenced divergence (D-01/D-02/D-04 together) — but was never formalized as a PDR. See Follow-ups.

### Follow-ups

- **R-01** — revisit `agentclient`'s shared-secret auth vs. a real fetched OIDC token post-MVP, if a stronger service-to-service auth guarantee is needed beyond Cloud Run's network-level IAM enforcement.
- **R-02/R-03** — `docs/04-architecture/api-design.md` and `data-model.md` (root `grade-ops-ai-docs` repo) are stale relative to the implemented 5-field brief payload and the hexagonal `Assessment`/`AssessmentBrief`/`AssessmentDraft` split. Out of scope for this `api/`-only planning; flagged for a documentation-sync pass in the docs repo.
- **Recommended PDR** — the hexagonal-split-vs-monolithic-table divergence (D-01/D-02/D-04, and R-03's target-resolution note) was decided and evidenced during execution but never formalized. Worth running `/plan-decision 003-assessment-creation -- assessment schema: hexagonal split vs. monolithic table` if/when the documentation-sync pass (R-02/R-03) happens, so the decision has a durable record independent of this planning's own files.
- **Story Residual #2** — `agents/`'s `MALFORMED_OUTPUT` reason code conflates genuine malformed output with live provider failures (rate limits, quota, network errors). Blocked on `agents/` adding a dedicated reason code — not fixable from `api/`'s side; tracked in `agents/`'s own follow-ups.
- **Story Residual #3** — no test in this story proves `DraftGenerationCoordinator` actually runs the agent call outside a DB transaction; all integration tests use `@DataJpaTest`'s ambient test transaction. Would need a dedicated `@SpringBootTest`-based test (e.g. asserting no connection is checked out from the pool during the agent call) if this invariant ever becomes load-bearing enough to require proof.

### Lessons For Future Plannings

- When a later task's design depends on an earlier task leaving a method behaving a specific (not-yet-implemented) way, state that contract explicitly and unambiguously in the earlier task file — task-01/task-10's `findAllByTeacherId` ambiguity ("query the real table and map to an empty-list-equivalent") is a concrete example of what goes wrong when it's left inferable instead.
- `@DataJpaTest`'s first-level cache silently makes "round-trip" persistence tests non-round-tripping unless the persistence context is explicitly flushed **and** cleared before reading back — flush alone isn't enough, and a stale in-memory read looks identical to a correct DB read for every field except ones with representation drift (like sub-millisecond timestamp precision).
- A handler that persists two-or-more related rows across separate ports needs its own real-repository integration test; a mocked-handler test plus per-adapter round-trip tests can each pass while the actual cross-entity transactional flow between them is still broken.
- Verify cross-repo contract source code directly, not just its docs, before implementing against it — this story's own docs (`api-design.md`, `data-model.md`) were stale relative to `agents/`'s actual implementation more than once, and checking `agents/`'s source directly caught real mismatches before they became implementation bugs.
- When a dev environment's Docker/Testcontainers access silently breaks (e.g. a WSL integration disconnect), a fast `docker ps` before investing time in a task that needs it would surface the blocker immediately instead of after the implementation is already written; treat it as a blocker requiring explicit user action, not something to route around by skipping verification.
- In a workspace where some plugin tooling is missing (test-suite generator, `RECORD-EDGE-CASE`), the existing raw-notes log and sibling tests are adequate substitutes — don't let missing tooling block otherwise-ready work; note the gap in the raw notes and proceed on established precedent from earlier tasks in the same story.

---

> [← planning/README.md](../README.md)
