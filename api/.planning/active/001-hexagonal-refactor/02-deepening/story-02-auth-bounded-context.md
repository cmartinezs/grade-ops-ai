# 🔍 DEEPENING: Story 02 — Auth Bounded Context

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Create the complete `auth` feature package using hexagonal / feature-package architecture. This includes: a pure `PasswordResetCode` aggregate root (domain logic extracted from `PasswordResetCodeEntity`), `TeacherIdentity` value object (moved from `port/`), all use case ports, four atomic handlers, two orchestrators, and the full infrastructure stack (Firebase adapter, PasswordResetCode persistence stack, Thymeleaf email notification adapter). Move and reorganize `AuthController` with its request/response DTOs. Delete all superseded flat `auth.*` classes and the old `adapter/auth/` and `port/AuthPort` locations.

Depends on **Story 01** (AggregateRoot, DomainEvent, shared exceptions must exist).

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 01 | [Move port types (AuthPort, TeacherIdentity)](story-02-auth-bounded-context/task-01-move-port-types.md) | REFACTOR | TODO | `auth.application.port.out.AuthPort`, `auth.domain.model.TeacherIdentity`; `port/` deleted |
| 02 | [Create PasswordResetCode aggregate + InvalidResetCodeException + test](story-02-auth-bounded-context/task-02-create-domain-model.md) | IMPLEMENT | TODO | `PasswordResetCode.java`, `InvalidResetCodeException.java`, `PasswordResetCodeTest.java` |
| 03 | [Create auth commands and results](story-02-auth-bounded-context/task-03-create-commands-and-results.md) | IMPLEMENT | TODO | 4 command records + 2 result records with `@Builder` |
| 04 | [Create application port interfaces (6 port.in + 2 port.out)](story-02-auth-bounded-context/task-04-create-application-ports.md) | IMPLEMENT | TODO | 6 port.in interfaces + `PasswordResetCodeRepositoryPort` + `EmailNotificationPort` |
| 05 | [Create RegisterHandler + SignOutHandler + RevokeRefreshTokensHandler + tests](story-02-auth-bounded-context/task-05-create-simple-handlers.md) | IMPLEMENT | TODO | 3 handler classes + 3 `*Test.java` files |
| 06 | [Create IssuePasswordResetCodeHandler + test](story-02-auth-bounded-context/task-06-create-issue-code-handler.md) | IMPLEMENT | TODO | `IssuePasswordResetCodeHandler.java` + `IssuePasswordResetCodeHandlerTest.java` |
| 07 | [Create SendPasswordResetEmailOrchestrator + ResetPasswordOrchestrator + tests](story-02-auth-bounded-context/task-07-create-orchestrators.md) | IMPLEMENT | TODO | 2 orchestrator classes + 2 `*Test.java` files |
| 08 | [Move FirebaseAuthAdapter to auth.infrastructure.adapter.out.firebase](story-02-auth-bounded-context/task-08-move-firebase-auth-adapter.md) | REFACTOR | TODO | `FirebaseAuthAdapter` at new path; `adapter/auth/` deleted |
| 09 | [Create PasswordResetCode persistence stack](story-02-auth-bounded-context/task-09-create-persistence-stack.md) | IMPLEMENT | TODO | `PasswordResetCodeJpaEntity`, `PasswordResetCodeJpaRepository`, `PasswordResetCodePersistenceAdapter`, `PasswordResetCodePersistenceMapper` + adapter test |
| 10 | [Create ThymeleafEmailNotificationAdapter](story-02-auth-bounded-context/task-10-create-email-notification-adapter.md) | IMPLEMENT | TODO | `ThymeleafEmailNotificationAdapter.java` + test |
| 11 | [Create AuthController + request/response DTOs + migrate controller tests](story-02-auth-bounded-context/task-11-create-auth-controller.md) | IMPLEMENT | TODO | `AuthController` + 4 DTO records in `auth.infrastructure.adapter.in.web` |
| 12 | [Delete old auth flat-package files and verify full test suite](story-02-auth-bounded-context/task-12-cleanup-and-verify.md) | CLEANUP | TODO | 0 flat-package files remain in `auth/`; `./mvnw test` exits 0 |

---

## Done Criteria

- [ ] `auth.domain.model.PasswordResetCode` is a pure Java aggregate (extends `AggregateRoot`, no `@Entity`, has `issue(...)`, `restore(...)`, `markUsed()`, `isExpired()`, `isUsed()`)
- [ ] `auth.domain.model.TeacherIdentity` is a value object (Java `record` or `final class`)
- [ ] All 6 use case port interfaces exist in `auth.application.port.in`
- [ ] All 3 output port interfaces exist in `auth.application.port.out`
- [ ] All 4 command records and 2 result records exist with `@Builder`
- [ ] `RegisterHandler`, `SignOutHandler`, `RevokeRefreshTokensHandler`, `IssuePasswordResetCodeHandler` use `@Service @RequiredArgsConstructor`, are `@Transactional`, and inject only `port/out` interfaces or other `port/in` interfaces
- [ ] `SendPasswordResetEmailOrchestrator` and `ResetPasswordOrchestrator` use `@Component @RequiredArgsConstructor`, are **not** `@Transactional`
- [ ] `PasswordResetCodeJpaEntity` has no business logic (`isExpired()`, `isUsed()` removed); only JPA annotations + `@Getter @Setter @NoArgsConstructor(access = PROTECTED)`
- [ ] `FirebaseAuthAdapter` implements `AuthPort`; `ThymeleafEmailNotificationAdapter` implements `EmailNotificationPort`
- [ ] `AuthController` injects use case interfaces only (no direct service references)
- [ ] Old flat `auth.*` classes and `adapter/auth/`, `port/AuthPort`, `port/TeacherIdentity` deleted
- [ ] `PasswordResetCodeTest` covers `isExpired()`, `isUsed()`, `markUsed()` invariants
- [ ] Unit tests exist for all 4 handlers and 2 orchestrators
- [ ] `./mvnw test` passes with 0 failures; `HexagonalArchitectureTest` passes
- [ ] TRACEABILITY.md updated: `PasswordResetCode`, `TeacherIdentity`, `AuthPort`, `EmailNotificationPort`, `IssuePasswordResetCodeUseCase`, handlers, orchestrators

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | ~~`SendPasswordResetEmailOrchestrator` imports `GradeOpsEmailProperties` from application layer~~ — resolved: `ttlMinutes` is a plain `int` field; `AuthConfig` resolves the property at wire-up time | — | CLOSED |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
