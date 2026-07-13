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
| `GRADEOPS_GROQ_API_KEY` (Secret Manager secret + Cloud Run secret-bound env var) | ✅ | N/A | N/A | ✅ | N/A | | Defined in `agents/002-groq-genai-provider` story-01 task-02 (`agents/src/main/resources/application-demo.yml`); provisioned as infra by this planning's story-01 task-01/task-02 |
| `GRADEOPS_GROQ_MODEL` (Cloud Run plain env var) | ✅ | N/A | N/A | ✅ | N/A | | Value `llama-3.3-70b-versatile`, matching `agents/.env.example`; wired by this planning's story-01 task-02 |
| `GRADEOPS_GROQ_BASE_URL` | ✅ | N/A | N/A | N/A | N/A | | Intentionally not set in Cloud Run — `agents/src/main/resources/application-demo.yml` already defaults it to `https://api.groq.com/openai/v1` |

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
