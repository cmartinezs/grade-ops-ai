# Ambiguity Review Agent

The Ambiguity Review Agent checks objective questions for interpretation problems that could invalidate the item or produce inconsistent results during grading.

## Role

Detect ambiguity, double-valid answers, missing context, inappropriate complexity, and other structural problems in question items before they are approved into the active bank.

## Non-Goals

The agent must not approve or reject questions autonomously, determine absolute pedagogical validity, or function as a language-quality proofreader. It detects interpretive risks; the teacher resolves them.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `question` | Yes | Full question item: stem, all options, correct option(s), type |
| `subject_area` | Yes | Domain context for meaning interpretation |
| `learning_outcome` | No | Used to verify question relevance |
| `level` | No | Audience level to calibrate terminology expectations |
| `prior_context` | No | Preceding questions or instructions in the same assessment |

## Output Contract

```json
{
  "schema_version": "1.0",
  "item_id": "ITEM-007",
  "ambiguity_verdict": "flagged",
  "flags": [
    {
      "code": "multiple_valid_interpretations",
      "severity": "high",
      "description": "The stem uses 'default behavior' which may refer to both language defaults and framework defaults without distinguishing context.",
      "suggestion": "Specify whether the question refers to Java language specification or Spring Framework defaults."
    }
  ],
  "requires_teacher_review": true
}
```

## Ambiguity Verdicts

| Verdict | Meaning |
| --- | --- |
| `clean` | No significant interpretation issues detected |
| `flagged` | At least one ambiguity risk requires teacher attention |
| `critical` | Multiple valid correct answers exist; question should not be used as-is |

## Ambiguity Flags

| Flag | Description |
| --- | --- |
| `multiple_valid_interpretations` | More than one option could be reasonably argued as correct |
| `missing_context` | Stem requires information not provided in the question or assessment instructions |
| `double_negation` | Question uses negation inside negation, making intent unclear |
| `vague_qualifier` | Terms like "usually," "sometimes," or "often" make the correct answer ambiguous |
| `terminology_not_taught` | Question references concepts that may not be part of the declared learning outcome |
| `cultural_or_locale_sensitivity` | Answer may vary by region, language version, or library version |
| `assumption_required` | Correct answer depends on an assumption not stated in the stem |
| `scope_drift` | Question tests knowledge beyond the declared learning outcome |
| `dependent_on_previous_question` | Answer requires context from a previous item in the assessment |

## Severity Levels

| Severity | Recommendation |
| --- | --- |
| `low` | Stylistic concern; teacher may accept without change |
| `medium` | Should be addressed before publication but does not invalidate the question |
| `high` | Question is likely to produce inconsistent student results; should be revised |
| `critical` | Multiple correct answers exist or the question cannot be reliably scored |

## Human Control

Teacher receives ambiguity flags as part of the question curation queue. The teacher can:

- accept the question despite flags (noted in curation log);
- edit the stem or options to resolve the flag;
- request regeneration of the full question;
- reject the question.

A question flagged with `critical` severity should require explicit confirmation to approve.

## Logging Requirements

Log item ID, subject area, flags detected with severity, overall verdict, model policy, and token and cost estimate.

## Acceptance Criteria

The Ambiguity Review Agent is ready when it evaluates question stems and alternatives, produces actionable flags with specific descriptions and suggestions, assigns severity levels consistently, and integrates into the question curation review queue without blocking teacher decisions.

<!-- nav -->

---

← [Distractor Quality Agent](distractor-quality-agent.md) | [↑ inicio](#ambiguity-review-agent) | [README](README.md) | [Assessment Assembly Agent →](assessment-assembly-agent.md)
