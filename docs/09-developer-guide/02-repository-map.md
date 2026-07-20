# Repository Map

GradeOps AI is organized as a multi-repo workspace. Each subdirectory under the workspace root is an independent git repository. This document maps every significant file in each repo, explaining what it does and why it exists.

---

## `api/` — Spring Boot 4 / Java 21 (domain API)

**Package root:** `cl.gradeops.ai.api`
**Maven groupId / artifactId:** `cl.gradeops.ai` / `grade-ops-api`
**Spring Boot version:** 4.1.0
**Key dependencies:** Spring Web MVC, Spring Data JPA, Spring Security, Flyway, PostgreSQL driver, Firebase Admin SDK 9.3, Logstash Logback Encoder

### Source tree

```
api/src/main/java/cl/gradeops/ai/api/
├── GradeOpsApiApplication.java
├── config/
│   └── FirebaseConfig.java
├── security/
│   ├── SecurityConfig.java
│   ├── InternalAuthFilter.java
│   ├── FirebaseTokenFilter.java
│   ├── EmailVerifiedFilter.java
│   ├── AuthenticatedTeacher.java
│   └── OwnershipVerifier.java
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── RegisterRequest.java
│   ├── RegisterResponse.java
│   └── InvalidTokenException.java
├── internal/teacher/
│   ├── InternalTeacherController.java
│   ├── ProvisionTeacherService.java
│   ├── ProvisionTeacherRequest.java
│   ├── ProvisionTeacherResponse.java
│   ├── PilotFlagService.java
│   ├── PilotFlagRequest.java
│   └── PilotFlagResponse.java
├── assessment/
│   ├── AssessmentController.java
│   ├── AssessmentService.java
│   ├── AssessmentStatus.java
│   └── AssessmentSummaryDto.java
├── domain/teacher/
│   ├── TeacherEntity.java
│   └── TeacherRepository.java
└── common/
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    ├── DuplicateEmailException.java
    └── InvalidTokenException.java   (defined in auth/, referenced here)
```

### File-by-file reference

#### Entry point

**`GradeOpsApiApplication.java`**
Standard `@SpringBootApplication` entry point. No custom configuration.

#### `config/`

**`FirebaseConfig.java`**
Declares two Spring beans: `FirebaseApp` and `FirebaseAuth`. Both are annotated with `@ConditionalOnMissingBean` so test configurations can substitute mocks without overriding the production beans.

Credentials are loaded via `GoogleCredentials.getApplicationDefault()`:
- Local dev: reads the path in `GOOGLE_APPLICATION_CREDENTIALS` env var (points to a service account JSON file)
- Cloud Run: uses the Cloud Run service account via the GCP metadata server (no env var needed, provided the SA has `roles/firebaseauth.admin`)

#### `security/`

**`SecurityConfig.java`**
Defines the Spring Security filter chain. Key settings:
- CSRF disabled (stateless REST API, no cookie sessions)
- Session creation policy: `STATELESS`
- Authentication entry point: returns HTTP 401 (not a redirect)
- Permits `/internal/**`, `/auth/register`, and `/auth/verify/resend` without authentication
- All other requests require authentication
- Filter insertion order: `InternalAuthFilter` and `FirebaseTokenFilter` are inserted before `UsernamePasswordAuthenticationFilter`; `EmailVerifiedFilter` runs after `FirebaseTokenFilter`

**`InternalAuthFilter.java`**
`OncePerRequestFilter` that guards paths starting with `/internal/`. Reads the `X-Internal-Key` request header and compares it to the value of `app.internal.secret` (from `application.yml`). Returns HTTP 403 with `{"error":"FORBIDDEN"}` if the header is absent or wrong. Skips itself for all non-internal paths via `shouldNotFilter()`.

