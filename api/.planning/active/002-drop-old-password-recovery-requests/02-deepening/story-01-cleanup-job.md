# 🔍 DEEPENING: Story 01 — Cleanup job para `password_reset_codes`

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Implementar un `@Scheduled` job diario en Spring Boot que elimine registros de `password_reset_codes` que cumplan **ambas** condiciones: (1) están cerrados operacionalmente —usados (`used_at IS NOT NULL`) o expirados (`expires_at < NOW()`)— y (2) superaron el período de retención de auditoría (`created_at < now - retentionDays`, default 90 días). El TTL es configurable por entorno. El job debe ser idempotente, observable mediante logs estructurados y no exponer datos sensibles.

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Fix JPA entity PK: `@Id UUID id`](story-01-cleanup-job/task-01-fix-jpa-entity-pk.md) | GENERATE-DOCUMENT | DONE | entidad JPA consistente con el PK real de la tabla |
| 2 | [Flyway V8: índice en `created_at`](story-01-cleanup-job/task-02-flyway-v8-index.md) | GENERATE-DOCUMENT | DONE | `V8__add_index_prc_created_at.sql` |
| 3 | [Configurar `app.auth.reset-code-retention-days`](story-01-cleanup-job/task-03-configure-retention-property.md) | GENERATE-DOCUMENT | DONE | propiedad en `application.yml` y `application-local.example.yml` |
| 4 | [Validación estática JPA y Flyway](story-01-cleanup-job/task-04-static-jpa-flyway-validation.md) | GENERATE-DOCUMENT | DONE | regla ArchUnit + checklist V8 |
| 5 | [Port + adapter: `deleteAllClosedCreatedBefore`](story-01-cleanup-job/task-05-port-adapter-delete-method.md) | GENERATE-DOCUMENT | DONE | extensión del port + `@Modifying @Query` en adapter |
| 6 | [`CleanupPasswordResetCodesHandler` con `Clock`](story-01-cleanup-job/task-06-cleanup-use-case.md) | GENERATE-DOCUMENT | DONE | use case + bean `Clock` + test unitario |
| 7 | [`PasswordResetCodeCleanupJob` con `@Scheduled`](story-01-cleanup-job/task-07-cleanup-job.md) | GENERATE-DOCUMENT | DONE | job en `adapter/in/scheduler/` con logging |
| 8 | [Tests de integración del repositorio](story-01-cleanup-job/task-08-integration-tests-repository.md) | GENERATE-DOCUMENT | DONE | 6 casos + idempotencia |
| 9 | [Tests de no-regresión](story-01-cleanup-job/task-09-no-regression-tests.md) | GENERATE-DOCUMENT | DONE | flujos `IssuePasswordResetCode` y `ResetPassword` intactos |
| 10 | [Smoke test: arranque local y endpoints](story-01-cleanup-job/task-10-smoke-test.md) | GENERATE-DOCUMENT | DONE | app arranca + V1–V8 aplican + endpoints responden |

---

## Done Criteria

- [x] Solo se eliminan registros que cumplan ambas condiciones: cerrados (usados O expirados) Y fuera del período de retención
- [x] Registros dentro del período de retención no son eliminados, independientemente de su estado
- [x] Registros pendientes y no expirados no son eliminados aunque superen el TTL (protección de edge case)
- [x] El método del port retorna `long` con la cantidad de registros eliminados en la ejecución
- [x] `Clock` inyectado en el use case; `Instant.now()` no se llama directamente en ninguna clase propia
- [x] Job es idempotente: ejecuciones consecutivas no producen errores ni efectos secundarios negativos
- [x] Job loguea en INFO: inicio, `retentionDays`, `threshold` calculado, cantidad eliminada, duración en ms
- [x] Job loguea en ERROR si ocurre cualquier excepción durante la ejecución
- [x] No se loguean `rawCode`, `teacherUid` ni ningún dato sensible
- [x] TTL configurable vía `app.auth.reset-code-retention-days` (default: 90)
- [x] No hay regresión en tests de `ForgotPasswordUseCase` ni `ResetPasswordUseCase`
- [x] TRACEABILITY.md actualizado con nuevos términos de este story

---

## Design Notes

### Criterio de borrado

Un registro es elegible para eliminación si y solo si cumple **ambas** condiciones:

```sql
created_at < :threshold           -- fuera del período de retención
AND (
    used_at IS NOT NULL           -- fue usado
    OR expires_at < NOW()         -- expiró sin usarse
)
```

No se eliminan registros activos (pendientes, no expirados) aunque sean "viejos". Si en un futuro se emiten códigos con TTL mayor a 90 días, la condición `expires_at < NOW()` los protegería correctamente.

### Port

```java
// PasswordResetCodeRepositoryPort
long deleteAllClosedCreatedBefore(Instant threshold);
```

### JPA adapter — `PasswordResetCodeJpaRepository`

