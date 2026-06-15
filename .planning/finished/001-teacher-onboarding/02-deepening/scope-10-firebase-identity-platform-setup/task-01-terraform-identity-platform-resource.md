# ⚛️ TASK 01 — Terraform Identity Platform Resource

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-10-firebase-identity-platform-setup.md)

---

## Objective

Create `infra/terraform/environments/demo/identity_platform.tf` that enables Google Identity Platform and configures the email/password sign-in provider and the verification email template for the demo environment.

---

## Technical Design

- **Approach:** Use `google_identity_platform_config` to enable Identity Platform on the demo GCP project, `google_identity_platform_default_supported_idp_config` (or the equivalent provider resource) to enable the email/password provider, and the Identity Platform email template settings for the verification email. Verification sender domain customization is set here or via a separate `google_identity_platform_email_template` resource if available.
- **Affected files / components:**
  - `infra/terraform/environments/demo/identity_platform.tf` (new file)
  - `infra/terraform/environments/demo/variables.tf` (add `firebase_project_id` if not already present)
  - `infra/terraform/environments/demo/outputs.tf` (export `firebase_project_id`, `firebase_api_key`, `firebase_auth_domain`)
- **Interfaces / contracts:** Outputs `firebase_project_id`, `firebase_api_key`, `firebase_auth_domain` consumed by task-03.
- **Design notes:** The `google_identity_platform_config` resource requires the Identity Platform API to be enabled on the project. Add `google_project_service "identitytoolkit" {}` if not present. Keep email template sender within Firebase's default domain for MVP; custom domain is a later hardening step.

---

## Implementation Steps

1. Add `google_project_service "identitytoolkit"` resource in `infra/terraform/environments/demo/identity_platform.tf` to enable the Identity Toolkit API.
2. Add `google_identity_platform_config` resource enabling sign-in and setting `authorized_domains`.
3. Add `google_identity_platform_project_default_config` (if needed by provider version) or equivalent to enable the email/password provider.
4. Add Terraform `output` blocks for `firebase_api_key`, `firebase_auth_domain`, `firebase_project_id` in `outputs.tf`.
5. Run `terraform -chdir=terraform/environments/demo init` and `terraform plan` to verify no errors.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | `terraform plan` on demo environment | Plan shows create/no-op for `google_identity_platform_config` with no errors | Local Terraform run |
| 2 | Email/password provider listed in plan | `sign_in.email.enabled = true` in planned resource | Local Terraform run |
| 3 | Outputs defined | `terraform output firebase_api_key` returns non-empty value after apply | Post-apply check |

---

## Done Criteria

- [x] `infra/terraform/environments/demo/identity_platform.tf` exists and is syntactically valid.
- [x] `terraform plan` completes with no errors for the demo environment.
- [x] Email/password sign-in provider is enabled in the plan.
- [x] `firebase_api_key`, `firebase_auth_domain`, `firebase_project_id` outputs are defined.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-10-firebase-identity-platform-setup.md)
