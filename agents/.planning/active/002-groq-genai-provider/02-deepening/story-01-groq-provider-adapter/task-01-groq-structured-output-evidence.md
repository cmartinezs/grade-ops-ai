# Task 01 — Groq structured-output evidence

> [← task-01-groq-adapter.md](task-01-groq-adapter.md)

Real request/response against Groq's live API, made to verify (R-01, `01-expansion.md`) whether Groq's OpenAI-compatible endpoint can produce JSON matching `AssessmentResult`'s schema before finalizing `GroqAssessmentGenerationAdapter`'s mapping approach.

---

## Attempts against blocked models

The API key's Groq project initially had every chat model blocked at the project level (`403 model_permission_blocked_project`), confirmed against 6 distinct models before the human enabled one via the Groq console (`console.groq.com/settings/project/limits`):

- `llama-3.3-70b-versatile`
- `llama-3.1-8b-instant`
- `openai/gpt-oss-20b`
- `meta-llama/llama-4-scout-17b-16e-instruct`
- `qwen/qwen3-32b`
- `allam-2-7b`
- `groq/compound` (an agentic/compound system that internally routed to the also-blocked `meta-llama/llama-4-scout-17b-16e-instruct`)

Each returned a real HTTP round trip (not a client-side failure) with the same `permissions_error` — an account/project configuration state, not a code or design defect.

---

## Successful call

After the human enabled `llama-3.1-8b-instant` in the Groq console, the same request succeeded.

**Request:**

```bash
curl -s https://api.groq.com/openai/v1/chat/completions \
  -H "Authorization: Bearer ${GROQ_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama-3.1-8b-instant",
    "response_format": {"type": "json_object"},
    "messages": [
      {"role": "system", "content": "You generate structured programming assessment drafts. Respond ONLY with a JSON object with exactly these fields: title (string), context (string), instructions (string), objectives (array of strings), deliverables (array of strings), constraints (array of strings)."},
      {"role": "user", "content": "Generate an assessment for: evaluate loops and conditionals in Java for first-semester students, introductory level, 60 minutes."}
    ]
  }'
```

**Result:** `HTTP 200`

```json
{
  "id": "chatcmpl-11ce50c2-16cb-42fd-9c55-7a6a0eba47e5",
  "object": "chat.completion",
  "model": "llama-3.1-8b-instant",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "{\n  \"title\": \"Assessment: Evaluating Loops and Conditionals in Java\",\n   \"context\": \"This assessment is designed to evaluate student understanding of basic loops and conditionals in Java programming.\",\n   \"instructions\": \"Please answer the following questions within the given time frame of 60 minutes. Do not use any external resources or electronic devices.\",\n   \"objectives\": [\n      \"To demonstrate the ability to write simple loops in Java\",\n      \"To understand the concept of conditional statements in Java\",\n      \"To evaluate the use of if-else statements in Java\",\n      \"To identify common errors in Java loop syntax\"\n   ],\n   \"deliverables\": [\n      \"Completed assessment sheet with all questions\",\n      \"Java code written to solve problems as required\"\n   ],\n   \"constraints\": [\n      \"Maximum time allowed: 60 minutes\",\n      \"No external resources or electronic devices allowed\",\n      \"All questions must be answered in the given space\"\n   ]\n}"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 125,
    "completion_tokens": 198,
    "total_tokens": 323
  }
}
```

The `message.content` string, parsed on its own, is:

```json
{
  "title": "Assessment: Evaluating Loops and Conditionals in Java",
  "context": "This assessment is designed to evaluate student understanding of basic loops and conditionals in Java programming.",
  "instructions": "Please answer the following questions within the given time frame of 60 minutes. Do not use any external resources or electronic devices.",
  "objectives": [
    "To demonstrate the ability to write simple loops in Java",
    "To understand the concept of conditional statements in Java",
    "To evaluate the use of if-else statements in Java",
    "To identify common errors in Java loop syntax"
  ],
  "deliverables": [
    "Completed assessment sheet with all questions",
    "Java code written to solve problems as required"
  ],
  "constraints": [
    "Maximum time allowed: 60 minutes",
    "No external resources or electronic devices allowed",
    "All questions must be answered in the given space"
  ]
}
```

This matches `AssessmentResult`'s six fields exactly (`title`, `context`, `instructions`, `objectives`, `deliverables`, `constraints`), with correct types (strings vs. string arrays) and no extraneous top-level fields.

