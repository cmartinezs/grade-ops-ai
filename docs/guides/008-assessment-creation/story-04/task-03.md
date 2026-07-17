# Verify Render Beta Live

**Source:** task-03 | **Area:** unknown | **Date:** 2026-07-16

## What it does
A documented, evidence-backed finding on whether the `beta` environment described in `docs/04-architecture/beta-environment-design.md` (Render + Neon + Cloudflare R2 + Vercel) is actually provisioned and deploying today — not assumed either way. Mirrors the same verify-before-trusting-docs approach `009-groq-infra-provisioning` used for `demo`/GCP, where documented Terraform had in fact never been applied.

---

## How to use it
- Install the Render CLI: `curl -fsSL https://raw.githubusercontent.com/render-oss/cli/main/bin/install.sh | sh` (or the current install method per `render.com/docs/cli` at execution time — verify the command is still current, since install instructions can change).
- Obtain a `RENDER_API_KEY` from the human (Account Settings → API Keys on the Render Dashboard) and set it as an environment variable for non-interactive auth.
- List services: confirm whether `grade-ops-ai-api` and `grade-ops-ai-agents` (or whatever they were actually named at creation time, if different from the design doc) exist in the workspace.
- If they exist: pull deploy history for each, confirm at least one successful deploy, and check whether the GitHub integration for auto-deploy-on-push is configured (via the CLI or a documented dashboard check if the CLI doesn't expose this directly).
- If they don't exist, or credentials aren't available: document the finding plainly — do not guess or fabricate a "looks fine" conclusion.
- Record the finding in this task's report and, if a gap exists, add an `Inconsistencies Found` row on the story file.

## Example
Use `Install` through the public interface introduced by this task.
