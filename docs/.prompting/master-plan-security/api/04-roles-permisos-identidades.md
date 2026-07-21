# Roles, permisos e identidades

## Roles humanos del MVP

| Rol | Propósito |
|---|---|
| `TEACHER` | Gestionar recursos académicos propios y revisar resultados GenAI |
| `OPERATOR` | Operar el piloto, aprovisionar cuentas y consultar evidencia operacional autorizada |

No se recomienda introducir todavía `ADMIN`, `REVIEWER`, `INSTITUTION_ADMIN` o `STUDENT`: no existe evidencia funcional suficiente que justifique su semántica y aumentaría la complejidad del modelo.

## Permisos de Teacher

```text
assessment:read
assessment:create
assessment:update
assessment:generate
assessment:approve
submission:manage
grading:review
feedback:approve
report:read
ai-operation:read
ai-operation:execute
```

Todos permanecen condicionados al ownership o scope del recurso.

## Permisos de Operator

```text
teacher:provision
teacher:pilot-flags:update
evidence:read
evidence:export
cost:read
revenue:read
pilot:manage
system-health:read
```

`OPERATOR` no debe recibir automáticamente acceso a assessments, submissions, calificaciones o feedback individual. Los paneles operacionales deben preferir información agregada y minimizada.

## Persistencia recomendada

### `user_accounts`

```text
firebase_uid
email
status
created_at
updated_at
last_authenticated_at
```

### `account_roles`

```text
firebase_uid
role
granted_by
granted_at
revoked_by
revoked_at
```

Restricciones mínimas:

```text
UNIQUE(firebase_uid, role)
role IN ('TEACHER', 'OPERATOR')
```

No se recomienda agregar un único `role VARCHAR` a `teacher`: mezcla el perfil docente con identidad/autorización, impide múltiples roles y dificulta futuras relaciones organizacionales.
