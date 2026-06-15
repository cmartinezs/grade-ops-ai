# 🔍 DEEPENING: Scope 03 — web-google-sign-in

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Add a "Sign in with Google" button to `/login` and `/register`, implement the `signInWithPopup` flow, call the API upsert endpoint after the first Google sign-in, and update `AuthGuard` to bypass email verification for Google-authenticated users.

---

## Tasks

| # | Task | Area | Status | Output |
|---|------|------|--------|--------|
| 1 | Create `GoogleSignInButton` component using `signInWithPopup(auth, new GoogleAuthProvider())` | `web/` | DONE | `src/components/auth/GoogleSignInButton.tsx` |
| 2 | Add `GoogleSignInButton` alongside email/password form in `/login` page | `web/` | DONE | `src/app/login/page.tsx` |
| 3 | Add `GoogleSignInButton` alongside email/password form in `/register` page | `web/` | DONE | `src/app/register/page.tsx` |
| 4 | After Google sign-in, call `registerTeacher(idToken, displayName)` to upsert teacher record in API | `web/` | DONE | Inside `GoogleSignInButton.handleClick` |
| 5 | Update `AuthGuard` to bypass `/verify-email` redirect for Google-authenticated users (`providerId === 'google.com'`) | `web/` | DONE | `src/components/auth/AuthGuard.tsx` |
| 6 | Write tests: Google sign-in button renders, calls provider, handles errors; AuthGuard bypasses verification for Google users | `web/` | DONE | `GoogleSignInButton.test.tsx` (4 tests), `AuthGuard.test.tsx` (+1 test) |

---

## Done Criteria

- [x] "Sign in with Google" button visible on `/login` and `/register` alongside the existing email/password form.
- [x] Successful Google sign-in creates/retrieves teacher record via API and redirects to `/dashboard`.
- [x] Google-authenticated users are never redirected to `/verify-email`.
- [x] Error states (popup closed, network failure) display a clear message in the UI.
- [x] Existing email/password login and registration tests pass without modification.
- [x] `auth/operation-not-allowed` error code handled in Google sign-in error map.
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
