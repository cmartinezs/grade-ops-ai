# 04 Architecture

This folder defines the technical architecture for GradeOps AI MVP.

It translates the strategic, business, product, and agent documentation into a buildable system design.

## Architecture Goal

Support two complementary product outcomes:

> A programming educator can run open assessments (practical code/text, rubric-based, AI grading suggestion) and closed assessments (objective questions, AI-native question bank, deterministic grading) with teacher approval, structured persistence, cost tracking, and auditable evidence across both modes.

The architecture must prove:

- the product works end to end;
- AI operations are real and traceable;
- teachers remain in control;
- Google Cloud / Gemini requirements can be demonstrated;
- usage, cost, and evidence can be extracted for hackathon submission.

## Canonical Inputs

| Folder | Architecture Implication |
| --- | --- |
| `00-project/` | Defines strategic scope, hackathon requirements, cost policy, and non-negotiables. |
| `01-business/` | Defines paid pilots, pricing units, revenue/cost evidence, and GTM needs. |
| `02-product/` | Defines MVP scope, workflows, personas, stories, states, and metrics. |
| `03-ai-agents/` | Defines agent roles, contracts, logs, control points, and handoffs. |

## Architecture Principles

1. **Build the narrow workflow first.** Avoid full LMS, broad academic management, and enterprise complexity.
2. **Keep teacher approval explicit.** AI suggestions are not final until reviewed.
3. **Persist structured evidence.** Agent runs, model usage, cost estimates, approval states, and final actions are first-class records.
4. **Use bounded operational complexity.** Prefer a modular monolith plus agent worker over premature microservices.
5. **Separate product runtime from development tooling.** Production AI calls must use traceable API/cloud billing.
6. **Optimize for demo and pilot reliability.** The 3-minute demo must show product flow, agent logs, and business evidence.
7. **Use cloud services only where they reduce risk.** Do not over-engineer infrastructure before product validation.

## Recommended MVP Architecture

```text
Web App
  -> Backend API / Workflow Orchestrator
    -> Agent Runtime / Agent Worker
      -> Gemini API
    -> Database
    -> Object Storage
    -> Evidence / Logs
```

Recommended implementation stance:

| Layer | Recommended MVP Choice | Reason |
| --- | --- | --- |
| Web | Next.js + TypeScript | Fast teacher workspace and dashboard. See [[technology-stack]]. |
| API | Spring Boot modular monolith | Strong fit for workflow, security, audit, and persistence. |
| Agents | Separate agent worker/service or internal module | Keeps LLM orchestration observable and replaceable. |
| AI | Gemini API / Vertex AI Gemini | Hackathon requirement and traceable runtime. |
| DB | Cloud SQL PostgreSQL or Firestore | Structured workflow and evidence storage. |
| Files | Cloud Storage | Student files, report exports, evidence artifacts. |
| Runtime | Cloud Run | Simple container deployment and Google Cloud evidence. |
| Auth | Firebase Auth or backend JWT | Fast MVP authentication. |
| Observability | Cloud Logging + application evidence tables | Technical and business-grade logs. |

## Files In This Folder

| File | Purpose |
| --- | --- |
| [`system-architecture.md`](system-architecture.md) | System components, runtime topology, module boundaries, and diagrams. |
| [`repository-structure.md`](repository-structure.md) | Internal folder and module structure for each of the five repositories. |
| [`data-model.md`](data-model.md) | Core entities, relationships, states, audit/evidence records, and modeling rules. |
| [`api-design.md`](api-design.md) | REST API resources, commands, endpoints, DTO expectations, and error behavior. |
| [`security.md`](security.md) | Authentication, authorization, privacy, data handling, audit, and AI safety posture. |
| [`deployment.md`](deployment.md) | Environments, Google Cloud deployment, CI/CD, config, observability, and release strategy. |

## What Belongs Here

- logical architecture;
- component boundaries;
- data model;
- API design;
- deployment topology;
- security posture;
- observability design;
- evidence architecture;
- cloud service assumptions;
- technical constraints for MVP.

## What Does Not Belong Here

- sales scripts;
- pricing copy;
- customer interview notes;
- product backlog details beyond architecture implications;
- implementation code;
- full infrastructure-as-code definitions;
- vendor-specific tuning that belongs in implementation repos.

## Mermaid Rule

Architecture diagrams must use Mermaid by default.

Use PlantUML only if Mermaid cannot express the relationship clearly. Use ASCII only as a last fallback.

## Architecture Cut Line

Protect these architecture capabilities first:

1. authenticated teacher workspace;
2. assessment/rubric/submission persistence;
3. agent execution with Gemini;
4. teacher approval state;
5. agent log and cost evidence;
6. report generation;
7. basic deployment on Google Cloud;
8. demo-ready observability.

Everything else is secondary until the first real pilot works end to end.
