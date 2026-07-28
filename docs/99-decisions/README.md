# 99 Decisions

This folder records important decisions that should remain visible over time.

It answers:

> What did we decide, why did we decide it, and what tradeoffs does that create?

## Essence

`99-decisions` is for durable decisions, not notes.

Use this folder when a choice affects multiple documents, implementation direction, business strategy, architecture, scope, or future contributors.

## How To Use This Folder

Use [`adr-template.md`](adr-template.md) to create new decision records.

Recommended naming:

```text
YYYY-MM-DD-short-decision-title.md
```

Examples:

- [`2026-06-08-use-gradeops-ai-name.md`](2026-06-08-use-gradeops-ai-name.md);
- [`2026-06-08-price-by-graded-submissions.md`](2026-06-08-price-by-graded-submissions.md);
- [`2026-06-08-mermaid-diagram-standard.md`](2026-06-08-mermaid-diagram-standard.md).

## What Belongs Here

- Architecture decisions.
- Business model decisions.
- Product scope decisions.
- Naming decisions.
- Pricing model decisions.
- Product, environment, or external-claim interpretation decisions.
- Documentation conventions that affect the whole repo.

## What Does Not Belong Here

- Meeting notes.
- Raw brainstorming.
- Temporary todos.
- Implementation details that are not decision-worthy.

## Decision Quality Bar

A decision record should explain:

- context;
- decision;
- rationale;
- consequences;
- owner;
- status;
- date.

## Active Decision Records

