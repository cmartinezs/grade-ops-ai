<a id="top"></a>

# TASKS — Agents: Assessment Authoring Operation Foundation

**Status:** Ready. **Parent:** [README](README.md) · **Prev:** [CLAUDE-IMPLEMENTATION-PROMPT](CLAUDE-IMPLEMENTATION-PROMPT.md) · **Next:** [LOCAL-CONTRACTS →](LOCAL-CONTRACTS.md)

One task. Corresponds to subtask 07A of the plan's Task 07 — see [root packet § Task 07 split](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/04-task-distribution.md#task-07-split).

---

### Task 07A — Add resolved provider to the generation response

- **Objective:** `AgentExecutionLogPayload` (and its `api/`-side mirror, `AssessmentAgentResponse.Log` — not this packet's file to edit, but API's Task 07B depends on knowing the field name you choose) does not currently carry which provider actually served a generation request. Add it.
- **Verified current state (read-only investigation performed while drafting this packet, not an assumption):**
  - `agents/src/main/java/cl/gradeops/ai/agents/assessment/application/result/AgentExecutionLogPayload.java:42-55` — 13 fields: `agentExecutionId`, `agentName`, `model`, `promptVersion`, `inputHash`, `outputHash`, `estimatedInputTokens`, `estimatedOutputTokens`, `costEstimate`, `status`, `errorCode`, `startedAt`, `finishedAt`. **No `provider` field.**
  - `agents/src/main/java/cl/gradeops/ai/agents/assessment/application/orchestrator/AssessmentAgentOrchestrator.java:76` — `selector.resolve(command.provider())` returns a `SelectedProvider` record (`name` + `port`, defined in `AssessmentGenerationPortSelector.java:54`). `selected.name()` (the resolved provider string, e.g. `"gemini"`/`"groq"`) is used locally at lines 86, 89, 94, 163 (cost estimation, logging) but is **never assigned to any field** of `AgentExecutionLogPayload` before it is returned.
  - Wrapped unmodified by `AssessmentExecutionResponse` (`.../infrastructure/adapter/in/web/response/AssessmentExecutionResponse.java:12`), the actual JSON body served at `POST /internal/agents/assessment`.
- **Files:** `agents/src/main/java/cl/gradeops/ai/agents/assessment/application/result/AgentExecutionLogPayload.java`; `AssessmentAgentOrchestrator.java` (populate the new field at the same point `model` is already populated, using `selected.name()` which is already in scope there).
- **Dependencies:** none — this task needs zero API migrations or domain code to exist.
- **Steps:**
  1. Add a `provider` (or your team's preferred exact name — `resolvedProvider` is the name `api/`'s domain model uses on `AgentAttempt`, but this response DTO's own field may simply be `provider` for brevity; pick one and state it clearly in `HANDOFF.md`, since Task 07B needs the exact string) field to `AgentExecutionLogPayload`, same nullability discipline as `model` (non-null on a completed response, null only if resolution never happened before failure).
  2. Populate it in `AssessmentAgentOrchestrator` at the same point `model` is already set, from the already-resolved `selected.name()` — no new resolution logic needed.
  3. Confirm `AssessmentExecutionResponse` serializes the new field without any wrapper change (it should, if it's a straightforward field addition to the wrapped record).
- **Tests to write first:** Extend the orchestrator's existing successful-generation test with one new assertion: the response payload's provider field equals `selected.name()`. Write the assertion first, watch it fail against current code (field doesn't exist / is always null), then implement.
- **Verification command:** `./mvnw -f agents/pom.xml -Pdemo test -Dtest=AssessmentAgentOrchestratorTest` (or the actual existing test class name — verify it at implementation time), then full suite `./mvnw -f agents/pom.xml -Pdemo clean test`
- **Acceptance criteria:** Criterion #13 from [10 — Acceptance Criteria](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md) ("Real provider, model, correlation id are persisted") — this task supplies the provider value API's Task 07B then persists; the criterion is only fully closed once both land, but this task's own test proves its half.
- **Migration considerations:** None — this is a Java DTO field, not a database column.
- **Risks:** Low. Additive field, no rename, no behavior change to any existing field.
- **Commit boundary:** `feat(agents): add resolved provider field to assessment generation response`

---

**Not owned by this packet:** every other task in the plan. See [04 — Task Distribution](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/04-task-distribution.md) for the full map.

---

← [CLAUDE-IMPLEMENTATION-PROMPT](CLAUDE-IMPLEMENTATION-PROMPT.md) | [↑ inicio](#top) | [Siguiente: LOCAL-CONTRACTS →](LOCAL-CONTRACTS.md)
