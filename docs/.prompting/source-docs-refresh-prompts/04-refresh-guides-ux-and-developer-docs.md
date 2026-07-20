# 04 — Refresh de guias, UX y documentacion para desarrollo

Actua como Documentation Lead, UX Systems Lead y Developer Experience Lead de GradeOps AI.

## Objetivo

Actualizar y enriquecer guias de usuario, UX, desarrollo, testing y buenas practicas para que sean accionables y coherentes con las funcionalidades planificadas y la arquitectura vigente.

## Entradas obligatorias

- `@docs/source-docs-refresh/audit-report.md`
- `@docs/master-plan/master-plan-executive.md`
- `@docs/master-plan/releases/`
- `@docs/02-product/workflows.md`
- `@docs/02-product/student-access.md`
- `@docs/03-ai-agents/`
- `@docs/04-architecture/`
- `@docs/06-ux/`
- `@docs/08-user-guide/`
- `@docs/09-developer-guide/`
- `@docs/10-best-practices/`
- codigo existente en `web/`, `api/`, `agents/` e `infra/`, cuando exista.

## Archivos que puede modificar

- `@docs/06-ux/`
- `@docs/08-user-guide/`
- `@docs/09-developer-guide/`
- `@docs/10-best-practices/`
- `@docs/source-docs-refresh/audit-report.md`, solo para marcar hallazgos abordados.

## Archivos que no puede modificar

- `@docs/master-plan/`
- `@docs/.raw/`
- `@docs/.prompting/`
- Codigo.

## Actualizaciones obligatorias

### UX

- Alinear screen inventory con releases funcionales.
- Separar experiencia docente y estudiante.
- Reflejar no student login en MVP.
- Incluir estados esperados: draft, generated, needs review, approved, published, blocked y error cuando correspondan.
- Documentar revision docente de outputs IA sin prometer automatizacion total.

### Guia de usuario

- Enriquecer flujos Open y Closed con pasos verificables.
- Aclarar que Closed se califica deterministicamente.
- Aclarar donde interviene IA, donde interviene docente y donde interviene software deterministico.
- Evitar instrucciones de UI que dependan de pantallas no planificadas.

### Guia de desarrollo

- Sincronizar setup local, API reference, database guide, agent development, testing y deployment con arquitectura actual.
- Incluir contratos, recursos, migraciones, logging, testing y guardrails cuando correspondan.
- Asegurar que tipos fluyen desde API hacia Web.
- Asegurar que prompts viven en archivos versionados.

### Buenas practicas

- Enriquecer checklists operativos con criterios verificables.
- Agregar ejemplos concretos de review, testing, observabilidad y seguridad cuando falten.
- Mantener recomendaciones alineadas al stack real.

## Reglas de edicion

- No inventar pantallas, endpoints o comandos.
- Si una guia depende de una funcionalidad futura, marcarla como roadmap o prerequisito.
- Preferir ejemplos cortos y concretos sobre instrucciones abstractas.
- Mantener consistencia de nombres con docs de producto y arquitectura.

## Criterios de finalizacion

- Las guias permiten ejecutar, revisar o validar flujos reales.
- UX y user guide no contradicen MVP ni roadmap.
- Developer guide no contradice codigo ni decisiones vigentes.
- Los hallazgos abordados quedan marcados en `audit-report.md`.

## Cierre obligatorio

Finaliza indicando:

1. Documentos actualizados.
2. Guias enriquecidas.
3. Contradicciones resueltas.
4. Pendientes por falta de codigo o decision.
5. Prompt recomendado para continuar.
