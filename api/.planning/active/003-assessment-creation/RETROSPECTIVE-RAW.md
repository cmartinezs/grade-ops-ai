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

### 2026-07-13 00:00 - Human code review requested corrections on task-01

- **Source:** manual (human developer review on PR #45)
- **Related story/task:** story-01, task-01
- **What happened:** Human reviewer filed `.code-reviews/story-01-assessment-creation-persistence/task-01-assessment-aggregate.md` with two MEDIUM findings after task-01 was already marked DONE and its PR opened: (1) `Assessment.restore(...)` accepted a null `id` and null `createdAt`, letting an invalid aggregate be reconstructed and deferring the failure to infrastructure (NPE in the mapper, or a DB NOT NULL rejection); (2) `AssessmentPersistenceAdapter.findAllByTeacherId` queried the real `assessments` table and mapped whatever rows existed (with `title = null`), instead of staying empty-list-preserving — this contradicts task-10's own Technical Design, which explicitly describes task-01's `findAllByTeacherId` as "left as an empty-list-preserving stub-equivalent," and would have silently broken the dashboard as soon as task-06 started creating `Assessment` rows, before task-10's join logic exists. The reviewer also could not run `AssessmentPersistenceAdapterIntegrationTest` locally because Docker/Testcontainers was unavailable (`/var/run/docker.sock` absent).
- **Expected instead:** `restore` should validate all four fields it reconstructs (mirroring the guideline example's `requireNonNull` pattern), and `findAllByTeacherId` should match the atomized plan's documented contract exactly (always empty until task-10), not just "empty today because the table happens to be empty."
- **Resolution:** Added null checks for `id` and `createdAt` in `Assessment.restore`; changed `findAllByTeacherId` to unconditionally return `List.of()` with a comment pointing at task-10's Technical Design as the source of the contract; added `AssessmentTest.java` (9 domain-level invariant tests) that didn't exist before; updated `AssessmentPersistenceAdapterTest`/`AssessmentPersistenceAdapterIntegrationTest` to assert the corrected empty-list contract instead of asserting row-mapping. Verified the fix via `./mvnw test` excluding the three Testcontainers-dependent classes (same Docker-unavailable environment the reviewer hit) — 137/138 green, the one failure (`OwnershipVerifierTest$GlobalExceptionHandlerNotFoundTest`, unrelated to assessment code) was confirmed pre-existing and order-dependent by reproducing it on a stashed pre-fix baseline with the identical exclusion set. Pushed as a follow-up commit to the existing task branch/PR #45 for re-review.
- **Retrospective signal:** task-01's own Implementation Steps (step 7) phrased the temporary `findAllByTeacherId` behavior ambiguously ("query the real table and map to `List.of()`-equivalent") in a way that was satisfiable by two different implementations — only one of which matched what task-10 was written to assume. When a later task's design depends on an earlier task leaving a method behaving a specific way, that inter-task contract should be stated as an explicit, unambiguous instruction in the earlier task file (e.g. "return `List.of()` unconditionally, do not query"), not inferred from the later task's description.

---

> [← README](README.md)
