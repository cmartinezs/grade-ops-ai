# US-081: Cost Estimate Per Run

- **Epic:** 09 — Evidence and Metrics
- **Priority:** P0
- **ID:** US-081

## Story

As an operator, I want token and cost estimates per agent run so unit economics can be tracked.

## Acceptance Criteria

- Agent log stores model used.
- Input/output token estimate is stored if available.
- Cost estimate is calculated or stored.
- Cost can be aggregated by assessment/customer.

---

## Definition of Done

> Precise, verifiable conditions for execution completeness. Complements AC: covers tests, migrations, agent logs, UI paths, etc.

- [ ] Each billable or potentially billable `AgentAttempt` records provider, model, token/cost fields, pricing version and currency.
- [ ] Missing token or cost data is explicit (`missing_reason` or equivalent), not silently treated as zero.
- [ ] Cost is aggregatable by assessment, operation, agent, provider/model, organization/customer, environment and release period.
- [ ] Estimated cost and actual/reconciled cost are represented separately when both exist.
- [ ] Retries and failed attempts are counted according to provider billing semantics and can be excluded/included deliberately in reports.
- [ ] Provider/model price policy is centralized and versioned; handlers/controllers do not hardcode Gemini/Groq prices.
- [ ] Dashboard/export values reconcile against persisted source events and SQL/control queries.
- [ ] Tests cover normal estimate, missing token data, provider/model unknown, retry, failed paid run and aggregation by assessment/customer.

## Technical Notes

> Repos and layers affected. Implementation hints, constraints, or known gotchas at story time.

- **Area:** `api/`, `agents/`, `infra/`
- `agents/`: returns token usage/cost metadata available from Gemini/Groq adapters plus normalized missing-data warnings.
- `api/`: owns workflow authorization/budget, credit quote/ledger, cost-event persistence, aggregation, idempotency and revenue/cost dashboard queries.
- `agents/`: owns deterministic provider/model routing, fallback, budget enforcement and resolved-route evidence.
- `infra/`: environment-specific provider credentials, quotas and telemetry must not change the cost-event schema.
- Costs are evidence, not pricing narrative. R06 can aggregate and export them, but D-07 controls customer-facing pricing language.

## Dependencies

> Other user stories or epics that must be complete before this one can be executed.

| Depends on | Reason |
|------------|--------|
| US-080 | Cost estimates are attached to execution evidence |
| D-04 provider/model policy | Prices and model families must be versioned before final aggregation |
| D-06 agent log schema | Token/cost fields must align with the canonical execution log |
| R01 assessment generation | First real cost data comes from generation/regeneration |

## Complexity

> **S** = 1-2 days · **M** = 3-5 days · **L** = 1-2 weeks · **XL** = should be split

**Estimate:** M *(provider/model policy, persistence, aggregation and reconciliation tests)*
