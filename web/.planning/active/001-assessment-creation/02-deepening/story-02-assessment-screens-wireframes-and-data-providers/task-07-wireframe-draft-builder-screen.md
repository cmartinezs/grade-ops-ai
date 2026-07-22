# ⚛️ TASK 07 — wireframe-draft-builder-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A written, low-fidelity textual wireframe for the Draft Builder screen (US-011 + US-012 combined: view/edit draft, regenerate with adjustment notes, browse version history), following `02-ux-wireframes-y-maquetas.md` §2-3, that `task-08` (hierarchy) and `task-09` (mockup) are built from. The wireframe must include the screen API I/O matrix, the sync/async decision and the i18n matrix before any layout is accepted.

---

## Technical Design

- **Approach:** One screen, not three — regeneration and version history are sections *within* the draft view, per story-01's own Done Criteria ("Teacher can trigger regeneration ... from the draft view" / "Previous draft version(s) remain visible ... after a regeneration"). Splitting these into separate routes would contradict that requirement and add navigation overhead for no benefit.
- **Affected files / components:** New file `wireframes/draft-builder-screen.md` (design artifact, not application code).
- **Interfaces / contracts:** Design artifact only, but it must document the API I/O contract the UI depends on: current draft read, version-list read, draft update mutation, regenerate mutation, errors, locale/content-language metadata, and operation/status data if regeneration is async. Missing `api/` support becomes a task/residual before the UI can be marked done.
- **Risk:** Medium — this screen has more states than Intake (loading, empty-before-first-draft, error, 409 conflict, viewing a past version vs. the current one); under-specifying any of these surfaces as a missing state later in `task-09`. Mitigated by listing every state explicitly below.
- **Design notes:** Per `task-01`, there is **no restore-version endpoint** — the wireframe must not imply a "make this version current" action. Version history here is read-only browsing. Regeneration must be shown as sync or async according to the API contract; if async, the wireframe names the completion mechanism (polling, SSE, WebSocket, webhook/push or another explicit contract) and states. Generated draft content must show or preserve its `outputLocale`/`contentLocale` so the teacher can understand why AI text appears in a given language.

---

## Implementation Steps

1. Write `wireframes/draft-builder-screen.md` using the guide's §3 format, covering:
   - Usuario: docente autenticado, dueño de la evaluación.
   - Objetivo: revisar el draft generado por IA, editarlo, regenerarlo con notas de ajuste si no es correcto, y consultar versiones previas.
   - Layout: `AppShell` protegido → Header (título de la evaluación) → Section "Editor de draft" (campos editables: title, context, instructions, objectives, deliverables, constraints) → Section "Regenerar" (input de notas de ajuste + acción) → Section "Historial de versiones" (lista de versiones previas, solo lectura).
   - Acción primaria: guardar cambios editados / regenerar (son dos acciones distintas, ambas con su propio loading/error).
   - Estados: loading inicial (skeleton), empty (aún no existe draft — no debería ocurrir si se llega desde `task-06`'s redirect, pero documentarlo como estado defensivo), listo (draft renderizado), guardando edición, regenerando, error de conflicto 409 (alguien más modificó el draft), error 404 (assessment no existe), error 500.
2. Add an API I/O matrix before the layout: `GET /draft`, `GET /draft/versions`, `PATCH /draft`, `POST /draft/regenerate`, required fields, source of truth, result shape, error shape, and any missing endpoint/read model/mutation/operation state in `api/`.
3. Add an i18n matrix before the layout: localized UI copy and errors, generated-content locale (`outputLocale`/`contentLocale`), version locale display/fallback, regenerate locale capture at request time, and English-only logs/telemetry/codes.
4. Add a sync/async decision for save and regenerate. Save is expected to be sync; regenerate may be sync legacy or operation-backed async. If async, name the completion mechanism and UI states: queued, running, succeeded, failed, timeout and retry/cancel where applicable.
5. Explicitly note: version history is read-only browsing only, no "restore this version" action (per `task-01`'s confirmed contract).
6. Note the AI-generated-content disclosure per `02-ux-wireframes-y-maquetas.md` §9: the draft is AI-generated until edited/approved — the wireframe should note where "generado por IA" vs "editado por el docente" would be indicated, even though full approval-workflow UI is out of this story's scope.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Wireframe names user, objective, layout (3 sections), and all 8 states listed above | Manual review against `02-ux-wireframes-y-maquetas.md` §3 |
| 2 | No "restore version" action is implied anywhere in the wireframe | Manual review against `task-01`'s confirmed contract |
| 3 | Editable fields match `GenerateAssessmentDraftResponse`'s fields from `task-01` (title, context, instructions, objectives, deliverables, constraints) | Cross-check against `task-01-verify-api-contract.md` § Verification rows 2-6 |
| 4 | Wireframe includes API I/O matrix and sync/async decision for draft load, save and regenerate | Manual review against `task-01` and the API orchestration strategy |
| 5 | Wireframe includes i18n matrix for UI copy/errors, generated-content locale, version locale handling and regenerate locale capture | Manual review against `docs/master-plan/analysis/i18n-strategy.md` |

### Software Smoke Test Check

N/A — design document only, no runtime surface.

### Database / ORM Consistency Check

N/A.

### Logging / Observability

N/A — this task produces no executable code.

### Generated Test Suite

- **Task suite file:** `test-suites/task-07-wireframe-draft-builder-screen-test-suite.md`
- **Required gates:** architecture/design guide review only.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md`.
- **Acceptance environment:** N/A.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Done Criteria

- [ ] `wireframes/draft-builder-screen.md` exists and follows the guide's §3 format.
- [ ] All 8 states are documented with their visual treatment.
- [ ] No "restore version" action is implied.
- [ ] Editable fields match the confirmed `GenerateAssessmentDraftResponse` shape.
- [ ] API I/O matrix and sync/async decision appear before the wireframe content; missing `api/` support is recorded as task/residual.
- [ ] i18n matrix appears before the wireframe content; generated-content locale and localized UI/errors/version labels are explicit.
- [ ] Async regenerate states are documented if regeneration is operation-backed: queued/running/succeeded/failed/timeout plus completion mechanism.
- [ ] Software smoke/build/startup/connectivity checks: N/A, no runtime surface; for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [ ] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
