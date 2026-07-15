# ⚛️ TASK 03 — Verify `beta` on Render is actually live

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-04-e2e-integration-verification.md)

---

## Objective

A documented, evidence-backed finding on whether the `beta` environment described in `docs/04-architecture/beta-environment-design.md` (Render + Neon + Cloudflare R2 + Vercel) is actually provisioned and deploying today — not assumed either way. Mirrors the same verify-before-trusting-docs approach `009-groq-infra-provisioning` used for `demo`/GCP, where documented Terraform had in fact never been applied.

---

## Technical Design

- **Approach:** Install the official Render CLI (`github.com/render-oss/cli`) and authenticate non-interactively via `RENDER_API_KEY` (an API key generated from the Render Dashboard's Account Settings — a human action, cannot be scripted). Use it to list the workspace's services and check whether `grade-ops-ai-api`/`grade-ops-ai-agents` (the names `beta-environment-design.md` specifies) exist, and if so, pull their deploy history to confirm at least one successful deploy and that auto-deploy-on-push is actually configured (not just documented as the intended design). Cross-check against Vercel (`web/`) and Neon (Postgres) similarly if credentials are available; if not, note that as a scoping limitation rather than blocking on it — the story's core concern is the `api/`↔`agents/` path, so those two Render services are the priority.
- **Affected files / components:** No code changes — this task produces a documented finding (in this task's report and, if a gap is found, an `Inconsistencies Found` row on the story). If services genuinely don't exist yet, that finding feeds task-04's scope (which cannot smoke-test a service that isn't there) and should be flagged to the human rather than silently worked around.
- **Interfaces / contracts:** None.
- **Risk:** M — the human must generate and share a `RENDER_API_KEY` (or grant Render Dashboard access) for this task to produce real evidence; without it, this task can only report "unable to verify — no credentials," which is itself a valid, honest finding, not a task failure requiring a workaround.
- **Design notes:** Do not assume `beta` is live just because `beta-environment-design.md`'s `Status: Approved` — that field means the *design* was approved, not that deployment happened, per the same lesson learned in `009-groq-infra-provisioning`.

---

## Implementation Steps

1. Install the Render CLI: `curl -fsSL https://raw.githubusercontent.com/render-oss/cli/main/bin/install.sh | sh` (or the current install method per `render.com/docs/cli` at execution time — verify the command is still current, since install instructions can change).
2. Obtain a `RENDER_API_KEY` from the human (Account Settings → API Keys on the Render Dashboard) and set it as an environment variable for non-interactive auth.
3. List services: confirm whether `grade-ops-ai-api` and `grade-ops-ai-agents` (or whatever they were actually named at creation time, if different from the design doc) exist in the workspace.
4. If they exist: pull deploy history for each, confirm at least one successful deploy, and check whether the GitHub integration for auto-deploy-on-push is configured (via the CLI or a documented dashboard check if the CLI doesn't expose this directly).
5. If they don't exist, or credentials aren't available: document the finding plainly — do not guess or fabricate a "looks fine" conclusion.
6. Record the finding in this task's report and, if a gap exists, add an `Inconsistencies Found` row on the story file.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Render CLI is installed and authenticated | `render services list` (or equivalent) returns a real response, not an auth error |
| 2 | Service existence is confirmed one way or the other | Documented finding: services exist with evidence (IDs, URLs), or documented finding that they don't |
| 3 | Deploy history is checked (if services exist) | At least one deploy's status/timestamp captured as evidence |
| 4 | Auto-deploy-on-push is confirmed (if services exist) | Documented evidence of the GitHub integration being active, or a documented gap if it isn't |

### Software Smoke Test Check

N/A for this task's own deliverable — it is an investigation/verification task producing a documented finding, not a running application change. (Task-04 is where a live smoke request against Render happens, gated on this task's finding.)

### Database / ORM Consistency Check

N/A — no database or ORM artifacts involved.

---

## Done Criteria

- [ ] Render CLI installed and authenticated via `RENDER_API_KEY`.
- [ ] A definitive, evidence-backed finding exists: `beta`'s `api`/`agents` Render services either are live with a confirmed deploy history and auto-deploy configured, or are not — documented either way, not assumed.
- [ ] If a gap is found (services missing, no successful deploy, auto-deploy not configured), it's recorded as an `Inconsistencies Found` row on the story, not silently absorbed.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — no smoke request against the live service yet, that's task-04, and only if this task confirms there's something live to test.

---

> [← story file](../story-04-e2e-integration-verification.md)
