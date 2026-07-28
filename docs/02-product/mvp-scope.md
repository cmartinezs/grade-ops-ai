# MVP Scope

GradeOps AI MVP is the smallest transversal assessment-operations platform that lets a teacher plan, apply, evaluate, review, publish, correct, and analyze real assessments while remaining the final academic authority.

Programming is an initial validation domain, not a boundary of the product.

## Product Thesis

> A teacher can run a real open, closed, or mixed assessment with AI assistance, retain control of every high-impact decision, publish traceable results, and convert approved evidence into curricular action.

GradeOps AI is not a full LMS, a generic quiz generator, an autonomous grader, or a student chatbot.

## MVP Users and Context

- Independent teacher, tutor, or instructor.
- Teacher working inside an institution.
- Student participating through authenticated access or a secure purpose-limited link.
- Institutional operator/administrator where governance, policy, or audit requires it.

Roles are capability- and resource-scoped. A person may hold more than one profile.

## Foundation Required in the MVP

### Academic organization

- Academic period.
- Course and section.
- Teacher assignment.
- Enrollment or equivalent learner relationship.
- Minimal institutional policy configuration.
- Versioned assessment templates.

### Structured curriculum

```text
Course -> Unit -> Topic -> Learning Objective -> Indicator or expected competency
```

The MVP must support criterion-to-objective alignment, declared teaching coverage, assessed coverage, and demonstrated learning. Free-text tags may remain as compatibility fields but are not the analytical source of truth.

### Assessment modalities

- **Open:** constructed evidence reviewed against rubric criteria.
- **Closed:** objective responses scored deterministically against a frozen answer-key and scoring-policy snapshot.
- **Mixed:** open and closed components in one assessment operation.

Modality is composable. It must not create three unrelated products.

## Independent Lifecycles

The product does not use one global status to represent everything. At minimum it distinguishes:

1. template/definition preparation;
2. application and availability;
3. invitation, attempt, and submission;
4. scoring or AI-assisted evaluation;
5. criterion/evidence review;
6. final-result approval;
7. publication;
8. correction and republication;
9. appeal;
10. curricular analysis and pedagogical action.

UI may expose summary projections, but domain transitions remain independent and auditable.

## Protected End-to-End Slice

1. Teacher selects course, section, curriculum scope, and modality.
2. Teacher creates from a template or drafts an assessment.
3. AI may propose instructions, items, rubric criteria, alignments, and feedback rules.
4. Teacher reviews and approves the applicable version.
5. Publication freezes the assessment, rubric, answer key, scoring policy, and curriculum alignment snapshot.
6. Students participate through the configured access policy; teacher upload remains available when appropriate.
7. Closed components are scored deterministically. Open components receive criterion-level AI proposals with evidence and uncertainty.
8. Teacher reviews exceptions, evidence, criterion judgments, score, and feedback.
9. Material edits invalidate dependent approvals.
10. Teacher approves the final result.
11. Teacher explicitly publishes it.
12. Student receives a privacy-safe notification and accesses the official version.
13. Teacher may start a correction as a new version without changing the current official result.
14. An approved correction requires explicit republication.
15. GradeOps updates the appeal window under the approved academic-business-day rule.
16. Approved evidence feeds the curriculum coverage view and proposed pedagogical actions.

## Human Authority Rules

- AI output is a proposal unless the applicable deterministic policy says otherwise.
- High-impact academic outputs are not silently published.
- Teacher review is criterion- and evidence-aware.
- Bulk approval requires policy eligibility and preserves attribution.
- Editing an approved dependency invalidates downstream approval.
- Approval and publication are different actions.
- Historical published versions are immutable.
- An accidental publication is corrected through a new version, never by erasing history.

## Student Rights and Result Governance

- The current published result remains official while a teacher review is in progress.
- The student sees a neutral review notice with start date and general scope where appropriate.
- Teacher-initiated review and formal student appeal are different processes.
- A corrected result enters `ApprovedPendingRepublication` after approval.
- Only explicit `RepublishEvaluationResult` changes the official version.
- Affected students are notified without sensitive data in subject, preview, or URL.
- A substantive republication guarantees two complete academic business days to appeal, never shortening a more favorable deadline.
- If no valid deadline can be determined, the appeal remains `OpenPendingDeadlineConfiguration`.

## Curriculum Coverage MVP

The primary analytical entry point is:

```text
Unit -> Topic -> Learning Objective -> Criteria -> Cohort/Student evidence
```

Show separate indicators for:

- curriculum worked;
- curriculum assessed;
- objectives with sufficient evidence;
- objectives reaching expected achievement;
- critical objectives at risk;
- open pedagogical gaps.

Initial gap types:

- planned but not taught;
- taught but not assessed;
- assessed with low achievement;
- insufficient evidence.

AI may propose reinforcement, formative activity, reassessment, rubric adjustment, or targeted support. The teacher chooses whether to adopt an action. Analysis is versioned.

## MVP Scope Matrix

| Area | Must Build | Later |
| --- | --- | --- |
| Identity and authorization | Teacher identity, learner relationship, resource-scoped authorization | Advanced federation and delegated administration |
| Academic structure | Period, course, section, enrollment, teacher assignment | Cross-institution networks |
| Curriculum | Structured hierarchy, versioning, criterion alignment, coverage | External benchmarks and complex prerequisite graphs |
| Assessment | Open, closed, mixed; templates and snapshots | Marketplace |
| Participation | Secure link and policy-based authenticated access; attempts/submissions | Advanced proctoring |
| Evaluation | Deterministic closed scoring; AI-assisted open evaluation | Fully autonomous academic decisions |
| Review | Criterion/evidence review, invalidation, exceptions, bulk controls | Multi-stage institutional committees |
| Results | Approval, explicit publication, correction/republication, history | Regulatory workflow packs |
| Appeals | Deadline and status tracking, correction-aware extension | Institution-specific advanced case management |
| Analytics | Curriculum-first coverage, four gaps, versioned snapshots | Prediction and external benchmarking |
| AI operations | Provider/model trace, cost, uncertainty, prompt/version evidence | Autonomous replanning |
| Evidence | Usage, audit, approval, cost, pilot evidence | Enterprise BI |

## Explicitly Out of Scope for the First Cut

- Full LMS authoring and content delivery.
- Student social features or chatbot.
- Fully autonomous grading or publication.
- Prediction of individual performance.
- Cross-teacher or cross-institution ranking.
- National curriculum marketplace.
- Complex proctoring and plagiarism claims.
- Replanning curricula without teacher confirmation.
- A blind rewrite of existing services.

## Acceptance Criteria

The MVP is acceptable when a teacher can demonstrate one real assessment end to end and GradeOps can prove:

1. the academic and curriculum context used;
2. the immutable published assessment snapshot;
3. valid student participation and attempt/submission history;
4. deterministic or evidence-backed criterion results;
5. teacher review and final approval;
6. explicit publication and student visibility;
7. correction/republication without historical mutation;
8. appeal deadline handling;
9. curriculum coverage and actionable gaps;
10. complete actor, version, AI-run, policy, cost, and transition audit.

## Migration Constraint

This is the target product scope. Existing code must first be classified as reusable, adaptable, incompatible, absent, or obsolete. Implementation planning must prefer incremental migration and compatibility projections over an unexamined rewrite.

<!-- nav -->

---

[↑ inicio](#mvp-scope) | [README](README.md) | [Personas →](personas.md)
