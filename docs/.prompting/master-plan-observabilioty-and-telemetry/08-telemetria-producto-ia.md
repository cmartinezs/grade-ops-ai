# Telemetría de producto e IA

## Distinción fundamental

La telemetría de producto responde qué valor obtiene el usuario; la observabilidad técnica explica por qué el sistema funciona o falla. Ambas pueden compartir IDs, pero no el mismo almacenamiento ni retención.

## Eventos canónicos

Ejemplos:

```text
assessment.created
assessment.draft_generated
assessment.approved
submission.received
grading.suggestion_generated
grading.suggestion_edited
grading.suggestion_approved
grading.suggestion_rejected
feedback.published
ai.operation.started
ai.operation.completed
ai.operation.failed
```

## Envelope

```json
{
  "eventId": "uuid",
  "eventName": "grading.suggestion_approved",
  "occurredAt": "2026-07-21T14:00:00Z",
  "schemaVersion": 1,
  "actor": { "type": "TEACHER", "id": "pseudonym" },
  "context": {
    "organizationId": "uuid",
    "operationId": "uuid",
    "agentRunId": "uuid"
  },
  "properties": {}
}
```

## Calidad real de IA

Además de disponibilidad, medir:

- Output técnicamente válido.
- Resultado aprobado sin cambios.
- Resultado editado y magnitud/tipo de edición.
- Resultado rechazado y categoría de razón.
- Tiempo hasta revisión docente.
- Ahorro de tiempo estimado con metodología explícita.
- Costo por resultado aprobado, no solo por llamada.

La edición/rechazo docente es una señal de producto crítica; no debe inferirse desde logs.

## Gobernanza de esquema

- Catálogo versionado de eventos.
- Propietario, propósito y consumidores por evento.
- Idempotencia mediante `eventId`.
- Momento transaccional exacto documentado.
- Backfill y cambios incompatibles mediante nueva versión.
- Tests de contrato en CI.
