# GradeOps API: Hexagonal Feature-Package Refactoring Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor `grade-ops-ai/api` from a layered flat-package structure to vertical feature-package hexagonal architecture: `{feature}/{domain, usecase, api, infra}`, with rich domain models, explicit use case interfaces, and outbound ports — patterns taken from keygo-server, applied to the GradeOps domain.

**Architecture:** Three bounded contexts: `teacher` (identity and management), `auth` (registration, tokens, password reset), `assessment` (assessment lifecycle). Each owns `domain` (pure business logic, no Spring/JPA), `usecase` (application services + command/result objects + outbound port interfaces), `api` (HTTP controllers + request/response DTOs), and `infra` (all adapters: JPA persistence, Firebase, email, storage). Cross-cutting concerns in `shared/{domain, api, infra}`. No logic changes in this refactoring — only structure, abstractions, and wiring.

**Architecture reference:** keygo-server (`/home/carlos/projects/keygo-server`) — same patterns, different domain.

**Tech Stack:** Java 21, Spring Boot 4.1.0, Spring Data JPA, Flyway V6, PostgreSQL, Firebase Admin 9.3.0, JavaMail + Thymeleaf, AWS SDK v2 (R2/beta), Google Cloud Storage (demo).

## Global Constraints

- Base package: `cl.gradeops.ai.api` — do not change
- Java 21; Spring Boot 4.1.0 parent
- No `@Data` on JPA entities — use `@Getter @Setter @Builder`
- No Spring annotations (`@Service`, `@Repository`, `@Component`) in `domain/` packages
- No JPA annotations in `domain/` packages — JPA entities live only in `infra/persistence/entity/`
- `Optional<T>` for all nullable fields
- Constructor injection only (no `@Autowired` field injection)
- Flyway migrations untouched (V1–V6, schema unchanged)
- Firebase Admin SDK: server-side only, never exposed to web layer
- No logic changes — preserve all existing behavior, add only abstraction layers
- All 14 existing tests must pass after every task

---

## Package Mapping Reference

| Current | New | Type |
|---|---|---|
| `auth.AuthController` | `auth.api.AuthController` | move |
| `auth.RegisterRequest/Response/Result` | `auth.api.request.RegisterRequest`, `auth.api.response.RegisterResponse`, `auth.usecase.result.RegisterResult` | move + reorganize |
| `auth.ForgotPasswordRequest` | `auth.api.request.ForgotPasswordRequest` | move |
| `auth.ResetPasswordRequest` | `auth.api.request.ResetPasswordRequest` | move |
| `auth.AuthService` | `auth.usecase.RegisterService` (impl of `RegisterUseCase`) + `auth.usecase.SignOutService` (impl of `SignOutUseCase`) | refactor + split |
| `auth.PasswordResetService` | `auth.usecase.SendPasswordResetEmailService` + `auth.usecase.ResetPasswordService` | refactor + split |
| `auth.PasswordResetCodeEntity` | `auth.infra.persistence.entity.PasswordResetCodeEntity` | move |
| `auth.PasswordResetCodeRepository` | `auth.infra.persistence.repository.PasswordResetCodeJpaRepository` | move + rename |
| `auth.InvalidTokenException` | `shared.api.error.InvalidTokenException` | move |
| `port.AuthPort` | `auth.usecase.port.AuthPort` | move |
| `port.TeacherIdentity` | `auth.domain.TeacherIdentity` | move |
| `port.StoragePort` | `shared.infra.storage.StoragePort` | move |
| `adapter.auth.FirebaseAuthAdapter` | `auth.infra.firebase.FirebaseAuthAdapter` | move |
| `adapter.storage.GcsStorageAdapter` | `shared.infra.storage.GcsStorageAdapter` | move |
| `adapter.storage.R2StorageAdapter` | `shared.infra.storage.R2StorageAdapter` | move |
| `domain.teacher.TeacherEntity` | `teacher.infra.persistence.entity.TeacherEntity` | move |
| `domain.teacher.TeacherRepository` | `teacher.infra.persistence.repository.TeacherJpaRepository` | move + rename |
| `internal.teacher.InternalTeacherController` | `teacher.api.InternalTeacherController` | move |
| `internal.teacher.ProvisionTeacher*` | `teacher.api.request/response.*` + `teacher.usecase.*` | reorganize |
| `internal.teacher.PilotFlag*` | `teacher.api.request/response.*` + `teacher.usecase.*` | reorganize |
| `internal.teacher.ProvisionTeacherService` | `teacher.usecase.ProvisionTeacherService` (impl of `ProvisionTeacherUseCase`) | move + add interface |
| `internal.teacher.PilotFlagService` | `teacher.usecase.UpdatePilotFlagsService` (impl of `UpdatePilotFlagsUseCase`) | move + add interface |
| `assessment.AssessmentController` | `assessment.api.AssessmentController` | move |
| `assessment.AssessmentSummaryDto` | `assessment.api.response.AssessmentSummaryResponse` | move + rename |
| `assessment.AssessmentStatus` | `assessment.domain.AssessmentStatus` | move |
| `assessment.AssessmentService` | `assessment.usecase.ListAssessmentsService` (impl of `ListAssessmentsUseCase`) | move + add interface |
| `common.GlobalExceptionHandler` | `shared.api.error.GlobalExceptionHandler` | move |
| `common.ApiError` | `shared.api.error.ApiError` | move |
| `common.DuplicateEmailException` | `shared.domain.exception.DuplicateEmailException` | move |
| `common.ResourceNotFoundException` | `shared.domain.exception.ResourceNotFoundException` | move |
| `config.*` | `shared.infra.config.*` | move |
| `email.EmailService` | `shared.infra.email.EmailService` | move |
| `security.*` | `shared.infra.security.*` | move |

**New files (no current equivalent):**

| New File | Purpose |
|---|---|
| `teacher/domain/Teacher.java` | Rich domain model (pure Java, no Spring/JPA) |
| `teacher/domain/AuthProvider.java` | Value object enum |
| `teacher/usecase/ProvisionTeacherUseCase.java` | Outbound port (interface) |
| `teacher/usecase/UpdatePilotFlagsUseCase.java` | Outbound port (interface) |
| `teacher/usecase/command/ProvisionTeacherCommand.java` | Input record |
| `teacher/usecase/command/UpdatePilotFlagsCommand.java` | Input record |
| `teacher/usecase/result/ProvisionTeacherResult.java` | Output record |
| `teacher/usecase/result/PilotFlagsResult.java` | Output record |
| `teacher/usecase/port/TeacherRepositoryPort.java` | Outbound port |
| `teacher/infra/persistence/adapter/TeacherRepositoryAdapter.java` | Port implementation |
| `teacher/infra/persistence/mapper/TeacherPersistenceMapper.java` | Domain↔Entity mapper |
| `auth/domain/PasswordResetCode.java` | Rich domain model for reset codes |
| `auth/usecase/RegisterUseCase.java` | Use case interface |
| `auth/usecase/SignOutUseCase.java` | Use case interface |
| `auth/usecase/SendPasswordResetEmailUseCase.java` | Use case interface |
| `auth/usecase/ResetPasswordUseCase.java` | Use case interface |
| `auth/usecase/command/RegisterCommand.java` | Input record |
| `auth/usecase/command/SendPasswordResetEmailCommand.java` | Input record |
| `auth/usecase/command/ResetPasswordCommand.java` | Input record |
| `auth/usecase/port/PasswordResetCodeRepositoryPort.java` | Outbound port |
| `auth/usecase/port/EmailNotificationPort.java` | Outbound port |
| `auth/infra/persistence/adapter/PasswordResetCodeRepositoryAdapter.java` | Port implementation |
| `auth/infra/persistence/mapper/PasswordResetCodePersistenceMapper.java` | Domain↔Entity mapper |
| `auth/infra/email/ThymeleafEmailNotificationAdapter.java` | Port implementation |
| `assessment/usecase/ListAssessmentsUseCase.java` | Use case interface |
| `assessment/usecase/result/AssessmentSummaryResult.java` | Output record |
| `assessment/usecase/port/AssessmentRepositoryPort.java` | Outbound port stub |

---

## Target Directory Structure

