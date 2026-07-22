# ⚛️ TASK 02 — Local end-to-end smoke script and evidence

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← story file](../story-04-e2e-integration-verification.md)

---

## Objective

A committed, re-runnable script drives a real brief→generate flow through `api/`'s public endpoints against the full docker-compose stack (task-01), proving `api/` reaches `agents/` over the real network and produces a genuine `AssessmentDraft` + `AgentExecutionLog` — not a mocked result. Captured evidence (request/response logs) from one successful real run is attached to this task's report.

---

## Technical Design

- **Approach:** Use the already-documented local-dev auth pattern from `docs/09-developer-guide/01-local-setup.md` — provision (or reuse an existing provisioned) teacher account, then obtain a real Firebase ID token via `POST https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=<FIREBASE_API_KEY>`. This is a real external call to Firebase's own Identity Toolkit REST API (not `api/`), so it needs a real Firebase Web API key — already available locally per that same doc's setup steps. The script then uses that token as `Authorization: Bearer <idToken>` against `api/`'s real endpoints: `POST /api/v1/assessments` (brief intake), `POST /api/v1/assessments/{id}/draft` (draft generation — this is the call that internally invokes `agentclient` → `agents/`), `GET /api/v1/assessments/{id}/draft` (retrieval, confirms persistence). A successful, non-empty structured draft response with a plausible `AgentExecutionLog`-derived `costEstimate`/`model` (visible via the draft-generation response or a follow-up query, per `api/`'s `GenerateAssessmentDraftResponse` shape) is the proof the real network path and real LLM call both worked — a mocked path could never produce this, since `agents/`'s prompt/schema-validation pipeline would reject any hand-crafted fake response shape it didn't itself produce.
- **Affected files / components:** New script, e.g. `scripts/smoke-e2e-local.sh` (create `scripts/` at repo root if it doesn't exist — first script of its kind in this repo). Reads required config (`FIREBASE_API_KEY`, a provisioned test teacher's email/password) from environment variables or a local `.env`, never hardcoded.
- **Interfaces / contracts:** None — the script is a test harness, not a new application interface.
- **Risk:** M — depends on a real, already-provisioned test teacher account existing locally (per `001-teacher-onboarding`'s provisioning flow) and a real Groq API key being populated in `agents/`'s `.env`. If neither exists yet in this developer's environment, the script must fail with a clear, actionable error (e.g. "no test teacher found — run provisioning first"), not a confusing downstream 401/500.
- **Design notes:** Keep the script idempotent-safe to re-run — creating a new `AssessmentBrief` each run is fine (it's what a real teacher would do repeatedly), but the script should not depend on any one-time state from a previous run to succeed.

---

## Implementation Steps

1. Create `scripts/` at repo root if absent.
2. Write `scripts/smoke-e2e-local.sh`: (a) verify `docker compose ps` shows `db`, `api`, `agents` running (fail fast with a clear message if not — do not attempt to start them itself, since task-01's compose config is the source of truth for how they start), (b) obtain a real Firebase ID token via the documented REST API call, (c) `POST /api/v1/assessments` with a real brief payload (learning goal, topic, level, duration, language), (d) `POST /api/v1/assessments/{id}/draft` to trigger generation, (e) `GET /api/v1/assessments/{id}/draft` to confirm retrieval, (f) print a clear pass/fail summary with the key response fields (draft title, model used, cost estimate) as evidence.
3. Run the script once for real against the running compose stack (with real Groq credentials and a real provisioned test teacher) and capture the output as evidence for this task's report.
4. Document any one-time local setup this script assumes (test teacher provisioning, `.env` population) directly in the script's header comment, so a future developer isn't blocked figuring out preconditions.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Script obtains a real Firebase ID token | Script output shows a non-empty `idToken` retrieved from the real Identity Toolkit REST API |
| 2 | Brief intake persists before generation is triggered | `POST /api/v1/assessments` returns `201`/`200` with a real `assessmentId` before step 2 begins |
| 3 | Draft generation reaches `agents/` for real | `POST /api/v1/assessments/{id}/draft` returns a structured draft (title, context, instructions, objectives, deliverables, constraints all populated) — a shape only `agents/`'s real Gemini/Groq pipeline can produce |
| 4 | Draft is retrievable | `GET /api/v1/assessments/{id}/draft` returns the same draft just generated |
| 5 | Script is re-runnable | Running the script a second time succeeds again (creates a new brief/draft, doesn't error on leftover state) |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up -d db api agents` (task-01's stack), confirmed healthy/running before the script starts |
| 2 | App compiles and starts | Already covered by task-01; this task only drives traffic against the running stack |
| 3 | Connectivity or schema validation succeeds | Script's brief-intake step succeeding proves `api/`↔Postgres connectivity; draft-generation step succeeding proves `api/`↔`agents/` connectivity |
| 4 | Changed surface responds correctly | Full script run (steps c–e above) completes with a real generated draft |
| 5 | No startup or migration regressions are visible | `docker compose logs api agents` show no new errors during the script's run |

### Database / ORM Consistency Check

N/A — no database or ORM changes in this task; it exercises the existing schema through the real API.

---

## Evidence

Real run against the local compose stack (`db`, `api`, `agents` — `web` excluded, pre-existing unrelated docker build break per task-01's retrospective), executed twice to prove re-runnability, plus two negative checks:

```
==> Checking docker compose services (db, api, agents)...
    db, api, agents are running.
==> Provisioning test teacher smoke-e2e-1784135910@gradeops.test...
    provisioned firebaseUid=YBhgmL2D4MaB5HKfKGxa0P6pgLF3
==> Setting test teacher password...
    password set.
==> Obtaining a real Firebase ID token...
    idToken obtained (1021 chars).
==> POST /api/v1/assessments (brief intake)...
    assessmentId=e0df2ae9-d2f3-4351-a5bd-2ae5137f444d
==> POST /api/v1/assessments/e0df2ae9-d2f3-4351-a5bd-2ae5137f444d/draft (triggers agents/ over the real network)...
    draft generated: title="Recursive Tree Traversal Challenge", objectives=2
==> GET /api/v1/assessments/e0df2ae9-d2f3-4351-a5bd-2ae5137f444d/draft (confirms persistence)...
    retrieval matches generated draft.

PASS: real brief -> generate -> retrieve flow completed against the local compose stack.
  draft title       : Recursive Tree Traversal Challenge
  draft objectives  : 2
  full draft payload:
{
  "draftId": "1ba20417-cc6a-42de-8074-4feda2482fa4",
  "title": "Recursive Tree Traversal Challenge",
  "context": "Intermediate Python students practice recursive algorithms under a course on algorithms and data structures.",
  "instructions": "Implement a recursive function to traverse a binary tree and return the sum of all node values.",
  "objectives": ["Apply recursive problem-solving strategies", "Understand recursive function calls and returns"],
  "deliverables": ["A single Python file with the recursive tree traversal function"],
  "constraints": ["Must be implemented recursively", "Must run within 60 minutes of development time", "Submission deadline: end of 60-minute session", "Only use built-in Python data structures"],
  "versionNumber": 1
}
```

- **Re-run:** second full run immediately after produced a different, equally real draft ("Recursive Factorial Function Generator") — confirms the script is re-runnable without depending on state from the previous run (fresh timestamp-suffixed teacher email each time).
- **`agents`-down failure:** `docker compose stop agents` then running the script → `FAIL: service 'agents' is not running. Run 'docker compose up -d db api agents' first...`, exit code 1.
- **Missing-credentials failure:** running with `.env` renamed away → `INTERNAL_API_SECRET: INTERNAL_API_SECRET is required — set it in .../.env (see .env.example)`, exit code 1 (bash's `: "${VAR:?msg}"` guard).
- **Log check:** `docker compose logs api agents` across all runs contains zero `ERROR`/`Exception` lines.
- **Design-vs-reality correction:** the task's Technical Design assumed the draft-generation response would carry a "plausible `AgentExecutionLog`-derived `costEstimate`/`model` value." Checking the real `GenerateAssessmentDraftResponse` DTO (`api/.../response/GenerateAssessmentDraftResponse.java`) shows it does not — only `draftId, title, context, instructions, objectives, deliverables, constraints, versionNumber`. The `AgentExecutionLog` has no query endpoint via the API.

### Correction — code review (2026-07-16)

Human review (`.code-review/story-04-e2e-integration-verification/task-02-local-e2e-smoke.md`) found P1: the script proved the draft response but never independently verified the persisted `AgentExecutionLog`, which both the story's Done Criteria and this task's own Objective explicitly require. The prior "no query endpoint" note undersold what was actually verifiable — a direct DB read was always possible.

Fixed: after draft generation, the script now runs `docker compose exec -T db psql -U gradeops -d gradeops` to query `agent_execution_logs WHERE assessment_id = '<assessmentId>'` and asserts `status = 'COMPLETED'` (confirmed the literal success value from `agents/`'s `AgentExecutionLogPayload.java:35` — not `'SUCCEEDED'` as initially suggested), non-empty `model` and `agent_execution_id`, and that `draft_id` matches the generated draft (confirming `DraftGenerationCoordinator`'s two-step save/backfill actually happened).

Re-verified with two more real runs against the live stack:

```
==> Verifying persisted AgentExecutionLog in Postgres...
    persisted log confirmed: status=COMPLETED, model=llama-3.3-70b-versatile, agent_execution_id=8b09e493-97c8-4fb7-b7c8-69106266068a, draft_id backfilled correctly
```

One transient `401` on brief intake was observed on the very first request immediately after `docker compose up -d` (before any warm-up request) — the script failed clearly (`FAIL: brief intake returned HTTP 401`) rather than silently, and two immediate re-runs both succeeded. Not chased further: `api`/`agents` logs showed nothing (the auth filter only logs failures at DEBUG), and the script's own fail-loud behavior is exactly the intended failure mode, so no code change was needed. Worth a note for whoever runs this next: if the very first request after a fresh `docker compose up -d` gets a 401, just re-run.

---

## Done Criteria

- [x] `scripts/smoke-e2e-local.sh` exists, is executable, and is committed.
- [x] One real run's output is captured as evidence in this task's report — showing a genuine generated draft (not a placeholder/mocked shape) and a persisted `AgentExecutionLog` verified directly in Postgres (status, model, agent_execution_id, backfilled draft_id).
- [x] The script fails clearly (not silently) if compose isn't running or credentials are missing.
- [x] All verification checks listed above pass.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — no Render/beta work here, that's tasks 03–04.

---

> [← story file](../story-04-e2e-integration-verification.md)
