# Resumen general

El runtime actual expone un único endpoint interno de Assessment Agent. Un filtro exige `X-Internal-Key`, el orquestador valida campos, selecciona provider, construye un prompt versionado, llama al LLM, valida estructura y devuelve resultado más evidencia. No persiste dominio.

La principal brecha es confiar en un secreto compartido estático. Cualquier poseedor puede invocar cualquier `/internal/**`, elegir provider/model y consumir capacidad sin identidad verificable, scopes, presupuesto por request o replay protection.

El objetivo es:

- Cloud Run privado + OIDC service-to-service.
- Identidad del caller y allowlist de audiencias/service accounts.
- Comandos firmemente tipados con `operationId`, `runId` y policy emitida por API.
- Capabilities por agente, proveedor, modelo y herramientas.
- Límites de pasos, tokens, costo, duración, tamaño y concurrencia.
- Protección contra prompt injection y output no confiable.
- Egress y secretos mínimos.

RBAC humano no pertenece aquí: Teacher/Operator se resuelven en `api/` antes del despacho.
