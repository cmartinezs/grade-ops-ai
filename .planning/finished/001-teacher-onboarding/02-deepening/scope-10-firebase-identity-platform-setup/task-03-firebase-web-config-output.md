# ⚛️ TASK 03 — Firebase Web Config as Build Environment Variable

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← scope file](../scope-10-firebase-identity-platform-setup.md)

---

## Objective

Export the Firebase web app public configuration (apiKey, authDomain, projectId, appId) as environment variables available to the `web/` Next.js build, so the Firebase client SDK can be initialized.

---

## Technical Design

- **Approach:** Use the Terraform outputs from task-01 (`firebase_api_key`, `firebase_auth_domain`, `firebase_project_id`) and create a `google_firebase_web_app` resource (if using the Firebase Terraform provider) to obtain the `app_id`. Expose these as environment variables in the `web/` Cloud Run service or as a `.env.local` convention for local dev. For the demo environment, inject them as Cloud Run environment variables via Terraform.
- **Affected files / components:**
  - `infra/terraform/environments/demo/firebase_web_app.tf` (new file — `google_firebase_web_app` resource)
  - `infra/terraform/environments/demo/cloud_run.tf` (add `NEXT_PUBLIC_FIREBASE_*` env vars to `web/` Cloud Run service, if it exists; otherwise document for later)
  - `web/.env.local.example` (document the required variables for local dev)
- **Interfaces / contracts:** Environment variables `NEXT_PUBLIC_FIREBASE_API_KEY`, `NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN`, `NEXT_PUBLIC_FIREBASE_PROJECT_ID`, `NEXT_PUBLIC_FIREBASE_APP_ID` consumed by `web/src/lib/firebase/client.ts`.
- **Design notes:** `NEXT_PUBLIC_` prefix is required for Next.js to expose env vars to the browser bundle. These values are public by design (Firebase web config is not a secret). Do not confuse with the Admin SDK credentials (which are secret and server-side only).

---

## Implementation Steps

1. Create `infra/terraform/environments/demo/firebase_web_app.tf` with a `google_firebase_web_app` resource for the demo web app.
2. Use `data.google_firebase_web_app_config` datasource to read back the web app config values.
3. Add `NEXT_PUBLIC_FIREBASE_API_KEY`, `NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN`, `NEXT_PUBLIC_FIREBASE_PROJECT_ID`, `NEXT_PUBLIC_FIREBASE_APP_ID` to the `web/` Cloud Run service environment in `cloud_run.tf` (or document as TODO if Cloud Run is not yet provisioned).
4. Create `web/.env.local.example` documenting all four variables with placeholder values and instructions.
5. Run `terraform plan` to verify no errors.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | `terraform plan` for demo | Shows `google_firebase_web_app` creation with no errors | Local Terraform run |
| 2 | `.env.local.example` exists in `web/` | File contains all four `NEXT_PUBLIC_FIREBASE_*` variables | `ls web/.env.local.example` |
| 3 | Firebase client SDK initializes with env vars | `initializeApp(firebaseConfig)` does not throw when vars are set | `web/` local dev test |

---

## Done Criteria

- [x] `firebase_web_app.tf` exists and plans cleanly.
- [x] `web/.env.local.example` documents all four `NEXT_PUBLIC_FIREBASE_*` variables.
- [x] `NEXT_PUBLIC_FIREBASE_*` vars are wired into the `web/` Cloud Run service in Terraform (or documented as TODO with a clear path).
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-10-firebase-identity-platform-setup.md)