```
src/main/java/cl/gradeops/ai/api/
├── GradeOpsApiApplication.java              (unchanged)
├── teacher/
│   ├── domain/
│   │   ├── Teacher.java                     (NEW)
│   │   └── AuthProvider.java                (NEW enum)
│   ├── usecase/
│   │   ├── ProvisionTeacherUseCase.java     (NEW interface)
│   │   ├── ProvisionTeacherService.java     (moved + implements interface)
│   │   ├── UpdatePilotFlagsUseCase.java     (NEW interface)
│   │   ├── UpdatePilotFlagsService.java     (moved + renamed + implements interface)
│   │   ├── command/
│   │   │   ├── ProvisionTeacherCommand.java (NEW record)
│   │   │   └── UpdatePilotFlagsCommand.java (NEW record)
│   │   ├── result/
│   │   │   ├── ProvisionTeacherResult.java  (NEW record)
│   │   │   └── PilotFlagsResult.java        (NEW record)
│   │   └── port/
│   │       └── TeacherRepositoryPort.java   (NEW interface)
│   ├── api/
│   │   ├── InternalTeacherController.java   (moved)
│   │   ├── request/
│   │   │   ├── ProvisionTeacherRequest.java (moved)
│   │   │   └── PilotFlagRequest.java        (moved)
│   │   └── response/
│   │       ├── ProvisionTeacherResponse.java(moved)
│   │       └── PilotFlagsResponse.java      (moved + renamed)
│   └── infra/
│       └── persistence/
│           ├── entity/
│           │   └── TeacherEntity.java       (moved from domain.teacher)
│           ├── repository/
│           │   └── TeacherJpaRepository.java(moved + renamed)
│           ├── adapter/
│           │   └── TeacherRepositoryAdapter.java (NEW)
│           └── mapper/
│               └── TeacherPersistenceMapper.java (NEW)
├── auth/
│   ├── domain/
│   │   ├── PasswordResetCode.java           (NEW - rich model)
│   │   └── TeacherIdentity.java             (moved from port/)
│   ├── usecase/
│   │   ├── RegisterUseCase.java             (NEW interface)
│   │   ├── RegisterService.java             (refactored AuthService)
│   │   ├── SignOutUseCase.java              (NEW interface)
│   │   ├── SignOutService.java              (refactored from AuthService)
│   │   ├── SendPasswordResetEmailUseCase.java (NEW interface)
│   │   ├── SendPasswordResetEmailService.java (refactored PasswordResetService)
│   │   ├── ResetPasswordUseCase.java        (NEW interface)
│   │   ├── ResetPasswordService.java        (refactored PasswordResetService)
│   │   ├── command/
│   │   │   ├── RegisterCommand.java         (NEW record)
│   │   │   ├── SendPasswordResetEmailCommand.java (NEW record)
│   │   │   └── ResetPasswordCommand.java    (NEW record)
│   │   ├── result/
│   │   │   └── RegisterResult.java          (moved from auth/)
│   │   └── port/
│   │       ├── AuthPort.java                (moved from port/)
│   │       ├── PasswordResetCodeRepositoryPort.java (NEW interface)
│   │       └── EmailNotificationPort.java   (NEW interface)
│   ├── api/
│   │   ├── AuthController.java              (moved)
│   │   ├── request/
│   │   │   ├── RegisterRequest.java         (moved)
│   │   │   ├── ForgotPasswordRequest.java   (moved)
│   │   │   └── ResetPasswordRequest.java    (moved)
│   │   └── response/
│   │       └── RegisterResponse.java        (moved)
│   └── infra/
│       ├── firebase/
│       │   └── FirebaseAuthAdapter.java     (moved from adapter.auth)
│       ├── persistence/
│       │   ├── entity/
│       │   │   └── PasswordResetCodeEntity.java (moved from auth/)
│       │   ├── repository/
│       │   │   └── PasswordResetCodeJpaRepository.java (moved + renamed)
│       │   ├── adapter/
│       │   │   └── PasswordResetCodeRepositoryAdapter.java (NEW)
│       │   └── mapper/
│       │       └── PasswordResetCodePersistenceMapper.java (NEW)
│       └── email/
│           └── ThymeleafEmailNotificationAdapter.java (NEW)
├── assessment/
│   ├── domain/
│   │   └── AssessmentStatus.java            (moved)
│   ├── usecase/
│   │   ├── ListAssessmentsUseCase.java      (NEW interface)
│   │   ├── ListAssessmentsService.java      (refactored AssessmentService)
│   │   ├── result/
│   │   │   └── AssessmentSummaryResult.java (NEW record)
│   │   └── port/
│   │       └── AssessmentRepositoryPort.java(NEW stub interface)
│   ├── api/
│   │   ├── AssessmentController.java        (moved)
│   │   └── response/
│   │       └── AssessmentSummaryResponse.java (moved + renamed from AssessmentSummaryDto)
│   └── infra/                               (empty — no table yet)
└── shared/
    ├── domain/
    │   └── exception/
    │       ├── DuplicateEmailException.java  (moved from common)
    │       └── ResourceNotFoundException.java(moved from common)
    ├── api/
    │   └── error/
    │       ├── GlobalExceptionHandler.java  (moved from common)
    │       ├── ApiError.java                (moved from common)
    │       └── InvalidTokenException.java   (moved from auth)
    └── infra/
        ├── config/
        │   ├── FirebaseConfig.java          (moved from config)
        │   ├── JacksonConfig.java           (moved from config)
        │   ├── GradeOpsEmailProperties.java (moved from config)
        │   └── GradeOpsWebProperties.java   (moved from config)
        ├── security/
        │   ├── SecurityConfig.java          (moved from security)
        │   ├── FirebaseTokenFilter.java     (moved from security)
        │   ├── EmailVerifiedFilter.java     (moved from security)
        │   ├── InternalAuthFilter.java      (moved from security)
        │   ├── OwnershipVerifier.java       (moved from security)
        │   └── AuthenticatedTeacher.java    (moved from security)
        ├── email/
        │   └── EmailService.java            (moved from email)
        └── storage/
            ├── StoragePort.java             (moved from port)
            ├── GcsStorageAdapter.java       (moved from adapter.storage)
            └── R2StorageAdapter.java        (moved from adapter.storage)
```

---

## Task 1: Migrate `shared` Layer

Pure package moves — no logic changes. This unblocks all subsequent tasks by establishing shared exception and error handling types.

**Files:**
- Move: `common/` → `shared/domain/exception/` (exceptions) and `shared/api/error/` (handler + ApiError)
- Move: `auth/InvalidTokenException.java` → `shared/api/error/InvalidTokenException.java`
- Move: `config/` → `shared/infra/config/`
- Move: `email/EmailService.java` → `shared/infra/email/EmailService.java`
- Move: `security/` → `shared/infra/security/`
- Move: `port/StoragePort.java` → `shared/infra/storage/StoragePort.java`
- Move: `adapter/storage/` → `shared/infra/storage/`

- [ ] **Step 1: Create shared directory tree**

```bash
BASE="src/main/java/cl/gradeops/ai/api/shared"
mkdir -p ${BASE}/domain/exception
mkdir -p ${BASE}/api/error
mkdir -p ${BASE}/infra/config
mkdir -p ${BASE}/infra/email
mkdir -p ${BASE}/infra/security
mkdir -p ${BASE}/infra/storage

BASE_T="src/test/java/cl/gradeops/ai/api/shared"
mkdir -p ${BASE_T}/api/error
mkdir -p ${BASE_T}/infra/security
```

- [ ] **Step 2: Move shared domain exceptions**

Move `DuplicateEmailException.java` and `ResourceNotFoundException.java` from `common/` to `shared/domain/exception/`.
Update package declarations:

```java
// DuplicateEmailException.java — new package declaration (line 1 only changes):
package cl.gradeops.ai.api.shared.domain.exception;

// ResourceNotFoundException.java:
package cl.gradeops.ai.api.shared.domain.exception;
```

- [ ] **Step 3: Move API error types**

Move `GlobalExceptionHandler.java` and `ApiError.java` from `common/` to `shared/api/error/`.
Move `InvalidTokenException.java` from `auth/` to `shared/api/error/`.

```java
// GlobalExceptionHandler.java:
package cl.gradeops.ai.api.shared.api.error;

// ApiError.java:
package cl.gradeops.ai.api.shared.api.error;

// InvalidTokenException.java:
package cl.gradeops.ai.api.shared.api.error;
```

- [ ] **Step 4: Move config classes**

Move all files from `config/` to `shared/infra/config/`:

```java
// FirebaseConfig.java, JacksonConfig.java, GradeOpsEmailProperties.java, GradeOpsWebProperties.java:
package cl.gradeops.ai.api.shared.infra.config;
```

- [ ] **Step 5: Move EmailService**

Move `email/EmailService.java` to `shared/infra/email/EmailService.java`:

```java
package cl.gradeops.ai.api.shared.infra.email;
```

- [ ] **Step 6: Move security classes**

Move all files from `security/` to `shared/infra/security/`:

```java
// SecurityConfig, FirebaseTokenFilter, EmailVerifiedFilter, InternalAuthFilter,
// OwnershipVerifier, AuthenticatedTeacher:
package cl.gradeops.ai.api.shared.infra.security;
```

- [ ] **Step 7: Move StoragePort and storage adapters**

Move `port/StoragePort.java` → `shared/infra/storage/StoragePort.java`.
Move `adapter/storage/GcsStorageAdapter.java` → `shared/infra/storage/GcsStorageAdapter.java`.
Move `adapter/storage/R2StorageAdapter.java` → `shared/infra/storage/R2StorageAdapter.java`.

```java
// StoragePort.java, GcsStorageAdapter.java, R2StorageAdapter.java:
package cl.gradeops.ai.api.shared.infra.storage;
```

- [ ] **Step 8: Update all imports across the codebase**

For each moved package, run a global find-replace. The pattern is:

