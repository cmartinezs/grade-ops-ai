# Reviewing AI Outputs — The Approval Model

GradeOps AI operates on a simple principle: AI agents generate and suggest, you decide. This guide explains how the approval model works, what specifically requires your action, and how to use the review queue effectively.

---

## 1. The Principle

Every AI output in GradeOps AI is a suggestion until you act on it.

No grade is finalized. No feedback reaches a student. No report is published. No gap is confirmed. None of these things happen without your explicit approval. The AI agents do the heavy lifting — they analyze, draft, summarize, and propose — but they cannot take any action that has a consequence for a student or for the integrity of your assessment without your sign-off.

This is not a limitation of the AI. It is a deliberate design choice. You are the pedagogical authority. The AI is your assistant.

---

## 2. What Requires Your Approval

| Output | When It Appears | Consequence if Approved |
|---|---|---|
| **Assessment draft** | After the Assessment Agent runs on your brief | Draft is locked; rubric generation becomes available |
| **Rubric** | After the Rubric Agent drafts grading criteria | Rubric is locked as the permanent grading standard; AI grading can begin |
| **Grading suggestions** | After the Grading Agent analyzes a submission | Score becomes the confirmed result for that student |
| **Feedback drafts** | After the Feedback Agent writes personalized feedback | Feedback is available for delivery and export |
| **Learning gaps** | After the Learning Gap Agent summarizes cohort patterns | Gap is confirmed and included in the teacher report |
| **Recovery activities** | After the Recovery Agent proposes remedial exercises | Activity is confirmed and included in the teacher report |
| **Teacher report** | After the Teacher Report Agent compiles the assessment summary | Report is finalized and can be exported |
| **Question quality** (closed) | After Distractor Quality and Ambiguity Review Agents flag issues | Question is approved into the active bank |
| **Assessment composition** (closed) | After the Assembly Agent proposes a question set | Composition is locked; publication becomes available |
| **Grade results** (closed) | After deterministic grading and exception review | Results are published to students |

---

## 3. The Review Queue

Each assessment has a **Pending approvals** count on the dashboard. This number tells you how many AI outputs are waiting for your action. When it is zero, you are up to date.

To access the review queue for an assessment:

1. Click the assessment card on the dashboard.
2. Navigate to the relevant tab (Rubric, Grading, Feedback, Gaps, etc.).
3. Items awaiting your review are shown in a queue, ordered by priority.

Each item in the queue shows:

- What the agent produced (the suggestion or draft)
- Which agent produced it, which model it used, and when
- Any uncertainty or quality flags
- Your available actions

You can navigate through items one at a time or view them as a list with inline quick-approve options.

---

## 4. The Three Actions

For any AI output, you have three options:

### Approve

You agree with what the agent produced. The output moves to confirmed state. For grading suggestions, this finalizes the score for that submission. For feedback, this makes it available for delivery.

Use **Approve** when:
- You have read the output carefully
- You agree it is accurate
- There are no uncertainty flags, or you have resolved them

### Edit then Approve

You accept the output in principle but want to make changes. Click **Edit** to modify the content inline. Your changes are saved, and then you approve the edited version.

Use **Edit then Approve** when:
- The output is mostly correct but one detail is wrong
- You want to adjust the tone or phrasing of feedback
- You want to change a score up or down from the AI suggestion
- You want to add a teacher comment or note

When you edit an AI output, your edit is logged with your identity, the timestamp, and the original AI suggestion. The audit trail shows what the AI proposed and what you changed it to.

### Reject

You disagree with the output entirely. It is discarded and the item is marked for regeneration or manual handling.

Use **Reject** when:
- The output is fundamentally wrong or inappropriate
- The AI missed the point entirely
- You prefer to handle this item manually

Rejected outputs remain in the audit trail — they are not deleted. The final report notes how many outputs were rejected vs. approved vs. edited.

---

## 5. Uncertainty Flags

Uncertainty flags are signals from the AI that it is not confident about a particular output. Flagged items require more careful review before you act on them.

### Flag types and their meaning

