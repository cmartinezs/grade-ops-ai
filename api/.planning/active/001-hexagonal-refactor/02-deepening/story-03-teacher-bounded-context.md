# 🔍 DEEPENING: Story 03 — Teacher Bounded Context

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Create the complete `teacher` feature package: `Teacher` aggregate root (extending `AggregateRoot<TeacherId>`), `TeacherId` value object, `AuthProvider` enum, use case ports, `ProvisionTeacherHandler` (saga pattern with Firebase compensation), `UpdatePilotFlagsHandler`, full persistence stack (JPA entity, Spring Data repo, persistence adapter, mapper), and move `InternalTeacherController` with its request/response DTOs. Delete superseded classes from `domain/teacher/` and `internal/teacher/`.

Depends on **Story 01** (AggregateRoot) and **Story 02** (`IssuePasswordResetCodeUseCase` interface, which `ProvisionTeacherHandler` injects).

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Create `Teacher` aggregate, `TeacherId`, `AuthProvider`, `TeacherNotFoundException`, `TeacherTest`](story-03-teacher-bounded-context/task-01-create-domain-model.md) | IMPLEMENT | DONE | 4 domain files + `TeacherTest.java` |
| 2 | [Create commands + results: `ProvisionTeacherCommand`, `UpdatePilotFlagsCommand`, `ProvisionTeacherResult`, `UpdatePilotFlagsResult`](story-03-teacher-bounded-context/task-02-create-commands-and-results.md) | IMPLEMENT | DONE | 4 record files |
| 3 | [Create port interfaces: `ProvisionTeacherUseCase`, `UpdatePilotFlagsUseCase`, `TeacherRepositoryPort`](story-03-teacher-bounded-context/task-03-create-application-ports.md) | IMPLEMENT | DONE | 3 interface files |
| 4 | [Extend `AuthPort` with `createUser` + `deleteUser`; implement in `FirebaseAuthAdapter`; 3 new test cases](story-03-teacher-bounded-context/task-04-extend-auth-port.md) | IMPLEMENT | DONE | 2 new `AuthPort` methods + 3 test cases |
| 5 | [Create `ProvisionTeacherHandler` (saga + compensation) + `ProvisionTeacherHandlerTest`](story-03-teacher-bounded-context/task-05-create-provision-teacher-handler.md) | IMPLEMENT | DONE | `ProvisionTeacherHandler.java` + `ProvisionTeacherHandlerTest.java` |
| 6 | [Create `UpdatePilotFlagsHandler` + `UpdatePilotFlagsHandlerTest`](story-03-teacher-bounded-context/task-06-create-update-pilot-flags-handler.md) | IMPLEMENT | DONE | `UpdatePilotFlagsHandler.java` + `UpdatePilotFlagsHandlerTest.java` |
| 7 | [Create persistence stack (`TeacherJpaEntity`, `TeacherJpaRepository`, `TeacherPersistenceAdapter`, `TeacherPersistenceMapper`) + update cross-context auth references to `TeacherRepositoryPort`](story-03-teacher-bounded-context/task-07-create-persistence-stack.md) | REFACTOR+IMPLEMENT | DONE | 4 infrastructure files + `TeacherPersistenceAdapterTest.java`; `AuthConfig` updated |
| 8 | [Create `InternalTeacherController` in new package + `@WebMvcTest` tests; delete `internal/teacher/`](story-03-teacher-bounded-context/task-08-create-internal-teacher-controller.md) | REFACTOR+IMPLEMENT | DONE | Controller + 4 DTOs; `internal/teacher/` deleted |
| 9 | [Create `TeacherConfig` (`@Bean` wiring); delete `domain/teacher/`; run `./mvnw test`](story-03-teacher-bounded-context/task-09-cleanup-and-verify.md) | CLEANUP+VERIFY | DONE | `TeacherConfig.java`; `domain/teacher/` deleted; full suite green |

---

## Done Criteria

