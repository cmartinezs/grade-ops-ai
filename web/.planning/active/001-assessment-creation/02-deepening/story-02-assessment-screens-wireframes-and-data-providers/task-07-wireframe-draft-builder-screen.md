# ⚛️ TASK 07 — wireframe-draft-builder-screen

> **Status:** IN PROGRESS
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A written, low-fidelity textual wireframe for the Draft Builder screen (US-011 + US-012 combined: view/edit draft, regenerate with adjustment notes, browse version history), following `02-ux-wireframes-y-maquetas.md` §2-3, that `task-08` (hierarchy) and `task-09` (mockup) are built from.

---

## Technical Design

- **Approach:** One screen, not three — regeneration and version history are sections *within* the draft view, per story-01's own Done Criteria ("Teacher can trigger regeneration ... from the draft view" / "Previous draft version(s) remain visible ... after a regeneration"). Splitting these into separate routes would contradict that requirement and add navigation overhead for no benefit.
- **Affected files / components:** New file `wireframes/draft-builder-screen.md` (design artifact, not application code).
- **Interfaces / contracts:** None — design document. Its content becomes the input to `task-08`'s hierarchy.
- **Risk:** Medium — this screen has more states than Intake (loading, empty-before-first-draft, error, 409 conflict, viewing a past version vs. the current one); under-specifying any of these surfaces as a missing state later in `task-09`. Mitigated by listing every state explicitly below.
- **Design notes:** Per `task-01`, there is **no restore-version endpoint** — the wireframe must not imply a "make this version current" action. Version history here is read-only browsing.

---

## Implementation Steps

