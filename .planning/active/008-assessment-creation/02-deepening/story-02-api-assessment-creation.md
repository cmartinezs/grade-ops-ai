# 🔍 DEEPENING: Story 02 — api-assessment-creation

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Implement in `api/` the full backend flow for assessment creation: persist the teacher's brief before any agent call (US-010), integrate with the Assessment Agent via the `agentclient` module to generate a structured draft and persist it together with its `AgentExecutionLog` (US-011), and support draft regeneration as a new, non-destructive version linked to the previous one (US-012).

**Source stories:** `docs/02-product/user-stories/epic-02-assessment-creation/01-assessment-brief-intake.md`, `02-assessment-draft-generation.md`, `03-assessment-draft-regeneration.md`.

---

## Context

- **Depends on Story 01** for the `AssessmentCommand` / `AssessmentResult` contract and the internal agent endpoint.
- Base package: `cl.gradeops.ai.api`.
- Only the `agentclient` module may import Spring AI / call `agents/` — no other module in `api/` does.
- Brief persistence must happen in its own transaction/request, strictly before the agent-invoking request — a failed agent call must never lose the teacher's input (epic DoD).
- Draft versioning: each regeneration creates a new version row linked to the same assessment; the previous version is never overwritten or deleted and must remain retrievable.
- Every agent execution (initial generation and each regeneration) produces its own `AgentExecutionLog` row (model, cost estimate, status, timestamps) — logs are never shared across executions.
- Check `api/src/main/resources/db/migration/` for the last Flyway version number before naming new migrations.

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Draft versioning model is under-designed and a regeneration silently overwrites a previous version | H | L | DoD explicitly requires previous versions to remain retrievable; cover with an integration test (task 8) before marking this story done |

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | Flyway migration + entity + repository for `AssessmentBrief` (learning goal, topic, level/difficulty, duration, language) | GENERATE-DOCUMENT | TODO | `V<N>__add_assessment_brief.sql`, `AssessmentBriefEntity.java`, `AssessmentBriefRepository.java` |
| 2 | Flyway migration + entity + repository for `AssessmentDraft` with versioning (FK to brief, FK to previous version, version number) | GENERATE-DOCUMENT | TODO | `V<N+1>__add_assessment_draft.sql`, `AssessmentDraftEntity.java`, `AssessmentDraftRepository.java` |
| 3 | Brief intake endpoint (`POST`) — persists the brief before any agent call; returns the persisted brief id | GENERATE-DOCUMENT | TODO | `AssessmentController` endpoint + `AssessmentService.createBrief()` |
| 4 | Draft generation endpoint — builds `AssessmentCommand` from the persisted brief, calls `agents/` via `agentclient`, persists the returned draft (version 1) and its `AgentExecutionLog` | GENERATE-DOCUMENT | TODO | `AssessmentService.generateDraft()`, `agentclient` integration |
| 5 | Draft regeneration endpoint — builds `AssessmentCommand` from brief + adjustment notes + previous version reference, persists the new version and its own `AgentExecutionLog`, keeps the previous version intact | GENERATE-DOCUMENT | TODO | `AssessmentService.regenerateDraft()` |
| 6 | Draft edit endpoint (`PATCH`/`PUT`) — teacher edits the current version's fields directly, no new agent call, no new `AgentExecutionLog` | GENERATE-DOCUMENT | TODO | `AssessmentController` endpoint + `AssessmentService.updateDraft()` |
| 7 | Draft/version retrieval endpoint(s) — current version + version history, retrievable after page refresh | GENERATE-DOCUMENT | TODO | `AssessmentController` GET endpoints |
| 8 | Unit + integration tests: persist-before-agent-call ordering, versioning (no overwrite), `AgentExecutionLog` fields per execution, edit does not create a new version/log | GENERATE-DOCUMENT | TODO | Test classes |

---

## Done Criteria

- [ ] Brief is persisted before the Assessment Agent is invoked, in a separate step — an agent failure never loses the teacher's input.
- [ ] Persisted brief and draft are retrievable after a page refresh.
- [ ] Draft generation calls `agents/` exclusively through the `agentclient` module; no other `api/` module imports Spring AI.
- [ ] Draft generation persists the full structured result (title, context, instructions, objectives, deliverables, constraints) plus an `AgentExecutionLog` (model, cost estimate, status).
- [ ] Regeneration creates a new version linked to the assessment without deleting or overwriting the previous version; the previous version remains retrievable.
- [ ] Each regeneration produces its own distinct `AgentExecutionLog` row.
- [ ] Draft editing (teacher-made changes) persists without triggering a new agent call or a new `AgentExecutionLog`.
- [ ] Gemini API key is never exposed to the frontend — the key stays server-side within `agents/`, `api/` only calls the internal agent endpoint.
- [ ] `./mvnw test` passes.
- [ ] TRACEABILITY.md updated.

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
