# Code review - Task 01: Assessment aggregate root

## Hallazgos

### MEDIUM - `Assessment.restore` permite aggregates incompletos

- **Archivo:** `src/main/java/cl/gradeops/ai/api/assessment/domain/model/Assessment.java`
- **Lineas:** 28-35
- **Tipo:** incumplimiento DDD / invariante de dominio incompleta

`Assessment.restore(...)` valida `teacherUid` y `status`, pero acepta `id == null` y `createdAt == null`. Eso permite reconstruir un aggregate que no cumple el contrato de la propia tarea (`id`, `teacherUid`, `status`, `createdAt`) y difiere el fallo a infraestructura: `AssessmentPersistenceMapper.toEntity(...)` puede explotar con NPE al acceder a `a.getId().value()`, o JPA/PostgreSQL puede rechazar `created_at` por `NOT NULL`.

Esto incumple `docs/gradeops-ai-java-guidelines/02-ddd-tactico.md`: el aggregate root debe proteger invariantes, y el metodo `restore` no debe ser una puerta trasera para estados invalidos.

Recomendacion:

- Validar `id` y `createdAt` en `restore(...)`.
- Agregar tests puros de dominio para `Assessment.restore(null, ...)` y `Assessment.restore(..., null createdAt)`.
- Considerar tests de `Assessment.create(...)` para bloquear `teacherUid` nulo/blank y asegurar `DRAFT`.

## Evidencia

```java
public static Assessment restore(AssessmentId id, String teacherUid, AssessmentStatus status, Instant createdAt) {
    if (teacherUid == null || teacherUid.isBlank()) throw new DomainInvariantViolationException("teacherUid must not be blank");
    if (status == null)                             throw new DomainInvariantViolationException("status must not be null");
    Assessment a = new Assessment();
    a.id = id;
    a.teacherUid = teacherUid;
    a.status = status;
    a.createdAt = createdAt;
    return a;
}
```

### MEDIUM - `findAllByTeacherId` expone filas parciales antes de task-10

- **Archivo:** `src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/out/persistence/AssessmentPersistenceAdapter.java`
- **Lineas:** 30-42
- **Tipo:** regresion incremental / incumplimiento de alcance atomico

La tarea dice explicitamente que task-01 no debe implementar la logica real del dashboard y que `findAllByTeacherId` debe seguir comportandose como `List.of()` hasta task-10, porque el titulo saldra del draft/brief cuando esas tablas existan. La implementacion actual consulta `assessments` y devuelve un `AssessmentSummaryResult` por fila con `title(null)`.

En cuanto task-06 empiece a crear `Assessment` + `AssessmentBrief`, `GET /api/v1/assessments` puede devolver elementos visibles con `title: null`, contadores en cero y `reportLink: null` antes de que task-10 haga el join real. Eso rompe el contrato temporal que esta tarea uso para mantener el dashboard estable.

Recomendacion:

- Para este task, mantener `findAllByTeacherId` devolviendo una lista vacia aunque use el adapter real, o documentar y ajustar explicitamente task-06/task-10 si se decide adelantar el listado parcial.
- Si se mantiene el listado parcial, agregar tests de controller/adapter que fijen el contrato esperado para `title` nulo y validar que el frontend/API lo toleran.

## Evidencia

```java
return jpaRepository.findAllByTeacherUid(teacherUid).stream()
        .map(e -> AssessmentSummaryResult.builder()
                .id(e.getId().toString())
                .title(null)
                .status(AssessmentStatus.valueOf(e.getStatus()))
                .submissionCount(0)
                .pendingApprovals(0)
                .reportLink(null)
                .build())
        .toList();
```

## Verificacion ejecutada

- `./mvnw test -Dtest=AssessmentPersistenceAdapterTest,AssessmentControllerTest` paso correctamente.
- `./mvnw test -Dtest=AssessmentPersistenceAdapterTest,AssessmentPersistenceAdapterIntegrationTest,AssessmentControllerTest` no pudo completar porque Testcontainers no encontro Docker (`/var/run/docker.sock` ausente). Los tests unitarios y de controller de esa corrida pasaron antes del fallo de ambiente.
- `git diff --check gradeops-api/story-01-assessment-creation-persistence...HEAD` paso sin whitespace errors.

