# 🔍 DEEPENING: Story 02 — assessment-screens-wireframes-and-data-providers

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Design and build, per screen, the full pipeline defined by `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §2 (objetivo de usuario → flujo → estados → wireframe → jerarquía de componentes → maqueta funcional con datos fake → validar copy → conectar API real) for the two screens that make up Story 01's scope:

1. **Intake screen** (US-010) — brief form that kicks off draft generation.
2. **Draft Builder screen** (US-011 + US-012) — a single screen with the draft editor, the regenerate action, and the version history, since the Done Criteria in `story-01` require regeneration and version history to happen "from the draft view," not as separate routes.

This story delivers the wireframes, the navigable fake-data mockups, the DTOs/view models/Screen Data Facade that talk to `api/`'s real endpoints, and the final wiring between both screens. Story 01 then builds on top of this: its component/hook implementation and test tasks consume the wireframes, mockups, and data providers delivered here instead of starting from an unspecified screen shape.

**Source stories:** `docs/02-product/user-stories/epic-02-assessment-creation/01-assessment-brief-intake.md` (US-010), `02-assessment-draft-generation.md` (US-011), `03-assessment-draft-regeneration.md` (US-012).

---

## Context

- **Real endpoint contracts** (verified directly against `api/src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/AssessmentController.java`, not against `docs/04-architecture/api-design.md` alone — already found stale once):
  - `POST /api/v1/assessments` — `CreateAssessmentBriefRequest{learningGoal, topic, level, duration, language}` → `CreateAssessmentBriefResponse{assessmentId}`
  - `POST /api/v1/assessments/{id}/draft` — no body → `GenerateAssessmentDraftResponse{draftId, title, context, instructions, objectives[], deliverables[], constraints[], versionNumber}`
  - `POST /api/v1/assessments/{id}/draft/regenerate` — `RegenerateAssessmentDraftRequest{adjustmentNotes}` → `GenerateAssessmentDraftResponse`
  - `PATCH /api/v1/assessments/{id}/draft` — `UpdateAssessmentDraftRequest{title?, context?, instructions?, objectives?[], deliverables?[], constraints?[]}` (partial update, `null` fields are left unchanged) → `GenerateAssessmentDraftResponse`
  - `GET /api/v1/assessments/{id}/draft` — current draft → `GenerateAssessmentDraftResponse`
  - `GET /api/v1/assessments/{id}/draft/versions` — `GenerateAssessmentDraftResponse[]` (all versions, newest included)
  - There is **no endpoint to restore a past version as current** — version history in this MVP is read-only browsing of past drafts, not a rollback action. Don't design a "restore this version" affordance; it isn't backed by an API contract.
- **Screen Data Facade rule** (`docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md` §7): a screen with 2+ remote sources needs a loader/facade, not direct calls from the Page/TSX.
  - Draft Builder screen loads *current draft* + *version list* together → needs `loadAssessmentDraftBuilderPage(assessmentId)`.
  - Intake screen has no GET on load (blank form) — its two API calls are a sequential **mutation** (create brief, then generate draft), not a page-load facade; it's covered by §10 Mutaciones, orchestrated in `lib/api`/a feature-level submit function, not inline in the Page.
- Every form uses React Hook Form + Zod (`zodResolver`) — never native HTML validation.
- TSX is mandatory for every React file, including the fake-data mockups (`docs/gradeops-ai-frontend-guidelines/01-arquitectura-next-react.md` §2) — no `.jsx` prototypes checked into `src/`.
- Fake data for the mockups must cover real edge cases, not symmetric happy-path mocks (`02-ux-wireframes-y-maquetas.md` §6): long AI-generated instructions/objectives text, accented names, zero prior versions, many versions, a partial error.
- Gemini/Groq API keys are never touched by `web/` — only `agents/` calls the LLM providers; `web/` only calls `api/` endpoints.
- **API-Agent Orchestration gate:** tasks that verify contracts, define `lib/api` functions, connect real screens, or validate the end-to-end route must apply `docs/master-plan/analysis/api-agent-orchestration-strategy.md`. `web/` consumes functional `api/` routes only; it must not know `agents/` URLs, providers or prompts. Endpoint-facing tasks must also record a Richardson REST maturity check for resource URI, HTTP method, status/error handling, idempotency expectations and route states.

---

## Master Plan Addendum — i18n, UI Data Semantics and Testing

Added during the `planning/master-plan` merge from `develop` on 2026-07-22. This story was already `DONE` in `develop`; the addendum does not rewrite historic task evidence. It records the release-readiness gates that must be revalidated or converted into follow-up tasks before R01 is considered complete under the updated Master Plan:

- UI entry paths must be tested from visible actions, especially `/dashboard` "Nueva evaluacion" -> `/assessments/new`; direct URL access alone is not accepted as the happy path.
- Intake and Draft Builder screens must keep DS-first field/control semantics: long text, enum/catalog, numeric/preset, read-only provenance and generated editable content cannot all collapse into unrestricted text inputs.
- All screen read/write data, catalog labels, safe errors and operation states must be backed by `api/` contracts or explicit residuals.
- Generation/regeneration must declare sync vs async behavior; async flows require an API-backed completion/progress mechanism.
- i18n applies beyond static labels: UI copy, validation, safe errors, catalog labels and generated draft/regeneration output must respect effective locale/`outputLocale`; source code, DTO fields, status/error codes, logs and telemetry remain in English.
- Existing `DONE` evidence predates the full i18n strategy unless a task explicitly proves locale/fallback/output-language behavior. Missing coverage is a release-readiness residual, not evidence that i18n is already implemented.

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Fake-data mockup ships without covering the edge cases in `02-ux-wireframes-y-maquetas.md` §6 (long text, zero/many versions), and those gaps surface only after connecting the real API | M | M | Explicit fake-data checklist per screen before "conectar API real" tasks start |
| Version-history UI implies a "restore version" action that the API doesn't support | M | L | Context above states explicitly: version history is read-only browsing in this MVP |
| Draft Builder screen's loader duplicates calls already made in a shared hook, defeating the Screen Data Facade's purpose | L | L | Single `loadAssessmentDraftBuilderPage` per Screen Data Facade rule; no ad hoc `getX()` calls from the Page/TSX |
| New API orchestration rules are only present in templates and are missed by already-atomized tasks | M | M | Endpoint-facing tasks in this story must include the API / Agent / Web Contract Gate before implementation/review |
| Story was completed before the full Master Plan i18n/UI data/testing gates existed | H | M | Treat this addendum as a R01 revalidation checklist; open follow-up tasks for gaps instead of marking i18n/API I/O/DS semantics as implicitly satisfied |

---

## Tasks

> Atomized via `/plan-atomize` on 2026-07-15. Each row links to its task file under `story-02-assessment-screens-wireframes-and-data-providers/`.

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [verify-api-contract](story-02-assessment-screens-wireframes-and-data-providers/task-01-verify-api-contract.md) | GENERATE-DOCUMENT | DONE | Confirmed request/response shapes for all 6 endpoints, verified against `AssessmentController.java` |
| 2 | [wireframe-intake-screen](story-02-assessment-screens-wireframes-and-data-providers/task-02-wireframe-intake-screen.md) | GENERATE-DOCUMENT | DONE | Wireframe textual + estados documentados |
| 3 | [component-hierarchy-intake-screen](story-02-assessment-screens-wireframes-and-data-providers/task-03-component-hierarchy-intake-screen.md) | GENERATE-DOCUMENT | DONE | Jerarquía de componentes documentada |
| 14 | [design-system-form-primitives](story-02-assessment-screens-wireframes-and-data-providers/task-14-design-system-form-primitives.md) | GENERATE-DOCUMENT | DONE | **Inserted out of numeric order — must run before task-04/task-09.** `Form`/`Field`/`Input`/`Textarea`/`Select`/`Checkbox`/`DynamicForm` per `pdr-001-design-system-form-primitives.md` |
| 4 | [functional-mockup-intake-screen](story-02-assessment-screens-wireframes-and-data-providers/task-04-functional-mockup-intake-screen.md) | GENERATE-DOCUMENT | DONE | Maqueta funcional navegable (TSX, datos fake) — depends on task-14 |
| 5 | [data-provider-intake-screen](story-02-assessment-screens-wireframes-and-data-providers/task-05-data-provider-intake-screen.md) | GENERATE-DOCUMENT | DONE | DTOs + `submitAssessmentBrief` en `lib/api` |
| 6 | [connect-real-api-intake-screen](story-02-assessment-screens-wireframes-and-data-providers/task-06-connect-real-api-intake-screen.md) | GENERATE-DOCUMENT | DONE | Intake screen conectada a `api/` real |
| 7 | [wireframe-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-07-wireframe-draft-builder-screen.md) | GENERATE-DOCUMENT | DONE | Wireframe textual + estados documentados |
| 8 | [component-hierarchy-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-08-component-hierarchy-draft-builder-screen.md) | GENERATE-DOCUMENT | DONE | Jerarquía de componentes documentada |
| 9 | [functional-mockup-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-09-functional-mockup-draft-builder-screen.md) | GENERATE-DOCUMENT | DONE | Maqueta funcional navegable (TSX, datos fake) — depends on task-14 |
| 10 | [data-provider-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-10-data-provider-draft-builder-screen.md) | GENERATE-DOCUMENT | DONE | DTO + `loadAssessmentDraftBuilderPage` Screen Data Facade |
| 11 | [mutations-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-11-mutations-draft-builder-screen.md) | GENERATE-DOCUMENT | DONE | `updateAssessmentDraft` + `regenerateAssessmentDraft` en `lib/api` |
| 12 | [connect-real-api-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-12-connect-real-api-draft-builder-screen.md) | GENERATE-DOCUMENT | DONE | Draft Builder screen conectada a `api/` real |
| 13 | [end-to-end-connection](story-02-assessment-screens-wireframes-and-data-providers/task-13-end-to-end-connection.md) | GENERATE-DOCUMENT | DONE | Flujo completo verificado sin datos fake residuales |
| 15 | [deterministic-e2e-suite](story-02-assessment-screens-wireframes-and-data-providers/task-15-deterministic-e2e-suite.md) | GENERATE-DOCUMENT | DONE | **Inserted out of numeric order — added after task-13 closed.** Suite Playwright determinística + script de orquestación que reproduce el walkthrough de task-13 sin intervención manual |

---

## Done Criteria

- [ ] Wireframe textual de baja fidelidad documentado para Intake screen y Draft Builder screen.
- [ ] Maqueta funcional navegable con datos fake existe para ambas pantallas, cubriendo estados loading/empty/error y casos límite (texto largo, muchas versiones, cero versiones), antes de conectar el backend.
- [ ] DTOs de `lib/api` reflejan exactamente los tipos reales de `AssessmentController` — sin campos inventados.
- [ ] Draft Builder screen usa un Screen Data Facade (`loadAssessmentDraftBuilderPage`) porque combina 2+ fuentes remotas (draft actual + versiones).
- [ ] Intake screen orquesta sus 2 llamadas secuenciales (crear brief → generar draft) fuera del Page/TSX, con estados de mutación explícitos.
- [ ] API real conectada en ambas pantallas; mocks o datos fake removidos o aislados explícitamente.
- [ ] Errores 401/403/404/409/422/500 traducidos a mensajes de usuario, no mostrados crudos.
- [ ] Endpoint-facing tasks complete the API / Agent / Web Contract Gate, including Richardson REST maturity, idempotency expectations and functional route states.
- [ ] `npm run test` y `npm run lint` pasan.
- [ ] TRACEABILITY.md actualizado.

---

## Inconsistencies Found

*Record any contradictions or gaps detected during this story. Use RECORD-INCONSISTENCY workflow.*

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | `task-01` (verify-api-contract, `DONE`/merged) only transcribed success-path request/response shapes. Tracing `task-02`'s two endpoints' actual exception paths in `api/` found a much richer error surface than the generic 401/403/404/409/422/500 taxonomy in `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md` §9: 422 has two distinct body shapes (`List<FieldErrorResponse>` for Bean Validation vs. `ApiErrorResponse{error, message}` for everything else), and draft generation can return 502/503 when `agents/` is down. | `task-01-verify-api-contract.md`, `task-02-wireframe-intake-screen.md`, `06-estado-datos-y-api.md` §9 | DONE | Not reopening `task-01` (its narrower success-shape scope is already satisfied). Each remaining task that implements error handling (`task-05`, `task-06`, and `task-10`/`task-11`/`task-12` for the Draft Builder screen's other 4 endpoints) must trace its own endpoints' real exception paths in `api/` source the same way, rather than assuming the generic taxonomy. |

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| R-POST-01 | Post-closeout R01 revalidation required for i18n, UI data semantics, API I/O, sync/async completion and UI-action reachability under the updated Master Plan. | R01 release readiness / follow-up planning | Open |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
