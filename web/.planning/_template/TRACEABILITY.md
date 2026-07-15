# 🔗 Traceability: [Planning Name]

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

Add terms, concepts, and decisions introduced by this planning. Use one row per concept and mark each area with the cell values above.

Example row to model, not to keep: `password reset token | ⚠️ | AP owns persistence and expiry rules; WB consumes public reset flow copy`.

<!-- MATRIX-HEADER: plan-init adds one column per area between "Term / Concept" and "Notes" -->
| Term / Concept | WB | W | Notes |
|---------------|----|---|-------|
| *[term or concept, e.g. password reset token]* | | | [Which area owns or consumes this concept?] |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| PDR-001 | [e.g. Reset token responses must not reveal account existence] | [e.g. Prevents user enumeration across API and web flows] | [AP, WB, DO] | YYYY-MM-DD |
| — | *None yet* | — | — | — |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R-001 | [e.g. Email template ownership unresolved] | [e.g. Provider and brand template are not selected] | OPEN | [Future email-template planning] |
| — | *None* | — | — | — |

---

> [← planning/README.md](../../README.md)
