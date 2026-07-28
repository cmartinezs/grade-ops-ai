<a id="top"></a>

# API Execution Packet — Assessment Authoring Operation Foundation

**Status:** Ready for execution (Session A, split into A1–A4). **Parent:** [api/docs/](../../README.md)

Self-contained execution packet for the `api/` workspace's share of the Assessment Authoring Operation Foundation cut. A session opening only `api/` can execute this packet without reading the root coordination package or any other workspace's files.

## API is four sequential, recoverable sessions on one branch

`api/` owns 12 of the plan's 14 tasks — too many for one session's context without reintroducing the large horizon and unrecoverable-mid-way risk the workspace split was meant to eliminate. [CLAUDE-IMPLEMENTATION-PROMPT.md](CLAUDE-IMPLEMENTATION-PROMPT.md) is now an **orchestrator/index**, not something to execute directly — it points to four self-contained session prompts, each with its own scope, preflight, and handoff:

| Session | Scope | Tasks | Prompt | Handoff |
|---|---|---|---|---|
| A1 | Schema and Inert Domain | 01, 02, 03, 04, 05 | [CLAUDE-API-A1-PROMPT.md](CLAUDE-API-A1-PROMPT.md) | [API-A1-HANDOFF.md](API-A1-HANDOFF.md) |
| A2 | Idempotency and Durable Coordinator | 06, 07B | [CLAUDE-API-A2-PROMPT.md](CLAUDE-API-A2-PROMPT.md) | [API-A2-HANDOFF.md](API-A2-HANDOFF.md) |
| A3 | Authoring Mutations and Public API | 08, 09, 10 | [CLAUDE-API-A3-PROMPT.md](CLAUDE-API-A3-PROMPT.md) | [API-A3-HANDOFF.md](API-A3-HANDOFF.md) |
| A4 | Backfill, Cleanup and Final Handoff | 12, 13 | [CLAUDE-API-A4-PROMPT.md](CLAUDE-API-A4-PROMPT.md) | [API-A4-HANDOFF.md](API-A4-HANDOFF.md) + consolidated [HANDOFF.md](HANDOFF.md) |

All four sessions share the same branch (`feat/assessment-authoring-operation-foundation-api`) and exchange explicit handoff files — a session will not proceed if the previous one's handoff is missing, incomplete, or records a HEAD that doesn't match the branch's actual state. See [CLAUDE-IMPLEMENTATION-PROMPT.md § Why this is split](CLAUDE-IMPLEMENTATION-PROMPT.md#why-this-is-split) for the full reasoning.

## Files in this packet

| File | Purpose |
|---|---|
| [CLAUDE-IMPLEMENTATION-PROMPT.md](CLAUDE-IMPLEMENTATION-PROMPT.md) | Orchestrator/index — explains the A1–A4 split, do not execute directly |
| [CLAUDE-API-A1-PROMPT.md](CLAUDE-API-A1-PROMPT.md) | Self-contained prompt for Session A1 |
| [CLAUDE-API-A2-PROMPT.md](CLAUDE-API-A2-PROMPT.md) | Self-contained prompt for Session A2 |
| [CLAUDE-API-A3-PROMPT.md](CLAUDE-API-A3-PROMPT.md) | Self-contained prompt for Session A3 |
| [CLAUDE-API-A4-PROMPT.md](CLAUDE-API-A4-PROMPT.md) | Self-contained prompt for Session A4 |
| [TASKS.md](TASKS.md) | All 12 API-owned tasks, full detail: objective, files, dependencies, steps, tests-first, verification command, acceptance criteria, risks, commit boundary — now tagged with owning session (A1–A4), unchanged otherwise |
| [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) | Domain/persistence field-level reference, API↔Agents contract, API↔Web public contract — all scoped to what `api/` must implement or consume |
| [TEST-PLAN.md](TEST-PLAN.md) | Test-type matrix and the mandatory regression guards for `api/` |
| [API-A1-HANDOFF.md](API-A1-HANDOFF.md) | Handoff template for Session A1 → A2 |
| [API-A2-HANDOFF.md](API-A2-HANDOFF.md) | Handoff template for Session A2 → A3 |
| [API-A3-HANDOFF.md](API-A3-HANDOFF.md) | Handoff template for Session A3 → A4 (also the contract artifact Web consumes) |
| [API-A4-HANDOFF.md](API-A4-HANDOFF.md) | Handoff template for Session A4's own two tasks |
| [HANDOFF.md](HANDOFF.md) | **Final, consolidated** handoff template — filled in by Session A4, summarizing all four sessions; this is the document Session D reads |

## Origin

This packet is one of three workspace-scoped packets produced from [`docs/implementation-packets/assessment-authoring-operation-foundation/`](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/README.md), itself built on top of [`docs/implementation-plans/assessment-authoring-operation-foundation/`](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/README.md) and the four [2026-07-28 ADRs](../../../../docs/99-decisions/README.md#active-decision-records). If anything in this packet appears to contradict one of those, the ADR/plan wins — this packet has a defect, report it, do not silently reinterpret. The A1–A4 session split does not change any task's ID, objective, dependency, commit boundary, or acceptance criterion — it only changes how execution is sequenced across sessions.

---

[← api/docs/](../../README.md) · [Siguiente: CLAUDE-IMPLEMENTATION-PROMPT →](CLAUDE-IMPLEMENTATION-PROMPT.md) · [↑ Volver al inicio](#top)
