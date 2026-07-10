# 🌱 INITIAL: 008-assessment-creation

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

> *What needs to be done, in one sentence.*

Enable the first step of the open-assessment pipeline: turning a teacher's learning intent into a structured, AI-generated assessment draft that is editable and fully logged.

---

## Why

> *Why does this planning exist? What problem does it solve or what value does it deliver?*

As a programming instructor, the teacher wants to describe a learning goal and constraints and have the AI generate a structured assessment draft, so they spend their time reviewing and refining rather than writing from scratch.

---

## Approximate Scope

> *Which repositories or areas might be affected? This does not need to be exhaustive.*

- [ ] `docs/` — Epic 02 user stories (US-010 Assessment Brief Intake, US-011 Assessment Draft Generation, US-012 Assessment Draft Regeneration)
- [ ] `web/` — Intake form (learning goal, topic, level/difficulty, duration, programming language) and draft editing UI
- [ ] `api/` — Persistence of brief before agent call, draft storage/retrieval, `AgentExecutionLog` records
- [ ] `agents/` — Assessment Agent execution producing structured draft (title, context, instructions, objectives, deliverables, constraints); regeneration with adjustment notes (P1)
- [ ] `infra/` — Verify Terraform resources exist for any new/expanded api/agents services touched by this story
- [ ] `.planning/` — none

---

## Initiator

- **Requested by:** human
- **Date:** 2026-07-09
- **Related planning (if continuation):** none

---

## Next Step

- [ ] When dimensioned → fill `01-expansion.md` and move to `planning/active/`
- [ ] If needs clarification first → document open questions below

### Open Questions

*None yet.*

---

> [← planning/README.md](../../README.md)
