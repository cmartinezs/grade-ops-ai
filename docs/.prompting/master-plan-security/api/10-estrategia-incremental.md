# Estrategia incremental

## Fase 1 — Fundaciones

- Crear `PlatformRole` y `Permission`.
- Definir matriz central Role → Permission.
- Crear migraciones de `user_accounts` y `account_roles` o una primera variante compatible.
- Introducir `AuthenticatedAccount`.
- Mantener compatibilidad con Teacher existente.

## Fase 2 — Authorities y seguridad de métodos

- Cargar roles/permisos tras verificar Firebase.
- Construir `Authentication` con authorities reales.
- Habilitar `@EnableMethodSecurity`.
- Agregar pruebas de autenticación y traducción de permisos.

## Fase 3 — Protección de superficie Teacher

- Inventariar todos los controladores.
- Asignar una authority por intención funcional.
- Aplicar `@PreAuthorize`.
- Convertir repositorios a consultas scoped.
- Preservar `404` ante acceso horizontal.

## Fase 4 — Operator

- Crear endpoints `/api/v1/operator/**`.
- Aprovisionar identidades Operator.
- Eliminar `setBy` de requests.
- Registrar auditoría desde el principal.
- Restringir acceso académico por mínimo privilegio.

## Fase 5 — Seguridad interna

- Clasificar consumidores de `/internal/**`.
- Introducir OIDC y service accounts.
- Validar audience e identidad.
- Mantener secreto solo en perfil local temporal.
- Eliminar inconsistencias de headers.

## Fase 6 — Invitaciones y hardening

- Formalizar el contexto de invitación.
- Implementar expiración/revocación/nonce.
- Añadir rate limiting en rutas públicas.
- Completar auditoría, métricas y alertas.
- Ejecutar revisión de amenazas y pruebas negativas.

## Condición de salida

La migración no se considera terminada solo porque existan anotaciones. Debe demostrarse que:

- Un Teacher no accede a recursos de otro.
- Operator no obtiene PII académica por defecto.
- Roles revocados dejan de autorizar dentro del SLA.
- Ningún cliente puede declarar al actor auditado.
- Los servicios se atribuyen individualmente.
- Las rutas públicas y de invitación tienen límites y semántica coherente.
