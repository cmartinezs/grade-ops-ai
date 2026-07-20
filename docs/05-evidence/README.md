# 05 Evidence

This folder defines the proof GradeOps AI must collect.

It answers:

> What evidence proves real users, real usage, real value, real costs, and real business viability?

## Essence

`05-evidence` exists because product validation is not proven by a polished demo alone.

GradeOps AI must prove:

- real educators engaged;
- real or semi-real assessments processed;
- agent operations happened in production;
- teachers reviewed and approved outputs;
- costs were tracked;
- revenue or payment intent exists;
- customers found value.

## How To Use This Folder

Use these files to collect and organize proof:

1. [`users.md`](users.md) — user and pilot records.
2. [`revenue.md`](revenue.md) — payment, commitment, related-party, and revenue evidence.
3. [`usage-metrics.md`](usage-metrics.md) — product usage and activation metrics.
4. [`agent-logs.md`](agent-logs.md) — evidence of AI-native operations.
5. [`testimonials.md`](testimonials.md) — customer quotes, feedback, and proof of value.

## Evidence Bundles

Every pilot or validation run should produce one evidence bundle.

| Bundle Area | Captured Automatically | Captured Manually |
| --- | --- | --- |
| User/customer | Customer ID, account, assessment ownership, role when available | Interview notes, buying authority, consent, related-party context |
| Usage | Assessments created, submissions processed, Closed attempts, approvals, reports, exports | Time-saved estimate, workflow notes, adoption blockers |
| Agent operations | Agent execution logs, provider/model, token estimates, status, latency, cost estimate | Human review notes, quality interpretation, demo narrative |
| Revenue | Payment event, plan, amount, currency, status when integrated | Manual receipt, commitment evidence, discount reason, related-party explanation |
| Costs | AI/API/cloud/payment/support cost events when available | Manual cost allocation, founder support time, non-integrated spend |
| Value proof | Approved feedback/report outputs, item analytics, teacher approvals | Testimonial, before/after comparison, case-study note |

## Release Evidence Map

| Release | Evidence Focus |
| --- | --- |
| R01 | UsageEvent, AgentExecutionLog, CostEvent, RevenueEvent, ApprovalEvent backbone. |
| R02 | Open assessment setup, rubric approval, submission grading suggestions, feedback drafts. |
| R03 | Learning-gap report, recovery suggestions, teacher report, exportable value summary. |
| R04 | Closed question bank, AI question generation/review, teacher approval, frozen answer-key snapshot. |
| R05 | Signed student links, Closed attempts, deterministic grading, student result access, item analytics. |
| R06 | Pricing, billing, revenue evidence, cost dashboards, customer/testimonial readiness. |

## Cross-Document Traceability

| Evidence Event | Related Docs |
| --- | --- |
| `AgentExecutionLog` | [`agent-logs.md`](agent-logs.md), [`../03-ai-agents/agents-overview.md`](../03-ai-agents/agents-overview.md), [`../04-architecture/data-model.md`](../04-architecture/data-model.md), [`../09-developer-guide/06-agent-development.md`](../09-developer-guide/06-agent-development.md) |
| `ApprovalEvent` | [`../02-product/workflows.md`](../02-product/workflows.md), [`../06-ux/teacher-workspace-ux.md`](../06-ux/teacher-workspace-ux.md), [`../08-user-guide/04-reviewing-ai-outputs.md`](../08-user-guide/04-reviewing-ai-outputs.md) |
| `UsageEvent` | [`usage-metrics.md`](usage-metrics.md), [`../01-business/pricing.md`](../01-business/pricing.md), [`../02-product/metrics.md`](../02-product/metrics.md) |
| `RevenueEvent` | [`revenue.md`](revenue.md), [`../01-business/business-model.md`](../01-business/business-model.md), [`../01-business/pricing.md`](../01-business/pricing.md) |
| `CostEvent` | [`agent-logs.md`](agent-logs.md), [`../00-project/cost-model.md`](../00-project/cost-model.md), [`../01-business/pricing.md`](../01-business/pricing.md) |

## What Belongs Here

- Customer/pilot evidence.
- Revenue evidence.
- Usage summaries.
- Agent log summaries.
- API/dashboard screenshots.
- Cost evidence.
- Testimonials.
- Time-saved estimates.
- Validation-ready evidence checklists.

## What Does Not Belong Here

- Raw private student data.
- Sensitive credentials.
- Unredacted billing secrets.
- Unsupported public claims.
- Product plans without evidence.

## Evidence Principle

Every pilot should produce a traceable evidence bundle:

- who used it;
- what workflow ran;
- what agents executed;
- what the teacher approved;
- what it cost;
- what value was perceived;
- what revenue or commitment was generated.

Evidence must distinguish facts captured by the product from claims gathered manually. Manual evidence is valid, but it should be labeled so later roadmap or investor narratives do not confuse it with automated product telemetry.
