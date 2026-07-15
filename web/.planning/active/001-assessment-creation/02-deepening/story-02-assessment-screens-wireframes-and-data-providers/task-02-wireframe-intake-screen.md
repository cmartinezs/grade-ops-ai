# ⚛️ TASK 02 — wireframe-intake-screen

> **Status:** TODO
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

## Done Criteria

- [ ] `wireframes/intake-screen.md` exists and follows the guide's §3 format.
- [ ] All 5 states (idle, validating, submitting, success, error) are documented with their visual treatment.
- [ ] Every field matches `CreateAssessmentBriefRequest`'s confirmed shape from `task-01`.
- [ ] Software smoke/build/startup/connectivity checks: N/A, no runtime surface; for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [ ] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
