# Student Access via Secure Link

- Status: Accepted
- Date: 2026-06-10
- Decision owner: Product

## Context

Early GradeOps AI documentation stated that students do not need accounts and are represented only as `StudentSubmission` records loaded by the teacher. This was appropriate for an initial prototype where the teacher uploads responses on behalf of students.

The integration analysis (`.raw/14-chat.md`) identified that a real assessment operation requires a minimal student access surface: students must be able to submit their own responses and review published results. Building full student accounts is out of scope and would turn the product into an LMS.

## Decision

Students access GradeOps AI via **unique, signed access tokens delivered to their email**. No student account is required.

Specifically:

- The teacher creates a `LearnerRef` per student per course section (email, optional name, optional external ID).
- When an assessment is published, the system generates one secure token per `LearnerRef`.
- The system emails the token as an access link.
- The student opens the link, responds to the assessment, and submits.
- After grading and result publication, the student receives or can request a result link.
- The student never creates a password or a GradeOps AI profile.

## Rationale

- Real assessment operations require real student participation; teacher-loaded submissions alone are not sufficient for scalable pilots.
- Secure links are the industry-standard pattern for account-free access (magic links, invitation tokens).
- Avoids building a full student portal, student social features, or student authentication flow.
- Consistent with the principle "avoid building a full LMS during the hackathon."
- Enables genuine graded submissions (not just teacher-simulated ones), which strengthens business evidence.

## Consequences

- `StudentSubmission` alone is insufficient; introduce `LearnerRef`, `AssessmentInvitation`, and `AssessmentAttempt` to the data model.
- Token generation and email delivery must be part of the assessment publication flow.
- Security model must include token hashing, expiry, revocation, and rate limiting.
- Student privacy rules apply to `LearnerRef` data (minimal PII, tenant-scoped, deletable).
- Student result portal must be added to product scope; it is a read-only, link-authenticated view.
- Prior documentation stating "student portal: future" is superseded; a minimal result view is now P0 for closed assessments and P1 for open assessments.

<!-- nav -->

---

← [Deterministic Grading for Closed Assessments](2026-06-10-deterministic-grading-for-closed.md) | [↑ inicio](#student-access-via-secure-link) | [README](README.md) | [Curriculum Taxonomy →](2026-06-10-curriculum-taxonomy.md)
