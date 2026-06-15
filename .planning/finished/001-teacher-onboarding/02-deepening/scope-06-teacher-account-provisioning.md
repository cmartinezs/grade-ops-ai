# 🔍 DEEPENING: Scope 06 — Teacher Account Provisioning

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

As an operator, I want to provision teacher accounts directly so pilot teachers can be onboarded without going through self-registration.

> Source: `docs/02-product/user-stories/epic-01-teacher-onboarding/06-teacher-account-provisioning.md` — US-006

## Area

AP (`api/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Flyway migration: teacher table](scope-06-teacher-account-provisioning/task-01-flyway-teacher-table.md) | GENERATE-DOCUMENT | DONE | `V1__create_teacher_table.sql` |
| 2 | [Firebase Admin SDK bean](scope-06-teacher-account-provisioning/task-02-firebase-admin-sdk-bean.md) | GENERATE-DOCUMENT | DONE | `FirebaseConfig.java`, `FirebaseAuth` bean |
| 3 | [POST /internal/teachers endpoint](scope-06-teacher-account-provisioning/task-03-provision-teacher-endpoint.md) | GENERATE-DOCUMENT | DONE | `InternalTeacherController`, `ProvisionTeacherService` |

---

## Done Criteria

- [x] Operator can create a teacher account with name and email.
- [x] A provisioned teacher can sign in immediately after receiving credentials or an invite.
- [x] A new account starts with an empty workspace (zero assessments).
- [x] Operator provisioning coexists with self-registration (scope-08); both produce the same kind of teacher record.
- [x] (DoD) Internal provisioning operation in `api/` creates the Firebase Auth user (Admin SDK) and the teacher record (Firebase UID) in one flow.
- [x] (DoD) Access delivery via Firebase invite / password-reset link — no passwords handled manually.
- [x] (DoD) Duplicate email fails with a clear error and no partial records.
- [x] (DoD) Tests cover: provision → sign-in path, duplicate email, empty workspace.
- [ ] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- Firebase Admin SDK runs server-side only (`api/`).
- Option: create the account email pre-verified (Admin SDK), skipping the scope-09 verification gate for provisioned pilots — document the choice either way.
- Pilot flagging (scope-03) applies after provisioning.
- **Open point (shared with scope-03):** operator access mechanism undefined — decide before implementation.

## Dependencies

| Depends on | Reason |
|------------|--------|
| scope-10 (firebase-identity-platform-setup) | Identity provider must exist before accounts can be created |
| scope-11 (firebase-auth-adr) | Closes the operator-access and pre-verified-email decisions |

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
