# 🔍 DEEPENING: Scope 02 — web-auth-ux-polish

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md)

---

## Objective

Fix auth flow independence, COOP popup warning, Google button spec compliance, and error messages for edge cases (email-already-in-use with Google account, wrong-password on Google-only account).

---

## Tasks

| # | Task | Area | Status | Output |
|---|------|------|--------|--------|
| 1 | Make `GoogleSignInButton` a pure auth component — move `registerTeacher` + redirect to `onSuccess` callbacks in each page | `web/` | DONE | `GoogleSignInButton.tsx`, `login/page.tsx`, `register/page.tsx` |
| 2 | Split Firebase error handling from `onSuccess` error handling (two-phase try/catch) | `web/` | DONE | `GoogleSignInButton.tsx` |
| 3 | Fix Google button to match Google spec: `h-10`, `border-[#dadce0]`, `text-[#3c4043]`, 18px G logo | `web/` | DONE | `GoogleSignInButton.tsx` |
| 4 | Derive loading text from label: "Sign up with Google" → "Signing up…", default → "Signing in…" | `web/` | DONE | `GoogleSignInButton.tsx` |
| 5 | Add `Cross-Origin-Opener-Policy: same-origin-allow-popups` header in `next.config.ts` to fix Firebase popup COOP warning | `web/` | DONE | `next.config.ts` |
| 6 | Improve `auth/email-already-in-use` message on `/register` to mention Google | `web/` | DONE | `register/page.tsx` |
| 7 | Improve `auth/invalid-credential` and `auth/wrong-password` messages on `/login` to mention Google sign-in option | `web/` | DONE | `login/page.tsx` |
| 8 | Update `GoogleSignInButton` tests to cover two-phase error handling and loading text variants | `web/` | DONE | `GoogleSignInButton.test.tsx` |

---

## Done Criteria

- [x] `/login` and `/register` each control their own post-auth redirect independently.
- [x] Backend errors from `onSuccess` show "Something went wrong. Please try again later." — not a Firebase-specific message.
- [x] Google button matches Google's official design spec.
- [x] No COOP browser warning when `signInWithPopup` opens.
- [x] User who registered with Google and tries email/password on `/register` sees a message mentioning Google.
- [x] User who registered with Google and tries email/password on `/login` sees a message pointing to the Google button.

---

## Key Decision

`signInWithRedirect` was tried as an alternative to fix the COOP warning but requires Firebase Hosting deployed on `authDomain` (causes `/__/firebase/init.json 404` otherwise). Reverted to `signInWithPopup` with the COOP header fix.
