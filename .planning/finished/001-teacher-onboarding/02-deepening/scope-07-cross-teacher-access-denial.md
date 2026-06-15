# 🔍 DEEPENING: Scope 07 — Cross-Teacher Access Denial

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

As a teacher, I want any attempt to reach another teacher's data to be safely denied so each workspace stays private.

> Source: `docs/02-product/user-stories/epic-01-teacher-onboarding/07-cross-teacher-access-denial.md` — US-007

## Area

AP (`api/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [OwnershipVerifier + ResourceNotFoundException](scope-07-cross-teacher-access-denial/task-01-ownership-verifier.md) | GENERATE-DOCUMENT | DONE | `OwnershipVerifier.java`, `ResourceNotFoundException.java`, `GlobalExceptionHandler.java` |
| 2 | [Aplicar OwnershipVerifier y structured logging](scope-07-cross-teacher-access-denial/task-02-apply-ownership-and-logging.md) | GENERATE-DOCUMENT | DONE | `AssessmentService.java` (update), `logback-spring.xml` |

---

## Done Criteria

- [x] A direct URL or deep link to another teacher's assessment is denied.
- [x] API requests for another teacher's resources are rejected server-side.
- [x] The denial response does not reveal whether the resource exists.
- [x] Denied access attempts are logged.
- [x] (DoD) Ownership verified against the authenticated Firebase UID in the service/repository layer (not ad hoc per controller).
- [x] (DoD) Another teacher's resource returns `404` with a body identical to a nonexistent resource — never `403`.
- [x] (DoD) Denied attempts logged with teacher id, target resource, timestamp (Cloud Logging).
- [x] (DoD) Integration tests: cross-teacher GET and mutation denied, owner allowed, denial indistinguishable from not-found.
- [x] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- Implement as a reusable ownership-scoping pattern (`teacher_id` filter at the data access layer) — every teacher-scoped entity from Epics 02+ must reuse it.
- `web/` treats the `404` as standard not-found; no special denial UI.
- Deepens the US-001 / epic criterion "teacher cannot access another teacher's data".

## Dependencies

| Depends on | Reason |
|------------|--------|
| scope-01 (teacher-login) | Denial rules act on the authenticated teacher identity |

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
