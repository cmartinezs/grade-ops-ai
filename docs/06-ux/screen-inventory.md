# Screen Inventory

This inventory describes the approved target UX. Current routes may remain as compatibility paths until an incremental release migrates them.

**Priority:** `P0` protected end-to-end slice, `P1` next, `P2` later.

## State Presentation Rule

Do not present one shared assessment status as if it represented the whole operation. Screens may show a summary, but must expose the relevant lifecycle:

| Lifecycle | Representative states |
| --- | --- |
| Preparation | Draft, InReview, Approved, PublishedSnapshot |
| Application | Scheduled, Available, Closed, Cancelled |
| Attempt/submission | Invited, Started, Submitted, Late, Absent, Recovery |
| Evaluation | Pending, Processing, Proposed, DeterministicallyScored, Exception |
| Criterion review | PendingReview, PartiallyReviewed, Reviewed, Invalidated |
| Result approval | PendingApproval, Approved |
| Publication | Unpublished, Published, Superseded |
| Correction | NotUnderReview, ReviewInProgress, ApprovedPendingRepublication, Discarded |
| Appeal | NotOpen, Open, OpenPendingDeadlineConfiguration, Resolved |
| Analysis | Pending, CurrentSnapshot, Stale, Superseded |

Labels may be localized; transitions and meanings must remain explicit.

## Teacher Workspace

| Screen | Target route | Priority | Purpose |
| --- | --- | --- | --- |
| Dashboard and action queue | `/teacher/dashboard` | P0 | Work awaiting teacher action across independent lifecycles |
| Academic periods/courses/sections | `/teacher/academic` | P0 | Academic context, assignments, enrollment |
| Curriculum map | `/teacher/courses/[courseId]/curriculum` | P0 | Unit/topic/objective planning and taught-state declarations |
| Assessment list | `/teacher/assessments` | P0 | Filtered operational overview |
| Create/edit assessment | `/teacher/assessments/new` | P0 | Template, course, curriculum scope, open/closed/mixed components |
| Preparation review | `/teacher/assessments/[id]/design` | P0 | Instructions, items, rubric, policies, alignments and snapshot approval |
| Invitations and access | `/teacher/assessments/[id]/participants` | P0 | Learners, access policy, signed links and delivery state |
| Attempts/submissions | `/teacher/assessments/[id]/submissions` | P0 | Attempts, uploads, late/absence/recovery cases |
| Evaluation queue | `/teacher/assessments/[id]/evaluation` | P0 | Deterministic outcomes, AI proposals, exceptions and uncertainty |
| Criterion review | `/teacher/results/[resultId]/review` | P0 | Evidence-aware per-criterion review and approval invalidation |
| Result approval | `/teacher/results/[resultId]/approval` | P0 | Derived score, grade, achievement and feedback confirmation |
| Publish result | `/teacher/assessments/[id]/results` | P0 | Explicit publication and affected-student confirmation |
| Correction/republication | `/teacher/results/[resultId]/correction` | P0 | New version, comparison, approval and explicit republication |
| Appeals | `/teacher/assessments/[id]/appeals` | P1 | Formal appeals, deadlines and resolution |
| Curriculum coverage | `/teacher/courses/[courseId]/coverage` | P0 | Planned/taught/assessed/demonstrated/action layers |
| Objective detail | `/teacher/objectives/[objectiveId]` | P0 | Criteria, evidence, cohort/student drill-down and actions |
| Agent/audit log | `/teacher/audit` | P0 | Model, prompt, cost, actor, policy, version and transition history |
| Operational evidence | `/teacher/evidence` | P1 | Usage, cost, time saved, pilots and business evidence |

## Student Experience

Student access may be authenticated or token-gated according to policy. Token-gated pages have no unrelated navigation.

| Screen | Target route | Priority | Purpose |
| --- | --- | --- | --- |
| Assessment welcome | `/student/assessment/[token]` | P0 | Identity/access validation, instructions, availability and policy |
| Response workspace | `/student/assessment/[token]/respond` | P0 | Open, closed, or mixed response with autosave where applicable |
| Submission confirmation | `/student/assessment/[token]/done` | P0 | Receipt, timestamp and next step |
| Official result | `/student/results/[token]` | P0 | Current published version, allowed evidence/feedback and appeal deadline |
| Review-in-progress notice | Result surface | P0 | Neutral notice while official result remains in force |
| Corrected-result notice | Result surface | P0 | Direct access to republished version and exact appeal deadline |
| Formal appeal | `/student/results/[token]/appeal` | P1 | Submit and follow permitted appeal workflow |

Sensitive academic data must not appear in notification subject, preview, or URL.

## Curriculum Coverage UX

The main analytical entry is unit, topic, and objective. Do not collapse coverage, achievement, and confidence into one score.

Summary cards:

- worked;
- assessed;
- sufficient evidence;
- expected achievement reached;
- critical objectives at risk;
- not assessed;
- open actions.

Objective drill-down includes alignments, assessment strength/depth, approved evidence, distribution, errors, evolution, affected learners, AI proposals, and adopted teacher actions.

## P0 Cut Line

Protect the complete traceable flow: academic/curriculum setup; assessment design and snapshot; participation; evaluation; criterion review; result approval; explicit publication; correction/republication; curriculum coverage; and audit evidence.

Landing, pricing, advanced administration, benchmarking, prediction, and enterprise BI do not outrank this flow.

<!-- nav -->

---

← [README](README.md) | [↑ inicio](#screen-inventory) | [Teacher Workspace UX →](teacher-workspace-ux.md)
