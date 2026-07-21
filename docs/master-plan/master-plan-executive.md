# Master Plan Ejecutivo — GradeOps AI

## Resumen ejecutivo

GradeOps AI debe avanzar como producto de operaciones de evaluacion para docentes de programacion: crear evaluaciones, procesar respuestas reales, mantener control docente, generar feedback/reportes y producir evidencia auditable de IA, costos, uso y negocio.

El plan queda organizado en ocho releases. Las primeras seis componen el corte validacion MVP; las dos ultimas concentran refinamientos P1 para no convertir el MVP en un backlog XL.

La decision de planificacion mas importante ya tomada es que **Open y Closed son P0** para el Master Plan. D-01 ya resolvio los roles de entorno: `beta` sostiene evidencia funcional y pilotos tempranos; `demo` se mantiene como target Google Cloud/Gemini-capable para claims que requieran prueba en GCP. La restriccion restante no es de decision, sino de evidencia: cada claim debe apuntar al ambiente, commit y provider realmente probados.

La Plataforma de Agentes se trata como capacidad transversal: no se implementa como una release tecnica aislada, sino como incrementos del runtime generico dentro de cada release funcional. El objetivo es evolucionar desde el Assessment Agent actual, que ejecuta una llamada LLM estructurada, hacia un runtime headless capaz de iterar con herramientas autorizadas, validar salidas, declarar bloqueos y registrar pasos sin entregar autoridad de dominio al modelo.

La orquestacion API-Agent sigue la misma regla: no se crea una release tecnica separada para robustecer `api/`. Cada release funcional debe incorporar el incremento necesario para que `api/` sea el intermediario durable entre `web/` y `agents/`, con endpoints REST de intencion, estados consultables, idempotencia, evidencia y un gate de madurez Richardson para toda tarea que defina endpoints o rutas.

La seguridad y autorizacion siguen la misma regla: no se crea una release tecnica separada de hardening. Cada release funcional debe incorporar el incremento de roles, permissions, ownership, service-to-service, rutas web, signed links, headers, secretos, exports, topologia multiambiente y pruebas negativas que corresponda al valor entregado.

La observabilidad y telemetria siguen la misma regla: no se crea una release tecnica aislada de logging o dashboard. Cada release funcional debe incorporar las trazas, logs, metricas, eventos canonicos, evidencia durable, alertas y adaptadores multiambiente necesarios para explicar el valor entregado y operar el flujo.

## Estado documental

- Fase 01 diagnostico que la documentacion de intencion es solida, pero el estado real del repo esta desactualizado en varios documentos.
- Fase 02 fijo 15 capacidades, 62 historias in-scope y 5 out-of-scope, sin colisiones de ID tras renumerar Epic 01.
- Fase 03 identifico 21 procesos automatizables y confirmo que C13/C14 no pueden dejarse para el final.
- La estrategia transversal de Agent Runtime fija que cada release con IA declare agente, herramientas, validadores, limites, costo, HITL y evidencia.
- La estrategia API-Agent Orchestration fija que `api/` es la unica interfaz de `web/`, que `agents/` no persiste dominio y que los endpoints/rutas nuevos se revisan contra madurez REST.
- La estrategia Security & Authorization fija que `api/` conserva RBAC/ownership/dominio, `agents/` protege capacidades service-to-service, `web/` representa capacidades sin autorizar recursos y `demo`/`beta` deben aislar identidad, datos, secretos y config.
- La estrategia Observability & Telemetry fija que `web`, `api`, `agents` e `infra` emiten senales portables con OpenTelemetry, W3C Trace Context, eventos canonicos y adaptadores por ambiente.
- Fase 05 documento R01-R06 y Fase 06 valido el plan con resultado `PASS WITH CONDITIONS`.
- Los documentos con mayor drift pendiente son `CLAUDE.md`, `09-developer-guide/`, `05-evidence/agent-logs.md`, los cortes P0 de `02-product/user-stories*.md` y la narrativa/pricing de validacion MVP.

