# Assessment Assembly Agent

The Assessment Assembly Agent composes a closed assessment by selecting and organizing questions from the approved Question Bank. It optimizes for coverage, difficulty balance, and learning outcome alignment.

## Role

Propose a question set and scoring structure for a closed assessment based on teacher-defined parameters. The teacher reviews and approves the composition before the snapshot is created.

## Non-Goals

The agent must not create a snapshot, publish an assessment, use questions that are not in `approved` or `active` state, include questions that are `retired` or `rejected`, or apply final scoring without teacher confirmation.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `assessment_intent` | Yes | Title, subject, topic, level, purpose |
| `question_count` | Yes | Target number of questions |
| `difficulty_distribution` | No | e.g., `{ "easy": 30, "medium": 50, "hard": 20 }` in percentage |
| `learning_outcomes_to_cover` | No | List of outcomes that must be represented |
| `available_questions` | Yes | Approved/active questions from the bank with metadata |
| `scoring_policy` | No | Total points, per-question weight, partial credit rules |
| `grade_scale` | No | Target scale (e.g., 1.0–7.0, 0–100) |
| `exclusions` | No | Questions to avoid (recently used, marked ambiguous, specific IDs) |

## Output Contract

```json
{
  "schema_version": "1.0",
  "assembly_run_id": "ASSY-001",
  "assessment_id": "ASSESSMENT-042",
  "proposed_composition": [
    {
      "position": 1,
      "item_id": "ITEM-023",
      "type": "single_choice",
      "difficulty": "easy",
      "learning_outcome": "Variable declaration",
      "proposed_points": 5
    },
    {
      "position": 2,
      "item_id": "ITEM-045",
      "type": "multiple_choice",
      "difficulty": "medium",
      "learning_outcome": "Loop control",
      "proposed_points": 10
    }
  ],
  "coverage_summary": {
    "total_questions": 15,
    "total_points": 100,
    "difficulty_achieved": { "easy": 33, "medium": 47, "hard": 20 },
    "outcomes_covered": ["Variable declaration", "Loop control", "Conditionals"],
    "outcomes_missing": []
  },
  "alerts": [],
  "requires_teacher_approval": true
}
```

## Selection Rules

The agent must only select questions that are `approved` or `active` in the bank. It must not:

- include the same item twice;
- include items flagged as `retired`, `rejected`, or `needs_revision` (unless teacher explicitly overrides);
- exceed the requested question count;
- produce a composition where all questions have the same difficulty;
- leave declared learning outcomes uncovered unless no bank questions exist for them.

## Composition Alerts

| Alert | Description |
| --- | --- |
| `insufficient_questions` | Bank does not have enough approved questions for the requested composition |
| `coverage_gap` | A declared learning outcome has no matching approved question |
| `difficulty_imbalance` | Cannot achieve requested difficulty distribution with available questions |
| `recently_used_items` | Proposed questions were used in the last N assessments for the same cohort |
| `low_bank_diversity` | Multiple proposed questions test the same concept at the same level |

## Human Control

The teacher reviews the proposed composition before the snapshot is created. The teacher can:

- approve the composition as proposed;
- swap individual questions with alternatives from the bank;
- adjust scoring weights per question;
- change the difficulty target and request a new proposal;
- approve a partial composition with manual additions.

The snapshot is created only after teacher explicit approval of the final composition.

## Handoff

When teacher approves the composition, pass the confirmed item list, scoring policy, and grade scale to the snapshot service. The assessment transitions to `published` state.

## Logging Requirements

Log assessment ID, requested and achieved composition parameters, coverage summary, alerts, number of items considered, number proposed, model policy, and token and cost estimate.

## Acceptance Criteria

The Assessment Assembly Agent is ready when it selects only approved bank questions, respects difficulty and coverage constraints, produces clear alerts for gaps, returns a composition the teacher can review and modify before approval, and correctly passes the approved composition for snapshotting.

<!-- nav -->

---

← [Ambiguity Review Agent](ambiguity-review-agent.md) | [↑ inicio](#assessment-assembly-agent) | [README](README.md) | [Item Analytics Agent →](item-analytics-agent.md)
