# Release 02 - Open Graded Feedback Thin Slice

## 1. Identificacion

| Campo | Valor |
|---|---|
| Release | R02 |
| Nombre | Open Graded Feedback Thin Slice |
| Archivo | `docs/master-plan/releases/release-02-open-graded-feedback-thin-slice.md` |
| Estado | Documentada |
| Complejidad | L |
| Corte | MVP / primer ciclo Open con submissions reales |
| Fuente estrategica | `docs/master-plan/analysis/release-strategy.md` |

## 2. Prevalidacion

| Precondicion | Resultado |
|---|---|
| La release existe en `master-plan-executive.md` | OK |
| Sus US estan asignadas | OK: US-020, US-021, US-022, US-030, US-031, US-033, US-034, US-040, US-041, US-050, US-051 |
| No existen decisiones bloqueantes para definir esta release | OK con condicion: R01 debe cerrar D-04/D-06 o dejarlas como decisiones heredadas explicitas |
| La release no es XL | OK si se limita a happy path, errores principales y una o pocas submissions |
| Habilita un flujo vertical | OK: assessment aprobado -> rubrica -> submission -> grading suggestion -> feedback aprobado |
| Dependencias anteriores claras | OK: R01 es dependencia funcional y tecnica |

## 3. Objetivo ejecutivo

Probar el primer ciclo Open con una respuesta real de estudiante: desde un assessment draft ya aprobado, el docente genera y aprueba rubrica, carga una submission, obtiene una sugerencia de calificacion, genera feedback y aprueba el resultado final antes de cualquier entrega.

R02 no busca cerrar reportes de curso ni automatizar delivery masivo. Su funcion es validar la unidad economica y pedagogica central del producto:

```text
1 student answer analyzed = 1 graded submission
```

## 4. Problema

GradeOps AI puede demostrar creacion de assessment en R01, pero todavia no prueba que la IA ayude sobre evidencia real de estudiantes. Sin submissions, grading suggestions, feedback aprobado y conteo de uso por respuesta analizada, el producto no valida el valor que justifica el cobro ni la promesa principal para docentes.

## 5. Hipotesis

Si un docente puede revisar una o pocas respuestas reales con rubrica aprobada, sugerencia trazable de grading y feedback editable, entonces el producto puede demostrar confianza docente, utilidad pedagogica y costo por graded submission sin esperar cohort reports, bulk import ni delivery automatico.

## 6. Actor beneficiado

- **Teacher**: obtiene el primer apoyo real para revisar una submission y generar feedback util.
- **Operator**: puede medir costo y uso por graded submission.
- **Developer**: puede validar contratos entre rubrics, submissions, grading, feedback y logs.
- **Student**: se beneficia indirectamente solo cuando el docente aprueba el feedback.

## 7. Valor entregado

- Primera salida student-level revisada por docente.
- Rubrica estructurada como base de grading.
- Submission real persistida y trazable.
- Sugerencia de grading etiquetada como no final.
- Feedback student-facing sujeto a aprobacion.
- Conteo de consumo por respuesta analizada, no por carga de archivo.
- Reutilizacion del backbone de evidencia, costo e idempotencia de R01.

## 8. Nivel de automatizacion

| Proceso | Nivel inicial | Nivel objetivo en R02 |
|---|---|---|
| Generacion de rubrica | Asistida | Supervisada con validaciones y aprobacion docente |
| Validacion de rubrica | Asistida | Supervisada, con reglas deterministicas antes del LLM cuando aplique |
| Intake de submissions | Manual | Automatizada en validacion/persistencia, docente controla carga |
| Grading suggestion | Asistida | Supervisada, nunca final |
| Feedback draft | Asistida | Supervisada, nunca enviado sin aprobacion |
| Logs, costo e idempotencia | Automatizada | Automatizada para cada run y usage event |

## 9. Alcance incluido

- Generar rubrica editable desde un assessment draft aprobado.
- Validar pesos, cobertura de objetivos, niveles, ambiguedad y warnings.
- Aprobar y bloquear una version de rubrica para grading.
- Cargar submissions por texto pegado.
- Cargar submissions por archivo simple soportado.
- Mostrar estados basicos de submission.
- Ejecutar grading suggestion para una o pocas submissions.
- Mostrar score sugerido por criterio, evidencia, issues y uncertainty flags.
- Generar feedback draft desde grading suggestion o resultado revisado.
- Permitir aprobacion, edicion minima o rechazo del feedback.
- Registrar agent runs de rubric, grading y feedback.
- Registrar uso y costo por graded submission analizada.
- Aplicar idempotencia y retry controlado a los comandos facturables.

## 10. Exclusiones explicitas

- Bulk import de submissions como flujo productivo completo.
- Score edit/reject workflow completo con analytics.
- Learning gaps.
- Recovery activity.
- Teacher report.
- Tone adjustment avanzado o regeneracion iterativa de feedback.
- Export/delivery automatico a estudiantes.
- Closed assessment mode.
- Evidence dashboard completo.
- Billing self-serve o pricing final.
- Revision de plagio, ejecucion de codigo o analisis dinamico de programas.

