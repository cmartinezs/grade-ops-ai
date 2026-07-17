# Render Post Deploy Smoke

**Source:** task-04 | **Area:** unknown | **Date:** 2026-07-17

## What it does
A committed, re-runnable script confirms (via the Render CLI) that the latest deploy of `beta`'s `api`/`agents` services is live, then drives the same brief→generate flow as task-02's local script against the deployed public API URL — proving the real `api/`↔`agents/` network path also works in the actual deployed `beta` environment, not only locally.

**Conditional on task-03's finding:** if task-03 found `beta`'s Render services are not actually live, this task cannot proceed as scoped — see Risk below.

---

## How to use it
- Using task-03's confirmed Render service info, write `scripts/smoke-e2e-render-beta.sh`: (a) `render` CLI check that the latest deploy for `grade-ops-ai-api`/`grade-ops-ai-agents` (or their actual names) is live, (b) issue a warm-up request to `api/`'s public health/root endpoint with a generous timeout (cold-start tolerant), (c) run the same brief→generate→retrieve flow as task-02, targeted at the deployed public URL.
- Run the script once for real against the live `beta` environment and capture the output as evidence.
- If task-03 found `beta` is not live: skip steps 1–2, document that finding clearly in this task's report, and do not mark this task's Done Criteria as met.
- --

## Example
Use `Using` through the public interface introduced by this task.
