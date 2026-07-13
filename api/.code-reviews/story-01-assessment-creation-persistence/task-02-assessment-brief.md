# Code review - Task 02: AssessmentBrief entity + persistence

Estado: CAMBIOS SOLICITADOS.

## Hallazgos

### MEDIUM - El integration test compara `TIMESTAMPTZ` con nanosegundos y puede fallar de forma intermitente

- **Archivo:** `src/test/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/out/persistence/AssessmentBriefPersistenceAdapterIntegrationTest.java`
- **Lineas:** 73-86
- **Tipo:** test flakiness / inconsistencia con convencion local de persistencia

`AssessmentBrief.create(...)` usa `Instant.now()` y el test luego exige igualdad exacta entre `found.get().getCreatedAt()` y `brief.getCreatedAt()`. PostgreSQL guarda `TIMESTAMPTZ` con precision de microsegundos; si el `Instant` local trae nanosegundos, el valor rehidratado puede diferir aunque el adapter y la migracion esten correctos.

El propio repo ya documenta y aplica esta regla en `PasswordResetCodeJpaRepositoryIntegrationTest`: antes de afirmar igualdad sobre timestamps persistidos, trunca a `ChronoUnit.MICROS`. Este nuevo test no sigue esa convencion, asi que puede romper CI o la validacion local segun la precision del reloj/JDBC.

Recomendacion:

- Truncar el `createdAt` esperado a `ChronoUnit.MICROS` antes de persistir o antes de comparar.
- Alternativamente, usar una assertion tolerante a precision si el objetivo del test no es validar precision exacta.

Evidencia:

```java
AssessmentBrief brief = AssessmentBrief.create(assessment.getId(), "goal", "topic", "basic", "90min", "Java");
briefAdapter.save(brief);

Optional<AssessmentBrief> found = briefAdapter.findByAssessmentId(assessment.getId());

assertThat(found.get().getCreatedAt()).isEqualTo(brief.getCreatedAt());
```

### LOW - `TRACEABILITY.md` sigue marcando `AssessmentBrief` como ausente en AP

- **Archivo:** `.planning/active/003-assessment-creation/TRACEABILITY.md`
- **Linea:** 28
- **Tipo:** documentacion de estado inconsistente

La tarea cambia `task-02` y el story index a `DONE`, y agrega `AssessmentBrief.java`, el puerto, el adapter JPA, la migracion V10 y tests. Sin embargo, la matriz de terminos sigue diciendo `AssessmentBrief | AP = ❌`, lo que contradice el estado real del codigo y deja la trazabilidad de la planning apuntando a que el concepto todavia no existe en `src/`.

Recomendacion:

- Cambiar `AssessmentBrief` a `AP = ✅`.
- Considerar `DO = ✅` si el nuevo guide en `docs/guides/003-assessment-creation/.../task-02-assessment-brief.md` cuenta como documentacion entregada para este termino.

Evidencia:

```markdown
| `AssessmentBrief` | ❌ | N/A | ✅ | task-02. Field names must mirror `agents/`'s `AssessmentCommand`. |
```

## Verificacion ejecutada

- `./mvnw test -Dtest=AssessmentBriefTest,AssessmentBriefPersistenceAdapterTest` paso correctamente: 15 tests, 0 failures, 0 errors.
- `./mvnw test -Dtest=AssessmentBriefPersistenceAdapterIntegrationTest` paso correctamente con Docker activo: 3 tests, 0 failures, 0 errors. Flyway valido y aplico 10 migraciones, dejando el schema en version V10.
- `./mvnw test -Dtest=AssessmentBriefTest,AssessmentBriefPersistenceAdapterTest,AssessmentBriefPersistenceAdapterIntegrationTest` paso correctamente: 18 tests, 0 failures, 0 errors.
- `git diff --check c3dc070..HEAD` paso sin whitespace errors.
- Worktree local limpio antes de escribir este artefacto de review.
