# Item Analytics Agent

The Item Analytics Agent analyzes objective assessment results after a graded run to evaluate item performance, detect problematic questions, and surface learning insights.

## Role

Process post-assessment response data to produce item-level statistics, identify questions that underperformed or produced unexpected patterns, and suggest instructional follow-up.

The agent runs after the assessment is graded, not during grading. Grading is always deterministic.

## Non-Goals

The agent must not modify the answer key, recalculate grades, change published results, or invalidate a question without teacher action. It provides analytical interpretation; the teacher acts.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `assessment_id` | Yes | The completed assessment |
| `graded_attempts` | Yes | All student attempts with selected options and scores |
| `answer_key_snapshot` | Yes | Frozen answer key used for grading |
| `question_snapshots` | Yes | Frozen question list with type, difficulty, and learning outcomes |
| `learner_count` | Yes | Total participants for context |

## Output Contract

```json
{
  "schema_version": "1.0",
  "analytics_run_id": "IANA-001",
  "assessment_id": "ASSESSMENT-042",
  "overall": {
    "mean_score": 72.4,
    "median_score": 75.0,
    "pass_rate": 0.68,
    "score_distribution": [
      { "range": "0-49", "count": 3 },
      { "range": "50-69", "count": 8 },
      { "range": "70-89", "count": 12 },
      { "range": "90-100", "count": 7 }
    ]
  },
  "item_analysis": [
    {
      "item_id": "ITEM-023",
      "position": 1,
      "correct_rate": 0.93,
      "difficulty_index": 0.93,
      "difficulty_rating": "easy_as_expected",
      "most_chosen_wrong_option": null,
      "flags": [],
      "interpretation": "Item performed as expected for an easy question on variable declaration."
    },
    {
      "item_id": "ITEM-045",
      "position": 5,
      "correct_rate": 0.23,
      "difficulty_index": 0.23,
      "difficulty_rating": "harder_than_expected",
      "most_chosen_wrong_option": "B",
      "flags": ["possible_ambiguity", "possible_misconception_target"],
      "interpretation": "Only 23% answered correctly. Option B was selected by 61% of students. The item may be ambiguous or option B may represent a widespread misconception about loop termination."
    }
  ],
  "learning_outcome_coverage": [
    {
      "outcome": "Loop control",
      "items_assessed": 3,
      "mean_correct_rate": 0.41,
      "insight": "Students consistently underperformed on loop control items. Reinforcement recommended."
    }
  ],
  "reinforcement_suggestions": [
    {
      "outcome": "Loop control",
      "suggested_action": "Dedicate one session to loop termination conditions with worked examples.",
      "priority": "high"
    }
  ],
  "items_flagged_for_review": ["ITEM-045"],
  "requires_teacher_review": true
}
```

## Item-Level Flags

| Flag | Description |
| --- | --- |
| `possible_ambiguity` | Very low correct rate combined with spread across multiple wrong options |
| `possible_misconception_target` | One wrong option attracted a large share of students; may indicate a predictable misconception |
| `distractor_not_chosen` | A distractor was selected by fewer than 5% of students; may be too obvious |
| `harder_than_expected` | Correct rate is substantially lower than declared difficulty |
| `easier_than_expected` | Correct rate is substantially higher than declared difficulty |
| `possible_key_error` | Correct rate is below 10%; may indicate an error in the answer key |

## Reinforcement Suggestions

When a learning outcome shows low correct rates across multiple items, the agent suggests a targeted reinforcement action. Suggestions are labeled as AI-generated and require teacher approval before any student-facing use.

## Human Control

Teacher reviews the item analytics report. The teacher can:

- accept the analysis and use reinforcement suggestions;
- flag a specific question for future review or retirement;
- initiate an answer-key correction if `possible_key_error` is raised (which triggers a recalculation event with audit log);
- approve reinforcement suggestions for inclusion in teacher report or recovery plan;
- dismiss flags they disagree with.

Item analytics do not retroactively change grades unless the teacher explicitly initiates a key correction through the official recalculation workflow.

## Logging Requirements

Log assessment ID, number of items analyzed, number of flags raised, reinforcement suggestions generated, model policy, token and cost estimate, and whether the teacher reviewed the report.

## Acceptance Criteria

The Item Analytics Agent is ready when it produces correct-rate statistics per item, applies difficulty and ambiguity flags, generates learning-outcome coverage summaries, suggests reinforcement for underperforming outcomes, and returns results for teacher review without modifying grades or the answer key.

<!-- nav -->

---

← [Assessment Assembly Agent](assessment-assembly-agent.md) | [↑ inicio](#item-analytics-agent) | [README](README.md)
