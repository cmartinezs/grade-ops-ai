# Release 04 - Closed Question Bank to Snapshot

## 1. Identificacion

| Campo | Valor |
|---|---|
| Release | R04 |
| Nombre | Closed Question Bank to Snapshot |
| Archivo | `docs/master-plan/releases/release-04-closed-question-bank-snapshot.md` |
| Estado | Documentada |
| Complejidad | L |
| Corte | MVP / primer thin slice Closed autoral |
| Fuente estrategica | `docs/master-plan/analysis/release-strategy.md` |

## 2. Prevalidacion

| Precondicion | Resultado |
|---|---|
| La release existe en `master-plan-executive.md` | OK |
| Sus US estan asignadas | OK: US-100, US-101, US-110, US-111, US-112, US-113, US-114 |
| No existen decisiones bloqueantes para definir esta release | OK: D-02 ya resolvio Closed como P0; R01 debe aportar auth/log/costo/idempotencia base |
| La release no es XL | OK si se excluyen student response, scoring, results, item analytics y anulacion/recalculo |
| Habilita un flujo vertical | OK: metadata -> question batch -> curation -> bank -> composition -> frozen snapshot |
| Dependencias anteriores claras | OK: R01 es dependencia transversal; R05 depende de R04 |

## 3. Objetivo ejecutivo

Entregar el primer flujo Closed autoral: un docente define scope curricular, genera preguntas objetivas con IA, resuelve flags de calidad, aprueba preguntas al banco, compone una evaluacion cerrada y publica un snapshot inmutable con preguntas, opciones, answer key, scoring policy y grade scale congelados.

R04 no procesa respuestas de estudiantes. Su funcion es probar que GradeOps AI puede crear y congelar un assessment cerrado auditable sin contaminar el flujo Open ni depender todavia de invitations, attempts o analytics.

## 4. Problema

Closed assessment es parte P0 del validacion MVP, pero no tiene implementacion real. Si se mezcla con Open o con respuesta de estudiantes desde el primer corte, el alcance crece demasiado. R04 separa el problema autoral: banco curado y snapshot confiable antes de ejecutar el ciclo estudiantil.

## 5. Hipotesis

Si el docente puede generar, curar y publicar un snapshot Closed con answer key congelada, entonces el producto demuestra una segunda modalidad AI-native de evaluacion objetiva sin usar IA para scoring ni exponer todavia complejidad de links, intentos, resultados o recalculo.

## 6. Actor beneficiado

- **Teacher**: crea preguntas objetivas, las cura y compone una evaluacion cerrada publicable.
- **Operator**: obtiene evidencia de que Closed existe como capacidad MVP separada de Open.
- **Developer**: obtiene contratos de bank, curation, composition y snapshot antes de R05.

## 7. Valor entregado

- Primer banco de preguntas cerrado con metadata curricular.
- Preguntas TF/SC/MC generadas por IA en estado pending review.
- Quality flags de distractores y ambiguedad visibles para curacion docente.
- Curation audit trail por pregunta.
- Bank activo buscable y filtrable.
- Assessment cerrado compuesto desde preguntas aprobadas.
- Snapshot inmutable listo para student response en R05.
- Evidencia de agent runs, costo, flags y aprobaciones.

## 8. Nivel de automatizacion

| Proceso | Nivel inicial | Nivel objetivo en R04 |
|---|---|---|
| Tagging curricular | Manual/asistida | Supervisada con sugerencias del agente y confirmacion docente |
| Generacion de preguntas | Asistida | Supervisada; output siempre pending review |
| Revision de distractores | Asistida | Supervisada; flags no bloquean salvo confirmacion explicita para critical |
| Revision de ambiguedad | Asistida | Supervisada; teacher resuelve flags |
| Ensamblaje Closed | Asistida/deterministica | Supervisada con reglas deterministicas primero |
| Snapshot publish | Deterministica | Automatizada despues de aprobacion docente explicita |
| Logs, costo e idempotencia | Automatizada | Automatizada para runs y snapshot events |

## 9. Alcance incluido

- Crear y persistir metadata curricular minima: `subject_area`, topic tags y `learning_outcome`.
- Impedir que una pregunta pase a active sin `subject_area` y `learning_outcome`.
- Filtrar banco por subject, topic, learning outcome, difficulty, type y status.
- Generar batch de preguntas objetivas TF/SC/MC desde scope docente.
- Incluir stem, options, answer key, explanation, difficulty y tags sugeridos.
- Guardar output generado como `pending_review` o equivalente.
- Ejecutar Distractor Quality Agent y Ambiguity Review Agent para flags de curacion.
- Permitir aprobar, editar, regenerar o rechazar cada pregunta.
- Auditar todas las acciones de curacion.
- Mover preguntas aprobadas al bank active.
- Mostrar bank activo por defecto, ocultando rejected/pending salvo filtro explicito.
- Permitir retiro de preguntas para futuros assessments.
- Componer assessment cerrado desde bank approved/active.
- Validar answer key completa, scoring policy y grade scale antes de publish.
- Crear snapshot inmutable de preguntas, opciones, answer key, scoring policy y grade scale.
- Registrar logs/costo de Question Generation, Distractor Quality, Ambiguity Review y Assessment Assembly.

