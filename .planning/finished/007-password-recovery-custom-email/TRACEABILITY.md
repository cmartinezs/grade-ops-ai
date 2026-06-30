# 🔗 Traceability: 007-password-recovery-custom-email

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

| Code | Area |
|------|------|
| AG | Agent Runtime (`agents/`) |
| AP | Backend / Domain (`api/`) |
| DO | Documentation (`docs/`) |
| IN | Infrastructure (`infra/`) |
| WB | Frontend (`web/`) |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

| Term / Concept | AG | AP | DO | IN | WB | W | Notes |
|---------------|----|----|----|----|----|---|-------|
| `PasswordResetCodeEntity` | N/A | | ✅ | N/A | N/A | ✅ | Entidad JPA en `cl.gradeops.ai.api.auth` |
| `PasswordResetCodeRepository` | N/A | | N/A | N/A | N/A | ✅ | |
| `PasswordResetService` | N/A | ✅ | ✅ | N/A | N/A | ✅ | Lógica: generar código, upsert, enviar email, validar reset, revokeRefreshTokens tras reset |
| `EmailService` (JavaMail + Thymeleaf) | N/A | ✅ | ✅ | N/A | N/A | ✅ | Nuevo paquete `cl.gradeops.ai.api.email` |
| `password-reset.html` (Thymeleaf template) | N/A | ✅ | ✅ | N/A | N/A | ✅ | `templates/email/password-reset.html` |
| `GradeOpsEmailProperties` | N/A | ✅ | N/A | N/A | N/A | ✅ | `@ConfigurationProperties(prefix = "gradeops.email")` |
| `GradeOpsWebProperties` | N/A | ✅ | N/A | N/A | N/A | ✅ | `@ConfigurationProperties(prefix = "gradeops.web")` |
| `AuthPort.updatePassword()` | N/A | ✅ | ✅ | N/A | N/A | ✅ | Nueva operación en el port; implementada en `FirebaseAuthAdapter` |
| `POST /api/v1/auth/forgot-password` | N/A | ✅ | ✅ | N/A | ✅ | ✅ | Endpoint público; siempre 200 |
| `PUT /api/v1/auth/reset-password` | N/A | ✅ | ✅ | N/A | ✅ | ✅ | Endpoint público; `?code=UUID` + body `{email, password, passwordRepeat}` |
| `password_reset_codes` (tabla DB) | N/A | ✅ | ✅ | N/A | N/A | ✅ | Flyway V5+V6; TTL 30 min, single-use, UNIQUE(teacher_uid), upsert con delete previo |
| SMTP secrets en Secret Manager | N/A | N/A | N/A | ✅ | N/A | ✅ | `smtp.tf` define los secretos; las versiones se cargan manualmente post-deploy |
| `GRADEOPS_WEB_BASE_URL` (Cloud Run env) | N/A | N/A | N/A | ✅ | N/A | ✅ | Env var definida para `api/`; reemplazar placeholder por URL real antes del próximo apply |
| `forgotPassword()` (web API function) | N/A | N/A | N/A | N/A | ✅ | ✅ | Implementado en `web/src/lib/api/auth.ts` |
| `resetPassword()` (web API function) | N/A | N/A | N/A | N/A | ✅ | ✅ | Implementado en `web/src/lib/api/auth.ts` |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D01 | Token UUID v4 propio en lugar de Firebase oobCode | Control total sobre TTL, formato del link, y almacenamiento; elimina dependencia de Firebase para el flujo de email | AP, WB | 2026-06-21 |
| D02 | `email` como factor adicional en PUT reset-password | El atacante que intercepte el link también necesita el email — defensa en profundidad | AP, WB | 2026-06-21 |
| D03 | Upsert con delete previo (un código activo por teacher) | Previene acumulación de códigos y evita confusion con tokens anteriores | AP | 2026-06-21 |
| D04 | Firebase Admin SDK solo para `updatePassword` — no para el flujo de email | Firebase sigue siendo el IdP; el cambio de contraseña debe aplicarse en Firebase. Solo se elimina el envío de correo de Firebase | AP | 2026-06-21 |
| D05 | Mailtrap para SMTP en entorno local | Sandbox gratuita, cero riesgo de envío accidental a emails reales durante desarrollo | AP, IN | 2026-06-21 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| — | *None* | — | — | — |

---

> [← planning/README.md](../../README.md)
