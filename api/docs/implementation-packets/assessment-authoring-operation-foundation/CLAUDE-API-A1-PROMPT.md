<a id="top"></a>

# CLAUDE-API-A1-PROMPT — Schema and Inert Domain

**Status:** Ready. **Parent:** [README](README.md) · **Session:** A1 of 4 (API) · **Next session:** [CLAUDE-API-A2-PROMPT.md](CLAUDE-API-A2-PROMPT.md)

Give this file, verbatim, as the task prompt to a fresh session that opens **only `api/`**. It is complete and self-contained — the session does not need to read the root coordination package, any other workspace, or the orchestrator prompt ([CLAUDE-IMPLEMENTATION-PROMPT.md](CLAUDE-IMPLEMENTATION-PROMPT.md)) to execute it.

## Repository and workspace identity

```text
Repository: cmartinezs/grade-ops-ai
Workspace: api/ (Spring Boot 4 + Java 21 + PostgreSQL, Maven)
Branch: feat/assessment-authoring-operation-foundation-api
Session: A1 — Schema and Inert Domain (first of four sequential, recoverable API sessions)
```

## Who you are and what you are building

You are Session A1, the first of four sequential sessions implementing the API-owned share of the **Assessment Authoring Operation Foundation** — a cut that replaces in-place-editable `AssessmentDraft` rows with immutable, provenance-tracked `AssessmentRevision`s, adds durable `AiOperation`/`AgentAttempt` evidence written *before* any external AI call, and makes every mutating authoring endpoint idempotent and optimistically concurrent. The full API scope (12 of the plan's 14 tasks) is split across four sessions specifically so no single session has to hold all 12 tasks' context at once — see [CLAUDE-IMPLEMENTATION-PROMPT.md](CLAUDE-IMPLEMENTATION-PROMPT.md) for why.

**Your job in this session: introduce schema, aggregates, and adapters that are entirely inert — nothing in the existing productive request/response flow changes.** Nobody calls this new code yet. This is deliberately the lowest-risk session in the sequence, and its output is a safe, well-defined recovery point: *the schema and domain exist, but no endpoint uses them.* If this session's branch were abandoned right after this session, the application would behave identically to before — that is the point.

**This is documentation-implementing work, not documentation-only.** You write real Java, real Flyway migrations, real tests.

## Authority — read in this order before writing any code

1. [Assessment Authoring Model](../../../../docs/99-decisions/2026-07-28-assessment-authoring-model.md)
2. [Idempotency and Concurrency Strategy](../../../../docs/99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md)
3. [Durable AI Operation Model](../../../../docs/99-decisions/2026-07-28-durable-ai-operation-model.md)
4. [02 — Domain and Data Changes](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/02-domain-and-data-changes.md) of the plan
5. This packet's [TASKS.md](TASKS.md) (Tasks 01–05 specifically) and [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md)

If anything here contradicts a document above it, the higher document wins. Fix this packet by reporting the discrepancy (see [Handling discoveries](#handling-discoveries)) — do not silently follow your own interpretation over the ADR. **Do not modify approved contracts, taxonomies, field names, or the task list itself** — this session implements what is already decided, it does not redesign it.

## Git preflight

```bash
git remote -v                       # confirm origin is cmartinezs/grade-ops-ai
git branch --show-current
git status                          # working tree must be clean before you start
git fetch origin
git switch develop
git pull --ff-only
git ls-remote origin feat/assessment-authoring-operation-foundation
```

- If `feat/assessment-authoring-operation-foundation` (the integration branch) already exists on `origin`, branch from it:
  ```bash
  git fetch origin feat/assessment-authoring-operation-foundation
  git switch -c feat/assessment-authoring-operation-foundation-api origin/feat/assessment-authoring-operation-foundation
  ```
- If it does not exist yet, create it off `develop` first, then branch from it:
  ```bash
  git switch -c feat/assessment-authoring-operation-foundation develop
  git push -u origin feat/assessment-authoring-operation-foundation
  git switch -c feat/assessment-authoring-operation-foundation-api
  ```
- Never implement directly on `develop` or on the bare integration branch.
- **This is the only one of the four API sessions that creates the `feat/assessment-authoring-operation-foundation-api` branch.** Sessions A2–A4 switch to it — see their own prompts.

## Baseline (run before your first commit)

```bash
./mvnw -f api/pom.xml clean test
```

Expected: 289+ tests pass (Research 03's recorded baseline). If this does not pass on a clean checkout of your starting commit, stop and report it — you are not responsible for fixing a pre-existing baseline failure, but you must not build on top of an unverified baseline.

## Scope — exactly these five tasks, in order

### Task 01 — Schema: durable AI operation tables

- **Objective:** Add `ai_operations` and `agent_attempts` tables, additive, unused by any application code yet.
- **Files:** `api/src/main/resources/db/migration/V13__add_agent_attempts_and_ai_operations.sql`
- **Dependencies:** none
- **Steps:** Write the migration per [LOCAL-CONTRACTS.md § Persistence changes](LOCAL-CONTRACTS.md#persistence-changes). Include the partial unique index `uq_ai_operations_in_flight`.
- **Tests to write first:** none (schema-only). After: a Testcontainers test asserting the migration applies cleanly to an empty DB and the partial unique index rejects a second `PENDING` row for the same `(assessment_id, operation_type)`.
- **Verification command:** `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** Flyway applies V13 cleanly alongside V1–V12; the partial unique index behaves as specified.
- **Risks:** Low. Rollback: `DROP TABLE agent_attempts, ai_operations;`.
- **Commit boundary:** `feat(api): add durable ai_operations/agent_attempts schema`

### Task 02 — Schema: assessment revisions and current-revision pointer

- **Objective:** Add `assessment_revisions` table and `assessments.current_revision_id`/`lock_version` columns.
- **Files:** `api/src/main/resources/db/migration/V14__add_assessment_revisions.sql`, `V15__add_assessment_current_revision.sql`
- **Dependencies:** Task 01 (FK to `agent_attempts`)
- **Steps:** Per [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md). `lock_version` defaults `0 NOT NULL`.
- **Tests to write first:** none; after: migration test extended for V14/V15, plus a direct-SQL test proving `UNIQUE (assessment_id, version_number)` and the self-referential FK exist.
- **Verification command:** `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** V14/V15 apply cleanly after V13; existing `assessments` rows get `lock_version = 0`, `current_revision_id = NULL`.
- **Risks:** Low. Rollback: `ALTER TABLE assessments DROP COLUMN current_revision_id, DROP COLUMN lock_version; DROP TABLE assessment_revisions;`.
- **Commit boundary:** `feat(api): add assessment_revisions schema and current-revision pointer`

### Task 03 — Schema: idempotency records

- **Objective:** Add `idempotency_records` table.
- **Files:** `api/src/main/resources/db/migration/V16__add_idempotency_records.sql`
- **Dependencies:** Task 02 (FK to `assessments`)
- **Steps:** Per [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md).
- **Tests to write first:** none; after: unique-constraint direct-SQL test.
- **Verification command:** `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** V16 applies cleanly; unique constraint rejects a duplicate `(scope_type, teacher_uid, assessment_id, operation_type, idempotency_key)`.
- **Risks:** Low. Rollback: `DROP TABLE idempotency_records;`.
- **Commit boundary:** `feat(api): add idempotency_records schema`

### Task 04 — Domain: `AssessmentRevision` aggregate + persistence

- **Objective:** Introduce `AssessmentRevision` domain class, JPA entity, repository port + adapter — parallel to `AssessmentDraft`, not yet wired into any use case.
- **Files:** `.../assessment/domain/model/AssessmentRevision.java`, `RevisionOrigin.java`; `.../application/port/out/AssessmentRevisionRepositoryPort.java`; `.../infrastructure/adapter/out/persistence/AssessmentRevisionJpaEntity.java`, `AssessmentRevisionPersistenceAdapter.java`
- **Dependencies:** Task 02
- **Steps:** Factories `generateFromAi`/`regenerateFromAi`/`createFromHumanEdit`/`restore`, no `applyEdit`. Full field list in [LOCAL-CONTRACTS.md § `AssessmentRevision`](LOCAL-CONTRACTS.md#assessmentrevision).
- **Tests to write first:** Unit tests for chain-integrity invariants (version 1 has no predecessor; N>1 requires same-assessment predecessor at N-1) and immutability, before the class exists — TDD.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AssessmentRevisionTest`, then full `./mvnw -f api/pom.xml test`
- **Acceptance criteria:** All invariants in [LOCAL-CONTRACTS.md § Invariants](LOCAL-CONTRACTS.md#invariants) hold under unit test; a Testcontainers round-trip test (`AssessmentRevisionPersistenceAdapterTest`) proves save/load fidelity.
- **Risks:** Low — purely additive, unreachable from any endpoint yet.
- **Commit boundary:** `feat(api): add AssessmentRevision aggregate and persistence`

### Task 05 — Domain: `AiOperation`/`AgentAttempt` aggregates + persistence

- **Objective:** Introduce both new aggregates, ports, and adapters — not yet wired into any use case.
- **Files:** `.../domain/model/AiOperation.java`, `AiOperationType.java`, `AiOperationStatus.java`, `AgentAttempt.java`, `AgentAttemptStatus.java`; ports + adapters mirroring Task 04's pattern.
- **Dependencies:** Task 01
- **Steps:** Per [LOCAL-CONTRACTS.md § `AiOperation`/`AgentAttempt`](LOCAL-CONTRACTS.md#aioperation). Include state-transition guards (e.g., cannot transition `SUCCEEDED` → anything).
- **Tests to write first:** Unit tests for valid/invalid state transitions on both aggregates, written first.
- **Verification command:** `./mvnw -f api/pom.xml test -Dtest=AiOperationTest,AgentAttemptTest`, then full suite
- **Acceptance criteria:** Invalid transitions throw `DomainInvariantViolationException`; Testcontainers round-trip tests pass; `uq_ai_operations_in_flight` exercised by an adapter-level test attempting two in-flight rows.
- **Risks:** Low.
- **Commit boundary:** `feat(api): add AiOperation and AgentAttempt aggregates and persistence`

## Out of scope for this session

Everything past Task 05: idempotency guard service (Task 06), the coordinator rewrite (Task 07B), human-edit/regenerate/retry/status endpoints (Tasks 08–10), legacy backfill and cleanup (Tasks 12–13) — those belong to Sessions A2–A4. Also out of scope, unchanged from the whole API packet: Organization/Membership, academic periods, sections, students, submissions, grading, results, publication, appeals; any change to `AssessmentStatus`; merging `Assessment` into a larger aggregate; a full credits/billing ledger; Kafka/queues/brokers/distributed locks; any change to `infra/`, `agents/`, or `web/`.

## Non-negotiable principles (reproduced from the ADRs — do not weaken these)

- **Revisions are immutable.** No `applyEdit`-equivalent. Every content change is an INSERT.
- **Current revision is `Assessment.currentRevisionId`**, never `MAX(version_number)`.
- **`AiOperation` has at most one `PENDING`/`IN_PROGRESS` row per `(assessmentId, operationType)`** — enforced structurally by `uq_ai_operations_in_flight`, not application logic alone.
- **No new `AssessmentStatus` values.** You are not touching `AssessmentStatus` in this session at all.

## TDD discipline

Every task above states its "Tests to write first." Follow RED → GREEN → REFACTOR: write the failing test against the target behavior, watch it fail, then implement, then refactor with the test green throughout.

## Migrations

- Only add `V13`, `V14`, `V15`, `V16` — never edit `V1`–`V12`. `V17` (legacy backfill) belongs to Session A4, not this session.
- Test every migration against Testcontainers/PostgreSQL, never H2.
- `assessment_drafts`/`agent_execution_logs` are untouched by this session — you are adding new tables alongside them, not migrating data yet.

## Testing

Unit tests for both new aggregate families' invariants and state transitions; repository/adapter round-trip tests via Testcontainers; direct-SQL tests for each new constraint. No integration test wiring these into a use case yet — there is no use case that calls them yet. Full matrix: [TEST-PLAN.md](TEST-PLAN.md).

## Security

- Never print or persist `GRADEOPS_GROQ_API_KEY`, `INTERNAL_API_SECRET`, Firebase credentials, provider API keys, database passwords, or tokens — in code, commit messages, test fixtures, logs, or your handoff. If you need to inspect Compose config, use `docker compose config --no-interpolate`, never plain `docker compose config`.
- No security-sensitive behavior changes in this session — no endpoint, no authorization path is touched.

## Commits

One commit per task (five commits total), at the exact boundary stated above. Before each commit: `git diff`, `git diff --cached`, `git status` — confirm only that task's files are touched.

## Handling discoveries

If a task turns out to be unimplementable as written, or the frozen contract in [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) is ambiguous: stop, do not silently adapt, record the discrepancy in [API-A1-HANDOFF.md](API-A1-HANDOFF.md)'s "Blockers" section with enough detail for Session A2 (or Session D) to resolve it, and continue with the parts unaffected. Do not invent scope to work around a gap.

## Verification before you consider this session done

```bash
./mvnw -f api/pom.xml clean test
```

Full suite green: the 289-test baseline plus every new test Tasks 01–05 require. Confirm `AssessmentRevision`, `AiOperation`, `AgentAttempt` are not referenced from any existing handler/controller — they must still be inert. `grep -rn "AssessmentRevision\|AiOperation\|AgentAttempt" api/src/main/java --include="*.java" -l` should show only the new files you just created, nothing in `application/usecase/` or `infrastructure/adapter/in/web/`.

## Push and handoff

```bash
git push -u origin feat/assessment-authoring-operation-foundation-api
```

Fill in [API-A1-HANDOFF.md](API-A1-HANDOFF.md) completely (every section — "None." where genuinely empty) and commit it as the last commit on this branch: `docs(api): record session A1 handoff`. Push again.

**Do not open a PR.** Do not merge anything. Session A2 continues on this same branch after reading your handoff.

## Handoff gate — what Session A2 checks before starting

Session A2 will not proceed if: this handoff is incomplete, its recorded HEAD doesn't match the branch's actual HEAD, its recorded test run wasn't actually green, or it flags a critical blocker. Make sure [API-A1-HANDOFF.md](API-A1-HANDOFF.md) is accurate enough that Session A2 can trust it without re-deriving your work from scratch.

## Final report to whoever invoked this session

Branch name and HEAD commit; each of the five tasks completed with its commit hash; full test count before/after; confirmation the new code is verifiably unreferenced by any productive path; any blocker recorded; confirmation `API-A1-HANDOFF.md` was pushed; the exact next command (`CLAUDE-API-A2-PROMPT.md`, given to a fresh session).

---

[← README](README.md) · [↑ Volver al inicio](#top)
