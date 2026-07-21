# 02 Product

This folder defines what the MVP must do for users.

It answers:

> What does the product experience need to include to prove the business and support a credible demo?

## Essence

`02-product` turns the project and business strategy into product behavior:

- MVP scope (two assessment modes: open and closed);
- personas;
- user stories;
- workflows;
- assessment modes and their specific flows;
- student access model;
- response intake channels;
- curriculum and subject structure;
- product metrics.

The folder is intentionally product-focused. It describes user experience, states, flows, priorities, and acceptance criteria without becoming backend architecture.

## How To Use This Folder

Use these files to drive product planning and implementation:

1. [`mvp-scope.md`](mvp-scope.md) — what is in scope, out of scope, and required for the demo; scope matrix with priorities.
2. [`personas.md`](personas.md) — users, buyers, operators, reviewers, and anti-personas.
3. [`user-stories.md`](user-stories.md) — backlog by epic, priority, and acceptance criteria (Epics 1–13).
4. [`workflows.md`](workflows.md) — end-to-end workflows for open and closed assessment, approval states, failure flows, and evidence flow.
5. [`assessment-modes.md`](assessment-modes.md) — open vs. closed assessment comparison, shared lifecycle, AI role per mode, question bank states, and snapshot rule.
6. [`student-access.md`](student-access.md) — student (LearnerRef) model, secure link types, invitation flow, what students can see, and security rules.
7. [`response-intake.md`](response-intake.md) — digital (P0) and physical paper (P1) intake channels, OCR vs OMR, conflict resolution, and normalization to AssessmentAttempt.
8. [`curriculum-structure.md`](curriculum-structure.md) — subject/topic/learning-outcome taxonomy; P0 string tagging; P1 structured model with CurriculumNode and LearningObjective; AI-generated curriculum; Chile pack reference.
9. [`metrics.md`](metrics.md) — product, business, AI-native operations, trust, and validation evidence metrics.

## What Belongs Here

- User needs.
- Product flows.
- Acceptance criteria.
- MVP boundaries.
- Product lifecycle states.
- Product and activation metrics.
- Mermaid diagrams for product workflows.

## What Does Not Belong Here

- Detailed API endpoints.
- Prompt templates.
- Infrastructure choices.
- Sales scripts.
- Financial ledgers.
- Raw technical exploration.

## Diagram Rule

Product diagrams must use Mermaid by default. Use PlantUML only when Mermaid cannot express the diagram clearly. Use ASCII only as a last fallback.

## Key Decisions And Traceability

| Decision | Product Documents |
| --- | --- |
| [`Closed Assessment Mode`](../99-decisions/2026-06-10-closed-assessment-mode.md) | [`mvp-scope.md`](mvp-scope.md), [`assessment-modes.md`](assessment-modes.md), [`workflows.md`](workflows.md), [`user-stories.md`](user-stories.md) |
| [`Deterministic Grading For Closed`](../99-decisions/2026-06-10-deterministic-grading-for-closed.md) | [`assessment-modes.md`](assessment-modes.md), [`workflows.md`](workflows.md), [`student-access.md`](student-access.md) |
| [`Assessment Snapshot On Publish`](../99-decisions/2026-06-10-assessment-snapshot-on-publish.md) | [`assessment-modes.md`](assessment-modes.md), [`workflows.md`](workflows.md) |
| [`Student Access Via Secure Link`](../99-decisions/2026-06-10-student-access-via-secure-link.md) | [`student-access.md`](student-access.md), [`workflows.md`](workflows.md), [`../06-ux/student-access-ux.md`](../06-ux/student-access-ux.md) |
| [`AI Native Question Bank`](../99-decisions/2026-06-10-ai-native-question-bank.md) | [`assessment-modes.md`](assessment-modes.md), [`curriculum-structure.md`](curriculum-structure.md), user-story epics 11-13 |

The MVP story cut in [`user-stories.md`](user-stories.md) is the product-level bridge into the release plan in [`../master-plan/`](../master-plan/README.md).
