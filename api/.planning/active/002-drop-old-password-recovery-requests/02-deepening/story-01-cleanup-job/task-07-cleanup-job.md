# ⚛️ TASK 07 — `PasswordResetCodeCleanupJob` con `@Scheduled` y logging

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-06
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Crear `PasswordResetCodeCleanupJob` en el infrastructure layer: un `@Component` Spring con un método `@Scheduled` que invoca `CleanupPasswordResetCodesHandler` y loguea inicio, resultado y duración sin exponer datos sensibles.

---

## Technical Design

- **Approach:** Spring `@Scheduled` con expresión cron `0 0 2 * * *` (02:00 UTC diario). El job es un adapter de entrada (`adapter.in.scheduler`) que delega toda la lógica al use case. Captura excepciones para loguear ERROR sin propagar (evita que el scheduler desactive el job en Spring Boot).
- **Affected files:**
  - `src/main/java/.../auth/infrastructure/adapter/in/scheduler/PasswordResetCodeCleanupJob.java` (nuevo)
- **Interfaces / contracts:** Ninguna expuesta hacia fuera. Consume `CleanupPasswordResetCodesHandler`.
- **Design notes:**
  - El job debe estar en `adapter.in.scheduler` — es un adaptador de entrada disparado por tiempo, análogo a un adaptador HTTP pero con trigger temporal.
  - Logging: `log.info` en inicio y fin (con `deleted` y `durationMs`); `log.error` en excepción. Nunca loguear `rawCode`, `teacherUid`, ni el `threshold` como fecha absoluta.
  - Habilitar scheduling en la aplicación: verificar que `@EnableScheduling` esté en alguna clase `@Configuration`. Añadirlo si no existe.
  - `@Scheduled` requiere que la clase sea un bean Spring (`@Component`) y que `@EnableScheduling` esté activo.
  - El job es idempotente por diseño: `DELETE WHERE created_at < :threshold` produce el mismo estado final en ejecuciones concurrentes.

---

## Implementation Steps

1. Verificar que `@EnableScheduling` esté presente en alguna `@Configuration` del proyecto. Si no, añadirlo en una clase existente o crear `SchedulingConfig.java` en `shared/infrastructure/config/`.
2. Crear `PasswordResetCodeCleanupJob.java` en `auth/infrastructure/adapter/in/scheduler/`:
   ```java
   @Slf4j
   @Component
   @RequiredArgsConstructor
   public class PasswordResetCodeCleanupJob {

       private final CleanupPasswordResetCodesHandler cleanupHandler;

       @Scheduled(cron = "0 0 2 * * *")
       public void run() {
           log.info("PasswordResetCode cleanup started");
           var start = Instant.now();
           try {
               long deleted = cleanupHandler.execute();
               log.info("PasswordResetCode cleanup finished — deleted={}, durationMs={}",
                        deleted, Duration.between(start, Instant.now()).toMillis());
           } catch (Exception e) {
               log.error("PasswordResetCode cleanup failed", e);
           }
       }
   }
   ```

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1 | `run()` llama a `cleanupHandler.execute()` exactamente una vez | `verify(cleanupHandler).execute()` con Mockito |
| 2 | Si `execute()` lanza excepción, `run()` no propaga y loguea ERROR | `when(cleanupHandler.execute()).thenThrow(...)` → `run()` no lanza; verificar log con `ListAppender` o similar |
| 3 | `run()` no accede a `rawCode`, `teacherUid` ni threshold literal | Revisión de código |

---

## Done Criteria

- [ ] `PasswordResetCodeCleanupJob` existe en `auth/infrastructure/adapter/in/scheduler/`
- [ ] `@Scheduled(cron = "0 0 2 * * *")` presente en el método `run()`
- [ ] `@EnableScheduling` activo en la aplicación
- [ ] Log INFO en inicio y fin con `deleted` y `durationMs`; log ERROR en fallo
- [ ] No se loguean datos sensibles
- [ ] `./mvnw test` pasa

---

> [← story file](../story-01-cleanup-job.md)
