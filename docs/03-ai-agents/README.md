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

Implementation status is release-driven. The current code has an Assessment Agent vertical slice and provider adapters; the remaining agents are contracts until their release slice creates a real consumer. See [`../09-developer-guide/06-agent-development.md`](../09-developer-guide/06-agent-development.md) for implemented runtime guidance.

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

## Traceability

| Topic | Source |
| --- | --- |
| Provider/model routing policy | [`../99-decisions/2026-07-27-policy-based-model-routing.md`](../99-decisions/2026-07-27-policy-based-model-routing.md) |
| Agent service separation | [`../99-decisions/2026-06-10-agent-runtime-separation.md`](../99-decisions/2026-06-10-agent-runtime-separation.md) |
| Evidence schema | [`../05-evidence/agent-logs.md`](../05-evidence/agent-logs.md) |
| Open/Closed workflows | [`../02-product/workflows.md`](../02-product/workflows.md) |
| Runtime implementation guide | [`../09-developer-guide/06-agent-development.md`](../09-developer-guide/06-agent-development.md) |
