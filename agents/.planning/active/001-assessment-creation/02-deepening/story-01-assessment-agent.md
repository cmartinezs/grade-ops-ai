# 🔍 DEEPENING: Story 01 — assessment-agent

> **Status:** DONE
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
- Every task in this story must be designed and reviewed against `api/docs/gradeops-ai-java-guidelines/` *before* implementation, not corrected after — task-01 needed four separate correction rounds (banned Java exceptions, wrong package placement, missing Lombok `@Builder`, non-exhaustive test assertions) because it was atomized without this cross-check. Tasks 02-05 were rewritten on 2026-07-10 to close that gap; see `RETROSPECTIVE-RAW.md` for the full history.
- **Architecture decisions locked in for tasks 03-05** (each chosen over a simpler alternative, per explicit human direction — see `RETROSPECTIVE-RAW.md` for the full reasoning):
  - The Gemini call is isolated behind `AssessmentGenerationPort` + `GeminiAssessmentGenerationAdapter` (`infrastructure.adapter.out.gemini`), not called directly from application code — matches `01-arquitectura-hexagonal-y-paquetes.md`'s own `agents/` example package.
  - The pipeline is wrapped in an explicit `GenerateAssessmentDraftUseCase` port + thin `GenerateAssessmentDraftHandler`, with `AssessmentAgentOrchestrator` running the actual pipeline — the Nivel-2 "use case con orquestador" pattern from `03-use-cases-orquestadores-y-pasos.md`, not a single flat service class.
  - `AgentExecutionLogPayload` carries the full audit field set `11-seguridad-observabilidad-y-auditoria.md` requires (`agentExecutionId`, `agentName`, `promptVersion`, `inputHash`/`outputHash`, token estimates, `errorCode`), scoped to what `agents/` actually knows — `tenantId`/`teacherId`/resource identifiers are `api/`'s responsibility to attach when persisting the final `AgentExecutionLog` row.

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
| 1 | [AssessmentCommand / AssessmentResult contracts](story-01-assessment-agent/task-01-contracts.md) | GENERATE-DOCUMENT | DONE | `AssessmentCommand.java`, `AssessmentResult.java` |
| 2 | [Prompt template assessment-generation.st](story-01-assessment-agent/task-02-prompt-template.md) | GENERATE-DOCUMENT | DONE | `src/main/resources/prompts/assessment-generation.st`, `org.antlr:ST4` dependency, `AssessmentGenerationTemplateTest.java` |
| 3 | [Assessment draft generation (orchestrator, port, adapter)](story-01-assessment-agent/task-03-assessment-agent-service.md) | GENERATE-DOCUMENT | DONE | `GenerateAssessmentDraftUseCase.java`, `GenerateAssessmentDraftHandler.java`, `AssessmentAgentOrchestrator.java`, `AssessmentGenerationPort.java`, `GeminiAssessmentGenerationAdapter.java`, `AssessmentAgentException.java`, `AgentExecutionLogPayload.java`, `AssessmentExecutionOutcome.java`, `AssessmentConfig.java` |
| 4 | [Internal REST endpoint](story-01-assessment-agent/task-04-internal-endpoint.md) | GENERATE-DOCUMENT | DONE | `AssessmentController.java`, `InternalAuthFilter.java`, `CorrelationIdFilter.java`, `AgentGlobalExceptionHandler.java`, `SharedWebConfig.java` |
| 5 | [Assessment draft generation unit tests](story-01-assessment-agent/task-05-unit-tests.md) | GENERATE-DOCUMENT | DONE | `GenerateAssessmentDraftHandlerTest.java`, `AssessmentAgentOrchestratorTest.java`, `GeminiAssessmentGenerationAdapterTest.java` |

---

## Done Criteria

- [x] Assessment Agent returns a structured draft (title, context, instructions, objectives, deliverables, constraints) for a valid `AssessmentCommand`. **Live-Gemini happy path still UNVERIFIED** — after this story merged, a real `GOOGLE_AI_API_KEY` was obtained and a real request was sent through the full pipeline. The request reached the live Gemini API (independently confirmed via a direct `curl` against `generativelanguage.googleapis.com`) and failed with `429 RESOURCE_EXHAUSTED` — the Google AI Studio account's prepayment credits are depleted. This is an account/billing condition, not a code defect. The failure exercised the full error-handling path end-to-end against a genuine, non-mocked failure: auth, correlation ID, orchestrator validation, `AgentGlobalExceptionHandler`, and a complete failure-path `AgentExecutionLogPayload` all behaved correctly. Human explicitly accepted this as sufficient evidence rather than sourcing a different key. A successful (200, real `AssessmentResult`) response still has never been observed and remains open — see Residual #1. See `RETROSPECTIVE-RAW.md` 2026-07-10 20:05, 2026-07-11, and 2026-07-11 (real-key attempt, this entry).
- [x] The same command shape accepts an optional `adjustmentNotes` field used for regeneration, without requiring a separate agent or contract.
- [x] Prompt lives in a versioned `.st` file, never inlined in Java.
- [x] Structured output is schema-validated before being returned to the caller.
- [x] Execution metadata (model, cost estimate, status) is produced per invocation, ready for `AgentExecutionLog` persistence in `api/`.
- [x] The endpoint is internal-only (service-to-service auth), not publicly reachable.
- [x] Unit tests pass (`./mvnw test`) — 18/18.
- [x] TRACEABILITY.md updated with new terms from this story (e.g. `AssessmentCommand`, `AssessmentResult`, `assessment-generation.st`).

---

## Inconsistencies Found

*Record any contradictions or gaps detected during this story. Use RECORD-INCONSISTENCY workflow.*

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | `AssessmentResult` (task-01) has 6 fields matching US-011's AC exactly; `docs/03-ai-agents/assessment-agent.md`'s Output Contract example has ~15 fields (incl. `summary`, `rubric_seed`, `warnings`, `uncertainty_flags`, `schema_version`) and renames 2 overlapping fields (`learning_objectives`, `student_instructions`) | `docs/03-ai-agents/assessment-agent.md`, US-011 | OPEN | Decide whether `docs/03-ai-agents/assessment-agent.md` should be narrowed to the MVP contract actually atomized, or the extra fields tracked as explicit residuals for a later story |

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | Story Done Criteria #1 (a real Gemini call returning a successful structured draft) is still unproven. A real key was later obtained and used post-merge: the request reached the live Gemini API but failed with `429 RESOURCE_EXHAUSTED` (account's prepayment credits depleted, confirmed independently via direct `curl`) — not a code defect. Error-handling was verified end-to-end against this real failure; the success path was not. | Whoever has a key with usable credits next — run one `POST /internal/agents/assessment` request against a live Gemini backend and record a successful `AssessmentResult` response as evidence | OPEN |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
