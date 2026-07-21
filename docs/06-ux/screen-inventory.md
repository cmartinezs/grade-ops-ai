# Screen Inventory

Complete inventory of screens in GradeOps AI, organized by role and assessment mode. Status indicates MVP priority relative to the cut line defined in `02-product/mvp-scope.md`.

This is a product/UX inventory, not a claim that every route is implemented today. The current web app has authentication, dashboard shell, and protected placeholder routes for assessments, question bank, students, and reports. Screens below marked P0 still need implementation unless the corresponding release slice has already delivered them.

**Status key:** `P0` = protect first / `P1` = build next / `P2` = later

## Current Web Availability

| Area | Current Route | Availability |
| --- | --- | --- |
| Landing | `/` | Implemented. |
| Login/register/verify email | `/login`, `/register`, `/verify-email` | Implemented. |
| Password reset | `/forgot-password`, `/reset-password` | Implemented. |
| Dashboard | `/dashboard` | Implemented shell with assessment list integration. |
| Assessments | `/assessments` | Protected route present; feature depth depends on assessment release slice. |
| Question bank | `/bank` | Protected placeholder/early shell; Closed bank workflow not complete. |
| Students | `/students` | Protected placeholder/early shell; no student accounts in MVP. |
| Reports | `/reports` | Protected placeholder/early shell; report workflow not complete. |

Target routes in the tables below may differ from current web routes. Keep the current app route stable until a release explicitly migrates navigation.

## Shared State Vocabulary

Use these states consistently across teacher screens:

| State | UX Meaning |
| --- | --- |
| `draft` | Teacher input or saved object exists but is not ready for agent processing or publication. |
| `generated` | AI produced an output; teacher has not acted on it yet. |
| `needs_review` | The next required action is teacher review. |
| `approved` | Teacher accepted the output or deterministic result. |
| `published` | Approved output/result is visible or exportable to its intended audience. |
| `blocked` | Workflow cannot continue until a validation, policy, missing input, or dependency problem is resolved. |
| `error` | A system or agent call failed; retry or support action is needed. |

---

## Teacher Workspace

### Authentication

| Screen | Route | Status | Notes |
|--------|-------|--------|-------|
| Login | `/login` | P0 | Email + password; no SSO for MVP |
| Register | `/register` | P0 | Single-teacher account; no team invites yet |
| Forgot password | `/forgot-password` | P1 | Required for pilots |

### Dashboard

| Screen | Route | Status | Notes |
|--------|-------|--------|-------|
| Assessment overview | `/teacher/dashboard` | P0 | List of assessments, status, pending approvals count |
| Notifications / pending queue | `/teacher/dashboard` | P0 | Inline — not a separate screen |

### Open Assessments

| Screen | Route | Status | Notes |
|--------|-------|--------|-------|
| Create assessment (intake form) | `/teacher/assessments/new` | P0 | Learning goal, topic, level, duration, constraints |
| Assessment draft review | `/teacher/assessments/[id]` | P0 | AI-generated draft; approve / edit / regenerate |
| Rubric review and approval | `/teacher/assessments/[id]/rubric` | P0 | Criteria, weights, levels; approve / edit / lock |
| Submission upload | `/teacher/assessments/[id]/submissions` | P0 | Paste or upload; student identifier |
| Grading review queue | `/teacher/assessments/[id]/grading` | P0 | Suggested scores per submission; approve / edit / reject |
| Feedback review queue | `/teacher/assessments/[id]/feedback` | P0 | Feedback drafts per student; approve / edit |
| Learning gap summary | `/teacher/assessments/[id]/gaps` | P1 | Cohort-level issues; review and confirm |
| Recovery suggestions | `/teacher/assessments/[id]/recovery` | P1 | Remedial activity per gap; review |
| Teacher report | `/teacher/assessments/[id]/report` | P0 | Final report; export PDF/CSV |

### Closed Assessments

| Screen | Route | Status | Notes |
|--------|-------|--------|-------|
| Define evaluative intent | `/teacher/assessments/new?mode=closed` | P0 | Learning outcomes, topic, format preferences |
| Question curation queue | `/teacher/question-bank/[batchId]` | P0 | Approve / edit / reject generated questions |
| Question bank browser | `/teacher/question-bank` | P1 | Browse, filter, search approved items |
| Assessment composition | `/teacher/assessments/[id]/compose` | P0 | Review assembled question set; approve composition |
| Answer key review | `/teacher/assessments/[id]/answer-key` | P0 | Confirm scoring policy before publish |
| Learner list management | `/teacher/assessments/[id]/learners` | P0 | Add students (email); manage invite status |
| Exception review queue | `/teacher/assessments/[id]/exceptions` | P0 | Edge cases in deterministic grading |
| Item analytics report | `/teacher/assessments/[id]/analytics` | P0 | Post-assessment item difficulty and outcome coverage |
| Publish results | `/teacher/assessments/[id]/results` | P0 | Confirm and send result links to students |

### Reports and Evidence

| Screen | Route | Status | Notes |
|--------|-------|--------|-------|
| Teacher report viewer | `/teacher/assessments/[id]/report` | P0 | Summary, gaps, distribution, time saved |
| Agent log viewer | `/teacher/agent-logs` | P0 | All agent runs; filter by assessment, date, agent, status |
| Evidence dashboard | `/teacher/evidence` | P0 | Usage, cost, approvals, revenue summary — demo-critical |
| Export | Inline buttons on report/evidence | P1 | PDF, CSV |

---

## Student Experience (Secure Link)

Students receive links via email. All student screens are token-gated: no login, no account, no password, no global navigation.

### Open Assessments

| Screen | Route | Status | Notes |
|--------|-------|--------|-------|
| Not applicable | — | — | Open assessments: teacher uploads submissions directly; no student-facing form |

### Closed Assessments

| Screen | Route | Status | Notes |
|--------|-------|--------|-------|
| Welcome / instructions | `/student/assessment/[token]` | P0 | Assessment title, instructions, duration, deadline |
| Response form | `/student/assessment/[token]/respond` | P0 | One question at a time or all questions; submit |
| Confirmation | `/student/assessment/[token]/done` | P0 | Submission received confirmation; no score shown yet |
| Published results | `/student/results/[token]` | P0 | Score, item-level feedback (if teacher released); read-only |

---

## Public Pages

| Screen | Route | Status | Notes |
|--------|-------|--------|-------|
| Landing | `/` | P1 | Product intro, value proposition, CTA to register/demo |
| Pricing | `/pricing` | P1 | Plan comparison; CTA to contact or register |

For the MVP, the product works without a polished landing. The teacher logs in directly. The landing can be minimal but must exist for demos.

---

## MVP Cut Line

**Protect these screens first (P0):**

1. Login
2. Dashboard (assessment list + pending queue)
3. Create assessment (open)
4. Assessment draft review + rubric approval
5. Submission upload
6. Grading review queue
7. Feedback review queue
8. Teacher report
9. Agent log viewer
10. Evidence dashboard

**Closed assessment P0 screens:**
1. Define evaluative intent
2. Question curation queue
3. Composition + answer key review
4. Learner list
5. Exception queue
6. Item analytics
7. Publish results
8. Student response form + results link

**Everything else (P1/P2) after the core flow is demonstrated.**

<!-- nav -->

---

← [README](README.md) | [↑ inicio](#screen-inventory) | [Teacher Workspace UX →](teacher-workspace-ux.md)
