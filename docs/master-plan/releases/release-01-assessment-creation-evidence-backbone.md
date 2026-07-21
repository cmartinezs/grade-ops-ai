# Release 01 — Assessment Creation + Evidence Backbone

## 1. Identificacion

| Campo | Valor |
|---|---|
| Release | R01 |
| Nombre | Assessment Creation + Evidence Backbone |
| Archivo | `docs/master-plan/releases/release-01-assessment-creation-evidence-backbone.md` |
| Estado | Documentada |
| Complejidad | M |
| Corte | MVP / validacion MVP foundation |
| Fuente estrategica | `docs/master-plan/analysis/release-strategy.md` |

## 2. Prevalidacion

| Precondicion | Resultado |
|---|---|
| La release existe en `master-plan-executive.md` | OK |
| Sus US estan asignadas | OK: US-001 a US-015, US-080, US-081 |
| No existen decisiones bloqueantes para definir esta release | OK con condicion: D-01 no bloquea R01; D-04/D-06 deben cerrarse dentro de R01 |
| La release no es XL | OK: M |
| Habilita un flujo vertical | OK: login -> brief -> draft/regeneracion -> revision -> log/costo |
| Dependencias anteriores claras | OK: parte de Epic 01 y Epic 02 ya implementada; web UI y evidencia enriquecida siguen pendientes |

## 3. Objetivo ejecutivo

Consolidar el primer flujo usable de GradeOps AI: un docente autenticado crea un brief de evaluacion, genera o regenera un draft con el Assessment Agent, revisa el resultado y el sistema registra evidencia operacional completa del run.

R01 no busca cerrar el ciclo de calificacion. Su funcion es estabilizar la base que todas las releases posteriores consumen: identidad, assessment creation, versionado, provider/model policy, idempotencia, reintentos, logs y costo estimado.

## 4. Problema

El repositorio ya tiene piezas importantes implementadas, pero la evidencia no esta aun tratada como capacidad transversal de producto. Si las siguientes automatizaciones avanzan sin un backbone consistente de `AgentExecutionLog`, costo y reintentos, cada release posterior tendra que resolver trazabilidad de forma distinta.

## 5. Hipotesis

Si el primer flujo de generacion de assessment produce logs completos, costo estimado, estado de aprobacion y recuperacion de fallos, entonces el resto de agentes puede incorporarse con menor retrabajo y el producto empieza a generar evidencia de validacion MVP desde el primer valor visible.

## 6. Actor beneficiado

- **Teacher**: puede crear y revisar el primer draft de assessment.
- **Operator**: puede verificar que la ejecucion de IA queda registrada con proveedor, modelo, costo, estado y errores.
- **Developer**: puede probar los endpoints de onboarding y assessment creation sin ambiguedad de contratos.

## 7. Valor entregado

- Primer flujo de IA demostrable para un docente.
- Base de evidence logging para todas las futuras automatizaciones.
- Cost tracking inicial por run y assessment.
- Versionado no destructivo para regeneracion.
- Reduccion de riesgo de proveedor/model policy antes de escalar a grading y feedback.

## 8. Nivel de automatizacion

| Proceso | Nivel inicial | Nivel objetivo en R01 |
|---|---|---|
| Generacion de assessment | Asistida | Supervisada con validaciones y aprobacion docente |
| Regeneracion de draft | Asistida | Supervisada con versionado no destructivo |
| Registro de agent runs | Automatizada | Automatizada |
| Estimacion de costo/tokens | Automatizada | Automatizada cuando el proveedor entrega datos; estimada cuando no |
| Reintentos/idempotencia | Parcial | Automatizada en los comandos de generacion/regeneracion |

## 9. Alcance incluido

- Teacher onboarding existente: login, registro, Google sign-in, email verification, sign-out, dashboard, ownership, provisioning y pilot flags.
- Assessment brief intake.
- Assessment draft generation.
- Assessment draft regeneration.
- Draft version history y seleccion de version actual cuando aplique.
- AgentExecutionLog para generacion y regeneracion.
- Provider/model policy visible en log y costo.
- Retry/failure state para errores de agent run.
- Idempotency key o mecanismo equivalente para evitar doble run facturable por doble submit.
- Postman/dev testing artifact si no existe o esta incompleto.

