# Epic 07 — Learning Gaps and Recovery

## Narrative

**As a** teacher who has graded a cohort,  
**I want** the AI to identify recurring mistakes across all submissions, suggest recovery activities to address them, and optionally provide student-specific next steps,  
**so that** I can act on assessment outcomes beyond just assigning grades.

## Goal

Transform graded submission data into actionable pedagogical intelligence: cohort-level gap detection, concrete recovery activities, and optional per-student next steps. This closes the loop between assessment outcomes and instruction.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-060 | Learning Gap Summary | P0 | [01-learning-gap-summary.md](01-learning-gap-summary.md) |
| US-061 | Recovery Activity Suggestion | P0 | [02-recovery-activity-suggestion.md](02-recovery-activity-suggestion.md) |
| US-062 | Student-Specific Recovery Notes | P1 | [03-student-specific-recovery-notes.md](03-student-specific-recovery-notes.md) |

## Scope

**In scope**
- Learning Gap Agent summarizing common mistakes across the cohort, linked to rubric criteria
- Severity or priority indication per detected gap, and count of affected submissions
- Recovery Agent suggesting at least one concrete activity per identified gap (instructions + expected output)
- Teacher editing or rejecting any recovery activity
- Optional per-student next steps generated and teacher-approved before delivery (P1)

**Out of scope**
- Automatic delivery of recovery activities or next steps to students without teacher approval
- Longitudinal gap tracking across multiple assessments (MVP is per-assessment)
- Adaptive learning path generation (out of scope)

## Epic Acceptance Criteria

- Learning Gap Agent produces a summary of recurring issues after grading is complete, linked to rubric criteria.
- Summary shows the number of submissions affected by each gap and a severity or priority indicator.
- Recovery Agent provides at least one activity per detected gap, with instructions and expected output.
- Teacher can edit or reject any suggested activity.
- (P1) Per-student next steps can be generated, approved, and edited; they are not delivered automatically.
- Both agents produce `AgentExecutionLog` records per run.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Authenticated teacher workspace required |
| Epic 05 — Grading Assistance | Gap analysis requires graded (approved or reviewed) submissions |
| Epic 06 — Feedback Generation and Approval | Gaps may be surfaced alongside feedback; agents share rubric and grading data |

## Definition of Done

- All P0 stories pass their acceptance criteria.
- Learning Gap Agent and Recovery Agent are called server-side only.
- Gap summaries are persisted and associated with the assessment.
- Recovery activities are editable and teacher-controlled; no auto-delivery path exists.
- Agent execution logs are created for every run.
