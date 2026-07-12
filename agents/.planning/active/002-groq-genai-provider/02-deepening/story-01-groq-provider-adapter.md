# 🔍 DEEPENING: Story 01 — Groq LLM provider adapter and on-demand provider/model selection

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Implement a Groq adapter (`AssessmentGenerationPort` implementation, parallel to `GeminiAssessmentGenerationAdapter` from `001-assessment-creation`) so the assessment pipeline can run against Groq's free tier instead of Gemini. Provider (and optionally model) must be selectable per request, defaulting to Groq when not specified, via a Strategy pattern (`Map<String, AssessmentGenerationPort>`) so future providers are additive. Local/test configuration loads from a `.env` file; both providers keep Spring AI's own `spring.ai.<provider>.*` property paths, but every value resolves from a project-specific (`GRADEOPS_*`) env var name instead of the provider's suggested default.

**Source:** `00-initial.md` (Why, Open Questions), `01-expansion.md` (Notes, R-01/R-02/R-03).

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Groq's OpenAI-compatible API doesn't fully support the structured-output behavior `AssessmentResult` mapping relies on | M | M | Verify with a real Groq call before finalizing the adapter's response-mapping design; fall back to explicit JSON parsing if native structured output isn't available |
| `.env` loading mechanism interacts badly with existing Spring profile setup (`test` excludes AI autoconfiguration entirely) | M | L | Prototype the chosen loading mechanism against all three profiles (`test`, `beta`, `demo`) before committing |
| Re-deriving a bean-gating conditional from scratch repeats `001`'s `@ConditionalOnBean` ordering bug | M | M | Reuse the already-fixed `@ConditionalOnProperty` pattern from `001`'s `AssessmentConfig`/`AssessmentController` |

---

## Tasks

> Atomize via `/plan-atomize` before execution begins.

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [GroqAssessmentGenerationAdapter](story-01-groq-provider-adapter/task-01-groq-adapter.md) | GENERATE-DOCUMENT | TODO | `GroqAssessmentGenerationAdapter.java`, `spring-ai-starter-model-openai` dependency, real-Groq-call evidence |
| 2 | [Project-specific env var names + `.env` loading](story-01-groq-provider-adapter/task-02-env-config.md) | GENERATE-DOCUMENT | TODO | `application-beta.yml`/`application-demo.yml` changes, `.env.example`, chosen dotenv-loading mechanism |
| 3 | [Provider/model selection (Strategy pattern), default Groq](story-01-groq-provider-adapter/task-03-provider-selection.md) | GENERATE-DOCUMENT | TODO | `AssessmentCommand` gains `provider`/`model`, `AssessmentGenerationPortSelector.java`, `AssessmentConfig` rewired, real end-to-end Groq call |
| 4 | [Unit tests: Groq adapter and provider selection](story-01-groq-provider-adapter/task-04-unit-tests.md) | GENERATE-DOCUMENT | TODO | `GroqAssessmentGenerationAdapterTest.java`, `AssessmentGenerationPortSelectorTest.java`, updated `AssessmentAgentOrchestratorTest`/`AssessmentCommandTest` |

---

## Done Criteria

- [ ] A request without an explicit provider/model defaults to Groq.
- [ ] A request can explicitly select a provider (and optionally a model) per call; the mechanism is documented and, if it touches `AssessmentCommand`, `api/`'s `agentclient` child planning has been notified.
- [ ] Gemini remains reachable (not removed) — `GeminiAssessmentGenerationAdapter` from `001` is untouched except for config-key renaming if required by the custom-env-var task.
- [ ] All provider configuration (API keys, model names) resolves from project-specific (`GRADEOPS_*`) env var names, while the YAML property paths stay Spring AI's own `spring.ai.<provider>.*`.
- [ ] Local/test runs load configuration from a `.env` file without requiring `export` in the shell.
- [ ] At least one real Groq call succeeds end-to-end (`POST /internal/agents/assessment` → real `AssessmentResult`), closing the live-verification gap `001`'s Residual #1 left open — using Groq's free tier rather than blocked Gemini credits.
- [ ] Unit tests pass (`./mvnw test`).
- [ ] TRACEABILITY.md updated with new terms from this story.

---

## Inconsistencies Found

*Record any contradictions or gaps detected during this story. Use RECORD-INCONSISTENCY workflow.*

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
