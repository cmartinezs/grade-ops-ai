# Auditoría, pruebas y respuesta

## Evidencia mínima

`correlationId`, `operationId`, `runId`, attempt, caller técnico, agent/version, schemas, provider/model, policy aplicada, latencia, tokens, costo, hashes, estado/error normalizado y validación. Pseudonimizar tenant/usuario y no registrar payloads.

## Pruebas

- Sin token OIDC, audience incorrecta, caller no permitido.
- Secreto default/ausente impide arranque cuando aplique.
- Capability incorrecta para endpoint/provider/tool.
- Payload oversized, strings enormes y campos desconocidos.
- Provider/model no allowlisted.
- Deadline/budget/steps agotados.
- Prompt injection en brief y draft previo.
- JSON malformado, output enorme o semánticamente inválido.
- Timeout, 429 y 5xx del provider con clasificación correcta.
- Duplicado/replay y concurrencia.
- Confirmar que errores/logs no contienen prompt ni secret.

## Respuesta a incidentes

Poder deshabilitar provider/model/capability mediante configuración controlada, revocar service account/secreto, detener dispatch desde API y correlacionar runs afectados. Preservar evidencia sin conservar datos sensibles innecesarios.
