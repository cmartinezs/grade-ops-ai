# Frequently Asked Questions

---

## Account and Access

### What if I did not receive the verification email?

First, check your spam or junk mail folder. Verification emails sometimes land there. If it is not there, wait two minutes and then return to the verification screen and click **Resend verification email**. The resend is rate-limited, so if you click it multiple times quickly, you may need to wait a few minutes before the next resend is available.

If the email still does not arrive after 10 minutes, verify that you registered with the correct address. If you made a typo during registration, register again with the correct email.

---

### Can I change my email address?

Email address changes are not available through the workspace in the current version. Contact your organization's administrator or GradeOps AI support to request an email update.

---

### What happens if I am logged out in the middle of grading?

Your session expires, and you are redirected to `/login` with a message confirming the session ended. Any grading suggestion you had approved and saved before the session expired is preserved — approvals are stored server-side immediately. Any unsaved changes in a form you were actively editing may need to be re-entered.

Sign back in and navigate to the assessment. The grading queue will show which items you have already reviewed and which are still pending.

---

### Can I share my account with a colleague?

No. Each account is scoped to one teacher's assessments. Sharing credentials creates security and audit problems — your account identity is used to attribute every approval, edit, and rejection to you. If a colleague needs their own access, they can self-register at `/register` or be provisioned by your organization's admin.

---

### Can students log in to GradeOps AI?

No. Students do not have accounts in GradeOps AI. For closed assessments, students access the platform through a unique signed link sent to their email — the link is their identity for that assessment. For open assessments, you upload their submissions directly. There is no student login screen, no student password, and no student registration.

---

## Open Assessments

### Can I edit the rubric after grading has started?

No, not silently. The approved rubric is the permanent grading standard for that assessment. If you need to change a criterion after grading has started, you must explicitly create a new rubric version. This requires an acknowledgment because changing the rubric mid-grading would make earlier approved suggestions inconsistent with later ones. All existing approvals reference the previous version of the rubric.

If you are still in the rubric review stage and have not yet started grading, you can edit freely before approving.

---

### What does an "AI uncertainty flag" mean, and when should I override the AI?

An uncertainty flag is the AI's way of saying "I'm not fully confident about this — please check." The most common flags are:

- `low_confidence_score` — The AI gave a score but is not sure it is right for this criterion.
- `ambiguous_submission` — The submission does not follow expected patterns (e.g., the code does not compile, the student answered a different question, the content is in an unexpected format).
- `incomplete_submission` — The submission is missing content the rubric expects.

When you see a flag, open the full review for that submission and read the evidence summary. Then decide for yourself. If you agree with the AI score despite the flag, approve it. If you think the score is wrong, edit it. The flag is information, not a block.

---

### Can I grade some submissions manually without using AI?

Yes. You do not have to run the Grading Agent on every submission. If you prefer to score a specific submission yourself, add the submission, skip the AI grading step for that item, and enter your own score directly. You can mix AI-graded and manually-graded submissions within the same assessment.

---

### What file formats can students submit?

In open mode, you upload submissions on the students' behalf. Supported upload formats are: `.txt`, `.java`, `.py`, `.js`, `.ts`, `.html`, `.css`, `.md`. You can also paste code or text directly into the submission form without uploading a file, which works for any text content regardless of format.

If a student submitted a format not on this list (e.g., a `.pdf` or a `.zip`), copy the relevant content into a supported format before uploading, or paste the text directly.

---

### How many students can I grade per assessment?

The number of students per assessment is not technically limited per assessment, but it is constrained by your plan's **graded submissions** limit. Each student submission analyzed by the Grading Agent counts as one graded submission toward your plan. Check your evidence dashboard to see your current usage.

---

### What if a student submitted the wrong file?

Delete the incorrect submission record from the Submissions tab and add a new one with the correct content. As long as grading has not yet been run for that submission, there is no impact. If grading was already run on the wrong file, delete the submission, add the correct one, and run grading again for that submission.

---

### Can I re-run grading if I changed the rubric?

No — the rubric is locked after you approve it. You cannot change an approved rubric and re-run grading against it, because doing so would retroactively change the standard against which earlier submissions were already reviewed. If you want to grade with a different rubric, create a new assessment.

---

## Closed Assessments

### Can students retake a closed assessment?

By default, no. Each access link is valid for one submission. Once a student submits, the link is marked as used and they cannot resubmit.

