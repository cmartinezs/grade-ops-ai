# US-007: Cross-Teacher Access Denial

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P0
- **ID:** US-007

## Story

As a teacher, I want any attempt to reach another teacher's data to be safely denied so each workspace stays private.

## Acceptance Criteria

- A direct URL or deep link to another teacher's assessment is denied.
- API requests for another teacher's resources are rejected server-side.
- The denial response does not reveal whether the resource exists.
- Denied access attempts are logged.

---

## Definition of Done

- [ ] Resource-level authorization in `api/`: every teacher-scoped resource verifies ownership against the authenticated Firebase UID, enforced in the service/repository layer (not ad hoc per controller).
- [ ] A request for another teacher's resource returns `404` with a body identical to a nonexistent resource — never `403` (no existence disclosure).
- [ ] Denied attempts logged with teacher id, target resource, and timestamp (Cloud Logging).
- [ ] Integration tests cover: cross-teacher GET and mutation denied, owner access allowed, denial response indistinguishable from not-found.
- [ ] No agent involvement — no `AgentExecutionLog` required (per epic DoD).

## Technical Notes

- **Area:** `api/`
- Implement as a reusable ownership-scoping pattern (`teacher_id` filter at the data access layer) — every teacher-scoped entity from Epics 02+ must reuse it.
- `web/` treats the `404` as a standard not-found; no special denial UI.
- Deepens the US-001 / epic criterion "a teacher cannot access another teacher's assessment data".

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-001 (Teacher Login) | Denial rules act on the authenticated teacher identity |

## Complexity

**Estimate:** M
