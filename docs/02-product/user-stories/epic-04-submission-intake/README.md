# Epic 04 — Student Submission Intake

## Narrative

**As a** teacher,  
**I want** to load student work into the system — individually, by file upload, or in bulk — and track each submission's processing state,  
**so that** the AI grading pipeline has the raw material it needs and I always know what is pending.

## Goal

Create `StudentSubmission` records that feed the grading pipeline. Each analyzed submission counts as one unit of plan usage, so intake is also the metering boundary.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-030 | Manual Student Submission Creation | P0 | [01-manual-submission.md](01-manual-submission.md) |
| US-031 | File Upload Student Submission | P0 | [02-file-upload-submission.md](02-file-upload-submission.md) |
| US-032 | Bulk Submission Intake | P1 | [03-bulk-submission-intake.md](03-bulk-submission-intake.md) |
| US-033 | Submission Status | P0 | [04-submission-status.md](04-submission-status.md) |
| US-034 | Graded Submission Usage Count | P0 | [05-graded-submission-usage.md](05-graded-submission-usage.md) |

## Scope

**In scope**
- Manual paste of code or text as a student submission
- File upload (code/text file types) with content extraction
- Bulk import from a batch or file (P1)
- Submission status lifecycle: received → pending → analyzed → needs review → approved / rejected/excluded
- Usage tracking: one analyzed attempt = one graded submission against the plan

**Out of scope**
- Direct student submission via portal (students use signed links in closed mode — Epic 12; open-mode student self-submission is not in MVP)
- GitHub or Git repository ingestion
- Plagiarism detection
- Re-analysis billing details (tracked separately if needed, not required for MVP)

## Epic Acceptance Criteria

- Teacher can create a `StudentSubmission` by pasting code/text and providing a `student_identifier`.
- Teacher can upload a supported file type; content is extracted and stored linked to the submission.
- Unsupported file types produce a clear, actionable error message.
- Each submission displays its current status from the defined lifecycle states.
- Errors on a submission are visible and the submission is recoverable (can be retried or excluded).
- A `UsageEvent` is created when grading/feedback analysis executes on a submission, not at submission creation.
- Usage totals are visible by assessment and by organization.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Authenticated teacher workspace required |
| Epic 02 — Assessment Creation | Submissions must be associated with an existing assessment |
| Epic 03 — Rubric Generation and Approval | Grading (Epic 05) requires an approved rubric; intake is independent but typically precedes grading |

## Definition of Done

- All P0 stories pass their acceptance criteria.
- `StudentSubmission` entity is created and persisted with `student_identifier` and assessment association.
- File content is stored in Cloud Storage; submission record holds a reference.
- Submission status transitions are auditable.
- `UsageEvent` is created at analysis time; usage totals are queryable.
- Plan usage counter is updated and comparable to plan limit.
