# 🌱 INITIAL: 003-assessment-creation

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

> *What needs to be done, in one sentence.*

Persist the teacher's assessment brief before any agent call, integrate with the Assessment Agent via the `agentclient` module to generate a structured draft, and support draft regeneration as a new, non-destructive version — the `api/` half of the assessment-creation pipeline (US-010, US-011, US-012).

---

## Why

> *Why does this planning exist? What problem does it solve or what value does it deliver?*

Enables the first step of the open-assessment pipeline: turning a teacher's learning intent into a structured, AI-generated assessment draft that is editable and fully logged. This child planning owns the `api/` implementation split out of the parent monorepo planning `008-assessment-creation` (see Initiator below) — brief persistence, draft persistence/versioning, and the call into `agents/`.

---

## Approximate Scope

- [ ] `src/` — Flyway migrations + entities/repositories for `AssessmentBrief` and versioned `AssessmentDraft`; brief intake endpoint; draft generation endpoint (calls `agents/` via `agentclient`); draft regeneration endpoint; draft edit endpoint; draft/version retrieval endpoints; `AgentExecutionLog` persistence per agent execution
- [ ] `docs/` — none expected (source stories already enriched in the root docs repo)

---

## Initiator

- **Requested by:** AI agent
- **Date:** 2026-07-09
- **Related planning (if continuation):** Split out of the parent monorepo planning `008-assessment-creation` (root `.planning/active/008-assessment-creation/`) per the monorepo parent/child coordination rule — `api/` has its own `.planning/` workspace, so its implementation must live here, not in the parent. The parent planning tracks this child planning's status under `01-expansion.md → Linked Child Plannings` and keeps only a coordination story.

---

## Supersedes

> *Fill only if this planning replaces or contradicts a previous planning. Leave blank otherwise. Run `SUPERSEDE-PLANNING` workflow before creating this planning if applicable.*

*(none)*

---

## Next Step

- [x] When dimensioned → fill `01-expansion.md` and move to `planning/active/`
- [ ] If needs clarification first → document open questions below

### Open Questions

*None yet.*

---

> [← planning/README.md](../../README.md)
