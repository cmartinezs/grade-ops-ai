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

*(No other unexpected events recorded yet.)*

---

> [← README](README.md) | [← planning/README.md](../../README.md)
