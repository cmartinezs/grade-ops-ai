# Student Access UX

This document describes the UX intent for student-facing screens in GradeOps AI.

Students access assessments and results through signed email links. They do not log in, create accounts, or navigate the product. Every student-facing screen is a single-purpose, ephemeral interaction.

---

## Design Constraints

These are non-negotiable for the MVP:

1. **No account creation.** The student arrives via a link and completes the task. There is no registration, no password, no profile.
2. **No ambient navigation.** There is no menu, no sidebar, no "go back to dashboard." The screen shows exactly what the link was sent to show.
3. **No persistent state beyond the session.** Once the student submits, the attempt is recorded on the server. The browser session can end without data loss.
4. **Link is the identity.** The signed token in the URL identifies the student and the assessment. The system does not need to know anything else.

---

## Assessment Response Screen (Closed Assessments)

**Route:** `/student/assessment/[token]`

### Welcome / Instructions

The first screen a student sees after opening their link.

Must show:
- Assessment title.
- Brief instructions (from teacher-approved content).
- Deadline or available time window (if configured).
- Start button.

Must not show:
- Any mention of GradeOps AI's internal systems.
- Other students' names or identifiers.
- Anything that implies a grading score will appear immediately.

### Response Form

One question per screen, or all questions on a single page — both are acceptable for the MVP. The simpler implementation is all questions on one page.

Each question shows:
- Question stem.
- Answer options (radio for TF/SC, checkboxes for MC).
- Optional question number indicator.

The form does not show:
- Correct/incorrect feedback during the response.
- The answer key or scoring policy.
- Other students' responses.

**Submission:** A single Submit button at the bottom. After submission, the student cannot resubmit unless the teacher explicitly resets the attempt.

### Confirmation

After submit:
- Clear confirmation that the submission was received.
- No score is shown.
- If the teacher has configured a result date: "Results will be available on [date]."
- No call to action. No navigation. The student can close the browser.

---

## Result Access Screen (Closed Assessments)

**Route:** `/student/results/[token]`

This link is sent after the teacher publishes results.

Must show:
- Assessment title.
- Student's total score (X / total or percentage, depending on teacher config).
- Per-item result (correct / incorrect) if the teacher chose to release item-level detail.
- AI-generated or teacher-approved feedback per item (if released).
- No other students' data.

Must not show:
- Answer key or distractors.
- Other students' scores.
- Teacher comments not intended for the student.

**Result link expiry:** The link should remain valid until the teacher archives the assessment. The teacher controls visibility.

---

## Open Assessment: No Student-Facing Form

Open assessments in the MVP do not have a student-facing submission form.

In the MVP, the teacher uploads student submissions directly (paste or file). Student access for open assessments (a form where students can submit their code or text) is a P1 feature — it does not block the MVP workflow but is desirable before pilots that require students to self-submit.

If implemented as P1, the student submission form for open assessments would follow the same UX constraints: token-gated, no account, no navigation, single-purpose.

---

## Language and Tone

Student-facing text must be:
- Clear and direct. Students are not using a teacher tool — they should not see terminology like "rubric," "agent run," or "approval state."
- Neutral in tone. Avoid phrases that imply fear of error or judgment ("warning," "you must," "fail").
- Brief. Students are not reading documentation; they are completing a task.

Teacher-approved feedback text follows the same tone. The Feedback Agent is instructed to produce student-appropriate language; the teacher reviews before delivery.

---

## Accessibility Baseline

For the MVP:
- Ensure radio and checkbox inputs are keyboard-navigable.
- Use sufficient color contrast (WCAG AA minimum).
- Do not rely on color alone to communicate answer state.
- Form labels must be explicitly associated with inputs.

Full WCAG audit is P1; the MVP baseline is sufficient for pilot use.

<!-- nav -->

---

← [Teacher Workspace UX](teacher-workspace-ux.md) | [↑ inicio](#student-access-ux) | [README](README.md)