**`FirebaseTokenFilter.java`**
`OncePerRequestFilter` that verifies Firebase ID tokens. Reads `Authorization: Bearer <token>`. Calls `firebaseAuth.verifyIdToken(token, true)` with `checkRevoked=true`. On success:
- Sets `request.setAttribute("firebaseToken", decodedToken)` so downstream filters can read claims without re-verifying
- Builds an `AuthenticatedTeacher` principal and sets it in `SecurityContextHolder`

On failure, logs the error at DEBUG level and clears the security context. Does not reject the request — that is left to Spring Security's access rules.

**`EmailVerifiedFilter.java`**
`OncePerRequestFilter` that runs after `FirebaseTokenFilter`. Reads the `"firebaseToken"` request attribute set by the preceding filter. If absent (unauthenticated request), the filter passes through. If present and the token's `email_verified` claim is `false`, returns HTTP 401 with `{"error":"EMAIL_NOT_VERIFIED"}`. Whitelisted paths (`/auth/register`, `/auth/verify/resend`) always pass through regardless of verification status.

**`AuthenticatedTeacher.java`**
A Java record used as the Spring Security principal:
```java
public record AuthenticatedTeacher(String uid, String email) {}
```
Accessed in controllers and services via:
```java
AuthenticatedTeacher teacher = (AuthenticatedTeacher)
    SecurityContextHolder.getContext().getAuthentication().getPrincipal();
```

**`OwnershipVerifier.java`**
Spring component injected into service classes that need to verify resource ownership. The `verify(ownerUid, authenticatedUid, resourceId)` method throws `ResourceNotFoundException` (HTTP 404) if the UIDs differ. Throwing 404 rather than 403 is intentional: it does not reveal to an attacker that a resource exists for a different teacher. A `WARN` log is also emitted and captured by Cloud Logging via the JSON appender.

#### `auth/`

**`AuthController.java`**
REST controller at `/auth`. Two endpoints:
- `POST /auth/register` — public; delegates to `AuthService.register()`
- `POST /auth/sign-out` — authenticated; reads the principal from `SecurityContextHolder`, delegates to `AuthService.signOut(uid)`

**`AuthService.java`**
Business logic for auth operations:
- `register(RegisterRequest)`: verifies the ID token from the request body (not from the Authorization header, because the client sends the token explicitly here), then creates or retrieves a `TeacherEntity`. Idempotent: calling register twice for the same Firebase UID returns the existing UID without error.
- `signOut(uid)`: calls `firebaseAuth.revokeRefreshTokens(uid)`. This invalidates all refresh tokens for the user; existing ID tokens remain valid for up to 1 hour unless `checkRevoked=true` is used (which it is, in `FirebaseTokenFilter`).

**`RegisterRequest.java`** — `record(String idToken, String name)`
**`RegisterResponse.java`** — `record(String firebaseUid)`
**`InvalidTokenException.java`** — runtime exception mapped to HTTP 401

#### `internal/teacher/`

**`InternalTeacherController.java`**
REST controller for operator-only endpoints. Protected by `InternalAuthFilter` (the `X-Internal-Key` header), not by Firebase token auth.
- `POST /internal/teachers` — provisions a new teacher (returns HTTP 201)
- `PATCH /internal/teachers/{uid}/flags` — updates pilot flags on an existing teacher (returns HTTP 200)

**`ProvisionTeacherService.java`**
Creates a Firebase user and a `TeacherEntity` in the same transaction. Key behavior:
- Creates the Firebase user with `emailVerified=true` (bypasses the normal email verification flow)
- Does not set a password in the `CreateRequest`; instead calls `generatePasswordResetLink()` to return an invite link the operator can send to the teacher
- If the DB save fails after Firebase user creation, compensates by deleting the Firebase user to avoid orphaned Firebase records
- Throws `DuplicateEmailException` (HTTP 409) if `EMAIL_ALREADY_EXISTS` is returned by Firebase

