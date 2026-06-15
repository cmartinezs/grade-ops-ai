```
 ██████╗ ██████╗  █████╗ ██████╗ ███████╗ ██████╗ ██████╗ ███████╗    █████╗ ██╗
██╔════╝ ██╔══██╗██╔══██╗██╔══██╗██╔════╝██╔═══██╗██╔══██╗██╔════╝   ██╔══██╗██║
██║  ███╗██████╔╝███████║██║  ██║█████╗  ██║   ██║██████╔╝███████╗   ███████║██║
██║   ██║██╔══██╗██╔══██║██║  ██║██╔══╝  ██║   ██║██╔═══╝ ╚════██║   ██╔══██║██║
╚██████╔╝██║  ██║██║  ██║██████╔╝███████╗╚██████╔╝██║     ███████║██╗██║  ██║██║
 ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ╚══════╝ ╚═════╝ ╚═╝     ╚══════╝╚═╝╚═╝  ╚═╝╚═╝
```

<div align="center">

**AI-Native Assessment Operations Platform**

`grade-ops-ai-api` · Domain API

![Build](https://img.shields.io/badge/build-passing-brightgreen?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Firebase Admin](https://img.shields.io/badge/Firebase_Admin-9.3-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![Flyway](https://img.shields.io/badge/Flyway-migrations-CC0200?style=flat-square&logo=flyway&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)

</div>

---

## Overview

`grade-ops-ai-api` is the domain backbone of the GradeOps AI platform. It owns all business logic, workflow state, persistence, and authorization. The frontend (`grade-ops-ai-web`) and the agent runtime (`grade-ops-ai-agents`) are both consumers of this API — neither holds domain state or enforces business rules independently.

Built as a **Spring Boot 4 modular monolith** on Java 21, the API exposes two surfaces:
- **Public API** — consumed by the Next.js frontend via Firebase ID token authentication.
- **Internal API** (`/internal/*`) — consumed by operators and the agent runtime via a pre-shared secret header, never exposed to the browser.

---

## Architecture

```
grade-ops-ai-web  ──── Bearer <firebase_id_token> ────►  /api/**
grade-ops-ai-agents ── X-Internal-Secret: <secret> ───►  /internal/**
                                                               │
                                                    ┌──────────▼──────────────┐
                                                    │   Spring Security        │
                                                    │   Filter Chain           │
                                                    │                          │
                                                    │  InternalAuthFilter      │
                                                    │       ↓                  │
                                                    │  FirebaseTokenFilter     │
                                                    │       ↓                  │
                                                    │  EmailVerifiedFilter     │
                                                    │       ↓                  │
                                                    │  Controllers / Services  │
                                                    └──────────┬───────────────┘
                                                               │
                                                    ┌──────────▼──────────────┐
                                                    │   Cloud SQL             │
                                                    │   PostgreSQL 15         │
                                                    │   (Flyway migrations)   │
                                                    └─────────────────────────┘
```

**Filter chain responsibilities:**

| Filter | Path | Responsibility |
|--------|------|---------------|
| `InternalAuthFilter` | `/internal/**` | Validates `X-Internal-Secret` header; rejects with 403 if missing or wrong |
| `FirebaseTokenFilter` | `/api/**` | Verifies Firebase ID token via Admin SDK; populates `AuthenticatedTeacher` principal and sets `firebaseToken` request attribute |
| `EmailVerifiedFilter` | `/api/**` | Reads `firebaseToken` attribute; rejects with 403 if `email_verified` is false |

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Framework | Spring Boot (Web MVC) | 4.1.0 |
| Language | Java | 21 |
| Security | Spring Security | (Boot-managed) |
| Auth | Firebase Admin SDK | 9.3.0 |
| Persistence | Spring Data JPA + Hibernate | (Boot-managed) |
| Database | PostgreSQL | 15+ |
| Migrations | Flyway | (Boot-managed) |
| Testing | JUnit 5 + Mockito + H2 | (Boot-managed) |
| Logging | Logback + logstash-logback-encoder (JSON) | — |
| Containerization | Docker (multi-stage) | — |
| Runtime target | Cloud Run (JRE 21 Jammy) | — |

---

## Project Structure

```
api/
├── src/
│   ├── main/
│   │   ├── java/cl/gradeops/ai/api/
│   │   │   ├── GradeOpsApiApplication.java
│   │   │   ├── auth/                          # Public auth endpoints
│   │   │   │   ├── AuthController.java        # POST /auth/register, POST /auth/sign-out
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── RegisterResponse.java
│   │   │   │   └── InvalidTokenException.java
│   │   │   ├── assessment/                    # Assessment listing
│   │   │   │   ├── AssessmentController.java  # GET /assessments
│   │   │   │   ├── AssessmentService.java
│   │   │   │   ├── AssessmentStatus.java
│   │   │   │   └── AssessmentSummaryDto.java
│   │   │   ├── domain/
│   │   │   │   └── teacher/
│   │   │   │       ├── TeacherEntity.java     # JPA entity — teacher table
│   │   │   │       └── TeacherRepository.java
│   │   │   ├── internal/
│   │   │   │   └── teacher/                   # Operator-only endpoints
│   │   │   │       ├── InternalTeacherController.java  # POST /internal/teachers, PATCH /internal/teachers/{uid}/flags
│   │   │   │       ├── ProvisionTeacherService.java
│   │   │   │       ├── ProvisionTeacherRequest.java
│   │   │   │       ├── ProvisionTeacherResponse.java
│   │   │   │       ├── PilotFlagService.java
│   │   │   │       ├── PilotFlagRequest.java
│   │   │   │       └── PilotFlagResponse.java
│   │   │   ├── security/
│   │   │   │   ├── SecurityConfig.java        # Filter chain wiring
│   │   │   │   ├── InternalAuthFilter.java
│   │   │   │   ├── FirebaseTokenFilter.java
│   │   │   │   ├── EmailVerifiedFilter.java
│   │   │   │   ├── AuthenticatedTeacher.java  # Spring Security principal
│   │   │   │   └── OwnershipVerifier.java     # Resource isolation helper
│   │   │   ├── config/
│   │   │   │   └── FirebaseConfig.java        # FirebaseApp bean initialization
│   │   │   └── common/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ResourceNotFoundException.java  # → 404
│   │   │       └── DuplicateEmailException.java    # → 409
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── logback-spring.xml             # JSON structured logging for Cloud Logging
│   │       └── db/migration/
│   │           ├── V1__create_teacher_table.sql
│   │           └── V2__add_pilot_flag_columns.sql
│   └── test/
│       └── java/cl/gradeops/ai/api/
│           ├── auth/AuthControllerTest.java
│           ├── assessment/AssessmentControllerTest.java
│           ├── domain/teacher/TeacherRepositoryTest.java
│           ├── internal/teacher/ProvisionTeacherControllerTest.java
│           ├── internal/teacher/PilotFlagControllerTest.java
│           ├── security/FirebaseTokenFilterTest.java
│           ├── security/EmailVerifiedFilterTest.java
│           ├── security/OwnershipVerifierTest.java
│           └── config/FirebaseTestConfig.java
├── Dockerfile
├── pom.xml
└── mvnw
```

---

## Getting Started

### Prerequisites

| Tool | Minimum version |
|------|----------------|
| Java (JDK) | 21 |
| Maven (or use `./mvnw`) | 3.9 |
| PostgreSQL | 15 |
| Firebase project (for token validation) | — |

### Environment Variables

The API is configured via `application.yml` with environment variable overrides. Set the following before starting:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/gradeops
DATABASE_USER=gradeops
DATABASE_PASSWORD=gradeops

# Internal API — shared secret between this service and the agent runtime / operators
INTERNAL_API_SECRET=change-me-in-development

# Firebase Admin SDK — path to service account JSON key file
GOOGLE_APPLICATION_CREDENTIALS=/path/to/firebase-adminsdk.json
```

For local development, place overrides in `src/main/resources/application-local.yml` (gitignored) and activate the profile with `-Dspring.profiles.active=local`.

### Database Setup

```bash
# Create the database (first time only)
createdb gradeops

# Migrations run automatically on startup via Flyway.
# To run manually:
./mvnw flyway:migrate
```

### Local Development

```bash
./mvnw spring-boot:run
# or with the local profile:
./mvnw spring-boot:run -Dspring.profiles.active=local
```

The API starts on **http://localhost:8080**.

---

## Available Commands

| Command | Description |
|---------|-------------|
| `./mvnw spring-boot:run` | Start the API server |
| `./mvnw spring-boot:run -Dspring.profiles.active=local` | Start with local profile |
| `./mvnw test` | Run the full test suite |
| `./mvnw test -Dtest=ClassName` | Run a single test class |
| `./mvnw test -Dtest=ClassName#methodName` | Run a single test method |
| `./mvnw package -DskipTests` | Build the JAR |
| `./mvnw flyway:migrate` | Run pending DB migrations manually |

---

## API Reference

### Public Endpoints (`/api/**`)

All public endpoints require a valid Firebase ID token in the `Authorization: Bearer <token>` header and a verified email address.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/auth/register` | Register a new teacher account (creates Firebase user + DB record) |
| `POST` | `/auth/sign-out` | Invalidate the server-side session; client must discard the token |
| `GET` | `/assessments` | List all assessments for the authenticated teacher |

### Internal Endpoints (`/internal/**`)

Internal endpoints require the `X-Internal-Secret` header with the value of `INTERNAL_API_SECRET`. These are never exposed to the browser.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/internal/teachers` | Provision a teacher account (operator-initiated, bypasses self-registration) |
| `PATCH` | `/internal/teachers/{uid}/flags` | Set pilot/free/paid plan flag and evidence metadata |

---

## Security Model

- **Workspace isolation.** `OwnershipVerifier` is injected into any service that returns teacher-scoped resources. It throws `ResourceNotFoundException` (404) on cross-teacher access — intentionally 404, not 403, to avoid leaking resource existence.
- **No credentials stored.** The API validates Firebase ID tokens but never stores passwords or raw tokens. The Firebase UID is the teacher's primary key.
- **Internal surface locked down.** `/internal/**` is unreachable without the correct `X-Internal-Secret`. Do not expose port 8080 publicly in production — traffic must come through the load balancer, which strips the header before forwarding to the public surface.
- **Structured JSON logs.** All logs are emitted as JSON via `logstash-logback-encoder`, compatible with Cloud Logging's structured log ingestion.

---

## Database Schema

Managed by Flyway. Migrations live in `src/main/resources/db/migration/`.

| Migration | Description |
|-----------|-------------|
| `V1__create_teacher_table.sql` | `teacher` table with `firebase_uid` as PK and unique email index |
| `V2__add_pilot_flag_columns.sql` | `plan_type`, `related_party`, `offer_details`, `evidence_link`, `flag_set_by`, `flag_set_at` |

---

## Docker

```bash
docker build -t grade-ops-api .

docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/gradeops \
  -e DATABASE_USER=gradeops \
  -e DATABASE_PASSWORD=gradeops \
  -e INTERNAL_API_SECRET=dev-secret \
  -e GOOGLE_APPLICATION_CREDENTIALS=/creds/firebase.json \
  -v /path/to/firebase.json:/creds/firebase.json:ro \
  grade-ops-api
```

---

## Related Repositories

| Repo | Description |
|------|-------------|
| [`grade-ops-ai-web`](../web/) | Next.js teacher workspace — API consumer |
| [`grade-ops-ai-agents`](../agents/) | Spring AI agent runtime — calls `/internal/**` |
| [`grade-ops-ai-infra`](../infra/) | Terraform — GCP infrastructure provisioning |
| [`grade-ops-ai-docs`](../docs/) | Canonical product and architecture documentation |
