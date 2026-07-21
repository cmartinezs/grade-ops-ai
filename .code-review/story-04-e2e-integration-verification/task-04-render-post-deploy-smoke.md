# Code Review: story-04 / task-04-render-post-deploy-smoke

## Scope

- Branch: `story-04-e2e-integration-verification--task-04-render-post-deploy-smoke`
- Reviewed commit: `8ec8b8f` (`docs(e2e-integration-verification): task-04 passes for real after Groq credential fix`)
- Re-reviewed commit: `7473fc9` (`fix(e2e-integration-verification): document Render beta smoke vars in .env.example (P2)`)
- Base branch used for diff: `origin/story-04-e2e-integration-verification`
- Task file: `.planning/active/008-assessment-creation/02-deepening/story-04-e2e-integration-verification/task-04-render-post-deploy-smoke.md`
- Primary changed surface: `scripts/smoke-e2e-render-beta.sh`, `scripts/lib/e2e-smoke-flow.sh`, `scripts/smoke-e2e-local.sh`, task-04 evidence

## Findings

### Resolved - P2 - The Render smoke script is now re-runnable from the committed env template

The follow-up commit resolves the prior blocker. `.env.example` now includes a dedicated "Render beta smoke" section with the variables that `scripts/smoke-e2e-render-beta.sh` requires: `RENDER_API_KEY`, `RENDER_WORKSPACE_ID`, and `BETA_API_BASE_URL`, plus documented optional overrides for `RENDER_API_SERVICE_NAME` and `RENDER_AGENTS_SERVICE_NAME` (`.env.example:38-54`). The placeholders are non-secret (`your-render-api-key`, `tea-xxxxxxxx...`, `https://your-api-service.onrender.com`) and the comments point operators to the task-03 evidence for resolving the workspace id.

The task file was also updated to record the correction and remove the stale wording that said generation was still failing after the Groq credential fix (`task-04-render-post-deploy-smoke.md:83-88`). The script's own precondition block and hard-fail guards now have a matching committed template, so the "committed, re-runnable script" handoff is complete from the repository.

### P2 - The Render smoke script is not fully re-runnable from the committed env template

`scripts/smoke-e2e-render-beta.sh` tells the next operator to create a root `.env` "see `.env.example`" containing `RENDER_API_KEY`, `RENDER_WORKSPACE_ID`, `BETA_API_BASE_URL`, `INTERNAL_API_SECRET`, and `NEXT_PUBLIC_FIREBASE_API_KEY` (`scripts/smoke-e2e-render-beta.sh:7-26`). The script then hard-fails if the three new Render/beta variables are missing (`scripts/smoke-e2e-render-beta.sh:75-79`). But the committed `.env.example` still only documents local compose/Firebase variables and has no `RENDER_API_KEY`, `RENDER_WORKSPACE_ID`, or `BETA_API_BASE_URL` entries (`.env.example:1-35`).

That leaves the task's re-runnable handoff incomplete. The task marks the script as committed and verification checks as passing (`task-04-render-post-deploy-smoke.md:85-88`), and the raw evidence proves the author had the right local `.env` values for the successful runs. A future developer starting from the repo, however, cannot follow the script's own `see .env.example` instruction to reconstruct the required configuration. They would have to discover the missing variable names from the script body or the task evidence instead of the canonical template.

Recommended fix: extend `.env.example` with a clearly separated optional "Render beta smoke" section containing placeholder values and comments for `RENDER_API_KEY`, `RENDER_WORKSPACE_ID`, `BETA_API_BASE_URL`, plus optional overrides for `RENDER_API_SERVICE_NAME` and `RENDER_AGENTS_SERVICE_NAME`. Keep secret placeholders non-real, as the file already does.

## Verification

- Re-review: confirmed `.env.example` documents `RENDER_API_KEY`, `RENDER_WORKSPACE_ID`, `BETA_API_BASE_URL`, and optional service-name overrides with placeholder values only.
- Re-review: `bash -n scripts/lib/e2e-smoke-flow.sh scripts/smoke-e2e-local.sh scripts/smoke-e2e-render-beta.sh` passed.
- Re-review: `git diff --check origin/story-04-e2e-integration-verification...HEAD` passed.
- Re-review: confirmed all three smoke scripts/shared library remain tracked executable (`100755`).
- Reviewed diff against `origin/story-04-e2e-integration-verification`: 6 files changed.
- `bash -n scripts/lib/e2e-smoke-flow.sh scripts/smoke-e2e-local.sh scripts/smoke-e2e-render-beta.sh` passed.
- `git diff --check origin/story-04-e2e-integration-verification...HEAD` passed.
- Confirmed all three smoke scripts/shared library are tracked executable (`100755`).
- Did not re-run the live Render smoke because it requires live Render/Firebase/beta secrets and network access; the task evidence includes two successful post-fix live runs with full generated draft payloads.

## Review Result

Approved for task-04. The prior P2 is resolved and no new findings were identified in the re-review.