## 10. Exclusiones explicitas

- Student invitations y secure links.
- Student response intake.
- Deterministic grading execution sobre attempts.
- Result access para estudiantes.
- Item analytics.
- Annul question and recalculate.
- Importar bancos externos o IMS QTI.
- Curriculum structure generation con IA (US-102).
- Coverage validation P1 completa (US-103), salvo warning minimo si el assembly ya lo calcula.
- AI scoring para closed assessments.
- Proctoring, OCR/OMR o student accounts.

## 11. Capacidades

| Capacidad | Rol en R04 |
|---|---|
| C5 Metadata curricular | Base para scope, filtros y activation rules del bank. |
| C6 Closed authoring workflow | Proceso principal de pregunta, banco, composicion y snapshot. |
| C13 Evidencia de ejecucion de agentes | Logs obligatorios para agentes de authoring Closed. |

## 12. Flujo funcional

```text
Teacher signs in
  -> defines subject, topic, learning outcome, difficulty, count and question type
  -> Question Generation Agent creates objective question batch
  -> questions enter pending_review state
  -> Distractor Quality Agent and Ambiguity Review Agent flag issues
  -> teacher approves, edits, regenerates, or rejects each question
  -> approved questions become active in the bank with required metadata
  -> teacher filters bank and defines closed assessment blueprint
  -> Assessment Assembly Agent or deterministic rules propose composition
  -> teacher swaps/adds questions and approves final composition
  -> system validates answer key, scoring policy and grade scale
  -> teacher publishes
  -> API creates immutable snapshot
```

## 13. User stories incluidas

| US | Titulo | Estado en R04 |
|---|---|---|
| US-100 | Tag Question With Subject and Learning Outcome | P0, debe enriquecerse antes de atomizar |
| US-101 | Filter Question Bank by Curriculum Metadata | P0, debe enriquecerse antes de atomizar |
| US-110 | Generate Question Batch With AI | P0, debe enriquecerse antes de atomizar |
| US-111 | Review AI-Generated Questions | P0, debe enriquecerse antes de atomizar |
| US-112 | Question Bank | P0, debe enriquecerse antes de atomizar |
| US-113 | Compose Closed Assessment From Bank | P0, debe enriquecerse antes de atomizar |
| US-114 | Publish Closed Assessment and Freeze Snapshot | P0, debe enriquecerse antes de atomizar |

## 14. Historias propuestas o modificadas

| ID | Nombre | Uso en R04 |
|---|---|---|
| US-PROPUESTA-04 | Idempotent Assessment/Question Generation Requests | Requerida para generation batches y evitar costo duplicado. |
| US-PROPUESTA-07 | AI Provider Transparency and Fallback Notice | Requerida si no quedo generalizada en R01 para mostrar provider/model en logs. |

No se crean archivos nuevos de US en este documento. Si R01 no generalizo idempotencia y provider transparency, R04 debe incorporarlas como tareas tecnicas antes de cerrar question generation.

## 15. Consideraciones adicionales para las US

- Todas las US de R04 estan en estado NOT READY/esqueletico; deben pasar por `/us-enrich`.
- US-100/101 son prerequisito de US-110/112/113 porque el bank depende de metadata.
- US-111 debe trasladar al cuerpo de la historia los agentes Distractor Quality y Ambiguity Review, actualmente reforzados por README/agents.
- US-114 solo cubre publish/freeze snapshot; response/scoring/result quedan para R05.
- US-115 queda fuera aunque el README de Epic 12 la mencione como P1.
- US-102/103 quedan fuera del corte porque no son necesarias para el primer snapshot.

## 16. Reglas de negocio

- Una pregunta generada por IA entra en pending review, nunca en active.
- Una pregunta sin `subject_area` y `learning_outcome` no puede pasar a active.
- Teacher debe aprobar, editar/aprobar o rechazar cada pregunta.
- Critical ambiguity requiere confirmacion explicita si teacher decide aprobar.
- Rejected y pending no aparecen en bank por defecto.
- Retired questions no pueden usarse en nuevos assessments.
- Assessment composition solo puede incluir preguntas approved/active.
- Snapshot publish requiere composition aprobada, answer key completa, scoring policy y grade scale.
- Un snapshot publicado no se edita estructuralmente.
- Closed scoring es deterministico contra snapshot; IA no corrige respuestas cerradas.

## 17. Dependencias

| Dependencia | Estado | Accion en R04 |
|---|---|---|
| R01 Assessment Creation + Evidence Backbone | Previa transversal | Reutilizar auth, logs, costo, retry e idempotencia |
| D-02 Closed = P0 | Resuelta | Mantener R04/R05 dentro del MVP sin mover refinamientos P1 |
| Epic 01 Teacher Onboarding | Requerida | Teacher workspace autenticado |
| C5 metadata curricular | Requerida | Activar antes o junto con question bank |
| AgentExecutionLog rico | Heredado | Extender a agentes Closed |
| Provider/model policy | Heredado | No redefinir proveedor en R04 |

