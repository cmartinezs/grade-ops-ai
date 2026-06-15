# Epic 02 — Assessment Creation

## Narrative

**As a** programming instructor,  
**I want** to describe my learning goal and constraints and have the AI generate a structured assessment draft,  
**so that** I spend my time reviewing and refining rather than writing from scratch.

## Goal

Enable the first step of the open-assessment pipeline: turning a teacher's learning intent into a structured, AI-generated assessment draft that is editable and fully logged.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-010 | Assessment Brief Intake | P0 | [01-assessment-brief-intake.md](01-assessment-brief-intake.md) |
| US-011 | Assessment Draft Generation | P0 | [02-assessment-draft-generation.md](02-assessment-draft-generation.md) |
| US-012 | Assessment Draft Regeneration | P1 | [03-assessment-draft-regeneration.md](03-assessment-draft-regeneration.md) |

## Scope

**In scope**
- Intake form: learning goal, topic, level/difficulty, duration, and programming language
- Assessment Agent execution producing a structured draft (title, context, instructions, objectives, deliverables, constraints)
- Draft editing by the teacher before proceeding to rubric generation
- Regeneration with adjustment notes (P1)

**Out of scope**
- Closed-assessment (objective question) creation — see Epic 11
- Auto-saving drafts as the teacher types (brief is saved before agent execution, not keystroke-by-keystroke)
- Multi-language or non-programming assessments (MVP is programming-only)

## Epic Acceptance Criteria

- Teacher can fill in the brief form and trigger Assessment Agent execution.
- The agent returns a structured draft with all required fields (title, context, instructions, objectives, deliverables, constraints).
- The draft is immediately editable by the teacher.
- The agent execution is logged with model, cost estimate, and status.
- Input is persisted before the agent call so a failure does not lose the teacher's brief.
- (P1) Teacher can request regeneration with adjustment notes; the previous version remains traceable.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Teacher must be authenticated and have an active workspace |

## Definition of Done

- All P0 stories pass their acceptance criteria.
- Assessment Agent is integrated and called server-side only.
- Every agent run produces an `AgentExecutionLog` record.
- Draft is stored in the database and retrievable after page refresh.
- Cost estimate and model name are captured in the execution log.
- Gemini API key is never exposed to the frontend.
