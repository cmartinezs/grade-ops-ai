# 🔍 DEEPENING: Story 02 — assessment-screens-wireframes-and-data-providers

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Design and build, per screen, the full pipeline defined by `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §2 (objetivo de usuario → flujo → estados → wireframe → jerarquía de componentes → maqueta funcional con datos fake → validar copy → conectar API real) for the two screens that make up Story 01's scope:

1. **Intake screen** (US-010) — brief form that kicks off draft generation.
2. **Draft Builder screen** (US-011 + US-012) — a single screen with the draft editor, the regenerate action, and the version history, since the Done Criteria in `story-01` require regeneration and version history to happen "from the draft view," not as separate routes.

This story delivers the wireframes, the navigable fake-data mockups, the DTOs/view models/Screen Data Facade that talk to `api/`'s real endpoints, and the final wiring between both screens. Story 01 then builds on top of this: its component/hook implementation and test tasks consume the wireframes, mockups, and data providers delivered here instead of starting from an unspecified screen shape.

The Intake screen must be reachable from the product UI, not only by typing `/assessments/new`. This story owns the UI reachability requirement for R01: the visible "Nueva evaluacion" action on `/dashboard` must navigate to `/assessments/new`, and the happy-path acceptance/e2e evidence must start from `/dashboard`, click that action, then continue through intake and draft generation.

The Intake screen must also apply UI Design/Data Semantics before wireframes, mockups or component work. A wireframe is not accepted unless it is preceded by a DS-based field matrix that classifies each field by data class, source of truth, restrictions, cardinality and control. In R01, `learningGoal` is long free text, `level` is enum/difficulty, `duration` is numeric or preset with unit, `language` is enum/catalog, and `topic` is a candidate master-data/tag field. Do not design or implement `/assessments/new` as five unrestricted text inputs unless an explicit residual documents why the source of truth is missing.

Every screen in this story must declare API I/O and sync/async behavior. Read data, write data, catalogs/defaults/capabilities, mutations, errors and operation states must come from `api/`. If `api/` does not expose what the UI needs, this story must create/track the API task or a blocking residual. If draft generation/regeneration is async, the story must define how the browser learns completion/progress/failure: operation polling, SSE, WebSocket, webhook server-to-server/push notification or another explicit mechanism.

Every screen in this story must also declare i18n behavior. UI copy, labels, placeholders, aria labels, validation messages, safe errors and catalog labels must use i18n keys or API-provided localized labels. Generation/regeneration must carry `outputLocale` when the draft text is user-facing. Intake `language` remains programming language/pseudocode and must not be reused as natural-language locale. Logs, telemetry, status codes and DTO fields remain in English.

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
- **UI Action Reachability gate:** tasks that create or connect screens must declare the visible UI action that reaches the route. For the intake route, `/dashboard` "Nueva evaluacion" -> `/assessments/new` is required. Unit/component tests cover the action callback/link; acceptance/e2e covers the real navigation. Direct URL tests may cover deep-linking/guards, but cannot be the only happy path.
- **UI Design/Data Semantics gate:** tasks that design, mock or implement intake controls must apply `docs/master-plan/analysis/ui-design-data-strategy.md`. The contract currently describes `topic`, `level`, `duration` and `language` as strings, but the UI must still detect whether those are enum, numeric, catalog/master-data or controlled-custom values. Missing API/catalog support becomes an explicit gap, not a hidden text input.
- **API I/O + sync/async gate:** tasks that design data providers, connect real API or verify e2e must map each screen datum/action to `api/`. Missing `api/` support becomes an API task/residual. Generation/regeneration must be declared sync or async; async requires completion/progress mechanism and UI states before Done.
- **i18n gate:** tasks that design, mock, connect API or verify e2e must map all user-facing strings and generated content to locale. Missing translation/catalog/error/output-locale support becomes API/Web/Agents scope or residual; telemetry and logs stay English.

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Fake-data mockup ships without covering the edge cases in `02-ux-wireframes-y-maquetas.md` §6 (long text, zero/many versions), and those gaps surface only after connecting the real API | M | M | Explicit fake-data checklist per screen before "conectar API real" tasks start |
| Version-history UI implies a "restore version" action that the API doesn't support | M | L | Context above states explicitly: version history is read-only browsing in this MVP |
| Draft Builder screen's loader duplicates calls already made in a shared hook, defeating the Screen Data Facade's purpose | L | L | Single `loadAssessmentDraftBuilderPage` per Screen Data Facade rule; no ad hoc `getX()` calls from the Page/TSX |
| New API orchestration rules are only present in templates and are missed by already-atomized tasks | M | M | Endpoint-facing tasks in this story must include the API / Agent / Web Contract Gate before implementation/review |
| Intake route ships as URL-only because the dashboard button is present but not wired | H | H | Wireframe/hierarchy/mockup/API/e2e tasks must include `/dashboard` "Nueva evaluacion" -> `/assessments/new` and test it before Done |
| Intake ships as all-text-input despite domain restrictions and future master data | H | H | Task 01 verifies semantics vs API/data model; task 02 writes DS field matrix; task 04 uses `Textarea`/`Select`/numeric/catalog controls; task 05 records API/catalog residuals; task 06/13 test valid/invalid values |
| UI depends on data/status not exposed by `api/` | H | M | Task 01 maps API I/O; task 05 records missing endpoint/catalog/mutation/operation state; task 06 does not connect with permanent fixtures; task 13 verifies no mock/timer completion remains |
| Async generation/regeneration has no completion model | H | M | Task 01/05/06 must decide sync vs async and define polling/SSE/WebSocket/webhook/push states before implementation |
| i18n stops at static labels and misses API errors/catalogs/generated draft locale | H | M | Task 01 maps i18n contract; tasks 02/04/07/09 avoid hardcoded copy; tasks 05/06/10/11/12 propagate locale/outputLocale; task 13 verifies logs/telemetry remain English |

---

## Tasks

> Atomized via `/plan-atomize` on 2026-07-15. Each row links to its task file under `story-02-assessment-screens-wireframes-and-data-providers/`.

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [verify-api-contract](story-02-assessment-screens-wireframes-and-data-providers/task-01-verify-api-contract.md) | GENERATE-DOCUMENT | TODO | Confirmed request/response shapes plus UI data-semantics, API I/O, sync/async and i18n gaps |
| 2 | [wireframe-intake-screen](story-02-assessment-screens-wireframes-and-data-providers/task-02-wireframe-intake-screen.md) | GENERATE-DOCUMENT | TODO | DS-first field matrix + wireframe textual + estados/i18n documentados, incluyendo entry point desde dashboard |
| 3 | [component-hierarchy-intake-screen](story-02-assessment-screens-wireframes-and-data-providers/task-03-component-hierarchy-intake-screen.md) | GENERATE-DOCUMENT | TODO | Jerarquía de componentes documentada, DS components por campo y dashboard action wiring |
| 4 | [functional-mockup-intake-screen](story-02-assessment-screens-wireframes-and-data-providers/task-04-functional-mockup-intake-screen.md) | GENERATE-DOCUMENT | TODO | Maqueta funcional navegable (TSX, datos fake) con controles DS correctos y dashboard -> intake |
| 5 | [data-provider-intake-screen](story-02-assessment-screens-wireframes-and-data-providers/task-05-data-provider-intake-screen.md) | GENERATE-DOCUMENT | TODO | DTOs + `submitAssessmentBrief` en `lib/api`, con gaps de catalogos/enums/numericos/API I/O/sync-async/i18n explicitados |
| 6 | [connect-real-api-intake-screen](story-02-assessment-screens-wireframes-and-data-providers/task-06-connect-real-api-intake-screen.md) | GENERATE-DOCUMENT | TODO | Intake screen conectada a `api/` real, reachable desde dashboard, validada con controles semanticos, estados sync/async e i18n |
| 7 | [wireframe-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-07-wireframe-draft-builder-screen.md) | GENERATE-DOCUMENT | TODO | Wireframe textual + estados documentados |
| 8 | [component-hierarchy-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-08-component-hierarchy-draft-builder-screen.md) | GENERATE-DOCUMENT | TODO | Jerarquía de componentes documentada |
| 9 | [functional-mockup-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-09-functional-mockup-draft-builder-screen.md) | GENERATE-DOCUMENT | TODO | Maqueta funcional navegable (TSX, datos fake) |
| 10 | [data-provider-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-10-data-provider-draft-builder-screen.md) | GENERATE-DOCUMENT | TODO | DTO + `loadAssessmentDraftBuilderPage` Screen Data Facade |
| 11 | [mutations-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-11-mutations-draft-builder-screen.md) | GENERATE-DOCUMENT | TODO | `updateAssessmentDraft` + `regenerateAssessmentDraft` en `lib/api` |
| 12 | [connect-real-api-draft-builder-screen](story-02-assessment-screens-wireframes-and-data-providers/task-12-connect-real-api-draft-builder-screen.md) | GENERATE-DOCUMENT | TODO | Draft Builder screen conectada a `api/` real |
| 13 | [end-to-end-connection](story-02-assessment-screens-wireframes-and-data-providers/task-13-end-to-end-connection.md) | GENERATE-DOCUMENT | TODO | Flujo completo verificado desde dashboard, sin datos fake residuales, con locale/outputLocale probado |

---

## Done Criteria

- [ ] Wireframe textual de baja fidelidad documentado para Intake screen y Draft Builder screen.
- [ ] Intake screen tiene diseno previo desde Design System y matriz de campos antes del wireframe/mockup.
- [ ] Maqueta funcional navegable con datos fake existe para ambas pantallas, cubriendo estados loading/empty/error y casos límite (texto largo, muchas versiones, cero versiones), antes de conectar el backend.
- [ ] Intake no implementa todos los campos como `Input` libre: `learningGoal` usa control de texto largo; `level`, `duration`, `language` y `topic` usan controles acordes a enum, numero, catalogo, master-data o residual documentado.
- [ ] DTOs de `lib/api` reflejan exactamente los tipos reales de `AssessmentController` — sin campos inventados.
- [ ] Draft Builder screen usa un Screen Data Facade (`loadAssessmentDraftBuilderPage`) porque combina 2+ fuentes remotas (draft actual + versiones).
- [ ] Intake screen orquesta sus 2 llamadas secuenciales (crear brief → generar draft) fuera del Page/TSX, con estados de mutación explícitos.
- [ ] Cada pantalla declara API I/O: datos de lectura/escritura, catalogos/defaults/capabilities, mutations, operation states y errores respaldados por `api/` o residual bloqueante.
- [ ] Cada accion declara sync/async. Si generation/regeneration es async, existe completion model y estados UI definidos antes de Done.
- [ ] Cada pantalla declara i18n: translation keys/copy, safe errors, catalog labels, locale/fallback, `outputLocale` para generation/regeneration y logs/telemetry en ingles.
- [ ] API real conectada en ambas pantallas; mocks o datos fake removidos o aislados explícitamente.
- [ ] `/dashboard` "Nueva evaluacion" navega a `/assessments/new`; la ruta no queda solo como deep link.
- [ ] Errores 401/403/404/409/422/500 traducidos a mensajes de usuario, no mostrados crudos.
- [ ] Endpoint-facing tasks complete the API / Agent / Web Contract Gate, including Richardson REST maturity, idempotency expectations and functional route states.
- [ ] UI Action Reachability gate complete: unit/component coverage for the dashboard action and acceptance/e2e evidence that starts from dashboard.
- [ ] UI Design/Data Semantics gate complete: unit/component/acceptance/e2e evidence covers DS controls, valid values, invalid/restricted values and missing catalog/error states where applicable.
- [ ] API I/O + sync/async gate complete: contract/unit/acceptance/e2e evidence proves screen data comes from `api/` and async completion/progress is not simulated locally.
- [ ] i18n gate complete: unit/contract/acceptance/e2e evidence covers locale/fallback, translated labels/messages, safe errors/catalog labels, `outputLocale` and non-localized observability.
- [ ] `npm run test` y `npm run lint` pasan.
- [ ] TRACEABILITY.md actualizado.

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