## 11. Capacidades

| Capacidad | Rol en R02 |
|---|---|
| C4 Rubrica Open | Base obligatoria para grading. |
| C7 Intake de submissions Open | Entrada real del flujo. |
| C8 Calificacion asistida Open | Proceso principal de sugerencia. |
| C9 Feedback individual | Salida student-facing aprobada por docente. |
| C13 Evidencia de ejecucion de agentes | Logs obligatorios para rubric, grading y feedback. |
| C15 Facturacion y limites de plan | Parcial: usage/cost por graded submission. |

## 12. Flujo funcional

```text
Teacher signs in
  -> opens an approved assessment draft from R01
  -> requests rubric generation
  -> Rubric Agent returns criteria, weights, levels and warnings
  -> teacher edits and approves rubric
  -> approved rubric version is locked for grading
  -> teacher adds one or a few StudentSubmission records
  -> teacher starts analysis for selected submissions
  -> Grading Agent returns rubric-based suggestions and uncertainty flags
  -> teacher reviews the suggestion
  -> Feedback Agent drafts student-readable feedback
  -> teacher edits, rejects, or approves feedback
  -> usage and cost are recorded per analyzed submission
```

## 13. User stories incluidas

| US | Titulo | Estado en R02 |
|---|---|---|
| US-020 | Rubric Draft Generation | P0, debe enriquecerse antes de atomizar |
| US-021 | Rubric Validation | P0, debe enriquecerse antes de atomizar |
| US-022 | Rubric Approval | P0, debe enriquecerse antes de atomizar |
| US-030 | Manual Student Submission Creation | P0, debe enriquecerse antes de atomizar |
| US-031 | File Upload Student Submission | P0, debe enriquecerse antes de atomizar |
| US-033 | Submission Status | P0, debe enriquecerse antes de atomizar |
| US-034 | Graded Submission Usage Count | P0, debe enriquecerse antes de atomizar |
| US-040 | Rubric-Based Grading Suggestion | P0, debe enriquecerse antes de atomizar |
| US-041 | Uncertainty Flags | P0, debe enriquecerse antes de atomizar |
| US-050 | Individual Feedback Draft | P0, debe enriquecerse antes de atomizar |
| US-051 | Feedback Approval | P0, debe enriquecerse antes de atomizar |

## 14. Historias propuestas o modificadas

| ID | Nombre | Uso en R02 |
|---|---|---|
| US-PROPUESTA-02 | Privacy and PII Handling for Real Pilot Data | Requerida si se usan submissions reales de piloto. |
| US-PROPUESTA-03 | Agent Run Retry and Failure Recovery | Heredada de R01; debe cubrir rubric, grading y feedback. |
| US-PROPUESTA-04 | Idempotent Assessment/Question Generation Requests | Extender a rubric, grading, feedback y usage events. |

Estas propuestas no se crean como archivos de user story dentro de este documento. Deben enriquecerse o incorporarse cuando se cree la planning de implementacion de R02.

## 15. Consideraciones adicionales para las US

- Las US de R02 estan en nivel inventario/esqueleto. No deben atomizarse sin pasar por `/us-enrich`.
- R02 depende de un assessment draft aprobado por R01; si R01 no esta cerrada, la implementacion queda bloqueada.
- US-034 debe contar consumo cuando se ejecuta grading/feedback, no cuando el docente carga la submission.
- US-041 impide que uncertainty alta quede escondida; en R02 no hay bulk approval, pero la revision explicita sigue siendo obligatoria.
- US-031 debe limitar tipos y tamano de archivo para evitar convertir R02 en una release de ingestion compleja.

## 16. Reglas de negocio

- Grading no puede iniciar sin rubrica aprobada.
- Una rubrica aprobada queda bloqueada para grading.
- Cambios posteriores a una rubrica aprobada crean nueva version o update event.
- Toda salida de grading es sugerencia hasta aprobacion docente.
- Feedback no se entrega, exporta ni marca como final sin aprobacion docente.
- El conteo de graded submissions se consume al ejecutar analisis, no al crear submission.
- Una re-ejecucion puede consumir costo adicional solo si queda registrada como nuevo analysis request.
- High uncertainty requiere revision explicita.
- El sistema no acusa plagio ni ejecuta codigo de estudiantes.

## 17. Dependencias

| Dependencia | Estado | Accion en R02 |
|---|---|---|
| R01 Assessment Creation + Evidence Backbone | Previa obligatoria | Reutilizar identity, assessment draft, agent logs, costo e idempotencia |
| D-04 Provider/model policy | Debe venir cerrada o heredada | No redefinir proveedor dentro de R02 |
| D-06 AgentExecutionLog rico | Debe venir cerrado o heredado | Extender esquema a rubric, grading y feedback |
| Assessment aprobado | Requerido | Bloquear R02 si solo existe brief/draft no aprobado |
| File storage | Parcial/desconocido | Definir corte minimo para archivos simples |
| Privacy/PII para pilotos reales | Requerido si hay datos reales | Crear o incorporar US-PROPUESTA-02 |

