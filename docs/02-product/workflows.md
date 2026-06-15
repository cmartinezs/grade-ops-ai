# Workflows

This document defines the main product workflows for GradeOps AI MVP.

The workflow principle is:

> AI agents operate repetitive assessment steps; teachers retain judgment, standards, and final approval.

## Student Submission Principle

The product is teacher-led, but not teacher-only.

Student answers are represented as `StudentSubmission` records loaded by the teacher. The MVP does not require student login, but it must process real student responses because graded submissions are the economic and technical usage unit.

```text
1 student answer analyzed = 1 graded submission
```

## Workflow Map

| Workflow | Primary User | MVP Priority |
| --- | --- | --- |
| Teacher onboarding | Teacher | P0 |
| Open assessment creation | Teacher + Assessment Agent | P0 |
| Rubric generation and approval | Teacher + Rubric Agent | P0 |
| Open submission intake | Teacher | P0 |
| Grading assistance (open) | Teacher + Grading Agent | P0 |
| Feedback approval | Teacher + Feedback Agent | P0 |
| Learning gap and recovery | Teacher + Learning Gap/Recovery Agents | P0 |
| Teacher report | Teacher + Teacher Report Agent | P0 |
| Evidence dashboard | Operator / demo | P0 |
| Closed assessment — question generation and curation | Teacher + Question Generation/Distractor/Ambiguity Agents | P0 |
| Closed assessment — composition and publish | Teacher + Assessment Assembly Agent | P0 |
| Student invitation and access | Teacher + System | P0 (closed) / P1 (open) |
| Closed response intake and grading | System + Teacher | P0 |
| Item analytics and reinforcement | Teacher + Item Analytics Agent | P0 |
| Pilot/business evidence | Operator | P1 |

## Core End-To-End Workflows

### Open Assessment

```text
Teacher logs in
  -> creates open assessment brief
  -> Assessment Agent drafts assessment
  -> Rubric Agent drafts and validates rubric
  -> teacher edits/approves assessment and rubric
  -> teacher uploads/pastes submissions or students submit via link
  -> Grading Agent generates rubric-based suggestions
  -> Feedback Agent drafts student feedback
  -> Learning Gap Agent summarizes cohort gaps
  -> Recovery Agent suggests reinforcement activity
  -> teacher reviews/edits/approves outputs
  -> Teacher Report Agent generates report
  -> teacher publishes results
  -> Ops Evidence Agent records logs, cost, usage, and evidence
```

### Closed Assessment

```text
Teacher logs in
  -> creates closed assessment intent
  -> Question Generation Agent produces question batch
  -> Distractor Quality Agent and Ambiguity Review Agent flag issues
  -> teacher reviews and approves questions into bank
  -> Assessment Assembly Agent proposes composition
  -> teacher reviews and approves composition
  -> system creates snapshot and publishes assessment
  -> system sends secure links to students
  -> students respond via portal
  -> deterministic engine grades responses
  -> teacher reviews exceptions
  -> Item Analytics Agent analyzes item performance
  -> teacher publishes results
  -> students view results via result link
  -> Ops Evidence Agent records logs, cost, usage, and evidence
```

## Workflow 1: Teacher Onboarding

### Goal

Teacher reaches the workspace and can start a first assessment.

### Steps

1. Teacher signs in.
2. Teacher sees workspace.
3. Teacher sees CTA: `Create assessment`.
4. Product explains control principle:
   - AI drafts;
   - teacher approves;
   - agent actions are logged.
5. Teacher starts first assessment.

### Required Data

- teacher account;
- email;
- organization or individual label;
- pilot/customer status;
- plan/usage limits if available.

### Success Criteria

- Teacher can reach `Create assessment` without confusion.
- Teacher understands AI will not finalize grading alone.

## Workflow 2: Assessment Creation

### Goal

Generate a practical programming assessment from a teacher-defined learning goal.

### Steps

1. Teacher enters:
   - learning goal;
   - topic;
   - level;
   - language/pseudocode;
   - duration;
   - student count;
   - constraints;
   - optional context.
2. Teacher clicks `Generate assessment`.
3. Assessment Agent runs.
4. Product stores agent run log.
5. Teacher reviews draft.
6. Teacher edits or regenerates.
7. Teacher accepts draft for rubric generation.

### Agent Output

Assessment Agent should return structured data:

```json
{
  "title": "Basic Java Control Flow Assessment",
  "context": "Students must solve...",
  "learning_objectives": [],
  "instructions": [],
  "deliverables": [],
  "constraints": [],
  "estimated_duration_minutes": 90,
  "difficulty": "basic"
}
```

### Failure Handling

