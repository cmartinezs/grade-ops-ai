# Structured Curriculum and Coverage Analytics

- Status: Superseded
- Date: 2026-06-10
- Superseded on: 2026-07-27
- Decision owner: Product
- Superseded by: [Assessment Operations Product Redesign](2026-07-27-assessment-operations-product-redesign.md)

## Historical context

The original decision introduced two layers: string tags at P0 and a relational curriculum model at P1. That was appropriate for the earlier programming-focused MVP, but it conflicts with the approved curriculum-first analytics and transversal assessment model.

## Current decision

This record is retained for history. Its P0/P1 sequencing is no longer normative.

Structured, versioned curriculum is now part of the product and MVP foundation:

```text
Course -> Unit -> Topic -> Learning Objective -> Indicator or expected competency
```

Assessments and rubric criteria align to one or more learning objectives with explicit strength, purpose, expected cognitive depth, and provenance. AI may propose alignments; the teacher confirms or edits them.

GradeOps distinguishes:

- planned curriculum;
- curriculum declared as taught;
- curriculum actually assessed;
- demonstrated learning;
- detected gaps and adopted pedagogical actions.

String fields such as subject, topic, or learning-outcome text may remain during migration for compatibility and display. They are not the target source of truth and cannot support the approved analytical model by themselves.

## Consequences

- Reports must not conflate content mentioned, taught, assessed, and achieved.
- Historical analysis retains the curriculum version and alignment rules used at the time.
- Missing evidence is `insufficient evidence`, not low achievement.
- Country or institution curriculum packs remain adapters/providers; the core model is not hardcoded to one jurisdiction.
- Migration from existing string tags requires reconciliation rather than destructive replacement.
