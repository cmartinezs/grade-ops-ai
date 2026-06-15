# 03 AI Agents

This folder defines the operational contracts for GradeOps AI agents.

It answers:

> What does each agent do, what does it receive, what does it return, what must it never do, and how is it audited?

## Essence

`03-ai-agents` prevents the system from becoming a collection of loose prompts.

Each agent should be treated as an operational component with:

- a clear responsibility;
- structured input;
- structured output;
- uncertainty flags;
- handoff rules;
- human review checkpoints;
- logging requirements;
- acceptance criteria.

## How To Use This Folder

Start with:

1. [`agents-overview.md`](agents-overview.md) — overall agent architecture, common envelope, logs, model routing, and control principles. Covers both open and closed assessment agents.

Then use the individual contracts.

**Open assessment agents:**

- [`assessment-agent.md`](assessment-agent.md) — generates assessment draft from learning goal;
- [`rubric-agent.md`](rubric-agent.md) — creates and validates rubric criteria;
- [`grading-agent.md`](grading-agent.md) — analyzes submissions against approved rubric;
- [`feedback-agent.md`](feedback-agent.md) — drafts student-facing feedback;
- [`learning-gap-agent.md`](learning-gap-agent.md) — identifies cohort-level recurring issues;
- [`recovery-agent.md`](recovery-agent.md) — suggests reinforcement activities;
- [`teacher-report-agent.md`](teacher-report-agent.md) — prepares the final class report;
- [`ops-agent.md`](ops-agent.md) — records usage, costs, outcomes, and agent logs.

**Closed assessment agents:**

- [`question-generation-agent.md`](question-generation-agent.md) — generates objective questions (TF/SC/MC) with alternatives, answer key, difficulty, and learning outcome;
- [`distractor-quality-agent.md`](distractor-quality-agent.md) — evaluates and flags weak, biased, or misleading incorrect alternatives;
- [`ambiguity-review-agent.md`](ambiguity-review-agent.md) — detects interpretation problems, double-valid answers, and structural issues;
- [`assessment-assembly-agent.md`](assessment-assembly-agent.md) — composes a balanced assessment from approved bank questions;
- [`item-analytics-agent.md`](item-analytics-agent.md) — analyzes post-assessment item performance, difficulty, and outcome coverage.

## What Belongs Here

- Agent responsibilities.
- Input/output contracts.
- JSON-compatible output structures.
- Agent-specific limits.
- Quality checks.
- Handoff rules.
- Logging and uncertainty rules.
- Human approval boundaries.

## What Does Not Belong Here

- Full backend architecture.
- Frontend UX copy.
- Pricing.
- Customer discovery.
- Legal policy beyond agent-relevant constraints.

## Control Principle

Agents operate the repetitive workflow. Teachers retain judgment, standards, and final approval.

No agent should silently create final grades, final feedback, or student-facing outputs without teacher review.
