# 🔍 DEEPENING: Scope 04 — Sign-Out and Session Expiry

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

As a teacher, I want to sign out and have idle sessions expire so my workspace stays secure on shared machines.

> Source: `docs/02-product/user-stories/epic-01-teacher-onboarding/04-sign-out-session-expiry.md` — US-004

## Area

WB (`web/`), AP (`api/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [SignOutButton + POST /auth/sign-out](scope-04-sign-out-session-expiry/task-01-sign-out-action.md) | GENERATE-DOCUMENT | DONE | `SignOutButton.tsx`, endpoint `POST /auth/sign-out` |
| 2 | [API client con interceptor 401](scope-04-sign-out-session-expiry/task-02-auth-error-interceptor.md) | GENERATE-DOCUMENT | DONE | `web/src/lib/api/client.ts` |

---

## Done Criteria

- [x] Teacher can sign out from any workspace screen.
- [x] After sign-out, protected pages require signing in again.
- [x] An expired session redirects to sign-in with a clear message.
- [x] Session lifetime is a deployment-level configuration, not hardcoded.
- [x] (DoD) Sign-out action available from any workspace screen in `web/` (Firebase `signOut()`), clearing the client session.
- [x] (DoD) Invalid/expired token gets `401` from `api/`; `web/` redirects to sign-in with a clear message (centralized handling).
- [x] (DoD) Session lifetime configurable at deployment level (Firebase session settings / env config).
- [x] (DoD) Sign-out optionally revokes refresh tokens server-side (Admin SDK `revokeRefreshTokens`) — decision documented either way.
- [x] (DoD) Tests cover: blocked route after sign-out, expired/revoked token rejected, redirect-with-message flow.
- [x] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- With Firebase, ID tokens last ~1 h and the SDK auto-refreshes — real expiry requires choosing: (a) Firebase session cookies managed by `api/` with configurable `maxAge`, or (b) refresh-token revocation + inactivity check. For MVP, (b) is simpler; document the choice.
- Enforcement is server-side: `api/` validates the token on every request; the UI only reflects state.
- The expiry message must not silently lose work (half-filled forms).

## Dependencies

| Depends on | Reason |
|------------|--------|
| scope-01 (teacher-login) | Sign-out and expiry act on the session created at sign-in |

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)
