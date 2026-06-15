# Roadmap

GradeOps AI is being built as a focused hackathon MVP and a real business experiment. The roadmap prioritizes evidence over feature volume.

Current planning date: **June 8, 2026**. Hackathon deadline: **August 17, 2026 at 1:00 PM PDT**.

## Roadmap Principles

- Build the smallest workflow that can process a real programming assessment.
- Collect business evidence from the first usable version.
- Keep the MVP narrow: programming assessments, teacher approval, agent logs, and reports.
- Avoid becoming a full LMS during the hackathon.
- Prioritize demo clarity over feature breadth.
- Treat evidence as product output, not as final paperwork.

## Critical Path

```mermaid
flowchart LR
  A[Positioning]
  B[Paid pilot offer]
  C[Landing / waitlist]
  D[MVP workflow]
  E[Agent logs]
  F[Pilot runs]
  G[Revenue evidence]
  H[Demo packaging]

  A --> B --> C --> D --> E --> F --> G --> H
```

The project loses strategic strength if the app works but cannot prove:

- real users;
- real or semi-real assessment runs;
- agent execution evidence;
- cost tracking;
- customer feedback;
- payment, payment intent, or signed pilot commitment.

## Phase 1: Strategic Foundation

Goal: define the smallest sellable assessment operation for programming educators.

### Deliverables

- Market wedge: programming educators.
- Core workflow: from learning goal to graded feedback.
- Commercial offer: paid pilot for the next real assessment.
- Landing page copy.
- Discovery interview script.
- Evidence plan for users, payments, usage, costs, and agent logs.
- Initial cost model and pricing policy.

### Success Evidence

- 10+ educator conversations.
- 3+ strong pilot candidates.
- Clear pain validation.
- Clear workflow validation.
- Initial willingness-to-pay signal.

### Exit Criteria

- One clear buyer persona.
- One clear assessment workflow.
- One paid-pilot offer.
- One pricing table with usage limits and no unlimited AI grading.
- One demo scenario based on a realistic programming class.

## Phase 2: Hackathon MVP

Goal: build a demonstrable workflow that can process real programming assessments.

### Core Workflow

1. Teacher creates an assessment from a learning goal.
2. Assessment Agent drafts the activity.
3. Rubric Agent drafts and validates criteria.
4. Students submit answers or code.
5. Grading Agent analyzes submissions.
6. Feedback Agent drafts personalized feedback.
7. Learning Gap Agent detects common issues.
8. Teacher reviews and approves.
9. Teacher Report Agent prepares the teacher report.
10. Ops Evidence Agent logs usage, cost, decisions, and evidence.

### Deliverables

- Teacher workspace.
- Assessment creation flow.
- Rubric generation.
- Submission intake.
- Grading assistance.
- Feedback drafts.
- Teacher approval step.
- Cohort report.
- Agent execution logs.
- Usage events.
- Cost dashboard events.
- Basic Google Cloud deployment.
- Gemini API integration.

### Success Evidence

- 5+ real or semi-real assessment runs.
- 100+ submissions processed.
- 300+ feedback outputs generated.
- 500+ agent log events.
- Measurable teacher time saved.

### Exit Criteria

- A teacher can complete the full workflow without manual setup by the team.
- Every agent run creates structured evidence.
- Every assessment run estimates token usage and cost.
- The demo can show the product and logs in under 3 minutes.

## Phase 3: Pilot Operations

Goal: operate GradeOps AI with real educators and collect business evidence.

### Deliverables

- Paid pilot flow.
- Stripe or manual payment evidence.
- Customer testimonials.
- Before/after workflow comparison.
- Exportable teacher reports.
- Cost tracking.
- Revenue and related-party revenue tracking.
- Marketing spend tracking, even if zero.
- Product feedback loop.

### Success Evidence

- 5-10 educators onboarded.
- 3+ paying customers or paid pilot commitments.
- Clear usage metrics.
- Clear repeat-use signal.
- At least one strong testimonial.

### Exit Criteria

- There is evidence that someone outside the builder team used the workflow.
- There is evidence of payment, payment intent, or signed pilot commitment.
- Costs, payment fees, and runtime usage are tracked separately.
- There is a credible time-saved estimate.

