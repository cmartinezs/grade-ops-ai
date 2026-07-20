# Inventario de automatizacion — GradeOps AI

> Fase 03 — Estrategia de automatizacion del Master Plan Ejecutivo.
> Este documento no fija la secuencia final de releases. Las releases candidatas son insumo para Fase 04.

## Resumen ejecutivo

GradeOps AI tiene 21 procesos automatizables relevantes para el MVP y el hackathon: 13 procesos de producto asociados a agentes de IA, 3 procesos deterministas, y 5 procesos operativos internos de evidencia, costos, monitoreo y pilotos.

La estrategia recomendada es **automatizacion progresiva con autoridad docente explicita**:

- Las decisiones pedagogicas sensibles comienzan como **Asistidas** o **Supervisadas**.
- Ningun agente publica, califica definitivamente, envia feedback o modifica resultados sin aprobacion humana.
- Las reglas deterministas se usan cuando son suficientes: scoring cerrado, limites de plan, snapshot, validaciones, idempotencia, ledger y alertas.
- La evidencia (`AgentExecutionLog`, `ApprovalEvent`, costo, uso, estado de piloto) debe instrumentarse desde el primer release vertical, no al final.
- El proveedor real de IA debe modelarse como `provider/model/policy`, no como una categoria Gemini-only, porque el estado real incluye Groq como proveedor por defecto (D-04 pendiente de ADR).

El principal hallazgo de esta fase es que **C13 (Evidencia de Ejecucion de Agentes) no puede tratarse como epic downstream**. Es una capacidad transversal habilitante: sin logging/costo/aprobacion no hay evidencia de hackathon, unit economics ni control humano verificable.

## Principios de automatizacion

1. **Human-in-the-loop por defecto**: teacher u operator conservan la decision final cuando el output afecta calificaciones, feedback, reportes, estudiantes, revenue o evidencia publica.
2. **IA para preparar, no para finalizar**: los agentes producen borradores, sugerencias, flags, reportes o interpretaciones; el sistema determinista aplica reglas aprobadas.
3. **Determinismo donde alcance**: scoring cerrado, snapshots, usage counters, ledger, ownership, idempotencia y validaciones no requieren LLM.
4. **Trazabilidad antes que velocidad**: cada ejecucion relevante debe dejar input/output summary, modelo/proveedor, costo estimado, estado, reintentos, errores y aprobacion humana.
5. **Privacidad minima**: la evidencia de negocio debe usar agregados, pseudonimos y enlaces controlados; nunca datos estudiantiles completos si no son necesarios.
6. **Costo visible**: premium fallback, reintentos y ejecuciones duplicadas deben quedar visibles y agregables por assessment/customer.
7. **Falla recuperable**: timeouts, outputs invalidos y errores de proveedor deben exponer accion clara: reintentar, cancelar, editar input o continuar manualmente.

## Mapa de procesos

| ID | Proceso | Capacidad | Responsable | Tipo | Estado actual estimado | Nivel inicial recomendado | Nivel objetivo |
|---|---|---|---|---|---|---|---|
| AUT-01 | Generacion de assessment open | C3 | Assessment Agent | IA producto | Implementado para Epic 02 | Asistida | Supervisada |
| AUT-02 | Regeneracion versionada de draft | C3 | Assessment Agent + API | IA producto | En progreso | Asistida | Supervisada |
| AUT-03 | Generacion de rubrica | C4 | Rubric Agent | IA producto | No implementado | Asistida | Supervisada |
| AUT-04 | Validacion de rubrica | C4 | Rubric Agent + reglas | IA + determinista | No implementado | Asistida | Supervisada |
| AUT-05 | Intake y validacion de submissions open | C7 | API/Web | Determinista | No implementado | Manual | Automatizada |
| AUT-06 | Sugerencia de calificacion open | C8 | Grading Agent | IA producto | No implementado | Asistida | Supervisada |
| AUT-07 | Generacion de feedback individual | C9 | Feedback Agent | IA producto | No implementado | Asistida | Supervisada |
| AUT-08 | Deteccion de brechas | C10 | Learning Gap Agent | IA producto | No implementado | Asistida | Supervisada |
| AUT-09 | Sugerencia de recuperacion | C10 | Recovery Agent | IA producto | No implementado | Asistida | Supervisada |
| AUT-10 | Reporte docente | C11 | Teacher Report Agent | IA producto | No implementado | Asistida | Supervisada |
| AUT-11 | Generacion de preguntas closed | C6 | Question Generation Agent | IA producto | No implementado | Asistida | Supervisada |
| AUT-12 | Revision de distractores y ambiguedad | C6 | Distractor Quality + Ambiguity Agents | IA producto | No implementado | Asistida | Supervisada |
| AUT-13 | Ensamblaje de assessment cerrado | C6 | Assessment Assembly Agent + reglas | IA + determinista | No implementado | Asistida | Supervisada |
| AUT-14 | Snapshot y scoring cerrado | C6/C2 | API | Determinista | No implementado | Automatizada | Automatizada |
| AUT-15 | Analitica de items | C12 | Item Analytics Agent + agregados | IA + determinista | No implementado | Asistida | Supervisada |
| AUT-16 | Registro de ejecuciones de agentes | C13 | Ops Agent + API | Operacion | Parcial en modelo/migracion | Automatizada | Automatizada |
| AUT-17 | Estimacion de tokens, costo y uso | C13/C15 | Ops Agent + API | Operacion | Parcial | Automatizada | Automatizada |
| AUT-18 | Dashboard de evidencia de negocio | C14 | Ops Agent + Web | Operacion | No implementado | Supervisada | Supervisada |
| AUT-19 | Links seguros y notificaciones a estudiantes | C2 | API + email | Determinista | No implementado | Automatizada | Automatizada |
| AUT-20 | Onboarding/pilotos/evidencia de pago | C1/C15 | Operator + Ops Agent | Operacion | Parcial/manual | Manual | Supervisada |
| AUT-21 | Reintentos, idempotencia y recuperacion de fallos | Transversal | API/Agents | Plataforma | Parcial | Automatizada | Automatizada |

## Inventario detallado

### AUT-01 — Generacion de assessment open

