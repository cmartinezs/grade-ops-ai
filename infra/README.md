```
 ██████╗ ██████╗  █████╗ ██████╗ ███████╗ ██████╗ ██████╗ ███████╗    █████╗ ██╗
██╔════╝ ██╔══██╗██╔══██╗██╔══██╗██╔════╝██╔═══██╗██╔══██╗██╔════╝   ██╔══██╗██║
██║  ███╗██████╔╝███████║██║  ██║█████╗  ██║   ██║██████╔╝███████╗   ███████║██║
██║   ██║██╔══██╗██╔══██║██║  ██║██╔══╝  ██║   ██║██╔═══╝ ╚════██║   ██╔══██║██║
╚██████╔╝██║  ██║██║  ██║██████╔╝███████╗╚██████╔╝██║     ███████║██╗██║  ██║██║
 ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ╚══════╝ ╚═════╝ ╚═╝     ╚══════╝╚═╝╚═╝  ╚═╝╚═╝
```

<div align="center">

**AI-Native Assessment Operations Platform**

`grade-ops-ai-infra` · Google Cloud Infrastructure

![Terraform](https://img.shields.io/badge/Terraform-≥1.5-7B42BC?style=flat-square&logo=terraform&logoColor=white)
![Google Cloud](https://img.shields.io/badge/Google_Cloud-Run-4285F4?style=flat-square&logo=googlecloud&logoColor=white)
![Google Provider](https://img.shields.io/badge/google_provider-~5.x-4285F4?style=flat-square&logo=terraform&logoColor=white)
![Identity Platform](https://img.shields.io/badge/Identity_Platform-Firebase_Auth-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)

</div>

---

## Overview

`grade-ops-ai-infra` contains all Terraform code to provision and manage the GradeOps AI platform infrastructure on Google Cloud. The primary target is the **`demo`** environment, used for the hackathon demo and pilot runs.

Infrastructure is organized by **environment** under `terraform/environments/`. Each environment is a self-contained Terraform root module that sources shared configuration from the top-level modules under `terraform/` (as they are added over time).

---

## Resources Provisioned

```
GCP Project
│
├── Firebase / Identity Platform
│   ├── google_identity_platform_config           — Enables Identity Platform
│   ├── google_identity_platform_default_supported_idp_config
│   │     └── email/password sign-in provider
│   └── google_firebase_web_app                   — Registers the Next.js web app
│         └── (data) google_firebase_web_app_config
│               └── Exposes api_key, auth_domain, messaging_sender_id
│
├── Firebase Admin SDK — Service Account
│   ├── google_service_account                    — firebase-admin@<project>.iam.gserviceaccount.com
│   ├── google_project_iam_member                 — roles/firebase.sdkAdminServiceAgent
│   ├── google_service_account_key                — JSON key for the Admin SDK
│   └── google_secret_manager_secret              — Stores the key in Secret Manager
│         └── google_secret_manager_secret_version
│
└── Secret Manager Access
    └── google_secret_manager_secret_iam_member   — Grants the api/ Cloud Run SA read access
```

> **Cloud Run, Cloud SQL, Cloud Storage, Artifact Registry, and GitHub Actions OIDC** are planned for subsequent Terraform modules as the epics that require them are built out.

---

## Directory Structure

```
infra/
└── terraform/
    ├── environments/
    │   └── demo/                          # Primary environment — hackathon / pilot
    │       ├── providers.tf               # google + google-beta providers (≥5.0, <6.0)
    │       ├── variables.tf               # project_id, region, environment, api_cloud_run_sa_email
    │       ├── identity_platform.tf       # Firebase Identity Platform + email/password IdP
    │       ├── firebase_web_app.tf        # Firebase web app registration + config data source
    │       ├── firebase_admin_iam.tf      # Firebase Admin SA + Secret Manager secret + IAM
    │       └── outputs.tf                 # Firebase config values + secret reference
    └── (modules/)                         # Shared modules — to be added as epics are built
```

---

## Environments

| Environment | Directory | Purpose | Status |
|-------------|-----------|---------|--------|
| `demo` | `terraform/environments/demo/` | Hackathon demo + pilot runs | Active |
| `staging` | _(planned)_ | Pre-production validation | Not provisioned |
| `prod` | _(planned)_ | General availability | Not provisioned |

---

## Prerequisites

| Tool | Minimum version | Notes |
|------|----------------|-------|
| Terraform | 1.5 | `brew install terraform` or [official installer](https://developer.hashicorp.com/terraform/install) |
| Google Cloud SDK (`gcloud`) | Latest | Required for ADC authentication |
| GCP project | — | Must have billing enabled |
| Firebase project | — | Same GCP project; Firebase must be enabled via the Firebase Console first |

### Authentication

Terraform uses **Application Default Credentials (ADC)**. Authenticate before running any commands:

```bash
gcloud auth application-default login
gcloud config set project <your-project-id>
```

---

## Usage

All commands use `-chdir` to target a specific environment without `cd`.

### Initialize

```bash
terraform -chdir=terraform/environments/demo init
```

### Plan

```bash
terraform -chdir=terraform/environments/demo plan \
  -var="project_id=<your-project-id>" \
  -var="api_cloud_run_sa_email=<api-service-account>@<project>.iam.gserviceaccount.com"
```

### Apply

```bash
terraform -chdir=terraform/environments/demo apply \
  -var="project_id=<your-project-id>" \
  -var="api_cloud_run_sa_email=<api-service-account>@<project>.iam.gserviceaccount.com"
```

### Using a tfvars file (recommended)

Create `terraform/environments/demo/terraform.tfvars` (gitignored):

```hcl
project_id             = "your-gcp-project-id"
region                 = "us-central1"
environment            = "demo"
api_cloud_run_sa_email = "grade-ops-api@your-project.iam.gserviceaccount.com"
```

Then run without `-var` flags:

```bash
terraform -chdir=terraform/environments/demo plan
terraform -chdir=terraform/environments/demo apply
```

### Destroy

```bash
terraform -chdir=terraform/environments/demo destroy
```

> **Warning:** Destroying the demo environment will delete the Firebase Identity Platform config, the web app registration, and the Firebase Admin secret. Existing Firebase user accounts are **not** deleted (managed by Firebase, not Terraform).

---

## Variables

| Name | Type | Default | Description |
|------|------|---------|-------------|
| `project_id` | `string` | _(required)_ | GCP project ID — also the Firebase project ID |
| `region` | `string` | `us-central1` | GCP region for all regional resources |
| `environment` | `string` | `demo` | Environment name tag applied to resources |
| `api_cloud_run_sa_email` | `string` | _(required)_ | Email of the `api/` Cloud Run service account; receives `secretmanager.secretAccessor` on the Firebase Admin secret |

---

## Outputs

After `terraform apply`, the following outputs are available. Use them to populate environment variables in `grade-ops-ai-web` and `grade-ops-ai-api`.

| Output | Description | Consumed by |
|--------|-------------|-------------|
| `NEXT_PUBLIC_FIREBASE_API_KEY` | Firebase web API key | `web/.env.local` |
| `NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN` | Firebase auth domain | `web/.env.local` |
| `NEXT_PUBLIC_FIREBASE_PROJECT_ID` | GCP / Firebase project ID | `web/.env.local` |
| `NEXT_PUBLIC_FIREBASE_APP_ID` | Firebase web app ID | `web/.env.local` |
| `NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID` | Cloud Messaging sender ID | `web/.env.local` |
| `firebase_admin_credentials_secret_id` | Secret Manager secret ID for the Admin SDK key | `api/` Cloud Run env |
| `firebase_admin_sa_email` | Firebase Admin service account email | IAM / audit reference |

Retrieve all outputs at once:

```bash
terraform -chdir=terraform/environments/demo output -json
```

Populate `web/.env.local` automatically:

```bash
terraform -chdir=terraform/environments/demo output -json \
  | jq -r 'to_entries | map(select(.key | startswith("NEXT_PUBLIC_"))) | .[] | "\(.key)=\(.value.value)"' \
  > ../web/.env.local
```

---

## Security Notes

- **`terraform.tfvars` is gitignored.** Never commit project IDs or service account emails to version control.
- **The Firebase Admin SDK JSON key** is stored in Secret Manager, not as a file on disk or a plain environment variable. The `api/` Cloud Run service accesses it at runtime via Secret Manager volume mount or env var reference.
- **The Firebase API key** (output as `NEXT_PUBLIC_FIREBASE_API_KEY`) is intentionally public — it identifies the project to Firebase but carries no privileged access. Authorization is enforced server-side.
- **Terraform state** should be stored in a GCS backend (not local) for team use. Configure a `backend "gcs"` block in `providers.tf` before sharing the environment.

---

## Related Repositories

| Repo | Description |
|------|-------------|
| [`grade-ops-ai-web`](../web/) | Next.js teacher workspace — consumes Firebase config outputs |
| [`grade-ops-ai-api`](../api/) | Spring Boot domain API — consumes Firebase Admin secret |
| [`grade-ops-ai-agents`](../agents/) | Spring AI agent runtime — future Vertex AI and Cloud Run provisioning |
| [`grade-ops-ai-docs`](../docs/) | Canonical product and architecture documentation |