## 10. Exclusiones explicitas

- Rubrica.
- Submission intake.
- Grading suggestion.
- Feedback.
- Learning gaps.
- Teacher report.
- Closed assessment mode.
- Evidence dashboard completo.
- Billing self-serve.
- Deploy definitivo `demo`/GCP como criterio de cierre de esta release.

## 11. Capacidades

| Capacidad | Rol en R01 |
|---|---|
| C1 Identidad y Acceso Docente | Base ya implementada para entrar al workspace y aislar datos. |
| C3 Creacion de Assessment Open | Flujo principal de R01. |
| C13 Evidencia de Ejecucion de Agentes | Backbone obligatorio para generar prueba AI-native. |
| C15 Facturacion y Limites de Plan | Parcial: costo/uso por run, sin limites comerciales completos. |

## 12. Flujo funcional

```text
Teacher signs in
  -> dashboard shows create assessment entry point
  -> teacher creates an assessment brief
  -> API persists the brief before invoking the agent
  -> API calls agents service through agentclient
  -> Assessment Agent generates structured draft
  -> API validates and persists draft version
  -> AgentExecutionLog stores provider/model/tokens/cost/status/retry/error
  -> teacher reviews, edits, accepts, or regenerates with notes
  -> regeneration creates a new version and a separate AgentExecutionLog
```

## 13. User stories incluidas

| US | Titulo | Estado en R01 |
|---|---|---|
| US-001 | Teacher Login | Implementada, fuente de autenticacion |
| US-002 | Assessment Dashboard | Implementada; debe exponer entry point y datos reales de assessments cuando Epic 02 lo complete |
| US-003 | Pilot Account Flag | Implementada; operator access sigue como gap para R06 |
| US-004 | Sign-Out and Session Expiry | Implementada |
| US-005 | Dashboard Empty State | Implementada |
| US-006 | Teacher Account Provisioning | Implementada; operator access sigue como gap |
| US-007 | Cross-Teacher Access Denial | Implementada; patron de ownership requerido |
| US-008 | Teacher Self-Registration | Implementada |
| US-009 | Email Verification | Implementada |
| US-010 | Assessment Brief Intake | Implementada en `api/`; UI pendiente de cierre en `web/` |
| US-011 | Assessment Draft Generation | Implementada en `api/`/`agents`; UI y evidencia enriquecida deben verificarse |
| US-012 | Assessment Draft Regeneration | En progreso; debe cerrar versionado visible y log separado |
| US-013 | Postman Collection | READY; verificar si existe antes de incluir como tarea |
| US-014 | Google Sign-In for Teachers | Implementada |
| US-015 | Password Recovery | Implementada con discrepancias de DoD pendientes |
| US-080 | Agent Execution Log | Esqueletica; R01 debe enriquecer y materializar corte minimo |
| US-081 | Cost Estimate Per Run | Esqueletica; R01 debe enriquecer y materializar corte minimo |

## 14. Historias propuestas o modificadas

| ID | Nombre | Uso en R01 |
|---|---|---|
| US-PROPUESTA-03 | Agent Run Retry and Failure Recovery | Corte minimo: teacher ve retry/cancel/error cuando falla generation/regeneration. |
| US-PROPUESTA-04 | Idempotent Assessment/Question Generation Requests | Corte minimo: generation/regeneration no duplica runs por doble submit. |
| US-PROPUESTA-07 | AI Provider Transparency and Fallback Notice | Corte minimo: logs guardan provider/model y la UI/admin puede distinguir proveedor usado. |

Estas propuestas no se crean como archivos de user story dentro de esta release documentada. Deben enriquecerse o incorporarse cuando se atomice la planning de implementacion.

## 15. Consideraciones adicionales para las US

- US-080 y US-081 son P0 pero estan esqueléticas. Deben pasar por `/us-enrich` antes de implementar si la planning necesita tareas ejecutables.
- US-015 tiene dos discrepancias documentadas en su DoD: metodo HTTP y granularidad de errores. R01 debe decidir si las corrige o si las deja como deuda explicitamente excluida del release candidate.
- US-003/US-006 mencionan operator access indefinido. No bloquea R01 si el operator evidence se limita a inspeccion interna o developer/admin temporal, pero debe quedar como riesgo para R06.
- US-013 debe verificar si ya existe `docs/postman/` o equivalente antes de planificar trabajo nuevo.

