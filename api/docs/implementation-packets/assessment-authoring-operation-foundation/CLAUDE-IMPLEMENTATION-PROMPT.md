<a id="top"></a>

# CLAUDE-IMPLEMENTATION-PROMPT — API: Assessment Authoring Operation Foundation

**Status:** Ready. **Parent:** [README](README.md)

Give this file, verbatim, as the task prompt to a fresh session that opens **only `api/`**. It is complete and self-contained — the session does not need to read the root coordination conversation, `agents/`, or `web/` to execute it.

## Who you are and what you are building

You are implementing the API-owned share of the **Assessment Authoring Operation Foundation** — a cut that replaces in-place-editable `AssessmentDraft` rows with immutable, provenance-tracked `AssessmentRevision`s, adds durable `AiOperation`/`AgentAttempt` evidence written *before* any external AI call, and makes every mutating authoring endpoint idempotent and optimistically concurrent. This closes real, reproduced defects: human edits today destroy AI provenance by overwriting the same row; a failed generation call leaves an assessment un-recoverable in the UI; a crash between "AI call succeeds" and "API persists the result" loses all evidence that the call ever happened.

**This is documentation-implementing work, not documentation-only.** Unlike the coordination session that produced this packet, you write real Java, real Flyway migrations, real tests.

## Authority — read in this order before writing any code

1. [Assessment Authoring Model](../../../../docs/99-decisions/2026-07-28-assessment-authoring-model.md)
2. [Authoring Operation Contract](../../../../docs/99-decisions/2026-07-28-authoring-operation-contract.md)
3. [Idempotency and Concurrency Strategy](../../../../docs/99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md)
4. [Durable AI Operation Model](../../../../docs/99-decisions/2026-07-28-durable-ai-operation-model.md)
5. [Assessment Authoring Operation Foundation plan](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/README.md) — all 11 documents, especially [02](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/02-domain-and-data-changes.md), [03](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/03-operation-and-api-contracts.md), [04](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/04-ai-operation-lifecycle.md), [06](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/06-database-migration.md), [08](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/08-implementation-sequence.md), [10](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md)
6. This packet's own [TASKS.md](TASKS.md), [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md), [TEST-PLAN.md](TEST-PLAN.md)