## 18. Integraciones

- `web/` teacher workspace para rubric, submissions, review queue y feedback approval.
- `api/` Spring Boot para ownership, persistencia y comandos.
- `agents/` Rubric Agent, Grading Agent y Feedback Agent.
- `api` -> `agents` via `agentclient`.
- Storage de archivos si US-031 entra en corte real.
- PostgreSQL/Flyway para rubrics, submissions, suggestions, feedback, approvals, usage y logs.
- Provider LLM definido por la policy heredada de R01.

## 19. Arquitectura minima necesaria

- `web/` no llama modelos directamente.
- `api/` mantiene ownership y reglas de negocio.
- `agentclient` concentra invocaciones a Rubric, Grading y Feedback Agent.
- Rubric approval es una transicion persistida, no un estado solo de UI.
- Grading suggestion se persiste separado del resultado docente final.
- Feedback draft se persiste separado del feedback aprobado.
- Usage event se persiste como evento auditable, no como contador mutable sin trazabilidad.
- File ingestion limita extensiones, tamano y extraccion para el corte R02.

## 20. Datos y migraciones

Entidades/datos esperados:

- `Rubric`.
- `RubricVersion`.
- `RubricValidationWarning`.
- `StudentSubmission`.
- `SubmissionArtifact` o metadata equivalente de archivo/texto.
- `GradeSuggestion`.
- `CriterionGradeSuggestion`.
- `FeedbackDraft`.
- `FeedbackApproval`.
- `ApprovalEvent`.
- `UsageEvent` o `GradedSubmissionUsage`.
- `AgentExecutionLog`.

Campos minimos nuevos o extendidos:

- `rubric_version_id`.
- `submission_id`.
- `student_identifier`.
- `submission_status`.
- `artifact_hash`.
- `analysis_request_id`.
- `grade_suggestion_status`.
- `uncertainty_flags`.
- `feedback_approval_state`.
- `usage_event_id`.
- `estimated_cost_usd`.

## 21. Seguridad y privacidad

- Aplicar [`security-strategy.md`](../analysis/security-strategy.md) a rubric, submission, grading, feedback, file ingestion y rutas web.
- Mantener auth y ownership server-side en assessment, rubric, submission, grading y feedback.
- Aplicar permissions semanticas para rubric/grading/feedback; la UI solo representa capacidades devueltas por `api`.
- Usar identificador de estudiante minimo; evitar PII innecesaria.
- Validar extension, tamano, contenido no vacio y ownership de archivos.
- Rechazar archivos fuera de policy con errores seguros, sin procesar contenido ni despachar agentes.
- Exigir idempotencia/replay protection para comandos GenAI mutantes de rubric/grading/feedback.
- `agents` solo acepta provider/model/capability permitidos por `api` y presupuesto del request.
- No exponer prompts internos ni payloads completos sensibles en logs visibles.
- Logs deben guardar summaries y referencias, no submissions completas cuando no sea necesario.
- No ejecutar codigo subido por estudiantes.
- No acusar plagio ni inferir intencion del estudiante.
- Feedback student-facing debe evitar lenguaje desalentador, finalista o no sustentado.

## 22. Observabilidad y auditoria

- Aplicar [`observability-strategy.md`](../analysis/observability-strategy.md) a rubric, submission, grading, feedback y eventos de aprobacion docente.
- Crear spans para ingestion/validation, rubric generation, grading suggestion, feedback generation y approval.
- Registrar eventos canonicos de producto/IA para submission received, grading suggestion generated/edited/approved/rejected y feedback approved/published.
- Medir latencia, tokens, costo, retries, validation failures y calidad IA por aprobacion/edicion/rechazo docente.
- `rubric_generated`.
- `rubric_validated`.
- `rubric_approved`.
- `rubric_version_created`.
- `submission_received`.
- `submission_rejected_invalid`.
- `submission_analysis_started`.
- `grading_suggestion_generated`.
- `grading_suggestion_failed`.
- `feedback_generated`.
- `feedback_approved`.
- `feedback_edited`.
- `feedback_rejected`.
- `usage_event_recorded`.
- `agent_run_started`, `agent_run_completed`, `agent_run_failed`.
- Correlation/request ID entre `web`, `api` y `agents`.

## 23. Automatizaciones

| ID | Proceso | Uso en R02 |
|---|---|---|
| AUT-03 | Generacion de rubrica | Proceso principal |
| AUT-04 | Validacion de rubrica | Proceso principal |
| AUT-05 | Intake de submissions Open | Proceso principal deterministico |
| AUT-06 | Grading suggestion Open | Proceso principal |
| AUT-07 | Feedback draft | Proceso principal |
| AUT-16 | Registro de ejecuciones | Obligatorio |
| AUT-17 | Estimacion de tokens/costo | Obligatorio |
| AUT-21 | Reintentos/idempotencia/fallos | Obligatorio |

## Capacidades de IA y Agent Runtime

### Agentes involucrados

