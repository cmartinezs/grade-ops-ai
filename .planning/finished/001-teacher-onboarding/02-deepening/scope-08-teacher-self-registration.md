# 🔍 DEEPENING: Scope 08 — Teacher Self-Registration

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

As a programming educator, I want to create my own GradeOps AI account so I can start using the platform without waiting for an operator.

> Source: `docs/02-product/user-stories/epic-01-teacher-onboarding/08-teacher-self-registration.md` — US-008

## Area

WB (`web/`), AP (`api/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [POST /auth/register endpoint](scope-08-teacher-self-registration/task-01-register-teacher-api-endpoint.md) | GENERATE-DOCUMENT | DONE | `AuthController`, `AuthService`, `TeacherRepository` (upsert) |
| 2 | [Página de registro en web/](scope-08-teacher-self-registration/task-02-registration-page.md) | GENERATE-DOCUMENT | DONE | `web/src/app/register/page.tsx` |

---

## Done Criteria

- [x] Teacher can register with email and password.
- [x] Registration creates a Firebase Auth user and a corresponding teacher record in the API.
- [x] Registering with an already-used email shows a clear error.
- [ ] A verification email is sent automatically after registration (scope-09).
- [x] A new account starts with an empty workspace (zero assessments).
- [x] (DoD) Registration flow implemented in `web/` using the Firebase Auth SDK (`createUserWithEmailAndPassword`).
- [x] (DoD) `api/` validates the Firebase ID token and creates the teacher record keyed by Firebase UID — the API never receives or stores raw passwords.
- [x] (DoD) Password policy enforced through Firebase Auth settings.
- [x] (DoD) Tests cover: successful registration, duplicate email, teacher record creation in the API.
- [ ] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- Firebase Authentication (Google Identity Platform) is the identity provider — record the ADR in `docs/99-decisions/`.
- API authenticates by verifying Firebase ID tokens (Admin SDK / JWKS); Firebase web config is public by design, the Gemini API key remains server-side only.
- Firebase project provisioning is scope-10 (`infra/`, Terraform `google_identity_platform_*` resources).
- Pilot/related-party flagging (scope-03) applies to self-registered accounts as well.
- Coexists with operator provisioning (scope-06); both create the same kind of teacher record.

## Dependencies

| Depends on | Reason |
|------------|--------|
| scope-10 (firebase-identity-platform-setup) | Identity provider must exist before any registration flow |
| scope-11 (firebase-auth-adr) | Implementation follows the recorded decision, not assumptions |

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
