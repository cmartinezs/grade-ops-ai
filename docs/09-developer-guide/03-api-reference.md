# API Reference

This document covers the GradeOps AI REST API. Section 1 documents endpoints that are currently implemented (Epic 01). Section 2 summarizes planned endpoints for Epics 02–13.

---

## Common headers and authentication

The API uses two independent authentication mechanisms:

### Teacher endpoints — Firebase ID token

All teacher-facing endpoints (outside `/internal/**`) require:

```
Authorization: Bearer <Firebase_ID_Token>
```

The token is a short-lived JWT issued by Firebase after the user signs in on the client. It is valid for one hour. The API verifies every token server-side using the Firebase Admin SDK with `checkRevoked=true`, which detects tokens whose refresh tokens have been revoked (e.g., after sign-out).

**Getting a token for manual testing:**

Option 1 — From a running browser session (browser console):
```javascript
const token = await firebase.auth().currentUser.getIdToken(true);
console.log(token);
```

Option 2 — Via the Firebase REST API (Identity Toolkit):
```bash
curl -X POST \
  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=YOUR_FIREBASE_WEB_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@example.com",
    "password": "yourpassword",
    "returnSecureToken": true
  }'
```

Extract `idToken` from the response JSON and use it as the Bearer token.

### Internal endpoints — shared secret header

Operator endpoints under `/internal/**` use a pre-shared secret key:

```
X-Internal-Key: <value of app.internal.secret>
```

For local development this is `dev-secret-change-me` (from `application-local.yml`). In production it is stored in Google Secret Manager and injected as `INTERNAL_API_SECRET`.

**These endpoints are not authenticated with Firebase tokens.** A request to `/internal/**` with a valid Firebase token but no (or wrong) `X-Internal-Key` header returns HTTP 403.

---

## Error response format

All errors return JSON. The exact shape depends on the error type:

**Standard errors:**
```json
{ "error": "ERROR_CODE" }
```

**Resource not found:**
```json
{ "error": "NOT_FOUND", "resource": "resource-id-or-uid" }
```

**Duplicate email:**
```json
{ "error": "EMAIL_ALREADY_EXISTS", "email": "duplicate@example.com" }
```

**Email not verified (from filter, not controller advice):**
```json
{ "error": "EMAIL_NOT_VERIFIED" }
```

**Internal auth failure (from filter, not controller advice):**
```json
{ "error": "FORBIDDEN" }
```

### Error code reference

| HTTP status | Error code | When |
|-------------|-----------|------|
| 400 | (varies) | Malformed request body |
| 401 | `INVALID_TOKEN` | Firebase ID token is invalid or expired |
| 401 | `EMAIL_NOT_VERIFIED` | Token valid but user has not verified email |
| 403 | `FORBIDDEN` | `X-Internal-Key` header missing or incorrect |
| 404 | `NOT_FOUND` | Resource not found, or cross-teacher access denied |
| 409 | `EMAIL_ALREADY_EXISTS` | Duplicate email on registration or provisioning |
| 500 | (unhandled exception) | Internal server error |

---

## Section 1 — Implemented endpoints (Epic 01)

### Authentication

---

#### `POST /auth/register`

Registers a new teacher. The client must first create a Firebase user using the Firebase client SDK (`createUserWithEmailAndPassword`), then call this endpoint with the resulting ID token to create the backend teacher record.

**Authentication:** None required on the `Authorization` header. The Firebase token is passed in the request body. This path is whitelisted in the security config because the teacher record does not yet exist at registration time.

**Request body:**

```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "name": "Ana Pérez"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `idToken` | string | Yes | Firebase ID token from `createUserWithEmailAndPassword` |
| `name` | string | No | Display name; falls back to Firebase display name, then email |

**Response — HTTP 201 Created:**

```json
{
  "firebaseUid": "pZ1mVr3qUBXyKn7oW8sNaC..."
}
```

**Behavior:** Idempotent. If a teacher record already exists for the UID, the existing UID is returned without error or modification.

**Error responses:**

| Status | Body | Condition |
|--------|------|-----------|
| 401 | `{"error":"INVALID_TOKEN"}` | `idToken` is invalid or expired |
| 409 | `{"error":"EMAIL_ALREADY_EXISTS","email":"..."}` | Teacher with this email already exists |

**curl example:**

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "idToken": "YOUR_FIREBASE_ID_TOKEN",
    "name": "Ana Pérez"
  }'
```

---

#### `POST /auth/sign-out`

Revokes all Firebase refresh tokens for the authenticated teacher. After this call, any cached ID tokens will fail to renew within approximately one minute (because the API uses `checkRevoked=true` on every `verifyIdToken` call).

**Authentication:** `Authorization: Bearer <Firebase_ID_Token>` (email must be verified)

**Request body:** Empty

**Response — HTTP 204 No Content:** No body.

**Error responses:**

