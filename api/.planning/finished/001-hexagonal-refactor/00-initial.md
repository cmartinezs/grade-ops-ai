# 🌱 INITIAL: 001-hexagonal-refactor

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

Refactorizar `grade-ops-ai/api` a arquitectura hexagonal completa con estructura feature-package, DDD táctico, use cases explícitos con patrón orquestador/pasos, y todos los patrones definidos en `docs/gradeops-ai-java-guidelines/` — sin cambiar comportamiento externo ni esquema de base de datos.

---

## Why

El código actual mezcla responsabilidades por capa técnica (`auth/`, `common/`, `config/`, `domain/teacher/`, `internal/`, `security/`, `port/`). Esto produce:

- Controladores que conocen repositorios JPA directamente.
- Servicios concretos sin interfaz de caso de uso (`AuthService`, `PasswordResetService`, `AssessmentService`).
- Entidad JPA `TeacherEntity` ubicada en `domain/` — violación directa de la guideline 08.
- `PasswordResetCodeEntity` con lógica de dominio embebida en la entidad JPA.
- Ausencia de modelos de dominio ricos (`Teacher`, `PasswordResetCode` como aggregates).
- Puertos dispersos en `port/` sin relación con su bounded context.
- Sin `AggregateRoot`, `DomainEvent`, ni arquitectura de eventos.
- Sin tests de arquitectura (ArchUnit).
- Sin patrón orquestador; la coordinación compleja está embebida en services monolíticos.

La refactorización establece la base estructural necesaria para el crecimiento hacia los 13 agentes AI, billing, auditoría y multi-tenancy definidos en el dominio de GradeOps AI.

---

## Arquitectura objetivo

### Regla de dependencias

```text
infrastructure / adapters  →  application  →  domain
api / controllers          →  application  →  domain
```

`domain` no depende de nadie. `application` solo depende de `domain`. `infrastructure` implementa puertos de `application`.

### Estructura de paquetes por feature

```text
cl.gradeops.ai.api.<feature>
├── domain/
│   ├── model/          ← Aggregate roots, value objects, domain entities
│   ├── event/          ← Domain events (interface DomainEvent, eventos concretos)
│   ├── service/        ← Domain services (lógica cross-aggregate)
│   └── exception/      ← Domain exceptions
├── application/
│   ├── port/
│   │   ├── in/         ← UseCase interfaces (puertos de entrada)
│   │   └── out/        ← RepositoryPort, external service ports (puertos de salida)
│   ├── usecase/        ← Handlers (implementaciones de use cases simples)
│   ├── orchestrator/   ← Orchestrators (use cases complejos con 4+ colaboradores)
│   ├── step/           ← Steps (pipeline de pasos para flujos largos)
│   ├── command/        ← Command records (@Builder)
│   ├── query/          ← Query records (@Builder)
│   └── result/         ← Result records (@Builder)
└── infrastructure/
    └── adapter/
        ├── in/
        │   └── web/    ← Controllers, request/, response/
        └── out/
            ├── persistence/  ← JpaEntity, JpaRepository, PersistenceAdapter, PersistenceMapper
            ├── firebase/     ← Firebase adapters
            ├── email/        ← Email adapters
            └── storage/      ← Storage adapters
```

### Base package

`cl.gradeops.ai.api` — no se cambia. Los features son `teacher`, `auth`, `assessment`, `shared`.

---

## Estructura objetivo completa

### `shared` (shared-kernel)

