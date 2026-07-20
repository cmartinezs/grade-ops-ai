# 05 — Sincronizacion de indices, decisiones y trazabilidad

Actua como Documentation Governance Lead y Architecture Decision Curator de GradeOps AI.

## Objetivo

Sincronizar indices, READMEs, decisiones y trazabilidad despues de actualizar la documentacion fuente, para que el repositorio sea navegable y mantenga una unica historia coherente.

## Entradas obligatorias

- `@docs/source-docs-refresh/audit-report.md`
- Documentos modificados en las fases 02, 03 y 04.
- `@docs/README.md`
- READMEs de carpetas bajo `@docs/`
- `@docs/99-decisions/`
- `@docs/master-plan/`

## Archivos que puede modificar

- `@docs/README.md`
- `@docs/CLAUDE.md`
- READMEs bajo:
  - `@docs/00-project/`
  - `@docs/01-business/`
  - `@docs/02-product/`
  - `@docs/03-ai-agents/`
  - `@docs/04-architecture/`
  - `@docs/05-evidence/`
  - `@docs/06-ux/`
  - `@docs/07-hackathon/`
  - `@docs/08-user-guide/`
  - `@docs/09-developer-guide/`
  - `@docs/10-best-practices/`
  - `@docs/99-decisions/`
- `@docs/99-decisions/`, si faltan ADRs necesarias.
- `@docs/source-docs-refresh/audit-report.md`, solo para marcar hallazgos abordados.

## Archivos que no puede modificar

- `@docs/master-plan/`
- `@docs/.raw/`
- `@docs/.prompting/`
- Codigo.

## Tareas obligatorias

### Indices

- Verificar que cada README lista documentos actuales.
- Corregir enlaces rotos o referencias a archivos renombrados.
- Agregar rutas nuevas necesarias para navegar por producto, arquitectura, agentes, evidencia y guias.
- Indicar que `docs/master-plan/` es plan ejecutivo derivado, no reemplazo de la documentacion fuente.

### Decisiones

- Verificar que decisiones vigentes estan enlazadas desde documentos que dependen de ellas.
- Crear ADRs si el refresh introdujo una decision normativa nueva.
- Evitar duplicar decisiones dentro de documentos narrativos sin enlazar ADR.
- Marcar decisiones antiguas como superseded solo si existe reemplazo explicito.

### Trazabilidad

- Conectar documentos fuente con releases o capacidades cuando ayude a ejecucion.
- Conectar agentes con workflows, evidencia y guias dev.
- Conectar eventos (`AgentExecutionLog`, `ApprovalEvent`, `UsageEvent`, `RevenueEvent`, `CostEvent`) con evidencia, arquitectura y negocio.

## Validaciones obligatorias

- No quedan links relativos rotos en documentos modificados.
- No quedan referencias a nombres obsoletos.
- No quedan documentos huerfanos relevantes.
- Las decisiones nuevas tienen fecha, contexto, decision, consecuencias y estado.

## Criterios de finalizacion

- Navegacion principal de `@docs/` queda coherente.
- Los READMEs reflejan la estructura real.
- Las ADRs necesarias existen o quedan listadas como pendientes bloqueantes.
- Los hallazgos abordados quedan marcados en `audit-report.md`.

## Cierre obligatorio

Finaliza indicando:

1. Indices actualizados.
2. ADRs creadas o modificadas.
3. Enlaces/trazabilidad agregados.
4. Hallazgos pendientes.
5. Prompt recomendado para continuar.
