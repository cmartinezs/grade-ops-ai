# Release 05 - Closed Student Response and Item Analytics

## 1. Identificacion

| Campo | Valor |
|---|---|
| Release | R05 |
| Nombre | Closed Student Response and Item Analytics |
| Archivo | `docs/master-plan/releases/release-05-closed-response-item-analytics.md` |
| Estado | Documentada |
| Complejidad | L |
| Corte | MVP / primer ciclo Closed end-to-end |
| Fuente estrategica | `docs/master-plan/analysis/release-strategy.md` |

## 2. Prevalidacion

| Precondicion | Resultado |
|---|---|
| La release existe en `master-plan-executive.md` | OK |
| Sus US estan asignadas | OK: US-120, US-121, US-122, US-123, US-124 |
| No existen decisiones bloqueantes para definir esta release | OK con condicion: privacidad, fallos visibles e idempotencia deben venir cerradas o incorporarse |
| La release no es XL | OK si se excluyen anulacion/recalculo, OCR/OMR, student accounts y proctoring |
| Habilita un flujo vertical | OK: learner list -> signed links -> response -> deterministic grading -> result access -> item analytics |
| Dependencias anteriores claras | OK: R04 es dependencia obligatoria |

## 3. Objetivo ejecutivo

Completar el ciclo Closed usando el snapshot congelado de R04: el docente crea una lista de estudiantes, envia links seguros, los estudiantes responden sin cuenta, el sistema califica deterministicamente contra la answer key congelada, el docente publica resultados y revisa item analytics.

R05 convierte el snapshot autoral de R04 en un assessment Closed operable con evidencia de acceso, attempts, grading reproducible y valor analitico por pregunta.

## 4. Problema

Un snapshot Closed publicado no prueba adopcion ni integridad operacional si ningun estudiante responde. El producto necesita demostrar acceso sin cuenta, aislamiento fuerte por estudiante, scoring reproducible y analitica de items sin usar IA para corregir respuestas objetivas.

## 5. Hipotesis

Si links firmados, response capture, grading deterministico y item analytics funcionan sobre un snapshot congelado, entonces GradeOps AI puede demostrar un modo Closed completo sin implementar cuentas de estudiante, proctoring, OCR/OMR ni recalculo avanzado.

## 6. Actor beneficiado

- **Teacher**: invita estudiantes, publica resultados y entiende calidad de preguntas.
- **Student/LearnerRef**: responde y consulta resultados sin crear cuenta.
- **Operator**: obtiene evidencia de completion, delivery, grading deterministico y access logs.
- **Developer**: valida integridad de token, snapshot, attempt, result y analytics.

## 7. Valor entregado

- Primer ciclo Closed end-to-end demostrable.
- Student access sin cuenta mediante links seguros.
- Attempt y response ledger por learner.
- Grading deterministico contra snapshot inmutable.
- Result publication controlada por docente.
- Result access aislado por estudiante.
- Item analytics con correct rate, difficulty index, flags y reinforcement suggestions.
- Evidencia de invitation, attempt, result access y analytics.

## 8. Nivel de automatizacion

| Proceso | Nivel inicial | Nivel objetivo en R05 |
|---|---|---|
| Learner list | Manual | Automatizada en validacion/import, control docente |
| Access links | Automatizada | Automatizada con envio, resend, revoke y logs |
| Student response | Automatizada | Automatizada con validacion de token y attempt unico |
| Closed scoring | Automatizada | Automatizada y deterministica contra snapshot |
| Result publication | Manual | Supervisada por teacher |
| Item analytics | Asistida | Supervisada con calculos deterministas e interpretacion IA |
| Logs, costo e idempotencia | Automatizada | Automatizada para access, grading, analytics y email events |

## 9. Alcance incluido

- Crear learner list por entrada manual.
- Importar CSV con emails de learners.
- Asociar learners a assessment cerrado publicado.
- Generar token unico por learner y assessment.
- Enviar email con access link.
- Permitir resend y revoke de links.
- Validar token server-side: no expirado, no revocado, assessment accepting responses.
- Mostrar assessment cerrado desde snapshot.
- Permitir seleccionar alternativas y revisar antes de submit.
- Registrar attempt y closed responses.
- Marcar link usado despues de submit.
- Ejecutar grading deterministico contra frozen answer key.
- Generar grade result por attempt.
- Permitir publicacion docente de resultados.
- Crear o enviar result access link por learner.
- Mostrar grade, score y feedback aprobado si existe.
- Mostrar correct answers/item details solo si teacher configuro visibilidad.
- Generar item analytics report con correct rate, difficulty index, distribution, flags y reinforcement suggestions.
- Registrar logs de access, attempts, grading, results, analytics y costos si aplica.

## 10. Exclusiones explicitas