## 18. Integraciones

- `web/` teacher workspace para question generation, curation, bank, composition y publish.
- `api/` Spring Boot para ownership, estados, persistencia y snapshot.
- `agents/` Question Generation Agent.
- `agents/` Distractor Quality Agent.
- `agents/` Ambiguity Review Agent.
- `agents/` Assessment Assembly Agent si se usa asistencia IA.
- `api` -> `agents` via `agentclient`.
- PostgreSQL/Flyway para questions, tags, curation, bank, composition, snapshot y logs.

## 19. Arquitectura minima necesaria

- `web/` no llama modelos directamente.
- `api/` conserva ownership, estados y reglas deterministicas.
- `agentclient` concentra invocaciones a agentes Closed.
- Question generation produce batch versionado o ligado a `generation_request_id`.
- Curation actions se persisten como eventos auditables.
- Bank query usa metadata estructurada, no busqueda ad hoc por texto.
- Assembly usa reglas deterministicas primero y agente solo si aporta seleccion/coverage.
- Snapshot service copia datos necesarios; no referencia datos mutables del bank para scoring futuro.

## 20. Datos y migraciones

Entidades/datos esperados:

- `QuestionItem`.
- `QuestionOption`.
- `QuestionMetadata`.
- `QuestionGenerationBatch`.
- `QuestionQualityFlag`.
- `DistractorReview`.
- `AmbiguityReview`.
- `QuestionCurationEvent`.
- `QuestionBankStatus`.
- `ClosedAssessment`.
- `ClosedAssessmentBlueprint`.
- `ClosedAssessmentComposition`.
- `ClosedAssessmentSnapshot`.
- `SnapshotQuestion`.
- `SnapshotOption`.
- `SnapshotAnswerKey`.
- `ScoringPolicy`.
- `GradeScale`.
- `AgentExecutionLog`.

Campos minimos nuevos o extendidos:

- `subject_area`.
- `topic_tags`.
- `learning_outcome`.
- `question_type`.
- `difficulty`.
- `question_status`.
- `generation_request_id`.
- `agent_run_id`.
- `quality_flags`.
- `ambiguity_severity`.
- `curation_action`.
- `approved_by_teacher_id`.
- `composition_status`.
- `snapshot_id`.
- `snapshot_version`.
- `published_at`.
- `snapshot_hash`.

## 21. Seguridad y privacidad

- Aplicar [`security-strategy.md`](../analysis/security-strategy.md) a question bank, question generation/review, composition y snapshot.
- Mantener auth y ownership server-side.
- Question bank, composition y snapshot requieren permissions semanticas y ownership explicito.
- R04 no requiere datos estudiantiles.
- No exponer prompts internos ni API keys.
- `agents` debe ejecutar question generation/review con provider/model/capability allowlisted y output schema cerrado.
- Logs deben guardar summaries, provider/model/costo y flags, no prompts completos.
- Rejected questions pueden conservarse para auditoria, pero no deben aparecer por defecto.
- Snapshot publicado debe ser inmutable por permisos y modelo de datos.
- Teacher solo ve/edita bank y assessments propios o compartidos explicitamente.

## 22. Observabilidad y auditoria

- Aplicar [`observability-strategy.md`](../analysis/observability-strategy.md) a question generation, quality review, ambiguity review, curation, composition y snapshot.
- Crear spans por batch, validacion deterministica, provider call, output validation, curation action y snapshot creation.
- Medir output validation failures, rejected questions, edit rate, cost per batch/question y latencia por provider/model family.
- Registrar snapshot hash/version como evidencia durable, no como label de metrica.
- `question_batch_generation_started`.
- `question_batch_generated`.
- `question_batch_generation_failed`.
- `question_quality_review_completed`.
- `question_ambiguity_review_completed`.
- `question_approved`.
- `question_edited`.
- `question_regenerated`.
- `question_rejected`.
- `question_retired`.
- `bank_filter_applied` si se usa para evidence/debug.
- `closed_assessment_composition_generated`.
- `closed_assessment_composition_approved`.
- `closed_assessment_snapshot_created`.
- `agent_run_started`, `agent_run_completed`, `agent_run_failed`.
- Correlation/request ID entre `web`, `api` y `agents`.

## 23. Automatizaciones

| ID | Proceso | Uso en R04 |
|---|---|---|
| AUT-11 | Generacion de preguntas closed | Proceso principal |
| AUT-12 | Revision de distractores y ambiguedad | Proceso principal |
| AUT-13 | Ensamblaje de assessment cerrado | Proceso principal |
| AUT-14 | Snapshot y scoring cerrado | Parcial: solo snapshot/freeze, no scoring ni attempts |
| AUT-16 | Registro de ejecuciones | Obligatorio |
| AUT-17 | Estimacion de tokens/costo | Obligatorio |
| AUT-21 | Reintentos/idempotencia/fallos | Obligatorio |

