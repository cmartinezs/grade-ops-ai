# 🔍 DEEPENING: Scope 11 — Firebase Authentication ADR

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Record the durable decision that Firebase Authentication (Google Identity Platform) is the identity provider, and close the open auth decisions that block execution of scopes 03, 04, 06, 08, and 09.

> Source: planning gap — decision referenced by scopes 01/08 and the epic's Technical Notes, required by `docs/` content rules (`99-decisions/`). No source user story.

## Area

DO (`docs/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Write Firebase Authentication ADR](scope-11-firebase-auth-adr/task-01-write-firebase-auth-adr.md) | GENERATE-DOCUMENT | DONE | `docs/99-decisions/2026-06-12-firebase-authentication.md` |

---

## Done Criteria

- [x] ADR created at `docs/99-decisions/2026-06-12-firebase-authentication.md` using `adr-template.md`.
- [x] Decision fixed: Firebase Auth (Identity Platform) as identity provider; `api/` validates ID tokens and never stores credentials; teacher-only auth model (students use signed token links).
- [x] Open decision closed: operator access mechanism for provisioning and pilot flags (scopes 03/06) — internal endpoint vs. console access.
- [x] Open decision closed: whether operator-provisioned accounts are created email-pre-verified (scope-06) or pass the scope-09 verification gate.
- [x] Open decision closed: session expiry strategy (scope-04) — Firebase session cookies vs. refresh-token revocation + inactivity check.
- [x] Affected user stories and docs reference the ADR with no contradictions introduced.
- [x] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- Use `docs/99-decisions/adr-template.md`; naming convention `YYYY-MM-DD-short-title.md` (per `docs/CLAUDE.md`).
- The decision affects multiple documents, implementation direction, and architecture — exactly the threshold `docs/CLAUDE.md` sets for a decision record.
- Once accepted, scopes 06/08/09 implement against the ADR, not against assumptions.

## Dependencies

| Depends on | Reason |
|------------|--------|
| — | Documentation-only; can start immediately (in parallel with scope-10) |

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