| File | Topic | Status |
| --- | --- | --- |
| [`2026-06-08-use-gradeops-ai-name.md`](2026-06-08-use-gradeops-ai-name.md) | Product is named GradeOps AI; "Ops" signals workflow platform, not a quiz tool | Accepted |
| [`2026-06-08-price-by-graded-submissions.md`](2026-06-08-price-by-graded-submissions.md) | Historical primary billing unit was the graded submission | Superseded |
| [`2026-06-08-mermaid-diagram-standard.md`](2026-06-08-mermaid-diagram-standard.md) | Mermaid is the default diagram format; PlantUML only when Mermaid is insufficient | Accepted |
| [`2026-06-10-closed-assessment-mode.md`](2026-06-10-closed-assessment-mode.md) | Adding closed (objective/alternatives) assessment mode alongside open mode | Accepted |
| [`2026-06-10-ai-native-question-bank.md`](2026-06-10-ai-native-question-bank.md) | Question bank is AI-generated and teacher-curated, not manually authored | Accepted |
| [`2026-06-10-assessment-snapshot-on-publish.md`](2026-06-10-assessment-snapshot-on-publish.md) | Questions, options, answer key, and scoring policy are frozen at publication | Accepted |
| [`2026-06-10-deterministic-grading-for-closed.md`](2026-06-10-deterministic-grading-for-closed.md) | Closed assessment grading is deterministic; no AI in the scoring calculation | Accepted |
| [`2026-06-10-student-access-via-secure-link.md`](2026-06-10-student-access-via-secure-link.md) | Students access assessments and results via signed email links; no account required | Accepted |
| [`2026-06-10-curriculum-taxonomy.md`](2026-06-10-curriculum-taxonomy.md) | Historical P0 string/P1 relational curriculum sequencing | Superseded |
| [`2026-06-10-technology-stack.md`](2026-06-10-technology-stack.md) | Next.js for web, Spring Boot + Java 21 for API and agents, Spring AI for the agent runtime | Accepted |
| [`2026-06-10-agent-runtime-separation.md`](2026-06-10-agent-runtime-separation.md) | `grade-ops-ai-agents` is a separate service; agents do not live inside the API | Accepted |
| [`2026-06-12-firebase-authentication.md`](2026-06-12-firebase-authentication.md) | Firebase Authentication as identity provider; ID token validation in `api/`; operator internal endpoint; pre-verified email for provisioned accounts; refresh-token revocation on sign-out | Accepted |
| [`2026-06-21-form-validation-react-hook-form-zod.md`](2026-06-21-form-validation-react-hook-form-zod.md) | Web forms use React Hook Form with Zod validation | Accepted |
| [`2026-06-21-web-design-system.md`](2026-06-21-web-design-system.md) | Web UI follows the documented design system and token set | Accepted |
| [`2026-07-20-agent-provider-model-policy.md`](2026-07-20-agent-provider-model-policy.md) | Historical provider/model-aware runtime policy with command-level selection | Superseded |
| [`2026-07-20-environment-roles.md`](2026-07-20-environment-roles.md) | `beta` is product-evidence environment; `demo` is the Google Cloud target with Gemini-capable provider path | Accepted |
| [`2026-07-20-archive-event-specific-constraints.md`](2026-07-20-archive-event-specific-constraints.md) | Event-specific constraints are historical only and must not govern active product/docs scope | Accepted |
| [`2026-07-20-api-agent-orchestration.md`](2026-07-20-api-agent-orchestration.md) | `api/` is the public intermediary between `web/` and `agents/`; endpoint tasks require API-Agent orchestration and Richardson REST maturity checks | Accepted |
| [`2026-07-21-security-authorization-by-release.md`](2026-07-21-security-authorization-by-release.md) | Security and authorization are implemented inside each functional release, not as a separate technical release | Accepted |
| [`2026-07-21-observability-telemetry-by-release.md`](2026-07-21-observability-telemetry-by-release.md) | Observability and telemetry are implemented inside each functional release, not as a separate technical release | Accepted |
| [`2026-07-21-testing-quality-by-release.md`](2026-07-21-testing-quality-by-release.md) | Testing and quality gates are implemented inside each functional release, not as a separate technical release | Accepted |
| [`2026-07-21-ui-design-data-semantics.md`](2026-07-21-ui-design-data-semantics.md) | UI work starts from the Design System, field data semantics, API I/O alignment and explicit sync/async contracts | Accepted |
| [`2026-07-21-i18n-by-release.md`](2026-07-21-i18n-by-release.md) | i18n is implemented inside each functional release while source code, technical contracts, logs and telemetry remain in English | Accepted |
| [`2026-07-22-master-plan-entry-decision.md`](2026-07-22-master-plan-entry-decision.md) | Pre-master-plan cleanup closes `008-assessment-creation` and `web/001-assessment-creation` — GO, no blocking conditions (initial CONDITIONAL GO condition retracted after verifying it was already fixed) | Accepted |
| [`2026-07-27-credit-based-pricing.md`](2026-07-27-credit-based-pricing.md) | Credits meter heterogeneous AI-assisted workflows while submissions remain an operational/value metric | Accepted |
| [`2026-07-27-policy-based-model-routing.md`](2026-07-27-policy-based-model-routing.md) | `agents/` selects provider/model through deterministic policy, authorized budgets and audited overrides | Accepted |

| [`2026-07-27-assessment-operations-product-redesign.md`](2026-07-27-assessment-operations-product-redesign.md) | Transversal assessment operations, independent lifecycles, explicit publication/republication, appeals, and curriculum-first analytics | Accepted |
| [`2026-07-28-assessment-authoring-model.md`](2026-07-28-assessment-authoring-model.md) | `Assessment` identity vs. `AssessmentBrief` intent vs. immutable `AssessmentRevision`; provenance, current-revision pointer, frozen `AssessmentStatus` | Accepted |
| [`2026-07-28-authoring-operation-contract.md`](2026-07-28-authoring-operation-contract.md) | Durable, recoverable, idempotent create/generate/retry/resume/regenerate/human-edit contract for `Assessment Authoring Operation Foundation` | Accepted |
| [`2026-07-28-idempotency-and-concurrency-strategy.md`](2026-07-28-idempotency-and-concurrency-strategy.md) | Idempotency key scope/storage/retention; optimistic locking + `expectedRevisionId` CAS for authoring mutations | Accepted |
| [`2026-07-28-durable-ai-operation-model.md`](2026-07-28-durable-ai-operation-model.md) | `AiOperation`/`AgentAttempt`: durable evidence before external dispatch, retry/attempt relationship, typed failure provenance | Accepted |

## Current Template

Use:

- [`adr-template.md`](adr-template.md)
