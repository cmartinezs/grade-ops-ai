# Implementation Plans

Task-by-task technical implementation plans for a specific, decision-backed cut of work. Each plan turns a set of accepted [decision records](../99-decisions/README.md) into an executable sequence — aggregates, persistence, API contracts, tests, and a task-by-task rollout — without re-litigating the decisions themselves.

## What belongs here

- A plan is created only after the decisions it depends on are accepted in `99-decisions/`.
- A plan targets one bounded cut of work (a "foundation," a slice, a scoped release-adjacent increment) — not the whole product roadmap. Whole-product release sequencing lives in [`../master-plan/`](../master-plan/README.md), which this folder does not replace or duplicate.
- A plan does not implement anything itself — it is the reference an implementing agent follows task by task.

## Active plans

| Plan | Status | Governing decisions |
|---|---|---|
| [Assessment Authoring Operation Foundation](assessment-authoring-operation-foundation/README.md) | Planned — not yet implemented | [2026-07-28 ADRs](../99-decisions/README.md#active-decision-records) (Assessment Authoring Model, Authoring Operation Contract, Idempotency and Concurrency Strategy, Durable AI Operation Model) |

---

[← Documentation home](../README.md)
