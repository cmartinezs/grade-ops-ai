# US-010: Assessment Brief Intake

- **Epic:** 02 — Assessment Creation
- **Priority:** P0
- **ID:** US-010

## Story

As a programming instructor, I want to describe the learning goal, topic, level, and constraints so AI can generate an assessment draft.

## Acceptance Criteria

- [ ] Teacher can reach the assessment brief intake from the dashboard using the visible "Nueva evaluacion" / "New assessment" action; direct URL entry is not the only supported access path.
- [ ] Teacher can enter learning goal.
- [ ] Teacher can select or type programming topic only according to the approved source of truth: master-data topic/catalog when available, or a controlled custom value when explicitly allowed.
- [ ] Teacher can set level/difficulty from predefined values, not arbitrary free text.
- [ ] Teacher can set expected duration as a numeric value or predefined duration option with explicit unit.
- [ ] Teacher can specify programming language or pseudocode from a controlled list/catalog, with custom entry only if the API/domain rule allows it.
- [ ] Input is saved before agent execution.
- [ ] Every data item shown or submitted by the intake screen has an aligned `api/` contract; if an endpoint/catalog/mutation does not exist, it is implemented or recorded as a blocking residual.
- [ ] Brief creation and draft generation declare whether each step is sync or async; if async, the UI has a defined way to know completion/failure.
- [ ] Intake UI renders labels, validation messages, catalog labels and safe errors in the effective user locale; programming language selection is not treated as the natural-language locale for generated content.

---

## Definition of Done

> Precise, verifiable conditions for execution completeness. Complements AC: covers tests, migrations, agent logs, UI paths, etc.

- [ ] Intake form captures learning goal, topic, level/difficulty, expected duration, and programming language (or pseudocode) as a single brief.
- [ ] Intake implementation starts from a Design System design decision before wireframe/mockup/component work, referencing the DS pattern and components to use.
- [ ] A field matrix classifies every input as free text, constrained text, enum, master-data catalog, numeric, boolean, read-only/provenance or generated editable, including source of truth, restrictions, cardinality and UI control.
- [ ] The same field matrix maps every read/write datum to `api/`: endpoint/read model/catalog/default/capability/mutation/error shape.
- [ ] If `api/` lacks a required read/write contract for the screen, the story creates the needed `api/`/DB/infra task or stays blocked/residual; the UI does not ship with permanent local fixtures as source of truth.
- [ ] The create/generate flow documents sync/async behavior. If generation is async, the DoD includes operation polling, SSE, WebSocket, webhook server-to-server/push notification or another explicit completion mechanism.
- [ ] All fields are validated client-side via React Hook Form + Zod (no native HTML validation), matching project convention.
- [ ] Submitting the brief persists it to the database (new `AssessmentBrief`-type record) BEFORE the Assessment Agent is invoked, so a failure during agent execution does not lose the teacher's input.
- [ ] Persisted brief is retrievable after page refresh (survives navigation away before agent completes).
- [ ] Successful submission transitions the teacher into the draft-generation step (US-011), passing the persisted brief as input.
- [ ] Required-field validation prevents submission with an empty learning goal, topic, level, duration, or language.
- [ ] Unit/component tests verify that controlled fields render as controls (`Select`/`Radio`/numeric or catalog picker as applicable), not plain text inputs, and reject invalid enum/catalog/numeric values.
- [ ] Unit/component and contract tests cover API I/O mapping for read/write data and async state handling when applicable.
- [ ] i18n tests cover translation keys, locale fallback, catalog labels, safe errors, and the separation between programming `language` and generated-content `outputLocale`.
- [ ] Unit/component tests cover the dashboard action wiring and the intake form validation/submit states.
- [ ] Acceptance/e2e tests start from `/dashboard`, click the visible new-assessment action, assert navigation to `/assessments/new`, and then submit the brief; a direct visit to `/assessments/new` may be tested only as a deep-link/guard case, not as the sole happy path.
- [ ] Acceptance/e2e tests submit valid values through the actual DS controls and cover at least one invalid/restricted value path.
- [ ] Acceptance/e2e waits for draft generation through the agreed sync response or async completion mechanism; timers/local mocks are not accepted as proof.

## Technical Notes

> Repos and layers affected. Implementation hints, constraints, or known gotchas at story time.

- **Area:** `web/`, `api/`
- `web/`: Intake form built with React Hook Form + Zod (`zodResolver`) — required per project convention, never native HTML validation.
- `web/`: `/assessments/new` is the intake route, but the functional entry point is the dashboard action. The existing "Nueva evaluacion" button on `/dashboard` must navigate to this route before US-010 can be considered done.
- `web/`: UI must apply `docs/master-plan/analysis/ui-design-data-strategy.md`; the form cannot be considered complete if `learningGoal`, `topic`, `level`, `duration`, and `language` are all rendered as unrestricted text inputs.
- `web/`: Expected control semantics for R01 are `Textarea` for `learningGoal`, enum/select/radio for `level`, numeric/preset control for `duration`, selector/catalog for `language`, and topic as master-data/tag/custom-controlled according to the API/domain decision.
- `api/`: If master data or restrictive values are needed but no table/API/catalog exists yet, create the API/DB task or record an explicit residual; do not hide the gap in `web/`.
- `web`/`api`: The screen data contract must include all I/O data, not only submit payload fields: catalogs/defaults/capabilities, create brief mutation, generation trigger, operation/draft status, errors and retry/cancel affordances if applicable.
- `web`/`api`: If draft generation remains sync, document timeout/error behavior. If it becomes async, expose an operation/status contract and choose polling/SSE/WebSocket/webhook/push before implementing UI.
- `web`/`api`: Apply `docs/master-plan/analysis/i18n-strategy.md`. `web` sends/resolves effective locale; `api` validates/falls back; generated content uses `outputLocale`/`contentLocale`. DTO field names remain in English.
- `web`/`api`/`agents`: `language` in this story means programming language/pseudocode. It must not be reused as UI locale or GenAI natural-language output locale.
- `api/`: New Flyway migration for the brief entity; brief must be persisted in its own transaction/request, separate from and prior to the agent-invoking request, per epic DoD ("Input is persisted before the agent call").
- No `agents/` changes in this story — the brief is the input contract consumed by the Assessment Agent in US-011.

## Dependencies

> Other user stories or epics that must be complete before this one can be executed.

| Depends on | Reason |
|------------|--------|
| Epic 01 — Teacher Onboarding | Teacher must be authenticated and have an active workspace before creating a brief |
| US-002 — Assessment Dashboard | Dashboard must expose a working new-assessment action that reaches the intake route |

## Complexity

> **S** = 1–2 days · **M** = 3–5 days · **L** = 1–2 weeks · **XL** = should be split

**Estimate:** M *(6 acceptance criteria, 2 affected areas: web/ + api/)*
