# Epic 10 — Billing and Plan Limits

## Narrative

**As an** operator,
**I want** to track workflow credits alongside assessments and graded submissions, and link payment or commitment evidence to customer records,
**so that** pricing is enforced, unit economics are measurable, and business validation is auditable from day one.

## Goal

Establish the metering and business evidence layer. In MVP, the goal is a correct credit ledger, workflow quoting/reservation, and reporting rather than full automated invoicing. Plans are bounded by credit balances; assessments and graded submissions remain operational metrics.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-090 | Usage Limits | P0 | [01-usage-limits.md](01-usage-limits.md) |
| US-091 | Payment Evidence Link | P1 | [02-payment-evidence-link.md](02-payment-evidence-link.md) |

## Scope

**In scope**
- Per-account credit ledger plus assessment/submission metrics
- Versioned workflow quotes and reserve/confirm/release lifecycle
- Comparison of available credits against the quoted operation
- Operator-visible overuse reporting
- Linking payment evidence (paid / commitment / manual) to customer records (P1)
- Related-party flag on revenue events for transparent traction reporting (P1)

**Out of scope**
- Automated payment processing or invoice generation
- Self-serve plan upgrades by teachers (operator-managed in MVP)
- Charging individual internal model calls or retries directly to the customer

## Epic Acceptance Criteria

- Each account tracks credit balance, reservations, confirmed debits, releases, expirations/refunds, assessments created, and graded submissions processed.
- The quoted workflow is comparable to available credits before execution.
- Failed GradeOps/provider workflows release reserved credits.
- Overuse is visible to the operator even if not automatically blocked.
- (P1) A customer/pilot record can store an evidence link (URL or reference) for payment or commitment.
- (P1) Revenue events can be marked as paid / commitment / manual and carry a related-party flag.
- (P1) `RevenueEvent.related_party` is required when the event is flagged as related-party.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Account/organization model is required for usage attribution |
| Epic 04 — Submission Intake | `UsageEvent` records the submission metric and links it to workflow/credit events |
| Epic 09 — Evidence and Metrics | Agent cost data informs unit economics alongside plan limit data |

## Definition of Done

- US-090 passes all acceptance criteria.
- Submission metrics and credit-ledger transactions are updated/reconciled transactionally around the workflow lifecycle.
- Available balance and active reservations are queryable without scanning the full event ledger.
- (P1) Revenue event and related-party flag are persisted and visible in the evidence dashboard.
