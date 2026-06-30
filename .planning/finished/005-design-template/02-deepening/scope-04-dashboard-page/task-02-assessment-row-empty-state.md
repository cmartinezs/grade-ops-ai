# ⚛️ TASK 02 — AssessmentRow y DashboardEmptyState DS

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01 (Badge, Card)
> [← scope file](../scope-04-dashboard-page.md)

---

## Objective

Implementar `AssessmentRow` (fila de evaluación para el listado del dashboard, con badge de tipo y estado DS) y `DashboardEmptyState` (empty state branded para cuando no hay evaluaciones), reemplazando los componentes genéricos existentes `AssessmentCard` y `EmptyDashboard`.

---

## Technical Design

- **Approach:** Dos componentes en `web/src/components/dashboard/`. `AssessmentRow` renderiza cada evaluación como una fila de tabla/lista que usa `Badge` DS para el tipo y estado, con hover interactivo. `DashboardEmptyState` es un placeholder centrado con ícono, título y CTA usando componentes DS. Los componentes existentes (`AssessmentCard.tsx`, `EmptyDashboard.tsx`) se mantienen pero ya no se usan en el dashboard (se pueden deprecar o eliminar en un planning futuro).

- **Affected files / components:**
  - `web/src/components/dashboard/AssessmentRow.tsx` (nuevo)
  - `web/src/components/dashboard/DashboardEmptyState.tsx` (nuevo)
  - `web/src/types/assessment.ts` (verificar campos disponibles en `AssessmentSummaryDto`)

- **Interfaces / contracts:**
  ```tsx
  // AssessmentRow
  interface AssessmentRowProps {
    assessment: AssessmentSummaryDto;
    onClick?: () => void;
  }

  // DashboardEmptyState
  // Sin props — es un componente self-contained
  export default function DashboardEmptyState()
  ```

- **Design notes (AssessmentRow):**
  - Layout: `display:grid; grid-template-columns:1fr auto; gap:12px; align-items:center`
  - Padding: `13px 20px`, border-bottom: `1px solid var(--border-subtle)`
  - Hover: `background:var(--surface-sunken)` (con `onMouseEnter/Leave`)
  - Cursor: pointer (futuro: navegar al detalle)
  - **Columna izquierda:**
    - Nombre: font-weight 600, font-size `var(--text-md)`, color `var(--text-strong)`, text-overflow ellipsis
    - Meta row: `<Badge tone={typeTone}>Tipo</Badge>` + texto "Curso · N estudiantes" (text-muted, text-sm)
  - **Columna derecha:** promedio + "promedio" label + `<Badge tone={statusTone} dot>Estado</Badge>` + `<LucideIcon name="chevron-right">`
  - **Mapeo de tipo a tone:**
    ```
    "CERRADA" → tone="brand", label="Cerrada"
    "ABIERTA" → tone="gold", label="Abierta"
    "MIXTA" → tone="info", label="Mixta"
    ```
    > Si los valores del enum en `AssessmentSummaryDto.type` difieren, ajustar el mapeo aquí.
  - **Mapeo de estado a tone:**
    ```
    "DRAFT" → tone="neutral", label="Borrador"
    "OPEN" → tone="info", dot, label="En curso"
    "IN_REVIEW" → tone="warning", dot, label="En revisión"
    "GRADED" → tone="success", dot, label="Corregida"
    "PUBLISHED" → tone="brand", label="Publicada"
    ```
    > Verificar los valores exactos del enum en el backend/types antes de implementar.

- **Design notes (DashboardEmptyState):**
  - Centrado en el área de contenido (`margin: auto`, `text-align: center`)
  - Contenedor: bg `var(--surface-card)`, border `1px dashed var(--border-subtle)`, radius `var(--radius-lg)`, padding `48px 32px`, max-width `480px`
  - Ícono: `<LucideIcon name="file-pen-line" size={40} color="var(--text-subtle)" />`
  - Título: "Aún no tienes evaluaciones" (font-display, font-600, text-xl, text-strong, margin-top 16px)
  - Descripción: "Crea tu primera evaluación y empieza a corregir con IA." (text-muted, text-md, margin-top 8px)
  - CTA: `<Button variant="primary" style={{ marginTop: 24 }}>+ Nueva evaluación</Button>`

---

## Implementation Steps

1. **Verificar `AssessmentSummaryDto`** en `web/src/types/assessment.ts`:
   - Confirmar campos: `id`, `title`, nombre del tipo (¿`type`? ¿`assessmentType`?), estado (¿`status`?), promedio (¿`average`?), conteo de estudiantes (¿`studentCount`?), curso (¿`courseName`?).
   - Registrar en el task cualquier campo faltante como inconsistencia.

2. Crear `web/src/components/dashboard/AssessmentRow.tsx`:
   - Definir `TYPE_MAP` y `STATUS_MAP` con los valores reales del enum.
   - Renderizar la fila según el diseño.

3. Crear `web/src/components/dashboard/DashboardEmptyState.tsx`:
   - Importar `Button` y `LucideIcon` de DS.
   - Renderizar el empty state.

---

## Unit Tests

| # | Caso | Resultado esperado | Archivo |
|---|------|-------------------|---------|
| 1 | `AssessmentRow` con evaluación tipo "CERRADA" | Badge verde "Cerrada" | Visual |
| 2 | `AssessmentRow` con estado "GRADED" | Badge verde con dot "Corregida" | Visual |
| 3 | `AssessmentRow` con estado "IN_REVIEW" | Badge ámbar con dot "En revisión" | Visual |
| 4 | `DashboardEmptyState` renderiza | Ícono + título + descripción + botón CTA | Visual |
| 5 | Hover en `AssessmentRow` | bg `var(--surface-sunken)` | Visual |

---

## Inconsistencies Found

> Registrar aquí si `AssessmentSummaryDto` no tiene los campos esperados.

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | `AssessmentSummaryDto` no tiene campo `type` (CERRADA/ABIERTA/MIXTA) — Badge de tipo omitido en AssessmentRow | `web/src/types/assessment.ts` | ACCEPTED | Badge de tipo no se muestra; solo se muestra Badge de estado |
| 2 | `AssessmentSummaryDto` no tiene campo `average` — se usa "—" como valor placeholder | `web/src/types/assessment.ts` | ACCEPTED | Valor "—" hasta que el API lo soporte |
| 3 | `AssessmentSummaryDto` no tiene campo `courseName` — se usa `submissionCount` como meta | `web/src/types/assessment.ts` | ACCEPTED | Mostrar "N entregas" usando submissionCount |
| 4 | Status real es "GRADING" y "CLOSED" (no "IN_REVIEW"/"GRADED") — mapeo ajustado | `web/src/types/assessment.ts` vs scope design | RESOLVED | GRADING→"En revisión", CLOSED→"Cerrada" |

---

## Done Criteria

- [x] `AssessmentRow.tsx` existe y renderiza nombre, meta (entregas/pendientes), estado (Badge), chevron
- [x] Los mapeos de tipo y estado usan los valores reales del enum `AssessmentSummaryDto` (ajustados por inconsistencia #1-#4)
- [x] Hover de `AssessmentRow` muestra `var(--surface-sunken)`
- [x] `DashboardEmptyState.tsx` existe con el layout DS descrito
- [x] Ambos componentes usan tokens DS, no clases Tailwind genéricas para colores/tipografía
- [x] TypeScript sin errores
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-04-dashboard-page.md)
