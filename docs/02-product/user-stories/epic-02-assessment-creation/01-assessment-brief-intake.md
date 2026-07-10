# US-010: Assessment Brief Intake

- **Epic:** 02 — Assessment Creation
- **Priority:** P0
- **ID:** US-010

## Story

As a programming instructor, I want to describe the learning goal, topic, level, and constraints so AI can generate an assessment draft.

## Acceptance Criteria

- [ ] Teacher can enter learning goal.
- [ ] Teacher can select or type programming topic.
- [ ] Teacher can set level/difficulty.
- [ ] Teacher can set expected duration.
- [ ] Teacher can specify programming language or pseudocode.
- [ ] Input is saved before agent execution.

---

## Definition of Done

> Precise, verifiable conditions for execution completeness. Complements AC: covers tests, migrations, agent logs, UI paths, etc.

- [ ] Intake form captures learning goal, topic, level/difficulty, expected duration, and programming language (or pseudocode) as a single brief.
- [ ] All fields are validated client-side via React Hook Form + Zod (no native HTML validation), matching project convention.
- [ ] Submitting the brief persists it to the database (new `AssessmentBrief`-type record) BEFORE the Assessment Agent is invoked, so a failure during agent execution does not lose the teacher's input.
- [ ] Persisted brief is retrievable after page refresh (survives navigation away before agent completes).
- [ ] Successful submission transitions the teacher into the draft-generation step (US-011), passing the persisted brief as input.
- [ ] Required-field validation prevents submission with an empty learning goal, topic, level, duration, or language.

## Technical Notes

> Repos and layers affected. Implementation hints, constraints, or known gotchas at story time.

- **Area:** `web/`, `api/`
- `web/`: Intake form built with React Hook Form + Zod (`zodResolver`) — required per project convention, never native HTML validation.
- `api/`: New Flyway migration for the brief entity; brief must be persisted in its own transaction/request, separate from and prior to the agent-invoking request, per epic DoD ("Input is persisted before the agent call").
- No `agents/` changes in this story — the brief is the input contract consumed by the Assessment Agent in US-011.

## Dependencies

> Other user stories or epics that must be complete before this one can be executed.

| Depends on | Reason |
|------------|--------|
| Epic 01 — Teacher Onboarding | Teacher must be authenticated and have an active workspace before creating a brief |

## Complexity

> **S** = 1–2 days · **M** = 3–5 days · **L** = 1–2 weeks · **XL** = should be split

**Estimate:** M *(6 acceptance criteria, 2 affected areas: web/ + api/)*
