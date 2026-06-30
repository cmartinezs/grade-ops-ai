# 🔍 DEEPENING: Scope 03 — infra-smtp-secrets

> **Status:** DONE
> **Depends on:** — (puede ejecutarse en paralelo con scope-01)
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Provisionar en Terraform (`infra/terraform/environments/demo/`) los secretos SMTP necesarios en Secret Manager y las referencias a esos secretos como variables de entorno en el Cloud Run service de `api/`, de modo que el backend pueda enviar emails en el entorno `demo`.

---

## Context

- **Entorno objetivo:** `demo` (`infra/terraform/environments/demo/`)
- **Secretos nuevos requeridos:**

| Secret Manager key | Descripción | Ejemplo |
|-------------------|-------------|---------|
| `smtp-host` | Hostname del servidor SMTP | `sandbox.smtp.mailtrap.io` |
| `smtp-port` | Puerto SMTP | `587` |
| `smtp-username` | Usuario SMTP | *(credencial del proveedor)* |
| `smtp-password` | Contraseña SMTP | *(credencial del proveedor)* |
| `mail-from` | Dirección remitente | `noreply@gradeops.app` |

- **Env var nueva en Cloud Run (no secret, valor directo):**

| Env var | Descripción | Valor en demo |
|---------|-------------|---------------|
| `GRADEOPS_WEB_BASE_URL` | URL base del frontend para los links del email | `https://<web-cloud-run-url>` |

- **Archivos Terraform relevantes a revisar antes de crear:**
  - `infra/terraform/environments/demo/secrets.tf` o equivalente — donde viven los `google_secret_manager_secret`
  - `infra/terraform/environments/demo/api.tf` o equivalente — donde se define el Cloud Run service de `api/` con sus env vars y secretRefs

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Secretos SMTP en Secret Manager](scope-03-infra-smtp-secrets/task-01-smtp-secrets.md) | GENERATE-DOCUMENT | DONE | `smtp.tf` con 5 recursos `google_secret_manager_secret` (sin versiones — se añaden manualmente) |
| 2 | [Referencias de secretos en Cloud Run api/](scope-03-infra-smtp-secrets/task-02-cloudrun-env.md) | GENERATE-DOCUMENT | DONE | `cloud_run.tf` actualizado: 5 `secretKeyRef` + env var directa `GRADEOPS_WEB_BASE_URL` |

---

## Approach Details

### Task 1 — Secret Manager

Agregar al archivo de secretos del entorno `demo` (verificar si es `secrets.tf`, `main.tf`, u otro):

```hcl
resource "google_secret_manager_secret" "smtp_host" {
  secret_id = "smtp-host"
  replication { auto {} }
}
resource "google_secret_manager_secret_version" "smtp_host" {
  secret  = google_secret_manager_secret.smtp_host.id
  secret_data = var.smtp_host
}

# Repetir el mismo patrón para: smtp-port, smtp-username, smtp-password, mail-from
```

Agregar las `variable` correspondientes en `variables.tf` o el archivo de variables del entorno.

> **Nota:** Los valores reales (`var.smtp_host`, etc.) deben setearse en el archivo `terraform.tfvars` (que está en `.gitignore`) o vía `-var` en CI. No hardcodear credenciales.

### Task 2 — Cloud Run env vars para api/

En el Cloud Run service de `api/`, agregar dentro del bloque `env` o `secret_environment_variables`:

```hcl
# Env var directa (no secret)
env {
  name  = "GRADEOPS_WEB_BASE_URL"
  value = "https://<web-service-url>"  # URL real del Cloud Run de web/
}

# Secretos via secretKeyRef
env {
  name = "SMTP_HOST"
  value_source {
    secret_key_ref {
      secret  = google_secret_manager_secret.smtp_host.secret_id
      version = "latest"
    }
  }
}
# Repetir para SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, GRADEOPS_MAIL_FROM
```

> Verificar cómo están declarados los demás secretos del Cloud Run de `api/` (Firebase, DB) y seguir el mismo patrón.

---

## Done Criteria

- [x] 5 recursos `google_secret_manager_secret` creados en `smtp.tf` (`SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `GRADEOPS_MAIL_FROM`)
- [x] Versiones sin crear — se añaden manualmente con `gcloud secrets versions add` (instrucciones en `smtp.tf`)
- [x] El Cloud Run service de `api/` tiene los 5 secretos referenciados como `secret_key_ref` en `cloud_run.tf`
- [x] La env var `GRADEOPS_WEB_BASE_URL` está seteada en el Cloud Run de `api/` (valor placeholder — actualizar a la URL real del web Cloud Run)
- [x] `terraform -chdir=terraform/environments/demo plan` no muestra errores — 26 recursos a crear, 0 cambios, 0 destrucciones
- [ ] `terraform -chdir=terraform/environments/demo apply` — pendiente de ejecutar en el entorno demo
- [ ] Tras el apply, versiones de secretos SMTP añadidas manualmente — pendiente
- [x] TRACEABILITY.md actualizado

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | La spec mencionaba `secret_manager_secret_version` — no se crean versiones en Terraform para secretos con credenciales externas (SMTP) | `password-recovery-custom-email-design.md` | ACCEPTED | Patrón consistente con `INTERNAL_API_SECRET` existente: secreto creado por Terraform, versión añadida manualmente vía gcloud |
| 2 | `GRADEOPS_WEB_BASE_URL` usa el mismo placeholder `https://placeholder.hosted.app` que `CORS_ALLOWED_ORIGINS` — debe actualizarse a la URL real del Cloud Run de `web/` al hacer el apply | `cloud_run.tf` | PENDING | Actualizar el valor al URL real antes del siguiente `terraform apply` |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)
