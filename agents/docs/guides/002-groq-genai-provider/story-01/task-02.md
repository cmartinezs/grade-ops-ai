# Provider env var configuration and `.env` loading

**Source:** task-02 | **Area:** AG | **Date:** 2026-07-12

## What it does

Both providers' Spring AI configuration properties (`spring.ai.google.genai.*` for Gemini, `spring.ai.openai.*` for Groq) resolve from project-specific (`GRADEOPS_*`-prefixed) environment variable names instead of Spring AI's own suggested defaults. Local/test runs load these from a `.env` file — no `export` required.

## How to use it

- Copy `agents/.env.example` to `agents/.env` and fill in real values for `GRADEOPS_GEMINI_API_KEY`, `GRADEOPS_GEMINI_MODEL`, `GRADEOPS_GROQ_API_KEY`, `GRADEOPS_GROQ_MODEL`, `GRADEOPS_GROQ_BASE_URL`. Never commit `.env` itself (already gitignored).
- Values in `.env` are loaded as the lowest-precedence property source — a real shell-exported variable or a `-D` system property always wins, so `.env` is purely local-dev convenience, not an override mechanism.
- Deployed environments (Cloud Run) set these same variable names directly as service env vars / Secret Manager references — no `.env` file involved there (see story-02 of this planning).
- `spring.ai.google.genai.*`/`spring.ai.openai.*` remain Spring AI's own property paths — only the variable *names* those paths interpolate from are project-specific.

## Example

```bash
# agents/.env (never committed)
GRADEOPS_GEMINI_API_KEY=your-real-key
GRADEOPS_GEMINI_MODEL=gemini-2.0-flash
GRADEOPS_GROQ_API_KEY=your-real-key
GRADEOPS_GROQ_MODEL=llama-3.3-70b-versatile
GRADEOPS_GROQ_BASE_URL=https://api.groq.com/openai/v1
```

```bash
./mvnw -Pbeta spring-boot:run -Dspring-boot.run.profiles=beta
# boots successfully with no shell `export` needed
```
