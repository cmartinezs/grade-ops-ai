# ⚛️ TASK 03 — Configurar propiedad `app.auth.reset-code-retention-days`

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Declarar la propiedad `app.auth.reset-code-retention-days` en `application.yml` (default 90) y en `application-local.yml` (valor reducido para facilitar pruebas locales).

---

## Technical Design

- **Approach:** Propiedad de configuración estándar de Spring Boot leída vía `@Value`. Se define en ambos YAMLs para que el perfil `local` pueda usar un TTL corto (e.g., 1 día) sin tocar el default de producción.
- **Affected files:**
  - `src/main/resources/application.yml`
  - `src/main/resources/application-local.yml`
- **Interfaces / contracts:** La propiedad será consumida por `CleanupPasswordResetCodesUseCase` (task-06) vía `@Value("${app.auth.reset-code-retention-days}")`.
- **Design notes:** Mantener la propiedad bajo el namespace `app.auth.*` ya existente, consistente con las propiedades de configuración del bounded context de auth.

---

## Implementation Steps

1. En `application.yml`, bajo la sección `app.auth`, añadir:
   ```yaml
   app:
     auth:
       reset-code-retention-days: 90
   ```
2. En `application-local.yml`, sobreescribir con un valor corto para pruebas manuales:
   ```yaml
   app:
     auth:
       reset-code-retention-days: 1
   ```

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1 | Propiedad presente en `application.yml` con valor `90` | `grep reset-code-retention-days src/main/resources/application.yml` |
| 2 | Propiedad presente en `application-local.yml` con valor distinto | `grep reset-code-retention-days src/main/resources/application-local.yml` |

---

## Done Criteria

- [ ] `app.auth.reset-code-retention-days: 90` presente en `application.yml`
- [ ] `app.auth.reset-code-retention-days` presente en `application-local.yml` con valor para pruebas locales
- [ ] `./mvnw test` pasa (el contexto de test levanta sin error de propiedad faltante cuando task-06 exista)

---

> [← story file](../story-01-cleanup-job.md)
