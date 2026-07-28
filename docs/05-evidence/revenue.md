# Revenue

Record commercial proof, even if early revenue is small. Revenue evidence must separate actual payments, written commitments, discounts, refunds, and related-party transactions.

## RevenueEvent Fields

| Field | Required | Notes |
| --- | --- | --- |
| `revenue_event_id` | Yes | Stable unique ID. |
| `customer_id` | Yes | Must match user/pilot evidence. |
| `offer` | Yes | Initial, Pro, Intensive, Institutional Pilot, Department, Institution, top-up, onboarding, or custom contract. |
| `status` | Yes | `proposed`, `committed`, `paid`, `refunded`, `cancelled`, or `written_off`. |
| `amount_original` | Yes | Original currency amount. |
| `currency` | Yes | ISO currency code. |
| `amount_usd` | Yes | USD equivalent for reporting. |
| `event_date` | Yes | Date of payment or commitment. |
| `reporting_month` | Yes | Month used in evidence dashboards. |
| `payment_method` | When paid | Stripe, bank transfer, cash, manual receipt, or other. |
| `processing_fee_usd` | When known | Payment processing cost. |
| `list_price_usd` | Yes | Needed to identify discounts. |
| `discount_reason` | If discounted | Early adopter, local test, community, founder-led pilot, etc. |
| `related_party` | Yes | Required even when false. |
| `related_party_explanation` | If true | Relationship to founder/team must be explicit. |
| `evidence_link` | Yes | Receipt, invoice, signed commitment, screenshot, or CRM note. |
| `visibility` | Yes | Private, internal, anonymized public, or public. |
| `owner` | Yes | Person responsible for follow-up. |

## Automatic Capture

Capture automatically when payment or billing integration exists:

- plan selected;
- checkout or invoice status;
- amount, currency, and processing fee;
- customer/account linkage;
- paid/refunded/cancelled status;
- usage plan limits affected by payment.

## Manual Capture

Manual evidence is acceptable for early pilots, but must be labeled:

- signed or written commitment;
- receipt or transfer screenshot;
- manually recorded cash/bank payment;
- discount rationale;
- related-party explanation;
- payment blocker or procurement note.

## Release Connection

| Release | Revenue Evidence |
| --- | --- |
| R01 | `RevenueEvent` exists as part of the evidence backbone. |
| R06 | Pricing, billing, revenue reporting, related-party separation, and gross-margin evidence become validation-ready. |

<!-- nav -->

---

← [Users](users.md) | [↑ inicio](#revenue) | [README](README.md) | [Usage Metrics →](usage-metrics.md)
