# Release 06 - Business Evidence and Hackathon Compliance

> Bloqueo de cierre: R06 puede documentarse y ejecutarse en paralelo, pero no puede marcarse como release candidate del hackathon hasta resolver D-01. La narrativa final tampoco puede cerrarse hasta resolver D-07.

## 1. Identificacion

| Campo | Valor |
|---|---|
| Release | R06 |
| Nombre | Business Evidence and Hackathon Compliance |
| Archivo | `docs/master-plan/releases/release-06-business-evidence-hackathon-compliance.md` |
| Estado | Documentada; ejecucion condicionada por D-01 y D-07 |
| Complejidad | M |
| Corte | MVP / cierre operativo y de evidencia del hackathon |
| Fuente estrategica | `docs/master-plan/analysis/release-strategy.md` |

## 2. Prevalidacion

| Precondicion | Resultado |
|---|---|
| La release existe en `master-plan-executive.md` | OK |
| Sus US estan asignadas | OK: US-082, US-090, US-091; cuatro US propuestas requeridas |
| No existen decisiones bloqueantes | Condicion: D-01 bloquea el cierre como release candidate; D-07 bloquea la narrativa final |
| La release no es XL | OK si se limita a dashboard interno, ledgers, checklist y paquete de evidencia |
| Habilita un flujo vertical | OK: operator revisa pilotos, uso, costos, revenue y cumplimiento, luego exporta un paquete validado |
| Dependencias anteriores claras | OK: evidencia acumulada desde R01 y outputs de R02, R03 y R05 |

R06 no esta bloqueada para instrumentar ledgers, dashboard, alertas, accesos y recopilacion de evidencia. Si D-01 o D-07 siguen pendientes al intentar marcarla `RELEASED`, debe pasar a `BLOCKED` y no sustituir la decision con supuestos silenciosos.

## 3. Objetivo ejecutivo

Consolidar evidencia real de operacion, usuarios, pilotos, uso, costos, revenue, related-party y cumplimiento tecnico en un dashboard interno y un paquete exportable, privado por defecto y validado por un Operator.

R06 convierte los eventos producidos desde R01-R05 en prueba auditable de producto, operacion AI-native y viabilidad comercial. Tambien verifica que la submission use una narrativa de pricing coherente y evidencia de despliegue compatible con la decision D-01.

## 4. Problema

Un producto funcional puede perder credibilidad o elegibilidad si sus afirmaciones de uso, revenue, costos, IA y despliegue no se pueden rastrear hasta registros reales. La evidencia esta distribuida entre logs, tablas, documentos, consolas cloud y enlaces privados, mientras el acceso Operator, los alertas de costo, la salud del servicio y el onboarding de pilotos aun no tienen historias completas.

## 5. Hipotesis

Si un Operator puede revisar datos reales, detectar faltantes, separar informacion privada y publica, corregir mediante eventos auditados y generar un paquete versionado, entonces GradeOps AI puede preparar una submission consistente sin fabricar traction, exponer PII ni reconstruir evidencia manualmente al final.

## 6. Actor beneficiado

- **Operator**: administra pilotos, ledgers, alertas, evidencia y readiness.
- **Founder/business owner**: entiende revenue, costos, uso y unit economics.
- **Teacher/pilot customer**: aporta evidencia y consentimiento con visibilidad controlada.
- **Judges/testers**: reciben un paquete verificable, coherente y minimizado.
- **Developer/Release Manager**: valida despliegue, logs, export y trazabilidad al commit.

## 7. Valor entregado

- Dashboard interno con metricas reales de producto, agentes, aprobaciones y negocio.
- Usage ledger comparable con limites de plan.
- Revenue, cost y customer evidence ledgers auditables.
- Separacion obligatoria de revenue arms-length y related-party.
- Separacion de cash cost, credit-covered cost y allocated tooling cost.
- Alertas de presupuesto, uso y datos faltantes.
- Visibilidad de salud del runtime de agentes.
- Checklist operativo por piloto.
- Export versionado con clasificacion private, judge-verifiable y public-safe.
- Matriz de cumplimiento y evidencia de despliegue para la submission.

## 8. Nivel de automatizacion

| Proceso | Nivel inicial | Nivel objetivo en R06 |
|---|---|---|
| Agent/usage/cost ingestion | Parcial | Automatizada y determinista |
| Usage vs plan | Parcial | Automatizada con alertas |
| Revenue/customer evidence | Manual | Supervisada por Operator |
| Business evidence dashboard | No implementado | Supervisada |
| Missing evidence detection | Manual | Automatizada con resolucion humana |
| Pilot onboarding | Manual | Supervisada con checklist |
| Agent health visibility | Tecnica | Automatizada para lectura y alertas |
| Evidence export | Manual | Supervisada, versionada y revocable |
| Submission readiness | Manual | Determinista para checklist; decision humana final |

## 9. Alcance incluido

