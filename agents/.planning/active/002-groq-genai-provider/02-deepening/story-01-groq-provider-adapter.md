# 🔍 DEEPENING: Story 01 — Groq LLM provider adapter and on-demand provider/model selection

> **Status:** IN PROGRESS
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Implement a Groq adapter (`AssessmentGenerationPort` implementation, parallel to `GeminiAssessmentGenerationAdapter` from `001-assessment-creation`) so the assessment pipeline can run against Groq's free tier instead of Gemini. Provider (and optionally model) must be selectable per request, defaulting to Groq when not specified, via a Strategy pattern (`Map<String, AssessmentGenerationPort>`) so future providers are additive. Local/test configuration loads from a `.env` file; both providers keep Spring AI's own `spring.ai.<provider>.*` property paths, but every value resolves from a project-specific (`GRADEOPS_*`) env var name instead of the provider's suggested default.

**Source:** `00-initial.md` (Why, Open Questions), `01-expansion.md` (Notes, R-01/R-02/R-03).

**Manual verification procedure (both providers, direct-API and local-endpoint):** [`MANUAL-PROVIDER-TESTING.md`](../MANUAL-PROVIDER-TESTING.md).

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
| 1 | [GroqAssessmentGenerationAdapter](story-01-groq-provider-adapter/task-01-groq-adapter.md) | GENERATE-DOCUMENT | IN PROGRESS | `GroqAssessmentGenerationAdapter.java`, `spring-ai-starter-model-openai` dependency, real-Groq-call evidence |
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
| 1 | This story's `provider`/`model` selection (task-03) only supports a flat provider name plus an optional literal model string, chosen by the caller who must already know valid values. A further step — explicitly flagged by the human during task-01's review, not in scope here — would have `agents/` maintain a registry of which models each provider offers and which are appropriate for which *capability* (e.g. "rubric-generation" vs. "assessment-generation" may warrant different models), **plus each model's usage limits (RPS, RPD, max tokens per request, and any plan-tier ceiling)** — both `api/` and `agents/` (`agents/` especially, since it's the one making the actual provider call) need this to decide whether to retry, queue, fall back to another provider/model, or surface a clear error when a limit is hit, rather than discovering it only from a failed live call. Exposing a discovery endpoint so `api/` can ask "what model should I use for X, and what are its limits" instead of hardcoding model names/limits. Closer to `agents/`'s own operational metadata than a domain entity (`CLAUDE.md`: "Agents do not own domain entities"), but would still need some persisted/configured registry — worth its own planning once a second agent exists to make the capability dimension concrete. **Related, already-observed gap:** `AssessmentAgentException.Reason` today only distinguishes `INVALID_COMMAND`/`MALFORMED_OUTPUT` — a provider's quota/rate-limit rejection (e.g. `001`'s real Gemini `429 RESOURCE_EXHAUSTED`) currently collapses into `MALFORMED_OUTPUT`, which mislabels a capacity problem as a parsing problem; a dedicated reason (e.g. `PROVIDER_LIMIT_EXCEEDED`) belongs with this same future work. | Future planning (candidate: once a second `agents/` agent exists, so "capability" has more than one real value to register against) | OPEN |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