- Student account creation o login persistente.
- Proctoring.
- OCR/OMR.
- Annul question and recalculate.
- Answer-key correction workflow.
- Recalculo post-publicacion.
- Adaptive question ordering.
- Student-to-student discussion.
- Open assessment student self-submission.
- Retakes complejos o multiples attempts por policy avanzada.
- LMS integration.
- Billing self-serve.
- Dashboard completo de negocio.

## 11. Capacidades

| Capacidad | Rol en R05 |
|---|---|
| C2 Student/Learner access sin cuenta | Proceso principal de invitations, response y result access. |
| C12 Item analytics Closed | Salida analitica principal del ciclo Closed. |
| C13 Evidencia de ejecucion y acceso | Logs obligatorios para audit trail y demo. |

## 12. Flujo funcional

```text
Teacher opens a published closed assessment snapshot from R04
  -> creates or imports learner list
  -> system creates AssessmentInvitation records with signed/hashed tokens
  -> teacher sends access links
  -> learner opens link
  -> system validates token and assessment accepting_responses state
  -> learner answers questions from the frozen snapshot
  -> learner reviews and submits
  -> system records attempt and marks invitation used
  -> deterministic engine grades against frozen answer key
  -> teacher reviews results and confirms publication
  -> system sends or enables result links
  -> learner opens own result link only
  -> teacher runs item analytics
  -> Item Analytics Agent interprets aggregates and suggests reinforcement
```

## 13. User stories incluidas

| US | Titulo | Estado en R05 |
|---|---|---|
| US-120 | Create Learner List | P0, debe enriquecerse antes de atomizar |
| US-121 | Send Assessment Access Links | P0, debe enriquecerse antes de atomizar |
| US-122 | Student Response via Link | P0, debe enriquecerse antes de atomizar |
| US-123 | Publish Results and Student Result Access | P0, debe enriquecerse antes de atomizar |
| US-124 | Item Analytics Report | P0, debe enriquecerse antes de atomizar |

## 14. Historias propuestas o modificadas

| ID | Nombre | Uso en R05 |
|---|---|---|
| US-PROPUESTA-02 | Student Data Deletion / Anonymization on Request | Requerida para privacidad de LearnerRef, attempts y result links. |
| US-PROPUESTA-03 | Agent Run Retry and Failure Recovery | Requerida para fallos visibles de email, token, submit, grading y analytics. |
| US-PROPUESTA-04 | Idempotent Assessment/Question Generation Requests | Heredada como patron; R05 debe extender idempotencia a invitations, attempts, grading y result publication. |

Si privacidad, retry e idempotencia quedaron generalizadas en R01/R04, R05 debe reutilizarlas. Si no, deben entrar como prerequisito tecnico antes de exponer links reales a estudiantes.

## 15. Consideraciones adicionales para las US

- Todas las US de R05 estan en estado NOT READY/esqueletico; deben pasar por `/us-enrich`.
- US-120 debe definir si CSV es corte obligatorio o fallback manual basta para primer smoke.
- US-121 debe especificar expiracion, revoke, resend y delivery failure.
- US-122 debe trasladar explicitamente el vinculo con grading deterministico contra snapshot.
- US-123 debe definir visibilidad de correct answers y feedback aprobado.
- US-124 debe separar calculos deterministas de interpretacion IA.

## 16. Reglas de negocio

- R05 solo opera sobre assessment cerrado con snapshot publicado.
- Cada learner tiene invitacion unica por assessment, salvo resend/version explicita.
- Token se valida server-side y se almacena hasheado o con proteccion equivalente.
- Link revocado, expirado o usado no permite nuevo submit.
- Student no necesita cuenta.
- Student no puede acceder a resultados de otro learner.
- Attempt se califica contra snapshot, no contra bank mutable.
- Closed grading es deterministico; IA no modifica score ni answer key.
- Teacher controla publicacion de resultados.
- Correct answers se muestran solo si teacher configuro visibilidad.
- Item analytics no cambia notas, answer key ni resultados publicados.

## 17. Dependencias

| Dependencia | Estado | Accion en R05 |
|---|---|---|
| R04 Closed Question Bank to Snapshot | Previa obligatoria | Usar snapshot inmutable, answer key, scoring policy y grade scale |
| D-02 Closed = P0 | Resuelta | Mantener R05 dentro de MVP y refinamientos en R08 |
| Privacy/PII para learners | Requerida | Incorporar US-PROPUESTA-02 si no existe |
| Email provider/config | Requerido | Definir modo local/beta y evidencia de delivery |
| AUT-14 snapshot/scoring | Parcial desde R04 | Completar scoring deterministico sobre attempts |
| AgentExecutionLog/cost backbone | Heredado | Extender a item analytics y access events donde aplique |

## 18. Integraciones

