# Planning: 002-groq-genai-provider

> [← planning/README.md](../README.md)

Short working summary for this planning. Keep this file current as the planning moves through INITIAL, EXPANSION, DEEPENING, and COMPLETED.

---

## Overview

- **Planning ID:** 002-groq-genai-provider
- **Current status:** Completed
- **Intent:** Implement a Groq adapter as an alternative LLM provider alongside the existing Gemini adapter (not a replacement), selectable on demand per request (default: Groq), with `.env`-based local test configuration and Cloud Run env vars using project-specific variable names instead of Spring AI's full YAML property path.
- **Owner:** AI agent
- **Started:** 2026-07-11
- **Completed:** 2026-07-12

---

## Key Links

- [Initial context](00-initial.md)
- [Expansion plan](01-expansion.md)
- [Story details](02-deepening/)
- [Traceability](TRACEABILITY.md)
- [Retrospective raw notes](RETROSPECTIVE-RAW.md)
- [Manual provider testing runbook (Gemini + Groq)](MANUAL-PROVIDER-TESTING.md)

---

## Current State

Summarize where this planning stands and what remains before archive.

- [x] Initial intent is complete.
- [x] Expansion stories are dimensioned (2 stories: groq-provider-adapter, groq-infra-provisioning).
- [x] Stories are DONE or intentionally SKIPPED. — story-01 `DONE`; story-02 `MOVED` (relocated to parent planning `009-groq-infra-provisioning`, see Deviations below — not abandoned, its content is executing there).
- [x] Traceability is complete.
- [x] Retrospective is complete.

---

## Retrospective

### Outcomes

