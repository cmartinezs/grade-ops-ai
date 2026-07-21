# Environment Roles

- Status: Accepted
- Date: 2026-07-20
- Decision owner: Architecture / Founder

## Context

GradeOps AI documentation uses multiple environment names for different operational needs. The repo contains a documented `beta` environment intended for no-credit-card, free-tier operation and a `demo` environment intended for a production-like Google Cloud footprint.

Current verified repository state:

- `docs/04-architecture/beta-environment-design.md` documents `beta` as Vercel + Render + Neon + Cloudflare R2 + Firebase Authentication.
- The same document documents `demo` as Firebase App Hosting + Cloud Run + Cloud SQL + Cloud Storage + Firebase Authentication + Vertex AI Gemini.
- `infra/terraform/environments/demo/cloud_run.tf` provisions Cloud Run services for `api` and `agents`, but uses placeholder container images and `lifecycle.ignore_changes = [template]`.
- `infra/terraform/environments/demo/firebase_app_hosting.tf` only enables the Firebase App Hosting API; backend creation is manual through Firebase CLI / GitHub OAuth.
- `infra/terraform/environments/demo/groq.tf` provisions `GRADEOPS_GROQ_API_KEY`, and the demo agents service sets `GRADEOPS_GROQ_MODEL`.
- The Master Plan keeps D-01 open because final environment ownership and deployment evidence still need confirmation.

## Decision

Document `beta` and `demo` as separate environment roles:

| Environment | Role | Current meaning |
|---|---|---|
| `beta` | Product-evidence environment | Fast iteration and real functional evidence using free-tier hosting and implemented provider adapters. |
| `demo` | Google Cloud target | Google Cloud footprint and Gemini-capable path, implemented through Terraform plus manual Firebase App Hosting steps where required. |

The source documentation must not claim that the product is fully deployed on Google Cloud until deployment evidence exists.

The source documentation may say:

- `beta` is the current evidence/iteration environment when supported by local planning or deployment evidence.
- `demo` is the target Google Cloud environment and must produce proof before it is used for customer-facing claims.
- both environments share application code but activate different infrastructure adapters and profiles.

## Rationale

This avoids two failure modes:

1. overstating Google Cloud deployment before evidence exists;
2. hiding the real beta path that already informs architecture, provider policy, and operational evidence.

Separating roles lets the team keep moving with a working environment while preserving a credible Google Cloud / Gemini deployment path.

## Consequences

- `docs/04-architecture/system-architecture.md` and deployment docs must show both environment roles.
- Terraform docs must state which resources are fully managed and which require manual steps.
- R06 in the Master Plan remains responsible for operational readiness and validation evidence.
- If a customer-facing demo uses only `beta`, documentation must avoid implying Google Cloud production deployment.

<!-- nav -->

---

← [Agent Provider And Model Policy](2026-07-20-agent-provider-model-policy.md) | [↑ inicio](#environment-roles) | [README](README.md) | [Archive Event-Specific Constraints →](2026-07-20-archive-event-specific-constraints.md)
