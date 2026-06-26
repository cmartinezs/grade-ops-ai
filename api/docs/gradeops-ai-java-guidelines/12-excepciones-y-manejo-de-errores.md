# 12 — Excepciones y manejo de errores

## Principio rector

GradeOps AI usa una jerarquía de excepciones propia, unchecked (extienden `RuntimeException`), organizada por capa arquitectónica. **Ninguna capa lanza excepciones de la API de Java** (`IllegalArgumentException`, `NullPointerException`, `IllegalStateException`) ni las captura para re-lanzarlas como `RuntimeException` genérica. Toda excepción explícita que cruza una frontera de capa hereda de `DomainException`, `ApplicationException` o `InfrastructureException`.

El resultado es que el código no se ensucia con `throws` ni con bloques `catch` defensivos: las excepciones propias son unchecked, fluyen solas hasta `GlobalExceptionHandler`, y este es el único punto que las traduce a respuestas HTTP.

---

## Jerarquía de excepciones

```
RuntimeException
├── DomainException                       (shared/domain/exception/)
│   ├── DomainInvariantViolationException
│   ├── DuplicateEmailException
│   ├── InvalidTokenException
│   └── ResourceNotFoundException
│
├── ApplicationException                  (shared/application/exception/)
│   └── UnsupportedProviderException
│
└── InfrastructureException               (shared/infrastructure/exception/)
    ├── AuthProviderException
    ├── EmailDeliveryException
    └── StorageException
```

### Regla de ubicación

| Tipo base | Paquete | Cuándo usarla |
|---|---|---|
| `DomainException` | `shared/domain/exception/` o `<feature>/domain/exception/` | Invariante de dominio rota, recurso no encontrado, token inválido, email duplicado. |
| `ApplicationException` | `shared/application/exception/` o `<feature>/application/exception/` | Precondición de caso de uso no cumplida, proveedor no soportado, conflicto de estado de aplicación. |
| `InfrastructureException` | `shared/infrastructure/exception/` o `<feature>/infrastructure/exception/` | Fallo de adaptador externo: Firebase, correo, storage, base de datos inesperada. |

---

## Clases base

### DomainException

```java
package cl.gradeops.ai.api.shared.domain.exception;

public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
    protected DomainException(String message, Throwable cause) { super(message, cause); }
}
```

### ApplicationException

```java
package cl.gradeops.ai.api.shared.application.exception;

public abstract class ApplicationException extends RuntimeException {
    protected ApplicationException(String message) { super(message); }
    protected ApplicationException(String message, Throwable cause) { super(message, cause); }
}
```

### InfrastructureException

```java
package cl.gradeops.ai.api.shared.infrastructure.exception;

public abstract class InfrastructureException extends RuntimeException {
    protected InfrastructureException(String message) { super(message); }
    protected InfrastructureException(String message, Throwable cause) { super(message, cause); }
}
```

Las tres son `abstract` para que no se instancien directamente: cada uso requiere crear una subclase con nombre significativo.

---

## Excepciones de dominio

### DomainInvariantViolationException

Reemplaza a `IllegalArgumentException`, `IllegalStateException` y `Objects.requireNonNull` en el dominio.

```java
package cl.gradeops.ai.api.shared.domain.exception;

public class DomainInvariantViolationException extends DomainException {
    public DomainInvariantViolationException(String message) { super(message); }
}
```

Uso en entidades y value objects:

```java
// value object
public record TeacherId(String value) {
    public TeacherId {
        if (value == null)   throw new DomainInvariantViolationException("firebaseUid must not be null");
        if (value.isBlank()) throw new DomainInvariantViolationException("firebaseUid must not be blank");
    }
}

// método de fábrica de agregado
public static Teacher provision(String uid, String firstName, ..., AuthProvider authProvider) {
    if (firstName == null || firstName.isBlank())
        throw new DomainInvariantViolationException("firstName must not be null or blank");
    if (authProvider == null)
        throw new DomainInvariantViolationException("authProvider must not be null");
    ...
}
```

### Excepciones de dominio específicas

Para condiciones que tienen semántica de negocio propia se crean subclases de `DomainException`:

