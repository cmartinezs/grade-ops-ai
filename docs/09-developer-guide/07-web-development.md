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

### 7. Styling and Design System

The web frontend uses a **two-layer styling model**:

| Layer | What it handles | How |
|-------|----------------|-----|
| GradeOps AI Design System | All branded UI — buttons, inputs, cards, badges, shell chrome | CSS custom property tokens + inline styles in TSX components |
| Tailwind CSS | Generic layout utilities where DS tokens are not applicable | Utility classes |

**Always prefer DS components over raw HTML or Tailwind for any branded UI element.** Tailwind is still available for structural utilities (e.g., `flex`, `gap-*`, `w-full`) when a DS component does not cover the need.

#### Design System tokens

Tokens are defined in `src/styles/ds-tokens/` and loaded into the global cascade via `src/app/globals.css`:

| File | Tokens |
|------|--------|
| `colors.css` | `--sprout-*`, `--gold-*`, `--slate-*`, `--success-*`, `--warning-*`, `--danger-*`, `--info-*` + semantic aliases (`--brand`, `--surface-card`, `--border-default`, etc.) |
| `typography.css` | `--font-display` (Bricolage Grotesque), `--font-sans` (Hanken Grotesque), `--text-xs` … `--text-3xl` |
| `spacing.css` | `--space-*` scale |
| `radius.css` | `--radius-sm`, `--radius-md`, `--radius-lg` |
| `shadows.css` | `--shadow-sm`, `--shadow-brand`, `--ring` |

Reference tokens by name in `style={{}}` props: `style={{ color: "var(--text-strong)" }}`.

#### DS component library

All DS components live in `src/components/ds/` and are re-exported from `src/components/ds/index.ts`.

| Component | Description |
|-----------|-------------|
| `Button` | `variant` primary/ghost/outline, `size` sm/md, `loading`, `block` |
| `Avatar` | Initials circle, `size` sm/md |
| `IconButton` | Icon-only button with `label` for aria/tooltip |
| `LucideIcon` | 18 Lucide icons as inline SVG (no npm dependency). `name`, `size`, `color` |
| `Input` | Text input with focus ring, icon prefix, optional password-toggle (`showToggle`), and inline error message |
| `Field` | Label wrapper for any input slot (`label`, `htmlFor`) |
| `FieldWithHelper` | Same as `Field` plus an inline `?` button that shows a tooltip explaining the field on hover/focus |
| `GoogleButton` | Google sign-in button with Firebase popup logic, loading state, and error display |
| `Badge` | Status/tone pill. `tone`: brand/gold/success/warning/danger/info/neutral. `dot` for pulsing indicator |
| `StatCard` | Metric card with label, value, optional unit, delta, and icon |
| `Card` | Compound card: `Card.Header`, `Card.Title`, `Card.Body`, `Card.Footer` |

Do not add CSS classes, Tailwind utilities, or `style={{}}` overrides that override DS token values on these components. Extend the DS component instead.

#### Shell layout

Protected pages run inside `AppShell` (sidebar 256 px + topbar 64 px), which is injected by `(protected)/layout.tsx`. Each page declares its topbar content via the `useShellConfig` hook:

```typescript
import { useShellConfig } from "@/components/shell/ShellContext";
import { Button } from "@/components/ds";

export default function MyPage() {
  useShellConfig({
    title: "Nombre de la sección",
    subtitle: "Descripción breve",
    actions: <Button variant="primary">+ Acción</Button>,
  });
  // ...
}
```

The sidebar nav item is highlighted automatically based on `usePathname()`. No prop needs to be passed.

#### Placeholder pages

Routes that are not yet implemented should render `PlaceholderPage` from `src/components/shell/PlaceholderPage.tsx`:

```typescript
import PlaceholderPage from "@/components/shell/PlaceholderPage";
import LucideIcon from "@/components/ds/LucideIcon";

<PlaceholderPage
  icon={<LucideIcon name="file-pen-line" size={40} color="var(--text-subtle)" />}
  title="Nombre de la sección"
  description="Descripción orientativa de la funcionalidad futura."
  badge="Próximamente"
/>
```

### 8. Form validation

All forms must use **React Hook Form + Zod**. Do not use HTML native validation attributes (`required`, `minLength`, `pattern`, `type="email"` as a validator, etc.).

```typescript
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

const schema = z.object({
  email: z.string().min(1, "Ingresa tu correo.").email("Formato de correo inválido."),
  password: z.string().min(8, "Mínimo 8 caracteres."),
});

type Fields = z.infer<typeof schema>;

export default function MyForm() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Fields>({
    resolver: zodResolver(schema),
  });

  async function onSubmit(data: Fields) { /* API call */ }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Field label="Correo" htmlFor="email">
        <Input id="email" type="email" error={errors.email?.message} {...register("email")} />
      </Field>
      <Button type="submit" loading={isSubmitting}>Enviar</Button>
    </form>
  );
}
```

- Field-level errors come from `errors.<field>.message` and are passed to `<Input error={...}>`, which renders them inline below the input.
- Server-side errors (API responses) are managed with separate `useState` and rendered as a `<p role="alert">`.
- `isSubmitting` from RHF replaces manual `loading` state for the submit button.

### Field help tooltips — design rule

**Every form field that a user must complete must have a contextual help tooltip** unless the field label is entirely self-explanatory (e.g., "Email"). Use `FieldWithHelper` instead of `Field` whenever the field purpose, format, or constraints benefit from a short explanation.

```typescript
import { FieldWithHelper, Input } from "@/components/ds";

<FieldWithHelper
  label="Correo electrónico"
  htmlFor="email"
  helper="Usamos tu correo solo para enviarte el enlace de acceso y notificaciones del sistema. No se comparte con terceros."
>
  <Input id="email" type="email" error={errors.email?.message} {...register("email")} />
</FieldWithHelper>
```

Rules for the `helper` text:
- One or two sentences maximum. Write for a non-technical user.
- Explain **why** the field exists or **what format** is expected — not just what the label already says.
- Do not repeat the label verbatim.
- Language must match the product language (Spanish for teacher-facing UI).

The `?` button is 15 px, uses `--text-subtle` for color, shows a dark tooltip above on hover and keyboard focus, and carries an accessible `aria-label`. It must not be removed or hidden even if the field appears obvious — the rule exists to reduce abandonment in registration and configuration flows.

---

## Adding a new page

### Protected page (requires login)

1. Create the page file:
   ```
   src/app/(protected)/your-page/page.tsx
   ```
   Add `"use client"` at the top (required — protected pages use hooks).

2. Declare the shell config at the top of the component:
   ```typescript
   useShellConfig({ title: "Mi página", subtitle: "Descripción", actions: <Button>Acción</Button> });
   ```

3. Fetch data using an API wrapper (do not call `apiClient` directly in page components):
   ```typescript
   // src/lib/api/your-resource.ts
   export async function getYourResource(): Promise<YourResource[]> {
     const response = await apiClient("/api/your-resource");
     if (!response.ok) throw new Error("Failed to fetch");
     return response.json();
   }
   ```

4. Add TypeScript interfaces:
   ```
   src/types/your-resource.ts
   ```

5. Add domain components:
   ```
   src/components/your-domain/YourComponent.tsx
   ```

### Public page (no auth required)

Create the page under `src/app/your-page/page.tsx` (outside the `(protected)` route group). Public pages do not get `AuthGuard` or `AppShell` automatically.

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
