# 🔍 DEEPENING: Scope 02 — Assessment Dashboard

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

As a teacher, I want a dashboard of assessments and statuses so I can know what needs action.

> Source: `docs/02-product/user-stories/epic-01-teacher-onboarding/02-assessment-dashboard.md` — US-002

## Area

WB (`web/`), AP (`api/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [GET /assessments endpoint](scope-02-assessment-dashboard/task-01-assessments-list-endpoint.md) | GENERATE-DOCUMENT | DONE | `AssessmentController`, `AssessmentSummaryDto` |
| 2 | [Dashboard page en web/](scope-02-assessment-dashboard/task-02-dashboard-page.md) | GENERATE-DOCUMENT | DONE | `web/src/app/(protected)/dashboard/page.tsx`, `AssessmentCard` |

---

## Done Criteria

- [x] Dashboard lists assessments.
- [x] Each assessment shows status.
- [x] Each assessment shows submission count.
- [x] Each assessment shows pending approvals.
- [x] Each assessment links to report/logs if available.
- [x] (DoD) Dashboard in `web/` consumes a single aggregated API endpoint scoped to the authenticated teacher.
- [x] (DoD) Status, submission count, and pending-approval count computed server-side in `api/` (approvals from `ApprovalEvent`).
- [x] (DoD) Status values come from the workflow state machine owned by `api/` — no frontend-invented states.
- [x] (DoD) Report/log links render only when the target exists.
- [x] (DoD) Tests cover aggregation correctness, teacher scoping, and rendering with 0, 1, and N assessments.
- [x] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- Dashboard DTO mirrors the API contract (types flow API → Web); avoid N+1 by aggregating counts in one endpoint/query.
- Pending approvals = outstanding `ApprovalEvent`s (rubric, grading, feedback) — definition must match Epics 03/05/06.
- Empty-state UX is owned by scope-05; this scope only guarantees error-free rendering with zero data.
- Report/log links point at outputs of Epics 08/09; render conditionally until those exist.

## Dependencies

| Depends on | Reason |
|------------|--------|
| scope-01 (teacher-login) | Dashboard is behind authentication and scoped per teacher |

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