- Rubric Agent.
- Grading Agent.
- Feedback Agent.
- Assessment Agent solo como dependencia previa de R01, no como foco de R02.

### Capacidades funcionales habilitadas

- Generar y validar rubrica candidata.
- Proponer grading por criterio basado en evidencia.
- Redactar feedback fundamentado y aprobable por docente.

### Incrementos del runtime requeridos

- `AgentDefinition` versionada para Rubric, Grading y Feedback.
- `AgentRegistry` liviano para resolver agente/version sin ramas hardcoded por nombre.
- `AgentModelGateway` comun para reutilizar provider/model, salida estructurada, uso, costo y errores.
- Contratos de entrada/salida por agente, con validadores propios.
- Politica de autonomia por agente: Rubric `DRAFT_ONLY`, Grading/Feedback `HUMAN_APPROVAL_REQUIRED`.
- Handoff tipado minimo Grading -> Feedback mediante resultado/evidencia aprobable, sin multiagente general.

### Incremento API-Agent Orchestration requerido

- Aplicar [`api-agent-orchestration-strategy.md`](../analysis/api-agent-orchestration-strategy.md) a rubric/grading/feedback como consumidores funcionales reales.
- Mantener endpoints publicos orientados a intencion, por ejemplo rubric generations, grading runs y feedback generations; no exponer ejecucion generica de agentes a `web/`.
- Usar `api/` para validar assessment state, ownership, rubrica aprobada, submission scope, cost policy e idempotencia antes de invocar `agents/`.
- Reutilizar o extender `AiOperation`/`AgentRun`/`AgentAttempt` de R01 para cada agente; grading de una submission puede seguir sincrono si cumple el presupuesto temporal.
- Separar estado tecnico de run de estado academico de rubric/grade/feedback review.
- Someter todo endpoint/ruta nueva al gate Richardson REST: recurso claro, status codes, `Location` para operaciones, links `operation`/`result`/`retry` cuando aplique y contract tests API-Agents/API-Web.

### Herramientas requeridas

- `load_assessment_draft`.
- `load_approved_rubric`.
- `load_submission`.
- `validate_rubric_weights`.
- `validate_criterion_observability`.
- `match_evidence_to_criterion`.
- `calculate_proposed_score`.
- `validate_feedback_grounding`.

Estas herramientas se implementan como adapters determinísticos/autorizados; el modelo no calcula nota final ni persiste estados.

### Validadores determinísticos

- Pesos de rubrica suman el total esperado.
- Criterios son observables y tienen escala valida.
- Grading requiere rubrica aprobada.
- Score sugerido cae dentro de rango por criterio.
- Feedback cita evidencia existente y no introduce afirmaciones nuevas.

### Autonomía y controles humanos

- Rubrica: `DRAFT_ONLY`; teacher aprueba antes de grading.
- Grading: `HUMAN_APPROVAL_REQUIRED`; teacher finaliza nota.
- Feedback: `HUMAN_APPROVAL_REQUIRED`; teacher aprueba antes de entrega.

### Límites operacionales

- Max steps bajo por agente; no ejecutar codigo de estudiantes en R02 salvo que exista sandbox aislado.
- Sin herramientas `WRITE_CRITICAL` en `agents`.
- Sin aprobacion bulk silenciosa.

### Métricas y consumo

- Costo por rubric generation, grading suggestion y feedback draft.
- Tasa de approval/edit/rejection por agente.
- Salidas invalidas y uncertainty flags.
- Latencia y retries por provider/model.

### Evidencia de finalización

- Tests de registry/gateway con al menos dos agentes reales.
- Tests de validadores de rubrica, grading y feedback grounding.
- Smoke del flujo assessment -> rubric -> submission -> grading suggestion -> feedback approved.

### Deuda o capacidades diferidas

- Sandbox real para codigo se difiere si R02 no compila/ejecuta submissions.
- Tool loop generico con observaciones multiples puede diferirse a R04 si R02 se resuelve con llamadas estructuradas y validadores.

## 24. Trigger, inputs y outputs

| Proceso | Trigger | Inputs | Outputs |
|---|---|---|---|
| Rubric generation | Teacher clicks generate rubric | approved assessment draft, objectives, expected evidence, scale, language | rubric draft, validation notes, agent log |
| Rubric approval | Teacher approves rubric | rubric draft/version, validation state | locked rubric version, approval event |
| Submission intake | Teacher pastes/uploads answer | assessment ID, student identifier, text/file | StudentSubmission, artifact metadata, status `received` |
| Grading analysis | Teacher starts analysis | submission, approved rubric version, assessment, policy | GradeSuggestion, uncertainty flags, usage event, agent log |
| Feedback generation | Teacher requests feedback | submission, grading result/suggestion, evidence summary, tone/language | FeedbackDraft, warnings, agent log |
| Feedback approval | Teacher approves/edits/rejects | feedback draft, teacher action | final approval state, approval event |

## 25. Human in the loop

