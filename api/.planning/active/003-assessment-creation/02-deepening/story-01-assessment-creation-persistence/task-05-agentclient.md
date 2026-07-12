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
  - `agentclient/AssessmentCommand.java`, `agentclient/AssessmentAgentResponse.java` (new — `api/`-side mirror DTOs of `agents/`'s `AssessmentCommand`/`AssessmentExecutionOutcome`; kept as a separate copy rather than a shared library, since the two services deploy independently). **As of 2026-07-10**, `agents/`'s `AssessmentCommand` has 8 fields, not 7: `learningGoal`, `topic`, `level`, `duration`, `language`, `adjustmentNotes`, `previousDraftId`, and `previousDraft` (the last one added post-hoc — `previousDraftId` alone cannot supply the regeneration prompt with content, since `agents/` never persists data or calls back into `api/`; `api/` must resolve the ID on its own side, from its own persisted `AssessmentDraft`, and send the rendered content directly as `previousDraft`). See task-08's Technical Design for how this content is built. **As of 2026-07-12** (`agents/.planning/active/002-groq-genai-provider`, story-01 task-03), `agents/`'s `AssessmentCommand` gained two more nullable fields, both optional per-request overrides: `provider` (e.g. `"gemini"`, `"groq"`; omitted defaults to `"groq"`) and `model` (a literal provider-specific model name, forwarded as a per-call option to the resolved provider — not validated against a list of models the provider actually supports, so an unrecognized value surfaces as whatever error that provider returns). An unrecognized `provider` value is rejected by `agents/` with a 422 (`AssessmentAgentException(INVALID_COMMAND)`), surfaced here as `AgentClientException(AGENT_REJECTED)` — no special handling needed beyond the existing non-2xx mapping. `api/` is not required to expose either field to its own callers yet; they only need to exist on the mirror DTO so a `null`/absent value passes through unchanged.
  - `agentclient/AssessmentAgentResponse.java` **must mirror `agents/`'s full `AgentExecutionLogPayload`, not a subset** (verified 2026-07-12 against `agents/src/main/java/.../assessment/application/result/AgentExecutionLogPayload.java`, ground truth): `agentExecutionId` (UUID), `agentName`, `model`, `promptVersion`, `inputHash`, `outputHash`, `estimatedInputTokens`, `estimatedOutputTokens`, `costEstimate`, `status`, `errorCode`, `startedAt`, `finishedAt` — 13 fields, not just `model`/`costEstimate`/`status` as this task's own earlier draft assumed. `status` and `errorCode` are two separate fields, not one — do not conflate them into a single "status" property on the mirror DTO. See task-07's `V12` migration for the corresponding DB columns.
  - **Correlation ID (added 2026-07-12, found by cross-checking `agents/`'s finished `CorrelationIdFilter`):** `agents/`'s internal endpoint honors an inbound `X-Correlation-Id` header, generates one if absent, returns it in the response header, and includes it in `AgentErrorResponse.correlationId` on every error. `api/` has no existing correlation-ID convention of its own (checked: no match anywhere in `api/src/main/java`) — `AgentClientConfig`'s `RestClient` must generate a correlation ID per outgoing call (`UUID.randomUUID()`, or reuse an inbound one if `api/` later adopts its own inbound-correlation convention) and set it as the outbound `X-Correlation-Id` header, so cross-service log tracing between `api/` and `agents/` is possible from day one rather than retrofitted later. Capture the response header value for logging alongside the mapped result/exception.
  - `agentclient/AgentClientException.java` (new — wraps connection failures, non-2xx responses, and malformed-output errors surfaced by `agents/`)
  - `application.yml` / `application-local.yml` — add `app.agents.base-url` and reuse `app.internal.secret` for the outgoing header
- **Interfaces / contracts:** `AssessmentAgentClient.generate(AssessmentCommand command) -> AssessmentAgentResponse` where `AssessmentAgentResponse` mirrors `agents/`'s `AssessmentExecutionOutcome` (`result`, `log` — `log`'s full 13-field shape per above, not a subset).
- **Risk:** M — this is the first cross-service HTTP call in the project; network/timeout/auth failures must not leak as raw 500s to the teacher. Mitigated by `AgentClientException` with a reason code, mapped to a clean error in task-07/08. **Note:** `agents/`'s `MALFORMED_OUTPUT` reason code currently covers both "the model produced genuinely malformed output" and "the underlying LLM provider call itself failed" (e.g. a live 429 rate-limit) — `agents/` has no separate reason code for the latter yet. Do not build retry/backoff logic in `api/` that assumes `AGENT_REJECTED`/`errorCode=MALFORMED_OUTPUT` means one specific thing; it currently means either.
- **Design notes:** set an explicit connect/read timeout on the `RestClient` (Gemini calls can be slow) — do not use the default (potentially unbounded) timeout.

---

## Implementation Steps

1. Create the `agentclient` package under `cl.gradeops.ai.api.agentclient`.
2. Create `AssessmentCommand.java` and `AssessmentAgentResponse.java` — mirror `agents/`'s contract field-for-field (cross-check against `agents/.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-01-contracts.md`).
3. Create `AgentClientConfig.java` — `RestClient` bean configured with base URL (`${app.agents.base-url}`), explicit timeouts, and a default `X-Internal-Key` header sourced from `${app.internal.secret}`.
4. Create `AgentClientException.java` (reason codes: `UNREACHABLE`, `AGENT_REJECTED` for 4xx from `agents/`, `AGENT_ERROR` for 5xx).
5. Create `AssessmentAgentClient.java` — `generate(AssessmentCommand)` posts to `/internal/agents/assessment`, generating a fresh `X-Correlation-Id` per call and attaching it as a request header, maps 2xx to `AssessmentAgentResponse` (capturing the response's `X-Correlation-Id` header alongside it), maps non-2xx/timeouts to `AgentClientException`.
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
- [ ] `AssessmentCommand`/`AssessmentAgentResponse` field names verified against `agents/`'s contract — including the full 13-field `AgentExecutionLogPayload` shape (`status` and `errorCode` as separate fields), not a subset.
- [ ] Non-2xx and connection failures are mapped to `AgentClientException`, never propagate as raw exceptions.
- [ ] Shared-secret header sent on every call; timeout explicitly configured (not default/unbounded).
- [ ] `X-Correlation-Id` is generated and sent on every outbound call to `agents/`; the response's correlation ID is captured for logging.
- [ ] Real-OIDC-token gap recorded as a residual (not silently dropped).
- [ ] `./mvnw test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)
