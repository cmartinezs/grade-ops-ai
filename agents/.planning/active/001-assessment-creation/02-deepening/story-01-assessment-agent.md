# 🔍 DEEPENING: Story 01 — assessment-agent

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Implement the Assessment Agent following the project's fixed agent pipeline pattern (validate command → load data → build envelope → call Gemini → validate structured output → log execution → return result). It must generate a structured assessment draft from a teacher's brief (US-011) and support regeneration with an adjustment-notes field, reusing the same contract (US-012).

**Source stories (root docs repo):** `docs/02-product/user-stories/epic-02-assessment-creation/02-assessment-draft-generation.md`, `03-assessment-draft-regeneration.md`.

**Cross-repo consumer:** the sibling child planning `api/.planning/003-assessment-creation` calls this story's internal endpoint via `agentclient` to generate and persist drafts. That dependency is tracked by the parent monorepo planning (root `.planning/active/008-assessment-creation/`).

---

## Context

- First planning ever executed in this `agents/.planning/` workspace — no prior Assessment Agent contract exists yet. This story defines it.
- Base package: `cl.gradeops.ai.agents`.
- Prompts are versioned `.st` (StringTemplate) files in `src/main/resources/prompts/` — never inlined in Java.
- The agent never persists domain entities — it receives `{Agent}Command`, returns `{Agent}Result`; persistence belongs to `api/` (sibling child planning `api/.planning/003-assessment-creation`).
- Vertex AI Gemini access for this service's Cloud Run service account (`aiplatform.user`) is already provisioned in `infra/terraform/environments/demo/service_accounts.tf` — no infra change needed.

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Gemini structured output is malformed or drifts from the expected schema | M | M | Schema-validate every response before returning it (task 4); reject and surface a clear error rather than returning a malformed result |

---

## Tasks

> Atomized via `/plan-atomize`. Task candidates 4 and 5 from the original story-level breakdown were merged into task-03 — schema validation and execution-log capture are inseparable steps of the same pipeline call, not independently verifiable deliverables (`[CHECK-ATOMICITY]` — fragment rule).

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [AssessmentCommand / AssessmentResult contracts](story-01-assessment-agent/task-01-contracts.md) | GENERATE-DOCUMENT | TODO | `AssessmentCommand.java`, `AssessmentResult.java` |
| 2 | [Prompt template assessment-generation.st](story-01-assessment-agent/task-02-prompt-template.md) | GENERATE-DOCUMENT | TODO | `src/main/resources/prompts/assessment-generation.st`, `org.antlr:ST4` dependency |
| 3 | [AssessmentAgentService (fixed pipeline)](story-01-assessment-agent/task-03-assessment-agent-service.md) | GENERATE-DOCUMENT | TODO | `AssessmentAgentService.java`, `AssessmentAgentException.java`, `AgentExecutionLogPayload.java`, `AssessmentExecutionOutcome.java` |
| 4 | [Internal REST endpoint](story-01-assessment-agent/task-04-internal-endpoint.md) | GENERATE-DOCUMENT | TODO | `AssessmentController.java`, internal-auth filter |
| 5 | [AssessmentAgentService unit tests](story-01-assessment-agent/task-05-unit-tests.md) | GENERATE-DOCUMENT | TODO | `AssessmentAgentServiceTest.java` |

---

## Done Criteria

- [ ] Assessment Agent returns a structured draft (title, context, instructions, objectives, deliverables, constraints) for a valid `AssessmentCommand`.
- [ ] The same command shape accepts an optional `adjustmentNotes` field used for regeneration, without requiring a separate agent or contract.
- [ ] Prompt lives in a versioned `.st` file, never inlined in Java.
- [ ] Structured output is schema-validated before being returned to the caller.
- [ ] Execution metadata (model, cost estimate, status) is produced per invocation, ready for `AgentExecutionLog` persistence in `api/`.
- [ ] The endpoint is internal-only (service-to-service auth), not publicly reachable.
- [ ] Unit tests pass (`./mvnw test`).
- [ ] TRACEABILITY.md updated with new terms from this story (e.g. `AssessmentCommand`, `AssessmentResult`, `assessment-generation.st`).

---

## Inconsistencies Found

*Record any contradictions or gaps detected during this story. Use RECORD-INCONSISTENCY workflow.*

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
