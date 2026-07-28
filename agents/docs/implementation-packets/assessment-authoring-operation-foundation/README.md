<a id="top"></a>

# Agents Execution Packet — Assessment Authoring Operation Foundation

**Status:** Ready for execution (Session B). **Parent:** [agents/docs/](../../README.md)

Self-contained execution packet for the `agents/` workspace's share of the Assessment Authoring Operation Foundation cut. A session opening only `agents/` can execute this packet without reading the root coordination package or any other workspace's files.

## Scope note — read this before anything else

Unlike the API and Web packets, this packet's implementation scope is **deliberately narrow**. Research 01, Research 02, and the [Durable AI Operation Model ADR](../../../../docs/99-decisions/2026-07-28-durable-ai-operation-model.md) all independently conclude that `agents/`'s existing behavior already does almost everything this cut needs — correct provider/model resolution, a detailed failure taxonomy, correlation-id propagation, structured output. The defect this whole cut fixes is that `api/` discards what `agents/` already provides; the fix is overwhelmingly on the API side.

This packet's one real, verified task: `AgentExecutionLogPayload` (the response DTO `agents/` returns) does not currently carry the resolved `provider` name — only `model`. That is the entire functional scope. See [TASKS.md](TASKS.md) for the exact fields and file paths, already verified against current code, not assumed from the ADR's own hedged language ("verify against `AgentExecutionLogPayload` at implementation time").

## Files in this packet

| File | Purpose |
|---|---|
| [CLAUDE-IMPLEMENTATION-PROMPT.md](CLAUDE-IMPLEMENTATION-PROMPT.md) | The complete, standalone prompt for the implementing session — read this first |
| [TASKS.md](TASKS.md) | Task 07A, full detail |
| [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) | The API↔Agents contract this packet must satisfy, and what `agents/` explicitly does not own |
| [TEST-PLAN.md](TEST-PLAN.md) | Test scope for this packet |
| [HANDOFF.md](HANDOFF.md) | Template to fill in and commit once this packet's work is done |

## Origin

This packet is one of three workspace-scoped packets produced from [`docs/implementation-packets/assessment-authoring-operation-foundation/`](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/README.md). If anything here appears to contradict an ADR or the plan, this packet has a defect — report it, do not silently reinterpret.

---

[← agents/docs/](../../README.md) · [Siguiente: CLAUDE-IMPLEMENTATION-PROMPT →](CLAUDE-IMPLEMENTATION-PROMPT.md) · [↑ Volver al inicio](#top)
