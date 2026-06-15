# ⚛️ TASK 01 — email_verified Filter in api/

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-09-email-verification.md)

---

## Objective

Implement a Spring filter in `api/` that rejects requests with a Firebase ID token where `email_verified = false`, returning 401 — except for the whitelisted endpoints `/auth/register` and `/auth/verify/resend`.

---

## Technical Design

- **Approach:** Extend `OncePerRequestFilter` as `EmailVerifiedFilter`. Runs after `FirebaseTokenFilter` (scope-01 task-01), which already validates the token and places the decoded token in the security context. This filter reads the decoded token's `email_verified` claim; if `false` and the path is not whitelisted, it returns HTTP 401 with a JSON body `{"error": "EMAIL_NOT_VERIFIED"}`. Whitelist: `/auth/register`, `/auth/verify/resend`, and any public path (no token = not this filter's concern).
- **Affected files / components:**
  - `api/src/main/java/com/gradeops/api/security/EmailVerifiedFilter.java` (new)
  - `api/src/main/java/com/gradeops/api/security/SecurityConfig.java` (register filter after `FirebaseTokenFilter`)
- **Interfaces / contracts:** Returns 401 `{"error": "EMAIL_NOT_VERIFIED"}` for authenticated requests from unverified teachers. Downstream web/ intercepts this to show the verification screen.
- **Design notes:** The filter only acts on authenticated requests (token present and valid). Unauthenticated requests (no token) pass through — they'll be rejected by the authentication layer. Order matters: `EmailVerifiedFilter` must run after `FirebaseTokenFilter`.

---

## Implementation Steps

1. Create `api/src/main/java/com/gradeops/api/security/EmailVerifiedFilter.java` extending `OncePerRequestFilter`:
   - Read `FirebaseToken` from the security context (set by `FirebaseTokenFilter`).
   - If token is null (unauthenticated request), skip.
   - If `token.isEmailVerified()` is `false` and path not in whitelist, write 401 JSON response.
2. Register `EmailVerifiedFilter` in `SecurityConfig` after `FirebaseTokenFilter` in the filter chain.
3. Add `/auth/verify/resend` to the public paths in `SecurityConfig`.
4. Write `EmailVerifiedFilterTest.java`.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Unverified token on protected endpoint | HTTP 401 `{"error": "EMAIL_NOT_VERIFIED"}` | `EmailVerifiedFilterTest` |
| 2 | Verified token on protected endpoint | Request proceeds normally | `EmailVerifiedFilterTest` |
| 3 | Unverified token on `/auth/verify/resend` | Request proceeds (whitelisted) | `EmailVerifiedFilterTest` |
| 4 | No token (unauthenticated) | Filter skips; auth layer handles it | `EmailVerifiedFilterTest` |

---

## Done Criteria

- [x] `EmailVerifiedFilter.java` exists and is registered in the filter chain.
- [x] Unverified token on protected endpoint → 401 `EMAIL_NOT_VERIFIED`.
- [x] Whitelisted paths (`/auth/register`, `/auth/verify/resend`) pass through.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-09-email-verification.md)
