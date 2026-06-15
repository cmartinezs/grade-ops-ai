# US-010: Postman Collection for Teacher Onboarding Endpoints

- **Epic:** 01 — Teacher Onboarding and Workspace
- **Priority:** P1
- **ID:** US-010

## Story

As a developer, I want a Postman collection and environment file for the Teacher Onboarding endpoints so that I can test and demo the API without writing raw curl commands.

## Acceptance Criteria

- A Postman collection file exists at `docs/postman/teacher-onboarding.postman_collection.json` covering all Teacher Onboarding API endpoints.
- A Postman environment file exists at `docs/postman/local.postman_environment.json` with variables for `base_url`, `api_token`, and `teacher_uid`.
- The collection includes requests for: `POST /auth/register`, `POST /auth/sign-out`, `GET /assessments`, `POST /internal/teachers`, and `PATCH /internal/teachers/{uid}/flags`.
- Each request includes example headers (`Authorization: Bearer {{api_token}}`), a short description, and at least one example response.
- The environment file works against a local dev server (`localhost:8080`) out of the box.

---

## Definition of Done

- [ ] `docs/postman/teacher-onboarding.postman_collection.json` exists and imports cleanly into Postman.
- [ ] `docs/postman/local.postman_environment.json` exists with `base_url`, `api_token`, and `teacher_uid` variables.
- [ ] All 5 endpoints are present with correct HTTP method, path, and headers.
- [ ] Each request has a short description and at least one saved example response.
- [ ] Collection is manually validated against a running local API server (`./mvnw spring-boot:run -Dspring.profiles.active=local`).
- [ ] No real tokens or secrets committed — all sensitive values use `{{variable}}` placeholders.

## Technical Notes

- **Area:** `docs/`
- Files go in `docs/postman/` (create the directory if it does not exist).
- Use **Postman Collection v2.1** format (`"schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"`).
- `api_token` holds a Firebase ID token. For local testing, generate one via the Firebase Auth REST API (`POST https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword`) or the Firebase CLI (`firebase auth:token`). Never commit a real token.
- `teacher_uid` is the Firebase UID used in the path `/internal/teachers/{uid}/flags`; retrieve it from the sign-in response or from Firestore/Firebase console.
- Internal endpoints (`/internal/*`) require an additional `X-Internal-Secret` header if that mechanism is wired — document the variable in the environment file even if left blank.
- The `demo.postman_environment.json` for the Cloud Run demo environment can be added later; this story covers local only.

## Dependencies

| Depends on | Reason |
|------------|--------|
| US-001 (Teacher Login) | `POST /auth/sign-out` and `GET /assessments` require Firebase auth |
| US-006 (Teacher Account Provisioning) | `POST /internal/teachers` endpoint must exist |
| US-003 (Pilot Account Flag) | `PATCH /internal/teachers/{uid}/flags` endpoint must exist |
| US-008 (Teacher Self-Registration) | `POST /auth/register` endpoint must exist |

## Complexity

**Estimate:** S
