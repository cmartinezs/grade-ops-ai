# Assessment Snapshot on Publish

- Status: Accepted
- Date: 2026-06-10
- Decision owner: Architecture

## Context

In closed assessments, the question set, alternatives, answer key, scoring policy, and grade scale must be stable while the assessment is running. If a question in the bank is edited after the assessment is published, or if the answer key changes, results become unreliable and comparisons between students break.

GRADE addressed this with a snapshot pattern: when an assessment is published, the system freezes the full question and option state. Subsequent bank edits do not affect in-flight assessments.

## Decision

When a closed assessment transitions from `approved` to `published`:

1. The system creates an immutable snapshot of every selected question (`AssessmentQuestionSnapshot`).
2. The system creates a snapshot of every option per question (`AssessmentOptionSnapshot`).
3. The system freezes the answer key derived from the approved options.
4. The system freezes the scoring policy and grade scale.

From this point forward, grading uses only the snapshot data — never the live bank records.

Any changes to the live question bank (edits, retirements, new versions) do not affect assessments that are already published or in progress.

If a teacher needs to correct an error after publication, the only supported paths are:

- **Key correction**: teacher performs an explicit recalculation with audit log; original result is preserved.
- **Question annulment**: teacher voids a specific question; system recalculates for all students with audit log.
- **New version**: teacher creates a new assessment version; original run is archived.

## Rationale

- Prevents silent data corruption when bank records are edited.
- Enables safe bank curation without impacting running assessments.
- Provides the audit trail required for academic integrity.
- Matches industry practice in objective assessment platforms.

## Consequences

- `AssessmentQuestionSnapshot` and `AssessmentOptionSnapshot` are mandatory entities for closed assessments.
- Snapshot creation is part of the assessment publication transaction; it must be atomic.
- Storage increases because question and option data is duplicated per assessment run.
- Key correction and question annulment must be first-class operations with audit event recording.
- The assessment lifecycle state machine must gate grading against snapshot existence, not bank state.

<!-- nav -->

---

← [AI-Native Question Bank](2026-06-10-ai-native-question-bank.md) | [↑ inicio](#assessment-snapshot-on-publish) | [README](README.md) | [Deterministic Grading for Closed Assessments →](2026-06-10-deterministic-grading-for-closed.md)
