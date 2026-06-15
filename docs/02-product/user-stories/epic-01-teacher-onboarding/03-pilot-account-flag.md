# US-003: Pilot Account Flag

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P1
- **ID:** US-003

## Story

As an operator, I want to mark an account as a pilot customer so business evidence can be tracked.

## Acceptance Criteria

- Account can be flagged as pilot/free/paid.
- Related-party flag can be stored.
- Offer/plan can be associated.
- Evidence can be linked.

---

## Definition of Done

- [ ] Account plan flag (`pilot` / `free` / `paid`) persisted on the teacher account record in `api/` (Flyway migration).
- [ ] `related_party` flag stored, consistent with the `RevenueEvent.related_party` requirement for evidence reporting.
- [ ] Associated offer/plan details and evidence links persisted and queryable through internal tooling (admin endpoint or query).
- [ ] Flag changes are auditable (who set it, when).
- [ ] Tests cover: setting each flag value, changing it, and querying accounts by flag.
- [ ] No agent involvement — no `AgentExecutionLog` required (per epic DoD).

## Technical Notes

- **Area:** `api/`
- No teacher-facing UI: the epic DoD only requires the flag to be "persisted and visible in internal tooling" — an internal/admin endpoint is enough for MVP.
- These flags feed the business evidence layer (`docs/05-evidence/`; Epic 09 — US-082 Business Evidence Dashboard consumes them).
- Applies equally to self-registered (US-008) and operator-provisioned (US-006) accounts.
- **Open point:** the "operator" has no defined access mechanism (no operator role/login exists in any story) — for MVP this can be a protected internal endpoint or direct console access; decide before implementation.

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-006 or US-008 | A teacher account must exist before it can be flagged |

## Complexity

**Estimate:** M