| Flag | What It Means | Recommended Action |
|---|---|---|
| `ambiguous_submission` | The student's submission is unclear or doesn't follow expected patterns | Read the submission carefully before approving or editing the score |
| `low_confidence_score` | The agent's score for a criterion may be unreliable | Review the evidence summary and override if needed |
| `incomplete_submission` | The submission appears to be missing required content | Check whether the student actually submitted everything; consider contacting the student |
| `requires_human_review` | The agent recommends direct teacher judgment for this item | Do not approve without carefully reading the full submission |
| `weak_distractor` (closed) | An incorrect alternative is too obviously wrong | Edit the question before approving to bank |
| `ambiguous_stem` (closed) | The question stem is open to more than one interpretation | Rewrite the stem before approving |
| `double_valid_answer` (closed) | More than one alternative may be defensibly correct | Correct the answer key or rewrite the alternatives before approving |

### What flags prevent

A submission with any uncertainty flag **cannot be bulk-approved**. You must open it individually and review it before taking any action. This is a hard constraint — it is not possible to click "approve all" and skip flagged items.

---

## 6. Editing AI Outputs

You can always change AI-generated content. There is no limit to how much you edit.

What you can change:

- Scores on any rubric criterion (up or down)
- Evidence summaries and notes
- Any section of a feedback draft
- Gap severity ratings and descriptions
- Recovery activity instructions
- Report summary text

What is tracked:

- Your identity (teacher account)
- The timestamp of the edit
- The original AI-generated value
- The value you entered

Your edits are credited to you in the audit trail. The report distinguishes between AI-generated outputs that were approved unchanged and outputs that were edited by the teacher.

---

## 7. What You Cannot Do

These actions are intentionally blocked to protect assessment integrity:

| Blocked Action | Why |
|---|---|
| **Silently change an approved rubric mid-grading** | All submissions must be graded against the same rubric. Changing criteria after grading has started would make earlier approvals inconsistent with later ones. Any change creates a new rubric version with an explicit acknowledgment. |
| **Auto-deliver feedback to students** | In open mode, no feedback reaches students automatically. You control how and when feedback is shared. |
| **Skip the approval step for grades** | Grades are not finalized by agent output alone. Your approval is required. |
| **Bulk-approve flagged items** | Uncertainty-flagged items must be reviewed individually. |
| **Publish closed results without reviewing exceptions** | The exception queue must be resolved before publication. |

---

## 8. Bulk Approval

Bulk approval lets you approve multiple grading suggestions at once. It is available only for submissions that have no uncertainty flags.

### When bulk approval is safe

- You have reviewed a representative sample of suggestions from the same assessment
- All items in the bulk set are flag-free
- The rubric was clear and well-structured, reducing the likelihood of systematic AI errors
- The assessment had relatively straightforward tasks where AI confidence tends to be high

### When to avoid bulk approval

- Any item in the set has an uncertainty flag
- The assessment involved complex or ambiguous tasks
- You have not reviewed any individual items from this batch yet
- The submission content is in a language or format the AI may have struggled with

A reasonable approach: review 3–5 individual suggestions first to calibrate your confidence in the AI's performance on this assessment, then use bulk approval for the remaining flag-free items.

---

## 9. The Audit Trail

Every approval, edit, and rejection is permanently logged. The audit trail is your record of academic integrity.

Each log entry contains:

| Field | Description |
|---|---|
| **Timestamp** | When the action was taken |
| **Teacher** | Your account identity |
| **Action type** | Approved, edited, rejected, or regenerated |
| **Output type** | Assessment draft, rubric, grading suggestion, feedback, gap, recovery, report |
| **Original AI value** | What the agent produced |
| **Final value** | What was confirmed (your edit or the original, if approved unchanged) |
| **Notes** | Any comments you added |

You can view the audit trail for any assessment through the Agent Log Viewer. See [Dashboard and Workspace](06-dashboard-and-workspace.md) for details.

---

*[← Closed Assessment Workflow](03-closed-assessment-cycle.md) | [Back to User Guide Index](README.md) | [Managing Students and Results →](05-student-management.md)*
