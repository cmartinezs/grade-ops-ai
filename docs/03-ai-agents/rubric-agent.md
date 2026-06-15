# Rubric Agent

The Rubric Agent creates and validates a structured scoring rubric aligned to the teacher-approved assessment.

## Role

Generate rubric criteria, weights, levels, scoring guidance, and validation notes for a practical programming assessment.

The Rubric Agent is the quality-control bridge between assessment design and grading assistance.

## Non-Goals

The Rubric Agent must not start grading, approve its own rubric, silently change an approved assessment, create vague criteria, introduce unrelated topics, or create final grading rules without teacher review.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `assessment` | Yes | Teacher-approved or draft assessment brief. |
| `learning_objectives` | Yes | Objectives generated or supplied by teacher. |
| `expected_evidence` | Yes | What submissions should demonstrate. |
| `programming_language` | Yes | Language or pseudocode context. |
| `target_level` | Yes | Skill level. |
| `preferred_scoring_scale` | Yes | 100 points, 1-7, percentage, rubric levels, etc. |
| `teacher_guidance` | No | Manual rubric preferences. |
| `prior_rubric` | No | Existing rubric to adapt. |
| `common_mistakes` | No | Known student error patterns. |

## Output Contract

```json
{
  "schema_version": "1.0",
  "rubric": {
    "assessment_id": "ASSESSMENT-001",
    "scoring_scale": "100_points",
    "total_weight": 100,
    "criteria": [
      {
        "criterion_id": "C1",
        "name": "Correct use of conditionals",
        "description": "Evaluates whether the solution applies conditional logic correctly.",
        "weight": 20,
        "levels": [
          {"level": "excellent", "score_range": "18-20", "description": "Handles all required branches."},
          {"level": "partial", "score_range": "10-17", "description": "Uses conditionals but misses cases."},
          {"level": "insufficient", "score_range": "0-9", "description": "Conditionals are absent or incorrect."}
        ],
        "evidence_to_look_for": ["if/else branches", "correct boolean expressions"],
        "common_mistakes": ["missing else branch", "incorrect logical operator"]
      }
    ]
  },
  "validation": {
    "status": "valid_with_notes",
    "total_weight_valid": true,
    "alignment_notes": [],
    "ambiguity_warnings": [],
    "coverage_warnings": []
  },
  "requires_teacher_approval": true,
  "next_action": "teacher_review"
}
```

## Quality Rules

The rubric must align with learning objectives, define observable evidence, support partial credit, match the target level, and be usable by the Grading Agent.

## Validation Checks

| Check | Requirement |
| --- | --- |
| Weight sum | Criteria should sum to expected total. |
| Objective coverage | Each objective maps to at least one criterion. |
| Evidence clarity | Criteria define what to look for. |
| Level clarity | Performance levels are distinguishable. |
| Difficulty fit | Criteria match target level. |
| Assessment fit | No unrelated criteria are added. |

## Uncertainty Flags

| Flag | Trigger |
| --- | --- |
| `criteria_weight_mismatch` | Weights do not sum correctly. |
| `objective_not_covered` | A learning objective lacks coverage. |
| `criterion_too_vague` | Criterion cannot guide grading. |
| `level_too_ambiguous` | Performance levels overlap. |
| `assessment_mismatch` | Criterion is not supported by assessment. |
| `requires_teacher_policy` | Teacher grading policy is needed. |

## Human Control

Teacher can edit criteria, weights, levels, notes, and approve/reject/regenerate. Grading must use only the approved rubric version.

## Handoff To Grading Agent

Pass approved rubric, criterion IDs, weights, levels, evidence guidance, common mistakes, teacher notes, and rubric version.

## Logging Requirements

Log assessment ID, rubric version, criteria count, validation status, warnings, uncertainty flags, model/cost estimate, and teacher approval state.

## Acceptance Criteria

The Rubric Agent is MVP-ready when it produces a structured rubric, validates weight and objective coverage, provides evidence guidance, flags ambiguity, requires teacher approval, and creates an auditable log.

<!-- nav -->

---

← [Assessment Agent](assessment-agent.md) | [↑ inicio](#rubric-agent) | [README](README.md) | [Grading Agent →](grading-agent.md)
