# Beta Environment Design

**Date:** 2026-06-16
**Status:** Approved
**Scope:** Free-tier deployment environment for GradeOps AI with full provider abstraction

---

## Context

The primary deployment target (environment `demo`) runs on Google Cloud Platform and requires a billing account with a credit card. The `beta` environment is a fully functional alternative using only free-tier services — no credit card required — with feature parity to `demo`.

The two environments share 100% of application code. What changes is which infrastructure adapters are activated, controlled by the Spring profile (`demo` vs `beta`).

---

## Architecture

### Beta (free tier)

```
Browser
  └── Vercel (Next.js)
        └── Render — grade-ops-ai-api (Spring Boot 4.1.0 native image)
              ├── Neon (serverless PostgreSQL — same Flyway migrations, no changes)
              ├── Cloudflare R2 (Storage — S3-compatible API)
              ├── Firebase Authentication (free, shared with Demo)
              └── Render — grade-ops-ai-agents (Spring Boot 4.1.0 native image, internal)
                    └── Google AI Studio OR OpenAI-compatible provider (Groq, Together AI, etc.)
```

### Demo (GCP)

```
Browser
  └── Firebase App Hosting (Next.js)
        └── Cloud Run — grade-ops-ai-api (Spring Boot 4.1.0 native image)
              ├── Cloud SQL PostgreSQL
              ├── Cloud Storage (GCS)
              ├── Firebase Authentication
              └── Cloud Run — grade-ops-ai-agents (internal)
                    └── Vertex AI Gemini
```

Application code is identical. Spring profile selection activates the correct adapter implementations.

---

## Ports & Adapters

### AuthPort (`api/`)

```java
interface AuthPort {
    TeacherIdentity verifyToken(String idToken);
}
```

| Adapter | Profile | Dependency |
|---|---|---|
| `FirebaseAuthAdapter` | `demo`, `beta` | `firebase-admin` |

Firebase Authentication is free with no billing requirement. A second adapter (`SupabaseAuthAdapter`) is not implemented now but the port makes it a drop-in replacement when needed.

### StoragePort (`api/`)

```java
interface StoragePort {
    void store(String key, byte[] content, String contentType);
    byte[] retrieve(String key);
    void delete(String key);
    URI signedUrl(String key, Duration ttl);
}
```

| Adapter | Profile | Dependency |
|---|---|---|
| `GcsStorageAdapter` | `demo` | `google-cloud-storage` |
| `R2StorageAdapter` | `beta` | `software.amazon.awssdk:s3` |

Cloudflare R2 implements the S3 API. `R2StorageAdapter` points the AWS S3 SDK to `https://<account_id>.r2.cloudflarestorage.com`. No R2-specific SDK required.

### AI — `agents/`

Spring AI's `ChatClient` is the port. No custom interface is needed — Spring AI already provides the abstraction. The active provider is determined by the Maven dependency and Spring configuration.

| Provider | Profile | Spring AI starter |
|---|---|---|
| Vertex AI Gemini | `demo` | `spring-ai-vertex-ai-gemini-spring-boot-starter` |
| Google AI Studio | `beta` (primary) | `spring-ai-google-ai-gemini-spring-boot-starter` |
| OpenAI-compatible (Groq, Together AI, OpenRouter) | `beta` (fallback) | `spring-ai-openai-spring-boot-starter` with custom `base-url` |

The model name is never hardcoded. It is set via `AI_MODEL_NAME` environment variable, resolved in `application-beta.yml`. This allows the model to be updated without a code change as provider offerings evolve.

```yaml
# application-beta.yml
spring:
  ai:
    google:
      ai:
        gemini:
          api-key: ${GOOGLE_AI_API_KEY}
          model: ${AI_MODEL_NAME}
```

To switch to an OpenAI-compatible provider (e.g., Groq), swap the Maven starter and update two config properties — no Java changes.

---

## Native Compilation

Both `api/` and `agents/` are compiled to native binaries using GraalVM via the `native` Maven profile.

**Build command:**
```bash
./mvnw -Pnative spring-boot:build-image
```

**Expected runtime characteristics:**

