# ⚛️ TASK 02 — Primitivos DS del shell: Button, Avatar, IconButton

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01 (LucideIcon)
> [← scope file](../scope-02-shell-layout.md)

---

## Objective

Implementar los tres componentes primitivos del design system necesarios para el shell del portal docente: `Button` (variantes primary/ghost/outline), `Avatar` (iniciales con color brand-soft) e `IconButton` (botón solo-ícono con aria-label), todos usando los tokens CSS del DS.

---

## Technical Design

- **Approach:** Componentes TSX en `web/src/components/ds/`. Usan `style` inline con tokens CSS (`var(--brand)`, `var(--radius-md)`, etc.) para máxima fidelidad con el DS sin depender de clases Tailwind para el branding. Se aplican event handlers `onMouseEnter/Leave` para el hover (o un enfoque con CSS modules si se prefiere). Cada componente es un thin wrapper — no contiene lógica de negocio.

- **Affected files / components:**
  - `web/src/components/ds/Button.tsx` (nuevo)
  - `web/src/components/ds/Avatar.tsx` (nuevo)
  - `web/src/components/ds/IconButton.tsx` (nuevo)

- **Interfaces / contracts:**

  **Button:**
  ```tsx
  interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: "primary" | "ghost" | "outline";
    size?: "sm" | "md";
    loading?: boolean;
    block?: boolean;            // width: 100%
    iconRight?: React.ReactNode;
  }
  ```

  **Avatar:**
  ```tsx
  interface AvatarProps {
    name: string;               // Genera iniciales (1-2 chars)
    size?: "sm" | "md";         // sm: 32px / md: 40px
    className?: string;
  }
  ```

  **IconButton:**
  ```tsx
  interface IconButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    label: string;              // aria-label + title (tooltip nativo)
    size?: "sm" | "md";         // sm: 32px / md: 40px
    children: React.ReactNode;  // El ícono
  }
  ```

- **Design notes (Button):**
  - `primary`: bg `var(--brand)`, color `#fff`, hover: bg `var(--brand-hover)` + `box-shadow: var(--shadow-brand)`, press: `translateY(1px)`
  - `ghost`: bg `transparent`, color `var(--text-body)`, hover: bg `var(--surface-sunken)`
  - `outline`: border `1px solid var(--border-default)`, bg `transparent`, hover: bg `var(--surface-sunken)`
  - `sm`: height 32px, padding `0 12px`, font-size `var(--text-sm)`
  - `md`: height 40px, padding `0 16px`, font-size `var(--text-md)`
  - Border-radius: `var(--radius-md)` (10px)
  - `loading`: spinner inline (border animated) + `disabled`
  - Transition: `background 120ms, box-shadow 120ms, transform 80ms`

- **Design notes (Avatar):**
  - Iniciales: `name.split(" ").map(w => w[0]).slice(0,2).join("").toUpperCase()`
  - bg: `var(--surface-brand-soft)`, color: `var(--brand)`
  - font: `var(--font-display)`, font-weight 700, font-size proporcional
  - border-radius: 50%

- **Design notes (IconButton):**
  - Cuadrado: `sm` → 32px, `md` → 40px
  - bg: transparent, hover: `var(--surface-sunken)`
  - border: none, border-radius: `var(--radius-md)`
  - `title={label}` para tooltip nativo del browser
  - `aria-label={label}` para accesibilidad

---

## Implementation Steps

1. Crear `web/src/components/ds/Button.tsx`:
   - Definir estilos base + variantes + tamaños como objetos de estilo.
   - Manejar hover con `onMouseEnter/Leave` (setState para isHovered) o con CSS class.
   - El spinner de loading: `<span style={{ width: 14, height: 14, border: "2px solid rgba(255,255,255,0.4)", borderTopColor: "#fff", borderRadius: "50%", display: "inline-block", animation: "spin 0.7s linear infinite" }} />` + keyframe en globals.css.
   - Agregar `@keyframes spin { to { transform: rotate(360deg); } }` en `globals.css`.

2. Crear `web/src/components/ds/Avatar.tsx`:
   - Función para extraer iniciales del nombre.
   - Renderizar un `<div>` con las iniciales centradas.

3. Crear `web/src/components/ds/IconButton.tsx`:
   - `<button title={label} aria-label={label} ...>` con el children.

4. Exportar los tres desde `web/src/components/ds/index.ts` (barrel export).

---

## Unit Tests

| # | Caso | Resultado esperado | Archivo |
|---|------|-------------------|---------|
| 1 | `<Button variant="primary">Guardar</Button>` renderiza con bg verde | bg = `var(--brand)` en computed style | Inspección visual |
| 2 | `<Button loading>` muestra spinner y está disabled | `disabled=true`, spinner visible | Inspección visual |
| 3 | `<Avatar name="Paula Méndez" />` muestra "PM" | Iniciales correctas | Inspección visual |
| 4 | `<Avatar name="Carlos" />` muestra "C" | 1 sola inicial cuando no hay apellido | Inspección visual |
| 5 | `<IconButton label="Cerrar sesión"><LucideIcon name="log-out" /></IconButton>` tiene aria-label | `aria-label="Cerrar sesión"` en DOM | DevTools |

---

## Done Criteria

- [x] `Button.tsx` existe con variantes `primary`, `ghost`, `outline` y tamaños `sm`, `md`
- [x] `Button` con `loading={true}` muestra spinner y tiene `disabled`
- [x] `Avatar.tsx` existe y genera iniciales correctamente para 1 y 2 palabras
- [x] `IconButton.tsx` existe con `aria-label` y `title` del `label` prop
- [x] Barrel export en `web/src/components/ds/index.ts`
- [x] Hover del Button primary muestra shadow verde (`--shadow-brand`)
- [x] TypeScript sin errores en los 3 componentes
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-02-shell-layout.md)
