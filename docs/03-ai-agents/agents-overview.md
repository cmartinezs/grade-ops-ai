# Agents Overview

GradeOps AI uses bounded AI agents inside a transversal assessment-operations platform. Agents return structured proposals and execution evidence; `api/` owns domain state, authorization, persistence, deterministic rules, approvals, publication, billing, and audit.

Programming-specific behavior is a specialization. Core agent contracts use assessment modality, curriculum context, evidence type, policy, and capabilities rather than assuming source code.

## Current Implementation Status

| Area | Status |
| --- | --- |
| Assessment Agent vertical slice | Implemented: `POST /internal/agents/assessment` with API orchestration |
| Gemini and Groq adapters | Implemented for the current Assessment Agent |
| Policy-based model routing | Planned incremental evolution |
| Generic runtime, registry, tool loop, async/cancel/resume | Planned |
| Remaining specialized agents | Contracted/planned; verify per release |

Documentation of a target agent is not evidence that it is implemented.

## Agent Families

| Family | Responsibilities |
| --- | --- |
| Design | Assessment, rubric, question and composition proposals |
| Quality | Rubric validation, distractor quality, ambiguity and alignment checks |
| Evaluation | Criterion-level open-evidence proposals; never deterministic closed scoring |
| Feedback | Student-facing drafts grounded in reviewed evidence |
| Analytics | Item patterns, curriculum gaps and evidence sufficiency |
| Pedagogical action | Reinforcement and recovery proposals |
| Reporting | Source-grounded teacher and operational summaries |
| Operations | Cost, usage, audit readiness and execution evidence |

The existing named catalog—Assessment, Rubric, Grading, Feedback, Learning Gap, Recovery, Teacher Report, Ops Evidence, Question Generation, Distractor Quality, Ambiguity Review, Assessment Assembly and Item Analytics—maps into these families. It may evolve as code-alignment reveals better boundaries.

## Contract Requirements

Every command supplies only authorized context and identifies:

- operation and requested capability;
- assessment/component/result versions;
- curriculum and policy context;
- evidence references rather than unrelated student data;
- budget/privacy/latency constraints;
- correlation and causation identifiers.

Every result separates:

- extracted facts;
- model interpretation;
- recommendation;
- requested action;
- uncertainty and missing context;
- execution metadata.

`NEEDS_INPUT` or a blocked result is preferable to fabrication.

## Human and Deterministic Boundaries

- AI may draft and recommend.
- Closed answer scoring remains deterministic in the API against the published snapshot.
- Open evaluation is criterion/evidence based and remains proposed until teacher review.
- AI cannot approve, publish, republish, close an appeal, or mutate an official result.
- Material teacher edits create new versions and may invalidate dependent approvals.
- Curriculum alignments proposed by AI require confirmation under policy.
- Pedagogical actions are non-binding until adopted by the teacher.

## Runtime Responsibilities

The generic runtime should provide:

- typed agent definitions and registry;
- provider/model routing by capability and authorized policy;
- output schema validation;
- bounded tool execution;
- retries, timeout, cancellation and idempotency;
- synchronous and asynchronous execution where appropriate;
- execution/run/step provenance;
- token and cost measurement;
- prompt/template/model versioning;
- privacy-safe logs;
- resumability only where domain idempotency allows it.

Agents do not read arbitrary databases or call provider-specific SDKs outside adapters.

## Workflow Placement

```text
Teacher/domain command
  -> API authorization and snapshot selection
  -> agent command with bounded context
  -> runtime route and execution
  -> structured proposal + evidence
  -> API persistence as proposal version
  -> teacher review or deterministic workflow
  -> explicit approval/publication commands
```

Analytics agents consume approved or clearly labeled provisional inputs. They must not silently mix proposed and official results.

## Observability and Audit

Each run records agent definition, capability, provider/model, prompt/template version, timestamps, retries, tool steps, token/cost estimate, uncertainty, input/output references, status, and correlation identifiers. Logs redact secrets and sensitive student content.

## Failure Rules

- Failure does not lose submissions or teacher edits.
- Retry creates attributable history and respects idempotency.
- Invalid output is rejected at the boundary.
- Provider fallback follows deterministic authorized policy.
- An agent cannot bypass a blocked domain transition.
- Reprocessing creates a new proposal; it does not overwrite the previous one.

<!-- nav -->

---

[↑ inicio](#agents-overview) | [README](README.md)
