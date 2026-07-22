# Contratos y simuladores

## Contratos como fuente de verdad

Definir y versionar:

- OpenAPI para la API consumida por Web.
- OpenAPI para la API interna consumida por API hacia Agents.
- JSON Schema para comandos/resultados de agentes y respuestas estructuradas del LLM.

El pipeline debe detectar breaking changes y generar/verificar clientes o tipos. Los tipos de Web continúan fluyendo desde API; no se mantienen DTOs independientes a mano.

## Estrategia recomendada

Combinar:

1. validación provider-side contra OpenAPI/JSON Schema;
2. tests consumer-side con ejemplos reales del contrato;
3. compatibility check antes de integrar cambios;
4. integración Compose para confirmar wiring/configuración.

Pact puede añadirse si crece el número de consumidores o el ritmo de evolución; no es obligatorio para el MVP si OpenAPI y schemas ya se gobiernan correctamente.

## Catálogo único de escenarios

Los simuladores deben leer fixtures versionados, por ejemplo:

```text
testkit/fixtures/firebase/
testkit/fixtures/api/
testkit/fixtures/agents/
testkit/fixtures/genai/
testkit/scenarios/
```

Cada escenario debe tener identificador estable, request, response, latencia y estado esperado:

- `happy-path`
- `unauthorized`
- `forbidden`
- `not-found`
- `validation-error`
- `timeout`
- `rate-limited`
- `malformed-output`
- `provider-unavailable`

## Anti-divergencia

- Los mocks deben validarse contra el mismo contrato del proveedor.
- Un cambio de contrato debe actualizar provider test, consumer test y fixtures en el mismo PR.
- No duplicar fixtures dentro de cada repo.
- Los simuladores deben poder registrar requests para comprobar headers, audience, correlation ID e idempotency key.
- Nunca copiar respuestas reales que contengan PII o prompts sensibles sin sanitizarlas.