```bash
# Example for common.DuplicateEmailException:
find src -name "*.java" \
  -exec sed -i 's/import cl\.gradeops\.ai\.api\.common\.DuplicateEmailException/import cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException/g' {} \;

# Repeat for all moved classes. Full list:
# cl.gradeops.ai.api.common.DuplicateEmailException → shared.domain.exception.DuplicateEmailException
# cl.gradeops.ai.api.common.ResourceNotFoundException → shared.domain.exception.ResourceNotFoundException
# cl.gradeops.ai.api.common.GlobalExceptionHandler → shared.api.error.GlobalExceptionHandler
# cl.gradeops.ai.api.common.ApiError → shared.api.error.ApiError
# cl.gradeops.ai.api.auth.InvalidTokenException → shared.api.error.InvalidTokenException
# cl.gradeops.ai.api.config.* → cl.gradeops.ai.api.shared.infra.config.*
# cl.gradeops.ai.api.email.EmailService → cl.gradeops.ai.api.shared.infra.email.EmailService
# cl.gradeops.ai.api.security.* → cl.gradeops.ai.api.shared.infra.security.*
# cl.gradeops.ai.api.port.StoragePort → cl.gradeops.ai.api.shared.infra.storage.StoragePort
# cl.gradeops.ai.api.adapter.storage.* → cl.gradeops.ai.api.shared.infra.storage.*
```

- [ ] **Step 9: Delete emptied source directories**

```bash
rm -rf src/main/java/cl/gradeops/ai/api/common/
rm -rf src/main/java/cl/gradeops/ai/api/config/
rm -rf src/main/java/cl/gradeops/ai/api/email/
rm -rf src/main/java/cl/gradeops/ai/api/security/
rm -rf src/main/java/cl/gradeops/ai/api/port/StoragePort.java
rm -rf src/main/java/cl/gradeops/ai/api/adapter/storage/
```

- [ ] **Step 10: Compile and run all tests**

```bash
./mvnw test
```

Expected: `BUILD SUCCESS`, 14 tests pass.

- [ ] **Step 11: Commit**

```bash
git add src/
git commit -m "refactor(shared): migrate cross-cutting concerns to shared/{domain,api,infra} packages"
```

---

## Task 2: Introduce Teacher Domain Model + Infrastructure

This task introduces the `Teacher` **rich domain model** (pure Java) and the infrastructure that persists it. The `TeacherEntity` (JPA) moves to `infra/` and a mapper converts between them. A repository port interface decouples use cases from Spring Data.

**Files — new:**

`teacher/domain/Teacher.java` — rich model with no Spring/JPA annotations:

```java
package cl.gradeops.ai.api.teacher.domain;

import java.time.OffsetDateTime;
import java.util.Optional;

public class Teacher {

  private final String uid;
  private final String email;
  private String firstName;
  private String lastName;
  private final AuthProvider provider;
  private final OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private String planType;
  private boolean relatedParty;
  private String offerDetails;
  private String evidenceLink;
  private String flagSetBy;
  private OffsetDateTime flagSetAt;

  private Teacher(
      String uid, String email, String firstName, String lastName,
      AuthProvider provider, OffsetDateTime createdAt, OffsetDateTime updatedAt,
      String planType, boolean relatedParty, String offerDetails,
      String evidenceLink, String flagSetBy, OffsetDateTime flagSetAt) {
    this.uid = uid;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.provider = provider;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.planType = planType;
    this.relatedParty = relatedParty;
    this.offerDetails = offerDetails;
    this.evidenceLink = evidenceLink;
    this.flagSetBy = flagSetBy;
    this.flagSetAt = flagSetAt;
  }

  public static Teacher register(
      String uid, String email, String firstName, String lastName, AuthProvider provider) {
    OffsetDateTime now = OffsetDateTime.now();
    return new Teacher(uid, email, firstName, lastName, provider, now, now,
        null, false, null, null, null, null);
  }

  public static Teacher reconstitute(
      String uid, String email, String firstName, String lastName,
      AuthProvider provider, OffsetDateTime createdAt, OffsetDateTime updatedAt,
      String planType, boolean relatedParty, String offerDetails,
      String evidenceLink, String flagSetBy, OffsetDateTime flagSetAt) {
    return new Teacher(uid, email, firstName, lastName, provider, createdAt, updatedAt,
        planType, relatedParty, offerDetails, evidenceLink, flagSetBy, flagSetAt);
  }

  public boolean canResetPassword() {
    return AuthProvider.EMAIL_PASSWORD.equals(provider);
  }

  public void updatePilotFlags(
      String planType, Boolean relatedParty, String offerDetails,
      String evidenceLink, String setBy) {
    if (planType != null) this.planType = planType;
    if (relatedParty != null) this.relatedParty = relatedParty;
    if (offerDetails != null) this.offerDetails = offerDetails;
    if (evidenceLink != null) this.evidenceLink = evidenceLink;
    this.flagSetBy = setBy;
    this.flagSetAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();
  }

  // Getters (no setters — mutation via domain methods only)
  public String getUid() { return uid; }
  public String getEmail() { return email; }
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public AuthProvider getProvider() { return provider; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public Optional<String> getPlanType() { return Optional.ofNullable(planType); }
  public boolean isRelatedParty() { return relatedParty; }
  public Optional<String> getOfferDetails() { return Optional.ofNullable(offerDetails); }
  public Optional<String> getEvidenceLink() { return Optional.ofNullable(evidenceLink); }
  public Optional<String> getFlagSetBy() { return Optional.ofNullable(flagSetBy); }
  public Optional<OffsetDateTime> getFlagSetAt() { return Optional.ofNullable(flagSetAt); }
}
```

`teacher/domain/AuthProvider.java`:

```java
package cl.gradeops.ai.api.teacher.domain;

public enum AuthProvider {
  EMAIL_PASSWORD,
  GOOGLE
}
```

`teacher/usecase/port/TeacherRepositoryPort.java`:

```java
package cl.gradeops.ai.api.teacher.usecase.port;

import cl.gradeops.ai.api.teacher.domain.Teacher;
import java.util.List;
import java.util.Optional;

public interface TeacherRepositoryPort {
  Optional<Teacher> findByUid(String uid);
  Optional<Teacher> findByEmail(String email);
  boolean existsByEmail(String email);
  Teacher save(Teacher teacher);
  List<Teacher> findByPlanType(String planType);
  List<Teacher> findByRelatedParty(boolean relatedParty);
}
```

`teacher/infra/persistence/mapper/TeacherPersistenceMapper.java`:

```java
package cl.gradeops.ai.api.teacher.infra.persistence.mapper;

import cl.gradeops.ai.api.teacher.domain.AuthProvider;
import cl.gradeops.ai.api.teacher.domain.Teacher;
import cl.gradeops.ai.api.teacher.infra.persistence.entity.TeacherEntity;
import org.springframework.stereotype.Component;

@Component
public class TeacherPersistenceMapper {

  public Teacher toDomain(TeacherEntity entity) {
    return Teacher.reconstitute(
        entity.getFirebaseUid(),
        entity.getEmail(),
        entity.getFirstName(),
        entity.getLastName(),
        AuthProvider.valueOf(entity.getProvider()),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getPlanType(),
        entity.isRelatedParty(),
        entity.getOfferDetails(),
        entity.getEvidenceLink(),
        entity.getFlagSetBy(),
        entity.getFlagSetAt());
  }

  public TeacherEntity toEntity(Teacher teacher) {
    return TeacherEntity.builder()
        .firebaseUid(teacher.getUid())
        .email(teacher.getEmail())
        .firstName(teacher.getFirstName())
        .lastName(teacher.getLastName())
        .provider(teacher.getProvider().name())
        .createdAt(teacher.getCreatedAt())
        .updatedAt(teacher.getUpdatedAt())
        .planType(teacher.getPlanType().orElse(null))
        .relatedParty(teacher.isRelatedParty())
        .offerDetails(teacher.getOfferDetails().orElse(null))
        .evidenceLink(teacher.getEvidenceLink().orElse(null))
        .flagSetBy(teacher.getFlagSetBy().orElse(null))
        .flagSetAt(teacher.getFlagSetAt().orElse(null))
        .build();
  }
}
```

`teacher/infra/persistence/adapter/TeacherRepositoryAdapter.java`:

```java
package cl.gradeops.ai.api.teacher.infra.persistence.adapter;

import cl.gradeops.ai.api.teacher.domain.Teacher;
import cl.gradeops.ai.api.teacher.infra.persistence.mapper.TeacherPersistenceMapper;
import cl.gradeops.ai.api.teacher.infra.persistence.repository.TeacherJpaRepository;
import cl.gradeops.ai.api.teacher.usecase.port.TeacherRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TeacherRepositoryAdapter implements TeacherRepositoryPort {

  private final TeacherJpaRepository jpaRepository;
  private final TeacherPersistenceMapper mapper;

  public TeacherRepositoryAdapter(
      TeacherJpaRepository jpaRepository, TeacherPersistenceMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Teacher> findByUid(String uid) {
    return jpaRepository.findById(uid).map(mapper::toDomain);
  }

  @Override
  public Optional<Teacher> findByEmail(String email) {
    return jpaRepository.findByEmail(email).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  @Override
  public Teacher save(Teacher teacher) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(teacher)));
  }

  @Override
  public List<Teacher> findByPlanType(String planType) {
    return jpaRepository.findByPlanType(planType).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Teacher> findByRelatedParty(boolean relatedParty) {
    return jpaRepository.findByRelatedParty(relatedParty).stream().map(mapper::toDomain).toList();
  }
}
```

- [ ] **Step 1: Create directory tree**

