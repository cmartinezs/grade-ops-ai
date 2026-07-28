# Pricing

GradeOps AI sells bounded assessment-operation capacity, not unlimited access to an AI model.

The canonical commercial unit is the **GradeOps credit**. Submission counts, assessments, tokens, provider calls, and runtime cost remain operational metrics, but they are not interchangeable billing units.

See the accepted decision: [`Credit-Based Pricing And Workflow Metering`](../99-decisions/2026-07-27-credit-based-pricing.md).

## Pricing Principles

- Price the workflow outcome and teacher value, not individual model calls.
- Keep the commercial model independent from provider/model names and token prices.
- Quote credits before execution using observable workload characteristics.
- Reserve credits before work and confirm consumption only after a valid result.
- Do not charge for internal failures, normal retries, or duplicate requests.
- Never sell unlimited GenAI evaluation or feedback.
- Track actual runtime cost separately from the commercial credit charge.
- Validate willingness to pay with real offers, not hypothetical enthusiasm.
- Treat all prices and included quantities below as `Credit Model v0.1` experiments.

## Credit Definition

Initial planning definition:

```text
1 GradeOps credit = USD 0.01 of P90 technical budget
```

This definition creates an economic reserve for provider inference, retries, structured-output repair, processing, and cost variability. It does not mean:

- one token;
- one provider call;
- one submission;
- one minute of processing;
- exactly USD 0.01 of actual provider spend.

Using the working exchange-rate buffer from the cost model:

```text
USD/CLP reference:              946.14
Exchange-rate buffer:           5%
Reserved technical cost/credit: approximately CLP 9.93
```

The credit definition and workflow schedule are versioned. Existing customer balances must not change retroactively when routing or providers change.

## Initial Workflow Catalog

These values are hypotheses to calibrate with beta telemetry:

| Operation | P50 cost hypothesis | P90 cost hypothesis | Credits |
| --- | ---: | ---: | ---: |
| Import and structure a rubric | USD 0.005 | USD 0.01 | 1 |
| Analyze assessment instructions | USD 0.01 | USD 0.02 | 2 |
| Generate or improve a rubric | USD 0.015 | USD 0.03 | 3 |
| OCR/vision block up to 10 pages | USD 0.015 | USD 0.04 | 4 |
| Standard evaluation with feedback | USD 0.04 | USD 0.08 | 8 |
| Complex, extensive, or multi-file evaluation | USD 0.08 | USD 0.16 | 16 |
| Independent second standard review | USD 0.04 | USD 0.08 | 8 |
| Regenerate feedback without reevaluation | USD 0.01 | USD 0.03 | 3 |
| Section results summary | USD 0.03 | USD 0.08 | 8 |

### Standard Evaluation

An 8-credit standard evaluation includes:

- a previously configured rubric;
- one bounded submission;
- analysis against rubric criteria;
- proposed score;
- criterion-level evidence;
- feedback draft;
- structured output validation;
- normal technical retries and output repair.

### Complex Evaluation

A workflow may be quoted as complex only through published, observable criteria such as:

- multiple files;
- a long document or codebase;
- intensive multimodal analysis;
- an unusually large rubric;
- context above the standard limit;
- an independent second reasoning pass;
- a capability that requires a premium model tier.

GradeOps must show the reason and credit quote before execution. It must not silently reclassify an 8-credit operation as 16 credits after processing.

If a complex evaluation already includes OCR or another component, GradeOps must not charge that component again.

## Credit Transaction Rules

| Situation | Commercial treatment |
| --- | --- |
| GradeOps internal error | No debit; release reservation |
| Provider timeout/failure | No debit; release reservation |
| Invalid output repaired automatically | Included in quote |
| Idempotent duplicate request | One reservation and one debit |
| User changes rubric or source files | New quote and workflow |
| Feedback-only regeneration | 3 credits |
| Objectively incomplete result | Free reprocessing or refund |
| Requested independent second opinion | New quoted workflow |
| Authorized routing fallback | Included while inside the workflow budget |

