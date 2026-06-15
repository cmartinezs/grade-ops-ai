# Epic 03 — Rubric Generation and Approval

## Narrative

**As a** teacher who has defined an assessment,  
**I want** the AI to generate a grading rubric, flag any issues, and wait for my explicit approval before grading begins,  
**so that** every submission is evaluated against criteria I have reviewed and authorized.

## Goal

Produce a validated, teacher-approved rubric that locks the grading criteria for the assessment. Teacher approval is the gate between draft creation and grading; no AI grading suggestion can run against an unapproved rubric.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-020 | Rubric Draft Generation | P0 | [01-rubric-draft-generation.md](01-rubric-draft-generation.md) |
| US-021 | Rubric Validation | P0 | [02-rubric-validation.md](02-rubric-validation.md) |
| US-022 | Rubric Approval | P0 | [03-rubric-approval.md](03-rubric-approval.md) |
| US-023 | Rubric Version History | P1 | [04-rubric-version-history.md](04-rubric-version-history.md) |

## Scope

**In scope**
- Rubric Agent generating criteria with weights and performance levels from the approved assessment draft
- Automated validation: ambiguity detection, missing criteria flags, inconsistent weight warnings
- Explicit teacher approval action that locks the rubric for grading
- Version history showing each change with timestamp and author (P1)

**Out of scope**
- Rubric templates or teacher-supplied rubric skeletons (MVP: AI-generated only)
- Rubric sharing across assessments or teachers
- Rubric for closed assessments (those use a deterministic answer key — see Epic 11)

## Epic Acceptance Criteria

- Rubric Agent generates a structured rubric with at least two criteria, each with a weight and performance level descriptions.
- Total weight is visible; the agent or UI flags if weights do not sum correctly.
- Validation notes (ambiguity, missing criteria, weight warnings) are visible to the teacher before approval.
- Teacher can edit any criterion before approving.
- Teacher approval action transitions the rubric to a locked state.
- Any post-approval edit creates a new version or an explicit update event — the original approved version is never silently overwritten.
- Every agent execution is logged with model, cost estimate, and status.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Authenticated teacher workspace required |
| Epic 02 — Assessment Creation | An approved or saved assessment draft is the input to the Rubric Agent |

## Definition of Done

- All P0 stories pass their acceptance criteria.
- Grading pipeline (Epic 05) cannot start unless a rubric is in `approved` state.
- `ApprovalEvent` is recorded when the teacher approves the rubric.
- Rubric is immutable after approval except via an explicit versioned update.
- Agent execution log is created for every Rubric Agent run.
