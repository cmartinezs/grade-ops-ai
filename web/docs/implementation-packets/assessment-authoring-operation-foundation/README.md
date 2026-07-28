<a id="top"></a>

# Web Execution Packet — Assessment Authoring Operation Foundation

**Status:** Ready for execution (Session C). **Parent:** [web/docs/](../../README.md)

Self-contained execution packet for the `web/` workspace's share of the Assessment Authoring Operation Foundation cut. A session opening only `web/` can execute this packet without reading the root coordination package or any other workspace's files.

## Files in this packet

| File | Purpose |
|---|---|
| [CLAUDE-IMPLEMENTATION-PROMPT.md](CLAUDE-IMPLEMENTATION-PROMPT.md) | The complete, standalone prompt for the implementing session — read this first |
| [TASKS.md](TASKS.md) | Task 11, full detail, against the actual current `assessment-creation` feature structure |
| [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) | The frozen API↔Web public contract, canonical status/failure-code taxonomies, new UI states |
| [TEST-PLAN.md](TEST-PLAN.md) | Test scope for this packet |
| [HANDOFF.md](HANDOFF.md) | Template to fill in and commit once this packet's work is done |

## Origin

This packet is one of three workspace-scoped packets produced from [`docs/implementation-packets/assessment-authoring-operation-foundation/`](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/README.md). If anything here appears to contradict an ADR or the plan, this packet has a defect — report it, do not silently reinterpret.

## A note on file paths

The governing plan's [05 — Web Migration](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/05-web-migration.md) cites `web/src/hooks/useIntakeAssessmentPage.ts` and `web/src/hooks/useAssessmentDraftBuilderPage.ts`. Verified against the current tree while drafting this packet, the actual paths are under `web/src/features/assessment-creation/hooks/`, not `web/src/hooks/` — the feature is organized as a self-contained `features/assessment-creation/` module (components, hooks, loaders, mappers, schemas). [TASKS.md](TASKS.md) uses the verified real paths throughout; treat the plan document's paths as directionally correct but not literally accurate.

---

[← web/docs/](../../README.md) · [Siguiente: CLAUDE-IMPLEMENTATION-PROMPT →](CLAUDE-IMPLEMENTATION-PROMPT.md) · [↑ Volver al inicio](#top)
