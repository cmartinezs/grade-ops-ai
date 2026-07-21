# Identidad service-to-service

## Objetivo

Cloud Run debe requerir autenticación. `api/` obtiene un ID token OIDC con audience exacta de `agents/`; IAM concede `roles/run.invoker` solo a service accounts aprobadas. `agents/` valida issuer, audience, expiración y caller.

## Principal técnico

Construir `ServicePrincipal` con subject/service account, audience y capabilities derivadas de configuración server-side. No aceptar `caller`, `role` o scopes autoritativos desde JSON.

## Migración

1. Mantener `X-Internal-Key` solo local/transición.
2. Añadir OIDC en ambiente demo y logs de rechazo sin token.
3. Cambiar API client a identity token.
4. Hacer obligatorio OIDC en producción.
5. Eliminar secreto del tráfico de producción.

No devolver información que permita distinguir callers válidos. Usar `401` para credencial ausente/inválida y `403` para identidad válida sin capability. Nunca usar el default `change-me-in-production`; el arranque debe fallar si el modo secreto está activo y falta valor fuerte.

## Replay

OIDC reduce suplantación, pero no evita duplicación funcional. Exigir `operationId`, `runId`, deadline e idempotency/attempt ID; `api/` mantiene la semántica canónica y el runtime rechaza requests vencidos o duplicados según política acordada.
