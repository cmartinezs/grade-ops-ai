# 🌐 Global Traceability Matrix

> [← planning/README.md](README.md)

Consolidated view of all terms and decisions mapped across the project's repository areas, drawn from all plannings.

> **Status:** Draft

---

## How to Read This Matrix

Each row is a term, concept, or decision introduced in a planning. The columns represent the project's repository areas, configured during `/plan-init`.

<!-- AREAS-REF: populated by plan-init — keep in sync with GUIDE.md AREAS-TABLE -->
| Code | Area |
|------|------|
| `AP` | `src/` — Java / Spring Boot 4 API |
| `DO` | `docs/` — documentación |
| `W` | Planning System (`.planning/`) |

Cell values:
- `✅` — term/concept explicitly present and consistent
- `⚠️` — present but needs review or update
- `❌` — not yet present (gap)
- `N/A` — area not applicable for this term
- `(blank)` — not yet evaluated

---

## Global Matrix

<!-- MATRIX-HEADER: plan-init adds area columns after "Source Planning" -->
| Term / Concept | Source Planning | AP | DO | W |
|---------------|----------------|----|----|---|
| Planning System | framework bootstrap | N/A | N/A | ✅ |
| Workflow (meta) | framework bootstrap | N/A | N/A | ✅ |
| Fundamental Rule | framework bootstrap | N/A | N/A | ✅ |

---

## Consolidated Residuals

*Terms or decisions deferred from individual plannings that require global resolution.*

| ID | Term / Issue | Source Planning | Status | Notes |
|----|-------------|----------------|--------|-------|
| — | *No open residuals* | — | — | — |

---

## Changelog

| Date | Planning | Change |
|------|----------|--------|
| 2026-06-11 | — | Matrix initialized. Area codes configured by plan-init based on project structure. |

---

> [← planning/README.md](README.md)
