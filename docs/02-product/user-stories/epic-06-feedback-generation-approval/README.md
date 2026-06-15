# Epic 06 — Feedback Generation and Approval

## Narrative

**As a** teacher,  
**I want** the AI to draft personalized feedback for each student based on the rubric and grading suggestion, and I want to review, edit, and explicitly approve it before any student sees it,  
**so that** every student receives high-quality, teacher-endorsed feedback without me writing each message from scratch.

## Goal

Produce student-readable feedback drafts and enforce a teacher approval gate before delivery. No feedback reaches students without an `ApprovalEvent`.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-050 | Individual Feedback Draft | P0 | [01-individual-feedback-draft.md](01-individual-feedback-draft.md) |
| US-051 | Feedback Approval | P0 | [02-feedback-approval.md](02-feedback-approval.md) |
| US-052 | Tone Adjustment | P1 | [03-tone-adjustment.md](03-tone-adjustment.md) |

## Scope

**In scope**
- Feedback Agent generating per-student feedback from the rubric and grading suggestion
- Feedback structure: strengths, improvement areas, and a concrete next step
- Feedback lifecycle states: pending → approved / edited / rejected
- Teacher approval, inline editing, and rejection
- Tone selection (concise / supportive / direct) influencing generation (P1)

**Out of scope**
- Automatic delivery of feedback to students without teacher approval
- Feedback templates pre-loaded by the teacher
- Bulk feedback generation without per-student review (a teacher can approve in sequence, but cannot skip the review step for flagged items)
- Student-facing feedback portal (delivery mechanism is out of MVP scope for open-assessment mode)

## Epic Acceptance Criteria

- Feedback Agent generates feedback for each student based on rubric criteria and grading suggestion.
- Each feedback draft includes strengths, improvement areas, and a next step.
- Feedback is editable by the teacher before or after approval.
- Teacher can approve, mark as edited, or reject each feedback item.
- Approved feedback is clearly marked final; rejected feedback is logged with optional reason.
- No feedback transitions to a student-visible state without an `ApprovalEvent`.
- Every Feedback Agent run produces an `AgentExecutionLog` record.
- (P1) Teacher can select a tone; the selection is passed to the agent and affects the draft.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Authenticated teacher workspace required |
| Epic 05 — Grading Assistance | Feedback Agent requires rubric and grading suggestion as input |

## Definition of Done

- All P0 stories pass their acceptance criteria.
- Feedback Agent is called server-side only.
- `ApprovalEvent` is recorded for every approval or rejection action.
- Original AI draft is preserved and traceable even after teacher edits.
- Feedback state machine enforces the pending → approved/rejected transition.
