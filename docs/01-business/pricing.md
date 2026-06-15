# Pricing

GradeOps AI pricing must validate business value while protecting margins from unbounded AI usage.

The pricing model is based on assessment operations and graded student submissions, not unlimited access.

## Pricing Principles

- Price by value and usage volume, not only API cost.
- Never sell unlimited AI grading.
- Keep plans simple enough for a hackathon demo.
- Make the first paid offer easy to buy.
- Use a paid pilot to validate willingness to pay.
- Track cost per assessment, graded student submission, active teacher, and customer.
- Separate free-tier/credit benefits from real unit economics.
- Separate related-party revenue from arms-length revenue.

## Definition Of Graded Submission

A **graded submission** is the unit consumed by the plans.

It means:

> One student answer, attempt, code file, pasted response, or delivered work item processed by the AI-assisted grading and feedback workflow.

It does **not** mean a student account.

Examples:

| Scenario | Counted Usage |
| --- | ---: |
| 30 students submit 1 answer each | 30 graded submissions |
| 10 students submit 3 exercises each | 30 graded submissions |
| 1 student submits 2 attempts and both are analyzed | 2 graded submissions |
| Teacher creates an assessment but no answers are analyzed | 0 graded submissions |

For MVP pricing, student login is not required. The teacher can upload or paste student responses. The cost driver is the number and size of responses processed, not the number of registered learners.

## Business Unit

Primary unit:

> 1 assessment = activity + rubric + grading assistance for 30 student submissions + personalized feedback + teacher report.

Primary cost drivers:

- number of submissions;
- length/complexity of submissions;
- model routing;
- retries/failures;
- premium fallback usage;
- teacher report generation;
- storage/logging;
- support/onboarding time.

## Recommended Public Pricing

| Plan | Price | Included Usage | Best For |
| --- | ---: | --- | --- |
| Free | US$0 | 1 assessment / 30 graded submissions | Controlled demo and lead capture |
| Teacher Lite | US$12/month | 3 assessments / 90 graded submissions | Tutor or low-volume educator |
| Teacher Pro | US$29/month | 10 assessments / 300 graded submissions | Main individual teacher plan |
| Cohort Pro | US$79/month | 30 assessments / 1,000 graded submissions | Bootcamps, tutors, small academies |
| Pilot Pack | US$99 one-time | 3 real assessments / up to 150 graded student submissions / onboarding | Best hackathon revenue offer |

## LatAm / Chile Testing Prices

These prices can be tested locally while reporting hackathon revenue in USD equivalent.

| Plan | Suggested CLP Price |
| --- | ---: |
| Teacher Lite | $9.990 CLP/month |
| Teacher Pro | $19.990-$24.990 CLP/month |
| Cohort Pro | $59.990-$79.990 CLP/month |
| Pilot Pack | $39.990-$79.990 CLP one-time |

For hackathon validation, the Pilot Pack matters more than perfect subscription optimization.

## Offer Details

### Free

Purpose:

- product trial;
- demo lead capture;
- controlled usage;
- avoid uncontrolled AI spend.

Limits:

- 1 assessment;
- 30 graded submissions;
- basic report;
- no premium fallback by default;
- watermark or “trial” positioning if needed.

### Teacher Lite

Purpose:

- low-friction paid entry;
- tutors and occasional instructors.

Limits:

- 3 assessments;
- 90 graded submissions;
- basic reports;
- standard model routing;
- email support only.

### Teacher Pro

Purpose:

- main individual plan;
- recurring programming educators.

Limits:

- 10 assessments;
- 300 graded submissions;
- feedback drafts;
- learning-gap reports;
- exportable summaries;
- priority feature access during pilot.

### Cohort Pro

Purpose:

- small academy, bootcamp, cohort program.

Limits:

- 30 assessments;
- 1,000 graded submissions;
- cohort reporting;
- multiple courses/cohorts if MVP supports it;
- priority support;
- exportable reports.

### Pilot Pack

Purpose:

- fastest path to real revenue and testimonials;
- guided onboarding;
- hackathon business evidence.

Includes:

- 3 real programming assessments;
- up to 150 graded submissions;
- onboarding call;
- assessment/rubric setup;
- grading assistance;
- feedback drafts;
- learning-gap report;
- teacher approval workflow;
- short value report;
- testimonial request if successful.

## Overuse Pricing