```java
// feature/domain/exception/
public class DuplicateEmailException extends DomainException {
    private final String email;
    public DuplicateEmailException(String email) {
        super("Email already registered: " + email);
        this.email = email;
    }
    public String getEmail() { return email; }
}

public class ResourceNotFoundException extends DomainException {
    private final String resourceId;
    public ResourceNotFoundException(String resourceId) {
        super("Resource not found: " + resourceId);
        this.resourceId = resourceId;
    }
    public String getResourceId() { return resourceId; }
}

public class InvalidTokenException extends DomainException {
    public InvalidTokenException(String message) { super(message); }
}
```

Estas excepciones pueden llevar datos adicionales (como `email` o `resourceId`) que el `GlobalExceptionHandler` usa para construir la respuesta sin necesidad de parsear el mensaje.

---

## Excepciones de aplicación

La capa de aplicación lanza `ApplicationException` cuando una precondición de negocio del caso de uso no se cumple y el error no corresponde al dominio en sí sino a la orquestación.

Ejemplo:

```java
package cl.gradeops.ai.api.auth.application.exception;

public class UnsupportedProviderException extends ApplicationException {
    public UnsupportedProviderException(String message) { super(message); }
}
```

Uso en handler:

```java
if (command.provider() != SignInProvider.EMAIL_PASSWORD) {
    throw new UnsupportedProviderException(
        "Password reset is only supported for EMAIL_PASSWORD provider, got: " + command.provider()
    );
}
```

---

## Excepciones de infraestructura

Los adaptadores de salida capturan excepciones del SDK externo (checked o unchecked) y las re-lanzan como `InfrastructureException`. Esto aisla el código de aplicación de las APIs de terceros.

### AuthProviderException

```java
package cl.gradeops.ai.api.auth.infrastructure.exception;

public class AuthProviderException extends InfrastructureException {
    public AuthProviderException(String message, Throwable cause) { super(message, cause); }
}
```

### EmailDeliveryException

```java
package cl.gradeops.ai.api.shared.infrastructure.exception;

public class EmailDeliveryException extends InfrastructureException {
    public EmailDeliveryException(String message, Throwable cause) { super(message, cause); }
}
```

### StorageException

```java
package cl.gradeops.ai.api.shared.infrastructure.exception;

public class StorageException extends InfrastructureException {
    public StorageException(String message, Throwable cause) { super(message, cause); }
}
```

Uso en adaptador:

```java
// FirebaseAuthAdapter
@Override
public void revokeRefreshTokens(String uid) {
    try {
        firebaseAuth.revokeRefreshTokens(uid);
    } catch (FirebaseAuthException e) {
        throw new AuthProviderException("Failed to revoke refresh tokens for uid=" + uid, e);
    }
}

// JavaMailEmailService
private void sendHtml(String to, String subject, String html) {
    try {
        ...
        mailSender.send(msg);
    } catch (MessagingException | UnsupportedEncodingException e) {
        throw new EmailDeliveryException("Failed to send email to " + to, e);
    }
}
```

---

## Patrón por capa

### Dominio

- Nunca captura excepciones.
- Lanza `DomainInvariantViolationException` o subclase propia de `DomainException` ante invariantes rotas.
- No conoce `ApplicationException` ni `InfrastructureException`.

### Aplicación

- Puede capturar excepciones de infraestructura si necesita compensación (saga manual), pero **siempre re-lanza** la misma excepción sin envolverla.
- Lanza `ApplicationException` para precondiciones de caso de uso no cumplidas.
- No captura `DomainException`; las deja fluir hasta el handler.

```java
// ProvisionTeacherHandler — saga con compensación
String firebaseUid = null;
boolean committed = false;
try {
    firebaseUid = authPort.createUser(email, displayName);
    teacherRepository.save(teacher);
    ...
    committed = true;
    return result;
} finally {
    if (!committed && firebaseUid != null) {
        try {
            authPort.deleteUser(firebaseUid);
        } catch (Exception compensationEx) {
            // best-effort: no relanzar para no enmascarar la excepción original
            log.warn("Firebase compensation failed: uid={} cause={}", firebaseUid, compensationEx.getMessage());
        }
    }
}
```

El `finally` con flag `committed` es el único patrón válido para sagas: no hay `catch` en el flujo principal, la excepción original fluye intacta hacia `GlobalExceptionHandler`, y la compensación se ejecuta sin interferir con ella. El `catch(Exception)` del bloque de compensación es la única excepción a la regla: es necesario para evitar que un fallo en la compensación enmascare la excepción original. Fuera de este contexto muy específico, `catch(Exception)` está prohibido.

