# 🔍 DEEPENING: Scope 01 — infra-google-provider

> **Status:** DONE (plan/apply pendientes — requieren credenciales GCP)
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Add the Google identity provider to the Terraform Firebase configuration for the `demo` environment, so that infrastructure state matches what is already enabled in the Firebase console.

---

## Tasks

| # | Task | Area | Status | Output |
|---|------|------|--------|--------|
| 1 | Add `google_identity_platform_default_supported_idp_config` resource for Google provider | `infra/` | DONE | `identity_platform.tf` + 2 new vars in `variables.tf` |
| 2 | Run `terraform plan` to verify no unintended drift | `infra/` | MANUAL | Requires real GCP credentials — run locally |
| 3 | Apply to `demo` environment | `infra/` | MANUAL | Requires real GCP credentials — run locally |

---

## Done Criteria

- [x] `google_identity_platform_default_supported_idp_config` for `google.com` exists in `infra/terraform/environments/demo/`.
- [ ] `terraform plan` shows no unexpected changes beyond the new resource. *(manual — requires GCP credentials)*
- [ ] Applied to `demo` environment without errors. *(manual — requires GCP credentials)*
- [x] TRACEABILITY.md updated with new terms from this scope.

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
