# ⚛️ TASK 05 — Port + adapter: `deleteAllClosedCreatedBefore(Instant)`

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Añadir `long deleteAllClosedCreatedBefore(Instant threshold)` al port `PasswordResetCodeRepositoryPort` e implementarlo en `PasswordResetCodePersistenceAdapter` usando `@Modifying @Query` en JPQL con doble condición de borrado (cerrado + fuera de retención).

---

## Technical Design

- **Approach:** Bulk delete vía JPQL con `@Modifying` — evita el N+1 de Spring Data (que haría `findAll` + `delete` por entidad). `clearAutomatically = true` invalida la caché L1 de Hibernate tras el delete masivo.
- **Affected files:**
  - `PasswordResetCodeRepositoryPort.java` — añadir método al interface
  - `PasswordResetCodeJpaRepository.java` — añadir `@Modifying @Query` con retorno `int`
  - `PasswordResetCodePersistenceAdapter.java` — implementar delegando a JPA repo y casteando `int → long`
  - `PasswordResetCodePersistenceAdapterTest.java` — añadir test del nuevo método
- **Interfaces / contracts:**
  ```java
  // PasswordResetCodeRepositoryPort
  long deleteAllClosedCreatedBefore(Instant threshold);
  ```
- **Design notes:**
  - `CURRENT_TIMESTAMP` en JPQL es evaluado por la DB en el momento de ejecución. Hibernate 6 lo gestiona correctamente con columnas `TIMESTAMPTZ` mapeadas a `Instant`.
  - El método JPA retorna `int` (límite de `@Modifying @Query`); el adapter castea a `long`.
  - La query borra: registros con `createdAt < threshold` Y (`usedAt IS NOT NULL` OR `expiresAt < CURRENT_TIMESTAMP`). Nunca borra registros pendientes no expirados, aunque sean viejos.
  - El método debe estar anotado con `@Transactional` en el adapter o en el JPA repository.

---

## Implementation Steps

1. En `PasswordResetCodeRepositoryPort.java`: añadir `long deleteAllClosedCreatedBefore(Instant threshold);`
2. En `PasswordResetCodeJpaRepository.java`: añadir:
   ```java
   @Modifying(clearAutomatically = true)
   @Transactional
   @Query("""
       DELETE FROM PasswordResetCodeJpaEntity e
       WHERE e.createdAt < :threshold
         AND (e.usedAt IS NOT NULL OR e.expiresAt < CURRENT_TIMESTAMP)
       """)
   int bulkDeleteClosedCreatedBefore(@Param("threshold") Instant threshold);
   ```
3. En `PasswordResetCodePersistenceAdapter.java`: implementar:
   ```java
   @Override
   public long deleteAllClosedCreatedBefore(Instant threshold) {
       return jpaRepository.bulkDeleteClosedCreatedBefore(threshold);
   }
   ```
4. En `PasswordResetCodePersistenceAdapterTest.java`: añadir test que verifica que el adapter delega al repo con el threshold correcto y retorna el `long` esperado.

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1 | Adapter llama a `jpaRepository.bulkDeleteClosedCreatedBefore(threshold)` | `verify(jpaRepository).bulkDeleteClosedCreatedBefore(threshold)` |
| 2 | Adapter retorna el valor `long` correcto que retorna el repo | `assertThat(result).isEqualTo(3L)` con stub que retorna `3` |

---

## Done Criteria

- [x] `PasswordResetCodeRepositoryPort` declara `long deleteAllClosedCreatedBefore(Instant threshold)`
- [x] `PasswordResetCodeJpaRepository` tiene el `@Modifying @Query` con doble condición
- [x] `PasswordResetCodePersistenceAdapter` implementa el método del port
- [x] `./mvnw test` pasa

---

> [← story file](../story-01-cleanup-job.md)
