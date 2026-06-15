# ⚛️ TASK 03 — Protected Route Wrapper (web/)

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-02
> [← scope file](../scope-01-teacher-login.md)

---

## Objective

Implement an `AuthGuard` component (or Next.js middleware) in `web/` that redirects unauthenticated users to `/login` when they attempt to access any protected route.

---

## Technical Design

- **Approach:** Next.js App Router middleware (`web/src/middleware.ts`) checking the Firebase auth state via a server-side session cookie or client-side auth check. For MVP, use a client-side approach: a `<AuthGuard>` client component that wraps protected layouts, checks `onAuthStateChanged`, and redirects to `/login` if the user is null. Alternatively, use Next.js `middleware.ts` with a lightweight token check (check for the presence of a Firebase session token in cookies). The client-side `AuthGuard` approach is simpler and avoids server-side Firebase SDK in the middleware edge runtime.
- **Affected files / components:**
  - `web/src/components/auth/AuthGuard.tsx` (new — client component)
  - `web/src/app/(protected)/layout.tsx` (new or updated — wraps all protected pages with `<AuthGuard>`)
- **Interfaces / contracts:** Wraps all routes under `app/(protected)/`. Redirects to `/login?from=<path>` if unauthenticated. `from` param used for post-login redirect (optional MVP hardening).
- **Design notes:** Use Firebase `onAuthStateChanged` to reactively detect auth state. Show a loading skeleton during the auth check to avoid flash of protected content. Also redirect to `/verify-email` if user is authenticated but `email_verified = false` (complements scope-09).

---

## Implementation Steps

1. Create `web/src/components/auth/AuthGuard.tsx` (`"use client"`):
   - Subscribes to `onAuthStateChanged(auth, callback)`.
   - Loading state: renders spinner/skeleton.
   - `user === null`: `router.replace('/login')`.
   - `user.emailVerified === false`: `router.replace('/verify-email')`.
   - Otherwise: renders `children`.
2. Create `web/src/app/(protected)/layout.tsx` wrapping content with `<AuthGuard>`.
3. Move `web/src/app/dashboard/` under `web/src/app/(protected)/dashboard/` (or add guard at the dashboard layout level).
4. Write `AuthGuard.test.tsx`.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Unauthenticated user hits `/dashboard` | Redirects to `/login` | `AuthGuard.test.tsx` |
| 2 | Authenticated but unverified user | Redirects to `/verify-email` | `AuthGuard.test.tsx` |
| 3 | Authenticated and verified user | Renders children normally | `AuthGuard.test.tsx` |
| 4 | Auth state loading | Renders spinner, no redirect | `AuthGuard.test.tsx` |

---

## Done Criteria

- [x] `web/src/components/auth/AuthGuard.tsx` exists.
- [x] Unauthenticated access to any protected route redirects to `/login`.
- [x] Authenticated but unverified access redirects to `/verify-email`.
- [x] Loading state shows a spinner, not a flash of protected content.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-01-teacher-login.md)
