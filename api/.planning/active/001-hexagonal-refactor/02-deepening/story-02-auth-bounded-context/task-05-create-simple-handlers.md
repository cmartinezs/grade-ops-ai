# ⚛️ TASK 05 — Create RegisterHandler + SignOutHandler + RevokeRefreshTokensHandler + unit tests

> **Status:** TODO
> **Workflow:** IMPLEMENT
> **Depends On:** task-01, task-03, task-04
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Create the three auth handlers derived from `AuthService`: `RegisterHandler`, `SignOutHandler`, and `RevokeRefreshTokensHandler`. Each is `@RequiredArgsConstructor` (no Spring stereotype), annotated with `@Transactional` only on the method, implements its port.in interface, and is declared as `@Bean` in `AuthConfig` (created in task-12). Write unit tests for all three.

---

## Technical Design

- **Approach:** `RegisterHandler` extracts `AuthService.register()` logic verbatim but wires to new types. It temporarily injects `domain.teacher.TeacherRepository` (the old JPA Spring Data interface) because `TeacherRepositoryPort` does not yet exist — Story 03 will swap this import when it creates `TeacherPersistenceAdapter`. The ArchUnit rule `application has no infrastructure imports` will not fire here because `TeacherRepository` is in `domain.teacher.*`, not `..infrastructure..` (it moves to infra in Story 03, at which point ArchUnit will enforce the swap). `SignOutHandler.execute(uid)` delegates to `RevokeRefreshTokensUseCase` by interface — it does not call `AuthPort` directly. `RevokeRefreshTokensHandler` is the atomic unit that wraps `AuthPort.revokeRefreshTokens`.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/application/usecase/RegisterHandler.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/application/usecase/SignOutHandler.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/application/usecase/RevokeRefreshTokensHandler.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/application/usecase/RegisterHandlerTest.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/application/usecase/SignOutHandlerTest.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/application/usecase/RevokeRefreshTokensHandlerTest.java` ← NEW
- **Interfaces / contracts:**
  - `RegisterHandler` implements `RegisterUseCase`; injects `AuthPort` + `domain.teacher.TeacherRepository`.
  - `SignOutHandler` implements `SignOutUseCase`; injects `RevokeRefreshTokensUseCase`.
  - `RevokeRefreshTokensHandler` implements `RevokeRefreshTokensUseCase`; injects `AuthPort`.
- **Design notes:** `RegisterHandler.execute(RegisterCommand)` must replicate the name-resolution logic from `AuthService.resolveNames()` — inline it or keep as a private method. `InvalidTokenException` thrown by Firebase token verification is caught and re-thrown as `shared.infrastructure.adapter.in.web.InvalidTokenException` (moved there in Story 01). `RegisterHandler` has `@Transactional` on `execute()` because it writes to `TeacherRepository`. `SignOutHandler` has `@Transactional` on `execute()` even though it only revokes tokens. None of the three handlers carry any Spring stereotype annotation (`@Service`, `@Component`); they are declared as `@Bean` in `AuthConfig` (created in task-12).

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/auth/application/usecase/`.
2. Create `RevokeRefreshTokensHandler.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.usecase;

   import cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase;
   import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
   import lombok.RequiredArgsConstructor;
   import org.springframework.transaction.annotation.Transactional;

   @RequiredArgsConstructor
   public class RevokeRefreshTokensHandler implements RevokeRefreshTokensUseCase {

       private final AuthPort authPort;

       @Override
       @Transactional
       public void execute(String uid) {
           authPort.revokeRefreshTokens(uid);
       }
   }
   ```
3. Create `SignOutHandler.java`:
   ```java
   package cl.gradeops.ai.api.auth.application.usecase;

   import cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase;
   import cl.gradeops.ai.api.auth.application.port.in.SignOutUseCase;
   import lombok.RequiredArgsConstructor;
   import org.springframework.transaction.annotation.Transactional;

   @RequiredArgsConstructor
   public class SignOutHandler implements SignOutUseCase {

       private final RevokeRefreshTokensUseCase revokeRefreshTokensUseCase;

       @Override
       @Transactional
       public void execute(String uid) {
           revokeRefreshTokensUseCase.execute(uid);
       }
   }
   ```
4. Create `RegisterHandler.java` — extracts `AuthService.register()` logic, using `auth.domain.model.TeacherIdentity` (moved in task-01) and `auth.shared.infrastructure.adapter.in.web.InvalidTokenException` (moved in Story 01):
   - Injects `AuthPort` and `domain.teacher.TeacherRepository` (old JPA repo — temporary until Story 03).
   - The `resolveNames` private method is copied from `AuthService`.
   - Throws `shared.infrastructure.adapter.in.web.InvalidTokenException` on bad token.
   - Creates `new domain.teacher.TeacherEntity(uid, firstName, lastName, email, provider)` for persistence (old JPA entity — temporary until Story 03).
5. Create unit test directory `src/test/java/cl/gradeops/ai/api/auth/application/usecase/`.
6. Create `RevokeRefreshTokensHandlerTest.java`:
   - Mocks `AuthPort`; verifies `authPort.revokeRefreshTokens("uid-1")` is called on `execute("uid-1")`.
7. Create `SignOutHandlerTest.java`:
   - Mocks `RevokeRefreshTokensUseCase`; verifies `revokeRefreshTokensUseCase.execute("uid-1")` is called.
8. Create `RegisterHandlerTest.java`:
   - Mock `AuthPort` + `TeacherRepository`.
   - Test: existing teacher → returns `RegisterResult(uid, false)`, no `save` called.
   - Test: new teacher → returns `RegisterResult(uid, true)`, `save` called with correct name.
   - Test: invalid token → `authPort.verifyTokenUnchecked` throws `IllegalArgumentException` → handler throws `InvalidTokenException`.
9. Run `./mvnw test -Dtest=RegisterHandlerTest,SignOutHandlerTest,RevokeRefreshTokensHandlerTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `RevokeRefreshTokensHandler` delegates to `AuthPort.revokeRefreshTokens` | Mock + verify call |
| 2 | `SignOutHandler` delegates to `RevokeRefreshTokensUseCase` (not `AuthPort` directly) | Mock use case interface + verify |
| 3 | `RegisterHandler` returns `created=false` for existing teacher | `existsById` returns `true` → result.created is false |
| 4 | `RegisterHandler` saves new teacher and returns `created=true` | `existsById` returns `false` → `save` is called, result.created is true |
| 5 | `RegisterHandler` throws `InvalidTokenException` on bad Firebase token | `verifyTokenUnchecked` throws `IllegalArgumentException` → handler throws `InvalidTokenException` |

---

## Done Criteria

- [ ] `RevokeRefreshTokensHandler`, `SignOutHandler`, `RegisterHandler` exist in `auth/application/usecase/`
- [ ] All three are `@RequiredArgsConstructor` with NO `@Service`/`@Component` annotation — declared as `@Bean` in `AuthConfig`
- [ ] `SignOutHandler` injects `RevokeRefreshTokensUseCase` (not `AuthPort`) — enforces single-responsibility
- [ ] `RegisterHandler` throws `InvalidTokenException` (from `shared.infrastructure.adapter.in.web`) on bad token
- [ ] All 3 unit test classes pass
- [ ] `./mvnw test -Dtest=RegisterHandlerTest,SignOutHandlerTest,RevokeRefreshTokensHandlerTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-02-auth-bounded-context.md)
