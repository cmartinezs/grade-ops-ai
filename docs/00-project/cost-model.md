# Cost Model

GradeOps AI must operate as a measurable business whose agent autonomy is economically bounded.

The product tracks actual runtime consumption by workflow, provider/model, tenant, and customer. Customer pricing is expressed in GradeOps credits, while actual provider spend, cloud spend, cash cost, free-tier coverage, and founder economics remain separate ledgers.

## Canonical Cost Principles

- Do not treat cloud credits or free tiers as zero economic cost.
- Do not tie commercial pricing directly to one provider or model.
- Budget workflows at P90; measure both P50 and P90 from real telemetry.
- Track cost per attempt, workflow, assessment, submission, teacher, tenant, and customer.
- Price heterogeneous workflows in credits rather than pretending all submissions cost the same.
- Never sell unlimited GenAI processing.
- Separate product runtime, development tooling, founder labor, marketing, payment fees, and tax.
- Enforce a maximum technical budget before every workflow starts.
- Record failed and retried provider calls even when the customer is not charged.

Canonical decisions:

- [`Credit-Based Pricing And Workflow Metering`](../99-decisions/2026-07-27-credit-based-pricing.md)
- [`Policy-Based Provider And Model Routing`](../99-decisions/2026-07-27-policy-based-model-routing.md)

## Cost Layers

| Layer | Examples | Treatment |
| --- | --- | --- |
| Variable workflow cost | LLM tokens, OCR/vision, paid retries, external tools | Attribute to workflow and tenant |
| Shared product runtime | Cloud Run, PostgreSQL, storage, logging, email | Allocate by measurable driver where practical |
| Payment and commercial | Processor fees, invoicing, support, onboarding | Include in contribution and offer margin |
| Founder/company fixed cost | Founder labor, hardware, subscriptions, administration | Include in economic burn and break-even |
| Acquisition | Ads, outreach tools, events, commissions | Report separately as CAC/marketing |
| Credits/free tiers | Provider promotional or cloud allowance | Reduce cash paid, not normalized economic cost |

## Runtime Cost Evidence

Every billable or potentially billable execution should record:

```text
estimatedCost
actualCost
inputTokens
outputTokens
cachedTokens
retryCost
provider
model
workflow
tenant
creditQuote
creditCharge
pricingVersion
```

The system must distinguish:

- estimated versus actual/reconciled provider cost;
- technical runtime cost versus customer credit charge;
- normalized economic cost versus cash paid;
- successful customer-billable workflow versus failed paid provider attempts;
- provider promotional coverage versus genuine zero-cost work.

## Workflow Budget Contract

Every agent workflow must enforce:

```text
maxInputTokens
maxOutputTokens
maxModelCalls
maxToolCalls
maxRetries
maxWorkflowCostUsd
modelRoutingPolicy
fallbackPolicy
timeout
idempotencyKey
```

No agent may decide to continue iterating beyond this budget. `api/` authorizes the operation and budget; the Model Router in `agents/` selects an allowlisted provider/model inside those constraints.

## Workflow Cost Hypotheses

These are `ESTIMATED` planning inputs for `Credit Model v0.1`, not observed facts:

| Workflow | P50 | P90 | Credit quote |
| --- | ---: | ---: | ---: |
| Import/structure rubric | USD 0.005 | USD 0.01 | 1 |
| Analyze instructions | USD 0.01 | USD 0.02 | 2 |
| Generate/improve rubric | USD 0.015 | USD 0.03 | 3 |
| OCR/vision up to 10 pages | USD 0.015 | USD 0.04 | 4 |
| Standard evaluation with feedback | USD 0.04 | USD 0.08 | 8 |
| Complex/multi-file evaluation | USD 0.08 | USD 0.16 | 16 |
| Feedback-only regeneration | USD 0.01 | USD 0.03 | 3 |
| Section summary | USD 0.03 | USD 0.08 | 8 |

One credit initially reserves USD 0.01 of P90 technical budget. The workflow catalog must be versioned and recalibrated using beta data.

## Model Routing Economics

