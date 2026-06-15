# US-004: Sign-Out and Session Expiry

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P1
- **ID:** US-004

## Story

As a teacher, I want to sign out and have idle sessions expire so my workspace stays secure on shared machines.

## Acceptance Criteria

- Teacher can sign out from any workspace screen.
- After sign-out, protected pages require signing in again.
- An expired session redirects to sign-in with a clear message.
- Session lifetime is a deployment-level configuration, not hardcoded.

---

## Definition of Done

- [ ] Sign-out action available from any workspace screen in `web/` (Firebase `signOut()`), clearing the client session.
- [ ] After sign-out or expiry, requests with an invalid/expired token get `401` from `api/` and `web/` redirects to sign-in with a clear message (centralized handling, e.g. fetch interceptor).
- [ ] Session lifetime configurable at deployment level (Firebase session settings / env config) — not hardcoded.
- [ ] For shared-machine safety, sign-out optionally revokes refresh tokens server-side (Admin SDK `revokeRefreshTokens`) — decision documented either way.
- [ ] Tests cover: protected route blocked after sign-out, expired/revoked token rejected by `api/`, redirect-with-message flow.
- [ ] No agent involvement — no `AgentExecutionLog` required (per epic DoD).

## Technical Notes

- **Area:** `web/` + `api/`
- With Firebase, ID tokens last ~1 h and the SDK refreshes them automatically — real "session expiry" requires choosing: (a) Firebase session cookies managed by `api/` with configurable `maxAge`, or (b) refresh-token revocation + inactivity check. For MVP, (b) is simpler; document the choice.
- Enforcement is server-side: `api/` validates the token on every request; the UI only reflects state.
- The expiry message must not silently lose work (the "redirects with a clear message" criterion covers half-filled forms).

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-001 (Teacher Login) | Sign-out and expiry act on the session created at sign-in |

## Complexity

**Estimate:** M