## 16. Reglas de negocio

- El brief se persiste antes de invocar al agente.
- Cada generacion o regeneracion produce un run independiente.
- La regeneracion no sobreescribe versiones previas.
- El draft no queda finalizado/publicado sin accion docente.
- Todo agent run debe registrar exito, fallo o cancelacion.
- El costo debe quedar asociado, como minimo, a assessment y customer/teacher cuando exista.
- Los datos de proveedor/modelo se guardan como dimensiones, no como texto libre sin estructura.

## 17. Dependencias

| Dependencia | Estado | Accion en R01 |
|---|---|---|
| Epic 01 auth/onboarding | Implementada | Usar como base; no reabrir salvo discrepancias US-015 |
| `api/.planning/finished/003-assessment-creation` | DONE | Usar como fuente de verdad para API |
| `agents/.planning/active/001-assessment-creation` | Deepening, pero reportado como contract work listo por parent planning | Verificar estado real antes de cerrar R01 |
| `web/.planning/active/001-assessment-creation` | Deepening, no atomizada | Cerrar UI o marcar alcance residual |
| `009-groq-infra-provisioning` | Completed | Usar como evidencia de Groq infra; no asumir `demo` aplicado |
| D-04 | Pendiente | Formalizar provider policy o documentar decision tecnica minima |
| D-06 | Pendiente | Adoptar esquema rico de AgentExecutionLog |

## 18. Integraciones

- Firebase Authentication / Identity Platform.
- `web/` Next.js teacher workspace.
- `api/` Spring Boot assessment endpoints.
- `agents/` Spring Boot Assessment Agent.
- `api` -> `agents` via `agentclient`.
- PostgreSQL/Flyway for assessment, brief, draft and agent logs.
- Provider LLM actual: Groq default en codigo; Gemini/GCP pendiente de reconciliacion.

## 19. Arquitectura minima necesaria

- `web/` solo consume API; no llama modelos directamente.
- `api/` conserva ownership y persistencia de agregados.
- `agentclient` es el unico modulo de `api/` que invoca `agents/`.
- `agents/` valida command, construye prompt/version, llama provider y devuelve output estructurado.
- `AgentExecutionLog` se persiste en `api/` como evidencia de producto, no solo log tecnico.
- Idempotencia se implementa en el borde de comando de generacion/regeneracion.

## 20. Datos y migraciones

Entidades/datos esperados:

- `Assessment`.
- `AssessmentBrief`.
- `AssessmentDraft`.
- `AgentExecutionLog`.
- Teacher/account/pilot flags ya existentes.

Campos minimos para `AgentExecutionLog` en R01:

- `agent_run_id` o identificador equivalente.
- `request_id`.
- `assessment_id`.
- `teacher_id` y/o customer/organization cuando exista.
- `agent_name`, `agent_version`.
- `provider`, `model`, `model_policy`.
- `started_at`, `completed_at`, `status`.
- `input_token_estimate`, `output_token_estimate`, `estimated_cost_usd`.
- `input_summary`, `output_summary`.
- `error_code`, `retry_count`.
- `requires_teacher_approval`, `teacher_approval_state`.

## 21. Seguridad y privacidad

