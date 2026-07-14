# End-to-end integration tests — assessment creation flow

**Source:** task-11-integration-tests | **Area:** AP | **Date:** 2026-07-14

## What it does

A dedicated, no-new-production-code verification pass that chains every endpoint from task-06 through task-10 together — brief intake → draft generation → regeneration → edit → dashboard listing → retrieve current → retrieve version history — and asserts consistency at each step, something no single task's own focused tests exercised on their own. Closes out story-01: every story-level Done Criterion is now traceable to an automated test.

## How to use it

`AssessmentCreationFlowIntegrationTest` (real Postgres via Testcontainers, `AssessmentAgentClient` mocked) covers four scenarios:

- **Full happy path** — brief → generate (v1) → regenerate (v2) → edit (still v2, in place) → dashboard listing reflects the edited title → current-draft retrieval matches → version history lists both versions newest-first, v1 unchanged.
- **Agent failure during generation** — the brief survives untouched, no draft row is created, exactly one `FAILED` `AgentExecutionLog` is persisted (`draftId` null), and the assessment stays queryable on the dashboard (falls back to the brief's topic since no draft exists).
- **Two regenerations in a row** — three distinct, retrievable versions, none overwritten, each version's `previousVersionId` chained to the one before it, and three distinct `AgentExecutionLog` rows.
- **Edit after regeneration** — the edit lands on the latest version's row in place (same id, same version number); the earlier version is untouched; the version count stays the same.

A residual was recorded during review: `@DataJpaTest` wraps each test in a rollback-managed transaction, so this class (like task-07/08/09's own integration tests) cannot prove `DraftGenerationCoordinator`'s "no DB transaction open while the agent call runs" invariant — see story Residual #3.

## Example

```bash
./mvnw test -Dtest=AssessmentCreationFlowIntegrationTest
./mvnw test
```
