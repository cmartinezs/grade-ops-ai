# Estrategia API-Agent Orchestration — GradeOps AI

> Incorporacion del paquete `docs/.prompting/master-plan-api-agent-orchestration/` al Master Plan.
> Este documento no crea una release tecnica independiente: define reglas obligatorias para que cada release funcional use `api/` como intermediario robusto entre `web/` y `agents/`.

## Principio rector

`api/` no es un proxy HTTP hacia `agents/`. `api/` es la fuente de verdad del workflow, la autorizacion, la persistencia, los estados, la evidencia y los efectos de dominio visibles para `web/`.

`agents/` ejecuta capacidades GenAI tipadas y devuelve resultados estructurados con evidencia de ejecucion. No aprueba, no publica, no factura y no persiste entidades de dominio.

`web/` consume rutas funcionales de `api/`. No conoce prompts, providers, tools, URLs internas de `agents/` ni nombres libres de agentes. Ninguna funcionalidad queda entregada si solo se puede alcanzar escribiendo una URL: toda ruta funcional debe ser accesible desde una accion visible de la UI, como boton, enlace, menu o affordance equivalente.

`web/` tampoco puede degradar la semantica de dominio a campos libres por conveniencia. Las pantallas deben partir de `web/design-system/` y representar cada dato segun su fuente de verdad: texto libre solo cuando el dominio lo permite, enums/statuses como controles o badges, datos maestros como selectores/catalogos, numeros con unidad/rango y datos read-only como provenance no editable.

Todo dato requerido por una pantalla, sea lectura o escritura, debe estar alineado con `api/`. Si el endpoint, read model, catalogo o mutation no existe, la release debe implementar lo necesario en `api/` antes de cerrar la UI o registrar un residual explicito que impida declararla completa.

La orquestacion tambien transporta i18n: field names, DTOs, enum codes y operation states permanecen en ingles, pero `web` debe indicar locale efectivo cuando espera texto visible, `api` debe resolver/fallback/persistir locale cuando impacta contenido durable, y `agents` debe recibir `outputLocale` para salidas GenAI teacher-facing o student-facing. Logs, traces, metrics y eventos tecnicos no se localizan.

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

Cuando una tarea elige asincronia, debe declarar como `web/` sabe que el trabajo termino:

- polling de una operacion consultable, por ejemplo `GET /api/v1/operations/{id}`;
- SSE si se requiere stream unidireccional de progreso hacia navegador;
- WebSocket si existe necesidad real de canal bidireccional;
- webhook solo para callbacks server-to-server, no como mecanismo primario de navegador;
- push/notification si existe infraestructura y UX para notificar fuera de la pantalla.

El contrato asincrono debe incluir estados, `Location`/link de operacion, timeout, retry/cancel si aplica, errores normalizados e idempotencia. Un spinner indefinido, un `setTimeout` local o una reconsulta ad hoc sin endpoint de estado no cuentan como completion model.

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
- Expone los endpoints/read models/catalogos/mutations que `web/` necesita para lectura y escritura; si no existen, se planifican en `api/` antes de declarar lista la UI.
- Valida auth, ownership, workflow state, budget/cost policy e idempotencia.
- Persiste `AiOperation`, runs, attempts, artefactos revisables, usage/cost y approval events.
- Define para cada comando si la respuesta es sincrona o asincrona, y en el caso asincrono expone el estado consultable o canal de completion acordado.
- Resuelve locale efectivo para respuestas user-facing, safe errors, catalog labels y comandos que generan contenido visible; persiste `contentLocale`/`outputLocale` en artefactos u operaciones cuando aplique.
- Invoca `agents/` solo desde el modulo `agentclient` o gateway equivalente aprobado.
- Encapsula transiciones de estado en dominio; controllers/adapters no asignan strings libres.

### `agents/`

