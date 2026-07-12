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

---

## Conclusion

- Groq's OpenAI-compatible endpoint supports JSON-mode structured output (`response_format: {"type": "json_object"}`) and reliably follows a field-level schema instruction embedded in the system prompt.
- `GroqAssessmentGenerationAdapter`'s mapping approach (`chatClient.prompt(...).call().responseEntity(AssessmentResult.class)`, identical in shape to `GeminiAssessmentGenerationAdapter`) needs no adjustment — Spring AI's `BeanOutputConverter` behind `responseEntity(Class)` relies on the same prompt-embedded-schema + text-parsing mechanism this evidence call used manually, not on the provider's native `response_format` parameter.
- The blocked-model attempts are recorded here as a separate finding for `task-03`/story-02: this Groq project's models are disabled by default and require explicit enablement per model in the Groq console — worth a note in whatever setup documentation eventually covers onboarding a new Groq project key.

---

> [← task-01-groq-adapter.md](task-01-groq-adapter.md)
