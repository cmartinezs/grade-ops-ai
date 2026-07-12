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
| `GRADEOPS_GEMINI_API_KEY` / `GRADEOPS_GEMINI_MODEL` / `GRADEOPS_GROQ_API_KEY` / `GRADEOPS_GROQ_MODEL` / `GRADEOPS_GROQ_BASE_URL` | ⚠️ | N/A | Planned env var names (story-01 task-02) — back Spring AI's own `spring.ai.google.genai.*`/`spring.ai.openai.*` property paths; replaces today's `GOOGLE_AI_API_KEY`/`AI_MODEL_NAME`. Property *paths* stay Spring AI's own per explicit human direction — only variable names are project-specific. |
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