- Aplicar [`security-strategy.md`](../analysis/security-strategy.md) a identidad docente, assessment endpoints, operaciones IA, logs y rutas web.
- Auth server-side con Firebase ID token.
- Crear o dejar planificada dentro de R01 la fundacion de cuenta/roles (`TEACHER`, `AuthenticatedAccount`, permissions base) sin bloquear por Operator completo.
- Ownership server-side en todos los endpoints de assessment.
- Actor auditado derivado del principal autenticado, nunca de un campo enviado por `web`.
- Internal auth entre `api` y `agents` segun la decision vigente; si sigue shared-secret, documentarlo como decision MVP y residual post-MVP.
- Declarar ambiente objetivo de R01 (`demo`, `beta` o ambos) y aplicar el contrato multiambiente de [`security-strategy.md`](../analysis/security-strategy.md).
- En `demo`, `api` -> `agents` debe migrar hacia Cloud Run privado + OIDC/IAM; en `beta`, hacia JWT interno firmado/rotatorio con audience, `env`, capability, `jti` y `kid`.
- El secreto interno legacy solo puede quedar como compatibilidad transitoria; `demo` y `beta` deben fallar startup si conservan `change-me-in-production`.
- `agents` debe fallar cerrado ante provider/model no permitido, payload fuera de limites o comando sin policy minima.
- `web` no persiste ID tokens en `localStorage`, logs, analytics ni estado durable.
- No exponer API keys ni prompts internos al frontend.
- No incluir PII innecesaria en summaries de logs.
- Logs de error deben evitar payloads completos sensibles.

## 22. Observabilidad y auditoria

- Aplicar [`observability-strategy.md`](../analysis/observability-strategy.md) a la cadena `web -> api -> agents -> LLM` de generation/regeneration.
- Definir ADR/backlog inicial de OpenTelemetry, destino OTel de `beta`, taxonomia de IDs/eventos/errores, redaccion, retencion y cardinalidad.
- Emitir JSON estructurado a stdout en `api` y `agents`; no depender de archivos rotativos como evidencia operacional en Cloud Run/Render.
- Propagar `traceparent`, `X-Request-Id` y `X-Correlation-Id` sin crear una correlacion aislada entre `api` y `agents`.
- `agent_run_started`, `agent_run_completed`, `agent_run_failed`.
- `assessment_created`.
- `assessment_draft_generated`.
- `assessment_draft_regenerated`.
- `assessment_draft_edited` si ya existe.
- `provider`, `model`, `status`, `retry_count`, `error_code`.
- Correlation/request ID entre `api` y `agents`.
- Dashboard o vista interna minima para inspeccionar run/costo, aunque R06 agregue el dashboard completo.

## 23. Automatizaciones

| ID | Proceso | Uso en R01 |
|---|---|---|
| AUT-01 | Generacion de assessment open | Proceso principal |
| AUT-02 | Regeneracion versionada | Proceso principal P1 |
| AUT-16 | Registro de ejecuciones | Obligatorio |
| AUT-17 | Estimacion de tokens/costo | Obligatorio |
| AUT-21 | Reintentos/idempotencia/fallos | Obligatorio en corte minimo |

## Capacidades de IA y Agent Runtime

### Agentes involucrados

- Assessment Agent actual, manteniendo el endpoint especifico `POST /internal/agents/assessment`.
- No incorporar Router Agent ni multiagente en R01.

### Capacidades funcionales habilitadas

- Generar y regenerar un draft desde brief docente.
- Registrar evidencia tecnica suficiente para que el run sea auditable y reutilizable por R02-R06.
- Declarar fallos recuperables sin perder el brief ni activar versiones parciales.

### Incrementos del runtime requeridos

- Baseline documentado del Assessment Agent existente.
- Catalogo validado de provider/model para Groq y Gemini; no reenviar modelos arbitrarios sin politica centralizada.
- Prompt, esquema de salida, agente y configuracion de modelo versionados en el log.
- Error model normalizado para `INVALID_COMMAND`, provider failure, malformed output, timeout y budget/cost missing.
- `AgentExecutionLog` enriquecido o decision tecnica D-06 que adopte explicitamente el esquema rico minimo.
- Idempotency key por generacion/regeneracion.
- Limites efectivos de timeout, tokens/costo estimado y reintentos para este flujo.

### Incremento API-Agent Orchestration requerido

- Aplicar [`api-agent-orchestration-strategy.md`](../analysis/api-agent-orchestration-strategy.md) al corte Assessment Creation, sin crear una release tecnica separada.
- Introducir `AiOperation`, `AgentRun` y `AgentAttempt` minimos para generation/regeneration, aun si la ejecucion sigue siendo sincrona.
- Generar IDs de operacion/run en `api/` antes de llamar a `agents/`; `agents/` devuelve evidencia tecnica, no decide efectos de dominio.
- Exigir o admitir `Idempotency-Key` en comandos GenAI mutantes y devolver la operacion existente cuando el mismo request se reintenta.
- Exponer una ruta consultable de operacion para `web/` cuando la generacion no pueda tratarse como resultado inmediato.
- Someter todo endpoint/ruta nueva al gate Richardson REST: recurso claro, metodo correcto, status code, `Location` si aplica, errores normalizados, links de estado/transicion y contract tests.

