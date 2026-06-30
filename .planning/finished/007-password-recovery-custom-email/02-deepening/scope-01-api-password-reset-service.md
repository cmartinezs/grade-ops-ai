# 🔍 DEEPENING: Scope 01 — api-password-reset-service

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Implementar en `api/` el servicio completo de reset de contraseña: migración Flyway, entidad JPA, repositorio, propiedades de configuración, `EmailService` con Thymeleaf, `PasswordResetService`, extensión de `AuthPort` + `FirebaseAuthAdapter`, dos nuevos endpoints en `AuthController`, whitelist en `SecurityConfig`, y YAML de configuración. Incluye tests unitarios e de integración.

**Spec de implementación:** `api/docs/password-reset-implementation.md` (contiene todo el código).

---

## Context

- **Design doc:** `docs/superpowers/specs/2026-06-21-password-recovery-custom-email-design.md`
- **Implementation spec:** `api/docs/password-reset-implementation.md`
- **Base package:** `cl.gradeops.ai.api`
- **Última migración Flyway:** verificar en `api/src/main/resources/db/migration/` antes de nombrar `V5__`.
- **AuthController existente:** ya tiene endpoints de registro y login — añadir los dos nuevos.
- **SecurityConfig existente:** ya tiene `permitAll()` para `/api/v1/auth/register` — añadir los dos nuevos paths.

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Maven dependencies + Flyway migration](scope-01-api-password-reset-service/task-01-deps-flyway.md) | GENERATE-DOCUMENT | DONE | `pom.xml` actualizado, `V5__add_password_reset_codes.sql`, `V6__add_unique_constraint_prc_teacher_uid.sql` |
| 2 | [Entity, Repository y Request DTOs](scope-01-api-password-reset-service/task-02-entity-repo-dtos.md) | GENERATE-DOCUMENT | DONE | `PasswordResetCodeEntity.java`, `PasswordResetCodeRepository.java`, `ForgotPasswordRequest.java`, `ResetPasswordRequest.java` |
| 3 | [Configuration Properties](scope-01-api-password-reset-service/task-03-config-properties.md) | GENERATE-DOCUMENT | DONE | `GradeOpsEmailProperties.java`, `GradeOpsWebProperties.java`, `application.yml` actualizado, `application-local.yml` actualizado |
| 4 | [EmailService + Thymeleaf template](scope-01-api-password-reset-service/task-04-email-service.md) | GENERATE-DOCUMENT | DONE | `EmailService.java`, `templates/email/password-reset.html` |
| 5 | [AuthPort.updatePassword + FirebaseAuthAdapter](scope-01-api-password-reset-service/task-05-auth-port.md) | GENERATE-DOCUMENT | DONE | `AuthPort.java` (método nuevo), `FirebaseAuthAdapter.java` (implementación) |
| 6 | [PasswordResetService](scope-01-api-password-reset-service/task-06-password-reset-service.md) | GENERATE-DOCUMENT | DONE | `PasswordResetService.java` |
| 7 | [AuthController endpoints + SecurityConfig + GlobalExceptionHandler](scope-01-api-password-reset-service/task-07-controller-security.md) | GENERATE-DOCUMENT | DONE | `AuthController.java` (2 endpoints nuevos), `SecurityConfig.java` (whitelist), `GlobalExceptionHandler.java` (extendido con `ResponseStatusException`) |
| 8 | [Tests unitarios e de integración](scope-01-api-password-reset-service/task-08-tests.md) | GENERATE-DOCUMENT | DONE | `PasswordResetServiceTest.java` (10 tests), `EmailServiceTest.java` (8 tests), `PasswordResetControllerTest.java` (10 tests), `PasswordResetCodeEntityTest.java` |

---

## Approach Details

Ver `api/docs/password-reset-implementation.md` para el código completo de cada tarea. El scope file es la guía de qué implementar; el implementation spec es el cómo.

### Orden de ejecución recomendado

```
task-01 (deps + flyway) → task-02 (entity) → task-03 (config) →
task-04 (email) → task-05 (authport) → task-06 (service) → task-07 (controller) → task-08 (tests)
```

### Verificaciones pre-implementación

Antes de task-02, verificar que `TeacherEntity` tiene `getUid()`, `getEmail()`, `getFirstName()`, `getProvider()`.  
Antes de task-02, verificar que `TeacherRepository` tiene `findByEmail(String email)` — si no, agregarlo.  
Antes de task-07, verificar si existe un `GlobalExceptionHandler` o `@ControllerAdvice` — si existe, extenderlo; si no, crearlo.

---

## Done Criteria

- [x] `V5__add_password_reset_codes.sql` migra limpiamente contra la DB local (H2 en tests + PostgreSQL local)
- [x] `POST /api/v1/auth/forgot-password` retorna 200 para email desconocido (sin body, sin log de error)
- [x] `POST /api/v1/auth/forgot-password` envía email vía Mailtrap para teacher EMAIL_PASSWORD conocido
- [x] `POST /api/v1/auth/forgot-password` retorna 200 silenciosamente para teacher GOOGLE
- [x] `PUT /api/v1/auth/reset-password?code=X` retorna 404 `{"error":"RESET_CODE_NOT_FOUND"}` para código inválido
- [x] `PUT /api/v1/auth/reset-password?code=X` retorna 410 `{"error":"RESET_CODE_EXPIRED"}` para código expirado
- [x] `PUT /api/v1/auth/reset-password?code=X` retorna 410 `{"error":"RESET_CODE_USED"}` para código ya usado
- [x] `PUT /api/v1/auth/reset-password?code=X` retorna 422 `{"error":"RESET_CODE_EMAIL_MISMATCH"}` para email incorrecto
- [x] `PUT /api/v1/auth/reset-password?code=X` con código válido + email correcto actualiza la contraseña en Firebase y marca `used_at`
- [x] Segunda llamada con el mismo código retorna 410 `RESET_CODE_USED`
- [x] Ambos endpoints son públicos (no requieren Bearer token ni internal key)
- [x] `./mvnw test` pasa sin errores — 65/65 tests pasan (suite completa)
- [x] TRACEABILITY.md actualizado

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | Spec dice `getUid()` pero `TeacherEntity` usa `getFirebaseUid()` — implementación usó el getter correcto | `api/docs/password-reset-implementation.md` vs `TeacherEntity.java` | RESOLVED | `PasswordResetService` usa `getFirebaseUid()` correctamente |
| 2 | Spec dice `REFERENCES teachers(uid)` en Flyway — tabla real es `teacher` con PK `firebase_uid` | `password-reset-implementation.md` vs V1 migration | RESOLVED | V5 migration usa `REFERENCES teacher(firebase_uid)` correctamente |
| 3 | Se agregó V6 migration (`UNIQUE` constraint en `teacher_uid`) — no estaba en la spec | V5 spec vs implementación real | ACCEPTED | Mejora de integridad: garantiza un solo código activo por teacher a nivel DB además del delete previo |
| 4 | `PasswordResetService.resetPassword()` llama `revokeRefreshTokens()` después de cambiar la contraseña — no estaba en la spec | `password-reset-implementation.md` | ACCEPTED | Mejora de seguridad: los tokens de sesión activos con la contraseña antigua quedan invalidados |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)
