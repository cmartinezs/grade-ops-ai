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
