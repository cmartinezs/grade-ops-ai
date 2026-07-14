# Code review - Task 10: Retrieval endpoints + dashboard wiring

## Review 2026-07-14

Estado: APROBADO, sin hallazgos abiertos.

PR informado: #58

Rama local revisada: `gradeops-api/story-01-assessment-creation-persistence--task-10-retrieval-and-dashboard-wiring` en `3ed31c2` (`docs(assessment-creation-persistence): add task-10 retrieval and dashboard wiring inline doc and ADR`), alineada con `origin/gradeops-api/story-01-assessment-creation-persistence--task-10-retrieval-and-dashboard-wiring`.

Base revisada: `gradeops-api/story-01-assessment-creation-persistence` en `0161bec`.

## Hallazgos

Sin hallazgos.

## Validaciones sin hallazgos

- `GET /api/v1/assessments/{id}/draft` queda protegido por autenticacion y construye `GetCurrentDraftCommand` con el `teacher.uid()` autenticado, no desde input externo.
- `GetCurrentDraftHandler` carga el `Assessment`, verifica ownership con `OwnershipVerifier` y recien despues consulta el draft actual; assessment inexistente, cross-owner y ausencia de draft se traducen a `404`.
- `GET /api/v1/assessments/{id}/draft/versions` sigue la misma compuerta de ownership y devuelve lista vacia cuando el assessment existe pero aun no tiene drafts, coherente con endpoint de coleccion.
- `AssessmentDraftPersistenceAdapter.findAllByAssessmentId(...)` mantiene el contrato newest-first mediante `findAllByAssessmentIdOrderByVersionNumberDesc(...)`; la cobertura de integracion valida el orden `3, 2, 1` en PostgreSQL.
- `GET /api/v1/assessments` reemplaza el stub de task-01 con una consulta real por profesor y una fila por assessment.
- La query native `LEFT JOIN LATERAL` evita N+1 y usa el titulo del draft de mayor `version_number`, con fallback a `assessment_briefs.topic` cuando aun no existe draft.
- La cobertura de integracion de `AssessmentPersistenceAdapterIntegrationTest` valida titulo desde v2, fallback a topic sin draft y aislamiento por profesor sobre PostgreSQL Testcontainers/Flyway.
- `submissionCount`, `pendingApprovals` y `reportLink` permanecen en placeholders `0`/`0`/`null`, alineado con el alcance documentado de story-01.
- La guia inline, ADR, task doc y `TRACEABILITY.md` quedaron sincronizados con la decision de single-query dashboard join y con las semanticas `404`/lista vacia de retrieval.
- `git diff --check gradeops-api/story-01-assessment-creation-persistence..HEAD` paso sin errores.

## Verificacion ejecutada

- `git status --short --branch`
- `git log --oneline --decorate -n 12`
- `git diff --stat gradeops-api/story-01-assessment-creation-persistence..HEAD`
- `git diff --name-status gradeops-api/story-01-assessment-creation-persistence..HEAD`
- Revision manual del task doc, guia inline, ADR, traceability, controller, command/use-case ports, handlers, config, persistence repositories/adapters/projection and tests.
- `./mvnw -Dtest=GetCurrentDraftHandlerTest,ListDraftVersionsHandlerTest,AssessmentControllerTest,AssessmentPersistenceAdapterTest,AssessmentPersistenceAdapterIntegrationTest,AssessmentDraftPersistenceAdapterIntegrationTest test` paso correctamente: 51 tests, 0 failures, 0 errors, 0 skipped. Incluyo Testcontainers con PostgreSQL 16.14 y Flyway valido/aplico 12 migraciones hasta V12; se observo la query `LEFT JOIN LATERAL` durante `AssessmentPersistenceAdapterIntegrationTest`.
- `./mvnw test` paso correctamente: 284 tests, 0 failures, 0 errors, 0 skipped. Incluyo Testcontainers con PostgreSQL 16.14 y Flyway valido/aplico 12 migraciones hasta V12 en las pruebas de integracion.
- `git diff --check gradeops-api/story-01-assessment-creation-persistence..HEAD`
