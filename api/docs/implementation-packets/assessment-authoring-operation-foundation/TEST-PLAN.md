<a id="top"></a>

# TEST-PLAN — API: Assessment Authoring Operation Foundation

**Status:** Ready. **Parent:** [README](README.md) · **Prev:** [LOCAL-CONTRACTS](LOCAL-CONTRACTS.md) · **Next:** [HANDOFF →](HANDOFF.md)

Reproduced and scoped from [07 — Testing Strategy](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/07-testing-strategy.md). Baseline tooling (already verified working, see [Research 03](../../../../design-system/research/research-03-baseline-technical-verification.md)): JUnit 5 + Testcontainers/PostgreSQL. No new test framework.

## Test types

| Type | Used for |
|---|---|
| Unit | `AssessmentRevision`/`AiOperation`/`AgentAttempt` invariants (chain integrity, immutability, valid state transitions) |
| Repository/adapter | Each new JPA entity + adapter round-trips correctly; unique/partial-index constraints actually reject what they should (Testcontainers, real PostgreSQL — not H2) |
| Integration | Full use-case flow against Testcontainers: create→generate, regenerate, human-edit, retry, resume |
| API acceptance | Request/response contract per endpoint: status codes, typed error bodies, header handling |
| Concurrency | Two regenerations / two edits from the same `expectedRevisionId` → exactly one success, one deterministic `409` |
| Idempotency | Replay with same key+payload → identical response, zero additional `agents/` calls (verified via a test double on the agent-client port); replay with different payload → `409` |
| Migration | Empty-DB migration V1→V17 succeeds; legacy backfill (V17) preserves row counts and produces the documented `LEGACY_UNKNOWN`/`provenanceComplete=false` labeling |
| Failure recovery | Simulated failure after Phase 0 commit but before Phase 2 — proves evidence survives; simulated agent failure → typed `failureCode` persisted verbatim |

## Specific regression guards required (not optional)

1. A test reproducing Research 02's demonstrated dead end end-to-end (brief → failed generation → reload → retry → success, asserting exactly one `Assessment` row) — the single most important test in this packet, since it is the literal defect that motivated the whole cut.
2. A test proving `UpdateAssessmentDraftHandler`'s old assertion ("edit preserves id/version/createdAt") is **gone**, replaced by the opposite assertion — the old test must be deleted, not left passing alongside a new contradictory one.
3. A test proving a late/stale AI response (`STALE_ON_COMPLETION`) still persists its `structuredResult` on the `AgentAttempt` even though no revision is created from it.
4. A test proving `uq_ai_operations_in_flight` actually rejects a second concurrent `PENDING`/`IN_PROGRESS` row at the database level — defense-in-depth must be proven, not assumed.

## What is not re-tested

Existing, already-passing coverage for behavior this packet does not change — `Teacher`, `AssessmentBrief` creation/validation, ownership denial — is left as-is. This packet adds and replaces tests for the authoring write path only.

## Cross-workspace contract test (Task 07C)

One additional test belongs to this packet even though its exit criterion is jointly owned with Agents (see [root packet § Task 07 split](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/04-task-distribution.md#task-07-split)): a test proving `api/`'s `agentclient` module correctly deserializes the `provider` field from a fixture matching Agents' actual response shape, not a hand-rolled JSON string. If Agents' `AGENTS-HANDOFF.md` includes a response fixture, use it directly rather than re-deriving your own.

---

← [LOCAL-CONTRACTS](LOCAL-CONTRACTS.md) | [↑ inicio](#top) | [Siguiente: HANDOFF →](HANDOFF.md)
