# Auditoria de obsolescencia de documentacion fuente — GradeOps AI

> Ejecutado desde `docs/.prompting/source-docs-refresh-prompts/01-audit-source-docs-staleness.md`.
> Esta fase no modifica documentacion fuente; solo inventaria hallazgos y recomienda correcciones.

## Resumen ejecutivo

La documentacion fuente de GradeOps AI no puede usarse hoy como fuente confiable unica para planificar o implementar sin contrastarla contra `docs/master-plan/`, `docs/.prompting/master-plan-runtime/` y el codigo real.

El problema no es falta general de documentacion. El corpus es amplio y muchas secciones estan bien estructuradas. El problema es que varios documentos canonicos quedaron congelados en etapas distintas:

- `00-project/` y partes de `01-business/` siguen centrados en el flujo Open, aunque el Master Plan y producto vigente tratan Open + Closed como P0.
- `09-developer-guide/` describe `agents/` e infraestructura como scaffolding o Gemini-only, mientras el codigo ya contiene Assessment Agent, adaptadores Gemini/Groq, provider selection y persistencia de `AgentExecutionLog`.
- La narrativa de validacion MVP afirma producto desplegado en Google Cloud/Gemini, pero el repo documenta un entorno `beta` Render/Vercel/Neon/Groq y el Master Plan mantiene D-01 como decision critica pendiente.
- El modelo de evidencia existe en varias versiones, desde una lista minima en `05-evidence/agent-logs.md` hasta modelos ricos en arquitectura, UX y master plan.

Resultado general: **FAIL** para uso como fuente confiable sin refresh. El refresh debe comenzar por decisiones/arquitectura/runtime y luego propagar a producto, negocio, evidencia, UX y guias.

## Mapa de documentos revisados

| Area | Documentos revisados | Estado |
|---|---|---|
| Proyecto | `docs/README.md`, `docs/CLAUDE.md`, `docs/00-project/` | Parcialmente obsoleto |
| Negocio | `docs/01-business/` | Coherente para Open; incompleto para Closed |
| Producto | `docs/02-product/` y `docs/02-product/user-stories/` | Mixto: `mvp-scope`/`workflows` actualizados; corte P0 de historias no sincronizado |
| Agentes | `docs/03-ai-agents/` | Buen catalogo funcional; necesita runtime/provider/status real |
| Arquitectura | `docs/04-architecture/` | Fragmentada entre GCP/Gemini objetivo y beta/Groq real |
| Evidencia | `docs/05-evidence/` | Plantillas demasiado pobres para ejecucion |
| UX | `docs/06-ux/` | Bastante alineada; sirve como fuente rica para agent logs |
| Validacion MVP | `materiales archivados del evento` | Riesgo alto por narrativa GCP/Gemini y demo sin Closed |
| Guia usuario | `docs/08-user-guide/` | Rica, pero puede sonar a producto completo sin estados de disponibilidad |
| Guia dev | `docs/09-developer-guide/` | Obsoleta en estado real, runtime y base de datos |
| Buenas practicas | `docs/10-best-practices/` | Generalmente valida, poco especifica |
| Decisiones | `docs/99-decisions/` | Faltan ADRs y el README no lista ADRs existentes recientes |
| Contraste | `docs/master-plan/`, `docs/.prompting/master-plan-runtime/`, `api/`, `agents/`, `web/`, `infra/` | Fuentes de verdad para refresh |

## Hallazgos por severidad

### BLOCKER

