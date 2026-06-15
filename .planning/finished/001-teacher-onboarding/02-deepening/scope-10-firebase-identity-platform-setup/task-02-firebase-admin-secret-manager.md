# ⚛️ TASK 02 — Firebase Admin SDK Credentials in Secret Manager

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← scope file](../scope-10-firebase-identity-platform-setup.md)

---

## Objective

Provision the Firebase Admin SDK service-account credentials in Secret Manager and grant the `api/` Cloud Run service access to them via IAM, with no key files committed to the repository.

---

## Technical Design

- **Approach:** Create a dedicated service account for the Firebase Admin SDK (`firebase-admin-sa@<project>.iam.gserviceaccount.com`), grant it the minimum required role (`roles/firebaseauth.admin`), generate a key and store it in Secret Manager, then grant the `api/` Cloud Run service account `roles/secretmanager.secretAccessor` for that secret. Alternatively, use workload identity binding to avoid exporting a key file — prefer this if the Cloud Run service already uses a service account with Workload Identity.
- **Affected files / components:**
  - `infra/terraform/environments/demo/firebase_admin_iam.tf` (new file)
  - `infra/terraform/environments/demo/secrets.tf` (add `FIREBASE_ADMIN_CREDENTIALS` secret)
  - `infra/terraform/environments/demo/cloud_run.tf` (add secret env var mount or volume to `api/` service, if it exists; otherwise note for later)
- **Interfaces / contracts:** Secret name `FIREBASE_ADMIN_CREDENTIALS` consumed by `api/` via Secret Manager or env var at runtime.
- **Design notes:** Prefer workload identity (no key file) when possible. For MVP, a service-account key stored in Secret Manager is acceptable. Never commit the key JSON to the repo. The `api/` Spring Boot app reads credentials from the env var `GOOGLE_APPLICATION_CREDENTIALS` or via `FirebaseOptions.Builder.setCredentials(GoogleCredentials.getApplicationDefault())`.

---

## Implementation Steps

1. Create `infra/terraform/environments/demo/firebase_admin_iam.tf` with a `google_service_account` resource for `firebase-admin-sa`.
2. Bind `roles/firebaseauth.admin` to `firebase-admin-sa` via `google_project_iam_member`.
3. Add `google_secret_manager_secret "firebase_admin_credentials"` in `secrets.tf`.
4. Add `google_project_iam_member` granting the `api/` Cloud Run service account `roles/secretmanager.secretAccessor` for the new secret.
5. Add a note (comment in `cloud_run.tf` or README) that the key value must be populated manually or via CI/CD after first apply (Terraform cannot create the key and upload it in one plan without a `google_service_account_key` resource — include it if accepted by the team security policy).
6. Run `terraform plan` to verify no errors.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | `terraform plan` for demo | Shows IAM binding and Secret Manager secret creation with no errors | Local Terraform run |
| 2 | Secret Manager secret exists after apply | `gcloud secrets describe FIREBASE_ADMIN_CREDENTIALS` returns the secret | Post-apply check |
| 3 | `api/` Cloud Run SA has secretAccessor | `gcloud secrets get-iam-policy FIREBASE_ADMIN_CREDENTIALS` lists the api SA | Post-apply check |

---

## Done Criteria

- [x] `firebase_admin_iam.tf` exists with service account and IAM binding.
- [x] `FIREBASE_ADMIN_CREDENTIALS` secret defined in Terraform.
- [x] `api/` Cloud Run service account has `secretAccessor` on the secret.
- [x] No key file is committed to the repository.
- [x] `terraform plan` completes with no errors.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-10-firebase-identity-platform-setup.md)
