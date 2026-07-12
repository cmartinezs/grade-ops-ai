# Re-Code Review: story-01-groq-provider-adapter / task-04-unit-tests

## Scope

- Task: `agents/.planning/active/002-groq-genai-provider/02-deepening/story-01-groq-provider-adapter/task-04-unit-tests.md`
- Branch reviewed: `gradeops-agents/story-01-groq-provider-adapter--task-04-unit-tests`
- Compared against: `origin/gradeops-agents/story-01-groq-provider-adapter`
- Review date: 2026-07-12
- Re-review date: 2026-07-12

## Findings

No open findings.

## Resolved Findings

### Resolved - Selector test no longer contradicts task-04's own design text

- Files:
  - `agents/.planning/active/002-groq-genai-provider/02-deepening/story-01-groq-provider-adapter/task-04-unit-tests.md:20`
  - `agents/.planning/active/002-groq-genai-provider/02-deepening/story-01-groq-provider-adapter/task-04-unit-tests.md:21`
  - `agents/src/test/java/cl/gradeops/ai/agents/assessment/application/port/out/AssessmentGenerationPortSelectorTest.java:48`
  - `agents/.planning/active/002-groq-genai-provider/TRACEABILITY.md:51`
  - `agents/.planning/active/002-groq-genai-provider/RETROSPECTIVE-RAW.md:28`

The previous review found that task-04 still said selector coverage should include fallback to the configured default when `provider` is `null/blank`, while the new selector test asserted that `""` is unsupported and rejected.

That mismatch is resolved. The task design now states the real behavior: only a literal `null` falls back to the configured default; a blank string is explicitly tested as an unrecognized provider. The task also explains why this review correction did not change `AssessmentGenerationPortSelector` under a test-only task. The product concern from the review was not dropped: `TRACEABILITY.md` now records R-02 for the open decision about where blank-string normalization should live, and `RETROSPECTIVE-RAW.md` captures the correction path.

## Non-Blocking Notes

- `AssessmentAgentOrchestratorTest` already contains the selector constructor update and unrecognized-provider `INVALID_COMMAND` coverage in the story base, so the absence of a task-04 diff there is not itself a blocker.
- The new Groq adapter test mirrors the Gemini test structure and covers usage metadata, per-call model forwarding, and unavailable usage metadata.
- R-02 remains open by design as future planning, not a task-04 blocker: no current client sends a blank `provider`, and `api/`'s `agentclient` sends either a real value or omits the field.

## Checks Performed

- `git status --short --branch`
- `git diff --name-status origin/gradeops-agents/story-01-groq-provider-adapter...HEAD`
- `git diff --check origin/gradeops-agents/story-01-groq-provider-adapter...HEAD` - pass
- Read task file, story file, traceability, retrospective, selector, orchestrator, command, Groq adapter, Gemini/Groq adapter tests, command test, and selector test.
- Re-read the P2 correction in `task-04-unit-tests.md`, `TRACEABILITY.md`, and `RETROSPECTIVE-RAW.md`.
- `./mvnw -Pbeta test` from `agents/` - pass, 32 tests, 0 failures, 0 errors.

## Conclusion

Approved from code-review perspective. The prior P2 is resolved, the remaining blank-provider concern is tracked as an explicit future residual, and the beta test suite is green.
