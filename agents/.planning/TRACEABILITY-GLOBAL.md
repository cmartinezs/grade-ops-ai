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
| `AG` | Agent Runtime (`src/`) |
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
| Term / Concept | Source Planning | AG | W |
|---------------|----------------|----|---|
| Planning System | framework bootstrap | N/A | ✅ |
| Workflow (meta) | framework bootstrap | N/A | ✅ |
| Fundamental Rule | framework bootstrap | N/A | ✅ |

---

## Consolidated Residuals

*Terms or decisions deferred from individual plannings that require global resolution.*

| ID | Term / Issue | Source Planning | Status | Notes |
|----|-------------|----------------|--------|-------|
| GR-01 | No per-provider model/capability registry or usage-limits (RPS/RPD/token ceilings) registry exists in `agents/`; `api/` and `agents/` currently discover limits only from a failed live call. A dedicated `AssessmentAgentException.Reason` for provider quota/rate-limit rejection (distinct from `MALFORMED_OUTPUT`) belongs with this work. | 002-groq-genai-provider | OPEN | Candidate trigger: once a second `agents/` agent exists so "capability" has more than one real value to register against |
| GR-02 | `AssessmentCommand.model`, once forwarded to a provider, is not validated against a list of models the resolved provider actually supports | 002-groq-genai-provider | OPEN | Natural extension of GR-01's registry, not a separate concern |
| GR-03 | `AssessmentGenerationPortSelector` only treats a literal `null` `provider` as "use the configured default"; a blank string (`""`) is rejected as an unrecognized provider. Web/DTO clients commonly send `""` for "not specified." | 002-groq-genai-provider | OPEN | Undecided whether normalization belongs at the API/DTO boundary (`api/`'s `agentclient`) or inside the selector itself |

---

## Changelog

| Date | Planning | Change |
|------|----------|--------|
| 2026-07-12 | 002-groq-genai-provider | Planning completed. Registered GR-01/GR-02/GR-03 consolidated residuals (model/capability registry, model validation, blank-provider normalization). Story-02 (infra provisioning) relocated to parent-owned planning `009-groq-infra-provisioning`, not tracked here. |
| 2026-07-09 | — | Matrix initialized. Area codes configured by plan-init based on project structure. |

---

> [← planning/README.md](README.md)
