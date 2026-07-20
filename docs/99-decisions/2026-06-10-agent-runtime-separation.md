# Agent Runtime Separation

- Status: Accepted
- Date: 2026-06-10
- Decision owner: Architecture / Founder

## Context

With Spring AI selected as the agent runtime (see [[technology-stack]]), there is a structural decision to make: should the agent runtime live inside `grade-ops-ai-api` as a module, or in a separate `grade-ops-ai-agents` repository and service?

Both options are technically viable. The choice affects operational complexity, architecture narrative, and demo clarity.

**Option A — Agent module inside `grade-ops-ai-api`:**

```text
grade-ops-ai-api/
└── src/main/java/.../gradeops/
    ├── assessment/
    ├── submission/
    ├── billing/
    └── agents/        ← Spring AI agent calls as an internal module
```

**Option B — Separate `grade-ops-ai-agents` service:**

```text
grade-ops-ai-api         ← business domain, persistence, billing
grade-ops-ai-agents      ← AI runtime, prompts, Gemini, execution logs
```

## Decision

**Keep `grade-ops-ai-agents` as a separate repository and service.**

The agent runtime communicates with the API via internal HTTP calls (or a lightweight event/queue mechanism in later phases).

For the MVP, it is acceptable to run both services in the same Cloud Run project and keep HTTP communication simple and synchronous where the workflow allows.

If delivery is tight during the MVP sprint, temporarily merging agents into the API as a module is acceptable, provided module boundaries remain explicit and the split is restored before the demo.

## Rationale

| Criterion | Option A (internal module) | Option B (separate service) |
| --- | --- | --- |
| Operational simplicity | Lower — one service, one deployment | Higher — two services, two deployments |
| AI/business separation | Mixed in one codebase | Clean boundary |
| Independent scaling | Not possible | Yes — agents can scale independently |
| Log and cost isolation | Harder to isolate per invocation | Natural — each agent call is its own trace |
| Demo narrative | "App with AI inside" | "Agent runtime operating the workflow" |
| Product positioning | Adequate | Stronger AI-native story |
| Failure isolation | Agent crash can affect API | Agent failure does not bring down business API |
| Prompt iteration speed | Requires full API redeploy | Agent service can be redeployed independently |

The separate service option produces a stronger AI-native product narrative. The demo can point explicitly to an agent runtime as a distinct operational layer — not just a module inside the backend.

## Responsibilities per service

**`grade-ops-ai-api`** owns:

- User authentication and session management
- Teacher and organization management
- Assessment and rubric lifecycle
- Submission ingestion
- Grading results and feedback records
- Billing and plan management
- Audit trail and approval events
- Database persistence (PostgreSQL)

**`grade-ops-ai-agents`** owns:

- All 13 agent implementations (open and closed assessment agents)
- Prompt templates (versioned)
- Provider/model integration via Spring AI, including the current Gemini and Groq adapters
- Structured output validation (Java POJOs / JSON)
- Tool calling definitions and execution control
- Agent execution payloads (`AgentExecutionLog` data: timestamp, agent, input summary, output summary, provider, model, tokens, cost, status, time saved)
- Token and cost estimation per invocation

The agents service does not own domain entities. It receives structured inputs from the API, executes model calls, validates outputs, and returns structured results plus execution payloads. The API persists domain results, persists `AgentExecutionLog`, and updates workflow state.

## Consequences

- `grade-ops-ai-api` calls `grade-ops-ai-agents` via HTTP (internal Cloud Run service URL or VPC connector).
- Agent service endpoints are not public-facing; they accept requests only from the API.
- Inter-service authentication must be handled (Cloud Run service-to-service auth via OIDC tokens is the recommended approach on Google Cloud).
- Both services must be deployed and healthy for the full workflow to operate; a health check for the agents service should be visible in the teacher dashboard.
- The `grade-ops-ai-infra` repo provisions both Cloud Run services.
- Agent execution payloads are returned by the agents service. The API is responsible for persisting `AgentExecutionLog` records with the domain aggregate and approval workflow.

## 2026-07-20 clarification

The first implemented vertical slice is the Assessment Agent, exposed by `agents/` as `POST /internal/agents/assessment` and called by the API through `agentclient`. This slice is not yet a generic headless agent runtime.

The generic runtime should grow only when functional releases create real consumers. The intended evolution is:

1. preserve the specific Assessment Agent endpoint while R01 stabilizes provider/model policy, errors, costs and logs;
2. introduce `AgentDefinition`, registry and a shared model gateway when Rubric/Grading/Feedback become real second consumers;
3. introduce tool loop, `AgentAction`, policy engine and budget manager when Closed assessment authoring needs controlled tool use;
4. introduce persistent `AgentRun`/`AgentStep`, async, cancellation and resume only when volume or latency justifies it.

The API remains the authority for domain state, approval, billing and persistence throughout this evolution.

<!-- nav -->

---

← [Technology Stack](2026-06-10-technology-stack.md) | [↑ inicio](#agent-runtime-separation) | [README](README.md) | [ADR Template →](adr-template.md)
