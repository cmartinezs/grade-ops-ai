# ⚛️ TASK 04 — Render post-deploy smoke script and evidence

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-03
> [← story file](../story-04-e2e-integration-verification.md)

---

## Objective

A committed, re-runnable script confirms (via the Render CLI) that the latest deploy of `beta`'s `api`/`agents` services is live, then drives the same brief→generate flow as task-02's local script against the deployed public API URL — proving the real `api/`↔`agents/` network path also works in the actual deployed `beta` environment, not only locally.

**Conditional on task-03's finding:** if task-03 found `beta`'s Render services are not actually live, this task cannot proceed as scoped — see Risk below.

---

## Technical Design

- **Approach:** Reuse task-02's script logic (same brief→generate→retrieve flow, same Firebase-token-based auth) but parameterize the target base URL — point at the deployed Render `api` service's public URL (from task-03's findings) instead of `localhost`. Before running the flow, use the Render CLI to confirm the latest deploy's status is live/healthy (per `render.com/docs/cli-reference`'s deploy-status commands) — this both proves the CLI itself is useful for this purpose (closing the "an existing CLI would be fantastic" ask this story originated from) and guards against running the smoke flow against a service mid-deploy or crashed.
- **Affected files / components:** New script, e.g. `scripts/smoke-e2e-render-beta.sh`, sharing as much logic as reasonable with task-02's `scripts/smoke-e2e-local.sh` (e.g. factor the brief→generate→retrieve flow into a shared function/file both scripts source, rather than duplicating it — decide the exact factoring when task-02's script already exists to reuse from).
- **Interfaces / contracts:** None.
- **Risk:** H if task-03 found `beta` isn't actually live — in that case this task cannot produce real evidence and must not fabricate a "passed" result. If blocked this way, report it plainly, mark this task's Done Criteria as unmet, and treat "getting `beta` actually deployed" as a residual for a future task/planning, not something this task silently works around by skipping verification. Render's free-tier cold start (per `beta-environment-design.md`, ~15 min idle) can also cause the first request to time out — mitigated by a warm-up request with a generous timeout before the real check.
- **Design notes:** Never commit a real `RENDER_API_KEY` or any real Firebase credentials used against the deployed `beta` project — read from environment variables the human supplies at run time, same convention as task-02.

---

## Implementation Steps

1. Using task-03's confirmed Render service info, write `scripts/smoke-e2e-render-beta.sh`: (a) `render` CLI check that the latest deploy for `grade-ops-ai-api`/`grade-ops-ai-agents` (or their actual names) is live, (b) issue a warm-up request to `api/`'s public health/root endpoint with a generous timeout (cold-start tolerant), (c) run the same brief→generate→retrieve flow as task-02, targeted at the deployed public URL.
2. Run the script once for real against the live `beta` environment and capture the output as evidence.
3. If task-03 found `beta` is not live: skip steps 1–2, document that finding clearly in this task's report, and do not mark this task's Done Criteria as met.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Render CLI confirms the latest deploy is live before the smoke flow runs | Script output shows a live/healthy deploy status check preceding the HTTP flow |
| 2 | Cold start is tolerated | Warm-up request completes (even if slow) before the timed real check |
| 3 | Real brief→generate→retrieve flow succeeds against the deployed URL | Same evidence bar as task-02 — a genuine structured draft, not a mocked/placeholder shape |
| 4 | Script is re-runnable | A second run succeeds again |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | Render CLI confirms `beta`'s `api`/`agents` services are deployed and live (task-03 + this task's own pre-check) |
| 2 | App compiles and starts | Already true by virtue of a successful deploy existing — this task doesn't build/deploy, it verifies what's already running |
| 3 | Connectivity or schema validation succeeds | Brief-intake step succeeding proves `api/`↔Neon Postgres connectivity in `beta` |
| 4 | Changed surface responds correctly | Full flow completes with a real generated draft from the deployed environment |
| 5 | No startup or migration regressions are visible | No errors surfaced in the script's run; if Render CLI exposes logs, spot-check for unexpected errors around the request window |

### Database / ORM Consistency Check

N/A — no database or ORM changes in this task.

---

## Done Criteria

- [ ] `scripts/smoke-e2e-render-beta.sh` exists, is executable, and is committed — **or**, if task-03 found `beta` isn't live, this criterion is explicitly marked not-applicable-yet with the finding documented, not silently skipped.
- [ ] If `beta` is live: one real run's output is captured as evidence in this task's report, showing a genuine generated draft produced by the deployed environment.
- [ ] The script fails clearly if the pre-deploy-status check shows the service isn't live, rather than proceeding to a confusing HTTP-level failure.
- [ ] All verification checks listed above pass (or are explicitly marked blocked, per the conditional Risk above).
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-04-e2e-integration-verification.md)
