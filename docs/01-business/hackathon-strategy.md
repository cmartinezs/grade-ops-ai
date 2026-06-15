# Business Hackathon Strategy

This document translates the Build with Gemini XPRIZE requirements into a business execution plan for `01-business`.

The strategic hackathon interpretation is already defined in `../00-project/hackathon-strategy.md`. This file focuses on how the business side will generate and package evidence.

## Business Objective

GradeOps AI must be presented as a real business, not only as a product prototype.

The business case must prove:

1. real educators have a painful recurring assessment workflow;
2. GradeOps AI operates meaningful parts of that workflow with AI agents;
3. teachers remain in control;
4. customers show willingness to pay;
5. the business tracks revenue, cost, usage, and agent evidence;
6. the model can become sustainable beyond the hackathon.

## Required Business Story

The story for judges:

> Programming educators lose time creating assessments, reviewing code, writing feedback, and preparing reports. GradeOps AI gives them AI-operated assessment capacity. Agents generate assessments, rubrics, grading suggestions, feedback drafts, learning-gap analysis, recovery activities, and teacher reports. Teachers approve important outputs. The business tracks real users, revenue, costs, agent logs, and customer evidence.

## Judging Criteria: Business Interpretation

| Criterion | Business Proof Needed | GradeOps AI Evidence |
| --- | --- | --- |
| Business Viability | Real users, real revenue, sustainable model | Paid pilots, subscription pricing, revenue ledger, cost model, customer testimonials |
| AI-Native Operations | AI live in production executing key decisions | Agent logs, model/API usage, approval states, workflow dashboard |
| Category Impact | Meaningful improvement in education | Time saved, faster feedback, learning-gap reports, student support evidence |

## Hackathon Evidence Plan

| Evidence Type | Minimum Target | Where It Comes From |
| --- | ---: | --- |
| Educator conversations | 10+ | Customer discovery notes |
| Pilot users | 5+ | Pilot pipeline |
| Paid pilots or commitments | 3+ | Payment records or signed commitments |
| Assessments processed | 5+ | Product usage logs |
| Submissions processed | 100+ | Submission records |
| Feedback outputs | 300+ | Agent output logs |
| Agent log events | 500+ | Agent execution log |
| Testimonials | 1+ strong | Customer feedback |
| Cost data | Every run | Cost ledger |
| Revenue data | Every payment/commitment | Revenue ledger |
| Related-party flags | Every revenue event | Revenue ledger |

## Revenue Evidence

Accepted business evidence should be easy to understand and audit.

Potential evidence:

- Stripe dashboard export;
- bank transfer screenshot;
- invoice or receipt;
- signed pilot commitment;
- manual P&L;
- revenue ledger;
- customer confirmation.

Each revenue event should track:

| Field | Required |
| --- | --- |
| Customer | Yes |
| Amount | Yes |
| Currency | Yes |
| USD equivalent | Yes |
| Date | Yes |
| Month | Yes |
| Offer | Yes |
| Payment method | Yes |
| Evidence link | Yes |
| Related-party flag | Yes |

## Customer Evidence

Customer evidence should be credible but privacy-aware.

Capture:

- customer name;
- email;
- phone if permitted;
- role;
- organization if applicable;
- segment;
- assessment use case;
- testimonial or feedback;
- permission level;
- related-party status.

Permission levels:

| Level | Meaning |
| --- | --- |
| Private evidence only | Can be shown to judges if requested, not public |
| Anonymous public quote | Quote can be used without name |
| Named public quote | Quote can be used with name/role |
| Case study | Customer permits a short public story |

## Agent Evidence

Business proof depends on showing that AI agents operate the workflow.

Each agent event should show:

- timestamp;
- customer/teacher;
- assessment;
- submission if applicable;
- agent name;
- action type;
- model used;
- token estimate;
- cost estimate;
- input summary;
- structured output summary;
- status;
- uncertainty flags;
- teacher approval state;
- final action taken.

The business dashboard should make this visible in the demo.

## Business Dashboard For Demo

The dashboard should show, at minimum:

