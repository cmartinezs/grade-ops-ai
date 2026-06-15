# Repository Structure

GradeOps AI is organized in five repositories. This document defines the recommended internal structure for each one, aligned with the technology stack decisions and the agent responsibilities established in the canonical documentation.

The goal is a starting structure that is coherent, navigable, and avoids over-engineering before the product is validated.

---

## `grade-ops-ai-docs`

Documentation-only repository. The canonical source of truth for product strategy, architecture, agents, business, evidence, and durable decisions.

```text
grade-ops-ai-docs/
├── 00-project/          # Vision, pitch, problem, solution, roadmap, hackathon strategy
├── 01-business/         # Business model, pricing, go-to-market, customer discovery
├── 02-product/          # Personas, MVP scope, user stories, workflows, metrics
├── 03-ai-agents/        # Agent roles, responsibilities, contracts, logs
├── 04-architecture/     # System design, data model, API design, security, deployment
├── 05-evidence/         # Usage, revenue, agent logs, testimonials, users
├── 06-ux/               # Screen inventory, interaction model, UX design intent
├── 07-hackathon/        # Demo script, evidence checklist, submission narrative
├── 99-decisions/        # Durable decision records (ADR format)
├── .raw/                # Historical conversation notes — not canonical
├── .all-by-category/    # Generated consolidations for NotebookLM — do not edit directly
├── CLAUDE.md
└── README.md
```

See [`CLAUDE.md`](../CLAUDE.md) for writing conventions and content rules.

---

## `grade-ops-ai-web`

Teacher and student-facing frontend. Built with Next.js + TypeScript + Tailwind CSS.

```text
grade-ops-ai-web/
├── src/
│   ├── app/                       # Next.js App Router
│   │   ├── (public)/              # Public pages (no auth required)
│   │   │   ├── page.tsx           # Landing page
│   │   │   └── pricing/
│   │   ├── (auth)/                # Login, register, password reset
│   │   │   └── login/
│   │   ├── (teacher)/             # Teacher workspace (authenticated)
│   │   │   ├── dashboard/         # Assessment overview, pending approvals
│   │   │   ├── assessments/       # Create, view, manage assessments
│   │   │   ├── rubrics/           # Rubric review and approval
│   │   │   ├── submissions/       # Upload and manage student submissions
│   │   │   ├── grading/           # Review grading suggestions
│   │   │   ├── feedback/          # Approve or edit feedback drafts
│   │   │   ├── gaps/              # Learning gap summaries
│   │   │   ├── reports/           # Teacher reports and exports
│   │   │   ├── question-bank/     # Closed assessment — question curation
│   │   │   └── agent-logs/        # Agent execution log viewer
│   │   ├── (student)/             # Student access (link-based, no account)
│   │   │   ├── assessment/[token] # Secure link to assessment form
│   │   │   └── results/[token]    # Secure link to published results
│   │   └── layout.tsx
│   ├── components/
│   │   ├── ui/                    # Generic UI primitives
│   │   ├── assessment/            # Assessment creation and review components
│   │   ├── rubric/                # Rubric viewer and approval
│   │   ├── grading/               # Grading suggestion cards
│   │   ├── feedback/              # Feedback approval UI
│   │   ├── agent-logs/            # Agent run log viewer
│   │   └── evidence/              # Evidence dashboard components
│   ├── hooks/
│   ├── lib/
│   │   ├── api/                   # API client (typed HTTP calls to backend)
│   │   └── auth/                  # Auth helpers
│   ├── types/                     # Shared TypeScript types aligned with API contracts
│   └── styles/
├── public/
├── next.config.ts
├── tailwind.config.ts
└── package.json
```

**Routing summary:**

| Route | Purpose |
|-------|---------|
| `/` | Landing page |
| `/pricing` | Plan and pricing information |
| `/login` | Authentication |
| `/teacher/dashboard` | Assessment workspace home |
| `/teacher/assessments` | Assessment list, creation |
| `/teacher/grading` | Grading review queue |
| `/teacher/reports` | Reports and exports |
| `/teacher/agent-logs` | AI execution evidence |
| `/teacher/question-bank` | Closed assessment question curation |
| `/student/assessment/[token]` | Student response form (secure link) |
| `/student/results/[token]` | Published results (secure link) |

---

## `grade-ops-ai-api`

Business backend. Spring Boot 4 + Java 21 + PostgreSQL. Owns all domain logic, persistence, workflow state, and billing.

