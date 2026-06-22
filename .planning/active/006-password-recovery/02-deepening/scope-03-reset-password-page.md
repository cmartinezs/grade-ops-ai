# 🔍 DEEPENING: Scope 03 — Página custom `/reset-password` (Opción B)

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Implementar la ruta `/reset-password` en `web/` como handler custom de la acción de Firebase. La página lee `mode` y `oobCode` de los query params del link enviado por Firebase, verifica el código con `verifyPasswordResetCode`, muestra el formulario de nueva contraseña, y confirma el cambio con `confirmPasswordReset`. Firebase Console debe configurar esta ruta como Custom Action URL para los correos de reset.

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | Página `/reset-password` + tests | GENERATE-DOCUMENT | DONE | `web/src/app/reset-password/page.tsx`, `web/src/app/reset-password/__tests__/ResetPasswordPage.test.tsx` |
| 2 | Actualizar US-012 DoD y Technical Notes | GENERATE-DOCUMENT | DONE | `docs/02-product/user-stories/epic-01-teacher-onboarding/12-password-recovery.md` |

---

## Done Criteria

- [x] Ruta `/reset-password` implementada con `verifyPasswordResetCode` en `useEffect` al montar
- [x] `oobCode` ausente o `mode !== "resetPassword"` muestra estado de error con `role="alert"`
- [x] `verifyPasswordResetCode` rechazado (código expirado/inválido) muestra estado de error
- [x] Formulario de dos campos (nueva contraseña + confirmar) con `FieldWithHelper` y `showToggle`
- [x] Validación Zod: `min(6)` en contraseña, `.refine()` para coincidir ambos campos
- [x] `confirmPasswordReset` rechazado con código expirado redirige a estado de error
- [x] Éxito muestra `role="status"` con confirmación y botón a `/login`
- [x] 6 tests pasan en `ResetPasswordPage.test.tsx`
- [x] US-012 DoD actualizado con los nuevos criterios marcados `[x]`
- [x] Technical Notes actualizadas con instrucciones de Firebase Console
- [x] Layout de dos columnas idéntico al resto de páginas de auth

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None* | — | — | — |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | Configurar Firebase Console Custom Action URL en el entorno `demo` | Infra / deploy | Deferred — requiere acceso a Firebase Console del proyecto |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)
