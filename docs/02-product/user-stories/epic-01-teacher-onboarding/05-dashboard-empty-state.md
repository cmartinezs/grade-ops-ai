# US-005: Dashboard Empty State

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P1
- **ID:** US-005

## Story

As a teacher new to GradeOps AI, I want a useful dashboard when I have no assessments yet so I know exactly how to start my first assessment cycle.

## Acceptance Criteria

- Dashboard renders correctly with zero assessments (no errors, no broken counters).
- Empty state explains the next step and links directly to assessment creation.
- Empty state disappears once the first assessment exists.

---

## Definition of Done

- [ ] Empty-state component in `web/` rendered when the dashboard endpoint returns an empty assessment list.
- [ ] Clear call-to-action linking directly to the assessment creation entry point.
- [ ] Empty state replaced by the normal list once at least one assessment exists.
- [ ] UI tests cover: zero-data render (empty state, no errors) and with-data render (list shown).
- [ ] No agent involvement — no `AgentExecutionLog` required (per epic DoD).

## Technical Notes

- **Area:** `web/`
- Reuses the dashboard endpoint from US-002 unchanged — no API work in this story.
- Copy and visuals should align with `docs/06-ux/` design intent for the teacher workspace.
- Implements the epic DoD item "Dashboard renders correctly with zero assessments (empty state)".

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-002 (Assessment Dashboard) | Empty state is a state of the dashboard view |

## Complexity

**Estimate:** S