- **Capacidad asociada**: C3.
- **Actor actual**: Teacher.
- **Problema operativo**: transformar un objetivo docente en una evaluacion programable, clara y reusable sin partir desde cero.
- **Evento disparador**: teacher guarda un brief y solicita generar assessment.
- **Inputs**: teacher/account, assessment ID, learning goal, topic, level, lenguaje, duracion, cantidad estimada de estudiantes, restricciones y notas.
- **Decisiones requeridas**: aprobar, editar, regenerar o rechazar el draft.
- **Acciones**: crear draft estructurado, validar campos minimos, persistir version, registrar `AgentExecutionLog`.
- **Responsable**: Assessment Agent; API coordina persistencia.
- **Herramientas/integraciones**: agents service, proveedor LLM configurado, API, DB.
- **Nivel actual**: Asistida, implementada para Epic 02.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada, con validaciones automaticas y aprobacion docente explicita.
- **Human-in-the-loop**: teacher aprueba antes de usar como base de rubrica o publicar.
- **Acciones permitidas**: sugerir instrucciones, objetivos, entregables, constraints y evidencia esperada.
- **Acciones prohibidas**: publicar assessment, definir rubrica final, omitir aprobacion, generar contenido fuera del nivel solicitado.
- **Validaciones**: JSON estructurado, campos obligatorios, consistencia de duracion/nivel/lenguaje, flags de ambiguedad.
- **Guardrails**: no asumir politica institucional; no generar actividades no relacionadas con programacion MVP.
- **Idempotencia**: `request_id` por generacion; doble submit no debe crear dos runs facturables.
- **Reintentos/fallos**: timeout o output invalido permite retry con mismo brief y deja run fallido registrado.
- **Reversion**: conservar versiones previas; permitir volver al draft aprobado anterior.
- **Auditoria**: agent run, version, input/output summary, teacher approval state.
- **Datos sensibles**: no requiere datos estudiantiles.
- **Tokens/costos**: Flash-class/Groq-equivalente; costo por assessment.
- **Metrica de exito**: assessment creation completion rate, time to first assessment.
- **Evidencia**: draft versionado + log de agente.
- **Dependencias**: C1, US-010/011, D-04 para proveedor.
- **Riesgos**: drift proveedor Gemini/Groq; costos sin categoria Groq formal.
- **Release candidata**: primer release vertical ya iniciado.

### AUT-02 — Regeneracion versionada de draft

- **Capacidad asociada**: C3.
- **Actor actual**: Teacher.
- **Problema operativo**: permitir iteracion sin perder historial ni sobreescribir decisiones.
- **Evento disparador**: teacher solicita regenerar con notas.
- **Inputs**: draft actual, notas de ajuste, historial de versiones, constraints originales.
- **Decisiones requeridas**: aceptar nueva version, volver a anterior o seguir editando.
- **Acciones**: generar nueva version, marcar current/pending, registrar motivo.
- **Responsable**: Assessment Agent + API.
- **Herramientas/integraciones**: agents service, API, DB.
- **Nivel actual**: En progreso.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher selecciona version activa.
- **Acciones permitidas**: crear versiones candidatas y resumir diferencias.
- **Acciones prohibidas**: reemplazar version aprobada silenciosamente.
- **Validaciones**: version monotona, integridad de referencias, notas no vacias.
- **Guardrails**: mantener trazabilidad del prompt/notas y output.
- **Idempotencia**: key por assessment+version_request.
- **Reintentos/fallos**: retry crea estado fallido separado, no version parcialmente activa.
- **Reversion**: volver a version previa aprobada.
- **Auditoria**: version, notas, run, aprobacion.
- **Datos sensibles**: no estudiantiles.
- **Tokens/costos**: similar AUT-01; controlar regeneraciones repetidas.
- **Metrica de exito**: draft accepted after regeneration, retry rate.
- **Evidencia**: historial de versiones.
- **Dependencias**: US-012.
- **Riesgos**: consumo duplicado por doble submit.
- **Release candidata**: primer release vertical.

### AUT-03 — Generacion de rubrica

- **Capacidad asociada**: C4.
- **Actor actual**: Teacher.
- **Problema operativo**: crear criterios evaluables alineados al assessment aprobado.
- **Evento disparador**: teacher solicita rubrica desde assessment draft.
- **Inputs**: assessment aprobado, objetivos, evidencia esperada, total de puntos, nivel.
- **Decisiones requeridas**: editar/aprobar/rechazar criterios y pesos.
- **Acciones**: generar criterios, niveles, pesos, notas y guia para grading.
- **Responsable**: Rubric Agent.
- **Herramientas/integraciones**: agents service, API, DB.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher aprueba rubrica antes de grading.
- **Acciones permitidas**: sugerir criterios y pesos.
- **Acciones prohibidas**: iniciar grading, aprobar su propia rubrica, cambiar assessment aprobado.
- **Validaciones**: suma de pesos, criterios observables, cobertura de objetivos.
- **Guardrails**: no criterios vagos ni topicos no solicitados.
- **Idempotencia**: key por assessment+rubric_request.
- **Reintentos/fallos**: retry con mismo input; output invalido queda como run fallido.
- **Reversion**: versiones de rubrica.
- **Auditoria**: rubric version, validation status, approval state.
- **Datos sensibles**: no estudiantiles.
- **Tokens/costos**: Flash-class/Groq-equivalente; costo por assessment.
- **Metrica de exito**: rubric approval rate.
- **Evidencia**: rubrica versionada + approval event.
- **Dependencias**: C3, US-020.
- **Riesgos**: rubricas utiles pero no suficientemente calibradas para grading.
- **Release candidata**: Open workflow MVP.

### AUT-04 — Validacion de rubrica

- **Capacidad asociada**: C4.
- **Actor actual**: Teacher.
- **Problema operativo**: detectar ambiguedad, pesos inconsistentes y gaps antes de calificar.
- **Evento disparador**: generacion o edicion de rubrica.
- **Inputs**: rubrica candidata, assessment, objetivos, total esperado.
- **Decisiones requeridas**: corregir, aceptar advertencias o regenerar.
- **Acciones**: ejecutar reglas deterministas y revision asistida.
- **Responsable**: Rubric Agent + validadores de API.
- **Herramientas/integraciones**: API, DB, agents service cuando aplique.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher decide si una advertencia bloquea.
- **Acciones permitidas**: flaggear inconsistencias.
- **Acciones prohibidas**: bloquear pedagogicamente sin opcion de revision docente salvo errores estructurales.
- **Validaciones**: pesos, duplicidad de criterios, criterios sin evidencia, language mismatch.
- **Guardrails**: distinguir error estructural de recomendacion pedagogica.
- **Idempotencia**: validacion pura por hash de rubrica.
- **Reintentos/fallos**: fallos de LLM no bloquean validaciones deterministas.
- **Reversion**: mantener version previa aprobada.
- **Auditoria**: flags y decision docente.
- **Datos sensibles**: no estudiantiles.
- **Tokens/costos**: usar determinismo primero; LLM solo para ambiguedad cualitativa.
- **Metrica de exito**: tasa de rubricas aprobadas sin ediciones mayores posteriores.
- **Evidencia**: validation notes.
- **Dependencias**: AUT-03.
- **Riesgos**: demasiados warnings pueden frenar adopcion.
- **Release candidata**: Open workflow MVP.

