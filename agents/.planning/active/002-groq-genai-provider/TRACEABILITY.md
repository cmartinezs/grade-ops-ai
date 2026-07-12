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
| `GroqAssessmentGenerationAdapter` | ✅ | N/A | Implemented in story-01 task-01 (`infrastructure.adapter.out.groq`) — `AssessmentGenerationPort` implementation for Groq, structurally identical to `001`'s `GeminiAssessmentGenerationAdapter`. Real-call evidence confirms Groq's structured-output mapping needs no adjustment (`task-01-groq-structured-output-evidence.md`). Not yet wired into Spring context — task-03. |
| `spring-ai-starter-model-openai` | ✅ | N/A | Added to `agents/pom.xml`'s `beta`/`demo` profiles (story-01 task-01) — Groq is consumed via its OpenAI-compatible endpoint; Spring AI has no dedicated Groq starter. |
| `AssessmentGenerationPortSelector` | ⚠️ | N/A | Planned in story-01 task-03 — Strategy-pattern resolver over `Map<String, AssessmentGenerationPort>` (Spring-populated by bean name), so future providers are additive. Per explicit human direction. |
| `AssessmentCommand.provider` / `AssessmentCommand.model` | ⚠️ | N/A | Planned contract change (story-01 task-03) — nullable per-request provider/model override, default `groq`. Cross-repo impact: `api/`'s `agentclient` child planning needs to know before implementing its request shape, same pattern as `001`'s `previousDraft` addition. |
| `GRADEOPS_GEMINI_API_KEY` / `GRADEOPS_GEMINI_MODEL` / `GRADEOPS_GROQ_API_KEY` / `GRADEOPS_GROQ_MODEL` / `GRADEOPS_GROQ_BASE_URL` | ✅ | N/A | Implemented in story-01 task-02 — back Spring AI's own `spring.ai.google.genai.*`/`spring.ai.openai.*` property paths in `application-beta.yml`/`application-demo.yml`; replaced `GOOGLE_AI_API_KEY`/`AI_MODEL_NAME`. Property *paths* stay Spring AI's own per explicit human direction — only variable names are project-specific. Documented in `.env.example`. |
| `DotenvEnvironmentPostProcessor` | ✅ | N/A | Implemented in story-01 task-02 (`shared.infrastructure.config`, registered via `META-INF/spring.factories`) — loads `.env` as the lowest-precedence property source for local dev; never overrides a real env var or `-D` property. Chosen over a third-party dotenv library to avoid unverified external dependency risk. |
| `ChatClientAutoConfiguration` exclusion + `AssessmentConfig`'s `@Qualifier("googleGenAiChatModel")` | ✅ | N/A | Fixed in story-01 task-02 — with both Gemini's and Groq's Spring AI starters on the classpath (task-01), Spring AI's own `ChatClientAutoConfiguration` finds two `ChatModel` beans and refuses to build its generic `ChatClient.Builder`. Excluded that autoconfiguration; `AssessmentConfig` now builds Gemini's `ChatClient` directly from the uniquely-named `googleGenAiChatModel` bean. This had silently broken real `-Pbeta` app boot since task-01 merged (the test suite's `test` profile never exercised it) — see `RETROSPECTIVE-RAW.md` 2026-07-12. |
| `app.agents.llm.default-provider` | ⚠️ | N/A | Planned config property (story-01 task-03) — default `groq`, not a secret so lives directly in `application.yml`, not `.env`. |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| — | *None yet* | — | — | — |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| — | *None* | — | — | — |

---

> [← planning/README.md](../../README.md)