| Status | Body | Condition |
|--------|------|-----------|
| 401 | HTTP 401 (no body) | No valid token provided |
| 401 | `{"error":"EMAIL_NOT_VERIFIED"}` | Token valid but email unverified |

**curl example:**

```bash
curl -X POST http://localhost:8080/auth/sign-out \
  -H "Authorization: Bearer YOUR_FIREBASE_ID_TOKEN"
```

**Note:** The web client calls this endpoint with a 3-second timeout and then always calls Firebase `signOut()` client-side regardless of the server response. Client-side sign-out is guaranteed; server-side revocation is best-effort.

---

### Assessments

---

#### `GET /assessments`

Returns all assessments for the authenticated teacher.

**Authentication:** `Authorization: Bearer <Firebase_ID_Token>` (email must be verified)

**Response — HTTP 200 OK:**

```json
[
  {
    "id": "string",
    "title": "string",
    "status": "DRAFT | OPEN | GRADING | CLOSED",
    "submissionCount": 0,
    "pendingApprovals": 0,
    "reportLink": null
  }
]
```

| Field | Type | Notes |
|-------|------|-------|
| `id` | string | Assessment identifier |
| `title` | string | Assessment display title |
| `status` | string enum | One of: `DRAFT`, `OPEN`, `GRADING`, `CLOSED` |
| `submissionCount` | integer | Number of student submissions |
| `pendingApprovals` | integer | AI outputs awaiting teacher review |
| `reportLink` | string or null | Link to generated report, if available |

**Current state:** The endpoint is implemented and secured, but the service returns an empty array `[]`. Assessment data will be populated in Epic 02 when the `assessment` table is added.

**Error responses:**

| Status | Body | Condition |
|--------|------|-----------|
| 401 | HTTP 401 | No valid token |
| 401 | `{"error":"EMAIL_NOT_VERIFIED"}` | Email unverified |

**curl example:**

```bash
curl http://localhost:8080/assessments \
  -H "Authorization: Bearer YOUR_FIREBASE_ID_TOKEN"
```

Expected current response: `[]`

---

### Internal (operator) endpoints

These endpoints are for platform operators. They are not accessible by teachers.

---

#### `POST /internal/teachers`

Provisions a new teacher account. Creates a Firebase user with `emailVerified=true` (bypassing the normal email verification step) and saves the teacher record in the database. Returns a password-reset invite link that the operator can share with the teacher.

**Authentication:** `X-Internal-Key: <app.internal.secret>`

**Request body:**

```json
{
  "name": "Ana Pérez",
  "email": "ana.perez@universidad.cl"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `name` | string | Yes | Teacher display name |
| `email` | string | Yes | Must be unique across both Firebase and the teacher table |

**Response — HTTP 201 Created:**

```json
{
  "firebaseUid": "pZ1mVr3qUBXyKn7oW8sNaC...",
  "inviteLink": "https://accounts.google.com/o/oauth2/auth?..."
}
```

| Field | Notes |
|-------|-------|
| `firebaseUid` | Firebase UID assigned to the new user |
| `inviteLink` | Firebase password-reset link; send to teacher so they can set a password |

**Error responses:**

| Status | Body | Condition |
|--------|------|-----------|
| 403 | `{"error":"FORBIDDEN"}` | `X-Internal-Key` header missing or wrong |
| 409 | `{"error":"EMAIL_ALREADY_EXISTS","email":"..."}` | Email already in Firebase or database |

**curl example:**

```bash
curl -X POST http://localhost:8080/internal/teachers \
  -H "X-Internal-Key: dev-secret-change-me" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ana Pérez",
    "email": "ana.perez@universidad.cl"
  }'
```

---

#### `PATCH /internal/teachers/{uid}/flags`

Updates pilot program flags on a teacher record. All body fields are optional — omitted fields are left unchanged. Always sets `flag_set_at` to the current timestamp.

**Authentication:** `X-Internal-Key: <app.internal.secret>`

**Path parameter:** `uid` — Firebase UID of the teacher (returned by `POST /internal/teachers` or from the Firebase console)

**Request body (all fields optional):**

```json
{
  "planType": "pilot",
  "relatedParty": true,
  "offerDetails": "Pilot program participant — XPRIZE demo cohort",
  "evidenceLink": "https://drive.google.com/...",
  "setBy": "carlos.martinez"
}
```

| Field | Type | Values | Notes |
|-------|------|--------|-------|
| `planType` | string or null | `"pilot"`, `"free"`, `"paid"` | Enforced by DB CHECK constraint |
| `relatedParty` | boolean or null | `true`, `false` | Whether this teacher has a business relationship with the team |
| `offerDetails` | string or null | — | Free text; description of the offer or arrangement |
| `evidenceLink` | string or null | — | URL to supporting evidence (Drive doc, email, etc.) |
| `setBy` | string or null | — | Identifier of the operator who set the flags |

**Response — HTTP 200 OK:**

```json
{
  "firebaseUid": "pZ1mVr3qUBXyKn7oW8sNaC...",
  "planType": "pilot",
  "relatedParty": true,
  "flagSetAt": "2026-06-13T14:30:00.000+00:00"
}
```

**Error responses:**

| Status | Body | Condition |
|--------|------|-----------|
| 403 | `{"error":"FORBIDDEN"}` | `X-Internal-Key` header missing or wrong |
| 404 | `{"error":"NOT_FOUND","resource":"<uid>"}` | No teacher with this Firebase UID |

**curl example:**

```bash
curl -X PATCH http://localhost:8080/internal/teachers/pZ1mVr3qUBXyKn7oW8sNaC/flags \
  -H "X-Internal-Key: dev-secret-change-me" \
  -H "Content-Type: application/json" \
  -d '{
    "planType": "pilot",
    "relatedParty": false,
    "setBy": "carlos.martinez"
  }'
