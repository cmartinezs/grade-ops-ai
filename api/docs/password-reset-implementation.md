# Implementation Spec: Password Reset — Custom Email Flow

**Planning:** 007-password-recovery-custom-email — Scope 01 (API)
**Status:** Ready to implement
**Design source:** `docs/superpowers/specs/2026-06-21-password-recovery-custom-email-design.md`

This document provides the full technical detail needed to implement the custom password reset email service in `api/`. Read this completely before writing any code.

---

## Overview

Replace Firebase's `sendPasswordResetEmail` with a backend-owned flow:

1. `POST /api/v1/auth/forgot-password` — generates a UUID code, stores it, sends a Thymeleaf HTML email.
2. `PUT /api/v1/auth/reset-password?code=<UUID>` — validates code + email, calls Firebase Admin SDK `updateUser` to set the new password.

---

## 1. New Maven dependencies (`pom.xml`)

Add inside `<dependencies>`:

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

---

## 2. Flyway migration: `V5__add_password_reset_codes.sql`

**Path:** `src/main/resources/db/migration/V5__add_password_reset_codes.sql`

```sql
CREATE TABLE password_reset_codes (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_uid VARCHAR     NOT NULL REFERENCES teachers(uid) ON DELETE CASCADE,
    code        VARCHAR(36) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_prc_teacher_uid ON password_reset_codes(teacher_uid);
```

---

## 3. JPA Entity

**Package:** `cl.gradeops.ai.api.auth`
**File:** `PasswordResetCodeEntity.java`

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
    void prePersist() {
        this.createdAt = Instant.now();
    }

    // Constructor for creating new codes
    public PasswordResetCodeEntity(String teacherUid, String code, Instant expiresAt) {
        this.teacherUid = teacherUid;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    protected PasswordResetCodeEntity() {}

    // Getters
    public UUID getId()            { return id; }
    public String getTeacherUid()  { return teacherUid; }
    public String getCode()        { return code; }
    public Instant getExpiresAt()  { return expiresAt; }
    public Instant getUsedAt()     { return usedAt; }
    public Instant getCreatedAt()  { return createdAt; }

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isUsed()    { return usedAt != null; }

    public void markUsed() { this.usedAt = Instant.now(); }
}
```

---

## 4. Repository

**Package:** `cl.gradeops.ai.api.auth`
**File:** `PasswordResetCodeRepository.java`

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

---

## 5. Request DTOs

**Package:** `cl.gradeops.ai.api.auth`

**File:** `ForgotPasswordRequest.java`
```java
package cl.gradeops.ai.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank @Email String email
) {}
```

**File:** `ResetPasswordRequest.java`
```java
package cl.gradeops.ai.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.") String password,
    @NotBlank String passwordRepeat
) {}
```

---

## 6. Configuration Properties

**Package:** `cl.gradeops.ai.api.config`
**File:** `GradeOpsEmailProperties.java`

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

        public int getTtlMinutes()  { return ttlMinutes; }
        public String getSubject()  { return subject; }
        public void setTtlMinutes(int v)   { this.ttlMinutes = v; }
        public void setSubject(String v)   { this.subject = v; }
    }

    public String getFromAddress()           { return fromAddress; }
    public String getFromName()              { return fromName; }
    public String getSupportEmail()          { return supportEmail; }
    public ResetPassword getResetPassword()  { return resetPassword; }
    public void setFromAddress(String v)     { this.fromAddress = v; }
    public void setFromName(String v)        { this.fromName = v; }
    public void setSupportEmail(String v)    { this.supportEmail = v; }
    public void setResetPassword(ResetPassword v) { this.resetPassword = v; }
}
```

**File:** `GradeOpsWebProperties.java`

```java
package cl.gradeops.ai.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gradeops.web")
public class GradeOpsWebProperties {

    private String baseUrl = "http://localhost:3000";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String v) { this.baseUrl = v; }
}
```

---

## 7. Email Service

**Package:** `cl.gradeops.ai.api.email`
**File:** `EmailService.java`

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

import java.util.Map;

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

---

## 8. Thymeleaf Email Template

