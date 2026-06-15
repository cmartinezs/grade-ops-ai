# Ops Agent

The Ops Agent supports traceability, evidence capture, usage tracking, and business proof.

It is the operational layer that helps GradeOps AI demonstrate AI-native operations and business viability.

## Role

Capture, summarize, and monitor workflow evidence across agents, assessments, submissions, customers, costs, approvals, and pilot outcomes.

The Ops Agent is not only a technical logger. It produces evidence for product quality, hackathon submission, unit economics, customer validation, operational debugging, and business dashboards.

## Non-Goals

The Ops Agent must not change grades, edit student feedback, approve teacher-facing outputs, hide failures, mix related-party and arms-length revenue, treat credits/free tiers as zero cost without reporting, expose private student details in public evidence, or fabricate business traction.

## Inputs

| Input | Required | Description |
| --- | --- | --- |
| `agent_events` | Yes | Logs from all agent runs. |
| `workflow_events` | Yes | Assessment/submission/approval status changes. |
| `usage_events` | Yes | Assessments, submissions, feedback, reports. |
| `cost_events` | Yes | Model, token, cloud, payment, support cost data. |
| `customer_events` | Yes | Pilot/customer status. |
| `revenue_events` | Yes | Payment or commitment evidence. |
| `approval_events` | Yes | Teacher approvals, edits, rejections. |
| `testimonial_events` | No | Customer quotes/permission. |
| `external_evidence_links` | No | Stripe, bank, Google Cloud, screenshots, invoices. |

## Output Contract

```json
{
  "schema_version": "1.0",
  "ops_summary": {
    "assessment_id": "ASSESSMENT-001",
    "customer_id": "CUSTOMER-001",
    "agent_runs": 95,
    "agent_success_rate": 0.97,
    "submissions_processed": 30,
    "feedback_outputs_generated": 30,
    "teacher_approval_summary": {"approved": 26, "edited": 4, "rejected": 0},
    "estimated_cost_usd": 0.84,
    "estimated_time_saved_minutes": 180,
    "evidence_links": []
  },
  "business_summary": {
    "offer": "Pilot Pack",
    "revenue_status": "paid",
    "amount_usd": 99,
    "related_party": false,
    "marketing_spend_usd": 0
  },
  "warnings": [],
  "missing_evidence": [],
  "next_action": "review_evidence_dashboard"
}
```

## Core Responsibilities

### 1. Agent Run Logging

Ensure every agent run is logged with agent name/version, workflow stage, assessment/submission/customer linkage, model policy, token estimate, cost estimate, status, retry/failure, uncertainty flags, and teacher approval state.

### 2. Workflow Evidence

Track assessment created, rubric generated, rubric approved, submissions received, grading suggestions generated, feedback generated, feedback approved, learning gaps generated, recovery activity generated, and teacher report generated.

### 3. Business Evidence

Track customer segment, pilot status, plan/offer, payment/commitment status, revenue by month, related-party flag, marketing spend, testimonial status, and evidence links.

### 4. Cost Evidence

Track model cost estimate, cloud runtime estimate if available, storage/logging estimate if available, payment processing fees, credits/free-tier coverage, cash cost, and allocated tooling if reported.

## Evidence Dashboard Metrics

| Metric | Source |
| --- | --- |
| Assessments processed | Workflow events. |
| Submissions reviewed | Submission events. |
| Feedback outputs generated | Feedback Agent logs. |
| Agent runs | Agent logs. |
| Agent success/failure/retry rate | Agent logs. |
| Model usage | Agent logs. |
| Estimated AI cost | Agent/cost logs. |
| Teacher approval/edit/rejection rate | Approval events. |
| Revenue | Revenue ledger. |
| Related-party revenue | Revenue ledger. |
| Marketing spend | Marketing ledger. |
| Testimonials | Customer evidence. |

## Missing Evidence Detection

| Missing evidence | Trigger |
| --- | --- |
| `missing_revenue_evidence` | Paid/committed customer without proof link. |
| `missing_related_party_flag` | Revenue event lacks relationship status. |
| `missing_cost_estimate` | Agent run lacks model/cost data. |
| `missing_teacher_approval` | Student-facing output lacks approval. |
| `missing_customer_consent` | Testimonial exists without permission. |
| `missing_api_usage_evidence` | No external API/billing proof attached. |
| `missing_marketing_spend_record` | No spend record or zero declaration. |

## Human Control

Operators can review the dashboard, correct metadata, attach evidence links, mark related-party status, export evidence, exclude private data from public views, and prepare hackathon summaries. Ops Agent must not publish evidence externally by itself.

## Privacy Rules

Ops evidence must separate internal private data, judge-verifiable private evidence, public demo evidence, anonymized customer proof, and student-safe summaries. Student-level data should be minimized in business-facing evidence.

## Logging Requirements

The Ops Agent itself logs evidence summaries generated, dashboard exports, missing evidence warnings, manual operator corrections, public/private evidence classification, and submission readiness.

## Acceptance Criteria

The Ops Agent is MVP-ready when every agent run can be traced, every assessment has usage metrics, cost/model metadata is present or flagged as missing, approval states are visible, customer/pilot evidence can be linked, revenue and related-party flags can be tracked, dashboard data supports the demo, and no private student data is required for public evidence.

<!-- nav -->

---

← [Teacher Report Agent](teacher-report-agent.md) | [↑ inicio](#ops-agent) | [README](README.md) | [Question Generation Agent →](question-generation-agent.md)
