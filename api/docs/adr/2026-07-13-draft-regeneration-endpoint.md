# ADR: Draft regeneration endpoint — shared coordinator extraction

**Date:** 2026-07-13
**Status:** Accepted
**Planning:** 003-assessment-creation / story-01-assessment-creation-persistence / task-08-draft-regeneration-endpoint

## Context

Task-08 regenerates a draft via near-identical logic to task-07's generation endpoint: call `agents/` outside a DB transaction, then persist a new `AgentExecutionLog` + `AssessmentDraft` inside one. Task-07's own design flagged this ordering as risky (Risk: M), already mitigated once there with a programmatic `TransactionTemplate`. Duplicating that same ordering-sensitive logic into a second handler would double the risk surface instead of reusing the already-verified implementation.

## Decision

Extracted the shared logic into a new `DraftGenerationCoordinator` (`assessment/application/usecase`), exposing one method, `callAgentAndPersist(assessmentId, agentCommand, draftFactory)`, where `draftFactory` is a `BiFunction<AssessmentAgentResponse.Result, UUID, AssessmentDraft>` supplied by the caller. `GenerateAssessmentDraftHandler` passes `AssessmentDraft.generate(...)`; `RegenerateAssessmentDraftHandler` passes `AssessmentDraft.regenerate(...)`. The coordinator owns `AssessmentAgentClient`/`AgentExecutionLogRepositoryPort`/`AssessmentDraftRepositoryPort`/`TransactionTemplate`; the two handlers keep only their own load/validate responsibilities (ownership check, brief loading, current-draft loading).

The coordinator class had to be made `public` (not package-private, despite being an internal implementation detail) because `AssessmentConfig` constructs it via `@Bean` across a package boundary — this codebase wires all its use-case handlers and adapters the same way (manual `@Bean` factory methods, not `@Component` scanning), so there was no way to keep it package-private without breaking that established convention.

`previousDraft` content is rendered via a private `toPromptSummary()` method on `RegenerateAssessmentDraftHandler` itself, not a domain method on `AssessmentDraft` — keeps the pure domain model free of prompt-formatting concerns. The "no prior draft" case is a new `NoPriorDraftException extends ApplicationException`, reusing the existing generic `422` handler rather than adding new `GlobalExceptionHandler` code.

## Consequences

This required a breaking change to the already-merged, already-reviewed `GenerateAssessmentDraftHandler` constructor. Its test coverage was split: persistence/transaction-order/failure-log assertions moved to a new `DraftGenerationCoordinatorTest`; `GenerateAssessmentDraftHandlerTest` was narrowed to only its own remaining responsibilities. `GenerateAssessmentDraftHandlerIntegrationTest` was updated to construct the coordinator explicitly. Any future draft-producing endpoint (e.g. a hypothetical third variant) can reuse the same coordinator by supplying its own `draftFactory`, without re-deriving the transaction-boundary logic.

## Alternatives Considered

Duplicating the "call agent, persist log+draft" logic directly inside `RegenerateAssessmentDraftHandler` (copy-paste from task-07) was rejected — task-07's own design explicitly called for extraction instead (its Risk callout exists precisely because this logic is easy to get backwards), and duplicating it would let the two implementations silently drift.

An `@Transactional`-annotated sibling method on each handler (instead of `TransactionTemplate`) was not reconsidered here since task-07 already ruled it out (Spring AOP self-invocation) — this task's extraction preserves that decision rather than revisiting it.
