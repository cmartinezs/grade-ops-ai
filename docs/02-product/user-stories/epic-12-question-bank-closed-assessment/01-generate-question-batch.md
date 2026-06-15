# US-110: Generate Question Batch With AI

- **Epic:** 12 — Question Bank and Closed Assessment Creation
- **Priority:** P0
- **ID:** US-110

## Story

As a teacher, I want to generate a batch of objective questions from a learning objective so I can build an assessment bank quickly.

## Acceptance Criteria

- Teacher defines subject, topic, difficulty, question count, and type (TF/SC/MC).
- Question Generation Agent produces questions with alternatives, answer key, explanation, and difficulty.
- Output enters review queue in `pending_review` state.
- Agent run is logged with cost estimate.
