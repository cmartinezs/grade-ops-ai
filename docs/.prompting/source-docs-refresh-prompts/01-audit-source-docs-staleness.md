# 01 — Auditoria de obsolescencia de documentacion fuente

Actua como Documentation Architect, Product Auditor y Principal Engineer de GradeOps AI.

## Objetivo

Detectar que documentacion fuente de `@docs/` esta desactualizada, incompleta, duplicada o contradice el Master Plan, la estrategia de Agent Runtime, decisiones vigentes o codigo existente.

No actualices documentos de contenido en esta fase. Primero produce un inventario de hallazgos con evidencia.

## Entradas obligatorias

- `@docs/README.md`
- `@docs/00-project/`
- `@docs/01-business/`
- `@docs/02-product/`
- `@docs/03-ai-agents/`
- `@docs/04-architecture/`
- `@docs/05-evidence/`
- `@docs/06-ux/`
- `@docs/archive/2026-event/07-hackathon/`
- `@docs/08-user-guide/`
- `@docs/09-developer-guide/`
- `@docs/10-best-practices/`
- `@docs/99-decisions/`
- `@docs/master-plan/`
- `@docs/.prompting/master-plan-runtime/`
- codigo existente en `api/`, `agents/`, `web/` e `infra/`, si existe en el checkout.

## Archivos que puede crear

- `@docs/source-docs-refresh/audit-report.md`

## Archivos que puede modificar

- Unicamente `@docs/source-docs-refresh/audit-report.md`.

## Archivos que no puede modificar

- Cualquier otro archivo de `@docs/`.
- Codigo.
- Prompts.

## Validaciones obligatorias

### Obsolescencia

- Detectar afirmaciones que contradicen el Master Plan.
- Detectar documentos que omiten decisiones ya aceptadas.
- Detectar referencias a arquitectura, runtime, agentes, auth, despliegue o pricing que ya no coinciden.
- Detectar documentos que prometen capacidades fuera del MVP sin marcarlas como roadmap.

### Enriquecimiento requerido

- Identificar documentos demasiado superficiales para guiar implementacion.
- Identificar documentos sin contratos, ejemplos, flujo, estados, guardrails o criterios verificables.
- Identificar documentos que requieren tablas, diagramas, trazabilidad o decision links.

### Consistencia

- Verificar nombres canonicos de producto, agentes, assessment modes y eventos.
- Verificar separacion `api/`, `agents/`, `web/` e `infra/`.
- Verificar que Closed assessment mantiene grading deterministico.
- Verificar que outputs IA con impacto academico requieren aprobacion docente.
- Verificar que evidencia, costos, uso y revenue son first-class.

### Codigo

Cuando haya codigo disponible:

- Identificar implementaciones no documentadas.
- Identificar documentacion que describe codigo inexistente.
- Identificar contratos documentados que no coinciden con DTOs, endpoints o recursos Terraform.

## Severidad

- BLOCKER: impide usar los docs como fuente confiable para planificar o implementar.
- HIGH: induce arquitectura o alcance incorrecto.
- MEDIUM: falta detalle necesario para ejecucion consistente.
- LOW: mejora de claridad, enlaces o ejemplos.
- INFORMATIONAL: contexto sin accion inmediata.

## Contenido de `audit-report.md`

- Resumen ejecutivo.
- Resultado general: PASS, PASS WITH CONDITIONS o FAIL.
- Mapa de documentos revisados.
- Hallazgos por severidad.
- Evidencia local por hallazgo.
- Documento afectado.
- Fuente de verdad usada para contrastar.
- Tipo de accion recomendada: actualizar, enriquecer, dividir, enlazar, crear ADR, eliminar duplicado o dejar como roadmap.
- Orden recomendado de ejecucion.
- Riesgos residuales.
- Preguntas abiertas.

## Criterios de finalizacion

- Cada hallazgo tiene evidencia concreta.
- Cada hallazgo tiene documento afectado y accion recomendada.
- Se separa obsolescencia de enriquecimiento.
- Se identifica si falta una ADR antes de editar.
- Se indica que prompt debe ejecutarse despues.

## Cierre obligatorio

Finaliza indicando:

1. Resultado general.
2. Numero de blockers.
3. Primer grupo de documentos a actualizar.
4. ADRs requeridas antes de editar.
5. Prompt recomendado para continuar.