| ID | Hallazgo | Evidencia | Fuente de verdad | Accion recomendada |
|---|---|---|---|---|
| B-01 | Entorno y proveedor de IA no estan reconciliados. Docs publicos/validacion MVP afirman GCP/Gemini, mientras codigo y beta documentan Groq/beta y el Master Plan mantiene D-01/D-04 pendientes. | `materiales archivados del evento` afirma producto desplegado en Google Cloud y 13 agentes con Vertex AI Gemini. `docs/04-architecture/beta-environment-design.md:21-43` documenta beta en Vercel/Render/Neon y demo en GCP. `docs/04-architecture/beta-environment-design.md:87-115` reconoce drift: default real `groq`. `agents/src/main/resources/application.yml:11-21` define `default-provider: groq`. | `docs/master-plan/master-plan-executive.md` D-01/D-04; `docs/master-plan/analysis/agent-runtime-strategy.md`; codigo `agents/`. | Crear ADR para provider policy Groq/Gemini y ADR/decision para beta/demo. Luego actualizar `CLAUDE.md`, `04-architecture/*`, `materiales archivados del evento`, `09-developer-guide/*` y evidencia. |
| B-02 | La guia dev induce implementacion incorrecta: declara `agents/` scaffolding/no implementado y base de datos V1/V2, pero el codigo tiene Assessment Agent, adapters, endpoints, V12 y `AgentExecutionLog`. | `docs/09-developer-guide/06-agent-development.md:5` dice que no hay agent code. `docs/09-developer-guide/09-deployment-guide.md:452-454` dice agents scaffolding-only. `docs/09-developer-guide/05-database-guide.md:15-18` dice una sola tabla y dos migraciones. Codigo: `agents/src/main/java/.../assessment/...`, `agents/src/main/resources/prompts/assessment-generation.st`, `api/src/main/resources/db/migration/V10__...` a `V12__add_agent_execution_logs.sql`. | Codigo `api/` y `agents/`; `docs/master-plan/analysis/documentation-diagnosis.md`; `agent-runtime-strategy.md`. | Actualizar `09-developer-guide/01-local-setup.md`, `02-repository-map.md`, `05-database-guide.md`, `06-agent-development.md`, `09-deployment-guide.md` con estado actual y objetivo separado. |
| B-03 | El alcance P0 de Closed no esta propagado. `mvp-scope` y `workflows` lo tratan como P0; el corte P0 de historias y el demo script no lo incluyen realmente. | `docs/02-product/mvp-scope.md:11-15` define Open + Closed. `docs/02-product/workflows.md:32-36` marca Closed como P0. `docs/02-product/user-stories.md:27-31` no incluye US-100..124 en el MVP cut. `materiales archivados del evento` exige datos Closed, pero escenas `27-159` muestran solo Open/evidence. | `docs/master-plan/master-plan-executive.md` D-02 resuelta: Closed = P0; release R04/R05. | Actualizar producto/validacion MVP/user-story cut o declarar explicitamente una demo parcial. Si se cambia decision, registrar ADR o decision de alcance. |

### HIGH

