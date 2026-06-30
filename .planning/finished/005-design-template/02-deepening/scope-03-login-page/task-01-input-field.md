# ⚛️ TASK 01 — Primitivos DS de formulario: Input y Field

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-03-login-page.md)

---

## Objective

Implementar los componentes `Input` y `Field` del design system para formularios — un input con soporte de ícono prefijo, estados de foco (anillo verde), error y disabled; y un Field wrapper con label, input y mensaje de error — usando los tokens CSS del DS.

---

## Technical Design

- **Approach:** Componentes controlados (reciben `value` + `onChange`). El foco se implementa con `onFocus`/`onBlur` + estado local para aplicar el box-shadow ring verde (`--ring`). El ícono prefijo se posiciona absolutamente a la izquierda dentro de un contenedor relativo. El error state cambia el borde a `--danger-500`.

- **Affected files / components:**
  - `web/src/components/ds/Input.tsx` (nuevo)
  - `web/src/components/ds/Field.tsx` (nuevo)
  - Actualizar barrel export `web/src/components/ds/index.ts`

- **Interfaces / contracts:**
  ```tsx
  // Input
  interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    icon?: React.ReactNode;      // SVG ícono prefijo (left)
    error?: string;              // mensaje de error (mueve borde a danger)
  }

  // Field
  interface FieldProps {
    label: string;
    htmlFor: string;
    children: React.ReactNode;  // El Input
    style?: React.CSSProperties;
  }
  ```

- **Design notes (Input):**
  - bg: `var(--surface-card)`
  - border: `1px solid var(--border-default)` → hover: `var(--border-strong)` → focus: `var(--border-brand)` + box-shadow: `0 0 0 3px var(--ring)` (anillo verde translúcido)
  - error: border: `1px solid var(--danger-500)` + focus: box-shadow en rojo (usar `var(--danger-200)`)
  - border-radius: `var(--radius-md)`
  - padding: `9px 12px` (con icon: `9px 12px 9px 38px`)
  - font-family: `var(--font-sans)`, font-size: `var(--text-md)` (15px)
  - color: `var(--text-body)`, placeholder: `var(--text-subtle)`
  - disabled: opacity 0.5, cursor not-allowed
  - Transition: `border-color 120ms, box-shadow 120ms`

- **Design notes (Field):**
  - `label`: font-size `var(--text-sm)`, font-weight 500, color `var(--text-body)`, margin-bottom 6px, display block
  - mensaje de error (si `Input` tiene `error`): font-size `var(--text-sm)`, color `var(--danger-600)`, margin-top 4px

---

## Implementation Steps

1. Crear `web/src/components/ds/Input.tsx`:
   - `useState(false)` para `isFocused`.
   - `onFocus={() => setIsFocused(true)}`, `onBlur={() => setIsFocused(false)}`.
   - Calcular border-color y box-shadow en función de `error` e `isFocused`.
   - Si hay `icon`, renderizar en un `<div style={{ position: "relative" }}>` con el icon posicionado absolutamente.

2. Crear `web/src/components/ds/Field.tsx`:
   - Renderizar `<div>` con `<label htmlFor={htmlFor}>` + `{children}`.
   - El mensaje de error no va aquí directamente — el `Input` lo muestra si se le pasa `error`; el `Field` solo agrupa label + input visualmente.

   > **Nota:** Si el `Input` no expone `error` visualmente (solo cambia el borde), añadir el mensaje de error en el `Field` en su lugar — la API es flexible.

3. Actualizar `web/src/components/ds/index.ts` con los nuevos exports.

---

## Unit Tests

| # | Caso | Resultado esperado | Archivo |
|---|------|-------------------|---------|
| 1 | `<Input />` sin focus → borde `--border-default` | Borde correcto | Visual |
| 2 | `<Input />` en focus → borde `--border-brand` + anillo verde | Ring verde visible | Visual |
| 3 | `<Input error="Campo requerido" />` → borde `--danger-500` | Borde rojo-cálido | Visual |
| 4 | `<Input icon={<MailIcon />} />` → ícono visible a la izquierda | Padding-left aumentado | Visual |
| 5 | `<Field label="Correo" htmlFor="em"><Input id="em" /></Field>` → label clickeable | Foco en input al clicar label | Interacción |

---

## Done Criteria

- [x] `Input.tsx` existe con soporte de `icon`, `error`, focus ring verde
- [x] `Field.tsx` existe con `label` y slot para el input
- [x] El foco del input muestra el anillo verde `--ring` (3px)
- [x] El estado error muestra borde `--danger-500`
- [x] Barrel export actualizado
- [x] TypeScript sin errores
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-03-login-page.md)
