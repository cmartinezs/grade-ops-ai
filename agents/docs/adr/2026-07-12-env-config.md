# ADR: Project-specific env var names + `.env` loading for both providers

**Date:** 2026-07-12
**Status:** Accepted
**Planning:** 002-groq-genai-provider / story-01 / task-02

## Context

Both providers' Spring AI autoconfiguration properties (`spring.ai.google.genai.*` for Gemini, `spring.ai.openai.*` for Groq) needed to resolve from project-specific (`GRADEOPS_*`-prefixed) environment variable names instead of the defaults Spring AI's own docs suggest, with local/test runs loading these from a `.env` file. Additionally, adding `spring-ai-starter-model-openai` (for Groq, task-01) put both providers' Spring AI starters on the classpath simultaneously — verified by actually starting the app, this broke real `-Pbeta` boot with `NoUniqueBeanDefinitionException`, since Spring AI's `ChatClientAutoConfiguration.chatClientBuilder()` factory method requires exactly one `ChatModel` bean and found two (`googleGenAiChatModel`, `openAiChatModel`).

## Decision

Rejected the library option (e.g. `me.paulschwarz:spring-dotenv`) because its exact Maven coordinates/behavior couldn't be verified against this project's actual Spring Boot 4.1/Spring AI 2.0 combination without a live dependency resolution attempt risking a broken build over a genuinely small amount of logic; a custom `EnvironmentPostProcessor` has zero external dependency risk and full control over precedence. Excluded `org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration` in `application-beta.yml`/`application-demo.yml`, and changed `AssessmentConfig.geminiAssessmentGenerationAdapter` to accept `@Qualifier("googleGenAiChatModel") ChatModel chatModel` instead of the ambiguous `ChatClient.Builder`, building the client directly via `ChatClient.builder(chatModel).build()` — a plain static factory call with no Spring bean-resolution ambiguity.

## Consequences

`.env` loading must not interfere with `test` Spring profile's existing AI-autoconfiguration exclusion (`application-test.yml`) — the two are orthogonal (one controls *which* autoconfiguration classes load, the other controls *where config values come from*), verified together. `openAiChatModel` (Groq's) stays unused/unwired until task-03, which registers it as a named adapter bean through the provider-selection resolver.

## Alternatives Considered

Rejected the library option (e.g. `me.paulschwarz:spring-dotenv`) because its exact Maven coordinates/behavior couldn't be verified against this project's actual Spring Boot 4.1/Spring AI 2.0 combination without a live dependency resolution attempt risking a broken build over a genuinely small amount of logic; a custom `EnvironmentPostProcessor` has zero external dependency risk and full control over precedence (must add `.env` values as *fallback* defaults, never overriding a real shell-exported env var or `-D` system property — local dev convenience, not an override mechanism).

## Affected Phases

| Phase Code | Impact |
|-----------|--------|
| AG | `application-beta.yml`/`application-demo.yml`, `AssessmentConfig.java`, new `DotenvEnvironmentPostProcessor` |

## Related

- **Planning:** 002-groq-genai-provider
- **ADR (if technical follow-up):** none
- **Supersedes:** none
- **Superseded by:** none