| Failure | Handling |
| --- | --- |
| Agent timeout | Show retry option and log failure |
| Output invalid | Show validation error and allow regeneration |
| Teacher dislikes draft | Allow edit or regenerate with notes |
| Missing input | Require minimum fields before running agent |

## Workflow 3: Rubric Generation And Approval

### Goal

Create a structured rubric that can drive grading suggestions.

### Steps

1. Teacher requests rubric.
2. Rubric Agent generates criteria and weights.
3. Rubric Agent validates consistency.
4. Product displays:
   - criteria;
   - weights;
   - levels;
   - validation notes;
   - ambiguity warnings.
5. Teacher edits rubric.
6. Teacher approves rubric.
7. Approved rubric becomes grading baseline.

### Approval Rule

Grading cannot proceed unless:

- rubric is approved; or
- workflow is explicitly marked as demo/draft with visible warning.

### Rubric Output Structure

```json
{
  "criteria": [
    {
      "id": "C1",
      "name": "Correct use of conditionals",
      "weight": 25,
      "levels": [
        {
          "label": "Excellent",
          "score": 25,
          "description": "Uses conditionals correctly..."
        }
      ],
      "common_mistakes": []
    }
  ],
  "validation_notes": [],
  "total_weight": 100
}
```

## Workflow 4: Open Submission Intake

### Goal

Collect student answers/submissions in a simple MVP-compatible way without requiring student accounts.

### Supported Intake

- paste code/text;
- upload simple files;
- simple bulk import/paste if available.

### Steps

1. Teacher opens assessment.
2. Teacher adds student submissions.
3. For each student submission:
   - `student_identifier` is added;
   - optional display name can be added;
   - code/text/file is stored;
   - status becomes `received`.
4. Teacher reviews submission list.
5. Teacher starts analysis.

### Failure Handling

| Failure | Handling |
| --- | --- |
| Unsupported file | Reject with clear message |
| Empty submission | Mark invalid |
| Duplicate student identifier | Warn teacher |
| Large submission | Warn and estimate higher cost or require confirmation |

## Workflow 4b: Closed Assessment — Question Generation and Curation

### Goal

Build an approved question bank from AI-generated questions, curated by the teacher.

### Steps

1. Teacher defines evaluative intent: subject, topic, difficulty distribution, question count, question types, level.
2. Question Generation Agent produces question batch with alternatives, answer key, explanations, and difficulty.
3. Distractor Quality Agent evaluates each incorrect alternative.
4. Ambiguity Review Agent checks each question for interpretation problems.
5. System presents curation queue showing questions with quality flags.
6. Teacher reviews each question:
   - approve: question moves to `approved` in bank;
   - edit and approve: teacher edits content, then approves;
   - regenerate: system requests new question for same slot;
   - reject: question discarded with optional reason.
7. Approved questions become available for assessment composition.

### Curation Queue States

| State | Meaning |
| --- | --- |
| `ai_generated` | Question created by agent |
| `pending_review` | Waiting for teacher action |
| `approved` | Teacher validated and accepted |
| `needs_revision` | Teacher flagged for rework |
| `rejected` | Teacher discarded |

### Failure Handling

| Failure | Handling |
| --- | --- |
| No questions generated | Show error; allow retry with adjusted parameters |
| All questions flagged critical ambiguity | Warn teacher; allow approval with explicit confirmation |
| Question generation timeout | Log failure; allow retry |

## Workflow 4c: Closed Assessment — Composition, Publish, and Student Invitation

### Goal

Compose a closed assessment from approved bank questions, publish it, and deliver secure access links to students.

### Steps

1. Teacher creates a new closed assessment (or continues from question bank).
2. Teacher defines: title, date, duration, learner list, scoring policy, grade scale.
3. Assessment Assembly Agent proposes a question composition from the approved bank.
4. Teacher reviews composition: questions selected, difficulty distribution, outcome coverage, scores.
5. Teacher approves composition (or modifies and approves).
6. System validates:
   - all questions are approved;
   - all questions have a defined correct answer;
   - total score is consistent;
   - no retired or rejected items are included.
7. System creates snapshot: questions, options, answer key, scoring policy, scale are frozen.
8. Assessment transitions to `published`.
9. System generates a unique `AssessmentInvitation` per `LearnerRef`.
10. System sends email with access link to each learner.

### Failure Handling

| Failure | Handling |
| --- | --- |
| Bank does not have enough approved questions | Alert teacher; suggest generating more questions |
| Learning outcome gap | Alert teacher; allow proceed with explicit acknowledgment |
| Snapshot fails | Rollback; prevent publication |
| Email delivery fails | Log; teacher can resend from invitation management |

## Workflow 4d: Student Response via Secure Link

### Goal

Enable students to respond to an assessment without a registered account.

### Steps — Closed Assessment

