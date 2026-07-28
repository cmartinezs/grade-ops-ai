<a id="top"></a>

# 07 — Integration and Final Verification

**Parent:** [README](README.md) · **Status:** Ready · **Prev:** [06 — Acceptance Criteria Ownership](06-acceptance-criteria-ownership.md) · **Next:** [08 — Risk and Decision Ledger →](08-risk-and-decision-ledger.md)

## Cross-workspace test list Session D must run

```bash
# API
./mvnw -f api/pom.xml clean test

# Agents (profile mandatory — a plain compile is not a valid baseline, see agents/CLAUDE-IMPLEMENTATION-PROMPT.md)
./mvnw -f agents/pom.xml -Pdemo clean test

# Web
cd web && npm ci && npm run lint && npm run test && npm run build
```

## Contract verification checklist

- API request to `agents/` matches what Agents' orchestrator actually parses.
- Agents' response matches what API's `agentclient` module actually deserializes (the 07C exit criterion, [04 — Task Distribution](04-task-distribution.md#task-07-split)).
- API's public response shapes match what Web's client actually consumes (no field Web reads that API doesn't send, no field API sends under one name that Web expects under another).
- Failure codes align exactly with [03 — Cross-Workspace API Contracts § Canonical failure-code taxonomy](03-cross-workspace-api-contracts.md#canonical-failure-code-taxonomy) — no workspace uses a synonym.
- Status values align exactly with [03 § Canonical status taxonomy](03-cross-workspace-api-contracts.md#canonical-status-taxonomy).
- Correlation id survives end-to-end: minted by API, echoed by Agents, persisted on `AgentAttempt`, retrievable by Session D via a direct query in a manual smoke check.
- Retry semantics: a retry creates a new `AgentAttempt` under the same `AiOperation`, never a new `AiOperation`, never a new `Assessment` — verified in API's suite; Web never re-derives this rule client-side.
- `expectedRevisionId` semantics are identical in Web's request payload and API's CAS check — no client-side "helpful" default value.
- Idempotency semantics are identical — Web generates a UUID once per submit action and does not regenerate it on internal re-render; API's scope tuples match exactly what's in [02 — Shared Domain Contract](02-shared-domain-contract.md).

## Database checks

- Flyway applies cleanly from an empty database, V1 through V17.
- Flyway applies cleanly over a fixture database seeded with realistic legacy `assessment_drafts`/`agent_execution_logs` rows (the backfill migration, V17).
- All new constraints (`uq_ai_operations_in_flight`, `UNIQUE (assessment_id, version_number)`, `UNIQUE (scope_type, teacher_uid, assessment_id, operation_type, idempotency_key)`, `UNIQUE (ai_operation_id, attempt_number)`) are present and behave as specified.
- Legacy removal: confirmed **not** happening in this cut — `assessment_drafts`/`agent_execution_logs` tables still exist, read-only, per [06 — Database Migration § Legacy compatibility window](../../implementation-plans/assessment-authoring-operation-foundation/06-database-migration.md#legacy-compatibility-window).

## Acceptance criteria

All 25 rows in [06 — Acceptance Criteria Ownership](06-acceptance-criteria-ownership.md) must read `PASS`, backed by a named, passing test Session D has personally re-run — not a workspace's self-report accepted at face value. `NOT_IMPLEMENTED` on any row blocks the functional PR.

## Alignment check table

This check was run **during this coordination session**, against the drafted content of every root and local packet, before the first commit — not deferred entirely to Session D. Session D must re-run it after all three workspace branches land, because implementation frequently drifts from a plan in ways a documentation-only pass cannot catch (e.g., a real field name chosen during coding that differs from the one specified here).

| Alignment check | API | Agents | Web | Result | Correction |
|---|---|---|---|---|---|
| Status taxonomy naming | `IN_PROGRESS`/`DISPATCHED`/`FAILED_TERMINAL` (ADR) | n/a (doesn't own statuses) | must render `IN_PROGRESS`/`DISPATCHED`/`FAILED_TERMINAL` | **Mismatch found and corrected** | The coordination brief's own template suggested a generic `RUNNING`/`DISPATCHING`/`FAILED_FINAL` sketch. Rejected in favor of the ADR's actual values — recorded in [03 § Canonical status taxonomy](03-cross-workspace-api-contracts.md#canonical-status-taxonomy) |
| Failure code naming | `AGENT_UNAVAILABLE`/`AGENT_REJECTED`/etc. (ADR) | produces `INVALID_COMMAND`/`MALFORMED_OUTPUT` (ADR) | must render the ADR's codes | **Mismatch found and corrected** | The coordination brief's template suggested a generic `TIMEOUT`/`PROVIDER_UNAVAILABLE`/etc. sketch. Rejected in favor of the ADR's actual, already-implemented taxonomy — recorded in [03 § Canonical failure-code taxonomy](03-cross-workspace-api-contracts.md#canonical-failure-code-taxonomy) |
| `expectedRevisionId` field name | consumes `expectedRevisionId` | n/a | sends `expectedRevisionId` | Aligned | none needed |
| Idempotency scope tuples | `(teacherUid, operationType, key)` / `(assessmentId, operationType, key)` per [Idempotency and Concurrency Strategy](../../99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md) | n/a | sends one `Idempotency-Key` header per submit action, scope resolved server-side | Aligned | none needed |
| Provenance origin values | `AI_GENERATED`/`HUMAN_EDITED`/`LEGACY_UNKNOWN` | n/a | must render these three, no synonym | Aligned | none needed |
| Ownership duplication | owns persistence/business rules | owns provider/model resolution only | owns UI/client state only | Aligned — no task in [01](01-workspace-responsibility-matrix.md) assigns the same authoritative rule to two workspaces | none needed |
| Task ownership completeness | 12 of 14 tasks | 1 of 14 (07A) | 1 of 14 (11) | All 14 tasks have exactly one row in [04 — Task Distribution](04-task-distribution.md); Task 14 owned by Integration | none needed |
| Acceptance criteria ownership completeness | 22 of 25 primary | 0 primary, 3 supporting (13, 14, 25) | 1 primary (15), 2 supporting (7, 23) | All 25 rows in [06](06-acceptance-criteria-ownership.md) have exactly one primary owner | none needed |
| Circular dependencies | 07B waits on 07A | 07A has no dependency | 11 waits on 10 (final verification only, not session start) | No cycle — see [05 — Execution Order](05-execution-order.md#session-start-vs-task-completion-gates) for the start-vs-completion distinction that prevents a false cycle reading | none needed |
| Endpoint naming between API and Web packets | defines endpoints in [03](03-cross-workspace-api-contracts.md#api--web-public-contract) | n/a | must reference the same table, not redefine endpoints | Aligned — Web's local packet links to and reproduces this exact table | none needed |
| Scope creep into out-of-plan areas | none introduced | none introduced | none introduced | Aligned — every local packet's "Out of scope" section matches [README § Scope](../../implementation-plans/assessment-authoring-operation-foundation/README.md#scope) verbatim | none needed |

No critical contradiction remains open. Two corrections were made (status taxonomy, failure-code taxonomy), both resolved in favor of the ADRs — the higher-authority source — before any local packet was written, so no local packet ever referenced the rejected generic sketches.

---

← [06 — Acceptance Criteria Ownership](06-acceptance-criteria-ownership.md) | [↑ inicio](#top) | [Siguiente: Risk and Decision Ledger →](08-risk-and-decision-ledger.md)
