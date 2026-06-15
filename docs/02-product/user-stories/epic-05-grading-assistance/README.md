# Epic 05 — Grading Assistance

## Narrative

**As a** teacher,  
**I want** the AI to suggest scores and evidence for each rubric criterion, flag where it is uncertain, and let me override or reject any suggestion,  
**so that** grading is faster and more consistent while I retain full pedagogical authority over every final score.

## Goal

Deliver AI-assisted grading suggestions against the approved rubric, with explicit teacher control at every decision point. No AI score becomes final without teacher action.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-040 | Rubric-Based Grading Suggestion | P0 | [01-rubric-based-grading-suggestion.md](01-rubric-based-grading-suggestion.md) |
| US-041 | Uncertainty Flags | P0 | [02-uncertainty-flags.md](02-uncertainty-flags.md) |
| US-042 | Teacher Edit of Score | P0 | [03-teacher-edit-score.md](03-teacher-edit-score.md) |
| US-043 | Reject AI Suggestion | P0 | [04-reject-ai-suggestion.md](04-reject-ai-suggestion.md) |

## Scope

**In scope**
- Grading Agent analyzing each submission against the approved rubric
- Suggested score per criterion with supporting evidence snippets
- Uncertainty/risk flags on outputs the agent is not confident about
- Teacher editing individual criterion scores with an explanatory note
- Teacher rejecting the full AI suggestion, changing submission status accordingly
- Full agent execution logging per run

**Out of scope**
- Bulk auto-approval of grading suggestions without teacher review
- Automatic final score delivery to students (requires approval — see Epic 06)
- Grading for closed assessments (deterministic, no AI scoring — see Epic 11)
- Cross-submission comparative grading

## Epic Acceptance Criteria

- Grading Agent runs against an approved rubric and a `StudentSubmission`.
- Output includes a suggested score per criterion and an overall suggested score.
- Each criterion suggestion links to evidence (snippets or summary) from the submission.
- Uncertain, incomplete, or ambiguous outputs are flagged; flagged outputs cannot be bulk-approved silently.
- Teacher can modify any per-criterion score and leave a note; the original AI suggestion remains traceable.
- Teacher can reject the full AI suggestion with an optional reason; rejection is logged.
- All outputs are clearly marked as suggestions until teacher approval.
- Every Grading Agent run produces an `AgentExecutionLog` record.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Authenticated teacher workspace required |
| Epic 03 — Rubric Generation and Approval | Rubric must be in `approved` state before Grading Agent can run |
| Epic 04 — Student Submission Intake | `StudentSubmission` records must exist |

## Definition of Done

- All four P0 stories pass their acceptance criteria.
- Grading Agent is called server-side; Gemini API key is not exposed to the frontend.
- AI-suggested score is stored separately from the teacher-approved final score.
- `ApprovalEvent` is recorded when the teacher approves or edits a grading suggestion.
- Rejection events are logged and visible in internal evidence dashboard.
- Flagged uncertainty outputs are enforced: no silent bulk approval path exists.
