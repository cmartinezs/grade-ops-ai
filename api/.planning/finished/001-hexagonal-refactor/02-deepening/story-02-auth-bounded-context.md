# 🔍 DEEPENING: Story 02 — Auth Bounded Context

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Create the complete `auth` feature package using hexagonal / feature-package architecture. This includes: a pure `PasswordResetCode` aggregate root (domain logic extracted from `PasswordResetCodeEntity`), `TeacherIdentity` value object (moved from `port/`), all use case ports, four atomic handlers, two orchestrators, and the full infrastructure stack (Firebase adapter, PasswordResetCode persistence stack, Thymeleaf email notification adapter). Move and reorganize `AuthController` with its request/response DTOs. Delete all superseded flat `auth.*` classes and the old `adapter/auth/` and `port/AuthPort` locations.

**Post-implementation code review (session 2026-06-24)** introduced ten additional fixes applied directly to this story's deliverables: `TeacherRepositoryPort` port + `TeacherJpaRepositoryAdapter` (originally planned for Story 03, pulled forward here); domain exceptions `PasswordMismatchException` and `ResetCodeEmailMismatchException`; `@Transactional` on `ResetPasswordOrchestrator`; `pullDomainEvents()` call after code save; `SignInProvider` provider guard on `IssuePasswordResetCodeHandler`; `@Component` removed from all security filters (registered via `@Bean` in `SecurityConfig`); extended ArchUnit application-layer isolation rule; `@WebMvcTest` sign-out test fixed with `SecurityContextHolder` (Spring Boot 4 has no `MockMvcSecurityAutoConfiguration`); test method naming convention; `HttpStatus.UNPROCESSABLE_CONTENT` replacing deprecated `UNPROCESSABLE_ENTITY`.

Depends on **Story 01** (AggregateRoot, DomainEvent, shared exceptions must exist).

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 01 | [Move port types (AuthPort, TeacherIdentity)](story-02-auth-bounded-context/task-01-move-port-types.md) | REFACTOR | DONE | `auth.application.port.out.AuthPort`, `auth.domain.model.TeacherIdentity`; `port/` deleted |
| 02 | [Create PasswordResetCode aggregate + InvalidResetCodeException + test](story-02-auth-bounded-context/task-02-create-domain-model.md) | IMPLEMENT | DONE | `PasswordResetCode.java`, `InvalidResetCodeException.java`, `PasswordResetCodeTest.java` |
| 03 | [Create auth commands and results](story-02-auth-bounded-context/task-03-create-commands-and-results.md) | IMPLEMENT | DONE | 4 command records + 2 result records with `@Builder` |
| 04 | [Create application port interfaces (6 port.in + 2 port.out)](story-02-auth-bounded-context/task-04-create-application-ports.md) | IMPLEMENT | DONE | 6 port.in interfaces + `PasswordResetCodeRepositoryPort` + `EmailNotificationPort` |
| 05 | [Create RegisterHandler + SignOutHandler + RevokeRefreshTokensHandler + tests](story-02-auth-bounded-context/task-05-create-simple-handlers.md) | IMPLEMENT | DONE | 3 handler classes + 3 `*Test.java` files |
| 06 | [Create IssuePasswordResetCodeHandler + test](story-02-auth-bounded-context/task-06-create-issue-code-handler.md) | IMPLEMENT | DONE | `IssuePasswordResetCodeHandler.java` + `IssuePasswordResetCodeHandlerTest.java` |
| 07 | [Create SendPasswordResetEmailOrchestrator + ResetPasswordOrchestrator + tests](story-02-auth-bounded-context/task-07-create-orchestrators.md) | IMPLEMENT | DONE | 2 orchestrator classes + 2 `*Test.java` files |
| 08 | [Move FirebaseAuthAdapter to auth.infrastructure.adapter.out.firebase](story-02-auth-bounded-context/task-08-move-firebase-auth-adapter.md) | REFACTOR | DONE | `FirebaseAuthAdapter` at new path; `adapter/auth/` deleted |
| 09 | [Create PasswordResetCode persistence stack](story-02-auth-bounded-context/task-09-create-persistence-stack.md) | IMPLEMENT | DONE | `PasswordResetCodeJpaEntity`, `PasswordResetCodeJpaRepository`, `PasswordResetCodePersistenceAdapter`, `PasswordResetCodePersistenceMapper` + adapter test |
| 10 | [Create ThymeleafEmailNotificationAdapter](story-02-auth-bounded-context/task-10-create-email-notification-adapter.md) | IMPLEMENT | DONE | `ThymeleafEmailNotificationAdapter.java` + test |
| 11 | [Create AuthController + request/response DTOs + migrate controller tests](story-02-auth-bounded-context/task-11-create-auth-controller.md) | IMPLEMENT | DONE | `AuthController` + 4 DTO records in `auth.infrastructure.adapter.in.web` |
| 12 | [Delete old auth flat-package files and verify full test suite](story-02-auth-bounded-context/task-12-cleanup-and-verify.md) | CLEANUP | DONE | 0 flat-package files remain in `auth/`; `./mvnw test` exits 0 |

---

## Done Criteria

