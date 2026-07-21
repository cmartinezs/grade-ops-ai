# Estado actual y evidencia

## Entrada interna

`AssessmentController` expone `POST /internal/agents/assessment` y recibe `AssessmentCommand` directamente. No usa Bean Validation en el controller; la validación vive en `AssessmentAgentOrchestrator.validate`.

`InternalAuthFilter` aplica a `/internal/**`, lee `X-Internal-Key` y compara mediante `String.equals`; ante ausencia/error devuelve `403`. `SharedWebConfig` registra correlación antes de auth.

`application.yml` configura `app.internal.secret` desde `INTERNAL_API_SECRET`, con fallback peligroso `change-me-in-production`.

## Ejecución

El comando incluye brief, regeneración, `provider` y `model`. El selector rechaza providers desconocidos, pero el modelo literal no usa allowlist: un valor no soportado se delega al proveedor.

El prompt está versionado en `resources/prompts/assessment-generation.st`. Se crea una instancia StringTemplate por llamada, evitando contaminación concurrente. Los textos del usuario se interpolan en el prompt.

## Salida y evidencia

Se valida presencia de campos del resultado. Se calculan SHA-256 de prompt y respuesta, tokens/costo cuando están disponibles, timestamps y estado. `AgentGlobalExceptionHandler` evita stack trace y prompt raw en la respuesta.

## Brechas comprobadas

- No OIDC, identidad individual, scopes o rotación por caller.
- Secreto con default inseguro y comparación no constant-time.
- Sin rate limit, concurrencia, deadline, max tokens/costo o idempotencia.
- Caller elige provider/model.
- No política explícita de prompt injection.
- No validación de tamaño de strings/listas ni output JSON Schema versionado.
- No tool/sandbox actual; deben diseñarse antes de incorporarlos.
