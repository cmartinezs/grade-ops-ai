# ⚛️ TASK 05 — agentclient module

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

Create the `agentclient` module — the only part of `api/` allowed to call `agents/` — with a client that sends an `AssessmentCommand`-shaped request to the internal agent endpoint and maps the response back to an `AssessmentResult` + execution-log metadata.

---

## Technical Design

- **Approach:** plain synchronous HTTP client using Spring's `RestClient` (already available via `spring-boot-starter-webmvc`; the project has no reactive/WebFlux dependency, so `WebClient` is not used). This module does **not** need a Spring AI dependency — Gemini is only called inside `agents/`; `agentclient` just does a REST call and JSON mapping, matching the project rule "no other module imports Spring AI" (that rule protects `api/`'s other bounded contexts from needing an AI dependency, not `agentclient` itself from needing Spring AI — `agentclient` needs neither).
- **Auth design decision (checked against `infra/terraform/environments/demo/cloud_run.tf`):** the `agents/` Cloud Run service is `INGRESS_TRAFFIC_INTERNAL_ONLY` and grants `roles/run.invoker` only to `api/`'s service account — this is the **primary** protection in `demo`/production, enforced by Cloud Run's platform-level IAM before a request ever reaches the Spring Boot app (real OIDC identity-token auth, matching `CLAUDE.md`'s description). Fetching and attaching a real Google-signed identity token from `api/` (e.g. via `google-auth-library`'s `IdTokenProvider` against the metadata server) is the fully-correct production implementation, but is meaningful extra scope (new dependency, credential plumbing, no local-dev equivalent since there's no real Cloud Run IAM in `docker compose`). For this task, implement the **shared-secret header** (`X-Internal-Key` / `app.internal.secret`, already scaffolded identically in both `api/` and `agents/`) as defense-in-depth that works identically in local dev and prod, and record the real-OIDC-token gap as a residual rather than silently skipping it or over-building it without a decision. See `agents/.planning/active/001-assessment-creation/TRACEABILITY.md` residual `R-01`, which should be updated to reflect this two-layer design (Cloud Run IAM primary in prod, shared secret defense-in-depth) instead of framing it as a pure documentation inconsistency.
- **Affected files / components:**
  - `agentclient/AgentClientConfig.java` (new — `RestClient` bean, base URL + `X-Internal-Key` header from config)
  - `agentclient/AssessmentAgentClient.java` (new — `generate(AssessmentCommand) -> AssessmentAgentResponse`)
  - `agentclient/AssessmentCommand.java`, `agentclient/AssessmentAgentResponse.java` (new — `api/`-side mirror DTOs of `agents/`'s `AssessmentCommand`/`AssessmentExecutionOutcome`; kept as a separate copy rather than a shared library, since the two services deploy independently). **As of 2026-07-10**, `agents/`'s `AssessmentCommand` has 8 fields, not 7: `learningGoal`, `topic`, `level`, `duration`, `language`, `adjustmentNotes`, `previousDraftId`, and `previousDraft` (the last one added post-hoc — `previousDraftId` alone cannot supply the regeneration prompt with content, since `agents/` never persists data or calls back into `api/`; `api/` must resolve the ID on its own side, from its own persisted `AssessmentDraft`, and send the rendered content directly as `previousDraft`). See task-08's Technical Design for how this content is built.
  - `agentclient/AgentClientException.java` (new — wraps connection failures, non-2xx responses, and malformed-output errors surfaced by `agents/`)
  - `application.yml` / `application-local.yml` — add `app.agents.base-url` and reuse `app.internal.secret` for the outgoing header
- **Interfaces / contracts:** `AssessmentAgentClient.generate(AssessmentCommand command) -> AssessmentAgentResponse` where `AssessmentAgentResponse` mirrors `agents/`'s `AssessmentExecutionOutcome` (`result`, `log`).
- **Risk:** M — this is the first cross-service HTTP call in the project; network/timeout/auth failures must not leak as raw 500s to the teacher. Mitigated by `AgentClientException` with a reason code, mapped to a clean error in task-07/08.
- **Design notes:** set an explicit connect/read timeout on the `RestClient` (Gemini calls can be slow) — do not use the default (potentially unbounded) timeout.

---

## Implementation Steps

1. Create the `agentclient` package under `cl.gradeops.ai.api.agentclient`.
2. Create `AssessmentCommand.java` and `AssessmentAgentResponse.java` — mirror `agents/`'s contract field-for-field (cross-check against `agents/.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-01-contracts.md`).
3. Create `AgentClientConfig.java` — `RestClient` bean configured with base URL (`${app.agents.base-url}`), explicit timeouts, and a default `X-Internal-Key` header sourced from `${app.internal.secret}`.
4. Create `AgentClientException.java` (reason codes: `UNREACHABLE`, `AGENT_REJECTED` for 4xx from `agents/`, `AGENT_ERROR` for 5xx).
5. Create `AssessmentAgentClient.java` — `generate(AssessmentCommand)` posts to `/internal/agents/assessment`, maps 2xx to `AssessmentAgentResponse`, maps non-2xx/timeouts to `AgentClientException`.
6. Add `app.agents.base-url` to `application.yml` (env-driven, e.g. `${AGENTS_BASE_URL:http://localhost:8081}`) and `application-local.yml` if one exists (create if not, per `CLAUDE.md`'s local-dev guidance).

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Successful call maps to `AssessmentAgentResponse` | Unit test with a mocked/stubbed HTTP server (e.g. `MockWebServer` or `RestClient` test support) returning a valid JSON body |
| 2 | Non-2xx response maps to `AgentClientException` with the right reason code | Unit test simulating a 422 and a 500 response |
| 3 | Connection failure maps to `AgentClientException(UNREACHABLE)` | Unit test pointing the client at an unreachable port |
| 4 | `./mvnw test` passes | Full suite green |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | None required for this task in isolation (no real `agents/` call yet — that's task-07) |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors with the new config properties present |
| 3 | Connectivity or schema validation succeeds | N/A — no DB change |
| 4 | Changed surface responds correctly | N/A — no new endpoint yet, client-only |
| 5 | No startup or migration regressions are visible | App logs show no missing-property errors for `app.agents.base-url` |

### Database / ORM Consistency Check

N/A — no database or ORM involved.

---

## Done Criteria

- [ ] `agentclient` module exists and is the only package importing/calling `agents/`.
- [ ] `AssessmentCommand`/`AssessmentAgentResponse` field names verified against `agents/`'s contract.
- [ ] Non-2xx and connection failures are mapped to `AgentClientException`, never propagate as raw exceptions.
- [ ] Shared-secret header sent on every call; timeout explicitly configured (not default/unbounded).
- [ ] Real-OIDC-token gap recorded as a residual (not silently dropped).
- [ ] `./mvnw test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
