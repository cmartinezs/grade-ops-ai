<a id="top"></a>

# 05 — Execution Order

**Parent:** [README](README.md) · **Status:** Ready · **Prev:** [04 — Task Distribution](04-task-distribution.md) · **Next:** [06 — Acceptance Criteria Ownership →](06-acceptance-criteria-ownership.md)

## Phases

```text
Phase 0 — Shared contracts frozen
  This packet set (docs/implementation-packets/.../02, 03) plus the four ADRs and the plan.
  Exit criterion: this coordination PR merged to develop.

Phase 1 — API schema and inert domain model
  Session A, Tasks 01–06. Additive only, nothing wired to a use case yet.

Phase 2 — Agents internal contract support
  Session B, Task 07A. Can run concurrently with Phase 1 — no dependency on it.

Phase 3 — API durable coordinator and Agents integration
  Session A, Task 07B (needs 07A's outcome — see 04-task-distribution.md#task-07-split).
  07C (contract test) closes this phase.

Phase 4 — API public contract
  Session A, Tasks 08, 09, 10.

Phase 5 — Web migration
  Session C, Task 11. Development may start once Phase 0 is done (against mocks of the
  frozen contract in 03-cross-workspace-api-contracts.md); final verification needs Phase 4 done.

Phase 6 — Legacy backfill and cleanup
  Session A, Tasks 12, 13. Needs Phases 1–4 deployed-compatible.

Phase 7 — Cross-workspace regression and acceptance
  Session D, Task 14. Needs Phases 1–6 all landed (their branches pushed, not necessarily
  merged to develop yet — see 09-session-handoff-protocol.md for the merge sequencing Session D owns).
```

This mirrors the plan's own verticality note in [08 — Implementation Sequence](../../implementation-plans/assessment-authoring-operation-foundation/08-implementation-sequence.md) — the phase boundaries are the same task boundaries, only annotated with which session executes which phase.

## Session-start vs. task-completion gates

A session may *start* work earlier than the point at which its work can be *fully verified end-to-end*. This distinction matters because it lets Sessions B and C begin substantially before Session A finishes:

| Session | Can start when | Can fully verify/close out when |
|---|---|---|
| **A — API** | Phase 0 exit criterion met (this packet merged) | Its own tasks pass locally; does not need Agents or Web to close Tasks 01–10, 12, 13 |
| **B — Agents** | Phase 0 exit criterion met | 07A's own test suite passes; does not need API's coordinator to exist |
| **C — Web** | Phase 0 exit criterion met (the target contract in [03](03-cross-workspace-api-contracts.md) is already complete and frozen) — build and test against a mocked API matching that contract | Task 11 is only *fully* verifiable end-to-end once Session A's Task 10 branch exists and Web can point at a real running API; until then, Web's own Jest/RTL suite against mocks is the available verification |
| **D — Integration** | Sessions A, B, C have each produced a `*-HANDOFF.md` and pushed their branch | All of A/B/C's local test suites are green *and* the cross-workspace contract checks in [07 — Integration and Final Verification](07-integration-and-final-verification.md) pass |

This resolves a possible reading conflict: the plan's [08 — Implementation Sequence](../../implementation-plans/assessment-authoring-operation-foundation/08-implementation-sequence.md) states Task 11's prerequisite as "Task 10 (API contract must exist first)" — that statement is about *final, non-mocked verification*, not about when Session C is permitted to open its editor. The contract itself (what Task 10 will expose) is already fully specified in [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md) before any API code exists, which is exactly what makes early, mock-based Web development possible without contradicting the plan's stated dependency.

## Rules during execution

- Web never implements against an endpoint shape it invented — only against [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md).
- Agents never introduces a field name the API session has not agreed to in [02](02-shared-domain-contract.md)/[03](03-cross-workspace-api-contracts.md).
- API never removes the legacy `PATCH .../draft` endpoint (Task 10) before Web's replacement call (Task 11) is ready to ship in the same coordinated deploy — per [Authoring Operation Contract § Compatibility Impact](../../99-decisions/2026-07-28-authoring-operation-contract.md#compatibility-impact) and the plan's [09 — Risks and Rollback](../../implementation-plans/assessment-authoring-operation-foundation/09-risks-and-rollback.md), the one narrow exception (a short-lived, explicitly time-boxed compatibility shim if truly simultaneous deploy is not operationally possible) is Session D's call to make at integration time, not Session A's to decide unilaterally mid-implementation.

## Gates for Session D specifically

Integration may begin only when:

- API handoff (`API-HANDOFF.md`) is complete.
- Agents handoff (`AGENTS-HANDOFF.md`) is complete.
- Web handoff (`WEB-HANDOFF.md`) is complete.
- All three branches are pushed to `origin`.
- Each workspace's own local test suite is green, as stated in its handoff.

See [09 — Session Handoff Protocol](09-session-handoff-protocol.md) for the handoff file format and branch topology, and [FINAL-INTEGRATION-PROMPT.md](FINAL-INTEGRATION-PROMPT.md) for Session D's complete instructions.

---

← [04 — Task Distribution](04-task-distribution.md) | [↑ inicio](#top) | [Siguiente: Acceptance Criteria Ownership →](06-acceptance-criteria-ownership.md)
