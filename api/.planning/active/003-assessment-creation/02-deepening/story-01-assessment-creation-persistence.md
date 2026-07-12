# 🔍 DEEPENING: Story 01 — assessment-creation-persistence

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Implement the full backend flow for assessment creation: persist the teacher's brief before any agent call (US-010), integrate with the Assessment Agent via the `agentclient` module to generate a structured draft and persist it together with its `AgentExecutionLog` (US-011), and support draft regeneration as a new, non-destructive version linked to the previous one (US-012).

**Source stories:** `docs/02-product/user-stories/epic-02-assessment-creation/01-assessment-brief-intake.md`, `02-assessment-draft-generation.md`, `03-assessment-draft-regeneration.md` (root docs repo).

**Cross-repo dependency:** the `AssessmentCommand`/`AssessmentResult` contract and the internal agent endpoint are implemented in the sibling child planning `agents/.planning/001-assessment-creation`. This story's command/result integration code can be written against the documented contract shape, but end-to-end integration testing (task 8) waits for that endpoint to exist. **As of 2026-07-12**, a second sibling child planning, `agents/.planning/active/002-groq-genai-provider`, extended `AssessmentCommand` with two more nullable fields — see the Inconsistencies Found table below.

---

## Context

- Base package: `cl.gradeops.ai.api`.
- Only the `agentclient` module may import Spring AI / call `agents/` — no other module in `api/` does.
- Brief persistence must happen in its own transaction/request, strictly before the agent-invoking request — a failed agent call must never lose the teacher's input (epic DoD).
- Draft versioning: each regeneration creates a new version row linked to the same assessment; the previous version is never overwritten or deleted and must remain retrievable.
- Every agent execution (initial generation and each regeneration) produces its own `AgentExecutionLog` row (model, cost estimate, status, timestamps) — logs are never shared across executions.
- Check `src/main/resources/db/migration/` for the last Flyway version number before naming new migrations.

---

## Tasks

> Atomized via `/plan-atomize`. Grew from the original 8 candidates to 11 atomic tasks after discovering an existing `Assessment` stub (`StubAssessmentPersistenceAdapter`, whose own comment says "Epic 02 will replace return type with domain Assessment objects") — the new work must replace that stub, not create a disconnected duplicate concept, adding a dedicated `Assessment` aggregate task (task-01) and a dashboard-wiring task (task-10) not present in the original breakdown.

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Assessment aggregate root](story-01-assessment-creation-persistence/task-01-assessment-aggregate.md) | GENERATE-DOCUMENT | TODO | `V9__add_assessments.sql`, `Assessment.java`, real `AssessmentPersistenceAdapter` (replaces stub) |
| 2 | [AssessmentBrief entity + persistence](story-01-assessment-creation-persistence/task-02-assessment-brief.md) | GENERATE-DOCUMENT | TODO | `V10__add_assessment_briefs.sql`, `AssessmentBrief.java` + persistence stack |
| 3 | [AssessmentDraft entity + versioning](story-01-assessment-creation-persistence/task-03-assessment-draft.md) | GENERATE-DOCUMENT | TODO | `V11__add_assessment_drafts.sql`, `AssessmentDraft.java` + persistence stack |
| 4 | [validate-db-orm-consistency (V9+V10+V11)](story-01-assessment-creation-persistence/task-04-db-orm-consistency.md) | GENERATE-DOCUMENT | TODO | Verification evidence, FK-chain integration check |
| 5 | [agentclient module](story-01-assessment-creation-persistence/task-05-agentclient.md) | GENERATE-DOCUMENT | TODO | `AssessmentAgentClient.java`, `AgentClientConfig.java` |
| 6 | [Brief intake endpoint](story-01-assessment-creation-persistence/task-06-brief-intake-endpoint.md) | GENERATE-DOCUMENT | TODO | `POST /api/v1/assessments` |
| 7 | [Draft generation endpoint (+ AgentExecutionLog)](story-01-assessment-creation-persistence/task-07-draft-generation-endpoint.md) | GENERATE-DOCUMENT | TODO | `V12__add_agent_execution_logs.sql`, `POST /api/v1/assessments/{id}/draft` |
| 8 | [Draft regeneration endpoint](story-01-assessment-creation-persistence/task-08-draft-regeneration-endpoint.md) | GENERATE-DOCUMENT | TODO | `POST /api/v1/assessments/{id}/draft/regenerate` |
| 9 | [Draft edit endpoint](story-01-assessment-creation-persistence/task-09-draft-edit-endpoint.md) | GENERATE-DOCUMENT | TODO | `PATCH /api/v1/assessments/{id}/draft` |
| 10 | [Retrieval endpoints + dashboard wiring](story-01-assessment-creation-persistence/task-10-retrieval-and-dashboard-wiring.md) | GENERATE-DOCUMENT | TODO | `GET .../draft`, `GET .../draft/versions`, real `GET /api/v1/assessments` data |
| 11 | [End-to-end integration tests](story-01-assessment-creation-persistence/task-11-integration-tests.md) | GENERATE-DOCUMENT | TODO | `AssessmentCreationFlowIntegrationTest.java` |

