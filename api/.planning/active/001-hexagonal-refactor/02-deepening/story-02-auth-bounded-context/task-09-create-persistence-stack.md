# ⚛️ TASK 09 — Create PasswordResetCode persistence stack (JPA entity + adapter + mapper)

> **Status:** TODO
> **Workflow:** IMPLEMENT
> **Depends On:** task-02, task-03
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Create the JPA persistence stack for `PasswordResetCode`: a pure-JPA entity (`PasswordResetCodeJpaEntity`) with no business logic, a package-private Spring Data `JpaRepository`, a `PasswordResetCodePersistenceAdapter` that implements `PasswordResetCodeRepositoryPort`, a `PasswordResetCodePersistenceMapper` for bidirectional domain↔entity mapping, and an adapter integration test.

---

## Technical Design

- **Approach:** Strict separation between the domain aggregate (`PasswordResetCode`) and the JPA entity (`PasswordResetCodeJpaEntity`). The JPA entity is a flat, annotated data class with no methods beyond getters/setters — all business logic lives in the domain aggregate. The Spring Data JPA repo is package-private (an implementation detail). The `PersistenceAdapter` converts via `PersistenceMapper`, calls the JPA repo, and is the only class that implements `PasswordResetCodeRepositoryPort`. The adapter is `@Repository` + `@RequiredArgsConstructor`. The mapper is a `@Component` with no state.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/persistence/PasswordResetCodeJpaEntity.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/persistence/PasswordResetCodeJpaRepository.java` ← NEW (package-private)
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/persistence/PasswordResetCodePersistenceMapper.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/persistence/PasswordResetCodePersistenceAdapter.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/persistence/PasswordResetCodePersistenceAdapterTest.java` ← NEW
- **Interfaces / contracts:** `PasswordResetCodePersistenceAdapter` implements `PasswordResetCodeRepositoryPort` from `auth.application.port.out`. No other class outside the `persistence` package references the JPA entity or JPA repo.
- **Design notes:**
  - `PasswordResetCodeJpaEntity` maps to the existing `password_reset_codes` table (same schema as `PasswordResetCodeEntity` in Story 02's old location — same table, same columns: `teacher_uid`, `raw_code`, `expires_at`, `created_at`, `used_at`).
  - `PasswordResetCodeJpaEntity` has no `isExpired()`, `isUsed()`, or `markUsed()` methods — those belong on the domain aggregate.
  - Mapper: `toDomain(entity) → PasswordResetCode` calls `PasswordResetCode.restore(...)`. `toEntity(domain) → entity` reads all fields from the domain aggregate via getters.
  - `PersistenceAdapter.deleteByTeacherUid(uid)`: delegates to `jpaRepo.deleteByTeacherUid(uid)` (Spring Data derived query).
  - `PersistenceAdapter.findByRawCode(rawCode)`: delegates to `jpaRepo.findByRawCode(rawCode)` → maps to domain if present.
  - `PersistenceAdapter.save(code)`: maps to entity → `jpaRepo.save(entity)`.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/persistence/`.
2. Create `PasswordResetCodeJpaEntity.java`:
   ```java
   package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

   import jakarta.persistence.*;
   import lombok.Getter;
   import lombok.NoArgsConstructor;
   import lombok.Setter;
   import java.time.Instant;

   @Entity
   @Table(name = "password_reset_codes")
   @Getter
   @Setter
   @NoArgsConstructor
   class PasswordResetCodeJpaEntity {

       @Id
       @Column(name = "teacher_uid", nullable = false)
       private String teacherUid;

       @Column(name = "raw_code", nullable = false, unique = true)
       private String rawCode;

       @Column(name = "expires_at", nullable = false)
       private Instant expiresAt;

       @Column(name = "created_at", nullable = false)
       private Instant createdAt;

       @Column(name = "used_at")
       private Instant usedAt;
   }
   ```
3. Create `PasswordResetCodeJpaRepository.java` (package-private interface):
   ```java
   package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

   import org.springframework.data.jpa.repository.JpaRepository;
   import java.util.Optional;

   interface PasswordResetCodeJpaRepository extends JpaRepository<PasswordResetCodeJpaEntity, String> {
       Optional<PasswordResetCodeJpaEntity> findByRawCode(String rawCode);
       void deleteByTeacherUid(String teacherUid);
   }
   ```
4. Create `PasswordResetCodePersistenceMapper.java`:
   ```java
   package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

   import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
   import org.springframework.stereotype.Component;

   @Component
   class PasswordResetCodePersistenceMapper {

       PasswordResetCode toDomain(PasswordResetCodeJpaEntity entity) {
           return PasswordResetCode.restore(
               entity.getTeacherUid(),
               entity.getRawCode(),
               entity.getExpiresAt(),
               entity.getCreatedAt(),
               entity.getUsedAt()
           );
       }

       PasswordResetCodeJpaEntity toEntity(PasswordResetCode code) {
           PasswordResetCodeJpaEntity entity = new PasswordResetCodeJpaEntity();
           entity.setTeacherUid(code.getTeacherUid());
           entity.setRawCode(code.getRawCode());
           entity.setExpiresAt(code.getExpiresAt());
           entity.setCreatedAt(code.getCreatedAt());
           entity.setUsedAt(code.getUsedAt());
           return entity;
       }
   }
   ```
5. Create `PasswordResetCodePersistenceAdapter.java`:
   ```java
   package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

   import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
   import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
   import lombok.RequiredArgsConstructor;

   import java.util.Optional;

   // NO @Repository — declared as @Bean in AuthConfig (task-12)
   @RequiredArgsConstructor
   public class PasswordResetCodePersistenceAdapter implements PasswordResetCodeRepositoryPort {

       private final PasswordResetCodeJpaRepository jpaRepository;
       private final PasswordResetCodePersistenceMapper mapper;

       @Override
       public void save(PasswordResetCode code) {
           jpaRepository.save(mapper.toEntity(code));
       }

       @Override
       public Optional<PasswordResetCode> findByRawCode(String rawCode) {
           return jpaRepository.findByRawCode(rawCode).map(mapper::toDomain);
       }

       @Override
       public void deleteByTeacherUid(String teacherUid) {
           jpaRepository.deleteByTeacherUid(teacherUid);
       }
   }
   ```
6. Create test directory `src/test/java/cl/gradeops/ai/api/auth/infrastructure/adapter/out/persistence/`.
7. Create `PasswordResetCodePersistenceAdapterTest.java`:
   - Use `@ExtendWith(MockitoExtension.class)`.
   - Mock `PasswordResetCodeJpaRepository` and `PasswordResetCodePersistenceMapper`.
   - Test `save`: `toEntity` called → `jpaRepository.save` called with result.
   - Test `findByRawCode` present: repo returns entity → `toDomain` called → result is `Optional` of domain aggregate.
   - Test `findByRawCode` absent: repo returns `Optional.empty()` → returns `Optional.empty()`, `toDomain` not called.
   - Test `deleteByTeacherUid`: `jpaRepository.deleteByTeacherUid` called with correct uid.
8. Run `./mvnw test -Dtest=PasswordResetCodePersistenceAdapterTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `save` maps domain → entity then persists | Mockito capture: `toEntity` called with the domain object; `jpaRepository.save` called with mapped entity |
| 2 | `findByRawCode` found → domain returned | Mock repo returns entity; verify `toDomain` called; assert `Optional.of(domainObj)` returned |
| 3 | `findByRawCode` missing → `Optional.empty()` | Mock repo returns `empty()`; verify `toDomain` NOT called |
| 4 | `deleteByTeacherUid` delegates to JPA repo | Verify `jpaRepository.deleteByTeacherUid("uid")` called |
| 5 | `PasswordResetCodeJpaEntity` has no business-logic methods | `grep -n "isExpired\|isUsed\|markUsed" src/main/java/.../PasswordResetCodeJpaEntity.java` returns 0 |

---

## Done Criteria

- [ ] `PasswordResetCodeJpaEntity` is package-private, maps to `password_reset_codes` table, has no business logic
- [ ] `PasswordResetCodeJpaRepository` is package-private, extends `JpaRepository<PasswordResetCodeJpaEntity, String>`
- [ ] `PasswordResetCodePersistenceMapper` converts via `PasswordResetCode.restore()` for `toDomain`
- [ ] `PasswordResetCodePersistenceAdapter` implements `PasswordResetCodeRepositoryPort` (from `auth.application.port.out`); NO `@Repository` annotation — declared as `@Bean` in `AuthConfig`
- [ ] All 4 adapter test cases pass
- [ ] `./mvnw test -Dtest=PasswordResetCodePersistenceAdapterTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-02-auth-bounded-context.md)
