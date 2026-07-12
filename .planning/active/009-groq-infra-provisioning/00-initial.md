# 🌱 INITIAL: 009-groq-infra-provisioning

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

Provision the Groq API key and its Cloud Run wiring as real Terraform infra for the `agents/` service, so `agents/`'s already-implemented Groq provider adapter (see `agents/.planning/active/002-groq-genai-provider`, story-01, DONE) can run in `demo` with real credentials instead of only local `.env` values.

---

## Why

`agents/`'s planning `002-groq-genai-provider` (story-01) added a Groq `AssessmentGenerationPort` adapter and made it the default provider, config-driven via project-specific `GRADEOPS_*` env vars. That planning originally scoped the matching infra work as its own story-02, living inside `agents/.planning/`. `infra/` has no `.planning/` workspace of its own (verified 2026-07-12: no `infra/.planning/` in this worktree), and per this project's convention, infra-only work without a dedicated child workspace is handled directly by the parent (root) planning rather than living inside a sibling child's planning tree. This planning is that handoff: it owns the actual Terraform implementation directly, since `infra/` doesn't have a child workspace to delegate to.

---

## Approximate Scope

- [ ] `docs/` — none
- [ ] `web/` — none
- [ ] `api/` — none
- [ ] `agents/` — none (already DONE in `002-groq-genai-provider`; this planning only consumes its final env-var names/shape)
- [x] `infra/` — Secret Manager entry for the Groq API key (`demo` environment) and the corresponding Cloud Run env var/secret binding for the existing `agents/` service. No new Cloud Run service, Artifact Registry repo, or Vertex AI IAM binding — the `agents/` service already exists and Groq is not a Google Cloud API.
- [ ] `.planning/` — this planning itself, plus a coordination note in `agents/.planning/active/002-groq-genai-provider` pointing here.

---

## Initiator

- **Requested by:** human
- **Date:** 2026-07-12
- **Related planning (if continuation):** `002-groq-genai-provider` (in the `agents/` worktree) — story-02 there is superseded by this planning; see that story file for the handoff note.

---

## Supersedes

*(none — this is a scope relocation from `agents/002-groq-genai-provider`'s story-02, not a supersession of a completed planning)*

---

## Next Step

- [x] When dimensioned → fill `01-expansion.md` and move to `planning/active/`
- [ ] If needs clarification first → document open questions below

### Open Questions

*None — story-02's original design (Objective, Risk, Tasks, Done Criteria) was already fully specified before the relocation; carried forward as-is into `01-expansion.md`/`02-deepening/`.*

---

> [← planning/README.md](../../README.md)
