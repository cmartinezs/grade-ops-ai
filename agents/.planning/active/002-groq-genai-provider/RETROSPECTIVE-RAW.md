# Retrospective Raw Notes: 002-groq-genai-provider

> [← README](README.md)

Working log for events that were unexpected, corrective, risky, or useful for the final retrospective.

Use `/plan-edge-case <planning-id> -- <what happened>` to add manual entries. Commands may also append entries when they encounter blockers, corrections, skipped work, recovery actions, validation failures, or other non-linear events.

---

## How To Use This File

Capture facts while they are fresh. Do not polish entries here. The final retrospective belongs in `README.md`.

Each entry should answer as many of these as possible:

- What happened?
- What was expected instead?
- How was it resolved or contained?
- What should be carried forward?

---

## Log

<!-- Add newest entries at the top. -->

### 2026-07-12 - task-01 code review: two real findings — OpenAI autoconfig broke contextLoads, and curl evidence didn't prove the real ChatClient path

- **Source:** human code review (PR #35), then verified directly rather than accepted at face value, per `superpowers:receiving-code-review` discipline
- **Related story/task:** story-01-groq-provider-adapter, task-01-groq-adapter
- **What happened (P1, confirmed real):** `./mvnw -Pbeta test -Dtest=GradeOpsAgentsApplicationTest` failed with `BeanCreationException` — adding `spring-ai-starter-model-openai` (for Groq) put OpenAI's six autoconfiguration classes (`OpenAiChatAutoConfiguration`, `OpenAiAudioSpeechAutoConfiguration`, etc.) on the classpath, and none had credentials under the `test` Spring profile, unlike Google GenAI's which are already excluded there. Reproduced exactly as the reviewer described before touching anything.
- **Resolution (P1):** added all six OpenAI autoconfiguration classes to `application-test.yml`'s `spring.autoconfigure.exclude` list, mirroring the existing Google GenAI exclusions. Verified with the reviewer's exact command (`-Dtest=GradeOpsAgentsApplicationTest,GeminiAssessmentGenerationAdapterTest` → `BUILD SUCCESS`) and the full suite (18/18).
- **What happened (P2, confirmed real and deeper than expected):** the reviewer noted `task-01-groq-structured-output-evidence.md` used raw `curl`, not the actual `ChatClient.responseEntity(AssessmentResult.class)` Java path the task claims to have validated. Verifying this properly (a scratch `ApplicationContextRunner` test building a real `ChatClient` via `OpenAiChatAutoConfiguration` pointed at Groq) revealed the raw-curl evidence had been **hiding a real failure**: `llama-3.1-8b-instant` — the model used throughout this story's examples — returns `HTTP 200` but echoes the JSON Schema itself back as the response content when called through `responseEntity(Class)`, instead of data conforming to it, because Spring AI's `BeanOutputConverter` builds a different (schema-embedding) prompt than the raw curl's explicit field-by-field instruction did. `AssessmentResult` came back with every field `null`/empty. Retrying with `llama-3.3-70b-versatile` (same test, same prompt, only the model changed) succeeded correctly.
- **Resolution (P2):** corrected every reference to `llama-3.1-8b-instant` as the example/default Groq model (`MANUAL-PROVIDER-TESTING.md`) to `llama-3.3-70b-versatile`. Rewrote `task-01-groq-structured-output-evidence.md`'s Conclusion to state plainly that the mapping code itself is correct but the model choice matters, with both the failing and passing real-`ChatClient` transcripts committed as evidence. Also confirmed empirically, as a side effect of building the real `ChatClient`, that Groq's OpenAI-compatible base URL needs the `/v1` suffix (`https://api.groq.com/openai/v1`) — a bare `https://api.groq.com/openai` 404s (the scratch test's first attempt used the bare form and failed this way before being corrected); task-02's already-planned `GRADEOPS_GROQ_BASE_URL` default was written with `/v1` from the start and needed no change.
- **Retrospective signal:** a raw-curl call and a `ChatClient.responseEntity(Class)` call can use entirely different mechanisms to reach "structured JSON" (explicit prompt instructions + basic `response_format: json_object` vs. Spring AI's own schema-embedding `BeanOutputConverter` prompt) — passing one is not evidence the other passes, especially for a smaller/faster model whose instruction-following is weaker than its raw JSON-mode compliance. When a task's Done Criteria claims a specific code path was validated, verify that exact path, not an adjacent one that happens to be easier to script. The reviewer's insistence on this distinction (P2) surfaced a real, previously-invisible model-selection bug, not just a documentation nit — validates the "verify before implementing, don't performatively agree" discipline even (especially) when the initial fix looks like it satisfies the letter of the feedback.
- **Follow-up (human direction):** the scratch verification test was originally planned for deletion after capturing evidence. Human explicitly asked to keep it instead — useful going forward to re-validate provider communication on demand whenever Groq's behavior, the chosen model, or the Spring AI version changes, without needing to re-derive the `ApplicationContextRunner`/autoconfiguration wiring from scratch each time. Kept as `GroqChatClientManualVerification` (renamed from `...ScratchVerification` to reflect its permanent role), made reusable: `GROQ_MODEL` is now an overridable env var (default `llama-3.3-70b-versatile`) instead of hardcoded, it skips gracefully via `Assumptions.assumeTrue` when `GROQ_API_KEY` isn't set rather than failing, and it asserts the mapped result is actually populated (not just prints it) so a run gives a clear pass/fail, not only text to eyeball. Confirmed the filename still keeps it out of Surefire's default `**/*Test.java`/`Test*.java`/`**/*Tests.java` include patterns, so `./mvnw test` doesn't pick it up automatically — full suite re-verified at 18/18 after the rename.

### 2026-07-12 - task-01: `./mvnw compile`/`test` fails without a Maven profile active; both AI starters are profile-gated

- **Source:** `/plan-task` execution, story-01 task-01, discovered running the baseline `./mvnw compile` before adding the Groq dependency
- **Related story/task:** story-01-groq-provider-adapter, task-01-groq-adapter
- **What happened:** `CLAUDE.md`'s documented command for `agents/` is `./mvnw test` with no profile, but running it as-written fails to compile at all — `GeminiAssessmentGenerationAdapter.java`/`AssessmentConfig.java` reference `org.springframework.ai.chat.client.ChatClient` and related types, which only exist on the classpath when the `beta` or `demo` Maven profile is active (that's where `spring-ai-starter-model-google-genai` lives, per `agents/pom.xml`). This is a pre-existing condition from `001`, not introduced by this task — confirmed by reproducing the failure on the unmodified codebase before touching anything.
- **Resolution:** added `spring-ai-starter-model-openai` (Groq's OpenAI-compatible client) to the same `beta`/`demo` profiles, matching the existing convention, rather than making either AI starter an unconditional default dependency. Proceeded using `./mvnw -Pbeta ...` for all compile/test/run commands in this planning, same as `001` did throughout its own execution.
- **Retrospective signal:** `CLAUDE.md`'s `agents/` command reference (`./mvnw test`) is inaccurate as written for this service and should eventually be corrected to `./mvnw -Pbeta test` (or similar) — out of scope for this task since it's a pre-existing gap unrelated to Groq, but worth a dedicated fix (flagged as a residual, not fixed here).

### 2026-07-12 - task-01: Groq project had every chat model blocked by default; resolved by enabling one in the console

- **Source:** `/plan-task` execution, story-01 task-01, real Groq API verification call (Implementation Step 3)
- **Related story/task:** story-01-groq-provider-adapter, task-01-groq-adapter
- **What happened:** the first real verification call against Groq (needed to settle R-01 — whether structured-output mapping works) returned `403 model_permission_blocked_project` for `llama-3.3-70b-versatile`. Assumed initially this was a per-model restriction and tried 5 more models (`llama-3.1-8b-instant`, `openai/gpt-oss-20b`, `meta-llama/llama-4-scout-17b-16e-instruct`, `qwen/qwen3-32b`, `allam-2-7b`, `groq/compound`) — every one returned the same error (`groq/compound`, an agentic/compound system, surfaced the error from the underlying model it routed to internally). All 6 attempts were real HTTP round trips with real error responses, not client-side failures — this is an account/project configuration state (the Groq project had zero models enabled by default), not a code or design defect, same category of finding as `001`'s Gemini-credits situation.
- **Resolution:** asked the human explicitly rather than guessing further or silently accepting a blocked-evidence gap; human enabled `llama-3.1-8b-instant` via `console.groq.com/settings/project/limits` and the same request succeeded (`200`, valid JSON matching `AssessmentResult`'s schema exactly). See `task-01-groq-structured-output-evidence.md` for the full request/response.
- **Retrospective signal:** a new Groq project key has no models enabled by default — this needs to be called out wherever the Groq API key setup gets documented for other environments (e.g. Cloud Run provisioning in story-02, or onboarding notes for a future teammate), so nobody else loses time thinking it's a code bug when it's project configuration. Same pattern as `001`'s Gemini-credits lesson: isolate whether a live-API failure is inside the code or upstream *before* concluding the integration itself is broken.

---

> [← README](README.md)
