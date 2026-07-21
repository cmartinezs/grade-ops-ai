# Estrategia API-Agent Orchestration — GradeOps AI

> Incorporacion del paquete `docs/.prompting/master-plan-api-agent-orchestration/` al Master Plan.
> Este documento no crea una release tecnica independiente: define reglas obligatorias para que cada release funcional use `api/` como intermediario robusto entre `web/` y `agents/`.

## Principio rector

`api/` no es un proxy HTTP hacia `agents/`. `api/` es la fuente de verdad del workflow, la autorizacion, la persistencia, los estados, la evidencia y los efectos de dominio visibles para `web/`.

`agents/` ejecuta capacidades GenAI tipadas y devuelve resultados estructurados con evidencia de ejecucion. No aprueba, no publica, no factura y no persiste entidades de dominio.

`web/` consume rutas funcionales de `api/`. No conoce prompts, providers, tools, URLs internas de `agents/` ni nombres libres de agentes.

## Aplicacion por release

| Release | Incremento API-Agent requerido | No incluir aun |
|---|---|---|
| R01 | `AiOperation`, `AgentRun` y `AgentAttempt` minimos; idempotencia; provider/model efectivo; error enriquecido real; consulta de operacion para generation/regeneration; compatibilidad sincrona del Assessment Agent | Cloud Tasks, tool loop, SSE, sandbox, multiagente |
| R02 | `AgentDefinition`/registry liviano, gateway comun de provider/model y contratos por agente para Rubric, Grading y Feedback | Batch durable si solo se procesa una submission |
| R03 | Handoffs tipados, herramientas read-only/agregadas y provenance para gaps, recovery y reports | Multiagente autonomo |
| R04 | Tool loop controlado, policy engine, validators de preguntas/banco/composicion y limites de steps/tokens/costo | Scoring IA para Closed |
| R05 | Outbox, Cloud Tasks, executor interno, progreso parcial, retry selectivo, leasing/heartbeat y polling cuando haya volumen/latencia real | WebSocket o reintento de lote completo |
| R06 | Health, costos, comparacion provider/model, warnings, readiness y Ops Agent read-only sobre hechos persistidos | Calculos de negocio o evidence fabricados por LLM |

## Modelo minimo de ejecucion

Toda historia AI debe decidir explicitamente si usa el corte sincrono compatible o el corte asincrono durable.

Incluso en modo sincrono, los comandos GenAI mutantes deben crear una operacion antes de ejecutar:

- `AiOperation`: intencion funcional consultable por UI.
- `AgentRun`: ejecucion logica de un agente dentro de la operacion.
- `AgentAttempt`: intento tecnico concreto, incluyendo provider/model/costo/error.

Batch grading, feedback masivo, question batches, cohort reports, tool loops, sandbox y latencia impredecible requieren asincronia durable. `@Async` no es aceptable como mecanismo durable en Cloud Run.

## Gate Richardson REST

Toda tarea que defina o modifique endpoints publicos de `api/`, endpoints internos de `agents/` o rutas funcionales de `web/` debe incluir un checkpoint de madurez REST cercano a Richardson nivel 3.

El checkpoint debe verificar:

- Recurso de negocio claro en la URI; no exponer `POST /agents/{agentName}/execute` a `web/`.
- Metodo HTTP correcto: `GET` consulta, `POST` crea comando/recurso/operacion, `PATCH` actualiza parcialmente, `DELETE` solo si existe eliminacion real.
- Status codes consistentes: `201` para recurso creado, `202` para operacion aceptada, `200` para resultado inmediato, `204` para exito sin body, `400/401/403/404/409/422` segun causa.
- `Location` cuando se crea una operacion o recurso consultable.
- Representacion con `data` y `meta` cuando aplique, incluyendo `requestId`/timestamp.
- Links o affordances para transiciones relevantes: `self`, `operation`, `runs`, `result`, `cancel`, `retry`.
- Errores normalizados con `errorCode`, `safeMessage`, `retryable` y correlation/request id.
- `Idempotency-Key` en comandos GenAI mutantes, con `409 IDEMPOTENCY_CONFLICT` para mismo key y request distinto.
- Ownership server-side antes de despachar y antes de aplicar resultados; acceso cruzado debe evitar filtrar existencia.
- OpenAPI/JSON Schema o contrato documentado y contract tests para API-Web o API-Agents.

Si una tarea no puede cumplir nivel 3 completo por una restriccion deliberada, debe dejar el residual explicitado en la tarea y en la story.

## Gate por area

### `api/`

- Recibe intenciones funcionales desde `web/`.
- Valida auth, ownership, workflow state, budget/cost policy e idempotencia.
- Persiste `AiOperation`, runs, attempts, artefactos revisables, usage/cost y approval events.
- Invoca `agents/` solo desde el modulo `agentclient` o gateway equivalente aprobado.
- Encapsula transiciones de estado en dominio; controllers/adapters no asignan strings libres.

### `agents/`

- Recibe command/envelope tipado construido por `api/`.
- Valida input, policy y salida estructurada.
- Retorna provider/model, prompt/schema version, tokens/costo, hashes, warnings, error normalizado y correlation.
- Mantiene endpoints internos, no publicos.
- No persiste entidades de dominio ni llama a bases del producto directamente.

### `web/`

- Consume rutas funcionales de `api/`.
- Usa rutas de acceso reales y navegables para cada flujo entregado.
- Soporta estados de operacion: loading, queued/running cuando aplique, success, failure seguro, retry/cancel si existe en API.
- No crea tipos independientes que contradigan DTOs de `api/`.
- No muestra como disponible una accion cuyo endpoint o estado no exista.

## Criterio de salida

Una entrega que toque AI/API no queda `DONE` solo porque el endpoint responde. Debe demostrar:

- valor funcional visible;
- contrato estructurado y versionado;
- idempotencia;
- estados consultables cuando aplique;
- errores normalizados;
- provider/model/costo/trazabilidad;
- approval humana antes de efectos academicos;
- tests unitarios, integracion, contrato y smoke acordes al riesgo;
- documentacion y planning sincronizados.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-20 | Creacion inicial | Convertir `master-plan-api-agent-orchestration` en reglas operativas del Master Plan sin crear una release tecnica transversal | Master Plan, R01-R06, templates de tareas | D-API-01..D-API-10 |
