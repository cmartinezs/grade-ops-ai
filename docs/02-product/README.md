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
9. [`metrics.md`](metrics.md) — product, business, AI-native operations, trust, and hackathon evidence metrics.

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
