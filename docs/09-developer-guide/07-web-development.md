# Web Development Guide

This guide covers the Next.js frontend in `web/`. It documents the conventions, patterns, and step-by-step procedures for adding pages, components, and API calls.

---

## Stack

| Layer | Technology |
|-------|-----------|
| Framework | Next.js 15 (App Router) |
| UI library | React 19 |
| Language | TypeScript |
| Styling | Tailwind CSS |
| Auth client | Firebase JS SDK 11 |
| Testing | Jest + React Testing Library |
| Module alias | `@/` maps to `web/src/` |

---

## Key conventions

### 1. Client vs Server components

- All **interactive pages** — any page with auth state, forms, route guards, or Firebase SDK calls — must include `"use client"` as the first line of the file.
- Firebase Authentication runs entirely in the browser. Any component that calls `onAuthStateChanged`, `signInWithEmailAndPassword`, or any Firebase Auth function must be a client component.
- **Server components** can be used for purely static layout content that does not depend on auth state or user interaction.

When in doubt, use `"use client"`. The App Router will warn if you try to use browser APIs in a server component.

### 2. Authentication flow

Authentication is managed by the Firebase JS SDK. The implementation has three key pieces:

**`auth` singleton** — exported from `web/src/lib/firebase/client.ts`. Initialized once. Import this everywhere auth is needed:

```typescript
import { auth } from "@/lib/firebase/client";
```

**`AuthGuard` component** — located at `web/src/components/auth/AuthGuard.tsx`. Wraps all protected routes. It subscribes to `onAuthStateChanged` and handles three states:

| Auth state | Action |
|-----------|--------|
| `user === null` | Redirect to `/login` |
| `user.emailVerified === false` | Redirect to `/verify-email` |
| Verified user | Set `authorized = true`, render children |

While waiting for the first auth state event, `AuthGuard` renders a loading spinner. This prevents a flash of the protected content before the auth check completes.

**Protected route group** — all protected pages live under `src/app/(protected)/`. The route group layout wraps its children with `AuthGuard`, so individual page components do not need to call auth checks themselves.

### 3. API calls — always use `apiClient`

Never use raw `fetch` for API calls. The `apiClient` wrapper handles:
- Bearer token injection (fetches the current Firebase ID token and sets the `Authorization` header)
- 401 interception (redirects to login on token expiry)
- Consistent error handling

```typescript
import { apiClient } from "@/lib/api/client";

// Basic call
const response = await apiClient("/api/assessments");

// With method and body
const response = await apiClient("/api/assessments", {
  method: "POST",
  body: JSON.stringify(payload),
});
```

Domain-specific wrappers in `src/lib/api/` call `apiClient` internally:

```typescript
// src/lib/api/assessments.ts
export async function getAssessments(): Promise<AssessmentSummary[]> {
  const response = await apiClient("/api/assessments");
  if (!response.ok) {
    throw new Error(`Failed to fetch assessments: ${response.status}`);
  }
  return response.json() as Promise<AssessmentSummary[]>;
}
```

### 4. Types flow from API to Web

The Web layer does not define types independently. TypeScript interfaces in `src/types/` mirror the API DTO contracts defined in Java.

When the API changes a DTO (adds a field, changes a type), the corresponding TypeScript interface in `src/types/` must be updated to match.

Currently implemented:
- `src/types/assessment.ts` — mirrors `AssessmentSummaryDto`

Do not use `any` to work around type mismatches. Fix the interface instead.

### 5. API proxy (Next.js rewrite)

`next.config.ts` contains a rewrite rule that proxies `/api/:path*` to `${API_BASE_URL}/:path*`. This means:

- All `apiClient` calls use paths that start with `/api/`: `apiClient("/api/assessments")`
- In local development (`API_BASE_URL` not set), Next.js proxies to `http://localhost:8080` — the Spring Boot API
- In production (Cloud Run), `API_BASE_URL` is set to the Cloud Run api/ service URL

This means the browser never makes cross-origin requests to the API — all calls go through the Next.js server.

### 6. Firebase env vars

The Firebase web SDK is initialized with values from environment variables with the `NEXT_PUBLIC_` prefix. These are public by design — Firebase web config is not a secret:

```
NEXT_PUBLIC_FIREBASE_API_KEY
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN
NEXT_PUBLIC_FIREBASE_PROJECT_ID
NEXT_PUBLIC_FIREBASE_APP_ID
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID
```

Set these in `.env.local` for local development. Terraform outputs them for the demo environment (see [`09-deployment-guide.md`](09-deployment-guide.md)).

`.env.local` is gitignored and must never be committed. The values come from the Firebase console or from `terraform output`.

### 7. Styling

Use Tailwind CSS utility classes for all styling. Do not create custom CSS files unless Tailwind cannot handle the requirement (e.g., a complex animation that requires `@keyframes`). Do not use CSS modules or `styled-components`.

---

## Adding a new page

### Protected page (requires login)

1. Create the page file:
   ```
   src/app/(protected)/your-page/page.tsx
   ```
   Add `"use client"` at the top if the page uses state, effects, or event handlers.

2. Fetch data using an API wrapper (do not call `apiClient` directly in page components — keep data fetching in `src/lib/api/`):
   ```typescript
   // src/lib/api/your-resource.ts
   export async function getYourResource(): Promise<YourResource[]> {
     const response = await apiClient("/api/your-resource");
     if (!response.ok) throw new Error("Failed to fetch");
     return response.json();
   }
   ```

3. Add TypeScript interfaces:
   ```
   src/types/your-resource.ts
   ```

4. Add components:
   ```
   src/components/your-domain/YourComponent.tsx
   ```

### Public page (no auth required)

Create the page under `src/app/your-page/page.tsx` (outside the `(protected)` route group). Public pages do not get `AuthGuard` automatically.

---

## Adding a new API endpoint call

1. Add a function to the appropriate file in `src/lib/api/`, or create a new file:
   ```typescript
   // src/lib/api/your-resource.ts
   import { apiClient } from "@/lib/api/client";
   import { YourResource } from "@/types/your-resource";

   export async function createYourResource(payload: YourResourcePayload): Promise<YourResource> {
     const response = await apiClient("/api/your-resource", {
       method: "POST",
       headers: { "Content-Type": "application/json" },
       body: JSON.stringify(payload),
     });
     if (!response.ok) {
       const error = await response.json().catch(() => ({}));
       throw new Error(error.message ?? `Request failed: ${response.status}`);
     }
     return response.json();
   }
   ```

2. Always check `response.ok` before calling `.json()`. Non-OK responses from the API return error JSON, not the expected resource.

3. Add the corresponding TypeScript interface to `src/types/your-resource.ts`, mirroring the Java DTO.

---

## Common mock setup for tests

Every component test that uses navigation must mock `next/navigation`. Every test that touches auth must mock `@/lib/firebase/client`:

```typescript
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: jest.fn(), replace: jest.fn() }),
  useSearchParams: () => ({ get: jest.fn().mockReturnValue(null) }),
  usePathname: () => "/",
}));

jest.mock("@/lib/firebase/client", () => ({
  auth: { currentUser: null },
}));
```

Firebase auth functions (`onAuthStateChanged`, `signInWithEmailAndPassword`, etc.) are automatically mocked via the module mapper in `jest.config.ts` — all `firebase/auth` imports resolve to `src/test/__mocks__/firebase/auth.ts`.

---

<!-- nav -->

← [06-agent-development.md](06-agent-development.md) | [↑ Top](#web-development-guide) | [Testing Guide →](08-testing-guide.md)