- Teacher aprueba rubrica antes de grading.
- Teacher revisa warnings de rubrica.
- Teacher decide que submissions se analizan.
- Teacher revisa score sugerido y uncertainty flags.
- Teacher aprueba, edita o rechaza feedback.
- Operator/developer inspecciona costo y logs, pero no modifica resultados pedagogicos.

## 26. Guardrails

- No grading sin rubrica aprobada.
- No final grades generados autonomamente por IA.
- No feedback enviado directamente por el agente.
- No prompts internos expuestos al docente o estudiante.
- No fallback premium silencioso.
- No costo duplicado por doble submit idempotente.
- No ejecucion de codigo de estudiantes.
- No claims de plagio.
- No ocultar uncertainty flags ni fallos de proveedor.

## 27. Idempotencia

R02 debe definir keys estables para:

- rubric generation: `assessment_id + rubric_request_id`;
- rubric validation: `rubric_version_id + validation_hash`;
- submission intake texto: `assessment_id + student_identifier + artifact_hash`;
- submission intake archivo: `assessment_id + student_identifier + file_hash`;
- grading analysis: `submission_id + rubric_version_id + analysis_request_id`;
- feedback generation: `submission_id + grade_version_or_suggestion_id + feedback_request_id`;
- usage event: `analysis_request_id + submission_id`;
- log event: `agent_run_id`.

Si el usuario repite una accion por refresh/doble click, el sistema debe devolver el resultado existente o bloquear el duplicado antes de invocar al agente o registrar consumo adicional.

## 28. Reintentos y fallos

| Falla | Comportamiento esperado |
|---|---|
| Rubrica invalida | Mostrar warnings, impedir aprobacion si el total/pesos son invalidos |
| Archivo no soportado | Rechazar con mensaje claro y sin crear analysis run |
| Submission vacia | Marcar invalid y permitir reemplazo |
| Timeout del agente | Log failed, mostrar retry controlado |
| Output malformado | Log validation failure, no crear resultado aprobado |
| High uncertainty | Mantener en review explicita |
| Cost estimate missing | Marcar costo como missing con razon, no ocultar el run |
| Doble submit | No ejecutar segundo run facturable |

## 29. Reversion

- Una rubrica aprobada no se edita destructivamente; se crea nueva version.
- Una submission puede invalidarse o eliminarse antes de analysis segun politica de datos.
- Una submission ya analizada conserva evidencia y usage event; correcciones usan eventos compensatorios.
- Una grading suggestion rechazada no desaparece del audit trail.
- Un feedback draft puede rechazarse o reemplazarse sin borrar el original.
- Un usage event erroneo se compensa con evento de ajuste, no con borrado silencioso.

## 30. Consumo y costos

R02 debe registrar consumo por analyzed student submission.

Campos:

- provider.
- model.
- model_policy.
- agent_name.
- rubric_version_id.
- submission_id.
- analysis_request_id.
- input tokens o estimate.
- output tokens o estimate.
- estimated cost USD.
- retry count.
- usage event type.
- teacher/customer attribution.

La carga de una submission no consume graded submission. El consumo ocurre cuando se ejecuta grading y/o feedback segun la politica de R02. Si ambos procesos se contabilizan por separado, debe quedar visible para evitar doble interpretacion comercial.

## 31. Criterios funcionales

- Teacher puede generar rubrica desde assessment aprobado.
- Teacher puede revisar warnings y aprobar rubrica.
- Teacher puede cargar al menos una submission por texto.
- Teacher puede cargar archivo simple soportado si US-031 entra al corte.
- Submission muestra estado claro.
- Teacher puede ejecutar grading suggestion sobre submission seleccionada.
- Teacher ve score por criterio, evidencia y uncertainty flags.
- Teacher puede generar feedback student-readable.
- Teacher puede aprobar, editar o rechazar feedback.
- Usage/cost queda asociado a la submission analizada.

## 32. Criterios tecnicos

- `api/` valida ownership, estados y transiciones.
- `api/` impide grading sin `rubric_version_id` aprobado.
- `agents` devuelve outputs estructurados segun contratos de Rubric, Grading y Feedback Agent.
- Persistencia separa suggestion, teacher state y final approval.
- Migraciones Flyway cubren rubrics, submissions, suggestions, feedback, approvals, usage y logs.
- Pantallas de rubric/submission/grading/feedback aplican UI Design/Data Semantics: pesos/scores numericos, archivos, estados reviewable, flags y feedback editable usan controles DS acordes a fuente de verdad.
- Pantallas de rubric/submission/grading/feedback declaran API I/O contract y sync/async por accion; grading/feedback async debe exponer completion model por operacion/polling/SSE/WebSocket/webhook/push segun contrato.
- Pantallas y outputs R02 aplican i18n: copy/safe errors/catalog labels localizados, feedback student-facing en `outputLocale`, y logs/telemetria/codes tecnicos en ingles.
- Tests cubren happy path y errores principales.
- Cross-service smoke test prueba `api` -> `agents` para grading y feedback real o provider controlado.

## 33. Criterios de calidad

