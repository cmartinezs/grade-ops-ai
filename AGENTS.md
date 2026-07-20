# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

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
No build step. Edit Markdown files directly. See `docs/AGENTS.md` for content rules.

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
- **Infra** provisions Cloud Run, Cloud SQL, Cloud Storage, Secret Manager, Artifact Registry, and IAM via Terraform. The `demo` environment is the primary Google Cloud target for product validation and production-like deployment.

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
- **Related-party revenue must be flagged.** `RevenueEvent.related_party` supports transparent revenue evidence and keeps pilot traction reporting auditable.
- **Gemini API key is server-side only.** Never expose it to the frontend.

## Planning conventions

Any planning scope that creates or modifies a service in `api/`, `agents/`, or `web/` **must include a corresponding infra task** that provisions the required Terraform resources. This task is never optional and must be DONE before the scope is marked complete.

Minimum infra checklist per service introduced:

| Introduced in | Required Terraform resources |
|---------------|------------------------------|
| `api/` | Cloud Run service, Cloud SQL database, Artifact Registry repo, IAM bindings, Secret Manager entries |
| `agents/` | Cloud Run service, Artifact Registry repo, IAM bindings (Vertex AI, Secret Manager) |
| `web/` | Cloud Run service (or static hosting), Artifact Registry repo, IAM bindings |

When expanding or deepening a planning, always check `infra/terraform/environments/demo/` to verify that each service referenced in code scopes has a corresponding `.tf` file. If it doesn't, add an infra scope or task explicitly.

### Commit scoping for parent/child plannings

When committing changes for a monorepo root planning that coordinates child plannings (see `.planning/GUIDE.md § Monorepo parent/child coordination`), **commits must be scoped per planning — the parent must never commit a child's changes, and vice versa.** This holds even though `api/`, `agents/`, `web/`, and the root currently share a single git history (verified 2026-07-09: no independent `.git` per subdirectory despite the "multi-repo" description above).

- Commit the parent planning's own files (root `.planning/`) in a separate commit from each child planning's files (`<child>/.planning/` and any child-owned implementation code).
- If one logical change touches both a parent and a child planning (e.g. creating a child planning and updating the parent's `Linked Child Plannings` section), split it into one commit per planning rather than a single mixed commit.
- Before checking which planning owns a given file, verify with `ls <child>/.planning/` whether that child directory actually has its own workspace — do not assume based on the repository map table alone.

### Git worktrees for child plannings

Follow the plugin's generic worktree-per-child-planning convention (`.planning/GUIDE.md § Workspace Boundary` / `§ Monorepo parent/child coordination`, plugin ≥ 3.6.0). Project-specific worktree names for this repo's child artifacts:

- `../gradeops-api` for a child planning owned by `api/`
- `../gradeops-agents` for a child planning owned by `agents/`

## Google Cloud targets

Primary runtime: Cloud Run (web, api, agents). Database: Cloud SQL PostgreSQL. Files: Cloud Storage. Secrets: Secret Manager. Logs: Cloud Logging.

For local dev, use a local PostgreSQL instance and a Gemini API key (not Vertex AI) configured in `application-local.yml`.
