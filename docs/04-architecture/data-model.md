# Target Conceptual Data Model

This document describes the approved target concepts. It is not a physical schema and does not authorize a direct rewrite. Existing entities will be classified during the code-alignment review and migrated incrementally.

## Design Rules

- Independent lifecycles are not collapsed into one assessment status.
- Published snapshots and historical results are immutable.
- Approval and publication are separate.
- Material dependency changes invalidate downstream approvals.
- AI proposals and human decisions are separate records.
- Curriculum, policy, calendar, rubric, scoring, and result versions remain reproducible.
- Missing evidence is represented explicitly.

## Academic Context

| Concept | Responsibility |
| --- | --- |
| Institution/Workspace | Governance and policy boundary where applicable |
| AcademicPeriod | Versioned academic dates and timezone context |
| Course | Teaching context and curriculum owner |
| Section | Concrete delivery/cohort |
| TeacherAssignment | Actor relationship and authority |
| Enrollment | Learner relationship and validity period |
| AcademicCalendarVersion | Business days, schedules, closures and exceptions |

Independent-teacher use may create a lightweight personal workspace while preserving the same boundaries.

## Curriculum

| Concept | Responsibility |
| --- | --- |
| CurriculumVersion | Immutable framework/plan version |
| CurriculumUnit | Ordered course unit |
| CurriculumTopic | Topic within a unit |
| LearningObjective | Expected learning outcome |
| ObjectiveIndicator | Observable competency/indicator |
| TeachingCoverageDeclaration | Teacher-confirmed taught state and depth |
| AssessmentObjectiveAlignment | Assessment-level scope |
| CriterionObjectiveAlignment | Strength, purpose, depth, directness and provenance |
| ObjectiveAchievementSnapshot | Versioned demonstrated-learning summary |
| CurriculumGap | Typed gap with evidence |
| PedagogicalAction | Proposed and teacher-adopted response |
| CoverageAnalysisSnapshot | Reproducible analysis version |

## Assessment Preparation

| Concept | Responsibility |
| --- | --- |
| AssessmentTemplate | Reusable versioned starting point |
| AssessmentDefinitionVersion | Student-facing definition and component composition |
| AssessmentComponent | Open or closed component inside an assessment |
| RubricVersion/Criterion | Open evidence expectations and scoring structure |
| QuestionVersion/Option | Closed assessment item content |
| AnswerKeyVersion | Approved correct response snapshot |
| ScoringPolicyVersion | Deterministic calculation and combination rules |
| AssessmentPublicationSnapshot | Frozen preparation dependencies |

An assessment summary may project preparation/application states, but the snapshot is the source for attempts and evaluation.

## Participation

| Concept | Responsibility |
| --- | --- |
| AssessmentApplication | Availability and scheduling for a section/audience |
| ParticipationGrant/Invitation | Authenticated or signed-link access |
| AssessmentAttempt | One attributable attempt under policy |
| Submission | Submitted open/closed/mixed response version |
| AttendanceException | Absence, late, recovery or accommodation case |
| AccessToken | Hashed, expiring, purpose-limited, revocable credential |

## Evaluation and Review

| Concept | Responsibility |
| --- | --- |
| EvaluationRun | Processing request and applicable snapshot |
| DeterministicItemOutcome | Closed scoring result with explanation |
| AIProposal | Versioned proposed judgment/feedback/alignment with provenance |
| CriterionReview | Teacher decision, evidence, score and state |
| ReviewApproval | Approval scope and dependencies |
| ApprovalInvalidation | Reason a dependent approval ceased to be valid |
| ResultVersion | Derived score, grade, achievement and feedback |
| ResultApproval | Teacher approval of one result version |

AI provider, model, prompt/template version, token/cost estimate, uncertainty, input/output references, and run status belong to attributable execution records, not inside the official decision alone.

## Publication, Correction, and Appeal

| Concept | Responsibility |
| --- | --- |
| ResultPublication | Makes one approved result version official |
| TeacherReviewCase | Review initiated by teacher against a published version |
| ResultCorrectionVersion | New candidate version branching from official result |
| Republication | Explicit replacement of official version |
| StudentNotification | Privacy-safe delivery record |
| AppealCase | Formal learner appeal, distinct from teacher review |
| AppealDeadline | Current deadline and calculation basis |
| DeadlineCalculation | Calendar, schedule, timezone, version and rule evidence |

A correction approval may enter `ApprovedPendingRepublication`. Only republication changes the official pointer. Previous result versions and publications remain immutable.

If deadline validation is unavailable, the appeal uses `OpenPendingDeadlineConfiguration` and cannot auto-close.

## Audit

Critical commands and transitions append an audit record containing actor, authority context, timestamp, source/target version, policy, reason, affected resources, correlation/causation identifiers, and relevant AI execution references.

Reports and analytics point to source versions; recalculation creates a new snapshot.

## Compatibility and Migration

Existing entities and enums may be retained temporarily as:

- source records to migrate;
- compatibility projections;
- API adapters;
- historical representations.

They must not be silently reinterpreted as the target model. The alignment review will classify each implementation concept as reusable, adaptable, incompatible, absent, or obsolete and define data/backfill/contract migration per release.

## Model Acceptance Questions

The implemented model must answer:

1. Which assessment, curriculum, rubric, answer-key, scoring-policy and calendar versions applied?
2. Which learner relationship and attempt produced the submission?
3. What was deterministic, what was proposed by AI, and what did the teacher decide?
4. Which evidence supports each criterion and objective outcome?
5. Who approved and who published each result version?
6. Which version is official and which versions are historical?
7. Why was an approval invalidated?
8. What changed in a correction and when was it republished?
9. What appeal deadline applies and how was it calculated?
10. Which curriculum gap and pedagogical action came from which approved evidence?
11. Which agent/model/prompt ran, at what cost, with what uncertainty?
12. Can every historical report be reproduced without mutating its sources?

<!-- nav -->

---

← [System Architecture](system-architecture.md) | [↑ inicio](#target-conceptual-data-model) | [README](README.md) | [API Design →](api-design.md)
