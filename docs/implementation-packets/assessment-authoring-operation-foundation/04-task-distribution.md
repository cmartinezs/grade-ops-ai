<a id="top"></a>

# 04 — Task Distribution

**Parent:** [README](README.md) · **Status:** Ready · **Prev:** [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md) · **Next:** [05 — Execution Order →](05-execution-order.md)

Full task detail (steps, tests-first, verification commands, acceptance criteria, migration considerations, risks, commit boundaries) is not repeated here — it is reproduced in full inside each local packet's `TASKS.md`, scoped to that workspace's tasks only. This document is the traceability map back to [08 — Implementation Sequence](../../implementation-plans/assessment-authoring-operation-foundation/08-implementation-sequence.md).

## API tasks (Session A, split into four sequential sub-sessions A1–A4)

`api/` owns 12 of the plan's 14 tasks — enough that one session holding all 12 would reintroduce the large horizon and unrecoverable-mid-way risk this whole workspace split exists to eliminate. Session A is therefore itself split into four sequential, recoverable sub-sessions on **one branch** (`feat/assessment-authoring-operation-foundation-api`), each with its own self-contained prompt and handoff in the [API local packet](../../../api/docs/implementation-packets/assessment-authoring-operation-foundation/README.md). This is an *execution-sequencing* decision only — it does not change any task's ID, objective, dependency, commit boundary, or acceptance criterion; the table below is identical in content to before, with one added column.

| Task | Title | Sub-session | Commit boundary (from the plan, unchanged) |
|---|---|---|---|
| 01 | Schema: `ai_operations`/`agent_attempts` | A1 | `feat(api): add durable ai_operations/agent_attempts schema` |
| 02 | Schema: `assessment_revisions` + pointer | A1 | `feat(api): add assessment_revisions schema and current-revision pointer` |
| 03 | Schema: `idempotency_records` | A1 | `feat(api): add idempotency_records schema` |
| 04 | Domain: `AssessmentRevision` aggregate | A1 | `feat(api): add AssessmentRevision aggregate and persistence` |
| 05 | Domain: `AiOperation`/`AgentAttempt` aggregates | A1 | `feat(api): add AiOperation and AgentAttempt aggregates and persistence` |
| 06 | Application: idempotency guard | A2 | `feat(api): add reusable idempotency guard service` |
| 07B | Rewrite generation coordinator | A2 | `feat(api): switch initial generation to durable AiOperation/AgentAttempt coordinator` |
| 08 | Human edit → revision | A3 | `feat(api): human edits create immutable revisions instead of mutating in place` |
| 09 | Regenerate requires `expectedRevisionId` | A3 | `feat(api): regenerate requires expectedRevisionId and persists adjustment reason` |
| 10 | API surface: retry/status/revisions endpoints | A3 | `feat(api): add retry/generation-status/revisions endpoints, remove in-place draft PATCH` |
| 12 | Legacy data backfill (V17) | A4 | `feat(api): backfill legacy assessment_drafts/agent_execution_logs into revision/operation model` |
| 13 | Cleanup dead legacy code | A4 | `refactor(api): remove superseded AssessmentDraft/AgentExecutionLog code paths` |

12 tasks, 12 commits (plus one handoff commit per sub-session — five total, since A4 commits both its own and the consolidated handoff together), executed in numeric order across four sub-sessions: **A1** (01→02→03→04→05) → **A2** (06→07B) → **A3** (08→09→10) → **A4** (12→13). Each sub-session boundary is a handoff gate — see [09 — Session Handoff Protocol § API sub-session handoffs](09-session-handoff-protocol.md#api-sub-session-handoffs). Task 07B (inside A2) specifically cannot start until 07A's outcome (does `provider` already exist on the response payload, yes/no) is known — see [Task 07 split](#task-07-split) below.

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
| **07B** | API (sub-session A2) | Replace `DraftGenerationCoordinator` with the three-phase `AiOperationCoordinator` per [04 — AI Operation Lifecycle](../../implementation-plans/assessment-authoring-operation-foundation/04-ai-operation-lifecycle.md), persisting `response.log().provider()`/`response.log().model()` as `AgentAttempt.resolvedProvider`/`resolvedModel`. | 07A's outcome (the field name/shape API deserializes must match what 07A produced) and API sub-session A1's handoff |
| **07C — integration exit criterion** | INTEGRATION (verified by whichever of A/B lands second, then re-verified by Session D) | A passing contract test in `api/` proving the API's `agentclient` module correctly deserializes the `provider` field from a fixture matching Agents' actual response shape — not a hand-rolled JSON string that happens to work. | 07A and 07B both landed |

**Why 07A has no hard dependency on API migrations:** Task 07A only touches `agents/`'s response DTO and its own test suite — it needs zero rows in `ai_operations`/`assessment_revisions` to exist. This is why Session B can start immediately once this packet is frozen, in parallel with Session A's Tasks 01–06, rather than waiting.

**Why 07B is blocked on 07A specifically (not on all of Session B):** 07B needs to know the exact field name and JSON shape before writing the deserialization code, and needs to know whether it's a rename (breaking) or an addition (additive) before writing a migration/compatibility note. If API sub-session A2 reaches Task 07B before Session B has resolved 07A, A2 must pause 07B and either (a) coordinate directly with Session B's output (the approved default — consume `AGENTS-HANDOFF.md`'s response fixture), or (b), only as a documented contingency, independently verify the current `agents/` response shape via read-only inspection of `agents/src/main/java/.../assessment/application/dto/` (permitted — A2 may *read* `agents/` source for this one verification, it may not *write* to `agents/`). Sub-sessions A1, A3, and A4 have no dependency on Agents at all.

---

← [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md) | [↑ inicio](#top) | [Siguiente: Execution Order →](05-execution-order.md)