| ID | Hallazgo | Evidencia | Fuente de verdad | Accion recomendada |
|---|---|---|---|---|
| H-01 | `00-project/solution.md` sigue describiendo MVP/agents como flujo Open de 8 agentes, aunque los docs vigentes tienen 13 agentes y Closed. | `docs/00-project/solution.md:16-29` lista solo workflow Open. `docs/00-project/solution.md:45-57` lista 8 agentes. `docs/03-ai-agents/agents-overview.md:15-39` lista Open + Closed con 13 agentes. | ADRs Closed en `99-decisions/`; Master Plan D-02; `docs/02-product/mvp-scope.md`. | Refresh de `00-project/solution.md`, `roadmap.md`, `cost-model.md`, `pitch.md` para distinguir Open wedge, Closed P0 y roadmap. |
| H-02 | Negocio y pricing no tienen posicionamiento ni unit economics claros para Closed, aunque pricing por graded submission incluye attempts conceptualmente. | `docs/01-business/business-model.md:37-74` ICP y segmentos solo hablan de practical assessments. `docs/01-business/pricing.md:18-37` define graded submission incluyendo attempts pero sin explicar Closed. `docs/02-product/mvp-scope.md:44-61` define Closed con attempts/result links. | Master Plan R04/R05; ADR deterministic closed grading; product workflows. | Enriquecer business model, pricing, GTM y customer discovery con oferta Closed, attempts, question bank value y limites. |
| H-03 | Narrativa de validacion MVP contradice pricing canonico. | `materiales archivados del evento` usa 150/500/2000 submissions. `docs/01-business/pricing.md:56-65` usa Free, 90/300/1000 y Pilot Pack 150. | `docs/01-business/pricing.md`; `docs/00-project/cost-model.md`; Master Plan D-07. | Actualizar `submission-narrative.md` o registrar cambio de pricing como ADR si las cifras nuevas son intencionales. |
| H-04 | Modelo de AgentExecutionLog esta fragmentado y `05-evidence/agent-logs.md` es insuficiente para implementacion/evidencia. | `docs/05-evidence/agent-logs.md:3-12` lista solo 7 campos basicos. `docs/04-architecture/data-model.md:513-536` define campos ricos. `docs/09-developer-guide/06-agent-development.md:327-350` define otro modelo rico, pero Gemini-only. | Master Plan D-06; `agent-runtime-strategy.md` DoD minima; R01/R06. | Adoptar una fuente canonica rica para `AgentExecutionLog`; actualizar `05-evidence/agent-logs.md`, arquitectura, UX, user guide y developer guide con los mismos campos. |
| H-05 | Model routing sigue expresado como Gemini/Flash-class sin provider/model catalog real. | `docs/03-ai-agents/agents-overview.md:200-217` define Flash/Flash-Lite. `docs/04-architecture/data-model.md:525` dice `model = Gemini model used`. `docs/04-architecture/data-model.md:677` `CostCategory` no incluye Groq/OpenAI-compatible. `agents/src/main/resources/application.yml:16-21` incluye Groq default y costos `gemini`/`groq`. | `agent-runtime-strategy.md` R01/R02; codigo `agents/`. | Actualizar model/provider policy: provider, model, cost category, pricing source, default/fallback y per-request override. |
| H-06 | `docs/CLAUDE.md` declaraba el repo como solo documentacion y target Gemini/GCP, pero este checkout contiene codigo multi-area y la arquitectura vigente incluye Firebase/beta/Groq. | `docs/CLAUDE.md` afirmaba que no habia codigo de aplicacion, asumia Google Cloud y trataba Gemini como runtime unico. Codigo existe en `api/`, `agents/`, `web/`, `infra/`; `docs/04-architecture/beta-environment-design.md:21-43`. | `AGENTS.md`; Master Plan; codigo real. | Actualizar `docs/CLAUDE.md` o aclarar que aplica solo al repo docs historico, no al workspace actual. |
| H-07 | `99-decisions/README.md` no lista ADRs existentes recientes y faltan ADRs para decisiones ya ejecutadas. | `docs/99-decisions/README.md:60-76` termina en Firebase 2026-06-12. `docs/99-decisions/` contiene `2026-06-21-form-validation-react-hook-form-zod.md` y `2026-06-21-web-design-system.md`. No existe ADR para Groq default ni beta/demo. | `docs/99-decisions/` filesystem; Master Plan D-04/D-05. | Sincronizar README y crear ADRs faltantes antes de propagar cambios normativos. |

### MEDIUM