```text
cl.gradeops.ai.api.shared
├── domain/
│   ├── model/
│   │   └── AggregateRoot.java                  ← NEW base class con pullDomainEvents()
│   ├── event/
│   │   └── DomainEvent.java                    ← NEW interface (eventId, occurredAt, eventType, aggregateId)
│   └── exception/
│       ├── DomainException.java                ← NEW base exception
│       ├── ResourceNotFoundException.java      ← moved from common/
│       └── DuplicateEmailException.java        ← moved from common/
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       ├── GlobalExceptionHandler.java ← moved from common/
    │   │       ├── ApiErrorResponse.java       ← renamed from common/ApiError
    │   │       ├── FieldErrorResponse.java     ← NEW (per guideline 09)
    │   │       └── InvalidTokenException.java  ← moved from auth/
    │   └── out/
    │       ├── email/
    │       │   └── JavaMailEmailService.java   ← renamed from email/EmailService
    │       └── storage/
    │           ├── StoragePort.java            ← moved from port/
    │           ├── GcsStorageAdapter.java      ← moved from adapter/storage/
    │           └── R2StorageAdapter.java       ← moved from adapter/storage/
    └── config/
        ├── FirebaseConfig.java                 ← moved from config/
        ├── JacksonConfig.java                  ← moved from config/
        ├── GradeOpsEmailProperties.java        ← moved from config/
        ├── GradeOpsWebProperties.java          ← moved from config/
        └── security/
            ├── SecurityConfig.java             ← moved from security/
            ├── FirebaseTokenFilter.java        ← moved from security/
            ├── EmailVerifiedFilter.java        ← moved from security/
            ├── InternalAuthFilter.java         ← moved from security/
            ├── OwnershipVerifier.java          ← moved from security/
            └── AuthenticatedTeacher.java       ← moved from security/
```

### `teacher` (bounded context)

```text
cl.gradeops.ai.api.teacher
├── domain/
│   ├── model/
│   │   ├── Teacher.java                           ← NEW aggregate root (extiende AggregateRoot<TeacherId>)
│   │   ├── TeacherId.java                         ← NEW value object (wraps String firebase uid)
│   │   └── AuthProvider.java                      ← NEW enum value object
│   └── exception/
│       └── TeacherNotFoundException.java           ← NEW domain exception
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── ProvisionTeacherUseCase.java        ← NEW interface
│   │   │   └── UpdatePilotFlagsUseCase.java        ← NEW interface
│   │   └── out/
│   │       └── TeacherRepositoryPort.java          ← NEW interface
│   ├── usecase/
│   │   ├── ProvisionTeacherHandler.java            ← refactor ProvisionTeacherService
│   │   └── UpdatePilotFlagsHandler.java            ← refactor PilotFlagService
│   ├── command/
│   │   ├── ProvisionTeacherCommand.java            ← NEW record @Builder
│   │   └── UpdatePilotFlagsCommand.java            ← NEW record @Builder
│   └── result/
│       ├── ProvisionTeacherResult.java             ← NEW record @Builder
│       └── UpdatePilotFlagsResult.java             ← NEW record @Builder
└── infrastructure/
    └── adapter/
        ├── in/
        │   └── web/
        │       ├── InternalTeacherController.java  ← moved from internal/teacher/
        │       ├── request/
        │       │   ├── ProvisionTeacherRequest.java ← moved
        │       │   └── PilotFlagRequest.java        ← moved
        │       └── response/
        │           ├── ProvisionTeacherResponse.java ← moved
        │           └── PilotFlagResponse.java        ← moved
        └── out/
            └── persistence/
                ├── TeacherJpaEntity.java            ← renamed + moved from domain/teacher/TeacherEntity
                ├── TeacherJpaRepository.java        ← renamed from domain/teacher/TeacherRepository
                ├── TeacherPersistenceAdapter.java   ← NEW implements TeacherRepositoryPort
                └── TeacherPersistenceMapper.java    ← NEW (toDomain/toEntity)
```

### `auth` (bounded context)

