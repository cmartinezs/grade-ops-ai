# 🌱 INITIAL: 002-groq-genai-provider

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

> *What needs to be done, in one sentence.*

Implement a Groq adapter as an alternative LLM provider alongside the existing Gemini adapter (not a replacement) — a Groq API key is already available — with test-time configuration loaded from a `.env` file and Cloud Run configuration loaded from its own service-level environment variables, using project-specific variable names rather than the full `spring.ai....` YAML property path Spring AI expects by default.

---

## Why

> *Why does this planning exist? What problem does it solve or what value does it deliver?*

A free-to-use alternative for testing and development that doesn't consume Gemini's paid credits (directly motivated by story-01-assessment-agent's still-open residual: no successful live Gemini call has ever been observed, blocked on account credits — see `001-assessment-creation`). Also serves as a proof of concept for how cheaply the pipeline can switch LLM provider and/or model via configuration alone.

Beyond a static per-environment default, the provider/model choice should be selectable **on demand, per request** — the caller can indicate which provider and/or model to use for that specific call; if omitted, the default is Groq (not Gemini).

---

## Approximate Scope

> *Which repositories or areas might be affected? This does not need to be exhaustive.*
> In a monorepo, if a child artifact has its own `.planning/`, list it here but keep its implementation in a child planning created inside that artifact. The parent planning coordinates only parent-scope work and synchronization.

- [ ] `docs/` — not applicable
- [ ] `web/` — not applicable
- [ ] `api/` — not applicable
- [x] `agents/` — Groq adapter/port wiring, provider/model selection, `.env`-based local config, custom env-var names instead of Spring's full YAML path
- [x] `infra/` — Cloud Run env var provisioning for the Groq API key and provider/model config (Secret Manager entry + service env vars), per `CLAUDE.md`'s infra-checklist rule for changes to `agents/`
- [ ] `.planning/` — not applicable

---

## Initiator

- **Requested by:** human
- **Date:** 2026-07-11
- **Related planning (if continuation):** 001-assessment-creation (adds an alternative LLM provider to the same agent pipeline `001` built; not a continuation and does not replace it — confirmed with the human, this is additive, Gemini stays)

---

## Supersedes

> *Fill only if this planning replaces or contradicts a previous planning. Leave blank otherwise. Run `SUPERSEDE-PLANNING` workflow before creating this planning if applicable.*

*(none)*

---

## Next Step

- [ ] When dimensioned → fill `01-expansion.md` and move to `planning/active/`
- [ ] If needs clarification first → document open questions below

### Open Questions

- Per-request provider/model selection is a contract change, not just an internal wiring change: does the caller-facing field live on `AssessmentCommand` itself (making it visible to `api/`'s `agentclient`, same cross-repo notification concern as `previousDraft` in `001`), or on a transport-only wrapper at `AssessmentController` that never reaches the application layer? Decide during expansion/deepening.
- Confirmed default flips: today's default is Gemini (`app.agents.gemini.enabled`); this planning's default becomes Groq. Does Gemini remain reachable at all after this planning (opt-in per request), or only as a fallback/manual override?

---

> [← planning/README.md](../../README.md)
