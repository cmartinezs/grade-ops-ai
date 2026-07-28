<a id="top"></a>

# 00 — Executive Summary

**Parent:** [README](README.md) · **Status:** Planned · **Next:** [01 — Current State and Target →](01-current-state-and-target.md)

## Why

Assessment authoring works end-to-end today (create brief → generate → regenerate → edit), but three defects block building anything on top of it: human edits silently destroy the AI-generated content they replace; a failed initial generation leaves a dead end that a form resubmit "fixes" by creating a second `Assessment`; and AI execution evidence is written only *after* the external call returns, so a crash between provider response and persistence loses all record that the call happened. None of these are missing features — they are correctness defects in the one workflow that already ships.

## What

Four decisions ([docs/99-decisions/2026-07-28-*](../../99-decisions/README.md)) close the technical design gap between the accepted product redesign and the current code:

1. Replace mutable `AssessmentDraft` with immutable `AssessmentRevision` (origin, actor, timestamp, predecessor on every row).
2. Make the create/generate/regenerate/edit operations idempotent, resumable, and CAS-protected against concurrent mutation.
3. Introduce `AiOperation`/`AgentAttempt` so evidence of an AI dispatch exists before the call is made, not after.
4. Leave `AssessmentStatus`, organizations, curriculum, submissions, grading, results, and publication untouched — none of them are needed to fix the three defects above, and touching them would violate the additive-migration principle this plan follows.

## How

Fourteen tasks ([08 — Implementation Sequence](08-implementation-sequence.md)), ordered so the repository stays compilable — and testable at almost every step — throughout: additive schema first, new domain aggregates next (unused by any handler yet), then the coordinator rewrite that wires everything together, then API contract changes, then Web migration, then the one-time legacy data backfill, executed last so it runs against a fully-built and tested target schema.

## What does not change

`Assessment` stays a minimal identity aggregate. `AssessmentBrief` stays immutable and unchanged. No new `AssessmentStatus` values. No organizations, sections, students, submissions, grading, results, publication, appeals, batch processing, or full credits ledger. No Kafka, queue, or microservice. The AI output remains a proposal — nothing in this cut lets an agent finalize or publish anything.

---

[↑ README](README.md) · [Siguiente: Current State and Target →](01-current-state-and-target.md)
