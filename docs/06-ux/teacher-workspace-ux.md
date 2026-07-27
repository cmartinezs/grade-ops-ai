# Teacher Workspace UX

The teacher workspace is an assessment-operations console. It must make academic state, pending decisions, evidence, versions, and consequences clear.

## Core Interaction Model

AI produces proposals; the teacher reviews evidence and decides. Every AI-assisted surface shows:

- proposed content or judgment;
- evidence and uncertainty;
- provider, model, prompt/version and timestamp;
- affected criterion/objective/result version;
- available teacher actions;
- effect of each action on approval and publication.

Editing is a first-class action. Rejecting or regenerating is normal workflow, not failure.

## Dashboard

The dashboard answers:

- What requires my decision now?
- Which assessment lifecycle is blocked?
- Which results are approved but unpublished?
- Which corrections await republication?
- Which appeals lack a valid deadline?
- Which curriculum objectives are at risk?

Do not use one badge such as “approved” to summarize unrelated lifecycles. Show a concise operation summary plus the exact pending action.

## Assessment Design

The teacher selects academic context, curriculum scope, modality, template, constraints, availability, evidence expectations, rubric/scoring policy, and result visibility. Programming language is an optional domain field, not a universal requirement.

Open, closed, and mixed components share one preparation workspace while retaining their specific controls.

Before publication, show a snapshot review containing:

- student-facing instructions and components;
- rubric and evidence expectations;
- question/option/answer-key snapshot where applicable;
- scoring and grade policy;
- curriculum alignment;
- access and timing policy;
- version and validation warnings.

## Evaluation and Criterion Review

Each criterion review shows student evidence, AI proposal, uncertainty, teacher decision, and downstream impact. Closed items show deterministic explanation and route only exceptions for judgment.

Bulk actions are available only when policy allows and must disclose scope and warnings. Material edits visibly invalidate dependent approvals.

The final-approval screen separates:

1. criterion/evidence completion;
2. derived score and grade;
3. achievement level;
4. feedback;
5. final approval.

Approval never implies publication.

## Publication and Correction

Publication is an explicit confirmation that shows the version, changes, affected students, visibility, notifications, and appeal deadline.

When a teacher starts a review of a published result:

- the current official version remains visible;
- the student sees a neutral notice;
- the correction is a new draft version;
- comparison highlights score, grade, achievement, criteria, evidence, and feedback changes.

After approval, the correction is labeled `ApprovedPendingRepublication`. The teacher must explicitly republish. Discarding the correction removes the notice without changing the official result.

If the academic calendar cannot produce a valid deadline, the UI explains that appeal remains open pending configuration. It must never imply a deadline that the system cannot validate.

## Curriculum Coverage

The landing view is unit -> topic -> objective. Separate visual encodings represent:

- planned;
- taught;
- assessed;
- demonstrated achievement;
- evidence sufficiency;
- open pedagogical action.

Never show one synthetic “coverage” score that hides these dimensions. “No evidence” and “low achievement” must look and read differently.

AI recommendations are presented as proposals with accept, edit, reject, and defer actions. Adopted actions remain attributable to the teacher.

## Audit and Trust

Audit is understandable product evidence, not a raw debug console. Teachers can trace:

- who did what and when;
- which version and policy applied;
- what AI contributed;
- what the teacher changed;
- why approval was invalidated;
- which version became official;
- how a deadline was calculated.

## Accessibility and Privacy

- State is not communicated by color alone.
- Critical actions include plain-language consequences.
- Notifications and token URLs expose no sensitive academic data.
- Date/time surfaces show the academic timezone.
- Version comparison and evidence review remain keyboard accessible.

<!-- nav -->

---

← [Screen Inventory](screen-inventory.md) | [↑ inicio](#teacher-workspace-ux) | [Student Access UX →](student-access-ux.md)
