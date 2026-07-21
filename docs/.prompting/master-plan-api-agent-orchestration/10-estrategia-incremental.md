# Estrategia incremental para el agente planificador

## Principio

No crear una release exclusivamente técnica llamada “Agent Runtime completo”. Incorporar capacidades transversales dentro de releases funcionales que produzcan valor observable.

## Incremento 1 — Consolidar Assessment Agent

Objetivo funcional: conservar creación/regeneración actual con trazabilidad confiable.

Incluir:

- `AiOperation` mínima;
- `AgentRun` y `AgentAttempt` mínimos;
- IDs generados por API antes de llamar a Agents;
- idempotency key;
- persistencia del error enriquecido real;
- enums de estado/error;
- endpoint de consulta de operación;
- contrato/versionamiento y contract tests;
- métricas de latencia, tokens y costo;
- mantener ejecución síncrona como compatibilidad temporal.

No incluir todavía:

- Cloud Tasks;
- tool loop;
- multiagente;
- sandbox;
- SSE.

## Incremento 2 — Segundo consumidor: Rubric/Grading/Feedback

Objetivo funcional: completar el thin slice Open con revisión docente.

Capacidades runtime:

- `AgentDefinition` y registry liviano;
- gateway común de modelo/provider;
- contracts/validators por agente;
- artifacts revisables;
- policies de modelo y presupuesto;
- estados técnico/académico separados.

Grading por una sola submission puede servir como transición antes del batch.

## Incremento 3 — Batch grading asíncrono

Objetivo funcional: procesar cohortes reales.

Capacidades:

- Outbox;
- Cloud Tasks;
- executor interno;
- un run por `StudentSubmission`;
- progreso parcial;
- retry selectivo;
- cancelación cooperativa;
- leasing/heartbeat;
- reserva y conciliación de uso;
- `PARTIALLY_SUCCEEDED`;
- polling UI.

Este es el primer punto donde la asincronía durable deja de ser opcional.

## Incremento 4 — Gaps, Recovery y Teacher Report

Objetivo funcional: análisis agregado del ciclo Open.

Capacidades:

- herramientas read-only tipadas;
- handoffs explícitos o composición dirigida por API;
- separación entre hechos, inferencias y recomendaciones;
- provenance de inputs;
- límites de contexto y reducción/agregación.

No permitir que los agentes consulten la base directamente sin ports/tools controladas.

## Incremento 5 — Closed assessment

Objetivo funcional: generación, revisión y ensamblaje de preguntas.

Capacidades:

- tool loop controlado;
- quality/ambiguity validators;
- composición y coverage tools;
- límites de steps/tokens/costo;
- question batch asíncrono;
- snapshot y scoring determinístico permanecen en API.

## Incremento 6 — Analytics, Ops y optimización

Objetivo funcional: evidencia real de impacto y operación.

Capacidades:

- dashboards de salud/costo/calidad;
- comparación provider/model;
- alertas de presupuesto;
- Ops Agent read-only;
- evaluation datasets continuos;
- medición de aceptación, edición y rechazo docente.

## Definition of Done para toda historia AI

- Resultado funcional visible y revisable.
- Contrato estructurado y validado.
- Agent/prompt/schema versionados.
- Tools restringidas, si existen.
- Límites de tiempo, steps, tokens, costo y retries.
- Operación y runs trazables.
- Idempotencia demostrada.
- Errores normalizados.
- Aprobación humana antes de efectos académicos.
- Tests unitarios, integración, contrato y smoke adecuados.
- Métricas de tokens, costo, latencia y calidad.
- Documentación sincronizada.

## Formato requerido para el plan

Para cada entrega, el agente planificador debe declarar:

1. valor funcional;
2. user stories incluidas y faltantes;
3. agentes consumidores;
4. capacidad runtime incremental;
5. cambios API, dominio, DB, agents, web e infra;
6. migración/compatibilidad;
7. riesgos y mitigaciones;
8. pruebas;
9. observabilidad y evidencia;
10. prompt de implementación;
11. criterios de salida medibles.

