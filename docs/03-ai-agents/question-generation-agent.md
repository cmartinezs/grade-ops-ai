# Question Generation Agent

The Question Generation Agent produces batches of objective questions from a teacher-defined evaluative intent. All output enters the Question Bank in `ai_generated` / `pending_review` state. Nothing activates without teacher curation.

## Role

Generate structured question items — True/False, Single Choice, Multiple Choice — for closed assessments. Every question includes alternatives, a proposed answer key, difficulty, learning outcome association, and rationale.

## Non-Goals

The agent must not publish questions directly to the active bank, auto-approve questions, create final answer keys for published assessments, or produce questions with factual errors or culturally biased language without flagging them.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `subject_area` | Yes | Topic domain (e.g., Java fundamentals, data structures, networking) |
| `learning_outcome` | Yes | Specific skill or knowledge to assess |
| `question_type` | Yes | `true_false`, `single_choice`, `multiple_choice`, or `mixed` |
| `difficulty_target` | Yes | `easy`, `medium`, `hard` |
| `question_count` | Yes | Number of questions to generate |
| `level` | No | Audience level (intro, intermediate, advanced) |
| `language` | No | Natural language for question text; defaults to English |
| `constraints` | No | Teacher restrictions (avoid topic X, use terminology Y) |
| `prior_questions` | No | Existing bank questions to avoid duplication |

## Output Contract

```json
{
  "schema_version": "1.0",
  "generation_request_id": "QG-001",
  "subject_area": "Java fundamentals",
  "learning_outcome": "Identify correct use of conditional expressions",
  "questions": [
    {
      "item_id": "ITEM-001",
      "type": "single_choice",
      "stem": "Which statement correctly implements a ternary conditional in Java?",
      "options": [
        { "label": "A", "text": "int x = (a > b) ? a : b;", "is_correct": true },
        { "label": "B", "text": "int x = if (a > b) a else b;", "is_correct": false },
        { "label": "C", "text": "int x = a > b then a else b;", "is_correct": false },
        { "label": "D", "text": "int x = a > b ? b : a;", "is_correct": false }
      ],
      "correct_option_labels": ["A"],
      "explanation": "The ternary operator syntax is condition ? valueIfTrue : valueIfFalse.",
      "difficulty_suggested": "easy",
      "learning_outcome_suggested": "Ternary operator syntax",
      "distractor_rationale": ["B is incorrect Java syntax", "C uses pseudo-code syntax", "D inverts the result"],
      "quality_flags": [],
      "requires_teacher_review": true
    }
  ],
  "batch_quality_summary": {
    "generated_count": 5,
    "flagged_count": 1,
    "duplicate_risk_count": 0
  }
}
```

## Question Type Rules

| Type | Alternatives | Correct answers |
| --- | --- | --- |
| `true_false` | Exactly 2 (True / False) | Exactly 1 |
| `single_choice` | 2–5 options | Exactly 1 |
| `multiple_choice` | 2–6 options | 1 or more |

The agent must validate these constraints and flag violations before returning the batch.

## Quality Rules

Questions must have a unique, clear stem. Alternatives must be plausible. The correct option must be unambiguously correct. Distractors must represent realistic misconceptions, not nonsense. Answer explanations must support teacher review decisions. The agent must not include trick questions, double negatives, or unstated assumptions.

## Uncertainty Flags

| Flag | Trigger |
| --- | --- |
| `possible_ambiguity` | More than one option could be argued correct |
| `weak_distractors` | Incorrect options are too obvious or absurd |
| `duplicate_risk` | Question is structurally similar to a prior_question in bank |
| `factual_uncertainty` | Agent is not confident about the correct answer |
| `scope_mismatch` | Generated question does not match the declared learning outcome |
| `language_quality` | Stem or options have grammatical or clarity issues |

## Human Control

Every generated question enters the bank as `ai_generated` and requires teacher curation before it can be used in an assessment. The teacher can:

- approve: question moves to `approved`;
- edit and approve: teacher edits content, then approves;
- regenerate: request a new question for the same slot;
- reject: question is moved to `rejected` with optional reason.

No question can skip teacher approval.

## Handoff To Assessment Assembly Agent

Pass approved questions from the bank, including their `item_id`, `type`, `difficulty`, `learning_outcome`, and `scoring_weight` if assigned.

## Logging Requirements

Log subject area, learning outcome, question count requested and returned, flagged count, model policy, token and cost estimate, any factual uncertainty flags, and the agent run ID for traceability to subsequent curation events.

## Acceptance Criteria

The Question Generation Agent is ready when it generates well-structured questions for all three supported types, validates type-specific cardinality rules, attaches explanations and distractor rationale, flags quality issues, produces output in `pending_review` state, and logs the full execution with cost estimate.

<!-- nav -->

---

← [Ops Agent](ops-agent.md) | [↑ inicio](#question-generation-agent) | [README](README.md) | [Distractor Quality Agent →](distractor-quality-agent.md)
