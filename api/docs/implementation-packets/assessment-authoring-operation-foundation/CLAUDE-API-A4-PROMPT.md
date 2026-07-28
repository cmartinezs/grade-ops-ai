<a id="top"></a>

# CLAUDE-API-A4-PROMPT — Backfill, Cleanup and Final API Handoff

**Status:** Ready. **Parent:** [README](README.md) · **Session:** A4 of 4 (API, final) · **Prev session:** [CLAUDE-API-A3-PROMPT.md](CLAUDE-API-A3-PROMPT.md) · **Next:** Session D — [FINAL-INTEGRATION-PROMPT.md](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/FINAL-INTEGRATION-PROMPT.md)

Give this file, verbatim, as the task prompt to a **fresh** session that opens **only `api/`**. It is complete and self-contained.

## Repository and workspace identity

```text
Repository: cmartinezs/grade-ops-ai
Workspace: api/ (Spring Boot 4 + Java 21 + PostgreSQL, Maven)
Branch: feat/assessment-authoring-operation-foundation-api  (continuing, not new)
Session: A4 — Backfill, Cleanup and Final API Handoff (fourth and last of four sequential API sessions)
```

## Who you are and what you are building

You are Session A4, the last of four API sessions. Sessions A1–A3 built and wired the entire new authoring model; the public contract is complete and Web-ready. This session does the one-time, irreversible-in-spirit (though technically additive) work that only makes sense once everything else is proven correct: migrate legacy `assessment_drafts`/`agent_execution_logs` data honestly into the new model, delete the now-dead legacy code paths, run the full regression, and produce the **consolidated API handoff** Session D will treat as the authoritative record of everything Sessions A1–A4 did.

**This is documentation-implementing work.** You write real Flyway migration SQL, delete real Java files, run real tests.

## Prerequisites gate

```bash
git fetch origin
git switch feat/assessment-authoring-operation-foundation-api
git pull --ff-only
git log --oneline -15
./mvnw -f api/pom.xml clean test
```

Confirm the branch's last commit is `docs(api): record session A3 handoff` (or later), [API-A3-HANDOFF.md](API-A3-HANDOFF.md) exists with no unresolved critical blocker, and the baseline you just ran is genuinely green. If any of this fails, stop and report it as a blocker.

## Authority — read in this order before writing any code

1. [Assessment Authoring Model](../../../../docs/99-decisions/2026-07-28-assessment-authoring-model.md) § Migration Impact
2. [06 — Database Migration](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/06-database-migration.md) of the plan — read this one in full, it contains the reasoning trail for why the labeling rule is unconditionally conservative, not just the conclusion
3. This packet's [TASKS.md](TASKS.md) (Tasks 12, 13 specifically), [LOCAL-CONTRACTS.md § Legacy migration](LOCAL-CONTRACTS.md#legacy-migration), [API-A3-HANDOFF.md](API-A3-HANDOFF.md)

