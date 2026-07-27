# Product Workflows

This document defines the target functional workflows approved in product discovery. Detailed state machines, aggregate boundaries, API contracts, and migration mechanics are technical design work to be derived after the code-alignment review.

## Governing Principles

- Teachers retain final academic authority.
- AI proposals and official decisions are visibly different.
- Approval and publication are separate.
- Every published artifact is backed by an immutable version snapshot.
- Material changes invalidate dependent approvals.
- Preparation, application, evaluation, review, publication, correction, appeal, and analytics have independent lifecycles.
- Missing evidence is not low achievement.

## Workflow Map

| Workflow | Primary actors | Outcome |
| --- | --- | --- |
| Academic setup | Teacher, institutional operator | Period, course, section, enrollment, assignments, policies |
| Curriculum planning | Teacher | Versioned unit/topic/objective plan |
| Assessment preparation | Teacher, AI | Reviewable open/closed/mixed assessment version |
| Publication and participation | Teacher, student, system | Frozen snapshot and attributable attempts/submissions |
| Evaluation | System, AI, teacher | Deterministic scores or evidence-backed proposals |
| Review and approval | Teacher | Criterion-reviewed approved result |
| Result publication | Teacher, student | Explicit official result version |
| Correction and republication | Teacher, student | New official version without historical mutation |
| Appeal | Student, teacher/institution | Traceable formal review with valid deadline |
| Curriculum analysis | Teacher, AI | Coverage, demonstrated learning, gaps, adopted actions |

## 1. Academic and Curriculum Setup

```text
Configure period/course/section
  -> assign teacher and learners
  -> select or create curriculum version
  -> define unit/topic/objectives
  -> record planned schedule and importance
  -> make curriculum available to assessments
```

Teaching coverage is declared separately from planning. GradeOps may suggest that an objective was worked from activity evidence, but the teacher confirms it.

## 2. Assessment Preparation

```text
Select course + section + curriculum scope
  -> choose open, closed, or mixed modality
  -> start from template or new definition
  -> AI drafts eligible components
  -> teacher edits and reviews
  -> align criteria/items to objectives
  -> validate rubric/answer key/policy
  -> approve preparation version
```

Open components use rubrics and evidence expectations. Closed components use questions, options, a deterministic answer key, and scoring policy. Mixed assessments preserve component semantics and combine them through an explicit approved result policy.

## 3. Publication and Participation

```text
Approved preparation version
  -> teacher publishes assessment
  -> freeze assessment snapshot
  -> create invitations/access grants
  -> student starts attributable attempt
  -> autosave where applicable
  -> submit or record absence/late/recovery case
  -> close according to policy
```

Secure links are signed, purpose-limited, expiring, revocable, and non-sensitive in their visible URL. They identify access, not authorization to unrelated resources.

## 4. Evaluation

### Closed component

```text
Submitted response
  -> load published answer-key/scoring snapshot
  -> deterministic calculation
  -> record item outcomes and explanation
  -> route policy exceptions to teacher
```

### Open component

```text
Submitted evidence
  -> AI analyzes each criterion
  -> produce proposed judgment, score, evidence, omissions, uncertainty
  -> teacher review queue
```

AI results never overwrite teacher work. Reprocessing creates a new proposal version with provenance.

## 5. Criterion Review and Final Approval

```text
Review evidence per criterion
  -> accept/edit/reject proposal
  -> resolve uncertainty and exceptions
  -> review derived score, grade, achievement and feedback
  -> approve final result
```

A material edit to evidence interpretation, criterion result, scoring policy, or feedback dependency invalidates dependent approvals. Bulk approval is limited to policy-eligible low-risk items and remains attributable.

Final approval produces an approved result version. It does not publish it.

## 6. Result Publication

```text
Approved result version
  -> show publication confirmation and affected students
  -> teacher executes PublishEvaluationResult
  -> version becomes official
  -> prior official version, if any, remains historical
  -> privacy-safe student notification
```

Students see only the official published version and the history/notice allowed by policy.

## 7. Teacher-Initiated Review and Correction

```text
Teacher starts review of published result
  -> current official result stays visible and valid
  -> student sees neutral review-in-progress notice
  -> correction draft branches from official version
  -> teacher reviews and approves corrected version
  -> state: ApprovedPendingRepublication
  -> teacher explicitly executes RepublishEvaluationResult
  -> corrected version becomes official
  -> affected student is notified
```

Discarding the draft removes the review notice and leaves the official version unchanged. Publication mistakes are repaired with another version; data is not deleted to simulate rollback.

## 8. Appeal

A formal student appeal is distinct from teacher-initiated review.

On substantive republication:

```text
Keep current deadline when more favorable
  -> otherwise start at next academic business day
  -> guarantee two complete academic business days
  -> expire at close of second day
  -> record calendar, schedule, timezone and version
```

If GradeOps cannot validate a deadline, republication may proceed but the appeal remains `OpenPendingDeadlineConfiguration`. It cannot close automatically until configuration and communication are complete.

## 9. Curriculum Analysis and Pedagogical Action

```text
Approved result evidence
  -> aggregate by objective and criterion
  -> compare planned / taught / assessed / demonstrated
  -> calculate evidence sufficiency and achievement separately
  -> detect gaps
  -> AI proposes actions
  -> teacher adopts, edits, rejects, or defers
  -> save versioned analysis snapshot
```

The primary view is unit, topic, and learning objective. Cohort and student views are drill-downs. Initial gaps are not taught, not assessed, low achievement, and insufficient evidence.

## Audit Requirements

Each critical transition records:

- actor and authority context;
- occurred-at timestamp and academic timezone;
- source and target version;
- applicable policy and calendar version;
- reason or teacher note;
- affected course, assessment, result, and students;
- AI provider/model/prompt/version when applicable;
- cost, uncertainty, and review outcome when applicable.

## Failure Rules

- A failed agent run does not lose submissions or teacher edits.
- Retry creates attributable execution history.
- A missing dependency blocks only the dependent transition.
- No background process silently changes an official result.
- No recalculation silently rewrites a historical report or curriculum snapshot.

<!-- nav -->

---

← [Personas](personas.md) | [↑ inicio](#product-workflows) | [README](README.md)