## Capacidades de IA y Agent Runtime

### Agentes involucrados

- Question Generation Agent.
- Distractor Quality Agent.
- Ambiguity Review Agent.
- Assessment Assembly Agent.

### Capacidades funcionales habilitadas

- Generar preguntas candidatas.
- Revisar calidad/ambiguedad de items.
- Proponer composicion cerrada desde banco aprobado.
- Mantener snapshot y answer key como resultado deterministico de API, no del modelo.

### Incrementos del runtime requeridos

- `AgentAction` tipada para `UseTool`, `Finish` y `Block`.
- `AgentLoop` acotado para iterar con herramientas read-only/compute-only.
- `ToolRegistry` y `ToolExecutor` para banco, similitud, validaciones y cobertura.
- `PolicyEngine` basico que autorice herramientas por agente/version/autonomia.
- Budget manager con max steps, model calls, tool calls, tokens, costo y timeout.
- Estados `BLOCKED`/`NEEDS_INPUT` cuando el banco no alcanza o faltan tags/outcomes.

### Incremento API-Agent Orchestration requerido

- Aplicar [`api-agent-orchestration-strategy.md`](../analysis/api-agent-orchestration-strategy.md) a question bank generation/review/assembly.
- Exponer endpoints publicos de recursos Closed (`question-banks`, `questions`, `snapshots`, `generation-runs`) y no endpoints genericos de agente.
- Mantener snapshot, answer key, scoring policy y publish transitions como responsabilidad deterministica de `api/`.
- Usar operaciones consultables para question batches, tool loop y assembly cuando exista latencia variable o progreso parcial.
- Validar policy de tools en `agents/`, pero construir contexto, ownership, estados y efectos finales en `api/`.
- Someter endpoints/rutas nuevas al gate Richardson REST, con `Location` para snapshots/operations, links de curation/approval/result y errores normalizados.

### Herramientas requeridas

- `load_learning_outcomes`.
- `search_similar_questions`.
- `validate_answer_consistency`.
- `validate_question_schema`.
- `estimate_question_difficulty`.
- `search_approved_questions`.
- `load_question_usage_history`.
- `calculate_difficulty_distribution`.
- `calculate_outcome_coverage`.
- `validate_assessment_composition`.

### Validadores determinísticos

- Una sola respuesta correcta para SC/MC cuando aplique.
- Schema de pregunta y opciones valido.
- Tags obligatorios antes de activar pregunta.
- Composition usa solo preguntas approved/active.
- Snapshot copia pregunta, opciones, answer key, scoring policy y grade scale.

### Autonomía y controles humanos

- Question Generation y Assembly: `DRAFT_ONLY`.
- Distractor Quality y Ambiguity Review: `ADVISORY`.
- Teacher cura preguntas y aprueba composicion/snapshot.
- API crea snapshot y transiciones de estado.

### Límites operacionales

- Tool loop solo con herramientas `READ_ONLY`/`COMPUTE_ONLY`/`PROPOSE_CHANGE`.
- Sin scoring IA para Closed.
- Sin autoactivacion de preguntas.
- Sin publicar snapshot sin aprobacion.

### Métricas y consumo

- Steps/model calls/tool calls por batch.
- Flags por pregunta y severidad.
- Costo por batch, review y assembly.
- Bloqueos por banco insuficiente o coverage gap.

### Evidencia de finalización

- Tests de policy engine rechazando herramientas no permitidas.
- Tests de validadores de pregunta, answer key, coverage y composition.
- Smoke generation -> review -> curation -> bank -> composition -> snapshot.

### Deuda o capacidades diferidas

- Delegacion multiagente general se difiere; la coordinacion es explicita por flujo.
- Recalculo/anulacion post-grading se difiere a R08.

## 24. Trigger, inputs y outputs

| Proceso | Trigger | Inputs | Outputs |
|---|---|---|---|
| Curriculum tagging | Teacher defines or confirms metadata | subject, topic tags, learning outcome | question metadata, activation eligibility |
| Question generation | Teacher requests batch | subject, topic, outcome, type, difficulty, count, constraints | pending question batch, answer key draft, agent log |
| Quality review | Batch generated or question edited | question, options, key, outcome, difficulty | distractor flags, ambiguity flags, agent logs |
| Curation | Teacher reviews item | question, flags, teacher action | active/rejected/regenerated state, curation event |
| Bank filtering | Teacher searches bank | metadata filters and status | active question results |
| Composition | Teacher defines blueprint | bank questions, outcomes, difficulty, scoring policy | proposed composition, coverage summary, alerts |
| Snapshot publish | Teacher approves publish | final composition, answer key, scoring policy, grade scale | immutable snapshot, publish event |

## 25. Human in the loop

