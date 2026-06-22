# ⚛️ TASK 01 — Marcar Definition of Done de US-012

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** scope-01 completo (DONE)
> [← scope file](../scope-02-docs-us012-dod.md)

---

## Objective

Verificar cada criterio de la Definition of Done de US-012 contra la implementación entregada en scope-01 y marcar los checkboxes correspondientes en `docs/02-product/user-stories/epic-01-teacher-onboarding/12-password-recovery.md`.

---

## Technical Design

- **Approach:** Lectura de la implementación real (archivos creados en scope-01) y verificación uno a uno contra cada ítem del DoD. Solo marcar `[x]` los criterios que estén efectivamente implementados — no anticipar.

- **Affected files:**
  - `docs/02-product/user-stories/epic-01-teacher-onboarding/12-password-recovery.md` (modificar — checkboxes)

- **Criterios a verificar:**

  | # | Criterio DoD | Cómo verificar |
  |---|-------------|---------------|
  | 1 | Ruta `/forgot-password` con formulario email RHF + Zod | `web/src/app/forgot-password/page.tsx` existe y usa `zodResolver` |
  | 2 | `sendPasswordResetEmail` maneja el reset sin API call | El archivo importa de `firebase/auth`, no de `@/lib/api` |
  | 3 | Confirmación neutral en éxito y `user-not-found` | Código maneja ambos casos con `setSubmitted(true)` |
  | 4 | `auth/invalid-email` → error de campo, no confirmación | Bloque `catch` diferencia este código específico |
  | 5 | Enlace en `/login` navega a `/forgot-password` | `login/page.tsx` tiene `<Link href="/forgot-password">` |
  | 6 | Tests cubren los casos requeridos | `ForgotPasswordPage.test.tsx` con 5 tests pasando |

---

## Implementation Steps

1. Leer `web/src/app/forgot-password/page.tsx` y verificar criterios 1–4.
2. Leer `web/src/app/login/page.tsx` y verificar criterio 5.
3. Ejecutar `npm test -- --testPathPattern=ForgotPasswordPage` y verificar criterio 6.
4. Marcar `[x]` los 6 checkboxes en `12-password-recovery.md`.

---

## Unit Tests

> Tarea documental — sin tests propios. La verificación es la ejecución de los tests del scope-01.

| # | Verificación | Cómo validar |
|---|-------------|-------------|
| 1 | Tests del scope-01 pasan | `npm test -- --testPathPattern=ForgotPasswordPage` → 5/5 |
| 2 | Los 6 checkboxes marcados `[x]` en el archivo MD | Lectura del archivo actualizado |

---

## Done Criteria

- [x] Los 6 checkboxes de la Definition of Done de US-012 están marcados `[x]`
- [x] La verificación fue hecha contra la implementación real, no anticipada
- [x] No scope creep: satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-02-docs-us012-dod.md)
