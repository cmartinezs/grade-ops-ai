# Estudiantes y enlaces firmados

## Decisión de alcance

El estudiante no debe transformarse en usuario Firebase ni recibir `ROLE_STUDENT` en el MVP. El modelo aprobado utiliza un enlace firmado o token opaco limitado a una invitación específica.

## Contexto mínimo derivado

```text
invitationId
assessmentId
learnerRefId
allowedActions
expiresAt
nonce/tokenVersion
```

Las acciones derivadas podrían ser:

```text
assessment-response:view
assessment-response:submit
published-result:view
```

No son authorities globales reutilizables: están ligadas a una invitación y recurso concretos.

## Validaciones obligatorias

- Firma o hash válido.
- Expiración.
- Estado de revocación.
- Nonce o versión vigente.
- Assessment publicado.
- Intentos disponibles.
- Correspondencia con `LearnerRef`.
- Acción incluida en el alcance.
- Resultado publicado antes de permitir lectura.

El contexto de estudiante jamás debe autorizar endpoints Teacher bajo `/api/v1/assessments/**`.

## Amenazas principales

- Reutilización de enlaces.
- Enumeración de invitaciones.
- Token expuesto en logs o analytics.
- Acceso después de revocación.
- Escalada desde una invitación hacia otro assessment.
- Presentación duplicada por carrera entre solicitudes.

El plan debe incluir tokens de alta entropía, almacenamiento seguro de hashes, expiración, invalidación y operaciones idempotentes.
