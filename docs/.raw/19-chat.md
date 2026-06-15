# Repository Architecture — 5 Repos for the MVP

**Decision:** Start with 5 repositories. More fragments the project too early; fewer mixes critical responsibilities. For the GradeOps AI MVP the focus must be: create, receive, grade, generate feedback, report, and log agent traceability. OCR, mobile app, full LMS, and large question banks are out of scope for now.

---

## Recommended repositories

| Repo | Status | Purpose |
|------|--------|---------|
| `grade-ops-ai-docs` | Exists | Strategic documentation: product, architecture, decisions, pitch, roadmap, requirements, UX, sanitized evidence |
| `grade-ops-ai-web` | Create | Public and private frontend: landing, onboarding, teacher dashboard, student view, correction review, reports |
| `grade-ops-ai-api` | Create | Main backend: users, courses, assessments, rubrics, submissions, teacher review, reports, payments, audit |
| `grade-ops-ai-agents` | Create | AI agent runtime: prompts, JSON schemas, Gemini, evaluation, feedback, gap detection, execution logs |
| `grade-ops-ai-infra` | Create | Infrastructure and deployment: Cloud Run, Cloud SQL/Firestore, Cloud Storage, Secret Manager, Cloud Logging, CI/CD |

---

## Repo details

### 1. `grade-ops-ai-docs`

The source of truth for the project — not just technical documentation, but also business narrative, decisions, hackathon criteria, roadmap, pricing, pitch, architecture, UX, and sanitized evidence.

```
grade-ops-ai-docs/
├── 00-project/
├── 01-product/
├── 02-business/
├── 03-architecture/
├── 04-ux/
├── 05-agents/
├── 06-hackathon/
├── 07-roadmap/
└── README.md
```

The hackathon folder is especially important:

```
06-hackathon/
├── pitch.md
├── demo-script.md
├── evidence-checklist.md
├── revenue-evidence.md
├── agent-logs-evidence.md
├── customer-interviews.md
└── submission-narrative.md
```

The hackathon rewards real business operated by AI — with customers, revenue, real usage, logs, dashboards, and verifiable evidence. This repo is not peripheral; it is part of the competitive product.

---

### 2. `grade-ops-ai-web`

The visible experience of the product.

**Recommended stack:** Next.js + TypeScript + Tailwind or Bootstrap + React Hook Form + Zod.

**Screens to cover:**

```
grade-ops-ai-web/
├── public landing
├── registration / login
├── teacher dashboard
├── assessment creation
├── rubric review
├── submission upload
├── AI correction view
├── teacher approval
├── individual feedback
├── activity report
├── pricing / checkout
└── agent log panel (for demo)
```

**Routing structure** — no need to split landing and app yet:

```
/
├── landing
├── pricing
├── login
├── app/teacher
├── app/student
├── app/reports
└── app/agent-logs
```

The MVP must show the full flow: teacher creates assessment → student submits → agents grade → teacher approves → dashboard shows results.

---

### 3. `grade-ops-ai-api`

The business backend. Given strong Java/Spring Boot background: **Spring Boot 3 + Java 21 + PostgreSQL**.

**Responsibilities:**

```
grade-ops-ai-api/
├── auth / users / tenants
├── teachers
├── courses
├── students
├── assessments
├── rubrics
├── submissions
├── results
├── feedback
├── reports
├── billing / Stripe webhooks
├── audit
└── agent integration
```

**Suggested module structure:**

```
src/main/java/.../gradeops/
├── identity/
├── tenant/
├── assessment/
├── rubric/
├── submission/
├── grading/
├── feedback/
├── report/
├── billing/
├── audit/
├── agentclient/
└── shared/
```

This repo should not contain complex prompts or heavy agent logic. Its role is to orchestrate business rules, persist data, and delegate work to the agent runtime.

---

### 4. `grade-ops-ai-agents`

The key repo for GradeOps AI to look like a real AI-operated system — not just an app that calls Gemini.

**Agents in scope for MVP:** Assessment Agent, Rubric Agent, Grading Agent, Feedback Agent, Learning Gap Agent, Recovery Agent, Teacher Report Agent, Ops Agent.

**Responsibilities:**