- Teacher define scope de generation.
- Teacher confirma tags antes de activar preguntas.
- Teacher resuelve flags de distractores y ambiguedad.
- Teacher aprueba, edita, regenera o rechaza cada pregunta.
- Teacher aprueba composicion final.
- Teacher confirma publish antes de crear snapshot.
- Operator/developer inspecciona logs y costo, pero no aprueba contenido pedagogico.

## 26. Guardrails

- No autoaprobar preguntas generadas por IA.
- No publicar al bank activo sin teacher curation.
- No usar preguntas rejected, retired o needs_revision en composition.
- No generar snapshot sin aprobacion docente.
- No modificar snapshot publicado.
- No usar IA para scoring closed.
- No ocultar flags critical.
- No trick questions, doble negacion o supuestos no escritos sin flag.
- No fallback premium silencioso.
- No costo duplicado por doble submit idempotente.

## 27. Idempotencia

R04 debe definir keys estables para:

- question generation: `generation_request_id`;
- generated item: `generation_request_id + item_slot`;
- distractor review: `question_version_hash + distractor_review_type`;
- ambiguity review: `question_version_hash + ambiguity_review_type`;
- curation event: `question_id + question_version + teacher_action_id`;
- assembly proposal: `blueprint_hash + bank_snapshot_id`;
- snapshot publish: `closed_assessment_id + approved_composition_id + publish_request_id`;
- log event: `agent_run_id`.

Si el usuario repite generation, review o publish por refresh/doble click, el sistema debe devolver el resultado existente o bloquear el duplicado antes de invocar agente, crear preguntas repetidas o generar un segundo snapshot.

## 28. Reintentos y fallos

| Falla | Comportamiento esperado |
|---|---|
| Question generation timeout | Log failed, permitir retry sin duplicar batch |
| Output con cardinalidad invalida | Marcar validation failure, mantener batch pending/invalid |
| Multiple correct answers detectadas | Flag critical, requiere edicion o confirmacion explicita |
| Weak distractors | Mostrar flags, teacher decide |
| Faltan tags obligatorios | Bloquear transition a active |
| Bank insuficiente para blueprint | Mostrar insufficient_questions y sugerir generar mas |
| Coverage gap | Mostrar alert, permitir ajuste o confirmacion segun regla MVP |
| Snapshot validation failure | No publicar, mostrar error, preservar composition draft |
| Doble publish | No crear segundo snapshot |

## 29. Reversion

- Un batch pending puede retirarse.
- Una pregunta puede volver a needs_revision antes de active.
- Una pregunta active puede retirarse para futuros assessments.
- Curation events no se borran; se agregan nuevos eventos.
- Composition puede editarse antes de publish.
- Snapshot publicado no se modifica; cambios requieren nueva version o flujo P1 de anulacion/recalculo fuera de R04.
- Agent logs incorrectos se corrigen con metadata/evento compensatorio, no con borrado silencioso.

## 30. Consumo y costos

R04 debe registrar costo por batch y por review/assembly asistido.

Campos:

- provider.
- model.
- model_policy.
- agent_name.
- generation_request_id.
- question_count_requested.
- question_count_returned.
- flagged_count.
- input tokens o estimate.
- output tokens o estimate.
- estimated cost USD.
- retry count.
- cost attribution por teacher/customer si existe.

Snapshot publish no deberia tener costo LLM si es deterministico; debe registrarse como evento operacional, no como agent cost.

## 31. Criterios funcionales

- Teacher puede definir scope de preguntas closed.
- Question Generation Agent genera TF/SC/MC con options, key, explanation y difficulty.
- Preguntas generadas quedan pending review.
- Teacher puede ver flags de distractor y ambiguedad.
- Teacher puede aprobar, editar, regenerar o rechazar preguntas.
- Preguntas aprobadas pasan a active con metadata obligatoria.
- Bank filtra por metadata y status.
- Teacher puede componer assessment desde preguntas active.
- System valida answer key y scoring policy antes de publish.
- Publishing crea snapshot inmutable.

## 32. Criterios tecnicos

- `api/` valida ownership, estados y transiciones.
- `api/` impide active sin metadata obligatoria.
- `api/` impide snapshot si composition no esta aprobada.
- `agents` devuelve outputs estructurados para generation, quality, ambiguity y assembly.
- Snapshot copia preguntas, opciones, answer key, scoring policy y grade scale.
- Migraciones Flyway cubren bank, curation, composition y snapshot.
- Pantallas de question bank/composition aplican UI Design/Data Semantics: subject/topic/outcome/type/difficulty/status y snapshot state usan catalogos, selectores, badges o controles DS segun fuente de verdad.
- Pantallas de question bank/composition declaran API I/O contract y sync/async por accion; generation/review/assembly async debe exponer completion model por operacion/polling/SSE/WebSocket/webhook/push segun contrato.
- Question authoring aplica i18n: question text/options/explanations se generan en `outputLocale`, catalogos curriculares tienen labels localizados, validators detectan locale mismatch y logs/telemetria quedan en ingles.
- Tests cubren happy path y errores principales.
- Cross-service smoke test prueba `api` -> `agents` para question generation y reviews.

