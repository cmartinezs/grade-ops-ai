# Mermaid as Diagram Standard

- Status: Accepted
- Date: 2026-06-08
- Decision owner: Documentation / Founder

## Context

GradeOps AI's documentation repo uses diagrams extensively: assessment workflows, agent pipelines, data model relationships, deployment topology, and state machines. A consistent diagramming standard was needed for the following reasons:

- Multiple contributors (human and AI coding agents) will author diagrams.
- Diagrams must render correctly in GitHub without external plugins.
- Diagrams must be version-controlled as plain text alongside the prose they illustrate.
- The documentation must remain accessible and editable without proprietary tooling.

Diagramming options considered:

| Tool | Rendering | Version-control friendly | GitHub native | Notes |
| --- | --- | --- | --- | --- |
| **Mermaid** | Inline in GitHub Markdown | Yes (plain text) | Yes | Supported natively since 2022 |
| **PlantUML** | Requires server or plugin | Yes (plain text) | No (needs rendering server) | More expressive for complex UML |
| **Draw.io / Lucidchart** | Exports PNG/SVG | No (binary or opaque XML) | No | Requires external tool; diagrams drift from text |
| **ASCII art** | Inline everywhere | Yes | Yes | Low precision; difficult to maintain at scale |

## Decision

All diagrams in this repository use **Mermaid** by default.

**PlantUML** is permitted only when Mermaid cannot adequately express the required diagram (e.g., very complex sequence diagrams or class hierarchies beyond Mermaid's expressive range).

**ASCII diagrams** are a last fallback only — acceptable for simple linear flows or quick inline sketches, but not for canonical workflow or architecture diagrams.

External diagramming tools (Draw.io, Lucidchart, Figma) must not be committed as binary exports. If used for exploration, the result must be transcribed into Mermaid before being committed.

## Rationale

- Mermaid renders natively in GitHub Markdown without plugins, making diagrams immediately visible to any reader of the repository.
- Mermaid syntax is readable in its raw form, allowing contributors to understand diagrams without rendering them.
- Plain-text diagrams are diff-friendly: changes are visible in pull requests just like prose changes.
- The diagram types needed (flowcharts, sequence diagrams, entity relationships, state machines) are all well-supported in Mermaid.
- AI coding agents (including Claude Code) produce correct Mermaid syntax reliably, enabling consistent AI-assisted documentation.
- PlantUML requires an external rendering server; introducing it as the default would add tooling friction for contributors.

## Consequences

- All existing and future diagrams in `00-project/` through `99-decisions/` must be authored in Mermaid.
- Contributors and AI coding agents must default to Mermaid; no other format should be used without checking this decision record first.
- `CLAUDE.md` documents this as a binding writing convention for AI coding agents working in this repo.
- If a diagram is genuinely not expressible in Mermaid, the author must note why PlantUML was used in a comment adjacent to the code block.
- The decision should be revisited if Mermaid's renderer in the target publishing environment (NotebookLM, internal wiki, etc.) changes significantly.

<!-- nav -->

---

← [Price by Graded Submission](2026-06-08-price-by-graded-submissions.md) | [↑ inicio](#mermaid-as-diagram-standard) | [README](README.md) | [Closed Assessment Mode →](2026-06-10-closed-assessment-mode.md)
