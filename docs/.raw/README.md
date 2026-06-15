# Raw Conversation Notes

This folder stores formatted source conversations and reasoning history for GradeOps AI.

It answers:

> Where did the current project decisions come from, and what reasoning led to the canonical docs?

## Essence

`.raw` is historical context, not the canonical product specification.

The files in this folder preserve:

- early strategic exploration;
- naming decisions;
- hackathon interpretation;
- technical reasoning;
- cost and pricing analysis;
- documentation review summaries;
- folder-by-folder alignment notes.

Use these notes to understand how decisions evolved. Do not treat them as the current source of truth when they conflict with the thematic folders.

## Canonical Sources

Prefer the thematic folders for current decisions:

| Topic | Canonical Folder |
| --- | --- |
| Project strategy | `../00-project/` |
| Business model and GTM | `../01-business/` |
| Product scope and workflows | `../02-product/` |
| Agent contracts | `../03-ai-agents/` |
| Architecture | `../04-architecture/` |
| Evidence | `../05-evidence/` |
| Durable decisions | `../99-decisions/` |

## File Index

| File | Essence |
| --- | --- |
| `00-chat.md` | First strategic reading of the hackathon and the initial ClassOps/assessment-ops framing. |
| `01-chat.md` | Pivot from broad GRADE framing to GradeOps AI as the hackathon positioning. |
| `02-chat.md` | Strategic reading of the TodoDeIA Gemini/XPRIZE article. |
| `03-chat.md` | Technical reading of Gemini, agents, tools, structured outputs, ADK, and Cloud Run. |
| `04-chat.md` | Cost, credits, pricing, unit economics, and operating-budget exploration. |
| `05-chat.md` | Personal AI development tooling versus production runtime costs. |
| `06-chat.md` | Review summary for canonical `00-project` documentation. |
| `07-chat.md` | Review summary for aligning `01-business` with `00-project`. |
| `08-chat.md` | Review summary for aligning `02-product` with project and business docs. |
| `09-chat.md` | Review summary for aligning `03-ai-agents` with project/product docs. |
| `10-chat.md` | Review summary for aligning `04-architecture` with prior canonical docs. |
| `11-chat.md` | Conceptual correction around student responses, `StudentSubmission`, and plan limits. |
| `12-chat.md` | UX/Product gap analysis: absorption of GRADE capabilities for objective/alternatives assessments into GradeOps AI. Information architecture, flows, functional requirements, edge cases, and conceptual data model. |
| `13-chat.md` | Strategic correction: the Question Bank is AI-native in GradeOps AI, not manually authored. AI generates questions; teacher curates. New agents: Question Generation, Distractor Quality, Ambiguity Review, Assessment Assembly, Item Analytics. |
| `14-chat.md` | Unified product model: open assessments (practical/rubric) + closed assessments (alternatives/deterministic), student access via secure links without account, grade result associated to subject, weighted scoring model. |
| `15-chat.md` | Response ingestion: digital channel (student portal via link) and physical channel (paper answer sheet, OMR/QR, docente validation). Canonical for PaperAnswerSheet, PaperCapture, ExtractedAnswer, and related UX flows. |

## How To Use This Folder

Use `.raw` when you need to:

- trace why a decision was made;
- recover earlier alternatives;
- compare old naming or pricing assumptions;
- understand the reasoning behind canonical docs;
- audit whether later docs drifted from the original strategy.

## What Does Not Belong Here

- Current product specifications.
- Final business strategy.
- Implementation contracts.
- Architecture decisions that should be durable.
- Evidence records.
- Customer data.

Move durable decisions to `../99-decisions/`. Move canonical specs to the matching thematic folder.

## Conflict Rule

If `.raw` conflicts with a thematic document, prefer the thematic document.

If `.raw` reveals a stronger or newer decision, promote that decision into the correct thematic folder or a decision record in `../99-decisions/`.

## Formatting Rule

Raw notes should still be readable:

- use professional Markdown;
- keep the original prompt when useful;
- separate diagnosis, decisions, changes, and outcomes;
- use Mermaid for diagrams by default, PlantUML only when needed, and ASCII only as a last fallback.
