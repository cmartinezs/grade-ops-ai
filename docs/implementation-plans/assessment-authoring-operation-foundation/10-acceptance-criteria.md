<a id="top"></a>

# 10 — Acceptance Criteria

**Parent:** [README](README.md) · **Status:** Planned · **Prev:** [09 — Risks and Rollback](09-risks-and-rollback.md)

The 25 minimum criteria this plan must satisfy, each mapped to the task and test that proves it. "Done" for this plan means every row below has a passing, named test — not a prose claim.

| # | Criterion | Task | Proven by |
|---|---|---|---|
| 1 | A human edit creates a new revision | 08 | `CreateHumanRevisionHandlerIntegrationTest` — new id/version asserted |
| 2 | The original AI content remains intact | 08 | Same test — old revision re-fetched, content unchanged |
| 3 | Every revision records origin, actor, timestamp, predecessor | 04, 07, 08, 09 | Unit tests on `AssessmentRevision` factories; integration tests assert all four fields on every created revision |
| 4 | No in-place updates destroy provenance | 08, 13 | `UpdateAssessmentDraftHandler`/`applyEdit` deleted; compiler + full suite confirm no path remains |
| 5 | Two concurrent edits don't produce silent last-write-wins | 08 | Concurrency test — exactly one success, one `409 STALE_REVISION` |
| 6 | Two concurrent regenerations don't create the same version number | 09 | Concurrency test — exactly one success; `UNIQUE (assessment_id, version_number)` as defense-in-depth |
| 7 | Repeating creation with the same idempotency key doesn't create another `Assessment` | 06, 10 | Idempotency replay test on `POST /assessments` |
| 8 | Reusing an idempotency key with a different payload produces an explicit conflict | 06, 10 | `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH` test |
| 9 | A generation failure leaves a visible, recoverable operation | 07, 10 | `GET generation-status` returns `FAILED_RETRYABLE`; Research 02 §5.6 regression test |
| 10 | Retrying doesn't create another `Assessment` | 10 | Retry endpoint test — same `assessmentId`, same `AiOperation` |
| 11 | Every external attempt is recorded before dispatch | 07 | Simulated-crash-after-Phase-0 test |
| 12 | Retries are associated with the same logical operation | 07, 10 | `attemptNumber` increments under one `AiOperation.id` |
| 13 | Real provider, model, correlation id are persisted | 07 | `AgentAttempt.resolvedProvider`/`resolvedModel`/`correlationId` non-null on a completed attempt, asserted against the value `agents/` actually returned, not the (often-null) request value |
| 14 | Tokens, cost, latency, failure code are recordable | 05, 07 | Field-level assertions in `AiOperationCoordinatorTest` |
| 15 | Web can resume a failed generation after refresh | 11 | Jest/RTL test reproducing Research 02 §5.6 from the client side |
| 16 | The system can differentiate operation, run, and attempt | 04, 05 | Documented consolidation (2 tables, not 3) with a stated reversal path — see [Durable AI Operation Model § Two tables, not three](../../99-decisions/2026-07-28-durable-ai-operation-model.md#two-tables-not-three); responsibilities fully resolved even though table count differs from the sketch |
| 17 | Flyway works from an empty database | 01–03, 12 | Migration test, empty-DB apply of V1–V17 |
| 18 | Existing migrations remain valid | 01–03 | Full suite still passes (289+ baseline from Research 03, plus new tests) with V1–V12 untouched |
| 19 | Legacy drafts migrate without inventing provenance | 12 | `LegacyAuthoringBackfillMigrationTest` — asserts `LEGACY_UNKNOWN`/`provenanceComplete=false` unconditionally, zero fabricated `AI_GENERATED`/`HUMAN_EDITED` |
| 20 | API compatibility is maintained or the cut is clearly documented | 03, 10 | [03 — Operation and API Contracts](03-operation-and-api-contracts.md) documents every breaking change explicitly; no silent break |
| 21 | Tests demonstrate idempotency | 06, 07, 09, 10 | Idempotency test suite across every mutating endpoint |
| 22 | Tests demonstrate optimistic concurrency | 08, 09 | Concurrency test suite |
| 23 | Tests demonstrate failure recovery | 07, 10, 11 | Failure-recovery integration tests + Web resume test |
| 24 | No universal status for authoring/application/grading/publication | 01–14 (structural) | `AssessmentStatus` untouched — zero diff in `AssessmentStatus.java` across this entire plan, verifiable by `git diff` at task 14 |
| 25 | AI output remains a proposal, never an authoritative academic decision | 07, 08 | No code path in this plan lets an `AiOperation`/`AgentAttempt` result become a revision without going through the same `AssessmentRevision` creation path a human edit also uses — no separate "auto-approve" path exists |

## Definition of done for the whole plan

All 25 rows above have a passing, named test on a branch where:

- [Research 03](../../../design-system/research/research-03-baseline-technical-verification.md)'s full command set (§2) still passes.
- `git diff` shows zero changes to `AssessmentStatus.java`, and zero new columns/endpoints for organizations, sections, students, submissions, grading, results, publication, appeals, or batch processing.
- The four [2026-07-28 ADRs](../../99-decisions/README.md) require no amendment to match what was actually built — if implementation reveals a decision was wrong, the ADR is updated (superseded or amended) before the code that contradicts it merges, not after.

---

← [09 — Risks and Rollback](09-risks-and-rollback.md) | [↑ README](README.md)