- `web/` teacher workspace para learner list, invitations, results y analytics.
- Student-facing web route para access link y result link.
- `api/` Spring Boot para token validation, attempts, grading, result publication y ownership.
- `agents` Item Analytics Agent.
- Email provider o adapter local para invitation/result notifications.
- PostgreSQL/Flyway para learners, invitations, attempts, responses, grade results, result access y analytics.
- Evidence/logging backbone heredado de R01/R04.

## 19. Arquitectura minima necesaria

- `web/` no llama modelos directamente.
- Student routes usan token, no teacher session.
- `api/` valida token y assessment state antes de entregar snapshot.
- Snapshot data se lee desde tablas congeladas, no desde live bank.
- Deterministic grading vive en `api` o modulo deterministico, no en agents.
- Item Analytics combina agregadores deterministas con interpretacion IA.
- Result publication crea estado separado de grade calculation.
- Access tokens y result tokens tienen expiracion, revocation y usage audit.

## 20. Datos y migraciones

Entidades/datos esperados:

- `LearnerRef`.
- `LearnerListImport`.
- `AssessmentInvitation`.
- `InvitationDeliveryEvent`.
- `AccessTokenHash`.
- `AssessmentAttempt`.
- `ClosedResponse`.
- `ClosedResponseItem`.
- `DeterministicGradeResult`.
- `ResultPublication`.
- `ResultAccessToken`.
- `ResultAccessEvent`.
- `ItemAnalyticsReport`.
- `ItemAnalyticsFlag`.
- `ReinforcementSuggestion`.
- `AgentExecutionLog`.

Campos minimos nuevos o extendidos:

- `learner_email`.
- `learner_display_name`.
- `external_id`.
- `token_hash`.
- `expires_at`.
- `revoked_at`.
- `used_at`.
- `delivery_status`.
- `attempt_status`.
- `submitted_at`.
- `snapshot_id`.
- `score`.
- `grade`.
- `result_publication_state`.
- `correct_answers_visible`.
- `analytics_run_id`.
- `correct_rate`.
- `difficulty_index`.
- `item_flags`.

## 21. Seguridad y privacidad

- Aplicar [`security-strategy.md`](../analysis/security-strategy.md) a signed links, invitation context, attempts, result access e item analytics.
- Learner email y result link son datos sensibles.
- Tokens deben guardarse hasheados o con proteccion equivalente.
- Token validation debe ser server-side.
- Tokens deben expirar, revocarse y rechazar replay/tampering con errores seguros.
- Result access debe aislar estrictamente por learner.
- Error messages no deben revelar si un email pertenece a otro assessment.
- Rutas publicas de student access requieren rate limiting o residual explicito antes de pilotos reales.
- Access logs deben evitar payloads completos de respuestas cuando no sea necesario.
- Deletion/anonymization de LearnerRef debe tener historia o residual explicito.
- Result links deben expirar o poder revocarse.
- Teacher ownership se valida para learner list, invitations, results y analytics.

## 22. Observabilidad y auditoria

- Aplicar [`observability-strategy.md`](../analysis/observability-strategy.md) a invitations, signed links, attempts, deterministic scoring, publication e item analytics.
- Propagar contexto por outbox/adaptador asincrono cuando analytics o delivery no sean sincronos.
- Medir queue delay, heartbeat, lease, retries, stuck runs, delivery failure rate, access denial rate y analytics duration.
- Registrar eventos canonicos de invitation created/sent/revoked, access opened/denied, attempt submitted, grading completed/failed y result accessed.
- `learner_added`.
- `learner_list_imported`.
- `assessment_invitation_created`.
- `assessment_invitation_sent`.
- `assessment_invitation_resend_requested`.
- `assessment_invitation_revoked`.
- `assessment_access_opened`.
- `assessment_access_denied`.
- `assessment_attempt_started`.
- `assessment_attempt_submitted`.
- `closed_grading_completed`.
- `closed_grading_failed`.
- `result_publication_confirmed`.
- `result_access_link_created`.
- `result_access_opened`.
- `item_analytics_generated`.
- `item_analytics_reviewed`.
- `agent_run_started`, `agent_run_completed`, `agent_run_failed`.

## 23. Automatizaciones

| ID | Proceso | Uso en R05 |
|---|---|---|
| AUT-14 | Snapshot y scoring cerrado | Completar scoring deterministico contra snapshot |
| AUT-15 | Analitica de items | Proceso principal de analytics |
| AUT-19 | Links seguros y notificaciones a estudiantes | Proceso principal de invitation/result access |
| AUT-16 | Registro de ejecuciones | Obligatorio para analytics y eventos clave |
| AUT-17 | Estimacion de tokens/costo | Obligatorio para Item Analytics Agent y costos email si aplica |
| AUT-21 | Reintentos/idempotencia/fallos | Obligatorio para email, submit, grading y analytics |

