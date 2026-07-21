# Estrategia incremental

## Fase 1 — Fundaciones

- Crear `AuthProvider`, sesión de plataforma y estados explícitos.
- Normalizar `apiClient`, errores y correlation ID.
- Añadir pruebas negativas al baseline actual.

## Fase 2 — Capabilities Teacher

- Incorporar `/api/v1/me`.
- Navegación/componentes basados en permisos.
- Ownership sigue exclusivamente en API.
- Idempotencia para operaciones GenAI.

## Fase 3 — Hardening HTTP

- CSP en report-only y luego enforcement.
- HSTS, nosniff, referrer y permissions policy.
- Auditoría de dependencias y render de contenido AI.
- Tareas `infra/` para validar headers en Cloud Run/hosting.

## Fase 4 — Operator

- Layout `/operator/**`, permisos mínimos y confirmaciones.
- Eliminar `setBy` confiado desde cliente.
- E2E cruzados Teacher/Operator.

## Fase 5 — Student

- Acceso firmado, canje/sesión scoped y protección anti-filtración.
- Estados de expiración/revocación/publicación.
- Pruebas de replay y acceso cruzado.

Cada fase debe desplegar valor verificable; no mezclar migración BFF con roles, Operator y Student en una sola release.
