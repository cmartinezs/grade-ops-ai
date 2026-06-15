# 00 Project

This folder defines the canonical foundation for GradeOps AI.

It answers:

> What are we building, why does it matter, and what must remain true across the project?

## Essence

`00-project` is the source of truth for the strategic shape of GradeOps AI:

- project positioning;
- vision;
- problem definition;
- solution definition;
- hackathon strategy;
- roadmap;
- cost model;
- canonical review.

Everything in later folders should align with the decisions captured here.

## How To Use This Folder

Start here when you need to understand the project.

Recommended reading order:

1. [`project-review.md`](project-review.md) — executive index and canonical decisions.
2. [`pitch.md`](pitch.md) — concise positioning and commercial language.
3. [`problem.md`](problem.md) — the pain and misinterpretation guardrails.
4. [`solution.md`](solution.md) — workflow, agents, control model, and evidence model.
5. [`cost-model.md`](cost-model.md) — pricing, unit economics, runtime costs, and reporting.
6. [`roadmap.md`](roadmap.md) — phases, exit criteria, and demo path.
7. [`hackathon-strategy.md`](hackathon-strategy.md) — hackathon constraints, evidence, and submission strategy.
8. [`vision.md`](vision.md) — long-term direction and strategic boundaries.

## What Belongs Here

- Canonical project decisions.
- High-level strategy.
- Scope boundaries.
- Hackathon positioning.
- Cross-folder alignment rules.
- Cost and pricing principles that affect the whole project.

## What Does Not Belong Here

- Detailed user stories.
- Agent implementation contracts.
- API schemas.
- Database tables.
- Customer interview notes.
- Raw conversation history.

Those belong in `02-product`, `03-ai-agents`, `04-architecture`, `05-evidence`, or `.raw`.

## Rule Of Interpretation

If a later document conflicts with this folder, prefer `00-project` unless a newer decision record in `99-decisions` explicitly supersedes it.
