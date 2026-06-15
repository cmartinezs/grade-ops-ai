# US-009: Email Verification

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P0
- **ID:** US-009

## Story

As a teacher who just registered, I want to verify my email before using my workspace so accounts are real, reachable, and recoverable.

## Acceptance Criteria

- A verification email with a Firebase verification link is sent automatically after registration.
- An unverified teacher cannot access the workspace and sees a pending-verification screen.
- Teacher can request the verification email again (rate-limited).
- Once verified, the teacher gets full workspace access on next sign-in or refresh.

---

## Definition of Done

- [ ] `sendEmailVerification` wired into the post-registration flow in `web/`.
- [ ] `api/` rejects requests whose Firebase ID token has `email_verified = false` (status/resend endpoints excepted) — enforcement is server-side, not only UI.
- [ ] Resend is rate-limited and gives clear user feedback.
- [ ] Tests cover: unverified access blocked, verified access allowed, resend flow.
- [ ] No agent involvement — no `AgentExecutionLog` required (per epic DoD).

## Technical Notes

- **Area:** `web/` + `api/`
- Uses Firebase's native email verification link and the `email_verified` token claim — no custom OTP code generation, storage, or expiry logic.
- Customize the verification email template and sender domain in the Firebase console / Identity Platform config (`infra/` note).
- Operator-provisioned accounts (US-006) also pass through verification unless created pre-verified by the Admin SDK.

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-008 (Teacher Self-Registration) | Verification is triggered by the registration flow |

## Complexity

**Estimate:** M
