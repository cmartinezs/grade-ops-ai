<a id="top"></a>

# 05 — Execution Order

**Parent:** [README](README.md) · **Status:** Ready · **Prev:** [04 — Task Distribution](04-task-distribution.md) · **Next:** [06 — Acceptance Criteria Ownership →](06-acceptance-criteria-ownership.md)

## Phases

```text
Phase 0 — Shared contracts frozen
  This packet set (docs/implementation-packets/.../02, 03) plus the four ADRs and the plan.
  Exit criterion: this coordination PR merged to develop.

Phase 1 — API schema and inert domain model
  API sub-session A1, Tasks 01–05. Additive only, nothing wired to a use case yet.

Phase 2 — Agents internal contract support
  Session B, Task 07A. Can run concurrently with Phase 1 — no dependency on it.

Phase 3 — API idempotency guard and durable coordinator (Agents integration)
  API sub-session A2, Tasks 06, 07B (07B needs 07A's outcome — see
  04-task-distribution.md#task-07-split). 07C (contract test) closes this phase.
  Needs Phase 1 (A1's handoff) complete.

Phase 4 — API authoring mutations and public contract
  API sub-session A3, Tasks 08, 09, 10. Needs Phase 3 (A2's handoff) complete.
  This is the handoff point that unblocks Web's final, non-mocked verification.

Phase 5 — Web migration
  Session C, Task 11. Development may start once Phase 0 is done (against mocks of the
  frozen contract in 03-cross-workspace-api-contracts.md); final verification needs Phase 4 done.

Phase 6 — Legacy backfill and cleanup
  API sub-session A4, Tasks 12, 13. Needs Phase 4 (A3's handoff) deployed-compatible.
  Produces the consolidated API-HANDOFF.md covering A1-A4.

Phase 7 — Cross-workspace regression and acceptance
  Session D, Task 14. Needs Phases 1–6 all landed (their branches pushed, not necessarily
  merged to develop yet — see 09-session-handoff-protocol.md for the merge sequencing Session D owns).
```

This mirrors the plan's own verticality note in [08 — Implementation Sequence](../../implementation-plans/assessment-authoring-operation-foundation/08-implementation-sequence.md) — the phase boundaries are the same task boundaries, only annotated with which session (and, for API, which of its four sequential sub-sessions) executes which phase. Session A is not one implementation session but four (A1→A2→A3→A4) on one branch — see [Session A internal sequencing](#session-a-internal-sequencing-a1a2a3a4) below and the [API local packet](../../../api/docs/implementation-packets/assessment-authoring-operation-foundation/README.md).

## Session A internal sequencing (A1→A2→A3→A4)

```text
Session A1 ───────────────┐
Session B / Agents ───────┼──→ Session A2 → Session A3 → Session A4
Session C / Web ──────────┘                          │
                                                       ▼
                                                 Session D
```

- A1, B (Agents), and C (Web) may all start in parallel — none of the three has a dependency on either of the other two at *start* time.
- A2 waits on both A1 (needs its schema/domain handoff) and B (needs Agents' Task 07A outcome — see the [API-A2-PROMPT's own Prerequisites gate](../../../api/docs/implementation-packets/assessment-authoring-operation-foundation/CLAUDE-API-A2-PROMPT.md#prerequisites-gate-check-this-before-anything-else) for the documented fallback if B hasn't finished).
- A3 waits on A2 only.
- A4 waits on A3 only.
- C (Web) may continue development against mocks throughout A1–A3; it does not block on any single API sub-session to *start*, only to fully verify Task 11 end-to-end (needs A3's public contract).
- D (Integration) waits on the final consolidated API handoff (produced by A4), Agents' handoff, and Web's handoff — not on each of A1/A2/A3's intermediate handoffs individually, though it does verify their continuity as part of validating A4's consolidated summary (see [07 — Integration and Final Verification](07-integration-and-final-verification.md)).

## Session-start vs. task-completion gates

A session may *start* work earlier than the point at which its work can be *fully verified end-to-end*. This distinction matters because it lets Sessions B and C begin substantially before Session A finishes:

| Session | Can start when | Can fully verify/close out when |
|---|---|---|
| **A1 (API)** | Phase 0 exit criterion met (this packet merged) | Its own five tasks pass locally; does not need Agents, Web, or any later A-sub-session |
| **A2 (API)** | A1's handoff complete **and** Agents' Task 07A resolved (handoff or documented fallback) | Its own two tasks pass locally, including the 07C contract test |
| **A3 (API)** | A2's handoff complete | Its own three tasks pass locally; produces the contract artifact Web needs for final verification |
| **A4 (API)** | A3's handoff complete | Its own two tasks pass locally; produces the consolidated `HANDOFF.md` Session D reads |
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

- API's **consolidated** handoff (`API-HANDOFF.md`, produced by sub-session A4 and summarizing A1–A4) is complete — which by construction means all four API sub-sessions, including their four intermediate handoffs (`API-A1-HANDOFF.md` through `API-A4-HANDOFF.md`), are done and continuous with no gap.
- Agents handoff (`AGENTS-HANDOFF.md`) is complete.
- Web handoff (`WEB-HANDOFF.md`) is complete.
- All three branches are pushed to `origin`.
- Each workspace's own local test suite is green, as stated in its handoff.

See [09 — Session Handoff Protocol](09-session-handoff-protocol.md) for the handoff file format and branch topology, and [FINAL-INTEGRATION-PROMPT.md](FINAL-INTEGRATION-PROMPT.md) for Session D's complete instructions.

---

← [04 — Task Distribution](04-task-distribution.md) | [↑ inicio](#top) | [Siguiente: Acceptance Criteria Ownership →](06-acceptance-criteria-ownership.md)