If anything here contradicts a document above it, the higher document wins — report the discrepancy (see [Handling discoveries](#handling-discoveries)). **Do not re-derive a more "generous" labeling rule than what's specified** — the conservative, unconditional `LEGACY_UNKNOWN` rule is the decision, already argued through in [06 — Database Migration](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/06-database-migration.md), not a starting hypothesis for you to improve on.

## Git — continuing on the same branch

```bash
git fetch origin
git switch feat/assessment-authoring-operation-foundation-api
git pull --ff-only
```

Do **not** create a new branch. Do not force-push, rebase destructively, or amend a commit from any prior session.

## Scope — exactly these two tasks plus final regression, in order

### Task 12 — Database: legacy data backfill

- **Objective:** Execute the honest legacy migration.
- **Files:** `api/src/main/resources/db/migration/V17__backfill_legacy_authoring_data.sql`
- **Dependencies:** Tasks 01–10 complete (Sessions A1–A3, all done)
- **Steps:** Every `assessment_drafts` row → `LEGACY_UNKNOWN`, `provenanceComplete = false`, `actorId = NULL`, unconditionally — **not** conditionally on whether the row was ever the "current" version, because the legacy schema has no edit-timestamp column and therefore no row can be honestly distinguished. Every `agent_execution_logs` row → one `ai_operations` + one `agent_attempts` row with fields copied faithfully (`status = SUCCEEDED` if the log's `status = 'COMPLETED'` and it produced a draft, else `FAILED_TERMINAL`; `attemptNumber = 1`; `operationType` inferred from the linked draft's `version_number`, `1` → `CREATE_INITIAL_REVISION`, `>1` → `REGENERATE_REVISION`).
- **Tests to write first:** Migration test with fixture data (single-version assessment, multi-version regenerated assessment, failed-generation-only assessment), asserting the exact labels above and zero fabricated fields, written before the migration script.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=LegacyAuthoringBackfillMigrationTest`, then full suite
- **Acceptance criteria:** Row counts match; no `AI_GENERATED`/`HUMAN_EDITED` label appears anywhere in migrated data; `provenanceComplete = false` on every migrated revision.
- **Risks:** Medium — main risk is scale on a large legacy table, not correctness (labeling rule is deliberately simple). Script must be safe to run twice (`WHERE NOT EXISTS` guard).
- **Commit boundary:** `feat(api): backfill legacy assessment_drafts/agent_execution_logs into revision/operation model`

### Task 13 — Cleanup: delete dead legacy code paths

- **Objective:** Remove `AssessmentDraft`, `AgentExecutionLog` domain classes, their adapters, and any remnants Sessions A2/A3's deletions missed.
- **Files:** grep for remaining references across `api/src/main/java` and `api/src/test/java`; remove.
- **Dependencies:** Task 07B (Session A2), Task 08 (Session A3), Task 12 (this session, above)
- **Steps:** Confirm zero remaining compile-time references. Note the deprecated tables in `docs/09-developer-guide/05-database-guide.md` (documentation only — the tables themselves stay, per the legacy compatibility window below).
- **Tests to write first:** none new — the existing suite passing with zero references to deleted classes is this task's test.
- **Verification command:** `./mvnw -f api/pom.xml clean test`
- **Acceptance criteria:** Clean compile, full suite green, `grep -r "AssessmentDraft\|AgentExecutionLog" api/src` returns no matches outside migration SQL comments/history.
- **Risks:** Low.
- **Commit boundary:** `refactor(api): remove superseded AssessmentDraft/AgentExecutionLog code paths`

### Legacy compatibility window — do not violate this

`assessment_drafts` and `agent_execution_logs` **tables** are **not** dropped in this cut — read-only retention for one release, per the ADR. Task 13 deletes the *Java code paths* that read/write them, not the tables themselves.

## Out of scope for this session

Any change to `api/`'s public contract (frozen and complete since Session A3). Any change to `agents/` or `web/`. Dropping the legacy tables (explicitly deferred to a future, unscheduled task).

## Non-negotiable principles (reproduced from the ADRs — do not weaken these)

- **Legacy migration is honest, not optimistic.** Every migrated row is `LEGACY_UNKNOWN`/`provenanceComplete=false`/`actorId=NULL`, unconditionally. Never fabricate a historical approval, publication, or human-edit provenance.
- **The migration script is idempotent-guarded** (`WHERE NOT EXISTS` or equivalent) — safe to run twice, for manual-recovery defense in depth, even though Flyway itself normally prevents re-running.
- **No `AssessmentStatus` values are touched.** This entire plan, across all four API sessions, produces zero diff in `AssessmentStatus.java` — verify this explicitly in this final session (`git diff origin/develop...HEAD -- api/src/main/java/**/AssessmentStatus.java` should be empty).

## TDD discipline

Write the migration test with realistic fixture data before the migration script, per Task 12's "Tests to write first."

## Testing

Full matrix: [TEST-PLAN.md](TEST-PLAN.md). This is the session that finally proves guard #1 end-to-end in full (Research 02 §5.6 reproduction including retry — all the pieces from A1–A3 are now in place) if it wasn't already fully provable by A3; confirm it explicitly here even if the test itself was written in A2/A3.

## Security

Never print or persist secrets — in code, migration fixtures, logs, or your handoff. The legacy backfill touches real historical data shapes; do not include real production-looking values in any fixture or log output beyond what's already in the test fixtures this codebase uses.

## Commits

Two implementation commits (Task 12, Task 13) at the exact boundaries above, plus the final regression/handoff commit described below. Before each: `git diff`, `git diff --cached`, `git status`.

## Handling discoveries

If the backfill reveals legacy data that doesn't fit the documented shapes (e.g., a draft row with no corresponding log, an orphaned log), do not invent a new labeling rule — apply the same conservative default (`LEGACY_UNKNOWN`) and record the edge case in [API-A4-HANDOFF.md](API-A4-HANDOFF.md)'s "Blockers" or "Assumptions" section for Session D's awareness.

## Verification before you consider this session — and the entire API packet — done

```bash
./mvnw -f api/pom.xml clean test
```

Full suite green. Then:

```bash
grep -r "AssessmentDraft\|AgentExecutionLog" api/src   # no matches outside migration SQL/history
git diff origin/develop...HEAD -- 'api/src/**/AssessmentStatus.java'   # empty
```

Confirm every one of the 25 acceptance criteria this packet's [06 — Acceptance Criteria Ownership](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/06-acceptance-criteria-ownership.md) assigns a primary or supporting role to API has a passing, named test somewhere across the four sessions' commits — you do not need to re-write these tests, just confirm they exist by walking the commit history.

## Push and produce the consolidated handoff

```bash
git push origin feat/assessment-authoring-operation-foundation-api
```

Fill in **two** documents, both as the last commits on this branch:

1. [API-A4-HANDOFF.md](API-A4-HANDOFF.md) — this session's own handoff, same format as A1–A3's.
2. [HANDOFF.md](HANDOFF.md) — the **consolidated, final API handoff** covering all of Sessions A1–A4 together. This is the document Session D actually reads as authoritative (per [09 — Session Handoff Protocol](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/09-session-handoff-protocol.md), the filled template is committed conceptually as `API-HANDOFF.md`). It must **summarize**, not just link to, A1–A4's individual handoffs: full commit list across all four sessions, every migration (V13–V17), every new endpoint, every deleted class, every test count, the full legacy-migration outcome, all residual risks, and explicit integration instructions for Session D.

Commit both together: `docs(api): record final consolidated handoff for sessions A1-A4`. Push again.

**Do not open a PR. Do not merge anything.** Session D (integration) merges `feat/assessment-authoring-operation-foundation-api` into the integration branch and opens the one functional PR to `develop`.

## Handoff gate — what Session D checks before starting

Session D will not integrate the API branch if: `HANDOFF.md` (the consolidated one) is incomplete or contradicts any of `API-A1-HANDOFF.md` through `API-A4-HANDOFF.md`; there's a gap in the commit sequence across the four sessions; any session's recorded test run wasn't actually green when re-verified; the legacy migration wasn't proven idempotent-safe; or `AssessmentStatus.java` shows any diff.

## Final report to whoever invoked this session

Branch and HEAD; both tasks completed with commit hashes; full test count before/after (and the cumulative count across all four API sessions); confirmation of the two `grep`/`git diff` checks above; confirmation both `API-A4-HANDOFF.md` and the consolidated `HANDOFF.md` were pushed; recommendation that Session D can now proceed once Agents and Web also report ready.

---

[← README](README.md) · [↑ Volver al inicio](#top)
