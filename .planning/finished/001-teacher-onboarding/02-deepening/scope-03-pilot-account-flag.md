# 🔍 DEEPENING: Scope 03 — Pilot Account Flag

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

As an operator, I want to mark an account as a pilot customer so business evidence can be tracked.

> Source: `docs/02-product/user-stories/epic-01-teacher-onboarding/03-pilot-account-flag.md` — US-003

## Area

AP (`api/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Flyway migration: columnas de flag piloto](scope-03-pilot-account-flag/task-01-flyway-pilot-flag-columns.md) | GENERATE-DOCUMENT | DONE | `V2__add_pilot_flag_columns.sql` |
| 2 | [PATCH /internal/teachers/{uid}/flags](scope-03-pilot-account-flag/task-02-pilot-flag-endpoint.md) | GENERATE-DOCUMENT | DONE | `InternalTeacherController` (patch), `PilotFlagService` |

---

## Done Criteria

- [x] Account can be flagged as pilot/free/paid.
- [x] Related-party flag can be stored.
- [x] Offer/plan can be associated.
- [x] Evidence can be linked.
- [x] (DoD) Account plan flag (`pilot` / `free` / `paid`) persisted on the teacher account record in `api/` (Flyway migration).
- [x] (DoD) `related_party` flag stored, consistent with the `RevenueEvent.related_party` evidence requirement.
- [x] (DoD) Offer/plan details and evidence links persisted and queryable through internal tooling.
- [x] (DoD) Flag changes are auditable (who set it, when).
- [x] (DoD) Tests cover: setting each flag value, changing it, and querying accounts by flag.
- [x] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- No teacher-facing UI: epic DoD only requires "persisted and visible in internal tooling" — an internal/admin endpoint is enough for MVP.
- Flags feed the business evidence layer (`docs/05-evidence/`; Epic 09 — US-082 consumes them).
- Applies to both self-registered (scope-08) and provisioned (scope-06) accounts.
- **Open point:** operator access mechanism is undefined (no operator role/login in any story) — decide before implementation (protected internal endpoint vs. console access).

## Dependencies

| Depends on | Reason |
|------------|--------|
| scope-06 or scope-08 | A teacher account must exist before it can be flagged |

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
