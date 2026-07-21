# Hackathon Strategy

GradeOps AI is being built for the **Build with Gemini XPRIZE** hackathon as an AI-native assessment operations business, not only as a software demo.

## Verified Hackathon Constraints

As of June 8, 2026, the public Devpost rules and challenge page indicate:

- submission period: May 19, 2026 to August 17, 2026;
- deadline: August 17, 2026 at 1:00 PM PDT;
- projects must be newly created after the submission period starts;
- pre-existing templates, frameworks, boilerplates, or code can be used only if their use is explained;
- the business must operate with AI;
- the project must use at least one Google Cloud product;
- LLM-powered projects must include Gemini API usage in the deployed product;
- submissions must disclose revenue, costs, marketing/customer acquisition spend, related-party revenue, users, product evidence, and agent/API logs;
- demo video should be under 3 minutes and show the product functioning on the target platform;
- material must be in English or include English translation;
- evaluation favors real business evidence, not only technical novelty.

These constraints should be rechecked before final submission.

Sources to recheck:

- Devpost challenge page: <https://xprize.devpost.com/>
- Devpost rules: <https://xprize.devpost.com/rules>
- Gemini API pricing: <https://ai.google.dev/gemini-api/docs/pricing>
- Cloud Run pricing: <https://cloud.google.com/run/pricing>
- Firestore pricing: <https://cloud.google.com/firestore/pricing>

## Strategic Thesis

Most teams will build AI tools. GradeOps AI will compete as an operational business: real educators, real assessment workflows, real usage, real payments, and auditable AI agent activity.

The product is intentionally focused on programming educators because this segment has frequent assessment cycles, high grading workload, strong need for personalized feedback, and clear adoption potential for practical workflow tools.

## Winning Position

GradeOps AI is not a generic quiz generator, chatbot, LMS, or grading toy.

It is an AI-operated assessment workflow where agents help educators:

- create assessments;
- generate rubrics;
- process submissions;
- assist grading;
- produce feedback;
- detect learning gaps;
- suggest recovery activities;
- prepare teacher reports;
- log operational evidence.

Teachers remain in control. Agents operate the repetitive workflow.

## Category Fit

Primary category:

> Education & Human Potential

Secondary narrative:

> Small education providers can operate with the assessment capacity of a larger academic team.

This framing avoids making the project sound like a generic teacher tool. It positions GradeOps AI as operational infrastructure for educators and small learning businesses.

## Judging Criteria Mapping

| Criterion | What Judges Need To See | GradeOps AI Evidence |
| --- | --- | --- |
| Business Viability | Real business, real users, real revenue, sustainable model | Paid pilots, pricing table, revenue ledger, cost model, user evidence, testimonials |
| AI-Native Operations | AI live in production executing key decisions/workflows | Agent workflow, agent execution logs, API usage, dashboard, approval states |
| Category Impact | Meaningful improvement in chosen category | Teacher time saved, faster feedback, learning gaps detected, small provider capacity |

## Business Viability Strategy

GradeOps AI targets educators, tutors, bootcamps, and small academies that need to reduce assessment workload without hiring additional academic operations staff.

The initial commercial offer should be simple:

> Run your next programming assessment with AI agents.

Evidence to collect:

- educator interviews;
- pilot commitments;
- paid pilots;
- arms-length revenue separated from related-party revenue;
- usage metrics;
- repeat-use intent;
- testimonials;
- time saved;
- payment evidence;
- product operating costs;
- marketing and customer acquisition spend, even if zero.

Initial commercial offer:

| Offer | Price | Purpose |
| --- | ---: | --- |
| Pilot Pack | US$99 one-time | Fastest path to real revenue and testimonials. |
| Teacher Lite | US$12/month | Entry paid plan. |
| Teacher Pro | US$29/month | Main individual teacher plan. |
| Cohort Pro | US$79/month | Small academies, bootcamps, and tutors. |

No plan should include unlimited AI grading. Usage must be bounded by assessments and graded submissions.

## AI-Native Operations Strategy

GradeOps AI must show agents executing meaningful operational steps, not only generating text.

Core agents:

