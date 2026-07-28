<a id="top"></a>

# 04 — Task Distribution

**Parent:** [README](README.md) · **Status:** Ready · **Prev:** [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md) · **Next:** [05 — Execution Order →](05-execution-order.md)

Full task detail (steps, tests-first, verification commands, acceptance criteria, migration considerations, risks, commit boundaries) is not repeated here — it is reproduced in full inside each local packet's `TASKS.md`, scoped to that workspace's tasks only. This document is the traceability map back to [08 — Implementation Sequence](../../implementation-plans/assessment-authoring-operation-foundation/08-implementation-sequence.md).

## API tasks (Session A)

| Task | Title | Commit boundary (from the plan, unchanged) |
|---|---|---|
| 01 | Schema: `ai_operations`/`agent_attempts` | `feat(api): add durable ai_operations/agent_attempts schema` |
| 02 | Schema: `assessment_revisions` + pointer | `feat(api): add assessment_revisions schema and current-revision pointer` |
| 03 | Schema: `idempotency_records` | `feat(api): add idempotency_records schema` |
| 04 | Domain: `AssessmentRevision` aggregate | `feat(api): add AssessmentRevision aggregate and persistence` |
| 05 | Domain: `AiOperation`/`AgentAttempt` aggregates | `feat(api): add AiOperation and AgentAttempt aggregates and persistence` |
| 06 | Application: idempotency guard | `feat(api): add reusable idempotency guard service` |
| 07B | Rewrite generation coordinator | `feat(api): switch initial generation to durable AiOperation/AgentAttempt coordinator` |
| 08 | Human edit → revision | `feat(api): human edits create immutable revisions instead of mutating in place` |
| 09 | Regenerate requires `expectedRevisionId` | `feat(api): regenerate requires expectedRevisionId and persists adjustment reason` |
| 10 | API surface: retry/status/revisions endpoints | `feat(api): add retry/generation-status/revisions endpoints, remove in-place draft PATCH` |
| 12 | Legacy data backfill (V17) | `feat(api): backfill legacy assessment_drafts/agent_execution_logs into revision/operation model` |
| 13 | Cleanup dead legacy code | `refactor(api): remove superseded AssessmentDraft/AgentExecutionLog code paths` |

12 tasks, 12 commits, executed in numeric order (01→02→03→04→05→06→07B→08→09→10→12→13). Task 07B specifically cannot start until 07A's outcome (does `provider` already exist on the response payload, yes/no) is known — see [Task 07 split](#task-07-split) below.

## Agents tasks (Session B)

| Task | Title | Commit boundary |
|---|---|---|
| 07A | Verify/add `provider` field on the Agents→API response payload | `feat(agents): add resolved provider field to assessment generation response` (only if the field is missing — see below) |

If verification finds the field already present, Session B's deliverable is the verification itself plus a contract test/fixture proving it, not a code change — see the Agents local packet's `TASKS.md` for both branches.

## Web tasks (Session C)

| Task | Title | Commit boundary |
|---|---|---|
| 11 | Web migration to the new authoring contract | `feat(web): migrate authoring flow to idempotent, resumable, revision-aware contract` |

## Integration task (Session D)

| Task | Title | Commit boundary |
|---|---|---|
| 14 | Full regression + docs sync | `docs(developer-guide): sync API reference and database guide with authoring operation foundation` |

Task 14 additionally covers the merge/verification/PR work described in [07 — Integration and Final Verification](07-integration-and-final-verification.md) and [FINAL-INTEGRATION-PROMPT.md](FINAL-INTEGRATION-PROMPT.md) — it is not only the docs-sync commit above, that commit is simply the last content commit the plan itself defines for this task.

## Task 07 split

The plan's Task 07 ("rewrite the generation coordinator — the pivot task") touches both workspaces. It is split, per [§16 of the coordination brief](00-coordination-overview.md), into three parts sharing one integration exit criterion:

| Subtask | Owner | Deliverable | Depends on |
|---|---|---|---|
| **07A** | AGENTS | Confirm whether `AssessmentAgentResponse.Log` (or `AgentExecutionLogPayload`, whichever currently exists — verify against `agents/` source) already carries a `provider` field alongside `model`. If yes: document it as already-satisfied in `AGENTS-HANDOFF.md`, add one assertion to the existing contract test proving it. If no: add the field additively (no rename, no breaking change to any existing consumer of the response), update the corresponding test. | none — can start as soon as this coordination packet is frozen |
| **07B** | API | Replace `DraftGenerationCoordinator` with the three-phase `AiOperationCoordinator` per [04 — AI Operation Lifecycle](../../implementation-plans/assessment-authoring-operation-foundation/04-ai-operation-lifecycle.md), persisting `response.log().provider()`/`response.log().model()` as `AgentAttempt.resolvedProvider`/`resolvedModel`. | 07A's outcome (the field name/shape API deserializes must match what 07A produced) |
| **07C — integration exit criterion** | INTEGRATION (verified by whichever of A/B lands second, then re-verified by Session D) | A passing contract test in `api/` proving the API's `agentclient` module correctly deserializes the `provider` field from a fixture matching Agents' actual response shape — not a hand-rolled JSON string that happens to work. | 07A and 07B both landed |

**Why 07A has no hard dependency on API migrations:** Task 07A only touches `agents/`'s response DTO and its own test suite — it needs zero rows in `ai_operations`/`assessment_revisions` to exist. This is why Session B can start immediately once this packet is frozen, in parallel with Session A's Tasks 01–06, rather than waiting.

**Why 07B is blocked on 07A specifically (not on all of Session B):** 07B needs to know the exact field name and JSON shape before writing the deserialization code, and needs to know whether it's a rename (breaking) or an addition (additive) before writing a migration/compatibility note. If Session A reaches Task 07 before Session B has resolved 07A, Session A must pause 07B and either (a) coordinate directly with Session B's output, or (b) independently verify the current `agents/` response shape via read-only inspection of `agents/src/main/java/.../assessment/application/dto/` (permitted — Session A may *read* `agents/` source for this one verification, it may not *write* to `agents/`).

---

← [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md) | [↑ inicio](#top) | [Siguiente: Execution Order →](05-execution-order.md)
