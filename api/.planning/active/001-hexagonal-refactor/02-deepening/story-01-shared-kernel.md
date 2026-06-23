# 🔍 DEEPENING: Story 01 — Shared Kernel + ArchUnit Setup

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Create the `shared` feature package with the foundational building blocks that all bounded contexts will extend: `AggregateRoot`, `DomainEvent`, shared domain exceptions, moved infrastructure components (security, config, email, storage). Add ArchUnit to `pom.xml` and create `HexagonalArchitectureTest` with the four mandatory architectural constraints. Delete the superseded legacy packages: `common/`, `config/`, `email/`, `security/`, `port/StoragePort`, `adapter/storage/`.

This story must be completed before any bounded-context story (02–04) begins.

---

## Tasks

> Story is atomized. Task files live under [`story-01-shared-kernel/`](story-01-shared-kernel/). Run `/plan-task 001-hexagonal-refactor story-01 task-01` to start executing.

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 01 | [Add archunit-junit5 dependency](story-01-shared-kernel/task-01-add-archunit-dependency.md) | CONFIGURE | DONE | `pom.xml` updated |
| 02 | [Create shared.domain foundation types](story-01-shared-kernel/task-02-create-shared-domain-types.md) | IMPLEMENT | DONE | `AggregateRoot`, `DomainEvent`, `DomainException` |
| 03 | [Move domain exceptions to shared.domain.exception](story-01-shared-kernel/task-03-move-domain-exceptions.md) | REFACTOR | DONE | `ResourceNotFoundException` + `DuplicateEmailException` relocated; 4 importing files updated; old `common/` exception files deleted |
| 04 | [Move exception-handler web stack](story-01-shared-kernel/task-04-move-exception-handler-web-stack.md) | REFACTOR | DONE | `GlobalExceptionHandler`, `ApiErrorResponse`, `FieldErrorResponse`, `InvalidTokenException` in `shared.infrastructure.adapter.in.web`; `common/` deleted |
| 05 | [Move security classes](story-01-shared-kernel/task-05-move-security-classes.md) | REFACTOR | DONE | 6 classes in `shared.infrastructure.config.security`; `security/` deleted |
| 06 | [Move config classes](story-01-shared-kernel/task-06-move-config-classes.md) | REFACTOR | DONE | 4 classes in `shared.infrastructure.config`; `config/` deleted |
| 07 | [Move email adapter + storage components](story-01-shared-kernel/task-07-move-email-and-storage-adapters.md) | REFACTOR | DONE | `JavaMailEmailService` + storage stack in `shared.infrastructure.adapter.out`; `email/`, `port/StoragePort`, `adapter/storage/` deleted |
| 08 | [Create HexagonalArchitectureTest](story-01-shared-kernel/task-08-create-hexagonal-architecture-test.md) | IMPLEMENT | DONE | `HexagonalArchitectureTest.java` with 4 ArchUnit rules passing |
| 09 | [Verify full test suite](story-01-shared-kernel/task-09-verify-full-test-suite.md) | TEST | DONE | `./mvnw test` exits 0, all tests green |

---

## Done Criteria

- [ ] Package `cl.gradeops.ai.api.shared.domain.model` contains `AggregateRoot<ID>` (abstract class with `pullDomainEvents()`)
- [ ] Package `cl.gradeops.ai.api.shared.domain.event` contains `DomainEvent` interface (`eventId`, `occurredAt`, `eventType`, `aggregateId`)
- [ ] Package `cl.gradeops.ai.api.shared.domain.exception` contains `DomainException`, `ResourceNotFoundException`, `DuplicateEmailException`
- [ ] `HexagonalArchitectureTest` exists in `src/test/java/` with 4 ArchUnit assertions (all green)
- [ ] Packages `common/`, `config/`, `email/`, `security/` are fully deleted
- [ ] `port/StoragePort` and `adapter/storage/` are fully deleted
- [ ] `shared.infrastructure.config.security` contains all 6 security classes
- [ ] `shared.infrastructure.config` contains all 4 config classes
- [ ] `shared.infrastructure.adapter.out.email.JavaMailEmailService` exists
- [ ] `shared.infrastructure.adapter.out.storage` contains `StoragePort`, `GcsStorageAdapter`, `R2StorageAdapter`
- [ ] `./mvnw test` passes with 0 failures
- [ ] TRACEABILITY.md updated with new terms: `AggregateRoot`, `DomainEvent`, `DomainException`, `shared` package, `HexagonalArchitectureTest`

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