```java
@Modifying(clearAutomatically = true)
@Query("""
    DELETE FROM PasswordResetCodeJpaEntity e
    WHERE e.createdAt < :threshold
      AND (e.usedAt IS NOT NULL OR e.expiresAt < CURRENT_TIMESTAMP)
    """)
int bulkDeleteClosedCreatedBefore(@Param("threshold") Instant threshold);
```

> `clearAutomatically = true` invalida la caché de primer nivel de Hibernate tras el bulk delete, evitando lecturas obsoletas en la misma sesión.
>
> `CURRENT_TIMESTAMP` en JPQL es evaluado por la base de datos al momento de la ejecución. Hibernate 6 lo gestiona correctamente con columnas `TIMESTAMPTZ` mapeadas a `Instant`.

El adapter (`PasswordResetCodePersistenceAdapter`) delega a este método y castea `int → long`.

### Use case — domain layer

```java
// CleanupPasswordResetCodesUseCase
public long execute() {
    var threshold = Instant.now(clock).minus(retentionDays, ChronoUnit.DAYS);
    return passwordResetCodeRepository.deleteAllClosedCreatedBefore(threshold);
}
```

`Clock` se declara como bean `@Bean Clock clock() { return Clock.systemUTC(); }` en la configuración de Spring. En tests unitarios se inyecta `Clock.fixed(Instant.parse("..."), ZoneOffset.UTC)`.

### Job — infrastructure layer

```java
@Component
public class PasswordResetCodeCleanupJob {

    // @Scheduled(cron = "0 0 2 * * *")  // 02:00 UTC diario
    public void run() {
        log.info("PasswordResetCode cleanup started — retentionDays={}", retentionDays);
        var start = Instant.now();
        try {
            long deleted = cleanupUseCase.execute();
            log.info("PasswordResetCode cleanup finished — deleted={}, durationMs={}",
                     deleted, Duration.between(start, Instant.now()).toMillis());
        } catch (Exception e) {
            log.error("PasswordResetCode cleanup failed", e);
        }
    }
}
```

> No loguear `threshold` como timestamp literal ni ningún valor derivado de `rawCode` o `teacherUid`.

### Índice — Migración V8

```sql
-- V8__add_index_prc_created_at.sql
CREATE INDEX idx_prc_created_at ON password_reset_codes(created_at);
```

Índice mínimo suficiente para el patrón `WHERE created_at < :threshold`. Un índice compuesto `(created_at, expires_at, used_at)` podría habilitar index-only scan, pero debe validarse con `EXPLAIN ANALYZE` sobre datos reales antes de añadirlo. Para esta iteración, el índice simple en `created_at` es suficiente.

### Cloud Run y concurrencia

`@Scheduled` requiere que el pod esté activo al momento de dispararse.

- **Decisión actual:** `min-instances=1` en producción (configurado en infra/007). Si esta configuración cambia, el cleanup deja de ser confiable. Esta dependencia es un riesgo conocido y aceptado para esta iteración.
- **Múltiples instancias:** si hay más de una réplica activa, el job puede ejecutarse concurrentemente. La **idempotencia es la mitigación mínima aceptada**: dos `DELETE WHERE created_at < :threshold` simultáneos producen el mismo estado final sin errores (la segunda ejecución no encontrará registros adicionales que eliminar).
- No se implementa ShedLock, advisory locks, Cloud Scheduler ni Cloud Run Job en esta iteración. Ver Residuals.

### Tests a cubrir

| Caso | Tipo |
|------|------|
| Elimina registro usado con `created_at` fuera del TTL | Integración |
| Elimina registro expirado (no usado) con `created_at` fuera del TTL | Integración |
| No elimina registro dentro del período de retención | Integración |
| No elimina registro pendiente no expirado aunque supere el TTL | Integración |
| Retorna la cantidad correcta de registros eliminados | Integración |
| `threshold` se calcula con `Clock.fixed(...)` en el use case | Unitario |
| Idempotencia: segunda ejecución retorna 0 eliminados | Integración |
| No regresión: `ForgotPasswordUseCase` y `ResetPasswordUseCase` no se ven afectados | Integración |

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | `PasswordResetCodeJpaEntity` usa `teacher_uid` como `@Id` (JPA), pero el PK real de la tabla es `id UUID` (`V5`). Funciona por el UNIQUE constraint en `teacher_uid` (V6), pero es no-estándar y puede causar sorpresas en Spring Data. | `V5__add_password_reset_codes.sql`, `PasswordResetCodeJpaEntity.java` | ABSORBED | Convertido en tarea 1 de esta story. |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | **Deuda operacional conocida:** migrar el cleanup job a Cloud Scheduler + endpoint interno protegido (`POST /internal/jobs/cleanup-reset-codes`) o a Cloud Run Job. **Razón:** `@Scheduled` depende de que exista al menos una instancia activa; no garantiza ejecución única en entornos multi-réplica. **Trigger de activación:** si producción cambia a `min-instances=0`, si aparecen problemas de ejecución concurrente entre réplicas, o si el cleanup pasa a ser un requisito operacional crítico con ejecución garantizada. | future planning | OPEN |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