If you need to allow a specific student to retake the assessment (for example, due to a technical problem on their end), you can revoke their original invitation and send them a new one. Their previous attempt will need to be handled separately in the exception review queue.

---

### What happens if a student's link expires before they submit?

If the link expires before the student submits, they will see an access-denied message when they try to use it. You can resend a new invitation link from the Learners tab. The new link resets the expiry window.

---

### Can I change questions after students have started responding?

No. Once the assessment is published, the snapshot is frozen. You cannot change any question, alternative, or answer key after publication. This is by design — changing questions after some students have responded would make the results unfair and the data unreliable.

If you discover an error in a question after students have started, the correct action is to annul that question (in the exception review queue), which removes it from the scoring calculation for all students and recalculates everyone's score. The annulment is logged with your reason.

---

### How is the grade calculated when I use the penalty scoring mode?

In penalty mode, each incorrect answer deducts a configured number of points. For example, with a standard penalty of 0.25 points per wrong answer:

- A question worth 1 point: correct = +1.00, incorrect = −0.25, blank = 0.00
- Total score = sum of all (correct answers earned − penalties from wrong answers)
- The minimum score is zero — penalty scoring cannot produce a negative total

This mode discourages random guessing. Students who leave answers blank do not lose points; students who guess incorrectly do. You configure the penalty amount when setting the scoring policy.

---

### Can I print the questions for a paper-based exam?

GradeOps AI does not include a built-in print/export feature for question sets in the current version. If you need a printable version, you can manually copy the questions from the composition review screen into a document. Grading paper-based responses by scanning is not supported in this version — paper OMR intake is a future feature.

---

## AI and Approvals

### Does GradeOps AI store my students' work on the internet?

Your students' submissions and responses are stored on GradeOps AI's infrastructure (Google Cloud). They are scoped to your organization — no other teacher or organization can see them. They are not used to train AI models. If you are running a pilot or demo, student data can be anonymized or deleted on request at the end of the engagement.

Do not include personally identifying student information (full name, national ID, etc.) in submission content unless your organization's policy permits it.

---

### Who sees my assessment content?

Your assessment content — the brief, drafts, rubric, submissions, feedback, and reports — is visible only to your account within your organization. GradeOps AI staff may access data for support purposes under the terms of service. Your content is not shared with other customers, used in AI training, or published.

---

### Can I reject AI grading and score manually?

Yes, at any time. If you reject a grading suggestion for a submission, the submission is marked as needing manual scoring. You can enter a score directly, without AI involvement. Your manually entered score is treated identically to an approved AI suggestion in the final report.

---

### How does the AI know how to grade? Does it know my rubric?

Yes. The Grading Agent receives your approved rubric as part of every grading request. The rubric criteria, weights, and performance level descriptions are included in the context the agent uses to analyze each submission. The agent does not make up grading criteria — it works from the rubric you approved.

This is why rubric quality matters. A specific, clearly written rubric with measurable criteria produces more reliable grading suggestions than a vague one.

---

### What if the AI feedback is inappropriate or incorrect?

Do not approve it. Edit it or reject it. Every feedback draft requires your explicit approval before it can be delivered to or exported for a student. If feedback is inaccurate, contains incorrect information about the student's submission, or is inappropriate in tone, click **Edit**, correct the text, and then approve your revised version.

If you notice a pattern — for example, the Feedback Agent consistently misjudges a particular type of submission — you can note this when rejecting and adjust your rubric language to give the agent better context for future assessments.

---

## Plans and Usage

### What counts as a "graded submission" for billing purposes?

One graded submission is one student answer analyzed by the Grading Agent.

- **Open mode:** Each student submission you send through the Grading Agent counts as one graded submission, regardless of how many rubric criteria it covers.
- **Closed mode:** Each `AssessmentAttempt` (one student's complete response) that is processed by the deterministic grading engine counts as one graded submission.

Adding a submission to the system without running grading does not consume a graded submission. Test submissions you create for demo or QA purposes also count if the agent processes them.

---

### What happens when I reach my plan limit?

When you reach your plan's graded submission or assessment limit, you will see a warning in the workspace. You can still access existing assessments and view approved outputs, but you cannot run new agent grading or create new assessments until your limit is increased.

Contact your organization's administrator or GradeOps AI support to discuss upgrading your plan. Plan upgrades take effect immediately.

---

*[← Dashboard and Workspace](06-dashboard-and-workspace.md) | [Back to User Guide Index](README.md)*
