# API Design

GradeOps AI API should be simple, auditable, and workflow-oriented.

The API must support the MVP assessment lifecycle, agent orchestration, teacher review, evidence extraction, and plan/usage tracking.

## API Design Principles

1. Prefer workflow commands over broad automation endpoints.
2. Keep teacher approval explicit.
3. Return structured IDs for every generated artifact.
4. Make agent operations observable.
5. Keep student-facing outputs pending until approved.
6. Separate internal evidence APIs from teacher-facing APIs.
7. Make every mutation auditable.
8. Support demo and pilot use before enterprise completeness.

## API Style

Recommended MVP style:

- REST API;
- JSON request/response;
- authenticated teacher/operator access;
- server-side Gemini calls only;
- idempotency support for long-running commands where possible;
- pagination for list endpoints;
- consistent error response.

Base path:

```text
/api/v1
```

## Student Submission API Meaning

In this API, `/submissions` means **student submissions**: answers, code, files, attempts, or delivered work loaded by the teacher for a specific assessment.

It does not imply student accounts or a student portal.

A submission becomes a **graded submission** for pricing/usage when it is analyzed by the grading/feedback workflow.

## Resource Overview

| Resource | Purpose |
| --- | --- |
| `/auth/me` | Current authenticated user. |
| `/organizations` | Customer/account boundary. |
| `/assessments` | Assessment lifecycle. |
| `/rubrics` | Rubric versions and approval. |
| `/submissions` | Student submissions. |
| `/grading-suggestions` | AI grading suggestions. |
| `/feedback-drafts` | Student feedback drafts. |
| `/learning-gaps` | Cohort gap summaries. |
| `/recovery-activities` | Remedial suggestions. |
| `/teacher-reports` | Assessment reports. |
| `/agent-runs` | Agent execution logs. |
| `/evidence` | Business/demo evidence. |
| `/usage` | Plan and usage metrics. |

## Common Response Envelope

```json
{
  "data": {},
  "meta": {
    "requestId": "req_123",
    "timestamp": "2026-06-08T12:00:00Z"
  }
}
```

