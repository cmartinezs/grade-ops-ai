# US-008: Teacher Self-Registration

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P0
- **ID:** US-008

## Story

As a programming educator, I want to create my own GradeOps AI account so I can start using the platform without waiting for an operator.

## Acceptance Criteria

- Teacher can register with email and password.
- Registration creates a Firebase Auth user and a corresponding teacher record in the API.
- Registering with an already-used email shows a clear error.
- A verification email is sent automatically after registration (see US-009).
- A new account starts with an empty workspace (zero assessments).

---

## Definition of Done

- [ ] Registration flow implemented in `web/` using the Firebase Auth SDK (`createUserWithEmailAndPassword`).
- [ ] `api/` validates the Firebase ID token and creates the teacher record, keyed by Firebase UID — the API never receives or stores raw passwords.
- [ ] Password policy enforced through Firebase Auth settings.
- [ ] Tests cover: successful registration, duplicate email, and teacher record creation in the API.
- [ ] No agent involvement — no `AgentExecutionLog` required (per epic DoD).

## Technical Notes

- **Area:** `web/` + `api/` + `infra/`
- Firebase Authentication (Google Identity Platform) is the identity provider — record the ADR in `docs/99-decisions/`.
- The API authenticates requests by verifying Firebase ID tokens (Admin SDK / JWKS); the Firebase web config is public by design, the Gemini API key remains server-side only.
- Firebase project provisioning belongs in `infra/` (Terraform `google_identity_platform_*` resources).
- Pilot/related-party flagging (US-003) applies to self-registered accounts as well.
- Operator provisioning (US-006) coexists with self-registration — both create the same kind of teacher record.

## Dependencies

| Depends on | Reason |
|------------|--------|
| Firebase project setup (`infra/`) | Identity provider must exist before any registration flow |

## Complexity

**Estimate:** M