Provider/model selection is not part of the ordinary public request. The Model Router evaluates:

```text
privacy/legal constraints
→ required capability
→ authorized budget
→ tenant policy
→ provider health
→ quality target
→ cost and latency
→ preference and fallback
```

Operational routing rules:

- Use economical models for high-volume, bounded work.
- Reserve premium models for objectively complex or quality-critical cases.
- Prefer provider batch pricing when latency is not user-critical.
- Never switch to a more expensive route above the authorized workflow budget.
- Record the resolved provider/model and all paid attempts.
- Treat fallback as part of the original quote only while it remains inside the budget.
- Do not use an additional LLM call solely to route ordinary executions.

Provider/model names and prices change. The pricing registry must be centralized, versioned, and verified against official primary sources before production commitments.

## Credit Economics

Working planning assumptions:

```text
USD/CLP reference:                946.14
Exchange-rate buffer:             5%
Buffered USD/CLP:                 993.45
P90 reserve per credit:           USD 0.01
Reserved cost per credit:         approximately CLP 9.93
Subscription selling range:       CLP 30-40 per credit
Absolute preliminary floor:       CLP 25 per credit
Top-up target:                    CLP 38-45 per credit
B2B committed-volume target:      CLP 25-35 per credit plus platform fee
```

| Selling price/credit | P90 reserve | Technical margin before shared costs |
| ---: | ---: | ---: |
| CLP $25 | CLP $9.93 | 60.3% |
| CLP $30 | CLP $9.93 | 66.9% |
| CLP $35 | CLP $9.93 | 71.6% |
| CLP $40 | CLP $9.93 | 75.2% |
| CLP $45 | CLP $9.93 | 77.9% |

This is not company gross margin. It excludes shared cloud, payment processing, support, tax, acquisition, compliance, development, and profit.

## Initial B2C Contribution Model

Assuming full credit use, CLP $9.93 reserve per credit, and 4% payment processing:

| Plan | Price | Credits | Technical reserve | Preliminary contribution |
| --- | ---: | ---: | ---: | ---: |
| Initial | CLP $8,990 | 250 | CLP $2,483 | CLP $6,147 |
| Pro | CLP $24,990 | 750 | CLP $7,448 | CLP $16,542 |
| Intensive | CLP $69,990 | 2,500 | CLP $24,825 | CLP $42,365 |

The Intensive plan has the narrowest preliminary margin and must not be discounted without observed cost and conversion data.

## Founder Economic Burn

Current fixed-cost assumptions:

| Component | Target-income basis | Market-income basis |
| --- | ---: | ---: |
| Founder labor | CLP $2,380,952 | CLP $2,476,190 |
| Development tools | CLP $33,251 | CLP $33,251 |
| Hardware allocation | CLP $31,500 | CLP $31,500 |
| **Fixed subtotal** | **CLP $2,445,703** | **CLP $2,540,941** |

Including initial production/runtime scenarios:

| Scenario | Approximate monthly economic burn |
| --- | ---: |
| P50, target-income basis | CLP $2.78M |
| P50, market-income basis | CLP $2.87M |
| P90, target-income basis | CLP $3.16M |
| P90, market-income basis | CLP $3.25M |

Initial business targets:

```text
Minimum sustainability MRR: CLP 4.3M-5.0M
Healthy initial MRR:        CLP 5.5M-6.5M
```

The healthy range supports variability, administration, support, and limited reinvestment. It does not finance a complete team.

## Break-Even Hypothesis

With a B2C mix of 25% Initial, 60% Pro, and 15% Intensive:

```text
Blended ARPU:                         approximately CLP 27,740
Blended contribution after reserve:  approximately CLP 17,818
Economic fixed cost/platform range:  CLP 2.7M-3.0M
Break-even:                          approximately 150-170 paid-teacher equivalents
```

Indicative MRR equivalents:

| MRR | Paid-teacher equivalents at blended ARPU |
| ---: | ---: |
| CLP $4.3M | 155 |
| CLP $5.5M | 198 |
| CLP $6.5M | 234 |

Institutional contracts can reduce customer count but add onboarding, support, compliance, and sales-cycle cost.

