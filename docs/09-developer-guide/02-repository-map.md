# Repository Map

GradeOps AI is organized as a multi-repo workspace. Each subdirectory under the workspace root is an independent git repository. This document maps every significant file in each repo, explaining what it does and why it exists.

---

## `api/` — Spring Boot 4 / Java 21 (domain API)

**Package root:** `cl.gradeops.ai.api`
**Maven groupId / artifactId:** `cl.gradeops.ai` / `grade-ops-api`
**Spring Boot version:** 4.1.0
**Key dependencies:** Spring Web MVC, Spring Data JPA, Spring Security, Flyway, PostgreSQL driver, Firebase Admin SDK 9.3, Logstash Logback Encoder

### Current source tree

```
api/src/main/java/cl/gradeops/ai/api/
├── GradeOpsApiApplication.java
├── agentclient/             — Internal client used by api/ to call agents/
├── auth/
│   ├── application/         — Register, sign-out, password reset use cases and ports
│   ├── domain/              — Auth provider/value objects and reset-code domain
│   └── infrastructure/      — Web controllers, Firebase adapter, email adapter, persistence
├── assessment/
│   ├── application/         — Brief/draft commands, ports, handlers, generation coordinator
│   ├── domain/              — Assessment aggregate, brief, draft, status, execution log
│   └── infrastructure/      — /api/v1 controller, persistence adapters, mappers, config
├── teacher/
│   ├── application/         — Provision teacher and pilot flags use cases
│   ├── domain/              — Teacher aggregate and exceptions
│   └── infrastructure/      — Internal web controller and persistence adapter
└── shared/
    ├── application/         — Shared application exceptions and ownership verifier
    ├── domain/              — Aggregate root, domain events, domain exceptions
    └── infrastructure/      — Firebase/security config, filters, storage/email config, errors
```

### Implemented API slices

| Slice | Implemented surface |
| --- | --- |
| Auth | `/api/v1/auth/register`, `/api/v1/auth/sign-out`, forgot/reset password flow. |
| Internal teacher ops | `/internal/teachers`, `/internal/teachers/{uid}/flags`, protected by `X-Internal-Key`. |
| Assessment draft | `/api/v1/assessments`, `/api/v1/assessments/{id}/draft`, regenerate/update/current/version endpoints. |
| Agent integration | `agentclient` calls `agents/` and persists `AgentExecutionLog` through assessment application ports. |

All protected endpoints derive the teacher from Firebase auth and enforce ownership in application/service boundaries. Cross-teacher access should return 404 rather than exposing resource existence.

### Resources

```
api/src/main/resources/
├── application.yml                    — Default config (env var overrides)
├── application-local.example.yml      — Template for local dev overrides
├── logback-spring.xml                 — JSON structured logging (Cloud Logging compatible)
└── db/migration/
    ├── V1__create_teacher_table.sql
    ├── ...
    └── V12__add_agent_execution_logs.sql
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

Single function `getAssessments()` that calls `GET /api/v1/assessments` via `apiClient` and returns `AssessmentSummaryDto[]`.

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

## `agents/` — Spring Boot 4 / Spring AI

The agents service has a real Assessment Agent vertical slice. It exposes `POST /internal/agents/assessment`, loads the versioned prompt at `agents/src/main/resources/prompts/assessment-generation.st`, supports Gemini and Groq adapters, validates structured output, and returns an execution payload to `api/` for persistence.

**Package root:** `cl.gradeops.ai.agents`

Current implemented structure:

```
agents/src/main/java/cl/gradeops/ai/agents/
├── GradeOpsAgentsApplication.java
├── assessment/
│   ├── application/
│   │   ├── command/         — AssessmentCommand
│   │   ├── orchestrator/    — AssessmentAgentOrchestrator
│   │   ├── port/            — GenerateAssessmentDraftUseCase and provider ports
│   │   ├── result/          — AssessmentResult and AgentExecutionLogPayload
│   │   └── usecase/         — GenerateAssessmentDraftHandler
│   └── infrastructure/
│       ├── adapter/in/web/  — POST /internal/agents/assessment
│       ├── adapter/out/gemini/
│       ├── adapter/out/groq/
│       └── config/          — provider-specific ChatClient wiring
└── shared/infrastructure/
    ├── adapter/in/web/      — internal auth, correlation IDs, error response
    └── config/              — dotenv and web config
```

Prompt templates are stored in `agents/src/main/resources/prompts/` as versioned StringTemplate (`.st`) files. Prompts must never be inlined in Java code.

Planned agents should follow the same boundary:

| Class | Role |
|-------|------|
| `{Agent}Command` | Input envelope received from the API via `agentclient` |
| `{Agent}Result` | Structured output returned to the API |
| `{Agent}Handler` or orchestrator | Load prompt, build envelope, select provider, validate output, estimate cost/log payload |
| `{Agent}Controller` | Internal REST endpoint called only by `api/` |

---

## `infra/` — Terraform

Infrastructure as code for Google Cloud. The primary target is the `demo` environment.

```
infra/terraform/
└── environments/
    └── demo/
        ├── artifact_registry.tf
        ├── cloud_run.tf
        ├── cloud_sql.tf
        ├── firebase_admin_iam.tf
        ├── firebase_app_hosting.tf
        ├── firebase_web_app.tf
        ├── groq.tf
        ├── identity_platform.tf
        ├── outputs.tf
        ├── providers.tf
        ├── service_accounts.tf
        ├── smtp.tf
        ├── variables.tf
        └── workload_identity.tf
```

**Google Cloud services provisioned:**

| Service | Used for |
|---------|---------|
| Cloud Run | `web`, `api`, and `agents` services |
| Cloud SQL (PostgreSQL 15) | Primary database for the API |
| Cloud Storage | Student submission files, report exports, evidence artifacts |
| Secret Manager | `INTERNAL_API_SECRET`, DB password, Groq/Gemini/provider keys, SMTP secrets |
| Artifact Registry | Docker image storage for CI/CD |
| IAM | Service accounts and role bindings for service-to-service auth |
| Cloud Logging | Structured log output from all services |

**Service-to-service authentication:** The API calls the agents service through the `agentclient` module. In `demo`, use Cloud Run service-to-service auth/OIDC where provisioned; in local/beta, use the documented internal shared secret until OIDC is available.

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
