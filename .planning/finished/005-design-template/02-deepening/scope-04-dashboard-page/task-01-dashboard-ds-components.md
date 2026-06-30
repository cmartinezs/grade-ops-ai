# ⚛️ TASK 01 — Componentes DS del dashboard: Badge, StatCard, Card

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-04-dashboard-page.md)

---

## Objective

Implementar los tres componentes DS necesarios para el dashboard: `Badge` (etiquetas de tipo y estado con tones de color), `StatCard` (métrica con valor, unidad, delta e iconTone) y `Card` (contenedor con sub-componentes Header/Title/Body/Footer), usando los tokens CSS del design system.

---

## Technical Design

- **Approach:** Componentes TSX en `web/src/components/ds/`. Usan tokens CSS via `style` inline. `Badge` usa forma de píldora (border-radius 999px) con variantes de tone. `StatCard` tiene layout flex con el valor prominente y un ícono de acento. `Card` usa compound pattern (`Card.Header`, `Card.Body`, `Card.Footer`) como sub-componentes.

- **Affected files / components:**
  - `web/src/components/ds/Badge.tsx` (nuevo)
  - `web/src/components/ds/StatCard.tsx` (nuevo)
  - `web/src/components/ds/Card.tsx` (nuevo)
  - Actualizar barrel export `web/src/components/ds/index.ts`

- **Interfaces / contracts:**

  **Badge:**
  ```tsx
  type BadgeTone = "brand" | "gold" | "success" | "warning" | "danger" | "info" | "neutral";
  interface BadgeProps {
    tone?: BadgeTone;       // default: "neutral"
    dot?: boolean;          // círculo de estado izquierda
    children: React.ReactNode;
    className?: string;
  }
  ```

  **StatCard:**
  ```tsx
  type IconTone = "default" | "gold" | "info" | "danger";
  interface StatCardProps {
    label: string;
    value: string | number;
    unit?: string;
    delta?: string;               // e.g. "+0,3" o "-1"
    deltaDir?: "up" | "down";
    iconTone?: IconTone;
    icon?: React.ReactNode;       // LucideIcon opcional (no requerido para la maqueta)
    className?: string;
  }
  ```

  **Card:**
  ```tsx
  // Compound component
  interface CardProps { children: React.ReactNode; className?: string; }
  interface CardHeaderProps { children: React.ReactNode; }
  interface CardTitleProps { children: React.ReactNode; }
  interface CardBodyProps { children: React.ReactNode; }
  interface CardFooterProps { children: React.ReactNode; }
  ```

- **Design notes (Badge):**
  Tokens por tone (bg / color / border):
  | Tone | bg | color | border |
  |------|----|-------|--------|
  | brand | `--sprout-50` | `--sprout-700` | `--sprout-200` |
  | gold | `--gold-50` | `--gold-700` | `--gold-200` |
  | success | `--success-50` | `--success-700` | `--success-200` |
  | warning | `--warning-50` | `--warning-700` | `--warning-200` |
  | danger | `--danger-50` | `--danger-700` | `--danger-200` |
  | info | `--info-50` | `--info-700` | `--info-200` |
  | neutral | `--slate-50` | `--slate-700` | `--slate-200` |
  
  Estilo base: `display:inline-flex; align-items:center; gap:5px; padding:2px 8px; border-radius:999px; font-size:var(--text-xs); font-weight:500; border:1px solid`
  
  Dot: `<span style={{ width:6, height:6, borderRadius:'50%', background:'currentColor', flexShrink:0 }}>`

- **Design notes (StatCard):**
  - Contenedor: bg `var(--surface-card)`, border `1px solid var(--border-subtle)`, radius `var(--radius-lg)`, shadow `var(--shadow-sm)`, padding `20px`
  - Layout: `display:flex; justify-content:space-between; align-items:flex-start`
  - Izquierda: label (text-xs, uppercase, tracking, text-muted) + row con value (font-display, font-700, text-3xl, text-strong) + unit (text-sm, text-muted, margin-left 4px)
  - delta: text-sm, verde si `up`, rojo si `down`, margin-top 4px
  - Ícono (top-right): círculo 36px, bg según tone, ícono LucideIcon centrado
    - default: bg `var(--surface-brand-soft)`, color `var(--brand)`
    - gold: bg `var(--gold-50)`, color `var(--gold-600)`
    - info: bg `var(--info-50)`, color `var(--info-600)`
    - danger: bg `var(--danger-50)`, color `var(--danger-600)`

- **Design notes (Card):**
  - `Card`: bg `var(--surface-card)`, border `1px solid var(--border-subtle)`, radius `var(--radius-lg)`, shadow `var(--shadow-sm)`, overflow `clip`
  - `Card.Header`: padding `16px 20px`, border-bottom `1px solid var(--border-subtle)`, display flex, justify-content space-between, align-items center
  - `Card.Title`: font-display, font-600, font-size `var(--text-lg)` (18px), color `var(--text-strong)`
  - `Card.Body`: padding `16px 20px`
  - `Card.Footer`: padding `12px 20px`, border-top `1px solid var(--border-subtle)`

---

## Implementation Steps

1. Crear `web/src/components/ds/Badge.tsx` con el mapa de tone styles y el componente.

2. Crear `web/src/components/ds/StatCard.tsx`:
   - Mapa de iconTone → colores de bg + ícono default (usar LucideIcon si está disponible).
   - Para la maqueta del dashboard, el ícono es opcional y se puede omitir.

3. Crear `web/src/components/ds/Card.tsx` con el compound pattern:
   ```tsx
   function Card({ children, className }: CardProps) { ... }
   function CardHeader({ children }: CardHeaderProps) { ... }
   function CardTitle({ children }: CardTitleProps) { ... }
   function CardBody({ children }: CardBodyProps) { ... }
   function CardFooter({ children }: CardFooterProps) { ... }

   Card.Header = CardHeader;
   Card.Title = CardTitle;
   Card.Body = CardBody;
   Card.Footer = CardFooter;

   export default Card;
   ```

4. Actualizar el barrel export en `web/src/components/ds/index.ts`.

---

## Unit Tests

| # | Caso | Resultado esperado | Archivo |
|---|------|-------------------|---------|
| 1 | `<Badge tone="success" dot>Corregida</Badge>` → píldora verde con punto | Colores correctos | Visual |
| 2 | `<Badge tone="warning">En revisión</Badge>` → píldora ámbar | Colores ámbar | Visual |
| 3 | `<StatCard label="Por corregir" value={14} iconTone="gold" />` → card con acento dorado | Card correcta | Visual |
| 4 | `<Card><Card.Header><Card.Title>Título</Card.Title></Card.Header></Card>` → card con header | Estructura correcta | Visual |
| 5 | `delta="+0,3" deltaDir="up"` → texto verde | Color verde en delta | Visual |

---

## Done Criteria

- [x] `Badge.tsx` tiene los 7 tones correctamente mapeados
- [x] `Badge` con `dot={true}` muestra el punto de estado
- [x] `StatCard.tsx` muestra label, value, unit, delta y el ícono de acento con tone
- [x] `Card.tsx` implementa compound pattern con Header/Title/Body/Footer
- [x] Barrel export actualizado con Badge, StatCard, Card
- [x] TypeScript sin errores en los 3 componentes
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-04-dashboard-page.md)
