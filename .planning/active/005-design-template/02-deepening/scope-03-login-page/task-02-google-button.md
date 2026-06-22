# ⚛️ TASK 02 — Componente GoogleButton DS

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-03-login-page.md)

---

## Objective

Crear el componente `GoogleButton` con el estilo visual del design system (borde sutil, hover sunken, loading state), extrayendo y reutilizando la lógica de autenticación Firebase Google de `GoogleSignInButton.tsx` existente, sin duplicar la lógica de Firebase.

---

## Technical Design

- **Approach:** Crear `web/src/components/ds/GoogleButton.tsx` como un componente presentacional que delega la lógica a la prop `onSuccess`. Internamente usa `signInWithPopup` + `GoogleAuthProvider` (lógica extraída de `GoogleSignInButton.tsx`). El componente existente `GoogleSignInButton.tsx` queda deprecado o se convierte en un thin wrapper de `GoogleButton`.

- **Affected files / components:**
  - `web/src/components/ds/GoogleButton.tsx` (nuevo)
  - `web/src/components/auth/GoogleSignInButton.tsx` (adaptar — usar GoogleButton internamente)

- **Interfaces / contracts:**
  ```tsx
  interface GoogleButtonProps {
    onSuccess: (idToken: string, displayName: string) => Promise<void>;
    loading?: boolean;
    disabled?: boolean;
  }
  ```
  La prop `onSuccess` recibe `idToken` y `displayName` después de que Google SSO finaliza exitosamente. El componente maneja su propio estado de loading interno durante el popup de Google.

- **Design notes:**
  - Height: 46px, width: 100%
  - bg: `var(--surface-card)`, border: `1px solid var(--border-default)`
  - border-radius: `var(--radius-md)`
  - hover: bg `var(--surface-sunken)` (manejado con `onMouseEnter/Leave`)
  - font-weight: 600, font-size: `var(--text-md)`, color: `var(--text-body)`
  - gap: 10px entre logo Google SVG y texto
  - loading interno: texto cambia a "Conectando…" + spinner pequeño + `disabled`
  - Texto: "Continuar con Google"
  - El SVG del logo de Google (4 colores) se hardcodea inline (como en el UI kit de referencia)
  - Transition: `background 120ms`

---

## Implementation Steps

1. Crear `web/src/components/ds/GoogleButton.tsx`:
   ```tsx
   "use client";
   import { useState } from "react";
   import { signInWithPopup, GoogleAuthProvider } from "firebase/auth";
   import { auth } from "@/lib/firebase/client";

   export default function GoogleButton({ onSuccess, loading: externalLoading, disabled }: GoogleButtonProps) {
     const [loading, setLoading] = useState(false);
     const isLoading = loading || externalLoading;

     async function handleClick() {
       setLoading(true);
       try {
         const provider = new GoogleAuthProvider();
         const result = await signInWithPopup(auth, provider);
         const idToken = await result.user.getIdToken();
         const displayName = result.user.displayName ?? "";
         await onSuccess(idToken, displayName);
       } catch (err) {
         // TODO: manejar error visible al usuario si necesario
         console.error(err);
       } finally {
         setLoading(false);
       }
     }

     return (
       <button
         onClick={handleClick}
         disabled={isLoading || disabled}
         style={{ /* estilos DS */ }}
       >
         {/* SVG logo Google 4 colores */}
         {isLoading ? "Conectando…" : "Continuar con Google"}
         {isLoading && <Spinner />}
       </button>
     );
   }
   ```

2. Adaptar `web/src/components/auth/GoogleSignInButton.tsx` para usar `GoogleButton`:
   ```tsx
   import GoogleButton from "@/components/ds/GoogleButton";
   // Re-exportar como wrapper para compatibilidad con código existente
   export default function GoogleSignInButton({ onSuccess }: { onSuccess: (idToken: string, displayName: string) => Promise<void> }) {
     return <GoogleButton onSuccess={onSuccess} />;
   }
   ```
   > Si la interfaz de `onSuccess` en `GoogleSignInButton.tsx` difiere, ajustar el adapter.

3. Agregar al barrel export `web/src/components/ds/index.ts`.

---

## Unit Tests

| # | Caso | Resultado esperado | Archivo |
|---|------|-------------------|---------|
| 1 | Click en botón → abre popup Google | `signInWithPopup` llamado | Mock test (existente en `GoogleSignInButton.test.tsx`) |
| 2 | Durante loading → texto "Conectando…" + disabled | Estado visible | Visual |
| 3 | `onSuccess` exitoso → callback llamado con idToken | Callback invocado | Test con mock |
| 4 | Hover → bg `var(--surface-sunken)` | Color de hover correcto | Visual |

---

## Done Criteria

- [x] `GoogleButton.tsx` existe con estilo DS (height 46px, borde sutil, hover sunken)
- [x] El texto es "Continuar con Google" (en español)
- [x] Loading state muestra "Conectando…" + spinner + disabled
- [x] `GoogleSignInButton.tsx` sigue funcionando (reutiliza `GoogleButton`)
- [x] TypeScript sin errores
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-03-login-page.md)
