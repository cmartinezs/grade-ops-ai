# Agent Logs

`AgentExecutionLog` is the evidence record for AI-native operations. Every agent run that influences assessment content, feedback, analytics, reports, or business evidence must create one structured log entry.

## Required Fields

| Field | Required | Notes |
| --- | --- | --- |
| `execution_id` | Yes | Stable unique ID for the agent run. |
| `timestamp` | Yes | Server-side timestamp. |
| `request_id` | Yes | Correlates API request, retries, and UI action. |
| `idempotency_key` | When applicable | Required for commands that may be retried. |
| `teacher_account_id` | Yes | Owner of the workflow. |
| `customer_id` | When known | Needed for business evidence and pilot reporting. |
| `assessment_id` | Yes | Assessment context. |
| `submission_id` | For Open grading/feedback | Empty for Closed-only operations. |
| `attempt_id` | For Closed attempt analytics | Empty for Open-only operations. |
| `agent_name` | Yes | Assessment, Rubric, Grading, Feedback, Learning Gap, Recovery, Teacher Report, Question Generation, Distractor Quality, Ambiguity Review, Assessment Assembly, Item Analytics, or Ops Evidence. |
| `agent_version` | Yes | Code/prompt-compatible version. |
| `workflow_stage` | Yes | Release or domain stage such as R02 Open grading or R04 Closed bank review. |
| `provider` | Yes | Runtime provider used for the call. |
| `model` | Yes | Actual model identifier returned or configured. |
| `model_policy` | Yes | Policy selected for workload, such as flash, flash-lite, fallback, or deterministic. |
| `prompt_template` | Yes for LLM calls | File-based prompt template path/name. |
| `prompt_version` | Yes for LLM calls | Version or content hash. |
| `input_summary` | Yes | Redacted summary, not raw private data. |
| `output_summary` | Yes | Structured result summary. |
| `input_tokens` | When available | Estimate allowed until provider billing is integrated. |
| `output_tokens` | When available | Estimate allowed until provider billing is integrated. |
| `estimated_cost_usd` | Yes | Cost estimate at execution time. |
| `latency_ms` | Yes | End-to-end agent runtime duration. |
| `status` | Yes | `succeeded`, `failed`, `blocked`, `retried`, or `cancelled`. |
| `error_code` | On failure | Sanitized. |
| `error_message` | On failure | Sanitized and safe for internal review. |
| `retry_count` | Yes | Zero when no retry occurred. |
| `uncertainty_flags` | When applicable | Required for academic-impact suggestions. |
| `approval_state` | When applicable | Mirrors teacher review state for generated output. |
| `final_action` | Yes | Approved, edited, rejected, published, stored as evidence, or no-op. |

## Automatic Capture

The product should create logs automatically when:

- an Open assessment, rubric, grading suggestion, feedback draft, learning-gap report, recovery activity, or teacher report is generated;
- a Closed question batch, distractor review, ambiguity review, assessment assembly, or item analytics summary is generated;
- the Ops Evidence Agent produces a usage, cost, revenue, or validation summary;
- an agent call fails, retries, or is blocked by validation.

Closed attempt scoring is deterministic and should not be logged as LLM grading. It should still create usage/cost/attempt evidence and, when an agent summarizes analytics, an `AgentExecutionLog` for that summary.

## Manual Review Notes

Manual notes can enrich the log but must not replace automatic fields. Useful manual additions:

- teacher quality assessment;
- reason for editing or rejecting an output;
- screenshots used in validation material;
- demo/pilot context;
- known limitations or follow-up actions.

## Release Connection

| Release | Agent Log Requirement |
| --- | --- |
| R01 | Canonical `AgentExecutionLog` schema and cost/provider/model fields. |
| R02 | Open grading and feedback logs tied to submissions and approval states. |
| R03 | Learning-gap, recovery, and teacher-report logs tied to value evidence. |
| R04 | Closed question generation/review/assembly logs tied to approved bank items and snapshots. |
| R05 | Item analytics logs tied to attempts and deterministic scoring outputs. |
| R06 | Ops Evidence logs tied to revenue, usage, and customer validation summaries. |

<!-- nav -->

---

← [Usage Metrics](usage-metrics.md) | [↑ inicio](#agent-logs) | [README](README.md) | [Testimonials →](testimonials.md)