| ID | Hallazgo | Evidencia | Fuente de verdad | Accion recomendada |
|---|---|---|---|---|
| M-01 | User guide esta escrita como producto completo y puede prometer pantallas/flujos no implementados. | `docs/08-user-guide/06-dashboard-and-workspace.md:110-134` navega Open/Closed; `web/src/app/(protected)/bank/page.tsx`, `reports/page.tsx`, `students/page.tsx` existen como rutas, pero no se verifico feature complete. | Codigo `web/`; Master Plan release sequencing. | Marcar disponibilidad por release o prerequisito; no presentar P1/P2 como ya disponible. |
| M-02 | Indices y READMEs requieren sync tras refresh. | `docs/99-decisions/README.md` no lista ADRs 2026-06-21. `docs/README.md` y READMEs deben enlazar documentos enriquecidos despues de fases 02-04. | Filesystem `docs/`; prompt 05. | Ejecutar fase 05 despues de actualizar contenido. |
| M-03 | `05-evidence/*` sigue como plantillas livianas pese a que el Master Plan exige evidence backbone desde R01/R06. | `docs/05-evidence/agent-logs.md:3-12`; `docs/master-plan/master-plan-executive.md` C13/C14 y R01/R06. | Master Plan R01/R06. | Enriquecer evidencia con schemas, responsables, automatic/manual capture, ejemplos y criterios de aceptacion. |
| M-04 | `04-architecture/beta-environment-design.md` reconoce drift pero lo deja localmente aislado. | Drift note en `docs/04-architecture/beta-environment-design.md:93-115`; otros documentos siguen GCP/Gemini-only. | Master Plan D-01; codigo. | Propagar decision o warning a system architecture, deployment, developer guide y validacion MVP. |
| M-05 | `docs/03-ai-agents/` documenta 13 agentes funcionalmente, pero no distingue implementado vs. especificacion futura. | `docs/03-ai-agents/agents-overview.md:15-39` lista todos; codigo `agents/src/main/java/.../assessment/...` implementa Assessment Agent actual. | Codigo `agents/`; Master Plan R01-R06. | Agregar estado por agente: implemented, planned release, runtime increment, dependencies. |
| M-06 | La documentacion fuente mezcla ingles y espanol entre archivos y secciones. | Observacion editorial 2026-07-20 durante fase 02; visible en nombres y contenido de documentos activos como `docs/00-project/solution.md`, `docs/01-business/*`, `docs/02-product/*` y reportes en espanol. | Decision editorial pendiente; debe definirse idioma canonico por audiencia y tipo de documento. | Crear fase breve de normalizacion idiomatica: inventario de idioma, regla canonica, glosario bilingue si aplica, y conversion por carpetas sin mezclar alcance tecnico. |
| M-07 | Web y API discrepan en reset password. | `api` implementa `POST /api/v1/auth/reset-password` con `code` en body; `web/src/lib/api/auth.ts` llama `PUT /api/v1/auth/reset-password?code=...`. | Codigo `api/` y `web/`; `docs/09-developer-guide/03-api-reference.md` sincronizado con API durante fase 04. | Corregir `web` o API en una tarea de implementacion; agregar test de contrato para reset password. |

### LOW

| ID | Hallazgo | Evidencia | Fuente de verdad | Accion recomendada |
|---|---|---|---|---|
| L-01 | `docs/00-project/solution.md` mantiene "Valid implementation candidates" aunque stack ya esta mayormente decidido. | `docs/00-project/solution.md:176-188` presenta opciones Next.js/Angular, Spring/Node, Firestore/Cloud SQL. ADR `2026-06-10-technology-stack.md` fija stack. | ADR technology stack; codigo. | Reescribir como "stack vigente" y mover alternativas a contexto historico si aportan. |
| L-02 | Algunos documentos mezclan `Ops Evidence Agent` y `Ops Agent`. | `docs/00-project/solution.md:56` usa Ops Evidence Agent; `docs/03-ai-agents/agents-overview.md:28` usa Ops Agent. | Master Plan usa Ops Agent/Ops Evidence segun contexto. | Normalizar nomenclatura o definir alias. |

### INFORMATIONAL

| ID | Hallazgo | Evidencia | Fuente de verdad | Accion recomendada |
|---|---|---|---|---|
| I-01 | Hay buena base para refresh: `02-product/mvp-scope.md`, `02-product/workflows.md`, `03-ai-agents/agents-overview.md`, `04-architecture/data-model.md` y `08-user-guide/04-reviewing-ai-outputs.md` ya contienen mucho contenido reusable. | Documentos citados. | Master Plan y ADRs. | Reusar y consolidar; evitar reescritura total. |
| I-02 | La carpeta `.raw/` no debe editarse. | Regla en `docs/.prompting/source-docs-refresh-prompts/README.md`. | Prompt package. | Mantener como evidencia historica. |

## Obsolescencia vs. enriquecimiento requerido

