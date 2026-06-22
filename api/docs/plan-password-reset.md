# Password Reset — Custom Email (API) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement `POST /api/v1/auth/forgot-password` and `PUT /api/v1/auth/reset-password` using backend-owned token management, JavaMail + Thymeleaf email, and Firebase Admin SDK `updateUser` for password changes.

**Architecture:** A UUID reset code with 30-minute TTL is generated on forgot-password, stored in `password_reset_codes`, and emailed via SMTP. On reset-password, the code is validated, the teacher's email is verified, Firebase Admin SDK updates the password, and the code is marked used.

**Tech Stack:** Spring Boot 4.1, Jakarta Validation, Spring Data JPA (PostgreSQL), Flyway, Spring Boot Starter Mail, Thymeleaf, Firebase Admin SDK, Mockito (unit tests), MockMvc (integration tests).

## Global Constraints

- All production code under `src/main/java/cl/gradeops/ai/api/`
- All test code under `src/test/java/cl/gradeops/ai/api/`
- `TeacherEntity` PK field: `firebaseUid`, getter: `getFirebaseUid()` — NOT `getUid()`
- `TeacherRepository.findByEmail(String email)` already exists — do not add it
- `ApiError` record lives at `cl.gradeops.ai.api.common.ApiError` with factory `ApiError.of(String error)`
- Run a specific test class: `./mvnw test -Dtest=ClassName`
- Run all tests: `./mvnw test`
- Integration tests use `@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Import(FirebaseTestConfig.class)`
- `FirebaseTestConfig` is at `cl.gradeops.ai.api.config.FirebaseTestConfig` — produces a Mockito mock `FirebaseAuth` bean
- Responses always use `Content-Type: application/json`
- Do NOT use `@MockBean` (deprecated); use `@MockitoBean` for Spring Boot 4.x mocking in tests

---

## File Map

**Create:**
- `src/main/resources/db/migration/V5__add_password_reset_codes.sql`
- `src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeEntity.java`
- `src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeRepository.java`
- `src/main/java/cl/gradeops/ai/api/auth/ForgotPasswordRequest.java`
- `src/main/java/cl/gradeops/ai/api/auth/ResetPasswordRequest.java`
- `src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java`
- `src/main/java/cl/gradeops/ai/api/config/GradeOpsEmailProperties.java`
- `src/main/java/cl/gradeops/ai/api/config/GradeOpsWebProperties.java`
- `src/main/java/cl/gradeops/ai/api/email/EmailService.java`
- `src/main/resources/templates/email/password-reset.html`
- `src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java`
- `src/test/java/cl/gradeops/ai/api/email/EmailServiceTest.java`
- `src/test/java/cl/gradeops/ai/api/auth/PasswordResetControllerTest.java`

**Modify:**
- `pom.xml` — add `spring-boot-starter-mail` + `spring-boot-starter-thymeleaf`
- `src/main/resources/application.yml` — add `spring.mail.*` + `gradeops.*`
- `src/main/resources/application-local.yml` — add SMTP local config + `gradeops.*`
- `src/main/java/cl/gradeops/ai/api/port/AuthPort.java` — add `updatePassword(String uid, String newPassword)`
- `src/main/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapter.java` — implement `updatePassword`
- `src/main/java/cl/gradeops/ai/api/auth/AuthController.java` — add 2 new endpoints
- `src/main/java/cl/gradeops/ai/api/security/SecurityConfig.java` — whitelist new public paths
- `src/main/java/cl/gradeops/ai/api/common/GlobalExceptionHandler.java` — add `ResponseStatusException` handler
- `src/test/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapterTest.java` — add `updatePassword` test

---

## Task 1: Maven dependencies + DB migration + Entity + Repository

**Files:**
- Modify: `pom.xml`
- Create: `src/main/resources/db/migration/V5__add_password_reset_codes.sql`
- Create: `src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeEntity.java`
- Create: `src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeRepository.java`

**Interfaces:**
- Produces: `PasswordResetCodeEntity(String teacherUid, String code, Instant expiresAt)`, `.isExpired()`, `.isUsed()`, `.markUsed()`, `.getTeacherUid()`, `.getCode()`
- Produces: `PasswordResetCodeRepository.findByCode(String)`, `.deleteByTeacherUid(String)`

---

- [ ] **Step 1: Add Maven dependencies**

In `pom.xml`, add inside `<dependencies>`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

- [ ] **Step 2: Write the Flyway migration**

Create `src/main/resources/db/migration/V5__add_password_reset_codes.sql`:
```sql
CREATE TABLE password_reset_codes (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_uid VARCHAR     NOT NULL REFERENCES teacher(firebase_uid) ON DELETE CASCADE,
    code        VARCHAR(36) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_prc_teacher_uid ON password_reset_codes(teacher_uid);
```

> Note: The `teacher` table uses `firebase_uid` as the PK column — verify in `V1__create_teacher_table.sql` before applying.

- [ ] **Step 3: Create PasswordResetCodeEntity**

