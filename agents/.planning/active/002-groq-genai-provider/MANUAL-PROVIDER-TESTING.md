# Manual provider testing — Gemini and Groq

> [← README](README.md)

Runbook for manually verifying an LLM provider two ways: (1) a direct `curl` against the provider's own API, bypassing this codebase entirely — isolates "does the account/key/model work at all" from "does our integration work"; (2) a `curl` against this service's own internal endpoint (`POST /internal/agents/assessment`) — proves the full pipeline (auth, orchestrator, prompt template, provider adapter, response mapping) end-to-end.

Always do (1) before (2) when diagnosing a failure — it tells you in one request whether the problem is upstream (account, credits, model permissions) or in this codebase, the same way both provider investigations below were actually resolved.

---

## 1. Direct provider API calls

### Gemini (Google AI Studio)

```bash
curl -s "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent" \
  -H "x-goog-api-key: ${GOOGLE_AI_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "contents": [{
      "parts": [{
        "text": "Generate an assessment for: evaluate loops and conditionals in Java for first-semester students, introductory level, 60 minutes. Respond ONLY with a JSON object with exactly these fields: title (string), context (string), instructions (string), objectives (array of strings), deliverables (array of strings), constraints (array of strings)."
      }]
    }],
    "generationConfig": { "responseMimeType": "application/json" }
  }'
```

Used in `001-assessment-creation` to isolate the live-Gemini failure: the app returned `422 MALFORMED_OUTPUT`, and this direct call against the same key confirmed the request genuinely reached Google's API and failed with `429 RESOURCE_EXHAUSTED — "Your prepayment credits are depleted"` — an account/billing state, not a bug in this codebase. See `001-assessment-creation/RETROSPECTIVE-RAW.md` (2026-07-11, post-merge entry) and `001-assessment-creation/02-deepening/story-01-assessment-agent.md` Residual #1.

### Groq

```bash
curl -s https://api.groq.com/openai/v1/chat/completions \
  -H "Authorization: Bearer ${GROQ_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama-3.3-70b-versatile",
    "response_format": {"type": "json_object"},
    "messages": [
      {"role": "system", "content": "You generate structured programming assessment drafts. Respond ONLY with a JSON object with exactly these fields: title (string), context (string), instructions (string), objectives (array of strings), deliverables (array of strings), constraints (array of strings)."},
      {"role": "user", "content": "Generate an assessment for: evaluate loops and conditionals in Java for first-semester students, introductory level, 60 minutes."}
    ]
  }'
```

Used in this planning's story-01 task-01 to verify Groq's structured-output support before finalizing `GroqAssessmentGenerationAdapter`'s mapping. First 6 attempts (across 6 different models) returned `403 model_permission_blocked_project` — the Groq project had every chat model disabled by default. Resolved by enabling models at `console.groq.com/settings/project/limits`. **Note for anyone provisioning a new Groq project key** (relevant to story-02's Cloud Run/Secret Manager work): models are not enabled by default; at least one must be explicitly enabled in the Groq console before any request will succeed, regardless of code correctness.

**Model capability matters, not just the raw curl call.** A raw curl with explicit field-by-field instructions is not equivalent evidence to the actual `ChatClient.responseEntity(AssessmentResult.class)` path this codebase uses (Spring AI's `BeanOutputConverter` builds its own schema-based prompt internally). Verified directly: `llama-3.1-8b-instant` passes the raw-curl form of this test but **fails** `responseEntity(AssessmentResult.class)` — it echoes the JSON Schema back instead of filling it in. `llama-3.3-70b-versatile` passes both. Use `llama-3.3-70b-versatile` (or a comparably capable model) as the default/example Groq model — not `llama-3.1-8b-instant`. Full transcript of both real-code-path results: `02-deepening/story-01-groq-provider-adapter/task-01-groq-structured-output-evidence.md`.

---

## 2. Local endpoint calls (`POST /internal/agents/assessment`)

Requires the app running locally (`./mvnw -Pbeta spring-boot:run -Dspring-boot.run.profiles=beta`, with a real key configured per the current provider config — `GOOGLE_AI_API_KEY`/`AI_MODEL_NAME` today, `GRADEOPS_GEMINI_API_KEY`/`GRADEOPS_GEMINI_MODEL`/`GRADEOPS_GROQ_API_KEY`/`GRADEOPS_GROQ_MODEL`/`GRADEOPS_GROQ_BASE_URL` once story-01 task-02 lands) and `app.internal.secret` (or `INTERNAL_API_SECRET`) known.

```bash
curl -s http://localhost:8081/internal/agents/assessment \
  -H "X-Internal-Key: ${INTERNAL_API_SECRET}" \
  -H "Content-Type: application/json" \
  -d '{
    "learningGoal": "Evaluate whether students can implement iterative algorithms correctly",
    "topic": "Array manipulation and loops",
    "level": "introductory",
    "duration": "60 minutes",
    "language": "Java"
  }'
```

**Current state (before story-01 task-03 lands):** this always routes to Gemini — there is no provider selection yet, `GeminiAssessmentGenerationAdapter` is the only bean registered. Once task-03 wires the provider selector, add `"provider": "groq"` (or `"gemini"`) to the request body to route explicitly; omitting it will default to Groq per this story's Done Criteria.

---

## Related

- **Planning:** `002-groq-genai-provider`
- **Supersedes/extends:** `001-assessment-creation`'s own live-Gemini verification (Residual #1), now documented as a repeatable procedure instead of a one-off investigation.

---

> [← README](README.md)