### Herramientas requeridas

R01 no necesita tool calling generico. Las "herramientas" siguen siendo deterministicas y orquestadas por `api/`:

- cargar y validar `AssessmentBrief`;
- cargar draft actual para regeneracion;
- persistir draft/version/log;
- verificar ownership y estado.

### Validadores determinísticos

- Campos obligatorios del brief.
- Consistencia de regeneracion: `adjustmentNotes`, `previousDraftId` y `previousDraft` completos.
- Esquema estructurado del draft.
- Version monotona y no destructiva.
- Provider/model permitido por politica.
- Costo marcado como estimado o `missing` con razon, nunca inventado.

### Autonomía y controles humanos

- Autonomia: `DRAFT_ONLY`.
- El agente puede proponer contenido, pero no publica assessment, no aprueba rubrica y no habilita grading.
- Teacher decide editar, aceptar o regenerar.

### Límites operacionales

- Una llamada o pocos reintentos controlados; no bucle agentic completo.
- Sin sandbox, sin herramientas de escritura del dominio dentro de `agents/`, sin memoria vectorial.
- Cualquier error no recuperable termina en estado visible y logueado.

### Métricas y consumo

- Provider, model, prompt version, agent version.
- Input/output tokens o estimacion/missing reason.
- Estimated cost USD.
- Latencia y retry count.
- Status, error code y correlation/request ID.

### Evidencia de finalización

- Smoke real `api` -> `agents` con draft y log persistidos.
- Tests de provider/model policy y errores normalizados.
- Tests de idempotencia o evidencia reproducible de doble submit.
- Documento o ADR minimo que cierre D-04 y D-06 o deje residual aceptado.

### Deuda o capacidades diferidas

- `AgentDefinition`, `AgentRegistry` y `AgentModelGateway` comun se difieren hasta R02, cuando exista segundo consumidor real.
- Tool loop, `AgentAction`, `ToolRegistry` y `PolicyEngine` se difieren hasta que Assessment contextual, Rubric/quality o Closed lo requieran.

## 24. Trigger, inputs y outputs

| Proceso | Trigger | Inputs | Outputs |
|---|---|---|---|
| Brief intake | Teacher submits form | learning goal, topic, level, language, duration, constraints | persisted brief |
| Draft generation | Teacher clicks generate | brief ID, teacher/account, provider policy | draft version, agent log |
| Draft regeneration | Teacher submits adjustment notes | current draft, original brief, notes | new draft version, new agent log |
| Failure recovery | Agent timeout/output/provider error | request ID, stage, error | visible error, retry option, failed log |
| Cost estimation | Run completed/failed | token counts or estimates, provider/model | estimated cost per run |

## 25. Human in the loop

- Teacher revisa el draft antes de usarlo.
- Teacher puede editar o regenerar.
- Teacher decide que version queda activa.
- Operator/developer puede revisar logs y costos, pero no modifica outputs pedagogicos.

## 26. Guardrails

- No publicar assessment desde R01.
- No generar rubrica en esta release.
- No calificar ni feedback.
- No premium fallback silencioso.
- No duplicar agent runs por doble submit.
- No ocultar fallos de proveedor.
- No guardar prompts completos en evidencia visible.

## 27. Idempotencia

R01 debe definir una key estable para:

- initial generation: `assessment_id + brief_version + generation_request_id`;
- regeneration: `assessment_id + draft_version + adjustment_request_id`;
- log event: `agent_run_id`.

Si el usuario repite la accion por refresh/doble click, el sistema debe devolver el run/version existente o bloquear el duplicado antes de invocar al agente.

## 28. Reintentos y fallos

