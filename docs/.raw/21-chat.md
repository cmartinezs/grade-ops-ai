# Spring AI as an Alternative for `grade-ops-ai-agents`

**Context:** Spring AI was considered as an alternative to TypeScript for the agent runtime, which would keep the full backend stack in Java.

---

## Updated stack proposal

Incorporating Spring AI, the full architecture becomes:

| Repo | Stack |
|------|-------|
| `grade-ops-ai-web` | Next.js / TypeScript |
| `grade-ops-ai-api` | Spring Boot / Java 21 |
| `grade-ops-ai-agents` | Spring Boot / Java 21 + Spring AI |
| `grade-ops-ai-infra` | Google Cloud / Cloud Run |
| `grade-ops-ai-docs` | Markdown |

---

## Why TypeScript was recommended first

The initial TypeScript recommendation optimized for:

- Fast prompt iteration
- JSON schema handling
- Lightweight runtime validation
- Direct Gemini SDK access
- Rapid experimentation

TypeScript is very comfortable for all of that. However, **the more important factor here is existing Java/Spring Boot expertise** — Spring AI may be a better bet because it reduces stack fragmentation.

---

## What Spring AI provides

Spring AI is not just a library for calling OpenAI or Gemini. Its goal is to connect enterprise data and APIs with AI models. It offers abstractions for:

- Chat models
- Embeddings
- Structured output
- Tool / function calling
- Observability
- RAG (Retrieval-Augmented Generation)
- Response evaluation
- Spring Boot autoconfiguration

For GradeOps AI, this maps directly to what the agent layer needs:

| Need | Spring AI capability |
|------|---------------------|
| Call Gemini | Google GenAI integration |
| Generate structured output | `Structured Output` support |
| Validate AI responses | Output converters + custom validation |
| Orchestrate tools | Tool Calling (client controls execution) |
| Save execution logs | Spring Boot observability integration |
| Measure executions | Micrometer / Cloud Logging |
| Future RAG support | Vector store abstractions |
| Future response evaluation | Evaluator support |

**Important note on Structured Output:** Spring AI supports converting model responses to JSON or Java classes, but the documentation explicitly warns that output validation is still necessary — the model does not guarantee strict schema compliance on every call.

**Important note on Tool Calling:** When the model requests a tool execution, the client application maintains full control. Gemini never directly touches the database or internal APIs — the application decides what tool to run and with what data.

---

## Gemini support in Spring AI

Spring AI supports Google GenAI with two integration paths:

**Option 1 — Gemini Developer API (prototyping)**
- Uses API key
- Simpler setup

**Option 2 — Vertex AI Gemini (production)**
- Uses Google Cloud credentials
- Enterprise features
- Better for GradeOps AI's production target

Maven dependency for Vertex AI:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-vertex-ai-gemini</artifactId>
</dependency>
```

Configuration via:

```properties
spring.ai.vertex.ai.gemini.project-id=...
spring.ai.vertex.ai.gemini.location=...
spring.ai.vertex.ai.gemini.chat.options.model=...
```

---

## Service boundary: keep `grade-ops-ai-agents` as a separate repo

Two options evaluated:

### Option A — Simpler for MVP: agents inside `grade-ops-ai-api`

```
grade-ops-ai-api/
└── module: agents (within the same backend)
```

**Advantages:**
- Fewer repos
- Fewer deployments
- Less networking overhead
- Less CI/CD complexity
- Less initial friction

**Disadvantages:**
- Mixes transactional business logic with AI execution
- Harder to scale agents independently later

This is a valid starting point for speed.

### Option B — Cleaner architecture: separate `grade-ops-ai-agents` repo

```
grade-ops-ai-api      (business backend)
grade-ops-ai-agents   (AI agent runtime)
```

**Advantages:**
- Clear separation of concerns
- Agents can scale independently
- Isolated logs and cost tracking
- Better architectural narrative for AI-native product
- Easier to demonstrate an "agent runtime" in the demo

**Disadvantages:**
- More infrastructure
- More inter-service contracts
- More deployments

---

## Decision for GradeOps AI

**Keep `grade-ops-ai-agents` as a separate repo, but implement it with Spring Boot + Spring AI instead of TypeScript.**

```
grade-ops-ai-api
Responsible for:
  - Users
  - Courses
  - Assessments
  - Submissions
  - Billing
  - Audit
  - Persistence
  - Permissions

grade-ops-ai-agents
Responsible for:
  - Assessment Agent
  - Rubric Agent
  - Grading Agent
  - Feedback Agent
  - Learning Gap Agent
  - Recovery Agent
  - Teacher Report Agent
  - Ops Agent
  - Gemini integration
  - Prompts
  - Structured outputs
  - Tool calling
  - Cost / token tracking
  - AI execution logs
```

**Updated architecture diagram:**

```
grade-ops-ai-web
Next.js / TypeScript
    ↓
grade-ops-ai-api
Spring Boot / Java 21
    ↓
grade-ops-ai-agents
Spring Boot / Java 21 + Spring AI
    ↓
Gemini API / Vertex AI Gemini
```

---

## Decision rule

| Choose Spring AI if you want to… | Choose TypeScript if you want to… |
|----------------------------------|-----------------------------------|
| Stay strong in Java | Iterate on prompts at maximum speed |
| Learn a career-relevant tool | Share language between frontend and agents |
| Maintain enterprise-friendly architecture | Use JS SDKs directly |
| Integrate cleanly with Spring Boot | Keep the agent runtime very lightweight |
| Avoid Node.js just for agents | |

**For Carlos + GradeOps AI + Spring Boot + hackathon + serious product:**

```
grade-ops-ai-agents = Spring Boot + Spring AI
```

This does not invalidate the TypeScript reasoning — it improves the recommendation given the specific context. TypeScript remains the best default for teams with equal familiarity or who prioritize prompt iteration speed above all else.
