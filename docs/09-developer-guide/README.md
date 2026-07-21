# Developer Guide

This guide is for engineers working on the GradeOps AI codebase. It covers local environment setup, repository layout, the implemented API/agent slice, and the security model. It assumes familiarity with Java/Spring Boot, Spring AI, React/Next.js, and PostgreSQL.

---

## What this guide covers

| Document | Topic |
|----------|-------|
| [00-gcp-project-setup.md](00-gcp-project-setup.md) | **Start here for cloud deployment** — GCP credits, billing, Firebase linking, Terraform apply |
| [01-local-setup.md](01-local-setup.md) | Step-by-step local development environment setup for all repos |
| [02-repository-map.md](02-repository-map.md) | Package-level map of every module, class, and resource file |
| [03-api-reference.md](03-api-reference.md) | All implemented REST endpoints with request/response schemas and curl examples |
| [04-security-implementation.md](04-security-implementation.md) | Filter chain, Firebase token flow, ownership verification, and the web interceptor |
| [05-database-guide.md](05-database-guide.md) | Schema actual, convenciones Flyway, cómo agregar migraciones |
| [06-agent-development.md](06-agent-development.md) | Patrón de agente Spring AI, prompt templates, output validation |
| [07-web-development.md](07-web-development.md) | Next.js conventions, apiClient, AuthGuard, agregar páginas |
| [08-testing-guide.md](08-testing-guide.md) | @SpringBootTest vs @WebMvcTest, Firebase stubs, Jest patterns |
| [09-deployment-guide.md](09-deployment-guide.md) | Terraform, Cloud Run, Secret Manager, CI/CD baseline |

---

## Prerequisites

Before starting, verify the following tools are installed:

| Tool | Minimum version | Verify |
|------|----------------|--------|
| Java (OpenJDK or Amazon Corretto) | 21 | `java -version` |
| Maven | Ships with project (use `./mvnw`) | `./mvnw --version` |
| Node.js | 18 | `node -v` |
| npm | 9 | `npm -v` |
| Docker | 24 | `docker --version` |
| Git | Any recent version | `git --version` |
| Firebase project | Email/password sign-in enabled | Firebase console |

---

## Quick start (3 commands)

These assume the API local profile can start PostgreSQL through `api/compose.yml` and that Firebase local config is present (see [01-local-setup.md](01-local-setup.md)):

```bash
# 1. Start the API
cd api && ./mvnw spring-boot:run -Dspring.profiles.active=local

# 2. Start the web app in a separate terminal
cd web && npm install && npm run dev

# 3. Verify the API is healthy
curl http://localhost:8080/actuator/health
```

The web app is then available at http://localhost:3000 and the API at http://localhost:8080. Assessment draft generation also requires the agents service on http://localhost:8081.

---

## What is currently implemented

### Implemented now

The following is implemented and covered by local tests:

- Teacher self-registration via Firebase email/password
- Google sign-in provider support in the auth domain
- Firebase ID token verification on every protected API request
- Email verification enforcement (unverified users are blocked)
- Teacher record persistence in PostgreSQL (`teacher` table)
- Password reset code lifecycle and email adapter
- Sign-out with server-side Firebase refresh token revocation
- Operator provisioning of teacher accounts via internal API (bypasses email verification)
- Pilot flag management on teacher records (plan type, related-party flag, evidence link)
- Assessment aggregate, brief, draft versioning, and assessment list persistence
- API `agentclient` integration with the internal agents service
- Agents service Assessment Agent vertical slice at `POST /internal/agents/assessment`
- Gemini and Groq provider adapters for assessment draft generation
- `AgentExecutionLog` persistence for assessment draft agent calls
- Web: AuthGuard, login/register/verify-email/forgot-password/reset-password pages, dashboard shell, and protected placeholder routes

### Planned next

The following have documented designs but are not yet implemented end to end:

- **Epic 03** — AI rubric generation and approval
- **Epic 04** — Student submission ingestion
- **Epic 05** — AI grading suggestions and teacher review
- **Epic 06** — AI feedback drafts and approval
- **Epic 07** — Learning gap detection
- **Epic 08** — Recovery activity suggestions
- **Epic 09** — Teacher report generation and export
- **Epic 10** — Closed assessment: question bank and generation
- **Epic 11** — Closed assessment: distractor and ambiguity review
- **Epic 12** — Closed assessment: assessment assembly
- **Epic 13** — Item analytics and evidence dashboard

Closed assessment user stories are P0 in the product cut, but the current code does not yet implement question bank, signed student links, deterministic attempt grading, or item analytics. See [`docs/04-architecture/api-design.md`](../04-architecture/api-design.md) for the planned endpoint contracts.

---

## Java base package

The correct base package for all Java code is:

```
cl.gradeops.ai.<artifact>
```

For the API: `cl.gradeops.ai.api`
For the agents: `cl.gradeops.ai.agents`

Do not use `com.gradeops.*` or any other variation. The Maven `groupId` is also `cl.gradeops.ai`.

---

<!-- nav -->

[↑ Top](#developer-guide) | [Local Setup →](01-local-setup.md)
