```
 ██████╗ ██████╗  █████╗ ██████╗ ███████╗ ██████╗ ██████╗ ███████╗    █████╗ ██╗
██╔════╝ ██╔══██╗██╔══██╗██╔══██╗██╔════╝██╔═══██╗██╔══██╗██╔════╝   ██╔══██╗██║
██║  ███╗██████╔╝███████║██║  ██║█████╗  ██║   ██║██████╔╝███████╗   ███████║██║
██║   ██║██╔══██╗██╔══██║██║  ██║██╔══╝  ██║   ██║██╔═══╝ ╚════██║   ██╔══██║██║
╚██████╔╝██║  ██║██║  ██║██████╔╝███████╗╚██████╔╝██║     ███████║██╗██║  ██║██║
 ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ╚══════╝ ╚═════╝ ╚═╝     ╚══════╝╚═╝╚═╝  ╚═╝╚═╝
```

<div align="center">

**AI-Native Assessment Operations Platform**

`grade-ops-ai-web` · Teacher Workspace

![Build](https://img.shields.io/badge/build-passing-brightgreen?style=flat-square)
![Next.js](https://img.shields.io/badge/Next.js-15.3-black?style=flat-square&logo=next.js&logoColor=white)
![React](https://img.shields.io/badge/React-19.1-61DAFB?style=flat-square&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-11.x-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![Node](https://img.shields.io/badge/Node.js-18-339933?style=flat-square&logo=node.js&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)

</div>

---

## Overview

`grade-ops-ai-web` is the teacher-facing frontend of the GradeOps AI platform. It provides the authenticated workspace where programming educators manage their assessment cycles — from viewing pending work to approving AI-generated rubrics, feedback, and reports.

The application is built on **Next.js 15 App Router** with server-side rendering disabled for authenticated routes (fully client-driven after the Firebase token handshake). All data is fetched from `grade-ops-ai-api`; the frontend owns no business logic and imposes no authorization rules — those are enforced server-side.

> Students never log in through this app. Student access is handled via signed token links (Epic 12).

---

## Architecture

```
Browser
  │
  ├── Public routes:  /login  /register  /verify-email
  │       └─── Firebase Auth (email/password via Identity Platform)
  │
  └── Protected routes:  /dashboard  (wrapped by AuthGuard)
          └─── grade-ops-ai-api  (Bearer token — Firebase ID token)
                      │
                      └── Cloud SQL PostgreSQL
```

**Auth flow:**
1. User signs in via Firebase Auth (client SDK).
2. Firebase returns an ID token.
3. Every API request includes `Authorization: Bearer <id_token>`.
4. `grade-ops-api` validates the token server-side via Firebase Admin SDK.
5. `EmailVerifiedFilter` blocks unverified accounts before they reach any endpoint.

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Framework | Next.js (App Router) | 15.3.0 |
| UI Library | React | 19.1.0 |
| Language | TypeScript | 5.x |
| Auth | Firebase JS SDK | 11.x |
| Form validation | React Hook Form + Zod | 7.x / 4.x |
| Testing | Jest + Testing Library | 29.x |
| Linting | ESLint (next config) | 9.x |
| Containerization | Docker (multi-stage) | — |
| Runtime target | Cloud Run (Node 18 Alpine) | — |

---

## Project Structure

```
web/
├── src/
│   ├── app/                          # Next.js App Router pages
│   │   ├── layout.tsx                # Root layout (fonts, global CSS)
│   │   ├── globals.css               # DS token imports + global resets
│   │   ├── page.tsx                  # Root — redirects to /login
│   │   ├── login/page.tsx            # Sign-in page (email + Google)
│   │   ├── register/page.tsx         # Self-registration page
│   │   ├── verify-email/page.tsx     # Post-registration verification gate
│   │   └── (protected)/              # Route group — AuthGuard + AppShell
│   │       ├── layout.tsx            # Injects AuthGuard + ShellProvider + AppShell
│   │       ├── dashboard/page.tsx    # Assessment dashboard (real data)
│   │       ├── assessments/page.tsx  # Placeholder — Evaluaciones
│   │       ├── bank/page.tsx         # Placeholder — Banco de preguntas
│   │       ├── students/page.tsx     # Placeholder — Estudiantes
│   │       └── reports/page.tsx      # Placeholder — Reportes
│   ├── components/
│   │   ├── auth/
│   │   │   ├── AuthGuard.tsx         # Redirects unauthenticated users to /login
│   │   │   ├── GoogleSignInButton.tsx # Thin wrapper around ds/GoogleButton
│   │   │   └── SignOutButton.tsx     # Calls POST /auth/sign-out + clears Firebase session
│   │   ├── brand/
│   │   │   └── AppLogo.tsx           # GradeOps AI wordmark with DS colors
│   │   ├── dashboard/
│   │   │   ├── AssessmentRow.tsx     # Assessment list row with DS Badge + status
│   │   │   └── DashboardEmptyState.tsx # Zero-state for empty dashboard
│   │   ├── ds/                       # GradeOps AI Design System components
│   │   │   ├── index.ts              # Re-exports all DS components
│   │   │   ├── Avatar.tsx            # Initials circle (sm/md)
│   │   │   ├── Badge.tsx             # Status pill — 7 tones, optional dot
│   │   │   ├── Button.tsx            # Primary/ghost/outline, sm/md, loading
│   │   │   ├── Card.tsx              # Compound card (Header/Title/Body/Footer)
│   │   │   ├── Field.tsx             # Label wrapper for form inputs
│   │   │   ├── GoogleButton.tsx      # Google sign-in button (Firebase + DS style)
│   │   │   ├── IconButton.tsx        # Icon-only button with aria-label
│   │   │   ├── Input.tsx             # Text input — focus ring, icon, inline error
│   │   │   ├── LucideIcon.tsx        # 18 Lucide icons as inline SVG (no npm dep)
│   │   │   └── StatCard.tsx          # Metric card (label/value/delta/icon)
│   │   └── shell/
│   │       ├── AppShell.tsx          # Sidebar (256px) + topbar (64px) + content slot
│   │       ├── PlaceholderPage.tsx   # Reusable empty-state for unimplemented routes
│   │       └── ShellContext.tsx      # Context for per-page title/subtitle/actions
│   ├── styles/
│   │   └── ds-tokens/               # CSS custom properties
│   │       ├── colors.css            # --sprout-*, --gold-*, --slate-*, semantic aliases
│   │       ├── typography.css        # --font-display/sans, --text-* scale
│   │       ├── spacing.css           # --space-* scale
│   │       ├── radius.css            # --radius-sm/md/lg
│   │       └── shadows.css           # --shadow-sm, --shadow-brand, --ring
│   ├── lib/
│   │   ├── api/
│   │   │   ├── client.ts             # fetch wrapper — attaches Bearer token, handles 401
│   │   │   ├── auth.ts               # registerTeacher(), signOutApi()
│   │   │   └── assessments.ts        # getAssessments()
│   │   └── firebase/
│   │       └── client.ts             # Firebase app + auth instance (singleton)
│   ├── types/
│   │   └── assessment.ts             # AssessmentSummaryDto — mirrors API contract
│   └── test/
│       └── __mocks__/firebase/       # Jest mocks for firebase/app and firebase/auth
├── Dockerfile                        # Multi-stage build (builder → runner)
├── next.config.ts                    # Rewrites: /api/* → localhost:8080 (dev)
├── tsconfig.json
└── jest.config.ts
```

---

## Documentation

| Guide | Purpose |
|-------|---------|
| [`docs/gradeops-ai-frontend-guidelines`](docs/gradeops-ai-frontend-guidelines/README.md) | Frontend coding standards and UI/UX best practices for GradeOps AI: component hierarchy, hooks, Design System, forms, accessibility, API contracts, testing, security, and AI-assisted development. |

---

## Getting Started

### Prerequisites

| Tool | Minimum version |
|------|----------------|
| Node.js | 18 |
| npm | 9 |
| A running `grade-ops-ai-api` instance | — |
| Firebase project with Identity Platform enabled | — |

### Environment Variables

Create a `.env.local` file at the project root. All five variables are required; their values are available as Terraform outputs from `grade-ops-ai-infra`:

```bash
# .env.local
NEXT_PUBLIC_FIREBASE_API_KEY=AIza...
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=<project-id>.firebaseapp.com
NEXT_PUBLIC_FIREBASE_PROJECT_ID=<project-id>
NEXT_PUBLIC_FIREBASE_APP_ID=1:...:web:...
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=...
```

To pull the values from Terraform:

```bash
cd ../infra
terraform -chdir=terraform/environments/demo output -json | jq 'with_entries(select(.key | startswith("NEXT_PUBLIC_")))'
```

### Local Development

```bash
npm install
npm run dev          # http://localhost:3000
```

The dev server proxies API calls based on `next.config.ts`. By default it expects `grade-ops-ai-api` at `http://localhost:8080`.

---

## Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server with hot reload |
| `npm run build` | Production build (outputs to `.next/`) |
| `npm run start` | Serve the production build locally |
| `npm run lint` | Run ESLint across `src/` |
| `npm run test` | Run Jest test suite |
| `npm run test -- --watch` | Jest in watch mode |

---

## Security Model

- **No server-side secrets.** All five `NEXT_PUBLIC_*` variables are safe to expose in the browser; they identify the Firebase project but carry no privileged access.
- **Token refresh.** The `apiClient` interceptor in `src/lib/api/client.ts` calls `getIdToken(true)` before each request, ensuring expired tokens are refreshed transparently.
- **Session expiry.** A 401 response from the API triggers an automatic sign-out and redirect to `/login` — the user is never left in a broken state.
- **Authorization enforced server-side.** `AuthGuard` redirects unauthenticated users on the client, but all resource access is gated server-side in `grade-ops-ai-api`. Frontend guards are UX, not security.

---

## Docker

The Dockerfile uses a two-stage build. Firebase config is passed as build-time `ARG`s so they are baked into the static bundle at build time (standard Next.js public env pattern):

```bash
docker build \
  --build-arg NEXT_PUBLIC_FIREBASE_API_KEY=... \
  --build-arg NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=... \
  --build-arg NEXT_PUBLIC_FIREBASE_PROJECT_ID=... \
  --build-arg NEXT_PUBLIC_FIREBASE_APP_ID=... \
  --build-arg NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=... \
  -t grade-ops-web .

docker run -p 3000:3000 grade-ops-web
```

---

## Related Repositories

| Repo | Description |
|------|-------------|
| [`grade-ops-ai-api`](../api/) | Spring Boot domain API — all business logic and persistence |
| [`grade-ops-ai-agents`](../agents/) | Spring AI agent runtime — Gemini-powered assessment pipeline |
| [`grade-ops-ai-infra`](../infra/) | Terraform — GCP infrastructure provisioning |
| [`grade-ops-ai-docs`](../docs/) | Canonical product and architecture documentation |
