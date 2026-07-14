# Planning: 009-groq-infra-provisioning

> [← planning/README.md](../README.md)

Short working summary for this planning. Keep this file current as the planning moves through INITIAL, EXPANSION, DEEPENING, and COMPLETED.

---

## Overview

- **Planning ID:** 009-groq-infra-provisioning
- **Current status:** Completed
- **Intent:** Provision the Groq API key and its Cloud Run wiring as real Terraform infra for the `agents/` service.
- **Owner:** human
- **Started:** 2026-07-12
- **Completed:** 2026-07-14

---

## Key Links

- [Initial context](00-initial.md)
- [Expansion plan](01-expansion.md)
- [Story details](02-deepening/)
- [Traceability](TRACEABILITY.md)
- [Retrospective raw notes](RETROSPECTIVE-RAW.md)

---

## Current State

Summarize where this planning stands and what remains before archive.

- [x] Initial intent is complete.
- [x] Expansion stories are dimensioned.
- [x] Stories are DONE or intentionally SKIPPED.
- [x] Traceability is complete.
- [x] Retrospective is complete.

---

## Retrospective

Generated from `RETROSPECTIVE-RAW.md` and planning context on 2026-07-14.

### Executive Summary

This single-story planning provisioned real Terraform infra for the Groq API key — a Secret Manager entry and its Cloud Run env/secret binding on the `agents/` service — exactly matching the scope carried forward from `agents/002-groq-genai-provider`'s original story-02 design. Execution surfaced a significant, pre-existing finding unrelated to the story's own scope: `demo`'s Cloud Run, Cloud SQL, Artifact Registry, and Secret Manager infra has never actually been applied to Google Cloud. Both tasks completed cleanly with human review at every checkpoint; no `terraform apply` was run.

### Outcomes

- Story-01 shipped exactly the scope defined in `00-initial.md`: a `google_secret_manager_secret.groq_api_key` (`GRADEOPS_GROQ_API_KEY`, manual post-apply population, mirroring `smtp.tf`'s pattern) and the corresponding Cloud Run env/secret binding on the `agents/` service (`GRADEOPS_GROQ_API_KEY` secret-bound, `GRADEOPS_GROQ_MODEL` plain). No new Cloud Run service, Artifact Registry repo, or Vertex AI IAM binding — matches the original "no new IAM binding" constraint exactly, since the `agents` service account already had project-level `secretAccessor`.
- `GRADEOPS_GROQ_BASE_URL` was deliberately left unset in Terraform, relying on the app-level default already in `agents/src/main/resources/application-demo.yml`.
- No `terraform apply` was run — by design, this planning only produced reviewed `plan` diffs; applying is a separate, deliberate human action.

### Deviations And Edge Cases

- While verifying task-01, `terraform plan` showed 27 resources pending instead of just the new secret. Investigation (direct `gcloud` queries against `gen-lang-client-0898391452`, not just local `tfstate`) confirmed `demo`'s Cloud Run, Cloud SQL, Artifact Registry, and Secret Manager infra had **never actually been applied** — those APIs are disabled, Cloud SQL has 0 instances. This predates the planning and isn't caused by it. Logged in full in `RETROSPECTIVE-RAW.md` (2026-07-13 00:00 and 00:15 entries).
- The atomize step's uncommitted output (new task files, story/traceability edits) was initially left dirty on `develop`; per this project's "never commit directly to develop" convention, it was branched and PR'd separately (PR #46) before task execution began, rather than folded into task-01's own commit.
- Task branch naming hit a real git limitation: `story-01-groq-infra-provisioning/task-01-...` collides with the existing `story-01-groq-infra-provisioning` branch ref (git cannot have a ref be both a leaf and a namespace). Resolved by reusing this repo's existing `--` separator convention (already present in `gradeops-agents/story-01-groq-provider-adapter--task-01-groq-adapter`).
- Closing the story required a rebase onto `develop` (three commits had merged upstream during story execution), which made the push non-fast-forward. Resolved with `--force-with-lease`, held for explicit human confirmation before running, per this project's git safety rules.

### Decisions And Tradeoffs

- Chose `smtp.tf`'s manual-population secret pattern (no Terraform variable, no `secret_version` resource) over `cloud_sql.tf`/`firebase_admin_iam.tf`'s sensitive-variable-plus-`secret_version` pattern — keeps the raw Groq API key out of `.tfvars`/CI secret inputs and Terraform state. No PDR — this is a task-local implementation choice consistent with an existing in-repo pattern, not a cross-cutting architectural decision.
- Left the literal task-level Done Criteria wording ("plan shows only this one resource") unsatisfied by the letter but satisfied by intent, after explicit human sign-off: the new resource itself plans cleanly and touches nothing already live; the extra 26 pending resources are a pre-existing, out-of-scope gap, not something this story's Technical Design could or should resolve unilaterally.
- No accepted cross-cutting decision here needed a PDR — all decisions were task- or story-local implementation choices.

### Follow-ups

- **Real first deploy of `demo` is still pending.** A deliberate, human-reviewed `terraform apply` is needed to actually stand up Cloud Run (both services), Cloud SQL, Artifact Registry, and all Secret Manager secrets (including this planning's `groq_api_key`) — not scoped to this planning, but blocks the Groq credentials from doing anything until it happens.
- **Terraform state is local-only.** `infra/README.md` already flags this as a pre-existing gap ("should be stored in a GCS backend... before sharing the environment") — worth prioritizing before more infra plannings stack more unapplied config on the same single-machine `tfstate` file.
- After the real `demo` apply, the Groq API key's actual value still needs to be populated manually: `gcloud secrets versions add GRADEOPS_GROQ_API_KEY --data-file=- <<< "<key>"`.

### Lessons For Future Plannings

- When an infra task's `terraform plan` shows more changes than expected, don't assume state drift — check ground truth in the cloud provider directly (`gcloud ... list`) before concluding anything about the local state file's accuracy. Here the state turned out to be accurate; the config was simply never applied.
- The literal wording of a task's Done Criteria can be technically unsatisfiable for reasons outside the task's own scope. Surfacing the gap explicitly and getting human sign-off on a reinterpretation (while still holding the line on "no existing resource is touched") keeps the task honest without blocking on an unrelated, larger decision.
- This repo's task-branch `--` naming convention (as opposed to the generic `/`-based one the planning skill documents) exists specifically to work around a git ref-namespace collision when the story branch and task branch would otherwise share a prefix — worth calling out explicitly in project conventions so future plannings don't rediscover it by trial and error.

---

> [← planning/README.md](../README.md)
