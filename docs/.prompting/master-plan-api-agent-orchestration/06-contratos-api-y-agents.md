# Contratos entre UI, API y Agents

## API pública orientada a negocio

Endpoints recomendados:

```text
POST /api/v1/assessments/{id}/draft-generations
POST /api/v1/assessments/{id}/rubric-generations
POST /api/v1/assessments/{id}/grading-runs
POST /api/v1/assessments/{id}/feedback-generations
POST /api/v1/assessments/{id}/learning-gap-analyses
POST /api/v1/assessments/{id}/recovery-generations
POST /api/v1/assessments/{id}/report-generations
POST /api/v1/question-banks/{id}/generation-runs
```

No exponer a la UI `POST /agents/{agentName}/execute`.

## Respuesta asíncrona

```http
202 Accepted
Location: /api/v1/ai-operations/{operationId}
```

```json
{
  "data": {
    "operationId": "uuid",
    "status": "QUEUED",
    "statusUrl": "/api/v1/ai-operations/uuid"
  },
  "meta": {
    "requestId": "uuid",
    "timestamp": "2026-07-20T12:00:00Z"
  }
}
```

## Estado transversal

```text
GET  /api/v1/ai-operations/{operationId}
GET  /api/v1/ai-operations/{operationId}/runs
POST /api/v1/ai-operations/{operationId}/cancel
POST /api/v1/ai-operations/{operationId}/retry
GET  /api/v1/ai-operations/{operationId}/events
```

`retry` debe crear un nuevo attempt o reactivar runs fallidos según política; nunca debe duplicar artefactos ya exitosos.

## Envelope interno común

```json
{
  "runId": "uuid",
  "operationId": "uuid",
  "agent": {
    "name": "grading",
    "version": "1.0"
  },
  "input": {},
  "context": {
    "organizationId": "uuid",
    "assessmentId": "uuid",
    "submissionId": "uuid"
  },
  "policy": {
    "deadline": "2026-07-20T15:30:00Z",
    "maxSteps": 8,
    "maxInputTokens": 30000,
    "maxOutputTokens": 5000,
    "maxCostUsd": 0.08
  },
  "contract": {
    "inputSchemaVersion": "1",
    "outputSchemaVersion": "1"
  }
}
```

`input` permanece especializado por agente. El envelope solo estandariza identidad, trazabilidad, policy y versionamiento.

## Respuesta interna común

```json
{
  "runId": "uuid",
  "status": "SUCCEEDED",
  "result": {},
  "execution": {
    "agentName": "grading",
    "agentVersion": "1.0",
    "provider": "gemini",
    "model": "model-id",
    "promptVersion": "grading-v1",
    "inputHash": "sha256",
    "outputHash": "sha256",
    "tokens": {
      "input": 1000,
      "output": 300
    },
    "cost": {
      "estimated": 0.01,
      "currency": "USD"
    },
    "startedAt": "...",
    "finishedAt": "..."
  },
  "warnings": []
}
```

## Errores normalizados

Separar:

```text
INVALID_COMMAND
UNAUTHORIZED_INTERNAL_CALL
POLICY_DENIED
BUDGET_EXCEEDED
PROVIDER_RATE_LIMITED
PROVIDER_TIMEOUT
PROVIDER_UNAVAILABLE
MALFORMED_OUTPUT
OUTPUT_VALIDATION_FAILED
TOOL_FAILED
SANDBOX_FAILED
DEADLINE_EXCEEDED
CANCELLED
INTERNAL_ERROR
```

Cada error debe incluir `retryable`, `safeMessage`, correlation y evidencia parcial disponible.

## Compatibilidad y versionamiento

- Mantener temporalmente `/internal/agents/assessment`.
- Añadir envelope sin romper el contrato existente o crear versión interna explícita.
- Versionar input schema, output schema, agent definition y prompt por separado.
- No compartir un JAR de DTOs que acople los despliegues.
- Publicar OpenAPI/JSON Schema interno y ejecutar contract tests en CI.

