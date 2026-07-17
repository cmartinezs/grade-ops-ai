# 🔗 Traceability: [Planning Name]

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

<!-- AREAS-REF: populated by plan-init from the project's configured areas — keep in sync with GUIDE.md -->
| Code | Area |
|------|------|
| AG | Agent Runtime (`agents/`) |
| AP | Backend / Domain (`api/`) |
| DO | Documentation (`docs/`) |
| IN | Infrastructure (`infra/`) |
| WB | Frontend (`web/`) |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

<!-- MATRIX-HEADER: plan-init adds one column per area between "Term / Concept" and "Notes" -->
| Term / Concept | AG | AP | DO | IN | WB | W | Notes |
|---------------|----|----|----|----|----|---|-------|
| `compose.yml` `agents` service | ✅ | N/A | N/A | ✅ | N/A | N/A | Added by story-04 task-01 — was previously absent from root `compose.yml` |
| `scripts/smoke-e2e-local.sh` | N/A | N/A | N/A | ✅ | N/A | N/A | Story-04 task-02 — real brief→generate flow against the docker-compose stack; verifies persisted `AgentExecutionLog` directly in Postgres |
| `scripts/smoke-e2e-render-beta.sh` | N/A | N/A | N/A | ✅ | N/A | N/A | Story-04 task-04 — real brief→generate flow against the deployed `beta` environment on Render; warms up both `api/` and `agents/` before the real check |
| `scripts/lib/e2e-smoke-flow.sh` | N/A | N/A | N/A | ✅ | N/A | N/A | Story-04 task-04 — shared brief→generate→retrieve flow sourced by both `smoke-e2e-local.sh` and `smoke-e2e-render-beta.sh`, avoiding duplication between the local and deployed smoke scripts |
| Render CLI (`render-oss/cli`, `RENDER_API_KEY`) | N/A | N/A | N/A | ✅ | N/A | N/A | Official Render CLI, used by story-04 tasks 03/04 for non-interactive deploy/service status checks |
| `grade-ops-ai-api` / `grade-ops-ai-agents` (Render service names, `beta`) | ✅ | ✅ | ✅ | ✅ | N/A | N/A | Story-04 task-03 — Render services renamed to match `docs/04-architecture/beta-environment-design.md`; both now track `develop` (previously `grade-ops-agents` tracked stale `master`, ~1 month behind) |
| `.env.example` Render beta smoke section (`RENDER_API_KEY`, `RENDER_WORKSPACE_ID`, `BETA_API_BASE_URL`) | N/A | N/A | N/A | ✅ | N/A | N/A | Story-04 task-04 — documents the vars `scripts/smoke-e2e-render-beta.sh` requires, added after a code-review P2 finding |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D-01 | Factor the brief→generate→retrieve smoke flow into a shared `scripts/lib/e2e-smoke-flow.sh` sourced by both the local and Render smoke scripts, instead of duplicating it | Avoids two copies of the same provisioning/auth/generation logic drifting apart across local and deployed environments | `scripts/smoke-e2e-local.sh`, `scripts/smoke-e2e-render-beta.sh` | 2026-07-16 |
| D-02 | Both `api/` and `agents/` must be warmed up directly before a Render beta smoke run, not just `api/` | A real run found that warming only `api/` isn't enough — a cold `agents/` returns a `429` from Render's edge to the internal `api/`→`agents/` call, distinct from a clean timeout | `scripts/smoke-e2e-render-beta.sh` | 2026-07-16 |
| D-03 | Render's `*.onrender.com` hostname is a separate slug from the service's display name and does not change when the display name is renamed | Discovered while renaming `gradeops-api`/`gradeops-agents` to match the design doc — `BETA_API_BASE_URL` still needed the original hostname | `.env.example`, `scripts/smoke-e2e-render-beta.sh` | 2026-07-16 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R-01 | `docs/04-architecture/beta-environment-design.md` still documents stale `AI_MODEL_NAME`/`GOOGLE_AI_API_KEY` env var names instead of the current code's `GRADEOPS_GEMINI_API_KEY`/`GRADEOPS_GEMINI_MODEL`/`GRADEOPS_GROQ_API_KEY`/`GRADEOPS_GROQ_MODEL`; also worth revisiting whether `agents/`'s `default-provider` should be `gemini` for beta per the design doc's stated intent | None — doc-only fix | OPEN | A future doc-maintenance task or the next planning touching `agents/`'s provider config |

---

> [← planning/README.md](../../README.md)
