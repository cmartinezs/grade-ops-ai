# ⚛️ TASK 02 — Actualizar enlace "¿Olvidaste tu contraseña?" en `/login`

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01 (la ruta `/forgot-password` debe existir)
> [← scope file](../scope-01-forgot-password-page.md)

---

## Objective

Cambiar el enlace `<a href="#">¿Olvidaste tu contraseña?</a>` en `web/src/app/login/page.tsx` por `<Link href="/forgot-password">` de Next.js para que la navegación sea correcta y prefetcheada.

---

## Technical Design

- **Approach:** Cambio mínimo en un único archivo. Reemplazar el `<a>` con tag `<Link>` de `next/link` para habilitar prefetch automático.

- **Affected files:**
  - `web/src/app/login/page.tsx` (modificar — una sola línea cambia)

- **Cambio concreto:**
  ```tsx
  // Antes
  <a href="#" style={{ ... }}>¿Olvidaste tu contraseña?</a>

  // Después
  import Link from "next/link";
  ...
  <Link href="/forgot-password" style={{ ... }}>¿Olvidaste tu contraseña?</Link>
  ```

- **Nota:** `Link` de Next.js acepta `style` igual que `<a>`. No requiere cambio de estilos.

---

## Implementation Steps

1. Abrir `web/src/app/login/page.tsx`.
2. Añadir `import Link from "next/link"` si no está ya importado.
3. Reemplazar `<a href="#">¿Olvidaste tu contraseña?</a>` por `<Link href="/forgot-password">¿Olvidaste tu contraseña?</Link>` manteniendo el `style` existente.
4. Verificar `npm run build`.

---

## Unit Tests

> Cambio de una línea — sin test unitario propio. El test de integración de `/forgot-password` (task-03) cubre que la navegación llega correctamente.

| # | Verificación | Cómo validar |
|---|-------------|-------------|
| 1 | El enlace existe en el DOM de `/login` | Inspección visual o test existente de login |
| 2 | Clic en el enlace navega a `/forgot-password` | `npm run dev` → probar en browser |
| 3 | `npm run build` sin errores | Terminal |

---

## Done Criteria

- [ ] `login/page.tsx` usa `<Link href="/forgot-password">` en lugar de `<a href="#">`
- [ ] `import Link from "next/link"` presente en el archivo
- [ ] El estilo visual del enlace no cambia
- [ ] `npm run build` sin errores TypeScript
- [ ] No scope creep: satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-01-forgot-password-page.md)