| Falla | Comportamiento esperado |
|---|---|
| Timeout del agente | Log failed, mostrar retry |
| Provider rate limit/quota | Log provider error, no crear draft activo |
| Output malformado | Log validation failure, permitir regenerar |
| Error de red `api` -> `agents` | Log failed, retry controlado |
| Doble submit | No ejecutar segundo run facturable |

## 29. Reversion

- Una version generada puede quedar no activa sin borrarse.
- Una regeneracion fallida no altera la version vigente.
- Un log incorrecto se corrige con metadata/evento compensatorio, no con borrado silencioso.
- Si se corrige US-015, mantener compatibilidad con flujos existentes o documentar migracion.

## 30. Consumo y costos

R01 debe registrar costo como estimacion, no como billing definitivo.

Campos:

- provider.
- model.
- model_policy.
- input tokens o estimate.
- output tokens o estimate.
- estimated cost USD.
- retry count.
- cost attribution por assessment y teacher/customer.

D-04 debe resolverse para que Groq/Gemini no queden codificados de forma contradictoria.

## 31. Criterios funcionales

- Teacher puede iniciar sesion y llegar al dashboard.
- Teacher puede crear un brief.
- Brief persiste antes de llamar al agente.
- Teacher puede generar draft.
- Teacher puede regenerar con notas.
- Teacher puede ver o recuperar version actual.
- Fallos de generacion no pierden el brief.
- Log y costo quedan disponibles para inspeccion interna.

## 32. Criterios tecnicos

- `web/` usa React Hook Form + Zod para formularios, segun convención vigente.
- `api/` valida ownership y payloads.
- `api/` no importa Spring AI fuera de `agentclient`.
- `agents/` devuelve output estructurado validado.
- Migraciones Flyway cubren los datos necesarios.
- Tests de API/agents/web cubren el flujo principal y errores relevantes.
- Cross-service smoke test prueba `api` -> `agents` real, no solo mocks.

## 33. Criterios de calidad

- Draft legible, editable y alineado a programming assessment MVP.
- Errores claros para el usuario.
- Sin copy que prometa calificacion/feedback aun no implementados.
- US-015 discrepancias cerradas o explicitamente documentadas como residual.
- Sin duplicidad entre provider/model policy y ADRs.

## 34. Criterios de seguridad

- Firebase auth protegida.
- Email verification o bypass Google consistente.
- Internal endpoint protegido.
- Roles/permisos base Teacher quedan persistidos o registrados como residual explicito antes de cerrar R01.
- `@PreAuthorize`/permission base o equivalente queda definido para endpoints de assessment.
- Actor de auditoria no puede ser falsificado desde body/query/header cliente.
- Provider/model no permitido falla cerrado en `agents`.
- El ambiente declarado no mezcla Firebase project, API URL, DB, storage, secrets ni service identity con otro ambiente.
- Token Firebase de otro ambiente y token interno con audience/env/capability incorrecto son rechazados.
- API keys solo server-side.
- Logs sin secretos.
- Ownership denial mantiene el patron 404 cuando corresponde.
- Pruebas negativas cubren usuario no autenticado, email no verificado/bypass invalido, ownership cruzado, idempotency conflict y aislamiento cruzado de ambiente.

## 35. Criterios de observabilidad

- 100% de generation/regeneration agent runs tienen log.
- El journey generation/regeneration puede reconstruirse por `trace_id`/`correlation_id`.
- `AiOperation`, `AgentRun` y `AgentAttempt` comparten operation/run/attempt IDs con logs, spans y eventos canonicos.
- Las senales incluyen `service.name`, `service.version`, `deployment.environment` y plataforma.
- Failed runs tambien se loguean.
- `retry_count` y `error_code` estan presentes.
- Cost estimate existe o queda marcado como missing con razon.
- Correlation ID permite seguir request entre web/api/agents.
- No se usan IDs de usuario, assessment, operation, run o submission como labels de metricas.
- Pruebas verifican redaccion de prompts, tokens, secrets, signed links y PII.

## 36. Criterios de despliegue

- Debe correr en entorno local integrado.
- Debe sostener smoke test contra `beta` si se usa como evidencia real.
- No requiere resolver `demo` GCP para cerrar R01.
- Si se usa `demo`, documentar que Terraform apply real sigue pendiente o completado con evidencia.

## 37. Criterios de negocio

