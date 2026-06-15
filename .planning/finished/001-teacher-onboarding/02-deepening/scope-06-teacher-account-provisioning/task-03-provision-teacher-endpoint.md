# ⚛️ TASK 03 — POST /internal/teachers Endpoint

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02
> [← scope file](../scope-06-teacher-account-provisioning.md)

---

## Objective

Implement `POST /internal/teachers` in `api/` that atomically creates a Firebase Auth user and a `teacher` DB record, delivers an invite link, and rejects duplicate emails with a clear error and no partial records.

---

## Technical Design

- **Approach:** `InternalTeacherController` → `ProvisionTeacherService` → `FirebaseAuth.createUser()` + `TeacherRepository.save()` in a `@Transactional` method. If the Firebase call succeeds but the DB save fails, roll back by calling `FirebaseAuth.deleteUser(uid)` in the catch block. Return the Firebase-generated invite/password-reset link via `FirebaseAuth.generatePasswordResetLink()`. Secure the `/internal/**` path with a shared-secret header (`X-Internal-Key`) validated in a `OncePerRequestFilter`.
- **Affected files / components:**
  - `api/src/main/java/com/gradeops/api/internal/teacher/InternalTeacherController.java` (new)
  - `api/src/main/java/com/gradeops/api/internal/teacher/ProvisionTeacherService.java` (new)
  - `api/src/main/java/com/gradeops/api/internal/teacher/ProvisionTeacherRequest.java` (new — `name`, `email`)
  - `api/src/main/java/com/gradeops/api/internal/teacher/ProvisionTeacherResponse.java` (new — `firebaseUid`, `inviteLink`)
  - `api/src/main/java/com/gradeops/api/domain/teacher/TeacherEntity.java` (new — JPA entity for `teacher` table)
  - `api/src/main/java/com/gradeops/api/domain/teacher/TeacherRepository.java` (new — `JpaRepository<TeacherEntity, String>`)
  - `api/src/main/java/com/gradeops/api/security/InternalAuthFilter.java` (new — shared-secret header filter)
  - `api/src/main/resources/application.yml` — add `app.internal.secret` property
- **Interfaces / contracts:** `POST /internal/teachers` body: `{"name": "...", "email": "..."}`. Response 201: `{"firebaseUid": "...", "inviteLink": "..."}`. Error 409 on duplicate email.
- **Design notes:** Operator-provisioned accounts are created with `emailVerified = true` (Admin SDK `UserRecord.EmailVerifiedField`) per the ADR decision. The `@Transactional` boundary covers only the DB operation; Firebase calls are compensated manually on failure (not XA). Internal endpoints are not exposed publicly — Cloud Run ingress is `internal` for the `api/` service.

---

## Implementation Steps

1. Create `TeacherEntity.java` (`@Entity`, `@Table(name="teacher")`, `firebaseUid` as `@Id`).
2. Create `TeacherRepository.java` extending `JpaRepository<TeacherEntity, String>`.
3. Create `ProvisionTeacherRequest.java` and `ProvisionTeacherResponse.java` records.
4. Create `ProvisionTeacherService.java`:
   - `provision(request)`: calls `FirebaseAuth.createUser(new UserRecord.CreateRequest().setEmail().setEmailVerified(true))`, then `TeacherRepository.save(new TeacherEntity(uid, name, email))`. On DB failure, calls `FirebaseAuth.deleteUser(uid)` and rethrows.
   - Catches `FirebaseAuthException` with `EMAIL_ALREADY_EXISTS` code → throw `DuplicateEmailException` mapped to 409.
5. Create `InternalTeacherController.java` — `@RestController`, `@PostMapping("/internal/teachers")`, delegates to service.
6. Create `InternalAuthFilter.java` — checks `X-Internal-Key` header against `${app.internal.secret}`, returns 403 if missing/wrong.
7. Register filter in `SecurityConfig` for `/internal/**`.
8. Write integration test `ProvisionTeacherControllerTest.java`.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Valid request creates Firebase user + teacher record | HTTP 201, `firebaseUid` non-null, teacher row in DB | `ProvisionTeacherControllerTest` |
| 2 | Duplicate email | HTTP 409, no new DB record, no orphaned Firebase user | `ProvisionTeacherControllerTest` |
| 3 | Missing `X-Internal-Key` header | HTTP 403 | `ProvisionTeacherControllerTest` |
| 4 | Firebase failure → no DB record | DB has no teacher row for that email | `ProvisionTeacherServiceTest` (mock FirebaseAuth) |

---

## Done Criteria

- [ ] `POST /internal/teachers` returns 201 with `firebaseUid` and `inviteLink` on success.
- [ ] Duplicate email returns 409 with no partial records in DB or Firebase.
- [ ] Missing `X-Internal-Key` returns 403.
- [ ] Provisioned account has `emailVerified = true` in Firebase.
- [ ] All unit tests listed above pass.
- [ ] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-06-teacher-account-provisioning.md)
