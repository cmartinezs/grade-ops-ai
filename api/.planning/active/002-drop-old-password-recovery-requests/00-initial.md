# 🌱 INITIAL: 002-drop-old-password-recovery-requests

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

Implementar una estrategia de ciclo de vida para la tabla `password_reset_codes`: limpiar registros expirados o ya usados para mantener la tabla transaccionalmente acotada, sin perder capacidad de auditoría.

---

## Why

Una vez que un código de recuperación de contraseña es usado o expira, su registro en `password_reset_codes` queda en modo lectura: nunca participará en otra operación transaccional. Sin un mecanismo de limpieza, la tabla crece indefinidamente acumulando registros que no aportan valor operacional. Sin embargo, esos registros sí tienen valor de auditoría (evidencia de cuántos resets pidió un usuario, si los usó o dejaron expirar). La solución debe mantener la tabla acotada sin sacrificar esa capacidad de trazabilidad.

**Decisión de diseño adoptada:** retención extendida (90 días) + scheduled cleanup. Los registros se conservan 90 días desde su creación y luego son eliminados por un `@Scheduled` job diario. No se introduce tabla de auditoría adicional: durante el período de retención la propia tabla sirve de fuente de verdad, y Cloud Logging cubre el historial para períodos mayores. El TTL es configurable por entorno. Las alternativas evaluadas se detallan en la sección de Open Questions.

---

## Approximate Scope

- [x] `api/` — `CleanupPasswordResetCodesUseCase`, extensión de `PasswordResetCodeRepositoryPort`, `PasswordResetCodeCleanupJob` (`@Scheduled`), configuración TTL en `application.yml`
- [x] `docs/` — n/a
- [x] `web/` — n/a
- [x] `agents/` — n/a
- [x] `infra/` — n/a

---

## Initiator

- **Requested by:** human
- **Date:** 2026-06-29
- **Related planning (if continuation):** 007-password-recovery-custom-email (parent planning que introduce `password_reset_codes`)

---

## Supersedes

*(none)*

---

## Next Step

- [x] When dimensioned → fill `01-expansion.md` and move to `planning/active/`
- [x] If needs clarification first → document open questions below

### Open Questions

1. **¿Qué tan pronto se puede eliminar un registro?** ¿Inmediatamente tras usarse o expirar, o tras una ventana de retención (e.g., 30 o 90 días)?
2. **¿Se requiere auditoría formal y consultable?** ¿Basta con Cloud Logging (logs de aplicación ya existentes), o se necesita una tabla `auth_audit_log` con registros estructurados?
3. **¿Dónde vive el mecanismo de limpieza?** Spring `@Scheduled` en la aplicación vs. `pg_cron` en Cloud SQL.
4. **¿Borrado o archivado?** Si en el futuro se necesita consultar "¿cuántos resets tuvo este email en los últimos 90 días?", un archivado a tabla secundaria tiene más valor que el borrado simple.

### Design Options (to evaluate in expansion)

| Opción | Descripción | Trade-off |
|--------|-------------|-----------|
| A — Borrado periódico + logs de aplicación | Scheduler (`@Scheduled`) elimina registros tras TTL; evidencia queda en Cloud Logging | Simple, sin overhead de tabla extra; auditoría solo en logs |
| B — Tabla `auth_audit_log` + borrado inmediato | Al usar/expirar el código se escribe un evento estructurado y se elimina de `password_reset_codes` | Auditoría consultable con SQL; requiere migración y lógica adicional |
| C — Retención extendida con cleanup tardío | Conservar registros N días antes de eliminar, sin tabla extra | Tabla crece más lento pero no indefinidamente; auditoría nativa durante período de retención |
| D — Soft delete (`deleted_at`) + purge periódico | Marcar como eliminado, purge de soft-deleted antiguos en segundo paso | Auditoría inmediata; agrega complejidad a queries que deben filtrar `deleted_at IS NULL` |

**Recomendación preliminar:** Opción C (retención 90 días + scheduler de limpieza) o B (audit log estructurado) son los mejores trade-offs. Opción A pierde auditoría estructurada; Opción D añade complejidad de query sin beneficio claro.

---

> [← planning/README.md](../../README.md)
