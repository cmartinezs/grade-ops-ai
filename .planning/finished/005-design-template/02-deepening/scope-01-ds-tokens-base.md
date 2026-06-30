# 🔍 DEEPENING: Scope 01 — ds-tokens-base

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Establecer la base del design system en el proyecto Next.js: importar los tokens CSS del DS, configurar las fuentes Google, copiar los assets SVG y limpiar el CSS genérico de TailwindCSS, dejando el entorno listo para que todos los componentes y páginas puedan usar los custom properties `--brand`, `--surface-card`, `--font-display`, etc.

---

## Context

El DS define sus tokens en `design/design-system/tokens/`:
- `colors.css` — paleta Sprout, Gold, Slate, semánticos, rúbrica, superficies, bordes, texto
- `typography.css` — `--font-display`, `--font-sans`, `--font-mono`, escalas de texto
- `spacing.css` — `--spacing-*`, `--radius-*`, `--shadow-*`, motion, z-index
- `fonts.css` — `@import` Google Fonts (Bricolage Grotesque, Hanken Grotesque, JetBrains Mono)
- `base.css` — reset + utilidades base

El punto de entrada `styles.css` importa todos los anteriores. El frontend actual solo tiene `@import "tailwindcss"` en `globals.css`.

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Copiar tokens CSS del DS al proyecto](scope-01-ds-tokens-base/task-01-copy-ds-tokens.md) | GENERATE-DOCUMENT | DONE | `web/src/styles/ds-tokens/*.css` + `globals.css` actualizado |
| 2 | [Configurar fuentes Google vía next/font en root layout](scope-01-ds-tokens-base/task-02-configure-fonts.md) | GENERATE-DOCUMENT | DONE | `web/src/app/layout.tsx` actualizado |
| 3 | [Copiar logos SVG y actualizar AppLogo](scope-01-ds-tokens-base/task-03-assets-applogo.md) | GENERATE-DOCUMENT | DONE | `web/public/brand/*.svg` + `AppLogo.tsx` actualizado |

---

## Approach Details

### Task 1 — Copiar tokens
```
design/design-system/tokens/colors.css    → web/src/styles/ds-tokens/colors.css
design/design-system/tokens/typography.css → web/src/styles/ds-tokens/typography.css
design/design-system/tokens/spacing.css   → web/src/styles/ds-tokens/spacing.css
design/design-system/tokens/base.css      → web/src/styles/ds-tokens/base.css
```
> No copiar `fonts.css` — las fuentes se manejan vía `next/font/google` en el layout (ver Task 3).

### Task 2 — globals.css
```css
@import "tailwindcss";
@import "../styles/ds-tokens/colors.css";
@import "../styles/ds-tokens/typography.css";
@import "../styles/ds-tokens/spacing.css";
@import "../styles/ds-tokens/base.css";

/* Sobrescribir variables de fuente con las de next/font */
:root {
  --font-display: var(--font-bricolage);
  --font-sans: var(--font-hanken);
  --font-mono: var(--font-jetbrains);
}

html, body {
  background: var(--surface-page);
  color: var(--text-body);
  font-family: var(--font-sans);
}
```

### Task 3 — layout.tsx con next/font
```tsx
import { Bricolage_Grotesque, Hanken_Grotesque, JetBrains_Mono } from "next/font/google";

const bricolage = Bricolage_Grotesque({
  subsets: ["latin"],
  variable: "--font-bricolage",
  display: "swap",
});
const hanken = Hanken_Grotesque({
  subsets: ["latin"],
  variable: "--font-hanken",
  display: "swap",
  weight: ["400", "500", "600"],
});
const jetbrains = JetBrains_Mono({
  subsets: ["latin"],
  variable: "--font-jetbrains",
  display: "swap",
});
```
> Pasar `className={`${bricolage.variable} ${hanken.variable} ${jetbrains.variable}`}` al `<html>` tag.

### Task 4 — Assets
```
design/design-system/assets/logo-mark.svg          → web/public/brand/logo-mark.svg
design/design-system/assets/logo-wordmark.svg      → web/public/brand/logo-wordmark.svg
design/design-system/assets/logo-wordmark-light.svg → web/public/brand/logo-wordmark-light.svg
```

### Task 5 — AppLogo.tsx
Reemplazar el SVG hardcoded con el logo-mark del DS. Usar `<Image>` de next/image o `<img>` con `/brand/logo-mark.svg`. El wordmark usa `font-display` (Bricolage Grotesque) con color `--text-strong` + `--brand` para "AI".

---

## Done Criteria

- [ ] `web/src/styles/ds-tokens/` contiene los 4 archivos CSS de tokens
- [ ] `globals.css` importa los tokens y setea `background: var(--surface-page)` en `body`
- [ ] Las fuentes Bricolage Grotesque y Hanken Grotesque se cargan correctamente (verificable en DevTools)
- [ ] `web/public/brand/` contiene los 3 SVGs del DS
- [ ] `AppLogo.tsx` usa colores DS: "GradeOps" en `--text-strong`, "AI" en `--brand`
- [ ] `npm run dev` corre sin errores de compilación
- [ ] TRACEABILITY.md actualizado con términos de este scope

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | `fonts.css` del DS usa `@import url(...)` de Google Fonts directamente — incompatible con next/font que requiere importación en layout | `design/design-system/tokens/fonts.css` vs `web/src/app/layout.tsx` | RESOLVED | Usar next/font/google en layout; no copiar fonts.css |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
