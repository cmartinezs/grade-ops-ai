# ⚛️ TASK 04 — Render post-deploy smoke script and evidence

> **Status:** DONE
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

## Evidence

Full raw, unedited command output (both runs, plus the log investigations that found each root cause) is captured separately in [`evidence/task-04-render-post-deploy-smoke.md`](evidence/task-04-render-post-deploy-smoke.md). Summary below.

`scripts/smoke-e2e-render-beta.sh` was written sharing the brief→generate→retrieve flow with task-02's script via a new `scripts/lib/e2e-smoke-flow.sh` (per this task's Technical Design), and run twice for real against the live `beta` environment (both services confirmed live per task-03).

**Run 1** found two real issues: (a) a script bug where the warm-up `curl`'s stderr got mixed into its captured status, and (b) warming up only `api/` isn't enough — `agents/` was fully cold (idle since `2026-07-16T18:27:19Z`, confirmed to take ~52s to wake from a direct timed check), and the internal `api/`→`agents/` call hit Render's edge while `agents/` was still waking, getting back a `429` that `api/` surfaced as `AGENT_CALL_FAILED`/`AGENT_REJECTED`. Fixed both: the script now warms up `agents/` directly (not just `api/`) with a retry on the first attempt, and the warm-up status capture no longer merges stderr.

**Run 2**, after that fix, found a *different*, deeper real issue: both services responded to their warm-up requests in well under a second (confirmed warm), yet draft generation still failed identically. `api/`'s logs for this run show the actual cause: `agents/` returned `422 {"errorCode":"MALFORMED_OUTPUT","message":"Assessment generation response could not be parsed: 401: Invalid API Key", ...}`. Traced to `agents/src/main/resources/application.yml:16` (`app.agents.llm.default-provider: groq`) and `application-beta.yml` (Groq key sourced from `${GRADEOPS_GROQ_API_KEY}`) — **the deployed `grade-ops-ai-agents` Render service's `GRADEOPS_GROQ_API_KEY` environment variable is invalid, expired, or unset.** This is a genuine infra/credential gap in the beta environment, not a defect in this script or in `api/`'s/`agents/`'s application code — `agents/`'s own error-handling worked correctly (returned a structured `MALFORMED_OUTPUT` error rather than crashing), and `api/` correctly surfaced it as a failed generation rather than a false success.

Per this task's own Risk section ("must not fabricate a 'passed' result... report it plainly, mark this task's Done Criteria as unmet"), this task did not claim a passing end-to-end run at that point. A real Render API call (`GET /v1/services/srv-d8oqosernols73erqc3g/env-vars`) confirmed the precise root cause: the deployed service had `AI_MODEL_NAME`/`GOOGLE_AI_API_KEY` (matching the design doc) but not `GRADEOPS_GROQ_API_KEY`/`GRADEOPS_GEMINI_API_KEY` (matching current code) — design-doc-vs-code naming drift, not just a bad key. The human then added `GRADEOPS_GROQ_API_KEY`/`GRADEOPS_GROQ_MODEL` on the `grade-ops-ai-agents` Render service and it auto-restarted (`manual` deploy, `live`, `2026-07-16T23:47:54Z`, same commit — a restart, not a redeploy).

**Runs 3 and 4**, after that fix, both passed for real: genuine structured drafts ("Recursive Factorial Calculation", "Recursive Fibonacci Calculation"), each from a fresh timestamp-suffixed teacher, confirming both a real end-to-end pass and re-runnability. Run 3 also exercised the retry-on-first-attempt warm-up logic for real (one transient no-response on `api/`'s first warm-up request, self-healed on the automatic retry, matching task-02's documented local pattern).

Recorded as `Inconsistencies Found` row #4 on the story, `DONE` — resolved by the human's Render env var fix, re-verified by two full real runs.

**Scoping note:** unlike `smoke-e2e-local.sh`, this script does not independently verify a persisted `AgentExecutionLog` row in Postgres — beta's Neon database has no access path from this script without a Neon connection string, which wasn't available when this task was executed.

**Resolved 2026-07-17 — local regression check on the shared-logic refactor:** the shared-logic refactor (`scripts/lib/e2e-smoke-flow.sh`) touches task-02's `scripts/smoke-e2e-local.sh`. Docker was unavailable in this environment at implementation time (`docker compose ps` → `Input/output error`, a WSL2/Docker Desktop integration issue unrelated to this change), so this was originally verified only by direct code review (each extracted function confirmed as a verbatim move of the original inline logic, no `local` declarations, preserving variable scope). Once Docker came back up, ran `scripts/smoke-e2e-local.sh` twice for real — both passed cleanly with distinct generated drafts and distinct persisted `AgentExecutionLog` rows (`status=COMPLETED` each time), confirming the code review was correct and the refactor introduced no regression. Full output in `evidence/task-04-render-post-deploy-smoke.md`'s "Local regression check" section.

### Correction — code review (2026-07-16)

Human review (`.code-review/story-04-e2e-integration-verification/task-04-render-post-deploy-smoke.md`) found P2: `scripts/smoke-e2e-render-beta.sh` tells operators to see `.env.example` for required config, and hard-fails without `RENDER_API_KEY`/`RENDER_WORKSPACE_ID`/`BETA_API_BASE_URL` — but the committed `.env.example` didn't document any of the three, leaving the "committed, re-runnable script" handoff incomplete from the repo alone.

Verified before implementing: confirmed by direct `grep` that `.env.example` genuinely had zero matches for `RENDER`/`BETA_API` — the finding was accurate, not a false positive.

Fixed: added a "Render beta smoke" section to `.env.example` documenting `RENDER_API_KEY`, `RENDER_WORKSPACE_ID` (with a pointer to task-03's evidence for how to resolve it), `BETA_API_BASE_URL` (with a note on the `*.onrender.com` slug-vs-display-name distinction discovered during this task), and the two optional service-name overrides — matching the file's existing style and language. Also fixed a stale sentence in this task's own Evidence section that still said generation "is currently failing" after the Groq fix had already resolved it.

---

## Done Criteria

- [x] `scripts/smoke-e2e-render-beta.sh` exists, is executable, and is committed.
- [x] If `beta` is live: one real run's output is captured as evidence in this task's report, showing a genuine generated draft produced by the deployed environment. Runs 3 and 4 (post Groq-credential fix) both show genuine generated drafts from the deployed environment.
- [x] The script fails clearly if the pre-deploy-status check shows the service isn't live, rather than proceeding to a confusing HTTP-level failure.
- [x] All verification checks listed above pass (or are explicitly marked blocked, per the conditional Risk above). All four verifications now pass for real.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-04-e2e-integration-verification.md)