| Tipo | Documentos principales | Resumen |
|---|---|---|
| Obsolescencia | `docs/09-developer-guide/*`, `docs/CLAUDE.md`, `materiales archivados del evento`, `docs/00-project/solution.md`, `docs/04-architecture/system-architecture.md` | Estado real de codigo, runtime, provider y entorno no coincide. |
| Enriquecimiento | `docs/05-evidence/*`, `docs/01-business/*`, `docs/03-ai-agents/*`, `docs/08-user-guide/*` | Faltan schemas canonicos, ejemplos, estados, release availability, provider policy y valor Closed. |
| Sincronizacion | READMEs, `docs/99-decisions/README.md`, links entre decisions/docs | Indices y referencias deben reflejar decisiones y docs vigentes. |
| Normalizacion editorial | Todo `docs/` activo | Se mezclan ingles y espanol; falta politica de idioma canonico y glosario de terminos que deben quedar en ingles por dominio tecnico/producto. |

## Estado de decisiones de documentacion fuente

| Decision | Estado | Motivo |
|---|---|---|
| Groq/Gemini provider policy | ADR creada: `2026-07-20-agent-provider-model-policy.md` | El codigo usa Groq por defecto y Gemini sigue soportado para entorno Google Cloud-oriented; docs deben hablar de provider/model policy. |
| Entorno beta vs demo | ADR creada: `2026-07-20-environment-roles.md` | `beta` y `demo` tienen responsabilidades separadas; no se debe afirmar despliegue GCP sin evidencia. |
| Closed P0 propagation | Resuelta por docs update | Master Plan ya lo asume; fase 02 propago Closed P0 a producto, negocio y evidencia. |
| Pricing correction | Resuelta por archivo historico | Pricing activo vive en `docs/01-business/pricing.md`; cifras del evento archivado no gobiernan oferta vigente. |
| Restricciones especificas del evento 2026 | ADR creada: `2026-07-20-archive-event-specific-constraints.md` | Material del evento queda historico y no impone scope activo. |
| Idioma canonico | Pendiente | Existe mezcla espanol/ingles; requiere decision editorial posterior. |

## Orden recomendado de ejecucion

1. **Decision/ADR gating:** resolver Groq/Gemini provider policy y beta/demo antes de editar docs que dependan de runtime o validacion MVP.
2. Ejecutar `03-refresh-architecture-agent-runtime.md` primero, porque corrige las fuentes tecnicas que condicionan producto, guias y evidencia.
3. Ejecutar `02-refresh-product-business-evidence.md` para propagar Closed P0, pricing y evidence model hacia producto/negocio/validacion MVP.
4. Ejecutar `04-refresh-guides-ux-and-developer-docs.md` para ajustar guias de usuario/desarrollo contra el estado real.
5. Ejecutar `05-sync-indexes-decisions-and-traceability.md` para READMEs, ADR links y trazabilidad.
6. Ejecutar `06-validate-source-docs-refresh.md` para cerrar hallazgos residuales.

## Seguimiento de ejecucion

### 2026-07-20 — Fase 03 arquitectura, agentes y runtime

Prompt ejecutado: `docs/.prompting/source-docs-refresh-prompts/03-refresh-architecture-agent-runtime.md`.

| Hallazgo | Estado | Evidencia de actualizacion | Pendiente |
|---|---|---|---|
| B-01 | Parcialmente abordado | Creadas ADRs `2026-07-20-agent-provider-model-policy.md` y `2026-07-20-environment-roles.md`; actualizados `system-architecture.md`, `beta-environment-design.md`, `deployment-guide.md` y `agent-development.md` para provider/model y roles beta/demo. | La evidencia real de despliegue/provider sigue pendiente para fases 02/06. |
| B-02 | Parcialmente abordado | `06-agent-development.md`, `09-deployment-guide.md` y `03-api-reference.md` ya reconocen Assessment Agent, provider adapters, `/api/v1` assessment draft endpoints y `api` como persistencia de logs. | `01-local-setup.md`, `02-repository-map.md` y `05-database-guide.md` siguen fuera del scope de prompt 03 y deben corregirse en fase 04. |
| B-03 | Pendiente | No corresponde a fase 03; requiere producto/validacion MVP/user-story cut. | Ejecutar fase 02 para propagar Closed P0 o registrar cambio de decision. |
| H-04 | Parcialmente abordado | `data-model.md`, `agents-overview.md` y `agent-development.md` incorporan provider/model y modelo rico de execution payload. | `05-evidence/agent-logs.md` sigue pendiente para fase 02. |
| H-05 | Parcialmente abordado | ADR provider/model, `agents-overview.md`, `data-model.md` y `agent-development.md` ya no tratan routing como Gemini-only. | Codigo actual `AgentExecutionLogPayload` aun no incluye `provider`; queda como item de implementacion/documentacion sincronizada. |
| H-06 | Parcialmente abordado | Arquitectura y guias tecnicas ya no dependen de `docs/CLAUDE.md` como verdad unica. | `docs/CLAUDE.md` no esta en scope de prompt 03 y sigue pendiente. |
| H-07 | Abordado para fase 03 | `99-decisions/README.md` lista ADRs 2026-06-21 y las nuevas ADRs 2026-07-20. | Ninguno en esta fase. |
| M-04 | Abordado para fase 03 | `beta-environment-design.md` reemplaza el drift note por la decision 2026-07-20; `system-architecture.md` y `deployment-guide.md` declaran roles beta/demo. | Evidencia final de despliegue queda para R06/fase 06. |
| M-05 | Abordado para fase 03 | `agents-overview.md` distingue Assessment Agent implementado, provider adapters implementados, runtime generico planificado y otros 12 agentes contratados/planificados. | Implementacion de agentes posteriores sigue release-by-release. |

