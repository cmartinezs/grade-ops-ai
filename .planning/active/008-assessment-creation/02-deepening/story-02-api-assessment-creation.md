# 🔍 DEEPENING: Story 02 — api-assessment-creation-coordination

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

**Coordination story — not an implementation story.** Track the `api/` half of the assessment-creation pipeline, which is implemented in the child planning [`api/.planning/active/003-assessment-creation`](../../../../api/.planning/active/003-assessment-creation/README.md) (`api/` already has its own `.planning/` workspace — see `01-expansion.md → Linked Child Plannings`).

The full implementation detail (brief/draft persistence, versioning, `agentclient` integration, tasks, Done Criteria) lives in that child planning's `02-deepening/story-01-assessment-creation-persistence.md` — it is intentionally not duplicated here.

**Source stories:** `docs/02-product/user-stories/epic-02-assessment-creation/01-assessment-brief-intake.md`, `02-assessment-draft-generation.md`, `03-assessment-draft-regeneration.md`.

---

## Sync Checkpoints

| # | Checkpoint | Status |
|---|-----------|--------|
| 1 | `api/.planning/003-assessment-creation` reaches EXPANSION/DEEPENING with brief/draft persistence and versioning designed | ✅ DONE (2026-07-09) |
| 2 | `agents/.planning/001-assessment-creation` (Story 01, this planning) reaches DONE — real endpoint available to integrate against | TODO |
| 3 | Child planning's Story 01 (`assessment-creation-persistence`) reaches DONE — endpoints live and testable | TODO |
| 4 | Endpoints confirmed stable enough for `web/` (Story 03, this planning) to integrate against | TODO |

---

## Done Criteria

- [ ] `api/.planning/active/003-assessment-creation` reports its story `assessment-creation-persistence` as DONE.
- [ ] Brief intake, draft generation, draft regeneration, edit, and retrieval endpoints are reachable from `web/` in the target environment.
- [ ] This coordination story's status here is updated to DONE only after the child planning confirms completion — do not mark this DONE independently.

---

## Inconsistencies Found

*Record any contradictions or gaps detected during this story. Use RECORD-INCONSISTENCY workflow.*

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | This story originally contained the full backend implementation task breakdown (entities, migrations, endpoints, integration — 8 tasks), duplicating what should live in a child planning, since `api/` already had its own `.planning/` workspace that was not checked during `/plan-expand`. Corrected 2026-07-09: content moved to `api/.planning/active/003-assessment-creation/02-deepening/story-01-assessment-creation-persistence.md`; this file rewritten as a coordination story. | This file (previous version), `api/.planning/active/003-assessment-creation/` | RESOLVED | Content moved, not duplicated; this file now only tracks child planning status |

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
