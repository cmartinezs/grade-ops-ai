# ⚛️ TASK 11 — Create AuthController + request/response DTOs + migrate controller tests

> **Status:** DONE
> **Workflow:** IMPLEMENT
> **Depends On:** task-03, task-04
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Create `AuthController` in `auth.infrastructure.adapter.in.web` that consolidates all auth HTTP endpoints (register, sign-out, forgot-password, reset-password). Create 4 request/response DTOs. Migrate `AuthControllerTest` and `PasswordResetControllerTest` to the new package pointing at the new controller. Delete the old flat-package controllers.

---

## Technical Design

- **Approach:** The new `AuthController` replaces two old controllers: `AuthController` (old package `auth/`) and `PasswordResetController` (old package `auth/`). All 4 endpoints move into a single new controller class under the hexagonal path. The controller injects use-case interfaces (port.in), not handlers directly. Request/response DTOs are public records in `auth.infrastructure.adapter.in.web` — they're web-layer concerns, not application-layer types. Controller tests use `@WebMvcTest(AuthController.class)` with mocked use-case beans.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/in/web/AuthController.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/in/web/RegisterRequest.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/in/web/ForgotPasswordRequest.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/in/web/ResetPasswordRequest.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/in/web/RegisterResponse.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/AuthController.java` ← DELETE (old)
  - `src/main/java/cl/gradeops/ai/api/auth/PasswordResetController.java` ← DELETE (old)
  - `src/test/java/cl/gradeops/ai/api/auth/infrastructure/adapter/in/web/AuthControllerTest.java` ← NEW (migrated)
  - `src/test/java/cl/gradeops/ai/api/auth/AuthControllerTest.java` ← DELETE
  - `src/test/java/cl/gradeops/ai/api/auth/PasswordResetControllerTest.java` ← DELETE
- **Interfaces / contracts:**
  - `POST /api/v1/auth/register` → injects `RegisterUseCase`, maps `RegisterRequest → RegisterCommand`, returns `RegisterResponse`.
  - `POST /api/v1/auth/sign-out` → injects `SignOutUseCase`, reads `uid` from security context.
  - `POST /api/v1/auth/forgot-password` → injects `SendPasswordResetEmailUseCase`, maps `ForgotPasswordRequest → SendPasswordResetEmailCommand`, returns `204 No Content`.
  - `POST /api/v1/auth/reset-password` → injects `ResetPasswordUseCase`, maps `ResetPasswordRequest → ResetPasswordCommand`, returns `204 No Content`.
- **Design notes:**
  - DTOs are Java records (not classes). Use `@NotBlank` or `@NotNull` for validation annotations where the old controller had them.
  - `RegisterRequest`: fields `idToken`, `firstName` (nullable), `lastName` (nullable) — matches old `AuthController`.
  - `ForgotPasswordRequest`: field `email`.
  - `ResetPasswordRequest`: fields `code`, `email`, `password`, `passwordRepeat`.
  - `RegisterResponse`: fields `uid`, `created` — same shape as old `RegisterResult`.
  - Controller is `@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor`.
  - All 4 endpoint methods preserve existing HTTP method, path, status code, and response shape. No behaviour changes — pure relocation.

---

## Implementation Steps

1. Read old `src/main/java/cl/gradeops/ai/api/auth/AuthController.java` and `PasswordResetController.java` to capture exact endpoint signatures, request bodies, and status codes.
2. Create directory `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/in/web/`.
3. Create the 4 DTO record files in that directory:
   - `RegisterRequest.java`: `record RegisterRequest(String idToken, String firstName, String lastName)`.
   - `ForgotPasswordRequest.java`: `record ForgotPasswordRequest(String email)`.
   - `ResetPasswordRequest.java`: `record ResetPasswordRequest(String code, String email, String password, String passwordRepeat)`.
   - `RegisterResponse.java`: `record RegisterResponse(String uid, boolean created)`.
