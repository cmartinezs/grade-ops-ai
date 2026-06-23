# ⚛️ TASK 07 — Create teacher persistence stack + update cross-context auth references

> **Status:** TODO
> **Workflow:** REFACTOR+IMPLEMENT
> **Depends On:** task-01, task-03
> [← story file](../story-03-teacher-bounded-context.md)

---

## Objective

Create `TeacherJpaEntity` (renamed from `TeacherEntity`), `TeacherJpaRepository` (package-private), `TeacherPersistenceMapper`, `TeacherPersistenceAdapter` (implements `TeacherRepositoryPort`), and `TeacherPersistenceAdapterTest`. In the same task, update `RegisterHandler`, `SendPasswordResetEmailOrchestrator`, and `ResetPasswordOrchestrator` (all Story 02 files) to inject `TeacherRepositoryPort` instead of the old `domain.teacher.TeacherRepository`. Update `AuthConfig` accordingly.

---

## Technical Design

- **Approach:** `TeacherJpaEntity` is a direct rename of `domain/teacher/TeacherEntity` — same table name (`teacher`), same column names, same 13 fields; JPA annotations untouched. No business logic on the JPA entity. `TeacherJpaRepository` is package-private (only `TeacherPersistenceAdapter` uses it). `TeacherPersistenceMapper.toDomain()` calls `Teacher.restore(...)` with all 13 fields. `TeacherPersistenceMapper.toEntity()` maps from aggregate getters. `TeacherPersistenceAdapter` is a plain class with `@RequiredArgsConstructor` — declared as `@Bean` in `TeacherConfig` (task-09).
- **Cross-context update:** `RegisterHandler`, `SendPasswordResetEmailOrchestrator`, and `ResetPasswordOrchestrator` in the auth bounded context currently inject `domain.teacher.TeacherRepository` (old JPA Spring Data interface). This task replaces those injections with `TeacherRepositoryPort`. This update is safe now because `TeacherPersistenceAdapter` implements the port and will be available in the Spring context when `TeacherConfig` is created in task-09. `AuthConfig` must also be updated: the `@Bean` declarations for those three beans replace the `TeacherRepository` parameter with `TeacherRepositoryPort`.
- **Affected files / components (new):**
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/out/persistence/TeacherJpaEntity.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/out/persistence/TeacherJpaRepository.java` ← NEW (package-private)
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/out/persistence/TeacherPersistenceMapper.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/out/persistence/TeacherPersistenceAdapter.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/out/persistence/TeacherPersistenceAdapterTest.java` ← NEW
- **Affected files / components (modified):**
  - `src/main/java/cl/gradeops/ai/api/auth/application/usecase/RegisterHandler.java` ← inject `TeacherRepositoryPort` (replaces `domain.teacher.TeacherRepository`)
  - `src/main/java/cl/gradeops/ai/api/auth/application/orchestrator/SendPasswordResetEmailOrchestrator.java` ← same
  - `src/main/java/cl/gradeops/ai/api/auth/application/orchestrator/ResetPasswordOrchestrator.java` ← same
  - `src/main/java/cl/gradeops/ai/api/auth/infrastructure/config/AuthConfig.java` ← update 3 `@Bean` methods to inject `TeacherRepositoryPort`