- [x] `auth.domain.model.PasswordResetCode` is a pure Java aggregate (extends `AggregateRoot`, no `@Entity`, has `issue(...)`, `restore(...)`, `markUsed()`, `isExpired()`, `isUsed()`)
- [x] `auth.domain.model.TeacherIdentity` is a value object (Java `record`)
- [x] All 6 use case port interfaces exist in `auth.application.port.in`
- [x] All 4 output port interfaces exist in `auth.application.port.out` (`AuthPort`, `PasswordResetCodeRepositoryPort`, `EmailNotificationPort`, `TeacherRepositoryPort` — pulled forward from Story 03)
- [x] All 5 command records and 2 result records exist with `@Builder` (`IssuePasswordResetCodeCommand` includes `SignInProvider provider` field)
- [x] `RegisterHandler`, `SignOutHandler`, `RevokeRefreshTokensHandler`, `IssuePasswordResetCodeHandler` are `@RequiredArgsConstructor` (no stereotype), `@Transactional` on `execute()`, inject only `port/out` or `port/in` interfaces
- [x] `SendPasswordResetEmailOrchestrator` is `@RequiredArgsConstructor`, not `@Transactional`; `ResetPasswordOrchestrator` is `@RequiredArgsConstructor` and IS `@Transactional` (owns the password-update + code-save unit of work)
- [x] `PasswordResetCodeJpaEntity` has no business logic; only JPA annotations + `@Getter @Setter @NoArgsConstructor(access = PROTECTED)`
- [x] `FirebaseAuthAdapter` implements `AuthPort`; `ThymeleafEmailNotificationAdapter` implements `EmailNotificationPort`; `TeacherJpaRepositoryAdapter` implements `TeacherRepositoryPort`
- [x] `AuthController` injects use case interfaces only (no direct service references)
- [x] Old flat `auth.*` classes and `adapter/auth/`, `port/AuthPort`, `port/TeacherIdentity` deleted
- [x] `PasswordResetCodeTest` covers `isExpired()`, `isUsed()`, `markUsed()` invariants
- [x] Unit tests exist for all 4 handlers and 2 orchestrators
- [x] `./mvnw test` passes with 72/72; `HexagonalArchitectureTest` (5 rules) passes
- [x] `auth.domain.exception` contains `InvalidResetCodeException`, `PasswordMismatchException`, `ResetCodeEmailMismatchException`; `GlobalExceptionHandler` maps them to HTTP 422 (`UNPROCESSABLE_CONTENT`)
- [x] `IssuePasswordResetCodeHandler` rejects non-`EMAIL_PASSWORD` providers with `IllegalArgumentException`
- [x] Security filters (`FirebaseTokenFilter`, `EmailVerifiedFilter`, `InternalAuthFilter`) carry no `@Component`; declared as `@Bean` in `SecurityConfig`
- [x] TRACEABILITY.md updated

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | `ResetPasswordOrchestrator` design said NOT `@Transactional`; post-review audit found it owns the password-update + code-save unit of work and MUST be transactional | task-07 | RESOLVED | Added `@Transactional` on `execute()` |
| 2 | `ResetPasswordOrchestrator` threw `ResponseStatusException` for email mismatch and password mismatch — infrastructure concern leaking into application layer | task-07 | RESOLVED | Created `PasswordMismatchException` + `ResetCodeEmailMismatchException` in `auth.domain.exception`; mapped in `GlobalExceptionHandler` |
| 3 | `pullDomainEvents()` on `PasswordResetCode` was never called after `markUsed()` — events were silently dropped | task-07, task-02 | RESOLVED | Called `code.pullDomainEvents()` in `ResetPasswordOrchestrator` after `codeRepository.save(code)` |
| 4 | `RegisterHandler` and orchestrators injected `domain.teacher.TeacherRepository` (JPA Spring Data interface) — application layer depended on Spring Data infra | task-05, task-07, story-03 | RESOLVED | Created `TeacherRepositoryPort` + `TeacherJpaRepositoryAdapter` in Story 02 (originally planned for Story 03); Story 03 task-04 is now a no-op |
| 5 | ArchUnit rule only covered `..usecase..` packages; orchestrators in `..orchestrator..` were not checked for Spring Web / JPA imports | HexagonalArchitectureTest | RESOLVED | Rule broadened to `..application..` |
| 6 | `IssuePasswordResetCodeHandler` had no guard against non-`EMAIL_PASSWORD` providers — could issue codes for Google accounts | task-06 | RESOLVED | Added `provider` field to `IssuePasswordResetCodeCommand`; handler throws `IllegalArgumentException` for non-`EMAIL_PASSWORD` |
| 7 | `FirebaseTokenFilter`, `EmailVerifiedFilter`, `InternalAuthFilter` carried `@Component` — beans registered twice (inside `FilterChainProxy` AND as standalone servlet filters) | SecurityConfig | RESOLVED | Removed `@Component`; all three declared as `@Bean` in `SecurityConfig` |
| 8 | `AuthControllerTest` used `@SpringBootTest + @AutoConfigureMockMvc`; post-review migrated to `@WebMvcTest` but sign-out test failed — Spring Boot 4 has no `MockMvcSecurityAutoConfiguration`, `FilterChainProxy` is absent from MockMvc | task-11 | RESOLVED | Sign-out test uses `SecurityContextHolder.getContext().setAuthentication(...)` before `mockMvc.perform()`; `spring-security-test` added to pom.xml |
| 9 | `HttpStatus.UNPROCESSABLE_ENTITY` deprecated since Spring Framework 7.0 | GlobalExceptionHandler | RESOLVED | Replaced with `HttpStatus.UNPROCESSABLE_CONTENT` (same 422 code) |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | ~~`SendPasswordResetEmailOrchestrator` imports `GradeOpsEmailProperties` from application layer~~ — resolved: `ttlMinutes` is a plain `int` field; `AuthConfig` resolves the property at wire-up time | — | CLOSED |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
