# ADR: Draft generation endpoint — transaction boundary, ownership check, cost_estimate type

**Date:** 2026-07-13
**Status:** Accepted
**Planning:** 003-assessment-creation / story-01-assessment-creation-persistence / task-07-draft-generation-endpoint

## Context

This endpoint calls `agents/` (a slow external HTTP call) and then persists two rows (`AgentExecutionLog`, `AssessmentDraft`), cross-referenced. The task's own design flagged the ordering as risky: getting it backwards either holds a DB connection open for the duration of the agent call, or persists evidence for a call that hasn't actually completed. Separately, this is the first endpoint in `api/` that acts on a specific resource by path `{id}`, and the DB/ORM consistency smoke test surfaced a real schema mismatch.

## Decision

**Transaction boundary:** "one `@Transactional` method for the persist step" cannot literally be a second method on `GenerateAssessmentDraftHandler` called via `this.` from `execute()` — Spring's proxy-based AOP does not intercept self-invocation, so a `@Transactional`-annotated sibling method called that way would silently run with no transaction at all. Implemented instead with a programmatic `TransactionTemplate` (backed by the auto-configured `PlatformTransactionManager`): the agent call runs with zero transaction-manager involvement, and the persist step opens a real transaction via `TransactionTemplate.execute(...)`/`executeWithoutResult(...)`, regardless of self-invocation.

**Log/draft cross-reference ordering:** save the `AgentExecutionLog` first (`draftId = null`), save the `AssessmentDraft` referencing the log's id, then re-save the log with `draftId` back-filled. Chosen over saving the draft first, because `AssessmentDraftRepositoryPort.save` is insert-only (task-03) while `AgentExecutionLogRepositoryPort.save` is a natural JPA upsert-by-id.

**Ownership verification:** this is the first endpoint operating on a specific resource by path `{id}` — without an ownership check, any authenticated teacher could trigger a billed draft generation on another teacher's assessment. Wired up a scaffolded-but-previously-unused `OwnershipVerifier` bean, moved from `shared/infrastructure/config/security/` to `shared/application/security/` (it's a pure POJO with no Spring/JPA dependency) since the application-layer handler that needs it cannot import from `..infrastructure..` per `HexagonalArchitectureTest`. A mismatch returns `404`, not `403`, to avoid leaking whether the resource exists.

**`cost_estimate` column type:** declared `DOUBLE PRECISION` in `V12`, not `NUMERIC`. Hibernate maps `Double` to `float(53)` by default; the original `NUMERIC` declaration passed every automated test (unit tests mock the repository; sibling `@DataJpaTest`s all run with `ddl-auto=none`) but failed a real `spring-boot:run` startup with `ddl-auto=validate`.

## Consequences

`AgentClientException` failures map to differentiated HTTP status codes (`503`/`422`/`502` for `UNREACHABLE`/`AGENT_REJECTED`/`AGENT_ERROR`) via a new handler in the shared `GlobalExceptionHandler`, rather than a generic 500. `AgentExecutionLogPersistenceAdapterIntegrationTest` deliberately uses `ddl-auto=validate` (unlike its sibling integration tests) so the `cost_estimate` class of mismatch — and any future one — fails automatically instead of only surfacing via a manual smoke test.

## Alternatives Considered

A second Spring bean (a dedicated "persister" collaborator with a real `@Transactional` method, injected into the handler so the call goes through the Spring proxy) was considered as the conventional way to avoid the self-invocation trap, but discarded in favor of `TransactionTemplate` to avoid adding a class beyond what the task's Technical Design already listed.

Declaring `cost_estimate` as `NUMERIC` and instead adding a `@Column(columnDefinition = "numeric")` override on the entity field was considered, but discarded — matching Hibernate's default `Double` mapping (`DOUBLE PRECISION`) needs no entity-level override and is simpler.