**Path:** `src/main/resources/templates/email/password-reset.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Restablecer contraseña</title>
  <style>
    body { margin: 0; padding: 0; background: #f4f4f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
    .wrapper { max-width: 560px; margin: 40px auto; background: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
    .header { background: linear-gradient(135deg, #22c55e, #16a34a); padding: 28px 32px; }
    .header h1 { margin: 0; color: #fff; font-size: 20px; font-weight: 700; letter-spacing: -0.02em; }
    .header p { margin: 4px 0 0; color: rgba(255,255,255,0.85); font-size: 13px; }
    .body { padding: 32px; }
    .body p { margin: 0 0 16px; color: #374151; font-size: 15px; line-height: 1.6; }
    .cta { text-align: center; margin: 28px 0; }
    .cta a { display: inline-block; background: #16a34a; color: #fff; text-decoration: none; padding: 13px 28px; border-radius: 8px; font-weight: 600; font-size: 15px; }
    .link-fallback { background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; padding: 12px 16px; margin: 20px 0; word-break: break-all; font-size: 12px; color: #6b7280; }
    .notice { background: #fefce8; border-left: 3px solid #eab308; padding: 12px 16px; border-radius: 0 6px 6px 0; margin: 20px 0; font-size: 13px; color: #713f12; }
    .footer { padding: 20px 32px; border-top: 1px solid #f3f4f6; font-size: 12px; color: #9ca3af; text-align: center; }
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

    <p style="font-size:13px; color:#6b7280;">Si el botón no funciona, copia y pega este enlace en tu navegador:</p>
    <div class="link-fallback" th:text="${resetLink}"></div>

    <div class="notice">
      Este enlace es válido por <strong th:text="${expiresInMinutes}">30</strong> minutos. Si no solicitaste este cambio, puedes ignorar este correo — tu contraseña no será modificada.
    </div>

    <p>Si tienes preguntas, escríbenos a <a th:href="'mailto:' + ${supportEmail}" th:text="${supportEmail}">soporte@gradeops.app</a>.</p>
  </div>

  <div class="footer">
    &copy; 2026 <span th:text="${appName}">GradeOps AI</span> · Portal docente · Este correo fue generado automáticamente.
  </div>

</div>
</body>
</html>
```

---

## 9. AuthPort — New Method

**File:** `src/main/java/cl/gradeops/ai/api/port/AuthPort.java`

Add to the existing interface:
```java
void updatePassword(String uid, String newPassword);
```

**File:** `src/main/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapter.java`

Add implementation:
```java
@Override
public void updatePassword(String uid, String newPassword) {
    try {
        firebaseAuth.updateUser(new com.google.firebase.auth.UserRecord.UpdateRequest(uid)
            .setPassword(newPassword));
    } catch (FirebaseAuthException e) {
        throw new RuntimeException("Failed to update password for uid " + uid, e);
    }
}
```

---

## 10. Password Reset Service

