# Code Review: story-01-groq-provider-adapter / task-01-groq-adapter.md

Re-review date: 2026-07-12

Scope reviewed:

- `.planning/active/002-groq-genai-provider/02-deepening/story-01-groq-provider-adapter/task-01-groq-adapter.md`
- `.planning/active/002-groq-genai-provider/02-deepening/story-01-groq-provider-adapter/task-01-groq-structured-output-evidence.md`
- `.planning/active/002-groq-genai-provider/MANUAL-PROVIDER-TESTING.md`
- `.planning/active/002-groq-genai-provider/02-deepening/story-01-groq-provider-adapter.md`
- `.planning/active/002-groq-genai-provider/RETROSPECTIVE-RAW.md`
- `pom.xml`
- `src/main/java/cl/gradeops/ai/agents/assessment/infrastructure/adapter/out/groq/GroqAssessmentGenerationAdapter.java`
- `src/main/java/cl/gradeops/ai/agents/assessment/infrastructure/adapter/out/gemini/GeminiAssessmentGenerationAdapter.java`
- `src/main/java/cl/gradeops/ai/agents/assessment/infrastructure/config/AssessmentConfig.java`
- `src/test/resources/application-test.yml`
- `src/test/java/cl/gradeops/ai/agents/GradeOpsAgentsApplicationTest.java`
- `src/test/java/cl/gradeops/ai/agents/assessment/infrastructure/adapter/out/gemini/GeminiAssessmentGenerationAdapterTest.java`
- `src/test/java/cl/gradeops/ai/agents/assessment/infrastructure/adapter/out/groq/GroqChatClientManualVerification.java`

## Findings

No blocking findings remain after the latest correction pass.

## Resolved Findings

### Resolved: OpenAI starter no longer breaks the hermetic Spring context test

The previous review found that adding `spring-ai-starter-model-openai` made `GradeOpsAgentsApplicationTest` fail under `-Pbeta` because OpenAI autoconfiguration required credentials in the `test` profile. `application-test.yml` now excludes the six OpenAI autoconfiguration classes alongside the existing Google GenAI exclusions (`src/test/resources/application-test.yml:9-23`). The exact regression command now passes:

```bash
./mvnw -Pbeta test -Dtest=GradeOpsAgentsApplicationTest,GeminiAssessmentGenerationAdapterTest
```

Result: `BUILD SUCCESS`, 3 tests, 0 failures, 0 errors.

### Resolved: Real `ChatClient.responseEntity(...)` evidence now exists

The previous review found that the evidence only used raw `curl`, which did not prove the adapter's Java/Spring AI mapping path. The task now adds `GroqChatClientManualVerification` as an on-demand diagnostic that builds a real Spring AI `ChatClient` through `OpenAiChatAutoConfiguration`, points it at Groq's OpenAI-compatible base URL, and calls `.prompt(...).call().responseEntity(AssessmentResult.class)` (`GroqChatClientManualVerification.java:49-79`).

The evidence file now explicitly records both outcomes:

- `llama-3.1-8b-instant` returns HTTP 200 but echoes the JSON Schema, mapping to an empty `AssessmentResult` (`task-01-groq-structured-output-evidence.md:105-119`).
- `llama-3.3-70b-versatile` succeeds through the same real Java path and maps to a populated `AssessmentResult` (`task-01-groq-structured-output-evidence.md:121-136`).

The task and manual runbook now document the model-capability distinction and use `llama-3.3-70b-versatile` as the default/example model for this adapter path (`task-01-groq-adapter.md:41-43`, `MANUAL-PROVIDER-TESTING.md:38-49`).

### Resolved: The implementation and evidence files are tracked

The earlier clean-checkout blocker remains resolved. The adapter, evidence, manual runbook, and on-demand diagnostic are present in tracked branch content; current `git status` only shows this `.code-review` artifact as untracked.

## Non-blocking Notes

- `GroqChatClientManualVerification` is intentionally not part of the default suite by filename, and an explicit no-key run skips cleanly rather than failing.
- The remaining `llama-3.1-8b-instant` references are historical or negative evidence, not defaults/examples for the Java mapping path.
- `GroqAssessmentGenerationAdapterTest` remains deferred to task-04 by the story plan, so it is not a task-01 blocker.

## Verification

Commands executed:

```bash
./mvnw -Pbeta compile
./mvnw -Pbeta test -Dtest=GradeOpsAgentsApplicationTest,GeminiAssessmentGenerationAdapterTest
./mvnw -Pbeta test
./mvnw -Pbeta test -Dtest=GroqChatClientManualVerification
```

Results:

- `./mvnw -Pbeta compile` passed.
- `./mvnw -Pbeta test -Dtest=GradeOpsAgentsApplicationTest,GeminiAssessmentGenerationAdapterTest` passed: 3 tests.
- `./mvnw -Pbeta test` passed: 18 tests, 0 failures, 0 errors.
- `./mvnw -Pbeta test -Dtest=GroqChatClientManualVerification` passed with 1 skipped test because `GROQ_API_KEY` is not set in this local environment.