## Capacidades de IA y Agent Runtime

### Agentes involucrados

- Item Analytics Agent.
- No usar IA para scoring closed.

### Capacidades funcionales habilitadas

- Interpretar metricas de items cerrados.
- Generar recomendaciones revisables sobre dificultad, discriminacion y distractores.
- Mantener scoring, attempts, links y publication como flujos deterministicos de API.

### Incrementos del runtime requeridos

- Persistencia `AgentRun`/`AgentStep` cuando analytics o volumen excedan tiempos HTTP seguros.
- Estados consultables para ejecuciones largas: `QUEUED`, `RUNNING`, `COMPLETED`, `FAILED`, `BLOCKED`, `TIMED_OUT`, `CANCELLED`.
- Idempotencia por analytics snapshot.
- Cancelacion/reanudacion solo si se justifica por volumen o costo; no anticiparla para runs cortos.
- Handoff desde scoring deterministico hacia analytics con snapshot fijo.

### Incremento API-Agent Orchestration requerido

- Aplicar [`api-agent-orchestration-strategy.md`](../analysis/api-agent-orchestration-strategy.md) a student attempts, deterministic scoring e item analytics.
- Introducir asincronia durable solo cuando attempts/analytics excedan el presupuesto HTTP seguro o requieran progreso parcial.
- Modelar batches como `AiOperation` con un `AgentRun` por item/subconjunto cuando aplique, sin repetir lotes completos ante fallos parciales.
- Mantener signed links, attempts, scoring deterministic y publication en `api/`; `agents/` solo interpreta analytics revisables.
- Exponer polling de operacion para `web`/student access cuando la ejecucion sea larga; SSE queda como mejora compatible.
- Someter endpoints/rutas nuevas al gate Richardson REST, incluyendo recursos de invitation/attempt/result/analytics, status codes, links y errores seguros para estudiantes.

### Herramientas requeridas

- `calculate_item_difficulty`.
- `calculate_discrimination_index`.
- `calculate_distractor_distribution`.
- `load_item_history`.
- `load_attempt_aggregates`.

### Validadores determinísticos

- Scoring reproduce answer key snapshot.
- Analytics usa solo attempts/submissions cerrados en el snapshot consultado.
- Small sample queda marcado como warning.
- Result publication requiere confirmacion docente.

### Autonomía y controles humanos

- Item Analytics: `EXECUTE_READ_ONLY`.
- Teacher revisa analytics antes de compartir o actuar.
- API controla links, attempts, grading, publication y result access.

### Límites operacionales

- Analytics puede ser asincrono si el volumen lo exige.
- Sin recalculo por anulacion en R05.
- Sin acceso de estudiantes a logs, prompts, costos o respuestas de otros learners.
- Sin red externa desde herramientas de analytics.

### Métricas y consumo

- Runs de analytics, duration, model calls y tool calls.
- Cost per analytics report.
- Small sample warnings.
- Invitation/attempt/grading deterministic success rate.

### Evidencia de finalización

- Tests de scoring deterministico contra snapshot.
- Tests de analytics con sample suficiente e insuficiente.
- Smoke learner link -> attempt -> deterministic grade -> publication -> item analytics.

### Deuda o capacidades diferidas

- Reanudacion completa de runs se limita a analytics/lotes; no se generaliza si no hay necesidad real.
- Annul/recalculate queda para R08.

## 24. Trigger, inputs y outputs

| Proceso | Trigger | Inputs | Outputs |
|---|---|---|---|
| Learner list | Teacher adds/imports learners | assessment ID, emails, optional names/external IDs | LearnerRef records |
| Invitation send | Teacher sends links | learner refs, snapshot, expiration policy | AssessmentInvitation, email event |
| Student access | Learner opens link | signed token | token validation result, assessment snapshot view |
| Attempt submit | Learner submits response | token, selected alternatives, snapshot ID | attempt, closed responses, used invitation |
| Deterministic grading | Attempt submitted | attempt responses, answer key snapshot, scoring policy | grade result, scoring event |
| Result publication | Teacher confirms publish | grade results, visibility config | result publication state, result links |
| Item analytics | Teacher requests analytics | graded attempts, answer key snapshot, question snapshots | item analytics report, reinforcement suggestions, agent log |

## 25. Human in the loop

- Teacher crea/importa learners.
- Teacher decide enviar, reenviar o revocar links.
- Student responde sin cuenta y confirma submit.
- Teacher confirma publicacion de resultados.
- Teacher decide visibilidad de correct answers.
- Teacher revisa item analytics antes de compartir o actuar.
- Operator/developer inspecciona eventos y logs, pero no accede a datos sensibles sin permiso.

## 26. Guardrails

