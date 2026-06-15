# ⚛️ TASK 02 — Verification Pending Screen and Resend (web/)

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← scope file](../scope-09-email-verification.md)

---

## Objective

Implement `web/src/app/verify-email/page.tsx` — a pending-verification screen that triggers `sendEmailVerification` after registration, shows a clear message, and provides a rate-limited resend button.

---

## Technical Design

- **Approach:** Next.js App Router page, client component. On mount, call `sendEmailVerification(currentUser)` if arriving from registration (use a URL param or session flag). Provide a "Resend email" button that calls `sendEmailVerification` again and is disabled for 60 s after each send (client-side rate limit, displayed as a countdown). Intercept API 401 `EMAIL_NOT_VERIFIED` responses in the global fetch wrapper (from scope-04 task-02) to redirect here if the user somehow reaches a protected page while unverified. On sign-in, check `currentUser.emailVerified` and redirect here if false.
- **Affected files / components:**
  - `web/src/app/verify-email/page.tsx` (new)
  - `web/src/lib/api/client.ts` (add 401 `EMAIL_NOT_VERIFIED` interception — may overlap with scope-04 task-02; coordinate)
  - `web/src/lib/auth/guard.ts` (add `emailVerified` check to protected route guard — scope-01 task-03 will set this up)
- **Interfaces / contracts:** Displayed after `/register` redirect. Responds to `api/` 401 `EMAIL_NOT_VERIFIED`. Disappears when `currentUser.reload()` shows `emailVerified = true`.
- **Design notes:** Firebase's `sendEmailVerification` has its own server-side rate limiting; the client-side 60-s disable is a UX courtesy only. The page must poll or wait for user action (button click) to check `emailVerified` — use `currentUser.reload()` + a "I've verified" button that reloads and redirects to `/dashboard`.

---

## Implementation Steps

1. Create `web/src/app/verify-email/page.tsx` (`"use client"`):
   - Show: "Check your email — we sent a verification link."
   - On mount: call `sendEmailVerification(currentUser)` if `?sent=1` param is absent; set param to prevent re-send on refresh.
   - "Resend email" button: calls `sendEmailVerification`, sets 60-s disable timer.
   - "I've verified my email" button: calls `currentUser.reload()`, if `emailVerified` is now true, `router.push('/dashboard')`.
2. Add redirect to `/verify-email` in `SignInPage` (scope-01 task-02): after sign-in, if `user.emailVerified === false`, redirect here instead of dashboard.
3. Write component tests with React Testing Library (mock Firebase auth).

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Page renders on first visit | `sendEmailVerification` called once, button shown | `VerifyEmailPage.test.tsx` |
| 2 | Resend clicked | `sendEmailVerification` called, button disabled for 60 s | `VerifyEmailPage.test.tsx` |
| 3 | "I've verified" clicked with verified user | `reload()` called, router pushes `/dashboard` | `VerifyEmailPage.test.tsx` |
| 4 | "I've verified" clicked with unverified user | Error message shown, stays on page | `VerifyEmailPage.test.tsx` |

---

## Done Criteria

- [x] `web/src/app/verify-email/page.tsx` exists and renders correctly.
- [x] `sendEmailVerification` is triggered after registration (not on every page refresh).
- [x] Resend button is disabled for 60 s after each send with visible countdown.
- [x] "I've verified" button reloads Firebase user and redirects to `/dashboard` if verified.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-09-email-verification.md)
