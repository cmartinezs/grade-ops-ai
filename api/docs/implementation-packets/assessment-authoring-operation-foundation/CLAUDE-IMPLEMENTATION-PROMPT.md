<a id="top"></a>

# CLAUDE-IMPLEMENTATION-PROMPT — API: Assessment Authoring Operation Foundation (Orchestrator)

**Status:** Ready — orchestrator/index. **Parent:** [README](README.md)

**This file is not a prompt to execute directly.** It is the index and rationale for the four session-specific prompts that actually implement `api/`'s share of the Assessment Authoring Operation Foundation cut. If you were handed this file expecting to implement all 12 API tasks in one sitting: stop, do not do that — read [Why this is split](#why-this-is-split) below, then execute the correct numbered prompt for the session you're starting.

```text
Do not execute Tasks 01–13 in one session.

Execute:
1. CLAUDE-API-A1-PROMPT.md  — Schema and Inert Domain            (Tasks 01, 02, 03, 04, 05)
2. CLAUDE-API-A2-PROMPT.md  — Idempotency and Durable Coordinator (Tasks 06, 07B)
3. CLAUDE-API-A3-PROMPT.md  — Authoring Mutations and Public API  (Tasks 08, 09, 10)
4. CLAUDE-API-A4-PROMPT.md  — Backfill, Cleanup and Final Handoff (Tasks 12, 13)
```

## Why this is split

`api/` owns 12 of the plan's 14 tasks — far more than `agents/`'s 1 or `web/`'s 1. Holding all 12 in one session's context defeats the purpose of splitting execution by workspace in the first place: it reintroduces the large horizon, high cognitive load, and unrecoverable-mid-way risk the workspace split was meant to eliminate. Four sessions, each a natural boundary in the plan's own verticality (schema/domain → the coordinator pivot → the mutation/API surface → legacy/cleanup), keep each session's scope small, testable, and independently recoverable — if a session fails partway through, the prior session's handoff is a safe rollback point, not a half-finished 12-task diff.

## Who you are, at the orchestrator level

You are implementing the API-owned share of the **Assessment Authoring Operation Foundation** — a cut that replaces in-place-editable `AssessmentDraft` rows with immutable, provenance-tracked `AssessmentRevision`s, adds durable `AiOperation`/`AgentAttempt` evidence written *before* any external AI call, and makes every mutating authoring endpoint idempotent and optimistically concurrent. This closes real, reproduced defects: human edits today destroy AI provenance by overwriting the same row; a failed generation call leaves an assessment un-recoverable in the UI; a crash between "AI call succeeds" and "API persists the result" loses all evidence that the call ever happened. Each numbered session prompt restates this in its own scoped terms — you do not need this file's context to execute one of them.

## Same branch, four sessions

```text
feat/assessment-authoring-operation-foundation-api
```

One branch, not four. Session A1 creates it (or branches from the integration branch if it already exists — see its own Git preflight). Sessions A2, A3, A4 each continue on the **same** branch after reading the previous session's handoff — they do not create `-a1`/`-a2`/`-a3`/`-a4` suffixed branches. No force push, no destructive rebase, no amending a commit a prior session already pushed.

## Fresh sessions, mandatory handoff gate

Each of the four sessions is a **fresh** Claude session with no memory of the others — everything it needs is in its own prompt plus the previous session's committed handoff file. A session must not proceed past its own preflight if the previous session's handoff is missing, incomplete, records a HEAD that doesn't match the branch's actual HEAD, or flags a critical blocker. This is not a formality — re-running the previous session's own baseline test command yourself, rather than trusting a recorded test count, is an explicit step in every session's own prompt.

## Baseline at the start, push at the end — every session

Every one of the four sessions: runs `./mvnw -f api/pom.xml clean test` (or the session-specific equivalent) before its first commit, and pushes to `origin` at the end after committing its handoff file. No session opens a PR or merges anything — Session D (integration) does that once Agents and Web also report ready.

## The one cross-workspace dependency: Session A2 and Agents

Task 07B (inside Session A2) needs to know the exact field name/shape `agents/`'s response uses for the resolved provider, which is Agents' own Task 07A. The approved path is consuming Agents' completed handoff and its response fixture — not reading `agents/` source directly as the default. [CLAUDE-API-A2-PROMPT.md](CLAUDE-API-A2-PROMPT.md)'s [Prerequisites gate](CLAUDE-API-A2-PROMPT.md#prerequisites-gate-check-this-before-anything-else) covers this in full, including the documented read-only-inspection fallback for when Agents' handoff genuinely isn't available yet.

## Session A4 produces the final handoff

Sessions A1–A3 each produce their own intermediate handoff (`API-A1-HANDOFF.md`, `API-A2-HANDOFF.md`, `API-A3-HANDOFF.md`) — a per-session recovery record, not the document Session D reads. **Session A4 alone** produces both its own `API-A4-HANDOFF.md` and the **consolidated** [HANDOFF.md](HANDOFF.md), which summarizes all four sessions into the one document Session D treats as authoritative.

## Authority, non-negotiable principles, and everything else — restated per session, not here

Full authority reading order, exact scope/out-of-scope, the ADRs' non-negotiable principles (immutable revisions, `currentRevisionId` never `MAX(version_number)`, durable evidence before dispatch, idempotency vs. concurrency as distinct mechanisms, `expectedRevisionId` pre-dispatch checks, frozen `AssessmentStatus`, AI-as-proposal-only), TDD discipline, migration rules, testing matrix, security requirements, commit discipline, discovery-handling, and residual-risk acknowledgment are **restated in full, tailored to its own scope**, inside each of [CLAUDE-API-A1-PROMPT.md](CLAUDE-API-A1-PROMPT.md), [CLAUDE-API-A2-PROMPT.md](CLAUDE-API-A2-PROMPT.md), [CLAUDE-API-A3-PROMPT.md](CLAUDE-API-A3-PROMPT.md), and [CLAUDE-API-A4-PROMPT.md](CLAUDE-API-A4-PROMPT.md). None of it was dropped in this restructuring — it moved from this one file into the four session-specific ones so each session is genuinely self-contained.

## Reference documents (used by all four sessions)

- [TASKS.md](TASKS.md) — the single authoritative task definition for all 12 tasks (IDs, objectives, dependencies, commit boundaries, acceptance criteria unchanged by the session split — see its own Session column for the A1–A4 mapping)
- [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) — field-level domain/persistence reference, API↔Agents contract, API↔Web public contract
- [TEST-PLAN.md](TEST-PLAN.md) — full test-type matrix and mandatory regression guards

## Final report — at the orchestrator level

There is no single "final report" for this file, since it is never executed directly. Each session prompt states exactly what its own session must report. The API packet as a whole is done only when Session A4 reports the consolidated [HANDOFF.md](HANDOFF.md) pushed and all 25 acceptance criteria this packet owns (per [06 — Acceptance Criteria Ownership](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/06-acceptance-criteria-ownership.md)) have a passing, named test somewhere across the four sessions' commits.

---

[← README](README.md) · [↑ Volver al inicio](#top)
