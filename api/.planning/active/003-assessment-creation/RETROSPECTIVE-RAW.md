# Retrospective Raw Notes: 003-assessment-creation

> [← README](README.md)

Working log for events that were unexpected, corrective, risky, or useful for the final retrospective.

Use `/plan-edge-case <planning-id> -- <what happened>` to add manual entries. Commands may also append entries when they encounter blockers, corrections, skipped work, recovery actions, validation failures, or other non-linear events.

---

## How To Use This File

Capture facts while they are fresh. Do not polish entries here. The final retrospective belongs in `README.md`.

Each entry should answer as many of these as possible:

- What happened?
- What was expected instead?
- How was it resolved or contained?
- What should be carried forward?

---

## Log

<!-- Add newest entries at the top. -->

### 2026-07-13 21:00 - Human code review requested corrections on task-07: missing real-repository integration test for the log/draft cross-reference

- **Source:** manual (human developer review on PR #52)
- **Related story/task:** story-01, task-07
- **What happened:** Human reviewer filed a MEDIUM finding: `GenerateAssessmentDraftHandlerTest` used mocked repositories (so it never exercised the `TransactionTemplate` against real JPA/Flyway/Postgres) and `AgentExecutionLogPersistenceAdapterIntegrationTest` validated the log's own schema/round-trip but never created a draft referencing it — so no automated test actually proved the log→draft→log-backfill cross-reference (`assessment_drafts.agent_execution_log_id` ↔ `agent_execution_logs.draft_id`) persisted correctly together through the real handler and a real transaction. A bug in that ordering could pass every existing test, since mocks accept any call order and the schema test never created a draft at all.
- **Expected instead:** An integration test using the real `GenerateAssessmentDraftHandler` with real repositories (Testcontainers Postgres, Flyway-migrated) and only `AssessmentAgentClient` stubbed, verifying both the success path (draft v1 + `COMPLETED` log, bidirectionally cross-referenced) and the failure path (no draft row, `FAILED` log with `errorCode` set) against actual persisted rows.
- **Resolution:** Added `GenerateAssessmentDraftHandlerIntegrationTest` (`@DataJpaTest` + Testcontainers, `ddl-auto=validate`) constructing the handler with real `AssessmentPersistenceAdapter`/`AssessmentBriefPersistenceAdapter`/`AssessmentDraftPersistenceAdapter`/`AgentExecutionLogPersistenceAdapter` and a mocked `AssessmentAgentClient`. The success test's captured Hibernate SQL confirms the exact intended sequence: insert `agent_execution_logs` (`draft_id=null`) → insert `assessment_drafts` (`agent_execution_log_id` set) → `UPDATE agent_execution_logs SET draft_id=...` — verified against real rows read back after `entityManager.flush()/clear()`, not just mock interactions. Full suite: 236/236 (Docker available).
- **Retrospective signal:** a unit test with mocked repositories and a persistence-adapter integration test that only exercises one entity in isolation can each pass while the actual cross-entity transactional flow between them is still broken — neither one, on its own, proves the two rows a handler is supposed to write together actually end up consistently cross-referenced in the database. Any future handler that persists two-or-more related rows across ports (not just a single adapter) should get its own real-repository integration test, not just a mocked handler test plus separate per-adapter round-trip tests.


### 2026-07-13 14:00 - Human code review requested corrections on task-03: mutable list aggregate state

- **Source:** manual (human developer review on PR #48)
- **Related story/task:** story-01, task-03
- **What happened:** Human reviewer filed a MEDIUM finding on `AssessmentDraft.java`: `generate(...)`, `regenerate(...)`, and `restore(...)` stored the raw `List<String>` references passed in for `objectives`/`deliverables`/`constraints` directly, and the getters returned those same references — allowing a caller to mutate a nominally immutable, append-only versioned draft either by mutating the list handed to the factory after construction, or by mutating the list returned from a getter. This directly contradicted this repo's own DDD guideline (`docs/gradeops-ai-java-guidelines/02-ddd-tactico.md`, which shows `List.copyOf(...)` in its own `Assessment` aggregate example) and the sibling `agents/`'s `AssessmentResult` record, which this task's fields are supposed to mirror and which already does `List.copyOf(...)` in its compact constructor for exactly this reason.
- **Expected instead:** Defensive copying of all three list fields at construction time (`List.copyOf(...)`), matching the established convention already present elsewhere in this codebase and in the mirrored `agents/` contract.
- **Resolution:** Changed all three assignment sites (`generate`, `regenerate`, `restore`) to `List.copyOf(objectives/deliverables/constraints)`; `List.copyOf` throwing on null is safe because `validateContent(...)` already rejects null lists before the copy runs. Added 4 domain tests: mutating the original list after `generate`/`regenerate`/`restore` doesn't affect the draft, and the three getters return lists that throw `UnsupportedOperationException` on mutation. Full suite: 199/199 (Docker available).
- **Retrospective signal:** task-01's `Assessment` and task-02's `AssessmentBrief` have no `List<String>` fields, so this class of bug had no earlier chance to surface in this story — worth a quick self-check whenever a new domain aggregate is the *first* one in a story to hold a collection field: does it copy-on-write like the guideline's own example, or did the pattern get missed because no prior task in the story needed it? Same applies going forward for task-07/08 (`AgentExecutionLog`) and any other draft-adjacent aggregate touching `List` fields.

### 2026-07-13 13:00 - Human code review requested corrections on task-02; fix itself required two more iterations

- **Source:** manual (human developer review on PR #47)
- **Related story/task:** story-01, task-02
- **What happened:** Human reviewer filed `.code-reviews/story-01-assessment-creation-persistence/task-02-assessment-brief.md`, status CAMBIOS SOLICITADOS, with two findings: (1) MEDIUM — `AssessmentBriefPersistenceAdapterIntegrationTest`'s round-trip assertion compared `createdAt` for exact equality against an untruncated `Instant.now()`, risking flakiness against Postgres's microsecond `TIMESTAMPTZ` precision, and didn't follow the project's own established truncation convention already present in `PasswordResetCodeJpaRepositoryIntegrationTest`; (2) LOW — `TRACEABILITY.md`'s term matrix still marked `AssessmentBrief` as `❌` in the `AP` column after task-02 was merged, contradicting the actual code state. Applying the straightforward fix for (1) — `.truncatedTo(ChronoUnit.MICROS)` on the expected value — immediately failed with `expected: ...342930Z but was: ...342930060Z`: the retrieved `createdAt` still carried full nanosecond precision, unchanged from the in-memory value. Root cause: `@DataJpaTest`'s Hibernate first-level cache returned the same managed entity instance on `findByAssessmentId` within the same persistence-context session, so the assertion never actually round-tripped through Postgres at all. Added `entityManager.flush()` + `entityManager.clear()` before the read to force a genuine DB round trip — which then surfaced a second, real issue: pgjdbc rounds to the nearest microsecond when storing rather than truncating, so a Java-side floor-truncated `Instant` was off by exactly 1 microsecond (`...974870Z` vs `...974871Z`) from the actual stored/retrieved value on some runs.
- **Expected instead:** A round-trip persistence test should force a real database read (not rely on incidental session-cache behavior) and compare timestamps at a granularity wide enough to absorb legitimate storage-precision and driver-rounding differences, not just nominal DB column precision.
- **Resolution:** Added `entityManager.flush()`/`.clear()` between save and read; changed the `createdAt` assertion to truncate **both** sides to `ChronoUnit.MILLIS` (wide enough to clear the 1-microsecond pgjdbc rounding noise) instead of comparing one truncated side against one untruncated side. Verified stable across 3 repeated runs. Fixed the `TRACEABILITY.md` term-matrix row for `AssessmentBrief` (`AP: ❌ → ✅`, also added `DO: ✅` since the task's inline doc guide counts as delivered documentation for that term, per the reviewer's own suggestion).
- **Retrospective signal:** `@DataJpaTest`'s first-level cache silently makes "round-trip" integration tests non-round-tripping unless the persistence context is explicitly cleared between write and read — this is a trap that isn't obvious from the test passing green, since a stale in-memory read looks identical to a correct DB read for every field except ones with representation drift (like sub-millisecond timestamp precision) that happen to expose it. Any future `@DataJpaTest` "round trip" test in this codebase should flush+clear before reading back, not just flush. Separately: this project's `truncatedTo(ChronoUnit.MICROS)` timestamp-comparison convention (from `PasswordResetCodeJpaRepositoryIntegrationTest`) is insufficient on its own when the value under comparison wasn't independently re-truncated on both sides before the assertion — MICROS-level comparisons are still vulnerable to pgjdbc's round-vs-truncate mismatch; MILLIS is the safer default granularity for this kind of assertion unless a test specifically needs to prove microsecond-level precision.

### 2026-07-13 00:00 - Human code review requested corrections on task-01

- **Source:** manual (human developer review on PR #45)
- **Related story/task:** story-01, task-01
- **What happened:** Human reviewer filed `.code-reviews/story-01-assessment-creation-persistence/task-01-assessment-aggregate.md` with two MEDIUM findings after task-01 was already marked DONE and its PR opened: (1) `Assessment.restore(...)` accepted a null `id` and null `createdAt`, letting an invalid aggregate be reconstructed and deferring the failure to infrastructure (NPE in the mapper, or a DB NOT NULL rejection); (2) `AssessmentPersistenceAdapter.findAllByTeacherId` queried the real `assessments` table and mapped whatever rows existed (with `title = null`), instead of staying empty-list-preserving — this contradicts task-10's own Technical Design, which explicitly describes task-01's `findAllByTeacherId` as "left as an empty-list-preserving stub-equivalent," and would have silently broken the dashboard as soon as task-06 started creating `Assessment` rows, before task-10's join logic exists. The reviewer also could not run `AssessmentPersistenceAdapterIntegrationTest` locally because Docker/Testcontainers was unavailable (`/var/run/docker.sock` absent).
- **Expected instead:** `restore` should validate all four fields it reconstructs (mirroring the guideline example's `requireNonNull` pattern), and `findAllByTeacherId` should match the atomized plan's documented contract exactly (always empty until task-10), not just "empty today because the table happens to be empty."
- **Resolution:** Added null checks for `id` and `createdAt` in `Assessment.restore`; changed `findAllByTeacherId` to unconditionally return `List.of()` with a comment pointing at task-10's Technical Design as the source of the contract; added `AssessmentTest.java` (9 domain-level invariant tests) that didn't exist before; updated `AssessmentPersistenceAdapterTest`/`AssessmentPersistenceAdapterIntegrationTest` to assert the corrected empty-list contract instead of asserting row-mapping. Verified the fix via `./mvnw test` excluding the three Testcontainers-dependent classes (same Docker-unavailable environment the reviewer hit) — 137/138 green, the one failure (`OwnershipVerifierTest$GlobalExceptionHandlerNotFoundTest`, unrelated to assessment code) was confirmed pre-existing and order-dependent by reproducing it on a stashed pre-fix baseline with the identical exclusion set. Pushed as a follow-up commit to the existing task branch/PR #45 for re-review.
- **Retrospective signal:** task-01's own Implementation Steps (step 7) phrased the temporary `findAllByTeacherId` behavior ambiguously ("query the real table and map to `List.of()`-equivalent") in a way that was satisfiable by two different implementations — only one of which matched what task-10 was written to assume. When a later task's design depends on an earlier task leaving a method behaving a specific way, that inter-task contract should be stated as an explicit, unambiguous instruction in the earlier task file (e.g. "return `List.of()` unconditionally, do not query"), not inferred from the later task's description.

---

> [← README](README.md)
