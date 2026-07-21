# Ownership y autorización por recurso

## Defensa en tres niveles

Para modificar un assessment no basta con poseer `assessment:update`:

1. Authority global: el actor tiene capacidad de actualización.
2. Scope: el assessment pertenece al Teacher autenticado.
3. Dominio: el estado actual permite editar el borrador.

## Consulta scoped

Patrón recomendado:

```java
assessmentRepository
    .findByIdAndTeacherUid(assessmentId, actor.uid())
    .orElseThrow(() -> new ResourceNotFoundException(assessmentId));
```

Luego:

```java
assessment.ensureDraftCanBeEdited();
```

Este enfoque evita cargar explícitamente recursos ajenos y preserva la respuesta `404` ya adoptada.

## Ubicación de reglas

No se recomienda convertir todas las reglas de ownership en expresiones complejas como:

```java
@PreAuthorize("@ownership.canEdit(#id, authentication)")
```

Eso puede duplicar consultas, ocultar I/O dentro de expresiones y trasladar invariantes al framework. `@PreAuthorize` debe cubrir la capacidad general; repositorio/caso de uso cubren scope; agregado cubre estado.

## Generalización necesaria

El patrón debe aplicarse sistemáticamente a todo recurso académico que dependa de Teacher u organización:

- Assessments y drafts.
- Rúbricas.
- Invitaciones y submissions.
- Sugerencias de grading y feedback.
- Operaciones GenAI.
- Reportes.
- Artefactos y exports.
