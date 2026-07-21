# Estrategia transversal de observabilidad y telemetria - GradeOps AI

> Incorporacion del paquete `docs/.prompting/master-plan-observabilioty-and-telemetry/` al Master Plan.
> Este documento no crea una release tecnica independiente: define el contrato comun de observabilidad que cada release funcional debe implementar de forma incremental.

## Principio rector

Observabilidad no significa agregar logs. GradeOps AI debe separar:

| Senal | Finalidad | Fuente de verdad |
|---|---|---|
| Logs tecnicos | Diagnostico discreto de eventos tecnicos | Backend operativo del ambiente |
| Metricas | Tendencias, SLI, SLO y alertas | Backend de metricas compatible con OTel |
| Trazas | Latencia y causalidad entre componentes | Backend de trazas compatible con OTel |
| Eventos de producto | Uso, conversion, calidad y resultados | PostgreSQL del ambiente |
| Evidencia de IA | Modelo, tokens, costo, validacion y decision humana | PostgreSQL del ambiente |
| Artefactos pesados | Exportaciones, muestras redacted y bundles | Object storage del ambiente |

El dashboard futuro no debe leer archivos de log ni depender del formato propietario de GCP, Vercel o Render como modelo de producto. Debe consumir APIs sobre eventos canonicos, agregados y artefactos autorizados.

## Fuentes incorporadas

| Area | Fuente |
|---|---|
| Paquete transversal | `docs/.prompting/master-plan-observabilioty-and-telemetry/` |
| Modelo objetivo | `docs/.prompting/master-plan-observabilioty-and-telemetry/03-modelo-objetivo-observabilidad.md` |
| Correlacion y contexto | `docs/.prompting/master-plan-observabilioty-and-telemetry/04-contratos-correlacion-contexto.md` |
| Metricas y alertas | `docs/.prompting/master-plan-observabilioty-and-telemetry/06-metricas-sli-slo-alertas.md` |
| Telemetria de producto e IA | `docs/.prompting/master-plan-observabilioty-and-telemetry/08-telemetria-producto-ia.md` |
| Topologia multiambiente | `docs/.prompting/master-plan-observabilioty-and-telemetry/13-topologia-multiambiente.md` |

## Contrato minimo

Toda release que toque `web/`, `api/`, `agents/` o `infra/` debe preservar estos identificadores como conceptos distintos:

| ID | Uso |
|---|---|
| `trace_id` | Causalidad tecnica distribuida. |
| `span_id` | Unidad tecnica dentro de una traza. |
| `request_id` | Interaccion HTTP concreta. |
| `correlation_id` | Busqueda humana y compatibilidad con clientes sin trazas. |
| `operation_id` | Intencion durable del usuario. |
| `agent_run_id` | Ejecucion logica de un agente. |
| `attempt_id` | Intento tecnico individual. |

`web` debe propagar `traceparent`, `X-Request-Id` y `X-Correlation-Id` cuando aplique. `api` valida IDs externos, crea spans servidor, retorna IDs al cliente y propaga contexto hacia `agents`. `agents` continua la traza y crea spans hijos para validacion, prompt rendering, llamada al proveedor y validacion de salida.

## Topologia multiambiente

El contrato de instrumentacion es portable; los adaptadores cambian por ambiente.

| Capacidad | `demo` - GCP | `beta` - Vercel + Render + Neon | Contrato comun |
|---|---|---|---|
| Web | Hosting/runtime GCP definido | Vercel | W3C Trace Context, release, environment, Web Vitals y errores redacted |
| API/Agents | Cloud Run | Render | JSON estructurado, OTel, health/readiness y nombres semanticos comunes |
| Logs | Cloud Logging | Logs/drains Vercel/Render; destino central opcional | Esquema comun y correlation IDs |
| Metricas | Cloud Monitoring | Backend compatible con OTLP por decidir | Metricas y dimensiones identicas |
| Trazas | Cloud Trace | Backend compatible con OTLP por decidir | `traceparent` extremo a extremo |
| Evidencia durable | PostgreSQL del ambiente | Neon PostgreSQL | Schemas/versiones de eventos comunes |
| Artefactos pesados | Cloud Storage | Object storage por decidir | Manifest, checksum, clasificacion y lifecycle |