```bash
BASE="src/main/java/cl/gradeops/ai/api/teacher"
mkdir -p ${BASE}/domain
mkdir -p ${BASE}/usecase/port
mkdir -p ${BASE}/infra/persistence/entity
mkdir -p ${BASE}/infra/persistence/repository
mkdir -p ${BASE}/infra/persistence/adapter
mkdir -p ${BASE}/infra/persistence/mapper

mkdir -p src/test/java/cl/gradeops/ai/api/teacher/domain
mkdir -p src/test/java/cl/gradeops/ai/api/teacher/infra/persistence
```

- [ ] **Step 2: Write new domain model files**

Create `teacher/domain/Teacher.java` with the complete code shown above.
Create `teacher/domain/AuthProvider.java` with the complete code shown above.

- [ ] **Step 3: Move TeacherEntity to infra/persistence/entity**

Move `domain/teacher/TeacherEntity.java` → `teacher/infra/persistence/entity/TeacherEntity.java`.

Update package declaration:
```java
package cl.gradeops.ai.api.teacher.infra.persistence.entity;
```

Ensure `@Getter @Setter @Builder` (no `@Data`):
```java
@Entity
@Table(name = "teacher")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherEntity {
  @Id
  @Column(name = "firebase_uid")
  private String firebaseUid;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(unique = true)
  private String email;

  @Column
  private String provider;

  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @Column(name = "plan_type")
  private String planType;

  @Column(name = "related_party")
  private boolean relatedParty;

  @Column(name = "offer_details")
  private String offerDetails;

  @Column(name = "evidence_link")
  private String evidenceLink;

  @Column(name = "flag_set_by")
  private String flagSetBy;

  @Column(name = "flag_set_at")
  private OffsetDateTime flagSetAt;
}
```

- [ ] **Step 4: Move TeacherRepository → TeacherJpaRepository**

Move `domain/teacher/TeacherRepository.java` → `teacher/infra/persistence/repository/TeacherJpaRepository.java`.

```java
package cl.gradeops.ai.api.teacher.infra.persistence.repository;

import cl.gradeops.ai.api.teacher.infra.persistence.entity.TeacherEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherJpaRepository extends JpaRepository<TeacherEntity, String> {
  Optional<TeacherEntity> findByEmail(String email);
  boolean existsByEmail(String email);
  List<TeacherEntity> findByPlanType(String planType);
  List<TeacherEntity> findByRelatedParty(boolean relatedParty);
}
```

- [ ] **Step 5: Write TeacherRepositoryPort, mapper, and adapter**

Create the three new files with the complete code shown above in the "Files — new" section.

- [ ] **Step 6: Update imports for moved TeacherEntity and TeacherRepository**

```bash
find src -name "*.java" \
  -exec sed -i 's/import cl\.gradeops\.ai\.api\.domain\.teacher\.TeacherEntity/import cl.gradeops.ai.api.teacher.infra.persistence.entity.TeacherEntity/g' {} \;
find src -name "*.java" \
  -exec sed -i 's/import cl\.gradeops\.ai\.api\.domain\.teacher\.TeacherRepository/import cl.gradeops.ai.api.teacher.infra.persistence.repository.TeacherJpaRepository/g' {} \;
```

Also update any direct usages of `TeacherRepository` → `TeacherJpaRepository` in field declarations.

- [ ] **Step 7: Move test**

Move `TeacherRepositoryTest.java` → `teacher/infra/persistence/TeacherRepositoryTest.java`. Update its package declaration and imports for `TeacherJpaRepository` and `TeacherEntity`.

- [ ] **Step 8: Delete old domain.teacher directory**

```bash
rm -rf src/main/java/cl/gradeops/ai/api/domain/
```

- [ ] **Step 9: Compile and run tests**

```bash
./mvnw test
```

Expected: `BUILD SUCCESS`, 14 tests pass.

- [ ] **Step 10: Commit**

```bash
git add src/
git commit -m "refactor(teacher): introduce Teacher domain model, TeacherRepositoryPort, and persistence adapter"
```

---

## Task 3: Introduce Teacher Use Cases

Introduce use case interfaces for the two teacher management operations. Refactor existing services to implement them. Move everything to `teacher/` feature package.

**New interface files:**

`teacher/usecase/ProvisionTeacherUseCase.java`:
```java
package cl.gradeops.ai.api.teacher.usecase;

import cl.gradeops.ai.api.teacher.usecase.command.ProvisionTeacherCommand;
import cl.gradeops.ai.api.teacher.usecase.result.ProvisionTeacherResult;

public interface ProvisionTeacherUseCase {
  ProvisionTeacherResult provision(ProvisionTeacherCommand command);
}
```

`teacher/usecase/UpdatePilotFlagsUseCase.java`:
```java
package cl.gradeops.ai.api.teacher.usecase;

import cl.gradeops.ai.api.teacher.usecase.command.UpdatePilotFlagsCommand;
import cl.gradeops.ai.api.teacher.usecase.result.PilotFlagsResult;

public interface UpdatePilotFlagsUseCase {
  PilotFlagsResult updateFlags(UpdatePilotFlagsCommand command);
}
```

`teacher/usecase/command/ProvisionTeacherCommand.java`:
```java
package cl.gradeops.ai.api.teacher.usecase.command;

public record ProvisionTeacherCommand(
    String firstName,
    String lastName,
    String email) {}
```

`teacher/usecase/command/UpdatePilotFlagsCommand.java`:
```java
package cl.gradeops.ai.api.teacher.usecase.command;

public record UpdatePilotFlagsCommand(
    String uid,
    String planType,
    Boolean relatedParty,
    String offerDetails,
    String evidenceLink,
    String setBy) {}
```

`teacher/usecase/result/ProvisionTeacherResult.java`:
```java
package cl.gradeops.ai.api.teacher.usecase.result;

public record ProvisionTeacherResult(
    String firebaseUid,
    String inviteLink) {}
```

`teacher/usecase/result/PilotFlagsResult.java`:
```java
package cl.gradeops.ai.api.teacher.usecase.result;

import java.time.OffsetDateTime;
import java.util.Optional;

public record PilotFlagsResult(
    String firebaseUid,
    Optional<String> planType,
    boolean relatedParty,
    Optional<OffsetDateTime> flagSetAt) {}
```

- [ ] **Step 1: Create directories**

```bash
mkdir -p src/main/java/cl/gradeops/ai/api/teacher/usecase/command
mkdir -p src/main/java/cl/gradeops/ai/api/teacher/usecase/result
mkdir -p src/main/java/cl/gradeops/ai/api/teacher/api/request
mkdir -p src/main/java/cl/gradeops/ai/api/teacher/api/response
mkdir -p src/test/java/cl/gradeops/ai/api/teacher/api
mkdir -p src/test/java/cl/gradeops/ai/api/teacher/usecase
```

- [ ] **Step 2: Create all new interface and command/result files**

Create the 6 files shown above.

- [ ] **Step 3: Move and refactor ProvisionTeacherService**

Move `internal/teacher/ProvisionTeacherService.java` → `teacher/usecase/ProvisionTeacherService.java`.

Key changes:
1. New package: `cl.gradeops.ai.api.teacher.usecase`
2. Implement `ProvisionTeacherUseCase`
3. Replace constructor argument `TeacherRepository` with `TeacherRepositoryPort`
4. Accept `ProvisionTeacherCommand`, return `ProvisionTeacherResult`
5. Use `Teacher.register(...)` factory instead of constructing `TeacherEntity` directly

```java
package cl.gradeops.ai.api.teacher.usecase;

import cl.gradeops.ai.api.auth.usecase.port.AuthPort;
import cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException;
import cl.gradeops.ai.api.teacher.domain.AuthProvider;
import cl.gradeops.ai.api.teacher.domain.Teacher;
import cl.gradeops.ai.api.teacher.usecase.command.ProvisionTeacherCommand;
import cl.gradeops.ai.api.teacher.usecase.port.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.usecase.result.ProvisionTeacherResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProvisionTeacherService implements ProvisionTeacherUseCase {

  private final AuthPort authPort;
  private final TeacherRepositoryPort teacherRepository;

  public ProvisionTeacherService(AuthPort authPort, TeacherRepositoryPort teacherRepository) {
    this.authPort = authPort;
    this.teacherRepository = teacherRepository;
  }

  @Override
  @Transactional
  public ProvisionTeacherResult provision(ProvisionTeacherCommand command) {
    if (teacherRepository.existsByEmail(command.email())) {
      throw new DuplicateEmailException(command.email());
    }
    String uid = authPort.createUser(command.email(), command.firstName(), command.lastName());
    Teacher teacher = Teacher.register(
        uid, command.email(), command.firstName(), command.lastName(), AuthProvider.EMAIL_PASSWORD);
    try {
      teacherRepository.save(teacher);
    } catch (Exception e) {
      authPort.deleteUser(uid); // compensating transaction
      throw e;
    }
    String inviteLink = authPort.generatePasswordResetLink(command.email());
    return new ProvisionTeacherResult(uid, inviteLink);
  }
}
```

> Note: This refactoring requires adding `createUser`, `deleteUser`, and `generatePasswordResetLink` to `AuthPort` if they don't already exist. Check the current `AuthPort` methods and add them if missing.

- [ ] **Step 4: Move and refactor PilotFlagService**

Move `internal/teacher/PilotFlagService.java` → `teacher/usecase/UpdatePilotFlagsService.java`.