```text
grade-ops-ai-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ai/gradeops/api/
│   │   │       ├── identity/          # Users, sessions, JWTs, passwords
│   │   │       ├── tenant/            # Organizations, plans, settings
│   │   │       ├── assessment/        # Assessment creation, states, lifecycle
│   │   │       ├── rubric/            # Rubric generation requests, criteria, approval
│   │   │       ├── submission/        # StudentSubmission ingestion and storage
│   │   │       ├── grading/           # Grading results, suggestions, teacher edits
│   │   │       ├── feedback/          # Feedback drafts, approval, delivery
│   │   │       ├── gap/               # Learning gap summaries per assessment
│   │   │       ├── recovery/          # Recovery activity suggestions
│   │   │       ├── report/            # Teacher report generation and export
│   │   │       ├── questionbank/      # Question items, curation states, answer keys
│   │   │       ├── learner/           # LearnerRef, AssessmentInvitation, AssessmentAttempt
│   │   │       ├── billing/           # Plans, usage counters, Stripe webhooks
│   │   │       ├── audit/             # Approval events, user actions, evidence records
│   │   │       ├── agentclient/       # HTTP client to grade-ops-ai-agents service
│   │   │       └── shared/            # Cross-cutting: exceptions, validation, pagination, base entities
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-demo.yml
│   │       └── db/
│   │           └── migration/         # Flyway migration scripts (V001__init.sql, …)
│   └── test/
│       └── java/
│           └── ai/gradeops/api/       # Integration and unit tests per module
├── Dockerfile
└── pom.xml
```

**Module responsibilities:**

| Module | Owns |
|--------|------|
| `identity` | Authentication, JWT issuance, password lifecycle |
| `tenant` | Organization, plan, feature flags |
| `assessment` | Assessment lifecycle and workflow state machine |
| `rubric` | Rubric generation triggers, criteria storage, locking |
| `submission` | Open assessment submission ingestion and storage |
| `grading` | Grading suggestions, score persistence, teacher edits |
| `feedback` | Feedback drafts, approval states, delivery records |
| `gap` | Cohort-level gap detection summaries |
| `recovery` | Remedial activity records |
| `report` | Teacher report assembly and export |
| `questionbank` | Question item generation, distractor/ambiguity review states, curation, bank composition |
| `learner` | LearnerRef records, invitation tokens, closed assessment attempts, result publication |
| `billing` | Plan entitlement, graded submission counters, Stripe webhook handling |
| `audit` | Approval events, user action history, AgentExecutionLog persistence |
| `agentclient` | HTTP client calls to the agent service; maps agent DTOs to API domain |
| `shared` | Common base entities, error codes, pagination, validation utilities |

The `agentclient` module is the only one that knows the agent service exists. All other modules call agents through it. Domain modules do not import Spring AI directly.

---

## `grade-ops-ai-agents`

Agent runtime. Spring Boot 4 + Java 21 + Spring AI + Vertex AI Gemini. Owns all agent implementations, prompts, structured output contracts, and execution logs.

```text
grade-ops-ai-agents/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ai/gradeops/agents/
│   │   │       ├── open/
│   │   │       │   ├── assessment/        # Assessment Agent
│   │   │       │   ├── rubric/            # Rubric Agent
│   │   │       │   ├── grading/           # Grading Agent
│   │   │       │   ├── feedback/          # Feedback Agent
│   │   │       │   ├── learninggap/       # Learning Gap Agent
│   │   │       │   ├── recovery/          # Recovery Agent
│   │   │       │   └── teacherreport/     # Teacher Report Agent
│   │   │       ├── closed/
│   │   │       │   ├── questiongeneration/    # Question Generation Agent
│   │   │       │   ├── distractorquality/     # Distractor Quality Agent
│   │   │       │   ├── ambiguityreview/       # Ambiguity Review Agent
│   │   │       │   ├── assessmentassembly/    # Assessment Assembly Agent
│   │   │       │   └── itemanalytics/         # Item Analytics Agent
│   │   │       ├── ops/
│   │   │       │   └── evidence/          # Ops Agent — usage, cost, log summaries
│   │   │       ├── shared/
│   │   │       │   ├── envelope/          # Common AgentCommand input structure
│   │   │       │   ├── logging/           # AgentExecutionLog recording
│   │   │       │   ├── output/            # Shared structured output base types
│   │   │       │   └── cost/              # Token and cost estimation utilities
│   │   │       └── provider/
│   │   │           └── gemini/            # Spring AI Vertex AI configuration and adapters
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-demo.yml
│   │       └── prompts/                   # Versioned prompt templates per agent
│   │           ├── open/
│   │           │   ├── assessment-v1.st   # StringTemplate prompt files
│   │           │   ├── rubric-v1.st
│   │           │   ├── grading-v1.st
│   │           │   └── …
│   │           └── closed/
│   │               ├── question-generation-v1.st
│   │               ├── distractor-quality-v1.st
│   │               └── …
│   └── test/
│       └── java/
│           └── ai/gradeops/agents/        # Agent unit tests and structured output validation
├── Dockerfile
└── pom.xml
```