## Supuestos y decisiones

| ID | Estado | Impacto en el plan |
|---|---|---|
| D-01 | Resuelta: roles de entorno | `beta` puede sostener evidencia funcional; `demo` prueba target Google Cloud solo cuando exista deployment/provider evidence. |
| D-02 | Resuelta: Closed = P0 | R04/R05 entran al corte validacion MVP. |
| D-03 | Resuelta | Trazabilidad de US queda estable. |
| D-04 | Pendiente | R01 debe formalizar provider/model policy para costos. |
| D-05 | Pendiente | Afecta onboarding de colaboradores y arquitectura documentada. |
| D-06 | Pendiente | R01/R06 deben adoptar esquema rico de AgentExecutionLog. |
| D-07 | Pendiente | R06 debe reconciliar pricing antes del paquete final de validacion. |
| D-SEC | Aceptada | Seguridad se implementa dentro de cada release funcional con gates de permisos, ownership, service-to-service, datos sensibles, topologia multiambiente y pruebas negativas. |
| D-OBS | Aceptada | Observabilidad se implementa dentro de cada release funcional con trazas, logs, metricas, eventos canonicos, evidencia durable y adaptadores multiambiente. |

## Objetivos estrategicos

1. Probar un flujo real de evaluacion de programacion con IA y aprobacion docente.
2. Procesar respuestas reales o semi-reales con costo y evidencia por unidad.
3. Generar feedback/reporte util para el docente y evidencia de impacto.
4. Mostrar operacion AI-native: logs, modelos, tokens, costos, reintentos y aprobaciones.
5. Obtener evidencia comercial: pilotos, revenue/commitments, costos y testimonios.
6. Cumplir las restricciones del validacion MVP sin sacrificar el entorno que ya produce evidencia real.

## Mapa de capacidades resumido

| Dominio | Capacidades |
|---|---|
| Acceso | C1 Identidad Docente; C2 Acceso Estudiante sin cuenta |
| Creacion | C3 Assessment Open; C4 Rubrica; C5 Curriculum; C6 Banco/ensamblaje Closed |
| Ejecucion Open | C7 Submissions; C8 Grading; C9 Feedback; C10 Brechas/recuperacion; C11 Reporte |
| Ejecucion Closed | C12 Analitica de items |
| Evidencia/operacion | C13 Agent logs; C14 Dashboard de evidencia |
| Negocio | C15 Facturacion y limites |

## Flujo critico

El flujo critico tiene dos ramas:

- **Open**: login -> brief -> assessment draft -> rubrica -> submissions -> grading suggestion -> feedback -> gaps/recovery -> teacher report -> evidencia.
- **Closed**: login -> tags/outcomes -> question batch -> curation -> bank -> composition -> snapshot -> links -> attempts -> deterministic grading -> item analytics -> evidencia.

Ambas convergen en C13/C14: sin logs/costos/aprobaciones no hay prueba de valor, operacion AI-native ni evidencia de negocio.

## Diagnostico global de US

- 62 historias in-scope, 5 out-of-scope.
- 17 historias enriquecidas o materializadas: Epics 01-02 mas US-080/US-081.
- 45 historias esqueléticas en Epics 03-13 que requieren `/us-enrich` antes de atomizar, excluyendo US-080/US-081 ya materializadas para R01.
- 41 historias P0, 17 P1, 1 condicional.
- 8 US propuestas cubren gaps de operator access, privacidad, fallos/retry, idempotencia, costos, health, provider transparency y pilotos.

## Automatizacion resumida

| Tipo | Procesos |
|---|---|
| IA asistida/supervisada | assessment, rubrica, grading, feedback, gaps, recovery, report, question generation, distractor/ambiguity, assembly, item analytics |
| Determinista | submission validation, closed snapshot/scoring, links, usage counters, ledgers |
| Operacion | agent logs, cost estimation, evidence dashboard, pilot/payment evidence, retry/idempotency |

