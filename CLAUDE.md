# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

GradeOps AI is an AI-native assessment operations platform for programming educators. Teachers run assessment cycles — from learning goal through grading, feedback, gap detection, and reports — using a pipeline of AI agents, while retaining final approval over all outputs.

Two assessment modes:
- **Open** — practical code/text submissions, rubric-based AI grading suggestion, teacher approval required.
- **Closed** — objective questions (TF/SC/MC), AI-native question bank, fully deterministic grading against a frozen answer key.

This directory is the root of a multi-repo workspace. Each subdirectory is an independent git repository at a different stage of development.

## Repository map

| Directory | Repo | Stack | Status |
|-----------|------|-------|--------|
| `docs/` | `grade-ops-ai-docs` | Markdown | Active — canonical documentation |
| `web/` | `grade-ops-ai-web` | Next.js + TypeScript + Tailwind CSS | Scaffolding |
| `api/` | `grade-ops-ai-api` | Spring Boot 4 + Java 21 + PostgreSQL | Scaffolding |
| `agents/` | `grade-ops-ai-agents` | Spring Boot 4 + Java 21 + Spring AI + Vertex AI Gemini | Scaffolding |
| `infra/` | `grade-ops-ai-infra` | Terraform + GitHub Actions | Scaffolding |

Each repo has its own git history. Commits must be made inside the relevant subdirectory.

## Commands

### Web (`web/` — Next.js)
```bash
npm install          # install dependencies
npm run dev          # dev server (localhost:3000)
npm run build        # production build
npm run lint         # ESLint
npm run test         # Jest tests
```

### API (`api/` — Spring Boot / Maven)
```bash
./mvnw spring-boot:run                          # start dev server
./mvnw test                                     # run all tests
./mvnw test -Dtest=ClassName                    # run a single test class
./mvnw test -Dtest=ClassName#methodName         # run a single test method
./mvnw spring-boot:run -Dspring.profiles.active=local
./mvnw flyway:migrate                           # run DB migrations manually
```

### Agents (`agents/` — Spring Boot / Maven)
```bash
./mvnw spring-boot:run
./mvnw test
./mvnw test -Dtest=ClassName#methodName
```

### Infrastructure (`infra/` — Terraform)
```bash
terraform -chdir=terraform/environments/demo init
terraform -chdir=terraform/environments/demo plan
terraform -chdir=terraform/environments/demo apply
```

### Docs (`docs/` — Markdown)
No build step. Edit Markdown files directly. See `docs/CLAUDE.md` for content rules.

## Architecture

The system is a **modular monolith + separate agent runtime**, both deployed on Cloud Run.

```
Browser → Web (Next.js) → API (Spring Boot) → Agents (Spring Boot / Spring AI)
                                     ↓                        ↓
                              Cloud SQL (PG)          Vertex AI Gemini
                              Cloud Storage
```

- **Web** handles teacher workspace, student access (secure token links, no login), and dashboards.
- **API** owns all domain logic, workflow state machine, persistence, and billing. It calls the agent service via the `agentclient` module — no other module imports Spring AI.
- **Agents** expose a REST API internally (service-to-service OIDC auth, not public). Each agent follows a fixed pattern: validate command → load data → build envelope → call Gemini → validate structured output → log execution → return result.
- **Prompts** are versioned file-based templates in `agents/src/main/resources/prompts/` (StringTemplate `.st` files). Never inline prompts in Java code.
- **Infra** provisions Cloud Run, Cloud SQL, Cloud Storage, Secret Manager, Artifact Registry, and IAM via Terraform. The `demo` environment is the primary hackathon target.

## Agent pipeline

Thirteen agents cover both assessment modes. Every run produces a structured `AgentExecutionLog` record.

**Open assessment:** Assessment → Rubric → Grading → Feedback → Learning Gap → Recovery → Teacher Report → Ops Evidence

**Closed assessment:** Question Generation → Distractor Quality → Ambiguity Review → Assessment Assembly → Item Analytics

Agents generate and suggest; they never finalize scores, silently modify approved rubrics, or deliver output to students without teacher approval. Closed assessment grading is always deterministic.

## Key cross-cutting rules

- **Types flow from API to Web.** The frontend mirrors API DTO contracts; no independent shared-type definitions.
- **Agents do not own domain entities.** They receive `{Agent}Command`, return `{Agent}Result`, and persist nothing directly. Persistence is the API's responsibility.
- **Schema migrations live in `api/src/main/resources/db/migration/`** (Flyway). No undocumented manual schema changes.
- **Teacher approval is explicit.** Every AI-generated output that affects grading, feedback, or student-facing content requires an `ApprovalEvent` before it is acted on.
- **Evidence is core, not a side-effect.** `AgentExecutionLog`, `ApprovalEvent`, `UsageEvent`, `RevenueEvent`, and `CostEvent` are first-class entities, not afterthoughts.
- **No student login in MVP.** Students access assessments and results via signed token links (`AssessmentInvitation`). `LearnerRef` is a minimal reference record, not an account.
- **Related-party revenue must be flagged.** `RevenueEvent.related_party` is required for hackathon evidence reporting.
- **Gemini API key is server-side only.** Never expose it to the frontend.

## Google Cloud targets

Primary runtime: Cloud Run (web, api, agents). Database: Cloud SQL PostgreSQL. Files: Cloud Storage. Secrets: Secret Manager. Logs: Cloud Logging.

For local dev, use a local PostgreSQL instance and a Gemini API key (not Vertex AI) configured in `application-local.yml`.
