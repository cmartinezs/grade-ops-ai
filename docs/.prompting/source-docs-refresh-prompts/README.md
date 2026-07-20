# GradeOps AI — Prompts para actualizar y enriquecer documentacion fuente

Este paquete contiene prompts para actualizar la documentacion de origen de `@docs/` cuando el Master Plan, el codigo o las decisiones recientes evidencian que quedo desactualizada o incompleta.

## Objetivo

Refrescar y enriquecer la documentacion fuente de GradeOps AI sin perder trazabilidad, separando:

- diagnostico de obsolescencia;
- actualizacion de contenido por dominio;
- enriquecimiento con decisiones ya aceptadas;
- sincronizacion de indices y referencias;
- validacion final.

La meta no es reescribir todo `@docs/`, sino llevar los documentos canonicos a un estado confiable para planificacion, implementacion y onboarding.

## Estructura

```text
source-docs-refresh-prompts/
├── README.md
├── 01-audit-source-docs-staleness.md
├── 02-refresh-product-business-evidence.md
├── 03-refresh-architecture-agent-runtime.md
├── 04-refresh-guides-ux-and-developer-docs.md
├── 05-sync-indexes-decisions-and-traceability.md
└── 06-validate-source-docs-refresh.md
```

## Orden de ejecucion

1. Ejecutar `01-audit-source-docs-staleness.md`.
2. Revisar blockers y decidir si algun documento requiere una decision nueva antes de editar.
3. Ejecutar `02-refresh-product-business-evidence.md`.
4. Ejecutar `03-refresh-architecture-agent-runtime.md`.
5. Ejecutar `04-refresh-guides-ux-and-developer-docs.md`.
6. Ejecutar `05-sync-indexes-decisions-and-traceability.md`.
7. Ejecutar `06-validate-source-docs-refresh.md`.

## Fuentes de contraste

- `@docs/master-plan/`
- `@docs/master-plan/analysis/agent-runtime-strategy.md`
- `@docs/.prompting/master-plan-runtime/`
- `@docs/99-decisions/`
- codigo existente en `api/`, `agents/`, `web/` e `infra/`, cuando corresponda
- `CHANGELOG.md`, PDRs o documentos de planificacion locales si existen

## Targets permitidos

Los prompts pueden actualizar documentos fuente bajo:

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
- `@docs/README.md`
- `@docs/CLAUDE.md`

## Targets no permitidos

- No editar `@docs/.raw/`; es evidencia historica.
- No editar `@docs/master-plan/` desde este paquete; se usa como contraste, no como destino.
- No editar `@docs/.prompting/` durante la ejecucion normal de estos prompts.
- No actualizar codigo desde estos prompts.

## Reglas principales

- Toda modificacion debe estar respaldada por evidencia local: documento, decision, master plan, runtime strategy o codigo.
- Las afirmaciones especulativas deben quedar como supuestos, riesgos o preguntas abiertas.
- Si una decision de producto o arquitectura no existe, crear o proponer ADR antes de reescribir documentos dependientes.
- No eliminar contenido historico util sin reemplazarlo por una version mas precisa o enlazarlo a la decision vigente.
- Mantener separada la autoridad: `api/` conserva dominio y persistencia; `agents/` ejecuta runtime y asistencia IA; `web/` consume contratos API.
- La documentacion enriquecida debe ser accionable: incluir contratos, limites, ejemplos, criterios verificables, responsables o enlaces cuando correspondan.

## Resultado esperado

El refresh debe dejar:

- documentos fuente actualizados y coherentes con el Master Plan;
- decisiones nuevas o actualizadas cuando haya cambios normativos;
- indices y enlaces sincronizados;
- reporte de validacion con hallazgos residuales;
- lista explicita de documentos que siguen pendientes y por que.
