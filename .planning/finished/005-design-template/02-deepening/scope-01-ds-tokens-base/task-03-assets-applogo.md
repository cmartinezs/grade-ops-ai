# ⚛️ TASK 03 — Copiar logos SVG y actualizar AppLogo

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← scope file](../scope-01-ds-tokens-base.md)

---

## Objective

Copiar los logos SVG del design system a `web/public/brand/` y actualizar el componente `AppLogo.tsx` para que use el `logo-mark.svg` del DS con los colores de la paleta Sprout (verde, no indigo), alineando la identidad visual del proyecto web con la marca definida.

---

## Technical Design

- **Approach:** Los SVGs del DS se sirven desde `web/public/brand/` como assets estáticos Next.js (accesibles en `/brand/*.svg`). El componente `AppLogo.tsx` se reescribe: en lugar del SVG hardcoded con gradiente indigo/azul, usa `<img src="/brand/logo-mark.svg">` para el mark y un span con tipografía Bricolage Grotesque para el wordmark. Los colores del wordmark usan tokens DS: "GradeOps" en `--text-strong`, "AI" en `--brand` (verde Sprout). El slogan se mantiene si aplica.

- **Affected files / components:**
  - `design/design-system/assets/logo-mark.svg` → `web/public/brand/logo-mark.svg` (copia)
  - `design/design-system/assets/logo-wordmark.svg` → `web/public/brand/logo-wordmark.svg` (copia)
  - `design/design-system/assets/logo-wordmark-light.svg` → `web/public/brand/logo-wordmark-light.svg` (copia)
  - `web/src/components/brand/AppLogo.tsx` (reescribir)

- **Interfaces / contracts:**
  ```tsx
  interface AppLogoProps {
    className?: string;
    size?: "sm" | "md" | "lg";  // sm: mark 28px / md: 34px / lg: 40px
    variant?: "mark-wordmark" | "mark-only" | "wordmark-only";
  }
  ```
  El componente es usado en `login/page.tsx` (mark + wordmark) y potencialmente en el sidebar del shell (mark-only + wordmark en texto).

- **Design notes:**
  - El `logo-mark.svg` del DS es un checkmark dentro de un cuadrado redondeado verde (`--sprout-600`) con un punto dorado (`--gold-400`) — SVG propio del DS, no generado aquí.
  - Usar `<img>` con `alt=""` (decorativa) y `aria-hidden="true"` para el mark.
  - El wordmark en código: `<span style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: 23, color: "var(--text-strong)", letterSpacing: "-0.02em" }}>GradeOps<span style={{ color: "var(--brand)" }}>AI</span></span>`
  - No usar `<Image>` de next/image para SVGs estáticos simples — `<img>` es suficiente y más sencillo.

---

## Implementation Steps

1. Crear directorio `web/public/brand/`.
2. Copiar los 3 SVGs:
   ```bash
   cp design/design-system/assets/logo-mark.svg web/public/brand/
   cp design/design-system/assets/logo-wordmark.svg web/public/brand/
   cp design/design-system/assets/logo-wordmark-light.svg web/public/brand/
   ```
3. Reescribir `web/src/components/brand/AppLogo.tsx`:
   ```tsx
   interface AppLogoProps {
     className?: string;
     size?: "sm" | "md" | "lg";
     variant?: "mark-wordmark" | "mark-only" | "wordmark-only";
   }

   const SIZES = { sm: 28, md: 34, lg: 40 };
   const FONT_SIZES = { sm: 18, md: 21, lg: 25 };

   export default function AppLogo({
     className = "",
     size = "md",
     variant = "mark-wordmark",
   }: AppLogoProps) {
     const px = SIZES[size];
     const fs = FONT_SIZES[size];

     return (
       <div className={`flex items-center gap-[10px] ${className}`}>
         {variant !== "wordmark-only" && (
           <img
             src="/brand/logo-mark.svg"
             alt=""
             aria-hidden="true"
             style={{ width: px, height: px }}
           />
         )}
         {variant !== "mark-only" && (
           <span style={{
             fontFamily: "var(--font-display)",
             fontWeight: 700,
             fontSize: fs,
             color: "var(--text-strong)",
             letterSpacing: "-0.02em",
             lineHeight: 1,
             userSelect: "none",
           }}>
             GradeOps
             <span style={{ color: "var(--brand)" }}>AI</span>
           </span>
         )}
       </div>
     );
   }
   ```
4. Verificar que `AppLogo` sigue funcionando en `login/page.tsx` (usa `<AppLogo className="mb-10" />`).
5. Confirmar visualmente que el logo-mark aparece verde (Sprout) y el wordmark usa Bricolage Grotesque.

---

## Unit Tests

> No hay tests de lógica — es un componente visual estático.

| # | Verificación | Cómo validar |
|---|-------------|-------------|
| 1 | `web/public/brand/logo-mark.svg` existe y es accesible en `/brand/logo-mark.svg` | Browser: `http://localhost:3000/brand/logo-mark.svg` |
| 2 | `AppLogo` renderiza el mark y el wordmark juntos por defecto | Inspección visual en login |
| 3 | El wordmark "AI" usa color `--brand` (verde, no indigo) | DevTools computed color |
| 4 | El mark SVG del DS (check verde + punto dorado) aparece correctamente | Inspección visual |
| 5 | `npm run dev` sin errores de import | Terminal |

---

## Done Criteria

- [x] `web/public/brand/` contiene `logo-mark.svg`, `logo-wordmark.svg`, `logo-wordmark-light.svg`
- [x] `AppLogo.tsx` usa `img` con `/brand/logo-mark.svg` para el mark
- [x] El wordmark usa `var(--font-display)` (Bricolage Grotesque) y `var(--brand)` para "AI"
- [x] `AppLogo` acepta props `size` y `variant`
- [x] El logo se ve correctamente en la página de login existente
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-01-ds-tokens-base.md)
