# US-006: Teacher Account Provisioning

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P0
- **ID:** US-006

## Story

As an operator, I want to provision teacher accounts directly so pilot teachers can be onboarded without going through self-registration.

## Acceptance Criteria

- Operator can create a teacher account with name and email.
- A provisioned teacher can sign in immediately after receiving credentials or an invite.
- A new account starts with an empty workspace (zero assessments).
- Operator provisioning coexists with self-registration (US-008); both produce the same kind of teacher record.

---

## Definition of Done

- [ ] Internal provisioning operation in `api/` creates the Firebase Auth user (Admin SDK) and the teacher record (keyed by Firebase UID) in one flow.
- [ ] Access delivery via Firebase invite / password-reset link — no passwords handled or sent manually.
- [ ] Provisioning with an already-used email fails with a clear error and no partial records.
- [ ] Tests cover: provision → sign-in path, duplicate email, and empty workspace for the new account.
- [ ] No agent involvement — no `AgentExecutionLog` required (per epic DoD).

## Technical Notes

- **Area:** `api/`
- Firebase Admin SDK runs server-side only (`api/`); never in `web/`.
- Option: create the account with email pre-verified (Admin SDK), skipping the US-009 verification gate for operator-provisioned pilots — document the choice either way.
- Produces the same teacher record as self-registration (US-008); pilot flagging (US-003) applies afterwards.
- **Open point (shared with US-003):** the operator access mechanism is undefined — protected internal endpoint vs. console access; decide before implementation.

## Dependencies

| Depends on | Reason |
|------------|--------|
| Firebase project setup (`infra/`) | Identity provider must exist before accounts can be created |

## Complexity

**Estimate:** M
