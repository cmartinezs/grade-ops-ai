# US-001: Teacher Login

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P0
- **ID:** US-001

## Story

As a programming educator, I want to access my teacher workspace so I can manage my assessments securely.

## Acceptance Criteria

- Teacher can sign in.
- Teacher can see their assessments.
- Teacher can create a new assessment.
- Teacher cannot see another teacher's private assessment data.

---

## Definition of Done

- [ ] Sign-in flow implemented in `web/` with error state for invalid credentials.
- [ ] Authentication endpoint and session handling implemented in `api/`; credentials never logged or exposed client-side.
- [ ] All assessment queries are scoped server-side to the authenticated teacher, verified by an integration test.
- [ ] Tests cover: successful sign-in, failed sign-in, and unauthenticated access to a protected route.
- [ ] No agent involvement — no `AgentExecutionLog` required (per epic DoD).

## Technical Notes

- **Area:** `web/` + `api/`
- Auth mechanism: Firebase Authentication (Google Identity Platform) — decided alongside US-008/US-009; record the ADR in `docs/99-decisions/`. The API validates Firebase ID tokens and never stores credentials.
- Authorization must be enforced in `api/`, never only in the frontend; types flow API → Web.
- Students never log in (signed token links — see Epic 13); keep the auth model teacher-only.
- The AC "Teacher can create a new assessment" only requires the creation entry point to be visible and reachable; the creation flow itself is US-010 (Epic 02).
- Cross-teacher denial behavior is specified in detail by US-007.

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-006 (Teacher Account Provisioning) or US-008 (Teacher Self-Registration) | An account must exist before sign-in is possible |

## Complexity

**Estimate:** M