```text
cl.gradeops.ai.api.auth
├── domain/
│   ├── model/
│   │   ├── PasswordResetCode.java                    ← NEW aggregate root (puro, sin JPA)
│   │   └── TeacherIdentity.java                      ← moved from port/ (value object de autenticación)
│   └── exception/
│       └── InvalidResetCodeException.java            ← NEW domain exception
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── RegisterUseCase.java                  ← NEW interface
│   │   │   ├── SignOutUseCase.java                   ← NEW interface
│   │   │   ├── RevokeRefreshTokensUseCase.java       ← NEW atomic interface (reusable)
│   │   │   ├── IssuePasswordResetCodeUseCase.java    ← NEW atomic interface (reusable)
│   │   │   ├── SendPasswordResetEmailUseCase.java    ← NEW interface (delegates to orchestrator)
│   │   │   └── ResetPasswordUseCase.java             ← NEW interface (delegates to orchestrator)
│   │   └── out/
│   │       ├── AuthPort.java                         ← moved from port/ (Firebase auth operations)
│   │       ├── PasswordResetCodeRepositoryPort.java  ← NEW interface
│   │       └── EmailNotificationPort.java            ← NEW interface
│   ├── usecase/
│   │   ├── RegisterHandler.java                      ← refactor AuthService.register()
│   │   ├── SignOutHandler.java                       ← refactor AuthService.signOut()
│   │   ├── RevokeRefreshTokensHandler.java           ← NEW atomic (reused by SignOut + ResetPassword)
│   │   └── IssuePasswordResetCodeHandler.java        ← NEW atomic (reused by SendEmail + Provision)
│   ├── orchestrator/
│   │   ├── SendPasswordResetEmailOrchestrator.java   ← NEW (coordinates: find teacher + issue code + send email)
│   │   └── ResetPasswordOrchestrator.java            ← NEW (coordinates: validate + update pwd + revoke + mark used)
│   ├── command/
│   │   ├── RegisterCommand.java                      ← NEW record @Builder
│   │   ├── IssuePasswordResetCodeCommand.java        ← NEW record @Builder
│   │   ├── SendPasswordResetEmailCommand.java        ← NEW record @Builder
│   │   └── ResetPasswordCommand.java                 ← NEW record @Builder
│   └── result/
│       ├── RegisterResult.java                       ← moved from auth/
│       └── IssuePasswordResetCodeResult.java         ← NEW record @Builder
└── infrastructure/
    └── adapter/
        ├── in/
        │   └── web/
        │       ├── AuthController.java               ← moved from auth/
        │       ├── request/
        │       │   ├── RegisterRequest.java          ← moved
        │       │   ├── ForgotPasswordRequest.java    ← moved
        │       │   └── ResetPasswordRequest.java     ← moved
        │       └── response/
        │           └── RegisterResponse.java         ← moved
        └── out/
            ├── firebase/
            │   └── FirebaseAuthAdapter.java          ← moved from adapter/auth/
            ├── persistence/
            │   ├── PasswordResetCodeJpaEntity.java   ← renamed + moved from auth/PasswordResetCodeEntity
            │   ├── PasswordResetCodeJpaRepository.java ← renamed + moved
            │   ├── PasswordResetCodePersistenceAdapter.java ← NEW implements PasswordResetCodeRepositoryPort
            │   └── PasswordResetCodePersistenceMapper.java  ← NEW (toDomain/toEntity)
            └── email/
                └── ThymeleafEmailNotificationAdapter.java   ← NEW implements EmailNotificationPort
```

### `assessment` (bounded context — stub)

```text
cl.gradeops.ai.api.assessment
├── domain/
│   └── model/
│       └── AssessmentStatus.java              ← moved from assessment/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── ListAssessmentsUseCase.java     ← NEW interface
│   │   └── out/
│   │       └── AssessmentRepositoryPort.java   ← NEW stub interface
│   ├── usecase/
│   │   └── ListAssessmentsHandler.java         ← refactor AssessmentService
│   └── result/
│       └── AssessmentSummaryResult.java        ← NEW record @Builder
└── infrastructure/
    └── adapter/
        └── in/
            └── web/
                ├── AssessmentController.java   ← moved from assessment/
                └── response/
                    └── AssessmentSummaryResponse.java ← renamed from AssessmentSummaryDto
```

---

## Reglas críticas por capa (guidelines obligatorias)

### domain/

- **Prohibido**: `@Service`, `@Component`, `@Autowired`, `@Entity`, `@Table`, `@Column`, `@ManyToOne`, `@JsonProperty`, `@NotNull` (Bean Validation), cualquier import `org.springframework.*`, `jakarta.persistence.*`, `jakarta.validation.*`
- Los aggregates extienden `AggregateRoot<ID>`
- Constructores privados; factory methods: `register(...)`, `draft(...)`, `issue(...)`
- Restauración desde persistencia: factory method `restore(...)` — no emite eventos
- No setters públicos para campos con reglas de negocio — solo métodos con nombre de intención
- Los value objects son `record` o clase `final` con validación en el constructor
- Los domain events son `record` que implementan `DomainEvent`

