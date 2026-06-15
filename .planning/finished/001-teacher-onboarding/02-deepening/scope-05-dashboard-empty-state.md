# 🔍 DEEPENING: Scope 05 — Dashboard Empty State

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

As a teacher new to GradeOps AI, I want a useful dashboard when I have no assessments yet so I know exactly how to start my first assessment cycle.

> Source: `docs/02-product/user-stories/epic-01-teacher-onboarding/05-dashboard-empty-state.md` — US-005

## Area

WB (`web/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [EmptyDashboard component](scope-05-dashboard-empty-state/task-01-empty-dashboard-component.md) | GENERATE-DOCUMENT | DONE | `web/src/components/dashboard/EmptyDashboard.tsx` |

---

## Done Criteria

- [x] Dashboard renders correctly with zero assessments (no errors, no broken counters).
- [x] Empty state explains the next step and links directly to assessment creation.
- [x] Empty state disappears once the first assessment exists.
- [x] (DoD) Empty-state component in `web/` rendered when the dashboard endpoint returns an empty list.
- [x] (DoD) Clear call-to-action linking directly to the assessment creation entry point.
- [x] (DoD) UI tests cover zero-data render and with-data render.
- [x] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- Reuses the dashboard endpoint from scope-02 unchanged — no API work in this scope.
- Copy and visuals aligned with `docs/06-ux/` design intent.
- Implements the epic DoD item "Dashboard renders correctly with zero assessments".

## Dependencies

| Depends on | Reason |
|------------|--------|
| scope-02 (assessment-dashboard) | Empty state is a state of the dashboard view |

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)
