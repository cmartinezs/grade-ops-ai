# ⚛️ TASK 02 — Centralized Auth Error Interceptor (web/)

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-04-sign-out-session-expiry.md)

---

## Objective

Implement a centralized API fetch wrapper in `web/` that intercepts HTTP 401 responses from `api/` and redirects to `/login` with a clear "Session expired" message, without losing the user's current path.

---

## Technical Design

- **Approach:** Create `web/src/lib/api/client.ts` — a typed fetch wrapper that attaches the `Authorization: Bearer <idToken>` header to every API call and intercepts 401 responses. On 401 (generic): fires `auth.signOut()` and `router.replace('/login?reason=expired')`. On 401 `EMAIL_NOT_VERIFIED`: redirects to `/verify-email` instead. The `/login` page reads `reason=expired` to display a "Your session has expired. Please sign in again." banner. This prevents silent session loss.
- **Affected files / components:**
  - `web/src/lib/api/client.ts` (new — central fetch wrapper)
  - `web/src/app/login/page.tsx` (update — read `?reason=expired` to show banner; scope-01 task-02 created this page)
- **Interfaces / contracts:** All API calls in `web/` use this client (`getAssessments`, `registerTeacher`, etc.). The wrapper auto-attaches the ID token by calling `user.getIdToken()` before each request.
- **Design notes:** Getting the current user and token is async — the wrapper must be async. Avoid caching the token between calls; Firebase SDK already caches and refreshes it internally. The "Session expired" message must not be shown on intentional sign-out (scope-04 task-01 calls `router.replace('/login')` without `?reason=expired`).

---

## Implementation Steps

1. Create `web/src/lib/api/client.ts`:
   - Export `apiClient(path, options?)` async function.
   - Gets `auth.currentUser`, calls `getIdToken()`, adds `Authorization: Bearer` header.
   - Calls `fetch(API_BASE_URL + path, options)`.
   - If response is 401 with body `{"error": "EMAIL_NOT_VERIFIED"}` → `router.replace('/verify-email')`.
   - If response is 401 (other) → `auth.signOut()`, `router.replace('/login?reason=expired')`.
   - Returns the response for non-401 cases.
2. Update `web/src/lib/api/assessments.ts` and `auth.ts` to use `apiClient` instead of raw `fetch`.
3. Update `web/src/app/login/page.tsx` to read `searchParams.reason` and display an expiry banner if `reason === 'expired'`.
4. Write `apiClient.test.ts`.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | API returns 401 (expired) | `auth.signOut()` called; router redirects to `/login?reason=expired` | `apiClient.test.ts` |
| 2 | API returns 401 `EMAIL_NOT_VERIFIED` | Router redirects to `/verify-email` | `apiClient.test.ts` |
| 3 | API returns 200 | Response returned normally | `apiClient.test.ts` |
| 4 | Login page with `?reason=expired` | Banner "Your session has expired" shown | `SignInPage.test.tsx` |

---

## Done Criteria

- [x] `web/src/lib/api/client.ts` exists and attaches `Authorization` header to all requests.
- [x] 401 (expired) → `signOut()` + redirect to `/login?reason=expired`.
- [x] 401 `EMAIL_NOT_VERIFIED` → redirect to `/verify-email`.
- [x] Login page shows "session expired" banner when `?reason=expired` param is present.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-04-sign-out-session-expiry.md)