El plan no recomienda autonomia pedagogica durante el MVP. El nivel objetivo para decisiones sensibles es **Supervisada**.

## Agent Runtime transversal

| Release | Incremento runtime | Valor funcional |
|---|---|---|
| R01 | Consolidar provider/model policy, logs ricos, errores, costo, idempotencia y compatibilidad del Assessment Agent actual | Primer draft/regeneration con evidencia confiable |
| R02 | Extraer registry/gateway/contratos reutilizables al incorporar Rubric, Grading y Feedback | Segundo consumidor real sin duplicar infraestructura |
| R03 | Incorporar handoffs tipados y herramientas read-only/agregadas para gaps, recovery y reportes | Interpretacion de cohorte basada en hechos y aprobacion docente |
| R04 | Introducir tool loop controlado, policy engine basico y validadores para preguntas/banco/composicion | Closed authoring con IA supervisada y snapshot deterministico |
| R05 | Agregar persistencia asincrona/reanudable cuando analytics o volumen lo requieran | Student attempts y analytics sin bloquear flujos largos |
| R06 | Operacionalizar metricas, health, costos, warnings, readiness y Ops Agent read-only | Evidencia de validacion y operacion sostenible |

## API-Agent Orchestration

| Release | Incremento de API robusta | Valor funcional |
|---|---|---|
| R01 | `AiOperation`/`AgentRun`/`AgentAttempt` minimos, `Idempotency-Key`, provider/model efectivo, errores enriquecidos y consulta de operacion para assessment generation/regeneration | El primer flujo IA deja evidencia durable y puede ser retomado/reintentado sin doble costo |
| R02 | Contratos REST de intencion para rubric/grading/feedback, registry liviano y contract tests API-Agents/API-Web | Primer ciclo Open usa agentes sin que `web/` orqueste prompts o providers |
| R03 | Handoffs tipados y herramientas read-only/agregadas detras de endpoints funcionales de reportes/gaps/recovery | Reportes de impacto se basan en hechos persistidos y no en inferencias opacas |
| R04 | Endpoints de question bank/snapshot con tool loop controlado y validators; snapshot/scoring quedan en API | Closed authoring usa IA supervisada sin entregar grading deterministico al modelo |
| R05 | Outbox/Cloud Tasks/executor/polling cuando el volumen lo exija para attempts, analytics o batches | Estudiantes y analytics pueden avanzar con progreso parcial y retry selectivo |
| R06 | Health, costs, ledgers, readiness y evidence exports calculados por API/DB; Ops Agent solo read-only | Evidencia operativa confiable para validacion y negocio |

## Security & Authorization

| Release | Incremento de seguridad | Valor funcional |
|---|---|---|
| R01 | Cuenta/roles base, `AuthenticatedAccount`, permisos Teacher, ownership en assessment, audit actor desde principal, fail-secure minimo en agents/web y contrato multiambiente inicial | Primer flujo IA protegido y auditable en el ambiente declarado |
| R02 | Authorities para rubric/submission/grading/feedback, validacion de archivos, minimizacion PII, provider/model allowlist y limites por request | Primer ciclo Open protege submissions y feedback |
| R03 | Reportes/gaps/recovery scoped, agregacion segura, ocultamiento student-level y herramientas read-only | Reporte de impacto sin filtrar datos sensibles |
| R04 | Question bank/snapshot con ownership, snapshot inmutable, policy engine basico y output cerrado | Closed authoring seguro sin datos estudiantiles |
| R05 | Signed links hasheados/expirables/revocables, anti-enumeracion, result access scoped y replay/tamper tests | Student access sin cuenta con aislamiento real |
| R06 | Operator auth, `/api/v1/operator/**`, exports allowlisted, evidence links revocables, health sin secretos, audit no editable y aislamiento `demo`/`beta` probado | Evidencia de negocio operable sin exponer PII/secrets ni mezclar ambientes |

