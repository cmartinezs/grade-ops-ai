# Release 03 - Open Cohort Report and Impact

## 1. Identificacion

| Campo | Valor |
|---|---|
| Release | R03 |
| Nombre | Open Cohort Report and Impact |
| Archivo | `docs/master-plan/releases/release-03-open-cohort-report-impact.md` |
| Estado | Documentada |
| Complejidad | M |
| Corte | MVP / cierre del ciclo Open con narrativa de impacto |
| Fuente estrategica | `docs/master-plan/analysis/release-strategy.md` |

## 2. Prevalidacion

| Precondicion | Resultado |
|---|---|
| La release existe en `master-plan-executive.md` | OK |
| Sus US estan asignadas | OK: US-042, US-043, US-060, US-061, US-070, US-083 |
| No existen decisiones bloqueantes para definir esta release | OK si R01/R02 dejaron resueltas o heredadas las decisiones de logs, costo, privacidad e idempotencia |
| La release no es XL | OK: M si no incluye export, delivery automatico, notas individuales ni dashboard completo |
| Habilita un flujo vertical | OK: varias submissions -> override/rechazo -> gaps -> recovery -> reporte -> time-saved estimate |
| Dependencias anteriores claras | OK: R02 es dependencia obligatoria |

## 3. Objetivo ejecutivo

Completar el cierre Open despues de grading y feedback: permitir que el docente edite o rechace sugerencias de IA, consolidar patrones de aprendizaje del cohorte, proponer una actividad de recuperacion y generar un reporte docente con evidencia de costo, uso y tiempo ahorrado.

R03 convierte los outputs aislados de R02 en una narrativa de impacto vendible para piloto, demo y validacion MVP.

## 4. Problema

R02 demuestra que una submission puede analizarse y recibir feedback aprobado, pero no prueba aun que el producto ayude a tomar decisiones de curso. Sin reporte, gaps, recuperacion y estimaciones marcadas como tales, el demo se queda en acciones individuales y no muestra ahorro ni valor pedagogico agregado.

## 5. Hipotesis

Si el docente puede revisar decisiones de grading, confirmar gaps agregados, aprobar una actividad de recuperacion y generar un reporte con costo y tiempo ahorrado estimado, entonces GradeOps AI puede comunicar impacto de manera creible sin necesitar un LMS, export masivo ni dashboard comercial completo.

## 6. Actor beneficiado

- **Teacher**: obtiene una sintesis accionable del assessment y puede ajustar resultados antes de usarlos.
- **Operator**: obtiene evidencia de valor, uso y ahorro para vender o defender el piloto.
- **Developer**: obtiene contratos de cierre Open sobre datos ya generados por R02.
- **Student**: se beneficia indirectamente por una recuperacion mejor dirigida, aprobada por el docente.

## 7. Valor entregado

- Control docente completo sobre score sugerido: aprobar, editar o rechazar.
- Trazabilidad de teacher overrides.
- Primer resumen de brechas de aprendizaje a nivel cohorte.
- Primera sugerencia de recuperacion vinculada a gaps.
- Primer reporte docente de assessment.
- Estimacion de tiempo ahorrado claramente marcada como estimada.
- Evidencia util para narrativa de impacto del validacion MVP y pilotos.

## 8. Nivel de automatizacion

| Proceso | Nivel inicial | Nivel objetivo en R03 |
|---|---|---|
| Edicion/rechazo de grading | Manual | Supervisada con trazabilidad completa |
| Deteccion de gaps | Asistida | Supervisada con confirmacion docente |
| Sugerencia de recuperacion | Asistida | Supervisada con aprobacion docente |
| Reporte docente | Asistida | Supervisada con validacion docente |
| Time-saved estimate | Parcial | Automatizada como estimacion etiquetada |
| Logs y metricas | Automatizada | Automatizada para eventos de cierre Open |

## 9. Alcance incluido

