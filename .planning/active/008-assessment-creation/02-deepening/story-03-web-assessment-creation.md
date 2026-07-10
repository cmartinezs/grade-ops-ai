# 🔍 DEEPENING: Story 03 — web-assessment-creation

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Build the teacher-facing UI for assessment creation in `web/`: the brief intake form (US-010), the editable draft view fed by the Assessment Agent's output (US-011), and the regeneration action with adjustment notes plus access to previous versions (US-012).

**Source stories:** `docs/02-product/user-stories/epic-02-assessment-creation/01-assessment-brief-intake.md`, `02-assessment-draft-generation.md`, `03-assessment-draft-regeneration.md`.

---

## Context

- **Depends on Story 02** — all screens integrate against the real `api/` endpoints (brief intake, draft generation, regeneration, edit, retrieval).
- Every form in `web/` uses React Hook Form + Zod (`zodResolver`) — never native HTML validation. This is an established project convention, not new for this story.
- Types mirror the API DTO contracts — no independent shared-type definitions in `web/`.
- Gemini API key is never touched by `web/` — the frontend only calls `api/` endpoints.

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Draft/version-history UI ships ahead of the real API contract from Story 02, causing rework | M | L | Story 03 depends on Story 02; do not start UI integration tasks until Story 02's endpoints are done |

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | Intake form (learning goal, topic, level/difficulty, expected duration, programming language/pseudocode) with RHF + Zod | GENERATE-DOCUMENT | TODO | Intake form component + Zod schema |
| 2 | Submit handler: persists the brief via the API before triggering draft generation; loading and error states | GENERATE-DOCUMENT | TODO | Submit handler + API client function |
| 3 | Draft view/edit screen: editable fields for title, context, instructions, objectives, deliverables, constraints | GENERATE-DOCUMENT | TODO | Draft edit component |
| 4 | Regenerate action: adjustment-notes input + trigger regeneration endpoint, with its own loading/error state | GENERATE-DOCUMENT | TODO | Regenerate action component |
| 5 | Version history view: list/switch between previous draft versions after one or more regenerations | GENERATE-DOCUMENT | TODO | Version history component |
| 6 | Component/unit tests: form validation (required fields), draft edit persistence, regenerate flow | GENERATE-DOCUMENT | TODO | Test files |

---

## Done Criteria

- [ ] Teacher can fill in and submit the intake form; required-field validation blocks submission with missing learning goal, topic, level, duration, or language.
- [ ] Submitting the brief persists it via the API before any agent call is triggered, and the teacher sees a clear loading/confirmation state.
- [ ] Generated draft is rendered fully editable (all six fields) and edits persist via the API.
- [ ] Teacher can trigger regeneration with adjustment notes from the draft view.
- [ ] Previous draft version(s) remain visible/accessible after a regeneration — nothing is silently lost.
- [ ] Draft (and its versions) are retrievable after a page refresh.
- [ ] All forms use React Hook Form + Zod exclusively — no native HTML validation.
- [ ] `npm run test` and `npm run lint` pass.
- [ ] TRACEABILITY.md updated.

---

## Inconsistencies Found

*Record any contradictions or gaps detected during this story. Use RECORD-INCONSISTENCY workflow.*

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
