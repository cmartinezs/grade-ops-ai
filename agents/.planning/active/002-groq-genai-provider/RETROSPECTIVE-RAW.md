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