### 2026-07-20 — Fase 02 producto, negocio y evidencia

Prompt ejecutado: `docs/.prompting/source-docs-refresh-prompts/02-refresh-product-business-evidence.md`.

Decision operativa adicional: la narrativa especifica del evento 2026 quedo archivada y no debe imponer restricciones al producto activo. Las correcciones de esta fase se hicieron sobre documentacion activa de producto, negocio y evidencia; no sobre `docs/master-plan/`, `docs/.prompting/`, `docs/.raw/` ni codigo.

| Hallazgo | Estado | Evidencia de actualizacion | Pendiente |
|---|---|---|---|
| B-03 | Parcialmente abordado | `docs/02-product/user-stories.md` y `docs/02-product/user-stories/README.md` ahora incluyen US-100, US-101, US-110..US-114 y US-120..US-124 en el corte P0, con Closed grading deterministico contra snapshot congelado. | Las historias Closed ya existen, pero requieren enriquecimiento/atomizacion release-by-release antes de implementacion. |
| H-01 | Abordado para fase 02 | `docs/00-project/solution.md` ahora describe Open y Closed workflows, scope matrix con question bank/snapshot/signed links/item analytics, 13 agentes y modelo de evidencia con provider/model/prompt version. | `roadmap.md`, `cost-model.md` y `pitch.md` no se tocaron en esta fase; revisar en prompt 05/06 si quedan inconsistencias. |
| H-02 | Abordado para fase 02 | `docs/01-business/business-model.md`, `pricing.md`, `go-to-market.md` y `customer-discovery.md` incorporan Closed como oferta P0, attempts como unidad comercial, question-bank value, item analytics y reglas de uso Open/Closed. | Validar despues contra pricing final y evidencia real de pilotos. |
| H-03 | Cerrado por archivo historico | La narrativa del evento quedo fuera de documentos activos; pricing canonico activo permanece en `docs/01-business/pricing.md` y fue enriquecido con reglas Open/Closed. | No usar materiales archivados como fuente de oferta vigente. |
| H-04 | Abordado para fase 02 | `docs/05-evidence/agent-logs.md` define campos requeridos de `AgentExecutionLog`, captura automatica/manual y mapeo R01-R06. | Sincronizar user/developer guides en fase 04 si repiten campos antiguos. |
| M-03 | Abordado para fase 02 | `docs/05-evidence/README.md`, `usage-metrics.md`, `revenue.md`, `users.md`, `agent-logs.md` y `testimonials.md` pasan de plantillas livianas a contratos de captura con automatic/manual capture. | Agregar ejemplos reales cuando existan pilots/telemetria. |
| L-01 | Abordado para fase 02 | `docs/00-project/solution.md` reemplaza candidatos de stack por direccion vigente: Next.js, Spring Boot API, Spring Boot/Spring AI agents, Cloud SQL PostgreSQL, Cloud Run y provider adapters. | Ninguno en esta fase. |