## Common Error Envelope

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Rubric must be approved before grading starts.",
    "details": []
  },
  "meta": {
    "requestId": "req_123",
    "timestamp": "2026-06-08T12:00:00Z"
  }
}
```

## Error Codes

| Code | Meaning |
| --- | --- |
| `VALIDATION_ERROR` | Invalid request or missing field. |
| `UNAUTHORIZED` | Not authenticated. |
| `FORBIDDEN` | Authenticated but not allowed. |
| `NOT_FOUND` | Resource not found in tenant scope. |
| `INVALID_STATE_TRANSITION` | Workflow command not allowed in current state. |
| `AGENT_RUN_FAILED` | Agent execution failed. |
| `OUTPUT_VALIDATION_FAILED` | Model output did not match schema. |
| `USAGE_LIMIT_EXCEEDED` | Plan/usage limit reached. |
| `FILE_UNSUPPORTED` | Unsupported upload. |
| `CONFLICT` | Version or state conflict. |

## Authentication

### `GET /auth/me`

Returns current user and organization.

```json
{
  "data": {
    "userId": "usr_123",
    "organizationId": "org_123",
    "email": "teacher@example.com",
    "role": "teacher",
    "plan": "pilot_pack"
  }
}
```

## Assessments API

### `POST /assessments`

Creates assessment brief.

```json
{
  "learningGoal": "Evaluate conditionals, loops, and functions",
  "topic": "Java basics",
  "language": "Java",
  "level": "basic",
  "durationMinutes": 90,
  "studentCountEstimate": 30,
  "constraints": ["No arrays", "Console input/output"],
  "teacherNotes": "First-semester students"
}
```

### `POST /assessments/{assessmentId}/generate-draft`

Runs Assessment Agent and returns a reviewable draft plus `agentRunId`.

### `GET /assessments/{assessmentId}`

Returns assessment detail, current state, and summary counts.

### `PATCH /assessments/{assessmentId}`

Updates teacher-editable assessment fields.

### `POST /assessments/{assessmentId}/approve`

Approves assessment draft.

## Rubrics API

### `POST /assessments/{assessmentId}/rubrics/generate`

Runs Rubric Agent.

Precondition: assessment exists and assessment draft is available.

### `PATCH /rubrics/{rubricId}`

Teacher edits rubric.

### `POST /rubrics/{rubricId}/approve`

Teacher approves rubric. This stores approval event and moves assessment to `ready_for_submissions`.

## Student Submissions API

### `POST /assessments/{assessmentId}/submissions`

Creates a student submission by text/code.

```json
{
  "studentIdentifier": "A001",
  "contentText": "public class Main { ... }",
  "language": "Java"
}
```

### `POST /assessments/{assessmentId}/submissions/upload`

Uploads one student answer file. MVP may implement multipart upload or pre-signed upload flow.

### `GET /assessments/{assessmentId}/submissions`

Lists submissions with `status`, `page`, and `size` filters.

### `GET /submissions/{submissionId}`

Returns one student submission with current processing state.

## Grading API

### `POST /assessments/{assessmentId}/grading-runs`

Starts grading for selected or all student submissions.

Preconditions:

- rubric approved;
- student submissions exist.

```json
{
  "submissionIds": ["sub_001", "sub_002"],
  "mode": "standard"
}
```

### `GET /assessments/{assessmentId}/grading-suggestions`

Returns teacher review queue.

### `POST /grading-suggestions/{suggestionId}/approve`

Teacher approves AI suggestion.

### `PATCH /grading-suggestions/{suggestionId}`

Teacher edits score/notes.

### `POST /grading-suggestions/{suggestionId}/reject`

Teacher rejects AI suggestion.

## Feedback API

### `POST /assessments/{assessmentId}/feedback-drafts/generate`

Runs Feedback Agent for selected submissions.

```json
{
  "submissionIds": ["sub_001"],
  "tone": "supportive",
  "useTeacherApprovedScoresOnly": true
}
```

### `GET /assessments/{assessmentId}/feedback-drafts`

Lists feedback drafts.

### `POST /feedback-drafts/{feedbackId}/approve`

Approves feedback.

### `PATCH /feedback-drafts/{feedbackId}`

Teacher edits feedback.

### `POST /feedback-drafts/{feedbackId}/reject`

Rejects feedback draft.

## Learning Gap API

### `POST /assessments/{assessmentId}/learning-gaps/generate`

Runs Learning Gap Agent.

### `GET /assessments/{assessmentId}/learning-gaps`

Lists gap summaries.

### `POST /learning-gaps/{gapId}/approve`

Teacher confirms gap.

## Recovery Activities API

### `POST /assessments/{assessmentId}/recovery-activities/generate`

Runs Recovery Agent using learning gaps.

### `GET /assessments/{assessmentId}/recovery-activities`

Lists suggested recovery activities.

### `POST /recovery-activities/{activityId}/approve`

Approves recovery activity.

## Teacher Report API

### `POST /assessments/{assessmentId}/teacher-report/generate`

Runs Teacher Report Agent.

### `GET /assessments/{assessmentId}/teacher-report`

Returns report.

### `POST /teacher-reports/{reportId}/approve`

Approves report.

### `GET /teacher-reports/{reportId}/export`

Returns export or artifact link.

## Agent Runs API

### `GET /agent-runs`

Operator/teacher endpoint for traceability.

Query params: `assessmentId`, `submissionId`, `agentName`, `status`, `from`, `to`.

### `GET /agent-runs/{agentRunId}`

Returns agent execution detail with model, operation, status, estimated cost, and approval state.

## Evidence API

### `GET /evidence/dashboard`

Returns hackathon/demo metrics:

- assessments processed;
- submissions processed;
- feedback outputs;
- agent runs;
- estimated cost;
- revenue references;
- related-party revenue summary;
- testimonials count;
- time saved estimate.

### `POST /evidence/customer`

Creates or updates customer evidence metadata.

### `POST /evidence/revenue-events`

Stores revenue/commitment record or evidence link.

### `POST /evidence/cost-events`

Stores manual cost record if not derived automatically.

## Usage API

### `GET /usage/current`

Returns plan usage.

```json
{
  "data": {
    "plan": "teacher_pro",
    "assessmentsUsed": 4,
    "assessmentsLimit": 10,
    "submissionsUsed": 87,
    "submissionsLimit": 300
  }
}
```

## Long-Running Operations

For batch operations:

1. command endpoint returns run ID;
2. UI polls run status;
3. backend stores partial results;
4. failures are visible.

## API State Rules

| Command | Required State |
| --- | --- |
| Generate rubric | Assessment draft exists. |
| Approve rubric | Rubric pending review. |
| Add submissions | Assessment `ready_for_submissions` or later. |
| Start grading | Approved rubric + submissions. |
| Generate feedback | Grading suggestions exist. |
| Approve feedback | Feedback draft exists. |
| Generate report | Grading/feedback/gap data exists. |
| Export report | Report exists. |

## API Acceptance Criteria

The API is sufficient when it supports end-to-end assessment creation, rubric generation and approval, submission intake, grading agent execution, teacher review, feedback approval, learning gap/recovery generation, teacher report generation, agent log retrieval, evidence dashboard retrieval, and usage limit tracking.

<!-- nav -->

---

← [Data Model](data-model.md) | [↑ inicio](#api-design) | [README](README.md) | [Security →](security.md)
