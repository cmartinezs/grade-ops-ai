# Price by Graded Submission

- Status: Accepted
- Date: 2026-06-08
- Decision owner: Business / Founder

## Context

GradeOps AI needs a pricing model that aligns with the value delivered to educators and can be validated early in the hackathon window with real or near-real usage.

Pricing models considered:

| Model | Description | Problem |
| --- | --- | --- |
| Per seat (teacher/month) | Fixed monthly fee per teacher account | Decouples revenue from usage; hard to justify before product proves value |
| Per student enrolled | Fee based on cohort size | Educators don't know class size upfront; seasonal variation makes billing unpredictable |
| Per assessment published | Fee per assessment created | Penalizes teachers who create but don't run many assessments |
| Per graded submission | Fee per student submission processed through the AI grading pipeline | Directly tied to the unit of work the platform automates |

The prior product GRADE was not commercially deployed, so no prior pricing data existed to anchor the model.

## Decision

GradeOps AI prices primarily by **graded submission** — each student submission processed through the AI-assisted assessment pipeline (open or closed mode) counts as one billable unit.

Pricing tiers bundle submissions into monthly or per-pilot allotments. A free tier includes a small submission allowance for onboarding and validation.

## Rationale

- A graded submission is the clearest unit of value: it represents the work the product automates (grading, feedback, logging, evidence capture) that would otherwise fall on the teacher.
- The model aligns revenue with usage: more assessments run → more submissions → more revenue. The educator only pays when the product is actively working.
- It enables a direct ROI story: each submission saves an estimated N minutes of teacher time, making the price-to-value comparison concrete.
- The variable cost of the platform (Gemini API inference, storage, compute) is also per-submission, making margin calculation tractable and safe.
- It works naturally for the target customers (bootcamps, tutors, small academies) who have predictable cohort sizes and assessment frequencies.
- It supports a pilot-first sales motion: offer a fixed submission bundle to validate willingness to pay before committing to monthly billing.

## Consequences

- Pricing tiers are structured around submission volume (e.g., 50, 200, 500 submissions per month).
- The system must count and record every graded submission for billing, evidence, and cost tracking purposes.
- The `AgentExecutionLog` and evidence records must include submission-level tracking from day one.
- Open and closed submissions are treated as equivalent billing units in the MVP; differential pricing by mode can be introduced post-validation.
- AI inference cost per submission must be tracked to protect margin as usage scales.
- The free tier submission allowance serves as both an onboarding tool and a hackathon evidence generator.

<!-- nav -->

---

← [Product Name: GradeOps AI](2026-06-08-use-gradeops-ai-name.md) | [↑ inicio](#price-by-graded-submission) | [README](README.md) | [Mermaid as Diagram Standard →](2026-06-08-mermaid-diagram-standard.md)
