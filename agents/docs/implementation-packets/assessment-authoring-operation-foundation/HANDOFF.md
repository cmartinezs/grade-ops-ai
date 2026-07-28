<a id="top"></a>

# AGENTS-HANDOFF — Assessment Authoring Operation Foundation

**Status:** Template — fill in and commit as `docs(agents): record assessment authoring foundation handoff` once [TASKS.md](TASKS.md) is complete. **Parent:** [README](README.md) · **Prev:** [TEST-PLAN](TEST-PLAN.md)

Format required by [09 — Session Handoff Protocol](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/09-session-handoff-protocol.md#-handoffmd-format).

```markdown
# Agents Handoff — Assessment Authoring Operation Foundation

## Commits
<hash> <message>

## Branch
feat/assessment-authoring-operation-foundation-agents, pushed to origin at <commit>

## Tasks completed
07A — <status>

## Test results
./mvnw -f agents/pom.xml -Pdemo clean test → <PASS/FAIL, test count, e.g. 33/33 (32 baseline + 1 new)>

## Contract implemented
Field name chosen: <exact name, e.g. `provider`>
Location: AgentExecutionLogPayload.java (and confirm AssessmentExecutionResponse serializes it)

## Request schema
Unchanged — this task does not modify the request contract.

## Response schema
<the full field list of AgentExecutionLogPayload after this change, so API's session doesn't
 have to re-derive it — copy it verbatim from the implemented record>

## Response fixture
<a real JSON example of a successful response including the new field>

## Failure schema
Unchanged — AssessmentAgentException.Reason.{INVALID_COMMAND, MALFORMED_OUTPUT} already correct,
not touched by this task.

## Tests
<list the specific test(s) added/extended>

## Integration instructions
The field name above is what api/'s Task 07B must deserialize onto AgentAttempt.resolvedProvider.
If it differs from `provider`, flag it prominently here.

## Known limitations
<"None." if none — e.g. note here if you took the read-only-verification fallback path
 for Task 07B's dependency instead of a direct handoff, per CLAUDE-IMPLEMENTATION-PROMPT.md>
```

---

← [TEST-PLAN](TEST-PLAN.md) | [↑ inicio](#top) | [↑ README](README.md)
