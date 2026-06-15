# Firebase Authentication

- Status: Accepted
- Date: 2026-06-12
- Decision owner: Architecture / Founder

## Context

GradeOps AI requires a teacher authentication system. The API (`api/`) must identify and authorize requests from authenticated teachers. Four decisions were open at the start of implementation:

1. **Identity provider** — which service issues and validates teacher credentials.
2. **Operator access mechanism** — how operators provision new teacher accounts and manage pilot flags without a separate admin UI.
3. **Pre-verified email for provisioned accounts** — whether operator-created accounts bypass the email verification gate.
4. **Session expiry strategy** — how sign-out and token revocation are handled to prevent unauthorized access after logout.

The system is teacher-only: students never authenticate. Students access assessments and results via signed token links (`AssessmentInvitation`) per [ADR 2026-06-10-student-access-via-secure-link.md](2026-06-10-student-access-via-secure-link.md).

Constraints:
- Runtime target is Google Cloud (Cloud Run, Cloud SQL, Secret Manager).
- MVP must avoid building a custom identity backend (password storage, token issuance, rotation, breach response).
- A single developer must be able to deliver a working auth flow quickly.
- Operator provisioning must be secure but requires no separate operator-facing admin application.

Alternatives considered for the identity provider:

| Option | Assessment |
| --- | --- |
| Firebase Authentication (Google Identity Platform) | GCP-native; no credential storage in the API; Admin SDK covers token validation, user provisioning, and revocation; integrates with the chosen runtime without additional infrastructure |
| Auth0 | Strong product; requires an external account and vendor relationship outside the Google Cloud ecosystem; adds monthly cost and a third-party dependency for an early-stage MVP |
| Self-managed JWT | Maximum control; requires building password hashing, token issuance, rotation, revocation storage, and breach response from scratch; not feasible in a hackathon timeline |

## Decision

### Decision 1 — Identity Provider

Firebase Authentication (Google Identity Platform) is the identity provider for teacher accounts.

- The `web/` client authenticates teachers using the Firebase client SDK (email/password or federated providers).
- The `web/` client obtains a Firebase ID token and passes it as a `Bearer` token in every API request.
- The `api/` validates the token using the Firebase Admin SDK: `admin.auth().verifyIdToken(token, checkRevoked: true)`.
- The `api/` never stores teacher passwords, raw tokens, or Firebase credentials. The only Firebase artifact persisted in the database is the Firebase UID, which becomes the primary key of the `Teacher` record.
- Students are excluded from this authentication model. Student access uses signed token links as defined in [ADR 2026-06-10-student-access-via-secure-link.md](2026-06-10-student-access-via-secure-link.md).

### Decision 2 — Operator Access Mechanism

Operator account management is handled via a protected internal endpoint on the `api/`, secured by a shared-secret header.

- Endpoints:
  - `POST /internal/teachers` — provision a new teacher account.
  - `PATCH /internal/teachers/{uid}/flags` — set or clear pilot flags on an existing teacher.
- All `/internal/**` routes require the `X-Internal-Key` header. The key is stored in Secret Manager and injected as an environment variable in the `api/` Cloud Run service.
- This endpoint is not publicly advertised. Cloud Run ingress settings restrict it to internal traffic; the `X-Internal-Key` header provides a defense-in-depth layer.

Alternatives considered:

| Option | Reason not chosen |
| --- | --- |
| GCP IAM service-to-service auth | Adds complexity: requires a dedicated service account, role bindings, and token exchange; no simpler to operate than the shared-secret approach at this scale |
| Firebase custom claims | Claims management requires the Admin SDK and a separate trigger; provides no UX advantage over the internal endpoint in an operator-only flow |
| Cloud Console direct DB access | Bypasses application validation; breaks audit log continuity; too risky for a production-adjacent system |

### Decision 3 — Pre-verified Email for Provisioned Accounts

Operator-provisioned teacher accounts are created with `emailVerified: true` via the Firebase Admin SDK.

- The provisioning call uses `admin.auth().createUser({ email, emailVerified: true, ... })`.
- These accounts skip the scope-09 email verification gate entirely.
- Rationale: pilot teachers are provisioned by the operator, who has already confirmed reachability out-of-band. Blocking them on email verification adds friction without a security benefit.
- Self-registered teacher accounts (scope-08) must always complete email verification before accessing the platform (scope-09). The two paths are mutually exclusive and handled by separate code branches.

### Decision 4 — Session Expiry Strategy

Session expiry uses refresh-token revocation on sign-out, with revocation checked on every API call.

Sign-out flow:
1. `web/` calls `POST /auth/sign-out` on the `api/`.
2. `api/` calls `admin.auth().revokeRefreshTokens(uid)`.
3. `api/` returns success.
4. `web/` calls `firebase.auth().signOut()` to clear the local client session.

Token lifecycle:
- Firebase ID tokens have a 1-hour TTL. The Firebase client SDK auto-refreshes them transparently.
- Because `api/` uses `verifyIdToken(token, checkRevoked: true)`, a revoked token is rejected on the next API call even if it has not yet expired.
- There is a propagation window of up to 1 minute between revocation and rejection; this is acceptable for MVP.

Alternative considered and rejected:

| Option | Reason not chosen |
| --- | --- |
| Firebase session cookies managed by `api/` | Requires server-side session storage, cookie issuance, and a separate session validation path. Adds meaningful complexity with no material security benefit for the teacher workspace use case |

## Rationale

Firebase Authentication is the lowest-friction path to a production-grade identity system on Google Cloud. The Admin SDK provides all required operations (token validation, user creation, revocation) without introducing a separate infrastructure component. Because the `api/` never receives or stores passwords, the attack surface for credential compromise is eliminated.

The internal endpoint pattern for operator access is simpler than service-to-service IAM at this stage and preserves full audit coverage — every provisioning action passes through application logic and is loggable.

Pre-verifying email for provisioned accounts avoids a support burden during the pilot without weakening the security model: the operator controls who is provisioned and is responsible for validating reachability before provisioning.

Refresh-token revocation on sign-out is the Firebase-recommended approach for web applications that use ID tokens directly. It is more reliable than relying solely on token expiry, and it avoids the complexity of server-side session storage.

## Consequences

**Easier:**
- No credential storage or password management in the `api/`.
- Token validation is a single Admin SDK call with built-in revocation support.
- User management (create, disable, delete) is available via the Admin SDK without custom infrastructure.
- Firebase Authentication integrates with Google Identity Platform, enabling future support for federated providers (Google SSO, GitHub) without architectural changes.

**Harder:**
- The `api/` takes a hard dependency on the Firebase Admin SDK and the Firebase Authentication service. Migrating to a different identity provider would require changes to token validation, user provisioning, and revocation logic.
- `checkRevoked: true` on every `verifyIdToken` call adds a network round-trip to Firebase per request. This latency must be monitored under load.
- Operator provisioning requires access to the `X-Internal-Key` secret. Key rotation must be coordinated with the Cloud Run service restart.

**Riskier:**
- Firebase is a Google-managed service. Any Firebase Authentication outage affects the teacher login flow. Mitigation: Cloud Run health checks and graceful error responses to the client.
- The 1-minute revocation propagation window means a signed-out session can make one additional valid API call. Acceptable for MVP; can be tightened in a future revision if required.

<!-- nav -->

---

← [Student Access via Secure Link](2026-06-10-student-access-via-secure-link.md) | [↑ inicio](#firebase-authentication) | [README](README.md)