- Acceso autenticado y autorizado para el rol Operator o mecanismo interno temporal explicitamente documentado.
- Dashboard interno con assessments, submissions/attempts, feedback outputs, reports, agent runs, approvals y usuarios/pilotos.
- Success, failure y retry rate de agentes por provider, model y workflow stage.
- UsageEvent para assessments creados y graded submissions consumidas.
- Comparacion de uso contra plan y reporte de overuse.
- Alertas de uso, tokens y costo contra umbrales configurables.
- RevenueEvent con paid, commitment y manual como estados distintos.
- Related-party obligatorio y visible por RevenueEvent.
- CostEvent con categoria, monto, cash cost, credits y evidence link.
- CustomerEvidence/PilotEvidence con estado, permiso y enlaces.
- Checklist de kickoff y seguimiento de piloto.
- Health status del servicio de agentes y ultima comprobacion.
- Missing evidence warnings.
- Clasificacion de evidencia como internal-private, judge-verifiable o public-safe.
- Export versionado con resumen, manifest y referencias a fuentes.
- Matriz de readiness de submission.
- Evidencia de entorno y uso de GCP/Gemini segun D-01.
- Reconciliacion de pricing y limites segun D-07.
- Tarea de infraestructura para verificar o ajustar `infra/terraform/environments/demo/`.

## 10. Exclusiones explicitas

- Automated payment processing.
- Invoice generation y tax accounting.
- Self-serve plan upgrade/downgrade.
- Marketplace.
- Public BI suite o analytics en tiempo real.
- CRM completo.
- Marketing automation.
- Publicacion automatica en Devpost o redes.
- Fabricacion o estimacion de usuarios/revenue como si fueran hechos.
- Almacenamiento de secretos, recibos o PII dentro del export publico.
- Reescritura de los flujos Open/Closed de R01-R05.
- Implementacion de refinamientos R07/R08.

## 11. Capacidades

| Capacidad | Rol en R06 |
|---|---|
| C14 Dashboard de evidencia | Capacidad principal de consulta, faltantes y export. |
| C15 Facturacion y limites | Usage, revenue, cost, plan limits y unit economics. |
| C13 Evidencia de agentes | Fuente de agent runs, provider/model, tokens, costos, retries y approvals. |
| C1 Identidad y acceso parcial | Acceso Operator y atribucion de customer/pilot. |

## 12. Flujo funcional

```text
Operator signs in with authorized role
  -> selects the hackathon reporting period
  -> dashboard loads product, agent, approval, usage, cost and customer aggregates
  -> system flags missing cost, revenue proof, related-party, consent and deployment evidence
  -> operator reviews pilot checklist and attaches controlled evidence references
  -> operator records or corrects revenue/cost metadata through auditable events
  -> system compares usage with plan limits and evaluates budget alerts
  -> operator reviews agent runtime health and run coverage
  -> operator chooses an evidence classification and export scope
  -> system generates a versioned manifest and private/public-safe artifacts
  -> operator validates pricing, environment proof and submission readiness
  -> operator approves the package for manual submission
```

## 13. User stories incluidas

| US | Titulo | Estado en R06 |
|---|---|---|
| US-082 | Business Evidence Dashboard | P0, NOT READY; evaluar division por dashboard, ledgers y export |
| US-090 | Usage Limits | P0, NOT READY; enriquecer contadores, limites y overuse |
| US-091 | Payment Evidence Link | P1 promovida al corte R06 por requisito de evidencia; NOT READY |

US-091 conserva su prioridad documental P1, pero su subconjunto de evidence link, revenue status y `related_party` es obligatorio para el cierre hackathon de R06. Esto no cambia silenciosamente la historia original; el enriquecimiento debe registrar el corte adoptado.

## 14. Historias propuestas o modificadas

| ID | Nombre | Uso en R06 |
|---|---|---|
| US-PROPUESTA-01 | Operator Authentication and Role Access | Requisito de seguridad para operar C14/C15. |
| US-PROPUESTA-05 | Cost/Token Budget Alerting | Requisito para umbrales y alertas proactivas. |
| US-PROPUESTA-06 | Agent Service Health Visibility | Requisito de readiness operacional. |
| US-PROPUESTA-08 | Pilot Kickoff and Onboarding Checklist | Requisito para evidencia comercial completa. |

Estas historias no existen como archivos en `docs/02-product/user-stories/`. Deben crearse o enriquecerse antes de atomizar R06; el documento de release no las convierte por si solo en historias READY.

## 15. Consideraciones adicionales para las US

- Ejecutar `/us-enrich` sobre US-082, US-090 y US-091 antes de atomizar.
- Evaluar dividir US-082 en dashboard, evidence export y missing-evidence readiness si supera M.
- US-090 debe definir el momento transaccional exacto de cada UsageEvent.
- US-091 debe distinguir revenue cobrado, commitment y registro manual; no sumarlos como la misma metrica.
- `related_party` debe ser requerido para todo RevenueEvent, con valor explicito `true` o `false`.
- US-PROPUESTA-01 debe elegir rol real o acceso interno temporal protegido; direct DB access no cuenta como UX final.
- US-PROPUESTA-05 debe definir umbrales, destinatario, cooldown y acknowledge.
- US-PROPUESTA-06 debe mostrar health sin revelar secretos ni detalles explotables.
- US-PROPUESTA-08 debe incluir consentimiento, evidencia, estado, siguiente accion y owner.

## 16. Reglas de negocio