**`PilotFlagService.java`**
Partial update of pilot-related fields on a `TeacherEntity`. All `PilotFlagRequest` fields are optional — null fields are ignored. Always sets `flag_set_at` and `updated_at` to the current timestamp.

**`ProvisionTeacherRequest.java`** — `record(String name, String email)`
**`ProvisionTeacherResponse.java`** — `record(String firebaseUid, String inviteLink)`
**`PilotFlagRequest.java`** — `record(String planType, Boolean relatedParty, String offerDetails, String evidenceLink, String setBy)`
**`PilotFlagResponse.java`** — `record(String firebaseUid, String planType, boolean relatedParty, String flagSetAt)`

#### `assessment/`

**`AssessmentController.java`**
`GET /assessments` — retrieves the principal from `SecurityContextHolder` and passes the teacher UID to `AssessmentService.listForTeacher()`. The scoping happens at the service level so no teacher can ever see another teacher's assessments, even if the controller were accidentally called without proper auth.

**`AssessmentService.java`**
Currently returns an empty list. Contains a `TODO Epic 02` comment marking where the repository query will be added when the assessment table exists.

**`AssessmentStatus.java`** — enum: `DRAFT`, `OPEN`, `GRADING`, `CLOSED`

**`AssessmentSummaryDto.java`**
```java
record AssessmentSummaryDto(
    String id,
    String title,
    AssessmentStatus status,
    int submissionCount,
    int pendingApprovals,
    String reportLink  // nullable
)
```

#### `domain/teacher/`

**`TeacherEntity.java`**
JPA entity mapped to the `teacher` table. Primary key is `firebase_uid` (a Firebase UID, not a database-generated ID). Includes pilot flag columns added in V2 migration. The constructor sets `created_at` and `updated_at` to the current timestamp at construction time.

**`TeacherRepository.java`**
`JpaRepository<TeacherEntity, String>` with additional queries:
- `findByEmail(String)` — look up by email
- `existsByEmail(String)` — check for duplicate email
- `findByPlanType(String)` — operator reporting query
- `findByRelatedParty(boolean)` — operator reporting query

#### `common/`

**`GlobalExceptionHandler.java`**
`@RestControllerAdvice` handling three exception types:
- `DuplicateEmailException` → HTTP 409, body `{"error":"EMAIL_ALREADY_EXISTS","email":"..."}`
- `ResourceNotFoundException` → HTTP 404, body `{"error":"NOT_FOUND","resource":"..."}`
- `InvalidTokenException` → HTTP 401, body `{"error":"INVALID_TOKEN"}`

**`ResourceNotFoundException.java`** — runtime exception; carries `resourceId` for the response body
**`DuplicateEmailException.java`** — runtime exception; carries `email` for the response body

### Resources

```
api/src/main/resources/
├── application.yml                    — Default config (env var overrides)
├── application-local.yml              — Local dev overrides (gitignored)
├── logback-spring.xml                 — JSON structured logging (Cloud Logging compatible)
└── db/migration/
    ├── V1__create_teacher_table.sql   — teacher table with PK, email unique index
    └── V2__add_pilot_flag_columns.sql — plan_type, related_party, offer_details,
                                         evidence_link, flag_set_by, flag_set_at
```

**`application.yml`** reads from environment variables with sensible local defaults:
```yaml
spring.datasource.url:      ${DATABASE_URL:jdbc:postgresql://localhost:5432/gradeops}
spring.datasource.username: ${DATABASE_USER:gradeops}
spring.datasource.password: ${DATABASE_PASSWORD:gradeops}
spring.jpa.hibernate.ddl-auto: validate   # Flyway owns schema; Hibernate only validates
app.internal.secret: ${INTERNAL_API_SECRET:change-me-in-production}
```

**`logback-spring.xml`** uses the Logstash JSON encoder (`net.logstash.logback`). All log output is structured JSON, which Cloud Run forwards directly to Cloud Logging. This means log messages are queryable as structured fields rather than raw text.

