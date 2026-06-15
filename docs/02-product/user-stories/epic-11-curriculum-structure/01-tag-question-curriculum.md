# US-100: Tag Question With Subject and Learning Outcome

- **Epic:** 11 — Subject and Curriculum Structure
- **Priority:** P0
- **ID:** US-100

## Story

As a teacher, I want every question in the bank to carry a subject, topic, and learning outcome so the system can filter the bank correctly and AI agents have a clear generation scope.

## Acceptance Criteria

- Teacher can set `subject_area`, topic tags, and `learning_outcome` on each question.
- Question Generation Agent suggests these values; teacher confirms at curation.
- A question without at least a `subject_area` and `learning_outcome` cannot be set to `active`.
- Assessment assembly filters available bank questions by the declared subject and outcome scope.
