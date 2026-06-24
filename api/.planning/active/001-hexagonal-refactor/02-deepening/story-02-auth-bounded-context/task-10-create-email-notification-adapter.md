# ⚛️ TASK 10 — Create ThymeleafEmailNotificationAdapter

> **Status:** DONE
> **Workflow:** IMPLEMENT
> **Depends On:** task-04
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Create `ThymeleafEmailNotificationAdapter` in `auth.infrastructure.adapter.out.email` that implements `EmailNotificationPort`. The adapter reads `gradeops.web.base-url` from config to build the reset link URL and delegates the actual send to `JavaMailEmailService` (Story 01, `shared.infrastructure.adapter.out.email`).

---

## Technical Design

- **Approach:** This adapter is the single point where a raw reset code is converted into a full reset URL (`{base-url}/reset-password?code={rawCode}`). It then delegates email rendering and sending to the existing `JavaMailEmailService` — which uses Thymeleaf + JavaMailSender. The adapter has no template logic of its own; it builds the URL string and calls the right `JavaMailEmailService` method. The base URL is injected via `@Value("${gradeops.web.base-url}")`, which must already be declared in `application.yml`.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/email/ThymeleafEmailNotificationAdapter.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/email/ThymeleafEmailNotificationAdapterTest.java` ← NEW
- **Interfaces / contracts:** `ThymeleafEmailNotificationAdapter` implements `auth.application.port.out.EmailNotificationPort`. Single method: `sendPasswordResetEmail(String toEmail, String firstName, String rawCode)`.
- **Design notes:**
  - Reset URL format: `String resetUrl = webBaseUrl + "/reset-password?code=" + rawCode`.
  - Calls `JavaMailEmailService.sendPasswordResetEmail(toEmail, firstName, resetUrl)` (the method that accepts a complete URL — verify the existing method signature before implementing).
  - The Thymeleaf template (`password-reset.html`) is unchanged — it already expects a full URL.
  - `@RequiredArgsConstructor` — no Spring stereotype. `webBaseUrl` is a plain `String` constructor field.
  - The `@Value("${gradeops.web.base-url}")` annotation lives in `AuthConfig`, not in this class. `AuthConfig` reads the property and passes the resolved string to the constructor at wire-up time. This keeps the adapter free of all Spring imports except `@RequiredArgsConstructor`.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/email/`.
2. Read `src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/out/email/JavaMailEmailService.java` to verify the exact method signature for sending a password reset email.
3. Create `ThymeleafEmailNotificationAdapter.java`:
   ```java
   package cl.gradeops.ai.api.auth.infrastructure.adapter.out.email;

   import cl.gradeops.ai.api.auth.application.port.out.EmailNotificationPort;
   import cl.gradeops.ai.api.shared.infrastructure.adapter.out.email.JavaMailEmailService;
   import lombok.RequiredArgsConstructor;

   // NO @Component — declared as @Bean in AuthConfig (task-12)
   @RequiredArgsConstructor
   public class ThymeleafEmailNotificationAdapter implements EmailNotificationPort {

       private final JavaMailEmailService javaMailEmailService;
       private final String webBaseUrl; // supplied by AuthConfig via @Value("${gradeops.web.base-url}")

       @Override
       public void sendPasswordResetEmail(String toEmail, String firstName, String rawCode) {
           String resetUrl = webBaseUrl + "/reset-password?code=" + rawCode;
           javaMailEmailService.sendPasswordResetEmail(toEmail, firstName, resetUrl);
       }
   }
   ```
4. Create test directory `src/test/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/email/`.
5. Create `ThymeleafEmailNotificationAdapterTest.java`:
   - Use `@ExtendWith(MockitoExtension.class)`.
   - Construct the adapter directly: `new ThymeleafEmailNotificationAdapter(mockMailService, "https://app.gradeops.cl")` — no Spring context needed since the adapter has no framework annotations.
   - Test: `sendPasswordResetEmail("t@test.com", "Ana", "abc-123")` → `javaMailEmailService.sendPasswordResetEmail("t@test.com", "Ana", "https://app.gradeops.cl/reset-password?code=abc-123")` is called.
   - Test: URL is built correctly when `rawCode` contains hyphens (UUID format).
6. Run `./mvnw test -Dtest=ThymeleafEmailNotificationAdapterTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Reset URL is built from base URL + path + rawCode | `ArgumentCaptor<String>` on `javaMailEmailService` → assert URL equals expected |
| 2 | `toEmail` and `firstName` are passed through unchanged | Assert `javaMailEmailService.sendPasswordResetEmail(toEmail, firstName, anyString())` with exact email + name |
| 3 | UUID-format raw code (hyphens) produces valid URL | `rawCode = "550e8400-e29b-41d4-a716-446655440000"` → URL contains full UUID |

---

## Done Criteria

- [ ] `ThymeleafEmailNotificationAdapter` exists in `auth/infrastructure/adapter/out/email/`
- [ ] Adapter is `@RequiredArgsConstructor` with NO `@Component` — declared as `@Bean` in `AuthConfig`
- [ ] Adapter has no Spring or `@Value` imports — `webBaseUrl` is a plain `String` field
- [ ] Adapter implements `auth.application.port.out.EmailNotificationPort`
- [ ] URL is built as `{webBaseUrl}/reset-password?code={rawCode}` — base URL string is supplied by `AuthConfig`
- [ ] Adapter does NOT do its own Thymeleaf rendering — delegates to `JavaMailEmailService`
- [ ] All unit test cases pass
- [ ] `./mvnw test -Dtest=ThymeleafEmailNotificationAdapterTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-02-auth-bounded-context.md)