Create `src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeEntity.java`:
```java
package cl.gradeops.ai.api.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_codes")
public class PasswordResetCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "teacher_uid", nullable = false)
    private String teacherUid;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); }

    public PasswordResetCodeEntity(String teacherUid, String code, Instant expiresAt) {
        this.teacherUid = teacherUid;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    protected PasswordResetCodeEntity() {}

    public UUID getId()           { return id; }
    public String getTeacherUid() { return teacherUid; }
    public String getCode()       { return code; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt()    { return usedAt; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isUsed()    { return usedAt != null; }
    public void markUsed()     { this.usedAt = Instant.now(); }
}
```

- [ ] **Step 4: Create PasswordResetCodeRepository**

Create `src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeRepository.java`:
```java
package cl.gradeops.ai.api.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCodeEntity, UUID> {
    Optional<PasswordResetCodeEntity> findByCode(String code);
    void deleteByTeacherUid(String teacherUid);
}
```

- [ ] **Step 5: Verify migration applies cleanly**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
Expected: application starts without Flyway errors. Stop it immediately (Ctrl+C). If Flyway fails, check the `teacher` table PK column name against V1 migration.

- [ ] **Step 6: Commit**
```bash
git add pom.xml \
  src/main/resources/db/migration/V5__add_password_reset_codes.sql \
  src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeEntity.java \
  src/main/java/cl/gradeops/ai/api/auth/PasswordResetCodeRepository.java
git commit -m "feat(api): add password_reset_codes table, entity, and repository"
```

---

## Task 2: Config properties + YAML

**Files:**
- Create: `src/main/java/cl/gradeops/ai/api/config/GradeOpsEmailProperties.java`
- Create: `src/main/java/cl/gradeops/ai/api/config/GradeOpsWebProperties.java`
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/resources/application-local.yml`

**Interfaces:**
- Produces: `GradeOpsEmailProperties.getFromAddress()`, `.getFromName()`, `.getSupportEmail()`, `.getResetPassword().getTtlMinutes()`, `.getResetPassword().getSubject()`
- Produces: `GradeOpsWebProperties.getBaseUrl()`

---

- [ ] **Step 1: Create GradeOpsEmailProperties**

Create `src/main/java/cl/gradeops/ai/api/config/GradeOpsEmailProperties.java`:
```java
package cl.gradeops.ai.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gradeops.email")
public class GradeOpsEmailProperties {

    private String fromAddress;
    private String fromName;
    private String supportEmail;
    private ResetPassword resetPassword = new ResetPassword();

    public static class ResetPassword {
        private int ttlMinutes = 30;
        private String subject = "Restablece tu contraseña en GradeOps AI";

        public int getTtlMinutes()       { return ttlMinutes; }
        public String getSubject()       { return subject; }
        public void setTtlMinutes(int v) { this.ttlMinutes = v; }
        public void setSubject(String v) { this.subject = v; }
    }

    public String getFromAddress()                { return fromAddress; }
    public String getFromName()                   { return fromName; }
    public String getSupportEmail()               { return supportEmail; }
    public ResetPassword getResetPassword()        { return resetPassword; }
    public void setFromAddress(String v)           { this.fromAddress = v; }
    public void setFromName(String v)              { this.fromName = v; }
    public void setSupportEmail(String v)          { this.supportEmail = v; }
    public void setResetPassword(ResetPassword v)  { this.resetPassword = v; }
}
```

- [ ] **Step 2: Create GradeOpsWebProperties**

Create `src/main/java/cl/gradeops/ai/api/config/GradeOpsWebProperties.java`:
```java
package cl.gradeops.ai.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gradeops.web")
public class GradeOpsWebProperties {

    private String baseUrl = "http://localhost:3000";

    public String getBaseUrl()       { return baseUrl; }
    public void setBaseUrl(String v) { this.baseUrl = v; }
}
```

- [ ] **Step 3: Update application.yml**

Append to `src/main/resources/application.yml` (after the existing `app:` block):
```yaml
spring:
  mail:
    host: ${SMTP_HOST:localhost}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USERNAME:}
    password: ${SMTP_PASSWORD:}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

