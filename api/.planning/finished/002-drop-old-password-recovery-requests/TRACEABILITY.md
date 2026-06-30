# 🔗 Traceability: 002-drop-old-password-recovery-requests

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

<!-- AREAS-REF: populated by plan-init from the project's configured areas — keep in sync with GUIDE.md -->
| Code | Area |
|------|------|
| AP | `src/` — Java / Spring Boot 4 API |
| DO | `docs/` — documentación |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

| Term / Concept | AP | DO | W | Notes |
|---|---|---|---|---|
| `password_reset_codes` (tabla) | ✅ | N/A | ✅ | Tabla target del cleanup job. PK `id UUID` corregido en task-01. |
| `PasswordResetCodeJpaEntity` | ✅ | N/A | ✅ | Entidad JPA; `@Id` cambiado de `teacher_uid` a `id UUID` en task-01. |
| `PasswordResetCodeRepositoryPort` | ✅ | N/A | ✅ | Port de salida extendido con `deleteAllClosedCreatedBefore(Instant)` en task-05. |
| `deleteAllClosedCreatedBefore` | ✅ | N/A | ✅ | Método del port que retorna `long`; implementado con `@Modifying @Query` JPQL en task-05. |
| `CleanupPasswordResetCodesUseCase` | ✅ | N/A | ✅ | Port de entrada (interfaz) del caso de uso de cleanup. |
| `CleanupPasswordResetCodesHandler` | ✅ | N/A | ✅ | Implementación del use case; usa `Clock` inyectado para calcular `threshold`. |
| `PasswordResetCodeCleanupJob` | ✅ | N/A | ✅ | `@Scheduled` job diario 02:00 UTC; loguea `retentionDays`, `deleted`, `durationMs`. |
| `app.auth.reset-code-retention-days` | ✅ | N/A | ✅ | Propiedad configurable (default 90 días). Inyectada en handler y job. |
| `retentionDays` | ✅ | N/A | ✅ | Período de retención de auditoría en días; registros más viejos son elegibles para borrado. |
| `threshold` | ✅ | N/A | ✅ | `Instant` calculado como `now(clock) - retentionDays`; usado en la query de borrado. |
| `Clock` (bean Spring) | ✅ | N/A | ✅ | Bean `Clock.systemUTC()` declarado en `ClockConfig`; inyectado en el handler para testabilidad. |
| `@EnableScheduling` | ✅ | N/A | ✅ | Activado en `ClockConfig`; habilita el scheduler de Spring. |
| `V8__add_index_prc_created_at.sql` | ✅ | N/A | ✅ | Migración Flyway que añade índice en `created_at` para optimizar la query de cleanup. |
| `idx_prc_created_at` | ✅ | N/A | ✅ | Índice en `password_reset_codes(created_at)` creado en V8. |
| `clearAutomatically = true` | ✅ | N/A | N/A | Flag en `@Modifying` para invalidar caché L1 de Hibernate tras bulk delete. |
| `CURRENT_TIMESTAMP` (JPQL) | ✅ | N/A | N/A | Evaluado por la DB al ejecutar la query; correcto con columnas `TIMESTAMPTZ` → `Instant`. |
| `PasswordResetCodePersistenceAdapterIntegrationTest` | ✅ | N/A | ✅ | 6 casos + idempotencia via Testcontainers PostgreSQL; cubre el criterio de borrado doble. |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D-01 | Opción C: retención 90 días configurable + `@Scheduled` diario | Simplicidad operacional para el MVP; sin tabla de auditoría adicional | AP | 2026-06 |
| D-02 | `Clock` inyectado, no `Instant.now()` directo | Testabilidad determinista del threshold en tests unitarios | AP | 2026-06 |
| D-03 | `clearAutomatically = true` en `@Modifying` | Evita lecturas obsoletas de caché Hibernate L1 tras bulk delete | AP | 2026-06 |
| D-04 | Índice simple en `created_at` (no compuesto) | Suficiente para el patrón `WHERE created_at < :threshold`; índice compuesto requiere `EXPLAIN ANALYZE` con datos reales | AP | 2026-06 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R-01 | Migrar cleanup a Cloud Scheduler + endpoint interno o Cloud Run Job | Cambio a `min-instances=0` o requisito de ejecución garantizada en multi-réplica | OPEN | future planning |

---

> [← planning/README.md](../../README.md)
