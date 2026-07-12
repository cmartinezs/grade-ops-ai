# Unit tests: Groq adapter and provider selection

**Source:** task-04-unit-tests | **Area:** AG | **Date:** 2026-07-12

## What it does

`GroqAssessmentGenerationAdapter` and `AssessmentGenerationPortSelector` (both introduced in task-01/task-03) get unit test coverage matching `001`'s established conventions, and `AssessmentAgentOrchestratorTest`/`AssessmentCommandTest` are updated for the selector-based constructor and the `provider`/`model` fields. No production code changed — this task closes out the coverage gap left after task-03 landed the Strategy-pattern selection mechanism.

## How to use it

- `GroqAssessmentGenerationAdapterTest` mirrors `GeminiAssessmentGenerationAdapterTest`'s structure: usage-metadata-present mapping, per-call model forwarding (captured via `ArgumentCaptor<ChatOptions.Builder>`, cast to `OpenAiChatOptions`), and `EmptyUsage` (null token fields when usage metadata is unavailable).
- `AssessmentGenerationPortSelectorTest` uses lambda-based `AssessmentGenerationPort` fakes (no Mockito — nothing to mock at this layer) to pin down actual `resolve`/`supports` behavior: a named provider resolves directly; a literal `null` falls back to the configured default; **a blank string (`""`) is rejected as an unrecognized provider, not treated as equivalent to `null`** — this is deliberately tested and documented, since it differs from how the mechanism might be described informally. See `TRACEABILITY.md`'s R-02 for the open question of whether blank-string normalization should happen at a different layer (e.g. the API/DTO boundary) in a future planning.
- `AssessmentAgentOrchestratorTest` and `AssessmentCommandTest` were already updated for the selector and `provider`/`model` fields as part of task-03's own review fixes; task-04 verified that coverage was already complete rather than duplicating it.

## Example

```bash
./mvnw -Pbeta test
# 32 tests, 0 failures — includes GroqAssessmentGenerationAdapterTest (3 cases)
# and AssessmentGenerationPortSelectorTest (7 cases)
```
