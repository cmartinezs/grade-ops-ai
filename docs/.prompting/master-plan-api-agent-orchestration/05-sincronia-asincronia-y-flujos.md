# Sincronía, asincronía y flujos

## Criterio de selección

La modalidad no depende solo del nombre del agente. Debe considerar:

- duración p95 esperada;
- cantidad de ítems;
- uso de herramientas o sandbox;
- necesidad de progreso parcial;
- costo y cuotas;
- reintentos posibles;
- deadline de infraestructura;
- tolerancia del usuario a esperar con la conexión abierta.

## Síncrono

Usar cuando:

- existe un solo input acotado;
- duración esperada menor a 10–15 segundos;
- no existe tool loop largo;
- resultado pequeño;
- no se requiere progreso parcial.

Incluso el modo síncrono debe crear una `AiOperation` antes de ejecutar. Si finaliza dentro del presupuesto temporal, responde `200`; si supera el umbral, puede continuar asíncronamente y responder `202`, siempre que el contrato público lo permita desde el inicio.

Ejemplos candidatos:

- revisión de una pregunta;
- ajuste de tono de un feedback;
- validación pequeña de rúbrica;
- Assessment Agent inicial durante la transición, sujeto a medición real.

## Asíncrono durable

Obligatorio para:

- grading de varias `StudentSubmission`;
- feedback masivo;
- generación de lotes de preguntas;
- análisis de cohortes;
- reportes consolidados;
- tool loops;
- sandbox de código;
- multiagente/handoffs;
- latencia impredecible o superior a 15 segundos.

## Flujo recomendado

1. UI envía comando con `Idempotency-Key`.
2. API autentica, autoriza y valida precondiciones.
3. API estima costo y reserva capacidad/créditos.
4. En una transacción crea `AiOperation=QUEUED` y `OutboxEvent`.
5. Dispatcher publica en Cloud Tasks.
6. Cloud Tasks llama un endpoint interno del executor de API.
7. Executor adquiere lease, marca `RUNNING` y crea runs.
8. Executor llama sincrónicamente a `agents/` por run.
9. API valida respuesta, persiste artefacto y actualiza progreso.
10. API concilia consumo/costo y finaliza la operación.
11. UI consulta estado mediante polling; SSE es una mejora posterior.

## Por qué no `@Async`

Devolver HTTP y continuar en un thread de la instancia no ofrece durabilidad en Cloud Run. La instancia puede ser detenida, escalada a cero o reiniciada. Un transporte durable debe poseer el reintento de entrega.

## Batch grading

Una solicitud para 35 entregas debe producir:

- una `AiOperation` de grading;
- hasta 35 `AgentRun`, uno por `StudentSubmission`;
- resultados y fallos independientes;
- progreso parcial;
- estado `PARTIALLY_SUCCEEDED` si existen fallos terminales;
- retry selectivo, no repetición completa del lote.

No usar una única transacción para todo el batch.

## Polling y SSE

Primera versión:

- polling cada 2–5 segundos;
- `ETag` o `updatedAt` para evitar payload innecesario;
- detener polling en estados terminales.

Evolución:

- SSE para progreso y terminales;
- polling permanece como fallback y mecanismo de recuperación.

No se justifica WebSocket en el MVP.

## Cancelación

La cancelación es cooperativa y best-effort:

- `QUEUED` puede cancelarse inmediatamente;
- `RUNNING` pasa a `CANCELLING`;
- no se inicia el siguiente step o run;
- una llamada ya enviada al provider puede no ser interrumpible;
- resultados recibidos después de cancelar se auditan, pero no se aplican automáticamente.

