# 🌱 INITIAL: 001-assessment-creation

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

Build the teacher-facing UI for assessment creation: the brief intake form, the editable AI-generated draft view, and the regeneration flow with version history — the `web/` half of the assessment-creation pipeline (US-010, US-011, US-012).

---

## Why

As a programming instructor, the teacher needs a real screen to describe a learning goal, see the AI-generated draft, edit it, and regenerate it with adjustment notes if it's not right — the pipeline's backend and agent halves (`agents/`, `api/`) are already `DONE`/`IN PROGRESS`, but none of it is usable by an actual teacher without this UI. This child planning owns the `web/` implementation split out of the parent monorepo planning `008-assessment-creation` (see Initiator below) — the same coordination pattern already used for `agents/.planning/001-assessment-creation` and `api/.planning/003-assessment-creation`.

---

## Approximate Scope

- [ ] `src/` — intake form (RHF + Zod), submit handler wired to `api/`'s brief-intake endpoint, draft view/edit screen, regenerate action with adjustment notes, version history view, component/unit tests

---

## Initiator

- **Requested by:** human
- **Date:** 2026-07-14
- **Related planning (if continuation):** Split out of the parent monorepo planning `008-assessment-creation` (root `.planning/active/008-assessment-creation/`) per the monorepo parent/child coordination rule — `web/` did not previously have its own `.planning/`; it was initialized via `/plan-init` as part of this correction, matching the same pattern already applied to `agents/` and `api/` on 2026-07-09. The parent planning tracks this child planning's status under `01-expansion.md → Linked Child Plannings` and keeps only a coordination story (Story 03). See also the sibling child plannings `agents/.planning/001-assessment-creation` (`DONE`) and `api/.planning/003-assessment-creation` (`DONE`).

---

## Supersedes

*(none)*

---

## Next Step

- [x] When dimensioned → fill `01-expansion.md` and move to `planning/active/`
- [ ] If needs clarification first → document open questions below

### Open Questions

*None yet — the full Objective, Context, Risk, Tasks, and Done Criteria were already fully specified as the root planning's Story 03 before this split; carried forward as-is into `01-expansion.md`/`02-deepening/`.*

---

> [← planning/README.md](../../README.md)