- Permitir editar score por criterio.
- Permitir registrar nota o motivo de edicion.
- Preservar sugerencia original de IA separada del score final.
- Permitir rechazar una sugerencia de IA.
- Registrar motivo de rechazo y cambio de estado de submission.
- Generar learning gap summary desde resultados por criterio, feedback y submission count.
- Mostrar affected submission count, severidad y criterios relacionados por gap.
- Permitir confirmar, editar o descartar gaps.
- Generar al menos una actividad de recuperacion asociada a gap confirmado o draft.
- Permitir editar, aprobar o rechazar actividad de recuperacion.
- Generar assessment report con resumen, submissions, stats simples, gaps, next action, evidencia/costo y tiempo ahorrado.
- Registrar time-saved estimate con baseline docente o default.
- Registrar logs/costo para Learning Gap, Recovery y Teacher Report Agent.

## 10. Exclusiones explicitas

- Student-specific recovery notes.
- Export report.
- Automatic delivery o publicacion del reporte.
- Tone adjustment avanzado.
- Dashboard completo de negocio/evidencia.
- Cohort analytics avanzados.
- Predicciones high-stakes.
- Perfilamiento individual de estudiantes.
- Integracion LMS.
- Closed assessment mode.
- Billing self-serve o pricing final.

## 11. Capacidades

| Capacidad | Rol en R03 |
|---|---|
| C8 Calificacion asistida Open | Se completa con edicion/rechazo docente. |
| C10 Gaps y recuperacion | Proceso principal para impacto pedagogico. |
| C11 Reporte docente | Salida principal de cierre Open. |
| C13 Evidencia de ejecucion de agentes | Logs obligatorios para gaps, recovery y report. |
| C14 Evidencia de impacto | Parcial: time-saved estimate y narrative evidence. |

## 12. Flujo funcional

```text
Teacher opens an assessment processed in R02
  -> reviews multiple grading suggestions
  -> approves, edits, or rejects suggestions
  -> product preserves original AI suggestion and teacher decision
  -> teacher requests learning gap summary
  -> Learning Gap Agent aggregates rubric results and feedback themes
  -> teacher confirms, edits, or discards gaps
  -> teacher requests recovery activity for selected gaps
  -> Recovery Agent suggests short focused activity
  -> teacher edits, approves, or rejects activity
  -> teacher requests assessment report
  -> Teacher Report Agent generates report with cost, usage and time-saved estimate
  -> teacher validates report before sharing or using it externally
```

## 13. User stories incluidas

| US | Titulo | Estado en R03 |
|---|---|---|
| US-042 | Teacher Edit of Score | P0, debe enriquecerse antes de atomizar |
| US-043 | Reject AI Suggestion | P0, debe enriquecerse antes de atomizar |
| US-060 | Learning Gap Summary | P0, debe enriquecerse antes de atomizar |
| US-061 | Recovery Activity Suggestion | P0, debe enriquecerse antes de atomizar |
| US-070 | Assessment Report | P0, debe enriquecerse antes de atomizar |
| US-083 | Time Saved Estimate | P1 incluido por valor de impacto, debe enriquecerse antes de atomizar |

## 14. Historias propuestas o modificadas

| ID | Nombre | Uso en R03 |
|---|---|---|
| US-PROPUESTA-02 | Privacy and PII Handling for Real Pilot Data | Heredada de R02 si hay datos reales. |
| US-PROPUESTA-03 | Agent Run Retry and Failure Recovery | Debe cubrir Learning Gap, Recovery y Teacher Report Agent. |
| US-PROPUESTA-04 | Idempotent Assessment/Question Generation Requests | Extender a gap summary, recovery y report generation. |

No se requieren historias nuevas si R01/R02 ya cubrieron fallos, idempotencia y privacidad. Si no lo hicieron, esas propuestas deben entrar como prerequisito o corte tecnico dentro de R03.

## 15. Consideraciones adicionales para las US