- Toda metrica mostrada debe derivar de datos persistidos y tener periodo de corte.
- No usar mocks como evidencia de traction o revenue.
- Commitments y paid revenue se muestran por separado.
- Revenue arms-length y related-party se muestran por separado.
- `RevenueEvent.related_party` no puede ser nulo.
- Credits/free tiers reducen cash cost, pero no eliminan el costo consumido.
- Cash cost, credit-covered cost, allocated tooling y marketing spend son dimensiones separadas.
- Evidencia externa se referencia; secretos y recibos privados no se copian al export publico.
- Testimonios requieren permiso y nivel de visibilidad.
- Datos estudiantiles se agregan o anonimizan para evidencia de negocio.
- Correcciones de ledger usan eventos compensatorios o invalidacion auditada.
- Ningun resumen IA puede crear o alterar montos, counts o estados fuente.
- Operator aprueba todo export y toda evidencia publica.
- Pricing final debe usar la fuente canonica decidida en D-07.
- Readiness de hackathon requiere D-01 resuelta y evidencia acorde.

## 17. Dependencias

| Dependencia | Estado | Accion en R06 |
|---|---|---|
| R01 Evidence Backbone | Previa obligatoria | Reutilizar AgentExecutionLog, costo e idempotencia |
| R02/R03 Open evidence | Previa para narrativa Open | Agregar submissions, feedback, reports y time saved |
| R04/R05 Closed evidence | Previa si Closed entra al demo | Agregar snapshots, attempts, grading y analytics |
| D-01 demo GCP/Gemini vs beta | Bloqueante de cierre | Resolver y adjuntar deployment/API proof |
| D-04 provider policy | Requerida para costos confiables | Mantener provider/model dinamicos |
| D-06 rich agent log | Requerida | Usar esquema rico, no vista reducida |
| D-07 canonical pricing | Bloqueante de narrativa final | Reconciliar pricing antes del export final |
| US propuestas 01/05/06/08 | No creadas | Crear/enriquecer antes de atomizar |
| Infra demo | Definida, no probada como evidencia final | Crear scope infra y dejarlo DONE antes de cerrar R06 |

## 18. Integraciones

- `web/` para dashboard, alertas, checklists y export controlado.
- `api/` para RBAC, queries, ledgers, agregaciones, manifest y audit trail.
- `agents/` y Ops Agent para resumen opcional y missing-evidence suggestions, nunca como fuente contable.
- PostgreSQL/Flyway para eventos y snapshots de export.
- Cloud Run, Cloud SQL, Cloud Storage/controlled evidence store, Secret Manager y IAM.
- Firebase Authentication/Identity Platform para acceso Operator segun arquitectura vigente.
- Google Cloud billing/API consoles como evidencia externa referenciada.
- Gemini/Groq usage records segun provider policy.
- Payment/bank/invoice/commitment evidence como enlaces privados.

## 19. Arquitectura minima necesaria

- `web/` consulta API; no agrega ledger ni llama modelos directamente.
- `api/` es fuente de verdad para UsageEvent, RevenueEvent, CostEvent y EvidenceExport.
- Ops Agent puede producir summaries y warnings estructurados, pero los totales se calculan deterministicamente.
- Aggregation queries aceptan periodo, tenant/customer y evidence classification.
- Private evidence references se entregan mediante autorizacion y URLs controladas.
- Export crea un manifest inmutable/versionado con source IDs, periodo, commit y environment.
- Health visibility consume endpoints tecnicos acotados y persiste observation timestamp.
- Infra scope verifica que `api`, `agents` y hosting web tengan runtime, images, DB, IAM y secrets necesarios en `demo`.

## 20. Datos y migraciones

Entidades/datos esperados:

- `UsageEvent`.
- `UsageLimit` o `PlanLimitSnapshot`.
- `RevenueEvent`.
- `CostEvent`.
- `CustomerEvidence`.
- `PilotChecklist`.
- `PilotChecklistItem`.
- `EvidenceReference`.
- `EvidenceConsent`.
- `EvidenceClassification`.
- `EvidenceWarning`.
- `BudgetAlert`.
- `ServiceHealthObservation`.
- `EvidenceExport`.
- `EvidenceExportItem`.
- `SubmissionReadinessCheck`.
- `AgentExecutionLog` y `ApprovalEvent` heredados.

Campos minimos de `RevenueEvent`:

- `event_id`, `customer_id`, `occurred_at`, `reporting_month`.
- `status` (`paid`, `commitment`, `manual`).
- `amount_original`, `currency`, `amount_usd`, `conversion_basis`.
- `offer`, `source`, `related_party` obligatorio.
- `evidence_reference_id`, `validity_state`, `created_by`.

Campos minimos de `CostEvent`:

- `event_id`, `occurred_at`, `category`, `amount_usd`.
- `cash_cost`, `covered_by_credit`, `allocated_tooling_cost`.
- `provider`, `model`, `customer_id`, `assessment_id` cuando aplique.
- `evidence_reference_id`, `validity_state`.

Migraciones Flyway deben incluir constraints, indexes por periodo/customer y unicidad de event/idempotency keys. No se permiten cambios manuales no documentados en la base.

## 21. Seguridad y privacidad

