# ⚛️ TASK 01 — POST /auth/register API Endpoint

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-08-teacher-self-registration.md)

---

## Objective

Implement `POST /auth/register` in `api/` that accepts a Firebase ID token from a newly registered user, verifies it, and creates the corresponding `teacher` DB record keyed by Firebase UID.

---

## Technical Design

- **Approach:** `AuthController` with a `register` endpoint. The client (web/) calls Firebase `createUserWithEmailAndPassword` first (client-side), then sends the resulting ID token to `POST /auth/register`. The API verifies the token via `FirebaseAuth.verifyIdToken(token)`, extracts UID, name (from `displayName` or request body), and email, then upserts the `teacher` record. The endpoint is public (no prior auth required). Uses `TeacherRepository` from scope-06 task-01/task-03.
- **Affected files / components:**
  - `api/src/main/java/com/gradeops/api/auth/AuthController.java` (new)
  - `api/src/main/java/com/gradeops/api/auth/RegisterRequest.java` (new — `idToken`, `name`)
  - `api/src/main/java/com/gradeops/api/auth/RegisterResponse.java` (new — `firebaseUid`)
  - `api/src/main/java/com/gradeops/api/auth/AuthService.java` (new — `register(request)`)
  - `api/src/main/java/com/gradeops/api/domain/teacher/TeacherRepository.java` (reuses from scope-06)
  - `api/src/main/java/com/gradeops/api/domain/teacher/TeacherEntity.java` (reuses from scope-06)
- **Interfaces / contracts:** `POST /auth/register` body: `{"idToken": "...", "name": "..."}`. Response 201: `{"firebaseUid": "..."}`. Error 409 if teacher record already exists.
- **Design notes:** This endpoint is on the public path (no auth required). Validates the token to prevent fake registrations. Uses `saveIfAbsent` semantics — if a teacher record already exists for the UID (re-registration attempt), return 200. `emailVerified` status is not checked here — scope-09 handles that gate for subsequent requests. Self-registered accounts start with `emailVerified = false`; verification is triggered client-side by scope-09.

---

## Implementation Steps

1. Create `RegisterRequest.java` record (`idToken`, `name`).
2. Create `RegisterResponse.java` record (`firebaseUid`).
3. Create `AuthService.java` with `register(RegisterRequest)`: calls `FirebaseAuth.verifyIdToken(idToken)`, extracts `uid` and `email`, saves `TeacherEntity` if not already present, returns `firebaseUid`.
4. Create `AuthController.java`: `@RestController`, `@PostMapping("/auth/register")`, delegates to `AuthService`.
5. Add `/auth/register` to the permitted (unauthenticated) paths in `SecurityConfig`.
6. Write `AuthControllerTest.java` integration test.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Valid Firebase ID token | HTTP 201, `firebaseUid` matches token UID, teacher row in DB | `AuthControllerTest` |
| 2 | Invalid / expired token | HTTP 401 | `AuthControllerTest` |
| 3 | Same UID registers twice | HTTP 200 (idempotent), no duplicate DB row | `AuthControllerTest` |

---

## Done Criteria

- [ ] `POST /auth/register` returns 201 on first registration.
- [ ] Invalid token returns 401.
- [ ] Re-registration of same UID is idempotent (200, no error).
- [ ] Teacher record in DB has correct `firebase_uid` and `email`.
- [ ] All unit tests listed above pass.
- [ ] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-08-teacher-self-registration.md)
