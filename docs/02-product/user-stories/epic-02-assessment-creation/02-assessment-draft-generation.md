# US-011: Assessment Draft Generation

- **Epic:** 02 — Assessment Creation
- **Priority:** P0
- **ID:** US-011

## Story

As a teacher, I want an assessment draft generated from my brief so I can start faster.

## Acceptance Criteria

- [ ] Assessment Agent generates structured output.
- [ ] Output includes title, context, instructions, objectives, expected deliverables, and constraints.
- [ ] Output is editable.
- [ ] Agent execution is logged.
- [ ] Model and cost estimate are stored.

---

## Definition of Done

> Precise, verifiable conditions for execution completeness. Complements AC: covers tests, migrations, agent logs, UI paths, etc.

- [ ] Submitting a persisted brief (US-010) triggers Assessment Agent execution through the `api/` → `agents/` `agentclient` module (service-to-service OIDC call).
- [ ] Assessment Agent returns a structured draft containing all required fields: title, context, instructions, objectives, deliverables, and constraints.
- [ ] Agent output is validated against the expected structured-output schema before being persisted or shown to the teacher.
- [ ] Draft is persisted in `api/` and retrievable after a page refresh.
- [ ] Draft is rendered in an editable UI in `web/`, allowing the teacher to modify any field before proceeding to rubric generation.
- [ ] Every agent execution produces an `AgentExecutionLog` record capturing model name, cost estimate, status, and timestamps.
- [ ] Agent invocation happens server-side only — the Gemini API key is never exposed to the frontend.

## Technical Notes

> Repos and layers affected. Implementation hints, constraints, or known gotchas at story time.

- **Area:** `web/`, `api/`, `agents/`
- `agents/`: Assessment Agent follows the fixed pipeline pattern (validate command → load data → build envelope → call Gemini → validate structured output → log execution → return result). Prompt lives as a versioned `.st` template in `agents/src/main/resources/prompts/`, never inlined in Java.
- `api/`: Builds `AssessmentCommand` from the persisted brief, calls `agents/` via the `agentclient` module (no other module imports Spring AI directly). Persists the returned draft and the `AgentExecutionLog`.
- `web/`: Editable draft form (title, context, instructions, objectives, deliverables, constraints); draft remains mutable until the teacher proceeds to rubric generation.

## Dependencies

> Other user stories or epics that must be complete before this one can be executed.

| Depends on | Reason |
|------------|--------|
| US-010 — Assessment Brief Intake | Draft generation consumes the persisted brief as its input |

## Complexity

> **S** = 1–2 days · **M** = 3–5 days · **L** = 1–2 weeks · **XL** = should be split

**Estimate:** L *(3 affected areas: web/ + api/ + agents/ triggers L per the estimation rule, even though criteria count alone would suggest M)*