- Recibe command/envelope tipado construido por `api/`.
- Valida input, policy y salida estructurada.
- Retorna provider/model, prompt/schema version, tokens/costo, hashes, warnings, error normalizado y correlation.
- Recibe `outputLocale`/`contentLocale` desde `api` cuando el resultado sera visible a usuario final; comandos/resultados tecnicos siguen en ingles.
- Mantiene endpoints internos, no publicos.
- No persiste entidades de dominio ni llama a bases del producto directamente.

### `web/`

- Consume rutas funcionales de `api/`.
- Usa rutas de acceso reales y navegables para cada flujo entregado; no basta con que la URL exista o cargue si no hay una accion de UI que lleve al usuario hasta ella.
- Cada ruta funcional nueva declara su entry point de UI: pantalla origen, accion visible, destino, estado disabled/hidden y comportamiento ante permiso insuficiente.
- Cada pantalla/formulario declara matriz de campos antes de wireframe/mockup: naturaleza del dato, lectura/escritura, fuente de verdad, restricciones, cardinalidad y componente DS.
- Cada dato visible o editable declara endpoint/API owner: datos de lectura, catalogos, defaults, capabilities, mutation submit, estado resultante y errores.
- Si falta un endpoint/read model/mutation/catalogo, no inventa fixture permanente ni DTO local: crea tarea `api/`/DB/infra o residual bloqueante para la UI.
- Cada accion define si espera respuesta sync o async. En async, `web/` implementa solo el completion mechanism acordado con `api/`: polling de operacion, SSE, WebSocket, webhook server-to-server indirecto o push/notification, segun el contrato.
- No reemplaza enums, estados, datos maestros, IDs relacionados, numeros, fechas o listas multiples por `input text` libre. Si falta endpoint/catalogo/API, registra gap de `api/`/DB o residual aceptado.
- Soporta estados de operacion: loading, queued/running cuando aplique, success, failure seguro, retry/cancel si existe en API.
- Indica locale efectivo en requests que devuelven texto visible o inician generacion visible; no hardcodea copy user-facing ni usa labels localizados como valores canonicos.
- No crea tipos independientes que contradigan DTOs de `api/`.
- No muestra como disponible una accion cuyo endpoint o estado no exista.

## Criterio de salida

Una entrega que toque AI/API no queda `DONE` solo porque el endpoint responde. Debe demostrar:

- valor funcional visible;
- contrato estructurado y versionado;
- idempotencia;
- estados consultables cuando aplique;
- contrato sync/async explicito y mecanismo de completion/progress probado para flujos asincronos;
- errores normalizados;
- provider/model/costo/trazabilidad;
- locale efectivo para superficies user-facing, safe errors/catalogos y outputs GenAI cuando aplique, sin localizar telemetria tecnica;
- approval humana antes de efectos academicos;
- tests unitarios, integracion, contrato y smoke acordes al riesgo;
- documentacion y planning sincronizados.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Incorporacion de i18n en orquestacion | Transportar locale entre web/api/agents sin traducir contratos tecnicos ni telemetria | API-Web/API-Agents contracts, operations, agents outputs | D-I18N-01..D-I18N-10 |
| 2026-07-21 | Incorporacion de API I/O y sync/async contract | Exigir que toda pantalla tenga datos alineados con `api/` y que los flujos async declaren completion model | API-Web contracts, operations, web routes | D-API-01..D-API-10, D-UI-01..D-UI-08 |
| 2026-07-21 | Incorporacion de UI Design/Data Semantics | Evitar que `web/` convierta contratos o datos maestros/restringidos en inputs libres sin fuente de verdad | Web routes, forms, API-Web contracts | D-UI-01..D-UI-08 |
| 2026-07-21 | Incorporacion de reachability por accion de UI | Evitar rutas funcionales accesibles solo por URL directa; todo flujo debe nacer desde una accion visible y testeada | Web routes, tasks UI, contract/acceptance/e2e | D-API-01..D-API-10, D-TEST-01..D-TEST-09 |
| 2026-07-20 | Creacion inicial | Convertir `master-plan-api-agent-orchestration` en reglas operativas del Master Plan sin crear una release tecnica transversal | Master Plan, R01-R06, templates de tareas | D-API-01..D-API-10 |
