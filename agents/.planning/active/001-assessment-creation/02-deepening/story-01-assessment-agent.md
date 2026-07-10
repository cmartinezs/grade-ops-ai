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

> **Each row in this table must have a corresponding `task-NN-name.md` file under `story-01-assessment-agent/` before this story can be marked `IN PROGRESS`.** Use `/plan-atomize` to generate all task files at once, or create them individually — but they must exist before execution begins.

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | Define `AssessmentCommand` (brief fields + optional `adjustmentNotes` + optional previous-version reference) and `AssessmentResult` (title, context, instructions, objectives, deliverables, constraints) contracts | GENERATE-DOCUMENT | TODO | `AssessmentCommand.java`, `AssessmentResult.java` |
| 2 | Create prompt template `assessment-generation.st` covering initial generation and regeneration-with-adjustment-notes cases | GENERATE-DOCUMENT | TODO | `src/main/resources/prompts/assessment-generation.st` |
| 3 | Implement `AssessmentAgentService`: validate command → load data → build envelope → call Gemini → validate structured output → log execution → return result | GENERATE-DOCUMENT | TODO | `AssessmentAgentService.java` |
| 4 | Implement structured-output schema validation (reject/raise on malformed Gemini responses before returning) | GENERATE-DOCUMENT | TODO | Validation logic in `AssessmentAgentService` or a dedicated validator |
| 5 | Capture `AgentExecutionLog`-shaping data (model name, cost estimate, status, timestamps) at the point of execution, ready for `api/` to persist | GENERATE-DOCUMENT | TODO | Execution log payload returned alongside `AssessmentResult` |
| 6 | Expose the internal REST endpoint(s) for `api/` to call (service-to-service OIDC, not public) | GENERATE-DOCUMENT | TODO | Controller/endpoint |
| 7 | Unit tests for `AssessmentAgentService` (mocked Gemini call): valid generation, regeneration with adjustment notes, malformed-output rejection | GENERATE-DOCUMENT | TODO | Test classes |

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
