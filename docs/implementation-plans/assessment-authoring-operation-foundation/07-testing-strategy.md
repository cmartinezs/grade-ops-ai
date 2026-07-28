<a id="top"></a>

# 07 — Testing Strategy

**Parent:** [README](README.md) · **Status:** Planned · **Prev:** [06 — Database Migration](06-database-migration.md) · **Next:** [08 — Implementation Sequence →](08-implementation-sequence.md)

This is the test-type matrix referenced by every task in [08 — Implementation Sequence](08-implementation-sequence.md). Baseline tooling (already verified working — see [Research 03](../../../design-system/research/research-03-baseline-technical-verification.md)): JUnit 5 + Testcontainers/PostgreSQL for `api/`, JUnit 5 for `agents/`, Jest + React Testing Library for `web/`. No new test framework is introduced.

## Test types and where they apply

| Type | Layer | Used for |
|---|---|---|
| Unit | `api/` domain | `AssessmentRevision`/`AiOperation`/`AgentAttempt` invariants (chain integrity, immutability, valid state transitions) |
| Repository/adapter | `api/` persistence | Each new JPA entity + adapter round-trips correctly; unique/partial-index constraints actually reject what they should (Testcontainers, real PostgreSQL — not H2) |
| Integration | `api/` use case + real DB | Full use-case flow against Testcontainers: create→generate, regenerate, human-edit, retry, resume — mirrors the existing `*IntegrationTest` pattern already in the codebase |
| API acceptance | `api/` controller layer | Request/response contract per endpoint in [03 — Operation and API Contracts](03-operation-and-api-contracts.md): status codes, typed error bodies, header handling |
| Concurrency | `api/` integration, two concurrent transactions | Two regenerations / two edits from the same `expectedRevisionId` → exactly one success, one deterministic `409` |
| Idempotency | `api/` integration | Replay with same key+payload → identical response, zero additional `agents/` calls (verified via a test double on the agent-client port); replay with different payload → `409` |
| Migration | `api/` Flyway, Testcontainers | Empty-DB migration V1→V17 succeeds; legacy backfill (V17) preserves row counts and produces the documented `LEGACY_UNKNOWN`/`provenanceComplete=false` labeling — never a fabricated `AI_GENERATED`/`HUMAN_EDITED` |
| Failure recovery | `api/` integration | Simulated failure after Phase 0 commit (durable `AgentAttempt` in `DISPATCHED`) but before Phase 2 — proves evidence survives; simulated agent failure → typed `failureCode` persisted verbatim |
| Agents mocks | `agents/` unit | Unchanged — `agents/`'s own test suite (32 tests, all passing per Research 03) is not restructured by this plan; only the additive `provider` field on `AssessmentAgentResponse.Log`, if needed per [04](04-ai-operation-lifecycle.md), gets a new assertion |
| Web unit/component | `web/` Jest + RTL | New states from [05 — Web Migration](05-web-migration.md): failed-retryable render, in-progress render, stale-conflict render |
| Web acceptance | `web/` (existing pattern, see `web/src/lib/api/__tests__/assessments.test.ts`) | The exact Research 02 §5.6 regression scenario: brief created, generation fails, reload, retry succeeds, exactly one `Assessment` exists |

## Specific regression guards required (not optional)

1. A test that reproduces Research 02's demonstrated dead end end-to-end (brief → failed generation → reload → retry → success, asserting exactly one `Assessment` row) — this is the single most important test in this plan, since it is the literal defect that motivated the whole cut.
2. A test proving `UpdateAssessmentDraftHandler`'s old assertion ("edit preserves id/version/createdAt") is **gone**, replaced by the opposite assertion (edit creates a new immutable revision) — the existing test that asserts the old, now-wrong behavior must be deleted, not left passing alongside a new contradictory test.
3. A test proving a late/stale AI response (`STALE_ON_COMPLETION`) still persists its `structuredResult` on the `AgentAttempt` even though no revision is created from it.
4. A test proving the `uq_ai_operations_in_flight` partial unique index actually rejects a second concurrent `PENDING`/`IN_PROGRESS` row at the database level (not only that the application-level pre-check catches it) — defense-in-depth must be proven, not assumed.

## What is not re-tested

Existing, already-passing coverage for behavior this plan does not change — `Teacher`, `AssessmentBrief` creation/validation, ownership denial, provider strategy selection inside `agents/` — is left as-is. This plan adds and replaces tests for the authoring write path; it does not re-verify unrelated, unchanged code.

---

← [06 — Database Migration](06-database-migration.md) | [↑ README](README.md) | [Siguiente: Implementation Sequence →](08-implementation-sequence.md)