- Todas las US incluidas estan en estado NOT READY o esqueletico; deben pasar por `/us-enrich`.
- US-042/043 completan el control docente que R02 dejo deliberadamente reducido.
- US-060 debe operar sobre agregados y evitar inferencias personales.
- US-061 no asigna actividades; solo propone y deja al docente aprobar.
- US-070 depende de datos aprobados o claramente marcados como pendientes.
- US-083 debe presentarse siempre como estimacion, nunca como ahorro certificado.

## 16. Reglas de negocio

- La sugerencia original de IA nunca se sobreescribe con el score docente.
- Todo edit/reject genera evento trazable.
- Un score final debe indicar si viene de aprobacion directa, edicion docente o rechazo de IA.
- Gaps se calculan desde resultados de grading y feedback aprobados o marcados como pending.
- Gap summary no es verdad final hasta confirmacion docente.
- Recovery activity no se asigna ni publica automaticamente.
- Reporte no es final ni compartible hasta validacion docente.
- Time-saved estimate debe mostrar formula, baseline o default usado.
- El reporte separa hechos, estimaciones e interpretaciones.

## 17. Dependencias

| Dependencia | Estado | Accion en R03 |
|---|---|---|
| R02 Open Graded Feedback Thin Slice | Previa obligatoria | Usar rubric, submissions, grading suggestions, feedback y usage events |
| US-040/041/050/051 | Deben existir funcionalmente | Fuente para decisions, gaps y report |
| D-04 Provider/model policy | Heredada | No redefinir proveedor |
| D-06 AgentExecutionLog rico | Heredada | Extender logs a gaps/recovery/report |
| Privacy/PII real | Heredada de R02 | Evitar student-level detail innecesario |
| Cost/usage events | Requerido | Alimentar report y time-saved evidence |

## 18. Integraciones

- `web/` teacher workspace para review queue, gaps, recovery y report.
- `api/` Spring Boot para transiciones, persistencia y ownership.
- `agents/` Learning Gap Agent, Recovery Agent y Teacher Report Agent.
- `api` -> `agents` via `agentclient`.
- PostgreSQL/Flyway para teacher decisions, gap summaries, recovery activities, reports y time-saved estimates.
- Evidence/logging backbone heredado de R01/R02.

## 19. Arquitectura minima necesaria

- `web/` no llama modelos directamente.
- `api/` conserva los estados finales y las decisiones docentes.
- `agentclient` concentra invocaciones a Learning Gap, Recovery y Teacher Report Agent.
- Teacher decisions se modelan separadas de AI suggestions.
- Gap summary se genera desde un source snapshot identificable.
- Recovery activity se versiona o conserva como draft editable.
- Report se versiona por `assessment + report_mode + source_snapshot`.
- Time-saved estimate se almacena como dato calculado con parametros visibles.

## 20. Datos y migraciones

Entidades/datos esperados:

- `TeacherGradingDecision`.
- `CriterionScoreOverride`.
- `AISuggestionRejection`.
- `LearningGapSummary`.
- `LearningGap`.
- `GapConfirmation`.
- `RecoveryActivity`.
- `RecoveryActivityApproval`.
- `AssessmentReport`.
- `AssessmentReportVersion`.
- `TimeSavedEstimate`.
- `AgentExecutionLog`.

Campos minimos nuevos o extendidos:

- `original_suggestion_id`.
- `final_score_source`.
- `override_note`.
- `rejection_reason`.
- `grading_snapshot_id`.
- `gap_summary_id`.
- `affected_submission_count`.
- `gap_severity`.
- `recovery_activity_status`.
- `report_mode`.
- `source_approval_state`.
- `time_saved_baseline_minutes`.
- `time_saved_estimate_minutes`.
- `estimate_method`.

## 21. Seguridad y privacidad

- Mantener auth y ownership server-side.
- Reporte debe minimizar student-level detail.
- Gap summary debe preferir agregados.
- No crear perfiles personales ni predicciones high-stakes.
- No exponer prompts internos, payloads completos ni datos sensibles innecesarios.
- Reportes student-safe, si existen, deben omitir evidencia interna y costos.
- Teacher validation es obligatoria antes de compartir cualquier reporte.

