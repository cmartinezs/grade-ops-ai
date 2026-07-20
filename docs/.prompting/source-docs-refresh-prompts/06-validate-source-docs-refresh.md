# 06 — Validacion final del refresh de documentacion fuente

Actua como Principal Documentation Auditor, Product Governance Reviewer y Release Readiness Lead de GradeOps AI.

## Objetivo

Validar que la documentacion fuente actualizada sea coherente, accionable, trazable y compatible con el Master Plan, decisiones vigentes y codigo existente.

No reescribas todo automaticamente. Primero genera un informe de validacion final y solo aplica correcciones menores si estan claramente acotadas.

## Entradas obligatorias

- `@docs/source-docs-refresh/audit-report.md`
- Todo `@docs/` excepto `@docs/.raw/`
- `@docs/master-plan/`
- `@docs/.prompting/master-plan-runtime/`
- codigo existente en `api/`, `agents/`, `web/` e `infra/`, cuando exista.

## Archivos que puede crear

- `@docs/source-docs-refresh/validation-report.md`

## Archivos que puede modificar

- `@docs/source-docs-refresh/validation-report.md`
- Correcciones menores en documentos fuente solo si cumplen todas estas condiciones:
  - son enlaces rotos, typos, indices o inconsistencias obvias;
  - no cambian decisiones de producto o arquitectura;
  - quedan registradas en el reporte.

## Archivos que no puede modificar

- `@docs/master-plan/`
- `@docs/.raw/`
- `@docs/.prompting/`
- Codigo.

## Validaciones obligatorias

### Coherencia fuente

- Producto, arquitectura, agentes, evidencia, UX y guias no se contradicen.
- Los documentos distinguen MVP, hackathon target y roadmap.
- Las afirmaciones normativas tienen decision o fuente clara.

### Runtime y agentes

- El runtime se documenta como capacidad transversal incremental.
- La API conserva autoridad de dominio, persistencia, aprobaciones y billing.
- Los agentes reciben command, retornan result y no persisten entidades de dominio directamente.
- Closed grading permanece deterministico.
- Human approval queda explicito para impactos academicos.

### Navegacion

- READMEs e indices apuntan a archivos existentes.
- No hay documentos relevantes huerfanos.
- Enlaces relativos funcionan.

### Accionabilidad

- Los documentos enriquecidos tienen suficiente detalle para guiar implementacion.
- Hay contratos, estados, guardrails, ejemplos o criterios verificables cuando corresponda.
- Los documentos no prometen automatizaciones sin trigger, inputs, outputs y limites.

### Contraste con codigo

Cuando haya codigo:

- Identificar documentacion que sigue describiendo codigo inexistente.
- Identificar codigo relevante no documentado.
- Identificar contratos o comandos divergentes.

## Severidad de hallazgos

- BLOCKER.
- HIGH.
- MEDIUM.
- LOW.
- INFORMATIONAL.

## Contenido de `validation-report.md`

- Resumen ejecutivo.
- Resultado general: PASS, PASS WITH CONDITIONS o FAIL.
- Documentos revisados.
- Cambios menores aplicados, si los hubo.
- Hallazgos residuales por severidad.
- Evidencia.
- Archivos afectados.
- Correccion recomendada.
- Responsable sugerido.
- Riesgos residuales.
- Checklist de validaciones aprobadas.
- Checklist de validaciones pendientes.
- Historial de cambios.

## Criterios de finalizacion

- Todo hallazgo residual tiene evidencia.
- No se modificaron decisiones o arquitectura durante la validacion sin ADR.
- Se declara si `@docs/` puede volver a usarse como fuente confiable.
- Se indica si el Master Plan requiere sincronizacion posterior.

## Cierre obligatorio

Finaliza indicando:

1. Resultado general.
2. Blockers residuales.
3. Documentacion fuente confiable: si/no/con condiciones.
4. Correcciones menores aplicadas.
5. Siguientes acciones recomendadas.
