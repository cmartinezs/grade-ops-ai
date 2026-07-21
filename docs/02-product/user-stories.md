# User Stories

User stories have been extracted to individual files organized by epic.

→ **[See `user-stories/`](user-stories/README.md)**

## Structure

```text
user-stories/
├── epic-01-teacher-onboarding/         # US-001 – US-009, US-013 – US-015
├── epic-02-assessment-creation/        # US-010 – US-012
├── epic-03-rubric-generation-approval/ # US-020 – US-023
├── epic-04-submission-intake/          # US-030 – US-034
├── epic-05-grading-assistance/         # US-040 – US-043
├── epic-06-feedback-generation-approval/ # US-050 – US-052
├── epic-07-learning-gaps-recovery/     # US-060 – US-062
├── epic-08-teacher-report/             # US-070 – US-071
├── epic-09-evidence-metrics/           # US-080 – US-083
├── epic-10-billing-plan-limits/        # US-090 – US-091
├── epic-11-curriculum-structure/            # US-100 – US-103
├── epic-12-question-bank-closed-assessment/ # US-110 – US-115
├── epic-13-student-invitation-access/       # US-120 – US-124
└── out-of-scope/                       # US-OUT-001 – US-OUT-005
```

## MVP Story Cut

P0 stories required for the MVP validation slice:

US-001, US-002, US-006, US-007, US-008, US-009, US-010, US-011, US-020, US-021, US-022, US-030, US-031, US-033, US-034, US-040, US-041, US-042, US-050, US-051, US-060, US-061, US-070, US-080, US-081, US-082, US-090, US-100, US-101, US-110, US-111, US-112, US-113, US-114, US-120, US-121, US-122, US-123, US-124

This cut intentionally includes both Open and Closed assessment paths:

- Open: setup, rubric, submission intake, grading assistance, feedback, learning gaps, report, and evidence.
- Closed: curriculum tags, question bank, AI question generation, teacher review, assessment assembly, frozen answer-key snapshot, signed student links, deterministic attempt grading, result access, and item analytics.

Closed grading is deterministic against the frozen answer key. AI can generate, review, or summarize Closed assessment assets, but it does not decide Closed attempt scores.

<!-- nav -->

---

← [Personas](personas.md) | [↑ inicio](#user-stories) | [README](README.md) | [Workflows →](workflows.md)