- No student accounts en R05.
- No acceso cruzado entre estudiantes.
- No scoring IA para closed.
- No modificar snapshot publicado.
- No recalcular por key correction dentro de R05.
- No publicar resultados sin teacher confirmation.
- No exponer correct answers si teacher no lo configuro.
- No reenviar links sin accion docente o politica explicita.
- No guardar tokens en claro.
- No ocultar email delivery failures.

## 27. Idempotencia

R05 debe definir keys estables para:

- learner import row: `assessment_id + normalized_email + import_batch_id`;
- invitation: `assessment_id + learner_ref_id`;
- resend event: `invitation_id + resend_request_id`;
- revoke event: `invitation_id + revoke_request_id`;
- attempt submit: `invitation_id + submit_request_id`;
- grading result: `attempt_id + snapshot_id + scoring_policy_hash`;
- result publication: `assessment_id + result_publication_request_id`;
- result access token: `result_publication_id + learner_ref_id`;
- item analytics: `assessment_id + result_snapshot_id`;
- log event: `agent_run_id`.

Si el estudiante repite submit por refresh o red, el sistema debe devolver confirmation del attempt existente o bloquear duplicado antes de crear un segundo attempt o recalificar.

## 28. Reintentos y fallos

| Falla | Comportamiento esperado |
|---|---|
| CSV invalido | Mostrar filas invalidas, no crear learners parciales sin resumen |
| Email delivery failure | Registrar failure, permitir resend visible |
| Token expirado/revocado | Rechazar con mensaje claro sin filtrar datos sensibles |
| Assessment no accepting responses | Rechazar access sin exponer estado interno excesivo |
| Submit duplicado | No crear segundo attempt |
| Grading deterministic failure | Log failed, attempt queda en estado recoverable |
| Result publication failure | No exponer result links parciales sin estado claro |
| Item analytics IA falla | Mantener agregados deterministas disponibles y loguear failure |
| Muestra insuficiente | Mostrar warning `small_sample_size` o equivalente |

## 29. Reversion

- Learner puede removerse o anonimizarse segun politica antes de attempt.
- Invitation puede revocarse.
- Result publication puede despublicarse si no rompe promesa de acceso vigente, registrando evento.
- Attempt submitted no se borra silenciosamente; deletion/anonymization requiere proceso auditado.
- Grade result se corrige solo por flujo futuro de key correction/recalculate fuera de R05.
- Item analytics report puede versionarse o descartarse sin cambiar resultados.

## 30. Consumo y costos

R05 tiene bajo costo LLM: scoring es deterministico y links/email no usan LLM. El costo principal de IA viene de Item Analytics Agent.

Campos:

- provider.
- model.
- model_policy.
- agent_name.
- analytics_run_id.
- assessment_id.
- learner_count.
- attempt_count.
- item_count.
- input tokens o estimate.
- output tokens o estimate.
- estimated cost USD.
- email_delivery_count.
- email_delivery_failures.
- retry count.

Email y DB pueden registrarse como costo operacional si R06 define ledger, pero no deben mezclarse con costo de agent run.

## 31. Criterios funcionales

- Teacher puede crear learner list manual.
- Teacher puede importar CSV con learner emails.
- System genera token unico por learner+assessment.
- Teacher puede enviar, reenviar y revocar links.
- Student puede abrir link valido sin cuenta.
- Student puede seleccionar alternativas, revisar y enviar.
- System registra attempt y marca invitation usada.
- System califica deterministicamente contra snapshot.
- Teacher puede publicar resultados.
- Student ve solo su resultado.
- Item Analytics Agent genera correct rate, difficulty index, distribution y reinforcement suggestions.

## 32. Criterios tecnicos

- `api/` valida ownership en teacher flows.
- `api` valida tokens en student flows sin depender de teacher session.
- Tokens se almacenan protegidos y expiran.
- Attempts usan snapshot ID obligatorio.
- Grading usa answer key snapshot, no live bank.
- Result access valida learner/result token.
- Item analytics separa agregados deterministas de interpretacion IA.
- Migraciones Flyway cubren learner, invitation, attempt, result y analytics.
- Pantallas teacher/student aplican UI Design/Data Semantics: learner lists, invitation states, answer controls, result visibility y analytics se tratan como controles o read-only provenance segun fuente de verdad.
- Pantallas teacher/student declaran API I/O contract y sync/async por accion; analytics async o delivery async debe exponer completion model por operacion/polling/SSE/WebSocket/webhook/push segun contrato.
- Student link, attempts, result publication e item analytics aplican i18n: public screens, emails/messages, result labels y safe errors respetan locale del estudiante/docente; audit/logs tecnicos quedan en ingles.
- Tests cubren happy path y errores principales.

## 33. Criterios de calidad

