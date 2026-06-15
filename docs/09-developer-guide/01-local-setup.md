# Local Development Setup

This guide walks through setting up a complete local development environment for GradeOps AI. By the end you will have the API running on port 8080 and the web app on port 3000, connected to a local PostgreSQL database and a Firebase project.

---

## Prerequisites

### Java 21

The API requires Java 21. OpenJDK and Amazon Corretto both work. The Maven wrapper (`./mvnw`) is checked into the repo, so no global Maven installation is needed.

```bash
java -version
# Expected: openjdk version "21.x.x" or similar
```

If Java 21 is not installed, download it from https://adoptium.net or use your OS package manager:

```bash
# Ubuntu / Debian
sudo apt install openjdk-21-jdk

# macOS (Homebrew)
brew install openjdk@21
```

### Node.js 18+ and npm

```bash
node -v   # Expected: v18.x.x or higher
npm -v    # Expected: 9.x.x or higher
```

If not installed, download from https://nodejs.org or use `nvm`:

```bash
nvm install 18
nvm use 18
```

### Docker

The API uses Spring Boot Docker Compose integration to start PostgreSQL automatically. Docker must be installed and running — no manual PostgreSQL installation is needed.

```bash
docker --version
# Expected: Docker version 24.x or higher
```

If Docker is not installed, download it from https://docs.docker.com/get-docker/.

### Git

```bash
git --version
```

### Firebase project

You need a Firebase project with both sign-in providers enabled. If you do not have one:

1. Go to https://console.firebase.google.com
2. Create a new project (or use an existing one)
3. In **Authentication → Sign-in method**, enable **Email/Password**
4. In the same section, enable **Google**

---

## Step 1 — Clone the repositories

GradeOps AI is a multi-repo workspace. Each subdirectory under `grade-ops-ai/` is an independent git repository. Clone them into a common parent directory:

```bash
mkdir grade-ops-ai && cd grade-ops-ai

git clone <grade-ops-ai-api-repo-url>   api
git clone <grade-ops-ai-web-repo-url>   web
git clone <grade-ops-ai-agents-repo-url> agents   # scaffolding only
git clone <grade-ops-ai-infra-repo-url>  infra     # scaffolding only
git clone <grade-ops-ai-docs-repo-url>   docs
```

After cloning, your working directory should look like:

```
grade-ops-ai/
├── api/
├── web/
├── agents/
├── infra/
└── docs/
```

Each subdirectory has its own `.git` history. Commits must be made from inside the relevant subdirectory.

---

## Step 2 — Configure the API (`api/`)

### 2a. PostgreSQL via Docker Compose

PostgreSQL is managed by `api/compose.yml`. The Spring Boot Docker Compose integration starts the container automatically when you run the API with the `local` profile — no manual database creation is needed.

The container mounts a named volume (`gradeops_pgdata`) so data persists across restarts. To reset the database completely:

```bash
docker compose -f api/compose.yml down -v
```

### 2b. Get a Firebase Admin SDK service account key

The API uses the Firebase Admin SDK to verify ID tokens. For local development you need a service account JSON file.

1. Open the Firebase console → Project Settings → **Service accounts**
2. Click **Generate new private key**
3. Save the downloaded JSON file somewhere outside the project directory (for example `~/.config/firebase/grade-ops-service-account.json`)

**Never commit this file to the repository.**

### 2c. Create `api/src/main/resources/application-local.yml`

This file is gitignored. Create it with the following content, substituting the path to your service account JSON:

```yaml
firebase:
  credentials-path: /path/to/your/firebase-admin-key.json

app:
  cors:
    allowed-origins: http://localhost:3000

spring:
  docker:
    compose:
      enabled: true
      lifecycle-management: start-and-stop
      file: compose.yml
```

The `firebase.credentials-path` property is read by `FirebaseConfig`. No environment variable is needed.

**Note:** In Cloud Run (the production environment) `credentials-path` is left blank and the service account assigned to the Cloud Run service provides credentials automatically via Application Default Credentials, provided it has the `roles/firebaseauth.admin` IAM role.

