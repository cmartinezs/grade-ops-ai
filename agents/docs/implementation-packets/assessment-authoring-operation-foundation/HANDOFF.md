<a id="top"></a>

# AGENTS-HANDOFF — Assessment Authoring Operation Foundation

**Status:** Complete. **Parent:** [README](README.md) · **Prev:** [TEST-PLAN](TEST-PLAN.md)

Format required by [09 — Session Handoff Protocol](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/09-session-handoff-protocol.md#-handoffmd-format).

```markdown
# Agents Handoff — Assessment Authoring Operation Foundation

## Commits
77868ef feat(agents): add resolved provider field to assessment generation response

## Branch
feat/assessment-authoring-operation-foundation-agents, pushed to origin at 77868ef

## Tasks completed
07A — Done. Verified `provider` was absent from `AgentExecutionLogPayload` (confirmed against
current source, matching the packet's own read-only investigation) and added it additively.

## Test results
./mvnw -f agents/pom.xml -Pdemo clean test → PASS, 32/32 (32 baseline, same count — the new
provider assertion was added inside the existing successful-generation test method per the
packet's own testing instructions, not as a new test method)

## Contract implemented
Field name chosen: `provider`
Location: AgentExecutionLogPayload.java (confirmed AssessmentExecutionResponse serializes it
without any wrapper change — AssessmentExecutionResponse wraps AgentExecutionLogPayload directly
as a record field with no Jackson customization, so the additive field flows through
automatically)

## Request schema
Unchanged — this task does not modify the request contract.

## Response schema
Full field list of `AgentExecutionLogPayload` after this change, in declared order:

```java
public record AgentExecutionLogPayload(
        UUID agentExecutionId,
        String agentName,
        String provider,          // NEW — resolved provider name, e.g. "gemini" / "groq"
        String model,
        String promptVersion,
        String inputHash,
        String outputHash,
        Integer estimatedInputTokens,
        Integer estimatedOutputTokens,
        Double costEstimate,
        String status,
        String errorCode,
        Instant startedAt,
        Instant finishedAt) {
}
```

`provider` is populated from `AssessmentGenerationPortSelector.SelectedProvider.name()` at every
point `model` is already populated in `AssessmentAgentOrchestrator`:
- Successful generation: `selected.name()` (non-null).
- `MALFORMED_OUTPUT` failure (output validation failed, or the port call itself threw after
  provider resolution already succeeded): the already-resolved `provider` parameter (non-null —
  resolution happened before this failure).
- `INVALID_COMMAND` failure (validation rejects the command before `selector.resolve(...)` is
  ever called): left unset — `null`, same as `model` already behaves in this path, since
  resolution never happened.

## Response fixture
Real JSON produced by serializing an actual `AssessmentExecutionResponse` instance through
Jackson (`ObjectMapper` + `JavaTimeModule`, `WRITE_DATES_AS_TIMESTAMPS` disabled — Spring Boot's
default auto-configuration), for a successful generation:

```json
{
  "result" : {
    "title" : "Iterative Array Traversal Exercise",
    "context" : "Practice iterative traversal and accumulation over arrays.",
    "instructions" : "Implement a program that reads an integer array and prints the sum and the maximum value using only iterative loops.",
    "objectives" : [ "Use for/while loops correctly", "Accumulate a running sum", "Track a running maximum" ],
    "deliverables" : [ "Source code file", "Sample input/output demonstration" ],
    "constraints" : [ "No external libraries", "No recursion" ]
  },
  "log" : {
    "agentExecutionId" : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "agentName" : "assessment",
    "provider" : "gemini",
    "model" : "gemini-2.0-flash",
    "promptVersion" : "// assessment-generation.v1",
    "inputHash" : "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
    "outputHash" : "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "estimatedInputTokens" : 120,
    "estimatedOutputTokens" : 80,
    "costEstimate" : 0.0084,
    "status" : "COMPLETED",
    "errorCode" : null,
    "startedAt" : "2026-07-28T18:40:00Z",
    "finishedAt" : "2026-07-28T18:40:02Z"
  }
}
```

`result`'s shape and every field of `log` other than `provider` are unchanged from the current
implementation — only `provider` (positioned between `agentName` and `model`) is new.

## Failure schema
Unchanged — AssessmentAgentException.Reason.{INVALID_COMMAND, MALFORMED_OUTPUT} already correct,
not touched by this task. Both failure paths now also populate `provider` on the log payload
whenever provider resolution had already succeeded before the failure (see "Response schema"
above) — this is a completion of the new field's own nullability contract, not a change to the
failure taxonomy itself.

## Tests
- Extended (not new): `AssessmentAgentOrchestratorTest.shouldReturnFullyPopulatedOutcomeWhenCommandIsValid`
  — added `assertThat(log.provider()).isEqualTo(PROVIDER)`.
- TDD discipline followed: the assertion was added first against the pre-change code, confirmed
  to fail with a compilation error (`cannot find symbol: method provider()`), then the field was
  implemented and the suite re-run to green.
- No new test file, no new test method — the existing successful-generation test structure
  allowed the one-field assertion to be added cleanly, per the packet's own testing scope.

## Integration instructions
The field name above is `provider` — this is what api/'s Task 07B must deserialize onto
`AgentAttempt.resolvedProvider`. It does not differ from the packet's suggested default, so no
prominent flag is needed.

## Known limitations
None. This was the "field is missing, add it" branch (not the "already present, verify only"
branch) — confirmed by direct inspection of `AgentExecutionLogPayload.java` before making any
change, matching TASKS.md's own recorded verification.
```

---

← [TEST-PLAN](TEST-PLAN.md) | [↑ inicio](#top) | [↑ README](README.md)
