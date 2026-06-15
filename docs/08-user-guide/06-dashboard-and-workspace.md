# Your Dashboard and Workspace

The teacher workspace is your operational environment for managing every assessment from creation to published results. This guide explains what you see, how to navigate it, and where to find information about agent activity, usage, and your plan.

---

## 1. Dashboard Overview

The dashboard at `/teacher/dashboard` is your command center. Every time you sign in, this is your starting point.

The dashboard answers three questions at a glance:

- What assessments are active right now?
- Which ones have something waiting for my approval?
- What is the current status of each assessment?

Assessments are listed sorted by most recent activity, so the assessment that most needs your attention is at the top.

---

## 2. Assessment Cards

Each assessment appears as a card. The card shows everything you need to decide whether to open it or move on.

| Element | Description |
|---|---|
| **Title** | The name of the assessment (AI-generated or set by you) |
| **Mode indicator** | Open or Closed |
| **Status badge** | The current stage of the assessment (see below) |
| **Submission count** | How many student submissions (open) or attempts (closed) have been recorded |
| **Pending approvals** | How many AI outputs are waiting for your review right now |
| **Report link** | A direct link to the teacher report — visible only after the report has been generated |

Click any card to open the full assessment view.

---

## 3. Status Badge Meanings

The status badge shows where an assessment is in its lifecycle. Color gives you a quick signal.

| Badge | Color | Meaning |
|---|---|---|
| **DRAFT** | Gray | The assessment brief has been created. The assessment is not yet fully configured or has not had its rubric/composition approved. |
| **OPEN** | Blue | The assessment is active. For open mode: accepting submissions. For closed mode: students can respond via their access links. |
| **GRADING** | Yellow | AI grading agents are running, or grading suggestions and review items are waiting for your attention. |
| **CLOSED** | Green | Grading is complete and results have been published to students. The cycle is finished. |

A GRADING badge paired with a non-zero pending approvals count is your most common action signal.

---

## 4. Pending Approvals Count

The pending approvals number tells you how many AI-generated outputs are waiting for your action in that assessment.

- **0** — You are up to date. There is nothing waiting for your review in this assessment.
- **1 or more** — Something needs your attention. Click the card to see what.

What counts as a pending approval:

- An assessment draft waiting for your review
- A rubric waiting for approval
- Grading suggestions not yet reviewed
- Feedback drafts not yet approved
- Learning gaps not yet confirmed
- Recovery activities not yet reviewed
- A teacher report not yet approved

When the pending count reaches zero for all assessments, you have completed all AI-assisted steps that require your input.

---

## 5. Navigation

The workspace has a main navigation structure with the following sections:

| Section | What You Find There |
|---|---|
| **Dashboard** | Assessment list and overview (your starting point) |
| **Question Bank** | Your approved question library for closed assessments |
| **Learner Registry** | Your organization's list of learner references for closed assessments |
| **Agent Logs** | Full history of every AI agent execution across all assessments |
| **Evidence Dashboard** | Usage statistics, cost estimates, agent run summaries, and operational metrics |
| **Account / Settings** | Your account details and sign-out option |

From any assessment card on the dashboard, clicking the card opens the assessment's internal navigation (draft, rubric, submissions, grading, feedback, gaps, recovery, report).

---

## 6. Empty State

When you have not yet created any assessments, the dashboard shows a helpful empty state:

> **No assessments yet.**
> Your first AI-assisted assessment cycle starts here.
> **Create your first assessment →**

The call-to-action link takes you directly to the assessment creation form. Once you have at least one assessment, the empty state is replaced by the normal list view.

---

## 7. Creating a New Assessment

You can create a new assessment from:

- The empty-state call-to-action
- A **Create assessment** button in the workspace navigation

Clicking this opens the assessment creation form where you choose between Open and Closed mode and fill in your brief. See the relevant workflow guide for full details:

- [Open Assessment Workflow](02-open-assessment-cycle.md)
- [Closed Assessment Workflow](03-closed-assessment-cycle.md)

---

## 8. Workspace Navigation Within an Assessment

When you open a specific assessment, you see a set of tabs specific to that assessment's mode and stage.

### Open assessment tabs

| Tab | Purpose |
|---|---|
| **Draft** | Review and approve the AI-generated assessment content |
| **Rubric** | Review and approve the grading rubric |
| **Submissions** | Upload or paste student submissions |
| **Grading** | Review AI grading suggestions |
| **Feedback** | Review AI feedback drafts |
| **Gaps** | Review detected learning gaps |
| **Recovery** | Review suggested recovery activities |
| **Report** | View and export the teacher report |

### Closed assessment tabs

| Tab | Purpose |
|---|---|
| **Composition** | Review the proposed question set |
| **Answer Key** | Confirm scoring policy before publish |
| **Learners** | Manage the learner list and invitations |
| **Exceptions** | Resolve grading exceptions |
| **Analytics** | Review item analytics report |
| **Results** | Confirm and publish results |

---

## 9. Agent Run Logs

Every AI agent execution is recorded in the agent log. The log is your complete, auditable record of AI operations — what ran, when, on which assessment, and what the outcome was.

### Accessing agent logs

Go to **Agent Logs** in the workspace navigation. You see a searchable, filterable list of all agent runs.

### What each log entry shows

| Field | Description |
|---|---|
| **Agent name** | Which agent ran (e.g., Grading Agent, Rubric Agent) |
| **Assessment** | Which assessment this run belongs to |
| **Submission** | The specific student submission, if applicable |
| **Timestamp** | When the agent ran |
| **Model used** | The AI model that processed this run |
| **Token estimate** | Approximate number of tokens consumed |
| **Cost estimate** | Estimated cost of this agent run |
| **Status** | succeeded, failed, retried, or requires human review |
| **Approval state** | pending, approved, edited, or rejected |
| **Input summary** | Brief description of what was sent to the agent |
| **Output summary** | Brief description of what the agent returned |
| **Estimated time saved** | Estimate of how long this task would have taken without AI |

### Filtering the log

You can filter by:

- Assessment
- Agent type
- Date range
- Status (succeeded / failed / requires review)
- Approval state

### Why the agent log matters

The agent log is the proof of AI-native operation. It shows that real assessments were processed by real agents. It is also your safety net — if an agent run failed, the log shows the failure and lets you retry.

---

## 10. Evidence Dashboard

The evidence dashboard at `/teacher/evidence` gives you a high-level view of all activity across all your assessments.

### What it shows

| Metric | Description |
|---|---|
| **Assessments processed** | Total number of assessment cycles completed or in progress |
| **Graded submissions** | Total number of student submissions analyzed by the Grading Agent |
| **Feedback outputs generated** | Total number of feedback drafts produced |
| **Agent runs** | Total AI agent executions, broken down by agent type |
| **Total estimated cost** | Aggregate estimated AI cost across all runs |
| **Total estimated time saved** | Aggregate teacher time saved estimate |
| **Approval rate** | Percentage of AI outputs approved unchanged vs. edited vs. rejected |

---

## 11. Your Plan and Usage

Usage tracking helps you understand how much of your plan you have consumed.

| Metric | What Counts |
|---|---|
| **Assessments** | Each assessment you create counts against your plan's assessment limit |
| **Graded submissions** | Each student submission analyzed by the Grading Agent (open mode) or each closed assessment attempt processed (closed mode) counts as one graded submission |

When you approach your plan limit, the workspace will show a usage warning. Contact your account operator or plan administrator to upgrade.

---

*[← Managing Students and Results](05-student-management.md) | [Back to User Guide Index](README.md) | [FAQ →](07-faq.md)*
