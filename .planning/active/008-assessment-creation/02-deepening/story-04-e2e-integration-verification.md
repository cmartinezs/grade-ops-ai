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
| 3 | [Verify `beta` on Render is actually live](story-04-e2e-integration-verification/task-03-verify-render-beta-live.md) | GENERATE-DOCUMENT | TODO | Confirmed (via Render CLI) that `grade-ops-ai-api`/`grade-ops-ai-agents` Render services exist, have a deploy history, and auto-deploy is wired to this repo — or a documented finding that they don't, same spirit as `009`'s GCP finding |
| 4 | [Render post-deploy smoke script and evidence](story-04-e2e-integration-verification/task-04-render-post-deploy-smoke.md) | GENERATE-DOCUMENT | TODO | `scripts/smoke-e2e-render-beta.sh` using the Render CLI (`RENDER_API_KEY` auth) to confirm the latest deploy is live, then drive the same brief→generate flow against the public Render API URL; captured evidence |

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

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
