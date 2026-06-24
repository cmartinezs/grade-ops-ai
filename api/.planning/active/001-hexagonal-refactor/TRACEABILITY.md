# 🔗 Traceability: 001-hexagonal-refactor

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

<!-- AREAS-REF: populated by plan-init from the project's configured areas — keep in sync with GUIDE.md -->
| Code | Area |
|------|------|
| AP | `src/` — Java / Spring Boot 4 API |
| DO | `docs/` — documentación |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

| Term / Concept | AP | DO | W | Notes |
|---------------|----|----|---|-------|
| `AggregateRoot` | ✅ | N/A | ✅ | `shared.domain.model.AggregateRoot`; Story 01 |
| `DomainEvent` | ✅ | N/A | ✅ | `shared.domain.model.DomainEvent`; Story 01 |
| `DomainException` | ✅ | N/A | ✅ | `shared.domain.exception.DomainException`; Story 01 |
| `PasswordResetCode` | ✅ | N/A | ✅ | `auth.domain.model.PasswordResetCode`; extends `AggregateRoot`; Story 02 |
| `RawCode` | ✅ | N/A | ✅ | `auth.domain.valueobject.RawCode`; wraps UUID; Story 02 |
| `TeacherIdentity` | ✅ | N/A | ✅ | `auth.domain.model.TeacherIdentity`; Java record; Story 02 |
| `SignInProvider` | ✅ | N/A | ✅ | `auth.domain.model.SignInProvider`; enum `GOOGLE`, `EMAIL_PASSWORD`; Story 02 |
| `AuthPort` | ✅ | N/A | ✅ | `auth.application.port.out.AuthPort`; Story 02 |
| `TeacherRepositoryPort` | ✅ | N/A | ✅ | `auth.application.port.out.TeacherRepositoryPort`; pulled forward from Story 03; Story 02 |
| `PasswordResetCodeRepositoryPort` | ✅ | N/A | ✅ | `auth.application.port.out.PasswordResetCodeRepositoryPort`; Story 02 |
| `EmailNotificationPort` | ✅ | N/A | ✅ | `auth.application.port.out.EmailNotificationPort`; Story 02 |
| `IssuePasswordResetCodeUseCase` | ✅ | N/A | ✅ | `auth.application.port.in`; Story 02 |
| `RegisterUseCase` | ✅ | N/A | ✅ | `auth.application.port.in`; Story 02 |
| `SignOutUseCase` | ✅ | N/A | ✅ | `auth.application.port.in`; Story 02 |
| `RevokeRefreshTokensUseCase` | ✅ | N/A | ✅ | `auth.application.port.in`; Story 02 |
| `SendPasswordResetEmailUseCase` | ✅ | N/A | ✅ | `auth.application.port.in`; Story 02 |
| `ResetPasswordUseCase` | ✅ | N/A | ✅ | `auth.application.port.in`; Story 02 |
| `RegisterHandler` | ✅ | N/A | ✅ | `auth.application.usecase`; `@Transactional`; Story 02 |
| `SignOutHandler` | ✅ | N/A | ✅ | `auth.application.usecase`; `@Transactional`; Story 02 |
| `RevokeRefreshTokensHandler` | ✅ | N/A | ✅ | `auth.application.usecase`; `@Transactional`; Story 02 |
| `IssuePasswordResetCodeHandler` | ✅ | N/A | ✅ | `auth.application.usecase`; `@Transactional`; validates `EMAIL_PASSWORD` provider; Story 02 |
| `SendPasswordResetEmailOrchestrator` | ✅ | N/A | ✅ | `auth.application.orchestrator`; NOT `@Transactional`; Story 02 |
| `ResetPasswordOrchestrator` | ✅ | N/A | ✅ | `auth.application.orchestrator`; IS `@Transactional`; Story 02 |
| `FirebaseAuthAdapter` | ✅ | N/A | ✅ | `auth.infrastructure.adapter.out.firebase`; implements `AuthPort`; Story 02 |
| `TeacherJpaRepositoryAdapter` | ✅ | N/A | ✅ | `auth.infrastructure.adapter.out.persistence`; implements `TeacherRepositoryPort`; Story 02 |
| `PasswordResetCodePersistenceAdapter` | ✅ | N/A | ✅ | `auth.infrastructure.adapter.out.persistence`; implements `PasswordResetCodeRepositoryPort`; Story 02 |
| `ThymeleafEmailNotificationAdapter` | ✅ | N/A | ✅ | `auth.infrastructure.adapter.out.email`; implements `EmailNotificationPort`; Story 02 |
| `AuthController` | ✅ | N/A | ✅ | `auth.infrastructure.adapter.in.web`; consolidates 4 auth endpoints; Story 02 |
| `InvalidResetCodeException` | ✅ | N/A | ✅ | `auth.domain.exception`; → HTTP 422 `INVALID_RESET_CODE`; Story 02 |
| `PasswordMismatchException` | ✅ | N/A | ✅ | `auth.domain.exception`; → HTTP 422 `PASSWORD_MISMATCH`; Story 02 code review |
| `ResetCodeEmailMismatchException` | ✅ | N/A | ✅ | `auth.domain.exception`; → HTTP 422 `RESET_CODE_EMAIL_MISMATCH`; Story 02 code review |
| `HexagonalArchitectureTest` | ✅ | N/A | ✅ | 5 ArchUnit rules; application layer isolated from Spring Web, Spring Data, JPA; Story 01 + Story 02 |
| `AuthConfig` | ✅ | N/A | ✅ | `auth.infrastructure.config`; 11 `@Bean` methods; Story 02 |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D-01 | `TeacherRepositoryPort` pulled forward from Story 03 to Story 02 | Post-review: application layer must not import `domain.teacher.TeacherRepository` (Spring Data interface); Story 03 task-04 is now a no-op | `RegisterHandler`, `SendPasswordResetEmailOrchestrator`, `ResetPasswordOrchestrator`, `AuthConfig` | 2026-06-24 |
| D-02 | `ResetPasswordOrchestrator` declared `@Transactional` despite original "orchestrators are not transactional" guideline | The orchestrator owns the password-update + code-save unit of work; without `@Transactional` a partial failure (update succeeds, save fails) leaves data inconsistent | `ResetPasswordOrchestrator` | 2026-06-24 |
| D-03 | Sign-out test uses `SecurityContextHolder.setContext()` directly instead of `SecurityMockMvcRequestPostProcessors.authentication()` | Spring Boot 4 `@WebMvcTest` has no `MockMvcSecurityAutoConfiguration`; `FilterChainProxy` absent from MockMvc filter chain; session-stored context never loaded | `AuthControllerTest` | 2026-06-24 |
| D-04 | `HttpStatus.UNPROCESSABLE_CONTENT` over `UNPROCESSABLE_ENTITY` | `UNPROCESSABLE_ENTITY` deprecated in Spring Framework 7.0 (Spring Boot 4 baseline) | `GlobalExceptionHandler` | 2026-06-24 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R-01 | Story 03 task-04 (`TeacherRepositoryPort`) is a no-op — already implemented in Story 02 | None | OPEN | Remove or mark SKIP in Story 03 task-04 when Story 03 is planned |
| R-02 | `GlobalExceptionHandler` maps all reset-code errors to `INVALID_RESET_CODE` (422); US-012 DoD specifies `RESET_CODE_NOT_FOUND` (404) / `RESET_CODE_EXPIRED` (410) / `RESET_CODE_USED` (410) — HTTP status and error code mismatch | US-012 backlog alignment | OPEN | Align with product during Story 02 / US-012 acceptance; may require `InvalidResetCodeException` subtypes |

---

> [← planning/README.md](../../README.md)
