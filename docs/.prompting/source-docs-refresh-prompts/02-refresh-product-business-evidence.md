# 02 — Refresh de producto, negocio y evidencia

Actua como Product Strategy Lead, Business Architect y Evidence Governance Lead de GradeOps AI.

## Objetivo

Actualizar y enriquecer la documentacion fuente de producto, negocio, evidencia y validacion MVP para que refleje el Master Plan, las decisiones vigentes y la estrategia de entrega incremental.

## Entradas obligatorias

- `@docs/source-docs-refresh/audit-report.md`
- `@docs/master-plan/master-plan-executive.md`
- `@docs/master-plan/analysis/capability-map.md`
- `@docs/master-plan/analysis/user-story-inventory.md`
- `@docs/master-plan/analysis/release-strategy.md`
- `@docs/master-plan/releases/`
- `@docs/99-decisions/`
- Documentos actuales bajo:
  - `@docs/00-project/`
  - `@docs/01-business/`
  - `@docs/02-product/`
  - `@docs/05-evidence/`
  - `@docs/archive/2026-event/07-hackathon/`

## Archivos que puede modificar

- `@docs/00-project/`
- `@docs/01-business/`
- `@docs/02-product/`
- `@docs/05-evidence/`
- `@docs/archive/2026-event/07-hackathon/`
- `@docs/source-docs-refresh/audit-report.md`, solo para marcar hallazgos abordados.

## Archivos que no puede modificar

- `@docs/master-plan/`
- `@docs/.raw/`
- `@docs/.prompting/`
- Codigo.

## Actualizaciones obligatorias

### Producto

- Reflejar los dos modos de assessment: Open y Closed.
- Mantener Closed grading como deterministico contra answer key congelada.
- Reflejar Student access por signed token links, sin student login en MVP.
- Explicitar teacher approval para outputs IA con impacto academico.
- Alinear user stories y workflows con releases ejecutables.

### Negocio

- Alinear pricing, go-to-market y business model con evidencia, usage events, revenue events y cost events.
- Incluir related-party revenue como dato requerido cuando aplique a evidencia de validacion MVP.
- Separar MVP, validacion MVP target y roadmap posterior.

### Evidencia

- Enriquecer metricas de uso, revenue, costos, agent logs y testimonials con estructura verificable.
- Definir que evidencia debe capturarse automaticamente y que evidencia puede cargarse manualmente.
- Conectar evidencia con releases R01-R06 cuando corresponda.

### Validacion MVP

- Alinear demo script, checklist y submission narrative con vertical slices reales.
- Evitar prometer capacidades no implementadas.
- Identificar prerequisitos operativos para demo.

## Reglas de edicion

- Reescribir secciones obsoletas completas si una actualizacion puntual dejaria contradicciones.
- Mantener historial o decisiones antiguas solo si se distinguen como contexto.
- Agregar ejemplos concretos cuando el documento deba guiar implementacion o demo.
- No duplicar tablas largas si existe una fuente mas especifica; enlazarla.
- Si aparece una decision nueva, crear o proponer ADR en vez de ocultarla dentro de un documento de producto.

## Criterios de finalizacion

- Los documentos modificados ya no contradicen el Master Plan.
- Cada documento enriquecido incluye suficiente detalle para ejecutar o validar.
- Los hallazgos abordados quedan marcados en `audit-report.md`.
- Las decisiones pendientes quedan listadas, no resueltas implicitamente.

## Cierre obligatorio

Finaliza indicando:

1. Documentos actualizados.
2. Hallazgos cerrados.
3. Hallazgos pendientes.
4. ADRs nuevas o recomendadas.
5. Prompt recomendado para continuar.
