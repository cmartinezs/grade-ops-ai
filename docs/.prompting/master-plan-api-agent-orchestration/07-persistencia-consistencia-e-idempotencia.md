# Persistencia, consistencia e idempotencia

## Consistencia

La llamada a `agents/` debe permanecer fuera de una transacción de base de datos. La transacción debe cubrir únicamente:

- transición de estado;
- persistencia del resultado validado;
- actualización del run/attempt;
- registro de eventos Outbox;
- conciliación de uso que deba ser atómica.

## Outbox

Al aceptar una operación asíncrona, persistir en una misma transacción:

```text
AiOperation(QUEUED)
OutboxEvent(AI_OPERATION_REQUESTED)
UsageReservation(RESERVED), cuando aplique
```

Un dispatcher publica el Outbox en Cloud Tasks y marca el evento como despachado. Si la publicación falla, el evento permanece pendiente y puede reintentarse.

## Idempotencia HTTP

Los comandos GenAI mutantes deben exigir o admitir:

```http
Idempotency-Key: UUID
```

Clave única recomendada:

```text
tenant/teacher + operation_type + aggregate_type + aggregate_id + idempotency_key
```

Comportamiento:

- mismo key + mismo request hash: devolver la operación existente;
- mismo key + distinto request hash: `409 IDEMPOTENCY_CONFLICT`;
- retry de transporte interno: mismo `runId`;
- retry de provider: nuevo `AgentAttempt` bajo el mismo `AgentRun`.

## Evitar efectos duplicados

- Restricción única sobre artefacto por `agent_run_id` cuando corresponda.
- Persistencia mediante compare-and-set de versión/estado.
- No procesar un run terminal nuevamente.
- Cloud Tasks puede entregar más de una vez; el consumer debe ser idempotente.
- Un provider timeout es ambiguo: pudo procesar aunque no haya respuesta. El sistema debe usar run IDs estables y, cuando el provider lo permita, request IDs idempotentes.

## Leasing y recuperación

Campos recomendados:

```text
lease_owner
lease_expires_at
heartbeat_at
next_attempt_at
```

Un watchdog puede recuperar runs `RUNNING` cuyo lease expiró. La recuperación debe verificar estado antes de crear un nuevo attempt.

## Resultados parciales

En batch:

- cada run persiste su resultado de forma independiente;
- el progreso se deriva de runs, no solo de un contador mutable;
- el contador puede mantenerse como proyección optimizada;
- una falla individual no revierte resultados exitosos anteriores;
- retry selectivo procesa únicamente runs elegibles.

## Créditos y costo

Ciclo recomendado:

1. estimar;
2. validar límite;
3. reservar;
4. ejecutar;
5. registrar consumo real;
6. conciliar reserva;
7. liberar saldo no utilizado.

La unidad facturable principal del ciclo Open continúa siendo la `StudentSubmission` efectivamente analizada, no el número bruto de requests HTTP.

