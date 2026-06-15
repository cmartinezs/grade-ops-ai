# Security

GradeOps AI handles student submissions, grading suggestions, feedback drafts, reports, and business evidence.

The MVP security posture must be simple but serious.

## Security Objectives

1. Protect student submissions and assessment data.
2. Restrict access by organization and role.
3. Preserve teacher approval over sensitive outputs.
4. Keep agent operations auditable.
5. Avoid leaking secrets or personal data into prompts/logs.
6. Make hackathon evidence credible without exposing private student data.
7. Use production-grade cloud configuration even for small pilots.

## Security Principles

- Minimum necessary data.
- Teacher control over grading/feedback.
- Tenant isolation.
- Server-side model calls only.
- No API keys in frontend.
- Redacted logs.
- Explicit approval states.
- Evidence screenshots should not expose private student content.
- Related-party/revenue evidence should be honest and traceable.

## Data Classification

| Data Type | Sensitivity | Examples | Handling |
| --- | --- | --- | --- |
| Public marketing data | Low | Landing copy, public pitch | Can be public. |
| Account data | Medium | Teacher email, organization | Auth required. |
| Assessment content | Medium | Instructions, rubric, questions | Tenant-restricted. |
| Student submissions | High | Code, answers, identifiers | Minimize, restrict, secure storage. |
| Grading/feedback | High | Scores, feedback, gaps | Teacher approval + restricted access. |
| Student access tokens | Critical | `AssessmentInvitation.token_hash`, result link tokens | Never store plain; hash only; rotate on revoke. |
| Learner data | High | Email, name, external ID | Minimize; tenant-scoped; deletable on request. |
| Paper captures | High | Scanned answer sheets | Private storage; treated as student submission. |
| Agent logs | Medium/High | Model, summaries, cost, errors | Redact and restrict. |
| Billing/revenue evidence | High | Receipts, invoices, customers | Operator/admin only. |
| Secrets | Critical | API keys, tokens | Secret manager/env only. |

## Authentication

MVP options:

| Option | Fit |
| --- | --- |
| Firebase Auth | Fastest for Google-aligned MVP. |
| Backend JWT/session | Good if Spring Boot owns auth. |
| Google OAuth | Useful for teacher convenience. |
| SSO | Out of MVP. |

Minimum requirement: every teacher/operator must authenticate, API must validate authenticated identity, and the frontend must never store Gemini/API secrets.

## Authorization

Recommended MVP roles:

| Role | Access |
| --- | --- |
| `teacher` | Own organization assessments, submissions, grading suggestions, feedback, reports. |
| `operator` | Evidence dashboards, pilot/customer records, logs, usage metrics. |
| `admin` | Organization/account administration. |

Tenant rule:

> A user can only access records belonging to their organization unless explicitly assigned an operator/admin role.

## Teacher Approval Security

Teacher approval is a security and trust control.

High-impact outputs requiring approval:

- rubric;
- grading suggestions;
- feedback drafts;
- learning gap interpretation if used in report;
- recovery activities;
- teacher report if exported/shared.

The system must not auto-finalize grades, auto-send feedback, silently change approved rubrics, hide AI uncertainty flags, or overwrite teacher edits.

## AI Prompt/Data Safety

Allowed prompt context:

- assessment instructions;
- approved rubric;
- submission content required for grading;
- anonymized student identifier;
- teacher preferences;
- prior structured outputs needed for workflow.

Avoid prompt context:

- teacher personal data not needed;
- full student names if anonymized IDs work;
- emails/phone numbers;
- payment records;
- secrets;
- unrelated customer evidence;
- internal API keys;
- raw logs not needed by the model.

## Agent Output Safety

Agent outputs should be validated before storage/use:

- JSON schema validation;
- score bounds validation;
- rubric criteria references must exist;
- total score must be within allowed scale;
- feedback must be student-appropriate;
- uncertainty flags must be preserved;
- invalid output becomes `OUTPUT_VALIDATION_FAILED`.

## Audit And Logging

