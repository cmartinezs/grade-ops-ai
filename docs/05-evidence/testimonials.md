# Testimonials

Store attributable or anonymized proof points from educators. Testimonials are evidence only when the source, consent, context, and usage limits are clear.

## Testimonial Fields

| Field | Required | Notes |
| --- | --- | --- |
| `testimonial_id` | Yes | Stable unique ID. |
| `date` | Yes | Date captured or approved. |
| `customer_id` | Yes | Links testimonial to user/pilot evidence. |
| `speaker` | Yes, private allowed | Name, initials, or anonymized label depending on consent. |
| `role` | Yes | Teacher, tutor, instructor, founder, manager, etc. |
| `organization` | When permitted | Can remain private or anonymized. |
| `assessment_mode` | Yes | Open, Closed, or both. |
| `quote` | Yes | Exact quote, lightly cleaned only with permission. |
| `context` | Yes | Interview, demo, paid pilot, report review, after first assessment, etc. |
| `value_claim` | Yes | Time saved, feedback quality, assessment quality, trust, reporting, student support. |
| `before_after` | When available | Baseline workflow and observed change. |
| `source_link` | Yes | Recording, notes, email, form response, or approved document. |
| `visibility` | Yes | Private, internal, anonymized public, or public. |
| `consent_status` | Yes | Pending, approved, denied, or revoked. |

## Capture Rules

- Do not publish quotes until consent is explicit.
- Separate private source notes from public testimonial copy.
- Label whether the quote came from discovery, demo, unpaid pilot, paid pilot, or active usage.
- Avoid unsupported claims such as exact hours saved unless the customer supplied or approved the estimate.
- Record whether the customer is related-party in the linked user/revenue evidence.

## Useful Proof Points

Strong testimonials should support at least one validation claim:

- the assessment workflow is painful and recurring;
- teacher approval makes AI assistance acceptable;
- Open grading/feedback support saves time;
- Closed question-bank or item analytics support improves assessment quality;
- evidence logs, costs, and reports increase trust;
- the customer would pay, renew, or run another assessment.

<!-- nav -->

---

← [Agent Logs](agent-logs.md) | [↑ inicio](#testimonials) | [README](README.md)