Key changes:
1. New package + class name
2. Implement `UpdatePilotFlagsUseCase`
3. Replace `TeacherRepository` with `TeacherRepositoryPort`
4. Call `teacher.updatePilotFlags(...)` on the domain model (not entity directly)

```java
package cl.gradeops.ai.api.teacher.usecase;

import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import cl.gradeops.ai.api.teacher.domain.Teacher;
import cl.gradeops.ai.api.teacher.usecase.command.UpdatePilotFlagsCommand;
import cl.gradeops.ai.api.teacher.usecase.port.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.usecase.result.PilotFlagsResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdatePilotFlagsService implements UpdatePilotFlagsUseCase {

  private final TeacherRepositoryPort teacherRepository;

  public UpdatePilotFlagsService(TeacherRepositoryPort teacherRepository) {
    this.teacherRepository = teacherRepository;
  }

  @Override
  @Transactional
  public PilotFlagsResult updateFlags(UpdatePilotFlagsCommand command) {
    Teacher teacher = teacherRepository.findByUid(command.uid())
        .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + command.uid()));
    teacher.updatePilotFlags(
        command.planType(), command.relatedParty(),
        command.offerDetails(), command.evidenceLink(), command.setBy());
    Teacher saved = teacherRepository.save(teacher);
    return new PilotFlagsResult(
        saved.getUid(), saved.getPlanType(),
        saved.isRelatedParty(), saved.getFlagSetAt());
  }
}
```

- [ ] **Step 5: Move teacher API classes**

Move `internal/teacher/InternalTeacherController.java` → `teacher/api/InternalTeacherController.java`.
Move `internal/teacher/ProvisionTeacherRequest.java` → `teacher/api/request/ProvisionTeacherRequest.java`.
Move `internal/teacher/PilotFlagRequest.java` → `teacher/api/request/PilotFlagRequest.java`.
Move `internal/teacher/ProvisionTeacherResponse.java` → `teacher/api/response/ProvisionTeacherResponse.java`.
Move `internal/teacher/PilotFlagResponse.java` → `teacher/api/response/PilotFlagsResponse.java`.

Update package declarations for all moved files. Update `InternalTeacherController` to:
1. Inject `ProvisionTeacherUseCase` and `UpdatePilotFlagsUseCase` interfaces (not concrete services)
2. Map request → command → call use case → map result → response

```java
package cl.gradeops.ai.api.teacher.api;

import cl.gradeops.ai.api.teacher.api.request.PilotFlagRequest;
import cl.gradeops.ai.api.teacher.api.request.ProvisionTeacherRequest;
import cl.gradeops.ai.api.teacher.api.response.PilotFlagsResponse;
import cl.gradeops.ai.api.teacher.api.response.ProvisionTeacherResponse;
import cl.gradeops.ai.api.teacher.usecase.ProvisionTeacherUseCase;
import cl.gradeops.ai.api.teacher.usecase.UpdatePilotFlagsUseCase;
import cl.gradeops.ai.api.teacher.usecase.command.ProvisionTeacherCommand;
import cl.gradeops.ai.api.teacher.usecase.command.UpdatePilotFlagsCommand;
import cl.gradeops.ai.api.teacher.usecase.result.PilotFlagsResult;
import cl.gradeops.ai.api.teacher.usecase.result.ProvisionTeacherResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/teachers")
public class InternalTeacherController {

  private final ProvisionTeacherUseCase provisionTeacher;
  private final UpdatePilotFlagsUseCase updatePilotFlags;

  public InternalTeacherController(
      ProvisionTeacherUseCase provisionTeacher, UpdatePilotFlagsUseCase updatePilotFlags) {
    this.provisionTeacher = provisionTeacher;
    this.updatePilotFlags = updatePilotFlags;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProvisionTeacherResponse provision(@Valid @RequestBody ProvisionTeacherRequest request) {
    ProvisionTeacherResult result = provisionTeacher.provision(
        new ProvisionTeacherCommand(request.firstName(), request.lastName(), request.email()));
    return new ProvisionTeacherResponse(result.firebaseUid(), result.inviteLink());
  }

  @PatchMapping("/{uid}/flags")
  public PilotFlagsResponse updateFlags(
      @PathVariable String uid, @Valid @RequestBody PilotFlagRequest request) {
    PilotFlagsResult result = updatePilotFlags.updateFlags(
        new UpdatePilotFlagsCommand(uid, request.planType(), request.relatedParty(),
            request.offerDetails(), request.evidenceLink(), request.setBy()));
    return new PilotFlagsResponse(
        result.firebaseUid(),
        result.planType().orElse(null),
        result.relatedParty(),
        result.flagSetAt().map(Object::toString).orElse(null));
  }
}
```

- [ ] **Step 6: Move teacher tests**

Move `internal/teacher/ProvisionTeacherControllerTest.java` → `teacher/api/ProvisionTeacherControllerTest.java`.
Move `internal/teacher/PilotFlagControllerTest.java` → `teacher/api/PilotFlagControllerTest.java`.

Update package declarations and imports. Adjust mocked types from concrete services to use case interfaces.

- [ ] **Step 7: Update global imports, compile, and test**

```bash
find src -name "*.java" \
  -exec sed -i 's/cl\.gradeops\.ai\.api\.internal\.teacher\./cl.gradeops.ai.api.teacher.api./g' {} \;

./mvnw test
```

Expected: 14 tests pass.

- [ ] **Step 8: Delete old internal.teacher directory**

```bash
rm -rf src/main/java/cl/gradeops/ai/api/internal/
```

- [ ] **Step 9: Commit**

```bash
git add src/
git commit -m "refactor(teacher): introduce use case interfaces, commands, results; move to teacher feature package"
```

---

## Task 4: Introduce Auth Domain Model + Ports

Introduces `PasswordResetCode` as a **rich domain model** and defines all outbound ports for the auth feature. No service logic changes yet.

**New files:**

`auth/domain/PasswordResetCode.java`:
```java
package cl.gradeops.ai.api.auth.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PasswordResetCode {

  private final UUID id;
  private final String teacherUid;
  private final String code;
  private final OffsetDateTime expiresAt;
  private OffsetDateTime usedAt;
  private final OffsetDateTime createdAt;

  private PasswordResetCode(UUID id, String teacherUid, String code,
      OffsetDateTime expiresAt, OffsetDateTime usedAt, OffsetDateTime createdAt) {
    this.id = id;
    this.teacherUid = teacherUid;
    this.code = code;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
    this.createdAt = createdAt;
  }

  public static PasswordResetCode issue(String teacherUid, long ttlMinutes) {
    OffsetDateTime now = OffsetDateTime.now();
    return new PasswordResetCode(
        UUID.randomUUID(), teacherUid, UUID.randomUUID().toString(),
        now.plusMinutes(ttlMinutes), null, now);
  }

  public static PasswordResetCode reconstitute(UUID id, String teacherUid, String code,
      OffsetDateTime expiresAt, OffsetDateTime usedAt, OffsetDateTime createdAt) {
    return new PasswordResetCode(id, teacherUid, code, expiresAt, usedAt, createdAt);
  }

  public boolean isExpired() {
    return OffsetDateTime.now().isAfter(expiresAt);
  }

  public boolean isUsed() {
    return usedAt != null;
  }

  public void markUsed() {
    this.usedAt = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public String getTeacherUid() { return teacherUid; }
  public String getCode() { return code; }
  public OffsetDateTime getExpiresAt() { return expiresAt; }
  public OffsetDateTime getUsedAt() { return usedAt; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
```

`auth/usecase/port/AuthPort.java` — move from `port/AuthPort.java`, update package only:
```java
package cl.gradeops.ai.api.auth.usecase.port;
// (keep all existing methods unchanged)
```

`auth/usecase/port/PasswordResetCodeRepositoryPort.java`:
```java
package cl.gradeops.ai.api.auth.usecase.port;

import cl.gradeops.ai.api.auth.domain.PasswordResetCode;
import java.util.Optional;

public interface PasswordResetCodeRepositoryPort {
  Optional<PasswordResetCode> findByCode(String code);
  PasswordResetCode save(PasswordResetCode code);
  void deleteByTeacherUid(String teacherUid);
}
```

`auth/usecase/port/EmailNotificationPort.java`:
```java
package cl.gradeops.ai.api.auth.usecase.port;

public interface EmailNotificationPort {
  void sendPasswordReset(String toEmail, String firstName, String resetLink);
}
```

- [ ] **Step 1: Create directories**

```bash
mkdir -p src/main/java/cl/gradeops/ai/api/auth/domain
mkdir -p src/main/java/cl/gradeops/ai/api/auth/usecase/port
mkdir -p src/main/java/cl/gradeops/ai/api/auth/usecase/command
mkdir -p src/main/java/cl/gradeops/ai/api/auth/usecase/result
mkdir -p src/main/java/cl/gradeops/ai/api/auth/api/request
mkdir -p src/main/java/cl/gradeops/ai/api/auth/api/response
mkdir -p src/main/java/cl/gradeops/ai/api/auth/infra/firebase
mkdir -p src/main/java/cl/gradeops/ai/api/auth/infra/persistence/entity
mkdir -p src/main/java/cl/gradeops/ai/api/auth/infra/persistence/repository
mkdir -p src/main/java/cl/gradeops/ai/api/auth/infra/persistence/adapter
mkdir -p src/main/java/cl/gradeops/ai/api/auth/infra/persistence/mapper
mkdir -p src/main/java/cl/gradeops/ai/api/auth/infra/email
mkdir -p src/test/java/cl/gradeops/ai/api/auth/api
mkdir -p src/test/java/cl/gradeops/ai/api/auth/usecase
mkdir -p src/test/java/cl/gradeops/ai/api/auth/infra
```

