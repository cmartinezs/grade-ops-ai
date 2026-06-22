# US-012: Password Recovery

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P1
- **ID:** US-012

## Story

As a teacher who signed up with email and password, I want to reset my password if I forget it, so that I can regain access to my workspace without contacting support.

## Acceptance Criteria

- A "¿Olvidaste tu contraseña?" link is present on the `/login` page and opens a password recovery flow.
- The teacher enters their email address and submits the form.
- The backend generates a one-time reset code, stores it, and sends a custom HTML email via SMTP.
- The UI shows a neutral confirmation message regardless of whether the email is registered, to avoid user enumeration ("Si existe una cuenta con ese correo, recibirás un enlace en los próximos minutos.").
- Submitting an invalid email format shows a validation error before any request is made.
- Clicking the reset link in the email takes the teacher to `/reset-password?code=<UUID>` where they can set a new password.
- The reset page shows the code as a read-only field, and asks for email, new password, and password confirmation.
- After a successful reset, the teacher can sign in with the new password.
- Teachers who registered exclusively via Google Sign-In receive the same neutral confirmation (no email is sent); they will discover they need Google Sign-In when no email arrives.
- Reset codes expire after 30 minutes. An expired or already-used code shows a clear error and a button to request a new link.

---

## Definition of Done

### `/forgot-password` page (web)
- [x] Route `/forgot-password` implemented in `web/` with a single-field form (email) validated with Zod + RHF.
- [x] Form calls `POST /api/v1/auth/forgot-password` with `{ email }`. No Firebase SDK call for this step.
- [x] The confirmation message is shown on both success and any API error (enumeration protection).
- [x] Validation error shown for invalid email format before the request is made.
- [x] The "¿Olvidaste tu contraseña?" link on `/login` navigates to `/forgot-password`.
- [ ] Tests cover: valid email shows confirmation, invalid format shows field error, API error still shows confirmation.

### `/reset-password` page (web)
- [ ] Route `/reset-password` implemented in `web/`. Reads `code` from `?code=` URL param (not Firebase's `oobCode`).
- [ ] Missing `code` in URL shows error state immediately (no API call needed).
- [ ] Form fields: email, new password, confirm password. Code displayed as read-only.
- [ ] On submit, calls `PUT /api/v1/auth/reset-password?code=<UUID>` with `{ email, password, passwordRepeat }`.
- [ ] `404` or `410` responses show error state with "Solicitar nuevo enlace" button to `/forgot-password`.
- [ ] `422 RESET_CODE_EMAIL_MISMATCH` shows field-level error on the email field.
- [ ] Successful reset (200) shows confirmation and offers link to `/login`.
- [ ] Passwords mismatch (Zod `.refine()`) shows field error without making API call.
- [ ] Tests cover: no code shows error, valid code shows form, API 404 shows error, API 410 expired shows error, API 410 used shows error, email mismatch shows field error, passwords mismatch shows Zod error, success shows status.
- [ ] All Firebase SDK imports for password reset removed from this page.

### API (`api/`)
- [ ] `POST /api/v1/auth/forgot-password` implemented and publicly accessible (no auth required).
- [ ] `PUT /api/v1/auth/reset-password?code=<UUID>` implemented and publicly accessible.
- [ ] `V5__add_password_reset_codes.sql` migration applied cleanly.
- [ ] Token TTL: 30 minutes. Upsert semantics: new request replaces existing code for the same teacher.
- [ ] Google-only teachers silently skipped (no email, no error, same 200 response).
- [ ] Error codes `RESET_CODE_NOT_FOUND` (404), `RESET_CODE_EXPIRED` (410), `RESET_CODE_USED` (410), `RESET_CODE_EMAIL_MISMATCH` (422) all verified end-to-end.
- [ ] Firebase Admin SDK `updateUser(uid, {password})` called on successful reset.
- [ ] Custom Thymeleaf HTML email template rendered and sent via JavaMail (SMTP).
- [ ] Unit tests for `PasswordResetService` and `EmailService` pass.
- [ ] Integration tests in `AuthControllerTest` for both endpoints pass.

### Infra
- [ ] SMTP secrets (`SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`) provisioned in Secret Manager for `demo` environment.
- [ ] `GRADEOPS_WEB_BASE_URL` set in Cloud Run service definition for `api/`.
- [ ] `GRADEOPS_MAIL_FROM` and `GRADEOPS_MAIL_FROM_NAME` configured.

---

## Technical Notes

**Architecture:** Custom email service — no Firebase email sending.

- `POST /api/v1/auth/forgot-password` receives the teacher's email, looks them up in the `teachers` table, generates a UUID code with 30-minute TTL, upserts a `password_reset_codes` record, and sends a Thymeleaf HTML email via JavaMail SMTP. Always returns 200.
- `PUT /api/v1/auth/reset-password?code=<UUID>` validates the code (exists, not expired, not used), checks that `body.email` matches the teacher's email, calls `FirebaseAuth.updateUser(uid, {password})` via the Admin SDK, and marks the code as used.
- Reset link in email: `{GRADEOPS_WEB_BASE_URL}/reset-password?code=<UUID>`
- Error codes returned as `{ "error": "CODE" }` body — see API Reference for full table.
- **No Firebase client SDK used in password reset flow.** Firebase Admin SDK is used only server-side.
- React Hook Form + Zod on both web forms per ADR `2026-06-21-form-validation-react-hook-form-zod.md`.
- All DS components: `FieldWithHelper`, `Input` (with `showToggle` on password fields), `Button`, `AppLogo`.
- For local development: configure Mailtrap SMTP credentials in `application-local.yml` or as env vars.

**Previous approach (superseded):** Planning `006-password-recovery` implemented this flow using `sendPasswordResetEmail` (Firebase client SDK) + `verifyPasswordResetCode` / `confirmPasswordReset`. That implementation is replaced by planning `007-password-recovery-custom-email`. The web page structure and layout are preserved; the API calls and Firebase SDK usage are replaced.

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-008 Teacher Self-Registration | Password recovery only applies to email/password accounts |
| US-001 Teacher Login | Recovery flow entry point is the `/login` page |

## Complexity

**Estimate:** M (was S — now includes API backend)