## 22. Observabilidad y auditoria

- `grading_score_edited`.
- `grading_suggestion_rejected`.
- `teacher_decision_recorded`.
- `learning_gap_summary_generated`.
- `learning_gap_confirmed`.
- `learning_gap_rejected`.
- `recovery_activity_generated`.
- `recovery_activity_approved`.
- `recovery_activity_rejected`.
- `assessment_report_generated`.
- `assessment_report_validated`.
- `time_saved_estimate_recorded`.
- `agent_run_started`, `agent_run_completed`, `agent_run_failed`.
- Correlation/request ID entre `web`, `api` y `agents`.

## 23. Automatizaciones

| ID | Proceso | Uso en R03 |
|---|---|---|
| AUT-06 | Grading suggestion Open | Fuente y extension por teacher overrides |
| AUT-08 | Deteccion de brechas | Proceso principal |
| AUT-09 | Sugerencia de recuperacion | Proceso principal |
| AUT-10 | Reporte docente | Proceso principal |
| AUT-16 | Registro de ejecuciones | Obligatorio |
| AUT-17 | Estimacion de tokens/costo | Obligatorio |

## Capacidades de IA y Agent Runtime

### Agentes involucrados

- Learning Gap Agent.
- Recovery Agent.
- Teacher Report Agent.
- Grading Agent como fuente de resultados y teacher decisions, no como nuevo alcance principal.

### Capacidades funcionales habilitadas

- Detectar patrones agregados de brechas.
- Proponer actividades de recuperacion.
- Redactar reporte docente con hechos, interpretaciones y estimaciones separadas.

### Incrementos del runtime requeridos

- Handoff tipado desde grading/feedback aprobado hacia gap/report.
- Herramientas read-only y compute-only para estadisticas, clusters y comparaciones.
- Soporte de `Block`/`NEEDS_INPUT` cuando no hay muestra suficiente o faltan datos aprobados.
- Validadores que distingan hechos estadisticos, hipotesis pedagogicas y estimaciones.
- Registro de fuentes/snapshot para que reportes y gaps sean reproducibles.

### Herramientas requeridas

- `load_performance_history`.
- `load_learning_outcome_map`.
- `calculate_error_clusters`.
- `compare_cohorts`.
- `load_learning_gaps`.
- `validate_recovery_alignment`.
- `calculate_assessment_statistics`.
- `load_criterion_performance`.
- `load_common_errors`.

### Validadores determinísticos

- Muestra minima o warning `small_sample_size`.
- Solo usar grading/feedback aprobado o etiquetar fuente pendiente.
- Time-saved estimate con metodo/version y etiqueta de estimacion.
- Reporte no mezcla PII innecesaria ni evidencia interna en vistas student-safe.

### Autonomía y controles humanos

- Learning Gap y Teacher Report: `EXECUTE_READ_ONLY` para consultar/calcular, con teacher validation antes de uso externo.
- Recovery: `DRAFT_ONLY`; teacher aprueba antes de asignar o compartir.
- El runtime no crea decisiones pedagogicas finales ni publica reportes.

### Límites operacionales

- No ejecucion masiva sin presupuesto por assessment.
- No inferencias personales sobre estudiantes.
- No reemplazar teacher overrides.
- Si falta informacion, finalizar con `BLOCKED` o warning, no inventar baseline.

### Métricas y consumo

- Costo por gap summary, recovery draft y report.
- Frecuencia de bloqueos por datos insuficientes.
- Teacher confirmation/edit/rejection rate.
- Time-saved estimate coverage.

### Evidencia de finalización

- Tests de handoff desde grading/feedback a report.
- Tests de small sample, pending data y source snapshot.
- Smoke con varias submissions procesadas y reporte validable.

### Deuda o capacidades diferidas

- Procesamiento masivo optimizado y memoria longitudinal profunda se difieren hasta tener volumen real.
- Multiagente autonomo se evita; los handoffs son tipados y orquestados por API/runtime.

