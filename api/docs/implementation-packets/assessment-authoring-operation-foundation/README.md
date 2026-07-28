<a id="top"></a>

# API Execution Packet — Assessment Authoring Operation Foundation

**Status:** Ready for execution (Session A). **Parent:** [api/docs/](../../README.md)

Self-contained execution packet for the `api/` workspace's share of the Assessment Authoring Operation Foundation cut. A session opening only `api/` can execute this packet without reading the root coordination package or any other workspace's files.

## Files in this packet

| File | Purpose |
|---|---|
| [CLAUDE-IMPLEMENTATION-PROMPT.md](CLAUDE-IMPLEMENTATION-PROMPT.md) | The complete, standalone prompt for the implementing session — read this first |
| [TASKS.md](TASKS.md) | All 12 API-owned tasks, full detail: objective, files, dependencies, steps, tests-first, verification command, acceptance criteria, risks, commit boundary |
| [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) | Domain/persistence field-level reference, API↔Agents contract, API↔Web public contract — all scoped to what `api/` must implement or consume |
| [TEST-PLAN.md](TEST-PLAN.md) | Test-type matrix and the mandatory regression guards for `api/` |
| [HANDOFF.md](HANDOFF.md) | Template to fill in and commit once this packet's work is done |

## Origin

This packet is one of three workspace-scoped packets produced from [`docs/implementation-packets/assessment-authoring-operation-foundation/`](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/README.md), itself built on top of [`docs/implementation-plans/assessment-authoring-operation-foundation/`](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/README.md) and the four [2026-07-28 ADRs](../../../../docs/99-decisions/README.md#active-decision-records). If anything in this packet appears to contradict one of those, the ADR/plan wins — this packet has a defect, report it, do not silently reinterpret.

---

[← api/docs/](../../README.md) · [Siguiente: CLAUDE-IMPLEMENTATION-PROMPT →](CLAUDE-IMPLEMENTATION-PROMPT.md) · [↑ Volver al inicio](#top)
