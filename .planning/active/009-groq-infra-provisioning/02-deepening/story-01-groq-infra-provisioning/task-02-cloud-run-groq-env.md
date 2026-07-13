# ⚛️ TASK 02 — Cloud Run env var / secret binding for the agents/ service

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← story file](../story-01-groq-infra-provisioning.md)

---

## Objective

The `agents/` Cloud Run service (`google_cloud_run_v2_service.agents` in `cloud_run.tf`) has `GRADEOPS_GROQ_API_KEY` (bound to task-01's Secret Manager secret) and `GRADEOPS_GROQ_MODEL` (plain value) wired as container env vars, so the Groq provider adapter can run against real credentials in `demo`.

---

## Technical Design

- **Approach:** Add two `env` blocks to the existing `containers` block inside `google_cloud_run_v2_service.agents` (`cloud_run.tf:172-203`), following the same shape already used for `api`'s secret-backed env vars (e.g. `SMTP_HOST` at `cloud_run.tf:85-93`) and plain env vars (e.g. `SPRING_PROFILES_ACTIVE`). `GRADEOPS_GROQ_API_KEY` uses `value_source.secret_key_ref` pointing at `google_secret_manager_secret.groq_api_key.secret_id` (task-01), version `"latest"`. `GRADEOPS_GROQ_MODEL` is a plain `value` — hardcoded literal `"llama-3.3-70b-versatile"`, matching `agents/.env.example`'s documented default, following the existing convention of hardcoded literals for non-secret plain env vars in this file (e.g. `GRADEOPS_WEB_BASE_URL`).
- **`GRADEOPS_GROQ_BASE_URL` is deliberately not set here:** `agents/src/main/resources/application-demo.yml:15` already defaults it to `https://api.groq.com/openai/v1` (`${GRADEOPS_GROQ_BASE_URL:https://api.groq.com/openai/v1}`) — the same value we would hardcode in Terraform. Adding it as a Cloud Run env var would be a no-op that only adds infra surface to maintain; per `[CHECK-ATOMICITY]`'s "no unintended expansion," it is left to the app-level default. Confirmed with the human during atomization (see this planning's `01-expansion.md` Notes / atomize confirmation).
- **Affected files / components:** `infra/terraform/environments/demo/cloud_run.tf` (agents service `containers` block only).
- **Interfaces / contracts:** None — purely infra wiring, no new interface.
- **Risk:** Low — additive-only within one existing resource block. The one thing to verify explicitly: the `agents` service account must already be able to read the new secret. `service_accounts.tf:51-55` grants `google_service_account.agents` project-level `roles/secretmanager.secretAccessor` — this already covers *any* Secret Manager secret in the project, including the new one, so no new IAM resource is needed. Verify this is still true (no narrower binding has since replaced it) before marking this task done.
- **Design notes:** Do not touch the `lifecycle { ignore_changes = [template] }` block or any `api` service env vars — this task's blast radius is the `agents` service's `containers.env` list only.

---

## Implementation Steps

1. In `infra/terraform/environments/demo/cloud_run.tf`, inside `google_cloud_run_v2_service.agents`'s `containers` block, add an `env` block for `GRADEOPS_GROQ_API_KEY` using `value_source.secret_key_ref` → `google_secret_manager_secret.groq_api_key.secret_id`, `version = "latest"`.
2. Add a second `env` block for `GRADEOPS_GROQ_MODEL` with plain `value = "llama-3.3-70b-versatile"`.
3. Confirm (`grep -n "secretAccessor" infra/terraform/environments/demo/service_accounts.tf`) that `agents_secret_accessor` still grants `google_service_account.agents` project-level `roles/secretmanager.secretAccessor`, so no new `google_secret_manager_secret_iam_member` resource is required.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Resource change is syntactically valid | `terraform -chdir=infra/terraform/environments/demo validate` passes with no errors |
| 2 | Plan shows only the expected diff | `terraform -chdir=infra/terraform/environments/demo plan` shows `google_cloud_run_v2_service.agents` as the only changed resource (in-place update to `template.containers.env`), plus task-01's new secret if not yet applied — no other resource (in particular, no IAM resource) appears |
| 3 | Both env vars are present with correct sourcing | Plan diff (or `terraform show`) confirms `GRADEOPS_GROQ_API_KEY` sources from `value_source.secret_key_ref` (not a plain `value`) and `GRADEOPS_GROQ_MODEL` is a plain `value = "llama-3.3-70b-versatile"` |
| 4 | Agents service account already has secret access | `grep -n "roles/secretmanager.secretAccessor" infra/terraform/environments/demo/service_accounts.tf` shows the existing `agents_secret_accessor` binding for `google_service_account.agents`, confirming no new IAM resource is needed |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | GCP application-default credentials for the `demo` project configured locally; task-01's secret already applied (or included in the same plan) |
| 2 | App compiles and starts | `terraform -chdir=infra/terraform/environments/demo init` and `terraform -chdir=infra/terraform/environments/demo validate` both succeed |
| 3 | Connectivity or schema validation succeeds | `terraform -chdir=infra/terraform/environments/demo plan` runs to completion against real `demo` state without provider errors, referencing task-01's secret by resource reference (not a hardcoded secret name) |
| 4 | Changed surface responds correctly | Plan output shows `google_cloud_run_v2_service.agents` updated in place with the two new `env` entries; `google_cloud_run_v2_service.api` and all other resources show no diff |
| 5 | No startup regressions are visible | No new/changed IAM resources appear in the plan diff, confirming the existing project-level `secretAccessor` binding was correctly reused |

### Database / ORM Consistency Check

N/A — no database or ORM artifacts involved.

---

## Done Criteria

- [ ] `google_cloud_run_v2_service.agents` in `cloud_run.tf` has `GRADEOPS_GROQ_API_KEY` (secret-bound to task-01's secret) and `GRADEOPS_GROQ_MODEL` (plain value) in its `containers.env` list.
- [ ] `GRADEOPS_GROQ_BASE_URL` is intentionally absent (relies on the app-level default) — not a gap.
- [ ] All verification checks listed above pass.
- [ ] `terraform plan` shows only `google_cloud_run_v2_service.agents` changed (plus task-01's secret if applied together) — no existing Cloud Run service, Artifact Registry repo, or IAM binding modified beyond this.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] Human has reviewed the `terraform plan` output before any `apply` (per this planning's Risk Register R-01).
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-groq-infra-provisioning.md)
