# Validacion final del refresh de documentacion fuente — GradeOps AI

> Ejecutado desde `docs/.prompting/source-docs-refresh-prompts/06-validate-source-docs-refresh.md`.
> Fecha: 2026-07-20.

## Resumen ejecutivo

Resultado general: **PASS WITH CONDITIONS**.

La documentacion fuente activa de `docs/` puede volver a usarse como base confiable para planificacion e implementacion, con condiciones explicitas. Las fases 02, 03, 04 y 05 corrigieron el drift principal: Open + Closed P0, runtime provider/model-aware, evidencia enriquecida, guias de usuario/desarrollo sincronizadas, indices/ADRs actualizados y restricciones del evento archivadas como material historico.

No quedan blockers documentales para continuar con el Master Plan o iniciar trabajo de release. Los riesgos residuales son implementables o editoriales: dos divergencias de codigo/contrato, una deuda de naming de configuracion, evidencia real de despliegue aun pendiente para R06 y una politica de idioma canonico que debe definirse pronto.

## Resultado general

| Dimension | Resultado |
|---|---|
| Coherencia fuente | PASS WITH CONDITIONS |
| Runtime y agentes | PASS WITH CONDITIONS |
| Navegacion | PASS |
| Accionabilidad | PASS WITH CONDITIONS |
| Contraste con codigo | PASS WITH CONDITIONS |
| Compatibilidad con Master Plan | PASS WITH CONDITIONS |

## Documentos revisados

- `docs/source-docs-refresh/audit-report.md`.
- `docs/README.md`, `docs/CLAUDE.md` y READMEs activos.
- `docs/00-project/` a `docs/10-best-practices/`.
- `docs/99-decisions/`.
- `docs/master-plan/`.
- `docs/.prompting/master-plan-runtime/`.
- Codigo relevante en `api/`, `agents/`, `web/` e `infra/`.

Se excluyo `docs/.raw/` de validaciones de vigencia por ser material fuente/historico no editable. `docs/archive/2026-event/` se trato como archivo historico; solo se verifico que su README declare esa condicion.

## Correcciones menores aplicadas

| Archivo | Cambio | Motivo |
|---|---|---|
| `docs/99-decisions/2026-06-10-agent-runtime-separation.md` | Reemplazado link wiki legacy a technology stack por link relativo al indice real de arquitectura. | Corregir navegacion sin cambiar la decision. |
| `docs/master-plan/analysis/documentation-diagnosis.md` | Reescrita una referencia a evaluacion de jurado/bases como riesgo durable de evidencia de despliegue beta/demo. | Eliminar restriccion residual del evento manteniendo el riesgo tecnico valido. |
| `docs/02-product/user-stories/README.md` | Agregados links directos a historias fuera de alcance y template. | Evitar documentos activos no alcanzables desde indices. |
| `docs/README.md` | Agregados links a guias smoke de assessment creation. | Hacer alcanzables guias operativas existentes. |

## Hallazgos residuales

### HIGH-01 — `AgentExecutionLog` puede perder `provider` cuando se usa default

**Evidencia:** `agents/src/main/resources/application.yml:16` define `default-provider: groq`; `agents/src/main/java/.../AgentExecutionLogPayload.java:42-55` no incluye `provider`; `api/src/main/java/.../DraftGenerationCoordinator.java:80-84` persiste `agentCommand.provider()`, no el proveedor efectivamente seleccionado por `agents/`.

**Impacto:** la documentacion fuente ahora exige provider/model como evidencia canonica, pero el codigo puede persistir `provider = null` cuando la solicitud delega al default. Esto afecta trazabilidad, costos, comparacion de modelos y evidencia R01/R06.

**Correccion recomendada:** agregar `provider` al payload de respuesta de `agents/`, actualizar el espejo `api/agentclient`, persistir el proveedor resuelto y cubrir default-provider con test de contrato/integracion.

**Responsable sugerido:** Tech lead `agents/` + `api/`.

### HIGH-02 — `web` y `api` siguen divergentes en reset password

**Evidencia:** `api/src/main/java/.../AuthController.java:57-65` expone `POST /api/v1/auth/reset-password` con `code` dentro del body; `web/src/lib/api/auth.ts:58-62` llama `PUT /api/v1/auth/reset-password?code=...`.

**Impacto:** el flujo de recuperacion de contrasena puede fallar aunque la documentacion ya refleje el contrato API actual.

**Correccion recomendada:** alinear `web` con el API actual o cambiar el API deliberadamente; agregar test de contrato para reset password.

**Responsable sugerido:** Frontend/API owner.

### MEDIUM-01 — La propiedad `app.agents.gemini.enabled` ya no expresa su comportamiento real

**Evidencia:** `agents/src/main/java/.../AssessmentConfig.java:23-51` indica que `app.agents.gemini.enabled` habilita toda la configuracion del Assessment Agent, incluyendo Gemini y Groq.

**Impacto:** no bloquea ejecucion, pero induce malentendidos al configurar el runtime provider/model-aware.

**Correccion recomendada:** renombrar o introducir una propiedad feature-level como `app.agents.assessment.enabled`, conservando compatibilidad temporal si hace falta.

**Responsable sugerido:** Tech lead `agents/`.

### MEDIUM-02 — Politica de idioma canonico pendiente

**Evidencia:** `docs/source-docs-refresh/audit-report.md` registra M-06; la documentacion activa mezcla espanol e ingles entre carpetas y secciones.

