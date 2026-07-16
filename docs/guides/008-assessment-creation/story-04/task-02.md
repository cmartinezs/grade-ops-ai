# Local E2e Smoke

**Source:** task-02 | **Area:** unknown | **Date:** 2026-07-16

## What it does
A committed, re-runnable script drives a real brief→generate flow through `api/`'s public endpoints against the full docker-compose stack (task-01), proving `api/` reaches `agents/` over the real network and produces a genuine `AssessmentDraft` + `AgentExecutionLog` — not a mocked result. Captured evidence (request/response logs) from one successful real run is attached to this task's report.

---

## How to use it
- Create `scripts/` at repo root if absent.
- Write `scripts/smoke-e2e-local.sh`: (a) verify `docker compose ps` shows `db`, `api`, `agents` running (fail fast with a clear message if not — do not attempt to start them itself, since task-01's compose config is the source of truth for how they start), (b) obtain a real Firebase ID token via the documented REST API call, (c) `POST /api/v1/assessments` with a real brief payload (learning goal, topic, level, duration, language), (d) `POST /api/v1/assessments/{id}/draft` to trigger generation, (e) `GET /api/v1/assessments/{id}/draft` to confirm retrieval, (f) print a clear pass/fail summary with the key response fields (draft title, model used, cost estimate) as evidence.
- Run the script once for real against the running compose stack (with real Groq credentials and a real provisioned test teacher) and capture the output as evidence for this task's report.
- Document any one-time local setup this script assumes (test teacher provisioning, `.env` population) directly in the script's header comment, so a future developer isn't blocked figuring out preconditions.
- --

## Example
`POST /api/v1/assessments`
