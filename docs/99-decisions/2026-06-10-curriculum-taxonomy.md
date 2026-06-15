# Curriculum Taxonomy for Questions and Assessments

- Status: Accepted
- Date: 2026-06-10
- Decision owner: Product

## Context

GRADE, the prior product, organized educational content around a simple three-level hierarchy: `Subject > Unit > Topic`. This structure was sufficient for filtering questions and composing assessments within a known domain.

GradeOps AI uses AI agents to generate questions and compose assessments. Without a curriculum anchor, the Question Generation Agent lacks scope boundaries: it may generate questions that are off-level, off-topic, or misaligned with the teacher's declared learning objectives. The Assessment Assembly Agent cannot guarantee coverage of specific learning outcomes unless questions carry curriculum metadata.

The integration analysis (`.raw/16-chat.md`) identified the need for a layered curriculum taxonomy that works at two levels:

1. A simple string-based model sufficient for the hackathon MVP and independent teachers.
2. A structured, versionable model that supports institutional use, national curricula, and multi-country expansion.

## Decision

GradeOps AI adopts a **two-layer curriculum taxonomy**:

**P0 — String-based tagging:** Questions and assessments carry `subject_area` (string), `topic_tags_json` (JSON array), and `learning_outcome` (string). These are sufficient for filtering, AI generation scoping, and basic reporting. No relational curriculum entities are required at P0.

**P1 — Structured taxonomy:** A relational curriculum model is introduced with entities `Subject`, `CurriculumNode`, `LearningObjective`, and optionally `CurriculumProvider`, `CurriculumFramework`, `CurriculumVersion`, and `EducationLevel`. This model supports national curricula (e.g., Chilean Currículum Nacional), institutional programs, and AI-generated curriculum structures.

At all times, the P0 string fields are backward-compatible with the P1 model: the transition adds relational IDs as foreign keys without removing the string fields immediately.

## Rationale

- A fully relational curriculum model at P0 adds development complexity without delivering hackathon value.
- String-based tagging still allows the Question Generation Agent to scope generation accurately when the teacher provides subject and topic labels.
- The P1 model matches the long-term direction of AI-native assessment operations: AI must know exactly which learning objectives it is targeting to produce reliable question batches and coverage reports.
- GRADE demonstrated that simple Subject > Topic structure is enough to get started; GradeOps AI needs to extend this model rather than copy it as-is, since AI generation requires richer metadata.
- Designing the P1 model now (even if not built) prevents a schema migration that breaks the P0 model later.

## Consequences

- Questions must carry at minimum `subject_area` and `learning_outcome` to transition from `draft` to `active` state in the bank.
- The Question Generation Agent receives subject, topic, and learning outcome as inputs; it also suggests these values for questions it generates.
- The Assessment Assembly Agent uses declared subject and outcome scope to filter eligible bank questions; mismatches trigger alerts.
- Curriculum-based reports (performance by topic, by learning outcome) are enabled at P0 using string aggregation; richer reports require the P1 structured model.
- The `CurriculumProvider` / `CurriculumFramework` architecture must be designed to avoid hardcoding any country's curriculum into the core model; each country is a separate provider.
- The Chile national curriculum pack (`Ministerio de Educación de Chile` as `CurriculumProvider`) is the first concrete P1 implementation target.

<!-- nav -->

---

← [Student Access via Secure Link](2026-06-10-student-access-via-secure-link.md) | [↑ inicio](#curriculum-taxonomy-for-questions-and-assessments) | [README](README.md) | [Technology Stack →](2026-06-10-technology-stack.md)