- Student UI es clara y no requiere entender GradeOps.
- Review-before-submit evita errores accidentales.
- Mensajes de token invalido son seguros y comprensibles.
- Result view distingue grade, score y feedback aprobado.
- Item analytics separa estadistica de interpretacion.
- Flags de possible key error no modifican resultados.
- Copy no promete proctoring ni cuenta estudiantil.

## 34. Criterios de seguridad

- Student no puede enumerar learners o results.
- Token tampered/expired/revoked se rechaza.
- Replay protection y nonce/attempt state evitan reutilizar links o submissions fuera de policy.
- Result links no exponen otros learners.
- Correct answers respetan visibility config.
- Logs no contienen tokens en claro.
- Learner PII minimizada.
- Deletion/anonymization queda implementada o registrada como residual aceptado antes de pilotos reales.
- Pruebas negativas cubren token alterado, token expirado, token revocado, intento cross-assessment, enumeracion por email/result y rate limit basico.

## 35. Criterios de observabilidad

- Invitation created/sent/revoked queda auditado.
- Access denied/opened queda auditado.
- Attempt submitted queda auditado.
- Closed grading completed/failed queda auditado.
- Result publication y access quedan auditados.
- Item Analytics Agent run tiene AgentExecutionLog.
- Failed email/analytics runs tambien se loguean.
- Correlation/request ID permite seguir invitation -> attempt -> grade -> result.
- Operaciones asincronas conservan trace context enlazable y crean attempt/span nuevo por retry.
- Alertas/runbooks cubren runs estancados, delivery failure sostenido y fallo de exportacion de telemetria.
- Tokens/link completos no aparecen en logs, spans, metric labels ni eventos de producto.

## 36. Criterios de despliegue

- Debe correr en entorno local integrado.
- Debe sostener smoke local con snapshot R04.
- Email puede usar adapter local si se documenta; entorno beta requiere evidencia de delivery real o simulada aceptada.
- Puede cerrarse sin proctoring, accounts, OCR/OMR ni recalculo.
- Si se usa entorno `beta`, documentar URL, fecha, commit y evidencia de smoke.

## 37. Criterios de negocio

- Primer closed attempts completed.
- Primer deterministic grading success rate.
- Primer result publication event.
- Primer item analytics report.
- Evidencia de zero-account student access.
- Base para R06 con logs de adoption y completion.
- No prometer integridad/proctoring de examen remoto.

## 38. Definition of Done

- [ ] US-120/121/122/123/124 enriquecidas antes de atomizar.
- [ ] R04 snapshot disponible como fuente de assessment.
- [ ] Teacher puede crear/importar learner list.
- [ ] System genera y envia invitation links unicos.
- [ ] Teacher puede resend/revoke links.
- [ ] Student responde por link sin cuenta.
- [ ] Attempt se registra una sola vez por invitation policy.
- [ ] Grading deterministico corre contra snapshot.
- [ ] Teacher publica resultados.
- [ ] Student accede solo a su resultado.
- [ ] Item analytics report genera stats, flags y reinforcement suggestions.
- [ ] Logs/idempotencia/fallos cubren invitation, submit, grading, result y analytics.
- [ ] Gate de testing R05 cumplido: Playwright student-link flow, tests negativos de token/replay/tamper, deterministic grading y JMeter smoke allowlisted.
- [ ] Gate UI Design/Data Semantics cumplido para UI afectada: learner/invitation/attempt/result/analytics controls respetan fuente de verdad, tokens no son editables y answers usan controles por tipo de pregunta.
- [ ] Gate API I/O + sync/async cumplido: datos de pantalla respaldados por `api/`, y acciones async con completion/progress/failure probado.
- [ ] Gate i18n cumplido: pantallas publicas/student-facing, results e item analytics respetan locale y no localizan logs/telemetria tecnica.
- [ ] README/planning/release artifacts actualizados.

## 39. Validacion

Validaciones esperadas:

- Unit tests de `api` para token validation, invitation state, attempt state, grading y result access.
- Integration tests de `api` con persistencia real para learner -> invitation -> attempt -> grade -> result.
- Tests de deterministic grading contra snapshot editado en bank para probar aislamiento.
- Tests de `web` student flow: open link, answer, review, submit, result access.
- Tests de teacher flow: learner list, send/resend/revoke, publish results, analytics.
- Tests de `web` para controles semanticos: answer inputs por tipo de pregunta, estados de invitation/result, visibility, token errors y analytics read-only/provenance.
- Tests Web-API para lectura/escritura de pantallas y completion model si analytics/delivery usa async.
- Tests i18n para student link flow, result publication, item analytics labels, safe errors, fallback y logs/telemetria en ingles.
- Tests de `agents` para Item Analytics structured output.
- Smoke local con R04 snapshot y al menos dos learners.
- Prueba de token expired/revoked/tampered.
- Prueba de no cross-student result access.
- Playwright student-link flow con link valido, expirado, revocado y manipulado; screenshots/video solo al fallar.
- Contract checks Web-API para rutas publicas student/result y API-Agents para Item Analytics.
- Compose Closed full-chain con snapshot R04, invitations, attempts, deterministic grading, result access e item analytics.
- Coverage/no-regression y Sonar quality gate sobre codigo nuevo en los artefactos modificados.
- JMeter smoke de rutas publicas allowlisted, con datos sinteticos, anti-enumeracion y abortado por error rate/latencia/costo.
- Artefactos sin tokens, signed links completos ni PII en logs, traces, JTL, screenshots o summaries.