- Assessment Agent;
- Rubric Agent;
- Grading Agent;
- Feedback Agent;
- Learning Gap Agent;
- Recovery Agent;
- Teacher Report Agent;
- Ops Evidence Agent.

Each agent execution should produce structured logs:

- timestamp;
- user;
- assessment;
- submission when applicable;
- agent name;
- input summary;
- output summary;
- model used;
- token estimate;
- status;
- uncertainty flags;
- teacher approval state;
- estimated time saved;
- estimated cost;
- operational evidence generated.

The business dashboard should also show cost per assessment, cost per graded submission, revenue by customer, payment fees, credits used, cash costs paid, marketing spend, and related-party revenue.

## Submission Evidence Inventory

| Evidence | Where To Capture | Owner |
| --- | --- | --- |
| GitHub repository | Code/docs repo | Team lead |
| Testing instructions | `04-submission/testing-instructions.md` | Product/tech |
| Demo video | 3-minute script and recording | Team lead |
| Written narrative | 500-1000 words in English | Team lead |
| Revenue evidence | Stripe export, bank record, invoice, receipt, or signed commitment | Business |
| Revenue by month | Revenue ledger | Business |
| Related-party revenue | Revenue ledger with flag | Business |
| Total costs | Cost ledger | Tech/business |
| Marketing spend | Marketing ledger, even if zero | Business |
| Agent logs | Product DB/dashboard export | Tech |
| API usage records | Google Cloud/Gemini console screenshots/export | Tech |
| Dashboard screenshots | Internal evidence dashboard | Tech |
| Customer evidence | Consent-aware customer list, testimonials, feedback | Business |
| English translation | Submission folder | Team lead |

## Category Impact Strategy

GradeOps AI improves education workflows by helping teachers provide faster, more consistent, and more actionable feedback.

Expected impact:

- less repetitive work for teachers;
- faster feedback for students;
- earlier detection of learning gaps;
- better support for small education providers;
- stronger assessment consistency across cohorts.

## What The Demo Must Prove

The demo must prove the business and the operation, not just the interface.

The 3-minute video should show:

1. A teacher creates a programming assessment from a learning goal.
2. Agents generate the activity and rubric.
3. A student submission is processed.
4. Agents assist grading and feedback.
5. The teacher reviews and approves.
6. The dashboard shows learning gaps and report output.
7. Agent logs prove AI-native operation.
8. Business evidence proves real validation.

## Demo Script Outline

| Time | Scene | Message |
| --- | --- | --- |
| 0:00-0:20 | Problem | Programming teachers lose hours creating, grading, and writing feedback. |
| 0:20-0:40 | Product | GradeOps AI runs assessment operations with agents while teachers approve. |
| 0:40-1:30 | Workflow | Create assessment, generate rubric, process submission, draft feedback. |
| 1:30-2:05 | AI operations | Show agent logs, model used, cost, approval state, output evidence. |
| 2:05-2:35 | Business evidence | Show pilots, users, payment evidence, cost dashboard, testimonials. |
| 2:35-3:00 | Close | Small education providers gain academic operations capacity without hiring. |

## What Not To Build

Avoid expanding into a full academic platform during the hackathon.

Do not prioritize:

- full LMS functionality;
- complex institutional workflows;
- mobile app;
- OCR-heavy flows;
- advanced curriculum administration;
- too many assessment formats;
- broad multi-subject support.

The winning version should be narrow, useful, demonstrable, and sellable.

## Primary Success Metrics

For the hackathon, success should be measured by evidence.

Target metrics:

- 10+ educator discovery conversations;
- 5+ pilot users;
- 3+ paid pilots or payment commitments;
- 5+ real or semi-real assessments processed;
- 100+ submissions processed;
- 300+ feedback outputs generated;
- 500+ agent log events;
- measurable teacher time saved;
- at least 1 strong testimonial.

## Final Positioning

GradeOps AI helps programming educators operate assessments with AI agents.

It gives small education teams the operational capacity of a larger academic team while keeping teachers in control of judgment, feedback, and student outcomes.

<!-- nav -->

---

← [Roadmap](roadmap.md) | [↑ inicio](#hackathon-strategy) | [README](README.md) | [Vision →](vision.md)