- Operator requiere autenticacion y autorizacion server-side.
- Queries y exports respetan tenant/customer scope.
- Evidence links privados no se exponen a teachers, students ni public exports.
- No guardar credenciales cloud, payment tokens o signed URLs permanentes en el ledger.
- Student data se limita a agregados sin nombres, emails, respuestas ni result links.
- Customer/testimonial evidence respeta consentimiento y nivel de permiso.
- Public-safe export usa allowlist de campos, no una blacklist.
- Judge-verifiable evidence se comparte solo por canal controlado y revocable.
- Logs de dashboard/export no contienen el contenido privado del evidence link.
- Health endpoint no revela variables, stack traces, providers keys ni topologia sensible.
- Retencion e invalidacion de evidencia deben quedar auditadas.

## 22. Observabilidad y auditoria

- `usage_event_recorded`.
- `usage_limit_compared`.
- `usage_overage_detected`.
- `cost_event_recorded`, `cost_event_invalidated`.
- `revenue_event_recorded`, `revenue_event_invalidated`.
- `related_party_classification_changed`.
- `pilot_checklist_updated`.
- `evidence_reference_attached`, `evidence_reference_revoked`.
- `evidence_warning_created`, `evidence_warning_acknowledged`.
- `budget_alert_triggered`, `budget_alert_acknowledged`.
- `agent_health_checked`, `agent_health_degraded`.
- `evidence_dashboard_viewed`.
- `evidence_export_requested`, `evidence_export_generated`, `evidence_export_failed`, `evidence_export_revoked`.
- `submission_readiness_evaluated`, `submission_package_approved`.

Cada evento incluye actor, timestamp, correlation/request ID, source entity, previous/new state cuando aplique y environment.

## 23. Automatizaciones

| ID | Proceso | Uso en R06 |
|---|---|---|
| AUT-16 | Registro de ejecuciones | Fuente transversal de evidencia AI-native |
| AUT-17 | Tokens, costo y uso | Cost attribution, usage limits y unit economics |
| AUT-18 | Dashboard de evidencia | Flujo principal de consulta, warnings y export |
| AUT-20 | Pilotos y evidencia de pago | Checklist, customer proof y revenue ledger |
| AUT-21 | Reintentos/idempotencia/fallos | Ingestion, ledger, health y export confiables |

## Capacidades de IA y Agent Runtime

### Agentes involucrados

- Ops Agent read-only/advisory.
- No usar LLM para calcular totales, revenue, costos, readiness o cumplimiento.

### Capacidades funcionales habilitadas

- Explicar anomalias de agent runs, provider errors, costos y coverage.
- Comparar proveedor/modelo y detectar degradaciones operacionales.
- Resumir faltantes de evidencia sin alterar hechos ni ledgers.

### Incrementos del runtime requeridos

- Observabilidad consolidada por agente, provider, modelo, prompt y release.
- Agent health visibility y history.
- Provider/model cost and quality comparison.
- Budget alerts y missing-evidence warnings.
- Export de manifest con source IDs, environment, commit y freshness.
- Policy que mantenga Ops Agent en `EXECUTE_READ_ONLY`.

### Herramientas requeridas

- `load_agent_metrics`.
- `load_provider_errors`.
- `compare_model_costs`.
- `detect_usage_anomalies`.
- `load_prompt_versions`.
- `load_budget_alerts`.
- `load_evidence_readiness`.

### Validadores determinísticos

- Totales de usage, cost y revenue se calculan en API/DB, no por LLM.
- Readiness es una matriz deterministicamente evaluada contra D-01/D-07 y evidencia requerida.
- Related-party no puede ser nulo.
- Export public-safe usa allowlist.

### Autonomía y controles humanos

- Ops Agent: `EXECUTE_READ_ONLY`.
- Operator revisa, corrige metadata por eventos auditados y aprueba export.
- Founder/Release Manager resuelve D-01 y D-07.

### Límites operacionales

- No publicar ni enviar submission automaticamente.
- No fabricar traction, costos, usuarios, revenue ni testimonios.
- No exponer PII, secrets ni signed URLs en export publico.
- No usar fallback de modelo para cambiar cifras.

### Métricas y consumo

- Agent success/failure/retry rate.
- Cost coverage por provider/model.
- Prompt/model version distribution.
- Budget alert count y acknowledgements.
- Evidence completeness.

### Evidencia de finalización

- Dashboard y export reconciliados contra consultas fuente.
- Tests de allowlist private/public-safe.
- Health/readiness smoke con environment, commit y timestamp.
- D-01/D-07 registradas antes de release candidate.

### Deuda o capacidades diferidas

- Optimizacion avanzada de modelos, evaluacion continua automatica y routing adaptativo quedan post-MVP salvo que R06 detecte un riesgo operacional inmediato.

## 24. Trigger, inputs y outputs

