# US-034: Graded Submission Usage Count

- **Epic:** 04 — Student Submission Intake
- **Priority:** P0
- **ID:** US-034

## Story

As an operator, I want every analyzed student submission to count against plan usage so pricing and cost controls reflect real AI workload.

## Acceptance Criteria

- A `StudentSubmission` is created when the teacher loads a student answer.
- Usage is consumed when grading/feedback analysis is executed, not when a student account is created.
- One analyzed attempt counts as one graded submission.
- Re-analysis can be tracked separately if it creates additional AI cost.
- Usage totals are visible by assessment and organization.
