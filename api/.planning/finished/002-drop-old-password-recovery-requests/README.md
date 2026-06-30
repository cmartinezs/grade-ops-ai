# 📋 Planning 002 — drop-old-password-recovery-requests

> [← planning/README.md](../README.md)

**Status:** DONE — story-01 completada, PR #22 abierto contra `develop`.
**Período:** 2026-06-29 → 2026-06-30
**Área:** `api/` — bounded context `auth`

---

## Intent

Implementar una estrategia de ciclo de vida para la tabla `password_reset_codes`: eliminar registros cerrados (usados o expirados) que superaron un período de retención de auditoría configurable (default 90 días), manteniendo la tabla transaccionalmente acotada sin perder capacidad de trazabilidad durante el período de retención.

**Decisión adoptada:** Opción C — retención 90 días + `@Scheduled` job diario. Sin tabla de auditoría adicional.

---

## Stories

| # | Story | Status |
|---|-------|--------|
| 01 | [Cleanup job para `password_reset_codes`](02-deepening/story-01-cleanup-job.md) | ✅ DONE |

---

## Key Outcomes

- `PasswordResetCodeJpaEntity` corregida: `@Id` cambiado de `teacher_uid` a `id UUID` (task-01).
- Flyway V8 añade `idx_prc_created_at` para optimizar la query de cleanup.
- Port `deleteAllClosedCreatedBefore(Instant) → long` + adapter `@Modifying @Query` JPQL con `clearAutomatically = true`.
- `CleanupPasswordResetCodesHandler` usa `Clock` inyectado (testabilidad determinista); `getRetentionDays()` expuesto vía interfaz con Lombok `@Getter`.
- `PasswordResetCodeCleanupJob` (`@Scheduled cron = "0 0 2 * * *"`) loguea `retentionDays`, `threshold`, `deleted` y `durationMs`.
- 6 casos de integración Testcontainers + idempotencia (task-08).
- 136/136 tests pasan. Smoke test local verificado.

---

## Retrospective

### Lo que fue bien

- **Diseño previo sólido.** Las Open Questions del `00-initial.md` estaban bien respondidas antes de empezar: la elección de Opción C evitó deuda técnica innecesaria (tabla audit_log, soft-delete).
- **Atomización efectiva.** Dividir en 10 tasks permitió PRs revisables y trazables. Cada task tenía evidencia propia.
- **`Clock` inyectado desde el inicio.** La decisión de no usar `Instant.now()` directamente en el use case simplificó los tests de integración con fechas fijas.
- **Tests de integración Testcontainers.** Cubrieron el criterio de borrado doble (closed AND fuera del TTL) de forma determinista sin mocks de DB.

### Lo que se puede mejorar

- **Logging incompleto detectado en audit.** El Done Criterion 7 (log con `retentionDays` + `threshold`) no fue verificado durante la ejecución de task-07. Fue detectado en la revisión previa al cierre (gap-fixes PR #20). Incorporar una checklist de log en el template de tareas de scheduler podría prevenir esto.
- **Rebase contra rama equivocada.** Al hacer el story PR se hizo rebase contra `master` en lugar de `develop`, causando conflictos que requirieron un segundo push. Documentar la rama base en `.planning/config.yml` o en `GUIDE.md` evitaría esto.
- **README.md del planning nunca se instanció.** El template no se reemplazó con contenido real durante la ejecución. Conviene instanciar el README al inicio de la fase de expansion.

### Decisiones clave

| Decisión | Motivo |
|----------|--------|
| Opción C (retención 90d + scheduler) | Simplicidad + auditoría nativa durante el período de retención |
| `Clock` como bean Spring | Testabilidad sin mocks de tiempo |
| `getRetentionDays()` en interfaz del use case | El job no necesita conocer el valor directamente; mantiene bajo acoplamiento |
| `clearAutomatically = true` en `@Modifying` | Evita lecturas obsoletas de caché L1 de Hibernate tras bulk delete |
| Índice simple en `created_at` | Suficiente para el patrón de query; índice compuesto requiere validación con datos reales |

### Residuals abiertos

| ID | Descripción | Estado |
|----|-------------|--------|
| R-01 | Migrar cleanup a Cloud Scheduler + endpoint interno o Cloud Run Job si `min-instances` cambia a 0 | OPEN → future planning |

---

> [← planning/README.md](../README.md)
