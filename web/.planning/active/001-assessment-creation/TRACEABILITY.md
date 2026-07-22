# 🔗 Traceability: 001-assessment-creation

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

<!-- AREAS-REF: populated by plan-init from the project's configured areas — keep in sync with GUIDE.md -->
| Code | Area |
|------|------|
| WB | Web App (`src/`) |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

<!-- MATRIX-HEADER: plan-init adds one column per area between "Term / Concept" and "Notes" -->
| Term / Concept | WB | W | Notes |
|---------------|----|---|-------|
| Intake screen | ✅ | | `/assessments/new` — brief form entry point that kicks off draft generation (US-010). It must be reachable from the `/dashboard` "Nueva evaluacion" action; direct URL access alone is not sufficient. Introduced by story-02 task breakdown. |
| Dashboard new-assessment action | ⚠️ | | `/dashboard` visible action that must navigate to `/assessments/new`; currently tracked as a required R01/UI reachability gate. |
| UI Design/Data Semantics | ⚠️ | | Required gate for all assessment-creation UI: DS-first design plus field matrix before wireframe/mockup; prevents treating restricted/master data as plain text inputs. |
| Intake field matrix | ⚠️ | | Story 02 artifact classifying `learningGoal`, `topic`, `level`, `duration`, `language` by data class, source of truth, restrictions, cardinality and DS control. |
| Master data catalog | ⚠️ | | Potential API/DB-backed values for subject/asignatura, topic/tema, course/curso, language, difficulty/level and workflow status. Missing catalog must become API/DB task or explicit residual, not a hidden free-text field. |
| API I/O contract | ⚠️ | | Screen-level mapping from every read/write datum to `api/`: read model, catalog/default/capability, mutation, operation state, errors and result shape. Missing API support blocks UI Done or becomes explicit residual. |
| Sync/async completion model | ⚠️ | | Per-action decision for sync response vs async operation. Async must define polling/SSE/WebSocket/webhook/push completion, states, timeout, retry/cancel and idempotency before UI implementation. |
| i18n contract | ⚠️ | | User-facing copy, labels, safe errors, catalog labels and generated draft output use effective locale/`outputLocale`; source code, DTO fields, status/error codes, logs and telemetry stay in English. |
| `outputLocale` | ⚠️ | | Natural-language locale for generated assessment draft/regeneration output. Distinct from intake `language`, which means programming language/pseudocode. |
| Draft Builder screen | ✅ | | `/assessments/[id]/draft` — single screen combining draft editor, regenerate action, and version history (US-011 + US-012). Introduced by story-02 task breakdown. |
| Screen Data Facade | ✅ | | `loadAssessmentDraftBuilderPage(assessmentId)` — combines the current-draft and version-list GETs into one page view model per `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md` §7. First concrete use of this pattern in `web/`. |
| AssessmentDraftDto | ✅ | | Mirrors `api/`'s `GenerateAssessmentDraftResponse` verbatim (verified in task-01) — `draftId, title, context, instructions, objectives[], deliverables[], constraints[], versionNumber`. |

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
