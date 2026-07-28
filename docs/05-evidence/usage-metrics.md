# Usage Metrics

Capture product activity that supports traction claims, activation analysis, cost control, and plan limits.

## UsageEvent Fields

| Field | Required | Notes |
| --- | --- | --- |
| `usage_event_id` | Yes | Stable unique ID. |
| `timestamp` | Yes | Server-side timestamp. |
| `teacher_account_id` | Yes | Actor or owner. |
| `customer_id` | When known | Required for pilots and revenue linkage. |
| `assessment_id` | When applicable | Required for assessment activity. |
| `mode` | When applicable | `open`, `closed`, or `mixed`. |
| `event_type` | Yes | Assessment created, rubric approved, submission analyzed, feedback approved, question generated, snapshot frozen, invitation sent, attempt submitted, report exported, etc. |
| `quantity` | Yes | Count associated with the event. |
| `plan` | When known | Trial, Initial, Pro, Intensive, Institutional Pilot, Department, Institution, or custom contract. |
| `source` | Yes | Product, import, manual admin, integration, or seed/demo. |
| `release_area` | Yes | R01-R06 evidence mapping. |
| `related_cost_event_id` | When available | Links usage to cost. |
| `related_revenue_event_id` | When available | Links usage to paid customer context. |

## Core Metrics

| Metric | Definition |
| --- | --- |
| Active educators | Teachers who performed at least one meaningful action in the period. |
| Assessments created | Open or Closed assessments created. |
| Open submissions processed | Student submissions analyzed in Open workflows. |
| Closed attempts processed | Student attempts graded deterministically against a frozen answer key. |
| Questions generated | AI-generated objective questions saved for review. |
| Questions approved | Teacher-approved questions available for assembly. |
| Answer-key snapshots frozen | Published Closed forms with immutable scoring key. |
| Feedback drafts generated | Student-facing Open feedback drafts. |
| Feedback outputs approved | Drafts approved or edited by teacher. |
| Reports generated/exported | Teacher reports or evidence summaries generated/exported. |
| Repeat usage | Same customer or teacher runs another assessment cycle. |

## Automatic Capture

The product should capture usage automatically when:

- an assessment is created, published, archived, or cloned;
- a rubric, question, feedback draft, report, or item analytics summary is generated or approved;
- a submission is analyzed;
- a Closed invitation is created or sent;
- a Closed attempt is submitted and scored;
- results or teacher reports are published/exported;
- a plan limit is consumed.

## Manual Capture

Manual usage notes can supplement telemetry:

- offline assessment runs imported later;
- guided pilot work performed by the founder;
- time estimates before/after the workflow;
- qualitative notes about adoption friction;
- excluded demo/seed data.

Manual records must mark `source = manual admin` or equivalent so traction dashboards can filter real product telemetry from assisted validation work.

<!-- nav -->

---

← [Revenue](revenue.md) | [↑ inicio](#usage-metrics) | [README](README.md) | [Agent Logs →](agent-logs.md)