| Proceso | Trigger | Inputs | Outputs |
|---|---|---|---|
| Usage ingestion | Product event committed | event ID, customer, assessment/submission, type | UsageEvent, counters |
| Cost attribution | Agent/cost event completes | provider, model, tokens, retries, billed/estimated cost | CostEvent, aggregates |
| Revenue evidence | Operator records payment/commitment | customer, amount, currency, status, related-party, proof ref | RevenueEvent |
| Pilot checklist | Pilot created or reviewed | owner, plan, consent, milestones, evidence refs | checklist state, warnings |
| Budget alert | Usage/cost crosses threshold | period, scope, threshold, actual | alert and audit event |
| Health check | Scheduled/read request | agents service endpoint, timestamp | health observation |
| Dashboard | Operator selects period | filters, classifications, source events | metrics, warnings, provenance |
| Evidence export | Operator approves scope | period, classification, selected sources, commit/environment | versioned manifest and artifacts |
| Readiness check | Operator requests validation | checklist, decisions, exports, deployment proof | pass/fail matrix |

## 25. Human in the loop

- Operator clasifica customer/pilot y related-party.
- Operator valida montos, moneda, conversion y evidence link.
- Operator corrige metadata mediante evento auditado.
- Operator configura y reconoce alertas.
- Operator decide que evidencia es private, judge-verifiable o public-safe.
- Operator valida testimonios y consentimientos.
- Operator aprueba cada export y la submission final.
- Founder/Release Manager resuelve D-01 y D-07.
- El sistema nunca publica, cobra ni declara traction autonomamente.

## 26. Guardrails

- No fabricar users, revenue, costs, testimonials ni agent runs.
- No mezclar paid revenue con commitments.
- No mezclar related-party con arms-length.
- No tratar credits como ausencia de consumo.
- No permitir `related_party = null`.
- No usar LLM para sumar ledgers o decidir readiness.
- No PII estudiantil en dashboard de negocio o export.
- No secrets ni URLs firmadas permanentes en exports.
- No export publico sin approval Operator.
- No release candidate con D-01 pendiente.
- No narrativa final con D-07 pendiente.
- No implementar R07/R08 desde R06.

## 27. Idempotencia

Keys estables:

- usage: `source_event_id + usage_type`;
- cost: `source_system + source_event_id + category`;
- revenue: `customer_id + external_reference + status` o `revenue_event_id` manual unico;
- checklist item: `pilot_id + checklist_item_code + version`;
- evidence reference: hash de `owner + reference_type + external_id` sin contenido secreto;
- alert: `scope + threshold_id + reporting_period + crossing_direction`;
- health observation: `service + environment + observed_at_bucket`;
- export: `reporting_period + classification + source_snapshot_hash`;
- readiness: `export_version + ruleset_version`.

Repetir una importacion, request o callback no debe duplicar uso, costo, revenue ni export. El sistema devuelve el resultado previo o registra una nueva version solo cuando cambia el source snapshot.

## 28. Reintentos y fallos

| Falla | Comportamiento esperado |
|---|---|
| Source event incompleto | Persistir warning; no inventar campo faltante |
| Pricing/provider rate faltante | Marcar costo `unpriced`; excluir de total final o mostrar coverage |
| Currency conversion faltante | Mantener monto original; no inventar USD |
| Evidence link inaccesible | Marcar invalid/unverified y mantener privado |
| Related-party faltante | Rechazar RevenueEvent final |
| Health endpoint falla | Registrar degraded/unknown sin exponer stack trace |
| Dashboard query falla | Mostrar error y correlation ID; no mostrar zero como dato real |
| Export falla | No marcar paquete approved ni published |
| Retry de import | Deduplicar por source event |
| Infra smoke falla | Bloquear readiness y conservar evidencia del fallo |

## 29. Reversion

- RevenueEvent y CostEvent no se borran silenciosamente; se invalidan o compensan.
- Related-party correction conserva previous/new value y actor.
- Evidence reference puede revocarse sin borrar el ledger.
- Export puede archivarse/revocarse; una nueva version no reemplaza evidencia historica.
- Alert acknowledgement se puede reabrir si la condicion persiste.
- Pilot state se corrige con historial.
- Despliegue sigue rollback normal por revision/imagen; la evidencia conserva commit y revision originales.
- D-01/D-07 se registran documentalmente antes de cambiar el criterio final.

## 30. Consumo y costos

R06 debe ser determinista por defecto. Un resumen opcional del Ops Agent puede usar LLM, pero no es necesario para dashboard, ledgers, alertas o export.

Dimensiones minimas:

- provider, model, model_policy, workflow_stage.
- tokens input/output y retries.
- estimated cost vs billed/actual cost.
- cash cost vs covered by credit.
- allocated tooling cost.
- customer, assessment, graded submission.
- reporting period.
- revenue paid vs commitment.
- related-party vs arms-length.
- marketing spend separado.

Metricas derivadas incluyen costo por run, assessment, graded submission, teacher/customer y gross margin. Las formulas y rate tables deben quedar versionadas con fecha de vigencia.

## 31. Criterios funcionales

- Operator autorizado puede abrir el dashboard por periodo.
- Dashboard muestra assessments, submissions/attempts, feedback, reports, agent runs y approvals.
- Dashboard muestra provider/model, success/failure/retry y cost coverage.
- Usage se compara contra plan limits y overuse es visible.
- Operator puede registrar revenue/payment/commitment evidence.
- Related-party es obligatorio y se agrega por separado.
- Operator puede registrar costos y distinguir cash/credit/tooling/marketing.
- Pilot checklist muestra owner, estado, consentimiento, prueba y siguiente accion.
- Missing evidence y budget alerts son visibles y reconocibles.
- Agent service health y timestamp son visibles.
- Operator puede generar, revisar y revocar un export versionado.
- Readiness matrix bloquea items faltantes y decisiones pendientes.

