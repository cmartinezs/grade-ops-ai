# 🔍 DEEPENING: Story 04 — e2e-integration-verification

> **Status:** IN PROGRESS
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Prove — with a real network call, not a mock — that `api/` can reach `agents/`'s internal endpoint end-to-end and produce a genuine `AssessmentDraft` + `AgentExecutionLog`. Two phases:

1. **Local (docker-compose):** add `agents/` to the root `compose.yml` (currently absent — only `db`, `api`, `web` exist), run the full stack, and drive a real brief→generate flow through `api/`'s public endpoints, hitting `agents/`'s real internal endpoint and a real Groq/Gemini call.
2. **Deployed (`beta` on Render):** verify the `beta` environment described in `docs/04-architecture/beta-environment-design.md` is actually live and auto-deploying on push (do not assume — confirm), then run the same smoke flow against the deployed public API URL, scripted with the official Render CLI (`github.com/render-oss/cli`, `RENDER_API_KEY` for non-interactive auth) for deploy/service status inspection.

**Why this exists:** neither `agents/001-assessment-creation` nor `api/003-assessment-creation`'s test suites exercise a real HTTP call between the two services — `api/`'s `AssessmentCreationFlowIntegrationTest` and every handler-level integration test mock `AssessmentAgentClient` directly; `agents/`'s own verification only proved a direct `curl` from the developer, never a call originating from `api/`'s real code path. This story closes that specific, previously-unverified gap.

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Local docker-compose run needs real credentials (Groq API key, Firebase Admin key, a valid teacher auth token) that can't be safely hardcoded or committed | M | H | Use `.env`-style local secrets already established by `agents/`'s `DotenvEnvironmentPostProcessor` pattern and `api/`'s existing `FIREBASE_ADMIN_KEY_PATH` compose convention; never commit real values |
| `beta` on Render may not actually be provisioned yet, despite being fully designed in `docs/04-architecture/beta-environment-design.md` (`Status: Approved` is a design decision, not deployment evidence) — mirrors this workspace's `demo`/GCP finding in `009-groq-infra-provisioning` where documented Terraform had never been applied | M | M | First task in phase 2 must verify actual Render service existence and deploy history via the Render CLI/dashboard before scripting anything against it — do not assume |
| Render free tier cold-starts after 15 min idle (per `beta-environment-design.md`) — a smoke check could time out on a cold service, producing a false failure | L | M | Smoke script retries with a generous timeout on the first request, or issues a warm-up request before the real check |
| This story spans three external systems (Groq/Gemini, Render, Neon) outside this repo's control — flakiness is possible and shouldn't be conflated with a real regression | M | M | Smoke evidence is captured with raw request/response logs so a failure can be diagnosed as external-service flakiness vs. a real integration break before reporting it as a blocker |

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Add `agents` service to root `compose.yml`](story-04-e2e-integration-verification/task-01-compose-agents-service.md) | GENERATE-DOCUMENT | DONE | `compose.yml` — new `agents` service (build context `./agents`, real env vars, internal network, `api`'s `AGENTS_BASE_URL` wired to it) |
| 2 | [Local end-to-end smoke script and evidence](story-04-e2e-integration-verification/task-02-local-e2e-smoke.md) | GENERATE-DOCUMENT | DONE | `scripts/smoke-e2e-local.sh` driving a real brief→generate flow through the compose stack; captured evidence of a real `AssessmentDraft` + `AgentExecutionLog` produced via a genuine Groq/Gemini call |
| 3 | [Verify `beta` on Render is actually live](story-04-e2e-integration-verification/task-03-verify-render-beta-live.md) | GENERATE-DOCUMENT | DONE | Confirmed (via Render CLI) that `grade-ops-ai-api`/`grade-ops-ai-agents` Render services exist, have a deploy history, and auto-deploy is wired to this repo — or a documented finding that they don't, same spirit as `009`'s GCP finding |
| 4 | [Render post-deploy smoke script and evidence](story-04-e2e-integration-verification/task-04-render-post-deploy-smoke.md) | GENERATE-DOCUMENT | IN PROGRESS | `scripts/smoke-e2e-render-beta.sh` using the Render CLI (`RENDER_API_KEY` auth) to confirm the latest deploy is live, then drive the same brief→generate flow against the public Render API URL; captured evidence |

---

## Done Criteria

- [ ] `compose.yml` includes a working `agents` service; `docker compose up` brings up `db`, `api`, `agents`, `web` together without manual intervention beyond populating local secrets.
- [ ] A real brief→generate request through `api/`'s public endpoint, running against the docker-compose stack, produces a genuine `AssessmentDraft` and a persisted `AgentExecutionLog` with a real (non-zero-cost-estimate-unless-Groq-free-tier) model response — not a mocked/stubbed result.
- [ ] `beta` on Render is confirmed either genuinely live (with evidence: service list, deploy history) or genuinely not yet provisioned (documented as a finding, not assumed either way).
- [ ] If `beta` is live: the same brief→generate flow succeeds against the deployed public API URL, verified with captured request/response evidence via a Render-CLI-scripted smoke check.
- [ ] Both smoke scripts are committed and re-runnable — not one-off manual commands lost to shell history.
- [ ] TRACEABILITY.md updated with new terms from this story.

---

## Inconsistencies Found

*Record any contradictions or gaps detected during this story. Use RECORD-INCONSISTENCY workflow.*

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | Root `compose.yml` is missing an `agents` service entirely — only `db`, `api`, `web` are defined, even though `agents/` has been a real, implemented service since `agents/001-assessment-creation` and `agents/002-groq-genai-provider` were completed. | `compose.yml` | DONE | Resolved by this story's task 1 |
| 2 | Neither `agents/001-assessment-creation` nor `api/003-assessment-creation`'s automated test suites exercise a real network call between the two services — discovered while checking `008 story-01`'s Done Criteria #2 ("internal agent endpoint is reachable from `api/` in the target environment"), which cannot be satisfied by either child planning's existing evidence. | `api/src/test/java/cl/gradeops/ai/api/assessment/AssessmentCreationFlowIntegrationTest.java`, `GenerateAssessmentDraftHandlerIntegrationTest.java`, `AssessmentAgentClientTest.java` (all mock/stub the client or the HTTP layer) | DONE | This story exists specifically to resolve it |
| 3 | The `gradeops-agents` Render service (`beta`) tracked branch `master` instead of `develop`. `gradeops-api` correctly tracked `develop` and was current, but `master`'s last commit touching `agents/` was a month older than `develop`'s (38 commits behind, missing the entire `002-groq-genai-provider` story). Auto-deploy-on-push was genuinely configured and firing — it was just deploying the wrong branch, so the live `beta` `agents/` service did not reflect current code. Also renamed both services to match `beta-environment-design.md` (`gradeops-api`/`gradeops-agents` → `grade-ops-ai-api`/`grade-ops-ai-agents`). | Render service `grade-ops-ai-agents` (`srv-d8oqosernols73erqc3g`) branch setting; `docs/04-architecture/beta-environment-design.md` | DONE | Fixed directly by the human on the Render Dashboard: repointed to `develop` and renamed both services. Re-verified via Render CLI — `grade-ops-ai-agents` redeployed and is `live` at `develop`'s exact HEAD commit (`6a9c8fd4e743`). |
| 4 | Root cause of the Groq 401 confirmed to be design-doc-vs-code drift, not just an unset var. `GET /v1/services/srv-d8oqosernols73erqc3g/env-vars` (Render API, real call) shows `grade-ops-ai-agents` has exactly `INTERNAL_API_SECRET`, `AI_MODEL_NAME=gemini-2.0-flash`, `GOOGLE_AI_API_KEY=...`, `SPRING_PROFILES_ACTIVE=beta` — these match `docs/04-architecture/beta-environment-design.md` (lines 93-103, 177, 202-203) exactly. But the actual shipped `agents/src/main/resources/application-beta.yml` (refactored during `002-groq-genai-provider` to support both providers) expects `GRADEOPS_GEMINI_API_KEY`/`GRADEOPS_GEMINI_MODEL` (Google GenAI) and `GRADEOPS_GROQ_API_KEY`/`GRADEOPS_GROQ_MODEL` (Groq) — none of which are set. The design doc was never updated when the code's env var naming changed, and the Render service still has the stale names. Both providers are actually broken on the deployed service, not just Groq — Groq is only the one that surfaced, since `app.agents.llm.default-provider: groq` (`application.yml:16`) makes it the active default regardless of profile. | `grade-ops-ai-agents` Render service env vars; `agents/src/main/resources/application-beta.yml`; `docs/04-architecture/beta-environment-design.md` (stale) | OPEN | Needs a human decision, not a silent fix: (a) rename/add the Render env vars to match current code (`GOOGLE_AI_API_KEY`→`GRADEOPS_GEMINI_API_KEY`, `AI_MODEL_NAME`→`GRADEOPS_GEMINI_MODEL`, plus a real `GRADEOPS_GROQ_API_KEY` if Groq should stay reachable too), and/or (b) update `beta-environment-design.md` to the current var names, and/or (c) reconsider whether `default-provider` should be `gemini` for beta per the design doc's "Google AI Studio (primary)" intent instead of the code's unconditional `groq` default. Then re-run `scripts/smoke-e2e-render-beta.sh` to confirm. Blocks task-04's full evidence bar until resolved. |

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
