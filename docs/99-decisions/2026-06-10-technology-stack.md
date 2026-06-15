# Technology Stack

- Status: Accepted
- Date: 2026-06-10
- Decision owner: Architecture / Founder

## Context

GradeOps AI is organized in five repositories: `docs`, `web`, `api`, `agents`, `infra`. Each requires a technology stack decision. The constraints are:

- **Hackathon speed**: the MVP must be demonstrable quickly; unfamiliar stacks increase delivery risk.
- **Developer profile**: strong background in Java/Spring Boot; limited prior production experience with Node.js or Python backends.
- **Google Cloud requirement**: the runtime must use Google Cloud services and Gemini / Vertex AI.
- **AI-native positioning**: agents must produce structured, observable outputs — not raw LLM text.
- **Frontend visibility**: the teacher workspace needs a modern, fast-to-build UI.

Options considered for the agent runtime:

| Option | Stack | Assessment |
| --- | --- | --- |
| A | Node.js / TypeScript | Fast for prompt iteration; comfortable JSON/schema tooling; adds a second backend language |
| B | Python / FastAPI | Strong ML ecosystem; would require learning FastAPI and managing a third language |
| C | Java / Spring Boot + Spring AI | Aligns with developer profile; Spring AI provides structured output, tool calling, observability, and Gemini integration |

## Decision

| Repository | Stack |
| --- | --- |
| `grade-ops-ai-web` | Next.js + TypeScript + Tailwind CSS |
| `grade-ops-ai-api` | Spring Boot 4 + Java 21 + PostgreSQL |
| `grade-ops-ai-agents` | Spring Boot 4 + Java 21 + Spring AI + Gemini / Vertex AI |
| `grade-ops-ai-infra` | Terraform (or OpenTofu) + GitHub Actions + Google Cloud |
| `grade-ops-ai-docs` | Markdown |

TypeScript is scoped to the frontend only. Both backend services use Java 21.

## Rationale

**`grade-ops-ai-web` — Next.js + TypeScript**

- Modern full-stack React framework with fast iteration and good DX.
- TypeScript provides type safety across the frontend codebase.
- Tailwind CSS enables rapid UI composition without context-switching to CSS files.
- No need to choose between Angular and Next.js: Next.js is lighter for a focused MVP workspace.

**`grade-ops-ai-api` — Spring Boot 4 + Java 21**

- Directly aligned with the developer's existing expertise; no ramp-up cost.
- Spring Boot's modular architecture maps cleanly to the domain modules (assessment, rubric, submission, grading, feedback, billing, audit).
- Spring Security, Spring Data JPA, and Spring Validation provide robust, production-grade building blocks.
- Java 21 adds virtual threads (Project Loom), which simplifies async I/O patterns.

**`grade-ops-ai-agents` — Spring Boot + Spring AI**

Spring AI is not only a wrapper for LLM calls. It provides:

| Capability | Relevance for GradeOps AI |
| --- | --- |
| Chat model abstraction | Uniform interface to Gemini without vendor lock-in |
| Structured output | Converts model responses to typed Java objects / JSON; required for reliable persistence |
| Tool / function calling | Agent can request tool execution; application retains control of what executes |
| Observability | Built-in metrics and tracing for model calls, latency, and cost |
| RAG support | Future: grounding grading agents on curriculum material |
| Response evaluation | Future: automated quality checks on agent outputs |
| Spring Boot autoconfiguration | No manual wiring; config via `application.properties` |

Vertex AI Gemini starter:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-vertex-ai-gemini</artifactId>
</dependency>
```

Configured via `spring.ai.vertex.ai.gemini.project-id`, `location`, credentials, and model name. Use API key (Gemini Developer API) for local development; Vertex AI credentials for demo and production environments.

**`grade-ops-ai-infra` — Terraform + GitHub Actions**

- Infrastructure as code, version-controlled alongside the rest of the system.
- Terraform (or OpenTofu) manages Cloud Run services, Cloud SQL, Cloud Storage, Secret Manager, and IAM.
- GitHub Actions handles CI/CD: build, test, container image push, and deploy to Cloud Run.

## Consequences

- TypeScript is not used in the backend. All backend logic lives in Java 21.
- Agent structured outputs are validated Java POJOs or `Map<String, Object>` via Spring AI's `StructuredOutputConverter`.
- Stack fragmentation is minimized: developers working on `api` can contribute to `agents` and vice versa.
- Spring AI is still a relatively young framework (1.x); some abstractions may evolve. Pin versions and test model integrations early.
- The team must evaluate Spring AI's Gemini integration in an early spike before committing to it for the full agent pipeline.
- Angular is not used; if the team later needs it, a new web repo can be created without affecting the backend.

<!-- nav -->

---

← [Curriculum Taxonomy](2026-06-10-curriculum-taxonomy.md) | [↑ inicio](#technology-stack) | [README](README.md) | [Agent Runtime Separation →](2026-06-10-agent-runtime-separation.md)
