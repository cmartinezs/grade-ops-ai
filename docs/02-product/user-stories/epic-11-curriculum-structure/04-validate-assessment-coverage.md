# US-103: Validate Assessment Curriculum Coverage

- **Epic:** 11 — Subject and Curriculum Structure
- **Priority:** P1
- **ID:** US-103

## Story

As a teacher, I want the system to warn me if my closed assessment does not cover all declared learning outcomes so I can fix gaps before publishing.

## Acceptance Criteria

- System checks that each declared learning outcome has at least one question in the composition.
- Missing outcomes generate a non-blocking alert with option to generate additional questions or acknowledge the gap.
- System warns if questions from an unrelated subject are included.
