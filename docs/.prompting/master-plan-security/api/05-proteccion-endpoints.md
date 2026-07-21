# Protección de endpoints

## Regla de diseño

Los endpoints expresan capacidades mediante authorities. Los roles se traducen centralmente a permisos, evitando dispersar `hasRole(...)` por todo el código.

Ejemplo:

```java
@PostMapping("/assessments")
@PreAuthorize("hasAuthority('assessment:create')")
public CreateAssessmentBriefResponse createAssessmentBrief(...) {
}
```

## Matriz recomendada

| Endpoint/capacidad | Authority | Defensa adicional |
|---|---|---|
| `POST /api/v1/auth/register` | Público controlado | Validar token Firebase suministrado |
| `POST /api/v1/auth/forgot-password` | Público | Rate limit y respuesta no enumerativa |
| `POST /api/v1/auth/reset-password` | Público con código | Un solo uso, expiración y rate limit |
| `POST /api/v1/auth/sign-out` | Usuario autenticado | Cuenta activa |
| `GET /api/v1/assessments` | `assessment:read` | Filtrar por UID |
| `POST /api/v1/assessments` | `assessment:create` | Plan/cuenta habilitados |
| `GET /api/v1/assessments/{id}/draft` | `assessment:read` | Ownership |
| `PATCH /api/v1/assessments/{id}/draft` | `assessment:update` | Ownership + estado editable |
| Generar/regenerar draft | `assessment:generate` | Ownership + cuota + idempotencia |
| Aprovisionar Teacher | `teacher:provision` | Operator autenticado o servicio autorizado |
| Modificar flags | `teacher:pilot-flags:update` | Auditoría con principal real |

## Semántica HTTP

- Sin credenciales o token inválido: `401`.
- Identidad válida sin permiso general: `403`.
- Recurso ajeno en dominio docente: `404` para evitar enumeración.
- Recurso propio pero transición inválida: error de dominio coherente (`409` o `422`, según convención final).

La política debe documentarse y probarse de forma uniforme.
