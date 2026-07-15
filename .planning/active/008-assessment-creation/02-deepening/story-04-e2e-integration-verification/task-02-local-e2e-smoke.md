# ⚛️ TASK 02 — Local end-to-end smoke script and evidence

> **Status:** TODO
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

## Done Criteria

- [ ] `scripts/smoke-e2e-local.sh` exists, is executable, and is committed.
- [ ] One real run's output is captured as evidence in this task's report — showing a genuine generated draft (not a placeholder/mocked shape) and a plausible `AgentExecutionLog`-derived cost/model value.
- [ ] The script fails clearly (not silently) if compose isn't running or credentials are missing.
- [ ] All verification checks listed above pass.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — no Render/beta work here, that's tasks 03–04.

---

> [← story file](../story-04-e2e-integration-verification.md)
