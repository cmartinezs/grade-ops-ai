# 03 — Refresh de arquitectura, agentes y runtime

Actua como Principal Architect, Agent Runtime Architect y Security Reviewer de GradeOps AI.

## Objetivo

Actualizar y enriquecer la documentacion fuente de arquitectura, agentes, seguridad, despliegue y decisiones tecnicas para reflejar el runtime agentic headless definido para GradeOps AI.

## Entradas obligatorias

- `@docs/source-docs-refresh/audit-report.md`
- `@docs/master-plan/analysis/agent-runtime-strategy.md`
- `@docs/.prompting/master-plan-runtime/`
- `@docs/master-plan/releases/`
- `@docs/99-decisions/2026-06-10-agent-runtime-separation.md`
- `@docs/03-ai-agents/`
- `@docs/04-architecture/`
- `@docs/09-developer-guide/06-agent-development.md`
- codigo existente en `api/`, `agents/`, `web/` e `infra/`, cuando exista.

## Archivos que puede modificar

- `@docs/03-ai-agents/`
- `@docs/04-architecture/`
- `@docs/09-developer-guide/06-agent-development.md`
- `@docs/09-developer-guide/03-api-reference.md`, si hay contratos afectados.
- `@docs/09-developer-guide/04-security-implementation.md`, si hay seguridad afectada.
- `@docs/09-developer-guide/09-deployment-guide.md`, si hay despliegue afectado.
- `@docs/99-decisions/`, solo para nuevas ADRs o actualizaciones justificadas.
- `@docs/source-docs-refresh/audit-report.md`, solo para marcar hallazgos abordados.

## Archivos que no puede modificar

- `@docs/master-plan/`
- `@docs/.raw/`
- `@docs/.prompting/`
- Codigo.

## Actualizaciones obligatorias

### Runtime

- Documentar la separacion entre API, runtime, agente especializado y modelo GenAI.
- Distinguir vertical slice actual de Assessment Agent versus runtime generico objetivo.
- Explicar que el runtime crece incrementalmente por releases funcionales.
- Incluir capacidades esperadas: `AgentDefinition`, registry, gateway, tool loop, policies, budget, validation, logs, estados y observabilidad.
- Explicitar que `AgentRun`/`AgentStep`, async, cancelacion y reanudacion se incorporan solo cuando exista necesidad funcional.

### Agentes

- Actualizar catalogo de 13 agentes y su pertenencia a Open o Closed.
- Para cada agente, documentar command, result, inputs, outputs, validadores, autonomia, bloqueos y aprobacion humana.
- Evitar describir agentes como autoridad de dominio o persistencia.
- Separar hechos, inferencias, recomendaciones y acciones.

### Arquitectura

- Mantener `api/` como autoridad de dominio, workflow, billing, persistence y aprobaciones.
- Mantener `agents/` como servicio interno no publico.
- Mantener prompts versionados como archivos, no inline en Java.
- Incluir implicancias de Terraform cuando se introducen servicios o recursos.

### Seguridad y politicas

- Documentar herramientas permitidas, policy engine, limites de tokens/costo/tiempo/pasos/reintentos.
- Documentar sandbox solo para codigo no confiable si el scope lo requiere.
- Documentar OIDC service-to-service, secretos server-side y no exposicion de Gemini API key al frontend.

## Reglas de edicion

- No generalizar runtime sin consumidor funcional real.
- No mover autoridad academica o financiera desde API hacia agents.
- No incorporar memoria vectorial, RAG, multiagente o nuevos proveedores sin decision explicita.
- Si una decision tecnica cambia una ADR vigente, actualizar la ADR o crear una nueva.

## Criterios de finalizacion

- La documentacion de agentes y arquitectura coincide con el runtime strategy.
- Las guias dev explican como agregar o modificar agentes sin inventar patrones.
- Las restricciones de seguridad y autonomia estan visibles.
- Los hallazgos abordados quedan marcados en `audit-report.md`.

## Cierre obligatorio

Finaliza indicando:

1. Documentos actualizados.
2. Cambios de arquitectura incorporados.
3. ADRs nuevas o actualizadas.
4. Riesgos pendientes.
5. Prompt recomendado para continuar.