## 32. Criterios tecnicos

- Aggregates se calculan en API/DB con queries testeadas.
- Ledger writes usan transacciones, constraints e idempotency keys.
- RBAC/ownership se valida server-side.
- Exports incluyen source IDs, periodo, schema version, commit y environment.
- Ops Agent output es structured y no altera facts.
- Cost rates y currency conversion basis quedan versionados.
- Health endpoint tiene timeout y respuesta minima.
- Flyway cubre entidades, constraints e indexes.
- Infra planning verifica Cloud Run/API/Agents, hosting web, Cloud SQL, Artifact Registry, IAM y Secret Manager.

## 33. Criterios de calidad

- Cada widget muestra fuente, periodo y estado de freshness.
- Zero, missing y not-applicable se distinguen.
- Paid, commitment y manual se distinguen visualmente.
- Related-party tiene etiqueta inequivoca.
- Cost coverage muestra porcentaje, no oculta eventos sin precio.
- Dashboard mantiene alcance interno y evita crecer a BI suite.
- Export es legible, reproducible y trazable.
- Copy no afirma cumplimiento hasta completar readiness.
- Metricas estimadas se etiquetan como estimadas.

## 34. Criterios de seguridad

- Solo Operator autorizado accede a C14/C15.
- Pruebas cubren acceso denegado para teacher/student y cross-tenant.
- Public export usa allowlist y no contiene PII, secrets ni private links.
- Judge-private export requiere canal controlado.
- Consentimiento se valida antes de incluir testimonial/customer identity.
- Health no filtra configuracion sensible.
- Audit events no pueden editarse desde UI.
- Evidencia revocada deja de estar disponible sin borrar el historial.

## 35. Criterios de observabilidad

- Coverage de AgentExecutionLog es medible.
- Eventos con tokens/costo faltante generan warning.
- Failed/retried agent runs cuentan en tasas y costos si consumieron recursos.
- Revenue/cost/usage corrections quedan auditadas.
- Dashboard/export/readiness actions tienen correlation ID.
- Infra deploy y smoke registran environment, revision, commit y timestamp.
- Alert delivery/acknowledgement queda auditado.
- Health history permite distinguir outage de dato desconocido.

## 36. Criterios de despliegue

- Debe correr integrado en local y en el entorno decidido por D-01.
- `infra/terraform/environments/demo/` debe verificarse para cambios de `api/`, `agents/` y `web/`.
- Scope infra obligatorio debe cubrir Cloud Run o hosting equivalente, Cloud SQL, Artifact Registry, IAM y Secret Manager segun cada servicio afectado.
- Terraform `fmt`, `validate` y `plan` deben quedar documentados cuando el entorno/credenciales lo permitan.
- API/Agents health checks y web dashboard deben pasar smoke.
- Deployment proof incluye URL, commit, revision, timestamp y evidencia de provider/API aplicable.
- La tarea infra debe estar DONE antes de marcar R06 completa.
- D-01 determina si `beta`, `demo` o ambos forman el paquete final.

## 37. Criterios de negocio

- User/customer/pilot counts tienen fuente y periodo.
- Paid pilots, commitments y revenue mensual se muestran por separado.
- Revenue arms-length y related-party se muestran por separado.
- Operating cost, marketing spend y credit coverage se reportan.
- Cost per assessment y graded submission tienen coverage conocida.
- Pricing mostrado coincide con la fuente canonica resuelta en D-07.
- Evidence completeness alcanza 100% para items obligatorios o muestra bloqueo explicito.
- Ninguna cifra del paquete depende de mocks o hardcode.

## 38. Definition of Done

- [ ] US-082/090/091 enriquecidas antes de atomizar.
- [ ] US-PROPUESTA-01/05/06/08 creadas o aceptadas como tareas explicitas.
- [ ] D-01 resuelta y registrada.
- [ ] D-07 resuelta y narrativa reconciliada.
- [ ] Operator access funciona con RBAC server-side.
- [ ] Usage, Revenue y Cost ledgers persisten eventos idempotentes.
- [ ] Related-party es obligatorio y visible por separado.
- [ ] Dashboard usa datos reales y muestra provenance/freshness.
- [ ] Missing evidence, budget alert y agent health estan disponibles.
- [ ] Pilot checklist y consent/evidence references funcionan.
- [ ] Export private/public-safe es versionado, revisable y revocable.
- [ ] Submission readiness bloquea faltantes reales.
- [ ] Infra scope para servicios afectados esta DONE.
- [ ] Terraform/smoke/deployment proof estan documentados segun D-01.
- [ ] Tests funcionales, seguridad, ledger y export pasan.
- [ ] README/planning/release artifacts actualizados.

## 39. Validacion