```

---

## Section 2 — Planned endpoints (Epics 02–13)

The following endpoints are designed in [`docs/04-architecture/api-design.md`](../04-architecture/api-design.md) and will be implemented in subsequent epics. They are listed here for navigation.

The designed API uses a base path of `/api/v1`. The current implementation does not include this versioned prefix yet — it will be added when Epic 02 development begins.

### Summary table

| Method | Path | Epic | Description |
|--------|------|------|-------------|
| `POST` | `/assessments` | 02 | Create assessment brief |
| `GET` | `/assessments/{id}` | 02 | Get assessment detail and state |
| `PATCH` | `/assessments/{id}` | 02 | Update teacher-editable fields |
| `POST` | `/assessments/{id}/generate-draft` | 02/03 | Run Assessment Agent |
| `POST` | `/assessments/{id}/approve` | 02 | Approve assessment draft |
| `POST` | `/assessments/{id}/rubrics/generate` | 03 | Run Rubric Agent |
| `PATCH` | `/rubrics/{id}` | 03 | Edit rubric criteria |
| `POST` | `/rubrics/{id}/approve` | 03 | Approve rubric |
| `POST` | `/assessments/{id}/submissions` | 04 | Add a student submission (text/code) |
| `POST` | `/assessments/{id}/submissions/upload` | 04 | Upload a submission file |
| `GET` | `/assessments/{id}/submissions` | 04 | List submissions |
| `GET` | `/submissions/{id}` | 04 | Get one submission |
| `POST` | `/assessments/{id}/grading-runs` | 05 | Start grading run |
| `GET` | `/assessments/{id}/grading-suggestions` | 05 | Get teacher review queue |
| `POST` | `/grading-suggestions/{id}/approve` | 05 | Approve grading suggestion |
| `PATCH` | `/grading-suggestions/{id}` | 05 | Edit grading score/notes |
| `POST` | `/grading-suggestions/{id}/reject` | 05 | Reject suggestion |
| `POST` | `/assessments/{id}/feedback-drafts/generate` | 06 | Run Feedback Agent |
| `GET` | `/assessments/{id}/feedback-drafts` | 06 | List feedback drafts |
| `POST` | `/feedback-drafts/{id}/approve` | 06 | Approve feedback |
| `PATCH` | `/feedback-drafts/{id}` | 06 | Edit feedback |
| `POST` | `/feedback-drafts/{id}/reject` | 06 | Reject feedback draft |
| `POST` | `/assessments/{id}/learning-gaps/generate` | 07 | Run Learning Gap Agent |
| `GET` | `/assessments/{id}/learning-gaps` | 07 | List gap summaries |
| `POST` | `/learning-gaps/{id}/approve` | 07 | Confirm gap |
| `POST` | `/assessments/{id}/recovery-activities/generate` | 08 | Run Recovery Agent |
| `GET` | `/assessments/{id}/recovery-activities` | 08 | List recovery activities |
| `POST` | `/recovery-activities/{id}/approve` | 08 | Approve activity |
| `POST` | `/assessments/{id}/teacher-report/generate` | 09 | Run Teacher Report Agent |
| `GET` | `/assessments/{id}/teacher-report` | 09 | Get report |
| `POST` | `/teacher-reports/{id}/approve` | 09 | Approve report |
| `GET` | `/teacher-reports/{id}/export` | 09 | Export report artifact |
| `GET` | `/agent-runs` | all | List agent execution logs |
| `GET` | `/agent-runs/{id}` | all | Get agent execution detail |
| `GET` | `/evidence/dashboard` | all | Evidence dashboard metrics |
| `POST` | `/evidence/customer` | all | Create/update customer evidence |
| `POST` | `/evidence/revenue-events` | all | Store revenue/commitment record |
| `GET` | `/usage/current` | all | Current plan usage |
| `GET` | `/auth/me` | 02 | Current authenticated user |

Closed assessment endpoints (Epics 10–13) follow similar patterns and are documented in `docs/04-architecture/api-design.md`.

---

<!-- nav -->

[← Repository Map](02-repository-map.md) | [↑ Top](#api-reference) | [Security Implementation →](04-security-implementation.md)