- Rubrica suma pesos correctamente y cubre objetivos declarados.
- Submission intake tiene errores claros.
- Grading suggestion esta etiquetada como sugerencia, no como nota final.
- Evidencia de grading es concreta y vinculada a criterios.
- Feedback es conciso, constructivo, especifico y accionable.
- Feedback no contradice resultados revisados por docente.
- UI evita copy que prometa delivery automatico o revision completa de curso.

## 34. Criterios de seguridad

- Ownership denial mantiene patron consistente de la API.
- Permissions cubren rubric, submission, grading y feedback; la UI no puede habilitar acciones sin capability devuelta por API.
- Student identifiers se tratan como datos sensibles.
- Archivos se validan antes de persistir/procesar.
- Archivos rechazados no generan AgentRun ni costo.
- No se guardan secretos ni prompts completos en logs visibles.
- Feedback no contiene datos personales inventados.
- No hay ejecucion de codigo ni llamadas inseguras sobre submissions.
- Internal auth `api` -> `agents` sigue la decision vigente.
- Pruebas negativas cubren archivo invalido, ownership cruzado, rol sin permiso, replay/idempotency conflict y provider/model no permitido.

## 35. Criterios de observabilidad

- 100% de rubric/grading/feedback agent runs tienen log.
- El journey assessment -> rubric -> submission -> grading -> feedback puede reconstruirse por trace/correlation ID.
- Failed runs tambien se loguean.
- Usage event existe para cada submission analizada.
- Cost estimate existe o queda marcado como missing con razon.
- `uncertainty_flags` quedan persistidas y visibles.
- Correlation ID permite seguir request entre `web`, `api` y `agents`.
- Aprobaciones de rubrica y feedback quedan auditadas.
- Edicion/rechazo docente queda como evento de producto, no inferido desde logs tecnicos.
- Archivos rechazados generan evento/metric de rechazo sin AgentRun ni costo.

## 36. Criterios de despliegue

- Debe correr en entorno local integrado.
- Debe sostener smoke local con `api` y `agents`.
- Puede cerrarse sin `demo` GCP si R01 lo dejo como residual aceptado.
- Si se usa entorno `beta`, documentar URL, fecha, commit y evidencia de smoke.
- File upload debe tener storage/configuracion reproducible o quedar limitado a texto en el corte.

## 37. Criterios de negocio

- Primer costo por graded submission.
- Primer feedback aprobado asociado a una respuesta real.
- Primera evidencia de confianza docente sobre grading/feedback.
- Base para metricas de tasa de aprobacion, uncertainty y costo.
- No prometer reportes ni delivery masivo hasta R03/R06.

## 38. Definition of Done

- [ ] US-020/021/022/030/031/033/034/040/041/050/051 enriquecidas antes de atomizar.
- [ ] Flujo assessment aprobado -> rubrica aprobada -> submission -> grading suggestion -> feedback aprobado probado de extremo a extremo.
- [ ] Rubric approval bloquea grading hasta existir version aprobada.
- [ ] Submission intake valida texto/archivo, tamano, no vacio y ownership.
- [ ] Grading suggestion registra score por criterio, evidencia y uncertainty flags.
- [ ] Feedback draft queda pendiente hasta aprobacion docente.
- [ ] Usage/cost queda registrado por analyzed submission.
- [ ] Idempotencia y retry cubiertos por tests o evidencia manual reproducible.
- [ ] Logs de rubric/grading/feedback incluyen provider, model, status, timestamps, costo y errores.
- [ ] Gate de testing R02 cumplido: contratos rubric/grading/feedback, acceptance de artefactos, Compose Open full-chain y performance smoke segun impacto.
- [ ] Gate UI Design/Data Semantics cumplido para UI afectada: controles DS, campos numericos/restringidos, archivos, estados y flags no se implementan como texto libre por defecto.
- [ ] Gate API I/O + sync/async cumplido: datos de pantalla respaldados por `api/`, y acciones async con completion/progress/failure probado.
- [ ] Gate i18n cumplido: UI copy, safe errors, catalog labels y feedback generado respetan locale; logs/traces/metrics/event/error codes siguen en ingles.
- [ ] README/planning/release artifacts actualizados.

## 39. Validacion

Validaciones esperadas:

- Unit tests de `api` para estados de rubric, submission, grading, feedback y usage.
- Integration tests de `api` con persistencia real para el flujo completo.
- Tests de `agents` para Rubric, Grading y Feedback structured output.
- Tests de `web` para rubric approval, submission intake, review queue y feedback approval.
- Tests de `web` para controles semanticos: pesos/scores numericos, file upload/textarea segun submission, estados reviewable, uncertainty flags y feedback editable con valores validos/invalidos.
- Tests Web-API para lectura/escritura de pantallas y completion model si grading/feedback usa async.
- Tests i18n para translation keys, fallback, safe errors/catalog labels y `outputLocale` en Rubric/Grading/Feedback Agents cuando el output sea visible.
- Smoke local con `api` y `agents` reales o provider controlado.
- Prueba de idempotencia para doble submit en grading y feedback.
- Prueba de falla para archivo no soportado y agent timeout.
- Contract checks Web-API y API-Agents para rubric, grading y feedback, con fixtures actualizados en el mismo PR que cambie el contrato.
- Aceptacion aislada de `web`, `api` y `agents` con Firebase, Agents y GenAI simulados segun frontera afectada.
- Compose `full-chain` Open para assessment aprobado -> rubric -> submission -> grading -> feedback, con PostgreSQL real y GenAI simulado.
- Coverage/no-regression y Sonar quality gate sobre codigo nuevo en los artefactos modificados.
- JMeter smoke de rutas criticas de submission/grading/feedback cuando cambien endpoints, payloads, archivos o timeouts; carga completa queda fuera del PR.
- Artefactos JUnit, coverage, Playwright, contract diff, Compose logs y summary normalizado publicados.