## Cloud Strategy And Portability

Current operating strategy:

- Use free tiers for development and controlled beta, while recording normalized cost.
- Use paid, privacy-appropriate GenAI for real student data.
- Use GCP as the first production cloud.
- Use Cloudflare initially for complementary security, distribution, and compatible storage where justified.
- Do not operate multicloud before a measurable economic, legal, customer, or resilience requirement exists.

Preserve portability through:

- OCI containers;
- standard PostgreSQL;
- S3-compatible storage interfaces where practical;
- OpenTelemetry;
- externalized configuration;
- Terraform or equivalent infrastructure as code;
- provider/model abstraction;
- portable job/event contracts;
- exportable backups;
- no unnecessary domain logic inside proprietary functions.

Reevaluate AWS when one or more explicit triggers appear:

- monthly cloud spend above approximately USD 2,000-5,000;
- a material B2B contract requires AWS;
- economically significant credits or a 20-30% advantage;
- residency/region requirements;
- dedicated platform/DevOps staffing;
- a non-Google GenAI provider becomes strategically dominant;
- contractual multicloud disaster recovery.

## Product Operating Costs

| Cost area | Required tracking |
| --- | --- |
| GenAI providers | Provider, model, tokens, cache, retries, batch, estimate, reconciled cost |
| Compute | Service, environment, request/CPU/memory allocation |
| PostgreSQL | Instance/storage/backups and tenant allocation when practical |
| Object storage | Bytes, operations, egress, retention |
| Logging/monitoring | Ingestion, retention, alerting, evidence storage |
| Email | Transactional sends and provider charges |
| Payments | Processor fee, tax/withholding where known |
| Support/onboarding | Founder/staff time by customer or offer |
| Security/compliance | Scanning, audit, legal, data-processing obligations |

Personal ChatGPT, Claude, Gemini web, or developer subscriptions may accelerate construction but must never become the untraceable production execution path.

## Ledger Requirements

### `CostEvent`

| Field | Purpose |
| --- | --- |
| `event_id` | Immutable identifier |
| `occurred_at` | Cost timestamp |
| `tenant_id` / `customer_id` | Attribution |
| `workflow_id` / `attempt_id` | Execution attribution |
| `provider` / `model` | Resolved route |
| `category` | Inference, compute, storage, payment, support, tooling, marketing |
| `estimated_amount_usd` | Pre/realtime estimate |
| `actual_amount_usd` | Reconciled amount when available |
| `cash_cost_usd` | Amount actually paid |
| `covered_by_credit_usd` | Promotional/free-tier coverage |
| `pricing_version` | Provider price registry version |
| `evidence_link` | Invoice, export, or billing reference |

### Credit Ledger

| Event | Purpose |
| --- | --- |
| `GRANT` | Subscription, purchase, promotion, or adjustment |
| `RESERVE` | Hold quoted credits before workflow |
| `DEBIT` | Confirm consumption after usable result |
| `RELEASE` | Return reservation after failure/cancellation |
| `EXPIRE` | Apply explicit validity rules |
| `REFUND` | Reverse a confirmed debit with reason |
| `ADJUST` | Audited operator correction |

Credit events must be immutable, idempotent, and reconcilable to workflow and revenue evidence.

## Validation Gates

Before final pricing:

- collect at least 100-300 representative workflow executions;
- calculate P50/P75/P90 cost by workflow and complexity class;
- compare quoted credits with actual normalized cost;
- measure fallback, retry, cache, output-repair, and failure rates;
- reconcile provider dashboards/invoices with internal `CostEvent` totals;
- simulate full utilization, partial utilization, rollover, and semester peaks;
- validate customer willingness to pay with paid offers;
- revise credit catalog or routing before cutting prices.

## Final Rule

GradeOps AI should always be able to answer:

> What did this workflow cost technically, why did it consume this many credits, which provider/model executed it, and what margin remains after the full service cost?

<!-- nav -->

---

← [Solution](solution.md) | [↑ inicio](#cost-model) | [README](README.md) | [Roadmap →](roadmap.md)