### 2d. Start the API

```bash
cd api/
./mvnw spring-boot:run -Dspring.profiles.active=local
```

Spring Boot detects `compose.yml` and starts the PostgreSQL container before booting the application. Expected output (last few lines):

```
Started GradeOpsApiApplication in 3.4 seconds (process running for 3.8)
```

### 2e. Verify the API is healthy

In a separate terminal:

```bash
curl http://localhost:8080/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

### 2f. Flyway migrations run automatically on startup

Flyway runs all pending migration scripts from `api/src/main/resources/db/migration/` on every startup. After the first successful start, the following schema is in place:

```
teacher (firebase_uid PK, name, email, created_at, updated_at,
         plan_type, related_party, offer_details, evidence_link,
         flag_set_by, flag_set_at)
```

To verify:

```bash
psql -U gradeops -d gradeops -c "\d teacher"
```

---

## Step 3 — Configure the web app (`web/`)

### 3a. Install dependencies

```bash
cd web/
npm install
```

Expected: resolves and installs packages with no errors. The key runtime dependencies are Next.js 15.3.0, React 19.1.0, and Firebase 11.x.

### 3b. Create `.env.local`

`.env.local` is gitignored. Create it in `web/` with your Firebase web app configuration (different from the Admin SDK key used by the API). To find these values:

1. Open the Firebase console → Project Settings → **Your apps**
2. If no web app exists, click the web icon (`</>`) to add one
3. Copy the config object values

```bash
NEXT_PUBLIC_FIREBASE_API_KEY=AIzaSy...
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=your-project-id.firebaseapp.com
NEXT_PUBLIC_FIREBASE_PROJECT_ID=your-project-id
NEXT_PUBLIC_FIREBASE_APP_ID=1:123456789:web:abc123
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=123456789
NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=your-project-id.firebasestorage.app
NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID=G-XXXXXXXXXX
```

**Do NOT set `NEXT_PUBLIC_API_BASE_URL`.** All API calls from the browser use the Next.js proxy (`/api/*` → `http://localhost:8080/*`). Setting that variable would bypass the proxy and cause CORS errors.

### 3d. Start the web app

```bash
npm run dev
```

Expected output:

```
 - ready started server on 0.0.0.0:3000, url: http://localhost:3000
```

Open http://localhost:3000.

---

## Step 4 — Verify end-to-end

### Automated path (self-registration)

1. Open http://localhost:3000
2. Click **Register** and create an account
3. Check your email inbox for the Firebase verification email
4. Click the verification link
5. Log in — you should see the empty dashboard

### Operator provisioning path (no email verification step)

Provision a teacher directly via the internal API. This is useful for seeding demo data or skipping the email verification step during development:

```bash
curl -X POST http://localhost:8080/internal/teachers \
  -H "X-Internal-Key: dev-secret-change-me" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Teacher", "email": "test@example.com"}'
```

Expected response (HTTP 201):

```json
{
  "firebaseUid": "abc123def456",
  "inviteLink": "https://accounts.google.com/..."
}
```

The `inviteLink` is a Firebase password-reset link that lets the provisioned user set their password. The account is created with `emailVerified=true`, so no separate email verification step is needed.

Then use the Firebase REST API to get an ID token for testing:

```bash
curl -X POST \
  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=YOUR_FIREBASE_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"YourPassword","returnSecureToken":true}'
```

Extract `idToken` from the response and use it in API requests:

```bash
curl http://localhost:8080/assessments \
  -H "Authorization: Bearer <idToken>"
```

Expected response: `[]` (assessment list stub, returns empty until Epic 02).

---

## Environment variables reference

### API (`api/`)

| Variable | Purpose | Source | Required |
|----------|---------|--------|---------|
| `DATABASE_URL` | JDBC URL for PostgreSQL | Terraform output (Cloud Run) / defaults | Cloud Run only |
| `DATABASE_USER` | PostgreSQL username | Terraform / defaults | Cloud Run only |
| `DATABASE_PASSWORD` | PostgreSQL password | Terraform / defaults | Cloud Run only |
| `INTERNAL_API_SECRET` | Shared secret for `X-Internal-Key` header | Secret Manager (Cloud Run) / `application-local.yml` | Yes |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | Terraform / `application-local.yml` | Cloud Run only |

For local dev, all database and CORS settings are in `application-local.yml`. The `firebase.credentials-path` property in that file replaces the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.

Defaults defined in `application.yml`:

```yaml
spring.datasource.url:      jdbc:postgresql://localhost:5432/gradeops
spring.datasource.username: gradeops
spring.datasource.password: gradeops
app.internal.secret:        change-me-in-production
app.cors.allowed-origins:   https://gradeops.app
```

### Web (`web/`)

| Variable | Purpose | Source | Required |
|----------|---------|--------|---------|
| `NEXT_PUBLIC_FIREBASE_API_KEY` | Firebase web API key | Firebase console | Yes |
| `NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN` | Firebase auth domain | Firebase console | Yes |
| `NEXT_PUBLIC_FIREBASE_PROJECT_ID` | Firebase project ID | Firebase console | Yes |
| `NEXT_PUBLIC_FIREBASE_APP_ID` | Firebase web app ID | Firebase console | Yes |
| `NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID` | Firebase messaging sender ID | Firebase console | Yes |
| `NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET` | Firebase storage bucket | Firebase console | Yes |
| `NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID` | Firebase Analytics measurement ID | Firebase console | No |
| `API_BASE_URL` | Server-side proxy target (not exposed to browser) | `next.config.ts` | No (defaults to `http://localhost:8080`) |

**`NEXT_PUBLIC_API_BASE_URL` must not be set.** All browser API calls go to `/api/...` and are proxied by Next.js to `API_BASE_URL`. Setting `NEXT_PUBLIC_API_BASE_URL` would send requests directly to the API port from the browser, bypassing the proxy and causing CORS errors.

---

## Common setup errors

| Error | Likely cause | Fix |
|-------|-------------|-----|
| `Could not obtain connection to any of these urls: jdbc:postgresql://localhost:5432/gradeops` | PostgreSQL not running or wrong credentials | Run `pg_lscluster` (Linux) or `brew services list` (macOS) to check PostgreSQL status. Verify database name, user, and password match `application-local.yml`. |
| `Failed to initialize Firebase App: error fetching credentials` | `firebase.credentials-path` missing or wrong path | Check that `firebase.credentials-path` in `application-local.yml` points to a valid Firebase Admin SDK service account JSON file. |
| `Flyway migration failed: relation "teacher" already exists` | A partial migration was previously applied | Run `psql -U gradeops -d gradeops -c "DELETE FROM flyway_schema_history WHERE success = false;"` then restart the API. If the table exists but the migration ran partially, drop and recreate the database. |
| `CORS error` in browser console | `NEXT_PUBLIC_API_BASE_URL` is set, bypassing the Next.js proxy | Remove `NEXT_PUBLIC_API_BASE_URL` from `web/.env.local`. Browser calls must go to `/api/...` — the Next.js rewrite proxies them to port 8080. |
| `401 Unauthorized` on every API request | Firebase token mismatch between projects | Verify that `NEXT_PUBLIC_FIREBASE_PROJECT_ID` in `.env.local` matches the project ID in the Firebase Admin SDK JSON used by the API. Both must point to the same Firebase project. |
| `PORT 3000 is already in use` | Another process using port 3000 | Run `lsof -i :3000` to find the process and kill it, or start Next.js on a different port: `npm run dev -- -p 3001`. |
| `npm install` fails with peer dependency error | Node version mismatch | Verify `node -v` returns 18 or higher. Use `nvm use 18` if you have multiple Node versions installed. |

---

<!-- nav -->

[← Developer Guide](README.md) | [↑ Top](#local-development-setup) | [Repository Map →](02-repository-map.md)