```
grade-ops-ai-agents/
├── prompts
├── input/output schemas
├── Gemini API / Vertex AI Gemini
├── structured JSON validation
├── submission evaluation
├── feedback generation
├── gap analysis
├── teacher report
├── cost / token calculation
└── execution logs
```

**Suggested structure:**

```
grade-ops-ai-agents/
├── src/
│   ├── agents/
│   │   ├── assessment-designer/
│   │   ├── rubric-validator/
│   │   ├── grading/
│   │   ├── feedback/
│   │   ├── learning-gap/
│   │   ├── recovery/
│   │   ├── teacher-report/
│   │   └── ops/
│   ├── schemas/
│   ├── prompts/
│   ├── evals/
│   ├── providers/
│   │   └── gemini/
│   └── logging/
├── tests/
└── README.md
```

**Each agent execution should log:**

- Timestamp
- User / teacher
- Assessment
- Agent executed
- Input
- Decision taken
- Structured output
- Model used
- Approximate tokens / cost
- Status: `suggested` / `approved` / `corrected` / `rejected`
- Estimated minutes saved

This data serves product, demo, metrics, and hackathon evidence simultaneously.

**Stack options:**

| Option | Notes |
|--------|-------|
| TypeScript / Node.js | Natural fit for JSON schemas, fast prompt iteration, shares language with the frontend |
| Python / FastAPI | Strong AI ecosystem, less coherent with the main stack |
| Java / Spring Boot | Single-stack governance, heavier for prompt iteration |

**Recommendation:** TypeScript for agents — it fits well with JSON schemas, validation, prompts, and fast contracts. If full Java stack is preferred, Spring AI is a valid alternative.

---

### 5. `grade-ops-ai-infra`

Prevents deployment from becoming "manual steps that only work on your machine."

**Responsibilities:**

```
grade-ops-ai-infra/
├── Cloud Run (web, api, agents)
├── Cloud SQL PostgreSQL or Firestore
├── Cloud Storage
├── Secret Manager
├── Cloud Logging
├── environment variables
├── CI/CD
└── deployment scripts
```

**Suggested structure:**

```
grade-ops-ai-infra/
├── terraform/ or opentofu/
├── cloud-run/
├── github-actions/
├── scripts/
├── environments/
│   ├── dev/
│   ├── staging/
│   └── prod/
└── README.md
```

---

## Repos NOT to create yet

| Repo | Reason to defer |
|------|----------------|
| `grade-ops-ai-mobile` | Mobile before business validation adds complexity with no return |
| `grade-ops-ai-ocr` | Complex OCR is out of MVP scope |
| `grade-ops-ai-lms` | Full LMS is not the product |
| `grade-ops-ai-question-bank` | Large question bank belongs post-validation |
| `grade-ops-ai-admin` | Internal admin panel is a Phase 2 concern |
| `grade-ops-ai-shared` | Premature shared library before patterns stabilize |
| `grade-ops-ai-sdk` | SDK for third parties comes after the product works |
| `grade-ops-ai-notifications` | Notification service can live inside the API for now |

---

## Optional repos for later

| Repo | When to create | Why |
|------|----------------|-----|
| `grade-ops-ai-evidence` | Only if sensitive evidence needs isolation | Screenshots of payments, interviews, testimonials, metrics. Keep private. |
| `grade-ops-ai-code-runner` | When executing real student code | Secure sandbox for Python/JS/Java with time and memory limits |
| `grade-ops-ai-mobile` | After MVP web is stable | Mobile capture, OCR, quick review, notifications |
| `grade-ops-ai-public-site` | Only if landing grows significantly | Marketing separated from the app |

---

## Final decision

**Create now:**

```
grade-ops-ai-web
grade-ops-ai-api
grade-ops-ai-agents
grade-ops-ai-infra
```

**Keep and maintain:**

```
grade-ops-ai-docs
```

**Architecture summary:**

```
grade-ops-ai-web
    ↓
grade-ops-ai-api
    ↓
grade-ops-ai-agents
    ↓
Gemini / Vertex AI

grade-ops-ai-api
    ↓
PostgreSQL / Storage / Logs / Stripe

grade-ops-ai-infra
    ↓
Cloud Run / Cloud Logging / Secret Manager / Cloud SQL

grade-ops-ai-docs
    ↓
product / business / architecture / evidence / hackathon
```

**Verdict:** 5 repos is the right number to start. It gives professional separation without premature microservices.
