# Users

Track who has evaluated, piloted, adopted, paid for, or rejected GradeOps AI. User evidence connects discovery, usage, revenue, testimonials, and follow-up decisions.

## Customer/User Fields

| Field | Required | Notes |
| --- | --- | --- |
| `customer_id` | Yes | Stable ID used across evidence files and product records. |
| `created_date` | Yes | First contact or account creation date. |
| `name_or_org` | Yes, private allowed | Can be anonymized in public artifacts. |
| `contact` | When permitted | Email, phone, LinkedIn, or other channel. |
| `role` | Yes | Teacher, tutor, bootcamp instructor, academy founder, program manager, etc. |
| `segment` | Yes | Independent, tutor, bootcamp, academy, program, institution, other. |
| `country` | Yes | Needed for pricing and market evidence. |
| `assessment_modes` | Yes | Open, Closed, or both. |
| `use_case` | Yes | Practical grading, feedback, question bank, item analytics, reporting, etc. |
| `stage` | Yes | Interviewed, demo, pilot candidate, paid pilot, active, churned, no fit, referral. |
| `buying_authority` | Yes | Buyer, influencer, evaluator, or unknown. |
| `related_party` | Yes | Required even when false. |
| `consent_status` | Yes | Private only, anonymized allowed, public quote allowed, or pending. |
| `owner` | Yes | Person responsible for relationship and follow-up. |
| `next_action` | Yes | Follow-up, demo, pilot, invoice, testimonial request, no action. |
| `evidence_links` | When available | Discovery notes, usage, payment, testimonial, report, screenshots. |

## Automatic Capture

Automatic user evidence should come from account creation, assessment ownership, usage, plan, and billing records.

## Manual Capture

Manual customer records are expected during early validation. They should capture:

- interview notes;
- pain severity;
- buying intent;
- current workflow;
- trust requirements;
- objections;
- consent and public-use limits;
- relationship context for related-party evidence.

Never publish identifiable customer, student, or institution details without explicit consent.

<!-- nav -->

---

[↑ inicio](#users) | [README](README.md) | [Revenue →](revenue.md)