- **Design notes on mapper:**
  - `toDomain(TeacherJpaEntity entity) → Teacher`: calls `Teacher.restore(entity.getFirebaseUid(), ..., AuthProvider.valueOf(entity.getProvider()), ...)`.
  - `toEntity(Teacher teacher) → TeacherJpaEntity`: creates a new `TeacherJpaEntity()` (default constructor) and sets all fields via setters.
  - The `AuthProvider` enum value maps to the DB column string: `EMAIL_PASSWORD` ↔ `"EMAIL_PASSWORD"`, `GOOGLE` ↔ `"GOOGLE"`.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/out/persistence/`.
2. Create `TeacherJpaEntity.java` — copy `domain/teacher/TeacherEntity.java` body, change:
   - Package: `cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence`
   - Class name: `TeacherJpaEntity`
   - Add `@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)` (Lombok); remove old manual constructors (mapper uses setters)
   - Keep all `@Entity`, `@Table`, `@Column`, `@Id` annotations unchanged (same DB schema)
3. Create `TeacherJpaRepository.java` (package-private):
   ```java
   package cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence;

   import org.springframework.data.jpa.repository.JpaRepository;
   import java.util.Optional;

   interface TeacherJpaRepository extends JpaRepository<TeacherJpaEntity, String> {
       Optional<TeacherJpaEntity> findByEmail(String email);
       boolean existsByEmail(String email);
   }
   ```
4. Create `TeacherPersistenceMapper.java`:
   ```java
   package cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence;

   import cl.gradeops.ai.api.teacher.domain.model.AuthProvider;
   import cl.gradeops.ai.api.teacher.domain.model.Teacher;

   class TeacherPersistenceMapper {

       Teacher toDomain(TeacherJpaEntity e) {
           return Teacher.restore(
               e.getFirebaseUid(), e.getFirstName(), e.getLastName(),
               e.getEmail(), AuthProvider.valueOf(e.getProvider()),
               e.getCreatedAt(), e.getUpdatedAt(),
               e.getPlanType(), e.isRelatedParty(),
               e.getOfferDetails(), e.getEvidenceLink(),
               e.getFlagSetBy(), e.getFlagSetAt()
           );
       }

       TeacherJpaEntity toEntity(Teacher t) {
           TeacherJpaEntity e = new TeacherJpaEntity();
           e.setFirebaseUid(t.getFirebaseUid());
           e.setFirstName(t.getFirstName());
           e.setLastName(t.getLastName());
           e.setEmail(t.getEmail());
           e.setProvider(t.getAuthProvider().name());
           e.setCreatedAt(t.getCreatedAt());
           e.setUpdatedAt(t.getUpdatedAt());
           e.setPlanType(t.getPlanType());
           e.setRelatedParty(t.isRelatedParty());
           e.setOfferDetails(t.getOfferDetails());
           e.setEvidenceLink(t.getEvidenceLink());
           e.setFlagSetBy(t.getFlagSetBy());
           e.setFlagSetAt(t.getFlagSetAt());
           return e;
       }
   }
   ```
5. Create `TeacherPersistenceAdapter.java`:
   ```java
   package cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence;

   import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
   import cl.gradeops.ai.api.teacher.domain.model.Teacher;
   import lombok.RequiredArgsConstructor;
   import java.util.Optional;

   // NO @Repository — declared as @Bean in TeacherConfig (task-09)
   @RequiredArgsConstructor
   public class TeacherPersistenceAdapter implements TeacherRepositoryPort {

       private final TeacherJpaRepository jpaRepository;
       private final TeacherPersistenceMapper mapper;

       @Override
       public void save(Teacher teacher) {
           jpaRepository.save(mapper.toEntity(teacher));
       }

       @Override
       public Optional<Teacher> findById(String firebaseUid) {
           return jpaRepository.findById(firebaseUid).map(mapper::toDomain);
       }

       @Override
       public Optional<Teacher> findByEmail(String email) {
           return jpaRepository.findByEmail(email).map(mapper::toDomain);
       }

       @Override
       public boolean existsByEmail(String email) {
           return jpaRepository.existsByEmail(email);
       }
   }
   ```
6. Create test directory `src/test/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/out/persistence/`.
7. Create `TeacherPersistenceAdapterTest.java`:
   - `@ExtendWith(MockitoExtension.class)`.
   - Mock `TeacherJpaRepository` and `TeacherPersistenceMapper`.
   - Test `save`: `toEntity` called → `jpaRepository.save` called with mapped entity.
   - Test `findById` found: repo returns entity → `toDomain` called → `Optional` of domain returned.
   - Test `findById` missing: `Optional.empty()` returned; `toDomain` not called.
   - Test `findByEmail` found: same pattern as `findById` found.
   - Test `existsByEmail` true/false: delegates to `jpaRepository.existsByEmail`.
8. Update **`RegisterHandler`**: replace `domain.teacher.TeacherRepository` field with `TeacherRepositoryPort` (from `teacher.application.port.out`); update all usages (`findById`, `save`, `existsByEmail`) to the port's method signatures. `findById` returns `Optional<Teacher>` — update any `.get()` calls accordingly.
9. Update **`SendPasswordResetEmailOrchestrator`**: replace `domain.teacher.TeacherRepository` field with `TeacherRepositoryPort`; update `findByEmail` call (returns `Optional<Teacher>` now, not `Optional<TeacherEntity>`); access `teacher.getEmail()`, `teacher.getAuthProvider().name()` (or compare `AuthProvider.EMAIL_PASSWORD`) instead of `teacher.getProvider()`.
10. Update **`ResetPasswordOrchestrator`**: same — replace `TeacherRepository` with `TeacherRepositoryPort`; update `findById` usage.
11. Update **`AuthConfig`**: in the 3 affected `@Bean` methods (`registerHandler`, `sendPasswordResetEmailOrchestrator`, `resetPasswordOrchestrator`), replace the `TeacherRepository teacherRepository` parameter with `TeacherRepositoryPort teacherRepository`.
12. Run `./mvnw test -Dtest=TeacherPersistenceAdapterTest,RegisterHandlerTest,SendPasswordResetEmailOrchestratorTest,ResetPasswordOrchestratorTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `save` maps domain → entity then persists | Verify `toEntity` called; `jpaRepository.save` called |
| 2 | `findById` found → domain returned | Assert `Optional.of(domainObj)` |
| 3 | `findById` missing → `Optional.empty()` | Assert empty; `toDomain` not called |
| 4 | `findByEmail` found → domain returned | Same as `findById` found |
| 5 | `existsByEmail` delegates to JPA repo | `verify(jpaRepository).existsByEmail("x@y.com")` |
| 6 | `TeacherJpaEntity` has no business logic | `grep -n "updatePilotFlags\|isExpired\|isUsed" src/main/java/.../TeacherJpaEntity.java` → 0 results |
| 7 | Auth handler tests still pass after refactor | `-Dtest=RegisterHandlerTest,...` all green |

---

## Done Criteria

- [ ] `TeacherJpaEntity` in `teacher/infrastructure/adapter/out/persistence/`; same table/columns as old `TeacherEntity`; no business logic
- [ ] `TeacherJpaRepository` is package-private; extends `JpaRepository<TeacherJpaEntity, String>`
- [ ] `TeacherPersistenceAdapter` implements `TeacherRepositoryPort`; NO `@Repository`
- [ ] `TeacherPersistenceMapper.toDomain()` calls `Teacher.restore()`
- [ ] `RegisterHandler`, `SendPasswordResetEmailOrchestrator`, `ResetPasswordOrchestrator` now inject `TeacherRepositoryPort` — no more `domain.teacher.TeacherRepository` references
- [ ] `AuthConfig` updated: 3 `@Bean` methods use `TeacherRepositoryPort` parameter
- [ ] All 5 adapter test cases pass
- [ ] `./mvnw test -Dtest=TeacherPersistenceAdapterTest,RegisterHandlerTest,SendPasswordResetEmailOrchestratorTest,ResetPasswordOrchestratorTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-03-teacher-bounded-context.md)
