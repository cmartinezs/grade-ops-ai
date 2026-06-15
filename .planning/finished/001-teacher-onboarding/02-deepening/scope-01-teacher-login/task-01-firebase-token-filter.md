# ⚛️ TASK 01 — FirebaseTokenFilter in api/

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-01-teacher-login.md)

---

## Objective

Implement `FirebaseTokenFilter` in `api/` — a `OncePerRequestFilter` that extracts and verifies the Firebase ID token from the `Authorization: Bearer` header on every request, placing the authenticated teacher's UID into the Spring Security context.

---

## Technical Design

- **Approach:** `FirebaseTokenFilter extends OncePerRequestFilter`. Reads `Authorization: Bearer <token>`. Calls `FirebaseAuth.verifyIdToken(token, checkRevoked: true)` (per ADR: `checkRevoked` enforces sign-out revocation). On success, creates an `UsernamePasswordAuthenticationToken` with the UID as principal and sets it in `SecurityContextHolder`. On failure (no header, invalid token, revoked token), clears the context and continues — the downstream endpoint's `@PreAuthorize` or role check will return 401/403 as appropriate. This filter is not an authentication entry point; it's a token extractor.
- **Affected files / components:**
  - `api/src/main/java/com/gradeops/api/security/FirebaseTokenFilter.java` (new)
  - `api/src/main/java/com/gradeops/api/security/SecurityConfig.java` (register filter before `UsernamePasswordAuthenticationFilter`)
  - `api/src/main/java/com/gradeops/api/security/AuthenticatedTeacher.java` (new — value object holding `uid` and `email`, stored as principal)
- **Interfaces / contracts:** `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` returns `AuthenticatedTeacher(uid, email)` for authenticated requests. Consumed by controllers and `OwnershipVerifier` (scope-07).
- **Design notes:** `checkRevoked: true` adds one Firebase Admin SDK network call per request. For MVP this is acceptable; cache the result in production. Public paths (`/auth/register`, `/auth/verify/resend`) bypass this filter via `SecurityConfig` `permitAll()`.

---

## Implementation Steps

1. Create `api/src/main/java/com/gradeops/api/security/AuthenticatedTeacher.java` record (`uid`, `email`).
2. Create `api/src/main/java/com/gradeops/api/security/FirebaseTokenFilter.java`:
   - Extract `Authorization: Bearer <token>` header.
   - Call `firebaseAuth.verifyIdToken(token, true)` (checkRevoked).
   - Build `UsernamePasswordAuthenticationToken` with `new AuthenticatedTeacher(uid, email)` as principal.
   - Set in `SecurityContextHolder`.
   - Catch `FirebaseAuthException` → clear context, log at DEBUG.
3. Register `FirebaseTokenFilter` in `SecurityConfig` before `UsernamePasswordAuthenticationFilter`.
4. Ensure `/auth/register` and `/auth/verify/resend` are in `permitAll()`.
5. Write `FirebaseTokenFilterTest.java`.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Valid non-revoked token | `SecurityContextHolder` has `AuthenticatedTeacher` with correct UID | `FirebaseTokenFilterTest` |
| 2 | Revoked token | Context cleared; downstream returns 401 | `FirebaseTokenFilterTest` |
| 3 | Missing Authorization header | Context cleared; public paths return 200, protected return 401 | `FirebaseTokenFilterTest` |
| 4 | Malformed token | Context cleared; no exception propagated | `FirebaseTokenFilterTest` |

---

## Done Criteria

- [x] `FirebaseTokenFilter.java` exists and is registered in `SecurityConfig`.
- [x] Valid token sets `AuthenticatedTeacher` principal in the security context.
- [x] Revoked token clears the context (does not propagate `FirebaseAuthException`).
- [x] `checkRevoked: true` is used in `verifyIdToken`.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-01-teacher-login.md)