**This curl call does not prove `GroqAssessmentGenerationAdapter`'s actual mapping approach works** — it manually sent an explicit, field-by-field instruction in the system prompt and asked for `response_format: {"type": "json_object"}` (basic JSON mode). Spring AI's `ChatClient.responseEntity(AssessmentResult.class)` builds its own prompt internally (via `BeanOutputConverter`, which appends a full JSON Schema and a generic "match this schema" instruction) and does not use the provider's native `response_format` parameter at all. These are different mechanisms; passing with one does not prove the other passes. See the next section, added after human code review correctly flagged this gap.

---

## Real `ChatClient.responseEntity(AssessmentResult.class)` verification

`GroqChatClientManualVerification` (`src/test/java/.../infrastructure/adapter/out/groq/`, kept permanently as an on-demand diagnostic — not deleted, per explicit human direction: useful to re-validate provider communication whenever Groq's behavior, the chosen model, or the Spring AI version changes; excluded from the automated suite by filename, run explicitly with `-Dtest=GroqChatClientManualVerification`) built a real Spring AI `ChatClient` via `OpenAiChatAutoConfiguration` pointed at Groq (`spring.ai.openai.base-url=https://api.groq.com/openai/v1` — note the required `/v1` suffix, the SDK does not append it automatically; a bare `https://api.groq.com/openai` 404s), and called `.prompt(renderedPrompt).call().responseEntity(AssessmentResult.class)` — the exact method `GroqAssessmentGenerationAdapter.generate()` calls.

### `llama-3.1-8b-instant` — FAILS

Real call, `HTTP 200`, but the model returned the **JSON Schema itself**, not data conforming to it:

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": { "title": {"type": "string"}, "context": {"type": "string"}, "...": "..." },
  "required": ["constraints", "context", "deliverables", "instructions", "objectives", "title"],
  "additionalProperties": false
}
```

`responseEntity` deserialized this into `AssessmentResult[title=null, context=null, instructions=null, objectives=[], deliverables=[], constraints=[]]` — every field null/empty, since the response has `properties`/`type`/`required` keys, not `title`/`context`/etc. This is a genuine, reproducible failure of the adapter's mapping approach against this specific model — not a transient error.

### `llama-3.3-70b-versatile` — SUCCEEDS

Same test, same prompt, only the model changed. Real call, `HTTP 200`, correctly filled `AssessmentResult`:

```json
{
  "title": "Evaluating Loops and Conditionals in Java",
  "context": "First-semester students, introductory level",
  "objectives": ["Understand the purpose of loops in Java", "Apply conditional statements to control program flow", "Demonstrate the use of for, while, and do-while loops", "Use if-else statements to handle different conditions"],
  "constraints": ["60-minute time limit", "Use only Java programming language", "No external libraries or frameworks allowed"],
  "instructions": "Complete the provided programming tasks to demonstrate understanding of loops and conditionals in Java",
  "deliverables": ["Completed Java code for each task", "Output or results of the programs", "Brief explanations of the programming choices made"]
}
```

Mapped correctly into a fully-populated `AssessmentResult`. `usage`: 341 prompt tokens, 178 completion tokens.

---

## Conclusion (revised)

- **`GroqAssessmentGenerationAdapter`'s mapping code is correct** — `responseEntity(AssessmentResult.class)` works against Groq exactly as it does against Gemini, when the underlying model is capable enough to follow Spring AI's schema-based prompting.
- **`llama-3.1-8b-instant` is not a reliable default/example model** for this adapter — it echoes the JSON Schema back instead of filling it in, a documented instruction-following limitation of smaller models under prompt-based (not natively-enforced) structured output, distinct from Groq's own basic `response_format: json_object` mode (which this same model handled correctly in the raw curl test above). `llama-3.3-70b-versatile` does not have this problem. Every reference to `llama-3.1-8b-instant` as the example/default Groq model elsewhere in this story (task-02, task-03, `MANUAL-PROVIDER-TESTING.md`) has been corrected to `llama-3.3-70b-versatile`.
- The blocked-model attempts are recorded here as a separate finding for `task-03`/story-02: this Groq project's models are disabled by default and require explicit enablement per model in the Groq console — worth a note in whatever setup documentation eventually covers onboarding a new Groq project key.
- **Retrospective signal:** a raw-curl call against a provider's REST API is not equivalent evidence to calling this codebase's actual Spring AI code path — they can use entirely different mechanisms (explicit field instructions + basic JSON mode vs. schema-based prompting) that succeed or fail independently of each other. This was caught by human code review requiring the real `ChatClient` path be exercised, not assumed equivalent — see `RETROSPECTIVE-RAW.md`.

---

> [← task-01-groq-adapter.md](task-01-groq-adapter.md)
