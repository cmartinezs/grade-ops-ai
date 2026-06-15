# Why TypeScript Appears for `grade-ops-ai-agents`

**Context:** The architecture diagram shows `grade-ops-ai-agents` with TypeScript as the runtime. This is a tactical recommendation, not a closed decision.

---

## The core distinction

The proposal is not:

> Put everything in TypeScript

It is:

| Repo | Stack |
|------|-------|
| `grade-ops-ai-api` | Spring Boot / Java |
| `grade-ops-ai-agents` | TypeScript |
| `grade-ops-ai-web` | TypeScript / Next.js |

**Java = strong business logic, persistence, security, payments, audit**
**TypeScript = web experience + lightweight AI agent runtime**

---

## Why TypeScript fits the agent layer

### 1. JSON, schemas, and validation are natural

Agents should return structured output like:

```json
{
  "score": 5.8,
  "rubricResults": [],
  "feedback": "...",
  "risks": [],
  "estimatedSavedMinutes": 12
}
```

TypeScript handles this cleanly with:

- **Zod** — runtime schema validation
- **JSON Schema** — contract definitions
- **Shared types** — across services
- **Runtime validation** — LLMs can return malformed output; catching this at the boundary matters

### 2. Fast prompt iteration

Agents will change frequently at the start:

- Assessment Agent prompt
- Grading Agent prompt
- Feedback Agent prompt
- Rubric schema
- Feedback schema
- Log format
- Gemini response format

TypeScript allows rapid iteration without recompiling a large backend or touching core business logic.

### 3. Agents are an orchestration layer, not a business domain

`grade-ops-ai-agents` should not know about:

- Users
- Billing plans
- Permissions
- Tenants
- Courses
- Subscriptions

That knowledge belongs in `grade-ops-ai-api`.

Agents should receive something like:

> *Evaluate this submission against this rubric and return a structured result.*

And respond with:

> *Result, feedback, gaps, risks, and execution metadata.*

This is a lightweight service, not a transactional system.

### 4. Shared language with the frontend

If `grade-ops-ai-web` is Next.js, types or schemas can be shared between `web` and `agents`:

- `AssessmentSchema`
- `RubricSchema`
- `FeedbackSchema`
- `AgentRunLogSchema`

This reduces contract drift between services.

### 5. Simple Cloud Run deployment

The agent service can be a lightweight container:

```
grade-ops-ai-agents
    ↓
Cloud Run
    ↓
Gemini API
    ↓
Structured response
```

No complex infrastructure needed for this layer.

---

## Trade-off comparison

| Option | Advantage | Disadvantage |
|--------|-----------|--------------|
| **TypeScript** | Fast, natural for JSON/schemas, shares language with Next.js | Adds a second stack alongside Java |
| **Python** | Very strong AI ecosystem | Less coherent with the primary stack |
| **Java / Spring Boot** | Single-stack governance | Heavier for iterating on prompts and agents |

---

## Recommendation

For hackathon / fast MVP:

| Repo | Stack |
|------|-------|
| `grade-ops-ai-web` | Next.js + TypeScript |
| `grade-ops-ai-api` | Spring Boot + Java 21 |
| `grade-ops-ai-agents` | TypeScript |
| `grade-ops-ai-infra` | Terraform/OpenTofu + GitHub Actions |
| `grade-ops-ai-docs` | Markdown |

For a more mature enterprise product, agents could be moved to Java or consolidated into the API — but that is a post-validation decision.

---

## Decision status

```
grade-ops-ai-agents
Runtime: TypeScript / Node.js
Reason: AI orchestration, schemas, prompts, Gemini integration, structured logs.
Status: provisional decision for MVP.
```

Explicit note to document:

> The choice of TypeScript for agents is tactical — it accelerates MVP iteration.
> Business logic, security, persistence, and billing remain in `grade-ops-ai-api` with Spring Boot.

**Verdict:** TypeScript appears in the diagram because the agent layer behaves more like an AI orchestration runtime than a classic transactional backend. Java remains the right choice for `grade-ops-ai-api`.
