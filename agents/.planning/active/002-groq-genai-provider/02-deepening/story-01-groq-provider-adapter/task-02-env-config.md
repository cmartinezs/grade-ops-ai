# ⚛️ TASK 02 — Project-specific env var names + `.env` loading for both providers

> **Status:** IN PROGRESS
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-groq-provider-adapter.md)

---

## Objective

Both providers' Spring AI autoconfiguration properties (`spring.ai.google.genai.*` for Gemini, `spring.ai.openai.*` for Groq) resolve from project-specific (`GRADEOPS_*`-prefixed) environment variable names instead of the defaults Spring AI's own docs suggest. Local/test runs load these from a `.env` file — no `export` required.

**Source:** explicit human direction — keep Spring AI's own `spring.ai.*` property paths (do not invent new `app.agents.*` properties); only the *env var names* those paths interpolate from change. `.env` for local, Cloud Run env vars for deployed environments (story-02 of this planning).

---

## Technical Design

- **Approach:** Spring's standard `${VAR_NAME}` YAML placeholder syntax already lets the *property path* stay Spring AI's own while the *variable name* is project-specific — e.g. `spring.ai.google.genai.api-key: ${GRADEOPS_GEMINI_API_KEY}` instead of today's `${GOOGLE_AI_API_KEY}`. This is a rename, not an architecture change, for the property paths themselves.
- **Technical Design update (reality contradicted the original design — verified by actually starting the app with both starters present, as the Risk row below required):** with both `spring-ai-starter-model-google-genai` and `spring-ai-starter-model-openai` on the classpath (task-01 added the latter), the app fails to start under `-Pbeta` with `NoUniqueBeanDefinitionException` — Spring AI's own `ChatClientAutoConfiguration.chatClientBuilder()` factory method requires exactly one `ChatModel` bean and finds two (`googleGenAiChatModel`, `openAiChatModel` — both already uniquely named by Spring AI itself). This is not a `task-03` concern deferred here by mistake; it is a full app-boot blocker *today*, since `AssessmentConfig`'s existing (pre-task-03) `geminiAssessmentGenerationAdapter(ChatClient.Builder chatClientBuilder)` can no longer resolve that parameter at all. Fix in *this* task (task-02), minimally, so the app boots again with Gemini alone (task-03 still owns registering Groq's adapter/routing): exclude `org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration` in `application-beta.yml`/`application-demo.yml`, and change `AssessmentConfig.geminiAssessmentGenerationAdapter` to accept `@Qualifier("googleGenAiChatModel") ChatModel chatModel` instead of the ambiguous `ChatClient.Builder`, building the client directly via `ChatClient.builder(chatModel).build()` — a plain static factory call with no Spring bean-resolution ambiguity. `openAiChatModel` (Groq's) stays unused/unwired until task-03.
- **Affected files / components:** `src/main/resources/application-beta.yml`, `application-demo.yml` (rename `GOOGLE_AI_API_KEY`/`AI_MODEL_NAME` → `GRADEOPS_GEMINI_API_KEY`/`GRADEOPS_GEMINI_MODEL`; add Groq's `spring.ai.openai.api-key: ${GRADEOPS_GROQ_API_KEY}`, `spring.ai.openai.base-url: ${GRADEOPS_GROQ_BASE_URL:https://api.groq.com/openai/v1}`, `spring.ai.openai.chat.options.model: ${GRADEOPS_GROQ_MODEL}`), `.env.example` (new, documents all four vars), a dotenv-loading mechanism (new dependency or `EnvironmentPostProcessor` — decide in this task), `.gitignore` check (`.env` must already be ignored — confirmed by the recently-merged root `.gitignore` expansion, verify the `agents/` nested one too).
- **Interfaces / contracts:** None — purely configuration.
- **Risk:** Spring AI 2.0 may register both providers' `ChatClient.Builder` autoconfiguration beans simultaneously once both starters are on the classpath (Gemini's from task's existing dependency, Groq's from task-01's new one) — if Spring AI requires a single active chat-model selector property (e.g. `spring.ai.model.chat`) to avoid an ambiguous-bean situation, that must be discovered and resolved here, since task-03 needs one distinct, named `ChatClient.Builder`-derived bean per provider to build each adapter from. Verify empirically by starting the app with both starters present before calling this task done.
- **Design notes:** `.env` loading must not interfere with `test` Spring profile's existing AI-autoconfiguration exclusion (`application-test.yml`) — the two are orthogonal (one controls *which* autoconfiguration classes load, the other controls *where config values come from*), but verify both together.
- **`.env`-loading decision:** custom `EnvironmentPostProcessor` (`cl.gradeops.ai.agents.shared.infrastructure.config.DotenvEnvironmentPostProcessor`), not a third-party dotenv library. Rejected the library option (e.g. `me.paulschwarz:spring-dotenv`) because its exact Maven coordinates/behavior couldn't be verified against this project's actual Spring Boot 4.1/Spring AI 2.0 combination without a live dependency resolution attempt risking a broken build over a genuinely small amount of logic; a custom `EnvironmentPostProcessor` has zero external dependency risk and full control over precedence (must add `.env` values as *fallback* defaults, never overriding a real shell-exported env var or `-D` system property — local dev convenience, not an override mechanism). Registered via `META-INF/spring.factories` (`org.springframework.boot.env.EnvironmentPostProcessor=...`), the mechanism Spring Boot uses for this specific extension point since it must run before the `ApplicationContext` (and therefore before the `.imports`-based auto-configuration mechanism) exists.

---

## Implementation Steps

1. Decide and add the `.env`-loading mechanism (dotenv library or custom `EnvironmentPostProcessor`); document the choice and rejected alternative briefly in this file once implemented.
2. Create `.env.example` at `agents/` root listing `GRADEOPS_GEMINI_API_KEY`, `GRADEOPS_GEMINI_MODEL`, `GRADEOPS_GROQ_API_KEY`, `GRADEOPS_GROQ_MODEL`, `GRADEOPS_GROQ_BASE_URL` (with the Groq default value noted), never committing real values.
3. Update `application-beta.yml` and `application-demo.yml`: rename Gemini's env var references, add Groq's `spring.ai.openai.*` block pointed at the new var names.
4. Start the app locally under a `beta`-equivalent profile with only a `.env` file populated (no shell `export`) and confirm both providers' `ChatClient.Builder`-derived beans initialize without ambiguity errors.
5. If Spring AI requires an explicit chat-model selector property to avoid autoconfiguring both providers ambiguously, add it and document why.
6. **(Added — step 4 surfaced the `NoUniqueBeanDefinitionException` predicted in Risk)** Exclude `ChatClientAutoConfiguration` in `application-beta.yml`/`application-demo.yml`; change `AssessmentConfig.geminiAssessmentGenerationAdapter` to inject `@Qualifier("googleGenAiChatModel") ChatModel` and build the `ChatClient` directly, instead of the now-ambiguous autoconfigured `ChatClient.Builder`.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Gemini and Groq config values resolve from the new `GRADEOPS_*` var names | `grep -rn "GOOGLE_AI_API_KEY\|AI_MODEL_NAME" src/main/resources/` → no matches; both yml files reference only `GRADEOPS_*` vars |
| 2 | `.env` loads without `export` | Verified: `env -u GRADEOPS_GEMINI_API_KEY -u GRADEOPS_GEMINI_MODEL -u GRADEOPS_GROQ_API_KEY -u GRADEOPS_GROQ_MODEL -u GRADEOPS_GROQ_BASE_URL ./mvnw -Pbeta spring-boot:run -Dspring-boot.run.profiles=beta` with only a local `.env` (dummy values) present — app started successfully in ~2s |
| 3 | `test` Spring profile is unaffected | `./mvnw -Pbeta test` → 18/18, `BUILD SUCCESS` |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | None required — no external services besides the LLM providers themselves |
| 2 | App compiles and starts | Verified — see row 2 above |
| 3 | Connectivity or schema validation succeeds | No `NoUniqueBeanDefinitionException` after excluding `ChatClientAutoConfiguration` and qualifying Gemini's `ChatModel` explicitly; confirmed by successful boot |
| 4 | Changed surface responds correctly | `curl` to `/internal/agents/assessment`: no `X-Internal-Key` → `403`; valid key + blank `learningGoal` → `422 INVALID_COMMAND` with full failure-log payload — proves the whole pipeline (auth → controller → orchestrator → exception handler) still wires correctly through the new Gemini `ChatModel`-qualified bean |
| 5 | No startup regressions are visible | No new exceptions in logs; full suite still 18/18 |

### Database / ORM Consistency Check

N/A — no database or ORM artifacts involved.

---

## Done Criteria

- [x] `spring.ai.google.genai.*` and `spring.ai.openai.*` properties in `application-beta.yml`/`application-demo.yml` resolve exclusively from `GRADEOPS_*`-prefixed env vars.
- [x] `.env.example` committed; `.env` confirmed ignored by `agents/.gitignore`.
- [x] Local run with only `.env` (no `export`) boots successfully.
- [x] `./mvnw test` passes unchanged (18/18 under `-Pbeta`; note `./mvnw test` with no profile fails for the pre-existing, unrelated reason documented in `RETROSPECTIVE-RAW.md` 2026-07-12).
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — the `ChatClientAutoConfiguration`/`AssessmentConfig` fix was required to keep the app bootable at all with task-01's dependency already merged, not scope creep.

---

> [← story file](../story-01-groq-provider-adapter.md)