**Flyway migration naming convention:** `V{number}__{description}.sql` (two underscores). Do not modify existing migration files after they have run in any environment. Add new migrations as new files.

---

## `web/` — Next.js 15 / React 19 / TypeScript

**Next.js version:** 15.3.0
**React version:** 19.1.0
**Firebase client SDK:** 11.x

The web app uses the **App Router** (not the legacy Pages Router). All route definitions are directory-based under `src/app/`.

### API proxying

`next.config.ts` defines a rewrite rule:

```typescript
{ source: "/api/:path*", destination: `${process.env.API_BASE_URL ?? "http://localhost:8080"}/:path*` }
```

All API calls from the frontend go to `/api/...` (relative URLs). Next.js proxies these server-side to the Spring Boot API. This means:
- No CORS configuration needed on the API for the web client
- The API URL is never exposed to the browser

### Source tree

```
web/src/
├── app/
│   ├── layout.tsx                              — Root layout (global styles, fonts)
│   ├── (protected)/
│   │   ├── layout.tsx                          — Wraps all protected routes in <AuthGuard>
│   │   └── dashboard/
│   │       ├── page.tsx                        — Dashboard: fetches and renders assessment list
│   │       └── __tests__/DashboardPage.test.tsx
│   ├── login/
│   │   ├── page.tsx                            — Sign-in form (Firebase email/password)
│   │   └── __tests__/SignInPage.test.tsx
│   ├── register/
│   │   ├── page.tsx                            — Registration form
│   │   └── __tests__/RegisterPage.test.tsx
│   └── verify-email/
│       ├── page.tsx                            — "Check your email" screen
│       └── __tests__/VerifyEmailPage.test.tsx
├── components/
│   ├── auth/
│   │   ├── AuthGuard.tsx                       — Auth state wrapper with routing logic
│   │   ├── SignOutButton.tsx                   — Best-effort server revocation + client sign-out
│   │   └── __tests__/
│   │       ├── AuthGuard.test.tsx
│   │       └── SignOutButton.test.tsx
│   └── dashboard/
│       ├── AssessmentCard.tsx                  — Renders one assessment summary card
│       ├── EmptyDashboard.tsx                  — "No assessments yet" CTA
│       └── __tests__/
│           ├── AssessmentCard.test.tsx
│           └── EmptyDashboard.test.tsx
├── lib/
│   ├── firebase/
│   │   └── client.ts                          — Firebase app singleton + auth export
│   └── api/
│       ├── client.ts                          — apiClient(): Bearer token auto-attach + 401 handling
│       ├── auth.ts                            — registerTeacher(), signOutApi()
│       ├── assessments.ts                     — getAssessments()
│       └── __tests__/apiClient.test.ts
├── test/
│   └── __mocks__/
│       └── firebase/
│           ├── app.ts                         — Mock for firebase/app
│           └── auth.ts                        — Mock for firebase/auth
└── types/
    └── assessment.ts                          — AssessmentStatus type, AssessmentSummaryDto interface
```

### Key file descriptions

#### Route groups

**`(protected)/layout.tsx`**
Wraps all routes in the `(protected)` group with `<AuthGuard>`. Any page added under `(protected)/` is automatically guarded with no additional code required.

**`app/login/page.tsx`**
Signs in with Firebase `signInWithEmailAndPassword`. On success, redirects to `/dashboard`. Reads the `?reason=expired` query parameter and displays a session-expired banner when present (set by `apiClient` on 401).

**`app/register/page.tsx`**
Creates a Firebase user with `createUserWithEmailAndPassword`, sends a verification email, calls `registerTeacher(idToken, name)` to create the backend teacher record, then redirects to `/verify-email`.

**`app/verify-email/page.tsx`**
Shown after registration. Displays a "check your email" message and a resend-verification button. Polls `onAuthStateChanged` — once the user is verified and signs in again, redirects to `/dashboard`.

#### Components

