# 🌱 INITIAL: 007-password-recovery-custom-email

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

Reemplazar el flujo de recuperación de contraseña basado en Firebase (`sendPasswordResetEmail`) por un email service propio en `api/` con Thymeleaf + JavaMail + SMTP, manteniendo Firebase Admin SDK sólo para aplicar el cambio de contraseña server-side.

---

## Why

El enfoque de Firebase (planning 006) enviaba el correo directamente desde el cliente usando `sendPasswordResetEmail`, lo que impide personalizar el remitente, el contenido del email y la URL del enlace. Con el nuevo enfoque el backend controla completamente el email, el diseño HTML, el TTL del token y el link generado.

---

## Approximate Scope

- [ ] `api/` — nuevo EmailService (JavaMail + Thymeleaf), PasswordResetService, PasswordResetCodeEntity, endpoints `POST /forgot-password` y `PUT /reset-password`, Flyway V5, AuthPort.updatePassword()
- [ ] `web/` — actualizar `/forgot-password` (reemplazar Firebase SDK por llamada API), reescribir `/reset-password` (quitar Firebase, agregar campo email, leer `?code=`, llamar PUT endpoint)
- [ ] `infra/` — secrets SMTP en Secret Manager, env vars en Cloud Run (`api/`)
- [ ] `docs/` — ✅ ya actualizado en sesión de diseño (US-012, API Reference, spec)

---

## Replaces

Planning `006-password-recovery` (SUPERSEDED). Los archivos web creados en 006 (`/forgot-password`, `/reset-password`) se modifican en este planning — no se eliminan.

---

## Design

Spec aprobado: `docs/superpowers/specs/2026-06-21-password-recovery-custom-email-design.md`
Spec de implementación API: `api/docs/password-reset-implementation.md`

---

## API Contracts (resumen)

```
POST /api/v1/auth/forgot-password   Body: { email }          → 200 always
PUT  /api/v1/auth/reset-password    ?code=<UUID>
                                     Body: { email, password, passwordRepeat }
                                    → 200 | 404 | 410 | 422
```

Error codes: `RESET_CODE_NOT_FOUND` (404), `RESET_CODE_EXPIRED` (410), `RESET_CODE_USED` (410), `RESET_CODE_EMAIL_MISMATCH` (422).

---

## Initiator

- **Requested by:** human
- **Date:** 2026-06-21
- **Replaces planning:** 006-password-recovery

---

## Next Step

- [ ] Ejecutar `/plan-expand 007-password-recovery-custom-email` para crear los scopes de implementación.

---

> [← planning/README.md](../../README.md)