| Metric | JVM | Native |
|---|---|---|
| Startup time | ~4–6s | ~80–150ms |
| Idle RAM | ~300–400MB | ~60–120MB |
| Docker image size | ~400MB | ~80–120MB |

Render free tier provides 512MB RAM per service — sufficient for both services as native images.

**Reflection hints required for native:**

1. **Firebase Admin SDK** — uses reflection for JSON deserialization. Requires `@RegisterReflectionForBinding` on Firebase response types or a `reflect-config.json` entry.
2. **Spring AI starters** — recent versions include their own native hints. Verify at build time; add hints if missing.
3. **PostgreSQL JDBC driver** — native-compatible since version 42.6+. No additional config needed.

Native hints are maintained in `api/src/main/resources/META-INF/native-image/` and `agents/src/main/resources/META-INF/native-image/`.

---

## Beta Infrastructure Setup

All services are configured manually (no Terraform). Each requires only a free-tier account.

### Vercel — Web

- Connect repo `cmartinezs/grade-ops-ai`, root directory `web/`
- Framework preset: Next.js
- Deploy on push to `main`
- Environment variables: same `NEXT_PUBLIC_FIREBASE_*` keys as Demo

### Render — API and Agents

Two separate Web Services. Agents is configured as an Internal Service (not publicly reachable).

- Runtime: Docker
- Plan: Free
- Build: `./mvnw -Pnative spring-boot:build-image`
- Cold start after 15 min inactivity: acceptable for Beta

### Neon — PostgreSQL

- Create project → copy connection string
- Connection string format: `jdbc:postgresql://ep-xxx.neon.tech/gradeops?sslmode=require`
- Flyway runs the same migrations without modification
- Free tier: 512MB storage, 1 compute unit

### Cloudflare R2 — Storage

- Create bucket `gradeops-beta`
- Generate R2 API token with Object Read & Write permissions
- Endpoint: `https://<account_id>.r2.cloudflarestorage.com`
- Free tier: 10GB storage, 1M Class A operations/month

### Google AI Studio — AI

- Go to [ai.google.dev](https://ai.google.dev) → Get API key
- No billing required
- Set `AI_MODEL_NAME` to the current free model (verify available models at deploy time)

---

## Environment Variables

### Render — API (`SPRING_PROFILES_ACTIVE=beta`)

```
SPRING_PROFILES_ACTIVE=beta
DATABASE_URL=jdbc:postgresql://ep-xxx.neon.tech/gradeops?sslmode=require
R2_ACCOUNT_ID=<cloudflare account id>
R2_ACCESS_KEY=<r2 access key id>
R2_SECRET_KEY=<r2 secret access key>
R2_BUCKET=gradeops-beta
FIREBASE_ADMIN_CREDENTIALS=<service account JSON, inline>
INTERNAL_API_SECRET=<shared secret with agents>
AGENTS_BASE_URL=<render internal URL for agents service>
CORS_ALLOWED_ORIGINS=https://<vercel-app>.vercel.app
```

### Render — Agents (`SPRING_PROFILES_ACTIVE=beta`)

```
SPRING_PROFILES_ACTIVE=beta
GOOGLE_AI_API_KEY=<google ai studio api key>
AI_MODEL_NAME=<model name from ai.google.dev>
INTERNAL_API_SECRET=<same shared secret as api>
```

### Vercel — Web

```
NEXT_PUBLIC_FIREBASE_API_KEY=...
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=...
NEXT_PUBLIC_FIREBASE_PROJECT_ID=gen-lang-client-0898391452
NEXT_PUBLIC_FIREBASE_APP_ID=...
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=...
NEXT_PUBLIC_API_BASE_URL=https://<render-api-service>.onrender.com
```

---

## What Does NOT Change Between Environments

- All Java application code
- All Flyway migration scripts
- All Spring AI `ChatClient` usage in `agents/`
- Firebase Authentication SDK usage in `api/`
- All API contracts and DTOs
- All prompt templates in `agents/src/main/resources/prompts/`
- The `web/` Next.js application

---

## Out of Scope

- Terraform resources for Beta (infra is configured manually via dashboards)
- CI/CD pipeline for Beta (builds and deploys triggered manually or via Render's GitHub integration)
- Monitoring and alerting for Beta (not required — Beta is for demos and testing)
- Custom domain for Beta