### application/

- Handlers: `@Service` + `@RequiredArgsConstructor`
- Orchestrators: `@Component` + `@RequiredArgsConstructor`
- `@Transactional` solo en handlers (no en orchestrators)
- Los handlers solo inyectan puertos de salida (`port/out/`) u otros use cases atómicos por interfaz (`port/in/`)
- Los orchestrators inyectan handlers atómicos por interfaz y puertos de salida
- Los comandos son `record` con `@Builder` y validación de nulls en compact constructor
- Los resultados son `record` con `@Builder`
- Los handlers NO retornan DTOs REST ni conocen HTTP

### infrastructure/

- JPA entities: `@Getter @Setter @Entity @Table @NoArgsConstructor(access = AccessLevel.PROTECTED)` — sin `@Data`
- Spring Data repos: `package-private` (solo los usa su persistence adapter)
- Persistence adapters: `@Component @RequiredArgsConstructor`, implementan puerto de salida
- Los mappers no contienen reglas de negocio; solo conversión estructural
- Los controllers inyectan use cases por interfaz (`port/in/`); nunca repositories
- Controllers: `@RequiredArgsConstructor`, mapean request → command, invocan use case, mapean result → response
- Requests con Bean Validation en los campos; sin lógica de negocio en validaciones custom
- `@Transactional` prohibido en controllers

### Lombok por capa (guideline 07)

| Capa | Permitido | Prohibido |
|---|---|---|
| `domain/` | `@Getter`, `@EqualsAndHashCode` en VO, `@Builder` con cuidado | `@Data`, `@Setter`, `@Slf4j`, `@SneakyThrows` |
| `application/` | `@RequiredArgsConstructor`, `@Builder` en commands/results, `@Slf4j` | `@Data` en commands con invariantes |
| `infrastructure/` | `@RequiredArgsConstructor`, `@Slf4j`, `@Getter @Setter` en JpaEntity | `@Data` en `@Entity`, `@SneakyThrows` |
| controllers | `@RequiredArgsConstructor`, `@Builder` en request/response | `@Data` con datos sensibles |

---

## Patrón orquestador/pasos aplicado (guideline 03)

### Use cases atómicos — nivel 1 (handler directo)

| Use case | Justificación |
|---|---|
| `RegisterUseCase` | 2 puertos (AuthPort + TeacherRepositoryPort), flujo lineal, < 40 líneas |
| `SignOutUseCase` | delega a `RevokeRefreshTokensUseCase` |
| `RevokeRefreshTokensUseCase` | **atómico** — 1 operación de puerto; reutilizado por SignOut y ResetPassword |
| `IssuePasswordResetCodeUseCase` | **atómico** — lógica encapsulada (delete + issue + save); reutilizado por SendEmail y Provision |
| `UpdatePilotFlagsUseCase` | 1 puerto, flujo lineal |
| `ListAssessmentsUseCase` | stub, sin lógica aún |

### Use cases con orquestador — nivel 2

| Handler (delgado) | Orquestador | Colaboradores coordinados |
|---|---|---|
| `SendPasswordResetEmailHandler` | `SendPasswordResetEmailOrchestrator` | `TeacherRepositoryPort` + `IssuePasswordResetCodeUseCase` + `EmailNotificationPort` |
| `ResetPasswordHandler` | `ResetPasswordOrchestrator` | `PasswordResetCodeRepositoryPort` + `AuthPort.updatePassword` + `RevokeRefreshTokensUseCase` |
| `ProvisionTeacherHandler` | *(saga inline — cabe en 40 líneas)* | `AuthPort.createUser` + `TeacherRepositoryPort` + `IssuePasswordResetCodeUseCase` + compensación |

> **Nota sobre ProvisionTeacher**: actualmente implementa una saga (crear Firebase user → guardar en DB → generar link; compensar si falla). Se mantiene como handler sin orquestador separado porque cabe limpiamente en < 40 líneas y no tiene pasos reutilizables adicionales. Si crece, extraer `ProvisionTeacherOrchestrator`.

