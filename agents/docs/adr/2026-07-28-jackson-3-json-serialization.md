# ADR: JSON serialization — Spring Boot 4 / Jackson 3

**Date:** 2026-07-28
**Status:** Accepted
**Planning:** assessment-authoring-operation-foundation / Session B / Task 07A (fixture-serialization correction)

## Context

While producing a response fixture for Task 07A (`provider` field added to `AgentExecutionLogPayload`), the fixture-generating test was first written against a manually constructed `com.fasterxml.jackson.databind.ObjectMapper` with a manually registered `JavaTimeModule`, described in the handoff as "equivalent to Spring Boot's default configuration." That description was never verified against the actual auto-configured bean.

GradeOps AI is on Spring Boot 4.1 / Jackson 3. Spring Boot 4's Jackson auto-configuration produces a `tools.jackson.databind.json.JsonMapper` bean (package `tools.jackson.*`, not `com.fasterxml.jackson.databind.*`), and Jackson 3's databind module has built-in `java.time` support — `JavaTimeModule` (a Jackson 2 class, `com.fasterxml.jackson.datatype.jsr310.JavaTimeModule`) does not exist in Jackson 3 and never needs manual registration. A hand-built Jackson 2 `ObjectMapper` is therefore not equivalent to what the application actually serializes with in production — it is a different major version's API, with different default behavior, that happens to produce superficially similar output for simple cases.

`api/`'s own precedent (`api/src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/JacksonConfig.java`) already establishes the correct pattern for this codebase: customize serialization via a `JsonMapperBuilderCustomizer` bean, consumed by Spring Boot's auto-configuration, never a manually constructed mapper.

## Decision

- Use Jackson 3 through `tools.jackson.databind.json.JsonMapper`.
- Prefer the Spring Boot auto-configured `JsonMapper` bean.
- Do not use Jackson 2 `com.fasterxml.jackson.databind.ObjectMapper`.
- Do not manually reproduce Spring Boot's mapper configuration in tests.
- Use `JsonMapperBuilderCustomizer` for application-wide customization.
- Contract fixtures must be produced and verified using the actual Spring-managed mapper.

Tests that need a real, application-equivalent `JsonMapper` obtain it from the Spring `ApplicationContext` — the lightest slice compatible with this codebase's existing test structure is `@JsonTest` (`org.springframework.boot.test.autoconfigure.json.JsonTest`, already transitively available via `spring-boot-starter-test`), with the mapper `@Autowired` directly:

```java
@JsonTest
class SomeContractTest {
    @Autowired
    private JsonMapper jsonMapper;
}
```

This is enforced by a regression test (`cl.gradeops.ai.agents.shared.architecture.JacksonUsageGuardTest`) that scans `agents/`'s own `src/main` and `src/test` sources — never dependency jars — for the Jackson 2 `ObjectMapper` FQN, manual `new ObjectMapper(...)`/`new JavaTimeModule(...)` construction, and the Jackson 2 `JavaTimeModule` FQN.

## Consequences

- `agents/`'s response-shape contract test (`AssessmentExecutionResponseContractTest`, covering `AssessmentExecutionResponse`/`AgentExecutionLogPayload`) serializes through the real auto-configured `JsonMapper`, so its assertions — including the exact `Instant` string format — reflect production behavior, not a simulation of it.
- Any future test needing JSON (de)serialization in this module follows the same `@JsonTest` + `@Autowired JsonMapper` pattern rather than instantiating a mapper by hand.
- `agents/`'s transitive dependencies (the Google GenAI client, the OpenAI-compatible client used for the Groq adapter, the Logstash encoder) carry their own internal Jackson 2 usage for their own HTTP/logging concerns. This ADR does not require removing those transitive dependencies — they never participate in `agents/`'s own request/response (de)serialization and are outside this module's own maintained code. See `AGENTS-HANDOFF.md`'s "Dependency inspection" section for the verification that confirmed this.

## Alternatives Considered

Keeping the hand-built Jackson 2 `ObjectMapper` + manually registered `JavaTimeModule` (as first written) was rejected: it does not exercise Spring Boot's actual auto-configured serialization behavior, silently diverges from Jackson 3's native `java.time` handling, and misrepresents a fixture as "real" when it was produced by a different library major version than the one the running application uses.