## 24. Trigger, inputs y outputs

| Proceso | Trigger | Inputs | Outputs |
|---|---|---|---|
| Score edit | Teacher edits criterion score | grade suggestion, criterion, new score, note | final teacher score, override event |
| Suggestion rejection | Teacher rejects suggestion | grade suggestion, reason | rejected state, audit event |
| Gap summary | Teacher requests gaps | assessment, approved rubric, grading results, feedback summaries, submission count | cohort_gap_summary, warnings, agent log |
| Recovery activity | Teacher selects gap | gap, rubric, level, constraints, time available | recovery activity draft, approval state, agent log |
| Report generation | Teacher requests report | assessment, rubric, submissions, decisions, gaps, recovery, cost summary | assessment report draft, time-saved estimate, agent log |
| Report validation | Teacher validates report | report draft, edits/hide sections | validated report state |

## 25. Human in the loop

- Teacher decide score final cuando edita o rechaza.
- Teacher confirma, modifica o descarta gaps.
- Teacher aprueba recuperacion antes de asignarla o compartirla.
- Teacher valida el reporte antes de usarlo externamente.
- Operator puede revisar evidencia agregada, pero no altera decisiones pedagogicas.

## 26. Guardrails

- No ocultar teacher overrides.
- No reemplazar judgment docente con conclusiones del agente.
- No reportar estimaciones como hechos medidos.
- No diagnosticar causas personales de bajo rendimiento.
- No publicar ni exportar reporte automaticamente.
- No exponer prompts internos o PII innecesaria.
- No crear recovery plans extensos fuera del corte MVP.
- No mezclar datos pending con aprobados sin etiqueta visible.

## 27. Idempotencia

R03 debe definir keys estables para:

- score edit event: `grade_suggestion_id + criterion_id + teacher_decision_id`;
- rejection event: `grade_suggestion_id + rejection_request_id`;
- gap summary: `assessment_id + grading_snapshot_id`;
- recovery activity: `gap_summary_id + selected_gap_ids + constraints_hash`;
- report generation: `assessment_id + report_mode + source_snapshot_id`;
- time-saved estimate: `assessment_id + report_version_id + estimate_method`;
- log event: `agent_run_id`.

Si el usuario repite generation por refresh/doble click, el sistema debe recuperar el resultado existente o crear una nueva version explicitamente marcada, nunca reemplazar silenciosamente un reporte validado.

## 28. Reintentos y fallos

| Falla | Comportamiento esperado |
|---|---|
| Sin submissions suficientes | Mostrar warning de muestra pequena o bloquear si no hay datos |
| Grading data pendiente | Marcar fuente como pending en gaps/reporte |
| Gap output malformado | Log validation failure, no crear summary activo |
| Recovery activity demasiado grande | Mostrar warning y permitir editar/regenerar |
| Missing cost data | Marcar `missing_cost_data` y no inventar costo |
| Report timeout | Log failed, retry sin reemplazar reporte validado |
| Doble submit | No ejecutar segundo run facturable |

## 29. Reversion

- Un score edit puede corregirse con nuevo decision event.
- Una rejection puede quedar auditada aunque se ejecute nuevo analysis posterior.
- Un gap confirmado puede ocultarse, editarse o reemplazarse sin borrar el original.
- Una recovery activity puede rechazarse o reemplazarse.
- Un reporte validado no se sobreescribe; se crea nueva version.
- Un time-saved estimate se ajusta con nueva version o evento compensatorio.

## 30. Consumo y costos

R03 debe consolidar costo por assessment y por agente de cierre Open.

Campos:

- provider.
- model.
- model_policy.
- agent_name.
- assessment_id.
- grading_snapshot_id.
- report_version_id.
- input tokens o estimate.
- output tokens o estimate.
- estimated cost USD.
- retry count.
- time_saved_baseline_minutes.
- time_saved_estimate_minutes.
- estimate_method.

Time saved debe quedar etiquetado como estimacion. Si el docente entrega baseline, usarlo y persistirlo; si no, usar default documentado y visible.

