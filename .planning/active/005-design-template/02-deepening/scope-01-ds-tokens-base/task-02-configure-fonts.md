# ⚛️ TASK 02 — Configurar fuentes Google vía next/font en root layout

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← scope file](../scope-01-ds-tokens-base.md)

---

## Objective

Configurar las tres fuentes del design system (Bricolage Grotesque, Hanken Grotesque, JetBrains Mono) en el root layout de Next.js usando `next/font/google`, exponerlas como variables CSS (`--font-bricolage`, `--font-hanken`, `--font-jetbrains`), y conectarlas con las variables `--font-display`, `--font-sans`, `--font-mono` del DS en `globals.css`.

---

## Technical Design

- **Approach:** `next/font/google` es el mecanismo recomendado para fuentes en Next.js — descarga y sirve las fuentes en build time (sin request a Google en runtime), optimizando LCP. Cada fuente se instancia con `variable` CSS para inyectarla en el `<html>` element. En `globals.css` se sobrescriben las variables del DS (`--font-display`, `--font-sans`, `--font-mono`) con las variables de next/font, para que todos los tokens que las referencien funcionen correctamente.

- **Affected files / components:**
  - `web/src/app/layout.tsx` (modificar — agregar fuentes y variables CSS en `<html>`)
  - `web/src/app/globals.css` (modificar — sobrescribir variables de fuente en `:root`)

- **Interfaces / contracts:**
  - `--font-display` → Bricolage Grotesque (variable font, 400/600/700)
  - `--font-sans` → Hanken Grotesque (variable font, 400/500/600)
  - `--font-mono` → JetBrains Mono (variable font, 400)
  - El `<html>` element recibe las clases de next/font que inyectan las variables CSS en el DOM

- **Design notes:**
  - `display: "swap"` en todas las fuentes para evitar FOIT.
  - Bricolage Grotesque no tiene `weight` fijo — es variable (`wght` axis). Usar `variable: "--font-bricolage"` sin especificar `weight` para cargar la fuente completa; o especificar `weight: ["400", "600", "700"]` como subset.
  - El metadata del root layout se actualiza con `lang="es"` (idioma del producto).

---

## Implementation Steps

1. En `web/src/app/layout.tsx`, importar las 3 fuentes de `next/font/google`:
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
     weight: ["400"],
   });
   ```

2. Pasar las variables al elemento `<html>`:
   ```tsx
   <html lang="es" className={`${bricolage.variable} ${hanken.variable} ${jetbrains.variable}`}>
     <body>{children}</body>
   </html>
   ```

3. Actualizar el `metadata` del layout:
   ```tsx
   export const metadata: Metadata = {
     title: "GradeOps AI",
     description: "Plataforma de evaluaciones con IA para docentes",
     icons: { icon: "/icon.svg" },
   };
   ```

4. En `web/src/app/globals.css`, agregar al final del bloque `:root` o en `html`:
   ```css
   :root {
     --font-display: var(--font-bricolage), system-ui, sans-serif;
     --font-sans: var(--font-hanken), system-ui, sans-serif;
     --font-mono: var(--font-jetbrains), "Courier New", monospace;
   }
   ```
   > Esto sobrescribe los valores que venían del `typography.css` del DS.

5. Ejecutar `npm run dev` y verificar en DevTools que Bricolage Grotesque y Hanken Grotesque se cargan.

---

## Unit Tests

> Esta tarea no tiene tests de código — es configuración de fuentes.

| # | Verificación | Cómo validar |
|---|-------------|-------------|
| 1 | Bricolage Grotesque se aplica a textos con `font-family: var(--font-display)` | DevTools → inspeccionar un elemento con esa variable |
| 2 | Hanken Grotesque se aplica al `body` | DevTools → computed style del `body` |
| 3 | `--font-display`, `--font-sans`, `--font-mono` están en `:root` y apuntan a las fuentes correctas | DevTools |
| 4 | No hay requests a `fonts.googleapis.com` en Network (next/font sirve las fuentes localmente) | DevTools → Network tab |
| 5 | `npm run dev` y `npm run build` compilan sin errores | Terminal |

---

## Done Criteria

- [x] Las 3 fuentes están configuradas con `next/font/google` en `layout.tsx`
- [x] El `<html>` element tiene las 3 variables CSS de fuente inyectadas
- [x] `--font-display` resuelve a Bricolage Grotesque en el browser
- [x] `--font-sans` resuelve a Hanken Grotesk en el browser (nota: nombre correcto en Google Fonts es "Hanken Grotesk", no "Grotesque")
- [x] `lang="es"` está en el `<html>` element
- [x] `npm run dev` compila sin warnings de font
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-01-ds-tokens-base.md)
