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
- Dashboard exposes a visible new-assessment action that navigates to the assessment brief intake route when US-010 is in scope.

---

## Definition of Done

- [ ] Dashboard implemented in `web/` consuming a single aggregated API endpoint (assessment list + status + counts), scoped to the authenticated teacher.
- [ ] Status, submission count, and pending-approval count computed server-side in `api/` (approvals derived from `ApprovalEvent`).
- [ ] Status values come from the workflow state machine owned by `api/` — no frontend-invented states.
- [ ] Report/log links render only when the target exists.
- [ ] The new-assessment action is wired to `/assessments/new` once US-010 is implemented; it must not be a dead button or require the teacher to type the URL manually.
- [ ] Every dashboard datum/action is backed by `api/` read models, capabilities or routes; missing API support becomes API scope/residual before the UI is declared complete.
- [ ] Any future async dashboard action or status card declares its completion/progress mechanism instead of relying on local timers or inferred states.
- [ ] Dashboard labels, empty/loading/error states, actions and status labels use i18n keys or API-backed localized labels for the effective user locale; technical status codes remain in English.
- [ ] Tests cover: aggregation correctness, teacher scoping, and rendering with 0, 1, and N assessments.
- [ ] Tests cover the new-assessment action wiring when the route is available.
- [ ] No agent involvement — no `AgentExecutionLog` required (per epic DoD).

## Technical Notes

- **Area:** `web/` + `api/`
- Dashboard DTO mirrors the API contract (types flow API → Web); avoid N+1 by aggregating counts in one endpoint/query.
- Dashboard reads are expected to be sync GETs. If a dashboard action starts async work, the API must expose operation/status or another agreed completion contract before the UI surfaces it as complete.
- Dashboard copy and status labels follow `docs/master-plan/analysis/i18n-strategy.md`: `api` may return status codes plus localized labels, or `web` maps codes to translation keys. Logs/telemetry remain in English.
- Pending approvals = outstanding `ApprovalEvent`s for the assessment (rubric, grading, feedback) — the definition must match what Epics 03/05/06 produce.
- Empty-state UX is owned by US-005; this story only guarantees error-free rendering with zero data.
- New-assessment reachability is shared with US-010: the dashboard owns the visible action, while US-010 owns the intake route and form.
- Report/log links point at outputs of Epics 08/09; render conditionally until those exist.

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-001 (Teacher Login) | Dashboard is behind authentication and scoped per teacher |

## Complexity

**Estimate:** M
