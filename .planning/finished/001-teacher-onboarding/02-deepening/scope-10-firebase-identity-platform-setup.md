# 🔍 DEEPENING: Scope 10 — Firebase / Identity Platform Setup

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Provision Firebase Authentication (Google Identity Platform) for the demo environment via Terraform, so the registration, login, and provisioning flows (scopes 01, 06, 08, 09) have a working identity provider.

> Source: planning gap — "Firebase setup (IN)" was a dependency of scopes 06/08 with no scope executing it. No source user story.

## Area

IN (`infra/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Terraform Identity Platform resource](scope-10-firebase-identity-platform-setup/task-01-terraform-identity-platform-resource.md) | GENERATE-DOCUMENT | DONE | `infra/terraform/environments/demo/identity_platform.tf` |
| 2 | [Firebase Admin credentials in Secret Manager](scope-10-firebase-identity-platform-setup/task-02-firebase-admin-secret-manager.md) | GENERATE-DOCUMENT | DONE | `firebase_admin_iam.tf`, Secret Manager secret + IAM |
| 3 | [Firebase web config as build env var](scope-10-firebase-identity-platform-setup/task-03-firebase-web-config-output.md) | GENERATE-DOCUMENT | DONE | `web/.env.local.example`, `NEXT_PUBLIC_FIREBASE_*` vars |

---

## Done Criteria

- [x] Identity Platform enabled and configured via Terraform in `infra/terraform/environments/demo` (`google_identity_platform_*` resources).
- [x] Email/password sign-in provider enabled; verification email template and sender domain configured.
- [x] `api/` can use the Firebase Admin SDK with service-account credentials via Secret Manager or workload identity — no key files in code or repo.
- [x] Firebase web app config (public client keys) available for the `web/` build configuration.
- [x] `terraform plan` / `apply` run clean for the demo environment.
- [ ] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- The `demo` environment is the primary hackathon target.
- Prefer workload identity / attached service accounts on Cloud Run over exported key files.
- The Firebase web config is public by design; the Gemini API key remains server-side only (workspace rule).

## Dependencies

| Depends on | Reason |
|------------|--------|
| — | First executable scope; no prerequisites |

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)
