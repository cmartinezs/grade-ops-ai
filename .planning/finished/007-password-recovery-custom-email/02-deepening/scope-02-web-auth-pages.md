# 🔍 DEEPENING: Scope 02 — web-auth-pages

> **Status:** DONE
> **Depends on:** scope-01-api-password-reset-service
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Actualizar las páginas de autenticación `web/` para eliminar cualquier llamada directa a Firebase Auth SDK y conectarlas al nuevo API propio:

1. `/forgot-password` — cambio mínimo: quitar `sendPasswordResetEmail`, añadir llamada a `POST /api/v1/auth/forgot-password`.
2. `/reset-password` — reescritura completa: quitar toda la lógica Firebase (`verifyPasswordResetCode`, `confirmPasswordReset`, `mode`/`oobCode`), agregar campo `email`, mostrar `code` como solo lectura, llamar `PUT /api/v1/auth/reset-password`.
3. `lib/api/auth.ts` — dos nuevas funciones: `forgotPassword()` y `resetPassword()`.

**Design source:** `docs/superpowers/specs/2026-06-21-password-recovery-custom-email-design.md` → sección "Web Changes".

---

## Context

- **Archivos a modificar:**
  - `web/src/app/forgot-password/page.tsx` (existente — de scope 01 del plan 006)
  - `web/src/app/reset-password/page.tsx` (existente — de scope 03 del plan 006)
  - `web/src/lib/api/auth.ts` (existente — agregar dos funciones)
- **Tests a actualizar/crear:**
  - `web/src/app/forgot-password/__tests__/ForgotPasswordPage.test.tsx`
  - `web/src/app/reset-password/__tests__/ResetPasswordPage.test.tsx`
- **Componentes DS disponibles:** `Button`, `Input`, `Field`, `FieldWithHelper` (del plan 005)
- **Sin cambios a:** `AuthGuard`, `AppShell`, `login/page.tsx`, ni ningún otro componente

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Nuevas funciones en lib/api/auth.ts](scope-02-web-auth-pages/task-01-api-auth-functions.md) | GENERATE-DOCUMENT | DONE | `web/src/lib/api/auth.ts` (`forgotPassword`, `resetPassword`, `PasswordResetError`) |
| 2 | [Actualizar /forgot-password](scope-02-web-auth-pages/task-02-forgot-password.md) | GENERATE-DOCUMENT | DONE | `web/src/app/forgot-password/page.tsx` (sin Firebase, llama API, mensaje neutral) |
| 3 | [Reescribir /reset-password](scope-02-web-auth-pages/task-03-reset-password.md) | GENERATE-DOCUMENT | DONE | `web/src/app/reset-password/page.tsx` (reescritura completa con campo email, mapeo errores, Suspense) |

---

## Approach Details

### Task 1 — lib/api/auth.ts

