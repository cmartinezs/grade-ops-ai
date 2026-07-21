# Contratos de correlación y contexto

## Propagación recomendada

`web/` debe generar o aceptar contexto W3C y enviar:

```text
traceparent
tracestate          # solo cuando corresponda
X-Request-Id
X-Correlation-Id    # compatibilidad y búsqueda humana
```

`api/` debe:

1. Validar IDs externos y reemplazarlos si son inválidos.
2. Crear el span servidor.
3. Añadir `operationId`, `agentRunId` y `attemptId` cuando existan.
4. Propagar el contexto actual a `agents/`; no crear una correlación aislada por defecto.
5. Retornar `X-Request-Id` y `X-Correlation-Id` al cliente.

`agents/` debe continuar la traza y crear spans hijos para validación, prompt rendering, llamada al modelo y validación de output.

## Envelope de observabilidad

Los comandos API → Agents deben transportar IDs de negocio tipados:

```json
{
  "operationId": "uuid",
  "agentRunId": "uuid",
  "attemptId": "uuid",
  "agent": { "name": "grading", "version": "1.0" },
  "context": { "organizationId": "pseudonym-or-internal-id" },
  "input": {}
}
```

El `traceparent` permanece en headers; no debe duplicarse en el payload de dominio.

## Campos comunes de logs

```text
timestamp, severity, message, service.name, service.version,
deployment.environment, trace_id, span_id, request_id, correlation_id,
operation_id, agent_run_id, attempt_id, event_name, error.type, error.code
```

## Reglas

- Longitud máxima y regex para IDs recibidos.
- No confiar en IDs del cliente como identidad o autorización.
- No usar email, Firebase UID, assessment ID o submission ID crudos como dimensiones públicas.
- Pseudonimizar actor/organización cuando el análisis lo necesite.
- Propagar contexto en tareas asíncronas mediante metadata de la tarea/outbox.
