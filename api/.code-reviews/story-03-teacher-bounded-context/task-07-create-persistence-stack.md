# Code review - Task 07: Create persistence stack

## Hallazgos

### LOW - Inconsistencia entre task, story y guideline sobre visibilidad de `TeacherJpaRepository`

- **Archivo:** `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/out/persistence/TeacherJpaRepository.java`
- **Lineas:** 7-10
- **Tipo:** incumplimiento documental / inconsistencia de arquitectura

`TeacherJpaRepository` esta declarado `public`. Esto cumple la story principal, que marca como done criterion que sea publico, pero incumple el task 07, que pide explicitamente que sea package-private. La guideline de persistencia tambien recomienda mantener los Spring Data repositories package-private cuando solo los usa el adapter.

La causa probable es el wiring actual: `TeacherConfig` vive en `teacher.infrastructure.config`, un paquete distinto a `teacher.infrastructure.adapter.out.persistence`, por lo que no puede recibir un repository package-private como parametro.

Impacto:

- La API tecnica de infraestructura queda mas expuesta de lo necesario.
- El task 07 y la story principal no son verificables de forma consistente.

Recomendacion:

- Resolver la decision arquitectonica en la planning: o se acepta `public` por el wiring en `TeacherConfig`, o se mueve el bean que consume el repository a un paquete que pueda respetar package-private, o se documenta una excepcion explicita.

## Evidencia

- `story-03-teacher-bounded-context.md` lineas 38-41: la story afirma que `TeacherJpaRepository` debe ser publico.
- `task-07-create-persistence-stack.md` lineas 12, 18, 22 y 168: el task pide package-private.
- `docs/gradeops-ai-java-guidelines/08-persistencia-jpa.md`: recomienda package-private cuando solo lo usa el adapter.

## Verificacion ejecutada

`./mvnw test -Dtest=TeacherTest,ProvisionTeacherHandlerTest,UpdatePilotFlagsHandlerTest,TeacherPersistenceAdapterTest,InternalTeacherControllerTest,FirebaseAuthAdapterTest -q` paso correctamente.
