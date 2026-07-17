# ⚛️ TASK 03 — Verify `beta` on Render is actually live

> **Status:** DONE
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

## Evidence

Full raw, unedited command output (JSON responses, deploy history, git branch-drift log) is captured separately in [`evidence/task-03-verify-render-beta-live.md`](evidence/task-03-verify-render-beta-live.md). The summary below is a condensed reading of that raw evidence.

Render CLI v2.21.0 installed via the documented install script and authenticated non-interactively with a scoped `RENDER_API_KEY` (added to root `.env`, gitignored). No `render workspace list` subcommand exists in this CLI version, so the workspace id was resolved via a direct `GET https://api.render.com/v1/owners` call, then set with `render workspace set <id> --confirm`:

```
$ render workspace set tea-d8oqlmm7r5hc73caqlrg --confirm -o json
{
  "email": "carlos.f.martinez.s+render@gmail.com",
  "id": "tea-d8oqlmm7r5hc73caqlrg",
  "name": "GradeOps AI",
  "type": "team"
}
```

**Service existence — both services exist**, in workspace "GradeOps AI", project "GradeOps Backend", environment "Production" (`evm-d8oqosbtqb8s73fc6oig`):

| Service | ID | Branch tracked | Auto-deploy | Latest deploy | URL |
|---|---|---|---|---|---|
| `gradeops-api` | `srv-d8oqvejeo5us73b41a80` | `develop` | `yes` (trigger: `commit`) | `live`, 2026-07-15T00:45:08Z, commit `cd771a0c` (merge PR #60) | `https://gradeops-api.onrender.com` |
| `gradeops-agents` | `srv-d8oqosernols73erqc3g` | `master` | `yes` (trigger: `commit`) | `live`, 2026-06-17T01:13:42Z, commit `f7f76c02` | `https://gradeops-agents.onrender.com` |

**Design-vs-reality correction:** actual service names are `gradeops-api`/`gradeops-agents`, not `grade-ops-ai-api`/`grade-ops-ai-agents` as `beta-environment-design.md` specifies.

**Deploy history / auto-deploy — confirmed for both** (`render deploys list <service-id> -o json`), each with multiple prior deploys and `autoDeploy: yes` visible directly on the service object — no need to check the dashboard separately.

**Gap found — `gradeops-agents` is tracking the wrong branch and is stale by a month:**

`gradeops-api` tracks `develop` (the repo's real integration branch) and is current — verified 0 commits touching `api/` have landed on `develop` since its last deploy. `gradeops-agents` tracks `master`, whose last commit touching `agents/` is `f7f76c0` (2026-06-16). `develop` has 38 additional commits touching `agents/` since then (last: `a75c33c`, 2026-07-12), including the entire `002-groq-genai-provider` story:

```
$ git log origin/master..origin/develop --oneline -- agents/ | wc -l
38
$ git log origin/master -1 --format='%H %ci %s' -- agents/
f7f76c02f008eba02dcb1432db7c189f743753f3 2026-06-16 21:12:38 -0400 fix(docker): revert to JVM build with startup optimizations
$ git log origin/develop -1 --format='%H %ci %s' -- agents/
a75c33c433c66bda14aaa353743586b0abdbfb5a 2026-07-12 15:05:07 -0400 chore(002-groq-genai-provider): remove leftover unfilled PDR template stub
```

Auto-deploy-on-push is genuinely configured and working — it just deploys the wrong branch. The `beta` `agents` service currently running on Render does not include the Groq provider adapter work at all. Task-04 (smoke test against Render) would be testing against known-stale `agents/` code if run before this is fixed.

**Vercel / Neon:** not checked — no credentials available for this task run. Documented as a scoping limitation per the task's Technical Design, not a blocker; the story's core concern is the `api/`↔`agents/` path, which is fully covered above.

### Correction — Render normalized (2026-07-16)

After the finding above, the human (Carlos) renamed both Render services to match `beta-environment-design.md` and repointed `gradeops-agents` from `master` to `develop`. Re-verified for real via the Render CLI/API rather than trusting the claim:

```
$ render services -o json | jq -r '.[].service | "\(.name) branch=\(.branch) autoDeploy=\(.autoDeploy)"'
grade-ops-ai-agents branch=develop autoDeploy=yes
grade-ops-ai-api    branch=develop autoDeploy=yes

$ render deploys list srv-d8oqosernols73erqc3g -o json | jq -r '.[0] | "\(.status) \(.startedAt) \(.commit.id[:12]) \(.commit.message | split("\n")[0])"'
live 2026-07-16T18:09:54.075725Z 6a9c8fd4e743 Merge pull request #63 from cmartinezs/planning/008-add-story-05-test-suite

$ git log origin/develop -1 --format='%H %ci %s'
6a9c8fd4e74311b371ca2f42ffbc887291c17031 2026-07-16 00:40:19 -0400 Merge pull request #63 ...
```

`grade-ops-ai-agents`'s new `live` deploy (`6a9c8fd4e743`) matches `develop`'s HEAD exactly — the branch-tracking gap is closed, the stale month of `agents/` work is now deployed. `grade-ops-ai-api` remains at its prior deploy (`cd771a0c`), which is correct: 0 commits touching `api/` have landed on `develop` since then, so no redeploy was needed. Both services are now named per the design doc and both track `develop`.

### Correction — code review (2026-07-16)

Human review (`.code-review/story-04-e2e-integration-verification/task-03-verify-render-beta-live.md`) found P2: the committed evidence file only captured the post-fix Render state ("Captured 2026-07-16, after Carlos renamed and repointed both Render services"), so the pre-fix inconsistency recorded above and on the story wasn't independently auditable from the repo.

Verified before implementing: the pre-fix `render services -o json` and both services' `render deploys list` output *had* genuinely been captured earlier in the same session, via real tool calls against the live Render API before the dashboard fix — they just hadn't been persisted to a file yet. That state can't be re-queried now (it's been changed), so the fix is to archive that already-real output verbatim rather than re-describe it in prose.

Fixed: added `evidence/task-03-verify-render-beta-live.md` §0 (`0.1`–`0.3`) with the exact pre-fix `render services -o json` output (showing `gradeops-agents`/`gradeops-api` names, `gradeops-agents` on `branch: master`) and both services' pre-fix deploy histories, clearly labeled as archived from this session's original tool output rather than freshly re-run, with an explicit note on why (state changed, can't be re-queried).

---

## Done Criteria

- [x] Render CLI installed and authenticated via `RENDER_API_KEY`.
- [x] A definitive, evidence-backed finding exists: `beta`'s `api`/`agents` Render services either are live with a confirmed deploy history and auto-deploy configured, or are not — documented either way, not assumed.
- [x] If a gap is found (services missing, no successful deploy, auto-deploy not configured), it's recorded as an `Inconsistencies Found` row on the story, not silently absorbed.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — no smoke request against the live service yet, that's task-04, and only if this task confirms there's something live to test.

---

> [← story file](../story-04-e2e-integration-verification.md)
