# ⚛️ TASK 01 — Fix JPA entity PK: `@Id UUID id`

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Corregir `PasswordResetCodeJpaEntity` para que use `id UUID` como `@Id` real (consistente con el PK de la tabla), y actualizar mapper y adapter en consecuencia para que el flujo save/update siga funcionando correctamente.

---

## Technical Design

- **Approach:** La tabla ya tiene `id UUID PRIMARY KEY DEFAULT gen_random_uuid()` (V5). El problema es que JPA usa `teacher_uid` como `@Id`, lo que es no-estándar y depende de un comportamiento de merge no garantizado. El fix mueve `@Id` a `id UUID`, cambia el adapter a un patrón load-then-update para actualizaciones, y deja que la DB genere el UUID en inserts.
- **Affected files:**
  - `PasswordResetCodeJpaEntity.java` — añadir campo `UUID id` con `@Id`, quitar `@Id` de `teacherUid`
  - `PasswordResetCodeJpaRepository.java` — cambiar tipo genérico a `UUID`, añadir `findByTeacherUid`
  - `PasswordResetCodePersistenceMapper.java` — `toEntity()` no setea `id`; añadir `updateEntity(existing, code)`
  - `PasswordResetCodePersistenceAdapter.java` — `save()` pasa a load-then-update
  - `PasswordResetCodePersistenceAdapterTest.java` — actualizar tests afectados
- **Interfaces / contracts:** El port `PasswordResetCodeRepositoryPort` no cambia. El comportamiento externo de `save()` es idéntico.
- **Design notes:**
  - `id` se genera en la DB (`DEFAULT gen_random_uuid()`); el campo en la entidad JPA debe tener `@GeneratedValue(strategy = GenerationType.UUID)` o declararse `insertable = true, updatable = false` con `@Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")`.
  - Con Spring Boot 4 + Hibernate 6, `@GeneratedValue(strategy = GenerationType.UUID)` funciona correctamente para columnas UUID.
  - El adapter `save()` debe hacer `findByTeacherUid` primero para detectar si es insert o update. Un teacher solo tiene un código activo a la vez (garantía del use case existente que llama `deleteByTeacherUid` antes de `save`), por lo que el `findByTeacherUid` siempre devolverá vacío en un insert real. Aun así, la lógica debe ser correcta para robustez.
  - `toEntity()` no setea `id` (null → Hibernate delega a la DB).

---

## Implementation Steps

1. En `PasswordResetCodeJpaEntity.java`: añadir `@Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id") private UUID id;`. Quitar `@Id` de `teacherUid`.
2. En `PasswordResetCodeJpaRepository.java`: cambiar a `JpaRepository<PasswordResetCodeJpaEntity, UUID>`. Añadir `Optional<PasswordResetCodeJpaEntity> findByTeacherUid(String teacherUid)`.
3. En `PasswordResetCodePersistenceMapper.java`: `toEntity()` no setea `id`. Añadir `void updateEntity(PasswordResetCodeJpaEntity entity, PasswordResetCode code)` que actualiza solo los campos mutables (`usedAt`).
4. En `PasswordResetCodePersistenceAdapter.java`: reescribir `save()` como load-then-update:
   ```java
   public void save(PasswordResetCode code) {
       PasswordResetCodeJpaEntity entity = jpaRepository
           .findByTeacherUid(code.getTeacherUid())
           .map(existing -> { mapper.updateEntity(existing, code); return existing; })
           .orElseGet(() -> mapper.toEntity(code));
       jpaRepository.save(entity);
   }
   ```
5. En `PasswordResetCodePersistenceAdapterTest.java`: actualizar `save_mapsDomainToEntity_thenPersists` para el nuevo flujo (stub `findByTeacherUid` → empty, verificar `toEntity` + `save`); añadir test `save_existingCode_updatesEntity`.

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1 | `save()` en insert: llama `findByTeacherUid` → empty → `toEntity` → `jpaRepository.save` | `verify(jpaRepository).findByTeacherUid(...)` + `verify(mapper).toEntity(...)` |
| 2 | `save()` en update: llama `findByTeacherUid` → entidad existente → `updateEntity` → `jpaRepository.save` | `verify(mapper).updateEntity(existing, code)` + `verify(mapper, never()).toEntity(any())` |
| 3 | `findByRawCode` y `deleteByTeacherUid` no cambian comportamiento | Tests existentes pasan sin modificación |

---

## Done Criteria

- [x] `PasswordResetCodeJpaEntity` tiene `@Id` en `UUID id` y no en `String teacherUid`
- [x] `PasswordResetCodeJpaRepository` extiende `JpaRepository<PasswordResetCodeJpaEntity, UUID>`
- [x] `PasswordResetCodePersistenceAdapter.save()` usa load-then-update
- [x] `PasswordResetCodePersistenceAdapterTest` cubre insert y update por separado
- [x] `./mvnw test` pasa sin errores

---

> [← story file](../story-01-cleanup-job.md)
