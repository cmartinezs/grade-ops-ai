# Epic 01 — Teacher Onboarding and Workspace

## Narrative

**As a** programming educator new to GradeOps AI,  
**I want** a secure workspace where I can sign in and see all my assessments at a glance,  
**so that** I can start running assessment cycles without friction and know exactly what needs my attention.

## Goal

Establish the teacher's authenticated entry point and operational home base. Without this epic, no other teacher workflow can begin.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-001 | Teacher Login | P0 | [01-teacher-login.md](01-teacher-login.md) |
| US-002 | Assessment Dashboard | P0 | [02-assessment-dashboard.md](02-assessment-dashboard.md) |
| US-003 | Pilot Account Flag | P1 | [03-pilot-account-flag.md](03-pilot-account-flag.md) |
| US-004 | Sign-Out and Session Expiry | P1 | [04-sign-out-session-expiry.md](04-sign-out-session-expiry.md) |
| US-005 | Dashboard Empty State | P1 | [05-dashboard-empty-state.md](05-dashboard-empty-state.md) |
| US-006 | Teacher Account Provisioning | P0 | [06-teacher-account-provisioning.md](06-teacher-account-provisioning.md) |
| US-007 | Cross-Teacher Access Denial | P0 | [07-cross-teacher-access-denial.md](07-cross-teacher-access-denial.md) |
| US-008 | Teacher Self-Registration | P0 | [08-teacher-self-registration.md](08-teacher-self-registration.md) |
| US-009 | Email Verification | P0 | [09-email-verification.md](09-email-verification.md) |
| US-010 | Postman Collection for Teacher Onboarding Endpoints | P1 | [10-postman-collection.md](10-postman-collection.md) |
| US-011 | Google Sign-In for Teachers | P0 | [11-google-sign-in.md](11-google-sign-in.md) |
| US-012 | Password Recovery | P1 | [12-password-recovery.md](12-password-recovery.md) |

## Scope

**In scope**
- Teacher authentication (sign-in flow, sign-out, session expiry) via Firebase Authentication
- Teacher self-registration with email verification (Firebase native link)
- Google Sign-In for registration and sign-in (OAuth via Firebase)
- Password recovery via Firebase Auth email reset link
- Operator-provisioned teacher accounts for pilots (coexists with self-registration)
- Assessment list with status, submission count, and pending approvals
- Dashboard empty state for first-time teachers
- Cross-teacher access denial (workspace isolation)
- Pilot/free/paid account flagging for business evidence

**Out of scope**
- Student login (students access via signed token links — see Epic 12)
- Multi-teacher collaboration or shared workspaces
- Role-based access control beyond a single teacher view

## Epic Acceptance Criteria

- A teacher can self-register and must verify their email before accessing the workspace.
- A teacher can sign in and land on a dashboard that lists their assessments.
- Each assessment on the dashboard shows its current status, submission count, and any pending approvals.
- A teacher cannot access another teacher's assessment data.
- An operator can flag an account as pilot/free/paid and attach evidence or offer details.
- Pilot flag and related-party flag are stored and queryable for business reporting.

## Dependencies

None — this is the entry-point epic. All other teacher-facing epics depend on it.

## Definition of Done

- All P0 stories pass their acceptance criteria.
- Authentication is secure; a teacher cannot access another teacher's data.
- Dashboard renders correctly with zero assessments (empty state).
- Pilot account flag is persisted and visible in internal tooling.
- Agent execution log entries are not required for this epic (no AI agents involved).
