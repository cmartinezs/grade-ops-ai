# Managing Students and Results

GradeOps AI handles students differently depending on the assessment mode. In open mode, students are represented as submission records you create. In closed mode, you maintain a lightweight learner list and the system delivers access links to each student. In neither mode do students create accounts or log in to GradeOps AI.

---

## 1. Open Assessments — Students as Submission Records

In open mode, "students" do not exist as entities in the system. What exists is a **submission record**: a piece of student work (code, text, or a file) that you have loaded into the assessment.

The student identifier you assign to each submission is entirely your choice. You can use:

- Real student names ("Maria Garcia")
- Codes ("S01", "S02")
- Aliases or pseudonyms ("Student A", "Blue team")
- External roster IDs from your institution

The system does not validate or require any specific format. The identifier is a label for your reference and appears on grading suggestions, feedback drafts, and the teacher report.

**No student account is involved.** You upload the submissions; the AI grades them; you review the results.

---

## 2. Closed Assessments — The Learner Registry

For closed mode, you need to tell the system who to invite so it can deliver signed access links. The **Learner Registry** is your list of learner references (called `LearnerRef` in the system) for your organization.

A learner reference is not a user account. It is a minimal record used for link delivery and result tracking.

| Field | Required | Description |
|---|---|---|
| **Email address** | Yes | The address where access and result links are sent |
| **Display name** | No | An optional name for your reference (not shown to other learners) |
| **External roster ID** | No | An optional code from your LMS, student information system, or roster |

You can use pseudonyms or generic identifiers instead of real names. Only the email address is required for link delivery.

---

## 3. Adding Learners

### Adding learners one at a time

1. Go to the **Learners** tab for the assessment.
2. Click **Add learner**.
3. Enter the email address. Optionally add a display name and external ID.
4. Click **Save**.

The learner is added to the list with status **pending** (invitation not yet sent).

### Bulk import

You can import multiple learners at once using a CSV file:

1. Click **Import from CSV**.
2. Download the CSV template if needed. The template has three columns: `email`, `display_name` (optional), `external_id` (optional).
3. Upload your completed CSV.
4. Review the preview — the system shows how many learners will be added and flags any rows with missing emails or duplicate entries.
5. Confirm the import.

---

## 4. Sending Invitations

Learners do not receive access links automatically when you add them. You send invitations explicitly.

### To send invitations

1. On the Learners tab, select the learners you want to invite (or use **Select all**).
2. Click **Send invitations**.
3. The system generates a unique signed token for each learner and dispatches an email containing their access link.

Each access link is:
- Scoped to one learner and one assessment (no other learner can use it)
- High-entropy and resistant to brute-force guessing
- Token stored only as a hash (a copy of the plain link cannot be retrieved from the database)
- Valid until the learner submits or you revoke it

### Invitation timing

You can send invitations at any time after publishing the assessment. You do not have to send all invitations at once — you can add learners later and send additional invitations.

---

## 5. Monitoring Access

The learner list shows the current status for each student throughout the assessment lifecycle.

| Status | Meaning |
|---|---|
| **pending** | Learner added but no invitation sent yet |
| **sent** | Invitation email dispatched |
| **accessed** | Learner opened the access link |
| **submitted** | Learner completed and submitted the assessment |
| **graded** | Automatic grading complete |
| **results_published** | Result link is active; learner can see their results |

---

## 6. Resending a Link

If a learner reports that they did not receive their invitation:

1. Find the learner on the list.
2. Check their status. If it shows **sent** but they say they did not receive it, ask them to check their spam folder and wait up to 5 minutes.
3. If the link is still not found, click **Resend invitation** for that learner.
4. A new email is dispatched. The previous link is invalidated.

A learner can also request a link resend themselves by entering their email address in the "resend my link" form on the student-facing portal. The system sends a new magic link if a result has been published for that email address (the response does not confirm or deny whether an email exists, to protect your roster).

---

## 7. Revoking Access

You can revoke any token immediately. This is useful if:

- A learner shared their link with another student
- A learner needs to be excluded from the assessment
- A link was sent to the wrong email address

### To revoke access

1. Find the learner on the list.
2. Click **Revoke access**.
3. Confirm the revocation.
4. The token is invalidated immediately. If the learner tries to use the link after revocation, they will see an access-denied message.

Revocation is irreversible. To restore access, send a new invitation to the learner.

---

## 8. Publishing Results

After grading is complete and exceptions are resolved, you publish results to make them visible to students.

### Before publishing

- Verify that all expected students have a grade.
- Check that any manual overrides or exclusions are correctly recorded.
- Review the grade summary if available.

### Publishing

1. Go to the **Results** tab for the assessment.
2. Review the list of grades.
3. Click **Publish results**.
4. Confirm the action.

Publishing sends each learner their result access link via email. This is an explicit, deliberate action — results do not become visible to students until you publish.

### What students see after publishing

When a student opens their result link:

| Information | Shown by Default | Configurable |
|---|---|---|
| Assessment title | Yes | No |
| Total score | Yes | No |
| Final grade | Yes | No |
| Per-item result (correct / incorrect) | No | Yes — you enable it |
| Teacher-approved feedback | No | Yes — you enable it |
| Correct answers for incorrect questions | No | Yes — you enable it |
| Other students' results | Never | Not available |

Students cannot navigate to other assessments or see any teacher-facing information.

---

## 9. Privacy Principles

GradeOps AI uses minimal student data by design.

| Principle | What It Means in Practice |
|---|---|
| **Minimal data** | Only email, optional display name, and optional roster ID are stored. No student password, no profile, no account. |
| **Pseudonyms are supported** | You can use codes or aliases instead of real names. The system does not require real names in any field. |
| **Tenant-scoped isolation** | Your students' submissions, results, and learner records are visible only to you. No other teacher can access your data. |
| **No public exposure** | Student data does not appear in public-facing content. Do not include student names or submission content in screenshots used for demo or marketing purposes. |
| **Token security** | Access tokens are stored as hashes. The plain link sent to a student cannot be retrieved from the database. |
| **Access events are logged** | Every time a student accesses an assessment or result link, the event is logged with the learner reference ID and event type (not the token itself). |
| **Data deletion on request** | Student data can be anonymized or deleted at the end of a pilot if requested. |

---

*[← Reviewing AI Outputs](04-reviewing-ai-outputs.md) | [Back to User Guide Index](README.md) | [Dashboard and Workspace →](06-dashboard-and-workspace.md)*
