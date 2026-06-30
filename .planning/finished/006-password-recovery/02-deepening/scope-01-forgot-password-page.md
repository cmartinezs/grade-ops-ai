# 🔍 DEEPENING: Scope 01 — Página `/forgot-password` y enlace desde `/login`

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Crear la ruta `/forgot-password` en `web/` con un formulario de un campo (email), validado con RHF + Zod, que llama a `sendPasswordResetEmail` de Firebase Auth. El resultado visible es siempre el mismo mensaje neutral (éxito o email no encontrado). Además, actualizar el enlace "¿Olvidaste tu contraseña?" en `/login` para que navegue a esta nueva ruta. Incluir tests.

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Crear página `/forgot-password`](scope-01-forgot-password-page/task-01-forgot-password-page.md) | GENERATE-DOCUMENT | DONE | `web/src/app/forgot-password/page.tsx` |
| 2 | [Actualizar enlace en `/login`](scope-01-forgot-password-page/task-02-update-login-link.md) | GENERATE-DOCUMENT | DONE | `web/src/app/login/page.tsx` (modificado) |
| 3 | [Tests de ForgotPasswordPage](scope-01-forgot-password-page/task-03-tests.md) | GENERATE-DOCUMENT | DONE | `web/src/app/forgot-password/__tests__/ForgotPasswordPage.test.tsx` |

---

## Done Criteria

- [x] Ruta `/forgot-password` existe y renderiza sin errores
- [x] Formulario usa `FieldWithHelper` + `Input` + `Button variant="primary"` del DS
- [x] Email validado con Zod antes de cualquier llamada a Firebase (`min(1)` + `.email()`)
- [x] `auth/invalid-email` de Firebase muestra error de campo inline, NO el mensaje de confirmación
- [x] Éxito y `auth/user-not-found` muestran el mismo mensaje neutral: "Si existe una cuenta con ese correo, recibirás un enlace en los próximos minutos."
- [x] Estado `isSubmitting` deshabilita el botón y muestra loader durante la llamada a Firebase
- [x] Enlace "¿Olvidaste tu contraseña?" en `/login` navega a `/forgot-password` (no `href="#"`)
- [x] Layout de dos columnas con panel Sprout idéntico al de `/login` y `/register`
- [x] Link de vuelta a `/login` presente en la página
- [x] Tests pasan: email válido → confirmación, formato inválido → error campo, `auth/invalid-email` de Firebase → error campo
- [ ] `npm run build` sin errores de TypeScript
- [ ] TRACEABILITY.md actualizado con nuevos términos

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | Página custom `/reset-password` via `ActionCodeSettings.url` | Future | Deferred — MVP usa pantalla default de Firebase |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)
