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
| Firebase Authentication | N/A | ✅ | ✅ | ✅ | ✅ | N/A | Identity provider for teacher accounts; ID token validation via Admin SDK |
| Firebase Admin SDK | N/A | ✅ | ✅ | N/A | N/A | N/A | Server-side SDK used in `api/` for `verifyIdToken`, `createUser`, `revokeRefreshTokens` |
| `verifyIdToken(token, checkRevoked: true)` | N/A | ✅ | ✅ | N/A | N/A | N/A | Token validation call in `api/`; rejects revoked tokens within ~1 min of revocation |
| `X-Internal-Key` | N/A | ✅ | ✅ | ✅ | N/A | N/A | Shared-secret header securing `/internal/**` operator endpoints |
| `/internal/teachers` | N/A | ✅ | ✅ | N/A | N/A | N/A | Operator endpoint for provisioning teacher accounts |
| `emailVerified: true` | N/A | ✅ | ✅ | N/A | N/A | N/A | Flag set by Admin SDK for operator-provisioned accounts to bypass email verification |
| `revokeRefreshTokens` | N/A | ✅ | ✅ | N/A | N/A | N/A | Admin SDK call that invalidates all refresh tokens for a UID on sign-out |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D-01 | Firebase Authentication as identity provider; teacher-only; API validates ID tokens, never stores credentials | GCP-native, no credential storage, Admin SDK covers all required operations | AP, DO, WB, IN | 2026-06-12 |
| D-02 | Operator access via `/internal/**` with `X-Internal-Key` header (Secret Manager) | Simpler than IAM service-to-service; preserves audit log continuity | AP, DO, IN | 2026-06-12 |
| D-03 | Operator-provisioned accounts created with `emailVerified: true`; self-registered accounts must verify | Pilots assumed reachable; avoids friction without security cost | AP, DO | 2026-06-12 |
| D-04 | Session expiry via refresh-token revocation (`revokeRefreshTokens`) + `checkRevoked: true` on every verify | Firebase-recommended approach; no server-side session storage needed | AP, DO, WB | 2026-06-12 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| — | *None* | — | — | — |

---

> [← planning/README.md](../../README.md)