- `GroqAssessmentGenerationAdapter` shipped as a full peer to `GeminiAssessmentGenerationAdapter`, both implementing the existing `AssessmentGenerationPort` — Gemini was not removed or degraded.
- `AssessmentGenerationPortSelector` (Strategy pattern over `Map<String, AssessmentGenerationPort>`) makes provider selection additive: a third provider needs one more named `@Bean`, no resolver changes. Default provider flipped to Groq (`app.agents.llm.default-provider: groq`), overridable per request via `AssessmentCommand.provider`.
- `AssessmentCommand.model` is a genuine per-call override forwarded as a `ChatOptions` builder to the resolved provider's `ChatClient` — not merely accepted and ignored (this required a mid-story code-review fix, see Deviations).
- Custom `GRADEOPS_*` env var names back Spring AI's own `spring.ai.<provider>.*` property paths; a custom `DotenvEnvironmentPostProcessor` loads `.env` as the lowest-priority property source for local/test runs without shell `export`.
- Closed `001-assessment-creation`'s open Residual #1: a real Groq call succeeded end-to-end (`POST /internal/agents/assessment` → real `AssessmentResult`), using Groq's free tier since Gemini's paid credits were exhausted. Verified both the default-provider path and the explicit-model-override path with a live differential test.
- Per-provider cost estimation (`app.agents.llm.cost-per-1k-tokens.<provider>`) replaced the old single Gemini-only constant.
- 4 tasks, 4 PRs (#35–#38), all with human code review — 7 real findings caught and fixed across the story (see Deviations), one re-review false positive correctly pushed back on with evidence.

### Deviations

- **Model field silently discarded (task-03 P1, real finding):** `AssessmentCommand.model` was documented as forwarded but never actually read by the orchestrator before calling the port. Fixed by threading `model` through `AssessmentGenerationPort.generate(String, String)` and both adapters; verified via a live differential Groq call (default vs. explicit override produced different `log.model`).
- **OpenAI autoconfiguration broke real app boot, not just a test (task-01/task-02, real finding):** adding Groq's OpenAI-compatible starter put six OpenAI autoconfiguration classes on the classpath; `ChatClientAutoConfiguration` found two `ChatModel` beans (Gemini + Groq) and refused to build its generic `ChatClient.Builder`, breaking `-Pbeta` boot even though the `test` profile's own exclusions masked it. Fixed by excluding `ChatClientAutoConfiguration` and building each provider's `ChatClient` directly from its own qualified `ChatModel` bean.
- **Groq model choice mattered for structured output (task-01, real finding):** raw-curl evidence of Groq's structured-output support had hidden a real failure — `llama-3.1-8b-instant` echoed the JSON Schema itself instead of populated data when called through Spring AI's `responseEntity(Class)` path; `llama-3.3-70b-versatile` worked correctly. A raw-curl pass is not evidence a `ChatClient.responseEntity` pass will succeed.
- **story-02 relocated out of this planning (2026-07-12):** originally kept inside this planning with the reasoning "`infra/` has no `.planning/` of its own, so keep it here" — the human corrected this: infra-only work with no dedicated child workspace belongs in the **parent** (root) planning, not a sibling child's tree. Relocated to `009-groq-infra-provisioning` (root worktree, branch `develop`), content carried forward unchanged. This planning's own scope is therefore narrower at completion than at expansion — story-01 (the actual Groq adapter) only.
- **Task-04's own Technical Design fell out of sync with already-approved task-03 behavior:** task-04 was written (at atomization time) before task-03 existed, and described the selector as falling back to default "when `provider` is null/blank." The real, reviewed task-03 implementation only special-cases literal `null`. Corrected the design text to match reality rather than reopening already-merged task-03 code under task-04's test-only scope; the underlying product question (should blank be treated as omitted?) is tracked as R-02, not resolved here.
- A re-review's P2 finding (task-03) claiming a cross-repo `api/` notification was missing turned out to be a false positive: the reviewer had inspected `api/` nested inside the `agents/` worktree rather than the actual sibling `api/` worktree the fix was committed to. Verified directly via `git log`/`grep` on the real worktree and pushed back with evidence instead of redoing already-correct work.

### Follow-ups

- **R-01 (open):** `AssessmentCommand.model` is forwarded but not validated against a list of models the resolved provider actually supports — an unsupported value surfaces as whatever error the provider returns.
- **R-02 (open):** `AssessmentGenerationPortSelector` only treats literal `null` as "use default"; a blank string (`""`) is rejected as an unrecognized provider. Web/DTO clients commonly send `""` for "not specified." Whether normalization belongs at the API/DTO boundary or inside the selector is undecided.
- **Carried-forward residual (from `001`, restated in this planning's story-01):** no per-provider model/capability registry or usage-limits (RPS/RPD/token ceilings) registry exists yet; both `api/` and `agents/` currently discover limits only from a failed live call. A dedicated `AssessmentAgentException.Reason` for provider quota/rate-limit rejection (distinct from `MALFORMED_OUTPUT`) belongs with this same future work. Deferred to a future planning, candidate trigger: once a second `agents/` agent exists so "capability" has more than one real value to register against.
- **`009-groq-infra-provisioning` (parent planning, root worktree):** owns the actual Terraform provisioning (Secret Manager entry + Cloud Run env/secret binding for the Groq key) that this planning's env-var naming depends on being wired into `demo`. Not yet atomized as of this planning's completion.

### Lessons

- A raw-curl call and a `ChatClient.responseEntity(Class)` call can reach "structured JSON" through entirely different mechanisms — passing one is not evidence the other passes, especially for a smaller/faster model. When a task's Done Criteria claims a specific code path was validated, verify that exact path.
- `./mvnw test` passing under the `test` profile is not evidence a different Maven profile's real autoconfiguration combination still boots, when that profile's exclusions are exactly what the `test` profile's own exclusions happen to mask. An empirical boot check (not just the test suite) is required whenever a task changes which autoconfiguration classes are on the classpath for a real-running profile.
- Logging a design/implementation discrepancy in `RETROSPECTIVE-RAW.md` is not the same as resolving it — the task file a reviewer actually diffs against the tests must be corrected too, or the same finding recurs on re-review.
- When a child artifact has no `.planning/` of its own, "keep the work in the nearest existing child planning" is not automatically correct — check with the human (or an explicit project convention) whether the parent should own it directly instead. This planning got that call wrong once at expansion time and had to relocate a whole story after execution.
- In a multi-worktree/child-planning setup, verifying a cross-repo review claim means checking the actual worktree path the change was committed to, not assuming any directory that happens to share the same name.

---

> [← planning/README.md](../README.md)
