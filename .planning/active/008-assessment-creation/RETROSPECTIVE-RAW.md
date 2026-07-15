# Retrospective Raw Notes: 008-assessment-creation

> [← README](README.md) | [← planning/README.md](../../README.md)

Working log for events that were unexpected, corrective, risky, or useful for the final retrospective.

Use `/plan-edge-case <planning-id> -- <what happened>` to add manual entries. Commands may also append entries when they encounter blockers, corrections, skipped work, recovery actions, validation failures, or other non-linear events.

---

## How To Use This File

Capture facts while they are fresh. Do not polish entries here. The final retrospective belongs in `README.md`.

Each entry should answer as many of these as possible:

- What happened?
- What was expected instead?
- How was it resolved or contained?
- What should be carried forward?

---

## Log

<!-- Add newest entries at the top. -->

### 2026-07-14 — Story 03 split into a child planning: web/.planning/001-assessment-creation

**Source:** manual (human-requested, "haz un child planning a web con story-03")

**What happened:** `web/` had no `.planning/` workspace of its own — Story 03 (`web-assessment-creation`) was implemented directly in this root planning, with a full 6-task breakdown, the same situation `agents/` and `api/` were originally in before the 2026-07-09 correction. Applied the identical fix: initialized `web/.planning/` via `/plan-init` (area `WB` → `src/`, git base branch `develop`, no `docs/` subdirectory found so only one area besides `W`), created `web/.planning/active/001-assessment-creation` porting Story 03's full content (Objective, Context, Risk, 6 Tasks, Done Criteria) unchanged, and rewrote this root planning's Story 03 as a coordination story (renamed `web-assessment-creation-coordination`) pointing to it — same shape as Stories 01/02.

**What was expected instead:** n/a — this wasn't discovered as a mistake mid-expansion like the 2026-07-09 case; the human explicitly asked for it once Stories 01/02/04 were already in a good state, applying the established pattern proactively.

