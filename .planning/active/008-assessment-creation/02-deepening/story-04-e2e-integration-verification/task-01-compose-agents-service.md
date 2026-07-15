# ⚛️ TASK 01 — Add `agents` service to root `compose.yml`

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-04-e2e-integration-verification.md)

---

## Objective

Root `compose.yml` brings up an `agents` service alongside `db`, `api`, `web`, so the full stack (including the Groq/Gemini-calling service) can run together locally with a single `docker compose up`.

---

## Technical Design

- **Approach:** Mirror the existing `api` service's shape (`build: ./agents`, environment block, `ports`, `depends_on`) — `agents` needs no database dependency of its own (it's stateless, persists nothing per `CLAUDE.md`'s "Agents do not own domain entities" rule), so its only `depends_on` is being reachable before `api` needs it; Compose's default startup ordering (not health-gated) is acceptable since `agents/` starting slightly after `api/` doesn't fail `api/`'s own boot — only a live agent call would need it up. `api`'s existing `AGENTS_BASE_URL` env var (already wired app-side per `api/src/main/resources/application.yml:31`, default `http://localhost:8081`) needs to be overridden in compose to the Docker-network service name: `http://agents:8081`. `INTERNAL_API_SECRET` must be the *same* value on both `api` and `agents` containers — it's the shared-secret header `agentclient` sends and `agents/` verifies (per `api/`'s task-05 Technical Design).
- **Affected files / components:** `compose.yml` (root) — new `agents` service block; `api` service's `environment` block gains `AGENTS_BASE_URL` and (if not already present) `INTERNAL_API_SECRET` pointing at the same value as the new `agents` service.
- **Interfaces / contracts:** None — pure local dev config.
- **Risk:** Low — additive-only, no existing service's behavior changes beyond `api` gaining two env vars it already reads with safe defaults.
- **Design notes:** `agents/`'s own env vars (`GRADEOPS_GROQ_API_KEY`, `GRADEOPS_GROQ_MODEL`, `GRADEOPS_GROQ_BASE_URL`, per `agents/.env.example`) must come from a local `.env` file (not hardcoded in `compose.yml`), consistent with `agents/`'s own `DotenvEnvironmentPostProcessor` local-dev convention and this project's "never commit real values" rule — reference them with `${VAR_NAME:?Set VAR_NAME in .env}` the same way `compose.yml`'s `web` service already does for Firebase vars.

---

## Implementation Steps

1. In `compose.yml`, add an `agents` service: `build: ./agents`, `environment` block with `SPRING_PROFILES_ACTIVE: local` (or the profile `agents/` uses locally — verify against `agents/src/main/resources/` before finalizing), `GRADEOPS_GROQ_API_KEY: ${GRADEOPS_GROQ_API_KEY:?Set GRADEOPS_GROQ_API_KEY in .env}`, `GRADEOPS_GROQ_MODEL: ${GRADEOPS_GROQ_MODEL:-llama-3.3-70b-versatile}`, `INTERNAL_API_SECRET: ${INTERNAL_API_SECRET:-dev-secret-change-me}` (same default as `api`'s existing entry), `ports: ["8081:8081"]`.
2. Update the `api` service's `environment` block: add `AGENTS_BASE_URL: http://agents:8081`, confirm `INTERNAL_API_SECRET` uses the identical `${INTERNAL_API_SECRET:-dev-secret-change-me}` default already present.
3. Add `depends_on: [agents]` (or leave unordered if `api`'s own boot doesn't require `agents` to be up first — verify by checking whether any `api` startup health check calls `agents/`; if not, ordering is cosmetic only and can be omitted).

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `compose.yml` is syntactically valid | `docker compose config` (no `--build`) parses without error |
| 2 | `agents` service starts | `docker compose up agents` (with a populated `.env`) — container starts, logs show Spring Boot boot success on port 8081 |
| 3 | `api` resolves `AGENTS_BASE_URL` to the compose network | `docker compose up api agents db` then `docker compose exec api sh -c 'echo $AGENTS_BASE_URL'` → `http://agents:8081` |
| 4 | Shared secret matches on both sides | `docker compose exec api sh -c 'echo $INTERNAL_API_SECRET'` and `docker compose exec agents sh -c 'echo $INTERNAL_API_SECRET'` → identical value |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up -d db` — Postgres healthy per its existing healthcheck |
| 2 | App compiles and starts | `docker compose up -d agents` — container reaches a running state, no crash loop |
| 3 | Connectivity or schema validation succeeds | N/A — `agents/` has no database |
| 4 | Changed surface responds correctly | `docker compose up -d api agents db` then `docker compose exec api curl -sf http://agents:8081/actuator/health` (or equivalent health endpoint) returns success from inside the `api` container's network namespace |
| 5 | No startup regressions are visible | `docker compose logs api` shows no `AGENTS_BASE_URL`/`INTERNAL_API_SECRET`-related startup errors; existing `db`/`web` services still start as before |

### Database / ORM Consistency Check

N/A — no database or ORM artifacts involved.

---

## Done Criteria

- [ ] `compose.yml` has a working `agents` service reachable at `http://agents:8081` from within the compose network.
- [ ] `api`'s `AGENTS_BASE_URL` and `INTERNAL_API_SECRET` env vars are correctly wired to the new `agents` service.
- [ ] `docker compose up` brings up `db`, `api`, `agents`, `web` together with no manual steps beyond populating local secrets (`.env` for `agents/`, `FIREBASE_ADMIN_KEY_PATH` for `api`, as already required today).
- [ ] All verification checks listed above pass.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — no smoke-test script, no real agent call proven yet; that's task-02.

---

> [← story file](../story-04-e2e-integration-verification.md)
