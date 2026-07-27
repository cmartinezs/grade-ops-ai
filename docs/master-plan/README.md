# Master Plan — GradeOps AI

> [!IMPORTANT]
> This master plan predates the accepted 2026-07-27 product redesign. Treat its release sequence as a baseline to be reconciled—not current scope authority—until the code-alignment review and migration plan are complete. See [Assessment Operations Product Redesign](../99-decisions/2026-07-27-assessment-operations-product-redesign.md).


## Proposito

Este Master Plan transforma la documentacion, decisiones, user stories y estado real de GradeOps AI en una secuencia de releases incrementales, demostrables y orientadas a valor.

## Alcance

Incluye:

- diagnostico documental y tecnico;
- decisiones, supuestos y riesgos;
- mapa de capacidades;
- inventario de user stories;
- inventario de automatizacion;
- estrategia transversal de Agent Runtime;
- estrategia API-Agent Orchestration;
- estrategia transversal de seguridad;
- estrategia transversal de observabilidad y telemetria;
- estrategia transversal de testing y quality gates;
- estrategia transversal de UI Design System y semantica de datos;
- estrategia transversal de i18n;
- estrategia ejecutiva de releases;
- resumen ejecutivo;
- documentos detallados de releases R01-R06;
- reporte de validacion integral.

## Estado general

| Area | Estado |
|---|---|
| Fase 01: diagnostico | Completa |
| Fase 02: capacidades y US | Completa |
| Fase 03: automatizacion | Completa |
| Fase 04: estrategia de releases | Completa |
| Fase 05: documentos por release | Completa para R01-R06 |
| Fase 06: validacion final | Completa con condiciones |

## Convenciones

- `P0` indica alcance validacion MVP.
- `P1` indica mejora posterior o no bloqueante para primer valor.
- `US-PROPUESTA-*` indica historia faltante recomendada, aun no creada como archivo en `docs/02-product/user-stories/`.
- `Asistida`, `Supervisada` y `Automatizada` siguen los niveles definidos en la especificacion maestra.
- `Agent Runtime` es una capacidad transversal: se implementa mediante vertical slices funcionales, no como una mega-release tecnica.
- `API-Agent Orchestration` es una capacidad transversal: se incorpora en las releases funcionales como reglas de API, dominio, agents y web, no como una release tecnica separada.
- `Security & Authorization` es una capacidad transversal: se implementa por release funcional con controles de `api/`, `agents/`, `web/`, `infra/` y topologia multiambiente, no como una release tecnica separada.
- `Observability & Telemetry` es una capacidad transversal: se implementa por release funcional con trazas, logs, metricas, eventos canonicos, evidencia durable y adaptadores multiambiente, no como una release tecnica separada.
- `Testing & Quality Gates` es una capacidad transversal: se implementa por release funcional con unit/component, acceptance, contract, Compose, smoke, Sonar y JMeter segun impacto, no como una release tecnica separada.
- `UI Design/Data Semantics` es una capacidad transversal: toda implementacion de `web/` debe partir de un diseno basado en `web/design-system/`, clasificar la naturaleza de cada dato, alinear lectura/escritura con `api/`, definir sync/async por accion y usar controles coherentes con fuentes de verdad, restricciones, enums y datos maestros.
- `i18n` es una capacidad transversal: codigo fuente, contratos tecnicos, logs y telemetria permanecen en ingles; toda superficie user-facing debe usar locale explicito, traducciones, catalog labels, errores seguros y salidas GenAI alineadas al idioma del usuario.
- Los documentos de analisis son fuente de contexto; los archivos de release R01-R06 son la fuente operativa por release.

## Orden de lectura

1. [Diagnostico de documentacion](analysis/documentation-diagnosis.md)
2. [Decisiones y supuestos](analysis/decisions-and-assumptions.md)
3. [Mapa de capacidades](analysis/capability-map.md)
4. [Inventario de user stories](analysis/user-story-inventory.md)
5. [Inventario de automatizacion](analysis/automation-inventory.md)
6. [Estrategia transversal de Agent Runtime](analysis/agent-runtime-strategy.md)
7. [Estrategia API-Agent Orchestration](analysis/api-agent-orchestration-strategy.md)
8. [Estrategia transversal de seguridad](analysis/security-strategy.md)
9. [Estrategia transversal de observabilidad y telemetria](analysis/observability-strategy.md)
10. [Estrategia transversal de testing y quality gates](analysis/testing-strategy.md)
11. [Estrategia UI Design System y semantica de datos](analysis/ui-design-data-strategy.md)
12. [Estrategia transversal de i18n](analysis/i18n-strategy.md)
13. [Estrategia de releases](analysis/release-strategy.md)
14. [Master Plan Ejecutivo](master-plan-executive.md)
15. [Reporte de validacion](validation-report.md)

