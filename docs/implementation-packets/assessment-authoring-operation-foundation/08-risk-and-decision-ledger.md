<a id="top"></a>

# 08 — Risk and Decision Ledger

**Parent:** [README](README.md) · **Status:** Ready · **Prev:** [07 — Integration and Final Verification](07-integration-and-final-verification.md) · **Next:** [09 — Session Handoff Protocol →](09-session-handoff-protocol.md)

## Infrastructure

```text
Infra implementation package: NOT_REQUIRED
Reason: No infrastructure changes are approved or needed for this cut. Every change in the
governing plan (docs/implementation-plans/assessment-authoring-operation-foundation/) is
additive Flyway migrations, new domain/application/infrastructure classes inside the existing
api/ Spring Boot service, and Web components/hooks inside the existing web/ Next.js app — all
deployed through the Cloud Run services Terraform already provisions
(infra/terraform/environments/demo/cloud_run.tf). No new service, no broker, no scheduler,
no new IAM binding, no new Secret Manager entry is introduced. Verified by inspecting every
.tf file under infra/terraform/environments/demo/ against every task in
08-implementation-sequence.md — none references a resource not already provisioned.
```

No `infra/` packet directory is created, per the coordination brief's own instruction not to manufacture artificial infra work.

## Residual risks carried forward from the ADRs and plan (not introduced or resolved by this packet)

These are reproduced, not reargued — splitting execution across workspaces does not change them, and no local packet may attempt to "solve" them by introducing distributed locks, a broker, a scheduler, or a coordinator service, which the original ADRs already explicitly rejected.

| Risk | Source | Still accepted after this split? |
|---|---|---|
| Narrow pre-check → commit race window can waste one LLM call under adversarial concurrency | [Idempotency and Concurrency Strategy § `expectedRevisionId`](../../99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md#expectedrevisionid--domain-meaningful-cas-checked-before-dispatch) | Yes — unchanged. No local packet introduces a lock manager. |
| `AgentAttempt` rows past the read-timeout threshold are classified `INDETERMINATE`, not reconciled by any job | [Durable AI Operation Model § Orphaned in-flight records](../../99-decisions/2026-07-28-durable-ai-operation-model.md#orphaned-in-flight-records-honest-residual-not-hidden) | Yes — unchanged. No local packet introduces a reconciliation scheduler. |
| No CI workflow currently runs `web/` | [Research 03 § 4](../../../design-system/research/research-03-baseline-technical-verification.md), reaffirmed in the plan's [09 — Risks and Rollback § Cross-cutting risks](../../implementation-plans/assessment-authoring-operation-foundation/09-risks-and-rollback.md#cross-cutting-risks) | Yes — pre-existing gap, out of scope. Session C and Session D must both run Web's verification commands manually; this is stated explicitly in both their prompts. |
| No feature flag for the cutover | [09 — Risks and Rollback § Rollout strategy](../../implementation-plans/assessment-authoring-operation-foundation/09-risks-and-rollback.md#rollout-strategy) | Yes — deliberate, justified choice, unchanged. Session D deploys API+Web together at the contract switch point (Tasks 10/11), per that same document. |
| A short-lived compatibility shim for `PATCH .../draft` is defensible only if truly simultaneous API+Web deploy is not operationally possible | Same document, per-task risk table, row "10 (API surface)" | Session D's call to make at integration time — see [05 — Execution Order § Rules during execution](05-execution-order.md#rules-during-execution). Session A does not decide this unilaterally. |

## Decisions made specifically to enable this workspace split

These are new, but scoped narrowly to *how the four sessions coordinate* — none of them touch the domain model, API contract, or database schema the ADRs already decided, and none require an ADR amendment.

| Decision | Options considered | Chosen | Why |
|---|---|---|---|
| Task 07 split | (a) Treat as one atomic API-only task, have Session A read `agents/` source directly and skip a separate Agents session for this cut; (b) split into 07A/07B/07C as in [04 — Task Distribution](04-task-distribution.md#task-07-split) | (b) | Keeps Session A's write access scoped to `api/` only (per its own restrictions), and gives Session B a real, honest, non-manufactured task instead of an empty packet |
| Git branch topology | (a) each session branches directly off `develop`; (b) an integration branch with three children, as in [09 — Session Handoff Protocol](09-session-handoff-protocol.md) | (b) | One functional PR to `develop` at the end, matching the coordination brief's explicit preference and avoiding three separate reviews for one coordinated cut |
| Pre-creating the three subrepo branches now, in this documentation-only session | (a) create them now off current `develop`; (b) document the exact commands, let the first session to start create them off a freshly-pulled `develop` | (b) | Branches created now would already be behind `develop` by the time any implementation session starts (no fixed timeline exists between this session and the next); pre-creating empty branches offers no benefit and one small staleness risk. Documented precisely instead — see [09](09-session-handoff-protocol.md#branch-topology) |
| Whether Web may start before API's Task 10 exists | (a) hard-block Web session start on Task 10; (b) allow Web to start against a frozen mock contract, gate only final verification | (b) | The public contract ([03](03-cross-workspace-api-contracts.md)) is already fully specified before any code exists; blocking Web's start would serialize two sessions that don't need to be serialized |

---

← [07 — Integration and Final Verification](07-integration-and-final-verification.md) | [↑ inicio](#top) | [Siguiente: Session Handoff Protocol →](09-session-handoff-protocol.md)