If anything here seems to contradict a document above it in this list, the higher document wins. Fix this packet by reporting the discrepancy (see [Handling discoveries](#handling-discoveries)) — do not silently follow your own interpretation over the ADR.

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
- Never implement directly on `develop` or on the bare integration branch — always on `feat/assessment-authoring-operation-foundation-api`.

## Baseline (run before your first commit)

```bash
./mvnw -f api/pom.xml clean test
```

Expected: 289+ tests pass (Research 03's recorded baseline). If this does not pass on a clean checkout of your starting commit, stop and report it — you are not responsible for fixing a pre-existing baseline failure, but you must not build on top of an unverified baseline.

## Scope

**In scope — implement these, and only these, in this session:** migrations V13–V17 (schema + legacy backfill); `AssessmentRevision`, `AiOperation`, `AgentAttempt` domain aggregates and persistence; the idempotency guard service; the rewritten `AiOperationCoordinator` (replacing `DraftGenerationCoordinator`); `CreateHumanRevisionHandler` (replacing `UpdateAssessmentDraftHandler`); `RegenerateAssessmentDraftHandler` updates for CAS; new retry/generation-status/revisions endpoints; removal of `PATCH .../draft`; deletion of `AssessmentDraft`/`AgentExecutionLog` and their now-superseded code paths. Full task-by-task detail: [TASKS.md](TASKS.md).

**Out of scope — do not add, even if it seems related:** Organization/Membership, academic periods, sections, students, participation, submissions, grading, academic results, publication, correction, appeals, batch processing, pedagogical analytics; any change to `AssessmentStatus` (zero new values, zero removed values, zero new transition methods); merging `Assessment` into a larger aggregate; a full credits/billing ledger (only `AgentAttempt`'s cost/usage evidence fields, already specified); Kafka, queues, message brokers, distributed locks, or any new microservice; any change to `infra/` (this cut needs none — see [08 — Risk and Decision Ledger](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/08-risk-and-decision-ledger.md#infrastructure) in the root packet); any change inside `agents/` or `web/` (those are Sessions B and C's work).

## Non-negotiable principles (reproduced from the ADRs — do not weaken these)

- **Revisions are immutable.** No `applyEdit`-equivalent. Every content change is an INSERT.
- **Current revision is `Assessment.currentRevisionId`**, never `MAX(version_number)`.
- **Durable evidence before dispatch.** The `AiOperation`/`AgentAttempt` row is committed in its own transaction *before* the HTTP call to `agents/`, not after.
- **Idempotency and optimistic concurrency are two different mechanisms.** `@Version`/`lockVersion` prevents lost updates between concurrent writers of the same row. Idempotency keys prevent duplicate effects from the same logical request submitted more than once. Implement both, do not substitute one for the other.
- **`expectedRevisionId` is checked before dispatch**, not only at persist time — this is what avoids wasting an LLM call on an already-obsolete request. The residual race between pre-check and commit is accepted, not eliminated (see [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) for `STALE_ON_COMPLETION`).
- **No new `AssessmentStatus` values.** Authoring state is a read model derived from `currentRevisionId` and the latest `AiOperation`, never a new column on `Assessment`.
- **AI output is a proposal.** No code path lets an `AgentAttempt` result become a revision without going through the same `AssessmentRevision` creation path a human edit also uses.

## TDD discipline

Every task in [TASKS.md](TASKS.md) states its "Tests to write first." Follow RED → GREEN → REFACTOR: write the failing test against the target behavior, watch it fail (or, where noted, watch it fail against the *old* code specifically — some tasks require the old assertion to be proven wrong before it's replaced), then implement, then refactor with the test green throughout.

## Migrations

- Only add new migrations (`V13` through `V17`, exact files and ordering in [TASKS.md](TASKS.md) and [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md)). Never edit `V1`–`V12`.
- Test every migration against Testcontainers/PostgreSQL, never H2 — this codebase's existing convention, unchanged.
- `V17` (legacy backfill) is honest, not optimistic: **every** migrated `assessment_drafts` row becomes `origin = LEGACY_UNKNOWN`, `provenanceComplete = false`, `actorId = NULL`, unconditionally — the plan's [06 — Database Migration](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/06-database-migration.md) explains in detail why a more optimistic labeling does not survive scrutiny of what the legacy schema actually proves. Do not re-derive a more "generous" labeling rule — the conservative one is the decision, not a starting hypothesis to improve on.
- `assessment_drafts`/`agent_execution_logs` tables are **not** dropped in this cut — read-only retention for one release, per the ADR.

## Testing

Full matrix in [TEST-PLAN.md](TEST-PLAN.md): unit, repository/adapter, integration, API acceptance, concurrency, idempotency, migration, failure-recovery. Use the existing Testcontainers/PostgreSQL pattern already in this codebase — no new test framework. Four regression guards are mandatory, not optional (see [TEST-PLAN.md § Specific regression guards](TEST-PLAN.md#specific-regression-guards-required-not-optional)) — the most important one reproduces Research 02 §5.6's exact failure scenario end-to-end.

## Security

- Never print or persist `GRADEOPS_GROQ_API_KEY`, `INTERNAL_API_SECRET`, Firebase credentials, provider API keys, database passwords, or tokens — in code comments, commit messages, test fixtures, logs you inspect, or this handoff. If you need to inspect Compose config, use `docker compose config --no-interpolate`, never plain `docker compose config`.
- `actorId` on every revision must come from the authenticated security context, never a client-supplied value.
- `Idempotency-Key` scope includes `teacherUid` for creation-type operations so keys cannot be replayed across teachers.
- Ownership checks (`OwnershipVerifier`) run before any CAS/idempotency check that would otherwise leak information about another teacher's assessment.

## Commits

One commit per task, at the exact boundary stated in [TASKS.md](TASKS.md) — do not batch multiple tasks into one commit, do not split one task's implementation from its own tests across two commits (Task 07 is explicitly called out as one commit including its tests). Before each commit: `git diff`, `git diff --cached`, `git status` — confirm only the files that task names are touched.

## Handling discoveries

If a task in [TASKS.md](TASKS.md) turns out to be unimplementable as written, or you find the frozen contract in [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) genuinely does not match what `agents/`'s current code actually returns (verify by reading, read-only, `agents/src/main/java/.../assessment/application/dto/` and `AssessmentAgentOrchestrator` — you may read `agents/` for this one verification, you may not write to it): stop, do not silently adapt, record the discrepancy in your `HANDOFF.md`'s "Blockers" section with enough detail for Session D to resolve it, and continue with the parts of the plan that are unaffected. Do not invent scope to work around a gap.

## Residual risks you are not expected to close

- The narrow pre-check-to-commit race that can waste one LLM call under adversarial concurrency — accepted, documented, not eliminated by a distributed lock.
- `INDETERMINATE` orphaned `AgentAttempt` classification is computed at read time only — no reconciliation job.

Do not introduce a lock manager, message broker, or scheduler to "fix" either of these — that is an explicitly rejected alternative in the ADRs.

## Verification before you consider this packet done

```bash
./mvnw -f api/pom.xml clean test
```

Full suite green (the pre-cut baseline plus every new test this packet's tasks require). Then: `grep -r "AssessmentDraft\|AgentExecutionLog" api/src` returns no matches outside migration SQL/history (after Task 13). Confirm every row in [TASKS.md](TASKS.md) has a passing named test, per the acceptance criteria each task cites from [10 — Acceptance Criteria](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md).

## Push and handoff

```bash
git push -u origin feat/assessment-authoring-operation-foundation-api
```

Fill in [HANDOFF.md](HANDOFF.md) completely (every section, "none" where genuinely empty) and commit it as the last commit on this branch: `docs(api): record assessment authoring foundation handoff`. Push again. Do not open a PR — Session D (integration) merges this branch and opens the one functional PR to `develop`.

## Final report to whoever invoked this session

State, in your own final message: branch name and HEAD commit, every task completed with its commit hash, full test count (before/after), any blocker recorded in `HANDOFF.md`, and confirmation that `HANDOFF.md` was pushed.

---

[← README](README.md) · [↑ Volver al inicio](#top)
