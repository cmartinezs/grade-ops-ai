# US-090: Usage Limits

- **Epic:** 10 — Billing and Plan Limits
- **Priority:** P0
- **ID:** US-090

## Story

As an operator, I want to track credit balances and workflow reservations alongside assessments and submissions so plans remain economically bounded and auditable.

## Acceptance Criteria

- Account tracks number of assessments.
- Account tracks graded submissions.
- Account tracks granted, reserved, debited, released, expired, refunded, and adjusted credits.
- Credit mutations are immutable, idempotent, and linked to the workflow quote.
- Available balance can be compared to the quoted operation before execution.
- Provider or GradeOps failures release the reservation instead of charging the customer.
- Overuse can be reported even if not billed automatically.