- Primer costo estimado por assessment draft.
- Primer evidence record reutilizable para dashboard R06.
- Pilot flags disponibles para asociar runs a cuentas piloto cuando aplique.
- No prometer pricing final hasta resolver D-07.

## 38. Definition of Done

- [ ] US-010/011/012 verificadas de extremo a extremo incluyendo UI.
- [ ] `AgentExecutionLog` registra el esquema minimo de R01.
- [ ] Costo estimado visible o exportable internamente.
- [ ] Provider/model policy reconciliada con D-04.
- [ ] D-06 resuelta o el esquema rico adoptado como decision tecnica de R01.
- [ ] Idempotencia y retry cubiertos por tests o evidencia manual reproducible.
- [ ] Cross-service smoke test real documentado.
- [ ] US-015 discrepancias cerradas o registradas como residual aceptado.
- [ ] README/planning/release artifacts actualizados.

## 39. Validacion

Validaciones esperadas:

- Unit tests de `api` para commands, handlers, persistence mappers y ownership.
- Integration tests de `api` con adapters reales para brief/draft/log.
- Tests de `agents` para command validation, prompt/template y structured output parsing.
- Tests de `web` para intake, draft view, regenerate action y errores.
- Smoke local con `api` y `agents` reales.
- Smoke `beta` si el entorno esta disponible.

## 40. Escenario Given/When/Then

```gherkin
Given a verified teacher is signed in
And the teacher opens the assessment creation flow
When the teacher submits a valid programming assessment brief
And requests AI draft generation
Then the brief is persisted before the agent call
And the Assessment Agent returns a structured draft
And the draft is stored as the current version
And an AgentExecutionLog records provider, model, status, timestamps, retry count, and estimated cost
And the teacher can review, edit, or regenerate the draft without losing previous versions
```

## 41. Metricas

- Time to first assessment.
- Assessment draft generation success rate.
- Regeneration rate.
- Agent log coverage.
- Cost tracking coverage.
- Retry rate.
- Failed run rate.
- Draft accepted rate.

## 42. Evidencias

- Screenshot o export de draft generado.
- `AgentExecutionLog` de generation.
- `AgentExecutionLog` de regeneration.
- Evidencia de costo estimado.
- Smoke test local o beta.
- Test output relevante.
- Enlace a planning y PRs cerrados.

## 43. Riesgos y mitigaciones

| Riesgo | Mitigacion |
|---|---|
| Groq default sin ADR | Resolver D-04 o documentar provider policy como decision de R01. |
| Agent log subdimensionado | Adoptar esquema rico de `automation-inventory.md` y cerrar D-06. |
| Web UI no cerrada | Mantener R01 abierta hasta cerrar `web/.planning/active/001-assessment-creation`. |
| Cross-service test con mocks | Exigir smoke real `api` -> `agents`. |
| Doble submit genera doble costo | Idempotency key obligatoria. |
| US-015 discrepancias contaminan cierre | Resolver o registrar residual aceptado antes de Done. |

## 44. Resultado esperado

Al cerrar R01, GradeOps AI tiene un primer flujo AI-native real y demostrable: un docente crea un assessment draft desde un brief, puede regenerarlo, y cada ejecucion queda trazada con costo y estado. Esta release desbloquea R02 porque ya existe la base de assessment, evidencia y costo que grading/feedback deben reutilizar.

## 45. Prompt ejecutable `/release-*`

Comandos inspeccionados en `.planning/scripts/release.mjs` y `.planning/TUTORIAL/reference.md`:

- `/release-init`
- `/release-new vX.Y.Z -- <purpose>`
- `/release-add vX.Y.Z NNN-slug [NNN-slug ...]`
- `/release-remove vX.Y.Z NNN-slug`
- `/release-status [vX.Y.Z] [--mark-planned|--mark-in-progress|--mark-blocked|--mark-released|--mark-cancelled]`

Prompt operativo para crear la release en el sistema `.releases/` del plugin, con placeholders explicitos porque el Master Plan no fija version semantica, target period ni fecha estimada:

```text
Contexto:
Estamos ejecutando R01 del Master Plan: Assessment Creation + Evidence Backbone.
Fuentes obligatorias:
- docs/master-plan/releases/release-01-assessment-creation-evidence-backbone.md
- docs/master-plan/master-plan-executive.md
- docs/master-plan/analysis/release-strategy.md
- docs/master-plan/analysis/automation-inventory.md
- docs/master-plan/analysis/user-story-inventory.md
- docs/02-product/user-stories/epic-01-teacher-onboarding/
- docs/02-product/user-stories/epic-02-assessment-creation/
- docs/02-product/user-stories/epic-09-evidence-metrics/01-agent-execution-log.md
- docs/02-product/user-stories/epic-09-evidence-metrics/02-cost-estimate-per-run.md

Objetivo:
Crear y gestionar la release operativa R01: login -> brief -> draft/regeneracion -> log/costo/idempotencia.

Comandos:
1. Si .releases/ no existe:
   /release-init
2. Crear la release:
   /release-new <VERSION> -- Assessment Creation + Evidence Backbone --target <YYYY-QN-MN-WN> --date <YYYY-MM-DD>
3. Agregar plannings existentes relacionados:
   /release-add <VERSION> 001-teacher-onboarding 002-google-sign-in 007-password-recovery-custom-email 008-assessment-creation 009-groq-infra-provisioning
4. Revisar estado:
   /release-status <VERSION>

Alcance:
- US-001 a US-015, US-080, US-081.
- AUT-01, AUT-02, AUT-16, AUT-17, AUT-21.
- Cortes minimos de US-PROPUESTA-03, US-PROPUESTA-04, US-PROPUESTA-07.

Exclusiones:
- Rubrica, submissions, grading, feedback, Closed mode, dashboard completo de negocio y billing self-serve.

Arquitectura:
- web -> api -> agents.
- agentclient es el unico caller de agents desde api.
- Provider/model policy debe quedar estructurada.
- No exponer provider API keys al frontend.

Seguridad:
- Mantener Firebase auth y ownership server-side.
- Internal auth api-agents vigente, documentando residual si sigue shared-secret.
- No prompts ni secretos en logs visibles.

Automatizacion:
- Assessment generation/regeneration asistida.
- Logs, costo, retry e idempotencia automatizados.
- Sin autonomia pedagogica ni publicacion automatica.

Trazabilidad:
- Registrar decisiones D-04 y D-06 si se resuelven.
- Si aparece contradiccion de docs vs codigo, registrarla y no resolverla silenciosamente.
- No implementar releases posteriores.

Metricas:
- time to first assessment.
- agent log coverage.
- cost tracking coverage.
- retry rate.
- failed run rate.

Criterios:
- El flujo es demostrable.
- Tests y smoke real api -> agents documentados.
- README/planning/release status actualizados.
```

## 46. Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Incorporacion de Observability & Telemetry | Exigir que R01 deje el primer journey IA trazable, con JSON stdout, OTel/W3C propagation y evidencia durable | Observabilidad, DoD operativo | D-OBS-01..D-OBS-08 |
| 2026-07-21 | Incorporacion de topologia de seguridad multiambiente | Exigir que la fundacion de R01 declare ambiente, aisle identidad/datos/secrets y reemplace el shared-secret segun `demo`/`beta` | Seguridad, service-to-service, DoD operativo | D-SEC-01..D-SEC-08 |
| 2026-07-21 | Incorporacion de Security & Authorization | Alinear R01 con fundaciones de identidad, permisos, ownership, audit actor y fail-secure sin crear release tecnica | Seguridad, DoD operativo | D-SEC-01..D-SEC-08 |
| 2026-07-20 | Incorporacion de capacidades de Agent Runtime | Alinear R01 con la estrategia headless/iterativa sin anticipar tool loop completo | Runtime, automatizacion, DoD operativo | D-04, D-06 |
| 2026-07-20 | Incorporacion de API-Agent Orchestration | Hacer que R01 consolide assessment generation como flujo API robusto con operaciones, runs, attempts, idempotencia y gate REST | API, agents, web routes, DoD operativo | D-API-01..D-API-10 |
| 2026-07-19 | Creacion inicial | Ejecucion de Fase 05 para R01 | Todo el documento | D-04, D-06 |
