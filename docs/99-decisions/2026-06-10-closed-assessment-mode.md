# Closed Assessment Mode

- Status: Accepted
- Date: 2026-06-10
- Decision owner: Product

## Context

GradeOps AI was initially scoped around open/practical programming assessments: code submissions, rubric-based grading, AI suggestions. This is the core open assessment mode.

GRADE, a prior product, documented a full cycle for objective assessments: question banks, multiple choice, deterministic grading, snapshots, and item analytics. The integration analysis (`.raw/12-chat.md`, `.raw/13-chat.md`, `.raw/14-chat.md`) identified that combining both modes under GradeOps AI makes the product more broadly sellable without requiring a full LMS.

## Decision

GradeOps AI will support two assessment modes:

1. **Open Assessment** — existing mode; practical, code/text/file, rubric-based, AI grading suggestion, teacher approval.
2. **Closed Assessment** — new mode; objective questions, alternatives (TF/SC/MC), deterministic grading against a frozen answer key, AI-assisted question generation and curation.

Both modes share the same assessment lifecycle envelope, teacher workspace, and evidence/audit infrastructure.

## Rationale

- The two modes cover the most common assessment types for programming educators: practical exercises and knowledge checks.
- Closed assessments have a lower teacher review burden after publishing, making the product more scalable.
- Adding closed assessments does not require building a full LMS; it is a self-contained operational flow.
- GRADE provided a proven data model for the closed mode; adoption reduces design risk.
- A product that handles both modes positions GradeOps AI as a full assessment operations platform, not just a code-grading tool.

## Consequences

- New agents are required: Question Generation, Distractor Quality, Ambiguity Review, Assessment Assembly, Item Analytics.
- New entities are required in the data model: `Question`, `QuestionOption`, `QuestionVersion`, `AssessmentQuestionSnapshot`, `ScoringPolicy`, `LearnerRef`.
- Grading for closed assessments is deterministic; AI does not suggest scores.
- The teacher workspace must support two assessment creation paths.
- MVP must prototype the closed assessment flow; it does not need full feature parity with the open flow on day one.

<!-- nav -->

---

← [Mermaid as Diagram Standard](2026-06-08-mermaid-diagram-standard.md) | [↑ inicio](#closed-assessment-mode) | [README](README.md) | [AI-Native Question Bank →](2026-06-10-ai-native-question-bank.md)
