# 🔍 DEEPENING: Scope 09 — Email Verification

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

As a teacher who just registered, I want to verify my email before using my workspace so accounts are real, reachable, and recoverable.

> Source: `docs/02-product/user-stories/epic-01-teacher-onboarding/09-email-verification.md` — US-009

## Area

WB (`web/`), AP (`api/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [EmailVerifiedFilter en api/](scope-09-email-verification/task-01-email-verified-filter.md) | GENERATE-DOCUMENT | DONE | `EmailVerifiedFilter.java`, whitelist `/auth/register`, `/auth/verify/resend` |
| 2 | [Pantalla verify-email + reenvío](scope-09-email-verification/task-02-verification-screen-and-resend.md) | GENERATE-DOCUMENT | DONE | `web/src/app/verify-email/page.tsx` |

---

## Done Criteria

- [x] A verification email with a Firebase verification link is sent automatically after registration.
- [x] An unverified teacher cannot access the workspace and sees a pending-verification screen.
- [x] Teacher can request the verification email again (rate-limited).
- [x] Once verified, the teacher gets full workspace access on next sign-in or refresh.
- [x] (DoD) `sendEmailVerification` wired into the post-registration flow in `web/`.
- [x] (DoD) `api/` rejects requests whose Firebase ID token has `email_verified = false` (status/resend endpoints excepted) — enforcement is server-side.
- [x] (DoD) Resend is rate-limited with clear user feedback.
- [x] (DoD) Tests cover: unverified access blocked, verified access allowed, resend flow.
- [x] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- Uses Firebase's native email verification link and the `email_verified` token claim — no custom OTP code generation, storage, or expiry logic.
- Customize the verification email template and sender domain in Firebase console / Identity Platform config (`infra/` note).
- Operator-provisioned accounts (scope-06) also pass through verification unless created pre-verified via the Admin SDK.

## Dependencies

| Depends on | Reason |
|------------|--------|
| scope-08 (teacher-self-registration) | Verification is triggered by the registration flow |
| scope-11 (firebase-auth-adr) | The ADR settles whether provisioned accounts skip the verification gate |

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