gradeops:
  web:
    base-url: ${GRADEOPS_WEB_BASE_URL:http://localhost:3000}
  email:
    from-address: ${GRADEOPS_MAIL_FROM:noreply@gradeops.app}
    from-name: ${GRADEOPS_MAIL_FROM_NAME:GradeOps AI}
    support-email: ${GRADEOPS_SUPPORT_EMAIL:soporte@gradeops.app}
    reset-password:
      ttl-minutes: 30
      subject: "Restablece tu contraseña en GradeOps AI"
```

- [ ] **Step 4: Update application-local.yml**

Append to `src/main/resources/application-local.yml`:
```yaml
spring:
  mail:
    host: sandbox.smtp.mailtrap.io
    port: 587
    username: ${SMTP_USERNAME:}
    password: ${SMTP_PASSWORD:}

gradeops:
  web:
    base-url: http://localhost:3000
  email:
    from-address: noreply@gradeops.local
    from-name: GradeOps AI (local)
    support-email: dev@gradeops.local
```

- [ ] **Step 5: Verify application starts**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
Expected: application starts. `GradeOpsEmailProperties` and `GradeOpsWebProperties` beans created (check for any `@ConfigurationProperties` binding errors). Stop immediately.

- [ ] **Step 6: Commit**
```bash
git add \
  src/main/java/cl/gradeops/ai/api/config/GradeOpsEmailProperties.java \
  src/main/java/cl/gradeops/ai/api/config/GradeOpsWebProperties.java \
  src/main/resources/application.yml \
  src/main/resources/application-local.yml
git commit -m "feat(api): add GradeOps email and web config properties"
```

---

## Task 3: AuthPort.updatePassword() + FirebaseAuthAdapter

**Files:**
- Modify: `src/main/java/cl/gradeops/ai/api/port/AuthPort.java`
- Modify: `src/main/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapter.java`
- Modify: `src/test/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapterTest.java`

**Interfaces:**
- Consumes: `FirebaseAuth.updateUser(UserRecord.UpdateRequest)`
- Produces: `AuthPort.updatePassword(String uid, String newPassword)`

---

- [ ] **Step 1: Read the existing FirebaseAuthAdapterTest**

Open `src/test/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapterTest.java` to understand the test structure before adding to it.

- [ ] **Step 2: Add failing test for updatePassword**

In `FirebaseAuthAdapterTest`, add:
```java
import com.google.firebase.auth.UserRecord;
import org.mockito.ArgumentCaptor;

@Test
void updatePassword_callsFirebaseUpdateUserWithCorrectUid() throws FirebaseAuthException {
    adapter.updatePassword("uid-teacher-1", "newSecurePass");

    ArgumentCaptor<UserRecord.UpdateRequest> captor =
        ArgumentCaptor.forClass(UserRecord.UpdateRequest.class);
    verify(firebaseAuth).updateUser(captor.capture());
    assertThat(captor.getValue().getUid()).isEqualTo("uid-teacher-1");
}

@Test
void updatePassword_wrapsFirebaseExceptionAsRuntimeException() throws FirebaseAuthException {
    when(firebaseAuth.updateUser(any())).thenThrow(mock(FirebaseAuthException.class));

    assertThatThrownBy(() -> adapter.updatePassword("uid-teacher-1", "pass"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to update password");
}
```

- [ ] **Step 3: Run tests to verify they fail**
```bash
./mvnw test -Dtest=FirebaseAuthAdapterTest
```
Expected: FAIL — `updatePassword` method not found on `adapter`.

- [ ] **Step 4: Add updatePassword to AuthPort**

In `src/main/java/cl/gradeops/ai/api/port/AuthPort.java`, add:
```java
void updatePassword(String uid, String newPassword);
```

- [ ] **Step 5: Implement in FirebaseAuthAdapter**

In `src/main/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapter.java`, add:
```java
import com.google.firebase.auth.UserRecord;

@Override
public void updatePassword(String uid, String newPassword) {
    try {
        firebaseAuth.updateUser(new UserRecord.UpdateRequest(uid).setPassword(newPassword));
    } catch (FirebaseAuthException e) {
        throw new RuntimeException("Failed to update password for uid " + uid, e);
    }
}
```

- [ ] **Step 6: Run tests to verify they pass**
```bash
./mvnw test -Dtest=FirebaseAuthAdapterTest
```
Expected: All tests PASS including the 2 new ones.

- [ ] **Step 7: Commit**
```bash
git add \
  src/main/java/cl/gradeops/ai/api/port/AuthPort.java \
  src/main/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapter.java \
  src/test/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapterTest.java
git commit -m "feat(api): add AuthPort.updatePassword() via Firebase Admin SDK"
```

---

## Task 4: Thymeleaf email template + EmailService

**Files:**
- Create: `src/main/resources/templates/email/password-reset.html`
- Create: `src/main/java/cl/gradeops/ai/api/email/EmailService.java`
- Create: `src/test/java/cl/gradeops/ai/api/email/EmailServiceTest.java`

**Interfaces:**
- Consumes: `GradeOpsEmailProperties` (from Task 2)
- Produces: `EmailService.sendPasswordReset(String toEmail, String firstName, String resetLink)`

---

- [ ] **Step 1: Create the Thymeleaf email template**

Create `src/main/resources/templates/email/password-reset.html`:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Restablecer contraseña</title>
  <style>
    body { margin:0; padding:0; background:#f4f4f5; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif; }
    .wrapper { max-width:560px; margin:40px auto; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.08); }
    .header { background:linear-gradient(135deg,#22c55e,#16a34a); padding:28px 32px; }
    .header h1 { margin:0; color:#fff; font-size:20px; font-weight:700; letter-spacing:-0.02em; }
    .header p { margin:4px 0 0; color:rgba(255,255,255,0.85); font-size:13px; }
    .body { padding:32px; }
    .body p { margin:0 0 16px; color:#374151; font-size:15px; line-height:1.6; }
    .cta { text-align:center; margin:28px 0; }
    .cta a { display:inline-block; background:#16a34a; color:#fff; text-decoration:none; padding:13px 28px; border-radius:8px; font-weight:600; font-size:15px; }
    .link-fallback { background:#f9fafb; border:1px solid #e5e7eb; border-radius:6px; padding:12px 16px; margin:20px 0; word-break:break-all; font-size:12px; color:#6b7280; }
    .notice { background:#fefce8; border-left:3px solid #eab308; padding:12px 16px; border-radius:0 6px 6px 0; margin:20px 0; font-size:13px; color:#713f12; }
    .footer { padding:20px 32px; border-top:1px solid #f3f4f6; font-size:12px; color:#9ca3af; text-align:center; }
  </style>
</head>
<body>
<div class="wrapper">
  <div class="header">
    <h1 th:text="${appName}">GradeOps AI</h1>
    <p>Restablecer contraseña</p>
  </div>
  <div class="body">
    <p>Hola <strong th:text="${firstName}">docente</strong>,</p>
    <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta. Haz clic en el botón para continuar:</p>
    <div class="cta">
      <a th:href="${resetLink}">Restablecer contraseña</a>
    </div>
    <p style="font-size:13px;color:#6b7280;">Si el botón no funciona, copia y pega este enlace en tu navegador:</p>
    <div class="link-fallback" th:text="${resetLink}"></div>
    <div class="notice">
      Este enlace es válido por <strong th:text="${expiresInMinutes}">30</strong> minutos.
      Si no solicitaste este cambio, puedes ignorar este correo — tu contraseña no será modificada.
    </div>
    <p>Dudas: <a th:href="'mailto:' + ${supportEmail}" th:text="${supportEmail}">soporte@gradeops.app</a>.</p>
  </div>
  <div class="footer">
    &copy; 2026 <span th:text="${appName}">GradeOps AI</span> · Portal docente
  </div>
</div>
</body>
</html>
```

- [ ] **Step 2: Write failing tests for EmailService**

Create `src/test/java/cl/gradeops/ai/api/email/EmailServiceTest.java`:
```java
package cl.gradeops.ai.api.email;

import cl.gradeops.ai.api.config.GradeOpsEmailProperties;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock TemplateEngine templateEngine;

    EmailService emailService;
    GradeOpsEmailProperties emailProperties;

    @BeforeEach
    void setUp() {
        emailProperties = new GradeOpsEmailProperties();
        emailProperties.setFromAddress("noreply@gradeops.app");
        emailProperties.setFromName("GradeOps AI");
        emailProperties.setSupportEmail("soporte@gradeops.app");

        emailService = new EmailService(mailSender, templateEngine, emailProperties);
    }

    @Test
    void sendPasswordReset_processesTemplateWithCorrectVariables() {
        MimeMessage mimeMsg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMsg);
        when(templateEngine.process(eq("email/password-reset"), any(IContext.class)))
            .thenReturn("<html>mock</html>");

        emailService.sendPasswordReset("teacher@school.cl", "Ana", "http://localhost:3000/reset-password?code=abc");

        ArgumentCaptor<IContext> ctxCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("email/password-reset"), ctxCaptor.capture());

        IContext ctx = ctxCaptor.getValue();
        assertThat(ctx.getVariable("firstName")).isEqualTo("Ana");
        assertThat(ctx.getVariable("resetLink")).isEqualTo("http://localhost:3000/reset-password?code=abc");
        assertThat(ctx.getVariable("expiresInMinutes")).isEqualTo(30);
        assertThat(ctx.getVariable("appName")).isEqualTo("GradeOps AI");
        assertThat(ctx.getVariable("supportEmail")).isEqualTo("soporte@gradeops.app");
    }

    @Test
    void sendPasswordReset_sendsEmailViaMailSender() {
        MimeMessage mimeMsg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMsg);
        when(templateEngine.process(any(), any())).thenReturn("<html>mock</html>");

        emailService.sendPasswordReset("teacher@school.cl", "Ana", "http://localhost:3000/reset-password?code=abc");

        verify(mailSender).send(mimeMsg);
    }

    @Test
    void sendPasswordReset_wrapsMessagingExceptionAsRuntimeException() throws Exception {
        MimeMessage mimeMsg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMsg);
        when(templateEngine.process(any(), any())).thenReturn("<html>mock</html>");
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() ->
            emailService.sendPasswordReset("teacher@school.cl", "Ana", "http://x.com/reset?code=abc"))
            .isInstanceOf(RuntimeException.class);
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**
```bash
./mvnw test -Dtest=EmailServiceTest
```
Expected: FAIL — `EmailService` class not found.

- [ ] **Step 4: Implement EmailService**

Create `src/main/java/cl/gradeops/ai/api/email/EmailService.java`:
```java
package cl.gradeops.ai.api.email;

import cl.gradeops.ai.api.config.GradeOpsEmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final GradeOpsEmailProperties emailProperties;

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        GradeOpsEmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailProperties = emailProperties;
    }

    public void sendPasswordReset(String toEmail, String firstName, String resetLink) {
        Context ctx = new Context();
        ctx.setVariable("firstName", firstName);
        ctx.setVariable("resetLink", resetLink);
        ctx.setVariable("expiresInMinutes", emailProperties.getResetPassword().getTtlMinutes());
        ctx.setVariable("appName", emailProperties.getFromName());
        ctx.setVariable("supportEmail", emailProperties.getSupportEmail());

        String html = templateEngine.process("email/password-reset", ctx);
        sendHtml(toEmail, emailProperties.getResetPassword().getSubject(), html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(emailProperties.getFromAddress(), emailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**
```bash
./mvnw test -Dtest=EmailServiceTest
```
Expected: All 3 tests PASS.

- [ ] **Step 6: Commit**
```bash
git add \
  src/main/resources/templates/email/password-reset.html \
  src/main/java/cl/gradeops/ai/api/email/EmailService.java \
  src/test/java/cl/gradeops/ai/api/email/EmailServiceTest.java
git commit -m "feat(api): add Thymeleaf password reset email template and EmailService"
```

---

## Task 5: PasswordResetService

**Files:**
- Create: `src/main/java/cl/gradeops/ai/api/auth/ForgotPasswordRequest.java`
- Create: `src/main/java/cl/gradeops/ai/api/auth/ResetPasswordRequest.java`
- Create: `src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java`
- Create: `src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java`

**Interfaces:**
- Consumes: `TeacherRepository.findByEmail(String)`, `TeacherRepository.findById(String)` (returns `Optional<TeacherEntity>` with key `firebaseUid`)
- Consumes: `PasswordResetCodeRepository.findByCode(String)`, `.deleteByTeacherUid(String)`, `.save(...)`
- Consumes: `EmailService.sendPasswordReset(String, String, String)` (from Task 4)
- Consumes: `AuthPort.updatePassword(String, String)` (from Task 3)
- Consumes: `GradeOpsEmailProperties.getResetPassword().getTtlMinutes()` (from Task 2)
- Consumes: `GradeOpsWebProperties.getBaseUrl()` (from Task 2)
- Produces: `PasswordResetService.sendResetEmail(ForgotPasswordRequest)`
- Produces: `PasswordResetService.resetPassword(String code, ResetPasswordRequest)`

---

- [ ] **Step 1: Create request DTOs**

Create `src/main/java/cl/gradeops/ai/api/auth/ForgotPasswordRequest.java`:
```java
package cl.gradeops.ai.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank(message = "El correo es requerido.")
    @Email(message = "Ingresa una dirección de correo válida.")
    String email
) {}
```

Create `src/main/java/cl/gradeops/ai/api/auth/ResetPasswordRequest.java`:
```java
package cl.gradeops.ai.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "El correo es requerido.")
    @Email(message = "Ingresa una dirección de correo válida.")
    String email,

    @NotBlank
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    String password,

    @NotBlank(message = "Repite la contraseña.")
    String passwordRepeat
) {}
```

- [ ] **Step 2: Write failing tests for PasswordResetService**

Create `src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java`:
```java
package cl.gradeops.ai.api.auth;

import cl.gradeops.ai.api.config.GradeOpsEmailProperties;
import cl.gradeops.ai.api.config.GradeOpsWebProperties;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import cl.gradeops.ai.api.email.EmailService;
import cl.gradeops.ai.api.port.AuthPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock TeacherRepository teacherRepository;
    @Mock PasswordResetCodeRepository codeRepository;
    @Mock EmailService emailService;
    @Mock AuthPort authPort;

    PasswordResetService service;

    @BeforeEach
    void setUp() {
        GradeOpsEmailProperties emailProps = new GradeOpsEmailProperties();
        GradeOpsWebProperties webProps = new GradeOpsWebProperties();
        webProps.setBaseUrl("http://localhost:3000");

        service = new PasswordResetService(
            teacherRepository, codeRepository, emailService, authPort, emailProps, webProps);
    }

    // ─── sendResetEmail ────────────────────────────────────────────────────────

    @Test
    void sendResetEmail_unknownEmail_doesNothing() {
        when(teacherRepository.findByEmail("unknown@school.cl")).thenReturn(Optional.empty());

        service.sendResetEmail(new ForgotPasswordRequest("unknown@school.cl"));

        verifyNoInteractions(codeRepository, emailService);
    }

    @Test
    void sendResetEmail_googleProvider_doesNothing() {
        TeacherEntity googleTeacher = new TeacherEntity("uid-g", "Ana", "López", "ana@gmail.com", "GOOGLE");
        when(teacherRepository.findByEmail("ana@gmail.com")).thenReturn(Optional.of(googleTeacher));

        service.sendResetEmail(new ForgotPasswordRequest("ana@gmail.com"));

        verifyNoInteractions(codeRepository, emailService);
    }

    @Test
    void sendResetEmail_emailPasswordTeacher_savesCodeAndSendsEmail() {
        TeacherEntity teacher = new TeacherEntity("uid-ep", "Pedro", "Soto", "pedro@school.cl", "EMAIL_PASSWORD");
        when(teacherRepository.findByEmail("pedro@school.cl")).thenReturn(Optional.of(teacher));

        service.sendResetEmail(new ForgotPasswordRequest("pedro@school.cl"));

        verify(codeRepository).deleteByTeacherUid("uid-ep");

        ArgumentCaptor<PasswordResetCodeEntity> codeCaptor =
            ArgumentCaptor.forClass(PasswordResetCodeEntity.class);
        verify(codeRepository).save(codeCaptor.capture());

        PasswordResetCodeEntity saved = codeCaptor.getValue();
        assertThat(saved.getTeacherUid()).isEqualTo("uid-ep");
        assertThat(saved.getCode()).hasSize(36); // UUID with hyphens
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordReset(
            eq("pedro@school.cl"), eq("Pedro"), linkCaptor.capture());
        assertThat(linkCaptor.getValue())
            .startsWith("http://localhost:3000/reset-password?code=");
    }

    @Test
    void sendResetEmail_deletesExistingCodeBeforeCreatingNew() {
        TeacherEntity teacher = new TeacherEntity("uid-ep", "Pedro", "Soto", "pedro@school.cl", "EMAIL_PASSWORD");
        when(teacherRepository.findByEmail("pedro@school.cl")).thenReturn(Optional.of(teacher));

        service.sendResetEmail(new ForgotPasswordRequest("pedro@school.cl"));
        service.sendResetEmail(new ForgotPasswordRequest("pedro@school.cl"));

        verify(codeRepository, times(2)).deleteByTeacherUid("uid-ep");
        verify(codeRepository, times(2)).save(any(PasswordResetCodeEntity.class));
    }

    // ─── resetPassword ─────────────────────────────────────────────────────────

    @Test
    void resetPassword_codeNotFound_throws404() {
        when(codeRepository.findByCode("bad-code")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.resetPassword("bad-code", new ResetPasswordRequest("x@x.cl", "pass12", "pass12")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void resetPassword_codeExpired_throws410WithExpiredReason() {
        PasswordResetCodeEntity expired = new PasswordResetCodeEntity(
            "uid-ep", "code-xyz", Instant.now().minus(1, ChronoUnit.HOURS));
        when(codeRepository.findByCode("code-xyz")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() ->
            service.resetPassword("code-xyz", new ResetPasswordRequest("x@x.cl", "pass12", "pass12")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException rse = (ResponseStatusException) e;
                assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.GONE);
                assertThat(rse.getReason()).isEqualTo("RESET_CODE_EXPIRED");
            });
    }

    @Test
    void resetPassword_codeAlreadyUsed_throws410WithUsedReason() {
        PasswordResetCodeEntity used = new PasswordResetCodeEntity(
            "uid-ep", "code-xyz", Instant.now().plus(30, ChronoUnit.MINUTES));
        used.markUsed();
        when(codeRepository.findByCode("code-xyz")).thenReturn(Optional.of(used));

        assertThatThrownBy(() ->
            service.resetPassword("code-xyz", new ResetPasswordRequest("x@x.cl", "pass12", "pass12")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException rse = (ResponseStatusException) e;
                assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.GONE);
                assertThat(rse.getReason()).isEqualTo("RESET_CODE_USED");
            });
    }

    @Test
    void resetPassword_emailMismatch_throws422() {
        PasswordResetCodeEntity code = new PasswordResetCodeEntity(
            "uid-ep", "code-xyz", Instant.now().plus(30, ChronoUnit.MINUTES));
        when(codeRepository.findByCode("code-xyz")).thenReturn(Optional.of(code));

        TeacherEntity teacher = new TeacherEntity("uid-ep", "Pedro", "Soto", "pedro@school.cl", "EMAIL_PASSWORD");
        when(teacherRepository.findById("uid-ep")).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() ->
            service.resetPassword("code-xyz",
                new ResetPasswordRequest("wrong@school.cl", "pass12", "pass12")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException rse = (ResponseStatusException) e;
                assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                assertThat(rse.getReason()).isEqualTo("RESET_CODE_EMAIL_MISMATCH");
            });
    }

    @Test
    void resetPassword_valid_updatesPasswordAndMarksCodeUsed() {
        PasswordResetCodeEntity code = new PasswordResetCodeEntity(
            "uid-ep", "code-xyz", Instant.now().plus(30, ChronoUnit.MINUTES));
        when(codeRepository.findByCode("code-xyz")).thenReturn(Optional.of(code));

        TeacherEntity teacher = new TeacherEntity("uid-ep", "Pedro", "Soto", "pedro@school.cl", "EMAIL_PASSWORD");
        when(teacherRepository.findById("uid-ep")).thenReturn(Optional.of(teacher));

        service.resetPassword("code-xyz",
            new ResetPasswordRequest("pedro@school.cl", "nuevaPass99", "nuevaPass99"));

        verify(authPort).updatePassword("uid-ep", "nuevaPass99");
        assertThat(code.isUsed()).isTrue();
        verify(codeRepository).save(code);
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**
```bash
./mvnw test -Dtest=PasswordResetServiceTest
```
Expected: FAIL — `PasswordResetService` class not found.

- [ ] **Step 4: Implement PasswordResetService**

Create `src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java`:
```java
package cl.gradeops.ai.api.auth;

import cl.gradeops.ai.api.config.GradeOpsEmailProperties;
import cl.gradeops.ai.api.config.GradeOpsWebProperties;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import cl.gradeops.ai.api.email.EmailService;
import cl.gradeops.ai.api.port.AuthPort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final TeacherRepository teacherRepository;
    private final PasswordResetCodeRepository codeRepository;
    private final EmailService emailService;
    private final AuthPort authPort;
    private final GradeOpsEmailProperties emailProperties;
    private final GradeOpsWebProperties webProperties;

    public PasswordResetService(TeacherRepository teacherRepository,
                                PasswordResetCodeRepository codeRepository,
                                EmailService emailService,
                                AuthPort authPort,
                                GradeOpsEmailProperties emailProperties,
                                GradeOpsWebProperties webProperties) {
        this.teacherRepository = teacherRepository;
        this.codeRepository = codeRepository;
        this.emailService = emailService;
        this.authPort = authPort;
        this.emailProperties = emailProperties;
        this.webProperties = webProperties;
    }

    @Transactional
    public void sendResetEmail(ForgotPasswordRequest request) {
        Optional<TeacherEntity> maybeTeacher = teacherRepository.findByEmail(request.email());
        if (maybeTeacher.isEmpty()) return;

        TeacherEntity teacher = maybeTeacher.get();
        if (!"EMAIL_PASSWORD".equals(teacher.getProvider())) return;

        String rawCode = UUID.randomUUID().toString();
        int ttlMinutes = emailProperties.getResetPassword().getTtlMinutes();
        Instant expiresAt = Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES);

        codeRepository.deleteByTeacherUid(teacher.getFirebaseUid());
        codeRepository.save(new PasswordResetCodeEntity(teacher.getFirebaseUid(), rawCode, expiresAt));

        String resetLink = webProperties.getBaseUrl() + "/reset-password?code=" + rawCode;
        emailService.sendPasswordReset(teacher.getEmail(), teacher.getFirstName(), resetLink);
    }

    @Transactional
    public void resetPassword(String code, ResetPasswordRequest request) {
        PasswordResetCodeEntity resetCode = codeRepository.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RESET_CODE_NOT_FOUND"));

        if (resetCode.isExpired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "RESET_CODE_EXPIRED");
        }
        if (resetCode.isUsed()) {
            throw new ResponseStatusException(HttpStatus.GONE, "RESET_CODE_USED");
        }

        TeacherEntity teacher = teacherRepository.findById(resetCode.getTeacherUid())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RESET_CODE_NOT_FOUND"));

        if (!teacher.getEmail().equalsIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "RESET_CODE_EMAIL_MISMATCH");
        }

        authPort.updatePassword(teacher.getFirebaseUid(), request.password());
        resetCode.markUsed();
        codeRepository.save(resetCode);
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**
```bash
./mvnw test -Dtest=PasswordResetServiceTest
```
Expected: All 9 tests PASS.

- [ ] **Step 6: Commit**
```bash
git add \
  src/main/java/cl/gradeops/ai/api/auth/ForgotPasswordRequest.java \
  src/main/java/cl/gradeops/ai/api/auth/ResetPasswordRequest.java \
  src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java \
  src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java
git commit -m "feat(api): implement PasswordResetService with sendResetEmail and resetPassword"
```

---

## Task 6: GlobalExceptionHandler + AuthController endpoints + SecurityConfig + integration tests

**Files:**
- Modify: `src/main/java/cl/gradeops/ai/api/common/GlobalExceptionHandler.java`
- Modify: `src/main/java/cl/gradeops/ai/api/auth/AuthController.java`
- Modify: `src/main/java/cl/gradeops/ai/api/security/SecurityConfig.java`
- Create: `src/test/java/cl/gradeops/ai/api/auth/PasswordResetControllerTest.java`

**Interfaces:**
- Consumes: `PasswordResetService.sendResetEmail(ForgotPasswordRequest)` (from Task 5)
- Consumes: `PasswordResetService.resetPassword(String, ResetPasswordRequest)` (from Task 5)
- Produces: `POST /api/v1/auth/forgot-password` → HTTP 200
- Produces: `PUT /api/v1/auth/reset-password?code=<UUID>` → HTTP 200 / 404 / 410 / 422

---

- [ ] **Step 1: Add ResponseStatusException handler to GlobalExceptionHandler**

In `src/main/java/cl/gradeops/ai/api/common/GlobalExceptionHandler.java`, add this handler **before** the catch-all `Exception` handler:

```java
import org.springframework.web.server.ResponseStatusException;

@ExceptionHandler(ResponseStatusException.class)
public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
    String errorCode = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
    return ResponseEntity.status(ex.getStatusCode()).body(ApiError.of(errorCode));
}
```

- [ ] **Step 2: Write failing integration tests**

Create `src/test/java/cl/gradeops/ai/api/auth/PasswordResetControllerTest.java`:
```java
package cl.gradeops.ai.api.auth;

import cl.gradeops.ai.api.config.FirebaseTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class PasswordResetControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean PasswordResetService passwordResetService;

    // ─── POST /forgot-password ──────────────────────────────────────────────────

    @Test
    void forgotPassword_validEmail_returns200() throws Exception {
        doNothing().when(passwordResetService).sendResetEmail(any());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "teacher@school.cl"}
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void forgotPassword_blankEmail_returns422() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": ""}
                    """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void forgotPassword_invalidEmailFormat_returns422() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "not-an-email"}
                    """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    // ─── PUT /reset-password ───────────────────────────────────────────────────

    @Test
    void resetPassword_validRequest_returns200() throws Exception {
        doNothing().when(passwordResetService).resetPassword(eq("valid-code"), any());

        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "valid-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void resetPassword_codeNotFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "RESET_CODE_NOT_FOUND"))
            .when(passwordResetService).resetPassword(eq("bad-code"), any());

        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "bad-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("RESET_CODE_NOT_FOUND"));
    }

    @Test
    void resetPassword_codeExpired_returns410WithExpiredCode() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.GONE, "RESET_CODE_EXPIRED"))
            .when(passwordResetService).resetPassword(eq("expired-code"), any());

        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "expired-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.error").value("RESET_CODE_EXPIRED"));
    }

    @Test
    void resetPassword_codeUsed_returns410WithUsedCode() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.GONE, "RESET_CODE_USED"))
            .when(passwordResetService).resetPassword(eq("used-code"), any());

        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "used-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.error").value("RESET_CODE_USED"));
    }

    @Test
    void resetPassword_emailMismatch_returns422() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "RESET_CODE_EMAIL_MISMATCH"))
            .when(passwordResetService).resetPassword(eq("ok-code"), any());

        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "ok-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"wrong@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("RESET_CODE_EMAIL_MISMATCH"));
    }

    @Test
    void resetPassword_shortPassword_returns422() throws Exception {
        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "ok-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"abc","passwordRepeat":"abc"}
                    """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void resetPassword_noCodeParam_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isBadRequest());
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**
```bash
./mvnw test -Dtest=PasswordResetControllerTest
```
Expected: FAIL — endpoints not registered yet.

- [ ] **Step 4: Add endpoints to AuthController**

In `src/main/java/cl/gradeops/ai/api/auth/AuthController.java`:

1. Add `PasswordResetService` dependency (constructor injection):
```java
private final PasswordResetService passwordResetService;

public AuthController(AuthService authService, PasswordResetService passwordResetService) {
    this.authService = authService;
    this.passwordResetService = passwordResetService;
}
```

2. Add the two new endpoints at the end of the class:
```java
@PostMapping("/forgot-password")
@ResponseStatus(HttpStatus.OK)
public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    passwordResetService.sendResetEmail(request);
}

@PutMapping("/reset-password")
@ResponseStatus(HttpStatus.OK)
public void resetPassword(@RequestParam String code,
                          @Valid @RequestBody ResetPasswordRequest request) {
    passwordResetService.resetPassword(code, request);
}
```

- [ ] **Step 5: Update SecurityConfig whitelist**

In `src/main/java/cl/gradeops/ai/api/security/SecurityConfig.java`, add to the `requestMatchers` block:
```java
.requestMatchers("/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
```

The updated `.authorizeHttpRequests` block should look like:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/internal/**").permitAll()
    .requestMatchers("/api/v1/auth/register").permitAll()
    .requestMatchers("/api/v1/auth/verify/resend").permitAll()
    .requestMatchers("/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
    .anyRequest().authenticated()
)
```

- [ ] **Step 6: Run tests to verify they pass**
```bash
./mvnw test -Dtest=PasswordResetControllerTest
```
Expected: All 8 tests PASS.

- [ ] **Step 7: Run full test suite**
```bash
./mvnw test
```
Expected: All tests pass including pre-existing ones. Confirm no regressions.

- [ ] **Step 8: Commit**
```bash
git add \
  src/main/java/cl/gradeops/ai/api/common/GlobalExceptionHandler.java \
  src/main/java/cl/gradeops/ai/api/auth/AuthController.java \
  src/main/java/cl/gradeops/ai/api/security/SecurityConfig.java \
  src/test/java/cl/gradeops/ai/api/auth/PasswordResetControllerTest.java
git commit -m "feat(api): add POST /forgot-password and PUT /reset-password endpoints"
```

---

## Verification Checklist

Before declaring Scope 01 (API) done, manually verify end-to-end with Mailtrap:

- [ ] Configure `SMTP_USERNAME` and `SMTP_PASSWORD` in `application-local.yml` using Mailtrap inbox credentials
- [ ] Start the API: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`
- [ ] `POST /api/v1/auth/forgot-password` with a known email/password teacher → 200, email appears in Mailtrap
- [ ] `POST /api/v1/auth/forgot-password` with an unknown email → 200, no email sent
- [ ] Click the link in the Mailtrap email → opens `/reset-password?code=<UUID>` in browser
- [ ] `PUT /api/v1/auth/reset-password?code=<UUID>` with correct email + password → 200
- [ ] Second `PUT` with same code → 410 `RESET_CODE_USED`
- [ ] `PUT` with invalid code → 404 `RESET_CODE_NOT_FOUND`
- [ ] `PUT` with mismatched email → 422 `RESET_CODE_EMAIL_MISMATCH`
- [ ] Sign in with new password on the frontend → succeeds