1. Student receives email with `assessment_access_link`.
2. Student opens link; system validates token (not expired, not revoked, assessment in `accepting_responses` state).
3. Student sees assessment instructions and questions.
4. Student selects alternatives for each question.
5. Student reviews their selections.
6. Student confirms submission.
7. System records `AssessmentAttempt` with `ClosedResponse`.
8. System closes or marks link as used.
9. Deterministic engine processes response against snapshot.
10. After teacher approves results, system sends `result_access_link` or student can request via email.

### Steps — Open Assessment

1. Teacher shares access link or student submits file/text via teacher-managed flow (same as Workflow 4).
2. Open assessment digital submission via link: P1.

### Security Rules at Intake

| Rule | Handling |
| --- | --- |
| Token expired | Show friendly error; allow magic link request |
| Token already used (submitted) | Show confirmation page; do not allow re-submission by default |
| Assessment not in `accepting_responses` | Inform student; do not expose internal state details |
| Attempt from different device | Allow unless single-device policy is set |

## Workflow 5: Grading Assistance

### Goal

Generate rubric-based scoring suggestions while preserving teacher authority.

### Steps

1. Teacher clicks `Analyze submissions` for selected `StudentSubmission` records.
2. Grading Agent processes each student submission.
3. Agent returns score suggestion per rubric criterion.
4. Agent flags uncertainty.
5. Product displays review queue.
6. Teacher reviews each suggestion.
7. Teacher approves, edits, rejects, or marks needs review.

### Grading Output

```json
{
  "submission_id": "SUB-001",
  "suggested_total_score": 82,
  "criteria_results": [
    {
      "criterion_id": "C1",
      "suggested_score": 20,
      "evidence_summary": "Uses if/else correctly...",
      "issues": [],
      "uncertainty": "low"
    }
  ],
  "overall_feedback_basis": [],
  "uncertainty_flags": []
}
```

### Teacher Review Actions

| Action | Meaning |
| --- | --- |
| Approve | Teacher accepts suggestion |
| Edit | Teacher changes score or notes |
| Reject | Teacher rejects AI output |
| Needs review | Teacher defers decision |
| Exclude | Submission excluded from report |

### Control Principle

AI output must be labeled as suggestion until teacher approval.

## Workflow 6: Feedback Draft And Approval

### Goal

Generate useful student-facing feedback based on rubric and teacher-reviewed grading.

### Steps

1. Feedback Agent uses grading suggestion or teacher-approved score.
2. Agent drafts feedback.
3. Teacher reviews feedback.
4. Teacher edits tone/content if needed.
5. Teacher approves final feedback.
6. Feedback becomes available for export/delivery.

### Feedback Output

```json
{
  "student_identifier": "student-01",
  "summary": "Good progress...",
  "strengths": [],
  "improvement_areas": [],
  "next_steps": [],
  "rubric_references": []
}
```

### Delivery

MVP delivery can be:

- copy/export;
- downloaded report;
- manual sending by teacher.

Automatic student delivery is not required for MVP.

## Workflow 7: Learning Gap And Recovery

### Goal

Summarize repeated cohort issues and suggest recovery actions.

### Steps

1. Learning Gap Agent reads rubric results and feedback basis.
2. Agent groups common mistakes.
3. Agent identifies affected criteria and severity.
4. Recovery Agent suggests reinforcement activity.
5. Teacher reviews suggestions.
6. Approved recovery action appears in report.

### Learning Gap Output

```json
{
  "gaps": [
    {
      "topic": "Loop termination condition",
      "criterion_ids": ["C2"],
      "affected_submissions": 12,
      "severity": "high",
      "evidence_summary": "Students often used..."
    }
  ]
}
```

### Recovery Output

```json
{
  "activity_title": "Fixing loop termination errors",
  "target_gap": "Loop termination condition",
  "instructions": [],
  "expected_output": "",
  "teacher_notes": ""
}
```

## Workflow 8: Teacher Report

### Goal

Generate a teacher-facing summary of the assessment run.

### Steps

1. Teacher requests report.
2. Teacher Report Agent gathers:
   - assessment;
   - rubric;
   - submission results;
   - feedback status;
   - learning gaps;
   - recovery suggestions;
   - usage/cost evidence.
3. Report is generated.
4. Teacher can edit summary.
5. Report can be exported or screenshotted.

### Report Sections

- assessment overview;
- submission count;
- grading summary;
- common mistakes;
- learning gaps;
- recommended next teaching action;
- recovery activity;
- time-saved estimate;
- agent/evidence summary.

## Workflow 9: Agent Logs And Evidence Dashboard

### Goal

Prove AI-native operations, cost awareness, and business evidence.

### Steps