**Agent pattern per package:**

Each agent package contains:

| Class | Purpose |
|-------|---------|
| `{Agent}Command` | Input envelope (received from API) |
| `{Agent}Result` | Structured output (returned to API) |
| `{Agent}Service` | Orchestration: load prompt → call Gemini → validate output → log execution |
| `{Agent}Controller` | REST endpoint (POST `/agents/open/grading`, etc.) |

**Execution log fields captured for every agent run:**

| Field | Notes |
|-------|-------|
| `agent_name` | Canonical agent identifier |
| `assessment_id` | Assessment the run belongs to |
| `submission_id` | Submission if applicable; null for assessment-level agents |
| `teacher_id` | Requesting teacher |
| `model_used` | Exact model name from Gemini response |
| `input_token_estimate` | Tokens in prompt |
| `output_token_estimate` | Tokens in response |
| `estimated_cost_usd` | Calculated from model pricing |
| `status` | `succeeded` / `failed` / `requires_human_review` |
| `teacher_approval_state` | `pending` / `approved` / `edited` / `rejected` |
| `estimated_minutes_saved` | For evidence dashboard |
| `uncertainty_flags` | List of structured flags (see agents-overview) |

---

## `grade-ops-ai-infra`

Infrastructure as code and CI/CD pipelines. Terraform (or OpenTofu) + GitHub Actions. Provisions and deploys all services to Google Cloud.

```text
grade-ops-ai-infra/
├── terraform/
│   ├── modules/
│   │   ├── cloud-run/             # Reusable Cloud Run service module
│   │   ├── cloud-sql/             # PostgreSQL instance and database
│   │   ├── cloud-storage/         # Storage buckets and lifecycle rules
│   │   ├── secret-manager/        # Secret creation and IAM bindings
│   │   └── iam/                   # Service accounts and roles
│   └── environments/
│       ├── local/                 # Not provisioned; developer machine only
│       ├── demo/                  # Hackathon demo environment (main target)
│       │   ├── main.tf
│       │   ├── variables.tf
│       │   └── terraform.tfvars.example
│       └── prod/                  # Post-hackathon production
│           ├── main.tf
│           ├── variables.tf
│           └── terraform.tfvars.example
├── github-actions/
│   ├── workflows/
│   │   ├── web-ci.yml             # Lint, build, test for grade-ops-ai-web
│   │   ├── api-ci.yml             # Lint, build, test for grade-ops-ai-api
│   │   ├── agents-ci.yml          # Lint, build, test for grade-ops-ai-agents
│   │   ├── deploy-demo.yml        # Manual or tag-triggered deploy to demo
│   │   └── infra-plan.yml         # Terraform plan on PR; apply on merge
├── scripts/
│   ├── seed-demo-data.sh          # Populate demo environment with seed data
│   ├── create-secrets.sh          # Push secrets to Secret Manager
│   └── health-check.sh            # Post-deploy health verification
└── README.md
```

**Google Cloud services provisioned:**

| Service | Used for |
|---------|---------|
| Cloud Run | `grade-ops-ai-web`, `grade-ops-ai-api`, `grade-ops-ai-agents` services |
| Cloud SQL (PostgreSQL) | Primary database for API |
| Cloud Storage | Student submissions, report exports, evidence artifacts |
| Secret Manager | Gemini API key, DB credentials, JWT secret |
| Cloud Logging | Technical runtime logs for all services |
| Artifact Registry | Docker image storage |
| IAM | Service account roles and service-to-service auth |

**Service-to-service auth:** `grade-ops-ai-api` calls `grade-ops-ai-agents` via Cloud Run internal URL. Authentication uses OIDC tokens issued to the API service account. The agent service is not publicly reachable.

---

## Cross-Repo Alignment Rules

1. **Types flow from API to Web.** The web frontend mirrors the API's DTO contracts. No independent type definitions for shared structures.
2. **Agents do not own domain entities.** Agent packages receive structured commands and return structured results; they do not persist domain records directly. Persistence is the API's responsibility.
3. **Prompts are versioned and file-based.** Prompt templates live in `grade-ops-ai-agents/src/main/resources/prompts/` as named, versioned files — not as inline strings in Java.
4. **Infrastructure is environment-aware.** The demo environment is the primary target for hackathon delivery. Production and staging are not required for the MVP, but the structure must support them.
5. **Migration scripts are the source of schema truth.** The API's `db/migration/` folder is the authoritative record of the database schema. No undocumented manual changes to production schema.

<!-- nav -->

---

← [Deployment](deployment.md) | [↑ inicio](#repository-structure) | [README](README.md) | [System Architecture →](system-architecture.md)