## 31. Criterios funcionales

- Teacher puede editar score por criterio.
- Teacher puede registrar nota de edicion.
- Teacher puede rechazar sugerencia de IA con razon.
- Original AI suggestion queda trazable.
- Teacher puede generar learning gap summary.
- Gap summary muestra criterios, affected count y severidad.
- Teacher puede confirmar, editar o descartar gaps.
- Teacher puede generar recovery activity.
- Teacher puede aprobar, editar o rechazar recovery activity.
- Teacher puede generar assessment report.
- Report incluye time-saved estimate marcado como estimado.

## 32. Criterios tecnicos

- `api/` valida ownership, estados y transiciones.
- `api/` separa AI suggestion, teacher decision y final state.
- `agents` devuelve outputs estructurados para gaps, recovery y report.
- Persistencia soporta source snapshots para gaps y reportes.
- Migraciones Flyway cubren decisions, gaps, recovery, reports y estimates.
- Tests cubren happy path y errores principales.
- Cross-service smoke test prueba `api` -> `agents` para gaps/recovery/report real o provider controlado.

## 33. Criterios de calidad

- Reporte distingue hechos, interpretaciones y estimaciones.
- Gap summary evita sobreinterpretacion y perfilamiento.
- Recovery activity es corta, editable y alineada a gap/rubrica.
- Time-saved estimate explica baseline o default.
- UI deja claro si datos de grading/feedback estan pending.
- Copy no promete medicion perfecta ni ahorro certificado.

## 34. Criterios de seguridad

- Ownership denial mantiene patron consistente de la API.
- Student-level details no aparecen en reportes agregados salvo necesidad explicita.
- No se guardan secretos ni prompts completos en logs visibles.
- No se exponen datos de costo internos en modos student-safe.
- No hay publicacion sin teacher validation.
- Internal auth `api` -> `agents` sigue la decision vigente.

## 35. Criterios de observabilidad

- 100% de gap/recovery/report agent runs tienen log.
- Failed runs tambien se loguean.
- Teacher overrides y rejections quedan auditados.
- Report generated y validated quedan separados.
- Time-saved estimate queda versionado o trazable.
- Cost estimate existe o queda marcado como missing con razon.
- Correlation ID permite seguir request entre `web`, `api` y `agents`.

## 36. Criterios de despliegue

- Debe correr en entorno local integrado.
- Debe sostener smoke local con `api` y `agents`.
- Puede cerrarse sin export report ni dashboard completo.
- Si se usa entorno `beta`, documentar URL, fecha, commit y evidencia de smoke.
- Reporte puede ser vista interna validada sin descarga formal.

## 37. Criterios de negocio

- Primer reporte de impacto por assessment.
- Primer time-saved estimate para narrativa comercial.
- Primer conteo de teacher overrides.
- Primer conjunto de recovery activities approved.
- Evidencia suficiente para demo/validacion MVP sin dashboard completo.
- No prometer pricing final hasta resolver decisiones comerciales.

## 38. Definition of Done

- [ ] US-042/043/060/061/070/083 enriquecidas antes de atomizar.
- [ ] Flujo R02 con multiples submissions disponible como fuente.
- [ ] Teacher puede aprobar, editar y rechazar grading suggestions.
- [ ] Original AI suggestion se conserva despues de teacher decision.
- [ ] Learning Gap Agent genera summary con criterios, affected count y severidad.
- [ ] Teacher puede confirmar, editar o descartar gaps.
- [ ] Recovery Agent genera actividad corta asociada a gap.
- [ ] Teacher puede aprobar, editar o rechazar recovery activity.
- [ ] Teacher Report Agent genera reporte con gaps, recovery, costo, uso y time-saved estimate.
- [ ] Reporte queda pending hasta validacion docente.
- [ ] Logs/costo/idempotencia cubren gap, recovery y report.
- [ ] README/planning/release artifacts actualizados.

## 39. Validacion

