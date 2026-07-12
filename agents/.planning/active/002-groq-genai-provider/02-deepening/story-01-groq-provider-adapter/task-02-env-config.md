# ⚛️ TASK 02 — Project-specific env var names + `.env` loading for both providers

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-groq-provider-adapter.md)

---

## Objective

Both providers' Spring AI autoconfiguration properties (`spring.ai.google.genai.*` for Gemini, `spring.ai.openai.*` for Groq) resolve from project-specific (`GRADEOPS_*`-prefixed) environment variable names instead of the defaults Spring AI's own docs suggest. Local/test runs load these from a `.env` file — no `export` required.

**Source:** explicit human direction — keep Spring AI's own `spring.ai.*` property paths (do not invent new `app.agents.*` properties); only the *env var names* those paths interpolate from change. `.env` for local, Cloud Run env vars for deployed environments (story-02 of this planning).

---

## Technical Design

- **Approach:** Spring's standard `${VAR_NAME}` YAML placeholder syntax already lets the *property path* stay Spring AI's own while the *variable name* is project-specific — e.g. `spring.ai.google.genai.api-key: ${GRADEOPS_GEMINI_API_KEY}` instead of today's `${GOOGLE_AI_API_KEY}`. No manual `ChatClient`/`ChatModel` bean construction needed; Spring AI's autoconfiguration still runs off these properties exactly as it does today for Gemini. This is a rename, not an architecture change.
- **Affected files / components:** `src/main/resources/application-beta.yml`, `application-demo.yml` (rename `GOOGLE_AI_API_KEY`/`AI_MODEL_NAME` → `GRADEOPS_GEMINI_API_KEY`/`GRADEOPS_GEMINI_MODEL`; add Groq's `spring.ai.openai.api-key: ${GRADEOPS_GROQ_API_KEY}`, `spring.ai.openai.base-url: ${GRADEOPS_GROQ_BASE_URL:https://api.groq.com/openai/v1}`, `spring.ai.openai.chat.options.model: ${GRADEOPS_GROQ_MODEL}`), `.env.example` (new, documents all four vars), a dotenv-loading mechanism (new dependency or `EnvironmentPostProcessor` — decide in this task), `.gitignore` check (`.env` must already be ignored — confirmed by the recently-merged root `.gitignore` expansion, verify the `agents/` nested one too).
- **Interfaces / contracts:** None — purely configuration.
- **Risk:** Spring AI 2.0 may register both providers' `ChatClient.Builder` autoconfiguration beans simultaneously once both starters are on the classpath (Gemini's from task's existing dependency, Groq's from task-01's new one) — if Spring AI requires a single active chat-model selector property (e.g. `spring.ai.model.chat`) to avoid an ambiguous-bean situation, that must be discovered and resolved here, since task-03 needs one distinct, named `ChatClient.Builder`-derived bean per provider to build each adapter from. Verify empirically by starting the app with both starters present before calling this task done.
- **Design notes:** `.env` loading must not interfere with `test` Spring profile's existing AI-autoconfiguration exclusion (`application-test.yml`) — the two are orthogonal (one controls *which* autoconfiguration classes load, the other controls *where config values come from*), but verify both together. Two concrete options for `.env` loading: (a) a small dotenv library (e.g. `me.paulschwarz:spring-dotenv`) that populates the `Environment` before Spring Boot's own property resolution runs, or (b) a custom `EnvironmentPostProcessor` reading `.env` manually. Pick based on which interacts more predictably with the existing profile-based YAML files — document the choice and why in this task's implementation notes.

---

## Implementation Steps

1. Decide and add the `.env`-loading mechanism (dotenv library or custom `EnvironmentPostProcessor`); document the choice and rejected alternative briefly in this file once implemented.
2. Create `.env.example` at `agents/` root listing `GRADEOPS_GEMINI_API_KEY`, `GRADEOPS_GEMINI_MODEL`, `GRADEOPS_GROQ_API_KEY`, `GRADEOPS_GROQ_MODEL`, `GRADEOPS_GROQ_BASE_URL` (with the Groq default value noted), never committing real values.
3. Update `application-beta.yml` and `application-demo.yml`: rename Gemini's env var references, add Groq's `spring.ai.openai.*` block pointed at the new var names.
4. Start the app locally under a `beta`-equivalent profile with only a `.env` file populated (no shell `export`) and confirm both providers' `ChatClient.Builder`-derived beans initialize without ambiguity errors.
5. If Spring AI requires an explicit chat-model selector property to avoid autoconfiguring both providers ambiguously, add it and document why.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Gemini and Groq config values resolve from the new `GRADEOPS_*` var names | Start the app with only those vars set (via `.env`), confirm no `GOOGLE_AI_API_KEY`/`AI_MODEL_NAME` references remain anywhere in `src/main/resources/` |
| 2 | `.env` loads without `export` | Start the app from a shell with none of the four vars exported, only a local `.env` file present; app boots successfully |
| 3 | `test` Spring profile is unaffected | `./mvnw test` still passes with `application-test.yml`'s existing AI-autoconfiguration exclusion untouched |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | None required — no external services besides the LLM providers themselves |
| 2 | App compiles and starts | `./mvnw -Pbeta spring-boot:run -Dspring-boot.run.profiles=beta` with a `.env` file populated with real or dummy values for both providers |
| 3 | Connectivity or schema validation succeeds | Confirm both `ChatClient.Builder`-derived beans exist in the context (e.g. via `/actuator/beans` if enabled, or a temporary log line) without a `NoUniqueBeanDefinitionException` |
| 4 | Changed surface responds correctly | N/A — no adapter is wired to the endpoint yet (task-03); this task only proves config resolution |
| 5 | No startup regressions are visible | No new `BeanCreationException`/`UnsatisfiedDependencyException` in logs versus the pre-task baseline |

### Database / ORM Consistency Check

N/A — no database or ORM artifacts involved.

---

## Done Criteria

- [ ] `spring.ai.google.genai.*` and `spring.ai.openai.*` properties in `application-beta.yml`/`application-demo.yml` resolve exclusively from `GRADEOPS_*`-prefixed env vars.
- [ ] `.env.example` committed; `.env` confirmed ignored by `agents/.gitignore`.
- [ ] Local run with only `.env` (no `export`) boots successfully.
- [ ] `./mvnw test` passes unchanged.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-groq-provider-adapter.md)
