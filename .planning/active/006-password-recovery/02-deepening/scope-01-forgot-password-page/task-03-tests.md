# ⚛️ TASK 03 — Tests de `/forgot-password`

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02
> [← scope file](../scope-01-forgot-password-page.md)

---

## Objective

Escribir los tests de `ForgotPasswordPage` que cubran los cuatro caminos del formulario: validación Zod en cliente, comportamiento ante `auth/invalid-email` de Firebase, confirmación ante éxito, y confirmación ante `auth/user-not-found`.

---

## Technical Design

- **Approach:** Jest + React Testing Library, mismo patrón que `login/__tests__/SignInPage.test.tsx`. Se mockea `sendPasswordResetEmail` de `firebase/auth` y el módulo `@/lib/firebase/client`.

- **Affected files:**
  - `web/src/app/forgot-password/__tests__/ForgotPasswordPage.test.tsx` (nuevo)

- **Mocks necesarios:**
  ```ts
  jest.mock("firebase/auth", () => ({
    sendPasswordResetEmail: jest.fn(),
    getAuth: jest.fn(),
  }));
  jest.mock("@/lib/firebase/client", () => ({ auth: {} }));
  ```

- **Imports clave:**
  ```ts
  import { render, screen, waitFor } from "@testing-library/react";
  import userEvent from "@testing-library/user-event";
  import { sendPasswordResetEmail } from "firebase/auth";
  import ForgotPasswordPage from "../page";
  ```

- **Patrón de interacción:**
  ```ts
  const user = userEvent.setup();
  render(<ForgotPasswordPage />);
  await user.type(screen.getByLabelText(/correo/i), "teacher@example.com");
  await user.click(screen.getByRole("button", { name: /enviar/i }));
  ```

---

## Implementation Steps

1. Crear directorio `web/src/app/forgot-password/__tests__/`.
2. Crear `ForgotPasswordPage.test.tsx` con los 4 casos listados en Unit Tests.
3. Ejecutar `npm test -- --testPathPattern=ForgotPasswordPage` y verificar que los 4 pasan.

---

## Unit Tests

| # | Caso | Setup | Resultado esperado |
|---|------|-------|-------------------|
| 1 | Email inválido (Zod) — sin llamada a Firebase | Dejar campo vacío y hacer submit | Error inline "Ingresa tu correo." visible; `sendPasswordResetEmail` NO llamado |
| 2 | Email con formato incorrecto (Zod) | Escribir `"nodomain"` y hacer submit | Error inline "Ingresa una dirección de correo válida."; `sendPasswordResetEmail` NO llamado |
| 3 | Éxito — Firebase resuelve OK | `sendPasswordResetEmail` resuelve `undefined`; escribir email válido y submit | Mensaje con `role="status"` visible; formulario ya no visible |
| 4 | `auth/user-not-found` — mismo mensaje que éxito | `sendPasswordResetEmail` rechaza con `{ code: "auth/user-not-found" }`; escribir email válido y submit | Mismo mensaje de confirmación neutral; formulario ya no visible |
| 5 | `auth/invalid-email` desde Firebase — error de campo | `sendPasswordResetEmail` rechaza con `{ code: "auth/invalid-email" }`; escribir email y submit | Error de campo inline visible; mensaje de confirmación NO visible |

---

## Done Criteria

- [x] Archivo `web/src/app/forgot-password/__tests__/ForgotPasswordPage.test.tsx` existe
- [x] 5 tests definidos y todos pasan (`npm test`)
- [x] `sendPasswordResetEmail` no es llamado cuando Zod rechaza (casos 1 y 2)
- [x] `role="status"` con confirmación neutral aparece en casos 3 y 4
- [x] En caso 5 (`auth/invalid-email`), el `role="status"` NO aparece
- [x] No scope creep: satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-01-forgot-password-page.md)