---

## Done Criteria

- [ ] Brief is persisted before the Assessment Agent is invoked, in a separate step — an agent failure never loses the teacher's input.
- [ ] Persisted brief and draft are retrievable after a page refresh.
- [ ] Draft generation calls `agents/` exclusively through the `agentclient` module; no other module imports Spring AI.
- [ ] Draft generation persists the full structured result (title, context, instructions, objectives, deliverables, constraints) plus an `AgentExecutionLog` (model, cost estimate, status).
- [ ] Regeneration creates a new version linked to the assessment without deleting or overwriting the previous version; the previous version remains retrievable.
- [ ] Each regeneration produces its own distinct `AgentExecutionLog` row.
- [ ] Draft editing (teacher-made changes) persists without triggering a new agent call or a new `AgentExecutionLog`.
- [ ] Gemini API key is never exposed to the frontend — the key stays server-side within `agents/`, this repo only calls the internal agent endpoint.
- [ ] `./mvnw test` passes.
- [ ] TRACEABILITY.md updated with new terms from this story.

---

## Inconsistencies Found

*Record any contradictions or gaps detected during this story. Use RECORD-INCONSISTENCY workflow.*

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | `agents/`'s `AssessmentCommand` gained a `previousDraft` field (content, not just `previousDraftId`) on 2026-07-10, discovered during that planning's task-02 code review: `agents/` never persists data or calls back into `api/`, so `previousDraftId` alone cannot supply the regeneration prompt with the prior draft's content — `api/` must resolve and send it. This story's tasks were written before that change. | `agents/.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-01-contracts.md`, this story's task-05 and task-08 | RESOLVED | task-05 and task-08 updated 2026-07-10 to include `previousDraft` (rendering the loaded `AssessmentDraft` to text) in the outgoing command — see their Technical Design sections |
| 2 | `agents/`'s `AssessmentCommand` gained two more nullable fields on 2026-07-12 (`agents/.planning/active/002-groq-genai-provider`, story-01 task-03): `provider` (e.g. `"gemini"`, `"groq"`; omitted defaults to `"groq"`, no longer Gemini-only) and `model` (a literal, provider-specific model name — as of task-03 this is actually forwarded to the resolved provider's chat call as a per-call option, not merely accepted and ignored). Neither field is validated against a list of values the resolved provider actually supports; an unrecognized `provider` gets a 422 (`AssessmentAgentException(INVALID_COMMAND)`) from `agents/`, surfaced here as the existing `AgentClientException(AGENT_REJECTED)` mapping — no new `agentclient` error handling needed. This story's task-05 was written when the contract had 8 fields, all Gemini-routed by default. | `agents/.planning/active/002-groq-genai-provider/02-deepening/story-01-groq-provider-adapter/task-03-provider-selection.md`, this story's task-05 | OPEN | task-05 must add `provider`/`model` (both nullable, both optional to expose to `api/`'s own callers — they only need to exist on the mirror DTO so a `null`/absent value passes through unchanged) to the `agentclient`-side `AssessmentCommand` mirror DTO when it is implemented |

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
