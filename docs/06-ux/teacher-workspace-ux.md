# Teacher Workspace UX

This document describes the interaction model and design intent for the teacher workspace in GradeOps AI. It is not a pixel-level spec — it establishes what the interface must communicate and how teacher decisions are structured.

---

## Core Interaction Model

The teacher workspace is built around a **review-and-approve loop**, not a creation tool.

Teachers rarely start from nothing. In most workflows, AI agents produce a draft and the teacher responds to it: approve, edit, reject, or regenerate. The UI must make this response primary — not an afterthought.

Every AI-generated output must show:

1. What the agent produced (the suggestion).
2. What the teacher can do with it (approve / edit / reject / regenerate).
3. Evidence of how it was produced (agent name, model, confidence, timestamp).

This is a product-level constraint, not a styling preference.

---

## Dashboard

**Intent:** Give the teacher a quick operational picture of all assessments in flight.

The dashboard should answer, at a glance:

- What assessments are active?
- What is waiting for my approval?
- Which assessments have agent runs in progress?

**Priority elements:**
- Assessment list sorted by activity (most recent action first).
- Status badge per assessment (draft, rubric pending, grading in progress, pending review, approved, reported).
- Count of pending approval items per assessment.
- Quick link to the active approval queue.

**Avoid:** Long lists without context, hidden state, or approval queues buried under navigation.

---

## Assessment Creation (Open Mode)

**Intent:** The teacher defines what they want to evaluate. Agents do the rest.

The intake form should feel like a professional brief, not a search box. The teacher provides:

- Learning goal (free text, primary field).
- Programming topic and language.
- Target level (intro, intermediate, advanced).
- Expected duration.
- Approximate number of students.
- Constraints (allowed resources, prohibited, special notes).
- Existing instructions (optional upload or paste).

The form should not ask for too much. The teacher's brief becomes the agent's context. Fewer fields with good defaults are better than many fields that produce confusion.

**After submission:** A loading state communicates that the Assessment Agent is running, not that the server is slow. Show the agent name.

---

## AI Output Review Screens

Every AI output review screen (rubric, grading suggestion, feedback draft) follows the same structure:

```
[Agent badge: agent name + model + timestamp + status]

[Generated output — readable, not raw JSON]

[Edit field — inline or side-by-side]

[Action buttons: Approve | Edit | Reject | Regenerate]

[Notes field — optional teacher comment]
```

**Key UX rules for these screens:**
- Approve is not the only visible option. Edit must be easy.
- Reject or Regenerate must not feel like admitting failure — they are legitimate workflow steps.
- After approval, the output must clearly change state (visual lock, different styling) so the teacher knows it is finalized.
- Uncertainty flags from the agent should be visible, not hidden. If the Grading Agent flagged a submission as `low_confidence_score`, the teacher should see that before approving.

---

## Submission Upload

**Intent:** Teacher loads student answers for grading.

For the MVP, the teacher is the upload actor. Students do not upload in open mode.

Options:
- Paste text per student (single textarea + student identifier).
- Upload file per student.
- Bulk import if feasible.

The student identifier field is controlled by the teacher — it can be a name, a code, a number, or a pseudonym. The product does not enforce a specific format.

After upload, each submission appears in the grading queue with status `received`.

---

## Grading Review Queue

**Intent:** Teacher reviews AI grading suggestions, one submission at a time or as a list.

Each row/card should show:
- Student identifier.
- Suggested score (X / total).
- Summary of per-criteria scores.
- One or two key flags (if uncertainty flags exist).
- Quick approve button.
- Edit link (opens full review).

The teacher should be able to bulk-approve low-risk submissions and spend more time on flagged ones. The UI should support this — flagged submissions should be visually distinct.

---

## Agent Log Viewer

**Intent:** Make AI operations visible and demo-ready.

The agent log viewer is evidence, not a debug console.

Each log entry should show:
- Agent name and type.
- Assessment and student (if applicable).
- Timestamp.
- Model used.
- Token estimate and cost estimate.
- Status (succeeded / failed / requires review).
- Teacher approval state (pending / approved / edited / rejected).
- Brief input summary and output summary.
- Estimated minutes saved.

**Filters:** By assessment, by agent, by date, by status, by approval state.

The agent log viewer must be presentable in the 3-minute demo. It is the most important proof of AI-native operation.

---

## Evidence Dashboard

**Intent:** Show that the product is running a real business operation, not only generating text.

For the hackathon demo, this screen must show:

- Total assessments run.
- Total graded submissions.
- Total feedback outputs generated.
- Total agent runs (with breakdown by type).
- Total estimated cost.
- Total estimated teacher time saved.
- Approval rate (% approved / edited / rejected).

And ideally:
- Active pilots count.
- Revenue summary (if available).
- Most recent agent activity timeline.

This screen is the closing slide of the demo. It must look credible.

<!-- nav -->

---

← [Screen Inventory](screen-inventory.md) | [↑ inicio](#teacher-workspace-ux) | [Student Access UX →](student-access-ux.md)
