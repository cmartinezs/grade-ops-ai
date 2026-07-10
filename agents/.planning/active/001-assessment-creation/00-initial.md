# 🌱 INITIAL: 001-assessment-creation

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

> *What needs to be done, in one sentence.*

Implement the Assessment Agent following the project's fixed agent pipeline pattern, generating a structured assessment draft from a teacher's brief and supporting regeneration with adjustment notes — the `agents/` half of the assessment-creation pipeline (US-011, US-012).

---

## Why

> *Why does this planning exist? What problem does it solve or what value does it deliver?*

Enables the first step of the open-assessment pipeline: turning a teacher's learning intent into a structured, AI-generated assessment draft that is editable and fully logged. This child planning owns the `agents/` implementation split out of the parent monorepo planning `008-assessment-creation` (see Initiator below) — the Assessment Agent contract, prompt, and pipeline that `api/` consumes.

---

## Approximate Scope

> *Which repositories or areas might be affected? This does not need to be exhaustive.*
> In a monorepo, if a child artifact has its own `.planning/`, list it here but keep its implementation in a child planning created inside that artifact. The parent planning coordinates only parent-scope work and synchronization.

- [ ] `src/` — `AssessmentCommand`/`AssessmentResult` contract, `assessment-generation.st` prompt template, `AssessmentAgentService` pipeline, structured-output validation, internal endpoint for `api/` to call

---

## Initiator

- **Requested by:** AI agent
- **Date:** 2026-07-09
- **Related planning (if continuation):** Split out of the parent monorepo planning `008-assessment-creation` (root `.planning/active/008-assessment-creation/`) per the monorepo parent/child coordination rule — `agents/` did not previously have its own `.planning/`; it was initialized via `/plan-init` as part of this correction. The parent planning tracks this child planning's status under `01-expansion.md → Linked Child Plannings` and keeps only a coordination story. See also the sibling child planning `api/.planning/003-assessment-creation`.

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