4. Create `AuthController.java`:
   - `@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor`.
   - Injects: `RegisterUseCase`, `SignOutUseCase`, `SendPasswordResetEmailUseCase`, `ResetPasswordUseCase`.
   - `register`: `@PostMapping("/register")` → builds `RegisterCommand` → calls `registerUseCase.execute(cmd)` → returns `RegisterResponse(result.uid(), result.created())`.
   - `signOut`: `@PostMapping("/sign-out")` → reads uid from `SecurityContextHolder` or `@AuthenticationPrincipal` → calls `signOutUseCase.execute(uid)` → returns `ResponseEntity.noContent().build()`.
   - `forgotPassword`: `@PostMapping("/forgot-password") @ResponseStatus(NO_CONTENT)` → maps request → calls `sendPasswordResetEmailUseCase.execute(cmd)`.
   - `resetPassword`: `@PostMapping("/reset-password") @ResponseStatus(NO_CONTENT)` → maps request → calls `resetPasswordUseCase.execute(cmd)`.
5. Create test directory `src/test/java/cl/gradeops/ai/api/auth/infrastructure/adapter/in/web/`.
6. Create `AuthControllerTest.java` — migrate all tests from both old test classes:
   - `@WebMvcTest(AuthController.class)`.
   - `@MockBean` for all 4 use-case interfaces.
   - Auth register: existing teacher → 200 with `created=false`.
   - Auth register: new teacher → 200 with `created=true`.
   - Sign-out: authenticated user → 204.
   - Forgot-password: known email → 204.
   - Reset-password: valid code → 204.
   - Reset-password: invalid code → 422 (verify `GlobalExceptionHandler` maps `InvalidResetCodeException` to 422).
7. Delete old `src/main/java/cl/gradeops/ai/api/auth/AuthController.java`.
8. Delete old `src/main/java/cl/gradeops/ai/api/auth/PasswordResetController.java`.
9. Delete old test classes:
   - `src/test/java/cl/gradeops/ai/api/auth/AuthControllerTest.java`
   - `src/test/java/cl/gradeops/ai/api/auth/PasswordResetControllerTest.java`
10. Run `./mvnw test -Dtest=AuthControllerTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `POST /api/v1/auth/register` existing teacher → 200 `created=false` | `MockMvc.perform(...).andExpect(jsonPath("$.created").value(false))` |
| 2 | `POST /api/v1/auth/register` new teacher → 200 `created=true` | `jsonPath("$.created").value(true)` |
| 3 | `POST /api/v1/auth/sign-out` → 204 and `signOutUseCase.execute` called | `andExpect(status().isNoContent())` + Mockito verify |
| 4 | `POST /api/v1/auth/forgot-password` → 204 | `andExpect(status().isNoContent())` |
| 5 | `POST /api/v1/auth/reset-password` valid code → 204 | `andExpect(status().isNoContent())` |
| 6 | `POST /api/v1/auth/reset-password` `InvalidResetCodeException` → 422 | Mock `resetPasswordUseCase.execute` to throw `InvalidResetCodeException`; assert `status().isUnprocessableEntity()` |

---

## Done Criteria

- [x] `AuthController` in `auth/infrastructure/adapter/in/web/` consolidates all 4 auth endpoints
- [x] 4 DTO records exist in same package
- [x] Old `auth/AuthController.java` and `auth/PasswordResetController.java` deleted
- [x] Old `AuthControllerTest.java` and `PasswordResetControllerTest.java` deleted
- [x] New `AuthControllerTest.java` covers all 6 test cases; methods follow `should...When...` convention
- [x] Sign-out test uses `SecurityContextHolder.getContext().setAuthentication(...)` + `@AfterEach` clear — Spring Boot 4's `@WebMvcTest` does not load `MockMvcSecurityAutoConfiguration`, so `authentication()` post-processor from `spring-security-test` does not work (session-stored context never loaded without `SecurityContextHolderFilter` in chain)
- [x] `spring-security-test` 7.1.0 added to pom.xml (test scope)
- [x] `./mvnw test -Dtest=AuthControllerTest -q` exits 0 — 6/6 pass

---

> [← story file](../story-02-auth-bounded-context.md)