No deben aparecer SDK propietarios dentro de casos de uso, agentes o entidades de dominio. Las integraciones especificas viven en infraestructura, inicializacion o configuracion del runtime.

## Aplicacion por release

| Release | Incremento de observabilidad requerido | No incluir aun |
|---|---|---|
| R01 | ADR/backends OTel, taxonomia de IDs/eventos/errores, JSON stdout en `api`/`agents`, W3C propagation, Web instrumentation minima, `AiOperation`/`AgentRun`/`AgentAttempt` trazables en `demo` y `beta` | Dashboard custom, SLO contractual, captura masiva de prompts/respuestas |
| R02 | Journey Open rubric/submission/grading/feedback con spans, metricas GenAI, eventos canonicos y calidad IA por aprobacion/edicion/rechazo docente | Batch observability compleja si no hay volumen |
| R03 | Report/gap/recovery con herramientas read-only trazadas, metricas de agregacion, tiempo hasta validacion docente y estimacion de impacto versionada | Inferir calidad de producto desde logs tecnicos |
| R04 | Closed authoring con question generation/review/assembly trazados, validadores medidos, snapshot auditado y costo por batch/pregunta | Data warehouse o dashboard avanzado |
| R05 | Attempts, signed links, scoring deterministico e item analytics con propagacion asincrona, queue delay, heartbeat, retry y estancamiento | Cloud Tasks obligatorio en `beta` sin adaptador equivalente |
| R06 | Dashboard Operator sobre APIs y agregados, SLI/SLO baseline, alertas, runbooks, exports, readiness y comparacion `demo`/`beta` | Dashboard leyendo logs crudos o queries libres sobre telemetria |

## Gate de observabilidad por tarea

Toda tarea que cree o modifique endpoints, rutas, agentes, operaciones asincronas, providers, storage, exports, dashboards o journeys criticos debe incluir un checkpoint de observabilidad.

El checkpoint debe verificar:

- IDs propagados y no confundidos: trace, request, correlation, operation, run y attempt.
- Logs JSON estructurados con `service.name`, `service.version`, `deployment.environment`, severity, event name y error code.
- Metricas con dimensiones de baja cardinalidad; no usar user IDs, assessment IDs, operation IDs, run IDs o submission IDs como labels.
- Eventos canonicos de producto/IA persistidos en PostgreSQL/Neon cuando sean evidencia durable.
- Redaccion de PII, prompts, respuestas completas, tokens, secrets y signed links.
- Instrumentacion multiambiente: `demo` y `beta` producen la misma semantica aunque usen adaptadores distintos.
- Fallo del exportador OTel/log drain no detiene el flujo funcional ni contamina el otro ambiente.
- Alertas/runbooks cuando el cambio introduce journeys publicos, asincronia, costos o dependencias LLM.
- Tarea `infra/` o manifiesto versionado cuando cambien backends OTel, log drains, retention, alert policies, dashboards, uptime checks o exports.

## Criterio de salida

Una release no queda lista solo porque puede diagnosticarse manualmente. Debe demostrar:

- Journey critico reconstruible por `trace_id`/`correlation_id`.
- Operaciones IA explican latencia, error, retry, tokens, costo, provider/model y aprobacion humana.
- Eventos de producto relevantes no se infieren desde logs tecnicos.
- Datos sensibles no aparecen en logs, spans, labels, dashboards ni exports.
- `demo` y `beta` comparten semantica de eventos, metricas, estados y errores.
- Las senales incluyen ambiente, servicio, version y plataforma.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Creacion inicial | Convertir `master-plan-observabilioty-and-telemetry` en reglas operativas por release sin crear una release tecnica transversal | Master Plan, R01-R06, tareas futuras de api/agents/web/infra | D-OBS-01..D-OBS-08 |
