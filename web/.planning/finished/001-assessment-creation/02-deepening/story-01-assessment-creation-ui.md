# 🔍 DEEPENING: Story 01 — assessment-creation-ui

> **Status:** SKIPPED
> **Skipped reason:** Scope fully absorbed by story-02 after atomization: story-02's functional-mockup and connect-real-api tasks (task-04, 06, 09, 10, 11, 12) already build the real components and wire them to the real API end-to-end, and task-13 explicitly re-verifies story-01's Done Criteria. Story-01 was never atomized/started, so nothing is lost by skipping it. (2026-07-15)
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Build the teacher-facing UI for assessment creation in `web/`: the brief intake form (US-010), the editable draft view fed by the Assessment Agent's output (US-011), and the regeneration action with adjustment notes plus access to previous versions (US-012).

**Source:** moved from the parent monorepo planning `008-assessment-creation`'s Story 03 (`web-assessment-creation`) — content carried forward unchanged as part of splitting `web/`'s implementation into its own child planning, matching the pattern already applied to `agents/` and `api/`. See that story file for the handoff note and `01-expansion.md`'s Notes for why.

**Source stories:** `docs/02-product/user-stories/epic-02-assessment-creation/01-assessment-brief-intake.md`, `02-assessment-draft-generation.md`, `03-assessment-draft-regeneration.md`.

---

## Context

- **Depends on Story 02 (`assessment-screens-wireframes-and-data-providers`)** — Story 02 delivers the wireframes, fake-data mockups, DTOs/view models, and the Screen Data Facade/mutation functions for both screens (Intake, Draft Builder), plus the real-API wiring. This story's component/hook implementation and test tasks build on that output rather than starting from an unspecified screen shape.
- **Depends on `api/003-assessment-creation`** (sibling child planning, already `DONE`) — all screens integrate against the real `api/` endpoints (brief intake, draft generation, regeneration, edit, retrieval). Request/response shapes are verified directly against `api/`'s actual controller/DTO source (`api/src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/AssessmentController.java` and its request/response types) as part of Story 02 — `docs/04-architecture/api-design.md` was found stale relative to `api/`'s real implementation during that planning's own execution, more than once.
- Every form in `web/` uses React Hook Form + Zod (`zodResolver`) — never native HTML validation. This is an established project convention, not new for this story.
- Types mirror the API DTO contracts — no independent shared-type definitions in `web/`.
- Gemini/Groq API key is never touched by `web/` — the frontend only calls `api/` endpoints.
- Story 02 now owns UI Design/Data Semantics for this skipped story's absorbed scope: intake implementation must start from Design System design plus field matrix, and must not implement `topic`, `level`, `duration` and `language` as unrestricted text inputs when they are enum, numeric, catalog/master-data or controlled-custom values.
- Story 02 now owns API I/O + sync/async for this skipped story's absorbed scope: every screen datum read/written by Intake and Draft Builder must come from `api/`; missing API support becomes child API scope or blocking residual; async generation/regeneration must expose completion/progress/failure through the agreed API-backed mechanism.
- Story 02 now owns i18n for this skipped story's absorbed scope: user-facing copy/safe errors/catalog labels must use effective locale; generated drafts/regenerations use `outputLocale`; source code/DTO fields/status/error codes/logs/telemetry remain in English.

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Draft/version-history UI is built against a stale understanding of `api/`'s response shapes | M | L | `api/003-assessment-creation` is already `DONE` and merged — verify request/response shapes directly against `api/`'s actual source before implementing |
| Intake form loses domain semantics by rendering every field as text input | H | H | Story 02 task-01/task-02/task-04/task-05/task-06/task-13 now carry UI Design/Data Semantics, field matrix, DS controls and invalid-value tests |
| UI closes against fixtures/local DTOs or async timers while `api/` lacks required I/O/status support | H | M | Story 02 task-01/task-05/task-06/task-10/task-11/task-12/task-13 now require API I/O mapping and sync/async completion before Done |
| UI/API/Agents close with inconsistent locale handling | H | M | Story 02 task-01 through task-13 must carry i18n contract, translation keys, safe errors/catalog labels, `outputLocale` and logs/telemetry in English |

---

## Tasks

> Atomize via `/plan-atomize` before execution begins.

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
- [ ] Intake form uses DS controls and field semantics from Story 02, not unrestricted text inputs for all fields.
- [ ] Submitting the brief persists it via the API before any agent call is triggered, and the teacher sees a clear loading/confirmation state.
- [ ] Every screen datum shown or submitted is backed by `api/`; missing endpoints/read models/catalogs/mutations/operation states are implemented by the owning child scope or recorded as blocking residuals.
- [ ] Brief creation, draft generation, save and regeneration declare sync/async behavior; async completion/progress/failure is observed through the agreed API-backed mechanism.
- [ ] User-facing copy, safe errors, catalog labels and generated draft/regeneration output respect effective locale/`outputLocale`; code/contracts/logs/telemetry remain in English.
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
