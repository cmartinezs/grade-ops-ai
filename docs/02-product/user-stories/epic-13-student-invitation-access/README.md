# Epic 13 — Student Invitation and Access

## Narrative

**As a** teacher who has published a closed assessment,  
**I want** to invite students via email with unique signed links, let them respond without creating an account, and then publish results back through the same secure channel,  
**so that** students participate in the assessment with zero friction while the system maintains full traceability and access control.

## Goal

Deliver the student-facing side of closed assessments: invitation, response capture, and result access — all through signed token links with no student login required. This epic also includes the item analytics report that closes the closed-assessment cycle.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-120 | Create Learner List | P0 | [01-create-learner-list.md](01-create-learner-list.md) |
| US-121 | Send Assessment Access Links | P0 | [02-send-assessment-links.md](02-send-assessment-links.md) |
| US-122 | Student Response via Link | P0 | [03-student-response-via-link.md](03-student-response-via-link.md) |
| US-123 | Publish Results and Student Result Access | P0 | [04-publish-results-student-access.md](04-publish-results-student-access.md) |
| US-124 | Item Analytics Report | P0 | [05-item-analytics-report.md](05-item-analytics-report.md) |

## Scope

**In scope**
- Learner list creation: manual entry (email, optional name, optional external ID) or CSV import
- Unique signed token generation per learner per assessment (`AssessmentInvitation`)
- Email delivery of access links; teacher can resend or revoke
- Student response UI: question presentation, alternative selection, review before submit
- Token validation on access: not expired, not revoked, assessment accepting responses
- Deterministic grading against frozen answer key after submission
- Teacher-controlled result publication; result access link per learner
- Student result view: grade, score, approved feedback; correct answers visible only if configured
- Item Analytics Agent: correct rate, difficulty index, distribution per question, ambiguity/key error flags, reinforcement suggestions per learning outcome

**Out of scope**
- Student account creation or persistent student login
- Student-to-student interaction or discussion
- Open-assessment student self-submission (MVP: teacher loads open submissions manually — Epic 04)
- Adaptive question ordering per student

## Epic Acceptance Criteria

- Teacher can add learners manually or import via CSV; learners are associated with the assessment.
- System generates a unique signed token per learner and sends an email with the access link.
- Teacher can resend or revoke individual learner links.
- Student opens the link; system validates the token (not expired, not revoked, assessment open).
- Student can select alternatives, review, and submit; the link is marked used after submission.
- Grading runs deterministically against the frozen answer key snapshot.
- Teacher triggers result publication; system sends or makes available a result link per learner.
- Student result view shows grade, score, and approved feedback; correct answers shown only if teacher-configured.
- One student cannot see another student's results.
- Item Analytics Agent produces correct rate, difficulty index, distribution, and reinforcement suggestions per question/outcome; agent run is logged.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Authenticated teacher workspace required |
| Epic 09 — Evidence and Metrics | Access events and agent runs must be logged |
| Epic 12 — Question Bank and Closed Assessment | Assessment must be published with a frozen snapshot before invitations can be sent |

## Definition of Done

- All five P0 stories pass their acceptance criteria.
- `AssessmentInvitation` entity stores token, expiry, revocation state, and usage state.
- Token validation is server-side; a tampered or expired token is rejected with a clear error.
- Grading is deterministic and operates against the snapshot, not live bank data.
- Students cannot access other students' results under any code path.
- Item Analytics Agent produces an `AgentExecutionLog` record per run.
- Access and result-view events are logged for evidence.
