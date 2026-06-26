# Code review - Task 05: Create ProvisionTeacherHandler

## Hallazgos

### MEDIUM - La compensacion no esta cubierta para fallo en emision del codigo

- **Archivo:** `src/test/java/cl/gradeops/ai/api/teacher/application/usecase/ProvisionTeacherHandlerTest.java`
- **Lineas:** 43-72
- **Tipo:** cobertura insuficiente / incumplimiento del task

El objetivo del task exige compensar eliminando el usuario Firebase si falla el guardado en DB **o la emision del codigo**. La implementacion actual en `ProvisionTeacherHandler` compensa cualquier excepcion despues de `createUser`, pero los tests solo cubren fallo de `teacherRepository.save(...)`.

Impacto:

- Una regresion futura podria mover o limitar el `catch` y dejar usuarios Firebase huerfanos cuando falle `issuePasswordResetCodeUseCase.execute(...)`.
- La suite no prueba una parte explicita del flujo saga descrito en el task.

Recomendacion:

- Agregar un test donde `teacherRepository.save(...)` succeed y `issuePasswordResetCodeUseCase.execute(...)` lance una excepcion.
- Verificar `authPort.deleteUser(firebaseUid)` y propagacion de la excepcion original.

### LOW - El `catch (Exception)` silenciosamente descarta fallos de compensacion

- **Archivo:** `src/main/java/cl/gradeops/ai/api/teacher/application/usecase/ProvisionTeacherHandler.java`
- **Lineas:** 57-60
- **Tipo:** observabilidad / code smell

La compensacion es best-effort, lo cual esta alineado con el task, pero el fallo de `deleteUser(...)` se descarta sin log ni metrica.

Impacto:

- Si Firebase no puede borrar el usuario, queda un recurso externo huerfano y no hay rastro operacional para remediarlo.
- Incumple el espiritu de `docs/gradeops-ai-java-guidelines/11-seguridad-observabilidad-y-auditoria.md`, que pide logs estructurados y contexto para operaciones relevantes.

Recomendacion:

- Registrar un `warn` sin datos sensibles cuando falle la compensacion, incluyendo `firebaseUid` y causa resumida.

## Verificacion ejecutada

`./mvnw test -Dtest=TeacherTest,ProvisionTeacherHandlerTest,UpdatePilotFlagsHandlerTest,TeacherPersistenceAdapterTest,InternalTeacherControllerTest,FirebaseAuthAdapterTest -q` paso correctamente.
