# Feedback Agent

The Feedback Agent drafts student-readable feedback from rubric results, submission evidence, and teacher preferences.

It supports faster feedback while preserving teacher review and tone control.

## Role

Generate concise, constructive, actionable feedback for each learner based on grading outcomes and rubric criteria.

## Non-Goals

The Feedback Agent must not send feedback directly, expose internal logs/prompts, invent personal student context, use discouraging language, change scores, contradict teacher edits, or claim finality before approval.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `submission_id` | Yes | Submission being addressed. |
| `assessment` | Yes | Assessment summary. |
| `rubric_results` | Yes | Suggested or teacher-approved criterion results. |
| `evidence_summary` | Yes | Evidence and issues from grading. |
| `teacher_approval_state` | Yes | Whether grading is approved, edited, or pending. |
| `teacher_tone_preference` | No | supportive, direct, concise, detailed. |
| `feedback_language` | No | English, Spanish, etc. |
| `teacher_notes` | No | Extra constraints or comments. |

## Output Contract

```json
{
  "schema_version": "1.0",
  "submission_id": "SUB-001",
  "feedback": {
    "summary": "You solved the main control-flow requirement and showed good function separation.",
    "strengths": ["Uses conditionals clearly for the main decision paths."],
    "improvement_areas": ["Add validation for invalid menu options."],
    "next_steps": ["Practice one exercise focused on input validation and loop exits."],
    "rubric_references": [
      {"criterion_id": "C1", "message": "Conditionals are mostly correct, with one missing validation branch."}
    ]
  },
  "tone": "supportive",
  "student_facing_ready": false,
  "requires_teacher_approval": true,
  "warnings": [],
  "uncertainty_flags": [],
  "next_action": "teacher_review"
}
```

## Quality Rules

Feedback should be specific, connected to rubric criteria, understandable, concise, constructive, actionable, aligned with teacher-approved grading, and free of internal details. Avoid generic praise, excessive length, final-grade claims before approval, and unsupported claims about effort or intent.

## Tone Modes

| Tone | Behavior |
| --- | --- |
| `concise` | Short, direct, minimal explanation. |
| `supportive` | Encouraging but still specific. |
| `direct` | Clear and no-nonsense. |
| `detailed` | More explanation and next steps. |
| `spanish_latam` | Spanish phrasing appropriate for LatAm context. |

## Uncertainty Flags

| Flag | Trigger |
| --- | --- |
| `grading_not_approved` | Feedback is based on unapproved grading. |
| `low_confidence_score` | Grading Agent flagged uncertainty. |
| `unsafe_student_facing_language` | Draft may need tone review. |
| `missing_evidence` | Not enough evidence to justify a comment. |
| `teacher_review_required` | Always true for MVP student-facing output. |

## Human Control

Teacher can approve, edit, reject, regenerate with tone notes, or export approved feedback. Feedback remains pending until teacher approval.

## Handoff To Learning Gap Agent

Pass repeated improvement areas, criterion-level issues, feedback tags, student-level next-step themes, and uncertainty flags for cohort aggregation.

## Logging Requirements

Log assessment ID, submission ID, source grading result, tone preference, output summary, model/cost estimate, uncertainty flags, and teacher approval/edit/rejection state.

## Acceptance Criteria

The Feedback Agent is MVP-ready when it generates feedback tied to rubric evidence, avoids final delivery language before approval, supports tone preferences, returns structured output, flags unsafe/uncertain drafts, and logs execution/cost metadata.

<!-- nav -->

---

← [Grading Agent](grading-agent.md) | [↑ inicio](#feedback-agent) | [README](README.md) | [Learning Gap Agent →](learning-gap-agent.md)
