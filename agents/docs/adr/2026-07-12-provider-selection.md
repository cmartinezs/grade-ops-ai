# ADR: Provider/model selection (Strategy pattern), default Groq

**Date:** 2026-07-12
**Status:** Accepted
**Planning:** 002-groq-genai-provider / story-01 / task-03

## Context

Add nullable `provider` (and `model`) fields to `AssessmentCommand` — it is already the direct `@RequestBody` of `AssessmentController.generate`, so this is the natural, minimal-friction place for a per-request override (same pattern already used for `previousDraft`). Register `geminiAssessmentGenerationAdapter`/`groqAssessmentGenerationAdapter` as beans with explicit `@Bean(name = "gemini")` / `@Bean(name = "groq")`. Introduce a small resolver (`AssessmentGenerationPortSelector`) constructed with `Map<String, AssessmentGenerationPort> portsByProvider` (Spring auto-populates this map keyed by bean name) plus the default-provider value (`app.agents.llm.default-provider: groq`, from `application.yml`, no per-provider env needed since it's not a secret). `AssessmentAgentOrchestrator` depends on the selector instead of a single `AssessmentGenerationPort`.

## Decision

Replaced the single `COST_PER_1K_TOKENS` constant with a `Map<String, Double>` read from `app.agents.llm.cost-per-1k-tokens.<provider>` — the orchestrator looks up the resolved provider's own rate **instead of applying Gemini's rate to every execution**, since Groq's free tier is effectively $0 and would otherwise be silently mispriced.

`AssessmentConfig`'s existing `app.agents.gemini.enabled` property continues to gate the whole `@Configuration` class (both providers, the orchestrator, the selector) rather than adding a second `app.agents.groq.enabled` flag: the `test` profile needs the entire assessment feature off (no real `ChatModel` bean for either provider there), and there is no scenario yet requiring one provider enabled while the other is disabled — a second flag would be speculative.

`AssessmentAgentOrchestrator`'s per-provider cost rates are injected as a plain `Map<String, Double>` built in `AssessmentConfig` from two `@Value`-annotated fields, not a `@ConfigurationProperties` class — this matches the codebase's existing `@Value`-only convention (`SharedWebConfig`) rather than introducing constructor-binding machinery for a two-entry map.

After code review (P1), `AssessmentCommand.model` — initially accepted on the contract but never read by the orchestrator, silently discarded — was wired through `AssessmentGenerationPort.generate(String, String)` into both adapters as a per-call `ChatOptions` override (`GoogleGenAiChatOptions.builder().model(...)` / `OpenAiChatOptions.builder().model(...)`), rather than removing the field or deferring it further. Confirmed via `javap` against the installed Spring AI 2.0.0 jars that both providers' options builders concretely support `.model(String)`, making the wiring a small, contained addition rather than a reason to de-scope.

## Consequences

Both adapters stay gated the same proven way as `001` (`@ConditionalOnProperty`, not `@ConditionalOnBean` — see `001-assessment-creation/RETROSPECTIVE-RAW.md` 2026-07-10 20:40 for why the latter silently breaks). An invalid/unknown `provider` value fails clearly via `AssessmentAgentException(INVALID_COMMAND)` rather than falling through to a `NullPointerException` from an absent map key — `AssessmentGenerationPortSelector.supports(String)` lets `validate()` reject it before the selector is ever consulted for resolution. `model` remains unvalidated against a per-provider whitelist (no such registry exists yet — tracked as this story's own Residual #1 and this planning's `TRACEABILITY.md` R-01); an unsupported value surfaces as whatever error the resolved provider itself returns.

## Alternatives Considered

Removing or de-scoping `AssessmentCommand.model` from this task's delivered contract until a later task actually implements the override was considered (raised explicitly by code review as one of two acceptable fixes) but rejected in favor of wiring it now, since the underlying Spring AI mechanism (`ChatOptions.Builder.model(String)`, inherited by both providers' options builders) made the real fix smaller than the documentation/rescoping alternative. A second, per-provider `app.agents.groq.enabled` flag (alongside the existing `app.agents.gemini.enabled`) was considered and rejected as speculative — see Decision above.
