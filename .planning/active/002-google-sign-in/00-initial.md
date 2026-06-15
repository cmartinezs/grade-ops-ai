# 🌱 INITIAL: 002-google-sign-in

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

Implement Google OAuth sign-in for teachers across `web/`, `api/`, and `infra/`, complementing the existing email/password registration flow.

---

## Why

Firebase Google Authentication is already enabled in the project's Firebase console. Teachers expect to sign in with their Google account — it reduces friction at onboarding and avoids managing a separate password. The existing email/password path stays intact; Google sign-in is an additive provider.

This is captured in **US-011** (`docs/02-product/user-stories/epic-01-teacher-onboarding/11-google-sign-in.md`).

---

## Approximate Scope

- [x] `docs/` — US-011 story already written; no further doc changes needed
- [ ] `web/` — Add "Sign in with Google" button to `/login` and `/register`; update `AuthGuard` to bypass email verification for Google-authenticated users
- [ ] `api/` — Upsert teacher record by Firebase UID on first Google sign-in (reuse existing token verification path; add find-or-create logic)
- [ ] `infra/` — Enable Google identity provider in Terraform Firebase config (`google_identity_platform_default_supported_idp_config`)
- [ ] `.planning/` — This planning document

---

## Initiator

- **Requested by:** human
- **Date:** 2026-06-14
- **Related planning (if continuation):** 001-teacher-onboarding

---

## Next Step

- [ ] When dimensioned → fill `01-expansion.md` and move to `planning/active/`
- [ ] If needs clarification first → document open questions below

### Open Questions

*Resolved.*

- "Sign in with Google" button appears **alongside** the email/password form on both `/login` and `/register`.
- `infra/` Terraform change is applied to `demo` **together** with the code, not as a separate step.

---

> [← planning/README.md](../../README.md)
