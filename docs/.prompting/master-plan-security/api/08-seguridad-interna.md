# Endpoints internos y service-to-service

## Separación requerida

Actualmente `/internal/teachers/**` combina operaciones potencialmente humanas con un secreto compartido. Deben distinguirse dos superficies.

## Operaciones humanas de Operator

Ruta recomendada:

```text
POST  /api/v1/operator/teachers
PATCH /api/v1/operator/teachers/{uid}/pilot-flags
GET   /api/v1/operator/evidence
GET   /api/v1/operator/costs
```

Cada endpoint se protege con la authority correspondiente. El actor se obtiene del principal:

```java
command.setBy(authenticatedAccount.uid());
```

`setBy` debe eliminarse del contrato de entrada público.

## Comunicación entre servicios

`/internal/**` debe reservarse para identidades de servicio:

- OIDC de Cloud Run.
- Service account distinta por consumidor.
- IAM `roles/run.invoker`.
- Audience validada.
- Allowlist de servicios autorizados cuando corresponda.
- Sin acceso desde navegador/CORS.

El secreto compartido puede conservarse temporalmente solo para desarrollo local, con configuración explícita y sin habilitarse accidentalmente en producción.

## Riesgo del secreto compartido

Un secreto único no permite:

- Saber qué consumidor ejecutó una acción.
- Revocar un consumidor sin afectar a otros.
- Aplicar permisos diferentes por servicio.
- Rotar sin coordinación global.
- Construir auditoría no repudiable.

También debe corregirse el drift entre `X-Internal-Key`, `X-Internal-Secret` y cualquier variante histórica.
