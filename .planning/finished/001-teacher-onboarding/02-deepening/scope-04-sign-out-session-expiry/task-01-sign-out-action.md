# ⚛️ TASK 01 — Sign-Out Action (web/)

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-04-sign-out-session-expiry.md)

---

## Objective

Implement a sign-out action in `web/` that calls Firebase `signOut()`, triggers refresh-token revocation on the `api/`, and redirects to `/login`.

---

## Technical Design

- **Approach:** A `SignOutButton` client component calls Firebase `auth.signOut()` (clears client session), then calls `POST /auth/sign-out` on the `api/` (which calls `Admin SDK revokeRefreshTokens(uid)` — per the ADR session expiry decision). After both calls (or if the API call fails, still redirect), `router.replace('/login')`. The revocation call is best-effort: if the API is unavailable, the client session is still cleared and the token expires in ≤1 h naturally.
- **Affected files / components:**
  - `web/src/components/auth/SignOutButton.tsx` (new — client component)
  - `web/src/lib/api/auth.ts` (update — add `signOut()` API call: `POST /auth/sign-out`)
  - `api/src/main/java/com/gradeops/api/auth/AuthController.java` (update — add `POST /auth/sign-out` handler that calls `firebaseAuth.revokeRefreshTokens(uid)`)
- **Interfaces / contracts:** `POST /auth/sign-out` is authenticated (requires valid token). Returns 204 on success. `SignOutButton` renders a button; placement in the workspace layout is a layout concern.
- **Design notes:** The API call is made before clearing the client session so the token is still valid when sent. Firebase `signOut()` is called after the API call returns (or times out after 3 s). If API call fails, proceed with client sign-out anyway.

---

## Implementation Steps

1. Add `signOut()` to `web/src/lib/api/auth.ts`: `POST /auth/sign-out` with current ID token.
2. Create `web/src/components/auth/SignOutButton.tsx` (`"use client"`):
   - On click: get current ID token, call `signOut()` API, call `auth.signOut()`, `router.replace('/login')`.
   - Show loading state during the call.
3. Add `POST /auth/sign-out` handler to `api/src/main/java/com/gradeops/api/auth/AuthController.java`:
   - Reads `AuthenticatedTeacher` from security context.
   - Calls `firebaseAuth.revokeRefreshTokens(uid)`.
   - Returns 204.
4. Write `SignOutButton.test.tsx` and update `AuthControllerTest.java` with the new endpoint.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Sign-out button clicked | Calls API `/auth/sign-out`, then `auth.signOut()`, redirects to `/login` | `SignOutButton.test.tsx` |
| 2 | API call fails | Still calls `auth.signOut()` and redirects | `SignOutButton.test.tsx` |
| 3 | `POST /auth/sign-out` with valid token | HTTP 204; `revokeRefreshTokens` called | `AuthControllerTest` |
| 4 | `POST /auth/sign-out` unauthenticated | HTTP 401 | `AuthControllerTest` |

---

## Done Criteria

- [x] `SignOutButton.tsx` exists and performs the full sign-out sequence.
- [x] Client session is always cleared (even if API call fails).
- [x] `POST /auth/sign-out` in `api/` calls `revokeRefreshTokens` and returns 204.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-04-sign-out-session-expiry.md)
