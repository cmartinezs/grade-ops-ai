# ⚛️ TASK 04 — Extend AuthPort with createUser + deleteUser; implement in FirebaseAuthAdapter

> **Status:** TODO
> **Workflow:** IMPLEMENT
> **Depends On:** (none — pure addition to existing auth context)
> [← story file](../story-03-teacher-bounded-context.md)

---

## Objective

Add `createUser(String email, String displayName) → String uid` and `deleteUser(String uid) → void` to `auth.application.port.out.AuthPort`. Implement both in `FirebaseAuthAdapter`. The adapter maps `FirebaseAuthException.EMAIL_ALREADY_EXISTS` to `DuplicateEmailException`. Add 3 new test cases to `FirebaseAuthAdapterTest`.

---

## Technical Design

- **Approach:** `AuthPort` is extended with 2 methods needed by `ProvisionTeacherHandler` (task-05). The implementation lives in `FirebaseAuthAdapter`. `createUser` wraps `FirebaseAuth.createUser(CreateRequest)` — the adapter catches `FirebaseAuthException` with error code `EMAIL_ALREADY_EXISTS` and throws `DuplicateEmailException` (from `shared.domain.exception`) so callers don't need to know Firebase error codes. `deleteUser` wraps `FirebaseAuth.deleteUser(uid)` and re-wraps any exception as `RuntimeException` (compensation best-effort — caller already has the real exception). `FirebaseAuthAdapter` is not annotated — it's declared as `@Bean` in `AuthConfig`.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/application/port/out/AuthPort.java` ← MODIFIED (add 2 methods)
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/firebase/FirebaseAuthAdapter.java` ← MODIFIED (implement 2 methods)
  - `src/test/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/firebase/FirebaseAuthAdapterTest.java` ← MODIFIED (add 3 cases)
- **Interfaces / contracts:**
  ```java
  String createUser(String email, String displayName);
  void deleteUser(String uid);
  ```

---

## Implementation Steps

1. Add 2 method signatures to `auth/application/port/out/AuthPort.java`:
   ```java
   String createUser(String email, String displayName);
   void deleteUser(String uid);
   ```
2. Implement `createUser` in `FirebaseAuthAdapter`:
   ```java
   @Override
   public String createUser(String email, String displayName) {
       try {
           UserRecord.CreateRequest req = new UserRecord.CreateRequest()
               .setEmail(email)
               .setDisplayName(displayName)
               .setEmailVerified(true);
           return firebaseAuth.createUser(req).getUid();
       } catch (FirebaseAuthException ex) {
           if (com.google.firebase.auth.AuthErrorCode.EMAIL_ALREADY_EXISTS
                   .equals(ex.getAuthErrorCode())) {
               throw new DuplicateEmailException(email);
           }
           throw new RuntimeException("Firebase user creation failed", ex);
       }
   }
   ```
   Add import: `cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException`.
3. Implement `deleteUser` in `FirebaseAuthAdapter`:
   ```java
   @Override
   public void deleteUser(String uid) {
       try {
           firebaseAuth.deleteUser(uid);
       } catch (FirebaseAuthException ex) {
           throw new RuntimeException("Failed to delete Firebase user: " + uid, ex);
       }
   }
   ```
4. Add 3 test cases to `FirebaseAuthAdapterTest`:
   - `createUser_happy_path_returns_uid`: mock `firebaseAuth.createUser(any())` → returns mock `UserRecord` with `getUid() = "uid-abc"`; assert result equals `"uid-abc"`.
   - `createUser_duplicate_email_throws_DuplicateEmailException`: mock `firebaseAuth.createUser(any())` → throws `FirebaseAuthException` with `getAuthErrorCode() = EMAIL_ALREADY_EXISTS`; assert `DuplicateEmailException` thrown.
   - `deleteUser_delegates_to_firebase`: verify `firebaseAuth.deleteUser("uid-abc")` called when `adapter.deleteUser("uid-abc")` is invoked.
5. Run `./mvnw test -Dtest=FirebaseAuthAdapterTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `createUser` returns Firebase UID on success | Mock `firebaseAuth.createUser` → assert returned string equals mock UID |
| 2 | `createUser` maps `EMAIL_ALREADY_EXISTS` → `DuplicateEmailException` | Mock throws `FirebaseAuthException` with that code; assert `DuplicateEmailException` |
| 3 | `deleteUser` delegates to `firebaseAuth.deleteUser` | Mockito `verify(firebaseAuth).deleteUser("uid-abc")` |
| 4 | Existing 7 adapter tests still pass | `./mvnw test -Dtest=FirebaseAuthAdapterTest -q` exits 0 with 10 tests passing |

---

## Done Criteria

- [ ] `AuthPort` declares `String createUser(String email, String displayName)` and `void deleteUser(String uid)`
- [ ] `FirebaseAuthAdapter` implements both methods
- [ ] `createUser` throws `DuplicateEmailException` on `EMAIL_ALREADY_EXISTS` — no Firebase error codes exposed to callers
- [ ] `deleteUser` wraps `FirebaseAuthException` as `RuntimeException`
- [ ] 3 new test cases added to `FirebaseAuthAdapterTest`; all 10 cases pass
- [ ] `./mvnw test -Dtest=FirebaseAuthAdapterTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-03-teacher-bounded-context.md)