## 33. Criterios de calidad

- Preguntas tienen stem claro y answer key consistente.
- TF/SC/MC respetan cardinalidad de opciones y correct answers.
- Distractores son plausibles y no absurdos sin flag.
- Ambiguity flags incluyen severidad y sugerencia accionable.
- Composition muestra coverage y difficulty achieved.
- UI deja claro que generated/pending no es active.
- Copy no promete perfeccion pedagogica ni scoring IA.

## 34. Criterios de seguridad

- Ownership denial mantiene patron consistente de la API.
- Question bank y snapshot no pueden consultarse ni modificarse por otro teacher sin permiso explicito.
- No hay datos estudiantiles en R04.
- API keys y prompts quedan server-side.
- Logs sin secretos ni prompts completos.
- Snapshot inmutable protegido por reglas de persistencia y API.
- Bank de un docente no se expone a otros salvo permiso explicito.
- Pruebas negativas cubren acceso cruzado a bank, edicion post-snapshot, provider/model no permitido y output de agente fuera de schema.

## 35. Criterios de observabilidad

- 100% de question generation runs tienen log.
- 100% de distractor/ambiguity review runs tienen log cuando se ejecutan.
- Question generation/review/assembly puede reconstruirse por trace/correlation ID.
- Failed runs tambien se loguean.
- Curation actions quedan auditadas.
- Snapshot creation queda auditado.
- Cost estimate existe o queda marcado como missing con razon.
- Correlation ID permite seguir request entre `web`, `api` y `agents`.
- Validadores reportan metrica/evento sin guardar prompts completos ni preguntas rechazadas como telemetria tecnica.
- Snapshot publish incluye evento canonico con schema version, hash y actor auditado.

## 36. Criterios de despliegue

- Debe correr en entorno local integrado.
- Debe sostener smoke local con `api` y `agents`.
- Puede cerrarse sin invitations, attempts, scoring ni analytics.
- Si se usa entorno `beta`, documentar URL, fecha, commit y evidencia de smoke.
- Snapshot puede validarse por inspeccion interna/API aunque no exista student portal.

## 37. Criterios de negocio

- Primer assessment Closed publicado como snapshot.
- Primer banco curado reusable.
- Primera evidencia de approved question rate.
- Primera evidencia de flags resolved.
- Base para R05 sin prometer ciclo estudiantil todavia.
- No mover refinamientos P1 al MVP si no se recorta alcance equivalente.

## 38. Definition of Done

- [ ] US-100/101/110/111/112/113/114 enriquecidas antes de atomizar.
- [ ] Teacher puede generar batch de preguntas closed.
- [ ] Preguntas generadas entran en pending review.
- [ ] Distractor Quality y Ambiguity Review producen flags visibles.
- [ ] Teacher puede aprobar, editar, regenerar o rechazar preguntas.
- [ ] Preguntas active tienen metadata obligatoria.
- [ ] Bank filtra correctamente por metadata combinable.
- [ ] Teacher puede componer assessment desde preguntas active.
- [ ] System valida answer key, scoring policy y grade scale.
- [ ] Publish crea snapshot inmutable.
- [ ] Logs/costo/idempotencia cubren generation, review, assembly y snapshot.
- [ ] Gate de testing R04 cumplido: fixtures/golden files, JSON Schema, GenAI mock adversarial, Compose Closed authoring y `ai-eval` separado.
- [ ] Gate UI Design/Data Semantics cumplido para UI afectada: metadata curricular, tipos de pregunta, dificultad, estados y snapshot controls no se implementan como texto libre.
- [ ] Gate API I/O + sync/async cumplido: datos de pantalla respaldados por `api/`, y acciones async con completion/progress/failure probado.
- [ ] Gate i18n cumplido: question generation/review/assembly respeta `outputLocale`, catalog labels localizados y telemetria tecnica en ingles.
- [ ] README/planning/release artifacts actualizados.

## 39. Validacion

Validaciones esperadas:

- Unit tests de `api` para estados de question, curation, bank, composition y snapshot.
- Integration tests de `api` con persistencia real para publish/freeze.
- Tests de `agents` para Question Generation, Distractor Quality, Ambiguity Review y Assessment Assembly structured output.
- Tests de `web` para generation form, curation queue, bank filters, composition y publish confirmation.
- Tests de `web` para controles semanticos: subject/topic/outcome/type/difficulty/status, bank filters, composition constraints y valores invalidos.
- Tests Web-API para lectura/escritura de pantallas y completion model si generation/review/assembly usa async.
- Tests i18n para generated questions/options, catalog labels, safe errors, fallback y `locale_mismatch`/`mixed_language_output` en validators.
- Smoke local con `api` y `agents` reales o provider controlado.
- Prueba de idempotencia para question generation y snapshot publish.
- Prueba de immutability del snapshot despues de editar bank.
- JSON Schema/contract checks para comandos/resultados de Question Generation, Distractor Quality, Ambiguity Review y Assessment Assembly.
- Golden files versionados para outputs validos, invalidos y adversariales; GenAI real queda fuera del gate de PR.
- Aceptacion aislada de `agents` con GenAI mock que cubra malformed output, timeout, rate limit y prompt injection fixture.
- Compose Closed authoring para tags -> generation -> curation -> bank -> composition -> snapshot, con PostgreSQL real y GenAI simulado.
- Coverage/no-regression y Sonar quality gate sobre codigo nuevo en `api`, `agents` y `web` afectados.
- `ai-eval` real solo como suite separada/manual o programada, con dataset dorado, presupuesto y revision humana para cambios significativos.

