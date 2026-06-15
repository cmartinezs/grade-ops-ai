# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

This is a **documentation-only** repository for GradeOps AI — an AI-native assessment operations platform for programming educators. There is no application code here. Do not add application code unless explicitly requested.

GradeOps AI is being built as a focused hackathon MVP (XPRIZE AI hackathon) and a real business experiment. The product is an AI-operated workflow that lets educators run assessment cycles — from learning goal through grading, feedback, gap detection, and teacher reports — using a pipeline of AI agents.

The product supports two assessment modes: **Open** (practical code/text submissions, rubric-based, AI grading suggestion) and **Closed** (objective questions with alternatives, AI-native question bank, deterministic grading). Both modes share the same operational infrastructure, teacher approval model, and evidence layer.

## Folder structure

| Folder | Purpose |
| --- | --- |
| `00-project/` | Vision, pitch, problem statement, solution, roadmap, hackathon strategy |
| `01-business/` | Business model, pricing, go-to-market, customer discovery, revenue evidence |
| `02-product/` | Personas, MVP scope, user stories, workflows, and product metrics |
| `03-ai-agents/` | Agent roles, responsibilities, boundaries, prompt contracts, execution logs |
| `04-architecture/` | System design, data model, API design, security, deployment, Google Cloud assumptions |
| `05-evidence/` | Templates for proof of demand, usage, revenue, outcomes, and demo evidence |
| `06-ux/` | Screen inventory, interaction model, and UX design intent for teacher workspace and student access |
| `07-hackathon/` | Operational hackathon deliverables: demo script, evidence checklist, submission narrative |
| `99-decisions/` | Durable architecture, product, business, and scope decision records |
| `.raw/` | Historical conversation notes and reasoning history — not canonical, not edited directly |
| `.all-by-category/` | Consolidated Markdown files per category for NotebookLM upload — generated from canonical sources, not edited directly |

## Writing conventions

- **Language**: Concise, professional English. Avoid generic AI assistant language.
- **Diagrams**: Mermaid by default. Use PlantUML only when Mermaid is insufficient. ASCII diagrams are a last fallback only.
- **Positioning**: Always frame GradeOps AI as an AI-native assessment operations business, not a quiz generator, chatbot, or LMS add-on.
- **Focus**: Prioritize business viability, customer evidence, agent operations, and hackathon deliverables.

## Key content rules

**Canonical vs. historical**: The thematic folders (`00-project/` through `99-decisions/`) are the source of truth. `.raw/` is historical context only. If `.raw/` conflicts with a thematic document, the thematic document wins. Promote stronger decisions from `.raw/` into the correct thematic folder or a new decision record.

**Decision records**: Use `99-decisions/adr-template.md` for any durable decision. Name files `YYYY-MM-DD-short-title.md`. Decision records belong in `99-decisions/` when the choice affects multiple documents, implementation direction, business strategy, architecture, or scope.

**`.all-by-category/` files**: These are generated consolidations. Edit the original category documents, then regenerate these files. Do not edit `.all-by-category/` files directly.

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

Each agent execution should produce structured evidence: timestamp, user, assessment, agent name, input/output summaries, model used, status, teacher approval state, estimated time saved.

The teacher is the final pedagogical authority — agents assist, they do not replace teacher judgment. For closed assessments, grading is always deterministic; AI agents generate and analyze, never score.

## Strategic constraints

- MVP is scoped to programming assessments only; do not expand scope to full LMS functionality.
- Gemini API + Google Cloud is the target runtime.
- Evidence-first: the project is designed to collect proof (interviews, pilot commitments, real assessment runs, testimonials) from day one. For the hackathon, success is measured by evidence, not feature volume.
