# Code review - Task 07: Draft generation endpoint

## Re-review 2026-07-13

Estado: APROBADO, sin hallazgos abiertos.

PR informado: #52

Rama local revalidada: `gradeops-api/story-01-assessment-creation-persistence--task-07-draft-generation-endpoint` en `8744d1b` (`test(assessment-creation-persistence): add real-repository integration test for draft/log cross-reference`), alineada con `origin/gradeops-api/story-01-assessment-creation-persistence--task-07-draft-generation-endpoint`.

Hallazgos anteriores:

- RESUELTO - Se agrego `GenerateAssessmentDraftHandlerIntegrationTest`, que ejecuta el `GenerateAssessmentDraftHandler` real con adapters/repositorios reales sobre PostgreSQL Testcontainers y Flyway hasta V12, dejando stubbeado solo `AssessmentAgentClient`.
- RESUELTO - El caso exitoso ahora valida, despues de `flush()`/`clear()`, que se persiste un draft v1, un log `COMPLETED`, `assessment_drafts.agent_execution_log_id` apunta al log y `agent_execution_logs.draft_id` queda backfilled con el draft.
- RESUELTO - El caso fallido ahora valida que no se persiste ningun draft y que queda un unico log `FAILED` con `errorCode = "AGENT_REJECTED"` y `draftId = null`.

Verificacion de re-review:

- `git fetch origin`
- `git status --short --branch`
- `git log --oneline --decorate -8`
- Revision manual de `src/test/java/cl/gradeops/ai/api/assessment/application/usecase/GenerateAssessmentDraftHandlerIntegrationTest.java`.
- `./mvnw -Dtest=GenerateAssessmentDraftHandlerIntegrationTest,GenerateAssessmentDraftHandlerTest,AgentExecutionLogPersistenceAdapterIntegrationTest,AssessmentControllerTest,HexagonalArchitectureTest test` paso correctamente: 27 tests, 0 failures, 0 errors. Testcontainers levanto PostgreSQL 16.14 y Flyway valido/aplico 12 migraciones hasta V12. La salida de Hibernate mostro la secuencia esperada del caso exitoso: insert log, insert draft, update log.
- `./mvnw test` paso correctamente: 236 tests, 0 failures, 0 errors, 0 skipped. Testcontainers levanto PostgreSQL 16.14 y Flyway valido/aplico 12 migraciones hasta V12.

## Review 2026-07-13

Estado: REQUIERE CAMBIOS.

PR informado: #52

Rama local revisada: `gradeops-api/story-01-assessment-creation-persistence--task-07-draft-generation-endpoint` en `1b9dcf5` (`docs(assessment-creation-persistence): add task-07 draft generation endpoint inline doc and ADR`), alineada con `origin/gradeops-api/story-01-assessment-creation-persistence--task-07-draft-generation-endpoint`.

## Hallazgos

### MEDIUM - Falta cobertura de integracion para el flujo real que persiste draft + log

- **Archivos:** `.planning/active/003-assessment-creation/02-deepening/story-01-assessment-creation-persistence/task-07-draft-generation-endpoint.md`, `src/test/java/cl/gradeops/ai/api/assessment/application/usecase/GenerateAssessmentDraftHandlerTest.java`, `src/test/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/out/persistence/AgentExecutionLogPersistenceAdapterIntegrationTest.java`
- **Lineas:** plan 57-58; handler test 41-64, 83-104, 110-120; log integration test 74-95, 98-111
- **Tipo:** brecha de verificacion / persistencia transaccional

La tarea pide validar con una prueba de integracion que una generacion exitosa persiste tanto el `AssessmentDraft` v1 como el `AgentExecutionLog`, y que una falla de `agents/` persiste solo el log de falla. La cobertura actual se queda en dos niveles separados: `GenerateAssessmentDraftHandlerTest` usa repositorios mockeados, por lo que no prueba el `TransactionTemplate` contra JPA/Flyway/PostgreSQL; `AgentExecutionLogPersistenceAdapterIntegrationTest` valida el schema y round-trip del log, pero no ejecuta el flujo completo que guarda log -> draft -> backfill del log dentro de la misma transaccion.

Ese es justamente el punto riesgoso de task-07: hay una referencia cruzada entre `assessment_drafts.agent_execution_log_id` y `agent_execution_logs.draft_id`, y el handler depende de que el orden de persistencia/flush y el rollback funcionen con repositorios reales. Hoy un bug en esa secuencia podria pasar los tests existentes, porque los mocks aceptan cualquier orden y el test de schema nunca crea el draft referenciando el log.

Recomendacion:

- Agregar un test de integracion con PostgreSQL/Testcontainers que use el `GenerateAssessmentDraftHandler` real, repositorios reales y un `AssessmentAgentClient` stub/mock.
- En el caso exitoso, crear teacher + assessment + brief, ejecutar `GenerateAssessmentDraftCommand`, hacer `flush/clear` y consultar filas reales para comprobar: un draft v1, un log `COMPLETED`, `assessment_drafts.agent_execution_log_id = agent_execution_logs.id` y `agent_execution_logs.draft_id = assessment_drafts.id`.
- En el caso fallido, hacer que el cliente lance `AgentClientException`, comprobar que no hay draft y que si queda un log `FAILED` con `error_code` igual al reason.

## Validaciones sin hallazgos

- El endpoint `POST /api/v1/assessments/{id}/draft` queda protegido por autenticacion y construye el comando con el `teacher.uid()` autenticado, no desde input externo.
- `GenerateAssessmentDraftHandler` verifica propiedad del `Assessment` antes de buscar el brief o llamar a `agents/`, evitando gasto de generacion para recursos ajenos.
- La llamada a `AssessmentAgentClient.generate(...)` ocurre antes de abrir el `TransactionTemplate`; el test de orden con Mockito fija ese contrato.
- `GlobalExceptionHandler` mapea `AgentClientException` a `503`, `422` o `502` segun `UNREACHABLE`, `AGENT_REJECTED` o `AGENT_ERROR`.
- `V12__add_agent_execution_logs.sql` usa `DOUBLE PRECISION` para `cost_estimate`, consistente con `AgentExecutionLogJpaEntity.costEstimate` como `Double` y con `ddl-auto=validate`.
- La guia inline y el ADR documentan la decision de `TransactionTemplate`, el ownership check y la correccion de `cost_estimate`.

## Verificacion ejecutada

- `git status --short --branch`
- `git log --oneline --decorate -12`
- `git diff --stat gradeops-api/story-01-assessment-creation-persistence...HEAD`
- `git diff --name-only gradeops-api/story-01-assessment-creation-persistence...HEAD`
- Revision manual del diff de handler, controller, config, migracion, persistence adapter/entity/mapper, tests, guia inline y ADR.
- `./mvnw -Dtest=GenerateAssessmentDraftHandlerTest,AgentExecutionLogPersistenceAdapterIntegrationTest,AssessmentControllerTest,HexagonalArchitectureTest test` paso correctamente: 25 tests, 0 failures, 0 errors. Testcontainers levanto PostgreSQL 16.14 y Flyway valido/aplico 12 migraciones hasta V12.
- `./mvnw test` paso correctamente: 234 tests, 0 failures, 0 errors, 0 skipped. Testcontainers levanto PostgreSQL 16.14 y Flyway valido/aplico 12 migraciones hasta V12.