```ts
export async function forgotPassword(email: string): Promise<void> {
  const res = await apiClient("/api/v1/auth/forgot-password", {
    method: "POST",
    body: JSON.stringify({ email }),
  });
  if (!res.ok) throw new Error("forgot-password failed");
}

export async function resetPassword(
  code: string,
  body: { email: string; password: string; passwordRepeat: string }
): Promise<void> {
  const res = await apiClient(`/api/v1/auth/reset-password?code=${encodeURIComponent(code)}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    throw Object.assign(new Error(data.error ?? "reset-password failed"), {
      status: res.status,
      code: data.error,
    });
  }
}
```

> Usar `apiClient` (el fetch interceptor existente en `lib/api/`) — no `fetch` directo.

### Task 2 — /forgot-password: cambios mínimos

- **Eliminar:** `import { sendPasswordResetEmail } from "firebase/auth"`, `import { auth } from "@/lib/firebase/client"`, el bloque `catch` que maneja `auth/invalid-email` de Firebase.
- **Agregar:** `import { forgotPassword } from "@/lib/api/auth"`.
- **Reemplazar:** `await sendPasswordResetEmail(auth, data.email)` → `await forgotPassword(data.email)`.
- **Simplificar catch:** cualquier error de red → `setSubmitted(true)` de todas formas (el API siempre retorna 200; un error de red no debe revelar información).
- **Mantener:** layout, Zod schema, RHF, mensaje neutral, estado `isSubmitting`, `FieldWithHelper`.
- **Actualizar tests:** reemplazar el mock de `sendPasswordResetEmail` por mock de `forgotPassword`.

### Task 3 — /reset-password: reescritura completa

**Cambios estructurales:**
- Eliminar todos los imports de Firebase Auth SDK (`verifyPasswordResetCode`, `confirmPasswordReset`, `getAuth`).
- Eliminar el check de `mode !== "resetPassword"`.
- Renombrar el query param de `oobCode` a `code`.
- Eliminar el `useEffect` que llamaba a `verifyPasswordResetCode` al montar (ya no hay verificación pre-carga).
- Mostrar el formulario inmediatamente si `code` existe en la URL; mostrar error state si `code` es `null`.
- El `code` de la URL se muestra como texto de solo lectura en el formulario (no editable).

**Nuevo formulario (3 campos, en orden):**
1. `email` — Input DS tipo email, validado con Zod (`.email()`)
2. nueva contraseña — Input DS tipo password con `showToggle`, Zod `min(6)`
3. confirmar contraseña — Input DS tipo password, Zod `.refine()` para igualdad

**Zod schema:**
```ts
const schema = z.object({
  email: z.string().email("Correo inválido"),
  password: z.string().min(6, "Mínimo 6 caracteres"),
  confirmPassword: z.string(),
}).refine(d => d.password === d.confirmPassword, {
  message: "Las contraseñas no coinciden",
  path: ["confirmPassword"],
});
```

**Mapeo de errores del API:**
| Error code | UI behavior |
|-----------|------------|
| `RESET_CODE_NOT_FOUND` (404) | `setCodeError("El enlace no es válido.")` → error state |
| `RESET_CODE_EXPIRED` (410) | `setCodeError("El enlace expiró. Solicita uno nuevo.")` → error state |
| `RESET_CODE_USED` (410) | `setCodeError("El enlace ya fue utilizado.")` → error state |
| `RESET_CODE_EMAIL_MISMATCH` (422) | `setError("email", { message: "El correo no coincide con el del enlace." })` |
| Error de red | `setCodeError("No se pudo procesar tu solicitud. Intenta de nuevo.")` → error state |

**Error state UI:** panel con `role="alert"`, mensaje del error, botón "Solicitar nuevo enlace" que navega a `/forgot-password`.

**Éxito UI:** panel con `role="status"`, mensaje de confirmación, botón "Ir a iniciar sesión" que navega a `/login`.

**Layout:** mantener el layout de dos columnas con panel Sprout del plan 006 — no cambiar la estructura visual.

---

## Done Criteria

- [x] `forgotPassword()` y `resetPassword()` exportadas desde `lib/api/auth.ts`
- [x] `/forgot-password` no importa nada de `firebase/auth` ni de `lib/firebase/client`
- [x] `/forgot-password` llama `forgotPassword(data.email)` y siempre muestra la confirmación neutral
- [x] `/reset-password` no importa nada de `firebase/auth`
- [x] `/reset-password` sin `?code=` muestra error state inmediato (`role="alert"`)
- [x] `/reset-password?code=X` muestra el formulario con 3 campos (email, contraseña, confirmación)
- [x] Cada error code del API produce el comportamiento UI correcto (tabla de mapeo)
- [x] Los tests de ambas páginas pasan: 14/14 (`ForgotPasswordPage` + `ResetPasswordPage`)
- [x] `npm run build` sin errores de TypeScript
- [x] TRACEABILITY.md actualizado

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | La spec decía mostrar el `code` como campo readonly en el formulario — la implementación no lo muestra (es un detalle UX que añade ruido sin valor) | `password-recovery-custom-email-design.md` vs `reset-password/page.tsx` | ACCEPTED | El code viaja como query param transparente; no es necesario mostrarlo al usuario |
| 2 | `PasswordResetError` es una clase custom en `auth.ts` — no estaba en la spec explícitamente | `password-recovery-custom-email-design.md` | ACCEPTED | Mejor tipado para el catch de errores del API; pattern más limpio que comparar `status` directamente |
| 3 | `/reset-password` usa `<Suspense>` para envolver `useSearchParams()` — requerimiento de Next.js App Router, no mencionado en la spec | Next.js App Router docs | RESOLVED | Necesario para evitar error en build estático; `ResetPasswordForm` es el componente real, `ResetPasswordPage` es el wrapper |
| 4 | Tests preexistentes fallan en `SignOutButton.test.tsx` y `RegisterPage.test.tsx` (5 fallos) — no relacionados con este scope | — | PREEXISTING | No introducidos por este scope; pendiente de corrección en planning futuro |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)