### AUT-05 — Intake y validacion de submissions open

- **Capacidad asociada**: C7.
- **Actor actual**: Teacher.
- **Problema operativo**: cargar respuestas reales con identidad minima y estados claros.
- **Evento disparador**: teacher pega texto, sube archivo o importa submissions.
- **Inputs**: assessment, student identifier, texto/archivo, mime/size, metadata opcional.
- **Decisiones requeridas**: aceptar warnings por archivo grande/duplicado o corregir input.
- **Acciones**: validar formato, persistir artifact/submission, asignar estado.
- **Responsable**: Web/API.
- **Herramientas/integraciones**: storage, DB, validadores de archivo.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Manual.
- **Nivel objetivo**: Automatizada.
- **Human-in-the-loop**: teacher corrige invalidos.
- **Acciones permitidas**: rechazar tipo no soportado, advertir duplicado/grande.
- **Acciones prohibidas**: ejecutar codigo de estudiantes o inferir identidad personal.
- **Validaciones**: extension permitida, size, non-empty, ownership.
- **Guardrails**: minimizacion PII.
- **Idempotencia**: hash de archivo/texto + assessment + student identifier.
- **Reintentos/fallos**: upload fallido no crea submission valida.
- **Reversion**: borrar/invalidar submission antes de analisis.
- **Auditoria**: status transitions.
- **Datos sensibles**: codigo de estudiantes y posible identificador.
- **Tokens/costos**: no LLM; costo storage.
- **Metrica de exito**: submission processing completion rate.
- **Evidencia**: submission received events.
- **Dependencias**: C4 para grading real.
- **Riesgos**: PII accidental en nombres/archivos.
- **Release candidata**: Open workflow MVP.

### AUT-06 — Sugerencia de calificacion open

- **Capacidad asociada**: C8.
- **Actor actual**: Teacher.
- **Problema operativo**: reducir tiempo de revision manteniendo criterio docente.
- **Evento disparador**: teacher inicia analisis sobre submissions validas con rubrica aprobada.
- **Inputs**: submission, rubrica aprobada, assessment, criterios, constraints.
- **Decisiones requeridas**: aprobar, editar, rechazar o marcar needs review.
- **Acciones**: generar score sugerido por criterio, evidencia, flags e incertidumbre.
- **Responsable**: Grading Agent.
- **Herramientas/integraciones**: agents service, API, DB.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher finaliza score.
- **Acciones permitidas**: sugerir puntuacion y razones.
- **Acciones prohibidas**: finalizar notas, enviar resultados, acusar plagio como hecho, ejecutar codigo inseguro.
- **Validaciones**: rubric approved, score ranges, criterios completos.
- **Guardrails**: separar evidencia de juicio; flags para integridad academica como senal, no acusacion.
- **Idempotencia**: key por submission+rubric_version+analysis_request.
- **Reintentos/fallos**: reintentos limitados; fallos visibles al teacher.
- **Reversion**: conservar sugerencia original aunque teacher edite.
- **Auditoria**: grade suggestion, approval/edit/rejection.
- **Datos sensibles**: submission y student identifier.
- **Tokens/costos**: Flash-Lite para volumen; fallback explicito.
- **Metrica de exito**: approval/edit/rejection rate, cost per graded submission.
- **Evidencia**: grading suggestion + agent log.
- **Dependencias**: C4, C7, AUT-16.
- **Riesgos**: calidad percibida baja; costo alto por submissions largas.
- **Release candidata**: Open workflow MVP.

### AUT-07 — Generacion de feedback individual

- **Capacidad asociada**: C9.
- **Actor actual**: Teacher.
- **Problema operativo**: transformar revision en feedback claro y accionable.
- **Evento disparador**: score aprobado/editado o teacher solicita feedback.
- **Inputs**: grade suggestion final/pending, rubrica, evidencia, tono.
- **Decisiones requeridas**: aprobar, editar, rechazar, regenerar.
- **Acciones**: generar feedback estructurado por estudiante.
- **Responsable**: Feedback Agent.
- **Herramientas/integraciones**: agents service, API, DB.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher aprueba antes de entrega/exportacion.
- **Acciones permitidas**: sugerir texto, fortalezas, mejoras y next step.
- **Acciones prohibidas**: enviar directamente, exponer prompts/logs, cambiar scores o inventar contexto personal.
- **Validaciones**: source grading approved/edited o warning visible.
- **Guardrails**: lenguaje constructivo, sin afirmaciones no sustentadas.
- **Idempotencia**: key por submission+grade_version+tone.
- **Reintentos/fallos**: retry con mismo input; fallos logueados.
- **Reversion**: conservar feedback original y final docente.
- **Auditoria**: feedback draft, approval state.
- **Datos sensibles**: student-level output.
- **Tokens/costos**: Flash-Lite por volumen.
- **Metrica de exito**: feedback approval rate, feedback outputs approved.
- **Evidencia**: approved feedback output.
- **Dependencias**: AUT-06.
- **Riesgos**: feedback generico o tono inadecuado.
- **Release candidata**: Open workflow MVP.

### AUT-08 — Deteccion de brechas

- **Capacidad asociada**: C10.
- **Actor actual**: Teacher.
- **Problema operativo**: identificar patrones de errores del cohorte sin revisar manualmente todo.
- **Evento disparador**: conjunto minimo de submissions calificadas.
- **Inputs**: resultados por criterio, feedback, objetivos, submission count.
- **Decisiones requeridas**: confirmar relevancia o descartar gap.
- **Acciones**: agrupar brechas, severidad, evidencia agregada.
- **Responsable**: Learning Gap Agent.
- **Herramientas/integraciones**: agents service, API, DB.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher confirma interpretacion.
- **Acciones permitidas**: sugerir gaps agregados.
- **Acciones prohibidas**: perfilar estudiantes, diagnosticar causas personales o hacer predicciones high-stakes.
- **Validaciones**: fuente aprobada o marcada pending, umbral de muestra.
- **Guardrails**: agregados primero; PII minima.
- **Idempotencia**: key por assessment+grading_snapshot.
- **Reintentos/fallos**: retry no duplica gaps activos.
- **Reversion**: teacher puede ocultar/editar gap.
- **Auditoria**: gap status y source snapshot.
- **Datos sensibles**: agregados de resultados.
- **Tokens/costos**: Flash/Flash-Lite segun volumen.
- **Metrica de exito**: learning gaps detected, teacher confirmation rate.
- **Evidencia**: gap summary.
- **Dependencias**: AUT-06.
- **Riesgos**: sobreinterpretacion.
- **Release candidata**: Open workflow MVP.

