# User Stories

All GradeOps AI product user stories, organized by epic.

| Priority | Meaning |
|----------|---------|
| P0 | Required for MVP and validation demo |
| P1 | Important for pilot quality |
| P2 | Useful later; not required for first MVP |
| Out | Explicitly outside MVP |

## Epics

| Epic | Title | Stories |
|------|-------|---------|
| [01](epic-01-teacher-onboarding/) | Teacher Onboarding and Workspace | 12 |
| [02](epic-02-assessment-creation/) | Assessment Creation | 3 |
| [03](epic-03-rubric-generation-approval/) | Rubric Generation and Approval | 4 |
| [04](epic-04-submission-intake/) | Student Submission Intake | 5 |
| [05](epic-05-grading-assistance/) | Grading Assistance | 4 |
| [06](epic-06-feedback-generation-approval/) | Feedback Generation and Approval | 3 |
| [07](epic-07-learning-gaps-recovery/) | Learning Gaps and Recovery | 3 |
| [08](epic-08-teacher-report/) | Teacher Report | 2 |
| [09](epic-09-evidence-metrics/) | Evidence and Metrics | 4 |
| [10](epic-10-billing-plan-limits/) | Billing and Plan Limits | 2 |
| [11](epic-11-curriculum-structure/) | Subject and Curriculum Structure | 4 |
| [12](epic-12-question-bank-closed-assessment/) | Question Bank and Closed Assessment | 6 |
| [13](epic-13-student-invitation-access/) | Student Invitation and Access | 5 |
| [—](out-of-scope/) | Out of Scope | 5 |

## Out-of-Scope Stories

- [`US-OUT-001: Student Chatbot`](out-of-scope/01-student-chatbot.md)
- [`US-OUT-002: Fully Autonomous Grading`](out-of-scope/02-fully-autonomous-grading.md)
- [`US-OUT-003: Full LMS`](out-of-scope/03-full-lms.md)
- [`US-OUT-004: Physical Paper Ingestion (OMR/OCR)`](out-of-scope/04-physical-paper-ingestion.md)
- [`US-OUT-005: Enterprise SSO`](out-of-scope/05-enterprise-sso.md)

## Templates

- [`User story template`](_template-user-story.md)

## MVP Story Cut

Minimum viable story set required for MVP validation (all P0):

US-001, US-002, US-006, US-007, US-008, US-009, US-010, US-011, US-020, US-021, US-022, US-030, US-031, US-033, US-034, US-040, US-041, US-042, US-050, US-051, US-060, US-061, US-070, US-080, US-081, US-082, US-090, US-100, US-101, US-110, US-111, US-112, US-113, US-114, US-120, US-121, US-122, US-123, US-124

If those stories work end to end, GradeOps AI can validate the core business across both supported modes:

- Open assessment operations: AI-assisted practical assessment, rubric, grading suggestion, feedback, learning gaps, report, teacher approval, and evidence.
- Closed assessment operations: AI-assisted question generation and review, approved question bank, deterministic grading against a frozen answer key, signed student links, item analytics, and evidence.

Stories US-102, US-103, and US-115 remain P1 because AI-generated curriculum structure, coverage validation, and post-publish annulment are quality upgrades rather than the minimum executable Closed slice.