**How it was resolved:** `web/.planning/` created directly in the main worktree (not a separate `../gradeops-web` worktree — that convention applies to actual task-execution branching, not the planning/expansion authoring phase, consistent with how `agents/.planning/` and `api/.planning/003-assessment-creation`'s planning files were themselves authored in 2026-07-09). Updated `01-expansion.md`'s Story Summary, Dependency Map, Impact table, Linked Child Plannings, and Notes sections; updated `active/README.md`'s story-03 row and link.

**What should be carried forward:** unlike `agents/`/`api/`, `web/`'s new child planning starts with both its dependencies (`agents/001`, `api/003`) already `DONE` — flagged this explicitly in the new child planning's own Risk Register (R-01) so implementation verifies against `api/`'s actual source rather than assuming the (already-known-stale-in-places) `docs/04-architecture/api-design.md` is current.

---

### 2026-07-14 — Story 02 sync checkpoints updated: api/'s child planning confirmed DONE

**Source:** manual (human-requested sync after confirming `api/003` merged)

**What happened:** With `api/003-assessment-creation` confirmed `DONE` (11/11 tasks, PR #44 merged, archived to `api/.planning/finished/`), updated Story 02's Sync Checkpoints (2 and 3 → DONE, with dated evidence), Done Criteria bullet 1 (checked), `01-expansion.md`'s Story Summary and Linked Child Plannings rows, and `active/README.md`'s status column. Also corrected two stale rows found in the same pass while here: `01-expansion.md`'s Story Summary table still listed both Story 01 and Story 02 as `TODO` even though Story 01 had already been `IN PROGRESS` since 2026-07-12 (that table was never updated when the story file's own status changed) — fixed both to `IN PROGRESS`.

**What was expected instead:** n/a — this is the coordination story working as designed, same pattern as Story 01's 2026-07-12 update. The stale Story Summary rows were an oversight from that earlier update not being propagated to every index file that duplicates story status.

**How it was resolved:** Story 02's own file, `01-expansion.md` (Story Summary + Linked Child Plannings), and `active/README.md` all updated in the same pass, cross-checked against each other for consistency. Left Checkpoint 4 and Done Criteria bullet 2 (both about `web/` reachability) unchecked — `web/`'s Story 03 hasn't started, so there's genuinely nothing to confirm yet, same reasoning as Story 01's still-unmet reachability criterion before Story 04 existed.

**What should be carried forward:** three files (`story-NN-*.md`, `01-expansion.md`'s Story Summary table, `active/README.md`) all independently duplicate each story's status — a status change in one doesn't propagate automatically. When updating any story's status, check all three rather than assuming the story file alone is the single source of truth.

---

### 2026-07-14 — Story 04 (e2e-integration-verification) added post-initial-expansion via /plan-enrich-epic

**Source:** `/plan-enrich-epic`

**What happened:** While checking what remained for Story 01 to reach `DONE`, confirmed that `api/003-assessment-creation` (Story 02's tracked child) had just finished and merged (PR #44) — but reading its actual test code (`AssessmentCreationFlowIntegrationTest.java`, `GenerateAssessmentDraftHandlerIntegrationTest.java`, `AssessmentAgentClientTest.java`) showed every one of them mocks or stubs `AssessmentAgentClient` — no automated test anywhere exercises a real HTTP call between `api/` and `agents/`. Story 01's Done Criteria #2 ("internal agent endpoint is reachable from `api/` in the target environment") therefore still cannot be satisfied by either child planning's existing evidence, even with both children functionally complete. Separately discovered root `compose.yml` has no `agents` service at all — only `db`, `api`, `web` — so there wasn't even a straightforward way to run a real local check.

**What was expected instead:** n/a — this is a genuine, previously-unnoticed coverage gap, not a process failure. Neither child planning's own scope included proving the cross-service network path; each proved its own side works in isolation (unit/integration tests) or was manually curl-tested standalone (`agents/`'s Groq end-to-end call).

**How it was resolved:** added Story 04 (`e2e-integration-verification`, area `IN`, depends on Story 01 and Story 02) directly to this root planning — same reasoning as Story 03 (`web/`): no single child workspace owns cross-service reachability, so it belongs here. Scoped in two phases per human direction: (1) add `agents` to `compose.yml` and run a real local docker-compose smoke flow; (2) verify and smoke-test the already-documented `beta` environment on Render, scripted with the official Render CLI (`github.com/render-oss/cli`, confirmed via web search to exist and support non-interactive `RENDER_API_KEY` auth).

**What should be carried forward:** when a coordination story's Done Criteria mention "reachable in the target environment," verify that claim against actual test code, not just each child planning's own "DONE" status — a child can be fully done by its own scope while a cross-cutting integration point neither child owns remains unverified. Consider whether future monorepo-root plannings with 2+ coordination stories that call each other should get an explicit integration-verification story from the start, rather than discovering the gap only when checking a coordination story's readiness for `DONE`.

---

### 2026-07-12 — Story 01 checkpoint 2 closed, story remains BLOCKED on api/'s child planning not having started

**What happened:** Ran `/plan-story 008-assessment-creation story-01-agents-assessment-agent` after the human confirmed the `agents/` child planning's tracked story reached completion. Verified directly: `agents/.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent.md` is `Status: DONE`, all 5 tasks `DONE`, all Done Criteria checked. Updated this coordination story's Sync Checkpoint 2 and Done Criteria bullet 1 to reflect that. `[EXECUTE-STORY]` then found Done Criteria bullet 2 ("internal agent endpoint is reachable from `api/`") unmet — `api/.planning/active/003-assessment-creation` is still status EXPANSION with story-01 `assessment-creation-persistence` still `TODO`; no `agentclient` call has been attempted yet, so reachability cannot be verified. Also found and logged a documentation inconsistency (Inconsistencies Found #3): `agents/.planning/active/001-assessment-creation`'s Residual #1 (Gemini success path unproven) is still `Status: OPEN` in that file, while the sibling child planning `agents/.planning/finished/002-groq-genai-provider`'s retrospective claims to have closed it — but only via a Groq call, not Gemini, so the two claims describe different things.

**What was expected instead:** n/a — this is the coordination story working as designed: a child reaching DONE does not imply the whole coordination story is DONE, since Sync Checkpoint 3 (api/ side) is a separate, later condition.

**How it was resolved or contained:** Story status set to `IN PROGRESS` (was `TODO`). Sync Checkpoints table and Done Criteria updated with dated evidence. Story left `BLOCKED`, not `DONE` — will be re-run once `api/.planning/active/003-assessment-creation` story-01 reaches a point where `agentclient` reachability against the live `agents/` endpoint can be confirmed.

**What should be carried forward:** the residual-status mismatch between `agents/`'s two child plannings (found above) is a documentation-accuracy issue, not a blocker, and was left unfixed in the child's own file — it belongs to whoever next touches `agents/.planning/active/001-assessment-creation`'s Residuals table, scoped as its own commit per the parent/child commit-scoping rule (`CLAUDE.md`).

---

### 2026-07-09 — Monorepo child-planning rule was missed during /plan-expand

**What happened:** During `/plan-expand`, Story 02 (`api-assessment-creation`) and Story 01 (`agents-assessment-agent`) were scoped as full implementation stories directly in this root planning. This skipped the monorepo parent/child coordination check: `api/` already had its own `.planning/` workspace (with prior plannings `001-hexagonal-refactor`, `002-drop-old-password-recovery-requests`), so its implementation should have been split into a child planning there instead of duplicated in the parent. The user caught this by asking for a review specifically focused on the monorepo rule.

**What was expected instead:** `/plan-expand` step 3b should have detected `api/.planning/` and created a child planning there, keeping only a coordination story in the parent. `agents/` and `web/` had no `.planning/` at the time, so per the rule they would have stayed as normal root stories — except the user additionally decided `agents/` should also get its own `.planning/` workspace (matching `api/`'s pattern), which is a project convention choice beyond what the rule strictly requires.

**How it was resolved:**
1. Initialized `agents/.planning/` via `/plan-init` (area `AG` → `src/`, git base branch `develop` detected from the remote).
2. Created `api/.planning/active/003-assessment-creation` and `agents/.planning/active/001-assessment-creation`, porting the full implementation content (Objective, Context, Risk, Tasks, Done Criteria) from this root planning's Story 02 and Story 01 respectively — not duplicating, moving.
3. Rewrote this root planning's Story 01 and Story 02 as coordination stories that link to the child plannings and track sync checkpoints, rather than containing implementation tasks.
4. Filled `01-expansion.md → Linked Child Plannings` with both child plannings, ownership, and sync notes; added risk R-03 (coordination drift between parent and children).

**What should be carried forward:** Before running `/plan-expand` on any future monorepo-root planning, explicitly check `<child>/.planning/` for every affected directory (`ls <dir>/.planning`) — do not assume a sub-repo lacks its own workspace just because it wasn't mentioned in the initial idea document.

---

### 2026-07-10 — Cross-child-planning contract drift: agents/'s AssessmentCommand changed after api/'s dependent tasks were already atomized

**What happened:** `agents/.planning/001-assessment-creation`'s task-02 code review found that `AssessmentCommand.previousDraftId` alone cannot supply the regeneration prompt with the prior draft's content — `agents/` never persists data or calls back into `api/`. A `previousDraft` (content) field was added to the contract on 2026-07-10, after `api/.planning/003-assessment-creation`'s task-05 (`agentclient`) and task-08 (regeneration endpoint) had already been atomized against the original 7-field shape (Sync Checkpoint 1 of Story 01 in this planning had been marked DONE the day before, 2026-07-09).

**What was expected instead:** the contract each child planning atomizes tasks against should stay stable once a sync checkpoint marks it DONE, or downstream tasks in the other child planning need to be notified and corrected before they're executed — not discovered only when someone tries to implement against a stale copy.

**How it was resolved:** the user explicitly asked to notify the `api/` child planning. Updated three layers: (1) `api/`'s own task-05/task-08 files plus that story's Inconsistencies Found, so whoever executes those tasks sees the correct 8-field contract; (2) this root planning's Story 01 and Story 02 coordination stories (Sync Checkpoints + Inconsistencies Found), so the drift is visible at the parent level, not just buried in a child planning's file; (3) `agents/`'s own task-01/task-03/TRACEABILITY already recorded the change on its side when the field was added. Each edit was committed on its own planning's branch (this root-level fix on `chore/notify-agentclient-previousdraft` off `develop`; `api/`'s fix on `gradeops-api/story-01-assessment-creation-persistence`) per the parent/child commit-scoping rule.

**What should be carried forward:** a sync checkpoint marked DONE is a snapshot, not a guarantee — if a child planning's contract changes after a dependent checkpoint is checked off, that change must be pushed back through the coordination story (this file's Story 01/02), not just fixed silently inside the child planning that happened to catch it. Consider whether sync checkpoints for "contract defined" should link directly to the contract file's task, so a later diff is easier to notice.

---

> [← README](README.md) | [← planning/README.md](../../README.md)
