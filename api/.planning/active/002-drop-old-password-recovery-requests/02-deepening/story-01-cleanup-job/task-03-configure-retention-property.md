# ⚛️ TASK 03 — Configurar propiedad `app.auth.reset-code-retention-days`

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Declarar la propiedad `app.auth.reset-code-retention-days` en `application.yml` (default 90) y en `application-local.example.yml` (valor reducido para facilitar pruebas locales). `application-local.yml` está gitignoreado, por lo que el override local se entrega vía plantilla versionada.

---

## Technical Design

- **Approach:** Propiedad de configuración estándar de Spring Boot leída vía `@Value`. Se define en `application.yml` (producción, default 90) y en la plantilla versionada `application-local.example.yml` (TTL corto, 1 día) que los desarrolladores copian a `application-local.yml` durante el setup inicial.
- **Affected files:**
  - `src/main/resources/application.yml`
  - `src/main/resources/application-local.example.yml` (plantilla versionada; `application-local.yml` está en `.gitignore`)
  - `README.md` — actualizar instrucción de setup para referenciar el archivo example
- **Interfaces / contracts:** La propiedad será consumida por `CleanupPasswordResetCodesUseCase` (task-06) vía `@Value("${app.auth.reset-code-retention-days}")`.
- **Design notes:** Mantener la propiedad bajo el namespace `app.auth.*` ya existente. `application-local.yml` está gitignoreado (contiene paths de credenciales); el valor local se propaga a nuevos desarrolladores mediante la plantilla `.example.yml`, siguiendo el mismo patrón ya usado para `.env.example`.

---

## Implementation Steps

1. En `application.yml`, bajo la sección `app.auth`, añadir:
   ```yaml
   app:
     auth:
       reset-code-retention-days: 90
   ```
2. En `application-local.example.yml`, añadir el override con TTL corto para pruebas locales:
   ```yaml
   app:
     auth:
       reset-code-retention-days: 1   # short TTL for local testing; default is 90 in application.yml
   ```
3. En `README.md`, actualizar la instrucción de setup local para indicar que se copie `application-local.example.yml` a `application-local.yml`.

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1 | Propiedad presente en `application.yml` con valor `90` | `grep reset-code-retention-days src/main/resources/application.yml` |
| 2 | Propiedad presente en `application-local.example.yml` con valor distinto | `grep reset-code-retention-days src/main/resources/application-local.example.yml` |

---

## Done Criteria

- [x] `app.auth.reset-code-retention-days: 90` presente en `application.yml`
- [x] `app.auth.reset-code-retention-days: 1` presente en `application-local.example.yml` (plantilla versionada para el perfil local)
- [x] `README.md` actualizado para referenciar `application-local.example.yml` en las instrucciones de setup
- [x] `./mvnw test` pasa (el contexto de test levanta sin error de propiedad faltante cuando task-06 exista)

---

> [← story file](../story-01-cleanup-job.md)
