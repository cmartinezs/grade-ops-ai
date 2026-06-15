# US-115: Annul Question and Recalculate

- **Epic:** 12 — Question Bank and Closed Assessment Creation
- **Priority:** P1
- **ID:** US-115

## Story

As a teacher, I want to void a specific question after grading and trigger an audited recalculation so errors do not penalize students unfairly.

## Acceptance Criteria

- Teacher can annul a specific question with a mandatory reason.
- System recalculates all student scores excluding the annulled question.
- Original results are preserved in the audit trail.
- Recalculation event is logged.
