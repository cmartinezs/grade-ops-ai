# Teacher Report Agent

The Teacher Report Agent generates a concise assessment report for the teacher or small education operator after an assessment run.

It translates assessment activity into decision-ready evidence.

## Role

Summarize the completed assessment workflow: assessment context, submissions, scoring outcomes, feedback status, learning gaps, recovery actions, time saved, and operational evidence.

## Non-Goals

The Teacher Report Agent must not publish reports without teacher validation, expose internal prompts, expose unnecessary sensitive student data, claim perfect measurement, hide uncertainty or teacher overrides, invent business metrics, or replace teacher interpretation.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `assessment` | Yes | Assessment brief and metadata. |
| `rubric` | Yes | Approved rubric. |
| `submission_results` | Yes | Grading suggestions and teacher-approved results. |
| `feedback_status` | Yes | Feedback generated/approved/edited/rejected. |
| `learning_gap_summary` | Yes | Gaps generated/confirmed. |
| `recovery_activities` | Yes | Draft or approved recovery suggestions. |
| `agent_logs_summary` | Yes | Agent runs and cost summary. |
| `teacher_notes` | No | Teacher comments to include. |
| `audience` | No | teacher_internal, operator_summary, student_safe_summary, hackathon_evidence. |

## Output Contract

```json
{
  "schema_version": "1.0",
  "assessment_id": "ASSESSMENT-001",
  "report": {
    "title": "Assessment Report: Java Control Flow",
    "executive_summary": "Most students handled conditionals correctly, while loop termination remains the main gap.",
    "assessment_overview": {
      "topic": "Conditionals, loops, and functions",
      "level": "basic",
      "submissions_processed": 30
    },
    "performance_summary": {
      "average_score": 78,
      "score_distribution": [
        {"range": "90-100", "count": 5},
        {"range": "70-89", "count": 16},
        {"range": "0-69", "count": 9}
      ]
    },
    "common_strengths": [],
    "common_errors": [],
    "learning_gaps": [],
    "recommended_next_actions": [],
    "recovery_activities": [],
    "feedback_status": {"generated": 30, "approved": 26, "edited": 4, "rejected": 0},
    "evidence_summary": {
      "agent_runs": 95,
      "estimated_ai_cost_usd": 0.84,
      "teacher_overrides": 4,
      "time_saved_estimate_minutes": 180
    }
  },
  "warnings": [],
  "uncertainty_flags": [],
  "requires_teacher_validation": true,
  "next_action": "teacher_review"
}
```

## Report Sections

Minimum report sections:

- assessment overview;
- submission count;
- rubric summary;
- score/achievement summary;
- common strengths;
- common mistakes;
- learning gaps;
- recovery recommendation;
- feedback completion status;
- teacher approval summary;
- evidence/cost summary.

## Audience Modes

| Mode | Description |
| --- | --- |
| `teacher_internal` | Full teacher-facing report. |
| `operator_summary` | Cohort and business outcome focus. |
| `student_safe_summary` | Shareable summary without private/internal evidence. |
| `hackathon_evidence` | Usage, agent, cost, and value proof focus. |

## Quality Rules

The report should be concise, separate facts from interpretation, use teacher-approved data when available, mark unapproved data clearly, avoid unnecessary student-level details, support next teaching decisions, and support business/demo evidence.

## Uncertainty Flags

| Flag | Trigger |
| --- | --- |
| `unapproved_grading_data` | Some scores are not teacher-approved. |
| `incomplete_feedback_review` | Feedback remains pending. |
| `small_sample_size` | Cohort size is too small. |
| `missing_cost_data` | Agent/cost summary incomplete. |
| `teacher_validation_required` | Always true before sharing. |

## Human Control

Teacher can validate, edit summary, hide sections, approve export, or reject/regenerate. The report is not final until teacher validation.

## Handoff To Ops Agent

Pass report generated event, report status, summary metrics, time-saved estimate, approved/exported status, and business evidence tags if relevant.

## Logging Requirements

Log assessment ID, submissions included, approval status of source data, report mode, model/cost estimate, uncertainty flags, and teacher validation state.

## Acceptance Criteria

The Teacher Report Agent is MVP-ready when it generates a concise report from assessment data, includes learning gaps and next actions, includes evidence/cost summary, respects approval states, requires teacher validation, and returns structured logs.

<!-- nav -->

---

← [Recovery Agent](recovery-agent.md) | [↑ inicio](#teacher-report-agent) | [README](README.md) | [Ops Agent →](ops-agent.md)
