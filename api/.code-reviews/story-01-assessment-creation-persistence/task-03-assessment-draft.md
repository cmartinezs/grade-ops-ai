# Code review - Task 03: AssessmentDraft entity + versioning

## Re-review 2026-07-13

Estado: APROBADO, sin hallazgos abiertos.

Hallazgos anteriores:

- RESUELTO - `AssessmentDraft.generate(...)`, `regenerate(...)` y `restore(...)` ahora copian defensivamente `objectives`, `deliverables` y `constraints` con `List.copyOf(...)`, evitando que una lista mutable externa modifique una version ya construida. Los getters devuelven esas listas inmutables.
- RESUELTO - `AssessmentDraftTest` agrega cobertura para mutacion posterior de la lista original en los tres caminos de construccion y para mutacion via getters.

Verificacion de re-review:

- Rama local alineada con `origin/gradeops-api/story-01-assessment-creation-persistence--task-03-assessment-draft` en `4f289fa` (`fix(assessment-creation-persistence): defensively copy AssessmentDraft list fields`).
- PR #48 sigue `OPEN`, no draft, `mergeStateStatus = CLEAN`; checks visibles de Vercel en verde.
- `./mvnw test` paso correctamente con Docker activo: 199 tests, 0 failures, 0 errors. Testcontainers levanto PostgreSQL 16 y Flyway valido/aplico 11 migraciones hasta V11.
- `git diff --check origin/gradeops-api/story-01-assessment-creation-persistence...HEAD` paso sin errores.

## Review 2026-07-13

Estado: REQUIERE CAMBIOS.

PR revisado: https://github.com/cmartinezs/grade-ops-ai/pull/48

Rama local revisada: `gradeops-api/story-01-assessment-creation-persistence--task-03-assessment-draft` (`f128d7f`), limpia y alineada con `origin/gradeops-api/story-01-assessment-creation-persistence--task-03-assessment-draft`.

## Hallazgos

### MEDIUM - `AssessmentDraft` expone listas mutables y rompe la inmutabilidad del draft versionado

- **Archivo:** `src/main/java/cl/gradeops/ai/api/assessment/domain/model/AssessmentDraft.java`
- **Lineas:** 41-43, 68-70, 97-99, 125-127
- **Tipo:** invariante de dominio / mutabilidad accidental

`AssessmentDraft` guarda directamente las referencias `List<String>` recibidas por `generate(...)`, `regenerate(...)` y `restore(...)`, y despues devuelve esas mismas referencias desde los getters. Eso permite modificar el contenido de un draft despues de construido, ya sea mutando la lista original entregada al factory o mutando la lista devuelta por `getObjectives()`, `getDeliverables()` o `getConstraints()`.

Eso contradice dos contratos importantes de esta tarea:

- El draft versionado se presenta como una fila inmutable por generacion/regeneracion; una vez creado, una version no deberia cambiar por aliasing de colecciones.
- Las guias locales de DDD dicen que el aggregate root protege invariantes y muestran `List.copyOf(...)` para estado de colecciones internas. El contrato hermano `agents` tambien usa `List.copyOf(...)` en `AssessmentResult`, que es justamente la forma que esta tarea dice espejar.

El riesgo practico es que un handler futuro podria crear o restaurar un draft, modificar accidentalmente una lista externa o un getter, y persistir una version con contenido distinto al resultado originalmente generado. Como `JpaRepository.save(...)` tambien puede actualizar una fila existente si recibe el mismo `id`, esta mutabilidad abre la puerta a sobrescribir JSON de una version ya existente sin pasar por un metodo de negocio explicito.

Recomendacion:

- Copiar defensivamente las tres listas al asignarlas: `List.copyOf(objectives)`, `List.copyOf(deliverables)`, `List.copyOf(constraints)`.
- Mantener los getters devolviendo esas listas copiadas e inmutables, o devolver `List.copyOf(...)` si se prefiere blindar en lectura.
- Agregar tests de dominio que usen `new ArrayList<>(...)`, muten la lista original despues del factory y validen que el draft no cambia.
- Agregar tests que intenten mutar `getObjectives()`, `getDeliverables()` y `getConstraints()` y esperen `UnsupportedOperationException`.

Evidencia:

```java
d.objectives = objectives;
d.deliverables = deliverables;
d.constraints = constraints;
```

```java
public List<String> getObjectives()        { return objectives; }
public List<String> getDeliverables()      { return deliverables; }
public List<String> getConstraints()       { return constraints; }
```

## Validaciones sin hallazgos

- `V11__add_assessment_drafts.sql` sigue la numeracion esperada despues de V9/V10 y define `assessment_drafts` con FK a `assessments`, FK self-reference, JSONB para `objectives`/`deliverables`/`constraints`, `created_at` y `UNIQUE (assessment_id, version_number)`.
- `AssessmentDraftJpaEntity` usa `@JdbcTypeCode(SqlTypes.JSON)` con `columnDefinition = "jsonb"` para las tres listas, consistente con la decision D-04.
- `findCurrentByAssessmentId(...)` devuelve la primera fila del query descendente por `version_number`; los tests fijan ese contrato.
- La forma persistida (`title`, `context`, `instructions`, `objectives`, `deliverables`, `constraints`) coincide con `agents/src/main/java/cl/gradeops/ai/agents/assessment/application/result/AssessmentResult.java`.
- `TRACEABILITY.md` ya marca `AssessmentDraft` como presente en AP/DO/W y registra el residual R-03 de `data-model.md`.
- `git diff --check origin/gradeops-api/story-01-assessment-creation-persistence...HEAD` paso sin errores.

## Verificacion ejecutada

- `gh pr view 48 --json ...`: PR #48 esta `OPEN`, no draft, base `gradeops-api/story-01-assessment-creation-persistence`, head `gradeops-api/story-01-assessment-creation-persistence--task-03-assessment-draft`, `mergeStateStatus = CLEAN`; checks visibles de Vercel en verde.
- `./mvnw test -Dtest=AssessmentDraftTest,AssessmentDraftPersistenceAdapterTest,AssessmentDraftPersistenceAdapterIntegrationTest` paso correctamente con Docker activo: 24 tests, 0 failures, 0 errors. Testcontainers levanto PostgreSQL 16, Flyway valido 11 migraciones y aplico el schema hasta V11.
