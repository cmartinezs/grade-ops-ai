# Assessment Operations Product Redesign

- Status: Accepted
- Date: 2026-07-27
- Decision owner: Product
- Supersedes in part: `2026-06-10-curriculum-taxonomy.md`, the programming-only MVP framing, and any single-state assessment workflow

## Context

GradeOps AI began with programming assessments as an initial market wedge. Product discovery established a broader and more precise target: a transversal assessment-operations platform for independent teachers and institutions, supporting different disciplines and assessment modalities while preserving human authority, evidence, version history, and student rights.

The previous documentation mixed assessment preparation, application, grading, review, approval, publication, correction, and reporting in one simplified lifecycle. It also treated structured curriculum, course organization, and institutional governance as later concerns. Those assumptions no longer describe the approved product.

## Decision

### Product boundary

GradeOps AI is an assessment-operations platform, not a programming-only grader, a generic quiz generator, a full LMS, or an autonomous decision maker. Programming remains a useful initial wedge and validation domain, but it does not constrain the core model.

The product supports independent teachers and institutional contexts. Profiles and capabilities may be combined; authorization is determined by actor, action, resource, relationship, and institutional policy rather than by one global role label.

### Academic organization

The target model includes versioned academic periods, courses, sections, enrollments, teacher assignments, groups where required, and reusable assessment templates. Student participation may use authenticated access or secure, purpose-limited links according to policy. Secure links do not eliminate enrollment, attempt, audit, or privacy rules.

### Assessment model

Assessment modality is composable:

- open: constructed evidence evaluated against criteria;
- closed: responses scored deterministically from an approved snapshot;
- mixed: open and closed components under one assessment operation.

Preparation, application, submission, grading, review, result approval, publication, correction/republication, appeal, and pedagogical analysis are separate lifecycles. No single `status` may be treated as the authoritative state for all of them.

Templates, assessment definitions, rubrics, questions, answer keys, policies, and published artifacts are versioned. Publication freezes the applicable snapshot. Later edits create a new version; they do not mutate historical attempts or results silently.

### Human authority and AI

AI may draft, classify, align, summarize, detect patterns, and recommend actions. It does not silently publish high-impact academic outputs.

For open evidence, AI-generated scores, criterion judgments, feedback, and curricular alignments are proposals until reviewed under the applicable policy. Closed scoring is deterministic against the published answer-key and scoring-policy snapshot; exceptions remain reviewable.

Review is criterion- and evidence-aware. Material edits invalidate dependent approvals. Bulk approval is allowed only when policy and risk controls permit it, and must remain attributable and reversible through a new version.

### Results, publication, correction, and appeal

Approval and publication are distinct:

- an approved result is not student-visible merely because it was approved;
- publication is an explicit teacher action;
- the official published version remains in force while a correction draft is under review;
- a correction is created as a new version;
- an approved correction enters `ApprovedPendingRepublication`;
- only explicit `RepublishEvaluationResult` makes it official and notifies affected students;
- previous versions remain immutable and auditable.

A teacher-initiated review is distinct from a formal student appeal. While a published result is under teacher review, the student sees a neutral review notice and the current official result remains visible.

Republishing a substantive correction preserves the existing appeal deadline when it is more favorable and guarantees at least two complete academic business days. Counting begins on the next academic business day and expires at the close of the second. The applicable calendar, schedule, timezone, and version are recorded. If a valid deadline cannot be determined, republication is permitted but the appeal remains `OpenPendingDeadlineConfiguration` until a valid deadline is configured and communicated.

### Curriculum and pedagogical analytics

Structured curriculum is part of the target product and MVP foundation:

```text
Course -> Unit -> Topic -> Learning Objective -> Indicator or expected competency
```

The primary analytical entry point is curriculum coverage, with drill-down to criteria, cohort, and student. GradeOps compares five distinct layers:

1. planned curriculum;
2. curriculum declared as taught;
3. curriculum actually assessed;
4. demonstrated learning from approved evidence;
5. gaps and teacher-adopted pedagogical actions.

Coverage, achievement, and measurement confidence are separate metrics. Missing or insufficient evidence is never represented as low achievement.

The initial analytical cut includes criterion-to-objective alignment, declared teaching coverage, accumulated assessed coverage, cohort achievement per objective, versioned analysis snapshots, and four gap types: not taught, not assessed, low achievement, and insufficient evidence. AI recommendations are non-binding proposals.

### Auditability and privacy

Every critical transition records actor, timestamp, source version, target version, policy, reason, and affected resources. Student-sensitive data must not appear in notification subjects, previews, or URLs. Historical versions, approvals, publications, corrections, deadlines, appeals, and analysis snapshots are retained according to policy.

## Consequences

- Existing code and schemas are implementation evidence, not authority when they conflict with this decision.
- A code-alignment review is required before implementation planning.
- Existing single-status enums may remain temporarily as compatibility projections, but new design must model independent lifecycles.
- String curriculum tags may be retained only as migration/display compatibility fields; they are not the target source of truth.
- Existing programming-specific UX and examples become optional specialization, not core product semantics.
- API, persistence, events, authorization, agents, and UI require an incremental migration plan; this decision does not authorize a blind rewrite.
- Unresolved aggregate boundaries, transaction boundaries, contracts, and migration mechanics are technical design work informed by the code review.

## Deferred evidence

Validate terminology, workflow fit, and policy defaults with teachers from multiple disciplines and with institutional stakeholders. This validation may refine defaults and UX, but does not reopen the foundational separations established above.
