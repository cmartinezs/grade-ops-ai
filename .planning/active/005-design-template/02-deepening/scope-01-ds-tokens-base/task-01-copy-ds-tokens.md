# ⚛️ TASK 01 — Copiar tokens CSS del DS al proyecto

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-01-ds-tokens-base.md)

---

## Objective

Copiar los archivos de tokens CSS del design system (`design/design-system/tokens/`) a `web/src/styles/ds-tokens/` e importarlos en `globals.css`, estableciendo todas las custom properties del DS (`--brand`, `--surface-card`, `--text-strong`, `--radius-md`, etc.) como base del proyecto Next.js.

---

## Technical Design

- **Approach:** Copia física de los archivos de tokens (no symlinks — Next.js necesita archivos dentro del proyecto para resolución de imports). Se copian 4 archivos: `colors.css`, `typography.css`, `spacing.css`, `base.css`. **No se copia `fonts.css`** — las fuentes se manejan en task-02 vía `next/font/google`. El archivo `globals.css` se actualiza para importar los tokens en el orden correcto (colors → typography → spacing → base) usando paths relativos. Tailwind v4 permanece — sus utilidades coexisten con los tokens DS sin conflicto (los tokens son custom properties CSS, no clases Tailwind).

- **Affected files / components:**
  - `design/design-system/tokens/colors.css` → `web/src/styles/ds-tokens/colors.css` (copia)
  - `design/design-system/tokens/typography.css` → `web/src/styles/ds-tokens/typography.css` (copia)
  - `design/design-system/tokens/spacing.css` → `web/src/styles/ds-tokens/spacing.css` (copia)
  - `design/design-system/tokens/base.css` → `web/src/styles/ds-tokens/base.css` (copia)
  - `web/src/app/globals.css` (modificar — agregar imports)

- **Interfaces / contracts:** Tras esta tarea, todos los componentes que usen `style={{ color: "var(--brand)" }}` o `var(--surface-card)` etc. resolverán correctamente. Los tokens están disponibles en todo el árbol de componentes de Next.js.

- **Design notes:**
  - El orden de importación importa: `colors.css` primero (define los primitivos `--sprout-*`, `--gold-*`, `--slate-*`), luego `typography.css` (puede referenciar tokens de color), luego `spacing.css`, luego `base.css` (reset que puede usar cualquier token).
  - Verificar que `base.css` no incluya `@import url(...)` de Google Fonts — si lo hace, eliminar esa línea (las fuentes van en task-02).
  - La variable `--font-display`, `--font-sans`, `--font-mono` que define `typography.css` será **sobreescrita** en `globals.css` por las variables de next/font (task-02). Esto es intencional.

---

## Implementation Steps

1. Crear directorio `web/src/styles/ds-tokens/`.
2. Copiar los 4 archivos de tokens:
   ```bash
   cp design/design-system/tokens/colors.css web/src/styles/ds-tokens/
   cp design/design-system/tokens/typography.css web/src/styles/ds-tokens/
   cp design/design-system/tokens/spacing.css web/src/styles/ds-tokens/
   cp design/design-system/tokens/base.css web/src/styles/ds-tokens/
   ```
3. Inspeccionar `base.css`: si contiene `@import url(...)` de Google Fonts, eliminarlo.
4. Actualizar `web/src/app/globals.css`:
   ```css
   @import "tailwindcss";
   @import "../styles/ds-tokens/colors.css";
   @import "../styles/ds-tokens/typography.css";
   @import "../styles/ds-tokens/spacing.css";
   @import "../styles/ds-tokens/base.css";

   /* Las variables --font-display/sans/mono serán sobrescritas por task-02 */
   html, body {
     background: var(--surface-page);
     color: var(--text-body);
     font-family: var(--font-sans);
   }
   ```
5. Ejecutar `npm run dev` y verificar en DevTools que `--brand`, `--surface-page`, `--sprout-600` etc. existen en `:root`.

---

## Unit Tests

> Esta tarea no tiene tests de código — es configuración de assets estáticos.

| # | Verificación | Cómo validar |
|---|-------------|-------------|
| 1 | Los 4 archivos CSS existen en `web/src/styles/ds-tokens/` | `ls web/src/styles/ds-tokens/` |
| 2 | `--brand` está definido en `:root` | DevTools → Elements → `:root` computed styles |
| 3 | `--surface-page` resuelve a `#F8F8F5` (slate-50 cálido) | DevTools |
| 4 | `body` tiene `background: var(--surface-page)` | Abrir el proyecto en el browser |
| 5 | `npm run dev` compila sin errores | Terminal |

---

## Done Criteria

- [x] `web/src/styles/ds-tokens/` contiene `colors.css`, `typography.css`, `spacing.css`, `base.css`
- [x] `globals.css` importa los 4 tokens en orden correcto
- [x] `--brand`, `--sprout-600`, `--gold-400`, `--surface-card`, `--border-subtle`, `--text-strong` están disponibles en `:root`
- [x] `body` tiene `background: var(--surface-page)` y `color: var(--text-body)`
- [x] `npm run dev` compila sin errores de CSS
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-01-ds-tokens-base.md)