### 2026-07-20 — Fase 04 guias, UX y developer docs

Prompt ejecutado: `docs/.prompting/source-docs-refresh-prompts/04-refresh-guides-ux-and-developer-docs.md`.

| Hallazgo | Estado | Evidencia de actualizacion | Pendiente |
|---|---|---|---|
| B-02 | Abordado para documentacion | `docs/09-developer-guide/README.md`, `01-local-setup.md`, `02-repository-map.md`, `03-api-reference.md`, `05-database-guide.md`, `07-web-development.md`, `08-testing-guide.md` y `09-deployment-guide.md` reconocen V1-V12, assessment draft slice, `agentclient`, Assessment Agent, provider adapters y `AgentExecutionLog`. | Mismatch real `web`/`api` en reset password queda registrado como M-07 para implementacion. |
| M-01 | Abordado para documentacion | `docs/08-user-guide/README.md`, `02-open-assessment-cycle.md`, `03-closed-assessment-cycle.md`, `06-dashboard-and-workspace.md` y `docs/06-ux/screen-inventory.md` declaran disponibilidad actual vs target MVP, rutas actuales y pantallas/release slices pendientes. | Las guias deben ajustarse de nuevo cuando cada slice pase de placeholder a implementado. |
| H-04 | Abordado para guias | UX/user guide ahora exigen provider/model/prompt version y estados de review; testing guide exige regresiones para `AgentExecutionLog` y side effects de evidencia. | Mantener sincronizado con `docs/05-evidence/agent-logs.md` si cambia el schema. |
| M-05 | Refuerzo aplicado | `02-repository-map.md` y `08-testing-guide.md` ya no tratan `agents/` como scaffolding; documentan endpoint interno, prompt file-based, Gemini/Groq adapters y tests. | Implementar agentes posteriores release-by-release. |
| M-07 | Nuevo pendiente tecnico | Se documento la API actual (`POST /api/v1/auth/reset-password` con `code` en body) y se registro que `web/src/lib/api/auth.ts` llama `PUT` con query param. | Requiere cambio de codigo y test de contrato; no estaba permitido por prompt 04. |
| Buenas practicas | Enriquecido | `docs/10-best-practices/08-checklists-operativos.md` incluye checklist GradeOps para tipos API->Web, prompts versionados, no student login, deterministic Closed grading, evidence events y related-party revenue. | Normalizacion idiomatica queda pendiente por M-06. |

### 2026-07-20 — Fase 05 indices, decisiones y trazabilidad

Prompt ejecutado: `docs/.prompting/source-docs-refresh-prompts/05-sync-indexes-decisions-and-traceability.md`.

| Hallazgo | Estado | Evidencia de actualizacion | Pendiente |
|---|---|---|---|
| H-06 | Abordado para documentacion | `docs/CLAUDE.md` ya no declara el repositorio como solo documentacion ni como runtime unico Gemini/GCP; ahora distingue `docs/` como area canonica dentro del workspace con `api/`, `agents/`, `web/` e `infra/`, y enlaza provider/environment/archive ADRs. | Mantener sincronizado con `AGENTS.md` si cambia la estructura del workspace. |
| H-07 | Abordado | `docs/99-decisions/README.md` lista ADRs 2026-06-21, provider/model, environment roles y archive-event-specific-constraints. | Ninguno para fase 05. |
| M-02 | Abordado para fase 05 | `docs/README.md`, READMEs de negocio/producto/agentes/arquitectura/evidencia y `99-decisions/README.md` enlazan documentos actuales, decisiones vigentes, Master Plan derivado, source-docs-refresh y archivo historico. | Ejecutar fase 06 para detectar links/huerfanos residuales. |
| H-03 | Reforzado | Creado ADR `2026-07-20-archive-event-specific-constraints.md`; el README historico del evento declara que el material no es canonico. | No usar archivo historico como oferta vigente. |
| Trazabilidad eventos | Abordado | `docs/05-evidence/README.md` conecta `AgentExecutionLog`, `ApprovalEvent`, `UsageEvent`, `RevenueEvent` y `CostEvent` con producto, arquitectura, agentes, negocio y guias dev. | Agregar ejemplos reales cuando existan datos de pilotos. |

