<a id="top"></a>

# Implementation Packets

Workspace-split execution documentation. An implementation packet takes one accepted [implementation plan](../implementation-plans/README.md) — already scoped to a single decision-backed cut of work — and divides it across the repository's independent workspaces (`api/`, `agents/`, `web/`, `infra/`) so that separate sessions, each opening only their own workspace, can execute in parallel without re-deriving cross-cutting decisions.

## What belongs here

- A packet set is created only after its governing implementation plan exists and its governing decisions are accepted in [`99-decisions/`](../99-decisions/README.md).
- A packet set does not repeat the plan's task-by-task detail — it assigns ownership, freezes shared contracts, defines execution order, and produces one self-contained prompt per workspace plus one integration prompt.
- A packet set implements nothing itself. Each local prompt is the reference the workspace-specific implementing session follows.

## Active packets

| Packet | Status | Governing plan |
|---|---|---|
| [Assessment Authoring Operation Foundation](assessment-authoring-operation-foundation/README.md) | Ready for distributed implementation | [Assessment Authoring Operation Foundation](../implementation-plans/assessment-authoring-operation-foundation/README.md) |

---

[← Documentation home](../README.md) · [Siguiente: Assessment Authoring Operation Foundation →](assessment-authoring-operation-foundation/README.md) · [↑ Volver al inicio](#top)
