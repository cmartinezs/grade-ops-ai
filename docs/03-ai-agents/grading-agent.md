# Grading Agent

The Grading Agent analyzes student submissions against the teacher-approved rubric and produces structured grading suggestions.

It must never finalize grades by itself.

## Role

Generate evidence-based scoring suggestions per rubric criterion for practical programming submissions.

The agent helps teachers review rather than score from scratch.

## Non-Goals

The Grading Agent must not finalize grades, send results to students, override teacher edits, grade against an unapproved rubric, accuse plagiarism as fact, execute unsafe code, infer personal student context, or reward unsupported assumptions.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `submission` | Yes | Student code/text/file content. |
| `approved_rubric` | Yes | Teacher-approved rubric version. |
| `assessment` | Yes | Assessment brief and instructions. |
| `programming_language` | Yes | Submission language/context. |
| `student_identifier` | Yes | Minimal identifier for tracking. |
| `grading_policy` | No | Partial credit, scale, or teacher rules. |
| `reference_solution` | No | Optional teacher-provided expected solution. |
| `teacher_notes` | No | Extra evaluator guidance. |

## Output Contract

```json
{
  "schema_version": "1.0",
  "submission_id": "SUB-001",
  "rubric_version": "RUBRIC-001-V1",
  "suggested_total_score": 82,
  "max_score": 100,
  "criteria_results": [
    {
      "criterion_id": "C1",
      "criterion_name": "Correct use of conditionals",
      "suggested_score": 18,
      "max_score": 20,
      "evidence_summary": "The solution uses if/else branches for the main decision cases.",
      "issues": ["Missing explicit handling for invalid menu option."],
      "uncertainty": "low",
      "teacher_review_recommended": false
    }
  ],
  "overall_evidence_summary": "The submission meets the core requirements but misses one validation branch.",
  "uncertainty_flags": [],
  "integrity_flags": [],
  "requires_teacher_approval": true,
  "next_action": "teacher_review"
}
```

## Quality Rules

The agent should grade only against rubric criteria, separate evidence from judgment, cite submission behavior in summaries, preserve partial-credit reasoning, mark uncertainty, avoid final-grade language, and keep output structured.

## Uncertainty Flags

| Flag | Trigger |
| --- | --- |
| `low_confidence_score` | Evidence is insufficient or ambiguous. |
| `rubric_mismatch` | Rubric does not map cleanly to submission. |
| `incomplete_submission` | Submission lacks required parts. |
| `possible_non_compilation` | Code appears syntactically invalid, not executed. |
| `unsupported_language_feature` | Submission uses unexpected technology. |
| `long_submission` | Submission is too long for standard review. |
| `possible_academic_integrity_issue` | Suspicious pattern; not a final accusation. |
| `requires_manual_execution` | Correctness depends on running code. |

## Academic Integrity Handling

The agent may flag suspicious patterns only as a review signal. It must not state that a student plagiarized. It may say that teacher review is recommended because the submission shows unusual similarity, unexplained complexity, or another review-worthy pattern.

## Human Control

Teacher can approve, edit score per criterion, add notes, reject, mark as needs review, or exclude the submission from the report. The original AI suggestion must remain traceable after teacher edits.

## Handoff To Feedback Agent

Pass suggested or teacher-approved criterion results, evidence summaries, issues, uncertainty flags, teacher edits, and final teacher-approved status when available.

## Logging Requirements

Log assessment ID, submission ID, rubric version, model policy, token/cost estimate, suggested score, uncertainty flags, status, teacher approval/edit/rejection state, and retry/failure state.

## Acceptance Criteria

The Grading Agent is MVP-ready when it accepts an approved rubric and submission, returns score suggestions per criterion, provides evidence notes, flags uncertainty, marks outputs as suggestions, supports teacher edit/reject workflows, and logs execution/cost metadata.

<!-- nav -->

---

← [Rubric Agent](rubric-agent.md) | [↑ inicio](#grading-agent) | [README](README.md) | [Feedback Agent →](feedback-agent.md)
