# Master Plan Ejecutivo — GradeOps AI

## Resumen ejecutivo

GradeOps AI debe avanzar como producto de operaciones de evaluacion para docentes de programacion: crear evaluaciones, procesar respuestas reales, mantener control docente, generar feedback/reportes y producir evidencia auditable de IA, costos, uso y negocio.

El plan queda organizado en ocho releases. Las primeras seis componen el corte MVP/hackathon; las dos ultimas concentran refinamientos P1 para no convertir el MVP en un backlog XL.

La decision de planificacion mas importante ya tomada es que **Open y Closed son P0** para el Master Plan. La decision mas critica aun pendiente es **D-01: entorno `demo` GCP/Gemini vs `beta` Render/Groq**. La estrategia avanza con el supuesto operativo recomendado: `beta` sostiene evidencia real de producto y `demo` debe resolverse como cumplimiento minimo si las bases exigen GCP/Gemini efectivo.

## Estado documental

- Fase 01 diagnostico que la documentacion de intencion es solida, pero el estado real del repo esta desactualizado en varios documentos.
- Fase 02 fijo 15 capacidades, 62 historias in-scope y 5 out-of-scope, sin colisiones de ID tras renumerar Epic 01.
- Fase 03 identifico 21 procesos automatizables y confirmo que C13/C14 no pueden dejarse para el final.
- Los documentos con mayor drift pendiente son `CLAUDE.md`, `09-developer-guide/`, `05-evidence/agent-logs.md`, los cortes P0 de `02-product/user-stories*.md` y la narrativa/pricing de hackathon.

## Supuestos y decisiones

| ID | Estado | Impacto en el plan |
|---|---|---|
| D-01 | Pendiente | Condiciona criterios de despliegue y evidencia de R06. |
| D-02 | Resuelta: Closed = P0 | R04/R05 entran al corte MVP/hackathon. |
| D-03 | Resuelta | Trazabilidad de US queda estable. |
| D-04 | Pendiente | R01 debe formalizar provider/model policy para costos. |
| D-05 | Pendiente | Afecta onboarding de colaboradores y arquitectura documentada. |
| D-06 | Pendiente | R01/R06 deben adoptar esquema rico de AgentExecutionLog. |
| D-07 | Pendiente | R06 debe reconciliar pricing antes de submission final. |

## Objetivos estrategicos

1. Probar un flujo real de evaluacion de programacion con IA y aprobacion docente.
2. Procesar respuestas reales o semi-reales con costo y evidencia por unidad.
3. Generar feedback/reporte util para el docente y evidencia de impacto.
4. Mostrar operacion AI-native: logs, modelos, tokens, costos, reintentos y aprobaciones.
5. Obtener evidencia comercial: pilotos, revenue/commitments, costos y testimonios.
6. Cumplir las restricciones del hackathon sin sacrificar el entorno que ya produce evidencia real.

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
- 15 historias enriquecidas en Epics 01-02.
- 47 historias esqueléticas en Epics 03-13 que requieren `/us-enrich` antes de atomizar.
- 41 historias P0, 17 P1, 1 condicional.
- 8 US propuestas cubren gaps de operator access, privacidad, fallos/retry, idempotencia, costos, health, provider transparency y pilotos.

## Automatizacion resumida

| Tipo | Procesos |
|---|---|
| IA asistida/supervisada | assessment, rubrica, grading, feedback, gaps, recovery, report, question generation, distractor/ambiguity, assembly, item analytics |
| Determinista | submission validation, closed snapshot/scoring, links, usage counters, ledgers |
| Operacion | agent logs, cost estimation, evidence dashboard, pilot/payment evidence, retry/idempotency |

El plan no recomienda autonomia pedagogica durante el MVP. El nivel objetivo para decisiones sensibles es **Supervisada**.

## Priorizacion

La secuencia prioriza:

1. Evidencia y costo desde el primer flujo.
2. Primer graded submission Open.
3. Reporte e impacto para piloto.
4. Closed en dos cortes para evitar XL.
5. Evidencia comercial y cumplimiento hackathon.
6. Refinamientos P1 solo despues de valor validado.

## Resumen de releases

| Release | Nombre | Complejidad | Estado | Resultado |
|---|---|---|---|---|
| R01 | Assessment Creation + Evidence Backbone | M | Planificada, parcialmente implementada | Primer flujo IA con logs/costo/idempotencia |
| R02 | Open Graded Feedback Thin Slice | L | Planificada | Primera submission calificada y feedback aprobado |
| R03 | Open Cohort Report and Impact | M | Planificada | Reporte docente, gaps y tiempo ahorrado |
| R04 | Closed Question Bank to Snapshot | L | Planificada | Banco curado y assessment cerrado publicado |
| R05 | Closed Student Response and Item Analytics | L | Planificada | Estudiantes responden por link y analytics de items |
| R06 | Business Evidence and Hackathon Compliance | M | Planificada/paralela | Dashboard, revenue/cost ledger y evidencia de submission |
| R07 | Open Workflow Refinements | M | Roadmap | Bulk, tone, exports y mejoras P1 Open |
| R08 | Closed and Curriculum Refinements | M | Roadmap | Curriculum IA, coverage y recalculo auditado |

## Camino critico

1. Resolver D-01 antes de cerrar R06.
2. Cerrar R01 con logs/costos/provider policy confiables.
3. Implementar R02 como primer valor economico: graded submission.
4. Usar R03 para producir narrativa de impacto.
5. Construir Closed como dos thin slices (R04/R05).
6. Consolidar R06 con evidencia comercial, costos y deployment proof.

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
| Release candidata hackathon | R06 |

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
| D-01 sin resolver | Decision explicita antes de R06; mantener beta como evidencia real mientras se valida demo GCP. |
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
| Hackathon | demo video, dashboard/export, deployment/Gemini evidence |

## Siguiente accion

Ejecutar Fase 05 para generar el archivo detallado de **R01 — Assessment Creation + Evidence Backbone**. Antes o durante ese corte, resolver D-04/D-06 a nivel de implementacion/documentacion minima y mantener D-01 visible como dependencia de R06.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-19 | Creacion inicial | Ejecucion de la Fase 04 del Master Plan Ejecutivo | Todo el documento | D-01, D-02, D-04, D-06 |
