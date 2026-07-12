# 🔍 DEEPENING: Story 02 — Cloud Run / Secret Manager provisioning for Groq credentials

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Provision the Groq API key and any provider/model env vars story-01 defines as real infra: a Secret Manager entry and the corresponding Cloud Run env var/secret binding for the existing `agents/` Cloud Run service, in `infra/terraform/environments/demo/`. Per `CLAUDE.md`'s infra checklist, this is required because story-01 modifies the `agents/` service's runtime configuration — no *new* Cloud Run service, Artifact Registry repo, or Vertex AI IAM binding is needed, since the service already exists and Groq is not a Google Cloud API.

**Depends On:** Story 01 (needs the final env var names/shape before wiring Terraform).

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Terraform apply against `demo` environment is a real, shared-state operation | M | L | Run `terraform plan` and review the diff with the human before any `terraform apply`; this is a destructive/hard-to-reverse-adjacent action per this project's execution-care rules |
| Env var names decided in story-01 change after infra is wired, causing drift | L | M | Do not start this story's tasks until story-01's env-var-naming task is DONE |

---

## Tasks

> Atomize via `/plan-atomize` before execution begins.

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | Secret Manager entry for the Groq API key (`demo` environment) | GENERATE-DOCUMENT | TODO | `infra/terraform/environments/demo/*.tf` — new `google_secret_manager_secret`/`secret_version` resources |
| 2 | Cloud Run env var / secret binding for the `agents/` service | GENERATE-DOCUMENT | TODO | `infra/terraform/environments/demo/cloud_run.tf` (or equivalent) updated with the new secret reference and any plain env vars story-01 requires |

---

## Done Criteria

- [ ] `terraform -chdir=terraform/environments/demo plan` shows only the expected new/changed resources for the Groq secret and Cloud Run env var binding.
- [ ] No existing Cloud Run service, Artifact Registry repo, or IAM binding is modified beyond what's needed to grant the `agents/` service account access to the new secret.
- [ ] Human has reviewed the `terraform plan` output before any `apply`.
- [ ] TRACEABILITY.md updated with new terms from this story.

---

## Inconsistencies Found

*Record any contradictions or gaps detected during this story. Use RECORD-INCONSISTENCY workflow.*

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
