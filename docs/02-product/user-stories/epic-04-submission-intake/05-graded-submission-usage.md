# US-034: Graded Submission Usage Count

- **Epic:** 04 — Student Submission Intake
- **Priority:** P0
- **ID:** US-034

## Story

As an operator, I want every analyzed student submission recorded as product usage and linked to its quoted workflow so value, cost, and credit consumption can be reconciled.

## Acceptance Criteria

- A `StudentSubmission` is created when the teacher loads a student answer.
- Usage is consumed when grading/feedback analysis is executed, not when a student account is created.
- One analyzed attempt increments the submission metric but does not imply a fixed credit charge.
- Each analysis links to the versioned workflow quote and resulting credit-ledger transaction.
- Re-analysis is tracked separately and consumes credits only when it is a new quoted user operation.
- Usage totals are visible by assessment and organization.
