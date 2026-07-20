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
| H-06 | `docs/CLAUDE.md` declara repo documentation-only y target Gemini/GCP, pero este checkout contiene codigo multi-area y la arquitectura vigente incluye Firebase/beta/Groq. | `docs/CLAUDE.md:7` dice documentation-only. `docs/CLAUDE.md:21` arquitectura Google Cloud assumptions. `docs/CLAUDE.md:73-75` Gemini API + Google Cloud target runtime. Codigo existe en `api/`, `agents/`, `web/`, `infra/`; `docs/04-architecture/beta-environment-design.md:21-43`. | `AGENTS.md`; Master Plan; codigo real. | Actualizar `docs/CLAUDE.md` o aclarar que aplica solo al repo docs historico, no al workspace actual. |
| H-07 | `99-decisions/README.md` no lista ADRs existentes recientes y faltan ADRs para decisiones ya ejecutadas. | `docs/99-decisions/README.md:60-76` termina en Firebase 2026-06-12. `docs/99-decisions/` contiene `2026-06-21-form-validation-react-hook-form-zod.md` y `2026-06-21-web-design-system.md`. No existe ADR para Groq default ni beta/demo. | `docs/99-decisions/` filesystem; Master Plan D-04/D-05. | Sincronizar README y crear ADRs faltantes antes de propagar cambios normativos. |

### MEDIUM

| ID | Hallazgo | Evidencia | Fuente de verdad | Accion recomendada |
|---|---|---|---|---|
| M-01 | User guide esta escrita como producto completo y puede prometer pantallas/flujos no implementados. | `docs/08-user-guide/06-dashboard-and-workspace.md:110-134` navega Open/Closed; `web/src/app/(protected)/bank/page.tsx`, `reports/page.tsx`, `students/page.tsx` existen como rutas, pero no se verifico feature complete. | Codigo `web/`; Master Plan release sequencing. | Marcar disponibilidad por release o prerequisito; no presentar P1/P2 como ya disponible. |
| M-02 | Indices y READMEs requieren sync tras refresh. | `docs/99-decisions/README.md` no lista ADRs 2026-06-21. `docs/README.md` y READMEs deben enlazar documentos enriquecidos despues de fases 02-04. | Filesystem `docs/`; prompt 05. | Ejecutar fase 05 despues de actualizar contenido. |
| M-03 | `05-evidence/*` sigue como plantillas livianas pese a que el Master Plan exige evidence backbone desde R01/R06. | `docs/05-evidence/agent-logs.md:3-12`; `docs/master-plan/master-plan-executive.md` C13/C14 y R01/R06. | Master Plan R01/R06. | Enriquecer evidencia con schemas, responsables, automatic/manual capture, ejemplos y criterios de aceptacion. |
| M-04 | `04-architecture/beta-environment-design.md` reconoce drift pero lo deja localmente aislado. | Drift note en `docs/04-architecture/beta-environment-design.md:93-115`; otros documentos siguen GCP/Gemini-only. | Master Plan D-01; codigo. | Propagar decision o warning a system architecture, deployment, developer guide y validacion MVP. |
| M-05 | `docs/03-ai-agents/` documenta 13 agentes funcionalmente, pero no distingue implementado vs. especificacion futura. | `docs/03-ai-agents/agents-overview.md:15-39` lista todos; codigo `agents/src/main/java/.../assessment/...` implementa Assessment Agent actual. | Codigo `agents/`; Master Plan R01-R06. | Agregar estado por agente: implemented, planned release, runtime increment, dependencies. |

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

## ADRs requeridas antes de editar contenido dependiente

| ADR / Decision | Severidad | Motivo |
|---|---|---|
| Groq/Gemini provider policy | BLOCKER | El codigo usa Groq por defecto y docs afirman Gemini/Vertex; afecta costos, runtime, validacion MVP y dev guide. |
| Entorno beta vs demo para validacion MVP | BLOCKER | Define si los docs deben presentar beta como evidencia real, demo como cumplimiento, o ambos con roles separados. |
| Closed P0 propagation decision | HIGH | Master Plan ya lo asume; si se mantiene, debe propagarse a user-story cut/demo/business. Si cambia, debe registrarse. |
| Pricing correction | MEDIUM | Si `submission-narrative.md` refleja pricing nuevo, requiere decision; si no, basta corregirlo. |

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

## Riesgos residuales

- Si D-01 se mantiene pendiente, la narrativa de validacion MVP no puede quedar en estado final sin disclaimers o alternativas.
- Si se actualizan docs fuente sin ADR de Groq/provider, el mismo drift reaparecera en pricing, cost categories y developer guide.
- Si Closed sigue P0 pero no se refleja en demo y business docs, el Master Plan y el corpus fuente seguiran contando historias distintas.
- Si la guia dev no se corrige pronto, futuros agentes implementadores pueden duplicar patrones ya existentes o ignorar contratos reales.

## Preguntas abiertas

1. ¿El entorno de evaluacion oficial para validacion MVP sera `demo` en GCP, `beta` como evidencia real, o ambos con responsabilidades separadas?
2. ¿Groq queda formalmente como default provider para beta y Gemini como requisito demo/validacion MVP, o debe revertirse el default?
3. ¿Closed debe aparecer en el video/demo de 3 minutos, o solo como evidencia secundaria?
4. ¿Las cifras de pricing en `submission-narrative.md` son una propuesta nueva o un drift accidental?
5. ¿Que documentos deben declarar disponibilidad real por release versus vision completa?

## Cierre obligatorio

1. **Resultado general:** FAIL.
2. **Numero de blockers:** 3.
3. **Primer grupo de documentos a actualizar:** arquitectura/runtime/decisiones: `docs/99-decisions/`, `docs/04-architecture/`, `docs/09-developer-guide/06-agent-development.md`, `docs/09-developer-guide/09-deployment-guide.md`, `docs/CLAUDE.md`.
4. **ADRs requeridas antes de editar:** Groq/Gemini provider policy; beta/demo validacion MVP deployment policy. Closed P0 y pricing pueden resolverse como docs update si se confirman las decisiones del Master Plan/pricing canonico.
5. **Prompt recomendado para continuar:** `docs/.prompting/source-docs-refresh-prompts/03-refresh-architecture-agent-runtime.md`, despues de resolver o dejar explicitamente como pendiente las ADRs de provider y entorno.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados |
|---|---|---|---|
| 2026-07-20 | Creacion inicial | Ejecucion de auditoria de obsolescencia de documentacion fuente | `docs/source-docs-refresh/audit-report.md` |