## 40. Escenario Given/When/Then

```gherkin
Given a verified teacher is signed in
And the teacher has an approved open assessment draft
When the teacher generates and approves a rubric
And loads a student submission
And requests grading analysis
And requests feedback generation
And approves the feedback
Then the approved rubric version is locked for grading
And the submission is marked analyzed
And the grading suggestion is stored as non-final until teacher review
And the feedback is stored as approved only after teacher action
And an AgentExecutionLog records provider, model, status, timestamps, uncertainty flags, and estimated cost for each agent run
And usage is counted for the analyzed student submission
```

## 41. Metricas

- Rubric generation success rate.
- Rubric approval rate.
- Submission intake success rate.
- Grading suggestion success rate.
- Feedback approval rate.
- Feedback edit/reject rate.
- Uncertainty flag rate.
- Cost per graded submission.
- Agent log coverage.
- Usage event coverage.

## 42. Evidencias

- Screenshot o export de rubrica aprobada.
- Submission record con estado.
- Grading suggestion con criterios y evidencia.
- Feedback aprobado.
- AgentExecutionLog de Rubric Agent.
- AgentExecutionLog de Grading Agent.
- AgentExecutionLog de Feedback Agent.
- Usage/cost event por analyzed submission.
- Smoke test local o beta.
- Test output relevante.
- Enlace a planning y PRs cerrados.

## 43. Riesgos y mitigaciones

| Riesgo | Mitigacion |
|---|---|
| R01 no esta cerrada | Bloquear implementacion de R02 hasta tener assessment/log/costo/idempotencia base. |
| US esqueléticas se atomizan prematuramente | Ejecutar `/us-enrich` antes de crear tareas. |
| R02 crece a XL por bulk/report/export | Mantener una o pocas submissions y excluir reportes/delivery masivo. |
| PII en submissions reales | Incorporar US-PROPUESTA-02 y limitar logs a summaries. |
| File upload abre alcance tecnico excesivo | Limitar extensiones/tamano o dejar texto como corte minimo. |
| Grading quality baja confianza docente | Mostrar evidence snippets, uncertainty flags y teacher approval. |
| Doble conteo de uso | Definir UsageEvent idempotente y politica clara grading vs feedback. |
| Feedback aparenta ser final sin aprobacion | Mantener `requires_teacher_approval` y copy visible de pendiente. |

## 44. Resultado esperado

Al cerrar R02, GradeOps AI demuestra el nucleo de valor Open: un docente usa una rubrica aprobada para analizar una respuesta real, revisa una sugerencia de grading, aprueba feedback student-facing y el sistema registra costo/uso por graded submission. Esta release desbloquea R03 porque ya existen resultados y feedback aprobados que pueden agregarse en learning gaps, recovery y reportes.

## 45. Prompt ejecutable `/release-*`

Comandos inspeccionados en `.planning/scripts/release.mjs` y `.planning/TUTORIAL/reference.md`:

- `/release-init`
- `/release-new vX.Y.Z -- <purpose>`
- `/release-add vX.Y.Z NNN-slug [NNN-slug ...]`
- `/release-remove vX.Y.Z NNN-slug`
- `/release-status [vX.Y.Z] [--mark-planned|--mark-in-progress|--mark-blocked|--mark-released|--mark-cancelled]`

Prompt operativo para crear la release en el sistema `.releases/` del plugin, con placeholders explicitos porque el Master Plan no fija version semantica, target period, fecha estimada ni planning ID operativo para R02:

```text
Contexto:
Estamos ejecutando R02 del Master Plan: Open Graded Feedback Thin Slice.
Fuentes obligatorias:
- docs/master-plan/releases/release-02-open-graded-feedback-thin-slice.md
- docs/master-plan/master-plan-executive.md
- docs/master-plan/analysis/release-strategy.md
- docs/master-plan/analysis/automation-inventory.md
- docs/master-plan/analysis/user-story-inventory.md
- docs/02-product/workflows.md
- docs/02-product/user-stories/epic-03-rubric/
- docs/02-product/user-stories/epic-04-submissions/
- docs/02-product/user-stories/epic-05-grading/
- docs/02-product/user-stories/epic-06-feedback/
- docs/03-ai-agents/rubric-agent.md
- docs/03-ai-agents/grading-agent.md
- docs/03-ai-agents/feedback-agent.md

Precondiciones:
- R01 debe estar cerrada o existir evidencia aceptada de assessment/log/costo/idempotencia base.
- No inventar planning IDs. Si no existe planning operativa para R02, crearla primero con el flujo de planning vigente.
- Ejecutar /us-enrich sobre las US de R02 antes de atomizar tareas.

Objetivo:
Crear y gestionar la release operativa R02: assessment aprobado -> rubrica aprobada -> submission -> grading suggestion -> feedback aprobado -> usage/cost por analyzed submission.

Comandos:
1. Si .releases/ no existe:
   /release-init
2. Crear la release:
   /release-new <VERSION> -- Open Graded Feedback Thin Slice --target <YYYY-QN-MN-WN> --date <YYYY-MM-DD>
3. Agregar planning(s) existentes de R02, despues de verificar que existen:
   /release-add <VERSION> <PLANNING_ID_R02>
4. Revisar estado:
   /release-status <VERSION>

Alcance:
- US-020, US-021, US-022.
- US-030, US-031, US-033, US-034.
- US-040, US-041.
- US-050, US-051.
- AUT-03, AUT-04, AUT-05, AUT-06, AUT-07, AUT-16, AUT-17, AUT-21.
- US-PROPUESTA-02 si se usan datos reales de piloto.
- Extensiones minimas de US-PROPUESTA-03 y US-PROPUESTA-04.

Exclusiones:
- Bulk import, learning gaps, recovery, teacher report, export/delivery automatico, Closed mode, dashboard completo y billing self-serve.

Arquitectura:
- web -> api -> agents.
- agentclient es el unico caller de agents desde api.
- Rubric approval y Feedback approval son transiciones persistidas.
- Grading suggestion no es nota final.
- UsageEvent debe ser idempotente.

Seguridad:
- Mantener auth y ownership server-side.
- Minimizar PII de student_identifier y submissions.
- Validar archivos antes de procesarlos.
- No ejecutar codigo ni acusar plagio.
- No exponer prompts internos ni payloads completos en logs visibles.

Automatizacion:
- Rubric, grading y feedback son procesos asistidos/supervisados.
- Logs, costo, retry e idempotencia automatizados.
- Feedback no se entrega sin aprobacion docente.

Trazabilidad:
- Si D-04 o D-06 no quedaron cerradas en R01, registrar el residual antes de implementar R02.
- Si aparece contradiccion de docs vs codigo, registrarla y no resolverla silenciosamente.
- No implementar R03 dentro de esta release.

Metricas:
- cost per graded submission.
- grading suggestion success rate.
- feedback approval rate.
- uncertainty flag rate.
- agent log coverage.
- usage event coverage.

Criterios:
- El flujo es demostrable de extremo a extremo.
- Tests y smoke real api -> agents documentados.
- README/planning/release status actualizados.
```

## 46. Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Incorporacion de i18n por release funcional | Alinear R02 con locale en UI, safe errors, catalogos y feedback student-facing sin localizar telemetria | UI/API/Agents, DoD operativo, validacion | D-I18N-01..D-I18N-10 |
| 2026-07-21 | Incorporacion de UI Design/Data Semantics | Alinear R02 con controles DS y semantica de datos para rubric/submission/grading/feedback | UI, DoD operativo, validacion | D-UI-01..D-UI-08 |
| 2026-07-21 | Incorporacion de API I/O y sync/async contract | Alinear R02 con datos de pantalla respaldados por `api/` y completion model para grading/feedback async | UI/API, DoD operativo, validacion | D-UI-01..D-UI-08, D-API-01..D-API-10 |
| 2026-07-21 | Incorporacion de Testing & Quality Gates | Alinear R02 con contratos rubric/grading/feedback, acceptance por artefacto, Compose Open y performance smoke de rutas criticas | Testing, CI/testkit, DoD operativo | D-TEST-01..D-TEST-09 |
| 2026-07-21 | Incorporacion de Observability & Telemetry | Alinear R02 con telemetria de graded submission, calidad IA, aprobacion docente y costo por resultado | Observabilidad, DoD operativo | D-OBS-01..D-OBS-08 |
| 2026-07-21 | Incorporacion de Security & Authorization | Alinear R02 con permissions, ingestion segura, minimizacion PII, provider/model allowlist y pruebas negativas | Seguridad, DoD operativo | D-SEC-01..D-SEC-08 |
| 2026-07-20 | Incorporacion de capacidades de Agent Runtime | Declarar el segundo consumidor real del runtime y sus limites de autonomia | Runtime, agentes Rubric/Grading/Feedback | D-04, D-06 |
| 2026-07-20 | Incorporacion de API-Agent Orchestration | Asegurar que rubric/grading/feedback usen `api/` como orquestador funcional y no como proxy de agentes | API, agents, web routes, DoD operativo | D-API-01..D-API-10 |
| 2026-07-20 | Creacion inicial | Ejecucion de Fase 05 para R02 | Todo el documento | D-04, D-06 |