---

## Convención de nombres (guideline 06)

| Elemento | Convención | Ejemplo |
|---|---|---|
| Aggregate Root | Nombre negocio | `Teacher`, `PasswordResetCode` |
| Value Object | Nombre concepto | `TeacherId`, `AuthProvider`, `TeacherIdentity` |
| Domain Event | Pasado + `Event` | `TeacherProvisionedEvent` (futuro) |
| Use case port in | `<Verb><Object>UseCase` | `ProvisionTeacherUseCase`, `RegisterUseCase` |
| Handler | `<Verb><Object>Handler` | `ProvisionTeacherHandler`, `RegisterHandler` |
| Orchestrator | `<Verb><Object>Orchestrator` | `SendPasswordResetEmailOrchestrator` |
| Command | `<Verb><Object>Command` | `ProvisionTeacherCommand` |
| Result | `<Verb><Object>Result` | `ProvisionTeacherResult` |
| Port out — repo | Aggregate + `RepositoryPort` | `TeacherRepositoryPort` |
| Port out — service | Capacidad + `Port` | `AuthPort`, `EmailNotificationPort` |
| JPA Entity | Aggregate + `JpaEntity` | `TeacherJpaEntity` |
| Spring Data repo | Aggregate + `JpaRepository` | `TeacherJpaRepository` |
| Persistence adapter | Feature + `PersistenceAdapter` | `TeacherPersistenceAdapter` |
| Mapper | Feature + `PersistenceMapper` | `TeacherPersistenceMapper` |
| Controller | Recurso + `Controller` | `AuthController`, `InternalTeacherController` |
| Request DTO | Acción + `Request` | `RegisterRequest` |
| Response DTO | Acción/Recurso + `Response` | `RegisterResponse`, `AssessmentSummaryResponse` |

---

## Mapeo de migración detallado

### Moves (solo cambio de package + posible rename)