**`AuthGuard.tsx`**
Client component that subscribes to `onAuthStateChanged`. The guard has three states:
- `user === null` → redirect to `/login`
- `user.emailVerified === false` → redirect to `/verify-email`
- `user` authenticated and verified → render children

While the auth state is loading, renders a centered spinner. This prevents protected page content from flashing before the redirect fires.

**`SignOutButton.tsx`**
On click:
1. Gets the current ID token from `auth.currentUser?.getIdToken()`
2. Calls `POST /api/auth/sign-out` with a 3-second timeout (best-effort server-side revocation)
3. Always calls `firebaseSignOut(auth)` in the `finally` block — the client-side sign-out happens regardless of whether the server call succeeded
4. Redirects to `/login`

#### `lib/firebase/client.ts`

Initializes the Firebase app as a singleton (guards against double-initialization via `getApps().length === 0`). Exports `app` and `auth`. This is the only place in the codebase where `initializeApp` is called.

#### `lib/api/client.ts`

`apiClient(path, options)` is the single HTTP client function for all authenticated API calls:
1. Gets the current ID token from `auth.currentUser?.getIdToken()` (returns a fresh or cached token — Firebase refreshes automatically before expiry)
2. Adds `Authorization: Bearer <token>` header if a token is available
3. On HTTP 401 response:
   - If `body.error === "EMAIL_NOT_VERIFIED"` → redirects to `/verify-email`
   - Otherwise → signs out client-side and redirects to `/login?reason=expired`

`registerTeacher()` and `signOutApi()` in `lib/api/auth.ts` do not use `apiClient` — they make direct `fetch` calls because they run before the auth state is fully established (`registerTeacher`) or need to work during sign-out (`signOutApi`).

#### `lib/api/assessments.ts`

Single function `getAssessments()` that calls `GET /api/assessments` via `apiClient` and returns `AssessmentSummaryDto[]`.

#### `types/assessment.ts`

```typescript
export type AssessmentStatus = "DRAFT" | "OPEN" | "GRADING" | "CLOSED";

export interface AssessmentSummaryDto {
  id: string;
  title: string;
  status: AssessmentStatus;
  submissionCount: number;
  pendingApprovals: number;
  reportLink: string | null;
}
```

Types mirror the API's `AssessmentSummaryDto` record exactly. When the API contract changes in Epic 02+, this interface must be updated to match.

#### Test infrastructure

**`src/test/__mocks__/firebase/`**
Jest manual mocks for the Firebase SDK. The mock path matches the module paths `firebase/app` and `firebase/auth` used in the source. Jest picks these up automatically because of the `moduleNameMapper` or `__mocks__` directory convention configured in `jest.config.js`.

Tests are co-located with components in `__tests__/` directories. The test runner is Jest with `jest-environment-jsdom` for DOM simulation and `@testing-library/react` for component rendering.

---

## `agents/` — Spring Boot 4 / Spring AI (scaffolding)

The agents service is scaffolded but contains no implemented agent logic yet. Implementation begins in Epic 03 (Rubric Agent) and continues through Epic 13.

**Expected package root:** `cl.gradeops.ai.agents`

When implemented, the package structure will be:

```
agents/src/main/java/cl/gradeops/ai/agents/
├── open/
│   ├── assessment/          — Assessment Agent (Command, Result, Service, Controller)
│   ├── rubric/              — Rubric Agent
│   ├── grading/             — Grading Agent
│   ├── feedback/            — Feedback Agent
│   ├── learninggap/         — Learning Gap Agent
│   ├── recovery/            — Recovery Agent
│   └── teacherreport/       — Teacher Report Agent
├── closed/
│   ├── questiongeneration/  — Question Generation Agent
│   ├── distractorquality/   — Distractor Quality Agent
│   ├── ambiguityreview/     — Ambiguity Review Agent
│   ├── assessmentassembly/  — Assessment Assembly Agent
│   └── itemanalytics/       — Item Analytics Agent
├── ops/evidence/            — Ops Evidence Agent
├── shared/
│   ├── envelope/            — AgentCommand base structure
│   ├── logging/             — AgentExecutionLog recording
│   ├── output/              — Shared structured output base types
│   └── cost/                — Token and cost estimation utilities
└── provider/gemini/         — Spring AI Vertex AI configuration and adapters
```