- Unit tests de ledger validation, currency/status, related-party y idempotencia.
- Integration tests para product events -> usage/cost aggregates -> dashboard.
- Integration tests para revenue/cost corrections mediante compensating events.
- RBAC tests para Operator, teacher, student y cross-tenant access.
- Export tests que comparan private vs public-safe allowlists.
- Tests de missing evidence, alert threshold, cooldown y acknowledge.
- Tests de health timeout/degraded/unknown.
- Reconciliation de dashboard totals contra SQL de control.
- Smoke del flujo completo con al menos un piloto, eventos de uso, costo y revenue/commitment.
- Terraform fmt/validate/plan o bloqueo documentado por credenciales.
- Smoke de URL/health y captura de commit/revision/environment.
- Revision manual del paquete contra `docs/07-hackathon/evidence-checklist.md`.

## 40. Escenario Given/When/Then

```gherkin
Given an authorized operator and real product events from R01 through R05
And a pilot with usage, cost, revenue or commitment evidence
And D-01 and D-07 have recorded resolutions
When the operator opens the evidence dashboard for the hackathon period
And reviews missing evidence, budget alerts and agent service health
And classifies evidence for judge-private and public-safe use
And requests a versioned submission export
Then all dashboard totals trace to persisted source events
And paid revenue is separated from commitments
And related-party revenue is separated from arms-length revenue
And cash cost is separated from credit-covered and allocated cost
And no student PII or secret link appears in the public-safe export
And the readiness matrix identifies every remaining blocking item
And the approved package records its source snapshot, commit and environment
```

## 41. Metricas

- Evidence completeness percentage.
- Agent log coverage percentage.
- Agent success, failure and retry rate.
- Runs with provider/model/tokens/cost coverage.
- Assessments, graded submissions and closed attempts by period.
- Active teachers, customers and pilots.
- Usage vs plan and overage count.
- Paid pilots and commitments.
- Revenue by month.
- Arms-length and related-party revenue.
- Operating, marketing, cash and credit-covered costs.
- Cost per run, assessment, graded submission and customer.
- Gross margin by offer when data is sufficient.
- Budget alerts triggered/acknowledged.
- Evidence exports generated/revoked.
- Submission readiness pass rate.

## 42. Evidencias

- AgentExecutionLog export with provider/model/status/tokens/cost/retry.
- ApprovalEvent summary.
- UsageEvent and plan-limit report.
- RevenueEvent ledger and payment/commitment references.
- Related-party split.
- CostEvent ledger with cash/credits/tooling/marketing split.
- Customer/pilot checklist and consent state.
- Dashboard screenshot/export.
- Missing-evidence and budget-alert records.
- Agent health observation.
- Evidence export manifest.
- Submission readiness matrix.
- Demo/product URLs, commit, revision and timestamps.
- GCP/Cloud Run/Cloud SQL/Gemini proof required by D-01.
- Terraform plan/apply or deployment evidence, without secrets.
- Tests, smoke output, planning and PR references.

## 43. Riesgos y mitigaciones

| Riesgo | Mitigacion |
|---|---|
| D-01 sigue pendiente | Trabajar dashboard/ledgers en paralelo; bloquear release candidate hasta decidir y probar entorno. |
| D-07 produce pricing contradictorio | No exportar narrativa final; reconciliar fuente canonica y registrar decision. |
| US-082 crece a BI suite | Limitar widgets al evidence checklist y dividir export/readiness si hace falta. |
| Operator no tiene acceso | Priorizar US-PROPUESTA-01 y aplicar RBAC minimo antes del dashboard. |
| Evidencia tardia o incompleta | Ingestar desde R01, medir coverage y alertar faltantes. |
| Revenue se interpreta incorrectamente | Separar paid/commitment/manual y related-party/arms-length. |
| Credits ocultan costo real | Reportar consumo, cash y credit coverage por separado. |
| PII en export | Allowlist, clasificacion, consent y pruebas de leakage. |
| LLM altera cifras | Agregacion determinista; Ops Agent solo resume hechos inmutables. |
| Infra definida pero no desplegada | Scope infra obligatorio, plan/smoke y deployment proof antes de cierre. |
| Reintentos duplican ledger | Keys unicas y compensating events. |

## 44. Resultado esperado

Al cerrar R06, GradeOps AI dispone de un panel interno y un paquete de evidencia versionado que demuestran uso real, operacion de agentes, control humano, costos, revenue/commitments, related-party, pilotos y cumplimiento tecnico sin exponer datos privados. La release candidata queda vinculada a un entorno y commit verificables, con D-01/D-07 resueltas, infraestructura validada y todos los faltantes visibles en una matriz de readiness.

## 45. Prompt ejecutable `/release-*`

Comandos inspeccionados en el plugin `claude-planning-with-ai`:

- `/release-init`
- `/release-new vX.Y.Z -- <purpose> --target <YYYY-QN-MN-WN> --date <YYYY-MM-DD>`
- `/release-add vX.Y.Z NNN-slug [NNN-slug ...]`
- `/release-status [vX.Y.Z] [--mark-planned|--mark-in-progress|--mark-blocked|--mark-released|--mark-cancelled]`

Secuencia correcta: inicializar una vez, crear la release DRAFT, agregar plannings existentes, revisar/mover estado. No forzar `--mark-released` si existen plannings incompletos; no forzar `--mark-blocked` sin confirmacion cuando el script lo rechaza.

