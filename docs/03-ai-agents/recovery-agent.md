# Recovery Agent

The Recovery Agent suggests remedial activities aligned to confirmed or draft learning gaps.

It turns gap analysis into teacher-actionable reinforcement work.

## Role

Generate short, focused recovery activities for programming skills that students struggled with in an assessment.

## Non-Goals

The Recovery Agent must not assign activities directly to students, create a full adaptive learning system, replace teacher lesson planning, generate broad curriculum plans in MVP, assume personal causes, or ignore teacher constraints.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `learning_gaps` | Yes | Gap list from Learning Gap Agent. |
| `assessment` | Yes | Original assessment context. |
| `rubric_criteria` | Yes | Related rubric criteria. |
| `target_level` | Yes | Student skill level. |
| `programming_language` | Yes | Language or pseudocode context. |
| `allowed_activity_formats` | Yes | Exercise, mini-lab, checklist, etc. |
| `teacher_notes` | No | Preferred reinforcement style. |
| `time_available_minutes` | No | Time for recovery activity. |
| `excluded_topics` | No | Concepts not yet covered. |

## Output Contract

```json
{
  "schema_version": "1.0",
  "assessment_id": "ASSESSMENT-001",
  "recovery_activities": [
    {
      "activity_id": "REC-001",
      "target_gap_id": "GAP-001",
      "title": "Fixing Loop Termination Errors",
      "objective": "Practice designing loops with clear exit conditions.",
      "estimated_duration_minutes": 20,
      "instructions": [
        "Read three loop examples.",
        "Identify the variable that controls termination.",
        "Fix the loop that can become infinite."
      ],
      "expected_output": "A corrected code snippet and a short explanation.",
      "teacher_notes": ["Use this before the next practical exercise."],
      "success_criteria": ["Student can explain how the loop stops."]
    }
  ],
  "warnings": [],
  "uncertainty_flags": [],
  "requires_teacher_approval": true,
  "next_action": "teacher_review"
}
```

## Quality Rules

Activities should be short, focused, aligned to the assessment/rubric, appropriate for the target level, practical, easy to edit, and connected to observable success criteria.

Avoid unrelated advanced topics, full new assessments, unsupported tooling, generic advice, or blame-oriented language.

## Activity Formats

| Format | Use when |
| --- | --- |
| Mini exercise | Students need practice on a specific construct. |
| Debugging task | Students made repeated code mistakes. |
| Reflection prompt | Students need to explain reasoning. |
| Checklist | Students missed procedural steps. |
| Re-teaching outline | Teacher needs class-level reinforcement. |
| Pair activity | Cohort needs collaborative review. |

## Uncertainty Flags

| Flag | Trigger |
| --- | --- |
| `gap_not_confirmed` | Teacher has not confirmed the gap. |
| `activity_too_large` | Suggested activity exceeds time limit. |
| `requires_uncovered_topic` | Activity needs a topic not yet taught. |
| `weak_gap_mapping` | Gap-to-activity relationship is indirect. |
| `requires_teacher_context` | Needs more class context. |

## Human Control

Teacher can approve, edit, reject, select which gaps to address, and decide whether to assign to all students or selected students. No recovery activity is sent automatically in MVP.

## Handoff To Teacher Report Agent

Pass approved/draft recovery activities, target gap IDs, estimated time, success criteria, and approval state.

## Logging Requirements

Log assessment ID, gap IDs used, activity count, estimated duration, uncertainty flags, model/cost estimate, and teacher approval/edit/rejection state.

## Acceptance Criteria

The Recovery Agent is MVP-ready when it generates at least one focused activity for a selected gap, ties it to rubric/gap evidence, respects target level and duration, requires teacher approval, returns structured output, and logs execution/cost metadata.

<!-- nav -->

---

← [Learning Gap Agent](learning-gap-agent.md) | [↑ inicio](#recovery-agent) | [README](README.md) | [Teacher Report Agent →](teacher-report-agent.md)