## 40. Escenario Given/When/Then

```gherkin
Given a verified teacher has a published closed assessment snapshot from R04
And the teacher creates a learner list with two learners
When the teacher sends assessment access links
And a learner opens a valid link
And submits answers after reviewing them
And the teacher publishes results
And requests item analytics
Then the system validates the token server-side
And records exactly one attempt for the learner invitation
And grades the attempt deterministically against the frozen snapshot answer key
And provides result access only for that learner
And the item analytics report includes correct rate, difficulty index, distribution, flags, and reinforcement suggestions
And all invitation, attempt, grading, result, and analytics events are auditable
```

## 41. Metricas

- Learners invited.
- Invitation delivery success rate.
- Links revoked.
- Assessment access opened.
- Closed attempts started.
- Closed attempts completed.
- Deterministic grading success rate.
- Results published.
- Result links opened.
- Item analytics reports generated.
- Items flagged for review.
- Student access denial rate.
- Email delivery failure rate.

## 42. Evidencias

- Learner list record.
- AssessmentInvitation record.
- Email delivery/resend/revoke event.
- Access validation event.
- AssessmentAttempt record.
- ClosedResponse records.
- DeterministicGradeResult.
- ResultPublication event.
- ResultAccess event.
- ItemAnalyticsReport.
- AgentExecutionLog de Item Analytics Agent.
- Smoke test local o beta.
- Test output relevante.
- Enlace a planning y PRs cerrados.

## 43. Riesgos y mitigaciones

| Riesgo | Mitigacion |
|---|---|
| R04 snapshot no esta listo | Bloquear R05 hasta tener snapshot inmutable y answer key. |
| US esqueleticas se atomizan prematuramente | Ejecutar `/us-enrich` antes de crear tareas. |
| Privacidad de learner/result links | Hash tokens, expiracion, revocation y no cross-access tests. |
| Deliverability de email | Adapter local documentado y failures visibles; beta con evidencia si aplica. |
| Snapshot mistake post-publish | Excluir recalculo en R05; documentar residual US-115/R08. |
| IA intenta interpretar scoring | Mantener grading deterministico y agent solo en analytics post-grade. |
| Doble submit crea doble attempt | Idempotencia por invitation + submit_request. |
| Item analytics sobre muestra pequena | Mostrar warning y separar estadistica de interpretacion. |

## 44. Resultado esperado

Al cerrar R05, GradeOps AI demuestra el primer ciclo Closed end-to-end: un docente invita estudiantes por links seguros, los estudiantes responden sin cuenta, el sistema califica deterministicamente contra el snapshot de R04, los resultados se publican con acceso aislado y el docente obtiene item analytics para entender calidad de preguntas y refuerzos. Esta release desbloquea R06 con evidencia real de adoption, completion y uso Closed.

## 45. Prompt ejecutable `/release-*`

Comandos inspeccionados en `.planning/scripts/release.mjs` y `.planning/TUTORIAL/reference.md`:

- `/release-init`
- `/release-new vX.Y.Z -- <purpose>`
- `/release-add vX.Y.Z NNN-slug [NNN-slug ...]`
- `/release-remove vX.Y.Z NNN-slug`
- `/release-status [vX.Y.Z] [--mark-planned|--mark-in-progress|--mark-blocked|--mark-released|--mark-cancelled]`

Prompt operativo para crear la release en el sistema `.releases/` del plugin, con placeholders explicitos porque el Master Plan no fija version semantica, target period, fecha estimada ni planning ID operativo para R05:

```text
Contexto:
Estamos ejecutando R05 del Master Plan: Closed Student Response and Item Analytics.
Fuentes obligatorias:
- docs/master-plan/releases/release-05-closed-response-item-analytics.md
- docs/master-plan/master-plan-executive.md
- docs/master-plan/analysis/release-strategy.md
- docs/master-plan/analysis/automation-inventory.md
- docs/master-plan/analysis/user-story-inventory.md
- docs/02-product/workflows.md
- docs/02-product/user-stories/epic-13-student-invitation-access/
- docs/03-ai-agents/item-analytics-agent.md
- docs/master-plan/releases/release-04-closed-question-bank-snapshot.md

Precondiciones:
- R04 debe estar cerrada o existir evidencia aceptada de closed assessment snapshot, answer key, scoring policy y grade scale.
- No inventar planning IDs. Si no existe planning operativa para R05, crearla primero con el flujo de planning vigente.
- Ejecutar /us-enrich sobre las US de R05 antes de atomizar tareas.

Objetivo:
Crear y gestionar la release operativa R05: learner list -> signed links -> student response -> deterministic grading -> result access -> item analytics.

Comandos:
1. Si .releases/ no existe:
   /release-init
2. Crear la release:
   /release-new <VERSION> -- Closed Student Response and Item Analytics --target <YYYY-QN-MN-WN> --date <YYYY-MM-DD>
3. Agregar planning(s) existentes de R05, despues de verificar que existen:
   /release-add <VERSION> <PLANNING_ID_R05>
4. Revisar estado:
   /release-status <VERSION>

Alcance:
- US-120, US-121, US-122, US-123, US-124.
- AUT-14, AUT-15, AUT-19, AUT-16, AUT-17, AUT-21.
- US-PROPUESTA-02 para privacidad/eliminacion.
- US-PROPUESTA-03 para fallos visibles.

Exclusiones:
- Annul/recalculate, OCR/OMR, student account, proctoring, adaptive ordering, LMS integration y dashboard completo.

Arquitectura:
- Teacher flows usan auth y ownership.
- Student flows usan token validado server-side.
- Grading deterministico usa snapshot frozen, no live bank.
- Item Analytics separa agregados deterministas de interpretacion IA.
- Result access se aisla por learner.

Seguridad:
- Hash/proteger tokens.
- Expiration, revocation y used state obligatorios.
- No cross-student result access.
- No tokens en claro en logs.
- Minimizar LearnerRef PII.

Automatizacion:
- Links, validation, attempt capture y scoring son deterministas/automatizados.
- Item analytics es asistida/supervisada.
- Results publication queda bajo control docente.

Trazabilidad:
- Si US-PROPUESTA-02/03 no existen, registrarlas como prerequisito de R05.
- Si aparece contradiccion de docs vs codigo, registrarla y no resolverla silenciosamente.
- No implementar R06/R08 dentro de esta release.

Metricas:
- closed attempts completed.
- deterministic grading success.
- result links opened.
- item analytics generated.
- delivery failure rate.
- access denial rate.

Criterios:
- El flujo es demostrable de extremo a extremo.
- Tests y smoke local documentados.
- README/planning/release status actualizados.
```

## 46. Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Incorporacion de i18n por release funcional | Alinear R05 con student-facing screens/resultados/analytics en locale y audit tecnico estable | UI/API/student routes, DoD operativo, validacion | D-I18N-01..D-I18N-10 |
| 2026-07-21 | Incorporacion de UI Design/Data Semantics | Alinear R05 con controles DS y semantica de datos para learner links, attempts, results y analytics | UI, DoD operativo, validacion | D-UI-01..D-UI-08 |
| 2026-07-21 | Incorporacion de API I/O y sync/async contract | Alinear R05 con datos de pantalla respaldados por `api/` y completion model para analytics/delivery async | UI/API, DoD operativo, validacion | D-UI-01..D-UI-08, D-API-01..D-API-10 |
| 2026-07-21 | Incorporacion de Testing & Quality Gates | Alinear R05 con student-link Playwright, pruebas negativas de token, deterministic grading, Compose Closed y JMeter smoke seguro | Testing, CI/testkit, DoD operativo | D-TEST-01..D-TEST-09 |
| 2026-07-21 | Incorporacion de Observability & Telemetry | Alinear R05 con trazabilidad de student access, asincronia, attempts, scoring e item analytics | Observabilidad, DoD operativo | D-OBS-01..D-OBS-08 |
| 2026-07-21 | Incorporacion de Security & Authorization | Alinear R05 con signed links seguros, anti-enumeracion, result access scoped, replay/tamper tests y rate limiting | Seguridad, DoD operativo | D-SEC-01..D-SEC-08 |
| 2026-07-20 | Incorporacion de capacidades de Agent Runtime | Declarar persistencia/asincronia solo donde analytics o volumen lo justifiquen | Runtime, Item Analytics, student flow | D-04, D-06 |
| 2026-07-20 | Incorporacion de API-Agent Orchestration | Ubicar asincronia durable, polling y retry selectivo dentro del flujo Closed funcional | API, agents, web/student routes, DoD operativo | D-API-01..D-API-10 |
| 2026-07-20 | Creacion inicial | Ejecucion de Fase 05 para R05 | Todo el documento | D-02 |
