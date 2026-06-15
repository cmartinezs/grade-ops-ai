# Review: 00-project

## Verdict

The `00-project` folder is coherent and strategically useful. It already points to one project:

> **GradeOps AI: AI-native assessment operations for programming educators.**

The strongest decision is the framing shift from an academic evaluation platform to an operational business: GradeOps AI should prove that AI agents can run a real assessment workflow with real educators, real usage, real payments, and auditable evidence.

This folder should be treated as the canonical strategic foundation for the hackathon. The `raw/` folder preserves reasoning history and naming exploration; it is not the current product specification.

## Canonical Decisions

| Decision | Current Position |
| --- | --- |
| Project name | **GradeOps AI** |
| Initial market | Programming educators, tutors, bootcamps, and small academies |
| Initial wedge | Practical programming assessments |
| Primary promise | Run the next programming assessment with AI agents |
| Category | Education & Human Potential |
| Business framing | Assessment operations capacity for small education providers |
| MVP boundary | Assessment creation, rubric, submission intake, grading assistance, feedback, learning gaps, teacher report, agent logs |
| Human role | Teachers retain pedagogical authority and final approval |
| AI role | Agents execute repetitive workflow steps and produce structured evidence |
| Pricing model | Bounded by assessments and graded submissions; no unlimited AI grading |
| Runtime policy | Production grading must use traceable API/cloud billing, not personal AI subscriptions |
| Evidence policy | Usage, agent logs, cost, revenue, testimonials, and customer evidence are first-class product outputs |

## Document Roles

| File | Role | Required Strengthening |
| --- | --- | --- |
| `vision.md` | North Star, wedge, expansion direction, non-negotiables | Add ICP, strategic moat, success metrics, and explicit boundaries |
| `pitch.md` | Messaging, buyer promise, offer, English/Spanish pitch | Add buyer-specific pitch variants, objections, and sharper demo language |
| `problem.md` | Pain definition and why existing tools fall short | Add jobs-to-be-done, buyer economics, validation hypotheses, and anti-scope guardrails |
| `solution.md` | Workflow, agents, evidence model, technical direction | Add MVP scope table, data model, approval states, privacy/security, and non-functional requirements |
| `roadmap.md` | Execution phases and success evidence | Add critical path, weekly plan, kill criteria, and hackathon submission readiness |
| `hackathon-strategy.md` | Rules alignment, evidence strategy, demo logic | Add compliance matrix, evidence inventory, demo script, and final submission checklist |
| `cost-model.md` | Pricing, costs, unit economics, reporting policy | Update model assumptions, add margin formula, add revenue/cost ledger schema, and make pricing defensible |
| `raw/` | Conversation history and source reasoning | Keep unchanged except for metadata notes if needed |

## What Changed In This Revision

- Converted the folder from a set of strategic notes into a decision-ready project foundation.
- Added explicit validation hypotheses so the team can test whether the wedge is sellable before overbuilding.
- Added an MVP scope matrix separating **must build**, **demo support**, **later**, and **do not build now**.
- Added a stricter human-in-the-loop control model for grading, feedback, and student-facing outputs.
- Added privacy, data-retention, IP, and academic-integrity considerations that were underdeveloped.
- Added a weekly execution path from June 8 to the August 17 submission deadline.
- Updated the cost model around current Gemini/Google Cloud assumptions and a stronger unit-economics ledger.
- Added a hackathon evidence inventory so every required submission artifact is collected while building, not at the end.

## Coherence Checks

- **Naming:** canonical name is **GradeOps AI**. `ClassOps AI` remains only in historical notes.
- **Category:** primary category is **Education & Human Potential**; secondary business narrative is support for small education providers.
- **Scope:** the project is not a full LMS, student chatbot, broad academic platform, or OCR-first product.
- **Authority:** the product must not claim autonomous grading authority. Agents suggest; teachers approve.
- **Evidence:** agent logs, API usage, costs, revenue, testimonials, and user evidence are mandatory from the first usable MVP.
- **Costing:** personal AI subscriptions accelerate development but are not production runtime and should not define SaaS pricing.
- **Pricing:** plans must be bounded by assessment and graded-submission volume.
- **Submission:** all hackathon material should be ready in English, even if the working documentation remains bilingual or Spanish-first.

## Remaining Validation Work

Before final submission, validate and cite:

- current Devpost rules;
- final judging criteria;
- required submission artifacts;
- Google Cloud and Gemini usage requirements;
- current Gemini API and Google Cloud pricing;
- education workload statistics used in the pitch;
- customer testimonials and payment evidence;
- whether each paid customer is arms-length or related-party;
- whether any pre-existing code/template/boilerplate requires disclosure.

## Critical Risks

| Risk | Why It Matters | Mitigation |
| --- | --- | --- |
| Overbuilding | A broad LMS will dilute the hackathon story and slow delivery | Build one end-to-end programming assessment workflow first |
| Weak revenue evidence | The hackathon emphasizes real business viability | Sell paid pilots immediately, even before full self-serve subscription |
| Black-box grading perception | Teachers may distrust AI-scored submissions | Keep teacher approval mandatory and log uncertainty flags |
| Unsupported statistics | Uncited workload claims weaken credibility | Use only cited statistics in public pitch/deck/video |
| Runtime cost ambiguity | Unlimited grading can destroy margins | Track cost per agent run, assessment, submission, and customer |
| Rules mismatch | Requirements may change or be interpreted strictly | Recheck Devpost and Google requirements before submission |
| Spanish-only evidence | Submission materials may require English | Prepare English pitch, video script, testing instructions, and narrative early |

## Next Documents To Create Outside `00-project`

These are not replacements for the current files, but implementation artifacts that should come next:

```text
01-product/
  mvp-spec.md
  user-stories.md
  data-model.md
  agent-contracts.md
  approval-workflow.md

02-business/
  landing-copy.md
  sales-script.md
  pilot-offer.md
  customer-interview-script.md
  evidence-ledger-template.md

03-technical/
  architecture.md
  api-contracts.md
  agent-log-schema.md
  deployment-plan.md
  security-privacy.md

04-submission/
  demo-script.md
  narrative-500-1000.md
  testing-instructions.md
  evidence-checklist.md
```

## Final Interpretation

GradeOps AI is not a general academic evaluation platform. It is a focused, AI-operated assessment workflow for programming educators. The hackathon version must prove a business: real educators, real usage, real payments, auditable AI operations, controlled runtime costs, and measurable value.

<!-- nav -->

---

[↑ inicio](#review-00-project) | [README](README.md) | [Pitch →](pitch.md)
