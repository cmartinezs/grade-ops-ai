# ⚛️ TASK 02 — wireframe-intake-screen

> **Status:** IN PROGRESS
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A written, low-fidelity textual wireframe for the Intake screen (US-010) — objetivo de usuario, flujo principal, estados secundarios — following `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §2-3, that `task-03` (component hierarchy) and `task-04` (functional mockup) are built from.

---

## Technical Design

- **Approach:** Follow the guide's recommended textual wireframe format (§3) exactly, rather than inventing a new format — the guide accepts Markdown/text wireframes and this project's own docs already model the expected shape. A maqueta funcional is required for this screen per §5 (it's a new screen with a form and an AI-triggered flow).
- **Affected files / components:** New file `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/intake-screen.md` (design artifact, not application code).
- **Interfaces / contracts:** None — this is a design document, not code. Its content (states, primary action, layout) becomes the input to `task-03`'s component hierarchy.
- **Risk:** Low — routine design documentation, no code risk. Main risk is under-specifying secondary states (loading/error), which would surface later as missing states in `task-04`'s mockup; mitigated by explicitly listing every state in the Verification table below.
- **Design notes:** The primary user is a teacher with limited time (per the guide's own framing) — the wireframe must optimize for getting from "blank form" to "draft generating" in as few steps as possible, not for visual impressiveness.

---

## Implementation Steps

1. Write `wireframes/intake-screen.md` using the guide's §3 format, covering:
   - Usuario: docente autenticado.
   - Objetivo: describir una meta de aprendizaje para iniciar la generación de un draft de evaluación con IA.
   - Layout: `AppShell` protegido (ya existe vía `(protected)/layout.tsx`) → Header (título, subtítulo) → Section única: formulario de brief (learningGoal, topic, level, duration, language).
   - Acción primaria: "Generar borrador con IA" (deshabilitada hasta que el formulario sea válido).
   - Estados: idle (formulario vacío), validando (errores inline por campo), enviando (submit deshabilitado, spinner), éxito (redirige a Draft Builder), error de negocio (422), error inesperado (500).
2. List the exact fields and their validation rules (learningGoal, topic, level, duration, language — all required, matching `CreateAssessmentBriefRequest`'s `@NotBlank` constraints from `task-01`).
3. Note the primary action's disabled/enabled logic and the loading/submitting visual treatment explicitly (per §7 estados de aprobación humana does not apply here — no AI output is shown yet on this screen).

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Wireframe names user, objective, layout, primary action, and all 5 states (idle/validating/submitting/success/error) | Manual review against `02-ux-wireframes-y-maquetas.md` §3's format |
| 2 | Every field in the wireframe matches `CreateAssessmentBriefRequest`'s fields from `task-01` (no invented or missing fields) | Cross-check against `task-01-verify-api-contract.md` § Verification row 1 |

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

## Verification Summary

`wireframes/intake-screen.md` written (see file). Evidence per Done Criteria item:

- **States documented:** 6, not 5 — the original "error" state was split into **error de negocio (422)** and **error inesperado (500)** as distinct rows, each with its own message/recovery treatment, since `15-backend-frontend-contracts.md` §4 requires different translated messages per error class. Table excerpt (full table in the wireframe file):

  | Estado | Disparador | Tratamiento visual |
  |--------|-----------|---------------------|
  | idle | Carga inicial | Formulario vacío, botón deshabilitado |
  | validando | Envío con campos vacíos | Error inline por campo |
  | enviando | Submit en curso | Botón deshabilitado + spinner |
  | éxito | `submitAssessmentBrief` resuelve | Navegación a `/assessments/{assessmentId}/draft` |
  | error de negocio (422) | API rechaza por regla de negocio | Mensaje traducido, formulario reeditable |
  | error inesperado (500) | Falla de red/servidor | Mensaje genérico de reintento |

- **Field cross-check against `task-01`:** wireframe lists `learningGoal, topic, level, duration, language` — identical set and order to `task-01`'s Verification row 1 (`CreateAssessmentBriefRequest{learningGoal, topic, level, duration, language}`). No invented or missing field.

## Done Criteria

- [x] `wireframes/intake-screen.md` exists and follows the guide's §3 format — see file, sections "Pantalla," "Estados," "Densidad y microcopy," "Resultado esperado del diseño (checklist §10)."
- [x] All 5 states (idle, validating, submitting, success, error) are documented with their visual treatment — 6 rows produced (error split into 422/business and 500/unexpected per §Verification Summary above); each state's original intent is still covered.
- [x] Every field matches `CreateAssessmentBriefRequest`'s confirmed shape from `task-01` — see §Verification Summary field cross-check.
- [ ] Software smoke/build/startup/connectivity checks: N/A, no runtime surface; for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [x] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [x] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — single deliverable (the wireframe doc), no scope creep.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
