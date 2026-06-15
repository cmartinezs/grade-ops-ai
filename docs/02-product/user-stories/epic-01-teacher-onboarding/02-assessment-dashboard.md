# US-002: Assessment Dashboard

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P0
- **ID:** US-002

## Story

As a teacher, I want a dashboard of assessments and statuses so I can know what needs action.

## Acceptance Criteria

- Dashboard lists assessments.
- Each assessment shows status.
- Each assessment shows submission count.
- Each assessment shows pending approvals.
- Each assessment links to report/logs if available.

---

## Definition of Done

- [ ] Dashboard implemented in `web/` consuming a single aggregated API endpoint (assessment list + status + counts), scoped to the authenticated teacher.
- [ ] Status, submission count, and pending-approval count computed server-side in `api/` (approvals derived from `ApprovalEvent`).
- [ ] Status values come from the workflow state machine owned by `api/` — no frontend-invented states.
- [ ] Report/log links render only when the target exists.
- [ ] Tests cover: aggregation correctness, teacher scoping, and rendering with 0, 1, and N assessments.
- [ ] No agent involvement — no `AgentExecutionLog` required (per epic DoD).

## Technical Notes

- **Area:** `web/` + `api/`
- Dashboard DTO mirrors the API contract (types flow API → Web); avoid N+1 by aggregating counts in one endpoint/query.
- Pending approvals = outstanding `ApprovalEvent`s for the assessment (rubric, grading, feedback) — the definition must match what Epics 03/05/06 produce.
- Empty-state UX is owned by US-005; this story only guarantees error-free rendering with zero data.
- Report/log links point at outputs of Epics 08/09; render conditionally until those exist.

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-001 (Teacher Login) | Dashboard is behind authentication and scoped per teacher |

## Complexity

**Estimate:** M
