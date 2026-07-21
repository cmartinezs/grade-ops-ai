# ADR - Seguridad y autorizacion incorporadas por release funcional

## Estado

Aceptada.

## Contexto

El paquete `docs/.prompting/master-plan-security/` identifica brechas y modelo objetivo para `api/`, `agents/` y `web`.

La necesidad de seguridad es transversal, pero crear una release tecnica aislada retrasaria valor funcional y tenderia a desconectar RBAC, service-to-service, rutas web, ownership, signed links, exports y observabilidad de los flujos reales que deben proteger.

El paquete actualizado tambien separa la topologia de `demo` y `beta`: `demo` opera sobre GCP/Cloud Run/IAM/OIDC, mientras `beta` opera sobre Vercel/Render/Neon y requiere controles equivalentes sin asumir que Render expone IAM de Cloud Run.

## Decision

La seguridad se incorpora por release funcional y no como una release transversal independiente.

- `api/` es la fuente canonica de roles, permisos, ownership, estado de dominio, auditoria y proteccion de endpoints.
- `agents/` no implementa RBAC humano; protege identidad service-to-service, capabilities, providers, prompts, outputs, budgets, herramientas y limites de ejecucion.
- `web/` no autoriza recursos; consume capacidades de `api/`, minimiza tokens/PII y representa rutas/estados seguros.
- El contrato funcional de autorizacion debe comportarse igual en `demo` y `beta`, aunque el mecanismo service-to-service sea OIDC/IAM en `demo` y JWT interno firmado/rotatorio en `beta`.
- Cada build/deploy se asocia a un unico ambiente; no se permite mezclar Firebase project, API URL, DB, storage, secrets o service identity entre ambientes.
- Cada tarea que toque endpoints, rutas, agentes, prompts, providers, secrets, uploads, signed links, exports o datos sensibles debe incluir un gate de seguridad con pruebas negativas.
- Todo cambio de hosting, headers, IAM, Secret Manager, Cloud Run privado, Render/Vercel/Neon config, egress o service accounts debe tener tarea `infra/` o manifiesto versionado equivalente.

## Consecuencias

- R01 debe cerrar las fundaciones minimas de identidad, roles/cuenta, ownership, actor auditado y fail-secure.
- R02-R05 profundizan permisos, datos sensibles, signed links, attempts, snapshots y agents segun el flujo funcional.
- R06 concentra Operator, dashboards, exports, health, evidence access y prueba de aislamiento multiambiente sin convertirlo en consola admin generica.
- Las releases pueden dejar residuales aceptados, pero no pueden presentar controles propuestos como implementados.

## Fuentes

- `docs/.prompting/master-plan-security/api/`
- `docs/.prompting/master-plan-security/api/12-topologia-seguridad-multiambiente.md`
- `docs/.prompting/master-plan-security/agents/`
- `docs/.prompting/master-plan-security/agents/11-topologia-seguridad-multiambiente.md`
- `docs/.prompting/master-plan-security/web/`
- `docs/.prompting/master-plan-security/web/11-topologia-seguridad-multiambiente.md`
- `docs/master-plan/analysis/security-strategy.md`
