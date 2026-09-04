# Academic Grading With AI as Behavioral Reference Implementation

- Status: Accepted
- Date: 2026-09-04
- Scope: Product relationship, agentic operations, determinism boundary

## Decision

`cmartinezs/academic-grading-with-ai` is an **active agentic reference implementation / living behavioral prototype** for GradeOps AI.

It is not a legacy alias, a superseded predecessor, or a competing academic-operations product.

GradeOps AI remains the broader product and system of record. Academic Grading With AI is the high-flexibility operational surface used to discover, exercise, and validate assessment workflows before all recurring behavior is formalized into GradeOps AI.

## Operating model of Academic Grading With AI

Academic Grading With AI is intentionally context-driven and prompt-driven. Provider agents such as Codex or Claude operate over a workspace containing contextual Markdown and raw academic artifacts, including:

- `statement.md` and `rubric.md`;
- `base.md`, `case.md`, and `plan.md`;
- PDFs and office documents;
- student submissions and extracted evidence;
- rosters, section configuration, and form assignments;
- generated review, grading, feedback, publication, and delivery artifacts.

A significant portion of the behavior therefore lives in agent reasoning, contextual interpretation, and orchestration rather than in deterministic application code.

## Relationship to GradeOps AI

The intended convergence is:

```mermaid
flowchart LR
    AG[Academic Grading With AI] -->|discovers and validates behavior| GO[GradeOps AI]
    AG --> MD[Markdown context]
    AG --> RAW[Raw academic artifacts]
    AG --> PROMPT[Prompt-driven orchestration]
    AG --> LLM[Provider-agent reasoning]

    GO --> DOMAIN[Explicit domain model]
    GO --> CONTRACTS[Typed contracts]
    GO --> WORKFLOWS[Deterministic or bounded workflows]
    GO --> STATE[Persisted lifecycle state]
    GO --> EVIDENCE[Reproducible evidence and audit]
    GO --> AGENTS[Controlled agent capabilities]
```

Academic Grading With AI discovers and exercises desired behavior; GradeOps AI industrializes it.

## Extraction rule

When a behavior in Academic Grading With AI becomes frequent, stable, and important enough to reproduce consistently, evaluate it for extraction into GradeOps AI as one of:

1. deterministic domain/application logic;
2. a typed and bounded agent contract;
3. an explicit human-approval workflow;
4. a retained exploratory behavior in the agentic reference workspace.

Do not move behavior into deterministic code merely because it exists in the agentic workspace. Conversely, do not leave stable business rules implicit in prompts or Markdown once they have become product invariants.

## Why the reference workspace remains valuable

GradeOps AI becoming more deterministic does not obsolete Academic Grading With AI. The workspace remains useful as a fast, context-rich environment for discovering new workflows, edge cases, grading heuristics, teacher interactions, and provider-agent behavior before those patterns are promoted into the structured product.

## Canonicality

- **GradeOps AI:** canonical broader product, domain model, structured workflows, persistence, lifecycle, audit and productized agent capabilities.
- **Academic Grading With AI:** canonical active agentic reference implementation and operational workspace for prompt-driven assessment operations.

Both are valid and active, with different responsibilities and maturity characteristics.
