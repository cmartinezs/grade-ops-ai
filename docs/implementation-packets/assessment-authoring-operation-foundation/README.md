<a id="top"></a>

# Implementation Packets — Assessment Authoring Operation Foundation

**Status:** Ready for distributed implementation. **Parent:** [docs/](../../README.md) → [Implementation Packets](../README.md)

## What this is

This is the **workspace-split execution layer** on top of [`docs/implementation-plans/assessment-authoring-operation-foundation/`](../../implementation-plans/assessment-authoring-operation-foundation/README.md). That plan defines *what* to build, task by task, against one unified repository view. This packet set defines *who* — which of `api/`, `agents/`, `web/` — executes each task, in what order, against what frozen shared contract, so that four independent sessions (API, Agents, Web, Integration) can each open only their own workspace and execute without re-deriving the cross-cutting decisions.

**This packet set implements nothing.** It is coordination and handoff documentation only. No migration, domain class, endpoint, or component exists yet as a result of this packet — see [08 — Risk and Decision Ledger](08-risk-and-decision-ledger.md) for what remains explicitly open, and the local packets below for what each future session must build.

## Authority

Nothing here may reinterpret or contradict:

1. The four [2026-07-28 ADRs](../../99-decisions/README.md#active-decision-records).
2. The [Assessment Authoring Operation Foundation plan](../../implementation-plans/assessment-authoring-operation-foundation/README.md).

If a document below appears to contradict either, the ADR/plan wins — treat the mismatch as a defect in this packet, not a reinterpretation license, and fix the packet.

## Root documents

| # | Document | Purpose |
|---|---|---|
| 00 | [Coordination Overview](00-coordination-overview.md) | Why the plan is split, what each session (A/B/C/D) does, big picture |
| 01 | [Workspace Responsibility Matrix](01-workspace-responsibility-matrix.md) | Which workspace owns which of the plan's 14 tasks; authoritative responsibility boundaries |
| 02 | [Shared Domain Contract](02-shared-domain-contract.md) | Canonical concept ownership table — one definition per concept, no per-workspace reinterpretation |
| 03 | [Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md) | API↔Agents internal contract, API↔Web public contract, canonical failure-code and status taxonomies |
| 04 | [Task Distribution](04-task-distribution.md) | The 14 plan tasks assigned to a workspace, with cross-workspace tasks split into lettered subtasks |
| 05 | [Execution Order](05-execution-order.md) | Phases 0–7, session start/completion gates |
| 06 | [Acceptance Criteria Ownership](06-acceptance-criteria-ownership.md) | The plan's 25 criteria, each assigned a primary owner and test |
| 07 | [Integration and Final Verification](07-integration-and-final-verification.md) | What Session D checks before opening the functional PR; the alignment table run during this coordination session, to be re-run at integration time |
| 08 | [Risk and Decision Ledger](08-risk-and-decision-ledger.md) | Residual risks carried from the ADRs/plan, plus decisions made specifically to split execution (branch strategy source, infra disposition) |
| 09 | [Session Handoff Protocol](09-session-handoff-protocol.md) | `*-HANDOFF.md` format, git branch topology, when each session pushes/opens what |
| — | [FINAL-INTEGRATION-PROMPT.md](FINAL-INTEGRATION-PROMPT.md) | Self-contained prompt for Session D (integration) |

## Local execution packets (self-contained — open only the listed workspace)

| Workspace | Path | Session |
|---|---|---|
| API | [`api/docs/implementation-packets/assessment-authoring-operation-foundation/`](../../../api/docs/implementation-packets/assessment-authoring-operation-foundation/README.md) | Session A — split into four sequential sub-sessions **A1–A4**, one branch, each with its own self-contained prompt (`CLAUDE-API-A{1,2,3,4}-PROMPT.md`) and handoff — see that packet's README |
| Agents | [`agents/docs/implementation-packets/assessment-authoring-operation-foundation/`](../../../agents/docs/implementation-packets/assessment-authoring-operation-foundation/README.md) | Session B |
| Web | [`web/docs/implementation-packets/assessment-authoring-operation-foundation/`](../../../web/docs/implementation-packets/assessment-authoring-operation-foundation/README.md) | Session C |
| Infra | Not created — see [08 — Risk and Decision Ledger § Infrastructure](08-risk-and-decision-ledger.md#infrastructure) | N/A |

Each local packet's `CLAUDE-IMPLEMENTATION-PROMPT.md` is a complete, standalone prompt — a session opening only `api/`, `agents/`, or `web/` can execute it without reading this root package or the original conversation that produced it. **Exception:** API's `CLAUDE-IMPLEMENTATION-PROMPT.md` is now an orchestrator/index pointing to the four A1–A4 prompts, since holding all 12 API tasks in one session's context would reintroduce the load this whole packet set exists to avoid — see [00 — Coordination Overview § Session A is four sub-sessions, not one](00-coordination-overview.md#session-a-is-four-sub-sessions-not-one).

## Governing sources

- [Assessment Authoring Model](../../99-decisions/2026-07-28-assessment-authoring-model.md)
- [Authoring Operation Contract](../../99-decisions/2026-07-28-authoring-operation-contract.md)
- [Idempotency and Concurrency Strategy](../../99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md)
- [Durable AI Operation Model](../../99-decisions/2026-07-28-durable-ai-operation-model.md)
- [Assessment Authoring Operation Foundation plan](../../implementation-plans/assessment-authoring-operation-foundation/README.md) (all 11 documents)
- [Research 03 — Baseline Technical Verification](../../../design-system/research/research-03-baseline-technical-verification.md)

## Security note for every downstream session

Never print or persist `GRADEOPS_GROQ_API_KEY`, `INTERNAL_API_SECRET`, Firebase credentials, provider API keys, database passwords, or tokens — in documentation, commits, prompts, PR bodies, terminal transcripts, examples, or fixtures. If Compose needs inspecting, use `docker compose config --no-interpolate` or read the files directly, never `docker compose config` with live interpolation. This restriction is reproduced verbatim in every local `CLAUDE-IMPLEMENTATION-PROMPT.md`.

---

[← Documentation home](../../README.md) · [Siguiente: Coordination Overview →](00-coordination-overview.md) · [↑ Volver al inicio](#top)