Canonical transaction flow:

```text
quote → reserve → execute → validate usable result → confirm debit
                                  └─ failure → release reservation
```

## Credit Validity

- Subscription credits: monthly allocation.
- Rollover: capped at no more than one additional monthly allocation.
- Purchased top-up credits: explicit 12-month validity.
- Promotional credits: explicit 30-90 day validity.
- Institutional credits: contract-specific validity.

Unlimited rollover creates an unbounded future service liability and should not be offered.

## B2C Plan Hypotheses

| Plan | Monthly price | Credits | Effective price/credit | Intended monthly profile |
| --- | ---: | ---: | ---: | --- |
| Initial | CLP $8,990 | 250 | CLP $35.96 | One 25-student cycle |
| Pro | CLP $24,990 | 750 | CLP $33.32 | About 70 mixed-complexity submissions |
| Intensive | CLP $69,990 | 2,500 | CLP $28.00 | About 210 mixed/multimodal submissions |

Theoretical standard evaluations are 31, 93, and 312 respectively, but real cycles also consume configuration, summaries, OCR, regeneration, and complex evaluation credits.

### Full-Utilization Margin Check

Using a CLP $9.93 P90 reserve per credit and 4% payment processing:

| Plan | Technical reserve | Payment fee | Preliminary contribution | Preliminary margin |
| --- | ---: | ---: | ---: | ---: |
| Initial | CLP $2,483 | CLP $360 | CLP $6,147 | 68.4% |
| Pro | CLP $7,448 | CLP $1,000 | CLP $16,542 | 66.2% |
| Intensive | CLP $24,825 | CLP $2,800 | CLP $42,365 | 60.5% |

These are contribution estimates before unallocated cloud cost, support, tax, acquisition, compliance, and founder labor. A high margin over inference alone does not prove business profitability.

### Top-Up Hypotheses

Top-ups should cost more per credit than the corresponding subscription:

| Package | Suggested price | Price/credit |
| --- | ---: | ---: |
| 100 credits | CLP $4,490 | CLP $44.90 |
| 500 credits | CLP $18,990 | CLP $37.98 |
| 1,000 credits | CLP $34,990 | CLP $34.99 |

## Consumption Personas

### Occasional Teacher

Assumptions: one 25-student section, one monthly assessment, 10% feedback regeneration, one section summary.

| Operation | Quantity | Credits each | Total |
| --- | ---: | ---: | ---: |
| Assessment setup | 1 | 5 | 5 |
| Standard evaluations | 25 | 8 | 200 |
| Feedback regenerations | 3 | 3 | 9 |
| Section summary | 1 | 8 | 8 |
| **Total** |  |  | **222** |

### Typical Teacher

Assumptions: two 35-student sections, 90% standard and 10% complex submissions, 10% feedback regeneration, one shared rubric, two summaries.

| Operation | Quantity | Credits each | Total |
| --- | ---: | ---: | ---: |
| Assessment setup | 1 | 5 | 5 |
| Standard evaluations | 63 | 8 | 504 |
| Complex evaluations | 7 | 16 | 112 |
| Feedback regenerations | 7 | 3 | 21 |
| Section summaries | 2 | 8 | 16 |
| **Total** |  |  | **658** |

### Intensive Teacher

Assumptions: three 35-student sections, two monthly assessments, 80% standard and 20% complex submissions, 10% regeneration, 25% OCR, six summaries.

| Operation | Quantity | Credits each | Total |
| --- | ---: | ---: | ---: |
| Assessment setup | 2 | 5 | 10 |
| Standard evaluations | 168 | 8 | 1,344 |
| Complex evaluations | 42 | 16 | 672 |
| Feedback regenerations | 21 | 3 | 63 |
| OCR blocks | 53 | 4 | 212 |
| Section summaries | 6 | 8 | 48 |
| **Total** |  |  | **2,349** |