| Extra Usage | Suggested Price |
| --- | ---: |
| Additional graded submission | US$0.08 |
| Additional assessment without submissions | US$0.50 |
| Additional assessment with up to 30 submissions | US$3.00 |
| Additional advanced report | US$1.00 |
| Premium model review | US$0.20-US$0.50 per submission |

Overuse should be logged and visible to the customer before it becomes a billing surprise.

## Margin Logic

Gross margin formula:

```text
Gross margin =
(Revenue - AI runtime - cloud runtime - storage/logging - payment fees - support allocation) / Revenue
```

Minimum planning targets:

| Offer | Target Gross Margin |
| --- | ---: |
| Free | Negative or break-even, capped hard |
| Teacher Lite | 70%+ |
| Teacher Pro | 75%+ |
| Cohort Pro | 70%+ |
| Pilot Pack | 60%+ after onboarding/support time |

The Pilot Pack can have lower margin because it generates evidence, testimonials, workflow learning, and revenue proof.

## Example Margin Thinking

If a 30-submission assessment costs roughly US$0.50-US$1.20 in AI usage with efficient routing, the technical AI cost is not the main price anchor.

The price anchor is customer value:

- hours saved;
- feedback consistency;
- faster student support;
- report generation;
- lower need for additional academic operations labor;
- trust and auditability.

However, technical costs still matter because long code submissions, retries, premium fallbacks, and support time can change margins quickly.

## Pricing Validation Questions

Ask during discovery:

- How often do you run practical programming assessments?
- How many submissions do you usually review?
- How many hours does grading/feedback take?
- What would saving 3-5 hours per assessment be worth?
- Would you rather pay per assessment, per month, or per submission volume?
- Is a guided Pilot Pack easier to approve than a subscription?
- Which price feels too cheap to trust?
- Which price feels expensive but possible?
- Which price is clearly too expensive?
- Who would approve payment?

## Pricing Experiments

| Experiment | Purpose | Success Signal |
| --- | --- | --- |
| Pilot Pack at US$99 | Validate paid willingness quickly | 3+ paid pilots or commitments |
| CLP Pilot Pack | Validate local market affordability | 3+ local paid pilots or strong commitments |
| Teacher Pro at US$29 | Validate recurring individual plan | 2+ customers willing to continue |
| Cohort Pro at US$79 | Validate small-team value | 1 academy/bootcamp serious conversation |
| Overuse pricing | Validate usage-bounded model | Customers accept submission caps |

## Discount Policy

Discounts should not destroy signal quality.

Allowed:

- early adopter discount;
- local market price testing;
- founder-led pilot discount;
- education/community discount;
- limited-time hackathon pilot price.

Avoid:

- unlimited lifetime deals;
- free unlimited pilots;
- hidden discounts that make revenue evidence unclear;
- discounts without written scope limits;
- free work for related parties counted as commercial traction.

Always record:

- list price;
- actual paid price;
- reason for discount;
- whether the customer is related-party;
- whether the payment is arms-length.

## Payment And Reporting

For each payment or commitment, record:

| Field | Required |
| --- | --- |
| Customer ID | Yes |
| Customer segment | Yes |
| Offer | Yes |
| List price | Yes |
| Actual amount | Yes |
| Currency | Yes |
| USD equivalent | Yes |
| Date | Yes |
| Month | Yes |
| Payment method | Yes |
| Processing fee | If known |
| Related-party flag | Yes |
| Evidence link | Yes |

## Pricing Risks

| Risk | Why It Matters | Mitigation |
| --- | --- | --- |
| Unlimited usage | Can destroy margins | Use assessment/submission caps |
| Too-low price | Weakens business viability signal | Price against value saved |
| Too-high first offer | Blocks early validation | Use Pilot Pack |
| Pricing based only on API cost | Underprices support, trust, and operations | Include support, onboarding, evidence, and value |
| Related-party payments inflate traction | Weak judge confidence | Separate and disclose |
| Local CLP pricing conflicts with USD reporting | Confusing evidence | Store original currency and USD equivalent |

## Current Pricing Decision

For the hackathon period:

1. Lead with **Pilot Pack**.
2. Keep subscription plans visible but secondary.
3. Limit usage by assessments and graded student submissions.
4. Track overuse but avoid aggressive billing friction early.
5. Report revenue, costs, discounts, and related-party status transparently.
6. Use pricing conversations as customer discovery, not only sales.

<!-- nav -->

---

← [Go-To-Market](go-to-market.md) | [↑ inicio](#pricing) | [README](README.md) | [Business Hackathon Strategy →](hackathon-strategy.md)
