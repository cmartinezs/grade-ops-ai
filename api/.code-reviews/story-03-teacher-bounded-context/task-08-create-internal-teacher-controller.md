# Code review - Task 08: Create InternalTeacherController

## Hallazgos

### HIGH - Los requests HTTP no tienen Bean Validation ni `@Valid`

- **Archivos:**
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/InternalTeacherController.java`
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/request/ProvisionTeacherRequest.java`
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/request/UpdatePilotFlagsRequest.java`
- **Lineas:** controller 34, 49; request records 3-9
- **Tipo:** incumplimiento guideline API REST / mala practica / error de contrato HTTP

Los endpoints reciben `@RequestBody` sin `@Valid` y los DTOs no declaran restricciones (`@NotBlank`, `@Email`, `@Size`, etc.). Esto incumple `docs/gradeops-ai-java-guidelines/09-api-rest-dtos-validacion.md`, donde el controller debe validar formato basico y los request DTOs deben usar Bean Validation.

Impacto:

- `POST /internal/teachers` acepta campos blank si no son null.
- Un body vacio o campos null pueden terminar en `NullPointerException`/errores internos en vez de `400 Bad Request`.
- `PATCH /internal/teachers/{uid}/flags` acepta valores arbitrarios para `planType`, `setBy`, `evidenceLink` y campos de texto sin limites.
- Los tests de controller no cubren request invalido, pese a que la guideline de testing exige validar request y traduccion de errores.

Recomendacion:

- Agregar `@Valid` a ambos parametros `@RequestBody`.
- Agregar constraints en `ProvisionTeacherRequest`: `@NotBlank` para nombres, `@NotBlank @Email` para email, y `@Size` segun limites de negocio/DB.
- Agregar constraints razonables en `UpdatePilotFlagsRequest`, por ejemplo `@Size` y `@Pattern` para `planType` si el dominio permitido esta acotado.
- Agregar tests `@WebMvcTest` para payload invalido y body faltante, esperando `400`.

### MEDIUM - Construccion manual del invite link puede generar URLs mal formadas

- **Archivo:** `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/InternalTeacherController.java`
- **Linea:** 42
- **Tipo:** code smell / robustez de API

El controller concatena `webBaseUrl + "/reset-password?code=" + result.rawCode()`. Si `gradeops.web.base-url` termina con `/`, el resultado queda con doble slash. Si `rawCode` en el futuro contiene caracteres reservados, no se encodea.

Impacto:

- Links inconsistentes segun configuracion.
- Fragilidad si cambia el formato del codigo.

Recomendacion:

- Normalizar el base URL o construir el link con `UriComponentsBuilder`.
- Agregar test para `webBaseUrl` con slash final.

## Verificacion ejecutada

`./mvnw test -Dtest=TeacherTest,ProvisionTeacherHandlerTest,UpdatePilotFlagsHandlerTest,TeacherPersistenceAdapterTest,InternalTeacherControllerTest,FirebaseAuthAdapterTest -q` paso correctamente, pero la suite no cubre payloads invalidos ni normalizacion de URL.
