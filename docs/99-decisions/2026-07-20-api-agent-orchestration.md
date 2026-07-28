# API-Agent Orchestration And REST Maturity

- Status: Accepted
- Date: 2026-07-20
- Decision owner: Architecture / Founder

## Context

GradeOps AI uses three runtime surfaces:

- `web/` for teacher and student-facing routes;
- `api/` for domain workflow, persistence, authorization, approval, billing and evidence;
- `agents/` for GenAI execution.

The current Assessment Agent slice proves the separation can work, but future releases will add rubric generation, grading, feedback, closed question authoring, analytics and operational evidence. If each feature adds ad hoc endpoints or lets `web/` orchestrate agents directly, the product will lose workflow consistency, auditability, security and cost control.

The Master Plan also requires agent runtime improvements to be delivered inside functional releases, not as a standalone technical release.

## Decision

Use `api/` as the only public intermediary between `web/` and `agents/`.

For all releases:

- `web/` calls functional `api/` routes only.
- `api/` validates intent, auth, ownership, workflow state, policy, idempotency, usage/cost and evidence.
- `api/` owns persistence and domain transitions.
- `agents/` receives typed commands/envelopes and returns structured results plus execution evidence.
- `agents/` does not persist domain entities, approve academic outputs, publish to students, bill or finalize grades.
- Runtime capabilities such as `AiOperation`, `AgentRun`, `AgentAttempt`, durable dispatch, tool loops, retries and polling are added only when a functional release consumes them.

Every task that defines or changes endpoints, agent contracts, or `web` API routes must include a Richardson REST maturity checkpoint. The checkpoint must cover resource-oriented URIs, HTTP methods, status codes, `Location` for created resources or operations, links/affordances, normalized errors, idempotency and contract tests.

## Rationale

| Concern | Decision impact |
|---|---|
| Product workflow | Teachers and students interact with domain operations, not raw agents. |
| Security | `web/` never receives provider, prompt, tool or internal agent details. |
| Auditability | `api/` can persist operation/run/attempt state, approvals, usage and cost consistently. |
| REST quality | Endpoint tasks have an explicit resource/method/status/link/idempotency review gate. |
| Incremental delivery | Runtime maturity grows through R01-R06 functional value, not through a speculative platform release. |
| Closed grading | Deterministic scoring remains in `api/`; agents can assist authoring/analytics but do not calculate final deterministic grades. |

## Consequences

- R01 must consolidate Assessment Creation with minimal `AiOperation`/`AgentRun`/`AgentAttempt`, idempotency and provider/model evidence before scaling further.
- R02-R06 must include their API-Agent orchestration increment in each release plan.
- `api/.planning`, `agents/.planning`, `web/.planning` and root `.planning` task templates must require the API / Agent / Web Contract Gate when applicable.
- Public API endpoints must be business-resource oriented. Do not expose a public generic `POST /agents/{agentName}/execute`.
- Internal `agents/` endpoints may remain specific while R01 stabilizes; any generic envelope/versioning must preserve compatibility or use an explicit version.
- `web/` route tasks must prove functional access states: loading, success, safe errors and polling/retry/cancel where the API supports them.

## References

- [`docs/.prompting/master-plan-api-agent-orchestration/README.md`](../.prompting/master-plan-api-agent-orchestration/README.md)
- [`docs/master-plan/analysis/api-agent-orchestration-strategy.md`](../master-plan/analysis/api-agent-orchestration-strategy.md)
- [`2026-06-10-agent-runtime-separation.md`](2026-06-10-agent-runtime-separation.md)
- [`2026-07-27-policy-based-model-routing.md`](2026-07-27-policy-based-model-routing.md)

<!-- nav -->

---

← [Archive Event-Specific Constraints](2026-07-20-archive-event-specific-constraints.md) | [↑ inicio](#api-agent-orchestration-and-rest-maturity) | [README](README.md)