| Widget | Purpose |
| --- | --- |
| Assessments processed | Product usage |
| Submissions processed | Operational volume |
| Feedback outputs generated | AI workload |
| Agent runs | AI-native operations |
| Approval states | Teacher control |
| Time saved estimate | Customer value |
| AI cost estimate | Unit economics |
| Revenue collected | Business viability |
| Marketing spend | Submission transparency |
| Related-party revenue | Rules transparency |

## 3-Minute Demo Business Arc

The demo must sell the business and prove operation.

| Time | Scene | Business Point |
| --- | --- | --- |
| 0:00-0:20 | Problem | Programming assessment work is repetitive, slow, and hard to scale |
| 0:20-0:40 | Product | GradeOps AI runs assessment operations with AI agents |
| 0:40-1:25 | Workflow | Teacher creates assessment; agents generate rubric, analyze submission, draft feedback |
| 1:25-1:55 | Teacher control | Teacher reviews, edits, approves |
| 1:55-2:20 | AI-native operations | Logs show agents, models, costs, statuses, decisions |
| 2:20-2:45 | Business evidence | Show users, pilots, revenue/commitments, testimonials, costs |
| 2:45-3:00 | Close | Small education providers gain academic operations capacity without hiring |

## Written Narrative Business Points

The 500-1000 word narrative should include:

- why programming assessment workflows are painful;
- what humans do versus what AI agents do;
- why teacher approval is mandatory;
- how the business acquired real users;
- how revenue was generated;
- how costs were measured;
- what jobs/economic opportunities are enabled beyond the founding team;
- why the model can scale beyond the initial pilots.

## Business Claim Discipline

Do not overclaim.

| Avoid Saying | Say Instead |
| --- | --- |
| AI grades students automatically | AI drafts grading suggestions against a rubric; teachers approve |
| We replace manual teaching work | We reduce repetitive assessment operations |
| We are an LMS | We operate the assessment workflow |
| We can serve every subject | We start with programming assessments |
| We have unlimited AI grading | Usage is bounded by assessment and submission volume |
| Revenue proves scale | Revenue proves early willingness to pay |

## Success Targets

### Minimum Credible Submission

- 10 educator interviews;
- 3 pilot commitments;
- 1-3 paid pilots;
- 5 assessment runs;
- 100 submissions processed;
- 500 agent log events;
- cost dashboard;
- one testimonial;
- English video and narrative.

### Strong Submission

- 20 educator interviews;
- 5+ paid pilots or commitments;
- 10+ assessment runs;
- 300+ submissions processed;
- 1,000+ agent log events;
- multiple testimonials;
- clear revenue/cost ledger;
- one short customer story.

## Risk Register

| Risk | Business Impact | Mitigation |
| --- | --- | --- |
| No paid customers | Weak viability | Sell Pilot Pack immediately and track commitments |
| Only related-party revenue | Weak evidence | Separate it and prioritize arms-length customers |
| Demo lacks real usage | Looks like prototype | Process real/semi-real assessments before recording |
| AI logs are hidden | Weak AI-native proof | Build dashboard early |
| Costs are not tracked | Weak unit economics | Log tokens, models, cost, and payment fees |
| Teacher trust is weak | Adoption risk | Keep approval states central |
| Narrative is too technical | Judges miss business value | Lead with pain, revenue, users, evidence |

## Final Submission Checklist

| Item | Status |
| --- | --- |
| GitHub repository ready |
| Testing instructions in English |
| 3-minute video recorded |
| Written narrative 500-1000 words |
| Revenue evidence exported |
| Revenue by month prepared |
| Related-party revenue separated |
| Total costs prepared |
| Marketing/customer acquisition spend prepared |
| Agent logs exported or screenshotted |
| API usage evidence prepared |
| Dashboard screenshots prepared |
| Customer evidence prepared |
| Testimonials approved |
| Google Cloud usage visible |
| Gemini API usage visible |
| Pre-existing templates/boilerplate disclosed if used |

## Business Hackathon Conclusion

The hackathon business strategy is simple:

> Build the narrowest workflow that can process real programming assessments, sell it as a paid pilot, and capture enough evidence to prove GradeOps AI is an AI-operated assessment business with real demand, real usage, real costs, and teacher-controlled value.

<!-- nav -->

---

← [Pricing](pricing.md) | [↑ inicio](#business-hackathon-strategy) | [README](README.md)