### AUT-09 — Sugerencia de recuperacion

- **Capacidad asociada**: C10.
- **Actor actual**: Teacher.
- **Problema operativo**: convertir gaps en acciones pedagogicas concretas.
- **Evento disparador**: teacher selecciona o confirma gap.
- **Inputs**: gap, rubrica, nivel, tiempo disponible, constraints.
- **Decisiones requeridas**: aprobar, editar o descartar actividad.
- **Acciones**: generar actividad corta, instrucciones y criterios de exito.
- **Responsable**: Recovery Agent.
- **Herramientas/integraciones**: agents service, API, DB.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher aprueba antes de asignar.
- **Acciones permitidas**: sugerir actividades enfocadas.
- **Acciones prohibidas**: asignar directamente, crear plan adaptativo completo, asumir causas personales.
- **Validaciones**: alineacion con gap y nivel.
- **Guardrails**: actividad corta y editable.
- **Idempotencia**: key por gap+constraints.
- **Reintentos/fallos**: retry logueado.
- **Reversion**: draft editable/descartable.
- **Auditoria**: recovery activity + approval.
- **Datos sensibles**: preferir agregados.
- **Tokens/costos**: Flash-class por calidad pedagogica.
- **Metrica de exito**: recovery activities approved.
- **Evidencia**: actividad aprobada.
- **Dependencias**: AUT-08.
- **Riesgos**: scope creep hacia LMS/adaptive learning.
- **Release candidata**: Open workflow MVP o release posterior si se recorta.

### AUT-10 — Reporte docente

- **Capacidad asociada**: C11.
- **Actor actual**: Teacher.
- **Problema operativo**: resumir ciclo, resultados, gaps, decisiones y evidencia.
- **Evento disparador**: teacher solicita reporte de assessment procesado.
- **Inputs**: assessment, rubric, submissions, grading decisions, feedback/gaps, cost summary.
- **Decisiones requeridas**: validar, editar, exportar o mantener privado.
- **Acciones**: generar reporte estructurado por audiencia.
- **Responsable**: Teacher Report Agent.
- **Herramientas/integraciones**: agents service, API, DB.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher valida antes de compartir.
- **Acciones permitidas**: resumir hechos e interpretacion separada.
- **Acciones prohibidas**: publicar sin validacion, exponer prompts o PII innecesaria, inventar metricas.
- **Validaciones**: source approval states visibles.
- **Guardrails**: marcar datos no aprobados; minimizar student-level detail.
- **Idempotencia**: key por assessment+report_mode+source_snapshot.
- **Reintentos/fallos**: retry no reemplaza reporte validado.
- **Reversion**: conservar reporte previo.
- **Auditoria**: report version y validation state.
- **Datos sensibles**: resultados y posiblemente datos estudiantiles.
- **Tokens/costos**: Flash-class; costo bajo por assessment.
- **Metrica de exito**: reports generated, report generation rate.
- **Evidencia**: teacher report + log.
- **Dependencias**: AUT-06/AUT-07/AUT-08.
- **Riesgos**: mezclar evidencia con interpretacion.
- **Release candidata**: cierre Open workflow MVP.

### AUT-11 — Generacion de preguntas closed

- **Capacidad asociada**: C6.
- **Actor actual**: Teacher.
- **Problema operativo**: crear banco inicial de preguntas objetivas alineadas al resultado de aprendizaje.
- **Evento disparador**: teacher solicita lote de preguntas.
- **Inputs**: subject, topic, learning outcome, tipos, dificultad, cantidad, constraints.
- **Decisiones requeridas**: revisar cada pregunta antes de bank active.
- **Acciones**: generar stem, alternativas, key, explicacion, tags.
- **Responsable**: Question Generation Agent.
- **Herramientas/integraciones**: agents service, API, DB.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher aprueba/edita/rechaza cada pregunta.
- **Acciones permitidas**: crear batch en `pending_review`.
- **Acciones prohibidas**: publicar al banco activo, autoaprobar o crear answer key final publicada.
- **Validaciones**: unica respuesta correcta, tipos soportados, tags requeridos.
- **Guardrails**: no trick questions, doble negacion, sesgos o supuestos no escritos.
- **Idempotencia**: key por generation_request.
- **Reintentos/fallos**: retry batch; no duplicar preguntas si misma key.
- **Reversion**: retirar batch pendiente.
- **Auditoria**: agent run ID ligado a preguntas.
- **Datos sensibles**: no requiere estudiantes.
- **Tokens/costos**: Flash-class; costo por batch.
- **Metrica de exito**: approved question rate.
- **Evidencia**: question batch + curation events.
- **Dependencias**: C5 tags P0.
- **Riesgos**: factualidad y ambiguedad.
- **Release candidata**: Closed workflow MVP.

### AUT-12 — Revision de distractores y ambiguedad

- **Capacidad asociada**: C6.
- **Actor actual**: Teacher.
- **Problema operativo**: reducir preguntas defectuosas antes de aprobarlas.
- **Evento disparador**: batch generado o pregunta editada.
- **Inputs**: pregunta, alternativas, key, explicacion, outcome, dificultad.
- **Decisiones requeridas**: aceptar flags, editar, rechazar o confirmar uso.
- **Acciones**: flaggear distractores debiles, sesgo, multiples respuestas validas, ambiguedad.
- **Responsable**: Distractor Quality Agent + Ambiguity Review Agent.
- **Herramientas/integraciones**: agents service, API, DB.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher resuelve flags; `critical` requiere confirmacion explicita.
- **Acciones permitidas**: recomendar ediciones y severidad.
- **Acciones prohibidas**: bloquear/aprobar autonomamente o garantizar perfeccion pedagogica.
- **Validaciones**: severidad, verdict, flags por opcion/pregunta.
- **Guardrails**: distinguir proofreading de validez pedagogica.
- **Idempotencia**: hash de pregunta+opciones+key.
- **Reintentos/fallos**: fallos visibles; pregunta queda pending.
- **Reversion**: teacher puede volver a version anterior.
- **Auditoria**: flags detectados y decision docente.
- **Datos sensibles**: no estudiantiles.
- **Tokens/costos**: Flash-Lite para distractores; Flash-class para ambiguedad.
- **Metrica de exito**: flags resolved before approval.
- **Evidencia**: curation audit trail.
- **Dependencias**: AUT-11.
- **Riesgos**: falsos positivos que ralenticen curacion.
- **Release candidata**: Closed workflow MVP.