## 40. Escenario Given/When/Then

```gherkin
Given a verified teacher is signed in
And the teacher defines a subject, topic, learning outcome, difficulty, count, and question type
When the teacher generates a closed question batch
And reviews quality and ambiguity flags
And approves enough questions into the active bank
And composes a closed assessment from active questions
And approves the final composition and publishes it
Then generated questions never skip pending review
And active questions have subject_area and learning_outcome
And all curation actions are auditable
And the composition includes only approved or active questions
And publishing creates an immutable snapshot of questions, options, answer key, scoring policy, and grade scale
And every agent run and snapshot event is logged with cost/status metadata where applicable
```

## 41. Metricas

- Question batches generated.
- Approved question rate.
- Rejected question rate.
- Flags detected.
- Critical flags confirmed/resolved.
- Bank active question count.
- Bank filter usage.
- Composition approval rate.
- Coverage achieved.
- Snapshots published.
- Agent log coverage.
- Cost per question batch.

## 42. Evidencias

- Generated question batch.
- Distractor quality review output.
- Ambiguity review output.
- Curation audit trail.
- Active bank view/filter result.
- Approved composition.
- Frozen snapshot record.
- Snapshot hash/version.
- AgentExecutionLog de Question Generation Agent.
- AgentExecutionLog de Distractor Quality Agent.
- AgentExecutionLog de Ambiguity Review Agent.
- AgentExecutionLog de Assessment Assembly Agent si se usa.
- Smoke test local o beta.
- Test output relevante.
- Enlace a planning y PRs cerrados.

## 43. Riesgos y mitigaciones

| Riesgo | Mitigacion |
|---|---|
| R04 crece hacia R05 | Excluir invitations, attempts, scoring, results e item analytics. |
| US esqueleticas se atomizan prematuramente | Ejecutar `/us-enrich` antes de crear tareas. |
| Factualidad o ambiguedad de preguntas | Usar Distractor Quality + Ambiguity Review y teacher curation obligatoria. |
| AI se usa para scoring closed | Mantener scoring deterministico fuera de R04/R05 AI agents. |
| Snapshot mutable por referencia al bank | Copiar datos al snapshot y probar immutability. |
| Bank sin metadata confiable | Bloquear active sin `subject_area` y `learning_outcome`. |
| Costos duplicados por retry | Idempotency key por generation_request y review hash. |

## 44. Resultado esperado

Al cerrar R04, GradeOps AI tiene un assessment Closed autoral y publicable: preguntas generadas por IA, curadas por docente, almacenadas en un bank filtrable, compuestas en una evaluacion cerrada y congeladas en un snapshot inmutable. Esta release desbloquea R05, que usa ese snapshot para links, attempts, scoring deterministico, resultados e item analytics.

## 45. Prompt ejecutable `/release-*`

Comandos inspeccionados en `.planning/scripts/release.mjs` y `.planning/TUTORIAL/reference.md`:

- `/release-init`
- `/release-new vX.Y.Z -- <purpose>`
- `/release-add vX.Y.Z NNN-slug [NNN-slug ...]`
- `/release-remove vX.Y.Z NNN-slug`
- `/release-status [vX.Y.Z] [--mark-planned|--mark-in-progress|--mark-blocked|--mark-released|--mark-cancelled]`

Prompt operativo para crear la release en el sistema `.releases/` del plugin, con placeholders explicitos porque el Master Plan no fija version semantica, target period, fecha estimada ni planning ID operativo para R04:

