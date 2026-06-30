# 🚀 EXPANSION: 002-drop-old-password-recovery-requests

> **Status:** Expansion
> [← planning/README.md](../../README.md)

---

## Story Summary

| # | Story | SDLC Phase(s) | Depends On | Status |
|---|-------|--------------|------------|--------|
| 01 | Cleanup job para `password_reset_codes` | D / I / T | — | DONE |

---

## Dependency Map

```mermaid
flowchart LR
    S01[Story 01: Cleanup Job]
```

---

## Impact per Repository Area

| Code | Area | Affected? | What changes |
|------|------|----------|-------------|
| DO | `docs/` | ☐ | — |
| WB | `web/` | ☐ | — |
| AP | `api/` | ☑ | `PasswordResetCodeRepositoryPort` + nueva implementación JPA, `CleanupPasswordResetCodesUseCase`, `PasswordResetCodeCleanupJob`, `application.yml` |
| AG | `agents/` | ☐ | — |
| IN | `infra/` | ☐ | — |
| W | `.planning/` | ☑ | este planning |

---

## Notes

- Diseño decidido: **Opción C** — retención 90 días configurable + `@Scheduled` diario. Sin tabla de auditoría adicional.
- El TTL `app.auth.reset-code-retention-days` debe estar en `application.yml` (default 90) y en `application-local.yml` puede ser menor para pruebas.
- El scheduler corre a las 02:00 UTC. En Cloud Run el pod puede estar dormido — evaluar si se requiere un trigger externo (Cloud Scheduler + endpoint HTTP) o si `@Scheduled` es suficiente.
- No hay impacto en queries existentes: ningún código de producción filtra `password_reset_codes` por fecha de creación (solo por `code` UUID y `status`).

---

> [← planning/README.md](../../README.md)