**Package:** `cl.gradeops.ai.api.auth`
**File:** `PasswordResetService.java`

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

        // Silently succeed if not found or is a Google-only account
        if (maybeTeacher.isEmpty()) return;
        TeacherEntity teacher = maybeTeacher.get();
        if (!"EMAIL_PASSWORD".equals(teacher.getProvider())) return;

        String rawCode = UUID.randomUUID().toString();
        int ttlMinutes = emailProperties.getResetPassword().getTtlMinutes();
        Instant expiresAt = Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES);

        // Upsert: delete any existing code for this teacher, then insert fresh one
        codeRepository.deleteByTeacherUid(teacher.getUid());
        codeRepository.save(new PasswordResetCodeEntity(teacher.getUid(), rawCode, expiresAt));

        String resetLink = webProperties.getBaseUrl() + "/reset-password?code=" + rawCode;
        emailService.sendPasswordReset(teacher.getEmail(), teacher.getFirstName(), resetLink);
    }

    @Transactional
    public void resetPassword(String code, ResetPasswordRequest request) {
        PasswordResetCodeEntity resetCode = codeRepository.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "RESET_CODE_NOT_FOUND"));

        if (resetCode.isExpired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "RESET_CODE_EXPIRED");
        }
        if (resetCode.isUsed()) {
            throw new ResponseStatusException(HttpStatus.GONE, "RESET_CODE_USED");
        }

        TeacherEntity teacher = teacherRepository.findById(resetCode.getTeacherUid())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "RESET_CODE_NOT_FOUND"));

        if (!teacher.getEmail().equalsIgnoreCase(request.email())) {
            throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY, "RESET_CODE_EMAIL_MISMATCH");
        }

        authPort.updatePassword(teacher.getUid(), request.password());
        resetCode.markUsed();
        codeRepository.save(resetCode);
    }
}
```

**Important:** `TeacherRepository` must have a `findByEmail(String email)` method. Check if it already exists; if not, add it:
```java
Optional<TeacherEntity> findByEmail(String email);
```

Also verify `TeacherEntity` has `getProvider()`, `getFirstName()`, `getEmail()`, `getUid()` getters.

---

## 11. Error Response Body

The existing `ResponseStatusException` approach used elsewhere in the API returns the HTTP status reason as the body. To match the structured `{"error": "CODE"}` format documented in the API reference, add a `@RestControllerAdvice` handler if not already present — or extend the existing one — to map `ResponseStatusException` to:

```json
{ "error": "<reason phrase from ResponseStatusException>" }
```

Check `src/main/java/cl/gradeops/ai/api/` for an existing `GlobalExceptionHandler` or `@ControllerAdvice`. If one exists, add a handler for `ResponseStatusException` that extracts `exception.getReason()` as the error code. If none exists, create:

**Package:** `cl.gradeops.ai.api.config`
**File:** `GlobalExceptionHandler.java` (or add to existing)

```java
@ExceptionHandler(ResponseStatusException.class)
public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
    String errorCode = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
    return ResponseEntity.status(ex.getStatusCode())
        .body(Map.of("error", errorCode));
}
```

---

## 12. Controller — New Endpoints

**File:** `src/main/java/cl/gradeops/ai/api/auth/AuthController.java`

Add to the existing `AuthController`:

```java
private final PasswordResetService passwordResetService;

// Add to constructor

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

---

## 13. SecurityConfig — Whitelist New Endpoints

**File:** `src/main/java/cl/gradeops/ai/api/security/SecurityConfig.java`

Add both new paths to the existing `permitAll()` list (where `/api/v1/auth/register` is already listed):

```java
.requestMatchers("/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
```

---

## 14. YAML Configuration Changes