- [x] `teacher.domain.model.Teacher` is a pure Java aggregate root (extends `AggregateRoot<TeacherId>`, no Spring/JPA annotations)
- [x] `teacher.domain.model.TeacherId` is a value object (`record` or `final class`, wraps String)
- [x] `teacher.domain.model.AuthProvider` is an enum value object
- [x] `TeacherTest` covers `provision(...)` and `restore(...)` factory methods and any invariants
- [x] `ProvisionTeacherHandler` is `@RequiredArgsConstructor` (NO `@Service`) `@Transactional`, injects `AuthPort` + `TeacherRepositoryPort` + `IssuePasswordResetCodeUseCase` by interface; compensation logic deletes Firebase user on DB save failure; declared as `@Bean` in `TeacherConfig`
- [x] `ProvisionTeacherHandler` is **< 40 lines** of actual logic (no orchestrator needed)
- [x] `UpdatePilotFlagsHandler` is `@RequiredArgsConstructor` (NO `@Service`) `@Transactional`, injects `TeacherRepositoryPort` only; declared as `@Bean` in `TeacherConfig`
- [x] `TeacherJpaRepository` is public (used by `TeacherConfig`; same pattern as `PasswordResetCodeJpaRepository`)
- [x] `TeacherPersistenceAdapter` implements `TeacherRepositoryPort`
- [x] `TeacherPersistenceMapper` contains no business logic — structural mapping only
- [x] `InternalTeacherController` injects only `ProvisionTeacherUseCase` and `UpdatePilotFlagsUseCase` (no direct service/repository references)
- [x] `domain/teacher/` and `internal/teacher/` packages fully deleted
- [x] Unit tests exist for `ProvisionTeacherHandler`, `UpdatePilotFlagsHandler`, `TeacherPersistenceAdapter`
- [x] `./mvnw test` passes with 0 failures; `HexagonalArchitectureTest` passes
- [x] TRACEABILITY: `Teacher`, `TeacherId`, `AuthProvider`, `TeacherRepositoryPort`, `ProvisionTeacherUseCase`, `UpdatePilotFlagsUseCase`, `TeacherPersistenceAdapter` all implemented

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | `task-07` pedía `TeacherJpaRepository` **package-private**, pero la story (done criterion línea 41) la pedía **public** porque `TeacherConfig` está en un paquete distinto. El implementador siguió la story correctamente. | `task-07-create-persistence-stack.md` vs. `story-03` línea 41 | RESOLVED | Se acepta `public` como correcto dado el wiring en `TeacherConfig`. `task-07` fue inconsistente con la story; se documenta aquí como incumplimiento del **task**, no del implementador. |

---

## Code Review Fixes (post-implementation)

Los siguientes hallazgos surgieron del code review de la story y fueron corregidos en rama `story-03-teacher-bounded-context`:

| # | Severidad | Hallazgo | Archivo(s) | Responsable | Fix aplicado |
|---|-----------|----------|------------|-------------|--------------|
| 1 | MEDIUM | `Teacher.provision(...)` no protegía invariantes (`firstName`, `lastName`, `email`, `authProvider` podían ser null/blank) | `Teacher.java` | **Implementador** — el task pedía TeacherTest cubriendo invariantes | Añadidas validaciones con `IllegalArgumentException`/`requireNonNull`; 5 tests de invariantes en `TeacherTest` |
| 2 | MEDIUM | Falta test de compensación Firebase cuando falla `issuePasswordResetCodeUseCase.execute(...)` | `ProvisionTeacherHandlerTest.java` | **Implementador** — el task describía el flujo saga completo | Añadido `issue_code_failure_triggers_firebase_compensation` |
| 3 | LOW | Fallo de `deleteUser(...)` en compensación descartado sin log | `ProvisionTeacherHandler.java` | **Implementador** — incumple guideline 11 (observabilidad) | Añadido `log.warn(...)` con `firebaseUid` y causa |
| 4 | LOW | `TeacherJpaRepository` public vs. task que pedía package-private | `TeacherJpaRepository.java` | **Story/task inconsistency** — ver tabla de Inconsistencies Found | Sin cambio de código; documentado como inconsistencia de planning |
| 5 | HIGH | `@RequestBody` sin `@Valid`; request DTOs sin Bean Validation | `InternalTeacherController.java`, `ProvisionTeacherRequest.java`, `UpdatePilotFlagsRequest.java` | **Implementador** — incumple guideline 09 (API REST / validación) | Añadidos `@Valid`, `@NotBlank`, `@Email`, `@Size`; 3 tests de payload inválido (→ 422) |
| 6 | MEDIUM | URL construida con concatenación string (`webBaseUrl + "/reset-password?code=..."`) puede generar doble slash | `InternalTeacherController.java` | **Implementador** — robustez de API | Reemplazado con `UriComponentsBuilder`; test de trailing slash |
| 7 | MEDIO | Nombres de tests no siguen convención `should...When...` de guideline 10 — 43 tests usaban snake_case | Todos los archivos `*Test.java` del bounded context | **Implementador** — guideline 10 § Nombres de tests es explícita | Renombrados todos los tests al formato `should...When...` |
| 8 | MEDIO | Anti-patrón: `save_delegates_to_jpa_repository` solo verificaba que se llamara `jpaRepository.save(...)` sin verificar el mapeo | `TeacherPersistenceAdapterTest.java` | **Implementador** — guideline 10 § Anti-patterns ("tests que solo verifican getters/setters") | Reemplazado con `shouldMapDomainToEntityAndDelegateToJpaRepositoryWhenSaving` usando `ArgumentCaptor` para verificar el contenido de la entidad |
| 9 | ALTO | Falta cobertura de seguridad: "acceso sin token" y "acceso con clave incorrecta" para `/internal/**` — guideline 10 § API / Seguridad | Ningún test cubría `InternalAuthFilter` | **Implementador** — guideline 10 § Qué probar obligatoriamente (Seguridad) | Creado `InternalAuthFilterTest` con 4 tests unitarios puros: sin header → 403, clave incorrecta → 403, clave correcta → delega, path no-interno → delega |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
