# ⚛️ TASK 01 — Íconos Lucide como SVGs inline

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-02-shell-layout.md)

---

## Objective

Implementar los íconos Lucide necesarios para el shell y el dashboard como SVGs inline en un componente `LucideIcon.tsx`, sin dependencias npm ni CDN, usando solo los path data de Lucide hardcodeados para los ~13 íconos requeridos.

---

## Technical Design

- **Approach:** Un único componente `LucideIcon.tsx` que recibe un `name` y devuelve el SVG correspondiente. Los path data se definen en un objeto de lookup (`ICON_PATHS`) con la lista exacta de íconos necesarios. El SVG usa siempre `stroke="currentColor"`, `strokeWidth={2}`, `strokeLinecap="round"`, `strokeLinejoin="round"`, `fill="none"`, `viewBox="0 0 24 24"`. Se puede pasar `size` (default 24) y `color` (default "currentColor") como props.

- **Affected files / components:**
  - `web/src/components/ds/LucideIcon.tsx` (nuevo)

- **Interfaces / contracts:**
  ```tsx
  type LucideIconName =
    | "layout-dashboard" | "file-pen-line" | "library" | "users" | "bar-chart-3"
    | "bell" | "sparkles" | "log-out" | "chevron-right"
    | "trending-up" | "trending-down" | "alert-triangle"
    | "calendar-clock" | "clipboard-check" | "mail" | "lock"
    | "graduation-cap" | "file-pen-line" | "minus";

  interface LucideIconProps {
    name: LucideIconName;
    size?: number;
    color?: string;
    className?: string;
    "aria-hidden"?: boolean;
  }
  ```
  Uso: `<LucideIcon name="bell" size={20} />`

- **Design notes:**
  - Los path data se obtienen del código fuente de Lucide (licencia ISC — uso permitido).
  - Cada ícono puede tener múltiples elements SVG (`path`, `line`, `circle`, `rect`, `polyline`, `polygon`). El componente renderiza el array de elements.
  - Para íconos con múltiples elements, el `ICON_PATHS` almacena la cadena SVG interior (el contenido del `<svg>`, no el tag externo) y se usa `dangerouslySetInnerHTML` dentro del `<svg>` wrapper.
  - `aria-hidden={true}` por defecto — los íconos son decorativos; el contexto semántico lo dan los labels/tooltips.

---

## Implementation Steps

1. Crear `web/src/components/ds/LucideIcon.tsx`:
   ```tsx
   // Map de name → SVG inner HTML (paths de Lucide)
   const ICON_PATHS: Record<string, string> = {
     "layout-dashboard": `<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/>`,
     "file-pen-line": `<path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/>`,
     "library": `<path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/>`,
     "users": `<path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/>`,
     "bar-chart-3": `<line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>`,
     "bell": `<path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/>`,
     "sparkles": `<path d="M12 3l1.9 5.8H20l-4.9 3.6 1.9 5.7L12 14.6l-5 3.5 1.9-5.7L4 8.8h6.1L12 3z"/>`,
     "log-out": `<path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>`,
     "chevron-right": `<polyline points="9 18 15 12 9 6"/>`,
     "trending-up": `<polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/>`,
     "trending-down": `<polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="17 18 23 18 23 12"/>`,
     "alert-triangle": `<path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>`,
     "calendar-clock": `<path d="M21 7.5V6a2 2 0 00-2-2H5a2 2 0 00-2 2v14a2 2 0 002 2h3.5"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h5"/><circle cx="17.5" cy="17.5" r="4.5"/><path d="M17.5 15.5v2l1 1"/>`,
     "clipboard-check": `<path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><path d="M9 14l2 2 4-4"/>`,
     "mail": `<path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>`,
     "lock": `<rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/>`,
     "graduation-cap": `<path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/>`,
     "minus": `<line x1="5" y1="12" x2="19" y2="12"/>`,
   };

   export default function LucideIcon({
     name, size = 24, color = "currentColor",
     className = "", "aria-hidden": ariaHidden = true,
   }: LucideIconProps) {
     const paths = ICON_PATHS[name];
     if (!paths) return null;
     return (
       <svg
         width={size} height={size} viewBox="0 0 24 24"
         fill="none" stroke={color} strokeWidth={2}
         strokeLinecap="round" strokeLinejoin="round"
         className={className} aria-hidden={ariaHidden}
         dangerouslySetInnerHTML={{ __html: paths }}
       />
     );
   }
   ```
2. Verificar que cada ícono renderiza correctamente en el browser.

---

## Unit Tests

> Sin tests unitarios de lógica — es renderizado de SVG estático.

| # | Verificación | Cómo validar |
|---|-------------|-------------|
| 1 | `<LucideIcon name="bell" size={20} />` renderiza un SVG de 20x20 | Inspección visual |
| 2 | `color="var(--brand)"` aplica el color al stroke | Inspección visual |
| 3 | Ícono desconocido devuelve `null` sin crash | Pasar `name="xxx"` |
| 4 | `aria-hidden={true}` está en el SVG por defecto | DevTools |

---

## Done Criteria

- [x] `web/src/components/ds/LucideIcon.tsx` existe con los 18 íconos necesarios
- [x] El componente recibe `name`, `size`, `color`, `className` como props
- [x] Todos los íconos listados renderizan correctamente
- [x] `aria-hidden={true}` por defecto
- [x] TypeScript no muestra errores en el componente
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-02-shell-layout.md)