**`application.yml`** — add after the existing `app:` block:
```yaml
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

spring:
  mail:
    host: ${SMTP_HOST:localhost}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USERNAME:}
    password: ${SMTP_PASSWORD:}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

**`application-local.yml`** — add:
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

---

## 15. Tests to Write

### `PasswordResetServiceTest.java` (unit test, mock dependencies)

| Test | Scenario |
|------|---------|
| `sendResetEmail_unknownEmail_doesNothing` | `teacherRepository.findByEmail` returns empty — no email sent, no exception |
| `sendResetEmail_googleProvider_doesNothing` | Teacher found but `provider = "GOOGLE"` — no email sent |
| `sendResetEmail_emailPassword_sendsEmail` | Teacher found, email/password — code saved, `emailService.sendPasswordReset` called with correct args |
| `sendResetEmail_deletesExistingCode` | Calling twice deletes previous code before inserting new |
| `resetPassword_codeNotFound_throws404` | Repository returns empty — `ResponseStatusException(NOT_FOUND)` |
| `resetPassword_codeExpired_throws410` | Code's `expires_at` is in the past — `ResponseStatusException(GONE, "RESET_CODE_EXPIRED")` |
| `resetPassword_codeUsed_throws410` | Code has `used_at` set — `ResponseStatusException(GONE, "RESET_CODE_USED")` |
| `resetPassword_emailMismatch_throws422` | Teacher email ≠ request email — `ResponseStatusException(UNPROCESSABLE_ENTITY, "RESET_CODE_EMAIL_MISMATCH")` |
| `resetPassword_valid_updatesPasswordAndMarksUsed` | All valid — `authPort.updatePassword(uid, password)` called, `code.usedAt` set |

### `AuthControllerTest.java` (integration test — extend existing)

Add to the existing `AuthControllerTest`:

| Test | Endpoint | Scenario |
|------|---------|---------|
| `forgotPassword_validEmail_returns200` | POST /auth/forgot-password | Valid request body → 200, service called |
| `forgotPassword_invalidEmail_returns422` | POST /auth/forgot-password | Missing/invalid email → 422 |
| `resetPassword_validCode_returns200` | PUT /auth/reset-password?code=x | Service succeeds → 200 |
| `resetPassword_codeNotFound_returns404` | PUT /auth/reset-password?code=x | Service throws NOT_FOUND → 404 |
| `resetPassword_codeExpired_returns410` | PUT /auth/reset-password?code=x | Service throws GONE EXPIRED → 410, body `{"error":"RESET_CODE_EXPIRED"}` |
| `resetPassword_codeUsed_returns410` | PUT /auth/reset-password?code=x | Service throws GONE USED → 410, body `{"error":"RESET_CODE_USED"}` |
| `resetPassword_emailMismatch_returns422` | PUT /auth/reset-password?code=x | Service throws 422 MISMATCH → 422 body `{"error":"RESET_CODE_EMAIL_MISMATCH"}` |
| `resetPassword_missingBody_returns422` | PUT /auth/reset-password?code=x | Missing required fields → 422 |

### `EmailServiceTest.java` (unit test — mock JavaMailSender + TemplateEngine)

| Test | Scenario |
|------|---------|
| `sendPasswordReset_callsTemplateEngineWithCorrectVariables` | Verify `ctx.getVariable("firstName")`, `ctx.getVariable("resetLink")`, etc. |
| `sendPasswordReset_sendsToCorrectAddress` | Verify `MimeMessageHelper.setTo()` called with correct email |
| `sendPasswordReset_throwsOnMailException` | JavaMailSender throws → `RuntimeException` propagated |

---

## 16. TeacherEntity / TeacherRepository — Required Methods

Before implementing, verify these exist:

**`TeacherEntity`:**
- `getUid()` — Firebase UID (PK)
- `getEmail()` — teacher email
- `getFirstName()` — first name for email greeting
- `getProvider()` — `"EMAIL_PASSWORD"` or `"GOOGLE"`

**`TeacherRepository`:**
- `findByEmail(String email)` — returns `Optional<TeacherEntity>`
- `findById(String uid)` — already in JpaRepository

If `findByEmail` doesn't exist, add it:
```java
Optional<TeacherEntity> findByEmail(String email);
```

---

## 17. Verification Checklist

Before marking Scope 01 DONE, verify:

- [ ] `V5__add_password_reset_codes.sql` applied cleanly against local DB
- [ ] `POST /api/v1/auth/forgot-password` returns 200 for unknown email (no error body)
- [ ] `POST /api/v1/auth/forgot-password` sends email via Mailtrap for known email/password teacher
- [ ] `PUT /api/v1/auth/reset-password?code=X` returns 404 with `{"error":"RESET_CODE_NOT_FOUND"}` for invalid code
- [ ] `PUT /api/v1/auth/reset-password?code=X` returns 410 with `{"error":"RESET_CODE_EXPIRED"}` for expired code
- [ ] `PUT /api/v1/auth/reset-password?code=X` returns 410 with `{"error":"RESET_CODE_USED"}` for used code
- [ ] `PUT /api/v1/auth/reset-password?code=X` returns 422 with `{"error":"RESET_CODE_EMAIL_MISMATCH"}` for wrong email
- [ ] `PUT /api/v1/auth/reset-password?code=X` with valid code + matching email updates password in Firebase
- [ ] `PUT /api/v1/auth/reset-password?code=X` marks code `used_at`; second call returns 410
- [ ] Both endpoints return 403 if internal secret is provided (they should NOT be behind internal auth)
- [ ] Both endpoints return 401 if Firebase Bearer token is provided (they should be fully public)
- [ ] All unit and integration tests pass: `./mvnw test`
