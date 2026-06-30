# 📐 Planning 007 — password-recovery-custom-email

> **Status:** DONE — COMPLETED 2026-06-30
> **Supersedes:** `006-password-recovery` (Camino B — sobreescribir)
> [← planning/README.md](../../README.md)

---

## Objetivo

Reemplazar el flujo de recuperación de contraseña basado en Firebase SDK (`sendPasswordResetEmail`) por un email service propio en `api/` con JavaMail + Thymeleaf + SMTP, manteniendo Firebase Admin SDK únicamente para aplicar el cambio de contraseña server-side. Las páginas `/forgot-password` y `/reset-password` de `web/` sobreviven pero su lógica interna se reescribe.

---

## Archivos de este planning

| Archivo | Fase | Estado |
|---------|------|--------|
| [00-initial.md](00-initial.md) | INITIAL | ✅ |
| [01-expansion.md](01-expansion.md) | EXPANSION | ✅ |
| [02-deepening/scope-01-api-password-reset-service.md](02-deepening/scope-01-api-password-reset-service.md) | DEEPENING | DONE |
| [02-deepening/scope-02-web-auth-pages.md](02-deepening/scope-02-web-auth-pages.md) | DEEPENING | DONE |
| [02-deepening/scope-03-infra-smtp-secrets.md](02-deepening/scope-03-infra-smtp-secrets.md) | DEEPENING | DONE |
| [TRACEABILITY.md](TRACEABILITY.md) | — | — |

---

## Retrospective

### Qué se entregó

3/3 scopes completados. El flujo de recuperación de contraseña pasó de depender de Firebase SDK client-side a ser controlado completamente por el backend: `EmailService` (JavaMail + Thymeleaf), `PasswordResetService` con token UUID propio, dos endpoints públicos en `AuthController`, migración Flyway V5+V6, y secretos SMTP en Terraform. Las páginas `/forgot-password` y `/reset-password` sobrevivieron del plan 006 con su lógica interna reescrita.

### Lo que estaba ya implementado al iniciar

Al ejecutar los scopes se encontró que la implementación estaba mayoritariamente completa desde sesiones anteriores. Los scopes funcionaron como auditoría y verificación: confirmaron que el código existía, era correcto, y los 65 tests de API + 14 tests de web pasaban. Solo el scope-03 (infra) requirió trabajo nuevo.

### Mejoras sobre la spec encontradas en el código

- **V6 migration** — constraint `UNIQUE(teacher_uid)` a nivel DB: garantía adicional más allá del delete previo en el servicio.
- **`revokeRefreshTokens()`** después del reset — los tokens de sesión con la contraseña anterior quedan invalidados inmediatamente.
- **`PasswordResetError` class** en `web/` — tipado limpio para el manejo de errores del API en vez de comparar status codes directamente.
- **`<Suspense>` wrapper** en `/reset-password` — requerido por Next.js App Router para `useSearchParams()`, no documentado en la spec.

### Decisiones clave

- Token UUID v4 propio en lugar de Firebase oobCode — control total sobre TTL, formato del link y almacenamiento.
- `email` como factor adicional en `PUT /reset-password` — el atacante necesita tanto el token (en el correo) como el email.
- Versiones de secretos SMTP no gestionadas por Terraform — patrón consistente con `INTERNAL_API_SECRET`; se añaden manualmente con `gcloud`.

### Pendientes post-deploy

- Añadir versiones a los 5 secretos SMTP en Secret Manager (`gcloud secrets versions add`).
- Actualizar `GRADEOPS_WEB_BASE_URL` en `cloud_run.tf` con la URL real del Cloud Run de `web/` antes del próximo `terraform apply`.
- Resolver los 5 tests preexistentes fallidos en `SignOutButton.test.tsx` y `RegisterPage.test.tsx` (no introducidos por este planning).

---

> [← planning/README.md](../../README.md)
