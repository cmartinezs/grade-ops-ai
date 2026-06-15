# Distractor Quality Agent

The Distractor Quality Agent evaluates the quality of incorrect alternatives (distractors) in objective questions. It helps the teacher identify weak, biased, or misleading options before the question enters the active bank.

## Role

Assess each incorrect alternative in a question and flag problems that would reduce the pedagogical validity of the item. Provide actionable suggestions for improvement.

## Non-Goals

The agent must not auto-fix alternatives without teacher review, block questions from approval, or guarantee that an approved question is pedagogically perfect. Recommendations are suggestions, not verdicts.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `question` | Yes | Full question item: stem, all options, correct option(s) |
| `question_type` | Yes | `true_false`, `single_choice`, `multiple_choice` |
| `subject_area` | Yes | Domain context |
| `learning_outcome` | No | Used to assess distractor relevance |
| `difficulty_target` | No | Used to calibrate expectations on distractor plausibility |

## Output Contract

```json
{
  "schema_version": "1.0",
  "item_id": "ITEM-001",
  "overall_quality": "acceptable",
  "distractor_assessments": [
    {
      "option_label": "B",
      "text": "int x = if (a > b) a else b;",
      "is_correct": false,
      "quality_verdict": "acceptable",
      "flags": [],
      "suggestion": null
    },
    {
      "option_label": "D",
      "text": "Something clearly nonsensical",
      "is_correct": false,
      "quality_verdict": "weak",
      "flags": ["absurd_option"],
      "suggestion": "Replace with a plausible misconception such as reversing the condition."
    }
  ],
  "summary_flags": ["weak_distractor_present"],
  "requires_teacher_review": true
}
```

## Quality Verdicts

| Verdict | Meaning |
| --- | --- |
| `acceptable` | Distractor is plausible and represents a genuine misconception |
| `weak` | Distractor is too easy to eliminate; lacks pedagogical value |
| `too_similar` | Distractor is nearly identical to the correct answer or another option |
| `partially_correct` | Distractor could be argued as correct under some interpretation |
| `biased` | Distractor contains stereotyping, leading language, or cultural bias |
| `length_mismatch` | Distractor is significantly longer or shorter than the correct answer (cues the test-wise) |

## Distractor Flags

| Flag | Description |
| --- | --- |
| `absurd_option` | Option is clearly non-serious; no student would choose it |
| `duplicate_text` | Two options share identical or near-identical text |
| `partial_overlap_with_correct` | Option includes part of the correct answer |
| `unintended_hint` | Option reveals information that helps the student identify the correct answer |
| `double_negative` | Option uses confusing negation |
| `length_signal` | Correct answer is consistently longer than distractors (format signal) |
| `outdated_content` | Distractor references deprecated behavior or syntax |

## Human Control

Teacher receives the distractor assessment as part of the question curation review queue. The teacher can:

- accept the assessment and approve the question;
- edit the flagged distractor and re-run the check;
- accept the weak distractor intentionally and note it;
- reject the question and request regeneration.

The agent does not block the teacher from approving a question with weak distractors. It flags; the teacher decides.

## Logging Requirements

Log item ID, number of distractors evaluated, flags raised per option, overall quality verdict, model policy, and token and cost estimate.

## Acceptance Criteria

The Distractor Quality Agent is ready when it evaluates all incorrect options per question, applies consistent quality verdicts, produces actionable improvement suggestions, and returns results as part of the question curation queue without blocking teacher authority.

<!-- nav -->

---

← [Question Generation Agent](question-generation-agent.md) | [↑ inicio](#distractor-quality-agent) | [README](README.md) | [Ambiguity Review Agent →](ambiguity-review-agent.md)