| Actual | Objetivo |
|---|---|
| `common.GlobalExceptionHandler` | `shared.infrastructure.adapter.in.web.GlobalExceptionHandler` |
| `common.ApiError` | `shared.infrastructure.adapter.in.web.ApiErrorResponse` |
| `common.DuplicateEmailException` | `shared.domain.exception.DuplicateEmailException` |
| `common.ResourceNotFoundException` | `shared.domain.exception.ResourceNotFoundException` |
| `auth.InvalidTokenException` | `shared.infrastructure.adapter.in.web.InvalidTokenException` |
| `config.FirebaseConfig` | `shared.infrastructure.config.FirebaseConfig` |
| `config.JacksonConfig` | `shared.infrastructure.config.JacksonConfig` |
| `config.GradeOpsEmailProperties` | `shared.infrastructure.config.GradeOpsEmailProperties` |
| `config.GradeOpsWebProperties` | `shared.infrastructure.config.GradeOpsWebProperties` |
| `email.EmailService` | `shared.infrastructure.adapter.out.email.JavaMailEmailService` |
| `security.SecurityConfig` | `shared.infrastructure.config.security.SecurityConfig` |
| `security.FirebaseTokenFilter` | `shared.infrastructure.config.security.FirebaseTokenFilter` |
| `security.EmailVerifiedFilter` | `shared.infrastructure.config.security.EmailVerifiedFilter` |
| `security.InternalAuthFilter` | `shared.infrastructure.config.security.InternalAuthFilter` |
| `security.OwnershipVerifier` | `shared.infrastructure.config.security.OwnershipVerifier` |
| `security.AuthenticatedTeacher` | `shared.infrastructure.config.security.AuthenticatedTeacher` |
| `port.StoragePort` | `shared.infrastructure.adapter.out.storage.StoragePort` |
| `adapter.storage.GcsStorageAdapter` | `shared.infrastructure.adapter.out.storage.GcsStorageAdapter` |
| `adapter.storage.R2StorageAdapter` | `shared.infrastructure.adapter.out.storage.R2StorageAdapter` |
| `port.AuthPort` | `auth.application.port.out.AuthPort` |
| `port.TeacherIdentity` | `auth.domain.model.TeacherIdentity` |
| `adapter.auth.FirebaseAuthAdapter` | `auth.infrastructure.adapter.out.firebase.FirebaseAuthAdapter` |
| `auth.AuthController` | `auth.infrastructure.adapter.in.web.AuthController` |
| `auth.RegisterRequest` | `auth.infrastructure.adapter.in.web.request.RegisterRequest` |
| `auth.ForgotPasswordRequest` | `auth.infrastructure.adapter.in.web.request.ForgotPasswordRequest` |
| `auth.ResetPasswordRequest` | `auth.infrastructure.adapter.in.web.request.ResetPasswordRequest` |
| `auth.RegisterResponse` | `auth.infrastructure.adapter.in.web.response.RegisterResponse` |
| `auth.RegisterResult` | `auth.application.result.RegisterResult` |
| `auth.PasswordResetCodeEntity` | `auth.infrastructure.adapter.out.persistence.PasswordResetCodeJpaEntity` |
| `auth.PasswordResetCodeRepository` | `auth.infrastructure.adapter.out.persistence.PasswordResetCodeJpaRepository` |
| `internal.teacher.InternalTeacherController` | `teacher.infrastructure.adapter.in.web.InternalTeacherController` |
| `internal.teacher.ProvisionTeacherRequest` | `teacher.infrastructure.adapter.in.web.request.ProvisionTeacherRequest` |
| `internal.teacher.PilotFlagRequest` | `teacher.infrastructure.adapter.in.web.request.PilotFlagRequest` |
| `internal.teacher.ProvisionTeacherResponse` | `teacher.infrastructure.adapter.in.web.response.ProvisionTeacherResponse` |
| `internal.teacher.PilotFlagResponse` | `teacher.infrastructure.adapter.in.web.response.PilotFlagResponse` |
| `domain.teacher.TeacherEntity` | `teacher.infrastructure.adapter.out.persistence.TeacherJpaEntity` |
| `domain.teacher.TeacherRepository` | `teacher.infrastructure.adapter.out.persistence.TeacherJpaRepository` |
| `assessment.AssessmentController` | `assessment.infrastructure.adapter.in.web.AssessmentController` |
| `assessment.AssessmentStatus` | `assessment.domain.model.AssessmentStatus` |
| `assessment.AssessmentSummaryDto` | `assessment.infrastructure.adapter.in.web.response.AssessmentSummaryResponse` |

### Archivos que se dividen o transforman profundamente

| Actual | Genera |
|---|---|
| `auth.AuthService` | `auth.application.usecase.RegisterHandler` + `auth.application.usecase.SignOutHandler` |
| `auth.PasswordResetService` | `auth.application.orchestrator.SendPasswordResetEmailOrchestrator` + `auth.application.orchestrator.ResetPasswordOrchestrator` + handlers atómicos |
| `internal.teacher.ProvisionTeacherService` | `teacher.application.usecase.ProvisionTeacherHandler` |
| `internal.teacher.PilotFlagService` | `teacher.application.usecase.UpdatePilotFlagsHandler` |
| `assessment.AssessmentService` | `assessment.application.usecase.ListAssessmentsHandler` |
| `auth.PasswordResetCodeEntity` (JPA + lógica) | `auth.domain.model.PasswordResetCode` (lógica pura) + `auth.infrastructure.adapter.out.persistence.PasswordResetCodeJpaEntity` (JPA) |
| `domain.teacher.TeacherEntity` (JPA) | `teacher.domain.model.Teacher` (aggregate, NEW) + `teacher.infrastructure.adapter.out.persistence.TeacherJpaEntity` (JPA) |

---

## Estrategia de testing (guideline 10)

### Tests nuevos a crear