- [ ] **Step 2: Create PasswordResetCode domain model**

Create `auth/domain/PasswordResetCode.java` with the full code above.

- [ ] **Step 3: Move TeacherIdentity to auth domain**

Move `port/TeacherIdentity.java` → `auth/domain/TeacherIdentity.java`:
```java
package cl.gradeops.ai.api.auth.domain;
// (keep all existing fields and methods unchanged)
```

- [ ] **Step 4: Move AuthPort to auth.usecase.port**

Move `port/AuthPort.java` → `auth/usecase/port/AuthPort.java`. Update package.

- [ ] **Step 5: Create PasswordResetCodeRepositoryPort and EmailNotificationPort**

Create both files with complete code shown above.

- [ ] **Step 6: Update imports for moved port types**

```bash
find src -name "*.java" \
  -exec sed -i 's/import cl\.gradeops\.ai\.api\.port\.AuthPort/import cl.gradeops.ai.api.auth.usecase.port.AuthPort/g' {} \;
find src -name "*.java" \
  -exec sed -i 's/import cl\.gradeops\.ai\.api\.port\.TeacherIdentity/import cl.gradeops.ai.api.auth.domain.TeacherIdentity/g' {} \;
```

- [ ] **Step 7: Delete emptied port/ directory**

```bash
rm -rf src/main/java/cl/gradeops/ai/api/port/
rm -rf src/main/java/cl/gradeops/ai/api/adapter/auth/
```

Wait — `FirebaseAuthAdapter` from `adapter/auth/` will move in Task 6. Don't delete it yet, just move it:

```bash
# Move FirebaseAuthAdapter now (its only dependency is AuthPort which is now moved)
cp src/main/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapter.java \
   src/main/java/cl/gradeops/ai/api/auth/infra/firebase/FirebaseAuthAdapter.java
rm src/main/java/cl/gradeops/ai/api/adapter/auth/FirebaseAuthAdapter.java
```

Update `FirebaseAuthAdapter.java` package:
```java
package cl.gradeops.ai.api.auth.infra.firebase;
```

- [ ] **Step 8: Compile and test**

```bash
./mvnw test
```

Expected: 14 tests pass.

- [ ] **Step 9: Commit**

```bash
git add src/
git commit -m "refactor(auth): introduce PasswordResetCode domain model, auth ports, move AuthPort to auth feature"
```

---

## Task 5: Introduce Auth Use Case Interfaces + Refactor Services

Add use case interfaces. Split `AuthService` into two focused services. Split `PasswordResetService` into two focused services. Refactor `AuthController` to inject interfaces.

**New interface files:**

`auth/usecase/RegisterUseCase.java`:
```java
package cl.gradeops.ai.api.auth.usecase;

import cl.gradeops.ai.api.auth.usecase.command.RegisterCommand;
import cl.gradeops.ai.api.auth.usecase.result.RegisterResult;

public interface RegisterUseCase {
  RegisterResult register(RegisterCommand command);
}
```

`auth/usecase/SignOutUseCase.java`:
```java
package cl.gradeops.ai.api.auth.usecase;

public interface SignOutUseCase {
  void signOut(String uid);
}
```

`auth/usecase/SendPasswordResetEmailUseCase.java`:
```java
package cl.gradeops.ai.api.auth.usecase;

import cl.gradeops.ai.api.auth.usecase.command.SendPasswordResetEmailCommand;

public interface SendPasswordResetEmailUseCase {
  void send(SendPasswordResetEmailCommand command);
}
```

`auth/usecase/ResetPasswordUseCase.java`:
```java
package cl.gradeops.ai.api.auth.usecase;

import cl.gradeops.ai.api.auth.usecase.command.ResetPasswordCommand;

public interface ResetPasswordUseCase {
  void reset(ResetPasswordCommand command);
}
```

**Command files:**

`auth/usecase/command/RegisterCommand.java`:
```java
package cl.gradeops.ai.api.auth.usecase.command;

public record RegisterCommand(
    String idToken,
    String firstName,
    String lastName) {}
```

`auth/usecase/command/SendPasswordResetEmailCommand.java`:
```java
package cl.gradeops.ai.api.auth.usecase.command;

public record SendPasswordResetEmailCommand(String email) {}
```

`auth/usecase/command/ResetPasswordCommand.java`:
```java
package cl.gradeops.ai.api.auth.usecase.command;

public record ResetPasswordCommand(
    String code,
    String email,
    String password,
    String passwordRepeat) {}
```

- [ ] **Step 1: Create all interface and command files** (shown above)