Prompt templates are stored in `agents/src/main/resources/prompts/` as versioned StringTemplate (`.st`) files. Prompts must never be inlined in Java code.

Each agent package follows the same four-class pattern:

| Class | Role |
|-------|------|
| `{Agent}Command` | Input envelope received from the API via `agentclient` |
| `{Agent}Result` | Structured output returned to the API |
| `{Agent}Service` | Orchestration: load prompt, build envelope, call Gemini, validate output, log execution |
| `{Agent}Controller` | REST endpoint (`POST /agents/open/{name}`, etc.) |

---

## `infra/` — Terraform (scaffolding)

Infrastructure as code for Google Cloud. The primary target is the `demo` environment.

```
infra/terraform/
├── modules/
│   ├── cloud-run/       — Reusable Cloud Run service module
│   ├── cloud-sql/       — PostgreSQL instance and database
│   ├── cloud-storage/   — Storage buckets and lifecycle rules
│   ├── secret-manager/  — Secret creation and IAM bindings
│   └── iam/             — Service accounts and IAM roles
└── environments/
    ├── demo/
    │   ├── main.tf               — Cloud Run services, SQL, Storage, IAM for demo
    │   ├── variables.tf          — Input variables (project ID, region, image tags)
    │   └── terraform.tfvars.example — Example values (do not commit actual .tfvars)
    └── prod/
        ├── main.tf
        ├── variables.tf
        └── terraform.tfvars.example
```

**Google Cloud services provisioned:**

| Service | Used for |
|---------|---------|
| Cloud Run | `web`, `api`, and `agents` services |
| Cloud SQL (PostgreSQL 15) | Primary database for the API |
| Cloud Storage | Student submission files, report exports, evidence artifacts |
| Secret Manager | `INTERNAL_API_SECRET`, DB password, Gemini API key |
| Artifact Registry | Docker image storage for CI/CD |
| IAM | Service accounts and role bindings for service-to-service auth |
| Cloud Logging | Structured log output from all services |

**Service-to-service authentication:** The API calls the agents service via Cloud Run's internal URL. Authentication uses OIDC tokens issued to the API's service account. The agents service is not publicly reachable — it accepts requests only from the API.

**Demo environment commands:**

```bash
terraform -chdir=terraform/environments/demo init
terraform -chdir=terraform/environments/demo plan
terraform -chdir=terraform/environments/demo apply
```

---

## `docs/` — Documentation only

No application code. The canonical source of truth for product strategy, architecture, agent contracts, business evidence, and durable decisions.

```
docs/
├── 00-project/     — Vision, pitch, roadmap, cost model
├── 01-business/    — Business model, pricing, go-to-market
├── 02-product/     — Personas, MVP scope, user stories
├── 03-ai-agents/   — Agent roles, contracts, execution logs
├── 04-architecture/ — System design, data model, API design, security
├── 05-evidence/    — Usage, revenue, testimonials, agent log evidence
├── 06-ux/          — Screen inventory, interaction model
├── archive/        — Historical material no longer active
├── 08-user-guide/  — End-user documentation
├── 09-developer-guide/ — This guide
├── 99-decisions/   — Architecture decision records (ADR format)
└── CLAUDE.md       — Writing conventions for this repository
```

See `docs/CLAUDE.md` for writing conventions and content rules that apply when editing documentation.

---

<!-- nav -->

[← Local Setup](01-local-setup.md) | [↑ Top](#repository-map) | [API Reference →](03-api-reference.md)
