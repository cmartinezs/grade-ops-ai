# 09 — API REST, DTOs y validación

## Rol de la API

La API es un adapter de entrada. Su responsabilidad es traducir HTTP hacia casos de uso.

Un controller debe:

- Recibir request.
- Validar formato básico.
- Convertir request a command/query.
- Invocar use case.
- Convertir result a response.
- Retornar HTTP status correcto.

Un controller no debe:

- Calcular reglas de negocio.
- Acceder a JPA repositories.
- Consumir Gemini directamente.
- Persistir entidades.
- Contener transacciones.

## Estructura

```text
infrastructure.adapter.in.web
├── AssessmentController.java
├── request
│   └── GenerateAssessmentRequest.java
├── response
│   └── GenerateAssessmentResponse.java
├── mapper
│   └── AssessmentWebMapper.java
└── error
    └── AssessmentExceptionHandler.java
```

## Controller

```java
@RestController
@RequestMapping("/api/v1/assessments")
@RequiredArgsConstructor
public class AssessmentController {

  private final GenerateAssessmentUseCase generateAssessmentUseCase;
  private final AssessmentWebMapper mapper;

  @PostMapping("/generate")
  public ResponseEntity<GenerateAssessmentResponse> generate(
      @Valid @RequestBody GenerateAssessmentRequest request,
      Authentication authentication
  ) {
    GenerateAssessmentCommand command = mapper.toCommand(request, authentication);
    GenerateAssessmentResult result = generateAssessmentUseCase.execute(command);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
  }
}
```

## DTO Request

Usar DTOs separados por endpoint.

```java
@Builder
public record GenerateAssessmentRequest(
    @NotBlank
    @Size(max = 160)
    String title,

    @NotEmpty
    @Size(max = 10)
    List<@NotBlank @Size(max = 300) String> learningObjectives,

    @NotNull
    DifficultyLevel difficultyLevel,

    @Min(10)
    @Max(240)
    int durationMinutes
) {
}
```

## DTO Response

No retornar dominio ni JPA entities.

```java
@Builder
public record GenerateAssessmentResponse(
    UUID assessmentId,
    String status,
    List<String> learningObjectives,
    RubricResponse rubric
) {
}
```

## Request vs Command

Aunque se parezcan, no son lo mismo.

| Objeto | Capa | Contiene |
|---|---|---|
| Request | API | Datos HTTP, strings, validación de formato. |
| Command | Application | Intención de negocio, IDs del usuario/tenant, value objects. |
| Domain Object | Domain | Reglas, invariantes y comportamiento. |

## Validación

### API validation

Usar Bean Validation para formato:

- `@NotNull`.
- `@NotBlank`.
- `@Size`.
- `@Min`.
- `@Max`.
- `@Pattern`.

### Application validation

Validar autorización contextual, existencia, idempotencia, créditos y reglas de flujo.

### Domain validation

Validar invariantes que siempre deben cumplirse.

Ejemplo:

- Un `Score` no puede ser negativo.
- Una evaluación publicada debe tener rúbrica.
- Un feedback aprobado no puede volver a draft sin transición explícita.

## HTTP status

| Caso | Status |
|---|---:|
| Creación exitosa | `201 Created` |
| Consulta exitosa | `200 OK` |
| Comando sin body de respuesta | `204 No Content` |
| Validación de request | `400 Bad Request` |
| No autenticado | `401 Unauthorized` |
| Sin permisos | `403 Forbidden` |
| Recurso inexistente | `404 Not Found` |
| Conflicto de negocio | `409 Conflict` |
| Error de proveedor externo recuperable | `502 Bad Gateway` o `503 Service Unavailable` |
| Rate limit / créditos insuficientes según diseño | `402 Payment Required`, `409 Conflict` o `429 Too Many Requests` según decisión del producto |

## Respuesta de error estándar

```java
@Builder
public record ApiErrorResponse(
    String code,
    String message,
    String correlationId,
    Instant timestamp,
    List<FieldErrorResponse> fieldErrors
) {
}
```

Ejemplo:

```json
{
  "code": "ASSESSMENT_CANNOT_BE_PUBLISHED",
  "message": "Assessment cannot be published without rubric",
  "correlationId": "01J...",
  "timestamp": "2026-06-23T10:15:30Z",
  "fieldErrors": []
}
```

## Exception handler

Centralizar traducción de errores.

```java
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(DomainException.class)
  ResponseEntity<ApiErrorResponse> handleDomainException(DomainException exception) {
    // ...
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
    // ...
  }
}
```

## OpenAPI

Toda API pública o interna consumida por frontend debe estar documentada.

Reglas:

- Endpoint nuevo implica actualizar OpenAPI.
- Cambios breaking deben versionarse.
- Requests/responses deben tener ejemplos cuando sea útil.
- Errores relevantes deben documentarse.

## Versionamiento API

Usar prefijo:

```text
/api/v1
```

No introducir `v2` hasta que exista una incompatibilidad real.

## Paginación

Para listados:

```text
GET /api/v1/assessments?page=0&size=20&sort=createdAt,desc
```

Response:

```java
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
```

## Seguridad en API

- Nunca confiar en `teacherId`, `tenantId` o `userId` enviados por el cliente si vienen del token.
- Extraer identidad desde el contexto autenticado.
- Validar ownership/tenant en aplicación.
- No exponer prompts crudos, submissions completas o datos personales innecesarios.

## Anti-patterns

- Controller con lógica de negocio.
- `ResponseEntity<?>` por comodidad.
- Retornar `Map<String, Object>`.
- Retornar entidades JPA.
- Validar reglas profundas solo con annotations.
- Exponer stack traces.
- Error messages inconsistentes.