```text
Contexto:
Estamos ejecutando R04 del Master Plan: Closed Question Bank to Snapshot.
Fuentes obligatorias:
- docs/master-plan/releases/release-04-closed-question-bank-snapshot.md
- docs/master-plan/master-plan-executive.md
- docs/master-plan/analysis/release-strategy.md
- docs/master-plan/analysis/automation-inventory.md
- docs/master-plan/analysis/user-story-inventory.md
- docs/02-product/workflows.md
- docs/02-product/user-stories/epic-11-curriculum-structure/
- docs/02-product/user-stories/epic-12-question-bank-closed-assessment/
- docs/03-ai-agents/question-generation-agent.md
- docs/03-ai-agents/distractor-quality-agent.md
- docs/03-ai-agents/ambiguity-review-agent.md
- docs/03-ai-agents/assessment-assembly-agent.md

Precondiciones:
- R01 debe estar cerrada o existir evidencia aceptada de auth, agent logs, costo, retry e idempotencia base.
- No inventar planning IDs. Si no existe planning operativa para R04, crearla primero con el flujo de planning vigente.
- Ejecutar /us-enrich sobre las US de R04 antes de atomizar tareas.

Objetivo:
Crear y gestionar la release operativa R04: metadata curricular -> question batch -> curation -> active bank -> composition -> frozen snapshot.

Comandos:
1. Si .releases/ no existe:
   /release-init
2. Crear la release:
   /release-new <VERSION> -- Closed Question Bank to Snapshot --target <YYYY-QN-MN-WN> --date <YYYY-MM-DD>
3. Agregar planning(s) existentes de R04, despues de verificar que existen:
   /release-add <VERSION> <PLANNING_ID_R04>
4. Revisar estado:
   /release-status <VERSION>

Alcance:
- US-100, US-101.
- US-110, US-111, US-112, US-113, US-114.
- AUT-11, AUT-12, AUT-13, AUT-14 parcial, AUT-16, AUT-17, AUT-21.
- US-PROPUESTA-04 y US-PROPUESTA-07 si R01 no las generalizo.

Exclusiones:
- Student invitations, attempts, deterministic scoring execution, results, item analytics, annul/recalculate, OCR/OMR, student accounts, proctoring, US-102 y US-115.

Arquitectura:
- web -> api -> agents.
- agentclient es el unico caller de agents desde api.
- Generated questions entran pending review.
- Curation actions son auditables.
- Snapshot copia datos inmutables; no referencia bank mutable para scoring futuro.
- Closed scoring sera deterministico, no IA.

Seguridad:
- Mantener auth y ownership server-side.
- No datos estudiantiles en R04.
- No exponer prompts internos ni payloads completos en logs visibles.
- Proteger snapshot contra edicion estructural post-publish.

Automatizacion:
- Question generation, distractor review, ambiguity review y assembly son asistidos/supervisados.
- Snapshot publish es deterministico despues de aprobacion docente.
- Logs, costo, retry e idempotencia automatizados.

Trazabilidad:
- Si D-04 o D-06 no quedaron cerradas en R01, registrar el residual antes de implementar R04.
- Si aparece contradiccion de docs vs codigo, registrarla y no resolverla silenciosamente.
- No implementar R05 dentro de esta release.

Metricas:
- approved question rate.
- flags resolved.
- coverage achieved.
- snapshot published.
- agent log coverage.
- cost per question batch.

Criterios:
- El flujo es demostrable de extremo a extremo hasta snapshot.
- Tests y smoke real api -> agents documentados.
- README/planning/release status actualizados.
```

## 46. Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Incorporacion de i18n por release funcional | Alinear R04 con question generation y catalogos curriculares en locale solicitado | UI/API/Agents, DoD operativo, validacion | D-I18N-01..D-I18N-10 |
| 2026-07-21 | Incorporacion de UI Design/Data Semantics | Alinear R04 con catalogos/controles DS para metadata curricular, banco, composicion y snapshot | UI, DoD operativo, validacion | D-UI-01..D-UI-08 |
| 2026-07-21 | Incorporacion de API I/O y sync/async contract | Alinear R04 con datos de pantalla respaldados por `api/` y completion model para Closed authoring async | UI/API, DoD operativo, validacion | D-UI-01..D-UI-08, D-API-01..D-API-10 |
| 2026-07-21 | Incorporacion de Testing & Quality Gates | Alinear R04 con golden files, JSON Schema, GenAI mock adversarial, Compose Closed authoring y `ai-eval` separado | Testing, CI/testkit, DoD operativo | D-TEST-01..D-TEST-09 |
| 2026-07-21 | Incorporacion de Observability & Telemetry | Alinear R04 con trazas de Closed authoring, validadores medidos, snapshot auditado y costo por batch/pregunta | Observabilidad, DoD operativo | D-OBS-01..D-OBS-08 |
| 2026-07-21 | Incorporacion de Security & Authorization | Alinear R04 con ownership de bank/snapshot, snapshot inmutable, policy de agentes y output cerrado | Seguridad, DoD operativo | D-SEC-01..D-SEC-08 |
| 2026-07-20 | Incorporacion de capacidades de Agent Runtime | R04 es el primer consumidor claro de tool loop controlado y policy engine | Runtime, closed agents, tools, validators | D-04, D-06 |
| 2026-07-20 | Incorporacion de API-Agent Orchestration | Asegurar que Closed authoring use endpoints REST de recursos y que snapshot/scoring sigan en `api/` | API, agents, web routes, DoD operativo | D-API-01..D-API-10 |
| 2026-07-20 | Creacion inicial | Ejecucion de Fase 05 para R04 | Todo el documento | D-02 |
