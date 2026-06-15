# US-011: Google Sign-In for Teachers

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P0
- **ID:** US-011

## Story

As a programming educator, I want to sign in or register using my Google account so that I can access GradeOps AI, create assessments, and review them without managing a separate password.

## Acceptance Criteria

- Teacher can sign in or register using their Google account via the Firebase Google OAuth provider.
- First-time Google sign-in creates a Firebase Auth user and a corresponding teacher record in the API (same structure as email/password registration).
- Returning teachers are recognized and redirected to the dashboard without creating a duplicate teacher record.
- Google-authenticated accounts skip the email verification step (Google already verifies the email).
- The API teacher record is keyed by Firebase UID regardless of sign-in method.
- A "Sign in with Google" button is present on both the `/login` and `/register` pages.
- If a teacher's Google email matches an existing email/password account, Firebase merges the accounts automatically.

---

## Definition of Done

- [ ] Google sign-in implemented in `web/` using `GoogleAuthProvider` and `signInWithPopup` from the Firebase Auth SDK.
- [ ] `/login` and `/register` pages display a "Sign in with Google" button alongside existing email/password forms.
- [ ] `api/` handles first-time Google sign-in: validates Firebase ID token and upserts the teacher record by Firebase UID (no duplicate if teacher already exists).
- [ ] Google-authenticated sessions bypass the email verification gate (`/verify-email`).
- [ ] Firebase project has Google as an enabled sign-in provider (`infra/` Terraform updated accordingly).
- [ ] Tests cover: first-time Google sign-in creates teacher record, returning Google user does not duplicate record, Google user bypasses email verification.
- [ ] No agent involvement — no `AgentExecutionLog` required.

## Technical Notes

- **Areas affected:** `web/` + `api/` + `infra/`
- Use `signInWithPopup(auth, new GoogleAuthProvider())` in the web client; the returned credential carries a Firebase ID token identical in structure to email/password tokens — the API verification path is unchanged.
- The upsert logic in `api/` must tolerate both first-time and returning Google users: find-by-UID, create if absent.
- Email verification gate in `AuthGuard` must check `user.emailVerified || user.providerData[0]?.providerId === 'google.com'` (Google accounts are pre-verified).
- Pilot/related-party flagging (US-003) applies to Google-registered accounts as well.
- `infra/`: add `google_identity_platform_default_supported_idp_config` for Google provider to the Terraform Firebase config.

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-008 Teacher Self-Registration | Shares teacher record creation logic in `api/`; upsert must coexist with email/password path |
| US-009 Email Verification | Auth guard must be updated to bypass verification for Google-authenticated users |
| Firebase project setup (`infra/`) | Google provider must be enabled before the OAuth flow works |

## Complexity

**Estimate:** M