Prompt operativo con placeholders explicitos porque el Master Plan no fija version semantica, target period, fecha estimada ni planning IDs de R06:

```text
Contexto:
Estamos ejecutando R06 del Master Plan: Business Evidence and Hackathon Compliance.
Fuentes obligatorias:
- docs/master-plan/releases/release-06-business-evidence-hackathon-compliance.md
- docs/master-plan/master-plan-executive.md
- docs/master-plan/analysis/release-strategy.md
- docs/master-plan/analysis/automation-inventory.md
- docs/master-plan/analysis/user-story-inventory.md
- docs/master-plan/analysis/decisions-and-assumptions.md
- docs/02-product/user-stories/epic-09-evidence-metrics/
- docs/02-product/user-stories/epic-10-billing-plan-limits/
- docs/03-ai-agents/ops-agent.md
- docs/07-hackathon/evidence-checklist.md
- docs/00-project/cost-model.md
- docs/01-business/pricing.md
- infra/terraform/environments/demo/

Precondiciones:
- Resolver D-01 antes de marcar release candidate o RELEASED.
- Resolver D-07 antes de aprobar narrativa/pricing final.
- No inventar planning IDs. Crear las plannings con el flujo vigente si no existen.
- Ejecutar /us-enrich sobre US-082, US-090 y US-091.
- Crear o aceptar explicitamente US-PROPUESTA-01/05/06/08 antes de atomizar.

Objetivo:
Crear y gestionar R06: operator access -> usage/cost/revenue/pilot evidence -> dashboard -> missing evidence/alerts/health -> versioned export -> submission readiness.

Comandos:
1. Si .releases/ no existe:
   /release-init
2. Crear la release:
   /release-new <VERSION> -- Business Evidence and Hackathon Compliance --target <YYYY-QN-MN-WN> --date <YYYY-MM-DD>
3. Verificar y agregar plannings separadas para producto e infraestructura:
   /release-add <VERSION> <PLANNING_ID_R06_PRODUCT> <PLANNING_ID_R06_INFRA>
4. Revisar estado:
   /release-status <VERSION>
5. Marcar BLOCKED si D-01/D-07 impiden el cierre y el estado real lo soporta:
   /release-status <VERSION> --mark-blocked
6. Marcar RELEASED solo con todas las plannings COMPLETED:
   /release-status <VERSION> --mark-released

Alcance:
- US-082, US-090, US-091.
- US-PROPUESTA-01, US-PROPUESTA-05, US-PROPUESTA-06, US-PROPUESTA-08.
- AUT-16, AUT-17, AUT-18, AUT-20, AUT-21.
- Dashboard interno, ledgers, alertas, health, pilot checklist, export y readiness.
- Scope infra obligatorio para todo servicio api/agents/web modificado.

Exclusiones:
- Billing automatico, invoices, self-serve plans, CRM, marketplace, public BI y publicaciones automaticas.
- No implementar R07 ni R08.

Arquitectura:
- API/DB calculan y persisten facts/aggregates deterministicamente.
- Ops Agent solo resume o detecta faltantes; no altera cifras.
- Web consume API con Operator RBAC.
- Export usa snapshot/version/manifest y clasificacion de evidencia.
- Infra demo verifica runtime, DB, images, IAM y secrets de servicios afectados.

Seguridad:
- Operator auth y authorization server-side.
- Private/judge/public-safe separados.
- No student PII, secrets ni signed URLs en public export.
- Consentimiento antes de identidad/testimonial.

Automatizacion:
- Ingestion, aggregates, limits, ledgers y readiness son deterministas.
- Revenue/customer metadata y export son supervisados por Operator.
- No publicacion autonoma.

Trazabilidad:
- Cada cifra incluye source ID, periodo y freshness.
- Registrar contradicciones de docs/codigo y decisiones; no resolver silenciosamente.
- Mantener D-01/D-07 visibles hasta su resolucion documentada.
- Infra planning debe estar DONE antes de cerrar.

Metricas:
- evidence completeness y agent log/cost coverage.
- usage vs limits, users/pilots, paid revenue/commitments.
- related-party split, cash/credit costs y unit economics.
- alert, health, export y readiness status.

Criterios:
- Datos reales, no mocks ni hardcode.
- Totales reconciliados contra fuentes.
- Exports seguros, versionados y revocables.
- Tests, Terraform validation/plan y smoke documentados.
- No marcar RELEASED con decision, planning o evidencia bloqueante.

Entregables:
- Product planning y planning infra separadas.
- Dashboard/ledger/export/readiness implementados y probados.
- Evidence package y manifest.
- Deployment/provider proof segun D-01.
- Documentacion de D-01/D-07 y estado de release actualizados.
```

## 46. Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-20 | Incorporacion de capacidades de Agent Runtime | Declarar Ops Agent read-only y observabilidad/costo/readiness del runtime | Runtime, Ops Agent, evidence dashboard | D-01, D-04, D-06, D-07 |
| 2026-07-20 | Creacion inicial | Ejecucion de Fase 05 para R06 | Todo el documento | D-01, D-04, D-06, D-07 |
