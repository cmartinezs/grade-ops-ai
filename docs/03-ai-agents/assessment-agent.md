# Assessment Agent

The Assessment Agent creates a practical programming assessment draft from a teacher's learning goal, constraints, and teaching context.

## Role

Generate a structured assessment draft that a programming educator can review, edit, and approve before it becomes student-facing material.

The agent moves the teacher from a simple intent such as:

> Evaluate conditionals, loops, and functions in Java for first-semester students.

to a usable assessment brief with objectives, instructions, deliverables, constraints, and expected evidence.

## Non-Goals

The Assessment Agent must not publish the assessment directly, create the final rubric, decide grading criteria, create broad non-programming assessments for MVP, assume institutional policies, or produce tasks outside the requested level.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `learning_goal` | Yes | What the teacher wants to evaluate. |
| `topic` | Yes | Programming topic or skill area. |
| `target_level` | Yes | Beginner, intermediate, advanced, or equivalent. |
| `estimated_duration_minutes` | Yes | Expected assessment duration. |
| `programming_language` | Yes | Java, Python, JavaScript, pseudocode, etc. |
| `assessment_type` | Yes | Exercise, lab, short project, practical task. |
| `teacher_constraints` | Yes | Rules, resources, submission expectations. |
| `course_context` | No | Course, module, cohort, or lesson context. |
| `excluded_topics` | No | Topics or techniques not yet covered. |
| `reference_examples` | No | Prior tasks to imitate or avoid. |

## Output Contract

```json
{
  "schema_version": "1.0",
  "assessment": {
    "title": "Basic Java Control Flow Assessment",
    "summary": "Students solve a small console problem using conditionals, loops, and functions.",
    "context": "You are building...",
    "learning_objectives": [
      "Use conditional structures correctly",
      "Apply loops to repeat controlled operations",
      "Decompose logic into functions"
    ],
    "student_instructions": [
      "Read the problem statement carefully.",
      "Implement the requested console program.",
      "Submit your source file and a short explanation."
    ],
    "deliverables": ["Source code file", "Short explanation"],
    "constraints": ["Use only console input/output"],
    "allowed_resources": ["Class notes"],
    "estimated_duration_minutes": 90,
    "difficulty": "basic",
    "programming_language": "Java",
    "expected_evidence": [
      "Correct conditional branching",
      "Loop with proper termination",
      "At least two functions with clear responsibilities"
    ]
  },
  "rubric_seed": {
    "suggested_criteria": ["Correctness", "Conditionals", "Loops", "Functions", "Readability"],
    "notes_for_rubric_agent": ["Prioritize basic control flow over advanced patterns."]
  },
  "warnings": [],
  "uncertainty_flags": [],
  "requires_teacher_approval": true,
  "next_action": "teacher_review"
}
```

## Behavior Rules

The agent should generate practical programming tasks aligned with the requested level, keep student instructions clear, avoid advanced language features unless requested, make expected evidence explicit, and prepare useful seed information for the Rubric Agent.

## Quality Checks

| Check | Requirement |
| --- | --- |
| Level fit | Task matches target level. |
| Topic fit | Task evaluates requested skills. |
| Duration fit | Scope is realistic for time limit. |
| Evidence fit | Deliverables allow grading. |
| Rubric readiness | Rubric Agent has enough structure. |
| Student clarity | Instructions are understandable. |
| MVP fit | Task does not require unsupported product features. |

## Uncertainty Flags

| Flag | Trigger |
| --- | --- |
| `missing_learning_goal` | Learning goal is absent or vague. |
| `ambiguous_level` | Target level is unclear. |
| `duration_mismatch` | Requested task seems too large or too small. |
| `unsupported_format` | Teacher asks for unsupported assessment format. |
| `non_programming_scope` | Request is outside initial wedge. |
| `requires_teacher_policy` | Institutional or classroom rule is needed. |

## Human Control

The teacher must be able to approve, edit, regenerate with notes, or reject the draft. No assessment is final until teacher approval.

## Handoff To Rubric Agent

Pass the teacher-approved assessment title, learning objectives, expected evidence, deliverables, constraints, suggested criteria, warnings, and teacher edits.

## Logging Requirements

Log teacher/account, assessment ID, input summary, output summary, model policy, estimated tokens/cost, warnings, uncertainty flags, and teacher approval state.

## Acceptance Criteria

The Assessment Agent is MVP-ready when it generates a complete structured assessment draft, includes objectives and expected evidence, prepares rubric seed data, flags ambiguity, requires teacher approval, and creates a valid agent log.

<!-- nav -->

---

← [Agents Overview](agents-overview.md) | [↑ inicio](#assessment-agent) | [README](README.md) | [Rubric Agent →](rubric-agent.md)
