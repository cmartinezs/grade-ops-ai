# ⚛️ TASK 01 — GroqAssessmentGenerationAdapter

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-groq-provider-adapter.md)

---

## Objective

A `GroqAssessmentGenerationAdapter` class exists, implements `AssessmentGenerationPort`, and maps a Groq chat-completion response into `AssessmentResult` — mirroring `GeminiAssessmentGenerationAdapter`'s structure, in a new `infrastructure.adapter.out.groq` package. Not yet wired into Spring context (no `@Bean` registration in this task).

---

## Technical Design

- **Approach:** Groq's chat completions API is OpenAI-compatible, so the adapter is built around Spring AI's `ChatClient` the same way `GeminiAssessmentGenerationAdapter` is — constructor takes a `ChatClient`, `generate(String)` calls `chatClient.prompt(renderedPrompt).call().responseEntity(AssessmentResult.class)`, same token-usage extraction pattern (`Usage` vs. `EmptyUsage`, see `GeminiAssessmentGenerationAdapter`'s Javadoc for why a literal `null` check is insufficient). This keeps the adapter itself provider-agnostic in shape; only the underlying `ChatClient`'s configuration (task-02) differs per provider. Add `spring-ai-starter-model-openai` to `agents/pom.xml` — this is the client Groq is consumed through, since Spring AI has no dedicated Groq starter.
- **Affected files / components:** `agents/pom.xml` (new dependency), `src/main/java/cl/gradeops/ai/agents/assessment/infrastructure/adapter/out/groq/GroqAssessmentGenerationAdapter.java`, `src/test/resources/application-test.yml` (exclude OpenAI's 6 autoconfiguration classes — code review finding P1, they require credentials `test` profile doesn't have), `src/test/java/cl/gradeops/ai/agents/assessment/infrastructure/adapter/out/groq/GroqChatClientManualVerification.java` (on-demand real-`ChatClient` diagnostic, kept permanently per human direction — not deleted after use).
- **Interfaces / contracts:** Implements the existing `AssessmentGenerationPort` (`application.port.out`) — no changes to that interface or to `AssessmentGenerationResponse`.
- **Risk:** Groq's OpenAI-compatible endpoint may not support the same structured-output/JSON-mode behavior `responseEntity(AssessmentResult.class)` relies on for Gemini (R-01 from `01-expansion.md`). Reduce by making a real Groq API call during this task (documented as evidence, same convention as `001`'s `task-02-evidence-variant-*.md` files) before finalizing the mapping logic — if native structured output isn't supported, the mapping approach must change here, not be discovered later in task-03/04.
- **Design notes:** Do not register this class as a `@Bean` yet — task-03 wires it into `AssessmentConfig` alongside the provider-selection resolver. Building/testing this class in isolation (constructed directly with `new GroqAssessmentGenerationAdapter(chatClient)`, `ChatClient` built manually just for the evidence call) keeps this task's deliverable independently verifiable without needing task-02's config mechanism to exist yet.

---

## Implementation Steps

1. Add `spring-ai-starter-model-openai` to `agents/pom.xml` (mirrors the existing `spring-ai-starter-model-google-genai` entry's profile/scope placement).
2. Create `infrastructure/adapter/out/groq/GroqAssessmentGenerationAdapter.java`, implementing `AssessmentGenerationPort`, following `GeminiAssessmentGenerationAdapter`'s structure (constructor, `generate`, `EmptyUsage` handling, Javadoc explaining the one-call `responseEntity` rationale).
3. Make a real Groq API call (a `ChatClient` built directly against a real Groq API key and Groq's OpenAI-compatible base URL, outside of Spring wiring) to confirm structured-output mapping actually works with the `AssessmentResult` schema. Record the exact request/response as evidence in a committed file (e.g. `task-01-groq-structured-output-evidence.md` under this story folder), same convention as `001`'s prompt-variant evidence files.
4. If the evidence call reveals structured output isn't natively supported, adjust the mapping approach in this same task before marking it done (do not defer to task-03/04).
5. **(Added after code review, P1)** Exclude `spring-ai-starter-model-openai`'s six autoconfiguration classes in `application-test.yml`, mirroring the existing Google GenAI exclusions — adding the dependency alone breaks `GradeOpsAgentsApplicationTest#contextLoads` under `-Pbeta` because OpenAI's autoconfiguration requires credentials the `test` profile doesn't set.
6. **(Added after code review, P2)** A raw `curl` call is not equivalent evidence to this codebase's actual `ChatClient.responseEntity(AssessmentResult.class)` path (Spring AI's `BeanOutputConverter` builds a different, schema-embedding prompt internally) — build a real `ChatClient` via `OpenAiChatAutoConfiguration` (an `ApplicationContextRunner`-based diagnostic, kept as `GroqChatClientManualVerification` per human direction rather than discarded) and call the exact method the adapter uses. This is what actually surfaced that `llama-3.1-8b-instant` fails this path (echoes the schema back) while `llama-3.3-70b-versatile` succeeds — correct every example/default model reference in this story accordingly.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `GroqAssessmentGenerationAdapter` compiles and implements `AssessmentGenerationPort` correctly | `./mvnw -Pbeta compile` (no-profile `compile` fails for a pre-existing, unrelated reason — see `RETROSPECTIVE-RAW.md` 2026-07-12) |
| 2 | Real Groq call, via the actual `ChatClient.responseEntity(AssessmentResult.class)` path, proves the mapping works | [`task-01-groq-structured-output-evidence.md`](task-01-groq-structured-output-evidence.md) — real `ChatClient` call (not just curl) against `llama-3.3-70b-versatile` returns a fully-populated `AssessmentResult`; `llama-3.1-8b-instant` documented as failing the same real path (schema echo). Reusable curl recipe (both providers, direct-API and local-endpoint) generalized into [`MANUAL-PROVIDER-TESTING.md`](../../MANUAL-PROVIDER-TESTING.md) |
| 3 | `GradeOpsAgentsApplicationTest#contextLoads` still passes under `-Pbeta` after adding the OpenAI starter | `./mvnw -Pbeta test -Dtest=GradeOpsAgentsApplicationTest,GeminiAssessmentGenerationAdapterTest` → `BUILD SUCCESS`; full suite `./mvnw -Pbeta test` → 18/18 |

### Software Smoke Test Check

N/A for this task — the adapter is not yet wired into the Spring context or the internal endpoint; task-03 covers end-to-end smoke verification once routing exists.

### Database / ORM Consistency Check

N/A — no database or ORM artifacts involved.

---

## Done Criteria

- [x] `GroqAssessmentGenerationAdapter.java` exists in `infrastructure.adapter.out.groq`, implements `AssessmentGenerationPort`.
- [x] `spring-ai-starter-model-openai` added to `agents/pom.xml`.
- [x] A real Groq API call's request/response — through the actual `ChatClient.responseEntity(AssessmentResult.class)` path, not just raw curl — is documented as committed evidence, proving the response-mapping approach works with a capable model and identifying which model(s) it fails with.
- [x] `./mvnw -Pbeta compile` succeeds.
- [x] `GradeOpsAgentsApplicationTest#contextLoads` passes under `-Pbeta` (OpenAI autoconfiguration excluded in `application-test.yml`, same as Google GenAI's).
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-groq-provider-adapter.md)
