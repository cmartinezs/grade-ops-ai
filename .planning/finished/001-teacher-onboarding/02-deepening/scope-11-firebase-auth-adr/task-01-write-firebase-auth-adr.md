# ⚛️ TASK 01 — Write Firebase Authentication ADR

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-11-firebase-auth-adr.md)

---

## Objective

Create `docs/99-decisions/2026-06-12-firebase-authentication.md` using the project ADR template, recording the Firebase Authentication decision and closing the four open decisions that block scopes 03, 04, 06, 08, and 09.

---

## Technical Design

- **Approach:** Use `docs/99-decisions/adr-template.md` as the base. Model the format on existing ADRs (e.g. `2026-06-10-technology-stack.md`). The document must close: (1) identity provider choice, (2) operator account access mechanism, (3) pre-verified email for provisioned accounts, (4) session expiry strategy.
- **Affected files / components:**
  - `docs/99-decisions/2026-06-12-firebase-authentication.md` (new file)
  - `docs/99-decisions/README.md` (add entry to the ADR index)
- **Interfaces / contracts:** Referenced by scopes 01, 04, 06, 08, 09 in their Technical Notes. Docs only — no code contract.
- **Design notes for each open decision:**
  - **Identity provider:** Firebase Authentication (Google Identity Platform). `api/` validates Firebase ID tokens via Admin SDK / JWKS. No credentials stored or received by the API. Teacher-only — students use signed token links (ADR `2026-06-10-student-access-via-secure-link.md`).
  - **Operator access mechanism:** Protected internal endpoint (`/internal/*`) secured by a shared secret header or by restricting network access to Cloud Run internal ingress. No separate operator login in MVP.
  - **Pre-verified email for provisioned accounts:** Operator-provisioned accounts are created with `emailVerified = true` via the Admin SDK (`UserRecord` with `emailVerified` flag), skipping the scope-09 verification gate. Self-registered accounts always go through the verification flow.
  - **Session expiry strategy:** Refresh-token revocation on sign-out (`Admin SDK revokeRefreshTokens`). `api/` checks token revocation on each request using the Admin SDK `verifyIdToken(token, checkRevoked: true)`. Session duration is configured via Firebase session cookie settings (not applicable in this MVP since we use ID tokens directly), so effective session = ID token TTL (~1 h) with explicit revocation on sign-out.

---

## Implementation Steps

1. Copy `docs/99-decisions/adr-template.md` to `docs/99-decisions/2026-06-12-firebase-authentication.md`.
2. Fill **Status**: `Accepted`, **Date**: `2026-06-12`, **Decision owner**: `Architecture / Founder`.
3. Write **Context** section: explain the auth requirements (teacher-only login, Firebase as GCP-native identity, four open decisions blocking implementation).
4. Write **Decision** section with four subsections — one per open decision — recording the chosen option.
5. Write **Rationale** section explaining why Firebase Auth and each sub-decision is the right choice for MVP.
6. Write **Consequences** section covering what becomes easier (unified identity, GCP-native), harder (no custom auth UI), and riskier (Firebase vendor dependency, revocation check adds latency).
7. Add nav links to `docs/99-decisions/README.md` linking the new ADR.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | File exists at correct path | `ls docs/99-decisions/2026-06-12-firebase-authentication.md` succeeds | Bash |
| 2 | All four decisions are explicitly addressed | Each decision subsection has a non-placeholder decision statement | Manual review |
| 3 | ADR indexed in README | `grep "firebase-authentication" docs/99-decisions/README.md` returns a match | Bash |

---

## Done Criteria

- [x] `docs/99-decisions/2026-06-12-firebase-authentication.md` exists and follows the ADR template format.
- [x] Identity provider decision recorded (Firebase Auth, ID token validation in `api/`, teacher-only).
- [x] Operator access mechanism decision recorded (internal endpoint with shared secret or internal ingress).
- [x] Pre-verified email for provisioned accounts decision recorded (`emailVerified = true` via Admin SDK).
- [x] Session expiry strategy decision recorded (refresh-token revocation + `checkRevoked: true`).
- [x] ADR linked in `docs/99-decisions/README.md`.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-11-firebase-auth-adr.md)