## Observability & Telemetry

| Release | Incremento de observabilidad | Valor funcional |
|---|---|---|
| R01 | Contratos/ADR OTel, taxonomia de IDs, JSON stdout, W3C propagation, Web instrumentation minima y `AiOperation`/`AgentRun`/`AgentAttempt` trazables | Primer flujo IA reconstruible de navegador a proveedor LLM |
| R02 | Journey rubric/submission/grading/feedback con spans, metricas GenAI, eventos canonicos y calidad por aprobacion/edicion/rechazo | Primer graded submission medible por valor, costo y calidad |
| R03 | Report/gap/recovery con herramientas read-only trazadas, metricas de agregacion, validacion docente y estimacion de impacto versionada | Impacto Open explicable sin depender de logs crudos |
| R04 | Question generation/review/assembly trazados, validadores medidos, snapshot auditado y costo por batch/pregunta | Closed authoring operable con calidad y costo visibles |
| R05 | Attempts, signed links, scoring e item analytics con propagacion asincrona, queue delay, heartbeat, retry y deteccion de estancamiento | Student flow y analytics observables sin duplicar ejecuciones |
| R06 | Dashboard Operator via APIs/agregados, SLI/SLO baseline, alertas, runbooks, exports, readiness y comparacion `demo`/`beta` | Operacion y evidencia comparables entre ambientes |

## Priorizacion

La secuencia prioriza:

1. Evidencia y costo desde el primer flujo.
2. Primer graded submission Open.
3. Reporte e impacto para piloto.
4. Closed en dos cortes para evitar XL.
5. Evidencia comercial y cumplimiento validacion MVP.
6. Refinamientos P1 solo despues de valor validado.

## Resumen de releases

| Release | Nombre | Complejidad | Estado | Resultado |
|---|---|---|---|---|
| R01 | Assessment Creation + Evidence Backbone | M | Planificada, parcialmente implementada | Primer flujo IA con logs/costo/idempotencia |
| R02 | Open Graded Feedback Thin Slice | L | Planificada | Primera submission calificada y feedback aprobado |
| R03 | Open Cohort Report and Impact | M | Planificada | Reporte docente, gaps y tiempo ahorrado |
| R04 | Closed Question Bank to Snapshot | L | Planificada | Banco curado y assessment cerrado publicado |
| R05 | Closed Student Response and Item Analytics | L | Planificada | Estudiantes responden por link y analytics de items |
| R06 | Business Evidence and Operational Readiness | M | Planificada/paralela | Dashboard, revenue/cost ledger y evidencia de submission |
| R07 | Open Workflow Refinements | M | Roadmap | Bulk, tone, exports y mejoras P1 Open |
| R08 | Closed and Curriculum Refinements | M | Roadmap | Curriculum IA, coverage y recalculo auditado |

## Camino critico

1. Cerrar R01 con logs/costos/provider policy confiables.
2. Implementar R02 como primer valor economico: graded submission.
3. Usar R03 para producir narrativa de impacto.
4. Construir Closed como dos thin slices (R04/R05).
5. Consolidar R06 con evidencia comercial, costos y deployment proof por ambiente.
6. Mantener claims de `beta` y `demo` separados hasta que cada uno tenga evidencia real.

## Hitos

| Hito | Release |
|---|---|
| Primera llamada IA trazada | R01 |
| Primer assessment draft revisado | R01 |
| Primer estudiante procesado | R02 |
| Primer feedback aprobado | R02 |
| Primer reporte de impacto | R03 |
| Primer assessment Closed publicado | R04 |
| Primer intento Closed calificado | R05 |
| Primer piloto/evidencia comercial completa | R06 |
| Release candidata validacion MVP | R06 |

## Matriz de trazabilidad global

