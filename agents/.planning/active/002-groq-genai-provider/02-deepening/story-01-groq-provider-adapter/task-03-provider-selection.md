# ⚛️ TASK 03 — Provider/model selection (Strategy pattern), default Groq

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02
> [← story file](../story-01-groq-provider-adapter.md)

---

## Objective

A request to `POST /internal/agents/assessment` can specify which provider (and optionally model) to use; when omitted, it defaults to Groq. Both `GeminiAssessmentGenerationAdapter` and `GroqAssessmentGenerationAdapter` are registered as named beans and selected at runtime through a Strategy-style resolver — adding a third provider later means adding one more named `@Bean`, no resolver changes. `AgentExecutionLogPayload.costEstimate` is computed with the resolved provider's own per-1K-token rate, not the single Gemini-only constant that exists today.

**Source:** explicit human direction — Strategy pattern via `Map<String, AssessmentGenerationPort>` injection, so future providers are additive. Per-provider cost rate: explicit human direction during task-01's review — `AssessmentAgentOrchestrator.COST_PER_1K_TOKENS` is a single hardcoded Gemini-only estimate; leaving it as-is would silently misprice every Groq execution (Groq's free tier is effectively $0) the moment this task wires Groq in. A full per-provider/per-model pricing+limits registry is out of scope (see story Residual #1) — this task only stops the estimate from being actively wrong.

---

## Technical Design

- **Approach:** Add nullable `provider` (and optionally `model`) fields to `AssessmentCommand` — it is already the direct `@RequestBody` of `AssessmentController.generate`, so this is the natural, minimal-friction place for a per-request override (same pattern already used for `previousDraft`). Register `geminiAssessmentGenerationAdapter`/`groqAssessmentGenerationAdapter` as beans with explicit `@Bean(name = "gemini")` / `@Bean(name = "groq")`. Introduce a small resolver (e.g. `AssessmentGenerationPortSelector`) constructed with `Map<String, AssessmentGenerationPort> portsByProvider` (Spring auto-populates this map keyed by bean name) plus the default-provider value (`app.agents.llm.default-provider: groq`, from `application.yml`, no per-provider env needed since it's not a secret). `resolve(String requestedProvider)` returns a small record carrying both the resolved provider's name and its port (e.g. `SelectedProvider(String name, AssessmentGenerationPort port)`), not just the port — the orchestrator needs the name too, to pick the matching cost rate. `AssessmentAgentOrchestrator` depends on the selector instead of a single `AssessmentGenerationPort` — its constructor signature changes; `generate` step resolves `selector.resolve(command.provider())` before calling `.generate(renderedPrompt)`. Replace the single `COST_PER_1K_TOKENS` constant with a `Map<String, Double>` read from `app.agents.llm.cost-per-1k-tokens.<provider>` (e.g. `.gemini: 0.000075`, `.groq: 0.0` for its free tier) — the orchestrator looks up the resolved provider's own rate instead of applying Gemini's rate to every execution.
- **Affected files / components:** `application/command/AssessmentCommand.java` (new field(s)), `application/orchestrator/AssessmentAgentOrchestrator.java` (constructor + `validate()` + generate step + cost-rate lookup replacing `COST_PER_1K_TOKENS`), new `application/port/out/AssessmentGenerationPortSelector.java` (or similar, including its `SelectedProvider` return type), `infrastructure/config/AssessmentConfig.java` (bean names, selector wiring, both adapters registered — replaces the current single-adapter wiring), `application.yml` (`app.agents.llm.default-provider`, `app.agents.llm.cost-per-1k-tokens.*`), `AssessmentCommandTest.java`, `TRACEABILITY.md` (cross-repo notification if `AssessmentCommand` changes — flag for `api/`'s `agentclient` child planning, same as `previousDraft`). **(Added after code review, P1)** `application/port/out/AssessmentGenerationPort.java` (`generate` gains a `model` parameter), `infrastructure/adapter/out/gemini/GeminiAssessmentGenerationAdapter.java` and `infrastructure/adapter/out/groq/GroqAssessmentGenerationAdapter.java` (forward a non-null `model` as a per-call `ChatOptions` override via `GoogleGenAiChatOptions.builder().model(...)` / `OpenAiChatOptions.builder().model(...)`), `GeminiAssessmentGenerationAdapterTest.java` (updated signature, new test proving the forward) — the first draft of this task accepted `AssessmentCommand.model` on the contract but never read it in `generate()`, silently discarding any value a caller sent; code review caught this before the task closed.
- **Interfaces / contracts:** `AssessmentCommand` gains `provider`/`model` — a contract change visible to `api/`'s `agentclient`. `AssessmentAgentOrchestrator`'s constructor signature changes (internal, no external caller depends on it directly).
- **Risk:** An invalid/unknown `provider` value must fail clearly, not silently fall through to a `NullPointerException` from an absent map key — `validate()` must reject it with `AssessmentAgentException(INVALID_COMMAND)` before the selector is ever consulted.
- **Design notes:** Both adapters stay gated the same proven way as `001` (`@ConditionalOnProperty`, not `@ConditionalOnBean` — see `001-assessment-creation/RETROSPECTIVE-RAW.md` 2026-07-10 20:40 for why the latter silently breaks). `app.agents.gemini.enabled` already exists and gates Gemini; add an equivalent for Groq if needed, but both must be enabled by default so the selector map is never empty under normal profiles.

---

## Implementation Steps

1. Add `provider` and `model` (both nullable `String`) to `AssessmentCommand`; update its Javadoc following the same non-citation convention as the rest of this record (state reasoning generically, no guideline-doc references).
2. Rename the existing Gemini `@Bean` method in `AssessmentConfig` to `@Bean(name = "gemini")`, add `@Bean(name = "groq") groqAssessmentGenerationAdapter(...)` using task-01's adapter and task-02's Groq-specific `ChatClient.Builder`.
3. Create `AssessmentGenerationPortSelector`, constructed with the injected `Map<String, AssessmentGenerationPort>` and the configured default provider; `resolve(String requestedProvider)` returns a `SelectedProvider(name, port)` for the requested or default provider, throwing if the requested key isn't in the map.
4. Update `AssessmentAgentOrchestrator`'s constructor to depend on the selector; update `validate()` to reject an unrecognized `provider` value before generation is attempted; update the generation step to call `selector.resolve(command.provider())` and use the returned name for both the cost-rate lookup and `AgentExecutionLogPayload`.
5. Add `app.agents.llm.default-provider: groq` and `app.agents.llm.cost-per-1k-tokens.gemini`/`.groq` to `application.yml`; replace `AssessmentAgentOrchestrator.COST_PER_1K_TOKENS` with a per-provider lookup keyed by the resolved provider's name.
6. Cross-notify `api/`'s `agentclient` child planning (same convention as the `previousDraft` addition in `001`) that `AssessmentCommand` gained `provider`/`model`.
7. Make one real end-to-end request through `POST /internal/agents/assessment` with no `provider` specified, confirming it reaches Groq and returns a real `AssessmentResult` — this is the first successful live-Gemini-alternative call, closing `001`'s Residual #1.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Omitting `provider` routes to Groq | Real end-to-end request with no `provider` field |
| 2 | `provider: "gemini"` routes to Gemini | Real end-to-end request with `provider: "gemini"` |
| 3 | Unrecognized `provider` value is rejected clearly | Request with `provider: "bogus"` returns 422 with `AssessmentAgentException(INVALID_COMMAND)` |
| 4 | `costEstimate` uses the resolved provider's own rate | Compare `AgentExecutionLogPayload.costEstimate` between a Groq run and a Gemini run with comparable token counts — they must differ (Groq ≈ 0), not both apply Gemini's rate |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | App running locally under `beta` profile with `.env` populated (task-02) |
| 2 | App compiles and starts | `./mvnw -Pbeta spring-boot:run -Dspring-boot.run.profiles=beta` |
| 3 | Connectivity or schema validation succeeds | Both `gemini`/`groq` beans present in the selector's map at startup (log or debug check) |
| 4 | Changed surface responds correctly | `curl` requests covering all three verification rows above |
| 5 | No startup regressions are visible | No new bean-wiring exceptions in logs |

### Database / ORM Consistency Check

N/A — no database or ORM artifacts involved.

---

## Done Criteria

- [x] `AssessmentCommand` carries `provider`/`model`; `api/`'s `agentclient` child planning notified of the contract change.
- [x] `AssessmentGenerationPortSelector` (Strategy pattern via `Map<String, AssessmentGenerationPort>`) resolves the correct adapter; adding a future provider requires only a new named `@Bean`.
- [x] Default provider is Groq when `provider` is omitted.
- [x] An unrecognized `provider` value is rejected with `AssessmentAgentException(INVALID_COMMAND)`, not an unhandled exception.
- [x] `AgentExecutionLogPayload.costEstimate` is computed from the resolved provider's own per-1K-token rate (`app.agents.llm.cost-per-1k-tokens.<provider>`), not a single Gemini-only constant applied to every provider.
- [x] At least one real end-to-end request against Groq succeeds with a genuine `AssessmentResult` — closes `001`'s Residual #1 (no successful live-provider call had ever been observed).
- [x] `./mvnw test` passes (existing tests updated for the orchestrator's new constructor signature; new tests deferred to task-04).
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-groq-provider-adapter.md)