| Test | Tipo | Herramienta |
|---|---|---|
| `TeacherTest` | Dominio puro — invariantes del aggregate | JUnit 5 (sin Spring) |
| `PasswordResetCodeTest` | Dominio puro — isExpired, isUsed, markUsed | JUnit 5 (sin Spring) |
| `RegisterHandlerTest` | Application — coordinación | JUnit 5 + Mockito |
| `ProvisionTeacherHandlerTest` | Application — saga con compensación | JUnit 5 + Mockito |
| `IssuePasswordResetCodeHandlerTest` | Application — atómico | JUnit 5 + Mockito |
| `SendPasswordResetEmailOrchestratorTest` | Application — orquestador | JUnit 5 + Mockito |
| `ResetPasswordOrchestratorTest` | Application — orquestador | JUnit 5 + Mockito |
| `TeacherPersistenceAdapterTest` | Adapter — mapeo JPA | JUnit 5 + Mockito |
| `PasswordResetCodePersistenceAdapterTest` | Adapter — mapeo JPA | JUnit 5 + Mockito |
| `HexagonalArchitectureTest` | ArchUnit — reglas de dependencias | ArchUnit |

### Tests existentes — migración de packages (no regresión)

Los 14 tests actuales se migran a sus nuevos packages en la story que mueva sus clases objetivo. Deben pasar en verde tras cada story.

### Reglas ArchUnit mínimas (nueva clase `HexagonalArchitectureTest`)

```java
// domain no depende de Spring ni JPA
noClasses().that().resideInAPackage("..domain..")
    .should().dependOnClassesThat()
    .resideInAnyPackage("org.springframework..", "jakarta.persistence..");

// application no depende de infrastructure
noClasses().that().resideInAPackage("..application..")
    .should().dependOnClassesThat()
    .resideInAPackage("..infrastructure..");

// controllers no acceden a JPA repositories
noClasses().that().resideInAPackage("..adapter.in.web..")
    .should().dependOnClassesThat()
    .implement(org.springframework.data.repository.Repository.class);

// API no retorna JPA entities
noClasses().that().resideInAPackage("..adapter.in.web..")
    .should().accessClassesThat()
    .resideInAPackage("..adapter.out.persistence..");
```

---

## Restricciones

- **Sin cambios de comportamiento externo**: mismos endpoints, mismos HTTP status, mismos payloads JSON.
- **Sin cambios de schema**: no se tocan migraciones Flyway V1–V6.
- **Sin cambio de base package**: `cl.gradeops.ai.api` permanece.
- **Sin Maven multi-module**: estructura de packages únicamente.
- **Lombok**: `@RequiredArgsConstructor` en handlers/orchestrators/adapters; `@Builder` en commands/results/requests/responses; `@Data` prohibido en JpaEntity y domain; `@SneakyThrows` prohibido.
- **Firebase Admin SDK**: solo en `auth.infrastructure.adapter.out.firebase.*`.
- **Transacciones**: `@Transactional` solo en handlers (application), nunca en controllers ni orchestrators.
- **ArchUnit**: añadir `com.tngtech.archunit:archunit-junit5` a `pom.xml` (scope `test`).

---

## Approximate Scope

- [x] `src/` (AP) — refactorización completa: nuevos packages, aggregates, ports, handlers, orchestrators, adapters, mappers, ArchUnit tests
- [x] `docs/` (DO) — este planning
- [ ] `web/` — no afectado
- [ ] `agents/` — no afectado
- [ ] `infra/` — no afectado

---

## Initiator

- **Requested by:** human
- **Date:** 2026-06-23
- **Related planning (if continuation):** none

---

## Supersedes

*(none)*

---

## Next Step

- [ ] Cuando esté listo para dimensionar → `/plan-expand 001-hexagonal-refactor`
- [ ] Resolver open questions antes de expansion

### Open Questions

1. **`TeacherIdentity`**: ¿permanece en `auth.domain.model` (identity de Firebase) o se mueve a `teacher.domain.model` (identidad del docente)? Representa el resultado de verificar un token Firebase — no es el `Teacher` del negocio. Recomendación: `auth.domain.model.TeacherIdentity` (permanece en auth).
2. **Rama de trabajo**: ¿el refactor se hace en `develop` directamente o en `refactor/hexagonal-architecture`?
3. **ArchUnit como dependencia**: ¿se agrega en la primera story del refactor o en una story dedicada de calidad al final?

---

> [← planning/README.md](../../README.md)