The OCR line is a conservative stress scenario. If complex evaluation already includes OCR, the quote engine removes the duplicate component.

## B2B Offer Hypotheses

Institutional pricing combines a platform fee, included credits, and optional services:

```text
Institutional price =
platform/administration fee + included credits + contracted services
```

| Offer | Target | Credits | Indicative price |
| --- | --- | ---: | ---: |
| Pilot | Up to 5 teachers | 4,000 | CLP $199,000/month |
| Department | Up to 20 teachers | 20,000 | CLP $749,000/month |
| Institution | 50+ teachers | From 50,000 | From CLP $1,549,000/month |

Institutional value includes administration, teacher management, budget controls, auditability, reporting, support, retention policies, contractual data handling, and future SSO/SLA capabilities.

Onboarding hypothesis: CLP $500,000-$1,500,000 depending on integration, training, data import, and customization. A strategic pilot may explicitly waive part of it, but the discount must be recorded.

## Value And Break-Even Hypotheses

If manual review takes 10 minutes and GradeOps-assisted approval takes 3 minutes, each submission saves approximately 7 minutes.

At a conservative CLP $15,000/hour teacher-time value:

| Profile | Submissions | Hours saved | Time value | Plan price |
| --- | ---: | ---: | ---: | ---: |
| Occasional | 25 | 2.9 | CLP $43,750 | CLP $8,990 |
| Typical | 70 | 8.2 | CLP $122,500 | CLP $24,990 |
| Intensive | 210 | 24.5 | CLP $367,500 | CLP $69,990 |

Value created does not prove willingness to pay. Trust, review effort, privacy, budget ownership, seasonality, and comparison with general-purpose AI subscriptions must be validated.

With an experimental B2C mix of 25% Initial, 60% Pro, and 15% Intensive:

```text
ARPU:                                approximately CLP 27,740
Contribution after P90 reserve/fees: approximately CLP 17,818
Economic break-even:                 approximately 150-170 paid-teacher equivalents
```

## Validation Plan

Do not publish these prices as final until real interviews and paid tests measure:

- submissions, files, pages, and assessments per month;
- observed P50/P90 workflow cost;
- actual credit consumption per profile;
- teacher review time before and after GradeOps;
- trust and output acceptance rate;
- willingness to pay at the displayed price;
- seasonal cancellation, pause, and rollover behavior;
- preference for monthly, semester, annual, or institutional purchase;
- conversion from quoted workflow to paid execution.

Strong evidence is a paid pilot, deposit, signed purchase intent with budget owner, or continued subscription. “I would use it” is weak evidence.

## Risks And Guardrails

| Risk | Guardrail |
| --- | --- |
| Credits obscure monetary value | Show approximate CLP equivalent under the customer's plan |
| Opportunistic complexity classification | Publish criteria and quote before execution |
| Provider price increase | Version catalog; budget from P90 rather than cheapest current model |
| Seasonal academic usage | Test capped rollover, pauses, semester bags, and annual institutional contracts |
| Intensive plan exceeds individual willingness to pay | Move heavy users toward institutions or optimize batch/routing before discounting |
| General-purpose AI appears cheaper | Differentiate through workflow, consistency, traceability, batch processing, privacy, and exports |
| Unused credit liability | Cap rollover and set explicit expiration |

## Current Pricing Decision

1. Use credits as the customer-facing metering unit.
2. Keep submission and assessment counts as operational/value metrics.
3. Treat 250, 750, and 2,500-credit B2C plans as validation hypotheses.
4. Treat institutional offers as platform plus capacity, not volume discount alone.
5. Do not charge customers for GradeOps/provider failures or normal internal retries.
6. Recalibrate catalog and plans from beta telemetry before final launch pricing.
7. Do not offer unlimited GenAI processing.

<!-- nav -->

---

← [Go-To-Market](go-to-market.md) | [↑ inicio](#pricing) | [README](README.md)