1. Write `wireframes/draft-builder-screen.md` using the guide's §3 format, covering:
   - Usuario: docente autenticado, dueño de la evaluación.
   - Objetivo: revisar el draft generado por IA, editarlo, regenerarlo con notas de ajuste si no es correcto, y consultar versiones previas.
   - Layout: `AppShell` protegido → Header (título de la evaluación) → Section "Editor de draft" (campos editables: title, context, instructions, objectives, deliverables, constraints) → Section "Regenerar" (input de notas de ajuste + acción) → Section "Historial de versiones" (lista de versiones previas, solo lectura).
   - Acción primaria: guardar cambios editados / regenerar (son dos acciones distintas, ambas con su propio loading/error).
   - Estados: loading inicial (skeleton), empty (aún no existe draft — no debería ocurrir si se llega desde `task-06`'s redirect, pero documentarlo como estado defensivo), listo (draft renderizado), guardando edición, regenerando, error de conflicto 409 (alguien más modificó el draft), error 404 (assessment no existe), error 500.
2. Explicitly note: version history is read-only browsing only, no "restore this version" action (per `task-01`'s confirmed contract).
3. Note the AI-generated-content disclosure per `02-ux-wireframes-y-maquetas.md` §9: the draft is AI-generated until edited/approved — the wireframe should note where "generado por IA" vs "editado por el docente" would be indicated, even though full approval-workflow UI is out of this story's scope.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Wireframe names user, objective, layout (3 sections), and all 8 states listed above | Manual review against `02-ux-wireframes-y-maquetas.md` §3 |
| 2 | No "restore version" action is implied anywhere in the wireframe | Manual review against `task-01`'s confirmed contract |
| 3 | Editable fields match `GenerateAssessmentDraftResponse`'s fields from `task-01` (title, context, instructions, objectives, deliverables, constraints) | Cross-check against `task-01-verify-api-contract.md` § Verification rows 2-6 |

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

## Verification Summary

`wireframes/draft-builder-screen.md` written (see file). Evidence per Done Criteria item:

- **States documented: 12, not 8** — the original plan (Implementation Steps #1) assumed an "error de conflicto 409" state. Traced the real code path before writing the table, same discipline `task-02` applied to Intake, rather than assuming the generic 401/403/404/409/422/500 taxonomy from `06-estado-datos-y-api.md` §9:

  | Source read | Finding |
  |-------------|---------|
  | `UpdateAssessmentDraftHandler.java`, `RegenerateAssessmentDraftHandler.java` | Neither implements optimistic locking — no version/ETag field is read from `UpdateAssessmentDraftRequest`/`RegenerateAssessmentDraftRequest`, and `AssessmentDraft.applyEdit()` unconditionally overwrites the row via `restore(...)`. |
  | `GlobalExceptionHandler.java` | Only `DuplicateEmailException` maps to `HttpStatus.CONFLICT` (409) — an auth-domain exception, unrelated to drafts. No draft-related exception produces a 409 anywhere in the handler. |

  **Conclusion: the assumed 409 conflict state does not exist.** A concurrent edit silently overwrites the previous write (last-write-wins) with no signal the UI could show. Replaced with the real, traced error surface of all 4 endpoints this screen calls (`GET .../draft`, `PATCH .../draft`, `POST .../draft/regenerate`, `GET .../draft/versions`), verified directly against `AssessmentController.java`, `GlobalExceptionHandler.java`, `{GetCurrentDraftHandler,UpdateAssessmentDraftHandler,RegenerateAssessmentDraftHandler,ListDraftVersionsHandler,DraftGenerationCoordinator}.java`, `NoPriorDraftException.java`, and `OwnershipVerifier.java` (2026-07-16). Final count: 3 structural states (loading/empty/listo) + 2 in-flight states (guardando/regenerando) + 7 distinct error states (validation-on-save 422, empty-notes-on-regenerate 422, no-prior-draft 422, agent-rejected 422, agent-down 502/503, 404 assessment/ownership, 500) = 12 — see the wireframe's "Estados" table (12 rows) and its trazabilidad note for the full mapping. The removed 409 assumption is preserved as a documented concurrency risk in the wireframe's "Riesgo real de concurrencia" note, not silently dropped.
- **No restore action:** confirmed against `task-01`'s exhaustive 7-mapping listing of `AssessmentController.java` (no 8th endpoint) — the wireframe's Layout and Section 3 description explicitly state "solo lectura" and the risk note in `task-01`/story context is cross-referenced.
- **Editable fields cross-check:** wireframe's Section 1 lists `title, context, instructions, objectives, deliverables, constraints` — identical set to `GenerateAssessmentDraftResponse`'s fields confirmed in `task-01`'s Verification Summary (`task-01-verify-api-contract.md` § Verification Summary, response records). No invented or missing field.
- **Additional finding beyond the task's original scope (§9 AI-disclosure):** read `AssessmentDraft.java` to design the "generado por IA" vs "editado por el docente" indicator against real data, not an assumed flag. Finding: `applyEdit()` preserves the original `agentExecutionLogId`/`versionNumber`/`createdAt` — **no `editedByTeacher` flag or edit timestamp is persisted anywhere**; a saved edit is indistinguishable from freshly-generated content once read back from `GET .../draft`. Documented as a real backend-contract gap in the wireframe's own "Nota sobre disclosure de IA" section, with a designed workaround (client-side-only "just generated" state, valid only within the same session, reverting to a neutral label after any save or reload) rather than fabricating a persisted distinction the API cannot back.

## Done Criteria

- [x] `wireframes/draft-builder-screen.md` exists and follows the guide's §3 format — see file, sections "Pantalla," "Boceto visual (estado listo)," "Estados," "Bocetos de variantes de estado," "Nota sobre disclosure de IA," "Densidad y microcopy," "Resultado esperado del diseño (checklist §10)."
- [x] All 8 states are documented with their visual treatment — 12 real states produced instead of 8 assumed; the assumed "409 conflict" state was traced and found not to exist in the real API, replaced with the actual error surface of all 4 endpoints — see § Verification Summary above and the wireframe's own trazabilidad note.
- [x] No "restore version" action is implied — confirmed against `task-01`'s exhaustive endpoint listing; wireframe explicitly states the history section is read-only.
- [x] Editable fields match the confirmed `GenerateAssessmentDraftResponse` shape — see § Verification Summary field cross-check.
- [x] Software smoke/build/startup/connectivity checks: N/A, no runtime surface; for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR. Branch `story-02-assessment-screens-wireframes-and-data-providers--task-07-wireframe-draft-builder-screen` pushed; PR to be opened via `publish --execute`.
- [x] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [x] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — single deliverable (the wireframe doc); the extra source-tracing (409/§9 disclosure) is evidence-gathering for that same deliverable, not a second deliverable, matching `task-02`'s precedent.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)
