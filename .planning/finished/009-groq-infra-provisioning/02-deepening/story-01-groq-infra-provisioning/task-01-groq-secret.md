# ⚛️ TASK 01 — Secret Manager entry for the Groq API key

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-groq-infra-provisioning.md)

---

## Objective

A `google_secret_manager_secret` resource named `GRADEOPS_GROQ_API_KEY` exists in `infra/terraform/environments/demo/`, ready to hold the Groq API key value (added manually after `terraform apply`, not via Terraform state).

---

## Technical Design

- **Approach:** Mirror `smtp.tf`'s pattern (`google_secret_manager_secret` only, no `google_secret_manager_secret_version`), not `cloud_sql.tf`'s `db_password` / `firebase_admin_iam.tf`'s pattern (sensitive Terraform variable + `secret_version` resource). The Groq API key is a raw, manually-managed credential like the SMTP credentials — routing it through a Terraform variable would put it in `.tfvars`/CI secret inputs and risk it landing in Terraform state or a committed file. The secret's *value* is added post-apply via `gcloud secrets versions add`, confirmed explicitly with the human per this project's convention for Terraform applies against `demo`.
- **Affected files / components:** New file `infra/terraform/environments/demo/groq.tf`.
- **Interfaces / contracts:** Exposes `google_secret_manager_secret.groq_api_key.secret_id` for task-02 to reference in `cloud_run.tf`.
- **Risk:** Low — routine change, additive-only (new resource, no existing resource touched).
- **Design notes:** `secret_id` must be the literal string `GRADEOPS_GROQ_API_KEY` (matches the env var name `agents/`'s `application-demo.yml` already expects — see `agents/src/main/resources/application-demo.yml:14`). Depends on `google_project_service.secretmanager`, already defined in `firebase_admin_iam.tf:15-18` — reference it, do not redeclare it.

---

## Implementation Steps

1. Create `infra/terraform/environments/demo/groq.tf` with a header comment (matching `smtp.tf`'s style) documenting the manual post-apply population step: `gcloud secrets versions add GRADEOPS_GROQ_API_KEY --data-file=- <<< "<your-groq-api-key>"`.
2. Add the `google_secret_manager_secret` resource `groq_api_key`, `secret_id = "GRADEOPS_GROQ_API_KEY"`, `replication { auto {} }`, `depends_on = [google_project_service.secretmanager]`.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Resource is syntactically valid and self-contained | `terraform -chdir=infra/terraform/environments/demo validate` passes with no errors |
| 2 | Plan shows exactly one new resource | `terraform -chdir=infra/terraform/environments/demo plan` shows only `google_secret_manager_secret.groq_api_key` as a new addition — no other resource is planned for change |
| 3 | `secret_id` matches what `agents/` expects | `grep -n "GRADEOPS_GROQ_API_KEY" infra/terraform/environments/demo/groq.tf agents/src/main/resources/application-demo.yml` shows the same literal string in both |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | GCP application-default credentials for the `demo` project are configured locally (`gcloud auth application-default login`), required for any `terraform plan`/`apply` against real state |
| 2 | App compiles and starts | `terraform -chdir=infra/terraform/environments/demo init` and `terraform -chdir=infra/terraform/environments/demo validate` both succeed |
| 3 | Connectivity or schema validation succeeds | `terraform -chdir=infra/terraform/environments/demo plan` runs to completion against the real `demo` state without provider errors |
| 4 | Changed surface responds correctly | Plan output shows `google_secret_manager_secret.groq_api_key` will be created, with no diff on any pre-existing resource |
| 5 | No startup regressions are visible | No unrelated resources appear in the plan diff (confirms `depends_on` reused the existing `secretmanager` API-enablement resource rather than redeclaring it) |

### Database / ORM Consistency Check

N/A — no database or ORM artifacts involved.

---

## Done Criteria

- [x] `infra/terraform/environments/demo/groq.tf` exists with the `google_secret_manager_secret.groq_api_key` resource (`secret_id = "GRADEOPS_GROQ_API_KEY"`).
- [x] All verification checks listed above pass.
- [x] `terraform plan` shows this resource created cleanly, with no diff to any resource already in local state. Caveat (see `RETROSPECTIVE-RAW.md` 2026-07-13 00:15): the same plan also shows 26 other resources from the existing config as pending first-time creates — confirmed via direct GCP query (Cloud Run/Artifact Registry/Secret Manager APIs disabled, 0 Cloud SQL instances) to be a genuine "never applied" gap predating this task, not drift caused by it.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] Human has reviewed the `terraform plan` output before any `apply` (per this planning's Risk Register R-01). No `apply` was run as part of this task.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — no secret *value*, IAM binding, or Cloud Run change is included here; those are task-02's or already covered by existing infra.

---

> [← story file](../story-01-groq-infra-provisioning.md)