### AUT-13 — Ensamblaje de assessment cerrado

- **Capacidad asociada**: C6.
- **Actor actual**: Teacher.
- **Problema operativo**: componer una evaluacion balanceada desde banco aprobado.
- **Evento disparador**: teacher define blueprint o solicita composicion.
- **Inputs**: banco aprobado, count, dificultad, outcomes, constraints, duracion.
- **Decisiones requeridas**: aprobar composicion y answer key antes de snapshot.
- **Acciones**: seleccionar preguntas, calcular cobertura y alertar gaps.
- **Responsable**: Assessment Assembly Agent + reglas deterministas.
- **Herramientas/integraciones**: API, DB, agents service opcional.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher aprueba composicion final.
- **Acciones permitidas**: proponer composicion y alternativas.
- **Acciones prohibidas**: crear snapshot/publicar, usar preguntas no aprobadas, alterar scoring final.
- **Validaciones**: solo approved/active, coverage, key completa.
- **Guardrails**: reglas deterministas primero; LLM solo si aporta interpretacion.
- **Idempotencia**: key por blueprint+bank_snapshot.
- **Reintentos/fallos**: si no hay cobertura suficiente, no forzar seleccion.
- **Reversion**: composicion editable antes de publish.
- **Auditoria**: blueprint, selected items, approval.
- **Datos sensibles**: no estudiantiles.
- **Tokens/costos**: bajo; potencialmente sin LLM si reglas alcanzan.
- **Metrica de exito**: coverage achieved, composition approval rate.
- **Evidencia**: approved composition.
- **Dependencias**: AUT-11/AUT-12.
- **Riesgos**: convertir optimizacion simple en IA innecesaria.
- **Release candidata**: Closed workflow MVP.

### AUT-14 — Snapshot y scoring cerrado

- **Capacidad asociada**: C6/C2.
- **Actor actual**: Teacher/System.
- **Problema operativo**: asegurar integridad de evaluacion publicada y scoring reproducible.
- **Evento disparador**: teacher publica assessment cerrado aprobado; estudiante envia intento.
- **Inputs**: composicion aprobada, answer key, scoring policy, responses.
- **Decisiones requeridas**: teacher confirma publish; revisa excepciones/anulaciones.
- **Acciones**: crear snapshot inmutable, evaluar respuestas, producir resultados.
- **Responsable**: API determinista.
- **Herramientas/integraciones**: DB, signed links, email.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Automatizada.
- **Nivel objetivo**: Automatizada.
- **Human-in-the-loop**: teacher aprueba publish y resuelve excepciones/anulacion.
- **Acciones permitidas**: scoring contra answer key congelada.
- **Acciones prohibidas**: IA corrigiendo respuestas objetivas, modificar snapshot publicado.
- **Validaciones**: token valido, attempt unico/reglas, respuestas completas, snapshot existente.
- **Guardrails**: determinismo, audit trail, no edicion estructural post-publish.
- **Idempotencia**: unique attempt per invitation policy.
- **Reintentos/fallos**: envio fallido no duplica intento; recalculo auditado por evento.
- **Reversion**: anulacion preserva resultados originales y recalculo nuevo.
- **Auditoria**: snapshot, attempt, grade result, annulment event.
- **Datos sensibles**: learner email/responses.
- **Tokens/costos**: sin LLM; costo DB/email.
- **Metrica de exito**: closed response completion, deterministic grading success.
- **Evidencia**: frozen snapshot + result ledger.
- **Dependencias**: AUT-13/AUT-19.
- **Riesgos**: errores de snapshot son dificiles de corregir tras publish.
- **Release candidata**: Closed workflow MVP.

### AUT-15 — Analitica de items

- **Capacidad asociada**: C12.
- **Actor actual**: Teacher.
- **Problema operativo**: entender dificultad, acierto y outcomes despues del assessment cerrado.
- **Evento disparador**: assessment cerrado con resultados calificados.
- **Inputs**: item responses, answer key, learning outcomes, cohort aggregates.
- **Decisiones requeridas**: aprobar interpretaciones/refuerzos; decidir anulacion si aplica.
- **Acciones**: calcular tasa de acierto determinista; interpretar dificultad y sugerir refuerzo.
- **Responsable**: Item Analytics Agent + agregadores API.
- **Herramientas/integraciones**: API, DB, agents service.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Asistida.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: teacher revisa antes de compartir o actuar.
- **Acciones permitidas**: interpretar agregados y sugerir refuerzo.
- **Acciones prohibidas**: modificar answer key, recalcular notas, invalidar preguntas sin accion docente.
- **Validaciones**: muestra minima, resultados completos, item IDs validos.
- **Guardrails**: separar calculo determinista de interpretacion IA.
- **Idempotencia**: key por assessment+result_snapshot.
- **Reintentos/fallos**: agregados deterministas quedan disponibles aunque falle IA.
- **Reversion**: reporte analitico versionado.
- **Auditoria**: report reviewed state.
- **Datos sensibles**: agregados; evitar detalle estudiante.
- **Tokens/costos**: Flash-class para narrativa, bajo volumen.
- **Metrica de exito**: item analytics report generated.
- **Evidencia**: item report + reinforcement suggestions.
- **Dependencias**: AUT-14.
- **Riesgos**: interpretar datos con muestra insuficiente.
- **Release candidata**: Closed workflow MVP.

### AUT-16 — Registro de ejecuciones de agentes

