<a id="top"></a>

# 00 — Coordination Overview

**Parent:** [README](README.md) · **Status:** Ready · **Next:** [01 — Workspace Responsibility Matrix →](01-workspace-responsibility-matrix.md)

## Why split

The [Assessment Authoring Operation Foundation plan](../../implementation-plans/assessment-authoring-operation-foundation/README.md) defines 14 tasks against one unified view of the repository. In practice, `api/`, `agents/`, and `web/` are independently buildable, independently testable Maven/npm workspaces with separate CI-relevant commands (`./mvnw -f api/pom.xml test`, `./mvnw -f agents/pom.xml -Pdemo test`, `npm run lint && npm run test && npm run build` in `web/`). A single session holding all three in context at once is unnecessary and wastes the very isolation those workspaces already provide. Splitting lets:

- an API session execute Tasks 01–10, 12, 13 without ever needing `agents/`'s or `web/`'s source;
- an Agents session verify/extend one small, already-mostly-correct contract surface without touching persistence, migrations, or Web;
- a Web session build against a frozen, documented target contract without waiting for the API implementation to physically exist;
- an integration session do exactly one job — verify the three landed correctly together — without re-implementing any of them.

## The four sessions

| Session | Opens | Executes | Cannot do |
|---|---|---|---|
| **A — API** | `api/` only | Tasks 01–10, 12, 13 (see [04 — Task Distribution](04-task-distribution.md)) | Implement Web or Agents; redesign the API↔Agents contract unilaterally |
| **B — Agents** | `agents/` only | Task 07A (verify/add the additive `provider` field on `AssessmentAgentResponse.Log`) and its contract tests | Persist API domain state; decide idempotency/business rules; implement Web |
| **C — Web** | `web/` only | Task 11 | Redesign visually beyond wiring existing components to new states; invent API contract fields not in [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md) |
| **D — Integration** | All of `api/`, `agents/`, `web/` | Task 14, plus merge/regression/PR | Rewrite any of A/B/C's implementations arbitrarily — it verifies handoffs, it does not redesign them |

This mapping is deliberately close to 1:1 with the plan's own verticality note (08-implementation-sequence.md): "tasks 1–5 build new, unused-by-anyone infrastructure... task 6 is the pivot... tasks 7–11 extend the switch... tasks 12–14 handle legacy data, cleanup, and final regression." The split does not change that ordering — it assigns who does which slice of it.

## What is frozen before any session starts

Everything in [02 — Shared Domain Contract](02-shared-domain-contract.md) and [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md) is decided by this coordination package, sourced directly from the four ADRs and the plan. No downstream session may rename a field, invent a status value, or redefine a failure code. If a downstream session finds the frozen contract genuinely unimplementable, it stops and reports the blocker — it does not silently adapt around it (see each local packet's "Handling discoveries" section).

## Reading order for this root package

[01](01-workspace-responsibility-matrix.md) (who owns what) → [02](02-shared-domain-contract.md) (shared vocabulary) → [03](03-cross-workspace-api-contracts.md) (shared contracts) → [04](04-task-distribution.md) (task-by-task assignment) → [05](05-execution-order.md) (sequencing and gates) → [06](06-acceptance-criteria-ownership.md) (criteria ownership) → [07](07-integration-and-final-verification.md) (how Session D closes the loop) → [08](08-risk-and-decision-ledger.md) (residual risk + packet-specific decisions) → [09](09-session-handoff-protocol.md) (handoff format, git strategy).

---

← [README](README.md) | [↑ inicio](#top) | [Siguiente: Workspace Responsibility Matrix →](01-workspace-responsibility-matrix.md)