**Impacto:** no invalida el contenido tecnico, pero reduce consistencia editorial y dificulta reuso por agentes headless, implementadores y materiales de producto.

**Correccion recomendada:** crear una fase breve de normalizacion idiomatica con regla canonica por audiencia, glosario y conversion por carpetas.

**Responsable sugerido:** Documentation owner / Product owner.

### MEDIUM-03 — Evidencia real de despliegue sigue condicionando R06

**Evidencia:** `docs/master-plan/validation-report.md` mantiene D-01 como condicion para cerrar R06; `infra/terraform/environments/demo/` existe con recursos Terraform, pero el refresh documental no prueba que `demo` este aplicado ni que `beta` este actualmente live.

**Impacto:** no bloquea usar `docs/` como fuente, pero si bloquea declarar una release candidate o paquete final de validacion sin evidencia de entorno.

**Correccion recomendada:** ejecutar las guias smoke enlazadas desde `docs/README.md` y registrar evidencia de `beta`, `demo` o ambos segun la ADR de roles.

**Responsable sugerido:** Release Manager / Infra owner.

## Evidencia de validacion

| Check | Resultado |
|---|---|
| Enlaces relativos activos | PASS: `checked=204 missing=0`. |
| Grafo de navegacion desde `docs/README.md` | PASS: `active_md=203 reachable=203 orphans=0`. |
| Whitespace del diff | PASS: `git diff --check` sin salida. |
| Restricciones activas del evento | PASS: sin hits para `hackathon`, `jurado`, `judging`, `demo day`, `semifinal`, `evaluador externo`, `America's Seed Fund`, `Vertex AI Agent Builder` fuera de archivo historico/ADR. |
| Frases obsoletas buscadas | PASS: sin hits para terminos legacy de repo solo-documentacion, Gemini/GCP-only, links wiki, scaffolding-only o versiones V1/V2 en superficies canonicas. |

## Checklist de validaciones aprobadas

- Producto, negocio y evidencia distinguen Open + Closed y tratan Closed como P0 documentado.
- Closed grading permanece deterministico contra snapshot/answer key congelado.
- Human approval queda explicito para grading, feedback, reportes y publicaciones con impacto academico.
- El runtime esta documentado como capacidad transversal incremental, no como mega-release tecnica.
- `api/` conserva dominio, persistencia, estados, aprobaciones, billing y logs durables.
- `agents/` recibe command, retorna result/log y no persiste entidades de dominio directamente.
- READMEs e indices activos apuntan a archivos existentes.
- No quedan documentos Markdown activos huerfanos desde `docs/README.md`.
- El material del evento queda archivado y no impone scope, pricing, deployment ni evidencia activa.
- El Master Plan sigue siendo capa derivada de coordinacion; no reemplaza las fuentes tematicas.

## Checklist de validaciones pendientes

- Corregir `provider` efectivo en `AgentExecutionLog` cuando `agents/` resuelve default provider.
- Corregir contrato reset-password entre `web` y `api`.
- Renombrar o compatibilizar `app.agents.gemini.enabled`.
- Definir politica de idioma canonico y ejecutar normalizacion editorial.
- Capturar evidencia real de despliegue/smoke para `beta` y/o `demo` antes de cerrar R06.
- Agregar ejemplos reales de pilotos, uso, costos y revenue cuando existan datos.

## Riesgos residuales

| Riesgo | Manejo recomendado |
|---|---|
| Implementacion vuelve a omitir provider/model en evidencia | Gatear R01 con test de contrato e integracion para `AgentExecutionLog`. |
| Documentacion de usuario se adelanta a features no implementadas | Mantener notas de disponibilidad por release y actualizar al cerrar cada slice. |
| Idioma mixto dificulta ejecucion por agentes | Resolver M-06 antes de ampliar mas documentacion fuente. |
| `demo`/`beta` quedan sin evidencia de entorno | No declarar R06 release candidate sin smoke y prueba de despliegue. |

## Archivos afectados

- `docs/source-docs-refresh/validation-report.md`.
- `docs/99-decisions/2026-06-10-agent-runtime-separation.md`.
- `docs/master-plan/analysis/documentation-diagnosis.md`.
- `docs/02-product/user-stories/README.md`.
- `docs/README.md`.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados |
|---|---|---|---|
| 2026-07-20 | Creacion inicial del reporte de validacion final | Cierre de prompt 06 del source-docs-refresh | `validation-report.md` |
| 2026-07-20 | Correcciones menores de navegacion y narrativa residual | Eliminar link roto, huerfanos y referencia activa a restricciones del evento | ADR runtime, master-plan diagnosis, user stories README, docs README |

## Cierre obligatorio

1. **Resultado general:** PASS WITH CONDITIONS.
2. **Blockers residuales:** ninguno para documentacion fuente; R06 sigue condicionada por evidencia de despliegue y decisiones de entorno.
3. **Documentacion fuente confiable:** si, con condiciones sobre los hallazgos HIGH/MEDIUM listados.
4. **Correcciones menores aplicadas:** link ADR, narrativa residual del evento, links out-of-scope/template y links a smoke guides.
5. **Siguientes acciones recomendadas:** corregir `provider` efectivo en logs, alinear reset-password, renombrar feature flag de agents, definir idioma canonico y ejecutar smoke beta/demo antes de cerrar R06.
