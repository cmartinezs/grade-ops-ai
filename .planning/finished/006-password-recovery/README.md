# 📐 Planning 006 — password-recovery

> **Status:** DONE — SUPERSEDED por `007-password-recovery-custom-email`
> [← planning/README.md](../../README.md)

---

## Objetivo

Implementar el flujo de recuperación de contraseña para docentes registrados con correo y contraseña: página `/forgot-password` con formulario de email, y página custom `/reset-password` como handler de los links de Firebase Auth.

---

## Archivos de este planning

| Archivo | Fase | Estado |
|---------|------|--------|
| [00-initial.md](00-initial.md) | INITIAL | ✅ |
| [01-expansion.md](01-expansion.md) | EXPANSION | ✅ SUPERSEDED |
| [02-deepening/scope-01-forgot-password-page.md](02-deepening/scope-01-forgot-password-page.md) | DEEPENING | DONE |
| [02-deepening/scope-02-docs-us012-dod.md](02-deepening/scope-02-docs-us012-dod.md) | DEEPENING | DONE |
| [02-deepening/scope-03-reset-password-page.md](02-deepening/scope-03-reset-password-page.md) | DEEPENING | DONE |
| [TRACEABILITY.md](TRACEABILITY.md) | — | — |

---

## Retrospective

### Qué se entregó

3/3 scopes completados. Se implementaron `/forgot-password` (formulario RHF+Zod, llamada a `sendPasswordResetEmail`, mensaje neutral anti-enumeración) y `/reset-password` (handler custom de Firebase con `verifyPasswordResetCode` + `confirmPasswordReset`, validación de `oobCode` y `mode`, 6 tests). US-012 fue marcada como implementada en docs.

### Por qué está SUPERSEDED

Durante la ejecución del plan se decidió reemplazar `sendPasswordResetEmail` (Firebase SDK client-side) por un email service propio en `api/` (JavaMail + Thymeleaf), para tener control total sobre el template del correo y eliminar la dependencia del correo genérico de Firebase. Ese cambio lo ejecuta el plan 007.

Las páginas `/forgot-password` y `/reset-password` siguen existiendo en `web/` — solo cambió el backend que las alimenta: en lugar de llamar directamente a Firebase Auth SDK, ahora llaman a `POST /api/v1/auth/forgot-password` y `POST /api/v1/auth/reset-password`.

### Decisiones clave

- **Mensaje neutral en `/forgot-password`:** éxito y `auth/user-not-found` muestran el mismo texto ("Si existe una cuenta con ese correo, recibirás un enlace en los próximos minutos.") — previene user enumeration.
- **`auth/invalid-email` sí muestra error inline** — es un error de formato validable antes del envío, no de existencia de cuenta.
- **Página `/reset-password` custom en lugar de pantalla default de Firebase** — para mantener el look&feel del DS y control sobre el flujo de UX.

### Residuales al cerrar

- Configurar Firebase Console Custom Action URL en el entorno `demo` — quedó pendiente, pero irrelevante: 007 elimina Firebase del flujo de email, por lo que esta configuración ya no se necesita.

---

> [← planning/README.md](../../README.md)