### Infraestructura

- Captura excepciones del SDK o proveedor externo.
- Las envuelve en la subclase de `InfrastructureException` que corresponda.
- Incluye siempre la causa (`Throwable cause`) para preservar el stack trace completo.
- Nunca lanza `RuntimeException` genérica.

---

## GlobalExceptionHandler

Es el único punto donde las excepciones se traducen a respuestas HTTP. Organizado en secciones:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Excepciones de dominio específicas (más específicas primero)
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) { ... }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) { ... }

    // 2. Raíces de jerarquía propia
    @ExceptionHandler(DomainInvariantViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainInvariant(DomainInvariantViolationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiErrorResponse.of("DOMAIN_INVARIANT_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleApplication(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiErrorResponse.of("APPLICATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ApiErrorResponse> handleInfrastructure(InfrastructureException ex) {
        log.warn("Infrastructure failure: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("INFRASTRUCTURE_ERROR"));
    }

    // 3. Validación HTTP
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<FieldErrorResponse>> handleValidation(...) { ... }

    // 4. Catch-all (nunca debe activarse en producción estable)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("INTERNAL_ERROR"));
    }
}
```

### Reglas de mapping HTTP

| Tipo de excepción | HTTP status | Cuándo |
|---|---|---|
| `DomainInvariantViolationException` | 422 Unprocessable Content | Invariante rota en dominio |
| `ApplicationException` (y subclases) | 422 Unprocessable Content | Precondición de caso de uso no cumplida |
| `InfrastructureException` (y subclases) | 500 Internal Server Error | Fallo de adaptador externo |
| `DuplicateEmailException` | 409 Conflict | Email ya registrado |
| `ResourceNotFoundException` | 404 Not Found | Recurso no existe |
| `InvalidTokenException` | 401 Unauthorized | Token inválido o revocado |
| `MethodArgumentNotValidException` | 422 Unprocessable Content | Bean Validation fallida |
| `HttpMessageNotReadableException` | 400 Bad Request | Body malformado o tipo incorrecto |
| `MissingServletRequestParameterException` | 400 Bad Request | Parámetro requerido ausente |
| `Exception` (catch-all) | 500 Internal Server Error | Error no previsto |

### Mensaje vs. código de error

- El mensaje de la excepción (`ex.getMessage()`) se incluye en la respuesta **solo** cuando es seguro exponerlo al cliente: errores de dominio e invariantes (indican un problema en la petición).
- Los errores de infraestructura retornan un código genérico (`INFRASTRUCTURE_ERROR`) sin mensaje interno. El detalle va al log del servidor.
- El catch-all nunca expone el mensaje (`INTERNAL_ERROR` genérico).

### Logging

- `InfrastructureException`: `log.warn(...)` con causa completa para facilitar diagnóstico.
- `Exception` (catch-all): `log.error(...)` con causa completa.
- Excepciones de dominio y aplicación: sin log en el handler (son errores esperados del cliente).

---

## Lo que está prohibido

### Lanzar excepciones de la API de Java

```java
// PROHIBIDO en cualquier capa del proyecto
throw new IllegalArgumentException("email must not be null");
throw new IllegalStateException("already used");
throw new NullPointerException("authProvider");
throw new RuntimeException("Failed to connect", e);
```

### Capturar excepciones genéricas solo para relanzar

```java
// PROHIBIDO — catch innecesario que solo envuelve
try {
    authPort.createUser(email, name);
} catch (Exception ex) {
    throw new RuntimeException(ex);  // nunca
}
```

### Capturar y silenciar

```java
// PROHIBIDO
try {
    sendEmail(to, subject, body);
} catch (Exception e) {
    // silencio
}
```

### Capturar `Throwable` o `Error`

```java
// PROHIBIDO
try {
    ...
} catch (Throwable t) { ... }
```

`Error` (OOM, StackOverflow) no debe capturarse en código de negocio.

---

## Guía para crear nuevas excepciones

Antes de crear una nueva excepción, verificar:

1. **¿Ya existe?** Buscar en `shared/domain/exception/`, `shared/application/exception/` y `shared/infrastructure/exception/`.
2. **¿Es de dominio, aplicación o infraestructura?** Determinar en qué capa se origina el error.
3. **¿Necesita datos adicionales?** Si el `GlobalExceptionHandler` necesita campos específicos (como `email` en `DuplicateEmailException`), agregarlos.
4. **¿Es compartida o específica de feature?** Si solo aplica a una feature, crearla en `<feature>/<capa>/exception/`. Si puede surgir en múltiples features, crearla en `shared/<capa>/exception/`.

Estructura de la clase:

```java
// Excepción de dominio específica de feature
package cl.gradeops.ai.api.rubric.domain.exception;

import cl.gradeops.ai.api.shared.domain.exception.DomainException;

public class RubricAlreadyApprovedException extends DomainException {

    private final String rubricId;

    public RubricAlreadyApprovedException(String rubricId) {
        super("Rubric already approved: " + rubricId);
        this.rubricId = rubricId;
    }

    public String getRubricId() { return rubricId; }
}
```

Y registrar el handler correspondiente en `GlobalExceptionHandler`:

```java
@ExceptionHandler(RubricAlreadyApprovedException.class)
public ResponseEntity<ApiErrorResponse> handleRubricAlreadyApproved(RubricAlreadyApprovedException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse.of("RUBRIC_ALREADY_APPROVED", ex.getRubricId()));
}
```

---

## Excepciones y ArchUnit

La arquitectura hexagonal impone que las excepciones no crucen la frontera incorrecta. Las reglas de `HexagonalArchitectureTest` que aplican aquí:

- El dominio no debe importar `ApplicationException` ni `InfrastructureException`.
- La aplicación no debe importar `InfrastructureException` directamente (salvo para re-lanzar en sagas).
- La infraestructura puede conocer tanto `DomainException` como `InfrastructureException`.

Si se añade una nueva excepción en la capa equivocada, el test de arquitectura lo detecta en CI.

---

## Tests

Toda excepción de dominio o aplicación debe tener tests unitarios directos sobre el método que la lanza.

### Test de invariante de dominio

```java
@Test
void shouldRejectNullAuthProviderWhenProvisioningTeacher() {
    assertThatThrownBy(() -> Teacher.provision("uid", "Ana", "Soto", "a@x.com", null))
            .isInstanceOf(DomainInvariantViolationException.class)
            .hasMessageContaining("authProvider");
}
```

### Test de precondición de aplicación

```java
@Test
void shouldThrowWhenProviderIsNotEmailPassword() {
    IssuePasswordResetCodeCommand command = IssuePasswordResetCodeCommand.builder()
            .teacherUid("uid").ttlMinutes(30).provider(SignInProvider.GOOGLE).build();

    assertThatThrownBy(() -> handler.execute(command))
            .isInstanceOf(UnsupportedProviderException.class);
}
```

### Test de adaptador de infraestructura

```java
@Test
void shouldThrowInvalidTokenExceptionWhenFirebaseThrowsAuthException() throws Exception {
    when(firebaseAuth.verifyIdToken("bad", true)).thenThrow(mock(FirebaseAuthException.class));

    assertThatThrownBy(() -> adapter.verifyToken("bad"))
            .isInstanceOf(InvalidTokenException.class);
}
```

Los tests **no deben** afirmar `RuntimeException.class` ni `Exception.class` como tipo esperado. Siempre usar el tipo concreto.

---

## Anti-patterns

- `throw new RuntimeException(e)` — envuelve sin añadir semántica.
- `throw new IllegalArgumentException(...)` — no pertenece a la jerarquía del proyecto.
- `catch (RuntimeException ex) { ... throw ex; }` — capturar la raíz de nuestras excepciones solo para re-lanzar; usar `finally` en su lugar.
- `catch (Exception ex) { throw new RuntimeException(ex); }` — wrapping inútil.
- `catch (Exception ex) { log.error(...); }` — silencia sin re-lanzar.
- `throw new InfrastructureException(...)` directamente — la base es `abstract`, no se instancia.
- Excepción de dominio en paquete de infraestructura o viceversa.
- `GlobalExceptionHandler` que captura excepciones de infraestructura y expone `ex.getMessage()` al cliente.
- Tests que asientan `isInstanceOf(RuntimeException.class)` cuando el tipo real es más específico.