Validaciones esperadas:

- Unit tests de `api` para teacher decisions, gap states, recovery states, report states y estimates.
- Integration tests de `api` con persistencia real para el flujo completo.
- Tests de `agents` para Learning Gap, Recovery y Teacher Report structured output.
- Tests de `web` para override/reject, gap review, recovery approval y report validation.
- Smoke local con `api` y `agents` reales o provider controlado.
- Prueba de idempotencia para gap summary y report generation.
- Prueba de fuente pending y missing cost data.

## 40. Escenario Given/When/Then

```gherkin
Given a verified teacher is signed in
And the teacher has an open assessment with multiple analyzed submissions from R02
When the teacher edits one AI score suggestion
And rejects another AI suggestion with a reason
And generates a learning gap summary
And approves a recovery activity
And generates and validates the assessment report
Then the original AI suggestions remain traceable
And teacher decisions are recorded separately from AI suggestions
And the gap summary links gaps to rubric criteria and affected submission counts
And the recovery activity is tied to a detected gap
And the report includes assessment summary, simple stats, gaps, recommended next action, cost evidence, and estimated time saved
And every agent run and teacher decision is auditable
```

## 41. Metricas

- Teacher override count.
- AI suggestion rejection count.
- Learning gaps detected.
- Gap confirmation rate.
- Recovery activities generated.
- Recovery activities approved.
- Reports generated.
- Reports validated.
- Time-saved estimate minutes.
- Agent log coverage.
- Missing cost data rate.

## 42. Evidencias

- Teacher override event.
- AI suggestion rejection event.
- Learning gap summary.
- Gap confirmation/edit/rejection state.
- Recovery activity approved.
- Assessment report generated.
- Assessment report validated.
- Time-saved estimate with method/baseline.
- AgentExecutionLog de Learning Gap Agent.
- AgentExecutionLog de Recovery Agent.
- AgentExecutionLog de Teacher Report Agent.
- Smoke test local o beta.
- Test output relevante.
- Enlace a planning y PRs cerrados.

## 43. Riesgos y mitigaciones

| Riesgo | Mitigacion |
|---|---|
| R02 no esta cerrada | Bloquear implementacion de R03 hasta tener submissions, grading y feedback reales. |
| US esqueleticas se atomizan prematuramente | Ejecutar `/us-enrich` antes de crear tareas. |
| Sobreinterpretacion pedagogica | Requerir confirmacion docente y mostrar uncertainty flags. |
| Reporte mezcla hechos con interpretacion | Separar secciones y source approval states. |
| Time saved parece dato certificado | Etiquetarlo como estimacion y mostrar baseline/metodo. |
| Scope creep hacia LMS/export/dashboard | Mantener export, delivery y dashboard fuera de R03. |
| PII en reportes | Usar agregados y modos de audiencia con minimizacion. |

## 44. Resultado esperado

Al cerrar R03, GradeOps AI tiene el primer cierre Open completo: el docente controla las decisiones de grading, confirma brechas, aprueba recuperacion y obtiene un reporte de assessment con evidencia de costo, uso y tiempo ahorrado estimado. Esta release entrega la narrativa de impacto necesaria para pilotos y desbloquea R06, donde esa evidencia se convierte en dashboard/operational readiness.

## 45. Prompt ejecutable `/release-*`

Comandos inspeccionados en `.planning/scripts/release.mjs` y `.planning/TUTORIAL/reference.md`:

- `/release-init`
- `/release-new vX.Y.Z -- <purpose>`
- `/release-add vX.Y.Z NNN-slug [NNN-slug ...]`
- `/release-remove vX.Y.Z NNN-slug`
- `/release-status [vX.Y.Z] [--mark-planned|--mark-in-progress|--mark-blocked|--mark-released|--mark-cancelled]`

Prompt operativo para crear la release en el sistema `.releases/` del plugin, con placeholders explicitos porque el Master Plan no fija version semantica, target period, fecha estimada ni planning ID operativo para R03:

```text
Contexto:
Estamos ejecutando R03 del Master Plan: Open Cohort Report and Impact.
Fuentes obligatorias:
- docs/master-plan/releases/release-03-open-cohort-report-impact.md
- docs/master-plan/master-plan-executive.md
- docs/master-plan/analysis/release-strategy.md
- docs/master-plan/analysis/automation-inventory.md
- docs/master-plan/analysis/user-story-inventory.md
- docs/02-product/workflows.md
- docs/02-product/user-stories/epic-05-grading-assistance/
- docs/02-product/user-stories/epic-07-learning-gaps-recovery/
- docs/02-product/user-stories/epic-08-teacher-report/
- docs/02-product/user-stories/epic-09-evidence-metrics/04-time-saved-estimate.md
- docs/03-ai-agents/learning-gap-agent.md
- docs/03-ai-agents/recovery-agent.md
- docs/03-ai-agents/teacher-report-agent.md

Precondiciones:
- R02 debe estar cerrada o existir evidencia aceptada de rubric, submissions, grading, feedback, usage y cost.
- No inventar planning IDs. Si no existe planning operativa para R03, crearla primero con el flujo de planning vigente.
- Ejecutar /us-enrich sobre las US de R03 antes de atomizar tareas.

Objetivo:
Crear y gestionar la release operativa R03: varias submissions procesadas -> teacher edits/rejections -> gaps -> recovery -> teacher report -> time-saved estimate.

Comandos:
1. Si .releases/ no existe:
   /release-init
2. Crear la release:
   /release-new <VERSION> -- Open Cohort Report and Impact --target <YYYY-QN-MN-WN> --date <YYYY-MM-DD>
3. Agregar planning(s) existentes de R03, despues de verificar que existen:
   /release-add <VERSION> <PLANNING_ID_R03>
4. Revisar estado:
   /release-status <VERSION>

Alcance:
- US-042, US-043.
- US-060, US-061.
- US-070.
- US-083.
- AUT-06, AUT-08, AUT-09, AUT-10, AUT-16, AUT-17.
- Extensiones minimas de privacidad, retry e idempotencia si R01/R02 no las cerraron.

Exclusiones:
- Student-specific recovery notes, export report, tone adjustment, automatic delivery, Closed mode, dashboard completo y billing self-serve.

Arquitectura:
- web -> api -> agents.
- agentclient es el unico caller de agents desde api.
- Teacher decisions se separan de AI suggestions.
- Gap/report generation usa source snapshots.
- Reporte no reemplaza versiones validadas.
- Time-saved estimate debe mostrar metodo/baseline.

Seguridad:
- Mantener auth y ownership server-side.
- Minimizar PII y usar agregados.
- No crear perfiles personales ni predicciones high-stakes.
- No publicar reportes sin teacher validation.
- No exponer prompts internos ni payloads completos en logs visibles.

Automatizacion:
- Gaps, recovery y report son procesos asistidos/supervisados.
- Logs, costo e idempotencia automatizados.
- Teacher controla decisiones pedagogicas y reporte final.

Trazabilidad:
- Si D-04 o D-06 no quedaron cerradas en R01/R02, registrar el residual antes de implementar R03.
- Si aparece contradiccion de docs vs codigo, registrarla y no resolverla silenciosamente.
- No implementar R04/R06 dentro de esta release.

Metricas:
- teacher override count.
- learning gaps detected.
- recovery activities approved.
- reports generated.
- reports validated.
- time-saved estimate minutes.
- agent log coverage.

Criterios:
- El flujo es demostrable de extremo a extremo.
- Tests y smoke real api -> agents documentados.
- README/planning/release status actualizados.
```

## 46. Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-20 | Incorporacion de capacidades de Agent Runtime | Declarar handoffs tipados y herramientas read-only/agregadas para impacto Open | Runtime, gaps, recovery, reports | D-04, D-06 |
| 2026-07-20 | Creacion inicial | Ejecucion de Fase 05 para R03 | Todo el documento | D-04, D-06 |