1. Every agent run creates an event.
2. Events are stored.
3. Dashboard aggregates:
   - assessments;
   - submissions;
   - agent runs;
   - feedback outputs;
   - model usage;
   - cost estimates;
   - approval states;
   - failures/retries.
4. Operator uses dashboard for demo and submission evidence.

### Dashboard Minimum

| Metric | Purpose |
| --- | --- |
| Assessments processed | Product usage |
| Submissions reviewed | Operational volume |
| Feedback outputs | Value output |
| Agent runs | AI-native proof |
| Teacher approval rate | Trust and quality |
| Estimated AI cost | Unit economics |
| Time saved estimate | Business value |
| Failed/retried runs | Reliability evidence |

## Workflow 10: Pilot Business Evidence

### Goal

Connect product usage to business validation.

### Steps

1. Operator creates customer/pilot record.
2. Customer is associated with assessment runs.
3. Payment/commitment evidence is stored externally or linked.
4. Product logs usage.
5. Operator records testimonial/feedback.
6. Evidence is summarized for hackathon.

### Evidence Fields

- customer ID;
- segment;
- related-party flag;
- offer/plan;
- payment/commitment status;
- assessments processed;
- submissions processed;
- agent runs;
- estimated cost;
- time saved;
- testimonial status.

## Workflow 5b: Closed Assessment — Grading and Exception Review

### Goal

Grade closed responses deterministically and surface exceptions for teacher review.

### Steps

1. System receives `AssessmentAttempt` records.
2. Deterministic engine compares each `ClosedResponse` against the `AssessmentOptionSnapshot` (frozen answer key).
3. Engine applies `ScoringPolicy` (full credit, partial credit, penalty) per question.
4. Engine calculates total score and converts to grade using the frozen scale.
5. System identifies exceptions:
   - blank answers;
   - duplicate attempts;
   - student not associated to `LearnerRef`;
   - response with invalid option reference.
6. Teacher reviews exception queue.
7. Teacher resolves exceptions (exclude, assign manually, flag for re-intake).
8. Teacher confirms results.
9. System generates item analytics report via Item Analytics Agent.
10. Teacher reviews analytics.
11. Teacher publishes results.
12. System sends `result_access_link` to students.

### Teacher Override Actions

| Action | Audit |
| --- | --- |
| Annul a question | Triggers recalculation for all students; logged with reason |
| Correct answer key | Triggers recalculation; original result preserved |
| Exclude a student attempt | Logged with reason |
| Manual grade override | Logged with reason; AI does not override teacher |

## Workflow States

### Assessment Lifecycle (Unified)

```text
draft
  -> pending_teacher_review         (AI output ready: rubric or question batch)
  -> approved                       (Teacher approved rubric / question composition)
  -> published                      (Snapshot created; links sent)
  -> accepting_responses            (Open for student input)
  -> responses_received             (At least one response)
  -> grading_in_progress            (Grading engine or agents running)
  -> pending_teacher_review         (Grading results ready for teacher)
  -> graded                         (Teacher confirmed results)
  -> results_published              (Results visible to students)
  -> archived
```

### Open Assessment Lifecycle (legacy states preserved for compatibility)

```text
draft
  -> rubric_pending_review
  -> ready_for_submissions
  -> submissions_received
  -> grading_in_progress
  -> pending_teacher_review
  -> approved
  -> reported
  -> archived
```

### Submission Lifecycle

```text
received
  -> analysis_pending
  -> analyzed
  -> needs_review
  -> approved | edited_by_teacher | rejected | excluded
```

### Agent Run Lifecycle

```text
queued
  -> running
  -> succeeded | failed | retried
  -> requires_human_review
  -> approved | edited | rejected
```

## Critical Failure Workflows

### Agent Failure

1. Agent fails.
2. Status becomes `failed`.
3. Failure is logged.
4. Teacher sees retry option.
5. Product prevents silent loss of work.

### Low Confidence Output

1. Agent marks uncertainty.
2. Submission becomes `requires_human_review`.
3. Bulk approval is blocked or warned.
4. Teacher must inspect manually.

### Teacher Rejects Output

1. Teacher rejects score/feedback.
2. Rejection reason can be captured.
3. Final report notes teacher override count.
4. Agent output remains in audit trail.

### Cost Spike Warning

1. Submission is unusually long or model fallback is required.
2. Product warns operator/teacher if needed.
3. Cost is logged.
4. Premium fallback is not default.

## Workflow Conclusion

The product workflow should feel controlled, transparent, and operational.

The best demo is not a tour of screens. It is a proof that:

> A real assessment moved from learning goal to rubric, submissions, grading suggestions, feedback, report, teacher approval, and auditable AI-agent evidence.

<!-- nav -->

---

← [User Stories](user-stories.md) | [↑ inicio](#workflows) | [README](README.md) | [Assessment Modes →](assessment-modes.md)