Technical logs should cover request IDs, errors, latency, service health, failed agent calls, deployment incidents, and authentication errors.

Business/audit logs should cover agent execution logs, approval events, usage events, cost estimates, teacher edits/rejections, and evidence records.

Do not rely only on Cloud Logging for business evidence. Store structured evidence in the application database.

## Log Redaction

| Field | Store |
| --- | --- |
| `input_summary` | Redacted summary. |
| `output_summary` | Redacted summary. |
| `model` | Full model identifier. |
| `tokens/cost` | Full estimate. |
| `submission_content` | Reference to submission, not full copy. |
| `error_message` | Redacted, no secrets. |
| `approval_state` | Full state. |

## File Security

For uploaded submissions and exports:

- store in Cloud Storage or equivalent;
- use private buckets;
- avoid public object URLs;
- use signed URLs only when needed;
- set content-type validation;
- limit file size;
- restrict extensions;
- scan or sanitize if execution is ever introduced;
- do not execute student code in MVP unless sandboxed.

## Student Access Token Security

Access tokens for `AssessmentInvitation` links must follow these rules:

- Token must be generated as a cryptographically random, high-entropy value.
- Only the hash of the token is stored in the database; the plain token is sent in the email link only.
- Each token is scoped to one `learner_ref_id` and one `assessment_id`.
- Tokens have a configurable expiry; default varies by link type.
- After submission, the `assessment_access` token is marked as used and cannot be reused by default.
- Teacher can revoke any token; revocation is immediate.
- The system validates assessment state at token use time: a `result_access` link must not work until results are published.
- Tokens must not appear in server logs; log only `learner_ref_id` and event type.
- Rate limit all student-facing endpoints to prevent token enumeration.
- The "resend link" endpoint must not confirm or deny whether an email exists.
- QR codes on physical answer sheets (P1) must use a signed, tokenized reference, not a plain assessment ID or database primary key.

## Academic Integrity Boundaries

GradeOps AI may identify suspicious patterns only as a cautious flag.

Allowed:

- “This submission requires teacher review.”
- “This answer pattern appears unusually similar to another submission.”
- “The evidence is insufficient to score confidently.”

Avoid plagiarism/cheating accusations or automated punitive actions in the MVP.

## Privacy Baseline

For pilot use:

- use minimal student identifiers;
- allow pseudonyms/student codes;
- avoid unnecessary personal data;
- collect consent if real student data is used beyond teacher workflow;
- avoid public screenshots containing student submissions or grades;
- separate private evidence from public evidence.

## Secret Management

Secrets must never be committed to the repo.

Secrets include Gemini API keys, Google Cloud credentials, database passwords, JWT signing secrets, OAuth client secrets, and payment provider secrets.

Recommended handling:

- environment variables for local development;
- Google Secret Manager or Cloud Run secrets for production;
- `.env.example` only with placeholder names;
- rotate exposed secrets immediately.

## Network And Deployment Security

MVP baseline:

- HTTPS only;
- authenticated API;
- CORS restricted to known frontend domains;
- rate limits for expensive endpoints;
- upload size limits;
- server-side agent calls;
- separate demo/pilot environments if possible;
- no public database.

## Cost Abuse Controls

Because AI calls cost money:

- enforce plan limits;
- limit submissions per assessment;
- cap file size;
- warn on unusually long submissions;
- avoid premium fallback by default;
- rate-limit agent endpoints;
- log every run;
- require operator override for high-volume tests.

## Security Acceptance Criteria

The MVP security baseline is sufficient when teacher access requires authentication, tenant boundaries are enforced, Gemini/API secrets are server-side only, student submissions are private, agent logs are structured and redacted, teacher approval events are stored, high-impact outputs remain pending until approval, uploaded files are private and size/type-limited, cost abuse controls exist, and public demo evidence can be shown without exposing private student data.

<!-- nav -->

---

← [API Design](api-design.md) | [↑ inicio](#security) | [README](README.md) | [Deployment →](deployment.md)
