# Learning Gap Agent

The Learning Gap Agent identifies repeated conceptual or skill gaps across submissions, rubric results, and feedback patterns.

It helps teachers decide what to reinforce next.

## Role

Aggregate assessment results into cohort-level and optional student-level learning gap summaries.

## Non-Goals

The Learning Gap Agent must not make high-stakes predictions, label students permanently, create psychological or personal profiles, override teacher interpretation, diagnose causes beyond evidence, or expose private student details unnecessarily.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `assessment` | Yes | Assessment summary. |
| `approved_rubric` | Yes | Rubric criteria. |
| `grading_results` | Yes | Suggested or teacher-approved criterion results. |
| `feedback_summaries` | Yes | Generated/approved feedback themes. |
| `submission_count` | Yes | Cohort size processed. |
| `teacher_notes` | No | Teacher interpretation or known context. |
| `target_objectives` | No | Objectives to map gaps against. |

## Output Contract

```json
{
  "schema_version": "1.0",
  "assessment_id": "ASSESSMENT-001",
  "cohort_gap_summary": {
    "total_submissions_analyzed": 30,
    "gaps": [
      {
        "gap_id": "GAP-001",
        "topic": "Loop termination condition",
        "description": "Several submissions used loops without a reliable exit condition.",
        "related_criteria": ["C2"],
        "affected_submission_count": 12,
        "severity": "high",
        "evidence_summary": "Repeated issues with while loop conditions and missing updates.",
        "recommended_teacher_action": "Review loop invariants and termination examples."
      }
    ]
  },
  "student_gap_references": [
    {"submission_id": "SUB-001", "gap_ids": ["GAP-001"]}
  ],
  "warnings": [],
  "uncertainty_flags": [],
  "requires_teacher_confirmation": true,
  "next_action": "teacher_review"
}
```

## Gap Severity

| Severity | Meaning |
| --- | --- |
| `low` | Isolated or minor issue. |
| `medium` | Repeated issue affecting a meaningful minority. |
| `high` | Common issue that should influence next instruction. |
| `critical` | Blocks expected objective for many students. |

## Quality Rules

The agent should aggregate evidence from rubric results, connect gaps to criteria/objectives, quantify affected submissions, avoid overinterpreting causes, separate cohort patterns from individual notes, and provide teacher-actionable summaries.

## Uncertainty Flags

| Flag | Trigger |
| --- | --- |
| `small_sample_size` | Too few submissions for strong pattern. |
| `unapproved_grading_data` | Results have not been teacher-approved. |
| `weak_pattern_evidence` | Gap pattern is tentative. |
| `conflicting_evidence` | Results show inconsistent signals. |
| `requires_teacher_context` | Instructional context is needed. |

## Human Control

Teacher can confirm, edit, merge/split, or reject detected gaps. Gap summaries are not final instructional truth until teacher confirmation.

## Handoff To Recovery Agent

Pass confirmed or draft gap list, severity, related criteria/objectives, affected count, evidence summary, and recommended teacher action.

## Logging Requirements

Log assessment ID, submission count, source status, gaps found, severity distribution, uncertainty flags, model/cost estimate, and teacher confirmation state.

## Acceptance Criteria

The Learning Gap Agent is MVP-ready when it groups repeated issues, links gaps to rubric criteria, gives affected counts, avoids unsupported student profiling, requires teacher confirmation, and produces structured logs.

<!-- nav -->

---

← [Feedback Agent](feedback-agent.md) | [↑ inicio](#learning-gap-agent) | [README](README.md) | [Recovery Agent →](recovery-agent.md)
