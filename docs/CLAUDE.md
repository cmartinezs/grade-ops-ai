# CLAUDE.md

Guidance for AI coding agents working in `docs/`.

## Product Authority

GradeOps AI is a transversal AI-native assessment-operations platform. Programming is an initial validation wedge and specialization, not a core-domain restriction.

Read before product or architecture work:

1. [Assessment Operations Product Redesign](99-decisions/2026-07-27-assessment-operations-product-redesign.md)
2. [MVP Scope](02-product/mvp-scope.md)
3. [Product Workflows](02-product/workflows.md)
4. [Target Conceptual Data Model](04-architecture/data-model.md)
5. [Screen Inventory](06-ux/screen-inventory.md)

If older source, master-plan, raw, generated, or archived material conflicts with an accepted decision, the accepted decision wins and the conflicting source must be reconciled explicitly.

## Repository Boundary

`docs/` contains canonical and derived documentation. Application code lives in `api/`, `agents/`, `web/`, and `infra/`. Do not add application code under `docs/`.

## Product Invariants

- Open, closed and mixed assessments share a transversal model.
- Preparation, application, participation, evaluation, criterion review, approval, publication, correction, appeal and analysis have independent lifecycles.
- Approval is not publication.
- Publication freezes the applicable snapshot.
- Corrections create new versions and require explicit republication.
- Historical official results are immutable.
- Teacher-initiated review is distinct from formal student appeal.
- AI output is a proposal unless an approved deterministic policy applies.
- Closed scoring is deterministic against the published answer-key/scoring snapshot.
- Curriculum is structured and versioned.
- Planned, taught, assessed, demonstrated and action layers are distinct.
- Missing evidence is not low achievement.
- Authorization is actor/action/resource/relationship/policy based.
- Student-sensitive data does not appear in notification subjects, previews, or URLs.

## Documentation Structure

| Folder | Purpose |
| --- | --- |
| `00-project/` | Vision, problem, solution and roadmap |
| `01-business/` | Business model, pricing, discovery and evidence |
| `02-product/` | Personas, scope, curriculum and workflows |
| `03-ai-agents/` | Agent roles, contracts, runtime and boundaries |
| `04-architecture/` | System, target model, API, security and deployment |
| `05-evidence/` | Demand, usage, cost and operational proof |
| `06-ux/` | Lifecycle-aware teacher and student UX |
| `08-user-guide/` | User workflows and availability |
| `09-developer-guide/` | Setup, services, tests and deployment |
| `10-best-practices/` | Quality guidance |
| `99-decisions/` | Durable authority |
| `master-plan/` | Derived release sequencing |
| `.raw/`, `archive/` | Historical context only |
| `.all-by-category/` | Generated; never edit directly |

## Working Rules

- Preserve a file's language until a language-normalization decision exists.
- Use Mermaid for diagrams when a diagram is actually useful.
- Separate current implementation facts from target design.
- Do not claim a target capability is implemented without code/test evidence.
- Existing code must be classified as reusable, adaptable, incompatible, absent, or obsolete before migration planning.
- Prefer incremental migration over an unexamined rewrite.
- Record durable cross-cutting decisions in `99-decisions/`.
- Update thematic source documents before regenerating master-plan or category consolidations.
- Keep AI provider/model/prompt/version, cost, uncertainty and human decision provenance auditable.

## Current Implementation Reminder

The verified baseline includes an assessment-generation vertical slice and Gemini/Groq provider adapters. The broader runtime, lifecycle model, curriculum foundation and remaining agents are target capabilities unless code evidence proves otherwise.
