# GroqAssessmentGenerationAdapter

**Source:** task-01 | **Area:** AG | **Date:** 2026-07-12

## What it does

A `GroqAssessmentGenerationAdapter` class implements `AssessmentGenerationPort` and maps a Groq chat-completion response into `AssessmentResult`, mirroring `GeminiAssessmentGenerationAdapter`'s structure, in a new `infrastructure.adapter.out.groq` package. It is not yet wired into the Spring context (no `@Bean` registration) — that lands in task-03.

## How to use it

- Groq is consumed through Spring AI's OpenAI-compatible `ChatClient`, since Groq exposes an OpenAI-compatible chat completions endpoint and Spring AI has no dedicated Groq starter. The `spring-ai-starter-model-openai` dependency was added to the `beta`/`demo` Maven profiles for this.
- The adapter's constructor takes a `ChatClient` — construction/configuration of that client (API key, base URL, model) is out of this task's scope; see task-02 for how those values are sourced.
- `generate(String renderedPrompt)` calls `chatClient.prompt(renderedPrompt).call().responseEntity(AssessmentResult.class)` in a single call, extracting both the mapped result and token-usage metadata from one round trip.
- Token usage is only reported when Spring AI's response metadata actually carries it — a provider that omits usage data reports `null` estimated tokens, not `0` (checked via `usage instanceof EmptyUsage`, not a plain `null` check).

## Example

```java
ChatClient groqChatClient = ...; // built from Groq's API key/base URL/model, see task-02
AssessmentGenerationPort groqPort = new GroqAssessmentGenerationAdapter(groqChatClient);

AssessmentGenerationResponse response = groqPort.generate(renderedPrompt);
AssessmentResult result = response.result();
```
