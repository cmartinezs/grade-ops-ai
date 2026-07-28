<a id="top"></a>

# 06 — Acceptance Criteria Ownership

**Parent:** [README](README.md) · **Status:** Ready · **Prev:** [05 — Execution Order](05-execution-order.md) · **Next:** [07 — Integration and Final Verification →](07-integration-and-final-verification.md)

Source: [10 — Acceptance Criteria](../../implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md), 25 rows, unchanged text. Every row below has exactly one primary owner. No row is marked `PASS` by two sessions contradictorily — Session D issues the one global `PASS` per criterion, using each workspace's local test as evidence, not by re-deriving the result itself.

| # | Criterion | Task(s) | Primary owner | Supporting owner | Local test | Integration check |
|---|---|---|---|---|---|---|
| 1 | A human edit creates a new revision | 08 | API | — | `CreateHumanRevisionHandlerIntegrationTest` | Re-run in Task 14 full suite |
| 2 | The original AI content remains intact | 08 | API | — | Same test, old revision re-fetched | Re-run in Task 14 |
| 3 | Every revision records origin, actor, timestamp, predecessor | 04, 07, 08, 09 | API | — | Unit + integration tests on `AssessmentRevision` factories | Re-run in Task 14 |
| 4 | No in-place updates destroy provenance | 08, 13 | API | — | `UpdateAssessmentDraftHandler`/`applyEdit` deleted; compiler + full suite | `grep -r "applyEdit" api/src` returns nothing (Session D) |
| 5 | Two concurrent edits don't produce silent last-write-wins | 08 | API | — | Concurrency test — one success, one `409 STALE_REVISION` | Re-run in Task 14 |
| 6 | Two concurrent regenerations don't create the same version number | 09 | API | — | Concurrency test | Re-run in Task 14 |
| 7 | Repeating creation with the same idempotency key doesn't create another `Assessment` | 06, 10 | API | WEB (must actually send the key) | Idempotency replay test on `POST /assessments` | Web acceptance test confirms client sends `Idempotency-Key`; API test confirms server-side dedup |
| 8 | Reusing an idempotency key with a different payload produces an explicit conflict | 06, 10 | API | — | `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH` test | Re-run in Task 14 |
| 9 | A generation failure leaves a visible, recoverable operation | 07, 10 | API | — | `GET generation-status` returns `FAILED_RETRYABLE`; Research 02 §5.6 regression test | Cross-checked against Web's resume test (#15) |
| 10 | Retrying doesn't create another `Assessment` | 10 | API | — | Retry endpoint test — same `assessmentId`, same `AiOperation` | Re-run in Task 14 |
| 11 | Every external attempt is recorded before dispatch | 07 | API | — | Simulated-crash-after-Phase-0 test | Re-run in Task 14 |
| 12 | Retries are associated with the same logical operation | 07, 10 | API | — | `attemptNumber` increments under one `AiOperation.id` | Re-run in Task 14 |
| 13 | Real provider, model, correlation id are persisted | 07 | API | AGENTS (07A supplies the values) | `AgentAttempt.resolvedProvider`/`resolvedModel`/`correlationId` non-null, asserted against Agents' actual returned value | 07C contract test (see [04 — Task Distribution](04-task-distribution.md#task-07-split)) |
| 14 | Tokens, cost, latency, failure code are recordable | 05, 07 | API | AGENTS (supplies the values) | Field-level assertions in `AiOperationCoordinatorTest` | Re-run in Task 14 |
| 15 | Web can resume a failed generation after refresh | 11 | WEB | API (provides `generation-status`) | Jest/RTL test reproducing Research 02 §5.6 from the client side | Session D runs it against a real API instance, not only the mock |
| 16 | The system can differentiate operation, run, and attempt | 04, 05 | API | — | Documented consolidation (2 tables, not 3) with a stated reversal path | N/A — structural/documentation criterion |
| 17 | Flyway works from an empty database | 01–03, 12 | API | — | Migration test, empty-DB apply of V1–V17 | Re-run in Task 14 |
| 18 | Existing migrations remain valid | 01–03 | API | — | Full suite still passes with V1–V12 untouched | Re-run in Task 14 |
| 19 | Legacy drafts migrate without inventing provenance | 12 | API | — | `LegacyAuthoringBackfillMigrationTest` | Re-run in Task 14 |
| 20 | API compatibility is maintained or the cut is clearly documented | 03, 10 | API | WEB (the one consumer being updated in the same cut) | [03 — Operation and API Contracts](../../implementation-plans/assessment-authoring-operation-foundation/03-operation-and-api-contracts.md) documents every breaking change | Session D confirms Web's `WEB-HANDOFF.md` references every breaking change it absorbed |
| 21 | Tests demonstrate idempotency | 06, 07, 09, 10 | API | — | Idempotency test suite across every mutating endpoint | Re-run in Task 14 |
| 22 | Tests demonstrate optimistic concurrency | 08, 09 | API | — | Concurrency test suite | Re-run in Task 14 |
| 23 | Tests demonstrate failure recovery | 07, 10, 11 | API | WEB (client-side resume proof) | Failure-recovery integration tests (API) + Web resume test (#15) | Session D confirms both exist and both pass |
| 24 | No universal status for authoring/application/grading/publication | 01–14 (structural) | API | — | `AssessmentStatus` untouched — zero diff in `AssessmentStatus.java` | `git diff` at Task 14, verified by Session D |
| 25 | AI output remains a proposal, never an authoritative academic decision | 07, 08 | API | AGENTS (never asserts authority; returns proposal only) | No code path lets an `AiOperation`/`AgentAttempt` result become a revision without the same creation path a human edit uses | Session D confirms no "auto-approve" path exists in either workspace |

## Definition of done, restated per this split

All 25 rows have a passing, named test, and:

- Session A's local suite ([Research 03](../../../design-system/research/research-03-baseline-technical-verification.md)'s API command) is green.
- Session B's local suite (`agents/` `-Pdemo` command) is green.
- Session C's local suite (`web/` lint/test/build) is green.
- Session D has independently re-run every "Re-run in Task 14" row against the merged integration branch, not only trusted each session's self-report.
- No row in this table is missing a primary owner (verified — every row above has one).
- No row is claimed `PASS` by two sessions with different evidence for the same claim.

---

← [05 — Execution Order](05-execution-order.md) | [↑ inicio](#top) | [Siguiente: Integration and Final Verification →](07-integration-and-final-verification.md)
