# ⚛️ TASK 04 — Internal REST endpoint

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-03
> [← story file](../story-01-assessment-agent.md)

---

## Objective

Expose `AssessmentAgentService` via an internal-only REST endpoint that `api/`'s `agentclient` module calls.

---

## Technical Design

- **Approach:** reuse the shared-secret internal-auth mechanism already scaffolded in `application.yml` (`app.internal.secret` / `INTERNAL_API_SECRET` env var) rather than introducing a new auth mechanism — this matches the existing config property and the analogous internal-key pattern already used elsewhere in the monorepo (`api/` has its own `X-Internal-Key`-style internal auth, per prior planning history). **Note:** `CLAUDE.md`'s architecture section describes agent endpoints as "service-to-service OIDC auth" — the actual scaffold uses a shared secret, not OIDC. Flag this as a documentation inconsistency via `RECORD-INCONSISTENCY` rather than silently building unplanned OIDC infrastructure in this task.
- **Affected files / components:** `AssessmentController.java` (new), an internal-auth filter/interceptor (new — none exists yet in `src/main/java`).
- **Interfaces / contracts:** `POST /internal/agents/assessment` — request body maps to `AssessmentCommand` (JSON), response body maps to `AssessmentExecutionOutcome` (JSON: `result`, `log`). Required header carrying the shared secret — confirm the exact header name against `api/`'s existing internal-auth convention before finalizing (see step 1), so both sides agree.
- **Risk:** M — if the internal-auth check is missing or misconfigured, the endpoint is accidentally public; mitigated by an explicit rejection test for missing/wrong header.
- **Design notes:** return 401/403 (not a generic 500) on missing/invalid internal key; return 422 with the `AssessmentAgentException` reason code on invalid command or malformed output, never a raw 500 stack trace.

---

## Implementation Steps

1. Confirm the exact internal-auth header name and validation approach already used by `api/` (inspect `api/src/main/java/.../SecurityConfig.java` or equivalent in this worktree's `api/` checkout) so `agents/` matches it exactly. If it differs from what was assumed in earlier tasks, record it via `RECORD-INCONSISTENCY` in the story file.
2. Create the internal-auth filter/interceptor validating the confirmed header against `app.internal.secret`; register it for `/internal/**` paths only.
3. Create `AssessmentController.java` with `POST /internal/agents/assessment`, delegating to `AssessmentAgentService.generate(...)`.
4. Add a `@ControllerAdvice`/`@ExceptionHandler` mapping `AssessmentAgentException` reason codes to HTTP status (422 for both `INVALID_COMMAND` and `MALFORMED_OUTPUT`).

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Valid request with correct internal key returns 200 + `AssessmentExecutionOutcome` JSON | `MockMvc`/`@WebMvcTest` posting a valid command with the correct header |
| 2 | Missing/incorrect internal key is rejected | Same test harness, omit or corrupt the header, assert 401/403 |
| 3 | Invalid command returns 422 with a reason code, not 500 | Test posting a command with a blank required field |

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

- [ ] `POST /internal/agents/assessment` exists and delegates to `AssessmentAgentService`.
- [ ] Requests without the correct internal-auth header are rejected (401/403), never reach `AssessmentAgentService`.
- [ ] Invalid command / malformed output return 422 with a reason code, never a raw 500.
- [ ] Header-name confirmation against `api/`'s existing convention is done; any mismatch found is recorded via `RECORD-INCONSISTENCY`.
- [ ] `./mvnw -Pbeta test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)
