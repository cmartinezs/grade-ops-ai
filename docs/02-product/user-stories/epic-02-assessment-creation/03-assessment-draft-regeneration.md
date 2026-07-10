# US-012: Assessment Draft Regeneration

- **Epic:** 02 — Assessment Creation
- **Priority:** P1
- **ID:** US-012

## Story

As a teacher, I want to regenerate the draft with additional instructions so I can improve quality without starting over.

## Acceptance Criteria

- [ ] Teacher can request regeneration.
- [ ] Teacher can provide adjustment notes.
- [ ] Previous version remains traceable.
- [ ] New agent run is logged.

---

## Definition of Done

> Precise, verifiable conditions for execution completeness. Complements AC: covers tests, migrations, agent logs, UI paths, etc.

- [ ] Teacher can trigger regeneration from the draft view, providing free-text adjustment notes.
- [ ] Regeneration re-invokes the Assessment Agent via the `agentclient` module, passing the original brief plus the adjustment notes as additional input.
- [ ] The new draft is persisted as a new version linked to the same assessment, without overwriting or deleting the previous version.
- [ ] Previous version(s) remain retrievable/traceable (e.g. via version history) after regeneration.
- [ ] Each regeneration produces its own `AgentExecutionLog` record (model, cost estimate, status), distinct from the original generation's log.
- [ ] The current version's fields (title, context, instructions, objectives, deliverables, constraints) remain editable, consistent with US-011.

## Technical Notes

> Repos and layers affected. Implementation hints, constraints, or known gotchas at story time.

- **Area:** `web/`, `api/`, `agents/`
- `web/`: Regenerate action with an adjustment-notes input (React Hook Form + Zod, per project convention); UI to view/switch between draft versions.
- `api/`: Versioning model for the draft (new version row per regeneration, previous retained); a new `AgentExecutionLog` per regeneration call.
- `agents/`: Assessment Agent invoked again with the same command shape as US-011, extended with the adjustment-notes field.

## Dependencies

> Other user stories or epics that must be complete before this one can be executed.

| Depends on | Reason |
|------------|--------|
| US-011 — Assessment Draft Generation | Regeneration operates on an already-generated draft |

## Complexity

> **S** = 1–2 days · **M** = 3–5 days · **L** = 1–2 weeks · **XL** = should be split

**Estimate:** L *(3 affected areas: web/ + api/ + agents/ triggers L per the estimation rule)*
