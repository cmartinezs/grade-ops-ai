# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

This folder is the canonical documentation area for GradeOps AI inside a workspace that also contains application code (`api/`, `agents/`, `web/`, and `infra/`). Do not add application code under `docs/`.

GradeOps AI is being built as a focused MVP and a real business experiment. The product is an AI-operated workflow that lets educators run assessment cycles — from learning goal through grading, feedback, gap detection, and teacher reports — using a pipeline of AI agents.

The product supports two assessment modes: **Open** (practical code/text submissions, rubric-based, AI grading suggestion) and **Closed** (objective questions with alternatives, AI-native question bank, deterministic grading). Both modes share the same operational infrastructure, teacher approval model, and evidence layer.

## Folder structure

| Folder | Purpose |
| --- | --- |
| `00-project/` | Vision, pitch, problem statement, solution, roadmap, and cost model |
| `01-business/` | Business model, pricing, go-to-market, customer discovery, revenue evidence |
| `02-product/` | Personas, MVP scope, user stories, workflows, and product metrics |
| `03-ai-agents/` | Agent roles, responsibilities, boundaries, prompt contracts, execution logs |
| `04-architecture/` | System design, data model, API design, security, deployment, Google Cloud assumptions |
| `05-evidence/` | Templates for proof of demand, usage, revenue, outcomes, and demo evidence |
| `06-ux/` | Screen inventory, interaction model, and UX design intent for teacher workspace and student access |
| `08-user-guide/` | Teacher-facing workflow guide and availability notes |
| `09-developer-guide/` | Local setup, API, DB, agent, web, testing, and deployment guidance |
| `10-best-practices/` | Spring Boot and GradeOps implementation checklists |
| `archive/2026-event/` | Historical event materials, no longer active product constraints |
| `99-decisions/` | Durable architecture, product, business, and scope decision records |
| `master-plan/` | Derived executive plan and release sequencing; coordinate with source docs |
| `source-docs-refresh/` | Audit trail for documentation refresh and remaining drift |
| `.raw/` | Historical conversation notes and reasoning history — not canonical, not edited directly |
| `.all-by-category/` | Consolidated Markdown files per category for NotebookLM upload — generated from canonical sources, not edited directly |

## Writing conventions

- **Language**: Preserve the language of the file you are editing until a canonical language policy is accepted. A language-normalization pass is pending; do not mix languages inside a file more than necessary.
- **Diagrams**: Mermaid by default. Use PlantUML only when Mermaid is insufficient. ASCII diagrams are a last fallback only.
- **Positioning**: Always frame GradeOps AI as an AI-native assessment operations business, not a quiz generator, chatbot, or LMS add-on.
- **Focus**: Prioritize business viability, customer evidence, agent operations, and product validation.

## Key content rules

**Canonical vs. historical**: The thematic folders (`00-project/` through `99-decisions/`) are the source of truth. `.raw/` is historical context only. If `.raw/` conflicts with a thematic document, the thematic document wins. Promote stronger decisions from `.raw/` into the correct thematic folder or a new decision record.

**Decision records**: Use `99-decisions/adr-template.md` for any durable decision. Name files `YYYY-MM-DD-short-title.md`. Decision records belong in `99-decisions/` when the choice affects multiple documents, implementation direction, business strategy, architecture, or scope.

**`.all-by-category/` files**: These are generated consolidations. Edit the original category documents, then regenerate these files. Do not edit `.all-by-category/` files directly.

**Master Plan**: `master-plan/` is derived planning output. Use it for release sequencing and traceability, but update source documentation in the thematic folders when durable facts change.

**Archive**: `archive/2026-event/` is historical. It must not impose active product, pricing, deployment, or evidence requirements unless a durable decision is promoted into active docs or `99-decisions/`.

## Agent pipeline (core concept)

Thirteen agents form the assessment operations workflow across two modes.

### Open assessment agents

1. **Assessment Agent** — generates the activity from a teacher's learning goal
2. **Rubric Agent** — creates and validates grading criteria
3. **Grading Agent** — analyzes submissions against the rubric
4. **Feedback Agent** — drafts personalized student feedback
5. **Learning Gap Agent** — identifies recurring issues across submissions
6. **Recovery Agent** — suggests reinforcement activities
7. **Teacher Report Agent** — prepares the final class report
8. **Ops Evidence Agent** — records usage, costs, outcomes, and agent logs

### Closed assessment agents

9. **Question Generation Agent** — generates objective questions (TF/SC/MC) with alternatives, answer key, difficulty, and learning outcome
10. **Distractor Quality Agent** — evaluates and flags weak or biased incorrect alternatives
11. **Ambiguity Review Agent** — detects interpretation problems and double-valid answers
12. **Assessment Assembly Agent** — composes a balanced assessment from approved bank questions
13. **Item Analytics Agent** — analyzes post-assessment item performance, difficulty, and outcome coverage

Each agent execution should produce structured evidence: timestamp, user, assessment, agent name, input/output summaries, provider, model, status, teacher approval state, estimated time saved, cost estimate, and prompt/template version.

The teacher is the final pedagogical authority — agents assist, they do not replace teacher judgment. For closed assessments, grading is always deterministic; AI agents generate and analyze, never score.

## Strategic constraints

- MVP is scoped to programming assessments only; do not expand scope to full LMS functionality.
- Agent runtime is provider/model-aware. Gemini remains the Google Cloud-oriented path; Groq is the current default provider for beta/iteration per [`99-decisions/2026-07-20-agent-provider-model-policy.md`](99-decisions/2026-07-20-agent-provider-model-policy.md).
- `beta` and `demo` have distinct environment roles per [`99-decisions/2026-07-20-environment-roles.md`](99-decisions/2026-07-20-environment-roles.md). Do not claim Google Cloud deployment without deployment evidence.
- Event-specific constraints are archived and historical per [`99-decisions/2026-07-20-archive-event-specific-constraints.md`](99-decisions/2026-07-20-archive-event-specific-constraints.md).
- Evidence-first: the project is designed to collect proof (interviews, pilot commitments, real assessment runs, testimonials) from day one. Success is measured by evidence, not feature volume.
