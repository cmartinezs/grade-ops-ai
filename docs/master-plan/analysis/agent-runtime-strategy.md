# Estrategia transversal de Agent Runtime — GradeOps AI

> Incorporacion del paquete `docs/.prompting/master-plan-runtime/` al Master Plan.
> Este documento no crea una release tecnica independiente: define como cada release funcional debe consumir y ampliar el runtime.
> Para el borde `web/` -> `api/` -> `agents/`, complementar con [`api-agent-orchestration-strategy.md`](api-agent-orchestration-strategy.md).

## Principio rector

La Plataforma de Agentes de GradeOps AI se gestiona como una capacidad arquitectonica transversal y evolutiva. No se implementa como una fase tecnica aislada previa a las funcionalidades del producto.

Cada release funcional que incorpore asistencia de IA debe identificar:

- agente especializado involucrado;
- reglas API-Agent Orchestration aplicables;
- capacidades incrementales del runtime generico;
- herramientas de dominio necesarias;
- validadores deterministicos;
- limites de autonomia;
- puntos de aprobacion humana;
- presupuesto, metricas y evidencia.
- endpoints, contratos y rutas sometidos al gate Richardson REST cuando correspondan.

## Estado actual asumido

El codigo actual de `agents/` ya contiene una vertical slice real del Assessment Agent:

- endpoint interno especifico;
- comando de generacion/regeneracion;
- seleccion Gemini/Groq;
- prompt versionado;
- salida estructurada;
- validacion de resultado;
- payload de ejecucion con proveedor, modelo, tokens, costo y estado.

Esto todavia no equivale a un runtime agentic headless comparable operacionalmente con Codex o Claude Code. No existen aun, como capacidades genericas:

- `AgentDefinition` y `AgentRegistry`;
- `AgentAction` tipada (`UseTool`, `Finish`, `Block`);
- bucle modelo-herramienta-observacion;
- `ToolRegistry`, `ToolExecutor` y `PolicyEngine`;
- `AgentRun` y `AgentStep` persistidos;
- reanudacion/cancelacion;
- ejecucion asincrona;
- sandbox para codigo de estudiantes;
- handoffs tipados entre agentes;
- evaluacion continua de calidad por agente.

## Separacion de responsabilidades

| Componente | Autoridad |
|---|---|
| `api/` | Interfaz publica de `web/`, dominio, auth, ownership, estados, persistencia, notas finales, publicacion, aprobaciones, creditos y billing |
| `agents/` runtime | Ejecucion de agentes, proveedor/modelo, tool loop, politicas, presupuesto, validacion tecnica, metricas y trazabilidad |
| Agente especializado | Instrucciones, contratos de entrada/salida, herramientas permitidas, validadores, autonomia y causas de bloqueo |
| Modelo GenAI | Propone acciones, interpreta evidencia, redacta y explica; no autoriza ni persiste efectos de dominio |

La API puede invocar un agente de forma sincrona o asincrona, pero no delega autoridad academica definitiva al modelo.

## Evolucion por releases

| Release | Incremento principal del runtime | Consumidor funcional |
|---|---|---|
| R01 | Consolidacion de ejecucion GenAI existente: provider/model policy, logs ricos, errores normalizados, costo, idempotencia y compatibilidad del Assessment Agent actual | Assessment creation y regeneration |
| R02 | Primer segundo consumidor fuerte: `AgentDefinition`, registry liviano, `AgentModelGateway` comun, contratos y validadores por agente | Rubric, Grading y Feedback |
| R03 | Handoffs tipados y herramientas read-only/agregadas para reportes, gaps y recovery, con separacion de hechos e hipotesis | Learning Gap, Recovery y Teacher Report |
| R04 | Tool loop controlado para generacion/revision/composicion Closed con herramientas de banco, cobertura y validacion deterministica | Question Generation, Distractor Quality, Ambiguity y Assembly |
| R05 | Persistencia asincrona/reanudable para analytics y eventos de estudiante cuando el volumen o latencia lo justifique; scoring Closed sigue deterministico en API | Item Analytics y student access |
| R06 | Observabilidad operacional: agent health, costos, provider/model comparison, warnings, budget alerts y Ops Agent read-only | Evidence dashboard y operational readiness |

R07/R08 pueden ampliar optimizacion, comparacion de modelos, refinamientos P1 y memoria/recuperacion avanzada solo si existe consumidor funcional concreto.

## Reglas de extraccion

- Generalizar una capacidad cuando exista un segundo consumidor real o una necesidad inmediata de la siguiente release.
- Mantener el endpoint especifico del Assessment Agent mientras se migra hacia contratos comunes.
- No introducir multiagente general, memoria vectorial, RAG, colas, sandbox o nuevos proveedores sin caso funcional que lo requiera.
- No mover calculos deterministas, autorizacion o persistencia de dominio desde `api/` hacia `agents/`.
- No almacenar razonamiento privado del modelo; registrar acciones, herramientas, observaciones normalizadas, validaciones y evidencia.

## Capacidades runtime por etapa

| Etapa | Capacidad | Primer release que la necesita |
|---|---|---|
| 0 | Baseline documental y contrato actual de Assessment Agent | R01 |
| 1 | Provider/model catalog, prompt/schema versioning, errores y costo | R01 |
| 2 | `AgentDefinition`, registry y gateway comun | R02 |
| 3 | `AgentAction`, tool registry/executor, policy engine y budget loop | R04, con preparacion parcial en R02/R03 |
| 4 | `AgentRun`/`AgentStep`, estados, idempotencia, cancelacion/reanudacion | R05 |
| 5 | Sandbox aislado para codigo no confiable | R02 si grading ejecuta codigo; si R02 no ejecuta codigo, diferir a una release especifica |
| 6 | Handoff tipado entre agentes | R03 |
| 7 | Ops Agent y evaluacion continua de calidad/costo | R06 |

## DoD minima para funcionalidad IA

Una release con IA no esta completa si solo "el endpoint responde". Debe demostrar:

- resultado funcional visible y revisable;
- contrato estructurado validado;
- agente y prompt versionados;
- herramientas restringidas y probadas;
- limites efectivos de pasos, tokens, costo, tiempo y reintentos;
- tokens, costo, latencia, proveedor/modelo y estado registrados;
- errores, `BLOCKED` o `NEEDS_INPUT` explicitos cuando corresponda;
- aprobacion humana aplicada antes de efectos academicos;
- pruebas unitarias, integracion y smoke de flujo;
- casos de evaluacion de calidad representativos;
- documentacion sincronizada con el codigo real.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-20 | Creacion inicial | Incorporar `master-plan-runtime` como estrategia transversal del Master Plan | `analysis/agent-runtime-strategy.md`, R01-R06 | D-04, D-06 |
| 2026-07-20 | Alineacion API-Agent Orchestration | Asegurar que runtime y API robusta avancen dentro de releases funcionales y con gate REST por tarea | `analysis/agent-runtime-strategy.md`, `analysis/api-agent-orchestration-strategy.md` | D-API-01..D-API-10 |
