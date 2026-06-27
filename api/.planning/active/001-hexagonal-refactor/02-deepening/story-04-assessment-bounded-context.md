# 🔍 DEEPENING: Story 04 — Assessment Bounded Context (stub)

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Create the `assessment` feature package as a structural stub — establishing the correct hexagonal layout for the future assessment domain without implementing business logic. Move `AssessmentStatus` to the domain layer, create `ListAssessmentsUseCase` port and `ListAssessmentsHandler`, introduce `AssessmentRepositoryPort` (stub), and move `AssessmentController` (renaming `AssessmentSummaryDto` → `AssessmentSummaryResponse` and `AssessmentSummaryResult`). Delete the superseded flat `assessment.*` package.

Depends on **Story 01** (shared domain exceptions, AggregateRoot foundation). Can be done in parallel with Stories 02 and 03.

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Move `AssessmentStatus` to `assessment.domain.model`; update references; delete old file](story-04-assessment-bounded-context/task-01-move-assessment-status.md) | REFACTOR | DONE | `AssessmentStatus.java` in `domain/model/`; old file deleted |
| 2 | [Create `AssessmentSummaryResult` record + `ListAssessmentsUseCase` + `AssessmentRepositoryPort` interfaces](story-04-assessment-bounded-context/task-02-create-result-and-ports.md) | IMPLEMENT | DONE | 3 files: result record + 2 port interfaces |
| 3 | [Create `ListAssessmentsHandler` + `ListAssessmentsHandlerTest`](story-04-assessment-bounded-context/task-03-create-list-assessments-handler.md) | IMPLEMENT | DONE | `ListAssessmentsHandler.java` + `ListAssessmentsHandlerTest.java` |
| 4 | [Move `AssessmentController` to new package + `AssessmentSummaryResponse` + update test + delete old flat `assessment.*`](story-04-assessment-bounded-context/task-04-move-controller-and-cleanup.md) | REFACTOR+IMPLEMENT | DONE | Controller + response DTO in new package; updated test; old flat files deleted |
| 5 | [Create `StubAssessmentPersistenceAdapter` + `AssessmentConfig` + run `./mvnw test`](story-04-assessment-bounded-context/task-05-create-config-and-verify.md) | CLEANUP+VERIFY | DONE | Stub adapter; `AssessmentConfig` wires 2 beans; full suite green |

---

## Done Criteria

- [ ] `assessment.domain.model.AssessmentStatus` exists (moved, no Spring/JPA annotations)
- [ ] `ListAssessmentsUseCase` exists in `assessment.application.port.in`
- [ ] `AssessmentRepositoryPort` exists in `assessment.application.port.out` as a stub interface
- [ ] `AssessmentSummaryResult` is a `record` with `@Builder`
- [ ] `ListAssessmentsHandler` is `@RequiredArgsConstructor` (NO `@Service`) `@Transactional(readOnly = true)`, injects `AssessmentRepositoryPort` only; declared as `@Bean` in `AssessmentConfig`
- [ ] `AssessmentController` is in `assessment.infrastructure.adapter.in.web`, injects `ListAssessmentsUseCase` by interface
- [ ] `AssessmentSummaryResponse` replaces the old `AssessmentSummaryDto` in the web response layer
- [ ] Old flat `assessment.*` classes (`AssessmentController`, `AssessmentService`, `AssessmentStatus`, `AssessmentSummaryDto`) fully deleted
- [ ] `./mvnw test` passes with 0 failures; `HexagonalArchitectureTest` still passes
- [ ] TRACEABILITY.md updated: `AssessmentStatus`, `ListAssessmentsUseCase`, `AssessmentRepositoryPort`, `AssessmentSummaryResult`, `AssessmentSummaryResponse`

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