## Tabla de releases

| Release | Nombre | Estado | Complejidad | Archivo |
|---|---|---|---|---|
| R01 | Assessment Creation + Evidence Backbone | Documentada | M | [release-01-assessment-creation-evidence-backbone.md](releases/release-01-assessment-creation-evidence-backbone.md) |
| R02 | Open Graded Feedback Thin Slice | Documentada | L | [release-02-open-graded-feedback-thin-slice.md](releases/release-02-open-graded-feedback-thin-slice.md) |
| R03 | Open Cohort Report and Impact | Documentada | M | [release-03-open-cohort-report-impact.md](releases/release-03-open-cohort-report-impact.md) |
| R04 | Closed Question Bank to Snapshot | Documentada | L | [release-04-closed-question-bank-snapshot.md](releases/release-04-closed-question-bank-snapshot.md) |
| R05 | Closed Student Response and Item Analytics | Documentada | L | [release-05-closed-response-item-analytics.md](releases/release-05-closed-response-item-analytics.md) |
| R06 | Business Evidence and Operational Readiness | Documentada | M | [release-06-business-evidence-operational-readiness.md](releases/release-06-business-evidence-operational-readiness.md) |
| R07 | Open Workflow Refinements | Roadmap | M | `releases/release-07-open-workflow-refinements.md` |
| R08 | Closed and Curriculum Refinements | Roadmap | M | `releases/release-08-closed-curriculum-refinements.md` |

## Enlaces relativos

- [Estrategia de releases](analysis/release-strategy.md)
- [Estrategia transversal de Agent Runtime](analysis/agent-runtime-strategy.md)
- [Estrategia API-Agent Orchestration](analysis/api-agent-orchestration-strategy.md)
- [Estrategia transversal de seguridad](analysis/security-strategy.md)
- [Estrategia transversal de observabilidad y telemetria](analysis/observability-strategy.md)
- [Estrategia transversal de testing y quality gates](analysis/testing-strategy.md)
- [Estrategia UI Design System y semantica de datos](analysis/ui-design-data-strategy.md)
- [Estrategia transversal de i18n](analysis/i18n-strategy.md)
- [Resumen ejecutivo](master-plan-executive.md)
- [Reporte de validacion](validation-report.md)
- [Especificacion maestra](../.prompting/master-plan-prompts/master-plan-specification.md)
- [Prompts de fases](../.prompting/master-plan-prompts/README.md)

## Leyenda de estados

| Estado | Significado |
|---|---|
| Completa | Artefacto generado y revisado para la fase actual. |
| Documentada | Release con archivo detallado generado. |
| Planificada | Release definida a nivel estrategico; falta archivo operativo detallado. |
| Roadmap | Release posterior al corte validacion MVP. |
| Pendiente | Trabajo aun no ejecutado. |
| Bloqueada | Requiere decision externa antes de avanzar. |

## Ultima actualizacion

2026-07-21.

## Reglas de mantenimiento

