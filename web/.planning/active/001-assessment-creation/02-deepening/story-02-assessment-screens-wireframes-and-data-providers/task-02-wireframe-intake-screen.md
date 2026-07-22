# ⚛️ TASK 02 — wireframe-intake-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A written, low-fidelity textual wireframe for the Intake screen (US-010) — preceded by a DS-first design note, field-semantics matrix, API I/O matrix, sync/async decision and i18n matrix — following `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §2-3 and `docs/master-plan/analysis/ui-design-data-strategy.md`, that `task-03` (component hierarchy) and `task-04` (functional mockup) are built from.

---

## Technical Design

- **Approach:** First declare the Design System pattern, field matrix, API I/O matrix, sync/async behavior and i18n behavior, then follow the guide's recommended textual wireframe format (§3) exactly, rather than inventing a new format — the guide accepts Markdown/text wireframes and this project's own docs already model the expected shape. A maqueta funcional is required for this screen per §5 (it's a new screen with a form and an AI-triggered flow).
- **Affected files / components:** New file `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/intake-screen.md` (design artifact, not application code).
- **Interfaces / contracts:** None — this is a design document, not code. Its content (states, primary action, layout and field-control mapping) becomes the input to `task-03`'s component hierarchy.
- **Risk:** Low — routine design documentation, no code risk. Main risk is under-specifying secondary states (loading/error), which would surface later as missing states in `task-04`'s mockup; mitigated by explicitly listing every state in the Verification table below.
- **Design notes:** The primary user is a teacher with limited time (per the guide's own framing) — the wireframe must optimize for getting from "blank form" to "draft generating" in as few steps as possible, not for visual impressiveness. It still must not flatten domain fields into generic text boxes: use `Textarea` for long free text, select/radio for fixed values, numeric/preset control for duration, and catalog/tag controls where the domain is master data.

---

## Implementation Steps

1. Write the DS-first section in `wireframes/intake-screen.md`, naming the page/form pattern from `web/design-system/` and the DS components expected for each field.
2. Add a field-semantics matrix before the wireframe:
   - `learningGoal`: long free text, `Textarea`, max length required.
   - `topic`: curriculum topic/master-data or controlled tag; selector/tag input if source exists, residual if not.
   - `level`: enum/difficulty; `Select` or `Radio`, not arbitrary `Input`.
   - `duration`: numeric minutes or preset; numeric input/stepper/select with unit, not string text.
   - `language`: programming language/pseudocode enum/catalog; selector/combo, custom only if API allows.
3. Add an API I/O matrix before the wireframe: required catalogs/defaults/capabilities, create brief mutation, generate draft command, expected response/error shape, and any missing `api/` support.
4. Add a sync/async decision: create brief sync response with `assessmentId`; generation sync legacy or async operation-backed. If async, name completion mechanism and UI states before writing the wireframe.
5. Add an i18n matrix before the wireframe: locale source/precedence, UI copy keys for labels/messages/buttons, localized catalog labels, safe validation/server errors, `outputLocale`/`contentLocale` for generated drafts, and the rule that programming `language` is not the UI/generated-content locale.
6. Write `wireframes/intake-screen.md` using the guide's §3 format, covering:
   - Entry point: authenticated teacher starts at `/dashboard` and clicks the visible "Nueva evaluacion" action; typing `/assessments/new` directly is not the primary happy path.
   - Usuario: docente autenticado.
   - Objetivo: describir una meta de aprendizaje para iniciar la generación de un draft de evaluación con IA.
   - Layout: `AppShell` protegido (ya existe vía `(protected)/layout.tsx`) → `/dashboard` action → `/assessments/new` Header (título, subtítulo) → Section única: formulario de brief (learningGoal, topic, level, duration, language).
   - Acción primaria: "Generar borrador con IA" (deshabilitada hasta que el formulario sea válido).
   - Estados: idle (formulario vacío), validando (errores inline por campo), enviando (submit deshabilitado, spinner), éxito (redirige a Draft Builder), error de negocio (422), error inesperado (500).
7. List the exact fields and their validation rules (learningGoal, topic, level, duration, language — all required, matching `CreateAssessmentBriefRequest`'s `@NotBlank` constraints from `task-01`, plus the semantic restrictions/gaps recorded by `task-01`).
8. Note the primary action's disabled/enabled logic and the loading/submitting visual treatment explicitly (per §7 estados de aprobación humana does not apply here — no AI output is shown yet on this screen).
9. If generation is async, include queued/running/succeeded/failed/timeout states and the selected completion mechanism in the wireframe.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Wireframe names user, objective, layout, primary action, and all 5 states (idle/validating/submitting/success/error) | Manual review against `02-ux-wireframes-y-maquetas.md` §3's format |
| 2 | Every field in the wireframe matches `CreateAssessmentBriefRequest`'s fields from `task-01` (no invented or missing fields) | Cross-check against `task-01-verify-api-contract.md` § Verification row 1 |
| 3 | Wireframe documents `/dashboard` "Nueva evaluacion" as the functional entry point into `/assessments/new` | Manual review against the UI Action Reachability rule |
| 4 | Wireframe includes DS-first field matrix and does not specify all fields as unrestricted text inputs | Manual review against `docs/master-plan/analysis/ui-design-data-strategy.md` |
| 5 | Wireframe includes API I/O matrix and sync/async decision for create/generate | Manual review against task-01 and API orchestration strategy |
| 6 | Wireframe includes i18n matrix for UI copy, catalog labels, safe errors, locale fallback and `outputLocale`/`contentLocale` | Manual review against `docs/master-plan/analysis/i18n-strategy.md` |

### Software Smoke Test Check

N/A — design document only, no runtime surface.

### Database / ORM Consistency Check

N/A.

### Logging / Observability

N/A — this task produces no executable code.

### Generated Test Suite

- **Task suite file:** `test-suites/task-02-wireframe-intake-screen-test-suite.md`
- **Required gates:** architecture/design guide review only.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md`.
- **Acceptance environment:** N/A.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Done Criteria

- [ ] `wireframes/intake-screen.md` exists and follows the guide's §3 format.
- [ ] A DS-first design note and field-semantics matrix appear before the wireframe content.
- [ ] API I/O matrix and sync/async decision appear before the wireframe content.
- [ ] i18n matrix appears before the wireframe content and separates UI/content locale from programming `language`.
- [ ] All 5 states (idle, validating, submitting, success, error) are documented with their visual treatment.
- [ ] Async states are documented if generation is operation-backed: queued/running/succeeded/failed/timeout plus completion mechanism.
- [ ] Every field matches `CreateAssessmentBriefRequest`'s confirmed shape from `task-01`.
- [ ] `level`, `duration`, `language` and `topic` are not represented as unrestricted text inputs unless an explicit API/domain residual is recorded.
- [ ] Entry from `/dashboard` "Nueva evaluacion" to `/assessments/new` is explicitly documented; URL-only access is not accepted as the main path.
- [ ] Software smoke/build/startup/connectivity checks: N/A, no runtime surface; for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [ ] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