- **Capacidad asociada**: C13.
- **Actor actual**: System/Operator.
- **Problema operativo**: probar que la IA opera de forma auditable y medible.
- **Evento disparador**: cualquier agent run started/completed/failed.
- **Inputs**: request ID, agent, version, model/provider, assessment/submission/customer, timestamps, status, tokens, costo, flags, approval state.
- **Decisiones requeridas**: operator corrige metadata faltante o excluye evidencia privada de vistas publicas.
- **Acciones**: persistir logs, exponerlos a dashboard, detectar missing fields.
- **Responsable**: Ops Agent + API.
- **Herramientas/integraciones**: DB, agents service, web dashboard.
- **Nivel actual**: Parcial: modelo/migracion existe; alcance completo debe verificarse por workflow.
- **Nivel inicial recomendado**: Automatizada.
- **Nivel objetivo**: Automatizada.
- **Human-in-the-loop**: operator valida evidencia antes de uso publico/demo.
- **Acciones permitidas**: loguear, resumir, exportar, marcar faltantes.
- **Acciones prohibidas**: ocultar fallos, fabricar runs, exponer PII innecesaria.
- **Validaciones**: coverage 100%, estados validos, IDs correlacionados.
- **Guardrails**: esquema rico de `agents-overview.md`; reconciliar `05-evidence/agent-logs.md` pobre.
- **Idempotencia**: event_id/run_id unico.
- **Reintentos/fallos**: fallos tambien se registran; retry_count obligatorio.
- **Reversion**: logs no se eliminan; se corrigen con metadata/evento compensatorio.
- **Auditoria**: log es el artefacto auditado.
- **Datos sensibles**: input/output summary sin prompts completos ni PII excesiva.
- **Tokens/costos**: campos obligatorios cuando proveedor los entrega; estimado cuando no.
- **Metrica de exito**: agent log coverage 100%.
- **Evidencia**: AgentExecutionLog export/dashboard.
- **Dependencias**: D-04, D-06.
- **Riesgos**: campos inconsistentes entre docs/codigo.
- **Release candidata**: transversal desde primer release.

### AUT-17 — Estimacion de tokens, costo y uso

- **Capacidad asociada**: C13/C15.
- **Actor actual**: Operator.
- **Problema operativo**: demostrar unit economics y evitar sobrecostos.
- **Evento disparador**: agent run complete/fail, submission analyzed, payment/cost event.
- **Inputs**: tokens, model/provider, retries, customer, assessment, plan, graded submissions, revenue/cost events.
- **Decisiones requeridas**: operator revisa anomalías, related-party y evidencia de pago.
- **Acciones**: calcular costo estimado, costo por assessment/submission/customer, uso vs limite.
- **Responsable**: Ops Agent + API determinista.
- **Herramientas/integraciones**: DB, billing exports/manual ledger, dashboard.
- **Nivel actual**: Parcial.
- **Nivel inicial recomendado**: Automatizada.
- **Nivel objetivo**: Automatizada.
- **Human-in-the-loop**: operator valida montos y evidencia externa.
- **Acciones permitidas**: estimar, agregar, alertar.
- **Acciones prohibidas**: tratar creditos/free tier como costo cero sin reportarlo; mezclar related-party con arms-length.
- **Validaciones**: categoria de costo, moneda, periodo, customer, assessment.
- **Guardrails**: mantener cash cost, allocated tooling, credits y revenue separados.
- **Idempotencia**: ledger event_id unico.
- **Reintentos/fallos**: failed runs tienen costo/estado si consumieron tokens.
- **Reversion**: correccion contable via evento compensatorio.
- **Auditoria**: RevenueEvent/CostEvent/UsageEvent.
- **Datos sensibles**: payment evidence links privados.
- **Tokens/costos**: provider/model dynamic; agregar Groq como categoria o dimension.
- **Metrica de exito**: cost tracking coverage 90%+.
- **Evidencia**: cost dashboard, ledger.
- **Dependencias**: AUT-16, D-04, D-07.
- **Riesgos**: precios Gemini/Groq desactualizados.
- **Release candidata**: transversal desde primer release.

### AUT-18 — Dashboard de evidencia de negocio

- **Capacidad asociada**: C14.
- **Actor actual**: Operator.
- **Problema operativo**: preparar evidencia verificable de usuarios, runs, costos, revenue y demo.
- **Evento disparador**: operator abre dashboard o exporta evidencia.
- **Inputs**: agent logs, usage, revenue, costs, pilots, testimonials, approvals.
- **Decisiones requeridas**: validar, anonimizar, excluir privado, exportar.
- **Acciones**: mostrar metricas reales, missing evidence, links y resumen.
- **Responsable**: Ops Agent + Web.
- **Herramientas/integraciones**: API, DB, dashboard, export.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Supervisada.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: operator aprueba evidencia antes de uso publico.
- **Acciones permitidas**: resumir, alertar faltantes, preparar export.
- **Acciones prohibidas**: publicar externamente, fabricar traction, exponer datos privados.
- **Validaciones**: sin mocks, source links presentes, related-party visible.
- **Guardrails**: vistas internas vs publicas separadas.
- **Idempotencia**: exports versionados.
- **Reintentos/fallos**: export fallido no marca evidencia como publicada.
- **Reversion**: revocar/archivar export.
- **Auditoria**: evidence_dashboard_viewed/exported.
- **Datos sensibles**: revenue, customer proof, possible student aggregates.
- **Tokens/costos**: LLM solo para resumen opcional; determinismo primero.
- **Metrica de exito**: evidence completeness, dashboard viewed.
- **Evidencia**: dashboard/export.
- **Dependencias**: AUT-16/AUT-17.
- **Riesgos**: US-082 puede crecer demasiado.
- **Release candidata**: hackathon evidence release.

### AUT-19 — Links seguros y notificaciones a estudiantes

- **Capacidad asociada**: C2.
- **Actor actual**: Teacher/System.
- **Problema operativo**: permitir respuesta y resultados sin crear cuentas de estudiantes.
- **Evento disparador**: teacher publica assessment y envia invitaciones/resultados.
- **Inputs**: learner refs, emails, assessment snapshot, expiration policy, visibility.
- **Decisiones requeridas**: teacher decide enviar, revocar, publicar resultados.
- **Acciones**: crear token firmado/hasheado, enviar email, validar acceso, registrar uso.
- **Responsable**: API + email provider.
- **Herramientas/integraciones**: email, DB, web student portal.
- **Nivel actual**: No implementado.
- **Nivel inicial recomendado**: Automatizada.
- **Nivel objetivo**: Automatizada.
- **Human-in-the-loop**: teacher controla envio/revocacion/publicacion.
- **Acciones permitidas**: enviar/revocar tokens, marcar usado/expirado.
- **Acciones prohibidas**: crear student accounts, exponer otros resultados, reenviar sin permiso.
- **Validaciones**: ownership, token hash, expiration, learner+assessment uniqueness.
- **Guardrails**: aislamiento fuerte entre estudiantes.
- **Idempotencia**: unique invitation per learner+assessment unless resend creates version.
- **Reintentos/fallos**: email retry visible; token no duplicado.
- **Reversion**: revoke token; unpublish results si corresponde.
- **Auditoria**: invitation events, attempt events.
- **Datos sensibles**: learner email/result link.
- **Tokens/costos**: sin LLM; costo email.
- **Metrica de exito**: student response completion.
- **Evidencia**: invitation/attempt/result access logs.
- **Dependencias**: AUT-14 for closed publish.
- **Riesgos**: privacidad y deliverability.
- **Release candidata**: Closed workflow MVP.