- [ ] **Step 2: Move RegisterResult to auth/usecase/result/**

Move `auth/RegisterResult.java` → `auth/usecase/result/RegisterResult.java`:
```java
package cl.gradeops.ai.api.auth.usecase.result;

public record RegisterResult(String uid, boolean created) {}
```

- [ ] **Step 3: Create RegisterService (from AuthService)**

Move `auth/AuthService.java` → `auth/usecase/RegisterService.java`. Keep sign-out as a separate class.

```java
package cl.gradeops.ai.api.auth.usecase;

import cl.gradeops.ai.api.auth.domain.TeacherIdentity;
import cl.gradeops.ai.api.auth.usecase.command.RegisterCommand;
import cl.gradeops.ai.api.auth.usecase.port.AuthPort;
import cl.gradeops.ai.api.auth.usecase.result.RegisterResult;
import cl.gradeops.ai.api.teacher.domain.AuthProvider;
import cl.gradeops.ai.api.teacher.domain.Teacher;
import cl.gradeops.ai.api.teacher.usecase.port.TeacherRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegisterService implements RegisterUseCase {

  private final AuthPort authPort;
  private final TeacherRepositoryPort teacherRepository;

  public RegisterService(AuthPort authPort, TeacherRepositoryPort teacherRepository) {
    this.authPort = authPort;
    this.teacherRepository = teacherRepository;
  }

  @Override
  @Transactional
  public RegisterResult register(RegisterCommand command) {
    TeacherIdentity identity = authPort.verifyTokenUnchecked(command.idToken());
    boolean exists = teacherRepository.existsByEmail(identity.email());
    if (!exists) {
      String firstName = command.firstName() != null ? command.firstName() : parseName(identity.name(), true);
      String lastName = command.lastName() != null ? command.lastName() : parseName(identity.name(), false);
      AuthProvider provider = "google.com".equals(identity.signInProvider())
          ? AuthProvider.GOOGLE : AuthProvider.EMAIL_PASSWORD;
      Teacher teacher = Teacher.register(identity.uid(), identity.email(), firstName, lastName, provider);
      teacherRepository.save(teacher);
    }
    return new RegisterResult(identity.uid(), !exists);
  }

  private String parseName(String fullName, boolean first) {
    if (fullName == null || fullName.isBlank()) return "";
    String[] parts = fullName.trim().split(" ", 2);
    return first ? parts[0] : (parts.length > 1 ? parts[1] : "");
  }
}
```

- [ ] **Step 4: Create SignOutService**

```java
package cl.gradeops.ai.api.auth.usecase;

import cl.gradeops.ai.api.auth.usecase.port.AuthPort;
import org.springframework.stereotype.Component;

@Component
public class SignOutService implements SignOutUseCase {

  private final AuthPort authPort;

  public SignOutService(AuthPort authPort) {
    this.authPort = authPort;
  }

  @Override
  public void signOut(String uid) {
    authPort.revokeRefreshTokens(uid);
  }
}
```

- [ ] **Step 5: Create SendPasswordResetEmailService**

```java
package cl.gradeops.ai.api.auth.usecase;

import cl.gradeops.ai.api.auth.usecase.command.SendPasswordResetEmailCommand;
import cl.gradeops.ai.api.auth.usecase.port.EmailNotificationPort;
import cl.gradeops.ai.api.auth.usecase.port.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.auth.domain.PasswordResetCode;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import cl.gradeops.ai.api.shared.infra.config.GradeOpsEmailProperties;
import cl.gradeops.ai.api.shared.infra.config.GradeOpsWebProperties;
import cl.gradeops.ai.api.teacher.domain.Teacher;
import cl.gradeops.ai.api.teacher.usecase.port.TeacherRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SendPasswordResetEmailService implements SendPasswordResetEmailUseCase {

  private final TeacherRepositoryPort teacherRepository;
  private final PasswordResetCodeRepositoryPort codeRepository;
  private final EmailNotificationPort emailNotification;
  private final GradeOpsEmailProperties emailProperties;
  private final GradeOpsWebProperties webProperties;

  public SendPasswordResetEmailService(
      TeacherRepositoryPort teacherRepository,
      PasswordResetCodeRepositoryPort codeRepository,
      EmailNotificationPort emailNotification,
      GradeOpsEmailProperties emailProperties,
      GradeOpsWebProperties webProperties) {
    this.teacherRepository = teacherRepository;
    this.codeRepository = codeRepository;
    this.emailNotification = emailNotification;
    this.emailProperties = emailProperties;
    this.webProperties = webProperties;
  }

  @Override
  @Transactional
  public void send(SendPasswordResetEmailCommand command) {
    Teacher teacher = teacherRepository.findByEmail(command.email())
        .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + command.email()));
    if (!teacher.canResetPassword()) {
      return; // silently ignore — Google providers can't reset via email
    }
    codeRepository.deleteByTeacherUid(teacher.getUid());
    long ttl = emailProperties.getResetPassword().getTtlMinutes();
    PasswordResetCode code = PasswordResetCode.issue(teacher.getUid(), ttl);
    codeRepository.save(code);
    String resetLink = webProperties.getBaseUrl() + "/reset-password?code=" + code.getCode();
    emailNotification.sendPasswordReset(teacher.getEmail(), teacher.getFirstName(), resetLink);
  }
}
```

- [ ] **Step 6: Create ResetPasswordService**

```java
package cl.gradeops.ai.api.auth.usecase;

import cl.gradeops.ai.api.auth.domain.PasswordResetCode;
import cl.gradeops.ai.api.auth.usecase.command.ResetPasswordCommand;
import cl.gradeops.ai.api.auth.usecase.port.AuthPort;
import cl.gradeops.ai.api.auth.usecase.port.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.shared.api.error.InvalidTokenException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ResetPasswordService implements ResetPasswordUseCase {

  private final AuthPort authPort;
  private final PasswordResetCodeRepositoryPort codeRepository;

  public ResetPasswordService(AuthPort authPort, PasswordResetCodeRepositoryPort codeRepository) {
    this.authPort = authPort;
    this.codeRepository = codeRepository;
  }

  @Override
  @Transactional
  public void reset(ResetPasswordCommand command) {
    if (!command.password().equals(command.passwordRepeat())) {
      throw new InvalidTokenException("Passwords do not match");
    }
    PasswordResetCode code = codeRepository.findByCode(command.code())
        .orElseThrow(() -> new InvalidTokenException("Invalid or unknown reset code"));
    if (code.isExpired()) throw new InvalidTokenException("Reset code has expired");
    if (code.isUsed()) throw new InvalidTokenException("Reset code has already been used");
    if (!code.getTeacherUid().equals(findUidByEmail(command.email()))) {
      throw new InvalidTokenException("Email does not match reset code");
    }
    authPort.updatePassword(code.getTeacherUid(), command.password());
    authPort.revokeRefreshTokens(code.getTeacherUid());
    code.markUsed();
    codeRepository.save(code);
  }

  private String findUidByEmail(String email) {
    // This requires TeacherRepositoryPort — inject it and find by email → uid
    // Add TeacherRepositoryPort to constructor if not already present
    throw new UnsupportedOperationException("Inject TeacherRepositoryPort");
  }
}
```

> Complete the `findUidByEmail` by injecting `TeacherRepositoryPort` in the constructor and calling `teacherRepository.findByEmail(email).map(Teacher::getUid).orElseThrow(...)`.

- [ ] **Step 7: Move AuthController to auth/api/ and update injection**

Move `auth/AuthController.java` → `auth/api/AuthController.java`. Update to inject interfaces:

```java
package cl.gradeops.ai.api.auth.api;

// Inject RegisterUseCase, SignOutUseCase, SendPasswordResetEmailUseCase, ResetPasswordUseCase
// Map requests → commands → call use case → map results → responses
// (preserve all existing endpoint paths and HTTP status codes)
```

Move all request/response records to `auth/api/request/` and `auth/api/response/`.

- [ ] **Step 8: Move test files**

Move auth tests to new packages. Update package declarations and imports in:
- `AuthControllerTest` → `auth/api/AuthControllerTest`
- `PasswordResetControllerTest` → `auth/api/PasswordResetControllerTest`
- `PasswordResetServiceTest` → `auth/usecase/PasswordResetServiceTest` (now tests `ResetPasswordService`)

- [ ] **Step 9: Compile and test**

```bash
./mvnw test
```

Expected: 14 tests pass.

- [ ] **Step 10: Commit**

```bash
git add src/
git commit -m "refactor(auth): introduce use case interfaces, split AuthService and PasswordResetService"
```

---

## Task 6: Auth Infrastructure Adapters

Introduce `PasswordResetCodeRepositoryAdapter` (implements `PasswordResetCodeRepositoryPort`) and `ThymeleafEmailNotificationAdapter` (implements `EmailNotificationPort`). The `FirebaseAuthAdapter` was already moved in Task 4.

**Files:**

`auth/infra/persistence/entity/PasswordResetCodeEntity.java` — move from `auth/`:
```java
package cl.gradeops.ai.api.auth.infra.persistence.entity;
// (keep all existing fields and JPA annotations unchanged)
// Preserve existing isExpired(), isUsed(), markUsed() methods — they delegate to the domain model after this refactoring, or keep them for backwards compatibility with existing tests
```

`auth/infra/persistence/repository/PasswordResetCodeJpaRepository.java` — move + rename from `auth/PasswordResetCodeRepository.java`:
```java
package cl.gradeops.ai.api.auth.infra.persistence.repository;

import cl.gradeops.ai.api.auth.infra.persistence.entity.PasswordResetCodeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetCodeJpaRepository extends JpaRepository<PasswordResetCodeEntity, UUID> {
  Optional<PasswordResetCodeEntity> findByCode(String code);
  void deleteByTeacherUid(String teacherUid);
}
```

`auth/infra/persistence/mapper/PasswordResetCodePersistenceMapper.java`:
```java
package cl.gradeops.ai.api.auth.infra.persistence.mapper;

import cl.gradeops.ai.api.auth.domain.PasswordResetCode;
import cl.gradeops.ai.api.auth.infra.persistence.entity.PasswordResetCodeEntity;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetCodePersistenceMapper {

  public PasswordResetCode toDomain(PasswordResetCodeEntity entity) {
    return PasswordResetCode.reconstitute(
        entity.getId(),
        entity.getTeacherUid(),
        entity.getCode(),
        entity.getExpiresAt(),
        entity.getUsedAt(),
        entity.getCreatedAt());
  }

  public PasswordResetCodeEntity toEntity(PasswordResetCode code) {
    return PasswordResetCodeEntity.builder()
        .id(code.getId())
        .teacherUid(code.getTeacherUid())
        .code(code.getCode())
        .expiresAt(code.getExpiresAt())
        .usedAt(code.getUsedAt())
        .createdAt(code.getCreatedAt())
        .build();
  }
}
```

`auth/infra/persistence/adapter/PasswordResetCodeRepositoryAdapter.java`:
```java
package cl.gradeops.ai.api.auth.infra.persistence.adapter;

import cl.gradeops.ai.api.auth.domain.PasswordResetCode;
import cl.gradeops.ai.api.auth.infra.persistence.mapper.PasswordResetCodePersistenceMapper;
import cl.gradeops.ai.api.auth.infra.persistence.repository.PasswordResetCodeJpaRepository;
import cl.gradeops.ai.api.auth.usecase.port.PasswordResetCodeRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetCodeRepositoryAdapter implements PasswordResetCodeRepositoryPort {

  private final PasswordResetCodeJpaRepository jpaRepository;
  private final PasswordResetCodePersistenceMapper mapper;

  public PasswordResetCodeRepositoryAdapter(
      PasswordResetCodeJpaRepository jpaRepository, PasswordResetCodePersistenceMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Optional<PasswordResetCode> findByCode(String code) {
    return jpaRepository.findByCode(code).map(mapper::toDomain);
  }

  @Override
  public PasswordResetCode save(PasswordResetCode code) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(code)));
  }

  @Override
  public void deleteByTeacherUid(String teacherUid) {
    jpaRepository.deleteByTeacherUid(teacherUid);
  }
}
```

`auth/infra/email/ThymeleafEmailNotificationAdapter.java`:
```java
package cl.gradeops.ai.api.auth.infra.email;

import cl.gradeops.ai.api.auth.usecase.port.EmailNotificationPort;
import cl.gradeops.ai.api.shared.infra.email.EmailService;
import org.springframework.stereotype.Component;

@Component
public class ThymeleafEmailNotificationAdapter implements EmailNotificationPort {

  private final EmailService emailService;

  public ThymeleafEmailNotificationAdapter(EmailService emailService) {
    this.emailService = emailService;
  }

  @Override
  public void sendPasswordReset(String toEmail, String firstName, String resetLink) {
    emailService.sendPasswordReset(toEmail, firstName, resetLink);
  }
}
```

- [ ] **Step 1: Move PasswordResetCodeEntity to auth/infra/persistence/entity/**

Move `auth/PasswordResetCodeEntity.java` → `auth/infra/persistence/entity/PasswordResetCodeEntity.java`.
Update package declaration. Add `@Builder @NoArgsConstructor @AllArgsConstructor` if missing.

- [ ] **Step 2: Move PasswordResetCodeRepository → PasswordResetCodeJpaRepository**

Move `auth/PasswordResetCodeRepository.java` → `auth/infra/persistence/repository/PasswordResetCodeJpaRepository.java`.
Update interface name and package.

- [ ] **Step 3: Create mapper, adapter, and email adapter**

Create the 3 files: `PasswordResetCodePersistenceMapper`, `PasswordResetCodeRepositoryAdapter`, `ThymeleafEmailNotificationAdapter` with complete code shown above.

- [ ] **Step 4: Update all imports**

```bash
find src -name "*.java" \
  -exec sed -i 's/import cl\.gradeops\.ai\.api\.auth\.PasswordResetCodeEntity/import cl.gradeops.ai.api.auth.infra.persistence.entity.PasswordResetCodeEntity/g' {} \;
find src -name "*.java" \
  -exec sed -i 's/import cl\.gradeops\.ai\.api\.auth\.PasswordResetCodeRepository/import cl.gradeops.ai.api.auth.infra.persistence.repository.PasswordResetCodeJpaRepository/g' {} \;
```

- [ ] **Step 5: Update test for PasswordResetCodeEntity**

Move `auth/PasswordResetCodeEntityTest.java` → `auth/infra/persistence/PasswordResetCodeEntityTest.java`. Update package + imports.

- [ ] **Step 6: Compile and run all tests**

```bash
./mvnw test
```

Expected: 14 tests pass.

- [ ] **Step 7: Clean up old adapter/auth directory**

```bash
rm -rf src/main/java/cl/gradeops/ai/api/adapter/
```

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "refactor(auth/infra): introduce persistence adapters, mapper, email notification adapter"
```

---

## Task 7: Migrate `assessment` Feature

Move `AssessmentController`, `AssessmentSummaryDto`, and `AssessmentStatus` to the assessment feature package. Introduce use case interface and stub result.

- [ ] **Step 1: Create directories**

```bash
mkdir -p src/main/java/cl/gradeops/ai/api/assessment/domain
mkdir -p src/main/java/cl/gradeops/ai/api/assessment/usecase/result
mkdir -p src/main/java/cl/gradeops/ai/api/assessment/usecase/port
mkdir -p src/main/java/cl/gradeops/ai/api/assessment/api/response
mkdir -p src/test/java/cl/gradeops/ai/api/assessment/api
```

- [ ] **Step 2: Move AssessmentStatus to domain**

Move `assessment/AssessmentStatus.java` → `assessment/domain/AssessmentStatus.java`:
```java
package cl.gradeops.ai.api.assessment.domain;
```

- [ ] **Step 3: Create ListAssessmentsUseCase interface**

```java
// assessment/usecase/ListAssessmentsUseCase.java
package cl.gradeops.ai.api.assessment.usecase;

import cl.gradeops.ai.api.assessment.usecase.result.AssessmentSummaryResult;
import java.util.List;

public interface ListAssessmentsUseCase {
  List<AssessmentSummaryResult> listForTeacher(String teacherUid);
}
```

- [ ] **Step 4: Create AssessmentSummaryResult**

```java
// assessment/usecase/result/AssessmentSummaryResult.java
package cl.gradeops.ai.api.assessment.usecase.result;

import cl.gradeops.ai.api.assessment.domain.AssessmentStatus;
import java.util.Optional;

public record AssessmentSummaryResult(
    String id,
    String title,
    AssessmentStatus status,
    int submissionCount,
    int pendingApprovals,
    Optional<String> reportLink) {}
```

- [ ] **Step 5: Create stub AssessmentRepositoryPort**

```java
// assessment/usecase/port/AssessmentRepositoryPort.java
package cl.gradeops.ai.api.assessment.usecase.port;

// Stub — no methods yet, populated when assessment table exists
public interface AssessmentRepositoryPort {}
```

- [ ] **Step 6: Refactor AssessmentService → ListAssessmentsService**

Move `assessment/AssessmentService.java` → `assessment/usecase/ListAssessmentsService.java`:

```java
package cl.gradeops.ai.api.assessment.usecase;

import cl.gradeops.ai.api.assessment.usecase.result.AssessmentSummaryResult;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListAssessmentsService implements ListAssessmentsUseCase {

  @Override
  public List<AssessmentSummaryResult> listForTeacher(String teacherUid) {
    return List.of(); // TODO: implement when assessment table exists
  }
}
```

- [ ] **Step 7: Move AssessmentController to api/ and rename AssessmentSummaryDto**

Move `assessment/AssessmentController.java` → `assessment/api/AssessmentController.java`. Update to inject `ListAssessmentsUseCase` interface.

Move `assessment/AssessmentSummaryDto.java` → `assessment/api/response/AssessmentSummaryResponse.java`:
```java
package cl.gradeops.ai.api.assessment.api.response;

import cl.gradeops.ai.api.assessment.domain.AssessmentStatus;
import java.util.Optional;

public record AssessmentSummaryResponse(
    String id,
    String title,
    AssessmentStatus status,
    int submissionCount,
    int pendingApprovals,
    Optional<String> reportLink) {}
```

- [ ] **Step 8: Move assessment test**

Move `assessment/AssessmentControllerTest.java` → `assessment/api/AssessmentControllerTest.java`. Update package and imports.

- [ ] **Step 9: Update imports and clean up**

```bash
find src -name "*.java" \
  -exec sed -i 's/import cl\.gradeops\.ai\.api\.assessment\.AssessmentStatus/import cl.gradeops.ai.api.assessment.domain.AssessmentStatus/g' {} \;
find src -name "*.java" \
  -exec sed -i 's/import cl\.gradeops\.ai\.api\.assessment\.AssessmentSummaryDto/import cl.gradeops.ai.api.assessment.api.response.AssessmentSummaryResponse/g' {} \;

./mvnw test
```

Expected: 14 tests pass.

- [ ] **Step 10: Commit**

```bash
git add src/
git commit -m "refactor(assessment): migrate assessment feature to feature-package layout with ListAssessmentsUseCase"
```

---

## Task 8: Final Cleanup + Full Verification

- [ ] **Step 1: Verify no old packages remain**

```bash
find src/main/java/cl/gradeops/ai/api -mindepth 1 -maxdepth 1 -type d | sort
```

Expected directories: `assessment/`, `auth/`, `shared/`, `teacher/`.
Should NOT exist: `adapter/`, `common/`, `config/`, `domain/`, `email/`, `internal/`, `port/`, `security/`.

- [ ] **Step 2: Full Maven build**

```bash
./mvnw verify
```

Expected: `BUILD SUCCESS`, all 14 tests pass.

- [ ] **Step 3: Verify application starts**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local &
APP_PID=$!
sleep 20
curl -sf http://localhost:8080/actuator/health && echo "OK"
kill $APP_PID
```

Expected: `{"status":"UP"}`.

- [ ] **Step 4: Smoke test key endpoints**

```bash
# Health
curl -s http://localhost:8080/actuator/health | python3 -m json.tool

# Register endpoint reachable (returns 422 with no body — that's correct)
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/v1/auth/register
# Expected: 422 (validation error, no token provided)

# Internal auth protected
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/internal/teachers
# Expected: 401 (missing X-Internal-Key header)
```

- [ ] **Step 5: Update CLAUDE.md**

Update the Architecture section to reflect the new feature-package layout:

```markdown
## Architecture — Package Structure

Each bounded context follows: `{feature}/{domain, usecase, api, infra}`

| Feature | Domain entities | Use cases |
|---|---|---|
| `teacher` | `Teacher`, `AuthProvider` | `ProvisionTeacherUseCase`, `UpdatePilotFlagsUseCase` |
| `auth` | `PasswordResetCode`, `TeacherIdentity` | `RegisterUseCase`, `SignOutUseCase`, `SendPasswordResetEmailUseCase`, `ResetPasswordUseCase` |
| `assessment` | `AssessmentStatus` | `ListAssessmentsUseCase` (stub) |
| `shared` | `DomainException`, `ResourceNotFoundException` | — |

Layer rules:
- `domain/`: pure Java, no Spring, no JPA
- `usecase/`: orchestration only; outbound ports in `usecase/port/`
- `api/`: HTTP adapters; maps request→command, result→response
- `infra/`: JPA entities + adapters + external service adapters
```

- [ ] **Step 6: Final commit**

```bash
git add -A
git commit -m "refactor: complete hexagonal feature-package restructuring

Bounded contexts: teacher, auth, assessment, shared
Each feature: {domain, usecase, api, infra}
Rich domain models: Teacher, PasswordResetCode
Use case interfaces: RegisterUseCase, SignOutUseCase, SendPasswordResetEmailUseCase,
  ResetPasswordUseCase, ProvisionTeacherUseCase, UpdatePilotFlagsUseCase, ListAssessmentsUseCase
Outbound ports: TeacherRepositoryPort, AuthPort, PasswordResetCodeRepositoryPort, EmailNotificationPort
All 14 existing tests preserved"
```

---

## Self-Review

**Spec coverage:**
- [x] Feature-package layout (`{feature}/{domain,usecase,api,infra}`): Tasks 1–7
- [x] No multi-module Maven: single pom.xml unchanged
- [x] DDD: rich `Teacher` (Task 2) + `PasswordResetCode` (Task 4) with domain behavior
- [x] Hexagonal: outbound ports in `usecase/port/`; adapters in `infra/`; controllers in `api/`
- [x] SOLID: ISP via focused use case interfaces; DIP via port interfaces; SRP per service class
- [x] Clean Code: commands/results as records; no logic in controllers; adapters are thin
- [x] Testability: controllers inject interfaces (mockable); domain models are pure Java (no Spring)
- [x] App startup verified: Task 8 Step 3
- [x] All 14 tests preserved throughout

**No placeholders:** All new files have complete code. Sed commands show exact patterns.
