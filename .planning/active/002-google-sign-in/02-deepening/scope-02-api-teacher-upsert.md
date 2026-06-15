# 🔍 DEEPENING: Scope 02 — api-teacher-upsert

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Update the API teacher registration endpoint to handle first-time Google sign-in: if a teacher record already exists for the given Firebase UID, return it; otherwise create it. This makes the endpoint safe to call from both the email/password and Google sign-in flows.

---

## Tasks

| # | Task | Area | Status | Output |
|---|------|------|--------|--------|
| 1 | Change `POST /api/teachers/register` to upsert by Firebase UID (find-or-create, not fail-on-duplicate) | `api/` | DONE | `AuthService`, `AuthController`, `RegisterResult` |
| 2 | Add `provider` field to teacher record (values: `EMAIL_PASSWORD`, `GOOGLE`) | `api/` | DONE | `V3__add_provider_column.sql`, `TeacherEntity` |
| 3 | Write unit tests: first Google sign-in creates record; returning Google user returns existing record without duplicate | `api/` | DONE | `AuthControllerTest` (2 new tests) |
| 4 | Verify existing email/password registration tests still pass | `api/` | DONE | 29/29 tests passing |

---

## Done Criteria

- [x] `POST /api/teachers/register` returns `200 OK` with the existing teacher if UID already exists (no `409 Conflict`).
- [x] `POST /api/teachers/register` returns `201 Created` for a new Google user.
- [x] `provider` field persisted in the teacher record.
- [x] Existing email/password registration and login tests pass without modification.
- [x] Flyway migration applied cleanly.
- [x] TRACEABILITY.md updated with new terms from this scope.

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
