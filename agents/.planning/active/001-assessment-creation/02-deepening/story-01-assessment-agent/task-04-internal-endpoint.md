# ⚛️ TASK 04 — Internal REST endpoint

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-03
> [← story file](../story-01-assessment-agent.md)

---

## Objective

Expose `GenerateAssessmentDraftUseCase` (task-03) via an internal-only REST endpoint that `api/`'s `agentclient` module calls, with the cross-cutting concerns `12-excepciones-y-manejo-de-errores.md` and `11-seguridad-observabilidad-y-auditoria.md` require: structured error responses, an internal-auth filter, and correlation-ID propagation.

---

## Technical Design

- **Approach:** reuse the shared-secret internal-auth mechanism already scaffolded in `application.yml` (`app.internal.secret` / `INTERNAL_API_SECRET` env var) as an app-level **defense-in-depth** check — this matches the existing config property and the analogous internal-key pattern already used elsewhere in the monorepo (`api/` has its own `X-Internal-Key`-style internal auth). The **primary** protection is at the infra layer: `infra/terraform/environments/demo/cloud_run.tf` sets `agents/`'s Cloud Run service to `INGRESS_TRAFFIC_INTERNAL_ONLY` and grants `roles/run.invoker` only to `api/`'s service account — this **is** real Cloud Run OIDC service-to-service auth, enforced by the platform before a request reaches this app, matching `CLAUDE.md`'s "service-to-service OIDC auth" description exactly (see `TRACEABILITY.md` D-02). This task only needs the app-level shared-secret check; no OIDC-token validation code is needed in the Spring Boot app itself.
- **Layering:** the internal-auth filter, correlation-ID filter, and the global exception handler are **not** assessment-specific — they apply to every current and future `agents/` endpoint, so they live under a `shared` package (mirrors `api/`'s own `cl.gradeops.ai.api.shared.*` convention, not a speculative abstraction: a request filter and an exception handler are inherently app-wide in Spring MVC, not feature-scoped). `AssessmentController` itself stays in the `assessment` feature package.
- **Affected files / components:**
  - `assessment/infrastructure/adapter/in/web/AssessmentController.java` (new)
  - `assessment/infrastructure/adapter/in/web/response/AssessmentExecutionResponse.java` (new — response DTO wrapping `AssessmentExecutionOutcome` for JSON; keeps Jackson annotations out of the application-layer `AssessmentExecutionOutcome` per `03-use-cases-orquestadores-y-pasos.md`'s "Evitar acoplar contratos públicos a Spring, JPA, Jackson" rule)
  - `shared/infrastructure/adapter/in/web/InternalAuthFilter.java` (new)
  - `shared/infrastructure/adapter/in/web/CorrelationIdFilter.java` (new — `11-seguridad-observabilidad-y-auditoria.md`'s "Todo request debe tener correlation ID" rule was not covered by any existing task)
  - `shared/infrastructure/adapter/in/web/AgentGlobalExceptionHandler.java` (new — this artifact's equivalent of `api/`'s `GlobalExceptionHandler`)
  - `shared/infrastructure/adapter/in/web/response/AgentErrorResponse.java` (new)
  - `shared/infrastructure/config/SharedWebConfig.java` (new — registers both filters; `AssessmentConfig` from task-03 stays feature-scoped and does not register these)
  - all under `cl.gradeops.ai.agents.*`
- **Interfaces / contracts:**
  - `POST /internal/agents/assessment` — request body maps to `AssessmentCommand` (JSON), response body maps to `AssessmentExecutionResponse` (JSON: `result`, `log`).
  - `AssessmentController` depends on `GenerateAssessmentDraftUseCase` (task-03's port in), never on the orchestrator or adapter directly.
  - Required header carrying the shared secret — confirm the exact header name against `api/`'s existing internal-auth convention before finalizing (see Implementation Steps), so both sides agree.
  - `AgentErrorResponse(String errorCode, String message, AgentExecutionLogPayload log, String correlationId)` — on a rejected/failed request the `log` field still carries `AssessmentAgentException`'s partial `AgentExecutionLogPayload` (`FAILED`, `errorCode` set), so `api/` gets execution evidence even when generation fails, per `00-principios-rectores.md` #8 ("cada cambio debe ser trazable" — evidence is not a happy-path-only side effect).
  - Correlation ID: read `X-Correlation-Id` if present and valid, else generate one; put it in MDC for the duration of the request (`try { MDC.put(...); chain.doFilter(...); } finally { MDC.clear(); }` per `11-seguridad-observabilidad-y-auditoria.md`'s exact pattern); always return it in the response header and in both `AssessmentExecutionResponse`... no — only in the response **header**, not the body (the body contract is defined by task-03/01's records; don't grow them for a transport-level concern). Include it in `AgentErrorResponse.correlationId` since error responses are exactly where a caller needs it to cross-reference logs.
- **Risk:** M — if the internal-auth check is missing or misconfigured, the endpoint is accidentally public; mitigated by an explicit rejection test for missing/wrong header. Secondary risk: forgetting to clear MDC on every exit path (including filter exceptions) — mitigated by the mandatory `try/finally` pattern from `11-seguridad-observabilidad-y-auditoria.md`.
- **Design notes:**
  - Return 401/403 (not a generic 500) on missing/invalid internal key; return 422 with the `AssessmentAgentException` reason code on invalid command or malformed output, never a raw 500 stack trace (`11-seguridad-observabilidad-y-auditoria.md` "Manejo de errores": never expose stack traces).
  - Never log the raw request body, prompt, or Gemini output in the filter or the exception handler — only `AgentExecutionLogPayload`'s hashes (`11-seguridad-observabilidad-y-auditoria.md` "Logging" prohibited-examples list).
  - `AssessmentController` is the only class in this task allowed the `@RestController` stereotype; everything else (`InternalAuthFilter`, `CorrelationIdFilter`, `AgentGlobalExceptionHandler` as `@RestControllerAdvice`) is either plain `Filter` implementations registered via `SharedWebConfig`, or the one documented exception to the stereotype rule (`@RestControllerAdvice`, matching `12-excepciones-y-manejo-de-errores.md`'s own `GlobalExceptionHandler` example).

---

## Implementation Steps

1. Confirm the exact internal-auth header name and validation approach already used by `api/` (inspect `api/src/main/java/.../SecurityConfig.java` or equivalent in this worktree's `api/` checkout) so `agents/` matches it exactly. If it differs from what was assumed in earlier tasks, record it via `RECORD-INCONSISTENCY` in the story file.
2. Create `shared/infrastructure/adapter/in/web/InternalAuthFilter.java` validating the confirmed header against `app.internal.secret`; register it in `SharedWebConfig` for `/internal/**` paths only.
3. Create `shared/infrastructure/adapter/in/web/CorrelationIdFilter.java` per the MDC/correlation-ID rules above; register it in `SharedWebConfig` for all paths, ordered before `InternalAuthFilter`.
4. Create `assessment/infrastructure/adapter/in/web/response/AssessmentExecutionResponse.java` and `shared/infrastructure/adapter/in/web/response/AgentErrorResponse.java`.
5. Create `assessment/infrastructure/adapter/in/web/AssessmentController.java` with `POST /internal/agents/assessment`, delegating to `GenerateAssessmentDraftUseCase.execute(...)`, mapping the result to `AssessmentExecutionResponse`.
6. Create `shared/infrastructure/adapter/in/web/AgentGlobalExceptionHandler.java` (`@RestControllerAdvice`) mapping `AssessmentAgentException` (both reason codes → 422) to `AgentErrorResponse`, including its attached `AgentExecutionLogPayload` and the current correlation ID.
7. Create `shared/infrastructure/config/SharedWebConfig.java` registering both filters (`FilterRegistrationBean`, explicit `order`).

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Valid request with correct internal key returns 200 + `AssessmentExecutionResponse` JSON | `MockMvc`/`@WebMvcTest` posting a valid command with the correct header |
| 2 | Missing/incorrect internal key is rejected | Same test harness, omit or corrupt the header, assert 401/403 |
| 3 | Invalid command returns 422 with a reason code and a `log` payload, not 500 | Test posting a command with a blank required field |
| 4 | Correlation ID is honored, generated, and returned | Test with and without `X-Correlation-Id` on the request; assert the response header and that it appears in the `AgentErrorResponse` on a failing request |
| 5 | MDC is cleared after every request, success or failure | Test asserting `MDC.get("correlationId")` is `null` after the filter chain completes, for both a 200 and a 422 response |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | None beyond the app itself |
| 2 | App compiles and starts | `./mvnw -Pbeta spring-boot:run` starts without errors |
| 3 | Connectivity or schema validation succeeds | N/A — no DB |
| 4 | Changed surface responds correctly | `curl -X POST localhost:8081/internal/agents/assessment -H "X-Internal-Key: $INTERNAL_API_SECRET" -H "Content-Type: application/json" -d '{...valid command...}'` returns 200 with a populated result (adjust header name per step 1 findings) |
| 5 | No startup regressions are visible | `./mvnw -Pbeta test` passes |

### Database / ORM Consistency Check

N/A — no database or ORM involved.

---

## Done Criteria

- [ ] `POST /internal/agents/assessment` exists and delegates to `GenerateAssessmentDraftUseCase`.
- [ ] Requests without the correct internal-auth header are rejected (401/403), never reach `GenerateAssessmentDraftUseCase`.
- [ ] Invalid command / malformed output return 422 with a reason code and an `AgentExecutionLogPayload`, never a raw 500.
- [ ] Every response (success and error) carries a correlation ID, honoring an inbound `X-Correlation-Id` and generating one otherwise; MDC is cleared on every exit path.
- [ ] Header-name confirmation against `api/`'s existing convention is done; any mismatch found is recorded via `RECORD-INCONSISTENCY`.
- [ ] No raw prompt, request body, or Gemini output appears in any log line produced by this task's code.
- [ ] `./mvnw -Pbeta test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)
