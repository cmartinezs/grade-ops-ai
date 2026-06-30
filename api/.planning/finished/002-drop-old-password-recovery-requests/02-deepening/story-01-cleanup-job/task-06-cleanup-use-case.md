# ⚛️ TASK 06 — `CleanupPasswordResetCodesUseCase` con `Clock` inyectado

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-03, task-05
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Crear `CleanupPasswordResetCodesUseCase` en el domain layer con `Clock` inyectado para cálculo determinista del threshold, y registrar el bean `Clock` en la configuración de Spring.

---

## Technical Design

- **Approach:** El use case calcula `threshold = Instant.now(clock).minus(retentionDays, ChronoUnit.DAYS)` y delega al port. Usar `Clock` en lugar de `Instant.now()` directo permite inyectar `Clock.fixed(...)` en tests unitarios, haciendo el cálculo 100% determinista.
- **Affected files:**
  - `src/main/java/.../auth/application/usecase/CleanupPasswordResetCodesHandler.java` (nuevo) — sigue la convención de naming `*Handler` del proyecto
  - `src/main/java/.../shared/infrastructure/config/ClockConfig.java` (nuevo) — bean `Clock`
  - `src/test/java/.../auth/application/usecase/CleanupPasswordResetCodesHandlerTest.java` (nuevo)
- **Interfaces / contracts:** El use case es invocado por el job (task-07). No expone HTTP.
- **Design notes:**
  - Seguir la convención del proyecto: los use cases se llaman `*Handler` (ver `IssuePasswordResetCodeHandler`, `RegisterHandler`, etc.).
  - El bean `Clock` es `@Bean Clock clock() { return Clock.systemUTC(); }` en una clase `@Configuration`. Centralizar el bean permite que todos los use cases futuros usen el mismo clock.
  - `retentionDays` se inyecta con `@Value("${app.auth.reset-code-retention-days}")`.
  - El use case vive en `application/usecase/` — no en `domain/` puro — ya que orquesta un port de infraestructura.

---

## Implementation Steps

1. Crear `CleanupPasswordResetCodesHandler.java` en `auth/application/usecase/`:
   ```java
   @RequiredArgsConstructor
   public class CleanupPasswordResetCodesHandler {
       private final PasswordResetCodeRepositoryPort codeRepository;
       private final Clock clock;
       private final int retentionDays;  // inyectado en @Bean factory, no via @Value en el handler

       public long execute() {
           var threshold = Instant.now(clock).minus(retentionDays, ChronoUnit.DAYS);
           return codeRepository.deleteAllClosedCreatedBefore(threshold);
       }
   }
   ```
   > **Desviación del diseño original:** `retentionDays` se declaró como `final int` en lugar de usar `@Value` en el campo. Razón: la convención del proyecto pasa todos los valores de configuración a través del constructor; `@Value` en el campo de un POJO creado por `@Bean` mezcla inyección Lombok con field injection de Spring. La `@Bean` factory en `AuthConfig` usa `@Value` al nivel del parámetro del método, manteniendo el handler puro.
2. Crear `ClockConfig.java` en `shared/infrastructure/config/`:
   ```java
   @Configuration
   public class ClockConfig {
       @Bean
       public Clock clock() { return Clock.systemUTC(); }
   }
   ```
3. Crear `CleanupPasswordResetCodesHandlerTest.java`:
   - Inyectar `Clock.fixed(Instant.parse("2026-06-29T02:00:00Z"), ZoneOffset.UTC)` y `retentionDays = 90`
   - Verificar que el threshold calculado es `Instant.parse("2026-03-31T02:00:00Z")`
   - Verificar que `codeRepository.deleteAllClosedCreatedBefore(threshold)` es llamado con ese valor exacto

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1 | `threshold` = `Instant.now(clock) - retentionDays` exacto | `verify(codeRepository).deleteAllClosedCreatedBefore(expectedThreshold)` con `Clock.fixed` |
| 2 | `execute()` retorna el valor devuelto por el port | `when(codeRepository...).thenReturn(5L)` → `assertThat(result).isEqualTo(5L)` |
| 3 | `Instant.now()` nunca se llama directamente en el handler | Revisión de código (sin `Instant.now()` sin `clock`) |

---

## Done Criteria

- [x] `CleanupPasswordResetCodesHandler` existe en `auth/application/usecase/`
- [x] `Clock` se inyecta; no hay llamadas directas a `Instant.now()` en la clase
- [x] `ClockConfig` registra el bean `Clock.systemUTC()`
- [x] Test unitario cubre cálculo de threshold con `Clock.fixed` y retorno del resultado
- [x] `./mvnw test` pasa

---

> [← story file](../story-01-cleanup-job.md)