## Riesgos residuales

- Si D-01 se mantiene pendiente, la narrativa de validacion MVP no puede quedar en estado final sin disclaimers o alternativas.
- Si se actualizan docs fuente sin ADR de Groq/provider, el mismo drift reaparecera en pricing, cost categories y developer guide.
- Si Closed sigue P0 pero no se implementa con slices R04/R05 verificables, el Master Plan y el producto real pueden volver a divergir.
- Si la guia dev no se corrige pronto, futuros agentes implementadores pueden duplicar patrones ya existentes o ignorar contratos reales.
- Si no se define idioma canonico, la documentacion seguira mezclando espanol e ingles y perdera consistencia editorial para implementadores, agentes y materiales de producto.
- Si no se corrige el contrato reset-password entre `web` y `api`, el flujo de recuperacion de contrasena puede fallar aunque la documentacion ya refleje el API actual.

## Preguntas abiertas

1. ¿El entorno de evaluacion oficial para validacion MVP sera `demo` en GCP, `beta` como evidencia real, o ambos con responsabilidades separadas?
2. ¿Groq queda formalmente como default provider para beta y Gemini como requisito demo/validacion MVP, o debe revertirse el default?
3. ¿Closed debe aparecer como demo principal o como segundo flujo validable cuando R04/R05 esten implementadas?
4. ¿Que documentos deben declarar disponibilidad real por release versus vision completa?
5. ¿El idioma canonico de la documentacion activa sera espanol, ingles, o espanol con terminos tecnicos/producto en ingles bajo glosario?
6. ¿Se corrige reset-password en `web` para llamar el API actual, o se cambia el API para aceptar el contrato del cliente?

## Cierre obligatorio

1. **Resultado general:** FAIL.
2. **Numero de blockers:** 3.
3. **Primer grupo de documentos a actualizar:** arquitectura/runtime/decisiones: `docs/99-decisions/`, `docs/04-architecture/`, `docs/09-developer-guide/06-agent-development.md`, `docs/09-developer-guide/09-deployment-guide.md`, `docs/CLAUDE.md`.
4. **ADRs requeridas antes de editar:** provider/model policy, environment roles y archive-event-specific-constraints ya existen. Idioma canonico sigue pendiente.
5. **Prompt recomendado para continuar:** `docs/.prompting/source-docs-refresh-prompts/06-validate-source-docs-refresh.md`, para validar links, huerfanos, drift residual y estado final.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados |
|---|---|---|---|
| 2026-07-20 | Creacion inicial | Ejecucion de auditoria de obsolescencia de documentacion fuente | `docs/source-docs-refresh/audit-report.md` |
| 2026-07-20 | Seguimiento fase 03 | Arquitectura, agentes, runtime y ADRs de provider/entornos actualizados | `docs/99-decisions/`, `docs/04-architecture/`, `docs/03-ai-agents/`, `docs/09-developer-guide/06-agent-development.md`, `docs/09-developer-guide/09-deployment-guide.md` |
| 2026-07-20 | Seguimiento fase 02 | Producto, negocio y evidencia actualizados para Open/Closed y captura R01-R06 | `docs/00-project/solution.md`, `docs/01-business/`, `docs/02-product/user-stories.md`, `docs/05-evidence/` |
| 2026-07-20 | Hallazgo editorial agregado | Mezcla de espanol/ingles en documentacion activa debe normalizarse en una fase posterior | `docs/source-docs-refresh/audit-report.md` |
| 2026-07-20 | Seguimiento fase 04 | Guias UX/usuario/desarrollo/testing actualizadas contra codigo y disponibilidad real | `docs/06-ux/`, `docs/08-user-guide/`, `docs/09-developer-guide/`, `docs/10-best-practices/08-checklists-operativos.md` |
| 2026-07-20 | Seguimiento fase 05 | Indices, ADRs y trazabilidad sincronizados | `docs/README.md`, `docs/CLAUDE.md`, READMEs activos, `docs/99-decisions/`, README historico del evento |