| Capacidad | Release primaria |
|---|---|
| C1 | R01 |
| C2 | R05 |
| C3 | R01 |
| C4 | R02 |
| C5 | R04/R08 |
| C6 | R04/R08 |
| C7 | R02/R07 |
| C8 | R02/R03 |
| C9 | R02/R07 |
| C10 | R03/R07 |
| C11 | R03/R07 |
| C12 | R05/R08 |
| C13 | R01-R06 |
| C14 | R06 |
| C15 | R01/R06 |

## Riesgos

| Riesgo | Mitigacion |
|---|---|
| Claims de entorno sin evidencia | Mantener `beta` como evidencia funcional y `demo` como target GCP solo cuando exista deployment/provider proof. |
| Scope creep por Closed P0 | Dividir Closed en R04/R05 y dejar refinamientos en R08. |
| Evidencia tardia | AUT-16/AUT-17 desde R01. |
| Historias NOT READY | Ejecutar enriquecimiento antes de atomizar cada release. |
| Operator sin acceso definido | Incluir US-PROPUESTA-01 en R06. |
| Cost model Gemini-only | Resolver D-04 y registrar provider/model dinamico. |

## Metricas y evidencias

| Categoria | Evidencia |
|---|---|
| Producto | assessments, submissions, attempts, reports |
| AI-native | agent runs, model/provider, tokens, costs, retries |
| Human control | approvals, edits, rejections, overrides |
| Unit economics | cost per run, assessment, graded submission, customer |
| Business | pilots, revenue, commitments, related-party split |
| Validacion MVP | demo video, dashboard/export, deployment/provider evidence por ambiente |

## Siguiente accion

Ejecutar **R01 — Assessment Creation + Evidence Backbone** usando `docs/master-plan/releases/release-01-assessment-creation-evidence-backbone.md`, `docs/master-plan/analysis/agent-runtime-strategy.md` y `.planning/active/008-assessment-creation/R01-RELEASE-BRIDGE.md` como fuentes operativas. R01 no debe bloquearse por D-01, pero si debe cerrar o registrar explicitamente D-04/D-06, usar US-080/US-081 ya materializadas y estabilizar el baseline runtime del Assessment Agent antes de escalar a Rubric/Grading/Feedback.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Aplicacion de fit review del planning plugin | Reconciliar D-01 como decision resuelta, apuntar R01 al release bridge y dejar claro el gate de evidencia por ambiente | Resumen, decisiones, camino critico, siguiente accion | D-01, D-04, D-06 |
| 2026-07-21 | Incorporacion de Observability & Telemetry | Alinear el Master Plan con observabilidad por release funcional desde `docs/.prompting/master-plan-observabilioty-and-telemetry/` | Resumen, estado documental, Observability & Telemetry | D-OBS-01..D-OBS-08 |
| 2026-07-21 | Incorporacion de topologia de seguridad multiambiente | Alinear `demo` GCP y `beta` Vercel-Render-Neon con un contrato comun de seguridad y mecanismos distintos | Resumen, estado documental, Security & Authorization | D-SEC-01..D-SEC-08 |
| 2026-07-21 | Incorporacion de Security & Authorization | Alinear el Master Plan con controles de seguridad por release funcional desde `docs/.prompting/master-plan-security/` | Resumen, estado documental, Security & Authorization | D-SEC-01..D-SEC-08 |
| 2026-07-20 | Incorporacion de Agent Runtime transversal y actualizacion post Fase 05/06 | Alinear el ejecutivo con la estrategia headless/iterativa de agentes y el estado documentado real | Resumen, estado documental, automatizacion, siguiente accion | D-04, D-06 |
| 2026-07-20 | Incorporacion de API-Agent Orchestration | Alinear el Master Plan con `api/` como intermediario robusto entre `web/` y `agents/` sin crear release tecnica transversal | Resumen, estado documental, API-Agent Orchestration | D-API-01..D-API-10 |
| 2026-07-19 | Creacion inicial | Ejecucion de la Fase 04 del Master Plan Ejecutivo | Todo el documento | D-01, D-02, D-04, D-06 |
