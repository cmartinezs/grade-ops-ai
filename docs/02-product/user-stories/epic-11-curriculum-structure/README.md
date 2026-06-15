# Epic 11 — Subject and Curriculum Structure

## Narrative

**As a** teacher building a question bank,  
**I want** every question tagged with a subject, topic, and learning outcome, and I want the option to generate a curriculum structure with AI,  
**so that** the bank is filterable, assessment composition is meaningful, and coverage gaps are detected before publishing.

## Goal

Provide the metadata backbone for the closed-assessment question bank. Curriculum structure is not a standalone product — it exists to make question generation scope explicit, bank filtering accurate, and assessment assembly coverage-aware. It is a prerequisite for activating questions in Epic 12.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-100 | Tag Question With Subject and Learning Outcome | P0 | [01-tag-question-curriculum.md](01-tag-question-curriculum.md) |
| US-101 | Filter Question Bank by Curriculum Metadata | P0 | [02-filter-question-bank.md](02-filter-question-bank.md) |
| US-102 | Generate Curriculum Structure With AI | P1 | [03-generate-curriculum-with-ai.md](03-generate-curriculum-with-ai.md) |
| US-103 | Validate Assessment Curriculum Coverage | P1 | [04-validate-assessment-coverage.md](04-validate-assessment-coverage.md) |

## Scope

**In scope**
- Mandatory tagging of each question with `subject_area`, topic tags, and `learning_outcome` before it can be set to `active`
- Question Generation Agent suggesting these values at generation time; teacher confirms during curation
- Bank filtering by subject, topic, difficulty, type, learning outcome, and status; filters are combinable
- AI-generated curriculum structure (subject, units/topics, learning outcomes) from a course description, entering `pending_review` before use (P1)
- Coverage validation warning when a closed assessment composition does not include all declared learning outcomes (P1, non-blocking)

**Out of scope**
- Curriculum standards alignment (e.g., ACM, IEEE, national frameworks)
- Competency mapping beyond learning outcomes
- Automatic re-tagging of existing questions when the curriculum structure changes

## Epic Acceptance Criteria

- Every question in the bank has `subject_area` and `learning_outcome` set; a question without these cannot transition to `active`.
- Question Generation Agent suggests subject, topic, and learning outcome at generation time; teacher confirms during curation review.
- Bank filter supports: `subject_area`, topic tag, `learning_outcome`, difficulty, question type, and status.
- Filters are combinable; results are accurate (a question tagged "OOP / Inheritance" does not appear in a filter for "OOP / Polymorphism" unless also tagged).
- (P1) Teacher can describe a course and trigger AI to propose a curriculum structure (subject, units, learning outcomes); structure enters `pending_review` and requires teacher approval before use.
- (P1) When composing a closed assessment, the system warns if any declared learning outcome has no corresponding question; warning is non-blocking with options to generate more questions or acknowledge the gap.
- (P1) System warns if questions from an unrelated subject are included in the composition.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Authenticated teacher workspace required |

## Definition of Done

- All P0 stories pass their acceptance criteria.
- Database enforces that a question cannot be set to `active` without `subject_area` and `learning_outcome`.
- Bank filter queries are accurate and tested with overlapping topic tags.
- (P1) AI-generated curriculum structure is stored in `pending_review` state and cannot be used for question generation scope until teacher-approved.
- (P1) Coverage validation runs at composition time and surfaces warnings in the UI without blocking publication.
