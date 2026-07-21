# Autorización por capacidad y policy

## Capability model

Ejemplos:

- `agent:assessment:execute`
- `agent:grading:execute`
- `provider:gemini:use`
- `provider:groq:use`
- `tool:repository:read`
- `tool:sandbox:execute`

La identidad de `api/` puede recibir capacidades por deployment; un worker futuro tendrá las mínimas necesarias. No usar roles Teacher/Operator.

## Envelope obligatorio

Cada comando debe incluir `operationId`, `runId`, agent/version, schema versions, deadline y policy: max steps, input/output tokens, costo, duración y herramientas permitidas. El runtime aplica el mínimo entre límites solicitados y límites locales.

## Provider/model

El caller no debe enviar cualquier string. Mantener un catálogo server-side de combinaciones aprobadas por capacidad, región, costo y clasificación de datos. El API puede solicitar una opción, pero `agents/` valida allowlist y policy; el default no debe depender solo de precio gratuito.

## Cuotas

API reserva consumo antes de despachar. Agents aplica circuit breakers, rate/concurrency limits globales y por provider, y devuelve consumo real. Errores de presupuesto no son retryables; throttling del proveedor puede serlo con backoff y deadline.
