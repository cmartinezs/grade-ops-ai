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
| `AG` | Agent Runtime (`agents/`) |
| `AP` | Backend / Domain (`api/`) |
| `DO` | Documentation (`docs/`) |
| `IN` | Infrastructure (`infra/`) |
| `WB` | Frontend (`web/`) |
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
| Term / Concept | Source Planning | AG | AP | DO | IN | WB | W |
|---------------|----------------|----|----|----|----|----|---|
| Planning System | framework bootstrap | N/A | N/A | N/A | N/A | N/A | ✅ |
| Workflow (meta) | framework bootstrap | N/A | N/A | N/A | N/A | N/A | ✅ |
| Fundamental Rule | framework bootstrap | N/A | N/A | N/A | N/A | N/A | ✅ |
| Firebase Authentication | 001-teacher-onboarding | N/A | ✅ | ✅ | ✅ | ✅ | N/A |
| Firebase Admin SDK | 001-teacher-onboarding | N/A | ✅ | ✅ | N/A | N/A | N/A |
| `verifyIdToken(token, checkRevoked: true)` | 001-teacher-onboarding | N/A | ✅ | ✅ | N/A | N/A | N/A |
| `X-Internal-Key` | 001-teacher-onboarding | N/A | ✅ | ✅ | ✅ | N/A | N/A |
| `/internal/teachers` | 001-teacher-onboarding | N/A | ✅ | ✅ | N/A | N/A | N/A |
| `emailVerified: true` (provisioned accounts) | 001-teacher-onboarding | N/A | ✅ | ✅ | N/A | N/A | N/A |
| `revokeRefreshTokens` | 001-teacher-onboarding | N/A | ✅ | ✅ | N/A | N/A | N/A |
| `AuthenticatedTeacher` principal | 001-teacher-onboarding | N/A | ✅ | N/A | N/A | N/A | N/A |
| `OwnershipVerifier` (404 cross-teacher denial) | 001-teacher-onboarding | N/A | ✅ | N/A | N/A | N/A | N/A |
| `AssessmentSummaryDto` | 001-teacher-onboarding | N/A | ✅ | N/A | N/A | ✅ | N/A |
| `apiClient` (web fetch interceptor) | 001-teacher-onboarding | N/A | N/A | N/A | N/A | ✅ | N/A |
| `plan_type` / `related_party` pilot flags | 001-teacher-onboarding | N/A | ✅ | N/A | N/A | N/A | N/A |

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
| 2026-06-13 | 001-teacher-onboarding | 12 terms added from teacher onboarding epic (Firebase auth, Admin SDK, internal key, pilot flags, apiClient, OwnershipVerifier). |

---

> [← planning/README.md](README.md)
