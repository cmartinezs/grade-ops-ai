# Retrospective Raw Notes: [Planning Name]

> [← README](README.md)

Working log for events that were unexpected, corrective, risky, or useful for the final retrospective.

Use `/plan-edge-case <planning-id> -- <what happened>` to add manual entries. Commands may also append entries when they encounter blockers, corrections, skipped work, recovery actions, validation failures, or other non-linear events.

---

## How To Use This File

Capture facts while they are fresh. Do not polish entries here. The final retrospective belongs in `README.md`.

Each entry should answer as many of these as possible:

- What happened?
- What was expected instead?
- How was it resolved or contained?
- What should be carried forward?

---

## Log

<!-- Add newest entries at the top. -->

### 2026-07-13 00:15 - Confirmed: demo's Cloud Run/Cloud SQL/Artifact Registry/Secret Manager infra was never applied (not state drift)

- **Source:** /plan-task 009 story-01 task-01
- **Related story/task:** story-01, task-01
- **What happened:** Follow-up to the 00:00 entry below. Queried Google Cloud directly (not just local `tfstate`) against project `gen-lang-client-0898391452`: `run.googleapis.com`, `artifactregistry.googleapis.com`, and `secretmanager.googleapis.com` are all **disabled** (`gcloud run services list` / `gcloud artifacts repositories list` / `gcloud secrets list` each returned `SERVICE_DISABLED`), and `gcloud sql instances list` returned `Listed 0 items`. This confirms the local `tfstate` is accurate, not stale — the ~26 resources missing from state (Cloud Run services, Cloud SQL instance, Artifact Registry repo, all `google_secret_manager_secret.*`) were never applied to real infra at all. Only the bootstrap layer (IAM, service accounts, Identity Platform, Workload Identity Federation) was ever run. Consistent with `api/`, `agents/`, `infra/` all being marked "Scaffolding" in the root `CLAUDE.md` — this project has not had a first real deploy yet.
- **Expected instead:** The 00:00 entry initially hedged between "stale local state" and "never applied" as two open possibilities; this is now resolved in favor of the latter, confirmed by ground truth, not inference.
- **Resolution:** No reconciliation/import needed — nothing to reconcile, since local state already accurately reflects nothing beyond bootstrap being live. The path forward is a deliberate, human-approved `terraform apply` to actually stand up `demo` for the first time (not in scope for task-01, which only adds one new resource definition to the config). Not performed here.
- **Retrospective signal:** This planning's Risk Register R-01 ("Terraform apply against demo is a real, shared-state operation") should be read literally as "will be the *first* apply of most of this environment," not an incremental change to already-live infra — raises the stakes of the human review step before any `apply`. Also surfaces a pre-existing gap worth a follow-up decision outside this story: `infra/README.md` already flags that Terraform state should move to a GCS backend before the environment is shared/team-used; it is still local-only on this machine, which is a single point of loss for all future planning's infra evidence.

### 2026-07-13 00:00 - Local terraform.tfstate for demo is far behind the committed config

- **Source:** /plan-task 009 story-01 task-01
- **Related story/task:** story-01, task-01
- **What happened:** `terraform plan` against `infra/terraform/environments/demo` (local state, `gen-lang-client-0898391452`) shows 27 resources to add, not just the new `google_secret_manager_secret.groq_api_key`. `terraform state list` shows only ~21 resources currently tracked (IAM, service accounts, Identity Platform, workload identity) — `google_cloud_run_v2_service.api`/`.agents`, `google_sql_database_instance.gradeops_demo`, `google_artifact_registry_repository.grade_ops_ai`, and every `google_secret_manager_secret.*` (SMTP, `internal_api_secret`, `firebase_admin_credentials`, `db_password`, and now `groq_api_key`) are all still absent from state and would be created by the next `apply`.
- **Expected instead:** Task-01's Verification #2 and Done Criteria expected `terraform plan` to show only the one new secret, assuming the rest of `demo` was already applied (consistent with `cloud_run.tf`/`smtp.tf`/`firebase_admin_iam.tf` reading as already-merged, presumably-live infra).
- **Resolution:** Confirmed `google_secret_manager_secret.groq_api_key` itself plans cleanly — created, no diff to any resource already in state, no unrelated resource touched by this task's Terraform code. Initially attributed to possible environment drift (see follow-up entry above for the confirmed root cause).
- **Retrospective signal:** Superseded by the 00:15 entry above — see there for the confirmed finding and recommended path forward.

---

> [← README](README.md)
