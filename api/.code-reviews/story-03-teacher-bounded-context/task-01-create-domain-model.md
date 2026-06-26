# Code review - Task 01: Create teacher domain model

## Hallazgos

### MEDIUM - El aggregate `Teacher` no protege invariantes obligatorias

- **Archivo:** `src/main/java/cl/gradeops/ai/api/teacher/domain/model/Teacher.java`
- **Lineas:** 22-30, 33-51
- **Tipo:** incumplimiento DDD / mala practica / error latente de persistencia

`Teacher.provision(...)` y `Teacher.restore(...)` solo validan indirectamente el `firebaseUid` via `TeacherId`, pero aceptan `null` o valores blank para `firstName`, `lastName`, `email` y `authProvider`.

Esto incumple `docs/gradeops-ai-java-guidelines/02-ddd-tactico.md`: el aggregate root debe proteger invariantes y los value objects/estado de dominio deben validar lo que siempre debe cumplirse. Tambien contradice la intencion del task, que pide `TeacherTest` cubriendo invariantes.

Impacto:

- El dominio puede construir un `Teacher` invalido.
- Los errores se difieren a JPA o Firebase, generando fallos mas tardios y menos expresivos.
- `RegisterHandler` puede persistir `identity.email()` sin validacion de dominio si el proveedor externo entrega un email nulo.

Recomendacion:

- Validar en `Teacher.provision(...)` los campos obligatorios con `Objects.requireNonNull` y validacion blank donde aplique.
- Validar `authProvider`.
- Agregar tests de dominio para `email`, `firstName`, `lastName` y `authProvider` invalidos.

## Evidencia

```java
public static Teacher provision(String firebaseUid, String firstName, String lastName,
                                String email, AuthProvider authProvider) {
    Teacher t = new Teacher();
    t.id = new TeacherId(firebaseUid);
    t.firstName = firstName;
    t.lastName = lastName;
    t.email = email;
    t.authProvider = authProvider;
    ...
}
```

## Verificacion ejecutada

`./mvnw test -Dtest=TeacherTest,ProvisionTeacherHandlerTest,UpdatePilotFlagsHandlerTest,TeacherPersistenceAdapterTest,InternalTeacherControllerTest,FirebaseAuthAdapterTest -q` paso correctamente, pero no cubre estas invariantes.
