# 🔗 Traceability: 002-groq-genai-provider

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

<!-- AREAS-REF: populated by plan-init from the project's configured areas — keep in sync with GUIDE.md -->
| Code | Area |
|------|------|
| AG | Agent Runtime (`src/`) |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

<!-- MATRIX-HEADER: plan-init adds one column per area between "Term / Concept" and "Notes" -->
| Term / Concept | AG | W | Notes |
|---------------|----|---|-------|
| `GroqAssessmentGenerationAdapter` | ✅ | N/A | Implemented in story-01 task-01 (`infrastructure.adapter.out.groq`) — `AssessmentGenerationPort` implementation for Groq, structurally identical to `001`'s `GeminiAssessmentGenerationAdapter`. Real-call evidence confirms Groq's structured-output mapping needs no adjustment (`task-01-groq-structured-output-evidence.md`). Wired into Spring context as the `"groq"` named bean in task-03. Unit test coverage added in task-04 (`GroqAssessmentGenerationAdapterTest`, mirroring `GeminiAssessmentGenerationAdapterTest`'s three cases: usage present, model-forwarding, `EmptyUsage`). |
| `spring-ai-starter-model-openai` | ✅ | N/A | Added to `agents/pom.xml`'s `beta`/`demo` profiles (story-01 task-01) — Groq is consumed via its OpenAI-compatible endpoint; Spring AI has no dedicated Groq starter. |
| `AssessmentGenerationPortSelector` | ✅ | N/A | Implemented in story-01 task-03 (`application.port.out`) — Strategy-pattern resolver over `Map<String, AssessmentGenerationPort>` (Spring-populated by bean name from `AssessmentConfig`'s `@Bean(name = "gemini")`/`@Bean(name = "groq")`), so future providers are additive: `resolve(String)` returns a `SelectedProvider(name, port)` record; `supports(String)` lets `AssessmentAgentOrchestrator.validate` reject an unrecognized provider before generation is attempted. Unit test coverage added in task-04 (`AssessmentGenerationPortSelectorTest`) pins down actual `resolve`/`supports` behavior precisely: only a literal `null` argument falls back to the configured default — a blank string (`""`) is looked up as a literal, unregistered provider name and rejected like any other unknown value, not treated as equivalent to omitting the field (this planning's own task-04 design note described the fallback loosely as "null/blank"; the implemented, reviewed behavior only special-cases `null`, and this test file makes that explicit rather than leaving it ambiguous). |
| `AssessmentCommand.provider` / `AssessmentCommand.model` | ✅ | N/A | Implemented in story-01 task-03 — nullable per-request provider/model override, default `groq`. `provider` drives selection via `AssessmentGenerationPortSelector`; an unrecognized value is rejected with `INVALID_COMMAND`. `model`, once flagged by code review (P1) as accepted but silently discarded, is now forwarded as a per-call `ChatOptions` override to the resolved provider's `ChatClient` (`GoogleGenAiChatOptions.builder().model(...)` / `OpenAiChatOptions.builder().model(...)`) — confirmed both at the unit level (`GeminiAssessmentGenerationAdapterTest.shouldForwardRequestedModelAsPerCallChatOptionsWhenModelIsProvided`) and via a real Groq call whose `log.model` changed from the configured default (`llama-3.3-70b-versatile`) to the explicitly requested override (`llama-3.1-8b-instant`). Neither field is validated against a list of values the resolved provider actually supports — see R-01. Cross-repo impact: `api/`'s `agentclient` child planning (`api/.planning/active/003-assessment-creation`, story-01 Inconsistencies Found #2 and `TRACEABILITY.md`) durably notified 2026-07-12, same pattern as `001`'s `previousDraft` addition. |
| `GRADEOPS_GEMINI_API_KEY` / `GRADEOPS_GEMINI_MODEL` / `GRADEOPS_GROQ_API_KEY` / `GRADEOPS_GROQ_MODEL` / `GRADEOPS_GROQ_BASE_URL` | ✅ | N/A | Implemented in story-01 task-02 — back Spring AI's own `spring.ai.google.genai.*`/`spring.ai.openai.*` property paths in `application-beta.yml`/`application-demo.yml`; replaced `GOOGLE_AI_API_KEY`/`AI_MODEL_NAME`. Property *paths* stay Spring AI's own per explicit human direction — only variable names are project-specific. Documented in `.env.example`. |
| `DotenvEnvironmentPostProcessor` | ✅ | N/A | Implemented in story-01 task-02 (`shared.infrastructure.config`, registered via `META-INF/spring.factories`) — loads `.env` as the lowest-precedence property source for local dev; never overrides a real env var or `-D` property. Chosen over a third-party dotenv library to avoid unverified external dependency risk. |
| `ChatClientAutoConfiguration` exclusion + `AssessmentConfig`'s `@Qualifier("googleGenAiChatModel")` / `@Qualifier("openAiChatModel")` | ✅ | N/A | Fixed in story-01 task-02, extended in task-03 — with both Gemini's and Groq's Spring AI starters on the classpath (task-01), Spring AI's own `ChatClientAutoConfiguration` finds two `ChatModel` beans and refuses to build its generic `ChatClient.Builder`. Excluded that autoconfiguration; `AssessmentConfig` builds each provider's `ChatClient` directly from its own uniquely-named `ChatModel` bean (`googleGenAiChatModel`, `openAiChatModel`). This had silently broken real `-Pbeta` app boot since task-01 merged (the test suite's `test` profile never exercised it) — see `RETROSPECTIVE-RAW.md` 2026-07-12. |
| `app.agents.llm.default-provider` / `app.agents.llm.cost-per-1k-tokens.<provider>` | ✅ | N/A | Implemented in story-01 task-03 (`application.yml`) — `default-provider: groq`; `cost-per-1k-tokens.gemini: 0.000075` / `.groq: 0.0`. Neither is a secret, so both live directly in `application.yml` rather than `.env`/`GRADEOPS_*`. Replaces `AssessmentAgentOrchestrator`'s old single Gemini-only `COST_PER_1K_TOKENS` constant — `costEstimate` now looks up the resolved provider's own rate, confirmed empirically (Groq run: `costEstimate: 0.0`) and at the unit level (`AssessmentAgentOrchestratorTest.shouldUseTheResolvedProviderOwnRateForCostEstimateNotAnotherProvidersRate`) since a live Gemini run could not complete due to the same quota exhaustion documented in `001`. |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D-01 | `AssessmentConfig`'s existing `app.agents.gemini.enabled` property continues to gate the whole `@Configuration` class (both providers, the orchestrator, the selector) rather than adding a second `app.agents.groq.enabled` flag | The `test` profile needs the entire assessment feature off (no real `ChatModel` bean for either provider there); there is no scenario yet requiring one provider enabled while the other is disabled — a second flag would be speculative | `AssessmentConfig.java` | 2026-07-12 |
| D-02 | `AssessmentAgentOrchestrator`'s per-provider cost rates are injected as a plain `Map<String, Double>` built in `AssessmentConfig` from two `@Value`-annotated fields, not a `@ConfigurationProperties` class | Matches the codebase's existing `@Value`-only convention (`SharedWebConfig`); a `@ConfigurationProperties` record would need constructor-binding machinery this project doesn't use elsewhere for a two-entry map | `AssessmentConfig.java`, `AssessmentAgentOrchestrator.java` | 2026-07-12 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R-01 | `AssessmentCommand.model`, once forwarded to a provider (fixed 2026-07-12, code review P1), is not validated against a list of models the resolved provider actually supports — an unsupported value surfaces as whatever error the provider itself returns, rather than a clear, provider-agnostic rejection | Not blocking — no registry of valid models per provider exists anywhere yet | OPEN | Same future planning as the story's own Residual #1 (model/capability registry + usage limits) — a model whitelist per provider is a natural extension of that registry, not a separate concern |
| R-02 | `AssessmentGenerationPortSelector.resolve`/`.supports` only treats a literal `null` `provider` as "use the configured default" — a blank string (`""`) is rejected as an unrecognized provider (`INVALID_COMMAND`). Web/DTO clients commonly submit `""` as a "not specified" shape, not `null`, so a caller that omits the provider via an empty string gets a rejection instead of the intended default-provider behavior (flagged by code review on task-04, 2026-07-12) | Not blocking — no client currently sends a blank `provider`; `api/`'s `agentclient` sends either a real value or omits the field entirely | OPEN | Decide whether blank-string normalization belongs at the API/DTO boundary (`api/`'s `agentclient`, before the `AssessmentCommand` is built) or inside `AssessmentGenerationPortSelector` itself, then implement in whichever future task/planning owns that boundary |

---

> [← planning/README.md](../../README.md)
