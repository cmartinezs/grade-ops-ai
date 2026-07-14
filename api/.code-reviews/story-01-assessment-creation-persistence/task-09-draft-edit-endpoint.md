# Code review - Task 09: Draft edit endpoint

## Review 2026-07-14

Estado: APROBADO, sin hallazgos abiertos.

PR informado: #57

Rama local revisada: `gradeops-api/story-01-assessment-creation-persistence--task-09-draft-edit-endpoint` en `81584a5` (`docs(assessment-creation-persistence): add task-09 draft edit endpoint inline doc and ADR`), alineada con `origin/gradeops-api/story-01-assessment-creation-persistence--task-09-draft-edit-endpoint`.

Base revisada: `gradeops-api/story-01-assessment-creation-persistence` en `08771cc`.

## Hallazgos

Sin hallazgos.

## Observacion no bloqueante

- `docs/adr/2026-07-14-draft-edit-endpoint.md:21` dice que `UpdateAssessmentDraftHandlerIntegrationTest` "asserts the resulting SQL is an `UPDATE ... where id=?`". La prueba si bloquea el comportamiento relevante: mismo `draftId`, mismo `versionNumber`, una sola fila en `assessment_drafts` y conteo estable de `AgentExecutionLog`. En la corrida local tambien se observo el SQL `update assessment_drafts ... where id=?`. Aun asi, la prueba no captura ni aserta el texto SQL directamente; si se quiere precision documental, conviene cambiar esa frase a que la prueba verifica el efecto observable de update in-place.

## Validaciones sin hallazgos

- `PATCH /api/v1/assessments/{id}/draft` queda protegido por autenticacion y construye `UpdateAssessmentDraftCommand` con el `teacher.uid()` autenticado, no desde input externo.
- `UpdateAssessmentDraftHandler` carga el `Assessment`, verifica ownership con `OwnershipVerifier` y recien despues consulta el draft actual; el mismatch de profesor sigue ocultando el recurso como `404`.
- La ausencia de draft previo reutiliza `NoPriorDraftException`, que conserva el mapeo global de `ApplicationException` a `422 UNPROCESSABLE_CONTENT`, alineado con task-08.
- `AssessmentDraft.applyEdit(...)` conserva `id`, `assessmentId`, `versionNumber`, `previousVersionId`, `agentExecutionLogId` y `createdAt`, y solo reemplaza los campos no nulos.
- El guardado por `AssessmentDraftRepositoryPort.save(...)` con el id existente queda validado por `UpdateAssessmentDraftHandlerIntegrationTest`: una sola fila en `assessment_drafts`, mismo `draftId`, mismo `versionNumber`, campos no editados preservados y sin nuevo `AgentExecutionLog`.
- El endpoint no llama a `agents/` ni usa `DraftGenerationCoordinator`, que es el comportamiento esperado para una edicion manual.
- `UpdateAssessmentDraftRequest` mantiene semantica parcial: campos ausentes o `null` no modifican el draft; `""` en strings y elementos blank en listas se rechazan con `422` antes del caso de uso.
- La cobertura agregada incluye dominio (`AssessmentDraftTest`), handler unitario, handler con repositorios reales y PostgreSQL/Flyway, y controller MockMvc.
- La guia inline, ADR, task doc y `TRACEABILITY.md` quedaron sincronizados con la decision de editar in-place y no crear nueva version/log.
- `git diff --check gradeops-api/story-01-assessment-creation-persistence...HEAD` paso sin errores.

## Verificacion ejecutada

- `git status --short --branch`
- `git log --oneline --decorate -5`
- `git diff --stat gradeops-api/story-01-assessment-creation-persistence...HEAD`
- `git diff --name-only gradeops-api/story-01-assessment-creation-persistence...HEAD`
- Revision manual del task doc, guia inline, ADR, traceability, controller, request DTO, command/use-case port, handler, config, domain edit method, persistence adapter/repository/entity/mapper and tests.
- `./mvnw test` paso correctamente: 266 tests, 0 failures, 0 errors, 0 skipped. Incluyo Testcontainers con PostgreSQL 16.14 y Flyway valido/aplico 12 migraciones hasta V12. En `UpdateAssessmentDraftHandlerIntegrationTest` se observo `update assessment_drafts ... where id=?`.
- `git diff --check gradeops-api/story-01-assessment-creation-persistence...HEAD`