## Phase 4: Hackathon Packaging

Goal: prepare a submission that sells the business, not just the app.

### Deliverables

- 3-minute demo video.
- 500-1000 word narrative.
- Public repository documentation or private repository shared with required judges/testers.
- Testing instructions.
- Revenue evidence.
- Expense evidence.
- Cost model and unit economics summary.
- Agent logs.
- Customer evidence.
- Google Cloud / Gemini usage evidence.
- Business viability summary.
- English version of all required materials.

### Success Evidence

- Judges understand the business in under 60 seconds.
- Demo proves AI-native operation.
- Evidence proves real demand.
- Product looks focused, useful, sellable, and operational.

### Demo Must Show

1. Teacher creates a programming assessment.
2. Agents generate activity and rubric.
3. A student submission is analyzed.
4. Feedback and learning gaps are produced.
5. Teacher approves outputs.
6. Report and dashboard summarize the run.
7. Agent logs prove AI-native operations.
8. Business evidence proves real validation.

## Weekly Execution Plan

| Week | Dates | Primary Outcome | Must Have Evidence |
| --- | --- | --- | --- |
| 1 | Jun 8-14 | Positioning, pilot offer, landing copy, outreach list | 10 target contacts, interview script, pilot offer |
| 2 | Jun 15-21 | Discovery and sales conversations | 5+ conversations, 1+ pilot commitment |
| 3 | Jun 22-28 | MVP skeleton: teacher flow + assessment/rubric generation | Deployed app shell, first Gemini call, first agent log |
| 4 | Jun 29-Jul 5 | Submission intake + grading assistance | End-to-end sample assessment processed |
| 5 | Jul 6-12 | Feedback, learning gaps, teacher report | Report output, approval states, cost estimates |
| 6 | Jul 13-19 | First pilot operations | Real/semi-real educator run, testimonial draft |
| 7 | Jul 20-26 | Payments and business dashboard | Revenue or payment-intent evidence, cost dashboard |
| 8 | Jul 27-Aug 2 | Hardening and second pilot wave | 3+ pilot runs, 100+ submissions target path |
| 9 | Aug 3-9 | Submission package draft | Demo script, narrative draft, testing instructions |
| 10 | Aug 10-17 | Final evidence, video, repo cleanup | Final video, evidence folder, cost/revenue summary |

## Kill Or Pivot Criteria

If these are not true by mid-July, adjust aggressively:

- No teacher understands the offer after a 30-second explanation.
- No educator agrees to test a real or semi-real assessment.
- The workflow requires too much manual intervention to demo clearly.
- Agent logs are not being captured reliably.
- The product cannot estimate cost per assessment.
- The team is building LMS features instead of assessment operations.

Pivot options:

- reduce to one assessment type;
- reduce to one programming language;
- use manual payments instead of full Stripe flow;
- use sample submissions for demo while still collecting customer interviews;
- sell “done-with-you assessment operation” before self-serve SaaS.

## Phase 5: Expansion

Goal: expand beyond the initial programming education wedge after validating the core operation.

### Possible Expansion Paths

- More assessment formats.
- More programming languages.
- Cohort and assignment management.
- Analytics across instructors.
- Institution-level pilots.
- LMS integrations.
- Advanced recovery plans.

### Deferred Until After MVP

- OCR-heavy workflows.
- Mobile app.
- Full LMS features.
- Institution-wide administration.
- Broad multi-subject support.
- Complex curriculum mapping.

## Definition Of Done For Hackathon Submission

GradeOps AI is submission-ready when it can prove:

- a deployed product uses Google Cloud and Gemini API;
- AI agents operate meaningful workflow steps;
- teachers remain in control through approval states;
- logs exist for agent executions and API usage;
- revenue, costs, marketing spend, and related-party revenue are documented;
- real users or pilot customers are evidenced;
- demo video shows the product functioning on the target platform;
- the written narrative explains what AI does, what humans do, and why the business matters.

<!-- nav -->

---

← [Cost Model](cost-model.md) | [↑ inicio](#roadmap) | [README](README.md) | [Hackathon Strategy →](hackathon-strategy.md)