### AUT-20 — Onboarding, pilotos y evidencia de pago

- **Capacidad asociada**: C1/C15.
- **Actor actual**: Operator.
- **Problema operativo**: convertir pilotos reales en evidencia de negocio auditada.
- **Evento disparador**: alta de piloto, pago, compromiso, entrevista o testimonial.
- **Inputs**: customer, plan, pilot status, related-party, revenue/payment evidence link, notes.
- **Decisiones requeridas**: operator marca related-party, valida evidencia y estado de piloto.
- **Acciones**: crear checklist, registrar revenue/cost/customer evidence, detectar faltantes.
- **Responsable**: Operator + Ops Agent.
- **Herramientas/integraciones**: API, dashboard, payment/manual evidence storage.
- **Nivel actual**: Parcial/manual.
- **Nivel inicial recomendado**: Manual.
- **Nivel objetivo**: Supervisada.
- **Human-in-the-loop**: operator conserva control total.
- **Acciones permitidas**: sugerir faltantes, resumir evidencia, preparar checklist.
- **Acciones prohibidas**: falsear revenue, mezclar related-party, publicar datos privados.
- **Validaciones**: evidencia link, monto, moneda, fecha, customer, related_party.
- **Guardrails**: separar commitments de revenue cobrado.
- **Idempotencia**: revenue_event_id unico.
- **Reintentos/fallos**: carga de evidencia fallida queda pendiente.
- **Reversion**: evento compensatorio o marca invalidated.
- **Auditoria**: revenue/customer evidence logs.
- **Datos sensibles**: datos comerciales y clientes.
- **Tokens/costos**: LLM opcional para resumen; no obligatorio.
- **Metrica de exito**: paid pilots, revenue by month, evidence completeness.
- **Evidencia**: revenue ledger, customer proof.
- **Dependencias**: US-PROPUESTA-01 y US-PROPUESTA-08.
- **Riesgos**: falta mecanismo de acceso Operator definido.
- **Release candidata**: hackathon evidence release.

### AUT-21 — Reintentos, idempotencia y recuperacion de fallos

- **Capacidad asociada**: transversal.
- **Actor actual**: System; Teacher/Operator ve acciones.
- **Problema operativo**: evitar costos duplicados, estados parciales y errores opacos.
- **Evento disparador**: timeout, proveedor no disponible, output invalido, doble submit.
- **Inputs**: request_id, idempotency key, workflow stage, retry policy, error code.
- **Decisiones requeridas**: user decide retry/cancel/edit input cuando falla una accion visible.
- **Acciones**: deduplicar, reintentar con limites, registrar fallo, mostrar recovery action.
- **Responsable**: API/Agents.
- **Herramientas/integraciones**: DB, queue si se introduce, agents service.
- **Nivel actual**: Parcial.
- **Nivel inicial recomendado**: Automatizada.
- **Nivel objetivo**: Automatizada.
- **Human-in-the-loop**: teacher/operator decide sobre fallos no recuperables.
- **Acciones permitidas**: retry acotado, fallback explicito, continuar manualmente.
- **Acciones prohibidas**: retry infinito, fallback premium silencioso, duplicar cargo por doble submit.
- **Validaciones**: unique keys, state transitions, retry_count.
- **Guardrails**: fail closed en student-facing y grading final.
- **Idempotencia**: obligatoria en generacion, grading, feedback, invitation, ledger.
- **Reintentos/fallos**: politica por stage; todo fallo logueado.
- **Reversion**: compensating events para ledger; versiones para outputs.
- **Auditoria**: error_code, retry_count, recovery action.
- **Datos sensibles**: summaries, no prompts completos.
- **Tokens/costos**: costo de retry visible.
- **Metrica de exito**: retry rate, failed run rate, duplicate run rate.
- **Evidencia**: failed/retried run logs.
- **Dependencias**: AUT-16.
- **Riesgos**: complejidad de plataforma si se intenta resolver con queue antes de necesitarla.
- **Release candidata**: transversal desde primer release.

## Matriz nivel actual versus objetivo

| Nivel actual | Procesos | Lectura |
|---|---|---|
| Manual | AUT-05, AUT-20 | Requieren UI/proceso humano antes de automatizar mas. |
| Asistida | AUT-01, AUT-02 | Ya hay o se espera IA con aprobacion docente directa. |
| Supervisada | Ninguno plenamente implementado | Es el objetivo de la mayoria de agentes cuando existan validaciones, logs y approval workflow. |
| Automatizada | AUT-14, AUT-16, AUT-17, AUT-19, AUT-21 como objetivo funcional | Deben ser deterministas, auditables e idempotentes. |
| Autonoma controlada | Ninguno | No recomendada para MVP en decisiones pedagogicas. |

## Human-in-the-loop

| Decision humana | Procesos afectados | Actor |
|---|---|---|
| Aprobar assessment draft | AUT-01, AUT-02 | Teacher |
| Aprobar rubrica | AUT-03, AUT-04 | Teacher |
| Confirmar/editar/rechazar score | AUT-06 | Teacher |
| Aprobar feedback | AUT-07 | Teacher |
| Confirmar gaps y recuperacion | AUT-08, AUT-09 | Teacher |
| Validar reporte | AUT-10 | Teacher |
| Aprobar pregunta/composicion/snapshot | AUT-11, AUT-12, AUT-13, AUT-14 | Teacher |
| Publicar resultados | AUT-14, AUT-19 | Teacher |
| Validar evidencia de negocio | AUT-18, AUT-20 | Operator |
| Resolver fallos no recuperables | AUT-21 | Teacher/Operator |

## Guardrails

- No final grading sin aprobacion docente en modo Open.
- No feedback student-facing sin aprobacion docente.
- No preguntas en banco activo sin curacion/aprobacion.
- No snapshot cerrado sin composicion y answer key aprobadas.
- No recalculo de closed assessment sin evento auditado.
- No fallback premium silencioso.
- No exposicion de prompts internos en reportes, feedback o evidencia publica.
- No PII estudiantil en evidencia de negocio salvo necesidad justificada y acceso privado.
- No logs perdidos: los fallos tambien cuentan.

## Observabilidad

El esquema base debe tomar la version rica de `docs/03-ai-agents/agents-overview.md` y la lista de campos de `master-plan-specification.md`. `docs/05-evidence/agent-logs.md` debe quedar como vista operacional simple o actualizarse para referenciar la fuente rica.

Campos minimos por run:

- `agent_run_id`, `request_id`, `tenant_id/customer_id`, `teacher_id`.
- `assessment_id`, `submission_id` cuando aplique.
- `agent_name`, `agent_version`, `workflow_stage`.
- `provider`, `model`, `model_policy`.
- `started_at`, `completed_at`, `status`, `error_code`, `retry_count`.
- `input_token_estimate`, `output_token_estimate`, `estimated_cost_usd`.
- `input_summary`, `output_summary`, `uncertainty_flags`.
- `requires_teacher_approval`, `teacher_approval_state`.

## Seguridad y privacidad

- Mantener ownership server-side en toda lectura/escritura.
- Usar identificadores minimos para estudiantes (`StudentSubmission.student_identifier`, `LearnerRef`).
- Hashear tokens de acceso; no almacenar links completos como secreto reutilizable.
- Separar vistas internas, judge-verifiable private evidence y evidencia publica.
- Permitir anulacion/revocacion de links e invalidacion de evidencia erronea via eventos compensatorios.
- Agregar historia propuesta de eliminacion/anonimizacion de datos de estudiante antes de pilotos reales con datos sensibles.

## Costos

La Fase 03 adopta el principio del `cost-model.md`: costo por agent run, assessment, graded submission, teacher y customer. La dimension de proveedor debe quedar explicita:

```text
Cost attribution = provider + model + model_policy + workflow_stage + customer + assessment + submission(optional)
```

Recomendaciones:

- Registrar Groq/Gemini como proveedor, no como categoria implicita.
- Separar costo estimado de costo facturado real.
- Mantener `cash_cost`, `covered_by_credit`, `allocated_tooling_cost` y `related_party` como dimensiones independientes.
- Calcular costo por assessment y por graded submission desde el inicio.
- Alertar ejecuciones con tokens/costo ausente.
- Hacer visible el costo de reintentos y fallback.

## Riesgos

| Riesgo | Impacto | Mitigacion |
|---|---|---|
| C13 se implementa tarde | Sin evidencia confiable para hackathon ni unit economics | Tratar AUT-16/AUT-17 como transversal desde el primer release de Fase 04 |
| Proveedor Groq no formalizado | Cost model y ADRs quedan inconsistentes | Resolver D-04 antes de cerrar estrategia de releases |
| `05-evidence/agent-logs.md` sigue pobre | Campos minimos divergentes entre producto, UX y evidencia | Actualizarlo o convertirlo en vista simplificada |
| Modo Closed P0 sobredimensiona el plan | Muchas capacidades no implementadas compiten por 4 semanas | Fase 04 debe separar demo viable de roadmap sin ocultar deuda |
| Operator sin acceso definido | C14/C15 y evidencia de negocio quedan sin actor operable | Crear/priorizar US-PROPUESTA-01 |
| Reintentos duplican costos | Doble cargo y metricas infladas | Idempotency key obligatoria en agent runs y ledger |
| Evidencia publica expone datos privados | Riesgo de privacidad y confianza | Vistas anonimizadas y export controlado por operator |
| Uso excesivo de LLM donde hay reglas | Mayor costo y menor reproducibilidad | Determinismo primero en scoring cerrado, assembly basico, ledgers y validaciones |

## Dependencias

| Dependencia | Afecta | Estado |
|---|---|---|
| D-01 entorno `demo` vs `beta` | AUT-16, AUT-17, AUT-18, criterios de evidencia | Pendiente |
| D-04 ADR Groq/default provider | AUT-01, AUT-02, AUT-16, AUT-17, costos | Pendiente |
| D-06 modelo unico de agent log | AUT-16, AUT-18 | Pendiente |
| D-07 pricing canonical | AUT-17, AUT-20 | Pendiente |
| US-PROPUESTA-01 Operator access | AUT-18, AUT-20 | Propuesta |
| US-PROPUESTA-02 Student deletion/anonymization | AUT-05, AUT-14, AUT-19 | Propuesta |
| US-PROPUESTA-03 Retry/failure recovery | AUT-21 | Propuesta |
| US-PROPUESTA-04 Idempotent generation requests | AUT-01, AUT-02, AUT-11, AUT-21 | Propuesta |
| US-PROPUESTA-05 Cost/token budget alerting | AUT-17 | Propuesta |
| US-PROPUESTA-06 Agent service health visibility | AUT-18, AUT-21 | Propuesta |
| US-PROPUESTA-07 Provider transparency/fallback notice | AUT-01, AUT-06, AUT-16, AUT-17 | Propuesta |
| US-PROPUESTA-08 Pilot onboarding checklist | AUT-20 | Propuesta |

## Metricas

| Grupo | Metricas prioritarias | Procesos fuente |
|---|---|---|
| Producto | assessments created, submissions received, closed attempts completed | AUT-01, AUT-05, AUT-14, AUT-19 |
| Workflow | creation completion, rubric approval, submission processing, report generation | AUT-01, AUT-03, AUT-05, AUT-10 |
| Confianza docente | approval/edit/rejection rate, uncertainty flag rate, override count | AUT-03, AUT-06, AUT-07, AUT-08 |
| Valor estudiante | feedback approved, turnaround, recovery approved, item reinforcement suggestions | AUT-07, AUT-09, AUT-15 |
| Operacion AI-native | agent runs logged, success rate, retry rate, model usage, token usage | AUT-16, AUT-21 |
| Unit economics | cost per run, assessment, graded submission, customer, gross margin | AUT-17 |
| Evidencia hackathon | users, paid pilots, revenue, related-party split, costs, agent logs | AUT-18, AUT-20 |

## Recomendaciones

1. **Priorizar AUT-16/AUT-17 temprano**: cada release funcional debe producir logs y costo desde el primer dia.
2. **Separar procesos IA de reglas deterministas**: especialmente AUT-14, AUT-17, AUT-19 y parte de AUT-13.
3. **No subir el nivel de autonomia pedagogica durante el MVP**: el objetivo realista es Supervisada, no Autonoma controlada.
4. **Usar releases candidatas por flujo vertical**: Open MVP, Closed MVP, Evidence/Hackathon, y Transversal Platform deben cruzarse en Fase 04.
5. **Formalizar proveedor/model policy antes de costo final**: D-04 debe resolverse para que cost model y dashboard no nazcan Gemini-only.
6. **Convertir fallos en experiencia de producto**: retry/cancel/edit input debe ser visible y medible, no solo log tecnico.
7. **Crear o enriquecer las US propuestas solo cuando Fase 04 las asigne**: no generar archivos nuevos de user stories desde esta fase.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-19 | Creacion inicial | Ejecucion de la Fase 03 del Master Plan Ejecutivo | Todo el documento | D-02, D-04, D-06 |
