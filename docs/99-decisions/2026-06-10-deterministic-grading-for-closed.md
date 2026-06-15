# Deterministic Grading for Closed Assessments

- Status: Accepted
- Date: 2026-06-10
- Decision owner: Product / Architecture

## Context

GradeOps AI's core value proposition is AI-assisted assessment operations. However, closed (objective) assessments have a fundamentally different grading characteristic: once a correct answer is defined, scoring is a deterministic comparison, not an interpretive judgment.

The integration analysis (`.raw/12-chat.md`, `.raw/13-chat.md`) explicitly recommends separating AI's role in closed assessments: AI can generate, improve, and explain; it must not introduce probabilistic judgment into objective scoring.

## Decision

For closed assessments, grading follows a deterministic formula:

```text
GradeResult =
  student selected option(s)
  + frozen answer key snapshot
  + scoring policy (full credit / partial credit / penalty for wrong)
  + grade scale
  = calculated score
```

No AI model is involved in this calculation. The Grading Agent (which handles open assessments) is not used for closed assessment scoring.

After scoring, the Item Analytics Agent interprets results, but it cannot modify grades.

Any post-grading key correction by the teacher creates a new recalculation event with a full audit trail. The original result is preserved.

## Rationale

- Deterministic scoring is more accurate, faster, and cheaper than AI scoring for objective questions.
- Students and teachers have stronger trust in a score derived from a clear rule than from an AI suggestion.
- Using AI for objective grading would introduce uncertainty flags where none are needed.
- Separating the grading mechanism by assessment type makes the architecture cleaner.
- GRADE demonstrated that deterministic grading is the right model for closed assessments.

## Consequences

- A scoring engine (not an AI agent) is required for closed assessments.
- The answer key snapshot must be immutable once the assessment starts.
- Post-hoc changes to the key require an explicit teacher action and trigger a recalculation with audit trail.
- The `GradeSuggestion` entity from the open assessment flow does not apply to closed assessments; `GradeResult` is calculated directly.
- Item Analytics Agent runs after scoring and provides interpretation, not scoring.

<!-- nav -->

---

← [Assessment Snapshot on Publish](2026-06-10-assessment-snapshot-on-publish.md) | [↑ inicio](#deterministic-grading-for-closed-assessments) | [README](README.md) | [Student Access via Secure Link →](2026-06-10-student-access-via-secure-link.md)
