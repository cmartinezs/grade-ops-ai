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
| `GoogleSignInButton` | 002-google-sign-in | N/A | N/A | N/A | N/A | ✅ | N/A |
| `GoogleAuthProvider` / `signInWithPopup` | 002-google-sign-in | N/A | N/A | N/A | N/A | ✅ | N/A |
| `google_identity_platform_default_supported_idp_config` | 002-google-sign-in | N/A | N/A | N/A | ✅ | N/A | N/A |
| `provider` column in `teachers` (`EMAIL_PASSWORD` / `GOOGLE`) | 002-google-sign-in | N/A | ✅ | N/A | N/A | N/A | N/A |
| `RegisterResult` (CREATED vs FOUND upsert) | 002-google-sign-in | N/A | ✅ | N/A | N/A | N/A | N/A |
| AuthGuard bypass for `providerId === 'google.com'` | 002-google-sign-in | N/A | N/A | N/A | N/A | ✅ | N/A |
| `GRADEOPS_GROQ_API_KEY` (Secret Manager secret + Cloud Run secret-bound env var) | 009-groq-infra-provisioning | ✅ | N/A | N/A | ✅ | N/A | N/A |
| `GRADEOPS_GROQ_MODEL` (Cloud Run plain env var) | 009-groq-infra-provisioning | ✅ | N/A | N/A | ✅ | N/A | N/A |
| `GRADEOPS_GROQ_BASE_URL` | 009-groq-infra-provisioning | ✅ | N/A | N/A | N/A | N/A | N/A |

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
| 2026-06-15 | 002-google-sign-in | 6 terms added: GoogleSignInButton, GoogleAuthProvider, Terraform IDP config, provider column, RegisterResult, AuthGuard bypass. |
| 2026-07-14 | 009-groq-infra-provisioning | 3 terms added: `GRADEOPS_GROQ_API_KEY`, `GRADEOPS_GROQ_MODEL`, `GRADEOPS_GROQ_BASE_URL` (Groq API key Secret Manager entry + Cloud Run env binding for `agents/`). |

---

> [← planning/README.md](README.md)
