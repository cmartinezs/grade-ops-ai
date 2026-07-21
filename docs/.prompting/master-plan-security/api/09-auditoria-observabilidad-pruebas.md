# Auditoría, observabilidad y pruebas

## Auditoría mínima

Toda acción privilegiada o sensible debe registrar:

```text
actor_uid
actor_roles
authentication_type
action
resource_type
resource_id
organization_id / teacher_uid scope
decision (allowed/denied)
outcome
timestamp
correlation_id
source_service cuando corresponda
```

No deben registrarse tokens, secretos, enlaces completos de estudiante, prompts sensibles ni PII innecesaria.

## Eventos relevantes

- Concesión y revocación de roles.
- Activación/bloqueo de cuentas.
- Aprovisionamiento de Teacher.
- Cambio de flags del piloto.
- Intentos de acceso cruzado.
- Uso de enlaces expirados o revocados.
- Denegación de una identidad de servicio.
- Exports y consultas de evidencia/costos.

## Suite mínima de seguridad

1. Sin token → `401`.
2. Token inválido/revocado → `401`.
3. Cuenta deshabilitada → acceso denegado.
4. Teacher con authority correcta y recurso propio → permitido.
5. Teacher sin permiso → `403`.
6. Teacher sobre recurso ajeno → `404`.
7. Operator sobre endpoint Teacher sin permiso académico → `403`.
8. Teacher sobre endpoint Operator → `403`.
9. Rol revocado → acceso denegado tras el SLA definido.
10. `setBy` no aceptado desde el cliente.
11. Auditoría atribuye el principal real.
12. Invitación expirada/revocada → denegada.
13. Invitación de otro assessment → denegada.
14. Endpoint interno sin identidad válida → denegado.
15. Service account válida con audience incorrecta → denegada.

## Estrategia de pruebas

- Unitarias para matriz Role → Permission.
- Unitarias para transiciones de cuenta/rol.
- Slice tests de Spring Security y `@PreAuthorize`.
- Integración con repositorios scoped.
- Pruebas de contrato HTTP para `401/403/404`.
- Pruebas de concurrencia e idempotencia en submissions.
- Pruebas end-to-end con tokens Firebase emulados o dobles controlados.
- Pruebas negativas obligatorias en CI.
