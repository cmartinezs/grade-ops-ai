# Epic 10 — Billing and Plan Limits

## Narrative

**As an** operator,  
**I want** to track how many assessments and graded submissions each account has consumed against their plan, and link payment or commitment evidence to customer records,  
**so that** pricing is enforced, unit economics are measurable, and business validation is auditable from day one.

## Goal

Establish the metering and business evidence layer. In MVP, the goal is tracking and reporting, not automated billing. Plans are bounded by assessments and graded submissions; exceeding limits is reported even if not automatically blocked.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-090 | Usage Limits | P0 | [01-usage-limits.md](01-usage-limits.md) |
| US-091 | Payment Evidence Link | P1 | [02-payment-evidence-link.md](02-payment-evidence-link.md) |

## Scope

**In scope**
- Per-account tracking of assessments created and graded submissions consumed
- Comparison of actual usage against plan limits
- Operator-visible overuse reporting
- Linking payment evidence (paid / commitment / manual) to customer records (P1)
- Related-party flag on revenue events (required for hackathon evidence reporting) (P1)

**Out of scope**
- Automated payment processing or invoice generation
- Self-serve plan upgrades by teachers (operator-managed in MVP)
- Metering of individual agent runs for billing purposes (agent costs are tracked in Epic 09 for evidence, not for direct customer billing in MVP)

## Epic Acceptance Criteria

- Each account tracks the number of assessments created and graded submissions consumed.
- Usage figures are comparable to the plan limits associated with the account.
- Overuse is visible to the operator even if not automatically blocked.
- (P1) A customer/pilot record can store an evidence link (URL or reference) for payment or commitment.
- (P1) Revenue events can be marked as paid / commitment / manual and carry a related-party flag.
- (P1) `RevenueEvent.related_party` is required when the event is flagged as related-party.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Account/organization model is required for usage attribution |
| Epic 04 — Submission Intake | `UsageEvent` at analysis time feeds the graded submission count |
| Epic 09 — Evidence and Metrics | Agent cost data informs unit economics alongside plan limit data |

## Definition of Done

- US-090 passes all acceptance criteria.
- Usage counters are updated transactionally when a graded submission is analyzed.
- Plan limit comparison is queryable without running a full scan of all events.
- (P1) Revenue event and related-party flag are persisted and visible in the evidence dashboard.
