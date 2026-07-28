# GradeOps AI

**One-line pitch:** AI-native assessment operations with human academic control.

GradeOps AI helps independent teachers and institutions plan, apply, evaluate, review, publish, correct, and analyze open, closed, and mixed assessments. AI drafts and recommends; teachers retain authority over high-impact academic decisions.

Programming remains an initial validation wedge and specialization. It does not constrain the transversal product model.

## Product Boundary

GradeOps AI is not a full LMS, generic quiz generator, student chatbot, or autonomous grader. It is the operational layer around assessment:

- academic and curriculum context;
- versioned templates and assessment definitions;
- participation, attempts and submissions;
- deterministic closed scoring and AI-assisted open evaluation;
- evidence-aware teacher review;
- final-result approval and explicit publication;
- correction, republication and appeals;
- curriculum coverage and pedagogical action;
- audit, AI-run, usage and cost evidence.

## Core Invariants

- Preparation, application, participation, evaluation, review, approval, publication, correction, appeal, and analysis have independent lifecycles.
- Publishing freezes the applicable assessment/rubric/answer-key/scoring/curriculum snapshot.
- Approval does not publish.
- Corrections create new versions; historical official results are not overwritten.
- AI output is a proposal unless an approved deterministic policy applies.
- Missing evidence is not low achievement.
- Sensitive student data is excluded from notification subjects, previews, and URLs.

## Curriculum-First Analytics

The primary analysis view follows:

```text
Unit -> Topic -> Learning Objective -> Criteria -> Cohort/Student evidence
```

GradeOps compares planned, taught, assessed, demonstrated, and action layers without collapsing coverage, achievement, and evidence sufficiency into one metric.

## Current Implementation vs Target

The repository contains an existing vertical slice and historical programming-focused contracts. The approved target is defined by [the 2026-07-27 redesign decision](99-decisions/2026-07-27-assessment-operations-product-redesign.md). Existing code must be classified as reusable, adaptable, incompatible, absent, or obsolete before migration planning. Do not infer that a target capability is implemented merely because it is documented here.

## Documentation Map

- [Project](00-project/README.md) — vision, problem, solution, roadmap and costs.
- [Business](01-business/README.md) — model, pricing, discovery and evidence.
- [Product](02-product/README.md) — personas, scope, curriculum and workflows.
- [AI agents](03-ai-agents/README.md) — runtime, agents, contracts and boundaries.
- [Architecture](04-architecture/README.md) — systems, target model, API, security and deployment.
- [Evidence](05-evidence/README.md) — demand, usage, cost and operational proof.
- [UX](06-ux/README.md) — lifecycle-aware teacher and student experiences.
- [User guide](08-user-guide/README.md) — supported workflows and availability notes.
- [Developer guide](09-developer-guide/README.md) — setup, services, tests and deployment.
- [Best practices](10-best-practices/README.md) — implementation quality guidance.
- [Decisions](99-decisions/README.md) — durable product and architecture authority.
- [Implementation plans](implementation-plans/README.md) — task-by-task technical plans for a specific decision-backed cut, e.g. [Assessment Authoring Operation Foundation](implementation-plans/assessment-authoring-operation-foundation/README.md).
- [Master plan](master-plan/README.md) — derived sequencing; must be regenerated after alignment review.
- [Archive](archive/2026-event/README.md) — historical event constraints only.

## Source-of-Truth Order

1. Accepted decision records.
2. Current thematic source documents.
3. Derived master-plan documents.
4. Historical/raw/archive material.

When sources conflict, the higher level wins and the lower level must be reconciled rather than silently interpreted.
