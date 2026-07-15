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
| `scripts/smoke-e2e-local.sh` | N/A | N/A | N/A | ✅ | N/A | N/A | Story-04 task-02 — real brief→generate flow against the docker-compose stack |
| `scripts/smoke-e2e-render-beta.sh` | N/A | N/A | N/A | ✅ | N/A | N/A | Story-04 task-04 — real brief→generate flow against the deployed `beta` environment on Render |
| Render CLI (`render-oss/cli`, `RENDER_API_KEY`) | N/A | N/A | N/A | ✅ | N/A | N/A | Official Render CLI, used by story-04 tasks 03/04 for non-interactive deploy/service status checks |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| — | *None yet* | — | — | — |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| — | *None* | — | — | — |

---

> [← planning/README.md](../../README.md)