- No modificar artefactos de fases anteriores sin registrar el cambio en su historial.
- No crear archivos de release fuera de Fase 05.
- Cada release debe conservar trazabilidad hacia capacidades, US, automatizaciones y evidencias.
- Toda nueva decision relevante debe registrarse en `analysis/decisions-and-assumptions.md`.
- Mantener D-01 visible como roles de entorno resueltos y exigir proof antes de hacer claims de `demo`, `beta` o provider.
- No mover P1 al MVP sin retirar o dividir otra carga equivalente.
- Toda release con IA debe declarar capacidades de Agent Runtime, herramientas, validadores, autonomia, HITL, limites, costo y evidencia.
- Toda release que toque `api/`, `agents/` o rutas funcionales de `web/` debe aplicar la estrategia API-Agent Orchestration y el gate Richardson REST en las tareas que definan endpoints, contratos o rutas.
- Toda release que toque endpoints, rutas, agentes, prompts, providers, secrets, uploads, signed links, exports o datos sensibles debe aplicar la estrategia transversal de seguridad y registrar pruebas negativas.
- Toda release que afecte despliegue, auth, service-to-service, CORS, secrets, DB, storage o frontend config debe declarar si aplica a `demo`, `beta` o ambos, y probar que no mezcla identidades, datos ni secretos entre ambientes.
- Toda release que toque journeys criticos, endpoints, agentes, asincronia, providers, dashboards, exports o evidencia debe aplicar la estrategia transversal de observabilidad y registrar trazas, metricas, eventos canonicos y pruebas de redaccion/cardinalidad.
- Toda release que toque `web/`, `api/`, `agents`, `.github/`, `infra/`, contratos, schemas, migrations, testkit, performance o despliegue debe aplicar la estrategia transversal de testing y declarar gates de PR, beta y demo segun impacto.
- Toda funcionalidad web debe ser alcanzable desde una accion visible de la UI. No se acepta cerrar una historia si la unica forma de llegar a la pantalla o flujo es escribir la URL directa; la US, tasks UI y pruebas unitarias/acceptance/e2e deben declarar y probar el entry point.
- Toda implementacion de UI debe tener diseno previo desde el Design System antes de wireframes/mockups, y una matriz de campos que clasifique dato libre, restringido, enum, catalogo maestro, numero, booleano, fecha, read-only o generated editable. Todos los datos de lectura/escritura deben estar alineados con `api/`; si falta endpoint/read model/catalogo/mutation, se debe crear scope `api/`/DB/infra o residual bloqueante. Toda accion debe declarar sync/async y, si es async, su completion model: polling, SSE, WebSocket, webhook server-to-server, push/notification u otro mecanismo explicito.
- Toda release con texto visible, catalogos, errores seguros, emails, reports, exports o contenido GenAI debe aplicar la estrategia i18n. El codigo fuente, field names, enum/error/event/metric/span codes, logs y telemetria siguen en ingles; `web` no hardcodea copy user-facing; `api` resuelve locale y fallback; `agents` recibe `outputLocale`/`contentLocale` cuando genere contenido visible; pruebas verifican traducciones, fallback, contratos de locale y que observabilidad no se localiza.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Incorporacion de i18n por release funcional | Exigir locale explicito para superficies user-facing y mantener codigo/telemetria en ingles | README, analysis/i18n-strategy.md, R01-R06, tasks UI/API/Agents | D-I18N-01..D-I18N-10 |
| 2026-07-21 | Incorporacion de UI Design/Data Semantics | Exigir diseno DS previo, matriz de campos y tratamiento de datos maestros/restringidos en toda implementacion web | README, analysis/ui-design-data-strategy.md, R01, US/tasks UI | D-UI-01..D-UI-08 |
| 2026-07-21 | Incorporacion de API I/O y sync/async contract | Exigir que datos de pantalla esten alineados con `api/` y que async tenga completion model explicito | README, API-Agent, testing, R01, tasks UI | D-UI-01..D-UI-08, D-API-01..D-API-10 |
| 2026-07-21 | Incorporacion de UI Action Reachability | Exigir que toda funcionalidad web sea accesible desde botones, enlaces, menus o acciones visibles y no solo por URL directa | README, API-Agent, testing, R01, US/tasks UI | D-API-01..D-API-10, D-TEST-01..D-TEST-09 |
| 2026-07-21 | Incorporacion de Testing & Quality Gates | Hacer obligatorios los gates de testing por release funcional, incluyendo unit/component, acceptance, contract, Compose, smoke, Sonar y JMeter | README, analysis/testing-strategy.md, releases | D-TEST-01..D-TEST-09 |
| 2026-07-21 | Incorporacion de Observability & Telemetry | Hacer obligatorios los controles de observabilidad por release funcional, sin crear release tecnica transversal | README, analysis/observability-strategy.md, releases | D-OBS-01..D-OBS-08 |
| 2026-07-21 | Incorporacion de topologia de seguridad multiambiente | Separar el contrato comun de seguridad de los mecanismos concretos de `demo` y `beta` | README, analysis/security-strategy.md, releases | D-SEC-01..D-SEC-08 |
| 2026-07-21 | Incorporacion de Security & Authorization | Hacer obligatorios los controles de seguridad por release funcional, sin crear release tecnica transversal | README, analysis/security-strategy.md, releases | D-SEC-01..D-SEC-08 |
| 2026-07-20 | Incorporacion de API-Agent Orchestration | Hacer obligatorias las reglas de intermediacion API entre `web/` y `agents/`, con madurez REST por tarea | README, analysis/api-agent-orchestration-strategy.md, releases, templates | D-API-01..D-API-10 |
| 2026-07-20 | Incorporacion de Agent Runtime transversal y actualizacion de estado Fase 05/06 | Alinear el README con releases documentadas, validacion final y estrategia runtime | README, orden de lectura, reglas de mantenimiento | D-04, D-06 |
