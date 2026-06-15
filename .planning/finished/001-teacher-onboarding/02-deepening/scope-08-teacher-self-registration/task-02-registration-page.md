# ⚛️ TASK 02 — Registration Page (web/)

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← scope file](../scope-08-teacher-self-registration.md)

---

## Objective

Implement `web/src/app/register/page.tsx` — an email/password registration form that calls Firebase `createUserWithEmailAndPassword`, then POSTs the ID token to `POST /auth/register`, with clear error handling for duplicate emails.

---

## Technical Design

- **Approach:** Next.js App Router page. Use the Firebase client SDK (`firebase/auth`) for `createUserWithEmailAndPassword`. On success, get the ID token (`user.getIdToken()`), POST it with the teacher's name to `POST /auth/register` in `api/`. On success, navigate to the email verification pending screen (scope-09). Error states: `auth/email-already-in-use` → show "Email already registered" message.
- **Affected files / components:**
  - `web/src/app/register/page.tsx` (new — registration form)
  - `web/src/lib/firebase/client.ts` (new or updated — `initializeApp` with env vars from scope-10 task-03)
  - `web/src/lib/api/auth.ts` (new — `registerTeacher(idToken, name)` API call)
- **Interfaces / contracts:** Calls `POST /api/auth/register` (proxied from Next.js to `api/` base URL). Navigates to `/verify-email` on success.
- **Design notes:** Firebase `createUserWithEmailAndPassword` must be called on the client side (browser), not in a Server Component. Use a `"use client"` directive. Form validation is minimal for MVP: non-empty email, password ≥ 6 chars (Firebase enforces it). Tailwind CSS for styling.

---

## Implementation Steps

1. Create `web/src/lib/firebase/client.ts`: initialize Firebase app from `NEXT_PUBLIC_FIREBASE_*` env vars; export `getAuth()`.
2. Create `web/src/lib/api/auth.ts`: export `registerTeacher(idToken: string, name: string)` that POSTs to `/api/auth/register` and returns `{ firebaseUid }`.
3. Create `web/src/app/register/page.tsx` (`"use client"`):
   - Form fields: name, email, password.
   - On submit: call `createUserWithEmailAndPassword(auth, email, password)`, then `registerTeacher(idToken, name)`, then `router.push('/verify-email')`.
   - Error display for `auth/email-already-in-use` and generic errors.
4. Add a link to the sign-in page from the registration page.
5. Write a component test with React Testing Library.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Valid registration | Calls `createUserWithEmailAndPassword`, then `registerTeacher`; navigates to `/verify-email` | `RegisterPage.test.tsx` (mock Firebase SDK) |
| 2 | Duplicate email | Displays "Email already registered" error, stays on page | `RegisterPage.test.tsx` |
| 3 | Password too short | Firebase rejects; error message shown | `RegisterPage.test.tsx` |

---

## Done Criteria

- [ ] `web/src/app/register/page.tsx` exists and renders the registration form.
- [ ] Successful registration navigates to `/verify-email`.
- [ ] Duplicate email shows a user-readable error without crashing.
- [ ] Firebase client SDK is initialized from `NEXT_PUBLIC_FIREBASE_*` env vars.
- [ ] All unit tests listed above pass.
- [ ] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-08-teacher-self-registration.md)
