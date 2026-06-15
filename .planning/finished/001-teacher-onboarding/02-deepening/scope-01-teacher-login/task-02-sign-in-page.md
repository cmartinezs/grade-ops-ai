# ⚛️ TASK 02 — Sign-In Page (web/)

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-01-teacher-login.md)

---

## Objective

Implement `web/src/app/login/page.tsx` — an email/password sign-in form that calls Firebase `signInWithEmailAndPassword`, checks `email_verified`, and redirects to `/dashboard` or `/verify-email` accordingly.

---

## Technical Design

- **Approach:** Client component. Uses Firebase `signInWithEmailAndPassword`. After sign-in, checks `user.emailVerified`: if false, redirects to `/verify-email`; if true, redirects to `/dashboard`. Shows clear error messages for `auth/wrong-password` and `auth/user-not-found`. Provides a link to `/register`.
- **Affected files / components:**
  - `web/src/app/login/page.tsx` (new)
  - `web/src/lib/firebase/client.ts` (reuses from scope-08 task-02)
- **Interfaces / contracts:** Navigates to `/dashboard` on success (verified) or `/verify-email` on success (unverified). Does not call `api/` directly — the ID token is stored in the Firebase SDK and sent as `Authorization: Bearer` by `web/src/lib/api/client.ts` on subsequent API calls.
- **Design notes:** Never store the ID token in `localStorage`. Firebase SDK manages the token lifecycle in IndexedDB (default persistence). The token is retrieved via `user.getIdToken()` when needed for API calls.

---

## Implementation Steps

1. Create `web/src/app/login/page.tsx` (`"use client"`):
   - Form: email + password fields.
   - On submit: call `signInWithEmailAndPassword(auth, email, password)`.
   - If `user.emailVerified === false` → `router.push('/verify-email')`.
   - If `user.emailVerified === true` → `router.push('/dashboard')`.
   - Error states: `auth/wrong-password` → "Incorrect password", `auth/user-not-found` → "No account found".
2. Add link to `/register` for new users.
3. Write `SignInPage.test.tsx` with React Testing Library.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Valid credentials, verified email | Calls `signInWithEmailAndPassword`, redirects to `/dashboard` | `SignInPage.test.tsx` |
| 2 | Valid credentials, unverified email | Redirects to `/verify-email` | `SignInPage.test.tsx` |
| 3 | Wrong password | Shows "Incorrect password" error, stays on page | `SignInPage.test.tsx` |
| 4 | User not found | Shows "No account found" error | `SignInPage.test.tsx` |

---

## Done Criteria

- [x] `web/src/app/login/page.tsx` exists and renders the sign-in form.
- [x] Successful sign-in redirects to `/dashboard` (verified) or `/verify-email` (unverified).
- [x] Error messages shown for wrong password and unknown email.
- [x] ID token is not stored in `localStorage`.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-01-teacher-login.md)
