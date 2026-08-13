# Decision — GradeOps AI as proving ground for the reusable Agent Capability Platform

**Date:** 2026-08-13  
**Status:** Accepted direction / extraction deferred until reuse evidence exists

## Context

GradeOps AI already has a real agent execution vertical slice and a documented staged runtime evolution. The current Assessment Agent uses versioned prompt resources, Gemini/Groq provider adapters, structured output validation and execution evidence. The backend API remains authoritative for assessment-domain state, persistence, approval, billing and publication.

Across the broader software portfolio, future products are expected to use GenAI/AI capabilities. Rebuilding provider routing, prompt/version handling, structured output, tools, budgets, retries, audit and orchestration independently in each SaaS would create avoidable duplication and drift.

At the same time, extracting a generic platform before a second real consumer exists would create a speculative platform dependency and could slow GradeOps delivery.

## Decision

GradeOps AI is the **seed and proving ground** for the portfolio-level `Agent Runtime Core`, but GradeOps does not become the portfolio's generic Agent Platform by ownership or by accidental coupling.

The reusable direction is:

```text
Agent Capability Platform
  ├─ model/provider routing
  ├─ prompt/template versioning
  ├─ structured command/result contracts
  ├─ output validation
  ├─ tool registry/executor
  ├─ policy + budget enforcement
  ├─ retries / idempotency / durable execution
  ├─ execution evidence / audit / cost
  ├─ approval handoff
  └─ evaluation hooks
          │
          ▼
      Domain Packs
          │
          ├─ gradeops.*
          ├─ commerce.*
          ├─ career.*
          ├─ consulting.*
          ├─ education.*
          ├─ legal.*
          ├─ creative.*
          ├─ content.*
          └─ portfolio.*
```

GradeOps-specific assessment semantics stay in GradeOps. A future shared runtime must not own assessment entities, grading state, teacher approvals, billing state or publication state.

## Domain Pack direction

When a shared Domain Pack contract becomes real, GradeOps becomes its first strong domain candidate.

A `gradeops` pack may contain:

- assessment/rubric/grading/feedback command and result schemas;
- versioned prompts;
- GradeOps tool allowlists;
- orchestration definitions;
- domain validators and safety constraints;
- quality/evaluation rules;
- GradeOps-specific policy profiles;
- capability dependencies.

The pack may initially remain a module/resource boundary inside the existing agents service. It does **not** require a separate deployable or repository.

## Generic extraction candidates

The following mechanics are candidates for portfolio reuse as real consumers appear:

1. capability-oriented Agent Command / Result / Execution contracts;
2. policy-based provider/model routing;
3. provider adapters and normalized provider metadata;
4. prompt/template registry and version evidence;
5. structured output parsing/validation;
6. normalized agent errors;
7. Tool Registry / Tool Executor and side-effect classification;
8. policy/budget enforcement for steps, tokens, calls, time, retries and cost;
9. idempotency, retry and circuit-breaker patterns;
10. durable `Run` / `Step`, cancellation and resume when volume/latency requires it;
11. approval-request / pause / resume contracts;
12. execution evidence, tokens, cost, latency, status, hashes and audit;
13. evaluation/quality hooks;
14. privacy-aware memory/retrieval adapters when justified.

## Extraction gate

Do **not** create a separate portfolio Agent Platform repository/service merely because the abstractions are attractive.

Promote/extract a capability when one or more of these is true:

- a second real product/workflow needs the same behavior;
- duplication has appeared;
- the boundary and contract are stable;
- centralized security/policy/audit is materially valuable;
- project-local implementations are slowing delivery or increasing risk.

Until then, GradeOps should continue shipping the minimum generic mechanics required by its functional releases.

This preserves the existing rule: shared abstractions are extracted after real duplication/consumers, not before.

## Alignment with the existing R01–R06 runtime evolution

The portfolio capability roadmap does not replace GradeOps functional releases. It overlays reusable extraction opportunities:

| GradeOps runtime stage | GradeOps need | Portfolio reuse signal |
|---|---|---|
| R01 | Assessment Agent baseline/evidence | seed command/result/execution contract |
| R02 | Rubric/Grading/Feedback | first meaningful internal duplication; candidate `AgentDefinition`, registry and common gateway |
| R03 | typed handoffs/read-only aggregate tools | reusable tool/read contract evidence |
| R04 | Closed authoring tool loops | Tool Registry/Executor, Policy Engine, Budget Manager become real |
| R05 | async/volume/latency | durable Run/Step only if operationally required |
| R06 | ops/evidence dashboard | health/cost/readiness and portfolio evidence adapters |

No stage is accelerated merely to satisfy the portfolio architecture.

## Authority boundaries

### GradeOps API owns

- domain state;
- persistence;
- teacher/user approvals;
- billing/credit state;
- publication/finalization;
- tenant/domain authorization.

### GradeOps agents service owns

- bounded AI execution;
- prompt rendering;
- provider/tool invocation through allowed ports;
- structured output validation;
- runtime policy/budget enforcement as introduced;
- execution metadata returned to API.

### Future portfolio capability layer may own

- generic execution contracts and reusable mechanics only after extraction gates are met.

### Powerful Brain / Project Pulse relationship

- Powerful Brain may register GradeOps-produced capabilities and sanitized evidence/relationships.
- Project Pulse / Work OS may display work/run/approval summaries through explicit projections.
- Neither is a runtime dependency required for the GradeOps MVP.
- Neither may override GradeOps domain state.

## Human-in-the-loop alignment

Portfolio autonomy levels map naturally to GradeOps:

- `A0` Observe — inspect evidence/state.
- `A1` Recommend — propose assessment/workflow actions.
- `A2` Prepare — generate drafts for teacher review.
- `A3` Execute reversible — bounded internal/reversible operations.
- `A4` Execute gated — multi-step work stops at teacher/configured approval gates.
- `A5` Exception-driven — only for mature low-risk operations with explicit policy; not a default target for grading/finalization.

Teacher approval and domain safety remain product requirements, not optional platform features.

## Consequences

### Positive

- Future SaaS products can reuse proven AI mechanics rather than clone them.
- GradeOps remains focused on product delivery.
- Domain boundaries stay explicit.
- Centralized policies/tools can emerge from evidence instead of speculation.
- GradeOps becomes a source of reusable portfolio leverage without becoming a monolith.

### Trade-offs

- Some duplication may intentionally exist before extraction.
- A future extraction may require adapters/refactoring.
- Shared contracts must remain capability-oriented and avoid leaking GradeOps terminology.

These trade-offs are preferred over premature platformization.

## Related portfolio documentation

Canonical portfolio-level direction lives in `cmartinezs/powerful-brain`:

- `CAPABILITY-ROADMAP.md`
- `TRANSVERSAL-CONTRACTS.md`
- `data/capabilities.yaml`

The portfolio capability release sequence is `CR-0` through `CR-7`; GradeOps is primarily a seed for `CR-2` and `CR-3`, with later evidence contributing to `CR-4` and `CR-6`.
