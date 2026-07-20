# Demo Script

**Format:** Recorded video, under 3 minutes, English.
**Goal:** Prove the business and the operation, not only the interface.
**Tone:** Professional, direct. Show, do not explain.

Refer to the strategic framing in [`00-project/hackathon-strategy.md`](../00-project/hackathon-strategy.md#demo-script-outline) for the high-level outline. This document is the execution version.

---

## Pre-Recording Checklist

Before recording, verify:

- [ ] Demo environment is deployed and reachable at a public URL.
- [ ] Seed data is loaded: at least one teacher account, one complete assessment, 5+ student submissions, visible agent logs.
- [ ] Evidence dashboard shows real or realistic numbers.
- [ ] Agent log viewer shows at least 10 agent run entries.
- [ ] Closed assessment flow has at least one question bank and one published result link.
- [ ] No broken screens, loading errors, or missing data.
- [ ] Screen resolution is clean for recording (1920×1080 or equivalent).
- [ ] Microphone is checked; audio is clear.
- [ ] Recording software is ready.

---

## Scene Breakdown

### Scene 1 — The Problem (0:00–0:20)

**Goal:** Establish the pain point in one sentence. Do not over-explain.

**Spoken script:**
> "Programming educators spend hours every week creating assessments, grading submissions, and writing feedback for every student. GradeOps AI operates that workflow with AI agents."

**Screen:** Static title card or intro screen with the product name and tagline.

*Cut.*

---

### Scene 2 — Product Overview (0:20–0:40)

**Goal:** Explain what the product does in one breath.

**Spoken script:**
> "Teachers define what they want to evaluate. Agents generate the activity, the rubric, the grading suggestions, and the feedback. Teachers review and approve. The whole cycle — from learning goal to published report — runs in one platform."

**Screen:** Dashboard with two or three assessments visible, different statuses (one in grading, one approved, one reported).

*Cut.*

---

### Scene 3 — Create Assessment (0:40–0:55)

**Goal:** Show that a teacher starts from intent, not from a blank page.

**Action:** Navigate to "Create Assessment." Fill in:
- Learning goal: *"Evaluate conditionals, loops, and functions in Java for first-semester students."*
- Level: Intro
- Duration: 90 minutes

Click "Generate."

**Spoken script:**
> "The teacher writes what they want to evaluate. The Assessment Agent generates the activity brief and the Rubric Agent creates the grading criteria."

**Screen:** Form submit → loading state (agent running) → assessment draft appears.

*Cut.*

---

### Scene 4 — Rubric Review and Approval (0:55–1:10)

**Goal:** Show that the teacher is in control — agents assist, teachers approve.

**Action:** Navigate to the rubric. Briefly scroll through criteria and weights. Click "Approve Rubric."

**Spoken script:**
> "The teacher reviews the rubric, edits if needed, and approves. Grading cannot start until the rubric is locked."

**Screen:** Rubric review screen → approval confirmation badge.

*Cut.*

---

### Scene 5 — Grading Suggestions (1:10–1:30)

**Goal:** Show the core value: agents graded the submissions; teacher reviews.

**Action:** Navigate to the grading queue. Show 3–4 student rows with suggested scores. Open one in full view — show the rubric-based breakdown, uncertainty flag if present, and the approve/edit buttons. Approve one.

**Spoken script:**
> "Student submissions are analyzed against the approved rubric. The Grading Agent suggests a score and explains the reasoning. The teacher approves or edits each result."

**Screen:** Grading queue → individual grading card → approval.

*Cut.*

---

### Scene 6 — Feedback Draft (1:30–1:45)

**Goal:** Show personalized feedback output.

**Action:** Navigate to one student's feedback draft. Read two lines of the feedback. Click "Approve Feedback."

**Spoken script:**
> "The Feedback Agent drafts personalized feedback for each student. The teacher approves before it is delivered."

**Screen:** Feedback draft card → approval.

*Cut.*

---

### Scene 7 — Agent Logs (1:45–2:10)

**Goal:** This is the proof of AI-native operations. Give it time.

**Action:** Navigate to the agent log viewer. Show at least 8–10 entries. Filter to show one assessment. Scroll slowly to reveal: agent names, models used, timestamps, token estimates, cost estimates, and approval states.

**Spoken script:**
> "Every agent execution is logged. Agent name, model, timestamp, token usage, estimated cost, and whether the teacher approved, edited, or rejected the output. This is not a demo mode — this is the live product."

**Screen:** Agent log table, clearly readable.

*Cut.*

---

### Scene 8 — Evidence Dashboard (2:10–2:35)

**Goal:** Close the loop: this is a business, not a prototype.

**Action:** Navigate to the evidence dashboard. Show: total assessments, submissions processed, feedback outputs, agent runs, estimated time saved, estimated cost per submission. If revenue evidence is available, show it.

**Spoken script:**
> "The product tracks usage, cost, and operational evidence from every session. GradeOps AI is not a prototype — it is an AI-operated assessment business."

**Screen:** Evidence dashboard with real or realistic numbers.

*Cut.*

---

### Scene 9 — Close (2:35–3:00)

**Goal:** Leave the judges with the product's position.

**Spoken script:**
> "GradeOps AI gives programming educators the assessment operations capacity of a larger academic team — with AI agents handling the workflow and teachers staying in control of every decision that matters."

**Screen:** Return to dashboard or product logo.

*End.*

---

## Recording Notes

- Do not read the script word for word. Use it as structure; speak naturally.
- Keep mouse movement deliberate — slow scrolls, no erratic movement.
- If a screen takes more than 3 seconds to load, cut after submission and resume after load.
- Show real data where possible; clearly staged demo data where necessary.
- Captions are not required but improve accessibility and should be considered if time permits.
- Upload to a stable host (YouTube unlisted or Vimeo) before submitting the Devpost link.
