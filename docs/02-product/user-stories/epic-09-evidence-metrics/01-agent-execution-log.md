# US-080: Agent Execution Log

- **Epic:** 09 — Evidence and Metrics
- **Priority:** P0
- **ID:** US-080

## Story

As an operator, I want every agent execution logged so we can prove AI-native operations and debug the workflow.

## Acceptance Criteria

- Every agent run has timestamp, agent, model, input summary, output summary, status, cost estimate, and approval state.
- Logs are associated with assessment/customer.
- Logs are visible in internal dashboard.
- Failed/retried runs are captured.

---

## Definition of Done

> Precise, verifiable conditions for execution completeness. Complements AC: covers tests, migrations, agent logs, UI paths, etc.

- [ ] `AgentExecutionLog` has one durable row per `AgentAttempt`, linked to `AiOperation` and `AgentRun` when those records exist.
- [ ] Each log records provider, model, agent name/version, prompt/schema version, status, started/completed timestamps, retry count, normalized error code, token estimate, cost estimate and approval state.
- [ ] Input/output are summarized or hashed; prompts, full responses, secrets, signed links and unnecessary PII are not persisted in technical logs.
- [ ] Failed, retried, timed out and validation-failed executions are captured with enough evidence to debug without re-running the model.
- [ ] Logs can be queried by assessment, teacher/organization scope, operation id, agent, provider/model and environment.
- [ ] Cross-service correlation is preserved through trace/correlation/request IDs without using those IDs as metric labels.
- [ ] Internal/operator access enforces permissions and ownership/scope; Teacher-facing views expose only student-safe data.
- [ ] Tests cover successful run, provider failure, output validation failure, retry, cost missing-with-reason, access denial and redaction.

## Technical Notes

> Repos and layers affected. Implementation hints, constraints, or known gotchas at story time.

- **Area:** `api/`, `agents/`, `web/`, `infra/`
- `api/`: owns persistence, Flyway migrations, query endpoints, ownership/scope and dashboard/operator read models.
- `agents/`: returns structured execution metadata, normalized errors, provider/model, token/cost estimates and trace context; it does not persist product-domain entities.
- `web/`: can show inspection surfaces only through `api/` routes and capability checks; it must not read `agents/` directly.
- `infra/`: observability storage/export/retention differs by environment; `demo` and `beta` must preserve the same semantic fields.
- Use `docs/master-plan/analysis/api-agent-orchestration-strategy.md`, `security-strategy.md` and `observability-strategy.md` when atomizing tasks.

## Dependencies

> Other user stories or epics that must be complete before this one can be executed.

| Depends on | Reason |
|------------|--------|
| US-011 | Initial assessment generation is the first agent run that must be logged |
| US-012 | Regeneration proves repeated attempts/versioned runs |
| D-04 provider/model policy | Log fields and cost attribution require provider/model semantics |
| D-06 agent log schema | This story materializes the rich execution evidence model |

## Complexity

> **S** = 1-2 days · **M** = 3-5 days · **L** = 1-2 weeks · **XL** = should be split

**Estimate:** M *(shared API/agents contract, persistence, redaction, query and cross-service tests)*
