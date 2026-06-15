# Epic 12 — Question Bank and Closed Assessment Creation

## Narrative

**As a** teacher running a closed assessment (objective questions),  
**I want** to generate batches of questions with AI, curate them into a bank, compose a balanced assessment, and publish it with a frozen snapshot,  
**so that** I can deliver consistent, auditable objective evaluations without manually writing every question.

## Goal

Enable the full closed-assessment authoring pipeline: AI-assisted question generation → teacher curation → question bank → assessment composition → publish with immutable snapshot. Grading for closed assessments is always deterministic against the frozen answer key; AI agents generate and analyze, never score.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-110 | Generate Question Batch With AI | P0 | [01-generate-question-batch.md](01-generate-question-batch.md) |
| US-111 | Review AI-Generated Questions | P0 | [02-review-generated-questions.md](02-review-generated-questions.md) |
| US-112 | Question Bank | P0 | [03-question-bank.md](03-question-bank.md) |
| US-113 | Compose Closed Assessment From Bank | P0 | [04-compose-closed-assessment.md](04-compose-closed-assessment.md) |
| US-114 | Publish Closed Assessment and Freeze Snapshot | P0 | [05-publish-freeze-snapshot.md](05-publish-freeze-snapshot.md) |
| US-115 | Annul Question and Recalculate | P1 | [06-annul-question-recalculate.md](06-annul-question-recalculate.md) |

## Scope

**In scope**
- Question Generation Agent: TF / SC / MC questions with alternatives, answer key, explanation, and difficulty from a teacher-defined scope
- Distractor Quality Agent and Ambiguity Review Agent flagging weak or ambiguous questions during review
- Question states: `pending_review` → `active` / `rejected` / `retired`
- Searchable and filterable question bank (active questions only by default)
- Assessment Assembly Agent proposing a balanced question set; teacher can swap or manually add
- Scoring policy and grade scale defined by teacher before publish
- Publish creates immutable snapshots of questions, options, answer key, scoring policy, and grade scale
- Post-publish annulment of a question with audited recalculation of all student scores (P1)

**Out of scope**
- Open-assessment (rubric-based) workflows — see Epics 02–08
- Importing questions from external question banks or IMS QTI format
- Automatic student grading via AI (grading is deterministic against the answer key snapshot)
- Student-facing assessment delivery (handled by Epic 13)

## Epic Acceptance Criteria

- Teacher can define scope (subject, topic, difficulty, count, type) and trigger Question Generation Agent.
- Generated questions enter a `pending_review` queue with quality flags from Distractor Quality and Ambiguity Review agents.
- Teacher can approve, edit, regenerate, or reject each question; all actions are audited.
- Approved questions enter the bank in `active` state; rejected questions are logged.
- Bank is searchable and filterable by subject, topic, difficulty, type, learning outcome, and status.
- Assessment Assembly Agent proposes a question set given the teacher's constraints; teacher can accept or swap.
- Teacher validates that all selected questions have an approved answer key before publishing.
- Publishing creates an immutable snapshot; the published assessment cannot be structurally edited.
- (P1) Teacher can annul a question post-grading with a mandatory reason; system recalculates all scores and logs the event; original results are preserved.
- All agent runs produce `AgentExecutionLog` records.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Authenticated teacher workspace required |
| Epic 11 — Subject and Curriculum Structure | Subject, topic, and learning outcome tagging is required to activate questions in the bank and scope question generation |

## Definition of Done

- All P0 stories pass their acceptance criteria.
- Published assessment snapshot is immutable; no code path can modify it after publish without explicit annulment.
- Deterministic grading runs against the frozen answer key, not live bank data.
- All three quality agents (Generation, Distractor, Ambiguity) produce `AgentExecutionLog` records.
- Annulment recalculation preserves original results in the audit trail.
