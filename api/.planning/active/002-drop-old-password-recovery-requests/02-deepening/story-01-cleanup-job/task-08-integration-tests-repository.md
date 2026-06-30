# ⚛️ TASK 08 — Tests de integración: `deleteAllClosedCreatedBefore`

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-05
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Escribir tests de integración para `PasswordResetCodePersistenceAdapter.deleteAllClosedCreatedBefore` que cubran los cinco casos del criterio de borrado más idempotencia, usando una DB real (patrón existente del proyecto).

---

## Technical Design

- **Approach:** Seguir el patrón de `PasswordResetCodePersistenceAdapterTest` del proyecto: `@ExtendWith(MockitoExtension.class)` con mocks para tests unitarios del adapter, o `@DataJpaTest` con `@Import` del adapter para tests de integración con H2/Testcontainers si el proyecto ya lo usa. Revisar el setup existente para determinar cuál aplica.
- **Affected files:**
  - `src/test/java/.../auth/infrastructure/adapter/out/persistence/PasswordResetCodePersistenceAdapterIntegrationTest.java` (nuevo)
- **Interfaces / contracts:** Ninguna nueva. Testa el método añadido en task-05.
- **Design notes:**
  - Los tests deben insertar registros directamente vía `PasswordResetCodeJpaRepository` (no vía adapter) para controlar `created_at`, `expires_at` y `used_at` con precisión.
  - Para fijar `created_at` en el pasado, la entidad JPA deberá permitir setearlo explícitamente (actualmente ya lo hace — el mapper lo setea desde el dominio).
  - `threshold = Instant.now().minus(90, ChronoUnit.DAYS)` es el valor de corte estándar a usar en cada test.
  - El test de idempotencia llama al método dos veces con el mismo threshold y verifica que la segunda ejecución retorna `0`.

---

## Implementation Steps

1. Determinar la estrategia de integración del proyecto (H2 en test scope, Testcontainers, o `@DataJpaTest` con embedded DB). Revisar `pom.xml` y tests existentes como `TeacherPersistenceAdapterTest`.
2. Crear `PasswordResetCodePersistenceAdapterIntegrationTest.java` con los siguientes casos:

   | Test | Setup | Expected |
   |------|-------|----------|
   | `deletesUsedRecordBeyondRetention` | `used_at=now-1h`, `created_at=now-91d` | deleted=1 |
   | `deletesExpiredRecordBeyondRetention` | `used_at=null`, `expires_at=now-1h`, `created_at=now-91d` | deleted=1 |
   | `doesNotDeleteRecentRecord` | `used_at=not null`, `created_at=now-1d` | deleted=0 |
   | `doesNotDeletePendingNotExpired` | `used_at=null`, `expires_at=now+1h`, `created_at=now-91d` | deleted=0 |
   | `returnsCorrectCount` | 3 elegibles + 2 no elegibles | deleted=3 |
   | `isIdempotent` | 1 elegible; ejecutar dos veces | 1ª→1, 2ª→0 |

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1–6 | Los 6 casos de la tabla anterior | Assertions sobre el `long` retornado + `findAll()` para verificar filas restantes |

---

## Done Criteria

- [x] Los 6 tests de integración existen y pasan
- [x] La segunda ejecución del test de idempotencia retorna `0` sin error
- [x] `./mvnw test` pasa

---

> [← story file](../story-01-cleanup-job.md)
